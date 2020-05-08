 package com.zapp.example.zmote;
 
 import com.zapp.example.api.RemoteSTB;
 
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.app.Activity;
 import android.content.Context;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.SeekBar;
 
public class Remote extends Activity {
 	RemoteSTB api;  
 	EditText ip;
 	SeekBar vol;
 	Vibrator vibe;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_remote);
                 
         api = new RemoteSTB();
         addListners();
         
         
     }
     
     @Override
     public boolean dispatchKeyEvent(KeyEvent event) {
         int action = event.getAction();
         int keyCode = event.getKeyCode();
             switch (keyCode) {
             case KeyEvent.KEYCODE_VOLUME_UP:
                 if (action == KeyEvent.ACTION_UP) {
                 	api.execute(RemoteSTB.VolUP);
     				vibe.vibrate(75);
                 }
                 return true;
             case KeyEvent.KEYCODE_VOLUME_DOWN:
                 if (action == KeyEvent.ACTION_DOWN) {
                 	api.execute(RemoteSTB.VolDOWN);
     				vibe.vibrate(75);
                 }
                 return true;
             default:
                 return super.dispatchKeyEvent(event);
             }
         }
     
     
     public void addListners(){
     	ip = (EditText)findViewById(R.id.editIP);
     	api.setAddress(ip.getText().toString());
     	
     	vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE) ;
     	
         ip.addTextChangedListener(new TextWatcher() {
 			public void onTextChanged(CharSequence s, int start, int before, int count) {}
 			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 			public void afterTextChanged(Editable s) {
 				api.setAddress(ip.getText().toString());				
 			}
 		});
         
         
         Button menu = (Button) findViewById(R.id.buttonMenu);
         menu.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {				
 				api.execute(RemoteSTB.MENU);
 				vibe.vibrate(75);
 			}
 		});
         
         Button back = (Button) findViewById(R.id.buttonBack);
         back.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {				
 				api.execute(RemoteSTB.BACK);
 				vibe.vibrate(75);
 			}
 		});
         
         Button exit = (Button) findViewById(R.id.buttonExit);
         exit.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {				
 				api.execute(RemoteSTB.EXIT);
 				vibe.vibrate(75);
 			}
 		});
         
         Button up = (Button) findViewById(R.id.buttonUP);
         up.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {				
 				api.execute(RemoteSTB.UP);
 				vibe.vibrate(75);
 			}
 		});
         
         Button down = (Button) findViewById(R.id.buttonDOWN);
         down.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {				
 				api.execute(RemoteSTB.DOWN);
 				vibe.vibrate(75);
 			}
 		});
         
         Button left = (Button) findViewById(R.id.buttonLEFT);
         left.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {				
 				api.execute(RemoteSTB.LEFT);
 				vibe.vibrate(75);
 			}
 		});
         
         Button right = (Button) findViewById(R.id.buttonRIGHT);
         right.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {				
 				api.execute(RemoteSTB.RIGHT);
 				vibe.vibrate(75);
 			}
 		});
         
         Button ok = (Button) findViewById(R.id.buttonOK);
         ok.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {				
 				api.execute(RemoteSTB.OK);
 				vibe.vibrate(75);
 			}
 		});
         
         Button volup = (Button) findViewById(R.id.buttonVolUp);
         volup.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {				
 				api.execute(RemoteSTB.VolUP);
 				vibe.vibrate(75);
 			}
 		});
         
         Button voldown = (Button) findViewById(R.id.buttonVolDown);
         voldown.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {				
 				api.execute(RemoteSTB.VolDOWN);
 				vibe.vibrate(75);
 			}
 		});
         
         Button pageup = (Button) findViewById(R.id.buttonPageUp);
         pageup.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {				
 				api.execute(RemoteSTB.PageUP);
 				vibe.vibrate(75);
 			}
 		});
         
         Button pagedown = (Button) findViewById(R.id.buttonPageDown);
         pagedown.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {				
 				api.execute(RemoteSTB.PageDOWN);
 				vibe.vibrate(75);
 			}
 		});
         
         vol = (SeekBar)findViewById(R.id.volBar);
         vol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 			boolean running;
 			public void onStopTrackingTouch(SeekBar seekBar) {
 				vol.setProgress(50);
 				running = false;
 				vibe.vibrate(75);
 			}
 			
 			public void onStartTrackingTouch(SeekBar seekBar) {
 				running = true;
 				new Thread(new Runnable() {
 					
 					public void run() {
 						while(running){
 							if(vol.getProgress() < 50)
 								api.execute(RemoteSTB.VolDOWN);
 							else if(vol.getProgress() > 50)
 								api.execute(RemoteSTB.VolUP);
 							
 							try {
 								Thread.sleep(500-9*Math.abs(vol.getProgress()-50));
 							} catch (InterruptedException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}
 						}
 					}
 				}).start();
 				
 			}
 			
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				
 				
 			}
 		});
     }
     
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_remote, menu);
         return true;
     }
 }
