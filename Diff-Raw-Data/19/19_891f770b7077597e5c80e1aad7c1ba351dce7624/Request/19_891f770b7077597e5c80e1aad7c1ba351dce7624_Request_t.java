 /*
 Android java library for asynchronous http requests with sessions support 
 Copyright (c) 2013 Michael Nedokushev <grouzen.hexy@gmail.com>
 http://grouzen.com/
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
 
 package com.grouzen.android.serenity;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.graphics.Bitmap;
 import android.os.Bundle;
 
 public class Request {
 
     private static final String TAG = Request.class.getCanonicalName();
 
 	private Session mSession;
 	
 	private Callback mCallback;
 
     private ExceptionHandler mExceptionHandler;
 	
 	private Bundle mParameters;
 	
 	private HttpConnection mConnection;
 	
 	private String mUrl;
 	
 	private HttpConnectionMethod mMethod;
 	
 	public Request(String url, Callback callback, Bundle parameters, 
 			Session session, HttpConnectionMethod method) {
 		mSession = session;
 		mParameters = parameters;
 		mCallback = callback;
 		mUrl = url;
 		mMethod = method;
 		mConnection = new HttpConnection(url, method);
 	}
 
     public Request(String url, Callback callback, Bundle parameters,
                    Session session, String multipartKey, String multipartFileName) {
         this(url, callback, parameters, session);
 
         mConnection.setMultipartKey(multipartKey);
         mConnection.setMultipartFileName(multipartFileName);
     }
 
 	public Request(String url, Callback callback, Bundle parameters, Session session) {
 		this(url, callback, parameters, session, HttpConnectionMethod.POST);
 	}
 	
 	public Request(String url, Callback callback, Session session) {
 		this(url, callback, null, session);
 	}
 	
 	public RequestAsyncTask execute() {
 		RequestAsyncTask task = new RequestAsyncTask(this);
 		SessionToken token = mSession.getToken();
 
 		if(token.isFilled()) {
            if(token.isExpired()) {
                 mSession.close();

                 return null;
             }
 
 			token.send(this);
 		}
 
 		task.execute();
 		
 		return task;
 	}
 	
 	public Callback getCallback() {
 		return mCallback;
 	}
 	
 	public Bundle getParameters() {
 		return mParameters;
 	}
 	
 	public Session getSession() {
 		return mSession;
 	}
 	
 	public HttpConnection getConnection() {
 		return mConnection;
 	}
 	
 	public String getUrl() {
 		return mUrl;
 	}
 
     public Request setExceptionHandler(ExceptionHandler handler) {
         mExceptionHandler = handler;
 
         return this;
     }
 
     public ExceptionHandler getExceptionHandler() {
         return mExceptionHandler;
     }
 
     public static interface ExceptionHandler {
 
         public void onException(Exception e);
 
     }
 
 	public static interface Callback {
 	
 		public void onComplete(Response response);
 	
 	}
 	
 	public static interface CallbackJSONObject {
 	
 		public void onComplete(JSONObject json, Response response);
 	
 	}
 	
 	public static interface CallbackJSONArray {
 		
 		public void onComplete(JSONArray json, Response response);
 		
 	}
 	
 	public static interface CallbackJSONAny {
 		
 		public void onComplete(Object json, Response response);
 	
 	}
 	
 	public static interface CallbackBitmap {
 		
 		public void onComplete(Bitmap bitmap, Response response);
 		
 	}
 	
 }
