 package com.msse.robot_cowboy;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.Toast;
 
 public class CameraPoster extends AsyncTask<String, Void, Void> {
 	private final String TAG = CameraPoster.class.getSimpleName();
 	private static final int STATUS_OK = 200;
 	private Context context;
 
 	public CameraPoster(Context context) {
 		this.context = context;
 	}
 
 	@Override
 	protected Void doInBackground(String... params) {
 		if (params.length != 1) {
 			Log.e(TAG, "CameraPoster requires one parameter");
 			return null;
 		}
 
 		PhotoQueue queue = PhotoQueue.getInstance();
 		while (queue.peek() != null) {
 			byte[] photo = queue.poll();
 
 			HttpClient client = new DefaultHttpClient();
 			HttpPost request = new HttpPost(params[0]);
 
 			try {
 				Log.e(TAG, "URL: " + params[0]);
 
 				if (photo != null) {
 					// Write the photo to the REST endpoint
 					request.setEntity(new ByteArrayEntity(photo));
 					HttpResponse response = client.execute(request);
 					HttpEntity entity = response.getEntity();
 					InputStream inStream = entity.getContent();
 					inStream.close();
 
 					if (response.getStatusLine().getStatusCode() != STATUS_OK) {
 						Log.e(TAG, "Server returned failure");
 						Log.e(TAG, response.getStatusLine().getReasonPhrase());
 					}
 					else {
						Toast.makeText(context, "Photo Uploaded!", Toast.LENGTH_SHORT).show();
 					}
 				}
 			} catch (UnsupportedEncodingException e) {
 				Log.e(TAG, "Unsupported Encoding Exception");
 			} catch (IOException e) {
 				Log.e(TAG, "IOException");
 			}
 		}
 		return null;
 	}
 }
