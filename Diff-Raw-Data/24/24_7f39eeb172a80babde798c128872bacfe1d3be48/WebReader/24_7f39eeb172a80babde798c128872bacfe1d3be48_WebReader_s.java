 package de.zwinkie.regcheck;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 
 public class WebReader {
	public static void main (String args[]){
		String user = "joejoe";
		System.out.println(isRegistered("http://zwinkie.de/search.php?stext="+user+"&search=Search&method=OR&forum_id=0&stype=members&order=0"));
	}
	
	public static boolean isRegistered(String in_URL)
     {	
         try {
        	URL sourceURL = new URL(in_URL);
             InputStream ins = sourceURL.openStream();
 					
 	        if (ins == null)
 	            return false;
 	        
 	        BufferedReader in = new BufferedReader(new InputStreamReader(ins), 1024);
 	        
 	        char[] copyBuffer = new char[1024];
 	        StringBuffer sb = new StringBuffer();
 	        in.skip(15050);       
 	        sb.append(copyBuffer, 0, in.read(copyBuffer, 0, 1024));  
 
 	        ins.close();
 	    	in.close();
 	    	
 	    	return sb.toString().contains("profile-link");
 		} catch (IOException e) {
 			return false;
 		}
     }
 }
 
