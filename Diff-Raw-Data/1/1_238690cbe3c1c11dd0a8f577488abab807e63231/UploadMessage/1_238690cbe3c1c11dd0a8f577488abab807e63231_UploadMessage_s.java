 package com.android.task.tools;
 
 
 import android.net.Uri;
import android.provider.MediaStore;
 import android.util.Log;
 import android.webkit.ValueCallback;
 
 public class UploadMessage {
 
 	final static String TAG = UploadMessage.class.getName();
 	private static ValueCallback<Uri> mUri = null;
 	
 	public static void set_upload_uri(ValueCallback<Uri> u)
 	{
 		UploadMessage.mUri = u;
 	}
 	public static ValueCallback<Uri> get_upload_uri()
 	{
 		return UploadMessage.mUri;
 	}
 	public static void set_upload_message(Uri file_uri)
 	{
 		if (UploadMessage.get_upload_uri() != null)
 		{
 			Log.d(TAG,file_uri== null?"null":file_uri.toString());
 			UploadMessage.get_upload_uri().onReceiveValue(file_uri);
 		}
 		UploadMessage.set_upload_uri(null);
 	}
 
 
 }
