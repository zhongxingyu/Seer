 package org.societies.thirdpartyservices.ijacket;
 
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.TimeoutException;
 
 
 import org.societies.android.api.cis.SocialContract;
 import org.societies.thirdpartyservices.ijacket.com.BluetoothConnection;
 import org.societies.thirdpartyservices.ijacket.com.ComLibException;
 import org.societies.thirdpartyservices.ijacket.com.ConnectionListener;
 import org.societies.thirdpartyservices.ijacket.com.ConnectionMetadata;
 import org.societies.thirdpartyservices.ijacket.com.ConnectionMetadata.DefaultServices;
 
 
 
 import android.app.Activity;
 import android.content.ActivityNotFoundException;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TableLayout;
 import android.widget.Toast;
 
 public class JacketMenuActivity extends Activity {
 
 	
 	static final int MAX_RETRY = 5;
 	
 	int retrycounter = 0;
 	// TODO: update for content uris from babak
 	
 	
 	private TableLayout layout;
 	private LedButton ledButton;
 	private SpeakerButton speakerButton;
 	private LCDButton lcdButton;
 	private VibrationButton vibrationButton;
 	
 	private Button connectButton;
 	
 	
 	BluetoothConnection con = null;
 	ActivityContentObserver actObs = null;
 	private Handler obsHandler = new Handler();
 	
 	private static final String LOG_TAG = JacketMenuActivity.class.getName();
 	
 	
 	public static final String METHOD_TAG = "method";
 	public static final String ENABLE_BUTTONS = "enableButtons";
 	
 	private myConList conList = new myConList();
 	
 	/*Handler handler = new Handler() {
 		  @Override
 		  public void handleMessage(Message msg) {
 			  Bundle b = msg.getData();
 			  String method =  b.getString(METHOD_TAG, "");
 			  if(method.isEmpty()){
 				  Log.d(LOG_TAG, "empty method on msg handler"); 
 			  }
 			  else{
 				  if(b.equals(ENABLE_BUTTONS))
 					  enableButtons();  
 			  }
 			  
 			  
 		     }
 		 };*/
 	
 	   /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
 
      Log.d(LOG_TAG, "on create JacketMenu");
 
      registerContentObservers();
      
 
      
      layout = new TableLayout(this);
      layout.setLayoutParams( new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT) );
      layout.setOrientation(TableLayout.VERTICAL);
      
      ledButton = new LedButton(JacketMenuActivity.this);
      addButton(ledButton);
      speakerButton = new SpeakerButton(JacketMenuActivity.this);
      addButton(speakerButton);
      vibrationButton = new VibrationButton(JacketMenuActivity.this);
      addButton(vibrationButton);
      lcdButton = new LCDButton(JacketMenuActivity.this);
      addButton(lcdButton);
      connectButton = new Button(this);
      connectButton.setText("Reconnect");
      connectButton.setOnClickListener( new View.OnClickListener() {
          public void onClick(View view) {
         	 connectBT();
          }
      } );
      addButton(connectButton);
      
      Log.d(LOG_TAG, "done with buttons");
      
      connectBT();
     
 
 
      
      super.setContentView(layout);         
  }
  
  	boolean connectBT(){
  		IJacketApp appState = (IJacketApp) (getApplication());
  		// first I retrieve from Application
  		if(null == con){
  			
  			con = appState.getCon();
  			Log.d("LOG_TAG", "con got from application" );
  		}
  		
  		Log.d(LOG_TAG, "starting connect");
  		if(null != con){
  			if(con.isConnected()){ // double check if it is really disconnected
  				Log.d("LOG_TAG", "connect instruction when we were already connected" );
 				retrycounter = 0;
 				return true;
  			}else{
  				Log.d("LOG_TAG", "con is not null but we are disconnected..." );
  			}
 
        	}
  		else{ // con doesnt exist
  			SharedPreferences mypref = getSharedPreferences(IJacketApp.PREF_FILE_NAME, MODE_PRIVATE);
 			String mac = mypref.getString(IJacketApp.MAC_PREFERENCE_TAG, "");
 			if(mac.isEmpty()){
 				quickToastMessage("Go back and scan the jacket again");
 				Log.d("LOG_TAG", "no mac stored on preferences" );
 				return false;
 			}
 			try {            	
 				con = new BluetoothConnection(mac, this, conList);
 				appState.setCon(con);
 			} catch (Exception e) {
 				Log.d("LOG_TAG", "exception trying to create connection" );
 				quickToastMessage(e.getMessage());
 	        	return false;
 			}
  		}
  		
 		
         	// get mac from preferences
         	
         	Log.d("LOG_TAG", "trying to connect" );
         	retrycounter++;
         	con.connect();
 
 	
 		
 		return true;
  	} 
  
  @Override
  protected void onStart() {
      super.onStart();
      // The activity is about to become visible.
  }
  @Override
  protected void onResume() {
      super.onResume();
      Log.d( LOG_TAG, "on resume" );
      // The activity has become visible (it is now "resumed").
  }
  @Override
  protected void onPause() {
      super.onPause();
      Log.d( LOG_TAG, "on pause called" );
      // Another activity is taking focus (this activity is about to be "paused").
  }
  @Override
  protected void onStop() {
      super.onStop();
      // The activity is no longer visible (it is now "stopped")
      Log.d( LOG_TAG, "on stop called" );
      //disconnect();
 	 //unregisterContentObservers();
  }
  @Override
  protected void onDestroy() {
      super.onDestroy();
      Log.d( LOG_TAG, "on destroy called" );
      disconnect();
 	 unregisterContentObservers();
  }
  
  
  private void addButton(final Button button) {
      this.runOnUiThread(new Runnable() {
           public void run() {
         	  button.setEnabled(false);
           	layout.addView(button);
           }
      });
   }
  
  private void buttonsOnConnect() {
      this.runOnUiThread(new Runnable() {
           public void run() {
 				ledButton.setEnabled(true);
 				vibrationButton.setEnabled(true);
 				speakerButton.setEnabled(true);
 				connectButton.setEnabled(false);
 				lcdButton.setEnabled(true);
           }
      });
   }
  
  private void buttonsOnDisconnect() {
      this.runOnUiThread(new Runnable() {
           public void run() {
 				ledButton.setEnabled(false);
 				vibrationButton.setEnabled(false);
 				speakerButton.setEnabled(false);
 				lcdButton.setEnabled(false);
 				connectButton.setEnabled(true);
           }
      });
   }
  
  
  private void disconnect(){
 		if(con != null) con.disconnect();
 		con = null;
 		IJacketApp appState = (IJacketApp) (getApplication());
 		appState.setCon(null);
 		buttonsOnDisconnect();
   }
 
  
  	private void registerContentObservers() {
 	  ContentResolver cr = getContentResolver();
 	  
 	  actObs = new ActivityContentObserver( obsHandler );
 	  Uri activURI = Uri.parse(SocialContract.AUTHORITY_STRING + SocialContract.UriPathIndex.COMMUNITY_ACTIVITIY);
 	  cr.registerContentObserver(activURI , true, actObs );
 	}
 
 	private void unregisterContentObservers() {
 	  ContentResolver cr = getContentResolver();
 	  if( actObs != null ) {	
 		cr.unregisterContentObserver( actObs );
 		actObs = null;
 	  }
 	}
 
  /**
   * SERVICE_LED
   */
  private class LedButton extends Button implements View.OnClickListener{
  	boolean ledIsToggled;
  	int pin;
  	
 		public LedButton(Context context) {
 			super(context);
 			ledIsToggled = false;
 			setOnClickListener(this);
 			setText("Toggle LED ");
 		}
 		
 		public void setPin(int i){
 			this.pin = i;
 		}
 
 		public void onClick(View v) {
 			ledIsToggled = !ledIsToggled;
 			try {
 				
 				con.write(pin, ledIsToggled, false);
 			} catch (TimeoutException e) {
 				Log.d(LOG_TAG, "exception on led on click");
 				disconnect();
 			}
 		}
  	
  }
  
  /**
   * SERVICE_SPEAKER
   */
  private class LCDButton extends Button implements View.OnClickListener {
  	int timesClicked;
  	
 		public LCDButton(Context context) {
 			super(context);
 			setOnClickListener(this);
 			setText("Print \"Hello World\"");
 		}
 
 		public void onClick(View v) {
 			try {
 				con.print("Hello World! (" + timesClicked++ + ")", false);
 			} catch (TimeoutException e) {
 				Log.d(LOG_TAG, "exception on lcd on click");
 				disconnect();
 			}
 		}
  	
  }
  
  /**
   * SERVICE_SPEAKER
   */
  private class SpeakerButton extends Button implements View.OnClickListener {
  	
 		public SpeakerButton(Context context) {
 			super(context);
 			setOnClickListener(this);
 			setText("Play Sound");
 		}
 		
 
 		public void onClick(View v) {
 			try {
 				con.data(new byte[]{100, 75, 52, 15}, false);
 			} catch (TimeoutException e) {
 				Log.d(LOG_TAG, "exception on speaker on click");
 				disconnect();
 			}
 		}
  	
  }
  
  /**
   * Activity observer
   */
  
  
 	class ActivityContentObserver extends ContentObserver {
 		  public ActivityContentObserver( Handler h ) {
 			super( h );
 		  }
 
 		  public void onChange(boolean selfChange) {
 			Log.d( LOG_TAG, "null onchange version called" );
 			onChange(selfChange, null);
 		  }
 		  
 		  @Override
 		  public void onChange(boolean selfChange, Uri uri) {
 			  if(null == uri) {
 				  Log.d( LOG_TAG, "null uri" );
 				  return;
 			  }
 			  Log.d( LOG_TAG, "uri path " +  uri.getPath()); 
 		     try{
 		    	 long row = ContentUris.parseId(uri);
 		    	 String mSelectionClause = SocialContract.CommunityActivity._ID + " = ?";
 		    	 String[] mSelectionArgs = {Long.toString(row)};
 		    	 ContentResolver cr = getContentResolver();
 		    	 Uri otherUri =  Uri.parse(SocialContract.AUTHORITY_STRING + SocialContract.UriPathIndex.COMMUNITY_ACTIVITIY);
 		    	 Log.d("LOG_TAG", "test " +  uri.getAuthority() + uri.getPath());
 		    	 Cursor cursor = cr.query(otherUri,null,mSelectionClause,mSelectionArgs,null);
 				if (cursor != null && cursor.getCount() >0) {
 				    while (cursor.moveToNext()) {
 				    	String actor = cursor.getString(cursor.getColumnIndex(SocialContract.CommunityActivity.GLOBAL_ID_ACTOR));
 				        Log.d("LOG_TAG", "found activity " + actor);
				        con.print("activity" + actor, false);
 				    }
 				} else {
 					Log.d(LOG_TAG, "empty CIS list query result");
 				}
 				if(null != cursor) cursor.close();
 			}catch (Exception e) {
 				// TODO Auto-generated catch block
 				Log.d(LOG_TAG, "exception in the create");
 				e.printStackTrace();
 			}
 			  
 			  
 		  }
 		  
 		}
  
  
  
  /**
   * SERVICE_VIBRATION
   */
  private class VibrationButton extends Button implements View.OnClickListener {
  	Timer timer = new Timer();
  	int pin;
  	
 		public VibrationButton(Context contextn) {
 			super(contextn);
 			setOnClickListener(this);
 			setText("Vibration");
 		}
 
 		public void setPin(int i){
 			this.pin = i;
 		}
 		
 		public void onClick(View v) {
      	timer.schedule(new TimerTask(){
      		public void run(){
              	try {
              		
              		//Vibrate mobile
              		Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
              		vib.vibrate(2000);
              		
              		
              		//Vibrate remote module
 					con.write(pin, true, false);
                  	Thread.sleep(2000);
                  	con.write(pin, false, false);
                  	
 					} 
              	catch (Exception e) {
              		 Log.d(LOG_TAG, "exception on vibration on click");
                      quickToastMessage(e.getMessage());
                      disconnect();
 					}
      		}
      	}, 0);
 		}
  }
 
  
  private void quickToastMessage(final String message) {
      this.runOnUiThread(new Runnable() {
          public void run() {
              Toast.makeText(JacketMenuActivity.this, message, Toast.LENGTH_SHORT).show();
          }
      });
  }
  
  
 
  public class myConList implements ConnectionListener {
 
  	
  	//private Activity parentActivity;
  	
  	public myConList(){
  		super();
  		//this.parentActivity = parentActivity;
  		Log.d(LOG_TAG, "conlist created");
  	}
  	
      public void onConnect(BluetoothConnection bluetoothConnection) {
      	retrycounter = 0;
     	 Log.d(LOG_TAG, "Blueetooth onconnect");
          quickToastMessage("Connected! (" + bluetoothConnection.toString() + ")");
          
          //Enable buttons for every service found
          ConnectionMetadata meta = bluetoothConnection.getConnectionData();
 			for(String service : meta.getServicesSupported()) {
 				Integer pins[] = meta.getServicePins(service);
 				
 				//Pin controlled button
 				if(pins.length > 0) {
 					// ILL just add one button of each
 					
 					if(service.equals(DefaultServices.SERVICE_LED_LAMP.name())){
 						ledButton.setPin(pins[0]);
 					}
 					if(service.equals(DefaultServices.SERVICE_VIBRATION.name())){
 						vibrationButton.setPin(pins[0]);
 					} 
 					if(service.equals(DefaultServices.SERVICE_SPEAKER.name())){
 							;
 					} 
 				}
 				
 				
 			}
 			Log.d(LOG_TAG, "finished with the buttons");
 			buttonsOnConnect();
 			// TODO: do comething about the conenct
          
      }
 
      public void onConnecting(BluetoothConnection bluetoothConnection) {
          quickToastMessage("Connecting");
      }
 
      public void onDisconnect(BluetoothConnection bluetoothConnection) {
     	 Log.d(LOG_TAG, "on disconnect");
     	 
     	 disconnect();
     	 quickToastMessage("Disconnected");
     	 
      	/*if(retrycounter < MAX_RETRY){
      		
 				quickToastMessage("Disconnected, going to retry " + retrycounter);
 				Log.d(LOG_TAG, "Disconnected, going to retry" + retrycounter);
 				BtConnectTask bt = new BtConnectTask();
 				Timer myTimer = new Timer();
 				myTimer.schedule(bt, 10000);
      	}else{
              quickToastMessage("Disconnected for good");
              Log.d(LOG_TAG, "Disconnected for good");
              
      	}*/
     	 
      }
      
      class BtConnectTask extends TimerTask {
 		  public void run() {
 			  connectBT();
 
 		  }
 		}
  }
  
 }
