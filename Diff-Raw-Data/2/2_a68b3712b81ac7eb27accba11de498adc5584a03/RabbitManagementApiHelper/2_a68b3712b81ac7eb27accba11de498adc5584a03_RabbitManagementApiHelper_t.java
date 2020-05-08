 package pegasus.eventbus.testsupport;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.httpclient.Credentials;
 import org.apache.commons.httpclient.Header;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.methods.DeleteMethod;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PutMethod;
 import org.apache.commons.httpclient.methods.StringRequestEntity;
 
 import pegasus.eventbus.amqp.ConnectionParameters;
 
 public class RabbitManagementApiHelper {
 	private String hostName; 
 	
 	private String userName = "guest";
 	private String userPassword = "guest";
 	private String virtualHostName;
 	
 	public RabbitManagementApiHelper(String hostName, String virtualHostName) {
 		this.hostName = hostName;
 		this.virtualHostName = virtualHostName;
 	}
 
 	public RabbitManagementApiHelper(ConnectionParameters connectionProperties) {
 		this.hostName = connectionProperties.getHost();
 		this.virtualHostName = connectionProperties.getVirtualHost();
 	}
 
 	public void createVirtualHost(){
 		HttpClient client = getClientForRabbitManagementRestApi();
 
 		PutMethod method = new PutMethod("http://" + hostName + ":55672/api/vhosts/" + urlEncode(virtualHostName));
 		final Header contentType = new Header("content-type","application/json");
 		method.addRequestHeader(contentType);
 		try {
 			client.executeMethod(method);
 			
 			method = new PutMethod("http://" + hostName + ":55672/api/permissions/" +urlEncode(virtualHostName) + "/" + urlEncode(userName));
 			method.setRequestEntity(new StringRequestEntity(
 					"{\"configure\":\".*\",\"write\":\".*\",\"read\":\".*\"}",
 					"application/json",
 					null));
 			client.executeMethod(method);
 		} catch (Exception e) {
 			throw new RuntimeException("Failed to create virtual host. See inner exception for details", e);
 		}
 	}
 
 	public void deleteVirtualHost(){
 		deleteVirtualHost(virtualHostName);
 	}
 
 	public void deleteVirtualHost(String vhostName){
 		
		if(vhostName.equals("/")) return;
 		
 		HttpClient client = getClientForRabbitManagementRestApi();
 
 		DeleteMethod method = new DeleteMethod("http://" + hostName + ":55672/api/vhosts/" + urlEncode(vhostName));
 		final Header contentType = new Header("content-type","application/json");
 		method.addRequestHeader(contentType);
 		try {
 			client.executeMethod(method);
 		} catch (Exception e) {
 			throw new RuntimeException("Failed to delete virtual host. See inner exception for details", e);
 		} 
 	}
 
 	public void deleteAllTestVhosts(){
 		GetMethod vhostGetter = getUrl(getRabbitApiUrl() + "vhosts");
 		String vhostJson ;
 		try {
 			vhostJson = vhostGetter.getResponseBodyAsString();
 		} catch (IOException e) {
 			throw new RuntimeException("Failed to get vhostList. See inner exception for details", e);
 		}
 		ArrayList<String> vhostNames = getNamesFromJson(vhostJson);
 		for(String vhostName : vhostNames)
 			if(vhostName != "/")
 				deleteVirtualHost(vhostName);
 	}
 	
 	public void assertExchangeExists(String exchangeName){
 		assertThatUrlReturnsStatusCode(getUrlForExchange(exchangeName), 200);
 	}
 
 	public void assertExchangeDoesNotExist(String exchangeName){
 		assertThatUrlReturnsStatusCode(getUrlForExchange(exchangeName), 404);
 	}
 	
 	public void assertQueueExists(String queueName){
 		assertThatUrlReturnsStatusCode(getUrlForQueue(queueName), 200);
 	}
 	
 	public void assertQueueDoesNotExists(String queueName){
 		assertThatUrlReturnsStatusCode(getUrlForQueue(queueName), 404);
 	}
 	
 	private void assertThatUrlReturnsStatusCode(String url, int statusCode){
 		GetMethod getExchange = getUrl(url);
 		assertEquals(statusCode, getExchange.getStatusCode());
 	}
 
 	public ArrayList<String> getAllQueueNames(){
 		GetMethod vhostGetter = getUrl(getUrlForQueues());
 		String vhostJson ;
 		try {
 			vhostJson = vhostGetter.getResponseBodyAsString();
 		} catch (IOException e) {
 			throw new RuntimeException("Failed to get vhostList. See inner exception for details", e);
 		}
 		return getNamesFromJson(vhostJson);
 	}
 	
 	public ArrayList<String> GetBindingsForQueue(String queueName, boolean omitBindingToDefaultExchange) {
 		return getBindingsForQueue(queueName, omitBindingToDefaultExchange);
 	}
 
 	public ArrayList<String> getBindingsForQueue(String queueName, boolean omitBindingToDefaultExchange) {
 		GetMethod getBindings = getUrl( getRabbitApiUrl() + "queues/"+urlEncode(virtualHostName)+"/" + urlEncode(queueName) + "/bindings");
 		assertEquals(200, getBindings.getStatusCode());
 		String bindingListJson;
 		try {
 			bindingListJson = getBindings.getResponseBodyAsString();
 		} catch (IOException e) {
 			throw new RuntimeException("Failed to get response body for binding list. See inner exception for details", e);
 		}
 		return getBindingKeysFromJson(bindingListJson, omitBindingToDefaultExchange);
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
 
 	private String getUrlForQueues() {
 		return getRabbitApiUrl() + "queues/"+urlEncode(virtualHostName);
 	}
 	
 	private String getRabbitApiUrl(){
 		return "http://" + urlEncode(hostName) + ":55672/api/";
 	}
 
 	private HttpClient getClientForRabbitManagementRestApi() {
 		HttpClient client = new HttpClient();
 		client.getParams().setAuthenticationPreemptive(true);
 
 		Credentials defaultcreds = new UsernamePasswordCredentials(userName, userPassword);
 		client.getState().setCredentials(new AuthScope(hostName, 55672, AuthScope.ANY_REALM), defaultcreds);
 		return client;
 	}
 	
 	private static String urlEncode(String rawString){
 		try {
 			return java.net.URLEncoder.encode(rawString, "ISO-8859-1");
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(e.getMessage());
 		}
 	}
 }
