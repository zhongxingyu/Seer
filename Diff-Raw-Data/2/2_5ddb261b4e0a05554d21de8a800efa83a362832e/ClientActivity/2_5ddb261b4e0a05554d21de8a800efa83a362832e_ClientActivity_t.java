 package edu.vub.at.nfcpoker.ui;
 
 import mobisocial.nfc.Nfc;
 import mobisocial.nfc.addon.BluetoothConnector;
 import edu.vub.at.nfcpoker.R;
 import android.app.Activity;
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.nfc.NfcAdapter;
 import android.os.Bundle;
 import android.provider.Settings.Secure;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ClientActivity extends Activity {
 
     private Nfc mNfc;
     private Long mLastPausedMillis = 0L;
     
     // Interactivity
     private static final boolean useIncognitoMode = true;
     private static final boolean useIncognitoLight = false;
     private static final boolean useIncognitoProxmity = true;
     private boolean incognitoMode;
     private long incognitoLight;
     private long incognitoProximity;
 	
 	// Game state
 	public static GameState GAME_STATE;
 	private int currentBet;
 	
 	// Enums
 	public enum GameState {
 	    INIT, NFCPAIRING, FLOP, TURN, RIVER
 	}
 	
     @Override
     @SuppressWarnings("unused")
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.client);
         
         // Settings
         
         // Game state
         currentBet = 0;
         GAME_STATE = GameState.INIT;
         
         // Interactivity
         if (useIncognitoMode) {
 	        incognitoMode = false;
 	        incognitoLight = -1;
 	        incognitoProximity = -1;
 	        SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
 
 	        if (useIncognitoLight) {
 		        Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
 		        if (lightSensor != null) {
 			        sensorManager.registerListener(incognitoSensorEventListener, 
 			        		lightSensor, 
 			        		SensorManager.SENSOR_DELAY_NORMAL);
 			        incognitoLight = 0;
 		        }
 	        }
 	        if (useIncognitoProxmity) {
 		        Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
 		        if (proximitySensor != null) {
 			        sensorManager.registerListener(incognitoSensorEventListener, 
 			        		proximitySensor, 
 			        		SensorManager.SENSOR_DELAY_NORMAL);
 			        incognitoProximity = 0;
 		        }
 	        }
         }
         
         // UI
         /*final Intent intent = new Intent(NfcAdapter.ACTION_TAG_DISCOVERED);
         intent.putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, AsciiNdefMessage.CreateNdefMessage(UUID));
         startActivity(intent);*/
         final ImageButton buttonAddBlack = (ImageButton) findViewById(R.id.AddBlack);
         buttonAddBlack.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	incrementBetAmount(100);
             }
         });
         final ImageButton buttonAddGreen = (ImageButton) findViewById(R.id.AddGreen);
         buttonAddGreen.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	incrementBetAmount(25);
             }
         });
         final ImageButton buttonAddBlue = (ImageButton) findViewById(R.id.AddBlue);
         buttonAddBlue.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	incrementBetAmount(10);
             }
         });
         final ImageButton buttonAddRed = (ImageButton) findViewById(R.id.AddRed);
         buttonAddRed.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	incrementBetAmount(5);
             }
         });
         final ImageButton buttonAddWhite = (ImageButton) findViewById(R.id.AddWhite);
         buttonAddWhite.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	incrementBetAmount(1);
             }
         });        
     }
 
     // Game
     private void incrementBetAmount(int value) {
     	currentBet += value;
         final EditText textCurrentBet = (EditText) findViewById(R.id.currentBet);
        textCurrentBet.setText(""+currentBet);
     }
     
     // Interactivity
     SensorEventListener incognitoSensorEventListener = new SensorEventListener() {
     	@Override
     	public void onAccuracyChanged(Sensor sensor, int accuracy) {
     		
     	}
 
     	@Override
     	public void onSensorChanged(SensorEvent event) {
     		if (event.sensor.getType()==Sensor.TYPE_LIGHT){
     			float currentReading = event.values[0];
     			if (currentReading < 10) {
     				if (incognitoLight == 0) incognitoLight = System.currentTimeMillis();
         			Log.d("Light SENSOR", "It's dark!" + currentReading);
     			} else {
     				incognitoLight = 0;
         			Log.d("Light SENSOR", "It's bright!" + currentReading);
     			}
     		}
     		if (event.sensor.getType()==Sensor.TYPE_PROXIMITY){
     			float currentReading = event.values[0];
     			if (currentReading < 1) {
     				if (incognitoProximity == 0) incognitoProximity = System.currentTimeMillis();
         			Log.d("Proximity SENSOR", "I found a hand!" + currentReading);
     			} else {
     				incognitoProximity = 0;
         			Log.d("Proximity SENSOR", "All clear!" + currentReading);
     			}
     		}
     		if ((incognitoLight != 0) && (incognitoProximity != 0)) {
 				incognitoMode = true;
     			runOnUiThread(new Runnable() {
     	            @Override
     	            public void run() {
     	            	Toast.makeText(ClientActivity.this, "INCOGNITO ENABLED", Toast.LENGTH_SHORT).show();
     	            }
     	        });
     		} else {
 				if (incognitoMode) {
 					incognitoMode = false;
 	    			runOnUiThread(new Runnable() {
 	    	            @Override
 	    	            public void run() {
 	    	            	Toast.makeText(ClientActivity.this, "DISABLE INCOGNITO", Toast.LENGTH_SHORT).show();
 	    	            }
 	    	        });
 				}
     		}
     	}
     };
 }
