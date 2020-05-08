 package com.patchingzone.energyvampire;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	MainApp ma;
 	
 	public Button BT_home;
 	public TextView info;
 	public TextView count;
 	public TextView GPS;
 	public Boolean ifConnected = false;
 	public RelativeLayout body;
 	public Animation fade;
 	
 	public SoundPool sp;
 	public int sound;
 	public int sound2;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         
         ma = (MainApp)getApplication();
         
         
         this.info = (TextView) this.findViewById(R.id.textView1);
 		this.count = (TextView) this.findViewById(R.id.Count);
 		this.GPS = (TextView) this.findViewById(R.id.textView2);
 		this.body = (RelativeLayout) this.findViewById(R.id.body); 
 		this.BT_home = (Button) this.findViewById(R.id.button1);
 		
 		MainApp.app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		
 		fade = AnimationUtils.loadAnimation(this, R.anim.fade);
 		
 		//soundpool
 		sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
 		sound = sp.load(this, R.raw.sound1, 1);
 		sound2 = sp.load(this, R.raw.sound2, 1);
 		
 		this.BT_home.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if(!ifConnected){
 					//connect to server.
 					
 					final EditText input = new EditText(MainActivity.this);
 					input.setHint("Please put name here.");
 					input.setText(ma.app_preferences.getString("ID", "")); 
 					new AlertDialog.Builder(MainActivity.this)
 				    .setTitle("Set Name")
 				    //.setMessage("")
 				    .setView(input)
 				    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 				        public void onClick(DialogInterface dialog, int whichButton) {
 				            String value = input.getText().toString();
 				            Log.d("hans", value);
 				            
 				            SharedPreferences.Editor editor = MainApp.app_preferences.edit();
 				            editor.putString("ID", input.getText().toString().trim());
 				            editor.commit();
 				            
 				            if(ma.startConnection()){
 					            
 								try {
 									ma.ioWebSocket.emit("debug", new JSONArray().put(value).put("Mobile"));
 									//ma.ioWebSocket.emit("clientType", new JSONArray().put("Mobile"));
 									ifConnected = true;
 									info.setText("U're in lobby.");
 									BT_home.setText("I'm Ready");
 									ma.createGPS();
 								} catch (Exception e) {
 									// TODO Auto-generated catch block
 									Toast.makeText(getApplicationContext(), "Can't Connect!", Toast.LENGTH_SHORT).show();
 									e.printStackTrace();
 								}
 				            }
 							else
 							{
 								Toast.makeText(getApplicationContext(), "Can't Connect!", Toast.LENGTH_SHORT).show();
 							}
 				        }
 				    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				        public void onClick(DialogInterface dialog, int whichButton) {
 				            // Do nothing.
 				        	//ma.closeConnection();
 				        }
 				    }).show();
 				}
 				else{
 					//Set rdy	
 					ma.testMsg();
 					Log.d("STATE", "LOBBY");
 				}
 				
 			}
 		});
 				
 	}
     
     @Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	        case R.id.Cred:
 	        	Intent cred =new Intent(MainActivity.this,Credits.class);
                 startActivity(cred);
 	        	return true;
 	        case R.id.Options:
 	        	Intent Pref =new Intent(MainActivity.this,Options.class);
 	        	startActivity(Pref);
 	        	return true;
 	        case R.id.Disconnect:
 	        	ifConnected = ma.closeConnection();
 	        	BT_home.setText("Connect");
 	        	info.setText("Click connect to connect to the server.");
 	        	return true;
 	        case R.id.Exit:
 	        	ma.closeConnection();
 	        	ma.stopGps();
 	        	finish();     	
 	        	return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}
 		
         
     
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     public void startGame()
     {
     	Intent intent = new Intent(this, Game.class );
 		startActivity(intent);
     }
     
     public boolean onPrepareOptionsMenu(Menu menu)
     {
     	
     	MenuItem options = menu.findItem(R.id.Options);
     	MenuItem dc = menu.findItem(R.id.Disconnect);
     	if(!ifConnected){
     		options.setVisible(true);
     		dc.setVisible(false);
     	}
     	else{
     		options.setVisible(false);
     		dc.setVisible(true);
     	}
     	return true;
     }
     
    
     
 
     
 }
