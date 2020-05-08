 //BEGIN FILE LobbyClient.java
 package view;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpResponseException;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 
 import controller.User;
 
 //BEGIN CLASS LobbyClient
 public class LobbyClient {
 	
 	private boolean hasBeenActivated;
 	private String userName;
 	private User myUser;
 	private HttpClient httpClient;
 	private String serverLocation;
 	
 	//BEGIN CONSTRUCTOR public LobbyClient(String serverLocation, String newUserName)
 	public LobbyClient(String serverLocation, String newUserName)
 	{
 		this.httpClient = new DefaultHttpClient();
 		this.serverLocation = serverLocation;
 		this.userName = newUserName;
 		this.hasBeenActivated = false;
 	}
 	//END CONSTRUCTOR public LobbyClient(String serverLocation, String newUserName)
 	
 	//BEGIN CONSTRUCTOR public LobbyClient(String serverLocation, User oldUser)
 	public LobbyClient(String serverLocation, User oldUser)
 	{
 		this.httpClient = new DefaultHttpClient();
 		this.serverLocation = serverLocation;
 		this.myUser = oldUser;
 		this.hasBeenActivated = false;
 	}
 	//END CONSTRUCTOR public LobbyClient(String serverLocation, User oldUser)
 	
 	//BEGIN METHOD public List<User> getUserList()
 	public List<User> getUserList()
 	{
 		HttpGet getRequest = new HttpGet(serverLocation);
 		ResponseHandler<String> r = new BasicResponseHandler();
 		try
 		{
 			return XMLDOMReader.convertUserList(this.httpClient.execute(getRequest, r));
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		return null;
 	}
 	//END METHOD public List<User> getUserList()
 	
 	//BEING METHOD public boolean registerUser()
 	public boolean registerUser()
 	{
 		if (!this.hasBeenActivated) 
 		{
 			HttpPost postRequest = new HttpPost(serverLocation);
 			List<NameValuePair> formParams = new ArrayList<NameValuePair>();
 			formParams.add(new BasicNameValuePair("RequestedUserName", this.userName));
 			ResponseHandler<String> r = new BasicResponseHandler();
 			try 
 			{
 				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, "UTF-8");
 				postRequest.setEntity(entity);
 				HttpResponse h = httpClient.execute(postRequest);
 				String s = r.handleResponse(h);
 				this.myUser = User.parseUser(s);
 				return true;
 			} 
 			catch (Exception e) 
 			{
 				System.err.println(e.getMessage());
 				return false;
 			}
 		}
 		else
 		{
 			System.err.println("Nothing?");
 			return false;
 		}
 	}
 	//END METHOD public boolean registerUser()
 	
 	//BEGIN METHOD public boolean registerUser()
 	public boolean keepAlive()
 	{
 		if (this.hasBeenActivated) 
 		{
 			HttpPost postRequest = new HttpPost(serverLocation);
			List<NameValuePair> formParams = new ArrayList<NameValuePair>();
			formParams.add(new BasicNameValuePair("UserToKeepAlive", this.myUser.toString()));
 			ResponseHandler<String> r = new BasicResponseHandler();
 			try 
 			{
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, "UTF-8");
				postRequest.setEntity(entity);
 				return httpClient.execute(postRequest, r).equals("Success");
 			} 
 			catch (Exception e) 
 			{
 				return false;
 			}
 		}
 		else
 		{
 			return false;
 		}
 	}
 	//END METHOD public boolean registerUser()
 	
 	//BEGIN METHOD public boolean registerUser()
 	public int requestGame(User requestee)
 	{
 		if(this.hasBeenActivated)
 		{
 			HttpPost postRequest = new HttpPost(serverLocation);
			List<NameValuePair> formParams = new ArrayList<NameValuePair>();
			formParams.add(new BasicNameValuePair("User", this.myUser.toString()));
			formParams.add(new BasicNameValuePair("RequestedUserName", requestee.toString()));
 			ResponseHandler<String> r = new BasicResponseHandler();
 			try 
 			{
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, "UTF-8");
				postRequest.setEntity(entity);
 				String response = httpClient.execute(postRequest, r);
 				if (response.equals("Success"))
 				{
 					return 0;
 				}
 				else
 				{
 					return Integer.parseInt(response);
 				}
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 		return -1;
 	}
 	//END METHOD public boolean registerUser()
 	
 }
 //END CLASS LobbyClient
 //END FILE LobbyClient.java
