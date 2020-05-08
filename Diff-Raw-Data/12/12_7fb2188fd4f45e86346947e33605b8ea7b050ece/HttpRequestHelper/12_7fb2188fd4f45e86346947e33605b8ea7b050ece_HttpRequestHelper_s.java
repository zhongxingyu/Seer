 package ru.terra.spending.core.network;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.ConnectException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import ru.terra.spending.R;
 import ru.terra.spending.core.constants.Constants;
 import ru.terra.spending.util.Logger;
 import ru.terra.spending.util.SettingsUtil;
 import android.app.Activity;
 
 //Синхронные вызовы rest сервисов, вызывать только внутри асинхронной таски
 public class HttpRequestHelper
 {
 	private static final int IMAGE_MAX_SIZE = 1024;
 	private final String TAG = "HttpRequestHelper";
 	private HttpClient hc;
 	private String baseAddress = "";
 	private Activity cntx;
 
 	public HttpRequestHelper(Activity c)
 	{
 		this.cntx = c;
 		if (c != null)
 			this.baseAddress = c.getString(R.string.server_address);
 		else
 			Logger.i(TAG, "activity is null");
 		hc = new DefaultHttpClient();
 		hc.getParams().setParameter("http.protocol.content-charset", "UTF-8");
 	}
 
	public String runSimpleJsonRequest(String uri) 
 	{
 		HttpGet httpGet = new HttpGet(baseAddress + uri);
 		return runRequest(httpGet);
 	}
 
 	private String runRequest(HttpUriRequest httpRequest)
 	{
 		StringBuilder builder = new StringBuilder();
 		try
 		{
			httpRequest.setHeader("Cookie", SettingsUtil.getSetting(cntx, Constants.CONFIG_SESSION, ""));
 			HttpResponse response = null;
 			try
 			{
 				response = hc.execute(httpRequest);
 			} catch (ConnectException e)
 			{
 				Logger.w("HttpRequestHelper", "Connect exception " + e.getMessage());
 				return null;
 			} catch (IllegalStateException e)
 			{
 				Logger.w("HttpRequestHelper", "IllegalStateException " + e.getMessage());
 				return null;
 			}
 			StatusLine statusLine = response.getStatusLine();
 			int statusCode = statusLine.getStatusCode();
 			// Logger.i("HttpRequestHelper", "Received status code " + statusCode);
 			if (HttpStatus.SC_OK == statusCode)
 			{
 				BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 				String line;
 				while ((line = reader.readLine()) != null)
 				{
 					builder.append(line);
 				}
 			}
 			else if (HttpStatus.SC_FORBIDDEN == statusCode)
 			{
 				Logger.i("HttpRequestHelper", "Received status code FORBIDDEN! invocating relogin and doing runRequest");
 				// if (new UserProvider(cntx).doStoredLogin())
 				// {
 				// return runRequest(httpRequest);
 				// }
 				// else
 				// {
 				// Logger.w(TAG, "Unable to login, need to enter valid login and password");
 				// throw new UnableToLoginException();
 				// }
 			}
 			else
 			{
 				Logger.w("HttpRequestHelper", statusLine.toString() + "at" + httpRequest.getURI());
 			}
 		} catch (ConnectException e)
 		{
 			Logger.w("HttpRequestHelper", "runRequest", e);
 			e.printStackTrace();
 			return null;
 		} catch (ClientProtocolException e)
 		{
 			Logger.w("HttpRequestHelper", "runRequest", e);
 			e.printStackTrace();
 			return null;
 		} catch (IOException e)
 		{
 			Logger.w("HttpRequestHelper", "runRequest", e);
 			e.printStackTrace();
 			return null;
 		}
 		return builder.toString();
 	}
 
 	public String runJsonRequest(String uri, NameValuePair... params)
 	{
 		HttpPost request = new HttpPost(baseAddress + uri);
 
 		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 		for (int i = 0; i < params.length; ++i)
 		{
 			nameValuePairs.add(params[i]);
 		}
 
 		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
 		UrlEncodedFormEntity entity;
 		try
 		{
 			entity = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
 			entity.setContentType("appplication/x-www-form-urlencoded");
 			request.setEntity(entity);
 
 			return runRequest(request);
 		} catch (UnsupportedEncodingException e)
 		{
 			Logger.w("HttpRequestHelper", "Failed to form request content" + e.getMessage());
 			return "";
 		}
 	}
 }
