 package pegasus.eventbus.services.rabbit.status;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.httpclient.*;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.methods.*;
 
 import pegasus.eventbus.amqp.AmqpConnectionParameters;
 
 public class RabbitManagementApiHelper {
 	private String hostName; 
 	
 	private String userName = "guest";
 	private String userPassword = "guest";
 	private String virtualHostName;
	private HttpClient httpClient;
 	
 	public RabbitManagementApiHelper(String hostName, String virtualHostName) {
 		this.hostName = hostName;
 		this.virtualHostName = virtualHostName;
		setupHttpClient();
 	}
 
 	public RabbitManagementApiHelper(AmqpConnectionParameters connectionProperties) {
		this(connectionProperties.getHost(),connectionProperties.getVHost());
 	}
 	
 	public ArrayList<String> getAllConnectionNames(){
 		return getNamesForUrl(getUrlForConnections());
 	}
 
 	public ArrayList<String> getAllChannelNames(){
 		return getNamesForUrl(getUrlForChannels());
 	}
 	
 	public ArrayList<String> getAllQueueNames(){
 		return getNamesFromJson(getQueuesJson());
 	}
 
 	public String getQueuesJson() {
 		return getResponseStringForUrl(getUrlForQueues());
 	}
 
 	private ArrayList<String> getNamesForUrl(String url) {
 		String json = getResponseStringForUrl(url);
 		return getNamesFromJson(json);
 	}
 
 	protected String getResponseStringForUrl(String url) {
 		GetMethod getter = getUrl(url);
 		String json ;
 		try {
 			json = getter.getResponseBodyAsString();
 		} catch (IOException e) {
 			throw new RuntimeException("Failed to get url: " + url + " See inner exception for details", e);
 		} finally {
 			getter.releaseConnection();
 		}
 		return json;
 		
 	}
 	
 	public ArrayList<String> GetBindingsForQueue(String queueName, boolean omitBindingToDefaultExchange) {
 		return getBindingsForQueue(queueName, omitBindingToDefaultExchange);
 	}
 
 	public ArrayList<String> getBindingsForQueue(String queueName, boolean omitBindingToDefaultExchange) {
 		String url = getRabbitApiUrl() + "queues/"+urlEncode(virtualHostName)+"/" + urlEncode(queueName) + "/bindings";
 		GetMethod getBindings = getUrl( url);
 		try{
 			if(200 != getBindings.getStatusCode()){
 				throw new RuntimeException("Failed to get url:" + url);
 			}
 			String bindingListJson;
 			try {
 				bindingListJson = getBindings.getResponseBodyAsString();
 			} catch (IOException e) {
 				throw new RuntimeException("Failed to get response body for binding list. See inner exception for details", e);
 			}
 			return getBindingKeysFromJson(bindingListJson, omitBindingToDefaultExchange);
 		} finally {
 			getBindings.releaseConnection();
 		}
 	}
 	
 	public String getOverviewJson(){
 		String overViewUrl = getRabbitApiUrl() + "overview";
 		GetMethod getter = getUrl(overViewUrl);
 		try {
 			return getter.getResponseBodyAsString();
 		} catch (IOException e) {
 			throw new RuntimeException("Failed to get vhostList. See inner exception for details", e);
 		} finally {
 			getter.releaseConnection();
 		}
 	}
 	
 	Pattern bindingFinder = Pattern.compile("\"routing_key\":\"(.*?)\"");
 	Pattern bindingFinderThatOmitsDefaultExchange = Pattern.compile("\"source\":(?!\"\").*?\"routing_key\":\"(.*?)\"");
 	/*
 	 * {"source":"",
 	 * 	"vhost":"vhost-name",
 	 * 	"destination":"TestQueue",
 	 * 	"destination_type":"queue",
 	 * 	"routing_key":"TestQueue",
 	 * 	"arguments":{},
 	 * 	"properties_key":"TestQueue"},
 	 * {"source":"TestExchange",
 	 * 	"vhost":"vhost-name",
 	 * 	"destination":"TestQueue",
 	 * 	"destination_type":"queue",
 	 * 	"routing_key":"Topic1",
 	 * 	"arguments":{},
 	 * 	"properties_key":"Topic1"}
 	 */
 	private ArrayList<String> getBindingKeysFromJson(String bindingListJson, boolean omitBindingToDefaultExchange) {
 		Matcher matcher = omitBindingToDefaultExchange 
 				? bindingFinderThatOmitsDefaultExchange.matcher(bindingListJson)
 				: bindingFinder.matcher(bindingListJson);
 		ArrayList<String> bindingKeys = new ArrayList<String>();
 		while (matcher.find())
 				bindingKeys.add(matcher.group(1));
 		
 		return bindingKeys;
 	}
 
 	Pattern nameFinder = Pattern.compile("\"name\":\"(.*?)\"");
 	private ArrayList<String> getNamesFromJson(String json) {
 		Matcher matcher = nameFinder.matcher(json);
 		ArrayList<String> names = new ArrayList<String>();
 		while (matcher.find())
 				names.add(matcher.group(1));
 		
 		return names;
 	}
 
 	private GetMethod getUrl(String url){
 		HttpClient client = getClientForRabbitManagementRestApi();
 		GetMethod getExchange = new GetMethod(url);
 		try {
 			client.executeMethod(getExchange);
 		} catch (Exception e) {
 			throw new RuntimeException("Failed to execute http-get for: " + url +" See inner exception for details", e);
 		}
 		return getExchange;
 	}
 
 	private String getUrlForExchange(String exchangeName) {
 		return getRabbitApiUrl() + "exchanges/"+ urlEncode(virtualHostName)+"/" + urlEncode(exchangeName);
 	}
 	
 	private String getUrlForQueue(String queueName) {
 		return getUrlForQueues() + "/" + urlEncode(queueName);
 	}
 
 	private String getUrlForConnections() {
 		return getRabbitApiUrl() + "connections";
 	}
 	
 	private String getUrlForChannels() {
 		return getRabbitApiUrl() + "channels";
 	}
 
 	private String getUrlForQueues() {
 		return getRabbitApiUrl() + "queues/"+urlEncode(virtualHostName);
 	}
 	
 	private String getRabbitApiUrl(){
 		return "http://" + urlEncode(hostName) + ":55672/api/";
 	}
 
	private void setupHttpClient() {
		MultiThreadedHttpConnectionManager mgr = new MultiThreadedHttpConnectionManager();
		mgr.getParams().setDefaultMaxConnectionsPerHost(50);
		mgr.getParams().setMaxTotalConnections(200);
		httpClient = new HttpClient(mgr);
		httpClient.getParams().setAuthenticationPreemptive(true);
 
 		Credentials defaultcreds = new UsernamePasswordCredentials(userName, userPassword);
		httpClient.getState().setCredentials(new AuthScope(hostName, 55672, AuthScope.ANY_REALM), defaultcreds);
	}

	private HttpClient getClientForRabbitManagementRestApi() {
		return httpClient;
 	}
 	
 	private static String urlEncode(String rawString){
 		try {
 			return java.net.URLEncoder.encode(rawString, "ISO-8859-1");
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(e.getMessage());
 		}
 	}
 }
