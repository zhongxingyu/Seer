 package a2;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.UnknownHostException;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.zip.GZIPInputStream;
 
 import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
 import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.SequenceFile;
 import org.apache.hadoop.io.SequenceFile.Writer;
 import org.apache.hadoop.io.Text;
 import org.apache.tools.tar.TarEntry;
 import org.apache.tools.tar.TarInputStream;
 
 
 import au.com.bytecode.opencsv.CSVReader;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.MongoClient;
 
 public class Fetcher {
 		
 	private Writer writer;
 	
 	//private static String HPATH = "/usr/local/Cellar/hadoop/1.2.1/libexec/";
 	private static String HPATH = "/home/ubuntu/hadoop/";
 	public Fetcher (){
 	}
 	
 	
 	public static void main(String [] args){
 		if(args.length>0){
 			HPATH=args[0];
 		}
 		Fetcher fetcher = new Fetcher();
 		fetcher.fetch();
 		
 	}
 	
 	public void initWriter(String path) throws IOException{
 		Configuration conf = new Configuration();
 		conf.addResource(new Path(HPATH+"conf/core-site.xml"));
 		conf.addResource(new Path(HPATH+"conf/hdfs-site.xml"));
 	    FileSystem fs = FileSystem.get(conf);
 	    Path seqFilePath = new Path(path);
         this.writer = SequenceFile.createWriter(fs,conf,seqFilePath,Text.class,Text.class);
 	}
 	public void closeWriter(){
 		try {
 			this.writer.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace(System.err);
 		}
 	}
 	
 	public void fetch(){
 		CSVReader reader = null;
 	    ArchiveStringIterator it = null;
 		try{
		initWriter("/files.seq");
 		reader = new CSVReader(new FileReader("projects.csv"));
 	    String [] nextLine;
 	    int i = 0;
 	    reader.readNext();
 	    long time = System.currentTimeMillis();
 	    int bad = 0;
 	    while ((nextLine = reader.readNext()) != null) {
 	    	i++;
 	    	String id = nextLine[0];
 	        String url = nextLine[1];
 	        System.out.println("Working on #"+i+", missed "+bad+" so far.");
 	        
 	        
 	        	System.out.println(id +", "+ url);
 	        	try{
 	    	        it = new ArchiveStringIterator(url+"/tarball/");
 	    			for(String s: it){
 	    				writeHDFS(id,s);
 	    			}
 	    			
 	    		}catch(IOException e){
 	    			e.printStackTrace(System.err);
 	    			bad++;
 	    		}finally {
 	    			IOUtils.closeQuietly(it);
 	    		}
 	        
 	    }
 		}catch(IOException e){
 			e.printStackTrace(System.err);
 		}finally{
 			IOUtils.closeQuietly(reader);
 			
 			closeWriter();
 		}
 	}
 	
 	public void writeHDFS(String key, String value) throws IOException{
 		writer.append(new Text(key), new Text(value));
 	}
 	
 	public DBCursor getRepos() throws UnknownHostException{
 			MongoClient mongo = new MongoClient();
 			DB db = mongo.getDB("github");
 			DBCollection repos = db.getCollection("repos");
 			DBCursor cursor = repos.find(new BasicDBObject("language","Java"));
 			return cursor;
 	}
 	
 	public File cloneRepo(String url, String path){
 		File root = new File(path);
 		BufferedReader reader;
 		final BufferedWriter out;
 		try {
 			if(root.exists() && root.isDirectory()) FileUtils.deleteDirectory(root);
 			System.out.println("URL "+url);
 			//final Process p = Runtime.getRuntime().exec("git clone --depth 1 "+url+" "+path);
 			ProcessBuilder pb = new ProcessBuilder("/usr/bin/git", "clone", "--depth", "1",url,path);
 			pb.redirectErrorStream(true);
 			Process p = pb.start();
 			
 			final BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
 		    StringBuilder sb = new StringBuilder();
 		    char[] cbuf = new char[100];
 		    while (input.read(cbuf) != -1) {
 		        sb.append(cbuf);
 		        
 		        if (sb.toString().contains("Username:")) {
 		        	System.out.println("Oh shit it happened.");
 		        	p.destroy();
 		        	return null;
 		        }
 		        Thread.sleep(1000);
 		    }
 		    System.out.println(sb);
 		    
 			
 			p.waitFor();
 				if(root!=null && root.isDirectory()){
 				 List<File> list = listFiles(root);
 				 for(File file: list){
 					 System.out.println(file.getName());
 				 }
 				}
 				 
 				 
 		
 		} catch (IOException e) {
 			root=null;
 			System.out.println("Weird shit.");
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			root=null;
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		return root;
 	}
 	
 	public static List<File> listFiles(final File folder) {
 		LinkedList<File> files = new LinkedList<File>();
 	    for (final File fileEntry : folder.listFiles()) {
 	        if (fileEntry.isDirectory()) {
 	            files.addAll(listFiles(fileEntry));
 	        } else {
 	        	if(fileEntry.getName().endsWith(".java")){
 	        		files.add(fileEntry);
 	        	}
 	        }
 	    }
 	    return files;
 	}
 	
 	
 }
