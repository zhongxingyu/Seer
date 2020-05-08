 package analyser.extractor;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 import com.google.common.collect.HashMultiset;
 import com.google.common.collect.Multiset;
 
 public class ExtractBlogURLS {
 	static String path = "/home/kira/CommonCrawl/URLIndex-Blogger/htmlDump-Total/";
 	 
 	public static Multiset<String>seenUrls = HashMultiset.create();
 	   
 	private static void insertToDB(File file,PreparedStatement pstmt) {
 	   
 		
 	    try{
 	    	BufferedReader br = new BufferedReader(new FileReader(file));
 	    	String line;
 	    	String[] tmp = null;
	    	String profileUrl = null;
 	    	while ((line = br.readLine()) != null) {
 	    		if(line.contains("blogger.com/profile/")){
 	    			tmp = line.split(" ");	
 	    			profileUrl = tmp[0];
 	    			break;
 	    		}
 	    	}	
 	    	if(seenUrls.contains(profileUrl)){
 				System.out.println("Seen url:"+profileUrl);
 			}else{
 				seenUrls.add(profileUrl);
 		    	Document doc = Jsoup.parse(file, "UTF-8", "");		
 			}
 	    	
 	    	br.close();
 					
 	    }catch(Exception e){
 	    	e.printStackTrace();
 	    }
    }
 	        
 	
 
 	private static void crawlDirectoryAndProcessFiles(File directory,PreparedStatement pstmt) {
 	    File[] files = directory.listFiles();
 		for (int i=0;i<files.length;i++) {
 			File file = files[i];
 	    	if (file.isFile()) {
 		        System.out.println("Scanning file :"+file.getName());
     			insertToDB(file, pstmt); 
 	    	}
 	    }
     }
 	
 	
 	private static Connection getDBConnection(){
 		 String url = "jdbc:mysql://localhost:3306/interestDemograph";
 	        String user = "root";
 	        String password = "root";
 	        Connection conn = null; 
 
 	        try {
 	            conn = DriverManager.getConnection(url, user, password);
 	        }catch(Exception e){
 	        	e.printStackTrace();
 	        	
 	        }
 	        return conn;
 	}
 	public static void main(String args[]) throws FileNotFoundException {
 
 		
 	    
 		try {
 			Connection conn = getDBConnection();
 			PreparedStatement pstmt = conn.prepareStatement("INSERT into BlogURLInfo VALUES(?,?,?)");	
 			File folder = new File(path);
 			crawlDirectoryAndProcessFiles(folder,pstmt);
 
 		}
 		 catch (Exception e) {
 			e.printStackTrace();
 			
 		}
 		
 	}
 }
