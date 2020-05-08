 /**
  * Copyright (c) 2012 Alvin S.J. Ng
  * 
  * Permission is hereby granted, free of charge, to any person obtaining 
  * a copy of this software and associated documentation files (the 
  * "Software"), to deal in the Software without restriction, including 
  * without limitation the rights to use, copy, modify, merge, publish, 
  * distribute, sublicense, and/or sell copies of the Software, and to 
  * permit persons to whom the Software is furnished to do so, subject 
  * to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be 
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT 
  * WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
  * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR 
  * PURPOSE AND NONINFRINGEMENT. IN NO EVENT 
  * SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
  * TORT OR OTHERWISE, ARISING FROM, OUT OF OR 
  * IN CONNECTION WITH THE SOFTWARE OR 
  * THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  * 
  * @author 		Alvin S.J. Ng <alvinsj.ng@gmail.com>
  * @copyright	2012	Alvin S.J. Ng
  * 
  */
 package com.stepsdk.android.api;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.ContentBody;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.InputStreamBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.client.DefaultRedirectHandler;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.CoreProtocolPNames;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 
 import android.content.Context;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.webkit.MimeTypeMap;
 import android.webkit.WebView;
 
 import com.stepsdk.android.api.data.APIDataRequestHandler;
 import com.stepsdk.android.api.data.DownloadFileTask;
 import com.stepsdk.android.api.strategy.CacheStrategy;
 import com.stepsdk.android.cache.CacheStore;
 import com.stepsdk.android.cache.api.CachableHttpEntity;
 import com.stepsdk.android.http.CountingMultipartEntity;
 import com.stepsdk.android.http.CountingMultipartEntity.ProgressListener;
 import com.stepsdk.android.util.DeviceUtil;
 import com.stepsdk.android.util.FileUtil;
 import com.stepsdk.android.util.NetworkUtil;
 import com.stepsdk.android.util.NetworkUtil.NetworkDownException;
 
 public class APIClient {
     public static final String TAG = "APIManager";
 
     public static final int NO_ERROR = 0;
 
     public static final int FAIL_LOGIN = 1;
 
     public static final int INTERNET_ERROR = 2;
 
     public static final int RESPONSE_ERROR = 3;
 
     public static final int DATA_ERROR = 4;
 
     public static final int REQUEST_ERROR = 5;
 
     public static final int HTTP_POST_ERROR = 6;
 
     private Context mContext = null;
 
     private DefaultHttpClient mHttpclient;
         
     public static String WEB_USER_AGENT;
 
     public APIClient(Context context, CacheStore cacheStore) {
     	initWithContext(context);  
     	mCacheStore = cacheStore;
     }
     
     public CacheStore cacheStore(){
     	return mCacheStore;
     }
 
     public APIClient(Context context) {
     	initWithContext(context);  
     }
     
     private void initWithContext(Context context){
     	mContext = context;
     }
     
     public Context getContext() {
     	return mContext;
     }
     
     public void cachedGet(CacheStrategy strategy, String address, final Map<String,String> headerParams, APIRequestHandler handler){
     	strategy.getRequest(this, address, headerParams, handler); 
     }
     
     public void cachedPost(CacheStrategy strategy, String address, Map<String, String> params, APIRequestHandler handler){
     	strategy.postRequest(this, address, params, handler);
     }
 
     public void get(final String address, final Map<String,String> headerParams , final APIRequestHandler handler) {
         log(TAG, "GET: "+address);
         new AsyncTask<Void, Void, Void>() {
             private boolean mInterrupt = false;
             @Override
             protected void onPreExecute() {
                 log(TAG, "starting request for " + address);
                 handler.before();
             };
 
             @Override
             protected Void doInBackground(Void... params) {
                 Integer retryRemaining = 3;
 
                 while (retryRemaining > 0) {
                     try {
                         if (!NetworkUtil.isOnline(mContext)){
                             handler.onException(new NetworkDownException());
                             return null;
                         }
 
                         log(TAG, "processing request for " + address);
                         HttpEntity response = getRequest(address, headerParams);
                         handler.onResponse(response);
                         break;
                     } catch (ClientProtocolException e) {
                         if (--retryRemaining == 0) {
                             handler.onException(e);
                         }
                     } catch (IOException e) {
                         if (--retryRemaining == 0) {
                             handler.onException(e);
                         }
                     } catch (NetworkDownException e) {
                         if (--retryRemaining == 0) {
                             handler.onException(e);
                         }
                     } catch (HttpGetException e) {
                         if (--retryRemaining == 0) {
                             handler.onException(e);
                         }
                     } catch (Exception e) {
                         if (--retryRemaining == 0) {
                             handler.onException(e);
                             e.printStackTrace();
                         }
                     }
                 }
                 
                 return null;
             };
 
             @Override
             protected void onPostExecute(Void result) {
                 log(TAG, "Completed request for " + address);
                 if(!mInterrupt)
                     handler.after();
             };
 
         }.execute();
     }
 
     public void post(final String address, final Map<String, String> params,
             final APIRequestHandler handler) {
         post(address, params, null, handler);
     }
     public void post(final String address, final Map<String, String> params, final Map<String, String> files,
             final APIRequestHandler handler) {
         log(TAG, "POST: "+address + "("+params.toString()+")");
 
         new AsyncTask<Void, Void, Void>() {
             
             private boolean mInterrupt = false;
             @Override
             protected void onPreExecute() {
                 handler.before();
             };
 
             @Override
             protected Void doInBackground(Void... nothing) {
                 Integer retryRemaining = 3;
 
                 while (retryRemaining > 0) {
                     try {
                         if (!NetworkUtil.isOnline(mContext)){
                             handler.onException(new NetworkDownException());
                             return null;
                         }
                         
                         HttpEntity response = null;
                         if(files==null || files.size()==0)
                             response = postRequest(address, params);
                         else
                             response = postRequest(address, params, files);
                         handler.onResponse(response);
                         break;
                     } catch (ClientProtocolException e) {
                         if (retryRemaining-- < 0) {
                             handler.onException(e);
                         }
                     } catch (IOException e) {
                         if (retryRemaining-- < 0) {
                             handler.onException(e);
                         }
                     } catch (NetworkDownException e) {
                         if (retryRemaining-- < 0) {
                             handler.onException(e);
                         }
                     } catch (HttpGetException e) {
                         if (retryRemaining-- < 0) {
                             handler.onException(e);
                         }
                     } catch (Exception e) {
                         if (retryRemaining-- < 0) {
                             handler.onException(e);
                         }
                     }
                 }
                 
                 return null;
             };
 
             @Override
             protected void onPostExecute(Void result) {
                 if(!mInterrupt)
                     handler.after();
             };
 
         }.execute();
     }
     
     private CacheStore mCacheStore;
     protected CacheStore defaultCacheStore() throws Exception{
     	if(mCacheStore == null)
     		return new CacheStore(getContext()) {
 				
 				@Override
 				public String storeName() {
 					return "APIClient";
 				}
 			};
     	else 
     		return mCacheStore;
     }
     
     public static class CacheStoreNotSetException extends Exception{
     	private static final long serialVersionUID = 0;
     	public CacheStoreNotSetException(String message){
     		super(message);
     	}
     }
     
     public void download(final String address, final String cacheFileId,
             final APIDataRequestHandler handler, final File folder) {
 
         new DownloadFileTask(mContext, address, cacheFileId) {
         	public File defaultCacheFolder(Context context) {
         		return folder;
         	};
             @Override
             protected void onPreExecute() {
                 super.onPreExecute();
                 handler.before();
             };
 
             @Override
             protected Boolean doInBackground(Void... nothing) {
                 boolean result = super.doInBackground(nothing);
                 if (result)
                     handler.onResponse(getData());
                 return result;
 
             };
 
             @Override
             protected void onPostExecute(Boolean result) {
                 super.onPostExecute(result);
                 handler.after();
             };
             @Override
             protected void onProgressUpdate(Integer[] changed) {
             	
             	handler.onProgressUpdate(changed[0]);
             };
 
         }.execute();
     }
 
     public void cancelRequest() {
         if (mHttpclient != null)
             mHttpclient.getConnectionManager().shutdown();
     }
 
     public HttpEntity getRequest(String url, Map<String,String> headerParams) throws NetworkDownException, HttpGetException {
         HttpEntity response = httpGet(url, headerParams);
         return response;
     }
 
     public HttpEntity postRequest(String url, Map<String, String> params)
             throws NetworkDownException, HttpGetException, UnsupportedEncodingException,
             HttpPostException {
         List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
 
         Iterator<Entry<String, String>> i = params.entrySet().iterator();
         while (i.hasNext()) {
             Entry<String, String> next = i.next();
             nameValuePairs.add(new BasicNameValuePair(next.getKey(), next.getValue()));
         }
 
         UrlEncodedFormEntity postEnt = new UrlEncodedFormEntity(nameValuePairs);
 
         HttpEntity response = httpPost(url, postEnt);
         return response;
     }
     public HttpEntity postRequest(String url, Map<String, String> params, Map<String, String> files)
         throws NetworkDownException, HttpGetException, HttpPostException, IOException {
         
         MultipartEntity mpEntity = new CountingMultipartEntity( new ProgressListener() {
             private int prevPercentage =0; 
             @Override
             public void transferred(long num) {
                 int percentage = (int)num;
                 if( percentage>prevPercentage) {
                     prevPercentage = percentage;
                 }
             }
         });
         
         Iterator<Entry<String, String>> i = params.entrySet().iterator();
         while (i.hasNext()) {
             Entry<String, String> next = i.next();
             mpEntity.addPart(next.getKey(), new StringBody(next.getValue()));
         }
         
         i = files.entrySet().iterator();
         while (i.hasNext()) {
             Entry<String, String> next = i.next();
             mpEntity = addFile(mpEntity, next.getKey(), next.getValue());
         }
         
         HttpEntity response = httpPost(url, mpEntity);
         return response;
     }
     
     
 
     public HttpEntity httpGet(String url, Map<String,String> headerParams) throws NetworkDownException, HttpGetException {
         HttpEntity entity = null;
         
         mHttpclient = new DefaultHttpClient();
 
         ClientConnectionManager mgr = mHttpclient.getConnectionManager();
         HttpParams params = mHttpclient.getParams();
         if(WEB_USER_AGENT != null)
         	params.setParameter(CoreProtocolPNames.USER_AGENT, WEB_USER_AGENT);
         int timeoutConnection = 3000;
         HttpConnectionParams.setConnectionTimeout(params, timeoutConnection);
         int timeoutSocket = 5000;
         HttpConnectionParams.setSoTimeout(params, timeoutSocket);
         mHttpclient = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);
         
         // Allow redirection from server
         // ref: http://stackoverflow.com/questions/3658721/httpclient-4-error-302-how-to-redirect
         mHttpclient.setRedirectHandler(new DefaultRedirectHandler() {                
             @Override
             public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
                 boolean isRedirect = super.isRedirectRequested(response, context);
                 if (!isRedirect) {
                     int responseCode = response.getStatusLine().getStatusCode();
                     if (responseCode == 302) {
                         return true;
                     }
                 }
                 return isRedirect;
             }
         });
         
         HttpGet get = new HttpGet(url);
         
         if(headerParams == null)
         {
         	headerParams = new HashMap<String, String>();
         }
         
         Iterator<String> i = headerParams.keySet().iterator();
         
         while(i.hasNext()){
             String key = i.next();
             get.addHeader(key, headerParams.get(key));
         }
         
         Integer retryRemaining = 3;
 
         while (entity == null) {
             try {
             	
             	if(!DeviceUtil.checkPermission(mContext, "android.permission.ACCESS_NETWORK_STATE"))
             		throw new NetworkDownException("ACCESS_NETWORK_STATE permission not set in AndroidManifest.xml");
             	
             	if(!DeviceUtil.checkPermission(mContext, "android.permission.INTERNET"))
             		throw new NetworkDownException("INTERNET permission not set in AndroidManifest.xml");
             	
                 if (!NetworkUtil.isOnline(mContext))
                     throw new NetworkDownException();
 
                 HttpResponse response = mHttpclient.execute(get);
                 if (response.getStatusLine().getStatusCode() == 404)
                     throw new HttpGetException("404");
                 entity = response.getEntity();
             } catch (ClientProtocolException e) {
                 if (retryRemaining-- != 0) {
                     entity = null;
                 } else {
                     throw new HttpGetException(e.getMessage());
                 }
             } catch (IOException e) {
                 if (retryRemaining-- != 0) {
                     entity = null;
                 } else {
                     throw new HttpGetException(e.getMessage());
                 }
             }
         }
 
         return entity;
     }
 
     public static class HttpGetException extends Exception {
         private static final long serialVersionUID = 1L;
 
         public HttpGetException(String msg) {
             super(msg);
         }
     }
 
     public HttpEntity httpPost(String url, UrlEncodedFormEntity ent) throws NetworkDownException,
             HttpPostException {
        
         ClientConnectionManager mgr = mHttpclient.getConnectionManager();
         HttpParams params = mHttpclient.getParams();
         if(WEB_USER_AGENT != null)
         	params.setParameter(CoreProtocolPNames.USER_AGENT, WEB_USER_AGENT);
         mHttpclient = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);
         
         HttpPost post = new HttpPost(url);
 
         if (ent != null)
             post.setEntity(ent);
 
         try {
             if (!NetworkUtil.isOnline(mContext))
                 throw new NetworkDownException();
 
             HttpResponse response = mHttpclient.execute(post);
             return response.getEntity();
 
         } catch (ClientProtocolException e) {
             throw new HttpPostException(e.getMessage());
         } catch (IOException e) {
             throw new HttpPostException(e.getMessage());
         }
     }
     
     private HttpEntity httpPost(String url, MultipartEntity mpEntity) throws NetworkDownException,
     HttpPostException {
         mHttpclient = new DefaultHttpClient();
         
         ClientConnectionManager mgr = mHttpclient.getConnectionManager();
         HttpParams params = mHttpclient.getParams();
         if(WEB_USER_AGENT != null)
         	params.setParameter(CoreProtocolPNames.USER_AGENT, WEB_USER_AGENT);
         int timeoutConnection = 3000;
         HttpConnectionParams.setConnectionTimeout(params, timeoutConnection);
         int timeoutSocket = 5000;
         HttpConnectionParams.setSoTimeout(params, timeoutSocket);
         mHttpclient = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);
 
         HttpPost httppost = new HttpPost(url);
         httppost.setEntity(mpEntity);
 
         try {
             if( !NetworkUtil.isOnline(mContext) )
                 throw new NetworkDownException();
             log(TAG, "executing request " + httppost.getRequestLine());
             HttpResponse response = mHttpclient.execute(httppost);
             log(TAG, response.getStatusLine().toString());
             HttpEntity result = response.getEntity();
             if (response != null) {
                 log(TAG, EntityUtils.toString(result));
                 result.consumeContent();
             }
             mHttpclient.getConnectionManager().shutdown();
             return result;
             
         }catch(ClientProtocolException e) {
             mHttpclient.getConnectionManager().shutdown();
             
             throw new HttpPostException(e.getMessage());
             
         }catch(IOException e) {
             mHttpclient.getConnectionManager().shutdown();
             throw new HttpPostException(e.getMessage());
             
         } 
     }
     
     private MultipartEntity addFile(MultipartEntity mpEntity, String key, String fromPath) throws IOException {
         
         String filepath;
         try {
             filepath = URLDecoder.decode(fromPath, "UTF-8"); // handle special character
         }catch(Exception e) {
             filepath = fromPath;
         }
         String fromFilename = filepath.substring(filepath.lastIndexOf("/")+1);
         
         String filename = "";
         long filesize = 0;
         ContentBody cbFile = null;
         
         log(TAG, "from upload path: "+fromPath);
         
         // upload from content uri
         if(fromPath.indexOf("content://")>-1) {
             Uri uri = Uri.parse(fromPath);
             String mime = mContext.getContentResolver().getType(uri);
             String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime);
             String mediatype = mime.substring(0,mime.indexOf('/'));
             
             InputStream is = mContext.getContentResolver().openInputStream(uri);
             filesize = is.available();
             filename = mediatype+fromFilename+"."+extension;
             cbFile = new InputStreamBody(is, mime, filename);
             
         }else { //upload from file uri
             File file = new File(filepath);
             
             filesize = file.length();
             filename = fromFilename;
             cbFile = new FileBody(file, FileUtil.getMimeTypeFromFilePath(fromPath));
         }
         
         //final String finalfilename = filename;
         final long finalfilesize = filesize;
         //final int task_id = params[1].getIntExtra("cancel_task_id", 0);
         //final Intent cancelintent = params[1];
         //final Intent doneintent = params[2];
         
         mpEntity.addPart(key, cbFile);
         
         return mpEntity;
     }
 
     public static class HttpPostException extends Exception {
         private static final long serialVersionUID = 1L;
 
         public HttpPostException(String msg) {
             super(msg);
         }
     }
     
     public void log(String where, String message){
     }
 
 }
