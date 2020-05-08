 //
 // Copyright 2012 Shaun Simpson
 // shauns2029@gmail.com
 //
 
 package uk.co.immutablefix.ClockControl;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.RadioButton;
 import android.widget.TextView;
 
 public class ClockControlActivity extends Activity {
 	String hostname = "";
 	String weather = "";
 	Boolean running = true, paused = true;
 
 	DnssdDiscovery dns;
 	UDP udp;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
        
         dns = new DnssdDiscovery(getBaseContext());
         dns.init();
         
         udp = new UDP();
         
         
         //  Log.d("Events", "Starting ... ");
     	
     	final TextView txtPlaying = (TextView) findViewById(R.id.txt_playing);	    
 
         Button btnVolUp = (Button) findViewById(R.id.btn_volumeup);
 	    btnVolUp.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				try {
 					udp.sendUDPMessage(getTargetIp(hostname), 44558, "CLOCK:VOLUP");
 					//  Log.d("Events", "VolUp");
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});	
 
 	    Button btnVolDown = (Button) findViewById(R.id.btn_volumedown);
 	    btnVolDown.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				try {
 					udp.sendUDPMessage(getTargetIp(hostname), 44558, "CLOCK:VOLDOWN");
 					//  Log.d("Events", "VolDown");
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});	
 	
 	    Button btnNext = (Button) findViewById(R.id.btn_next);
 	    btnNext.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				try {
 					udp.sendUDPMessage(getTargetIp(hostname), 44558, "CLOCK:NEXT");
 					//  Log.d("Events", "Next");
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});	
 	
 	    Button btnMusic = (Button) findViewById(R.id.btn_music);
 	    btnMusic.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				try {
 					udp.sendUDPMessage(getTargetIp(hostname), 44558, "CLOCK:MUSIC");
 					//  Log.d("Events", "Music");
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});	
 	
 	    Button btnSleep = (Button) findViewById(R.id.btn_sleep);
 	    btnSleep.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				try {
 					udp.sendUDPMessage(getTargetIp(hostname), 44558, "CLOCK:SLEEP");
 					//  Log.d("Events", "Sleep");
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});	
 	
 	    Button btnMeditation = (Button) findViewById(R.id.btn_meditation);
 	    btnMeditation.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				try {
 					udp.sendUDPMessage(getTargetIp(hostname), 44558, "CLOCK:MEDITATION");
 					//  Log.d("Events", "Meditation");
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});	
 
 	    Button btnPause = (Button) findViewById(R.id.btn_pause);
 	    btnPause.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				try {
 					udp.sendUDPMessage(getTargetIp(hostname), 44558, "CLOCK:PAUSE");
 					//  Log.d("Events", "Sleep");
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 			}
 		});	
 	    
 	    RadioButton rbtnClock1 = (RadioButton) findViewById(R.id.radioButton1);
 	    RadioButton rbtnClock2 = (RadioButton) findViewById(R.id.radioButton2);	    
 	    
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		
 		rbtnClock1.setText(prefs.getString("clock_name", (String) rbtnClock1.getText()));
 		rbtnClock2.setText(prefs.getString("clock2_name", (String) rbtnClock2.getText()));
 		
 	    rbtnClock1.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 				hostname = prefs.getString("clock_address", hostname);
 				
 			}
 		});	
 	    
 	    rbtnClock2.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 				hostname = prefs.getString("clock2_address", hostname);
 			}
 		});	
 
 	    final Handler handler = new Handler();
 	    
 		Runnable runnable = new Runnable() {
 		    String reply;
 		    
 			@Override
 			public void run() {
 				int time = 500;
 				
 				while (running) {
 			    	
 					try {
 						Thread.sleep(time);
 					} catch (InterruptedException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
 
 					if (!paused) {
 						try {
 							//  Log.d("TREAD", "Getting data ...");
 							reply = udp.getUDPMessage(getBaseContext(), getTargetIp(hostname), 44558, "CLOCK:PLAYING");
 						} catch (IOException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 					
 					handler.post(new Runnable() {
 						@Override
 						public void run() {
 							if ((reply != "") && (reply != txtPlaying.getText())) txtPlaying.setText(reply);
 						}
 					});
 				}
 			}
 		};
 		new Thread(runnable).start();
 
 		//  Log.d("Events", "Starting ... Fin");
     }
     
     public void onStart()
     {
     	super.onStart();
     	
 		//  Log.d("Events", "onStart");
 
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		
 		hostname = prefs.getString("clock_address", hostname);
 		
 		//  Log.d("Preferences", ipAddress);
 		
 		paused = false;
 	}
     
     public void onPause()
     {
     	super.onPause();    	
     	paused = true;
     }
     
     public void onReStart()
     {
 	    RadioButton rbtnClock1 = (RadioButton) findViewById(R.id.radioButton1);
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 	    
    	if (rbtnClock1.hasSelection())
     		hostname = prefs.getString("clock1_address", hostname);
     	else
     		hostname = prefs.getString("clock2_address", hostname);
     	
     	super.onRestart();   	
     	paused = false;
     }
     
     //Creates menus
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	super.onCreateOptionsMenu(menu);
     	MenuInflater inflater = getMenuInflater();
     	inflater.inflate(R.menu.menu, menu);
     	return true;
     }
     
     //Handles menu clicks
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	//  Log.d("MENUS", "MENUS");
     	switch(item.getItemId()) {
     	case R.id.mitmWeather:
     		startActivity(new Intent(this, WeatherActivity.class));
     		return true;
     	case R.id.mitmPreferences:
     		startActivity(new Intent(this, Preferences.class));
     		return true;
     	case R.id.mitmQuit:
     		finish();
     		break;
     	}
     	
     	return false;
     }    
   
     @Override
     public void onDestroy(){
     	super.onDestroy();
     	running = false;
     	udp.close();
     }
     
    @SuppressWarnings("deprecation")
 	private String getTargetIp(String host) {
         return dns.getHostAddress(host);
     }    
 }
