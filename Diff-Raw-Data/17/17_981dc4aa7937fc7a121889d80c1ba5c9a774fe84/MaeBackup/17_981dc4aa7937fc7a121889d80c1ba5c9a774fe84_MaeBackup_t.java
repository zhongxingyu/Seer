 /*
  * Before people complain, yes, I know this isn't the "proper" Java coding style.
  * I started writing it in C then switched part way through, and kept with the C style.
  * But it works. And it's one file instead of a gazillion. So bite me. :p
  *  - Mae
  */
 
 package maebackup;
 
 import java.io.*;
 import java.security.*;
 import java.util.*;
 
 import com.ice.tar.*;
 import com.amazonaws.*;
 import com.amazonaws.auth.*;
 import com.amazonaws.services.glacier.*;
 import com.amazonaws.services.glacier.model.*;
 import com.amazonaws.services.glacier.transfer.*;
 
 public class MaeBackup {
 	//public static final String compresscmd = "lrzip -U -L9 -z -v ";
 	public static String[] compresscmd = { "lrzip", "-U", "-L9", "-z", "-v", "%tar%" };
 	public static String vaultname;
 	public static AWSCredentials credentials;
 	public static int chunksize = 1*1024*1024; // Must be a power of 2 and >= 1 MB.
 	public static File cachedir;
 	
 	public static void usage() {
 		System.out.println("Commands:");
 		System.out.println("[f]ull <name> <path>          Creates a full backup");
 		System.out.println("[p]artial <name> <path>       Backup of changes since last full");
 		System.out.println("[i]ncremental <name> <path>   Backup of changes since last full/partial");
 		System.out.println("[u]pload <filename>           Uploads file to Glacier");
 		System.out.println("[d]ownload <filename> [<job>] Starts or completes a download job");
 		System.out.println("[l]ist [<job ID>]             Starts or completes an inventory fetch job");
 		System.out.println("delete <archive key>          Deletes a file from Glacier");
 		System.exit(1);
 	}
 	
 	public static void main(String[] args) {
 		if (args.length < 1 || args.length > 3) usage();
 	
 		String cachename = System.getenv("HOME") + "/.maebackup";
 		cachedir = new File(cachename);
 		if (!cachedir.exists()) cachedir.mkdir();
 
		try {
			Properties props = new Properties();
			props.load(new FileReader(new File(cachedir, "maebackup.properties")));
			vaultname = props.getProperty("vaultname");
			credentials = new BasicAWSCredentials(props.getProperty("awspublic"), props.getProperty("awssecret"));
		} catch (Exception e) {
			System.err.println(cachename+"/maebackup.properties not found or could not be read.");
			System.exit(1);
		}
 		
 		switch (args[0]) {
 		case "f":
 		case "full": {
 			if (args.length != 3) usage();
 			String name = args[1];
 			String tarname = name + "-" + String.format("%tF", new Date());
 
 			LinkedList<File> files = findFiles(args[2]);
 			
 			File tar = new File(tarname+".f.tar");
 			LinkedHashMap<File,String> hashes = archiveAndHash(files, tar);
 			
 			String lrz = compress(tar);
 			upload(lrz);
 			
 			writeHashes(hashes, new File(cachedir, name+".f.sha256"));
 			File partFile = new File(cachedir, name+".p.sha256");
 			if (partFile.exists()) partFile.delete();
 		} break;
 			
 		case "p":
 		case "partial": {
 			if (args.length != 3) usage();
 			String name = args[1];
 			String tarname = name + "-" + String.format("%tF", new Date());
 
 			LinkedHashMap<File,String> fullHashes = loadHashes(new File(cachedir, name+".f.sha256"));
 			
 			LinkedHashMap<File,String> hashes = hashFiles(findFiles(args[2]));
 			
 			LinkedList<File> newFiles = hashDiff(fullHashes, hashes);
 			
 			File tar = new File(tarname+".p.tar");
 			archiveFiles(newFiles, tar);
 			String lrz = compress(tar);
 			upload(lrz);
 			
 			writeHashes(hashes, new File(cachedir, name+".p.sha256"));
 		} break;
 		
 		case "i":
 		case "incremental": {
 			if (args.length != 3) usage();
 			String name = args[1];
 			String tarname = name + "-" + String.format("%tF", new Date());
 
 			LinkedHashMap<File,String> fullHashes = loadHashes(new File(cachedir, name+".f.sha256"));
 			LinkedHashMap<File,String> partHashes = loadHashes(new File(cachedir, name+".p.sha256"));
 			fullHashes.putAll(partHashes);
 			
 			LinkedHashMap<File,String> hashes = hashFiles(findFiles(args[2]));
 			
 			LinkedList<File> newFiles = hashDiff(fullHashes, hashes);
 			
 			File tar = new File(tarname+".i.tar");
 			archiveFiles(newFiles, tar);
 			String lrz = compress(tar);
 			upload(lrz);
 		} break;
 		
 		case "u":
 		case "upload": {
 			if (args.length != 2) usage();
 			upload(args[1]);
 		} break;
 		
 		case "d":
 		case "download": {
 			if (args.length == 2)
 				download(args[1], null);
 			else if (args.length == 3)
 				download(args[1], args[2]);
 			else
 				usage();
 		}
 		
 		case "delete": {
 			if (args.length != 2) usage();
 			delete(args[1]);
 		} break;
 		
 		case "l":
 		case "list": {
 			if (args.length == 1)
 				list(null);
 			else if (args.length == 2)
 				list(args[1]);
 			else
 				usage();
 		} break;
 		
 		default:
 			usage();
 		}
 	}
 
 	public static LinkedList<File> findFiles(String pathName) {	
 		File path = new File(pathName);
 		LinkedList<File> files = new LinkedList<File>();
 		return findFiles(path, files);
 	}
 	public static LinkedList<File> findFiles(File path, LinkedList<File> fileList) {
 		System.out.println("Searching: "+path);
 		File[] files = path.listFiles();
 		for (File file : files) {
 			if (file.isDirectory()) {
 				findFiles(file, fileList);
 			} else if (file.isFile()) {
 				fileList.add(file);
 			}
 		}
 		return fileList;
 	}
 	
 	public static LinkedHashMap<File,String> hashFiles(Collection<File> files) {
 		LinkedHashMap<File,String> hashes = new LinkedHashMap<File,String>();
 		
 		int count = files.size();
 		System.out.println("Hashing "+count+" files");
 		
 		int num = 0;
 		for (File file : files) {
 			if (num++ % (count/10) == 0) { System.out.printf("  %d/%d\n", num, count); }
 			try {
 				MessageDigest md = MessageDigest.getInstance("SHA-256");
 		
 				FileInputStream fis = new FileInputStream(file);
 				byte[] buffer = new byte[65536];
 				int bytes;
 		
 				while ((bytes = fis.read(buffer)) > 0) {
 					md.update(buffer, 0, bytes);
 				}
 		
 				fis.close();
 	
 				byte[] hash = md.digest();
 		
 				StringBuffer sb = new StringBuffer();
 				for (byte b : hash) {
 					sb.append(String.format("%02x", b));
 				}
 				
 				hashes.put(file, sb.toString());
 			} catch (Exception e) { e.printStackTrace(); }
 		}
 		
 		return hashes;
 	}
 	
 	public static LinkedHashMap<File,String> loadHashes(File file) {
 		LinkedHashMap<File,String> hashes = new LinkedHashMap<File,String>();
 		
 		System.out.println("Loading data from "+file);
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(file));
 			String line;
 			while ((line = br.readLine()) != null) {
 				String[] parts = line.split(" ", 2);
 				if (parts.length != 2) continue;
 				hashes.put(new File(parts[1]), parts[0]);
 			}
 			br.close();
 		} catch (Exception e) { e.printStackTrace(); }
 		
 		return hashes;
 	}
 	
 	public static void writeHashes(LinkedHashMap<File,String> hashes, File file) {
 		writeHashes(file, hashes);
 	}
 	public static void writeHashes(File file, LinkedHashMap<File,String> hashes) {
 		System.out.println("Writing data to "+file);
 		try {
 			PrintWriter pw = new PrintWriter(file);
 			for (Map.Entry<File,String> entry : hashes.entrySet()) {
 				pw.println(entry.getValue()+" "+entry.getKey());
 			}
 			pw.close();
 		} catch (Exception e) { throw new RuntimeException(e); }
 	}
 	
 	public static LinkedList<File> hashDiff(LinkedHashMap<File,String> old, LinkedHashMap<File,String> cur) {
 		LinkedList<File> diff = new LinkedList<File>();
 		
 		for (File f : cur.keySet()) {
 			if (!cur.get(f).equalsIgnoreCase(old.get(f))) {
 				diff.add(f);
 			}
 		}
 		
 		return diff;
 	}
 	
 	public static void archiveFiles(Collection<File> files, File archive) {
 		try {
 			TarOutputStream tar = new TarOutputStream(new FileOutputStream(archive));
 			byte[] buffer = new byte[65536];
 			int bytes;
 			int count = files.size();
 			int num = 0;
 			
 			System.out.println("Creating tar file "+archive);
 			for (File f : files) {
 				if (num++ % (count/10) == 0) { System.out.printf("  %d/%d\n", num, count); }
 				FileInputStream fis = new FileInputStream(f);
 				TarEntry entry = new TarEntry(f);
 				tar.putNextEntry(entry);
 				while ((bytes = fis.read(buffer)) > 0) {
 					tar.write(buffer, 0, bytes);
 				}
 				tar.closeEntry();
 				fis.close();
 			}
 			
 			tar.close();
 		} catch (Exception e) { throw new RuntimeException(e); }
 	}
 	public static LinkedHashMap<File,String> archiveAndHash(Collection<File> files, File archive) {
 		LinkedHashMap<File,String> hashes = new LinkedHashMap<File,String>();
 		try {
 			TarOutputStream tar = new TarOutputStream(new FileOutputStream(archive));
 			byte[] buffer = new byte[65536];
 			int bytes;
 			int count = files.size();
 			int num = 0;
 			
 			System.out.println("Hashing and creating tar file "+archive);
 			for (File f : files) {
 				if (num++ % (count/10) == 0) { System.out.printf("  %d/%d\n", num, count); }
 				MessageDigest md = MessageDigest.getInstance("SHA-256");
 
 				FileInputStream fis = new FileInputStream(f);
 				TarEntry entry = new TarEntry(f);
 				tar.putNextEntry(entry);
 				while ((bytes = fis.read(buffer)) > 0) {
 					md.update(buffer, 0, bytes);
 					tar.write(buffer, 0, bytes);
 				}
 				tar.closeEntry();
 				fis.close();
 
 				byte[] hash = md.digest();
 		
 				StringBuffer sb = new StringBuffer();
 				for (byte b : hash) {
 					sb.append(String.format("%02x", b));
 				}
 				hashes.put(f, sb.toString());
 			}
 			
 			tar.close();
 		} catch (Exception e) { throw new RuntimeException(e); }
 		return hashes;
 	}
 	
 	public static String compress(File archive) {
 		try {		
 			String lrzname = archive.toString()+".lrz";
 			compresscmd[compresscmd.length - 1] = archive.toString();
 			System.out.println("Compressing "+archive+" to "+lrzname);
 			Process lrz = new ProcessBuilder(compresscmd).redirectErrorStream(true).start();
 			
 			InputStream is = lrz.getInputStream();
 			int len;
 			byte[] data = new byte[1024];
 			while ((len = is.read(data)) != -1) {
 				System.out.write(data, 0, len);
 				System.out.flush();
 			}
 			lrz.waitFor();
 			return lrzname;
 		} catch (Exception e) { throw new RuntimeException(e); }
 	}
 	
 	public static void upload(String lrzname) {
 		try {	
 			System.out.println("Uploading to Glacier...");
 			ClientConfiguration config = new ClientConfiguration();
 			config.setProtocol(Protocol.HTTPS);
 			AmazonGlacierClient client = new AmazonGlacierClient(credentials, config);
 			
 			File file = new File(lrzname);
 			String archiveid = "";
 			if (file.length() < 5*1024*1024) {
 				System.out.println("File is small, uploading as single chunk");
 				String treehash = TreeHashGenerator.calculateTreeHash(file);
 				InputStream is = new FileInputStream(file);
 				UploadArchiveRequest request = new UploadArchiveRequest(vaultname, lrzname, treehash, is);
 				UploadArchiveResult result = client.uploadArchive(request);
 				archiveid = result.getArchiveId();
 			} else {
 				long chunks = file.length() / chunksize;
 				String chunksizestr = new Integer(chunksize).toString();
 				System.out.println("Starting multipart upload: "+chunks+" full chunks of "+chunksizestr+" bytes");
 
 				InitiateMultipartUploadResult imures = client.initiateMultipartUpload(
 					new InitiateMultipartUploadRequest(vaultname, lrzname, chunksizestr));
 				
 				String uploadid = imures.getUploadId();
 				RandomAccessFile raf = new RandomAccessFile(file, "r");
 				
 				byte[] buffer = new byte[chunksize];
 				
 				for (long x = 0; x < chunks; x++) {
 					System.out.println("Uploading chunk "+x+"/"+chunks);
 					
 					raf.seek(x*chunksize);
 					raf.read(buffer);
 
 					String parthash = TreeHashGenerator.calculateTreeHash(new ByteArrayInputStream(buffer));
 					String range = "bytes "+(x*chunksize)+"-"+((x+1)*chunksize-1)+"/*";
 					
 					client.uploadMultipartPart(new UploadMultipartPartRequest(vaultname, uploadid, parthash, range,
 						new ByteArrayInputStream(buffer)));
 				}
 				
 				if (file.length() > chunks * chunksize) {
 					System.out.println("Uploading final partial chunk");
 					raf.seek(chunks * chunksize);
 					int bytes = raf.read(buffer);
 					
 					String parthash = TreeHashGenerator.calculateTreeHash(new ByteArrayInputStream(buffer, 0, bytes));
 					String range = "bytes "+(chunks*chunksize)+"-"+(file.length()-1)+"/*";
 					
 					client.uploadMultipartPart(new UploadMultipartPartRequest(vaultname, uploadid, parthash, range,
 						new ByteArrayInputStream(buffer, 0, bytes)));				
 				}
 
 				System.out.println("Completing upload");				
 				String treehash = TreeHashGenerator.calculateTreeHash(file);
 				CompleteMultipartUploadResult result = client.completeMultipartUpload(
 					new CompleteMultipartUploadRequest(vaultname, uploadid, new Long(file.length()).toString(), treehash));
 				archiveid = result.getArchiveId();
 			}
 			
 			System.out.println("Uploaded "+lrzname+" to Glacier as ID "+archiveid);
 			
 			File listfile = new File(cachedir, "archives.lst");
 			FileWriter fw = new FileWriter(listfile, true);
 			fw.write(archiveid+" "+lrzname+"\n");
 			fw.close();
 		} catch (Exception e) { throw new RuntimeException(e); }
 		// TODO: Retry failed upload chunks
 	}
 	
 	public static void download(String filename, String jobid) {
 		try {
 			System.out.println("Starting download...");
 			ClientConfiguration config = new ClientConfiguration();
 			config.setProtocol(Protocol.HTTPS);
 			AmazonGlacierClient client = new AmazonGlacierClient(credentials, config);
 			
 			if (jobid == null || jobid == "") {
 				String archiveid;
 				// Yes, this will screw up on actual 138-character file names, but... yeah.
 				if (filename.length() == 138) {
 					archiveid = filename;
 				} else {
 					File listfile = new File(cachedir, "archives.lst");
 					Map<File,String> filemap = loadHashes(listfile);
 					archiveid = filemap.get(filename);
 					if (archiveid == null) {
 						System.err.println("Error: Could not find archive ID for file "+filename);
 						System.exit(1);
 						return;
 					}
 				}
 			
 				InitiateJobResult result = client.initiateJob(
 					new InitiateJobRequest(vaultname,
 						new JobParameters().withType("archive-retrieval").withArchiveId(archiveid)));
 				jobid = result.getJobId();
 				System.out.println("Started download job as ID "+jobid);
 			} else {
 				DescribeJobResult djres = client.describeJob(
 					new DescribeJobRequest(vaultname, jobid));
 				if (!djres.getStatusCode().equals("Succeeded")) {
 					System.out.println("Job is not listed as Succeeded. It is: "+djres.getStatusCode());
 					System.out.println(djres.getStatusMessage());
 					System.exit(2);
 				}
 				long size = djres.getArchiveSizeInBytes();
 				long chunks = size / chunksize;
 				RandomAccessFile raf = new RandomAccessFile(filename, "w");
 				raf.setLength(size);
 				byte[] buffer = new byte[chunksize];
 				
 				for (int x = 0; x < chunks; x++) {
 					System.out.println("Downloading chunk "+x+" of "+chunks);
 					String range = "bytes "+(x*chunksize)+"-"+((x+1)*chunksize-1)+"/*";
 					
 					GetJobOutputResult gjores = client.getJobOutput(
 						new GetJobOutputRequest(vaultname, jobid, range));
 						
 					gjores.getBody().read(buffer);
 					
 					MessageDigest md = MessageDigest.getInstance("SHA-256");
 					md.update(buffer, 0, chunksize);
 	
 					byte[] hash = md.digest();
 		
 					StringBuffer sb = new StringBuffer();
 					for (byte b : hash) {
 						sb.append(String.format("%02x", b));
 					}
 					if (!sb.toString().equalsIgnoreCase(gjores.getChecksum())) {
 						System.err.println("Error: Chunk "+x+" does not match SHA-256.");
 						// TODO: Retry instead of bailing out.
 						System.exit(3);
 					}
 					
 					raf.seek(x * chunksize);
 					raf.write(buffer);
 				}
 				
 				if (size > chunks*chunksize) {
 					System.out.println("Downloading final partial chunk");
 					String range = "bytes "+(chunks*chunksize)+"-"+(size-1)+"/*";
 
 					GetJobOutputResult gjores = client.getJobOutput(
 						new GetJobOutputRequest(vaultname, jobid, range));
 						
 					int bytes = gjores.getBody().read(buffer);
 					
 					MessageDigest md = MessageDigest.getInstance("SHA-256");
 					md.update(buffer, 0, bytes);
 	
 					byte[] hash = md.digest();
 		
 					StringBuffer sb = new StringBuffer();
 					for (byte b : hash) {
 						sb.append(String.format("%02x", b));
 					}
 					if (!sb.toString().equalsIgnoreCase(gjores.getChecksum())) {
 						System.err.println("Error: Final chunk does not match SHA-256.");
 						// TODO: Retry instead of bailing out.
 						System.exit(3);
 					}
 					
 					raf.seek(chunks * chunksize);
 					raf.write(buffer, 0, bytes);
 				}
 				raf.close();
 				
 				String treehash = TreeHashGenerator.calculateTreeHash(new File(filename));
 				if (!treehash.equalsIgnoreCase(djres.getSHA256TreeHash())) {
 					System.err.println("Error: File failed final tree hash check.");
 					System.exit(3);
 				}
 				
 				System.out.println("Download complete.");
 			}
 		} catch (Exception e) { throw new RuntimeException(e); }
 	}
 
 	public static void delete(String archive) {
 		try {	
 			System.out.println("Deleting from Glacier...");
 			ClientConfiguration config = new ClientConfiguration();
 			config.setProtocol(Protocol.HTTPS);
 			AmazonGlacierClient client = new AmazonGlacierClient(credentials, config);
 			client.deleteArchive(new DeleteArchiveRequest(vaultname, archive));
 			System.out.println("Archive deleted.");
 		} catch (Exception e) { throw new RuntimeException(e); }
 	}
 	
 	public static void list(String arg) {
 		try {
 			System.out.println("Listing Glacier vault...");
 			ClientConfiguration config = new ClientConfiguration();
 			config.setProtocol(Protocol.HTTPS);
 			AmazonGlacierClient client = new AmazonGlacierClient(credentials, config);
 			
 			if (arg == null || arg == "") {
 				InitiateJobResult result = client.initiateJob(
 					new InitiateJobRequest(vaultname,
 						new JobParameters().withType("inventory-retrieval")));
 				String jobid = result.getJobId();
 				System.out.println("Started inventory retrival job as ID "+jobid);
 			} else {
 				DescribeJobResult djres = client.describeJob(
 					new DescribeJobRequest(vaultname, arg));
 				if (!djres.getStatusCode().equals("Succeeded")) {
 					System.out.println("Job is not listed as Succeeded. It is: "+djres.getStatusCode());
 					System.out.println(djres.getStatusMessage());
 					System.exit(2);
 				}
 				
 				GetJobOutputResult gjores = client.getJobOutput(
 						new GetJobOutputRequest().withVaultName(vaultname).withJobId(arg));
 				byte[] buffer = new byte[1024];
 				int bytes;
 				while ((bytes = gjores.getBody().read(buffer)) > 0) {
 					System.out.write(buffer, 0, bytes);
 				}
 			}
 		} catch (Exception e) { throw new RuntimeException(e); }
 	}
 }
 
 
 
 // EOF
 
