 /*
  * Copyright 2011 - AndroidQuery.com (tinyeeliu@gmail.com)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.androidquery.callback;
 
 import java.io.ByteArrayInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.ref.Reference;
 import java.lang.ref.WeakReference;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.conn.params.ConnManagerParams;
 import org.apache.http.conn.params.ConnPerRouteBean;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.ExecutionContext;
 import org.apache.http.protocol.HttpContext;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.view.View;
 
 import com.androidquery.AQuery;
 import com.androidquery.auth.AccountHandle;
 import com.androidquery.auth.GoogleHandle;
 import com.androidquery.util.AQUtility;
 import com.androidquery.util.PredefinedBAOS;
 import com.androidquery.util.XmlDom;
 
 /**
  * The core class of ajax callback handler.
  *
  */
 public abstract class AbstractAjaxCallback<T, K> implements Runnable{
 	
 	private static int NET_TIMEOUT = 30000;
 	private static String AGENT = null;
 	private static int NETWORK_POOL = 4;
 	
 	private Class<T> type;
 	private Reference<Object> whandler;
 	private Object handler;
 	private String callback;
 	private WeakReference<View> progress;
 	
 	private String url;
 	private Map<String, Object> params;
 	private Map<String, String> headers;
 	
 	private T result;
 	
 	private File cacheDir;
 	private AccountHandle ah;
 	
 	protected AjaxStatus status;
 	
 	protected boolean fileCache;
 	protected boolean memCache;
 	private boolean refresh;
 	
 	private long expire;
 	private String encoding = "UTF-8";
 	private WeakReference<Activity> act;
 	
 	private boolean uiCallback = true;
 	
 	@SuppressWarnings("unchecked")
 	private K self(){
 		return (K) this;
 	}
 	
 	private void clear(){		
 		whandler = null;
 		handler = null;
 		progress = null;
 	}
 	
 	/**
 	 * Sets the timeout.
 	 *
 	 * @param timeout the default network timeout in milliseconds
 	 */
 	public static void setTimeout(int timeout){
 		NET_TIMEOUT = timeout;
 	}
 	
 	/**
 	 * Sets the agent.
 	 *
 	 * @param agent the default agent sent in http header
 	 */
 	public static void setAgent(String agent){
 		AGENT = agent;
 	}
 	
 	/**
 	 * Gets the ajax response type.
 	 *
 	 * @return the type
 	 */
 	public Class<T> getType() {
 		return type;
 	}
 
 	/**
 	 * Set a callback handler with a weak reference. Use weak handler if you do not want the ajax callback to hold the handler object from garbage collection.
 	 * For example, if the handler is an activity, weakHandler should be used since the method shouldn't be invoked if an activity is already dead and garbage collected.
 	 *
 	 * @param handler the handler
 	 * @param callback the callback
 	 * @return self
 	 */
 	public K weakHandler(Object handler, String callback){
 		this.whandler = new WeakReference<Object>(handler);
 		this.callback = callback;
 		this.handler = null;
 		return self();
 	}
 	
 	/**
 	 * Set a callback handler. See weakHandler for handler objects, such as Activity, that should not be held from garbaged collected. 
 	 *
 	 * @param handler the handler
 	 * @param callback the callback
 	 * @return self
 	 */
 	public K handler(Object handler, String callback){
 		this.handler = handler;
 		this.callback = callback;
 		this.whandler = null;
 		return self();
 	}
 	
 	/**
 	 * Url.
 	 *
 	 * @param url the url
 	 * @return self
 	 */
 	public K url(String url){
 		this.url = url;
 		return self();
 	}
 	
 	/**
 	 * Set the desired ajax response type. Type parameter is required otherwise the ajax callback will not occur.
 	 * 
 	 * Current supported type: JSONObject.class, String.class, byte[].class, Bitmap.class, XmlDom.class
 	 * 
 	 *
 	 * @param type the type
 	 * @return self
 	 */
 	public K type(Class<T> type){
 		this.type = type;
 		return self();
 	}
 	
 	/**
 	 * Set ajax request to be file cached.
 	 *
 	 * @param cache the cache
 	 * @return self
 	 */
 	public K fileCache(boolean cache){
 		this.fileCache = cache;
 		return self();
 	}
 	
 	/**
 	 * Indicate ajax request to be memcached. Note: The default ajax handler does not supply a memcache.
 	 * Subclasses such as BitmapAjaxCallback can provide their own memcache. 
 	 *
 	 * @param cache the cache
 	 * @return self
 	 */
 	public K memCache(boolean cache){
 		this.memCache = cache;
 		return self();
 	}
 	
 	/**
 	 * Indicate the ajax request should ignore memcache and filecache.
 	 *
 	 * @param refresh the refresh
 	 * @return self
 	 */
 	public K refresh(boolean refresh){
 		this.refresh = refresh;
 		return self();
 	}
 	
 	/**
 	 * Indicate the ajax request should use the main ui thread for callback. Default is true.
 	 *
 	 * @param uiCallback use the main ui thread for callback
 	 * @return self
 	 */
 	public K uiCallback(boolean uiCallback){
 		this.uiCallback = uiCallback;
 		return self();
 	}
 	
 	/**
 	 * The expire duation for filecache. If a cached copy will be served if a cached file exists within current time minus expire duration.
 	 *
 	 * @param expire the expire
 	 * @return self
 	 */
 	public K expire(long expire){
 		this.expire = expire;
 		return self();
 	}
 	
 	/**
 	 * Set the header fields for the http request.
 	 *
 	 * @param name the name
 	 * @param value the value
 	 * @return self
 	 */
 	public K header(String name, String value){
 		if(headers == null){
 			headers = new HashMap<String, String>();
 		}
 		headers.put(name, value);
 		return self();
 	}
 	
 	/**
 	 * Set the encoding used to parse the response.
 	 * 
 	 * Default is UTF-8.
 	 * 
 	 * @param encoding
 	 * @return self
 	 */
 	public K encoding(String encoding){
 		this.encoding = encoding;
 		return self();
 	}
 	
 	
 	/**
 	 * Set http POST params. If params are set, http POST method will be used. 
 	 * The UTF-8 encoded value.toString() will be sent with POST. 
 	 * 
 	 * Header field "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" will be added if no Content-Type header field presents.
 	 *
 	 * @param name the name
 	 * @param value the value
 	 * @return self
 	 */
 	public K param(String name, Object value){
 		if(params == null){
 			params = new HashMap<String, Object>();
 		}
 		params.put(name, value);
 		return self();
 	}
 	
 	/**
 	 * Set the http POST params. See param(String name, Object value).
 	 *
 	 * @param params the params
 	 * @return self
 	 */
 	public K params(Map<String, Object> params){
 		this.params = params;
 		return self();
 	}
 	
 	/**
 	 * Set the progress view (can be a progress bar or any view) to be shown (VISIBLE) and hide (GONE) when async is in progress.
 	 *
 	 * @param view the progress view
 	 * @return self
 	 */
 	public K progress(View view){
 		if(view != null){
 			this.progress = new WeakReference<View>(view);
 		}
 		return self();
 	}
 	
 	private static final Class<?>[] DEFAULT_SIG = {String.class, Object.class, AjaxStatus.class};	
 	
 	private boolean completed;
 	private void callback(){
 		
 		showProgress(false);
 		
 		completed = true;
 		
 		if(isActive()){
 		
 			if(callback != null){	
 				Object handler = getHandler();
 				Class<?>[] AJAX_SIG = {String.class, type, AjaxStatus.class};				
 				AQUtility.invokeHandler(handler, callback, true, false, AJAX_SIG, DEFAULT_SIG, url, result, status);					
 			}else{		
 				callback(url, result, status);
 			}
 		
 		}
 		
 		filePut();
 		
 		wake();
 		AQUtility.debugNotify();
 	}
 	
 	private void wake(){
 		
 		if(!blocked) return;
 		
 		synchronized(this){
 			try{
 				notifyAll();
 			}catch(Exception e){				
 			}
 		}
 		
 	}
 	
 	private boolean blocked;
 	public void block(){
 		
 		if(AQUtility.isUIThread()){
 			throw new IllegalStateException("Cannot block UI thread.");
 		}
 		
 		if(completed) return;
 		
 		try{
 			synchronized(this){
 				blocked = true;
 				//wait at most the network timeout plus 5 seconds, this guarantee thread will never be blocked forever
 				this.wait(NET_TIMEOUT + 5000);
 			}
 		}catch(Exception e){			
 		}
 		
 	}
 	
 	
 	/**
 	 * The callback method to be overwritten for subclasses.
 	 *
 	 * @param url the url
 	 * @param object the object
 	 * @param status the status
 	 */
 	public void callback(String url, T object, AjaxStatus status){
 		
 	}
 	
 	protected T fileGet(String url, File file, AjaxStatus status){
 		try {			
 			byte[] data = AQUtility.toBytes(new FileInputStream(file));			
 			return transform(url, data, status);
 		} catch(Exception e) {
 			AQUtility.debug(e);
 			return null;
 		}
 	}
 	
 	protected T datastoreGet(String url){
 		
 		return null;
 		
 	}
 	
 	protected void showProgress(boolean show){
 		
 		if(progress != null){
 			
 			View pb = progress.get();
 			if(pb != null){				
 				
 				if(show){
 					pb.setTag(AQuery.TAG_URL, url);
 					pb.setVisibility(View.VISIBLE);
 				}else{
 					Object tag = pb.getTag(AQuery.TAG_URL);
 					if(tag == null || tag.equals(url)){
 						pb.setTag(AQuery.TAG_URL, null);
 						pb.setVisibility(View.GONE);						
 					}
 				}
 			}
 		}
 		
 	}
 	
 	@SuppressWarnings("unchecked")
 	protected T transform(String url, byte[] data, AjaxStatus status){
 				
 		if(data == null || type == null){
 			return null;
 		}
 		
 		if(type.equals(JSONObject.class)){
 			
 			JSONObject result = null;
 	    	
 	    	try {    		
 	    		String str = new String(data, encoding);
 				result = (JSONObject) new JSONTokener(str).nextValue();
 			} catch (Exception e) {	  		
 				AQUtility.debug(e);
 			}
 			return (T) result;
 		}
 		
 		if(type.equals(JSONArray.class)){
 			
 			JSONArray result = null;
 	    	
 	    	try {    		
 	    		String str = new String(data, encoding);
 				result = (JSONArray) new JSONTokener(str).nextValue();
 			} catch (Exception e) {	  		
 				AQUtility.debug(e);
 			}
 			return (T) result;
 		}
 		
 		if(type.equals(String.class)){
 			String result = null;
 	    	
 	    	try {    		
 	    		result = new String(data, encoding);
 			} catch (Exception e) {	  		
 				AQUtility.debug(e);
 			}
 			return (T) result;
 		}
 		
 		if(type.equals(XmlDom.class)){
 			
 			XmlDom result = null;
 			
 			try {    
 				result = new XmlDom(data);
 			} catch (Exception e) {	  		
 				AQUtility.debug(e);
 			}
 			
 			return (T) result; 
 		}
 		
 		
 		if(type.equals(byte[].class)){
 			return (T) data;
 		}
 		
 		if(type.equals(Bitmap.class)){			
 			return (T) BitmapFactory.decodeByteArray(data, 0, data.length);
 		}
 		
 		return null;
 	}
 	
 	protected T memGet(String url){
 		return null;
 	}
 	
 	
 	protected void memPut(String url, T object){
 	}
 	
 	protected void filePut(String url, T object, File file, byte[] data){
 		
 		if(file == null || data == null) return;
 		
 		AQUtility.storeAsync(file, data, 0);
 		
 	}
 	
 	protected File accessFile(File cacheDir, String url){	
 		
 		if(expire < 0) return null;
 		
 		File file = AQUtility.getExistedCacheByUrl(cacheDir, url);
 		
 		if(file != null && expire != 0){
 			long diff = System.currentTimeMillis() - file.lastModified();			
 			if(diff > expire){
 				return null;
 			}
 		}
 		
 		return file;
 	}
 	
 	/**
 	 * Starts the async process. 
 	 *
 	 * If activity is passed, the callback method will not be invoked if the activity is no longer in use.
 	 * Specifically, isFinishing() is called to determine if the activity is active.
 	 *
 	 * @param Activity activity
 	 */
 	public void async(Activity act){
 		
 		this.act = new WeakReference<Activity>(act);
 		async((Context) act);
 		
 	}
 	
 	
 	
 	/**
 	 * Starts the async process. 
 	 *
 	 * @param context the context
 	 */
 	public void async(Context context){
 		
 		if(status == null){
 			status = new AjaxStatus();
 			status.redirect(url).refresh(refresh);
 		}
 		
 		showProgress(true);
 		
 		if(ah != null){
 			
 			if(!ah.authenticated()){
 				AQUtility.debug("auth needed", url);
 				ah.auth(this);
 				return;
 			}
 		}
 		
 		work(context);
 	
 	}
 	
 	
 	private boolean isActive(){
 		
 		if(act == null) return true;
 		
 		Activity a = act.get();
 		
		if(a == null || a.isFinishing()){					
 			return false;
 		}
 		
 		return true;
 	}
 	
 	
 	public void failure(int code, String message){
 		
 		if(status != null){
 			status.code(code).message(message);
 			callback();
 		}
 		
 	}
 	
 	protected void execute(){
 		
 		ExecutorService exe = getExecutor();	
 		exe.execute(this);
 		
 	}
 	
 	private void work(Context context){
 		
 		T object = memGet(url);
 			
 		if(object != null){		
 			result = object;
 			status.source(AjaxStatus.MEMORY).done();
 			callback();
 		}else{
 		
 			if(fileCache) cacheDir = AQUtility.getCacheDir(context);				
 			execute();			
 			
 		}
 	}
 	
 	protected boolean cacheAvailable(Context context){
 		return fileCache && AQUtility.getExistedCacheByUrl(context, url) != null;
 	}
 	
 	
 	/**
 	 * AQuert internal use. Do not call this method directly.
 	 */
 	
 	@Override
 	public void run() {
 		
 		
 		if(!status.getDone()){
 			
 			try{			
 				backgroundWork();			
 			}catch(Throwable e){
 				AQUtility.debug(e);
 				status.code(AjaxStatus.NETWORK_ERROR).done();
 			}
 			
 			if(!status.getReauth()){
 				//if doesn't need to reauth
 				if(uiCallback){
 					AQUtility.post(this);
 				}else{
 					afterWork();
 				}
 			}
 		}else{
 			afterWork();
 		}
 			
 		
 		
 		
 	}
 	
 	private void backgroundWork(){
 	
 		
 		
 		if(!refresh){
 		
 			if(fileCache){	
 				fileWork();			
 			}
 		}
 		
 		if(result == null){
 			datastoreWork();			
 		}
 		
 		if(result == null){
 			networkWork();
 		}
 		
 		
 	}
 	
 	private String getCacheUrl(){
 		if(ah != null){
 			return ah.getCacheUrl(url);
 		}
 		return url;
 	}
 	
 	private void fileWork(){
 		
 		File file = accessFile(cacheDir, getCacheUrl());
 		
 		//if file exist
 		if(file != null){
 			//convert
 			result = fileGet(url, file, status);
 			//if result is ok
 			if(result != null){				
 				status.source(AjaxStatus.FILE).time(new Date(file.lastModified())).done();
 			}
 		}
 	}
 	
 	private void datastoreWork(){
 		
 		result = datastoreGet(url);
 		
 		if(result != null){		
 			status.source(AjaxStatus.DATASTORE).done();
 		}
 	}
 	
 	private boolean reauth;
 	private void networkWork(){
 		
 		if(url == null){
 			status.code(AjaxStatus.NETWORK_ERROR).done();
 			return;
 		}
 		
 		
 		byte[] data = null;
 		
 		try{
 			
 			network();
 			
 			if(ah != null && ah.expired(this, status.getCode()) && !reauth){
 				AQUtility.debug("reauth needed", status.getMessage());	
 				reauth = true;
 				if(ah.reauth(this)){
 					network();
 				}else{
 					status.reauth(true);				
 					return;
 				}
 			}
 										
 			data = status.getData();
 			
 		}catch(Exception e){
 			AQUtility.debug(e);
 			status.code(AjaxStatus.NETWORK_ERROR).message("network error");
 		}
 		
 		
 		try{
 			result = transform(url, data, status);
 		}catch(Exception e){
 			AQUtility.debug(e);
 		}
 		
 		if(result == null && data != null){
 			status.code(AjaxStatus.TRANSFORM_ERROR).message("transform error");			
 		}
 		
 		lastStatus = status.getCode();
 		status.done();
 	}
 	
 	private void filePut(){
 		
 		if(result != null && fileCache){
 			
 			byte[] data = status.getData();
 			
 			try{
 				if(data != null && status.getSource() == AjaxStatus.NETWORK){
 				
 					//File file = AQUtility.getCacheFile(cacheDir, url);
 					File file = AQUtility.getCacheFile(cacheDir, getCacheUrl());
 					if(!status.getInvalid()){	
 						filePut(url, result, file, data);
 					}else{
 						if(file.exists()){
 							file.delete();
 						}
 					}
 					
 				}
 			}catch(Exception e){
 				AQUtility.debug(e);
 			}
 			
 			status.data(null);
 		}
 	}
 	
 	private void network() throws IOException{
 		
 		
 		String networkUrl = url;
 		if(ah != null){
 			networkUrl = ah.getNetworkUrl(url);
 		}
 		
 		if(params == null){
 			httpGet(networkUrl, headers, status);	
 		}else{
 			if(isMultiPart(params)){
 				httpMulti(networkUrl, headers, params, status);
 			}else{
 				httpPost(networkUrl, headers, params, status);
 			}
 			
 		}
 		
 	}
 	
 	
 	private void afterWork(){
 		
 		if(url != null && memCache){
 			memPut(url, result);
 		}
 		
 		callback();
 		clear();
 	}
 	
 	
 	private static ExecutorService fetchExe;
 	private static ExecutorService getExecutor(){
 		
 		if(fetchExe == null){
 			fetchExe = Executors.newFixedThreadPool(NETWORK_POOL);			
 		}
 		
 		return fetchExe;
 	}
 	
 	/**
 	 * Sets the simultaneous network threads limit. Highest limit is 8.
 	 *
 	 * @param limit the new network threads limit
 	 */
 	public static void setNetworkLimit(int limit){
 		
 		NETWORK_POOL = Math.max(1, Math.min(8, limit));
 		fetchExe = null;
 	}
 	
 	/**
 	 * Cancel ALL ajax tasks.
 	 */
 	
 	public static void cancel(){
 		
 		if(fetchExe != null){
 			fetchExe.shutdownNow();
 			fetchExe = null;
 		}
 		
 		BitmapAjaxCallback.clearTasks();
 	}
 	
 	private static String patchUrl(String url){
 		
 		url = url.replaceAll(" ", "%20");
 		return url;
 	}
 	
 	private void httpGet(String url, Map<String, String> headers, AjaxStatus status) throws IOException{
 		
 		AQUtility.debug("get", url);
 		url = patchUrl(url);
 		
 		HttpGet get = new HttpGet(url);
 		
 		httpDo(get, url, headers, status);
 		
 	}
 	
 	private void httpPost(String url, Map<String, String> headers, Map<String, Object> params, AjaxStatus status) throws ClientProtocolException, IOException{
 		
 		AQUtility.debug("post", url);
 		
 		HttpPost post = new HttpPost(url);
 		
 		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
 		
 		for(Map.Entry<String, Object> e: params.entrySet()){
 			Object value = e.getValue();
 			if(value != null){
 				pairs.add(new BasicNameValuePair(e.getKey(), value.toString()));				
 			}
 		}
 		
 		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairs, "UTF-8");
 		
 		if(headers != null  && !headers.containsKey("Content-Type")){
 			headers.put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
 		}
 		
 		post.setEntity(entity);
 		httpDo(post, url, headers, status);
 		
 		
 	}
 	
 	
 	private static DefaultHttpClient client;
 	private static DefaultHttpClient getClient(){
 		
 		if(client == null){
 		
 			HttpParams httpParams = new BasicHttpParams();
 			HttpConnectionParams.setConnectionTimeout(httpParams, NET_TIMEOUT);
 			HttpConnectionParams.setSoTimeout(httpParams, NET_TIMEOUT);
 			
 			ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(NETWORK_POOL));
 			
 			//Added this line to avoid issue at: http://stackoverflow.com/questions/5358014/android-httpclient-oom-on-4g-lte-htc-thunderbolt
 			HttpConnectionParams.setSocketBufferSize(httpParams, 8192);
 			
 			SchemeRegistry registry = new SchemeRegistry();
 			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
 			registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
 
 			
 			ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(httpParams, registry);			
 			client = new DefaultHttpClient(cm, httpParams);
 			
 		}
 		return client;
 	}
 	
 	
 	private void httpDo(HttpUriRequest hr, String url, Map<String, String> headers, AjaxStatus status) throws ClientProtocolException, IOException{
 		
 		if(AGENT != null){
 			hr.addHeader("User-Agent", AGENT);
         }
 		
 		if(headers != null){
         	for(String name: headers.keySet()){
         		hr.addHeader(name, headers.get(name));
         	}
         }
 		
 		if(ah != null){
 			ah.applyToken(this, hr);
 		}
 		
 		DefaultHttpClient client = getClient();
 		
 		HttpContext context = new BasicHttpContext(); 	
 		
 		HttpResponse response = client.execute(hr, context);
 		
         byte[] data = null;
         
         String redirect = url;
         
         int code = response.getStatusLine().getStatusCode();
         String message = response.getStatusLine().getReasonPhrase();
         String error = null;
         
         
         if(code < 200 || code >= 300){        	
         	//throw new IOException();
         	try{
         		HttpEntity entity = response.getEntity();
         		byte[] s = AQUtility.toBytes(entity.getContent());
         		error = new String(s, "UTF-8");
         		AQUtility.debug("error", error);
         	}catch(Exception e){
         		AQUtility.debug(e);
         	}
         	
         	
         }else{
         	
         	HttpEntity entity = response.getEntity();	
 			
 			HttpHost currentHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
 			HttpUriRequest currentReq = (HttpUriRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
 	        redirect = currentHost.toURI() + currentReq.getURI();
 			
 	        int size = Math.max(32, Math.min(1024 * 64, (int) entity.getContentLength()));
 	        
 	        PredefinedBAOS baos = new PredefinedBAOS(size);
 	        entity.writeTo(baos);
 	        
 	        data = baos.toByteArray();
         }
         
         AQUtility.debug("response", code);
         if(data != null){
         	AQUtility.debug(data.length, url);
         }
         
         status.code(code).message(message).error(error).redirect(redirect).time(new Date()).data(data).client(client);
 		
 	}
 	
 	/**
 	 * Set the authentication type of this request. This method requires API 5+.
 	 *
 	 * @param act the current activity
 	 * @param type the auth type
 	 * @param account the account, such as someone@gmail.com
 	 * @return self
 	 */
 	public K auth(Activity act, String type, String account){
 		
 		if(android.os.Build.VERSION.SDK_INT >= 5 && type.startsWith("g.")){		
 			ah = new GoogleHandle(act, type, account);
 		}
 		
 		return self();
 		
 	}
 	
 	public K auth(AccountHandle handle){		
 		ah = handle;
 		return self();
 	}
 	
 	/**
 	 * Gets the url.
 	 *
 	 * @return the url
 	 */
 	public String getUrl(){
 		return url;
 	}
 	
 	/**
 	 * Gets the handler.
 	 *
 	 * @return the handler
 	 */
 	public Object getHandler() {
 		if(handler != null) return handler;
 		if(whandler == null) return null;
 		return whandler.get();
 	}
 
 	/**
 	 * Gets the callback method name.
 	 *
 	 * @return the callback
 	 */
 	public String getCallback() {
 		return callback;
 	}
 
 	
 	private static int lastStatus = 200;
 	protected static int getLastStatus(){
 		return lastStatus;
 	}
 	
 	
 	public T getResult(){
 		return result;
 	}
 	
 	public AjaxStatus getStatus(){
 		return status;
 	}
 	
 	private static final String lineEnd = "\r\n";
 	private static final String twoHyphens = "--";
 	private static final String boundary = "*****";
 	
 	
 	private static boolean isMultiPart(Map<String, Object> params){
 		
 		for(Map.Entry<String, Object> entry: params.entrySet()){
 			Object value = entry.getValue();
 			AQUtility.debug(entry.getKey(), value);
 			if(value instanceof File || value instanceof byte[]) return true;
 		}
 		
 		return false;
 	}
 	
 	//String url, Map<String, String> headers, Map<String, Object> params, AjaxStatus status
 	//private static HttpURLConnection postRealMultiPartConnection(String urlString, Map<String, Object> params) throws IOException {
 	private void httpMulti(String url, Map<String, String> headers, Map<String, Object> params, AjaxStatus status) throws IOException {
 
 		AQUtility.debug("multipart", url);
 		
 		HttpURLConnection conn = null;
 		DataOutputStream dos = null;
 		
 		
 		URL u = new URL(url);
 		conn = (HttpURLConnection) u.openConnection();
 
 		conn.setInstanceFollowRedirects(false);
 		
 		conn.setConnectTimeout(NET_TIMEOUT * 4);
 		
 		/*
 		if(ah != null){
 			ah.applyToken(this, hr);
 		}
 		*/
 		//conn.addRequestProperty("Cookie", makeSessionCookie());	
 		
 		conn.setDoInput(true);
 		conn.setDoOutput(true);
 		conn.setUseCaches(false);
 		conn.setRequestMethod("POST");
 		conn.setRequestProperty("Connection", "Keep-Alive");
 		conn.setRequestProperty("Content-Type", "multipart/form-data;charset=utf-8;boundary=" + boundary);
 
 		//headers.put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
 		
 		dos = new DataOutputStream(conn.getOutputStream());
 
 		for(Map.Entry<String, Object> entry: params.entrySet()){
 			
 			writeObject(dos, entry.getKey(), entry.getValue());
 			
 		}
 		
 		dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
 		dos.flush();
 		dos.close();
 		
 		conn.connect();
 		
         int code = conn.getResponseCode();
         String message = conn.getResponseMessage();
         
         byte[] data = null;
         
         if(code < 200 || code >= 300){        	
         	//throw new IOException();
         }else{
         	
         	InputStream is = conn.getInputStream();
     		data = AQUtility.toBytes(is);
     		
         }
         
         AQUtility.debug("response", code);
         if(data != null){
         	AQUtility.debug(data.length, url);
         }
         
         status.code(code).message(message).redirect(url).time(new Date()).data(data).client(null);
 			
 	
 	
 	}
 	
 	private static void writeObject(DataOutputStream dos, String name, Object obj) throws IOException{
 		
 		if(obj == null) return;
 		
 		if(obj instanceof File){
 			writeData(dos, name, new FileInputStream((File) obj));
 		}else if(obj instanceof byte[]){
 			writeData(dos, name, new ByteArrayInputStream((byte[]) obj));
 		}else{
 			writeField(dos, name, obj.toString());
 		}
 		
 	}
 	
 	
 	private static void writeData(DataOutputStream dos, String name, InputStream is) throws IOException {
 		
 		dos.writeBytes(twoHyphens + boundary + lineEnd);
 		dos.writeBytes("Content-Disposition: form-data; name=\""+name+"\";"
 				+ " filename=\"" + name + "\"" + lineEnd);
 		dos.writeBytes(lineEnd);
 
 		AQUtility.copy(is, dos);
 		
 		dos.writeBytes(lineEnd);
 		
 	}
 	
     
 	private static void writeField(DataOutputStream dos, String name, String value) throws IOException {
 		dos.writeBytes(twoHyphens + boundary + lineEnd);
 		dos.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"");
 		dos.writeBytes(lineEnd);
 		dos.writeBytes(lineEnd);
 		
 		byte[] data = value.getBytes("UTF-8");
 		dos.write(data);
 		
 		dos.writeBytes(lineEnd);
 	}
 	
 }
 
 
 
