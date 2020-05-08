 /*******************************************************************************
  * Copyright (c) 2012 Moarub Oy.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Moarub Oy - initial API and implementation
  ******************************************************************************/
 package com.moarub.kipptapi;
 
 import java.io.IOException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.protocol.HTTP;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 public class CreateClip extends AsyncTask<String, Void, HttpResponse> {
 
 	private String fClipUrl;
 	private ClipCreatedListener fListener;
 	private String fNote;
 	private boolean fReadLater;
 	private boolean fStar;
 	private String fTitle;
 
 	public CreateClip(String clip, ClipCreatedListener listener) {
 		fClipUrl = clip;
 		fListener = listener;
 	}
 
 	public void addNote(String fNotes) {
 		fNote = fNotes;
 	}
 
 	public void addTitle(String title) {
 		fTitle = title;
 	}
 
 	@Override
 	protected HttpResponse doInBackground(String... params) {
 		String username = params[0];
 		String token = params[1];
 		String reqTokenUrl = "https://kippt.com/api/clips/";
 		DefaultHttpClient client = new DefaultHttpClient();
 
 		try {
 			HttpPost request = new HttpPost(reqTokenUrl);
 			request.addHeader("X-Kippt-Username", username);
 			request.addHeader("X-Kippt-API-Token", token);
 			request.addHeader("X-Kippt-Client", "ShareMore for Android,sharemore@moarub.com,http://moarub.com/sharemore");
 			
 			JSONObject job = new JSONObject();
 			job.put("url", fClipUrl);
 			if (fNote != null) {
 				job.put("notes", fNote);
 			}
 			if (fTitle != null) {
 				job.put("title", fTitle);
 			}
 			if(fReadLater) {
 				job.put("is_read_later", true);
 			}
 			if(fStar) {
				job.put("is_star", true);
 			}
 
 			StringEntity se = new StringEntity(job.toString(), "UTF-8");
 			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
 					"application/json"));
 			
 			Log.d("Sending clip JSON", job.toString());
 
 			request.setEntity(se);
 
 			return client.execute(request);
 		} catch (ClientProtocolException e) {
 			Log.d("ApiTokenFailure", "Can't fetch API Token");
 			return null;
 		} catch (IOException e) {
 			Log.d("ApiTokenFailure", "Can't fetch API Token");
 			return null;
 		} catch (JSONException e) {
 			Log.d("ApiTokenFailure", "Can't fetch API Token");
 			return null;
 		}
 	}
 
 
 	@Override
 	protected void onPostExecute(HttpResponse result) {
 		StatusLine sl = result.getStatusLine();
 
 		Log.d("CreateClip", sl.getStatusCode() + " " + sl.getReasonPhrase());
 		Log.d("CreateClip", KipptAPIHelpers.getResponseString(result).toString());
 
 		fListener.onClipCreated(sl.getStatusCode());
 
 	}
 
 	public void setStar(boolean star) {
 		fStar = star;
 	}
 
 	public void setReadLater(boolean checked) {
 		fReadLater = checked;
 	}
 
 }
