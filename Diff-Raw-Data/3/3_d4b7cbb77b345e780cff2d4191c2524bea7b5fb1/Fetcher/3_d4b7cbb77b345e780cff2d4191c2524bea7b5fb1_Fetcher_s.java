 package a2;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.UnknownHostException;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.SequenceFile;
 import org.apache.hadoop.io.SequenceFile.Writer;
 import org.apache.hadoop.io.Text;
 import org.eclipse.jgit.api.CloneCommand;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.api.errors.GitAPIException;
 import org.eclipse.jgit.api.errors.InvalidRemoteException;
 import org.eclipse.jgit.api.errors.TransportException;
 import org.eclipse.jgit.errors.UnsupportedCredentialItem;
 import org.eclipse.jgit.transport.CredentialItem;
 import org.eclipse.jgit.transport.CredentialsProvider;
 import org.eclipse.jgit.transport.URIish;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.MongoClient;
 
 public class Fetcher {
 		
 	private Writer writer;
 	
 	private static String HPATH = "/usr/local/Cellar/hadoop/1.2.1/libexec/conf/";
 	
 	public Fetcher (){
 	}
 	
 	
 	public static void main(String [] args){
 		if(args.length>0){
 			HPATH=args[0];
 		}
 		Fetcher fetcher = new Fetcher();
 		try {
 			fetcher.fetch();
 		} catch (IOException e) {
 			System.err.println("Crazy fucking bullshit.");
 			e.printStackTrace(System.err);
 		}
 	}
 	
 	public void initWriter(String path) throws IOException{
 		Configuration conf = new Configuration();
 		
 		conf.addResource(new Path(HPATH+"core-site.xml"));
 		conf.addResource(new Path(HPATH+"hdfs-site.xml"));
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
 	
 	public void fetch() throws IOException{
 		DBCursor cursor = null;
 		
 		try{
 			cursor = getRepos();
 			initWriter("/test-job");
 			int i = 0;
 			while(cursor.hasNext() && i<5){
 				DBObject repo = cursor.next();
 				System.out.println(repo);
 				File root = cloneRepo(repo.get("clone_url").toString(), "/tmp/repo");
 				if(root==null)continue;
 				i++;
 
 				List<File> files = listFiles(root);
 				for(File file: files){
 					String contents = FileUtils.readFileToString(file);
 					writeHDFS(repo.get("id").toString(), contents);
 				}
 			}
 			
 		}catch(UnknownHostException e){
 			System.err.println("Couldn't find database probably");
 			e.printStackTrace(System.err);
 		} catch (IOException e) {
 			System.err.println("Probably some sort of trouble with Hadoop.");
 			e.printStackTrace(System.err);
 		}finally{
 			if(cursor!=null) cursor.close();
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
 			System.out.println("Weird shit.");
 			e.printStackTrace();
 		} catch (InterruptedException e) {
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
