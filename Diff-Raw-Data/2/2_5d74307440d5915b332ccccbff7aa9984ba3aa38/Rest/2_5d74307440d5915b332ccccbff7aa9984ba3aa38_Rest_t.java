 package org.opentox.pol;
 
 import java.io.*;
 import java.net.*;
 import org.opentox.pol.httpreturn.*;
 import java.net.URLEncoder;
 import java.util.Properties;
 
 
 public class Rest {
 	
 	private String sso_url = "";
 
 	public URLConnection c;
 	public Rest(){
 		c=null;
 		InputStream fis = null;
 		String propfile = "org/opentox/pol/admin.properties";
 		fis = OpenssoHelper.class.getClassLoader().getResourceAsStream(propfile);
 		Properties config = new Properties();
 		try {
 			config.load(fis);
 			sso_url = config.getProperty("host");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void Connect(URL u) {
 	    try {
 	      c = u.openConnection();
 	    }
 	    catch (IOException e) {
 	      System.out.println("Rest: " + e.getMessage());
 	    }
 	  }
 
 	public void Send(HttpURLConnection urlc, String data) {
 	    urlc.setDoOutput(true);
 	    urlc.setAllowUserInteraction(false);
 	    PrintStream ps = null;
 	    try {
 	      ps = new PrintStream(urlc.getOutputStream());
 	      ps.print(data);
 	    }
 	    catch (IOException e){
 	      System.out.println("Rest: Could not open output stream: " + e.getMessage());
 	    }
 	    finally {
 	      if (ps != null) {
 	        ps.close();
 	      }
 	   }
 	}
 
     public HttpReturn DoIdCall(String subjectid) {
 
     	int status = 0;
     	String string = null;
     	
     	try {
 	    	BufferedReader br = null;
 	    	
 	    	//set data
 	        String data = "subjectid=" + URLEncoder.encode(subjectid.toString(),"UTF-8");
 	        data += "&attributes_names=uid";
 	        
 			//make connection
 			URL url = new URL(sso_url + "/identity/attributes");
 			Connect(url);
 			HttpURLConnection urlc = (HttpURLConnection) c;
 	
 			//use post mode
 			urlc.setDoOutput(true);
 			urlc.setAllowUserInteraction(false);
 	
 			//send query
 			PrintStream ps = new PrintStream(urlc.getOutputStream());
 			ps.print(data);
 			ps.close();
 	
 			//get result
 			br = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
 	        status = urlc.getResponseCode();
 			String l = "";
 			boolean found=false;
 			while ((l=br.readLine())!=null) {
 	            if (l.indexOf("userdetails.attribute.name=uid")!=-1) {
 	            	found=true;
 	            	break;
 	            }
 			}
 			if (found) {
 				l=br.readLine();
 				string=l.substring(l.indexOf('=')+1);
 			}
 			br.close();
 			if (string == null) System.out.println("NAME IS NULL");
     	} 
     	catch (IOException e) {
     	}
     	
         return new HttpReturn(status, string);
         
 	}
     
     public HttpReturn LogOut(String subjectid) {   	
 	   	int status = 0;
 	   	try {
 	    	//set data
 	        String data = "subjectid=" + URLEncoder.encode(subjectid.toString(),"UTF-8");
 	        
 	        //make connection
			URL url = new URL(sso_url + "/identity/logout");
 			Connect(url);
 			HttpURLConnection urlc = (HttpURLConnection) c;
 	
 			//use post mode
 			urlc.setDoOutput(true);
 			urlc.setAllowUserInteraction(false);
 			
 			//send query
 			PrintStream ps = new PrintStream(urlc.getOutputStream());
 			ps.print(data);
 			ps.close();
 			
 			//get result
 	        status = urlc.getResponseCode();        
 	    } 
 		catch (IOException e) {
 		}
 		return new HttpReturn(status, "");  
     }
 }
 
