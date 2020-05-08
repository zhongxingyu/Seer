 /*
  * Copyright (C) 2007 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package net.filiph.mothership;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.IntentFilter;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnErrorListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.media.MediaPlayer.OnSeekCompleteListener;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.text.Html;
 import android.text.method.LinkMovementMethod;
 import android.util.Log;
 import android.view.Display;
 import android.view.Menu;
 import android.view.View.MeasureSpec;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.VideoView;
 
 /**
  * This is the main activity, meaning the UI screen.
  */
 @TargetApi(8)
 public class MainActivity extends Activity {
     
 	@SuppressWarnings("unused")
 	private static final String TAG = "motherShip";
 	
 	private UpdateReceiver updateReceiver;
 	final private Handler typeHandler = new Handler(); 
 	
     public MainActivity() {
     }
 
     /** Called with the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // Inflate our UI from its XML layout description.
         setContentView(R.layout.skeleton_activity);
 
         // TODO: only do if there is no alarm set
         AlarmReceiver.setAlarmForNextMessage(getBaseContext());
     }
     
     VideoView vv = null;
 
     /**
      * Called when the activity is about to start interacting with the user.
      */
     @Override
     protected void onResume() {
         super.onResume();
         
         showCurrentMessage(TYPING_DEFAULT);
         
         updateReceiver = new UpdateReceiver(this);
         registerReceiver(updateReceiver, new IntentFilter("NewMothershipMessage"));
         
         if (vv == null) {
 	        //getWindow().setFormat(PixelFormat.TRANSLUCENT);
 	        vv = (VideoView) findViewById(R.id.videoView1);
 	        // video from http://www.istockphoto.com/stock-video-17986614-data-servers-loopable-animation.php
 	        Uri video = Uri.parse("android.resource://" + getPackageName() + "/" 
 	        		+ R.raw.servers);
 	        
 	        // this horrible spaghetti is responsible for showing the background video
 	        // with as few flicker as possible
 	        try {
 				vv.setVideoURI(video);
 				vv.setKeepScreenOn(false);
 				
 				// we can only access the MediaPlaye instance after video is Prepared
 				vv.setOnPreparedListener(new OnPreparedListener() {
 				    @Override
 				    public void onPrepared(MediaPlayer mp) {
 				    	mp.setOnSeekCompleteListener(new OnSeekCompleteListener() {
 							@Override
 							public void onSeekComplete(MediaPlayer mp) {
 								mp.start();
 								
 								// give the player 100 milliseconds to start playing
 								// then hide the placeholder static image
 								new Handler().postDelayed(new Runnable() {
 									@Override
 									public void run() {
 										ScrollView sv = (ScrollView) findViewById(R.id.scrollView);
 						            	sv.setBackgroundResource(0);
 									}
 								}, 100);
 							}
 				    	});
 				    	mp.setLooping(true);
 				    	mp.seekTo(0);
 				    }
 				});
 				
 				vv.setOnErrorListener(new OnErrorListener() {
 					@Override
 					public boolean onError(MediaPlayer mp, int what, int extra) {
						Log.e(TAG, "Error with video.");
						return false;
 					}
 				});
 				
 				// stretch the video full view
 				Display display = getWindowManager().getDefaultDisplay(); 
 				int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(display.getWidth(),
 				        MeasureSpec.UNSPECIFIED);
 				int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(display.getHeight(),
 				        MeasureSpec.UNSPECIFIED);
 				vv.measure(childWidthMeasureSpec, childHeightMeasureSpec);
 			} catch (Exception e) {
 				Log.e(TAG, "Error with background video, falling back to static background.");
 				e.printStackTrace();
 				
 				// something went wrong, fall back to static image
 				vv = null;
 				ScrollView sv = (ScrollView) findViewById(R.id.scrollView);
             	sv.setBackgroundResource(R.drawable.servers);
 			}
         }
        
         if (vv != null && vv.canSeekBackward() && vv.canSeekForward()) {
         	vv.seekTo(0);
         }
     }
     
     @Override
     protected void onPause() {
     	super.onPause();
     	
     	if (vv != null && vv.canPause()) {
     		vv.pause();
     		//vv.suspend();
     	}
     	
     	unregisterReceiver(updateReceiver); // don't update the activity when paused
     }
     
     private int funnyRemarkIndex = 0;
     
     /**
      * Clicking the menu button prints funny messages.
      */
     public boolean onPrepareOptionsMenu(Menu menu) {
     	final MainActivity mainActivity = this;
     	final TextView t = (TextView) findViewById(R.id.textView);
 		final TextView signature = (TextView) findViewById(R.id.signature);
 		
     	t.setText("");
 		signature.setText("");
 		
 		String[] funnyRemarks = getResources().getStringArray(R.array.menu_mothership_funny_remarks);
 		final String str = funnyRemarks[funnyRemarkIndex];
 		if (funnyRemarkIndex < funnyRemarks.length - 1) {
 			funnyRemarkIndex++;
 		}
 		
 		typeHandler.removeCallbacksAndMessages(null);
 		
 		typeHandler.postDelayed(new Runnable() {
 			int index = 0;
 			
 			@Override
 			public void run() {
 				// skip insides of HTML tags
 				if (index < str.length() && str.charAt(index) == '<') {
 					int closingIndex = str.indexOf('>', index);
 					if (closingIndex > index)
 						index = closingIndex;
 				}
 				t.setText(Html.fromHtml((String) str.subSequence(0, index++)));
 				if (index <= str.length()) {
 					typeHandler.postDelayed(this, 10);
 				} else {
 					typeHandler.postDelayed(new Runnable() {
 						@Override
 						public void run() {
 							mainActivity.showCurrentMessage(TYPING_FORCE_SHOW);
 						}
 					}, 5000);
 				}
 			}
 		}, 10);
     	return false;
     }
     
     public static final String PREFS_NAME = "MothershipPrefs";
     
     public static final int TYPING_DEFAULT = 0;
     public static final int TYPING_FORCE_SHOW = 1;
     
     /**
      * Checks which message should be shown. If that particular message has been
      * already shown, just make sure it's showing again (with setText).
      * If it's a new message, type it out ("typewriter effect").
      * 
      * @param typingOption	When set to TYPING_FORCE_SHOW, the message will be typed out
      * 						no matter if it's been already shown or not. Default is 
      * 						TYPING_DEFAULT.
      */
     public void showCurrentMessage(int typingOption) {
     	int currentUid = getSharedPreferences(PREFS_NAME, 0).getInt("currentUid", 0);
     	final Message currentMessage = Schedule.getCurrentMessage(currentUid);
     	
     	if (currentMessage != null) {
     		boolean isNewMessage = currentMessage.uid != currentUid;
     		Log.v(TAG, "Saving new currentUid = " + currentMessage.uid);
     		getSharedPreferences(PREFS_NAME, 0).edit().putInt("currentUid", currentMessage.uid).commit();
     		
     		final TextView t = (TextView) findViewById(R.id.textView);
     		final TextView signature = (TextView) findViewById(R.id.signature);
     		
     		// make sure system takes care of links in messages
     		t.setMovementMethod(LinkMovementMethod.getInstance());
     		
     		if (isNewMessage || typingOption == TYPING_FORCE_SHOW) {
     			t.setText("");
     			signature.setText("");
     			final Animation fadeinAniDelayed = AnimationUtils.loadAnimation(this, R.anim.fade_in_delayed);
     			
     			// typewriter effect
     			typeHandler.removeCallbacksAndMessages(null);
     			typeHandler.postDelayed(new Runnable() {
         			int index = 0;
         			String str = currentMessage.text;
     				@Override
     				public void run() {
     					// skip insides of HTML tags
     					if (index < str.length() && str.charAt(index) == '<') {
     						int closingIndex = str.indexOf('>', index);
     						if (closingIndex > index)
     							index = closingIndex;
     					}
     					t.setText(Html.fromHtml((String) str.subSequence(0, index++)));
     					if (index <= str.length()) {
     						typeHandler.postDelayed(this, 10);
     					} else {
     						// the small print should just fade in
     						signature.setText(currentMessage.getTimeString());
     			    		signature.startAnimation(fadeinAniDelayed);
     					}
     				}
         		}, 10);
     		} else {
     			// no typewriter effect
     			t.setText(Html.fromHtml(currentMessage.text));
     			signature.setText(currentMessage.getTimeString());
     		}
     	}
     }
 }
