 package fr.upsilon.inventirap;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 
 public class RequestActivity extends Activity {
 	private Context context;
 	private Runnable runnable;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.request_result);
         
         context = this;
         // get decoded value
         String decoded_value_internal = getString(R.string.DECODED_VALUE);
         final String decoded_value = getIntent().getExtras().getString(decoded_value_internal);
         
         // get server ip
         String name = getResources().getString(R.string.app_name);
         SharedPreferences prefs = context.getSharedPreferences(name, MODE_PRIVATE);
         final String server_ip = prefs.getString(getString(R.string.SERVER_IP), "");
         
         runnable = new Runnable(){
             public void run() {
                 Log.d(context.getClass().getName(), "requesting to "+server_ip);
                 String result = "";
 				try {
 					result = WebServicesTools.getXML(context, server_ip, decoded_value);
 					Log.d("", "get " + result);
				} catch (Exception e) {
 					setResult(MainActivity.REQUEST_BAD_ADDRESS);
                 	finish();
 				}
 				
 				if (result.startsWith("{\"materials\":[]}")) {
 					setResult(MainActivity.REQUEST_NO_MATERIAL);
 				} else {         
 	                if (!WebServicesTools.JSONFromString(result)) {
 	                	setResult(MainActivity.REQUEST_BADLY_FORMATTED);
 	                } else {
 		                Log.d(context.getClass().getName(), "JSON received and decoded !!");
 		
 		                Intent intent = new Intent(context, DisplayResultActivity.class);
 		                startActivity(intent);
 	                }
 				}
                 
                 finish();
                 
             }
         };
         Thread thread =  new Thread(null, runnable, "InventirapServerRequest");
         thread.start();
 
     }
 }
