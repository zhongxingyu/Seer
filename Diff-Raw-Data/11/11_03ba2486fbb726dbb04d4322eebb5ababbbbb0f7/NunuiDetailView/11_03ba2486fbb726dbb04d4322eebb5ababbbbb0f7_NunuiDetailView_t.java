 package com.xsace.nunui;
 
 import android.app.Activity;
 import android.content.Context;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 import com.markupartist.android.widget.ActionBar;
 import com.markupartist.android.widget.ActionBar.Action;
 
 public class NunuiDetailView extends Activity implements Runnable,
 		OnSeekBarChangeListener {
 
 	AudioManager _am;  
 	MediaPlayer _mp;
 	SeekBar _seekbar;
 	ImageButton _button;
 	int _position;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.detail);
 
 		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
 		actionBar.setTitle(R.string.app_name);
 		
		_am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

 		actionBar.addAction(new Action() {
 			
 			public void performAction(View view) {
 				
 				if (_am.isSpeakerphoneOn()) {
 					_am.setMode(AudioManager.MODE_IN_CALL); 
 					_am.setSpeakerphoneOn(false); 
 				}
 				else {
 					_am.setMode(AudioManager.MODE_NORMAL); 
 					_am.setSpeakerphoneOn(true); 
 				}
 			}
 			
 			public int getDrawable() {
 				return R.drawable.ic_lock_silent_mode_off;
 			}
 		});
 		
 		_button = (ImageButton) findViewById(R.id.button);
 		_button.setImageResource(R.drawable.ic_media_pause);
 		
 		_button.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				if (_mp.isPlaying()) {
 					_mp.pause();
 					_button.setImageResource(R.drawable.ic_media_play);
 				} else {
 					if (_seekbar.getProgress() == _mp.getDuration()) {
 						_seekbar.setProgress(0);
 					}
 					_mp.start();
 					_button.setImageResource(R.drawable.ic_media_pause);
 				}
 			}
 		});
 		
 		_seekbar = (SeekBar) findViewById(R.id.progress);
 		_seekbar.setVisibility(View.VISIBLE);
 		_seekbar.setOnSeekBarChangeListener(this);
 		_position = (Integer) getIntent().getExtras().get("position");
 		
 		TextView title = (TextView) findViewById(R.id.item_title);
 		title.setText(Init.TITLES[_position]);
 		
 		runMedia();
 	}
 
 
 	private void runMedia() {
 		_mp = MediaPlayer.create(getApplicationContext(), Init.SOUNDS[_position]);
 		_mp.start();
 
 		_mp.setOnCompletionListener(new OnCompletionListener() {
 			public void onCompletion(MediaPlayer mp) {
 				_button.setImageResource(R.drawable.ic_media_play);
 			}
 		});
 
 		_seekbar.setProgress(0);
 		_seekbar.setMax(_mp.getDuration());
 
 		new Thread(NunuiDetailView.this).start();
 	}
 
 	public void run() {
 		int currentPosition = 0;
 		int total = _mp.getDuration();
 		while (_mp != null && currentPosition < total) {
 			try {
 				Thread.sleep(50);
 				currentPosition = _mp.getCurrentPosition();
 			} catch (InterruptedException e) {
 				return;
 			} catch (Exception e) {
 				return;
 			}
 			_seekbar.setProgress(currentPosition);
 		}
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 
 		if (_mp != null) {
 			_mp.release();
 			_mp = null;
 		}

		// Reset the audio settings for future phone calls.
		if (_am != null) {
			_am.setMode(AudioManager.MODE_NORMAL);
			_am.setSpeakerphoneOn(false);
		}
 	}
 
 	public void onProgressChanged(SeekBar seekBar, int progress,
 			boolean fromUser) {
 		if (fromUser)
 			_mp.seekTo(progress);
 	}
 
 	public void onStartTrackingTouch(SeekBar arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void onStopTrackingTouch(SeekBar arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
