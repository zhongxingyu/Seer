 package org.transittales;
 
 import java.io.IOException;
 import java.util.Properties;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.AssetFileDescriptor;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class PlayerActivity extends Activity implements OnCompletionListener, Runnable {
 	final Context cont = this;
 	// Media Player
 	private MediaPlayer m;
 	private ProgressBar pb;
 	private boolean pingFinished = false;
 	private boolean rideFinished = false;
 	private String state;
 	private String nextState;
 	@SuppressWarnings("rawtypes")
 	private Class nextIntent;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		ImageButton rewind;
 		ImageButton forward;
 		final ImageButton play;
 		ImageButton home;
 		ImageView imageViewCharachter;
 		ImageView imageButtonRideFinished;
 		TextView textViewLabel;
 
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_player);
 
 		home = (ImageButton) findViewById(R.id.imageButtonHome);
 		imageButtonRideFinished = (ImageButton) findViewById(R.id.imageButtonRideFinished);
 		rewind = (ImageButton) findViewById(R.id.imageButtonRew);
 		forward = (ImageButton) findViewById(R.id.ImageButtonFwd);
 		play = (ImageButton) findViewById(R.id.ImageButtonPlayPause);
 		pb = (ProgressBar) findViewById(R.id.progressBarPlayer);
 		imageViewCharachter = (ImageView) findViewById(R.id.imageViewCharacter);
 		textViewLabel = (TextView) findViewById(R.id.textViewLabel);
 
 		home.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				if (null != m && m.isPlaying()) {
 					m.stop();
 				}
 				Intent i = new Intent(cont, MainActivity.class);
 				startActivity(i);
 			}
 		});
 		play.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				try {
 					if (null != m && m.isPlaying()) {
 						// pause
 						play.setBackgroundResource(R.drawable.btn_play);
 						m.pause();
 					} else if (null != m && !m.isPlaying()) {
 						// resume
 						play.setBackgroundResource(R.drawable.btn_pause);
 						m.start();
 					}
 				} catch (Exception e) {
 					Log.e("PlayerActivity", "error in onClick");
 				}
 			}
 		});
 		forward.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				if (null != m && m.isPlaying()) {
 					m.seekTo(m.getCurrentPosition() + 10000);
 				}
 			}
 		});
 		rewind.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				if (null != m && m.isPlaying()) {
 					int newPos = m.getCurrentPosition() - 10000;
 					m.seekTo(newPos > 0 ? newPos : 0);
 				}
 			}
 		});
 		Bundle bin = getIntent().getExtras();
 		state = bin.getString("state");
 		State.seenState(state);
 		String file = null;
 		int resID = -1;
 		Properties stateProp = State.load(this, state);
 		if (null == stateProp) {
 			Log.e("PlayerActivity", "state not found: " + state);
 			return;
 		} else {
 			textViewLabel.setText(stateProp.getProperty("text"));
 			resID = getResources().getIdentifier(stateProp.getProperty("img"), "drawable", getPackageName());
 			nextState = stateProp.getProperty("next_state");
 			try {
 				nextIntent = Class.forName(stateProp.getProperty("next_intent_class"));
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 				Log.e("PlayerActivity", "class not found: " + stateProp.getProperty("next_intent_class"));
 				return;
 			}
 			file = stateProp.getProperty("file");
 		}
 		imageViewCharachter.setImageResource(resID);
 		try {
 			AssetFileDescriptor descriptor;
 			descriptor = getAssets().openFd(file);
 			if (null != m && m.isPlaying()) {
 				m.stop();
 				m.release();
 				m = new MediaPlayer();
 			} else if (null == m) {
 				m = new MediaPlayer();
 			}
 			m.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
 			descriptor.close();
 			m.prepare();
 			pb.setMax(m.getDuration());
 			m.setOnCompletionListener(this);
 			m.setVolume(1f, 1f);
 			m.start();
 			play.setBackgroundResource(R.drawable.btn_pause);
 			new Thread(this).start();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		// configure 'ride is finished' button
 		final String character = stateProp.getProperty("img");
 		final PlayerActivity _this = this;
 		// TODO hardcoded id; fix in final version
 		if (character.equals("audio_abraham")) {
 			imageButtonRideFinished.setVisibility(View.GONE);
 		}
 		imageButtonRideFinished.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				rideFinished = true;
 				try {
 					if (null != m && m.isPlaying()) {
 						m.stop();
 						m.release();
 					}
 					m = new MediaPlayer();
 					AssetFileDescriptor descriptor = null;
 					// TODO hardcoded ids; fix in final version
 					if (character.equals("audio_bill")) {
 						descriptor = getAssets().openFd("MP3/Bill/B06.mp3");
 					} else if (character.equals("audio_tina_trisha")) {
 						descriptor = getAssets().openFd("MP3/TinaTrisha/TT06.mp3");
 					} else {
 						return;
 					}
 					m.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
 					descriptor.close();
 					m.setVolume(1f, 1f);
 					m.prepare();
 					m.setOnCompletionListener(_this);
 					m.start();
 					ImageView imageButtonRideFinished = (ImageButton) findViewById(R.id.imageButtonRideFinished);
 					imageButtonRideFinished.setVisibility(View.GONE);
 				} catch (Exception e) {
 					Log.e("PlayerActivity", "error in fide finished click listener");
 				}
 			}
 		});
 	}
 
 	public void onCompletion(MediaPlayer arg0) {
 		if (rideFinished) {
 			if (null != m) {
 				if (m.isPlaying()) {
 					m.stop();
 				}
 				try {
 					m.release();
 				} catch (Exception e) {
 					// ignore
 				}
 			}
 			finish();
 			moveTaskToBack(true);
 			return;
 		}
 		if (!pingFinished) {
 			// play glass ping
 			try {
 				m.release();
 				m = new MediaPlayer();
 				AssetFileDescriptor descriptor;
 				descriptor = getAssets().openFd("MP3/glass_ping.mp3");
 				m.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
 				descriptor.close();
 				m.setVolume(.5f, .5f);
 				m.prepare();
 				m.setOnCompletionListener(this);
 				m.start();
 				pingFinished = true;
 				Intent i = new Intent(cont, nextIntent);
 				Bundle b = new Bundle();
 				b.putString("state", nextState);
 				i.putExtras(b);
 				startActivity(i);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.player, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			if (null != m && m.isPlaying()) {
 				m.stop();
 				m.release();
 				m = null;
 			}
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public void run() {
 		while (null != m && !pingFinished && !rideFinished) {
 			try {
 				Thread.sleep(500);
				try {
					if (null != m && m.isPlaying()) {
						pb.setProgress(m.getCurrentPosition());
					}
				} catch (Exception e) {
					// ignore
 				}
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		AppUtils.getInstance().resume();
 	}
 
 	@Override
 	protected void onPause() {
		AppUtils.getInstance().pause();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
 		try {
 			if (null != m) {
 				if (m.isPlaying()) {
 					m.stop();
 				}
 				try {
 					m.release();
 				} catch (Exception e) {
 					// ignore
 				}
 			}
 		} catch (Exception e) {
 			// ignore
 		} finally {
 			m = null;
 		}
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 	}
 }
