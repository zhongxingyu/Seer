 package tengen;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.MongoClient;
 
 public class SentimentalAnalysis {
 
 	public String excutePost(String targetURL, String urlParameters) {
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
 	
 	public static void main(String args[]) throws UnknownHostException
 	{
 		MongoClient client = new MongoClient();
 		DB courseDB = client.getDB("codeassassins");
 		DBCollection collection = courseDB.getCollection("bigdata");
 		
 		// Query for fetching reviews for a particular restaurant
 		//DBObject query = new BasicDBObject("type","review").append("business_id", "VFslQjSgrw4Mu5_Q1xk1KQ");
 		DBObject query = new BasicDBObject("type","review").append("business_id", "LjOIxpH-89S18WI1ktmPBQ");
 		
 		DBCursor cursor = collection.find(query);
 
 		SentimentalAnalysis builder = new SentimentalAnalysis();
 		
 		// URL for using sentigem API for sentimental analysis
 		String baseurl = "https://api.sentigem.com/external/get-sentiment?api-key=c7b7029934a1159ae3e2a9c3f80d3f99PiXQg6kuBN_0dp2Z8E5lrWTD4-oAqJbY&text=";
 				
 		String param = "";
 		String response = "";
 		int neutral=0, negative=0, positive=0,notEval=0;
 		
 		
 		while(cursor.hasNext()){
 			
 			DBObject cur = cursor.next();
 			String userReview = (String) cur.get("text");
			
			String finalurl = baseurl+userReview;
 			response = builder.excutePost(finalurl,param);
 			System.out.print(response+"\n");
 			if (response.contains("neutral")){
 				neutral++;
 			}
 			else if (response.contains("positive")){
 				positive++;
 			}
 			else if (response.contains("negative")){
 				negative++;
 			}
 			else if (response.equalsIgnoreCase("This review is not evaluated")){
 				notEval++;
 			}
						
 		}
 		
 		System.out.println("Number of neutral reviews       : "+ neutral);
 		System.out.println("Number of positive reviews      : "+ positive);
 		System.out.println("Number of negative reviews      : "+ negative);
 		System.out.println("Number of reviews not evaluated : "+ notEval);
 
 	}
 }
