 package edu.uw.cs.cse461.AndroidApps;
 
import java.util.List;

 import org.json.JSONObject;
 
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 import edu.uw.cs.cse461.Net.Base.NetBase;
 import edu.uw.cs.cse461.Net.Base.NetLoadableAndroidApp;
 import edu.uw.cs.cse461.Net.DDNS.DDNSException;
 import edu.uw.cs.cse461.Net.DDNS.DDNSException.DDNSAuthorizationException;
 import edu.uw.cs.cse461.Net.DDNS.DDNSException.DDNSRuntimeException;
 import edu.uw.cs.cse461.Net.DDNS.DDNSFullName;
 import edu.uw.cs.cse461.Net.DDNS.DDNSRRecord.ARecord;
 import edu.uw.cs.cse461.Net.DDNS.DDNSResolverService;
 import edu.uw.cs.cse461.Net.RPC.RPCCall;
 import edu.uw.cs.cse461.util.BackgroundToast;
 import edu.uw.cs.cse461.util.ConfigManager;
 import edu.uw.cs.cse461.util.ContextManager;
 import edu.uw.cs.cse461.util.IPFinder;
 import edu.uw.cs.cse461.util.SampledStatistic.ElapsedTime;
 
 public class PingDDNSActivity extends NetLoadableAndroidApp {
 	private static final String TAG="PingDDNSActivity";
     public static final String PREFS_NAME = "CSE461";
     
     private DDNSResolverService resolver;
 	
 	private String mMyIP;
 	private String mServer;
 	private int timeout;
 	private TextView serverBox;
 	
 	/**
 	 * A public constructor with no arguments is required to function correctly
 	 * as a NetLoadableAndroidApp. 
 	 */
 	public PingDDNSActivity() {
 		super("PingDDNS", true);
 	}
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d(TAG, "onCreate");
         // select the screen to be displayed
         setContentView(R.layout.pingtcpmessagehandler_layout);
         
         // save my context so that this app can retrieve it later (?)
         ContextManager.setActivityContext(this);
     }
     
     /**
      * Called after we've been unloaded from memory and are restarting.  E.g.,
      * 1st launch after power-up; relaunch after going Home.
      */
     @Override
     protected void onStart() {
     	super.onStart();
         Log.d(TAG, "onStart");
         
 		ConfigManager config = NetBase.theNetBase().config();
 
 		// Set initial values for the server name and port text boxes
 		// See if there are values in the config file...
 		String defaultServer = config.getProperty("echoddns.server", "cse461.cs.washington.edu");
 		int defaultTimeout = Integer.parseInt(config.getProperty("ping.sockettimeout","500"));
 
         
 		// If we saved values the user provided during the last run of this program, use those
         SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
         mServer = settings.getString("server", defaultServer );
         timeout = Integer.parseInt(settings.getString("clienttimeout", Integer.toString(defaultTimeout)));
         
 
         // Now set the text in the UI elements
     	serverBox = ((TextView)findViewById(R.id.pingtcpmessagehandler_iptext));
     	if ( serverBox != null ) serverBox.setText(mServer);
     	
     	    	
     	// Now determine our IP address and display it.
     	
     	// (You may) Have to use a background thread in Android 4.0 and beyond -- can't
     	// touch network with main thread.
         new Thread(){
         	public void run() {
                 try {
                 	mMyIP = IPFinder.getMyIP();
                 	
                 	if ( mMyIP != null ) runOnUiThread(new Runnable() {
                 		                      public void run() {
                 	    	                        TextView myIpText = (TextView)findViewById(R.id.pingtcpmessagehandler_myiptext);
                 	    	                        if ( myIpText != null ) myIpText.setText(mMyIP);
                 		                      }
                 		                 });
                 } catch (Exception e) {
                 	Log.e(TAG, "Caught exception trying to display ip/port");
                 }
         	}
         }.start();
 		
     }
 
     /**
      * Called, for example, if the user hits the Home button.
      */
     @Override
     protected void onStop() {
     	super.onStop();
     	Log.d(TAG, "onStop");
     	
     	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
     	SharedPreferences.Editor editor = settings.edit();
     	editor.putString("server", mServer);
     	editor.putString("clienttimeout", Integer.toString(timeout));
     	editor.commit();
     }
     
     /**
      * System is shutting down...
      */
     @Override
     protected void onDestroy() {
     	super.onDestroy();
     	Log.d(TAG,"onDestroy");
     }
     
     public void onGoClicked(View b) {
     	Log.d(TAG,"reached onGoClicked()");
     	
     	//update IP, port to the latest from the UI
     	if ( serverBox != null ) mServer = serverBox.getText().toString();
     	
     	//save them to our preferences
     	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
     	SharedPreferences.Editor editor = settings.edit();
     	editor.putString("server", mServer);
     	editor.commit();
 
     	// Starting Android 4.0, can't use the main thread to touch the network.
     	// So, most of your code goes inside this thread's run() method.
     	new Thread(){
     		public void run() {
     			try {
     				runOnUiThread(new Runnable() {
     					public void run() {
     						ConfigManager config = NetBase.theNetBase().config();
    						// TODO remove this testing thing
    						List<String> services = NetBase.theNetBase().loadedServiceNames();
     						resolver = (DDNSResolverService)NetBase.theNetBase().getService("ddnsresolver");
     						DDNSFullName name = new DDNSFullName(config.getProperty("net.hostname", "default.12au.cse461."));
     				        
     						ElapsedTime.start("PingDDNS");
     						
     						try {
     				        	int port = Integer.parseInt(config.getProperty("echorpc.port", "46120"));
     							resolver.register(name, port); // registers myself
     						} catch (DDNSRuntimeException e) {
     							Log.e(TAG, "DDNS Runtime Exception: " + e.getMessage());
     						} catch (DDNSAuthorizationException e1) {
     							Log.e(TAG, "Authorization exception: " + e1.getMessage());
     						} catch (DDNSException e) {
     							Log.e(TAG, "We encountered a DDNSException we were not expecting with message " + e.getMessage());
     						}
     						
     						
     						try {
     							ARecord address = resolver.resolve(mServer);
 								RPCCall.invoke(address.ip(), address.port(), "echorpc", "echo", new JSONObject().put("msg", ""));
 							} catch (Exception e) {
 								Log.e(TAG, "Exception: " + e.getMessage());
 							}
     						// Calculate the ping time and display it on the UI
     						double realTime = ElapsedTime.stop("PingDDNS");
     						String time = Double.toString(realTime);
     						if(time.length()>5){
     							time = time.substring(0, 5);
     						}
     						if(realTime==0) Log.e(TAG,"response can't take 0 time");
     						_setOutputText("Ping time taken: " + time + " ms");
     					}
     				});
     			} catch (Exception e) {
     				Log.e(TAG, "Caught exception: " + e.getMessage());
     				BackgroundToast.showToast(ContextManager.getActivityContext(), "Ping attempt failed: " + e.getMessage(), Toast.LENGTH_LONG);
     			}
     		}
     	}.start();
     }
 
     private void _setOutputText(String msg) {
     	TextView outputText = (TextView)findViewById(R.id.pingtcpmessagehandler_outputtext);
 		if ( outputText != null ) outputText.setText(msg);
     }
 }
