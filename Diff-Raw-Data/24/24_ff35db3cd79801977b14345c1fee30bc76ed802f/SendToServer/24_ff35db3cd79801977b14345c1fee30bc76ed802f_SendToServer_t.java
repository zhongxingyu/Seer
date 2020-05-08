 package de.binaervarianz.sendtowebdav;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.apache.http.HttpException;
 import org.apache.http.client.ClientProtocolException;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.widget.Toast;
 
 public class SendToServer extends Activity {
 	private final String TAG = this.getClass().getName();
 	
 	private WebDAVhandler httpHandler;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// read preferences
 		SharedPreferences settings = getSharedPreferences(ConfigWebDAV.PREFS_PRIVATE, Context.MODE_PRIVATE);
 		String serverURI = settings.getString(ConfigWebDAV.KEY_SERVER_URI, "https://");
 		boolean trustAllSSLCerts = settings.getBoolean(ConfigWebDAV.KEY_TRUSTALLSSL, false);
 		String user = settings.getString(ConfigWebDAV.KEY_USERNAME, "");
 		String pass = settings.getString(ConfigWebDAV.KEY_PASSWORD, "");
 
 		try {
 			new URI(serverURI);
 		} catch (URISyntaxException e) {
 			Log.e(TAG, "URISyntaxException: "+e);
 			// TODO: user error message (maybe also do this check when testing/saving) -- is inherently done!?
 			// TODO: alert dialog notifying users of broken url and redirecting them to the config app
 			// "Please provide a valid Server URI in the Config App first"
 			return;
 		}
 		
 		httpHandler = new WebDAVhandler(serverURI, user, pass);
 		httpHandler.setTrustAllSSLCerts(trustAllSSLCerts);
 
 		Intent intent = getIntent();
 		Bundle extras = intent.getExtras();
 		
 		if (intent.getAction().equals(Intent.ACTION_SEND) && extras != null) {
 		//TODO figure out what's to be send
 		
 			//this is for debugging and getting information on new media types
 			String type = intent.resolveType(this);
 			//URLS give text/plain, images image/jpeg ....
 			Log.d(TAG, type);
 			for (String s : extras.keySet()) {
 				Log.d(TAG, s);
 				//Urls give EXTRA_TEXT, images EXTRA_STREAM
 			}
 			if (intent.getDataString() != null) {
 				Log.d(TAG, intent.getDataString());	
 				//no data so far attached to the intents
 			}			
 			/// end of debug, resume normal operation			
 			
 			String url = "";
			SimpleDateFormat dateformater = new SimpleDateFormat("yyyyMMddHHmmss"); 
 			try {				
 				if (extras.containsKey(Intent.EXTRA_TEXT)) {	// simple text like URLs
 					url += extras.getString(Intent.EXTRA_TEXT);
					httpHandler.putFile("URL-"+dateformater.format(new Date())+".txt", "", url);
					
 				} else if (extras.containsKey(Intent.EXTRA_STREAM)) {	// binary files
 					Uri contentUri = (Uri) extras.get(Intent.EXTRA_STREAM);
					
 					//debug
 					Log.d(TAG, contentUri.toString());
 					
 					String filePath = "";
 					// there are real file system paths and logical content URIs
					// I've so far only tested the first kind [chaos]
 					if (contentUri.toString().startsWith("content:")) {
 						filePath = this.getRealPathFromURI(contentUri);
 					} else {
 						filePath = contentUri.getEncodedPath();
 					}
 					// create a basename and suffix out of the type identifier
 					String basename = type.toUpperCase().charAt(0) + type.substring(1, type.indexOf('/'));
					String suffix = "." + type.substring(type.indexOf('/')+1);
					String name = basename + "-" + dateformater.format(new Date())+ suffix;
					
					//debug
					Log.d(TAG, name);
					
 					//TODO actually we should keep the original name for binary files, 
 					//     but we would have to check for existence on the server first
					//     as WebDAV defaults to 'replace' as far as I have read
 					httpHandler.putBinFile( name, "", filePath, type);
 			}
 			} catch (ClientProtocolException e) {
 				Toast.makeText(this, this.getString(R.string.app_name) + ": ClientProtocolException: "+e, Toast.LENGTH_LONG).show();
 				Log.e(TAG, "ClientProtocolException: "+e);
 				return;
 			} catch (IOException e) {
 				Toast.makeText(this, this.getString(R.string.app_name) + ": IOException: "+e, Toast.LENGTH_LONG).show();
 				Log.e(TAG, "IOException: "+e);
 				return;
 			} catch (IllegalArgumentException e) {
 				Toast.makeText(this, this.getString(R.string.app_name) + ": IllegalArgumentException: "+e, Toast.LENGTH_LONG).show();
 				Log.e(TAG, "IllegalArgumentException: "+e);
 				return;
 			} catch (HttpException e) {
 				Toast.makeText(this, this.getString(R.string.app_name) + ": HttpException: "+e, Toast.LENGTH_LONG).show();
 				Log.e(TAG, "HttpException: "+e);
 				return;
 			}
 			
 			Toast.makeText(this, R.string.data_send, Toast.LENGTH_SHORT).show();
 		}
 		
 	}
 	/**
 	 * convert a <content:> URI (like from the gallery) to an absolute path
 	 * 
 	 * This is stolen from http://stackoverflow.com/questions/3401579/android-get-filename-and-path-from-uri-from-mediastore
 	 * @param contentUri
 	 * @return String with absolute path to file
 	 */
 	private String getRealPathFromURI(Uri contentUri) {
         String[] proj = { MediaStore.Images.Media.DATA };
         Cursor cursor = managedQuery(contentUri, proj, null, null, null);
         int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
         cursor.moveToFirst();
         return cursor.getString(column_index);
     }
 }
