 /**
  * Wikidot API Open Source Project
  * Java API library
  * 
  * Lead developer: Shane Smith (leiger)
  * Developer Website: www.shane-smith.com
  * Project Website: api-java.wikidot.com
  * 
  * Licensed under GNU GPL v3
  **/
 
 package com.shanesmith.wikidot;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.apache.xmlrpc.client.XmlRpcClient;
 import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
 import org.apache.xmlrpc.client.XmlRpcSun15HttpTransportFactory;
 
 /**
  * Open-source implementation of the Wikidot API in Java
  * @author Shane Smith (leiger)
  */
 public class WikidotAPI
 {
 	private String user;
 	private String key;
 	private final String SERVER_URL = "https://www.wikidot.com/xml-rpc-api.php";
 	private XmlRpcClientConfigImpl config;
 	private XmlRpcClient client;
 	
 	/**
 	 * Get the XML-RPC client
 	 * @return XmlRpcClient object
 	 */
 	public XmlRpcClient getClient()
 	{
 		return client;
 	}
 	
 	/**
 	 * WikidotAPI constructor
 	 * @param applicationName Application name to send to API (as user)
 	 * @param apiKey Wikidot API key to send to API (as password)
 	 * @throws MalformedURLException If there is a problem with the URI used to access the Wikidot API
 	 */
 	public WikidotAPI(String applicationName, String apiKey) throws MalformedURLException
 	{
 		// Store variables
 		this.user = applicationName;
 		this.key = apiKey;
 		
 		// Set client configuration options
 		config = new XmlRpcClientConfigImpl();
 		config.setServerURL(new URL(SERVER_URL));
 		config.setBasicUserName(user);
 		config.setBasicPassword(key);
 		config.setEnabledForExtensions(true);
 		config.setConnectionTimeout(10 * 1000);
 		config.setReplyTimeout(30 * 1000);
 		
 		// Initialise the client object
 		client = new XmlRpcClient();
 		client.setTransportFactory(new XmlRpcSun15HttpTransportFactory(client));
 		client.setConfig(config);
 	}
 	
 	/**
 	 * Private helper method that is used internally
 	 * @param method Method name
 	 * @param params Parameters
 	 * @return Returned data
 	 * @throws XmlRpcException If there is an error during the API call
 	 */
	private Object pushToAPI(String method, Object[] params) throws XmlRpcException
 	{
 		return (Object) client.execute(method, params);
 	}
 	
 	/**
 	 * Get a list of all valid API methods (system.listMethods)
 	 * @return List of all valid API methods
 	 * @throws XmlRpcException If there is an error during the API call
 	 */
 	public String[] getMethodList() throws XmlRpcException
 	{
 		// RPC
 		Object[] result = (Object[]) pushToAPI("system.listMethods", (Object[])null);
 		
 		// Convert result to a String[]
 		String[] methodList = new String[result.length];
 		for (int i=0; i<result.length; i++)
 		{
 			methodList[i] = (String) result[i];
 		}
 		
 		// Return the result
 		return methodList;
 	}
 	
 	/**
 	 * Implementation of categories.select
 	 * @param site Wikidot site unix name
 	 * @return A list of categories for the requested site
 	 * @throws XmlRpcException If there is an error during the API call
 	 */
 	public String[] getCategoriesList(String site) throws XmlRpcException
 	{
 		// RPC
		Object[] result = (Object[]) pushToAPI("categories.select", site);
 		
 		// Convert result to a String[]
 		String[] categoryList = new String[result.length];
 		for (int i=0; i<result.length; i++)
 		{
 			categoryList[i] = (String) result[i];
 		}
 		
 		// Return the result
 		return categoryList;
 	}
 	
 	public void getPagesList()
 	{
 		// TODO: Implement pages.select
 	}
 	
 	public void getTagsList()
 	{
 		// TODO: Implement tags.select
 	}
 	
 	public void getPagesMeta()
 	{
 		// TODO: Implement pages.get_meta
 	}
 	
 	public void getPage()
 	{
 		// TODO: Implement pages.get_one
 	}
 	
 	public void setPage()
 	{
 		// TODO: Implement pages.save_one
 	}
 	
 	public void getFilesList()
 	{
 		// TODO: Implement files.select
 	}
 	
 	public void getFilesMeta()
 	{
 		// TODO: Implement files.get_meta
 	}
 	
 	public void getFile()
 	{
 		// TODO: Implement files.get_one
 	}
 	
 	public void getPostsList()
 	{
 		// TODO: Implement posts.select
 	}
 	
 	public void getPostsMeta()
 	{
 		// TODO: Implement posts.get
 	}
 	
 	/**
 	 * Implementation of users.get_me
 	 * @return User details
 	 * @throws XmlRpcException If there is an error during the API call
 	 */
 	public Map<String, Object> getUser() throws XmlRpcException
 	{
 		// RPC
 		@SuppressWarnings("unchecked")
 		Map<String, Object> result = (HashMap<String, Object>) pushToAPI("users.get_me", (Object[])null);
 		
 		// FORMAT: { name, title, id }
 		return result;
 	}
 }
