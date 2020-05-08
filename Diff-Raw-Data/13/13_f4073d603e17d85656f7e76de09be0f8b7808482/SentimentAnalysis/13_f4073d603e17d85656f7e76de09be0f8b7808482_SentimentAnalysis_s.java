 package edu.sjsu.cmpe.bigdata;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.MongoClient;
 
 public class SentimentAnalysis {
 
 	public String executePost(String targetURL, String urlParameters) {
 		URL url;
 		HttpURLConnection connection = null;
 		try {
 			// Create connection
 			url = new URL(targetURL);
 			connection = (HttpURLConnection)url.openConnection();
 			connection.setRequestMethod("POST");
 			connection.setRequestProperty("Content-Type",
 					"application/x-www-form-urlencoded");
 
 			connection.setRequestProperty("Content-Length",
 					"" + Integer.toString(urlParameters.getBytes().length));
 			connection.setRequestProperty("Content-Language", "en-US");
 
 			connection.setUseCaches(false);
 			connection.setDoInput(true);
 			connection.setDoOutput(true);
 
			// Send request
 			DataOutputStream wr = new DataOutputStream(
 					connection.getOutputStream());
 			wr.writeBytes(urlParameters);
 			wr.flush();
 			wr.close();
 
 			// Get Response
 			InputStream is = connection.getInputStream();
 			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
 			String line;
 			StringBuffer response = new StringBuffer();
 			while ((line = rd.readLine()) != null) {
 				response.append(line);
 				response.append('\r');
 			}
 			rd.close();
 			return response.toString();
 
 		} catch (Exception e) {

 			//e.printStackTrace();
 			return "This review is not evaluated";
 
 		} finally {
 
 			if (connection != null) {
 				connection.disconnect();
 			}
 		}
 	}
 }
