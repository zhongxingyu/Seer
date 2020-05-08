 import java.io.BufferedInputStream;
 import java.io.BufferedWriter;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import org.eclipse.jgit.diff.DiffEntry;
 import org.eclipse.jgit.diff.DiffEntry.ChangeType;
 import org.eclipse.jgit.diff.DiffFormatter;
 import org.eclipse.jgit.diff.RawTextComparator;
 import org.eclipse.jgit.lib.Constants;
 import org.eclipse.jgit.lib.ObjectId;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.revwalk.RevCommit;
 import org.eclipse.jgit.revwalk.RevWalk;
 import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
 import org.eclipse.jgit.util.io.DisabledOutputStream;
 
 public class Main {
 	
     public static void main(String [] args) {
     	
     	String strPath = "/Users/giovannia/Code/git.repo/amati_test/";
     	String strCommit = "3d32c595a740ab70006cd13247377c57bea8eb9a";
     	
 	    File gitWorkDir = new File(strPath);
 	    File gitDir = new File(gitWorkDir, Constants.DOT_GIT);
 	    try {
 	        FileRepositoryBuilder builder = new FileRepositoryBuilder();
 	        
 	        Repository repository = builder.setGitDir(gitDir)
 	        		.readEnvironment() // scan environment GIT_* variables
 	        		.findGitDir() // scan up the file system tree
 	        		.build();
 	        
 	        RevWalk rw = new RevWalk(repository);
 	        
 	        ObjectId head = repository.resolve(Constants.HEAD);
 	        RevCommit commit = rw.parseCommit(head);
 	        RevCommit parent = rw.parseCommit(ObjectId.fromString(strCommit));
 	        
 	        DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
 	        df.setRepository(repository);
 	        df.setDiffComparator(RawTextComparator.DEFAULT);
 	        df.setDetectRenames(true);
 	        
 	        List<String> files = new ArrayList<String>();
 	        
 	        List<DiffEntry> diffs = df.scan(parent.getTree(), commit.getTree());
 	        for (DiffEntry diff : diffs) {
 	        	String oper = diff.getChangeType().name();
 	        	ChangeType decodeOper = DiffEntry.ChangeType.valueOf(oper);
 	        	String origP = diff.getOldPath();
 	        	String destP = diff.getNewPath();
 	        	
 	        	// System.out.println(MessageFormat.format("{0} {1} {2}", oper, origP, destP));
 	        	
 	        	if (decodeOper == DiffEntry.ChangeType.ADD) {
 	        		System.out.println(MessageFormat.format("{0},{1}", "CP", destP));
 	        		files.add(destP);
 	        	} else if (decodeOper == DiffEntry.ChangeType.DELETE) {
 	        		System.out.println(MessageFormat.format("{0},{1}", "RM", origP));
 	        	} else if (decodeOper == DiffEntry.ChangeType.MODIFY) {
 	        		System.out.println(MessageFormat.format("{0},{1}", "CP", destP));
 	        		files.add(destP);
 	        	} else if (decodeOper == DiffEntry.ChangeType.RENAME) {
 	        		System.out.println(MessageFormat.format("{0},{1}", "RM", origP));
 	        		System.out.println(MessageFormat.format("{0},{1}", "CP", destP));
 	        		files.add(destP);
 	        	} else if (decodeOper == DiffEntry.ChangeType.COPY) {
 	        		
 	        	} else {
 	        		
 	        	}
 	        }
 	        
 	        byte[] zip = zipFiles(gitWorkDir, files);
 	        FileOutputStream fos = new FileOutputStream("last.zip");
 	        fos.write(zip);
	        fos.flush();
 	        fos.close();
 	        
 	    } catch (Exception e) {
 	    	System.out.println("error: " + e.getMessage());
 	    }
 	    
     }
     
     /**
      * Compress the given directory with all its files.
      */
     private static byte[] zipFiles(File directory, List<String> files) throws IOException {
     	System.out.println("Make zip file");
     	
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ZipOutputStream zos = new ZipOutputStream(baos);
         byte bytes[] = new byte[2048];
 
         for (String fileName : files) {
         	System.out.println("Add to zip -> " + directory.getPath() + File.separator + fileName);
         	
             FileInputStream fis = new FileInputStream(directory.getPath() + File.separator + fileName);
             BufferedInputStream bis = new BufferedInputStream(fis);
 
             zos.putNextEntry(new ZipEntry(fileName));
 
             int bytesRead;
             while ((bytesRead = bis.read(bytes)) != -1) {
                 zos.write(bytes, 0, bytesRead);
             }
             zos.closeEntry();
             bis.close();
             fis.close();
         }
         zos.flush();
         baos.flush();
         zos.close();
         baos.close();
 
         return baos.toByteArray();
     }
     
 }
 
