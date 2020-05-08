 /**
  * OOSE Project - Group 4
  * HttpGetWrapper.java
  */
 package edu.jhu.twacker.model.query;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.ParseException;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.util.EntityUtils;
 
 /**
  * This class is used to wrap the <code>HttpGet</code> class from the
  * Apache Http Client library to make http requests easier to manage.
  * 
  * @author Daniel Deutsch
  */
 public class HttpGetWrapper
 {
 	/**
 	 * The url that will be queried for a response. 
 	 */
 	private String url;
 	
 	/**
 	 * The <code>HttpClient</code> client to perform the get.
 	 */
 	private DefaultHttpClient client;
 	
 	/**
 	 * The <code>HttpGet</code> request.
 	 */
 	private HttpGet get;
 	
 	/**
 	 * The <code>HttpResponse</code> request.
 	 */
 	private HttpResponse response;
 	
 	/**
 	 * The constructor for the class.
 	 * @param url The url that should be queried.
 	 */
 	public HttpGetWrapper(String url)
 	{
 		// comment this section if you are running this locally
//		this.url = url;
//		HttpParams httpParams = new BasicHttpParams();
//		ClientConnectionManager connectionManager = new GAEConnectionManager();
//		this.client = new DefaultHttpClient(connectionManager, httpParams);
//		this.get = new HttpGet(this.url);
		
		// uncomment this section if you are running this locally
 		this.url = url;
		
		this.client = new DefaultHttpClient();
 		this.get = new HttpGet(this.url);
 	}
 	
 	/**
 	 * Sets the authorization for the <code>DefaultHttpClient</code> so that the 
 	 * Streaming API can be accessed. The scope should be AuthScope.ANY right now, and 
 	 * the username and password should be the username and password of a Twitter account.
 	 * 
 	 * @param scope The scope, AuthScope.ANY.
 	 * @param username A Twitter account username.
 	 * @param password The password for the account.
 	 */
 	public void setCredentials(AuthScope scope, String username, String password)
 	{
 		this.client.getCredentialsProvider().setCredentials(scope, new UsernamePasswordCredentials(username, password));
 	}
 	
 	/**
 	 * Executes the query for the object's url.
 	 * @return The result from the http get.
 	 */
 	public String get()
 	{		
 		try
 		{
 			return EntityUtils.toString(this.execute());
 		}
 		catch (ParseException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/**
 	 * Executes the get and returns the entity with has the resulting
 	 * content of the request. The content can be used to get the StreamReader
 	 * to get the text of the result.
 	 * 
 	 * @return The entity with the content of the request.
 	 */
 	private HttpEntity execute()
 	{
 		try
 		{
 			this.response = this.client.execute(this.get);
 			HttpEntity entity = response.getEntity();
 			
 			return entity;
 		}
 		catch (ClientProtocolException e)
 		{
 			e.printStackTrace();
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * Gets the <code>BufferedReader</code> that will contain the results of the
 	 * Streaming HttpGet request. This method should be used when interacting with the
 	 * Twitter Streaming API. When you read from the reader, it will return the
 	 * results as they come in from the API.
 	 * 
 	 * @return The reader.
 	 */
 	public BufferedReader getReader()
 	{
 		HttpEntity entity = this.execute();
 		try
 		{
 			return new BufferedReader(new InputStreamReader(entity.getContent()));
 		}
 		catch (IllegalStateException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
