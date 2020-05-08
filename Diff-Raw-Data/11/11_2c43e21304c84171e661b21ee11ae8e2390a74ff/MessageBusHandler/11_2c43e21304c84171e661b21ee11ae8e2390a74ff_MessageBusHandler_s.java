 package models.cmu.sv.sensor;
 
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.HTTP;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import play.Logger.ALogger;
 
 public class MessageBusHandler {
 	private static String serverUrl = "http://message-bus-soc.herokuapp.com/";
 	
 	private String topic = "";
 	private ArrayList<NameValuePair> metaData = null;
 	private ArrayList<NameValuePair> properties = null;
 	
 	public MessageBusHandler(){
 		this(null);
 	}
 	
 	public MessageBusHandler(String topic){
 		this.topic = topic;
 		this.metaData = new ArrayList<NameValuePair>();
 		this.properties = new ArrayList<NameValuePair>();
 		
 	}
 	
 	private JSONObject toJSON(List<NameValuePair> source){
 		JSONObject json = new JSONObject();
 		Iterator<NameValuePair> it = source.iterator();
 		while(it.hasNext()){
 			NameValuePair pair = it.next();
 			try {
 				json.append(pair.getName(), pair.getValue());
 				
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				continue;
 			}
 		}
 		return json;
 	}
 	public boolean isTopicExists(String topic){
 		String path = "catalog/topics";
 		HttpClient client = new DefaultHttpClient();
 		HttpGet get = new HttpGet(serverUrl + path);
 		StringBuilder builder = null;
 		try {
 			
 			HttpResponse response = client.execute(get);
 			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
 			builder = new StringBuilder();
 			for (String line = null; (line = reader.readLine()) != null;) {
 			    builder.append(line).append("\n");
 			}
 			
 			//parse Json file
 			JSONArray jsonArray = new JSONArray(builder.toString());
 			for(int i=0; i< jsonArray.length(); i++){
 				//System.err.println(jsonArray.getJSONObject(i).getString("topic"));
 				if(jsonArray.getJSONObject(i).getString("topic").equals(topic)){
 					return true;
 				}
 			}
 			return false; 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		} catch (JSONException e) {
 			System.err.println(builder.toString());
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		} 
 		
 	}
 	private JSONObject addFieldToTopic(String fieldName, String fieldType){
 		JSONObject jsonMeta = new JSONObject();
 	    try {
 			jsonMeta.put("field", fieldName);
 			 jsonMeta.put("type", fieldType);
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	    return jsonMeta;
 	}
 	public boolean addTopic(String topic){
 		String path = "catalog/topics";
 		HttpClient client = new DefaultHttpClient();
 		 HttpPost post = new HttpPost(serverUrl + path);
 		 try {
 
 		      JSONObject topicObject = new JSONObject();
 		      topicObject.put("topic", topic);
 		      
 		      JSONArray jsonMetaArray = new JSONArray();
 		      jsonMetaArray.put(addFieldToTopic("value", "Float"));
 		      jsonMetaArray.put(addFieldToTopic("deviceId", "String"));
 		      jsonMetaArray.put(addFieldToTopic("timeStamp", "Integer"));
 		      		      
 		      topicObject.put("metadata", jsonMetaArray);
 		     
 		      StringEntity entity = new StringEntity(topicObject.toString(), HTTP.UTF_8);
 		      //System.err.println(topicObject.toString());
 		      entity.setContentType("application/json");
 		      
 		      post.setEntity(entity);
 		      
 		     
 		      HttpResponse response =client.execute(post); 
 		      
 		      //Reading Content
 		      String output = "";
 		      String line = "";
 		      BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 		      while ((line = rd.readLine()) != null) {
 		    	  output += line;
 		      }
 		      if(output.equals("")) return true;
 		      
 		      
 		 }
 		 catch(IOException e){
 			 e.printStackTrace(); 
 		 } catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		 return false;
 	}
 	private String createPublishData(SensorReading reading){
 		StringBuilder builder = new StringBuilder();
 		builder.append("deviceId:");
 		builder.append(reading.getDeviceId());
 		builder.append("|");
 		builder.append("timeStamp:");
 		builder.append(reading.getTimeStamp());
 		builder.append("|");
 		builder.append("value:");
 		builder.append(reading.getValue());
 		return builder.toString(); 
 	}
 	public boolean publishToListener(String topic, String deviceID, int value){
 		String url = "http://message-peer-listener.herokuapp.com/publish";
 		HttpClient client = new DefaultHttpClient();
 		
 		try {
 			url += "?topic=" + topic + "&metadata=" + URLEncoder.encode("id:"+ deviceID + "|value:" + String.valueOf(value), "UTF-8");
 			System.out.println(url);
 			HttpGet get = new HttpGet(url);
 			
 		     HttpResponse response;
 			response = client.execute(get);
 			 String output = "";
 		      String line = "";
 		      BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 		      while ((line = rd.readLine()) != null) {
 		    	  output += line; 
 		      }
 		      if(output.equals("published successfully")) return true;
 		      System.err.println(output);
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 	    return false; 
 	      //Reading Content
 	    
 	}
 	public boolean publish(SensorReading reading){
 		if(!isTopicExists(reading.getSensorType())){
 			addTopic(reading.getSensorType());
 		}
 		String serverUrl = "http://message-peer-listener.herokuapp.com/";
 		
 		
 		
 		String path = "publish";
 		 HttpClient client = new DefaultHttpClient();
 
 		 try {
 			 
 			 String url = serverUrl + path + "?topic=" + URLEncoder.encode(reading.getSensorType(),"UTF-8") + "&metadata=" + URLEncoder.encode(createPublishData(reading), "UTF-8");
 			 
 			 ALogger log = play.Logger.of(MessageBusHandler.class);
 			 //System.err.println(serverUrl+path+queryString);
 			 System.out.println(url);
 			 HttpGet get = new HttpGet(url);
 		     HttpResponse response = client.execute(get); 
 		      
 		      //Reading Content
 		     String output = "";
 		      String line = "";
 		      BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 		      while ((line = rd.readLine()) != null) {
 		    	  output += line; 
 		      }
 		      if(output.equals("published successfully")) return true;
 		      System.err.println(output);
 		 }
		 catch(IOException e){
 			 e.printStackTrace();
 		 } 
 		 
 		return false;
 		 
 		 
 	}
 }
