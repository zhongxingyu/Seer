/**
 * Copyright (C) 2011 by Kik Interactive
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
*/
 package com.kik.phonegap.plugin.messenger;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.util.Base64;
 import android.util.Log;
 
 import com.kik.platform.KikClient;
 import com.kik.platform.KikData;
 import com.kik.platform.KikMessage;
 import com.phonegap.api.Plugin;
 import com.phonegap.api.PluginResult;
 import com.phonegap.api.PluginResult.Status;
 
 
 public class KikMessengerPlugin extends Plugin {
 	public static final String ACTION="sendMessage";
 	public static final String INIT_ACTION="init";
 	public static final String HAS_MESSAGE="hasMessage";
 	public static final String TITLE="title";
 	public static final String DESCRIPTION="description";
 	public static final String GENERICURIS="genericURIs";
 	public static final String IPHONEURIS="iphoneURIs";
 	public static final String ANDROIDURIS="androidURIs";
 	public static final String EXTRASDICT="extras";
 	public static final String FILEURL="fileUrl";
 	public static final String FILELOCATION="fileLocation";
 	public static final String PREVIEW="preview";
 	public static final String PREVIEWURL="previewUrl";
 	private String _appID;
 	
 	@Override
 	public PluginResult execute(String action, JSONArray arg1, String arg2) {
 		// Here we are going to basically going to read the json object and proccess the response
 		
 		Log.d("KikMessangerPlugin", "Plugin Called");
 		if (action.equals(ACTION)) {
 			
 			try {
 			// The first argument is the string containing the JSONObject
 			// that we will have to parse out
 			String messageString = arg1.getString(0);
 			System.out.println(messageString);
 			JSONObject messageObject = new JSONObject(messageString);
 			String title = messageObject.getString(TITLE);
 			String desription = messageObject.getString(DESCRIPTION);
 			JSONArray gen_urls = messageObject.getJSONArray(GENERICURIS);
 			JSONArray ios_urls = messageObject.getJSONArray(IPHONEURIS);
 			JSONArray android_urls = messageObject.getJSONArray(ANDROIDURIS);
 			String preview = messageObject.getString(PREVIEW);
 			String fileLocation = messageObject.getString(FILELOCATION);
 			String previewURL = messageObject.getString(PREVIEWURL);
 			
 			KikMessage message = new KikMessage(_appID);
 			message.setText(desription);
 			message.setTitle(title);
 			
 			if ( fileLocation.length() != 0 ) {
 				try {
 					File file = new File( fileLocation );
 					message.setFile(file);
 				} catch ( IOException e ) {
 					return new PluginResult(PluginResult.Status.ERROR, "Error Attaching File");
 				}
 			}
 			if ( preview.length() != 0 ) {
 				byte [] data_bytes = Base64.decode(preview, 0);
 				message.setImage(new BitmapDrawable( BitmapFactory.decodeByteArray(data_bytes,0, data_bytes.length)));
 			} else if ( previewURL.length() != 0 ) {
 				BitmapDrawable drawable = new BitmapDrawable( previewURL );
 				message.setImage(drawable);
 			}
 			
  			for (int i = 0; i < ios_urls.length(); i++ ) {
 				message.setIphoneDownloadUri(ios_urls.getString(i));
 			}
 			
 			for (int  i = 0; i < gen_urls.length(); i++ ){
 				message.setFallbackUri(gen_urls.getString(i));
 			}
 			
 			for ( int i = 0; i < android_urls.length(); i++ ){
 				message.setAndroidDownloadUri(android_urls.getString(i));
 			}
 			
 			try {
 				final KikMessage _messageToSend = message;
 				// These will be key value pairs that we will use
 				this.ctx.runOnUiThread(new Runnable() {
 					public void run() {
 						KikClient.sendMessage(ctx, _messageToSend);
 					}
 				});
 				return new PluginResult(Status.OK );
 			} catch ( Exception e) {
 				return new PluginResult(PluginResult.Status.ERROR, "Error passing message to UI thread");
 			}
 			
 			} catch (JSONException e) {
 				// If there is an issue parsing the JSON object then
 				// we will return a bad result
 				return new PluginResult(PluginResult.Status.ERROR, "Invalid JSON input, expected JSONObject as first item");
 			}
 			// If we want to initialize the context
 		} else if ( action.equals(INIT_ACTION)) {
 			try{
 				String appID = arg1.getString(2);
 				// We just want to take the AppId from this
 				_appID = appID;
 				if (_appID.length() == 0 ) {
 					return new PluginResult(PluginResult.Status.ERROR, "Invalid App ID");
 				} else {
 					return new PluginResult(PluginResult.Status.OK);
 				}
 			} catch (JSONException e) {
 				// If there is an issue parsing the JSON object then
 				// we will return a bad result
 				return new PluginResult(PluginResult.Status.ERROR, "Invalid JSON input, expected JSONObject as first item");
 			}
 			
 		} else if ( action.equals(HAS_MESSAGE)) {
 			Intent i = this.ctx.getIntent();
 			KikData data = KikClient.getDataFromIntent(i);
 			if (data.getType() == KikData.TYPE_PICK || data.getType() == KikData.TYPE_VIEW )
 		    {
 			// We will only write back the javascript 
 			// if its a valid KikData object
 			String output = "setTimeout(function() { KikAPIClient.message_ready( '"+SerializeKikData(data)+"'); }, 0 );";
 			super.sendJavascript(output);
 		    }
 			return new PluginResult(PluginResult.Status.OK);
 		}
 		// This should not really occur
 		return new PluginResult(PluginResult.Status.INVALID_ACTION,"INVALID COMMAND");
 	}
 	
 	public String SerializeKikData(KikData data_in)
 	{
 		try {
 			JSONObject wrapper = new JSONObject();
 			JSONObject ret_object = new JSONObject();
 			int result = 0;
 			
 			if ( (data_in.getType() == KikData.TYPE_PICK))
 			{
 				result = 2;
 			}else if ( (data_in.getType() == KikData.TYPE_VIEW))
 			{
 				result = 1;
 			} else {
 				result = 0;
 			}
 			
 			ret_object.put("type",result);
 			ret_object.put("myName",data_in.getMyName());
 			ret_object.put("senderName",data_in.getSenderName());
 			ret_object.put("contentID",data_in.getConvoId());
 			
 			if ( data_in.getType() == KikData.TYPE_VIEW) {
 				JSONObject message_data = new JSONObject();
 				KikMessage msg = data_in.getMessage();
 				message_data.put("title",msg.getTitle());
 				message_data.put("description",msg.getText());
 				message_data.put("fileUrl",msg.getFileUrl());
 				ret_object.put("message", message_data);
 		
 				// We are going to put the preview image 
 				// as serialized data
 				Drawable d = msg.getPreviewImage();
 				Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
 				ByteArrayOutputStream stream = new ByteArrayOutputStream();
 				bitmap.compress(Bitmap.CompressFormat.JPEG, 100,stream);
 				byte [] bitmap_data = stream.toByteArray();
 				message_data.put("preview",new String(Base64.encode(bitmap_data, 0)));
 				
 			}
 			
 			wrapper.put("message", ret_object);
 			return wrapper.toString();
 		} catch( JSONException e) {
 			return "";
 		}
 		
 	}
 }
 
 
 
 
 
