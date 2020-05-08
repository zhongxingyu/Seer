 /**
  * RightScale HTTP API client
  */
 package com.rightscale.api.client;
 
 /**
  * @author flaccid
  *
  */
 
 import java.util.List;
 import javax.ws.rs.core.NewCookie;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import com.sun.jersey.api.client.ClientHandlerException;
 import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
 import com.sun.jersey.api.client.filter.LoggingFilter;
 
 public class RightScaleAPIClient {
 	String userName;
 	String userPassword;
 	String accountID;
 	String apiVersion;
 	String endpoint;
 	
 	public static DefaultClientConfig config;
 	public static Client client;
 	public static WebResource resource;
 	public static ClientResponse response;
 	public static WebResource.Builder builder;
 	public static List<NewCookie> cookies;
 
 	public RightScaleAPIClient(String userName, String userPassword, String accountID, String apiVersion, String endpoint) {
 		this.userName = userName;
 		this.userPassword = userPassword;
 		this.accountID = accountID;
 		this.apiVersion = apiVersion;
 		this.endpoint = endpoint;
		
 		config = new DefaultClientConfig();
        client = Client.create(config);
 	}
 	
 	/**
 	 * Logins to the RightScale API
 	 *
 	 */
 	public void login() {
 		client.addFilter(new HTTPBasicAuthFilter(userName, userPassword));
        resource = client.resource("https://"+endpoint+"/api/acct/"+accountID+"/login");
        response = resource.queryParam("api_version", apiVersion).get(ClientResponse.class);
      	cookies = response.getCookies();
 	}
 
 	/**
 	 * Enables system debug output for the client
 	 *
 	 */
 	public void enableDebug() {
        client.addFilter(new LoggingFilter(System.out));		
 	}
 
 	/**
 	 * Generic GET request to the API
 	 *
 	 */
 	public String getRequest(String href) {
 		resource = client.resource("https://"+endpoint+"/api/acct/"+accountID+href);
 		builder = resource.getRequestBuilder();
 		builder.cookie(cookies.get(0));
 		builder.header("X-API-VERSION", apiVersion);
 		response = builder.get(ClientResponse.class);
 		return response.getEntity(String.class);
 	}
 	
 	/**
 	 * Return all RightScale servers from the API
 	 *
 	 */
 	public String getServers() {
 		resource = client.resource("https://"+endpoint+"/api/acct/"+accountID+"/servers");
 		builder = resource.getRequestBuilder();
 		builder.cookie(cookies.get(0));
 		builder.header("X-API-VERSION", apiVersion);
 		response = builder.get(ClientResponse.class);
 		return response.getEntity(String.class);
 	}
     
 	/**
 	 * Destroys the HTTP connection object
 	 *
 	 */
 	public void destroy() {
 		try {        
 			client.destroy();                     
 		}
 		catch (ClientHandlerException e)         
 		{
 			System.out.print("Failed to destroy Jersey client: " + e.getMessage());            
 		}
 	}
 
 	/**
 	 * Returns the username
 	 *
 	 */
    public String getUserName() {
  	   return userName;
     }
     
     /**
 	 * Returns the password
 	 *
 	 */
 	public String getUserPassword() {
 		return userPassword;
 	}
 	
 	/**
 	 * Returns the API version
 	 *
 	 */
 	public String getAPIVersion() {
 		return apiVersion;
 	}
 	
 	/**
 	 * Returns the API endpoint
 	 *
 	 */
 	public String getEndpoint() {
 		return endpoint;
 	}
 }
