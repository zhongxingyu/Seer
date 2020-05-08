 package org.hubiquitus.hubotsdk.adapters;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.camel.Exchange;
 import org.apache.camel.Message;
 import org.apache.camel.Processor;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.hubiquitus.hapi.hStructures.HMessage;
 import org.hubiquitus.hubotsdk.AdapterInbox;
 import org.hubiquitus.hubotsdk.adapters.HHttpAdapter.HHttpAdapterRouteBuilder;
 import org.joda.time.DateTime;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class HInstagramRealTimeInbox extends AdapterInbox implements Processor{
 
 	final Logger log = LoggerFactory.getLogger(HInstagramRealTimeInbox.class);
 
 
 	private String host = "0.0.0.0";
 	private int port = 80;
 	private String path = "";
 
 
 	private String clientId;
 	private String clientSecret;
 	private String object;
 	private String aspect;
 	private String objectID;
 	private String verifyToken;
 	private String callbackUrl;
 
 
 	private long lastTime = (new DateTime().getMillis()) /1000;
 
 
 	private static final String INSTAGRAM_SUBSCRIPTION_ENDPOINT = "https://api.instagram.com/v1/subscriptions/";
 
 
 	@Override
 	public void setProperties(JSONObject properties) {
 		if(properties != null){
 			try {
 				if (properties.has("host")){
 					this.host = properties.getString("host");
 				}
 				if (properties.has("port")){
 					this.port = properties.getInt("port");
 				}
 				if (properties.has("path")){
 					this.path = properties.getString("path");
 					if (this.path.contains("?")) {
 						int interrogationIndex = this.path.indexOf("?");
 						this.path = this.path.substring(interrogationIndex, this.path.length());
 					}
 				}
 
 				if (properties.has("clientId")){
 					this.clientId = properties.getString("clientId");
 				}
 				if (properties.has("clientSecret")){
 					this.clientSecret = properties.getString("clientSecret");
 				}
 				if (properties.has("object")){
 					this.object = properties.getString("object");
 				}
 				if (properties.has("aspect")){
 					this.aspect = properties.getString("aspect");
 				}
 				if (properties.has("verifyToken")){
 					this.verifyToken = properties.getString("verifyToken");
 				}
 				if (properties.has("callbackUrl")){
 					this.callbackUrl = properties.getString("callbackUrl");
 				}				
 
 				if (properties.has("object_id")){
 					this.objectID = properties.getString("object_id");
 				}
 
 
 			} catch (JSONException e) {
 				log.warn("message: ", e);
 			}
 		}
 	}
 
 
 	@Override
 	public void start() {
 		String jettyOptions = "matchOnUriPrefix=true";
 		String jettyCamelUri = "jetty:http://" + this.host + ":" + this.port + this.path;
 		jettyCamelUri += "?" + jettyOptions;
 
 		log.info("Starting HttpAdapter with jettyCamelUri : " + jettyCamelUri);
 
 		//add route 
 		HHttpAdapterRouteBuilder routes = new HHttpAdapterRouteBuilder(jettyCamelUri, this);
 		try {
 			camelContext.addRoutes(routes);
 		} catch (Exception e) {
 			log.warn("message: ", e);
 		}
 
 		Thread thread1 = new Thread(new RunnableInstagramThread(), "thread1");
 		thread1.start();
 
 	}
 
 	@Override
 	public void stop() {
 		// Not supported
 	}
 
 
 	class RunnableInstagramThread implements Runnable {
 
 		Thread runner;
 		public RunnableInstagramThread() {
 		}
 		public RunnableInstagramThread(String threadName) {
 			runner = new Thread(this, threadName); 
 			System.out.println(runner.getName());
 			runner.start(); 
 		}
 		public void run() {
 
 			try {
 				// Wait time to build a URL CallBack
 				Thread.sleep(3000);
 				deleteSubscription();
 				log.debug("All last subscription are deleted ...");
 
 				log.info(" Tag subscription params "+setTagSubscriptionParams());
 				postUrl( setTagSubscriptionParams(), INSTAGRAM_SUBSCRIPTION_ENDPOINT );
 				log.info(" Subscription Tag executed with succes ...");				
 
 			} catch (InterruptedException e) {
 				log.error("Thread can't sleep correctelly !!! ");
 			}
 		}
 
 	}
 
 
 	@Override
 	public void process(Exchange exchange) throws Exception {
 		Message in = exchange.getIn();
 		HttpServletRequest request = in.getBody(HttpServletRequest.class);
 		//Get Exchange body
 		byte[] rawBody = in.getBody(byte[].class);
 
 		if (request.getParameter("hub.verify_token") !=null){
 			if (this.verifyToken.equalsIgnoreCase(request.getParameter("hub.verify_token"))){ 
 				if(request.getParameter("hub.challenge") != null){
 					Object body = request.getParameter("hub.challenge");
 					exchange.getOut().setBody(body);
 					log.trace(" Challenge is posted,  his value  :"+request.getParameter("hub.challenge"));
 				}
 			}
 		}
 		if(in.getBody() != null){			 
 			String instagramBody = new String( rawBody );
 			log.debug("Received body value is  :  "+instagramBody);
 
 			if(instagramBody.startsWith("{")){
 				log.info("Instagram response  :"+new JSONObject(instagramBody));
 
 			}else if (instagramBody.startsWith("[{")){
 				// Get the first pagination order by the recent element 
 				JSONArray instagramJSArray = new JSONArray(instagramBody);				
 				long subscriptionID  =  instagramJSArray.getJSONObject(0).getLong("subscription_id");
 				String tag = instagramJSArray.getJSONObject(0).getString("object_id");
 
 				JSONArray data = getInstagramTag(tag);
 				if(data != null){	
 					JSONArray pileTags = empileTag(data);
 
 					if(pileTags !=null){
 						for (int i=(pileTags.length()-1); i >= 0 ; i--){
 							HMessage msg = new HMessage();							
 							msg = transformInstagramToHMessage(pileTags.getJSONObject(i));
 							JSONObject header = new JSONObject();
 							header.put("subscription_id", subscriptionID);
 							msg.setHeaders(header);
 							put(msg);
 
 						}
 					}
 				}
 			}
 		} 
 	}
 
 
 	/**
 	 * This method get the last the recent object from the first Instagram pagination.
 	 * @param tag String object
 	 * @return JSONArray as data : [{},{},{}, ...] recent instagram posted object
 	 */
 	public JSONArray getInstagramTag(String tag){
 		String url = "https://api.instagram.com/v1/tags/"+tag+"/media/recent?client_id="+clientId+"&count=30";
 		String result =  getURL(url);
 		if(result !=null){
 			try {
 				JSONObject json = new JSONObject(result);
 				if(json.has("data")){
 					return ( json.getJSONArray("data"));
 				}
 			} catch (Exception e) {
 				log.error("Error to build a json from a post url, error type : "+e);
 			}
 		}
 		return null;
 	}
 
 
 
 	/**
 	 * Used to build an HMessage, with payload conatain JSONObject received from instagram 
 	 * 
 	 * @param instagramResult JSONObject received from Instagram
 	 * @return HMessage
 	 */
 	public HMessage transformInstagramToHMessage(JSONObject instagramResult){
 
 		HMessage message = new HMessage();
 		message.setAuthor(this.actor);
 		message.setType("HInstagramRT");
 		message.setPublished(new DateTime());
 		message.setPayload(instagramResult);
 		return message; 
 	}
 
 
 	@Override
 	public String toString() {
 		return "HInstagram  [clientId="    + clientId
 				+ ", clientSecret="        + clientSecret
 				+ ", object="  + object
 				+ ", aspect=" + aspect
 				+ ", verifyToken="   + verifyToken
 				+ ", callbackUrl="   + callbackUrl
 				+"]";
 	}
 
 
 
 
 	/**
 	 * This method is used to delete all Instagram subscription.<br>
 	 * It used when we call to stop method. 
 	 */
 	public void deleteSubscription(){
 
 		DefaultHttpClient client = new DefaultHttpClient();		
 		String url = "https://api.instagram.com/v1/subscriptions?object=all&client_id="+this.clientId+"&client_secret="+this.clientSecret;
 
 		HttpDelete delete = new HttpDelete(url);
 
 		try {
 
 			log.debug("Delete  : "+delete + "Header = "+delete.getAllHeaders());
 			HttpResponse response = client.execute(delete);
 			log.info("-----> Delete  response  :"+response);
 
 		} catch (ClientProtocolException e) {
 			log.error(" Error in client Protocol, error type : "+e);
 		} catch (IOException e) {
 			log.error(" Error IO, error type :"+e);
 		}
 	}
 
 	/**
 	 * used to build subscription tag's subscription parameters 
 	 * @return  the params for the Tag's params
 	 */
 	public  ArrayList<BasicNameValuePair> setTagSubscriptionParams(){
 		ArrayList<BasicNameValuePair> nValuePairs = new ArrayList <BasicNameValuePair>();
 
 		nValuePairs.add(new BasicNameValuePair("client_id",     clientId));
 		nValuePairs.add(new BasicNameValuePair("client_secret", clientSecret));
 		nValuePairs.add(new BasicNameValuePair("object", object ));  
 		nValuePairs.add(new BasicNameValuePair("aspect", aspect));
 		nValuePairs.add(new BasicNameValuePair("object_id", objectID));
 		nValuePairs.add(new BasicNameValuePair("verify_token", verifyToken));
 		nValuePairs.add(new BasicNameValuePair("callback_url", callbackUrl)); 
 
 		return nValuePairs;
 	}
 
 	/***
 	 * 
 	 * @param br Buffer 
 	 * @return String 'body page as a strem'
 	 * @throws IOException
 	 */
 	private  String readBuffer(BufferedReader br) throws IOException{
 
 		String data="";
 		String line = br.readLine();
 		while (line!=null) {
 			if (line != null && !line.isEmpty()) {
 				data = data + line;
 			}
 			line = br.readLine();
 		}		
 		return data;
 	}
 
 	/**
 	 * 
 	 * @param myParmas a post parameters ('client_id, client_secret, url_callback, ...')
 	 */
 	public String postUrl(ArrayList<BasicNameValuePair> myParmas, String url){
 
 		DefaultHttpClient client = new DefaultHttpClient();		
 		HttpPost post = new HttpPost(url);
 
 		try {
 			if (myParmas != null){
 				post.setEntity(new UrlEncodedFormEntity(myParmas));
 			}
 			log.debug("post : "+post + "Entity = "+post.getEntity());
 
 			try {
 				HttpResponse response = client.execute(post);
 
 				log.trace("post executed");
 				log.debug("-HTTP Response  :"+response);	
 
 				HttpEntity entity = response.getEntity();
 				log.trace("HttpEntity fetched");			
 				if (entity == null) throw new IOException("No entity");
 				log.trace("HttpEntity not null :)");
 
 				BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));				
 				String answer = readBuffer(br);
 				log.info("- requeste response : "+answer);
 				return answer;
 
 			} catch (ClientProtocolException e) {	
 				log.error("Error during execution, ",e);			
 			} catch (IOException e) {
 				log.error("Error during execution, ",e);			
 			}
 		} catch (Exception e1) {
 			log.debug(" Error on setEntity :",e1);	}
 
 		return null;
 	}
 
 
 
 	/** 
 	 * Used to get data from the url specified on the parameter 
 	 * @param url: String 
 	 * @return body as string
 	 */
 	public String getURL( String url){
 		DefaultHttpClient client = new DefaultHttpClient();		
 		HttpGet get = new HttpGet(url);
 
 		try {
 			try {
 				HttpResponse response = client.execute(get);
 
 				log.trace("Get executed");
 				log.debug("get response  :"+response);	
 
 				HttpEntity entity = response.getEntity();
 				log.trace("HttpEntity fetched");			
 				if (entity == null) throw new IOException("No entity");
 				log.trace("HttpEntity not null :)");
 
 				BufferedReader br = new BufferedReader(new InputStreamReader(
 						entity.getContent(), "UTF-8"));
 
 				return ( readBuffer(br) );
 
 			} catch (ClientProtocolException e) {	
 				log.error("Error during execution, ",e);			
 			} catch (IOException e) {
 				log.error("Error during execution, ",e);			
 			}
 		} catch (Exception e1) {
 			log.debug(" Error on setEntity :",e1);	}
 		return null;
 	}
 
 	/**
 	 * 
 	 * @param data : JSONArray
	 * @return JSONArray (jsonarray of the diffrents tags received between the notification and  rceived last tag )
 	 */
 	public JSONArray empileTag( JSONArray data){
 		JSONArray pileOfTags = new JSONArray();		
 
 		try {
			for( int i=0; i<data.length() || (	 data.getJSONObject(i).getJSONObject("caption").getLong("created_time")  <  lastTime    )  ; i++ ){

				//if you see the strange comparaison, that's because when I have the same value for a and b, in the comparaison if a>b is not verified and it goes in the if.
				if(	 data.getJSONObject(i).getJSONObject("caption").getLong("created_time") >  (lastTime) &&( data.getJSONObject(i).getJSONObject("caption").getLong("created_time") !=  (lastTime))  )
				{	
					pileOfTags.put(data.getJSONObject(i));					
				}else
				{
					break;
				}
 			}
 			// lastTime: used to save the recent time of the object notified.
 			lastTime = data.getJSONObject(0).getJSONObject("caption").getLong("created_time");
 
 			return pileOfTags;
 		} catch (JSONException e) {
 			log.error(" Error to fetch a created time, error type  "+e);
 		}
 		return null;
 	}
 
 }
