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
 
 import java.io.IOException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.util.Log;
 
 public class Response {
 
 	private final static String TAG = Response.class.getCanonicalName();
 	
 	private Request request;
 	
 	private byte[] data;
 	
 	private Exception exception;
 	
 	public Response(Request request) {
 		this.request = request;
 	}
 	
 	private byte[] readData(HttpResponse response) throws IOException {
 		if(response != null) {
 			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 				return EntityUtils.toByteArray(response.getEntity());
 			}

            throw new IOException("Bad http status: " + response.getStatusLine().getStatusCode());
 		}
 		
 		throw new IOException("Bad response");
 	}
 		
 	public String toString() {
        return data != null ? new String(data) : "";
 	}
 	
 	public Object toJSONAny() {
 		Object json = null;
 		
 		try {
 			json = new JSONTokener(toString()).nextValue();
 		} catch(JSONException e) {
 			setException(e);
 		}
 		
 		return json;
 	}
 	
 	public JSONObject toJSONObject() {
 		JSONObject json = null;
 		
 		try {
 			json = new JSONObject(toString());
 		} catch(JSONException e) {
 			setException(e);
 		}
 		
 		return json;
 	}
 	
 	public JSONArray toJSONArray() {
 		JSONArray json = null;
 		
 		try {
 			json = new JSONArray(toString());
 		} catch(JSONException e) {
 			setException(e);
 		}
 		
 		return json;
 	}
 	
 	public Bitmap toBitmap() {
 		return BitmapFactory.decodeByteArray(data, 0, data.length);
 	}
 	
 	public Exception getException() {
 		return exception;
 	}
 	
 	public void setException(Exception e) {
 		exception = e;
 	}
 	
 	public Response fromHttpConnection() {
 		Bundle parameters = request.getParameters();
 		Session session = request.getSession();
 		Session.ValidateHandler validateHandler = session.getValidateHandler();
 		HttpResponse response;
 		
 		try {
 			response = request.getConnection().send(parameters);
 			data = readData(response);
 			
 			/*
 			 * In this place session validation go on.
 			 * If session is not valid any more,
 			 * session.close() is called.
 			 */
 			if(validateHandler != null) {
 				if(!validateHandler.onValidate(this)) {
 					session.close();
 				}
 			}
 		} catch (ClientProtocolException e) {
 			exception = e;
 		} catch (IOException e) {
 			exception = e;
 		} catch(IllegalStateException e) {
 			exception = e;
 		}
 		
 		return this;
 	}
 	
 	public Request getRequest() {
 		return request;
 	}
 	
 }
