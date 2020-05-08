 package com.backflip270bb.android.premacyremocon;
 
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 
 public class MainActivity extends Activity {
 	private SoundPool mSoundPool;
	private int mSoundId;
	private int mSoundId_0_1;
 	private ImageView mImageOn;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		mImageOn = (ImageView)findViewById(R.id.imageView2);
 		Button mButtonLock = (Button)findViewById(R.id.button_lock);
 		Button mButtonUnlock = (Button)findViewById(R.id.button_unlock);
 		Button mButtonRightDoor = (Button)findViewById(R.id.button_rightdoor);
 		Button mButtonLeftDoor = (Button)findViewById(R.id.button_leftdoor);
 		
 		
 		mButtonLock.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
				mSoundPool.play(mSoundId_0_1, 1.0f, 1.0f, 0, 0, 1.0f);
 			}
 		});
 		mButtonUnlock.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
				mSoundPool.play(mSoundId_0_1, 1.0f, 1.0f, 0, 1, 1.0f);
 			}
 		});
 		View.OnLongClickListener doorButtonListener = new View.OnLongClickListener() {
 			@Override
 			public boolean onLongClick(View arg0) {
				mSoundPool.play(mSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
 				return false;
 			}
 		};
 		mButtonRightDoor.setOnLongClickListener(doorButtonListener);
 		mButtonLeftDoor.setOnLongClickListener(doorButtonListener);
 		
 		mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		mSoundId_0_1 = mSoundPool.load(this, R.raw.sound_pi_0_1, 1);
		mSoundId = mSoundPool.load(this, R.raw.sound_pi, 1);
 		
 		View.OnTouchListener touchListener = new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				Log.d("MainActivity", event.toString());
 				switch(event.getAction()) {
 				case MotionEvent.ACTION_DOWN:
 					mImageOn.setVisibility(View.VISIBLE);
 					break;
 				case MotionEvent.ACTION_UP:
 					mImageOn.setVisibility(View.INVISIBLE);
 					break;
 				}
 				return false;
 			}
 		};
 		mButtonLock.setOnTouchListener(touchListener);
 		mButtonUnlock.setOnTouchListener(touchListener);
 		mButtonRightDoor.setOnTouchListener(touchListener);
 		mButtonLeftDoor.setOnTouchListener(touchListener);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 }
