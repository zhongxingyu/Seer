 package es.jmfrancofraiz.sleepplayer;
 
 import kankan.wheel.widget.WheelView;
 import kankan.wheel.widget.adapters.NumericWheelAdapter;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnBufferingUpdateListener;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import es.jmfrancofraiz.sleepplayer.SleepPlayer.State;
 
 public class SleepPlayerActivity extends Activity {
 	
 	private Button buttonPlayPause;
 	private SeekBar seekBar;
 	private TextView pendiente;
 	private TextView duracion;
 	private TextView posicion;
 	private View layoutWheels;
 	private View layoutLabelWheels;
 	private View layoutDuracion;
 
 	private Handler handlerSegundero = new Handler();
 	private Runnable runnableSegundero = null;
 	
     private WheelView hours = null;
     private WheelView mins = null;
     private WheelView secs = null;
     
     private String TAG = "SleepPlayerActivity";
     
     private boolean completedOnce;
 
 	private ProgressDialog pbarDialog;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	Log.i(TAG, "Creando...");
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);   
         
 		//rueda de horas
     	hours = (WheelView) findViewById(R.id.hours);
         NumericWheelAdapter nwah = new NumericWheelAdapter(this, 0, 99, "%02d");
         hours.setViewAdapter(nwah);
         
         //rueda de minutos
         mins = (WheelView) findViewById(R.id.mins);
         NumericWheelAdapter nwam = new NumericWheelAdapter(this, 0, 59, "%02d");
         mins.setViewAdapter(nwam);
 
         //rueda de segundos
         secs = (WheelView) findViewById(R.id.secs);
         NumericWheelAdapter nwas = new NumericWheelAdapter(this, 0, 59, "%02d");
         secs.setViewAdapter(nwas);
         
         //Play Pause
 		buttonPlayPause = (Button)findViewById(R.id.ButtonTestPlayPause);
 		buttonPlayPause.setOnClickListener(new OnClickListener() {public void onClick(View v) {
 			buttonPlayPauseClick();}
 		});
 		
 		seekBar = (SeekBar)findViewById(R.id.SeekBarTestPlay);	
 		seekBar.setMax(99); // It means 100% .0-99
 		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 			public void onStopTrackingTouch(SeekBar seekBar) {}
 			public void onStartTrackingTouch(SeekBar seekBar) {}
 			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 				if (fromUser) {
 					if (SleepPlayer.getInstance().getState() == State.PLAYING || SleepPlayer.getInstance().getState() == State.PAUSED) {
 						int playPositionInMillisecconds = (SleepPlayer.getInstance().getMediaFileLengthInMilliseconds() / 100) * seekBar.getProgress();
 						SleepPlayer.getInstance().seekTo(playPositionInMillisecconds);
 						posicion.setText(String.format("%02d:%02d:%02d", SleepPlayer.getInstance().getCurrentHoras(), SleepPlayer.getInstance().getCurrentMinutos(),SleepPlayer.getInstance().getCurrentSegundos()));
 					} else if (SleepPlayer.getInstance().getState() == State.READY) {
 						seekBar.setProgress(0);
 					}
 				}
 			}
 		});
 		
 		duracion = (TextView)findViewById(R.id.duracion);
 		pendiente = (TextView)findViewById(R.id.pendiente);
 		posicion = (TextView)findViewById(R.id.posicion);
 
 		layoutWheels = (View)findViewById(R.id.layoutWheels);
 		layoutLabelWheels = (View)findViewById(R.id.layoutLabelWheels);
 		layoutDuracion = (View)findViewById(R.id.layoutDuracion);
 		
         pbarDialog = new ProgressDialog(this);
         pbarDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
         pbarDialog.setMessage(getString(R.string.cargando));
         
     	runnableSegundero = new Runnable() {
 	        public void run() {
 	        	Log.d(TAG,"running segundero");
 	    		if (SleepPlayer.getInstance().getState() == State.PLAYING) {
 	    			seekBar.setProgress(SleepPlayer.getInstance().getProgress());
 	    			posicion.setText(String.format("%02d:%02d:%02d", SleepPlayer.getInstance().getCurrentHoras(), SleepPlayer.getInstance().getCurrentMinutos(), SleepPlayer.getInstance().getCurrentSegundos()));
 	    			pendiente.setText(String.format("%02d:%02d:%02d", SleepPlayer.getInstance().getPendingHoras(), SleepPlayer.getInstance().getPendingMinutos(), SleepPlayer.getInstance().getPendingSegundos()));
 	    			//hours.setCurrentItem(SleepPlayer.getInstance().getPendingHoras(), true);
 	    			//mins.setCurrentItem(SleepPlayer.getInstance().getPendingMinutos(), true);
 	    			//secs.setCurrentItem(SleepPlayer.getInstance().getPendingSegundos(), true);
 	    		    handlerSegundero.postDelayed(runnableSegundero,1000);
 	        	}
 			}
 	    };
 
 		SleepPlayer.getInstance().setOnPlayerReadyListener(new OnPlayerReadyListener() {
 			public void onPlayerReady() {
 				Log.d(TAG,"El reproductor esta listo");
 				if (!completedOnce) {
 					pausedScreen(true);
 				} else { 
 		        	SleepPlayer.getInstance().play(hours.getCurrentItem(), mins.getCurrentItem(), secs.getCurrentItem());
 		    		playingScreen();
 				}
 				pbarDialog.dismiss();
 				seekBar.setEnabled(false);
 			}
 		});
 		
 		SleepPlayer.getInstance().setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
 			public void onBufferingUpdate(MediaPlayer mp, int percent) {
 				seekBar.setSecondaryProgress(percent);
 			}
 		});
 		
 		SleepPlayer.getInstance().setOnCompleteListener(new OnCompleteListener() {
 			public void onComplete(int pos,int dur) {
 				Log.d(TAG, "Play completed");
 				seekBar.setProgress(0);
 				seekBar.setSecondaryProgress(0);
 				posicion.setText("00:00:00");
 				pausedScreen(true);
 				seekBar.setEnabled(false);
 				completedOnce = true;
 			}
 		});
 
     }
        
     @Override
     protected void onDestroy() {
     	Log.i(TAG, "Destruyendo...");
     	super.onDestroy();
     }
     
     @Override
     protected void onPause() {
     	Log.i(TAG, "Pausing...");
     	//if (SleepPlayer.getInstance().getState() != State.IDLE) SleepPlayer.getInstance().showNotification();
     	handlerSegundero.removeCallbacks(runnableSegundero);
     	super.onPause();
     }
     
     @Override
     protected void onRestart() {
     	Log.i(TAG, "Restarting...");
     	super.onRestart();
     }
     
     @Override
     protected void onResume() {
     	Log.i(TAG, "Resuming...");
     	//if (SleepPlayer.getInstance() != null) SleepPlayer.getInstance().cancelNotification();
     	super.onResume();
     }
 
         
     @Override
     protected void onStart() {
     	
     	Log.i(TAG, "onStart...");
         Log.d(TAG, "SleePlayer State = "+SleepPlayer.getInstance().getState());
     	super.onStart();
 		
 		if (getIntent() != null && getIntent().getData() != null && getIntent().getData().getScheme() != null
 				&& getIntent().getData().getScheme().equalsIgnoreCase("http")) {
 			
 			Log.i(TAG, "Nuevo intent recibido: "+getIntent().getDataString());
 	        pbarDialog.show();
 	        completedOnce = false;
 	        SleepPlayer.getInstance().setAudioStream(getIntent().getDataString());
 	        SleepPlayer.getInstance().prepare();
 	        
 		} else {
 			
 			if (SleepPlayer.getInstance().getState() == State.PLAYING) {
 				playingScreen();
 			} else if (SleepPlayer.getInstance().getState() == State.PAUSED || SleepPlayer.getInstance().getState() == State.READY) {
 				pausedScreen(false);
 			} else if ((SleepPlayer.getInstance().getState() == State.IDLE  && SleepPlayer.getInstance().getAudioStream() == null) || SleepPlayer.getInstance().getState() == State.ERROR) {
 				Log.d(TAG,"Mostramos splash");
 				Intent intent = new Intent(SleepPlayerActivity.this, SleepPlayerSplashActivity.class);
 				intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
 				startActivity(intent);
 			}
 			
 		}
 				
     	//SleepPlayer.getInstance().cancelNotification();
 	                
     }
     
     @Override
     protected void onStop() {
     	Log.i(TAG, "Stopping...");
     	handlerSegundero.removeCallbacks(runnableSegundero);
     	super.onStop();
     }
     
     @Override
     protected void onNewIntent(Intent i) {
     	Log.i(TAG, "New intent...");
     	Log.i(TAG, "Intent = "+(i.getData()!=null?i.getDataString():"no data"));
     	super.onNewIntent(i);
     }
     
     @Override
     protected void onUserLeaveHint() {
     	Log.i(TAG, "UserLeaveHinting...");
     	super.onUserLeaveHint();
     }
         
     private void playingScreen() {
     	Log.i(TAG, "Entering playing screen");
     	duracion.setText(String.format("%02d:%02d:%02d", SleepPlayer.getInstance().getTotalHoras(), SleepPlayer.getInstance().getTotalMinutos(), SleepPlayer.getInstance().getTotalSegundos()));
 		buttonPlayPause.setBackgroundDrawable(getApplicationContext().getResources().getDrawable(R.drawable.red_button));
 		buttonPlayPause.setText(getString(R.string.pausa));
     	layoutWheels.setVisibility(View.GONE);
     	layoutLabelWheels.setVisibility(View.GONE);
     	layoutDuracion.setVisibility(View.VISIBLE);
 	    handlerSegundero.post(runnableSegundero);
 		seekBar.setEnabled(true);
     }
     
     private void pausedScreen(boolean anim) {
     	Log.i(TAG, "Entering paused screen");
     	handlerSegundero.removeCallbacks(runnableSegundero);
     	duracion.setText(String.format("%02d:%02d:%02d", SleepPlayer.getInstance().getTotalHoras(), SleepPlayer.getInstance().getTotalMinutos(), SleepPlayer.getInstance().getTotalSegundos()));
 		buttonPlayPause.setBackgroundDrawable(getApplicationContext().getResources().getDrawable(R.drawable.green_button));
 		buttonPlayPause.setText(getString(R.string.reproducir));
 		layoutWheels.setVisibility(View.VISIBLE);
 		layoutLabelWheels.setVisibility(View.VISIBLE);
 		layoutDuracion.setVisibility(View.GONE);
 		hours.setCurrentItem(SleepPlayer.getInstance().getPendingHoras(), anim);
 		mins.setCurrentItem(SleepPlayer.getInstance().getPendingMinutos(), anim);
 		secs.setCurrentItem(SleepPlayer.getInstance().getPendingSegundos(), anim);
     }
     
     private void buttonPlayPauseClick() {
     	if (SleepPlayer.getInstance().getState() == State.PLAYING) {
     		SleepPlayer.getInstance().pause();
     		pausedScreen(true);
     	} else {
         	if (SleepPlayer.getInstance().getState() == State.IDLE) {
         		pbarDialog.show();
         		SleepPlayer.getInstance().prepare();
         	} else {
 	        	SleepPlayer.getInstance().play(hours.getCurrentItem(), mins.getCurrentItem(), secs.getCurrentItem());
 	    		playingScreen();
         	}
     	}	
 	}
 
 }
