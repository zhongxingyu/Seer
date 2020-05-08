 package com.allplayers.android;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 import org.json.JSONObject;
 
 public class APCI_RestServices
 {
 	public static String user_id = "";
 	
 	public APCI_RestServices()
 	{
 		//Create a trust manager that does not validate certificate chains
 		TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager()
 		{
 			public java.security.cert.X509Certificate[] getAcceptedIssuers()
 			{
 			return null;
 			}
 			
 			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
 			{
 			}
 			
 			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
 			{
 			}
 		}};
 		
 		//Install the all-trusting trust manager
 		try
 		{
 			SSLContext sc = SSLContext.getInstance("SSL");
 			sc.init(null, trustAllCerts, new java.security.SecureRandom());
 			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 		}
 		catch (Exception ex)
 		{
 		}
 		//Now you can access an https URL without having the certificate in the truststore
 	}
 	
 	public static String isLoggedIn()
 	{
 		if(user_id.equals(""))
 		{
 			//return false;
 			return "empty";
 		}
 		
 		//Check an authorized call
 		try
 		{
 			URL url = new URL("https://www.allplayers.com/?q=api/v1/rest/users/" + user_id + ".json");
 			URLConnection connection = url.openConnection();
 			connection.setDoInput(true);
 			InputStream inStream = connection.getInputStream();
 			BufferedReader input = new BufferedReader(new InputStreamReader(inStream));
 			
 			String line = "";
 			String result = "";
 			while((line = input.readLine()) != null)
 			{
 				result += line;
 			}
 			
 			JSONObject jsonResult = new JSONObject(result);
 			String retrievedUUID = jsonResult.getJSONObject("user").getString("uuid");
 			
 			if(retrievedUUID.equals(user_id))
 			{
 				//return true;
 				return user_id;
 			}
 			else //this case should not occur
 			{
 				//return false;
 				return retrievedUUID;
 			}
 		}
 		catch(Exception ex)
 		{
 			System.out.println(ex);
 			//return false;
 			return ex.toString();
 		}
 	}
 	
 	public static String validateLogin(String username, String password)
 	{
 		//Log in
 		try
 		{
 			HttpURLConnection urlConn;
 			DataOutputStream printout;
 			BufferedReader input;
 			
 			URL url = new URL("https://www.allplayers.com/?q=api/v1/rest/users/login.json");
 			urlConn = (HttpURLConnection)url.openConnection();
 			
 			urlConn.setDoInput(true);
 			urlConn.setDoOutput(true);
 			urlConn.setUseCaches(false);
 			urlConn.setRequestMethod("POST");
 			urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
 			
 			printout = new DataOutputStream(urlConn.getOutputStream());
 			
 			//Send POST output.
 			String content = "username=" + URLEncoder.encode(username, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8");
 			printout.writeBytes(content);
 			printout.flush();
 			printout.close();
 			
 			//Get response data.
 			input = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
 			String str;
 			
 			String result = "";
 			while((str = input.readLine()) != null)
 			{
 				result += str;
 			}
 			
 			input.close();
 			return result;
 		}
 		catch(Exception ex)
 		{
 			System.out.println(ex);
 			return ex.toString();
 		}
 	}
 	
 	public static String searchGroups(String search)
 	{
 		//Return all groups that meet search requirement
 		try
 		{
			URL url = new URL("https://www.allplayers.com/?q=api/v1/rest/groups.json?search=\"" + search + "\"");
 			URLConnection connection = url.openConnection();
 			connection.setDoInput(true);
 			InputStream inStream = connection.getInputStream();
 			BufferedReader input = new BufferedReader(new InputStreamReader(inStream));
 			
 			String line = "";
 			String result = "";
 			while((line = input.readLine()) != null)
 			{
 				result += line;
 			}
 			
 			return result;
 		}
 		catch(Exception ex)
 		{
 			System.out.println(ex);
 			return ex.toString();
 		}
 	}
 	
 	public static String getUserGroups()
 	{
 		/*if(!isLoggedIn())
 		{
 			return "not logged in\n\n" + user_id;
 		}*/
 		
 		String test = isLoggedIn();
 		
 		//Return all groups that meet search requirement
 		try
 		{
 			URL url = new URL("https://www.allplayers.com/?q=api/v1/rest/user/" + user_id + "/groups.json");
 			URLConnection connection = url.openConnection();
 			connection.setDoInput(true);
 			InputStream inStream = connection.getInputStream();
 			BufferedReader input = new BufferedReader(new InputStreamReader(inStream));
 			
 			String line = "";
 			String result = "";
 			while((line = input.readLine()) != null)
 			{
 				result += line;
 			}
 			
 			return test + "\n\n" + result;
 		}
 		catch(Exception ex)
 		{
 			System.out.println(ex);
 			return test + "\n\n" + ex.toString();
 		}
 	}
 }
