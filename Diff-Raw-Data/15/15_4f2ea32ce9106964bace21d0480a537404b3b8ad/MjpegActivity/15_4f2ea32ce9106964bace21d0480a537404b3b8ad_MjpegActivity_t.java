 package com.camera.simplemjpeg.imwatch;
 
 import java.io.IOException;
 import java.net.URI;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 
 public class MjpegActivity extends Activity {
 	private static final boolean DEBUG=false;
     private static final String TAG = "MJPEG";
 
     private static final int REQUEST_NW_SETTING = 0;
 
     private int width = 320;
     private int height = 240;
     
 	private int ip_ad1 = 192;
 	private int ip_ad2 = 168;
 	private int ip_ad3 = 44;
 	private int ip_ad4 = 1;
 	private int ip_port = 8080;
 	private String ip_command = "videofeed";
 	String URL;
 	
 	private boolean suspending = false;
 	
     private MjpegView mv;
     private Button config_button;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
         WindowManager.LayoutParams.FLAG_FULLSCREEN);
         setContentView(R.layout.main);
         
         SharedPreferences preferences = getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
         width = preferences.getInt("width", width);
         height = preferences.getInt("height", height);
         ip_ad1 = preferences.getInt("ip_ad1", ip_ad1);
 		ip_ad2 = preferences.getInt("ip_ad2", ip_ad2);
 		ip_ad3 = preferences.getInt("ip_ad3", ip_ad3);
 		ip_ad4 = preferences.getInt("ip_ad4", ip_ad4);
 		ip_port = preferences.getInt("ip_port", ip_port);
 		ip_command = preferences.getString("ip_command", ip_command);
         
         config_button = (Button)findViewById(R.id.network_configuration_start);
         config_button.setOnClickListener(
         		new View.OnClickListener(){
         			public void onClick(View view){     
         				
         				Intent nw_intent = new Intent(MjpegActivity.this, SettingsActivity.class);
         				nw_intent.putExtra("width", width);
         				nw_intent.putExtra("height", height);
         	        	nw_intent.putExtra("ip_ad1", ip_ad1);
         	        	nw_intent.putExtra("ip_ad2", ip_ad2);
         	        	nw_intent.putExtra("ip_ad3", ip_ad3);
         	        	nw_intent.putExtra("ip_ad4", ip_ad4);
         	        	nw_intent.putExtra("ip_port", ip_port);
         	        	nw_intent.putExtra("ip_command", ip_command);
         				startActivityForResult(nw_intent, REQUEST_NW_SETTING);
         			}
         		}        		
         );
         
         StringBuilder sb = new StringBuilder();
         String s_http = "http://";
         String s_dot = ".";
         String s_colon = ":";
         String s_slash = "/";
         sb.append(s_http);
         sb.append(ip_ad1);
         sb.append(s_dot);
         sb.append(ip_ad2);
         sb.append(s_dot);
         sb.append(ip_ad3);
         sb.append(s_dot);
         sb.append(ip_ad4);
         sb.append(s_colon);
         sb.append(ip_port);
         sb.append(s_slash);
         sb.append(ip_command);        
         URL = new String(sb);
 
         mv = (MjpegView) findViewById(R.id.mv);
         
 		if(mv!=null){
 			mv.setResolution(width, height);
 		}
         
         new DoRead().execute(URL);
     }
 
     
     public void onResume() {
     	if(DEBUG) Log.d(TAG,"onResume()");
         super.onResume();
         if(mv!=null){
         	if(suspending){
         		suspending = false;
         		new RestartApp().execute();
         	}
         }
 
     }
 
     public void onStart() {
     	if(DEBUG) Log.d(TAG,"onStart()");
         super.onStart();
     }
     public void onPause() {
     	if(DEBUG) Log.d(TAG,"onPause()");
         super.onPause();
         if(mv!=null){
        	if(mv.isStreaming()){
		        mv.stopPlayback();
		        suspending = true;
        	}
         }
     }
     public void onStop() {
     	if(DEBUG) Log.d(TAG,"onStop()");
         super.onStop();
     }
 
     public void onDestroy() {
     	if(DEBUG) Log.d(TAG,"onDestroy()");
         super.onDestroy();
     }
     
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch (requestCode) {
     	case REQUEST_NW_SETTING:
     		if (resultCode == Activity.RESULT_OK) {
     			width = data.getIntExtra("width", width);
     			height = data.getIntExtra("height", height);
 	    		ip_ad1 = data.getIntExtra("ip_ad1", ip_ad1);
 	    		ip_ad2 = data.getIntExtra("ip_ad2", ip_ad2);
 	    		ip_ad3 = data.getIntExtra("ip_ad3", ip_ad3);
 	    		ip_ad4 = data.getIntExtra("ip_ad4", ip_ad4);
 	    		ip_port = data.getIntExtra("ip_port", ip_port);
 	    		ip_command = data.getStringExtra("ip_command");
 	    			    		
 	    		SharedPreferences preferences = getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
 				SharedPreferences.Editor editor = preferences.edit();
 				
 				if(mv!=null){
 					mv.setResolution(width, height);
 				}
 				
 				editor.putInt("width", width);
 				editor.putInt("height", height);
 				editor.putInt("ip_ad1", ip_ad1);
 				editor.putInt("ip_ad2", ip_ad2);
 				editor.putInt("ip_ad3", ip_ad3);
 				editor.putInt("ip_ad4", ip_ad4);
 				editor.putInt("ip_port", ip_port);
 				editor.putString("ip_command", ip_command);
 				
 				editor.commit();
 
 				new RestartApp().execute();
     		}
     		break;
         }
     }
     
     public class DoRead extends AsyncTask<String, Void, MjpegInputStream> {
         protected MjpegInputStream doInBackground(String... url) {
             //TODO: if camera has authentication deal with it and don't just not work
             HttpResponse res = null;
             DefaultHttpClient httpclient = new DefaultHttpClient();  
             HttpParams httpParams = httpclient.getParams();
             HttpConnectionParams.setConnectionTimeout(httpParams, 5*1000);
             Log.d(TAG, "1. Sending http request");
             try {
                 res = httpclient.execute(new HttpGet(URI.create(url[0])));
                 Log.d(TAG, "2. Request finished, status = " + res.getStatusLine().getStatusCode());
                 if(res.getStatusLine().getStatusCode()==401){
                     //You must turn off camera User Access Control before this will work
                     return null;
                 }
                 return new MjpegInputStream(res.getEntity().getContent());  
             } catch (ClientProtocolException e) {
                 e.printStackTrace();
                 Log.d(TAG, "Request failed-ClientProtocolException", e);
                 //Error connecting to camera
             } catch (IOException e) {
                 e.printStackTrace();
                 Log.d(TAG, "Request failed-IOException", e);
                 //Error connecting to camera
             }
             return null;
         }
 
         protected void onPostExecute(MjpegInputStream result) {
             mv.setSource(result);
             if(result!=null) result.setSkip(1);
             mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
             mv.showFps(false);
         }
     }
     
 	public class RestartApp extends AsyncTask<Void, Void, Void> {
 		protected Void doInBackground(Void... v) {
 			MjpegActivity.this.finish();
 			return null;
 		}
 
 		protected void onPostExecute(Void v) {
 			startActivity((new Intent(MjpegActivity.this, MjpegActivity.class)));
 		}
 	}
 }
