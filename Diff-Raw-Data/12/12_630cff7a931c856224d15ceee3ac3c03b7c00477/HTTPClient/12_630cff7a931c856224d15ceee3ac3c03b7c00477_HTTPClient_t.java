 /**
  * 
  */
 package org.humanizer.rating.utils;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 
 /**
  * @author sonhv
  *
  */
 public class HTTPClient {
 	public static String request(String sUrl){
 		StringBuffer ret = new StringBuffer();
 		 try {	
 			 	//sUrl = URLEncoder.encode(sUrl,"UTF-8");
 		    	URL url = new URL(sUrl);
 			    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 			    connection.setDoOutput(true);
 			    connection.setRequestMethod("GET");
 			    connection.setRequestProperty("Authorization",
 			    		"Basic aHVtYW5pemVyOjEyMzQ1Ng==");
 			    connection.connect();
 			    //OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
 			    //writer.write("message=" + message);
 			    //writer.close();    
 		        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
 		            // OK
 		        	//String line = "!23";
 		        	BufferedReader reader =
		    	          new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
 		        	String line;
 		        	while ((line = reader.readLine()) != null) {
 		        		ret.append(line);
 		        	}
 			      reader.close();        	
 		        } else {
 		            //Server returned HTTP error code.
 		        	//String line = "456";
 		        }	    
 		        connection.disconnect();
 		    } catch (MalformedURLException e) {
 		        e.printStackTrace();
 		    } catch (IOException e) {
 		        e.printStackTrace();
 		    }finally{
 		    	
 		    }		
 		
 		return ret.toString();
 		
 	}
 }
