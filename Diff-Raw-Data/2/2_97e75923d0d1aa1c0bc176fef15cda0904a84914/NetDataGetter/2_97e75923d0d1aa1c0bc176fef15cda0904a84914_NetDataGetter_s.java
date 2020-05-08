 package com.tools.tvguide.utils;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.zip.GZIPInputStream;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.ParseException;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 import android.util.Pair;
 
 public class NetDataGetter
 {
 	URL mUrl = null;
 	//ProgressListener mProgressListener = null;
 	List<BasicNameValuePair> mPairs = null;
 	//String mRecvDataAsync = null;
 	private final int CONNECTION_TIMEOUT_WIFI = 6000;
 	private final int CONNECTION_TIMEOUT_GPRS = 10000;
 	private final int SOCKET_TIMEOUT_WIFI = 10000;
 	private final int SOCKET_TIMEOUT_GPRS = 20000;
 	private int mConnectionTimeout = CONNECTION_TIMEOUT_WIFI;
 	private int mSocketTimeout = SOCKET_TIMEOUT_WIFI;
 	private HashMap<String, String> mExtraHeaders = new HashMap<String, String>();
 	private HttpResponse mResponse;
 	private Map<String, List<String>> mReponseHeaders;
 	
 	public NetDataGetter()
 	{
 	}
 	
 	public NetDataGetter(String url) throws MalformedURLException
 	{
 		mUrl = new URL(url);
 	}
 	
 	public int setUrl(String url)
 	{
 		try
 		{
 			mUrl = new URL(url);
 		}
 		catch (MalformedURLException e)
 		{
 			e.printStackTrace();
 			return -1;
 		}
 		
 		return 0;
 	}
 	
 	public int setConnectionTimeout(int timeout)
 	{
 		Log.d("NetDataGetter::setConnectionTimeout", "timeout = " + timeout);
 		if(timeout <= 0)
 		{
 			Log.d("NetDataGetter::setConnectionTimeout", "timeout <= 0");
 			return -1;
 		}
 		mConnectionTimeout = timeout;
 		return 0;
 	}
 	
 	public int setSocketTimeout(int timeout)
 	{
 		Log.d("NetDataGetter::setSocketTimeout", "timeout = " + timeout);
 		if(timeout <= 0)
 		{
 			Log.d("NetDataGetter::setSocketTimeout", "timeout <= 0");
 			return -1;
 		}
 		mSocketTimeout = timeout;
 		return 0;
 	}
 	
 	public void setHeader(String name, String value)
 	{
 	    if (name == null || value == null)
 	        return;
 	    mExtraHeaders.put(name, value);
 	}
 	
 	public void setHeaders(HashMap<String, String> headers)
 	{
 	    if (headers == null)
 	        return;
 	    mExtraHeaders = headers;
 	}
 	
 	public String getFirstHeader(String name)
 	{
 	    if (mResponse == null)
 	        return null;
 	    if (mResponse.getFirstHeader(name) == null)
 	        return null;
 	    return mResponse.getFirstHeader(name).getValue();
 	}
 	
 	public InputStream getInputStream()
 	{
 		if(mUrl == null)
 		{
 			return null;
 		}
 		
 		InputStream input = null;
 		try
 		{
 			input =  mUrl.openStream();
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 		
 		Utility.emulateNetworkDelay();
 		
 		return input;
 	}
 	
 	public String getStringData()
 	{
 		return getStringData(null);
 	}
 	
 	public String getStringData(List<BasicNameValuePair> pairs)
 	{
 		if (mUrl == null)
 		{
 			return null;
 		}
 		if (Utility.isNetworkAvailable() == false)
 		{
 			return null;
 		}
 
 		if (!Utility.isWifi(MyApplication.getInstance()))
 		{
 		    mConnectionTimeout = CONNECTION_TIMEOUT_GPRS;
 		    mSocketTimeout = SOCKET_TIMEOUT_GPRS;
 		}
 		
 		HttpParams httpParameters = new BasicHttpParams();
 		HttpConnectionParams.setConnectionTimeout(httpParameters, mConnectionTimeout);
 		HttpConnectionParams.setSoTimeout(httpParameters, mSocketTimeout);
 		
 		HttpClient client = new DefaultHttpClient(httpParameters);
     	HttpResponse response = null;
     	HttpEntity entity = null;
     	String recvData = null;
     	
         try
 		{
             // POST 
             if(pairs != null)
             {
                 HttpPost post = new HttpPost(mUrl.toString());
                 post.setEntity(new UrlEncodedFormEntity(pairs, HTTP.UTF_8));
                 Iterator<Entry<String, String>> iter = mExtraHeaders.entrySet().iterator();
                 while (iter.hasNext())
                 {
                     Map.Entry<String, String> entry = iter.next();
                     String key = entry.getKey();
                     String value = entry.getValue();
                     post.setHeader(key, value);
                 }
                 post.addHeader("Accept-Encoding", "gzip");
                 response = client.execute(post);
             }
             // GET
             else
             {
                 HttpGet get = new HttpGet(mUrl.toString());
                 Iterator<Entry<String, String>> iter = mExtraHeaders.entrySet().iterator();
                 while (iter.hasNext())
                 {
                     Map.Entry<String, String> entry = iter.next();
                     String key = entry.getKey();
                     String value = entry.getValue();
                     get.setHeader(key, value);
                 }
                 get.addHeader("Accept-Encoding", "gzip");
                 response = client.execute(get);
             }
         	
         	if(response == null)
         	{
         		Log.e("Error", "Response Null");
         		client.getConnectionManager().shutdown();
 				return null;
         	}
         	if(response.getStatusLine().getStatusCode() != 200)
         	{
         		Log.e("Error", "Status code = %d" + response.getStatusLine().getStatusCode());
         		client.getConnectionManager().shutdown();
         		return null;
         	}
         	
         	mResponse = response;
         	entity = response.getEntity();
         	if(entity == null)
         	{
         		Log.e("Error", "Status code = %d" + response.getStatusLine().getStatusCode());
         		client.getConnectionManager().shutdown();
         		return null;
         	}
         	
         	InputStream is = entity.getContent();
        	if (entity.getContentEncoding().getValue().toLowerCase(Locale.ENGLISH).contains("gzip"))
         	{
         	    is = new GZIPInputStream(is);
         	}
         	String line;
             StringBuilder sb = new StringBuilder();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is));
             while ((line = reader.readLine()) != null)
             {
                 sb.append(line + "\n");
             }
             
             recvData = sb.toString();
 			if(recvData == null)
 			{
 				Log.e("Error", "Receive Null");
 				client.getConnectionManager().shutdown();
 				return null;
 			}
 			client.getConnectionManager().shutdown();
 		}
 		catch (UnsupportedEncodingException e1)
 		{
 			Log.e("Exception", "Set name pair failed!");
 			e1.printStackTrace();
 			client.getConnectionManager().shutdown();
 			return null;
 		}
 		catch (ClientProtocolException e)
 		{
 			Log.e("Exception", "Exec post protocol failed!");
 			e.printStackTrace();
 			client.getConnectionManager().shutdown();
 			return null;
 		}
 		catch (IOException e)
 		{
 			Log.e("Exception", "Exec post IO failed!");
 			e.printStackTrace();
 			client.getConnectionManager().shutdown();
 			return null;
 		}
 		catch (ParseException e)
 		{
 			Log.e("Error", "Entity parse failed");
 			e.printStackTrace();
 			client.getConnectionManager().shutdown();
 			return null;
 		}
 
         return recvData;
 		
 //		String recvData;
 //		try 
 //		{
 //            recvData = getStringData2(pairs);
 //        } 
 //		catch (IOException e) 
 //		{
 //            e.printStackTrace();
 //            return null;
 //        }
 //		return recvData;
 	}
 	
 	private String getStringData2(List<BasicNameValuePair> pairs) throws IOException
 	{
 	    String content = null;
 	    HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
         if (!Utility.isWifi(MyApplication.getInstance()))
         {
             mConnectionTimeout = CONNECTION_TIMEOUT_GPRS;
             mSocketTimeout = SOCKET_TIMEOUT_GPRS;
         }
         conn.setConnectTimeout(mConnectionTimeout);
         conn.setReadTimeout(mSocketTimeout);
         conn.setDoOutput(true);
         
         if (pairs != null)
         {
             conn.setRequestMethod("POST");
             for (int i=0; i<pairs.size(); ++i)
             {
                 content += pairs.get(i).getName() + "=" + pairs.get(i).getValue();
                 if (i != pairs.size()-1)      // Not the last
                     content += "&";
             }
         }
         else
             conn.setRequestMethod("GET");
         
         Iterator<Entry<String, String>> iter = mExtraHeaders.entrySet().iterator();
         while (iter.hasNext())
         {
             Map.Entry<String, String> entry = iter.next();
             String key = entry.getKey();
             String value = entry.getValue();
             conn.addRequestProperty(key, value);
         }
 
         conn.connect();
         if (content != null)
         {
             DataOutputStream out = new DataOutputStream(conn.getOutputStream());
             out.writeBytes(content);
             out.flush();
             out.close();
         }
         
         mReponseHeaders = conn.getHeaderFields();
         String line;
         StringBuilder sb = new StringBuilder();
         BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
         while ((line = reader.readLine()) != null)
         {
             sb.append(line + "\n");
         }
         
         return sb.toString();
 	}
 		
 	public JSONObject getJSONsObject()
 	{
 		return getJSONsObject(null);
 	}
 	
 	public JSONObject getJSONsObject(List<BasicNameValuePair> pairs)
 	{
 		JSONObject jsonObject = null;
 		String recvData = getStringData(pairs);
 		if(recvData == null)
 		{
 			Log.e("getJSONsData", "recvData is null");
 			return null;
 		}
 		
     	try
 		{
 			jsonObject = new JSONObject(recvData);
 		}
 		catch (JSONException e)
 		{
 			Log.e("NetDataGetter::getJSONsData", "recvData = " + recvData);
 			Log.e("getJSONsData", "create JSON failed " + e.toString());
 			e.printStackTrace();
 			
 			return null;
 		}
     	
     	return jsonObject;
 	}
 	
 	/*	
 	public void getStringDataAsync(Object userData)
 	{
 		new Thread()
 		{
 			@Override
 			public void run()
 			{
 				mRecvDataAsync = getStringData();
 			}
 		}.start();
 		if(mProgressListener != null)
 		{
 			mProgressListener.notifyStatus(this, ProgressListener.Progress.FINISHED, mRecvDataAsync, userData);
 		}
 	}
 	
 	public void getStringDataAsync(List<BasicNameValuePair> pairs, Object userData)
 	{
 		mPairs = pairs;
 		new Thread()
 		{
 			@Override
 			public void run()
 			{
 				mRecvDataAsync = getStringData(mPairs);
 			}
 		}.start();
 		if(mProgressListener != null)
 		{
 			mProgressListener.notifyStatus(this, ProgressListener.Progress.FINISHED, mRecvDataAsync, userData);		
 		}
 	}
 	
 	public void setProgressListener(ProgressListener listener)
 	{
 		mProgressListener = listener;
 	}
 	
 	public interface ProgressListener
 	{
 		public enum Progress
 		{
 			STARTING,
 			FINISHED,
 		}
 		public void notifyStatus(NetDataGetter getter, ProgressListener.Progress status, String recvData, Object userData);
 	}
 	*/
 }
