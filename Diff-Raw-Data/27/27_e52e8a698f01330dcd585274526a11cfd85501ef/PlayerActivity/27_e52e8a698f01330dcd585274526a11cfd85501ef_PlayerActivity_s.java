 package com.music.fmv.activities;
 
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.view.View;
 import android.widget.*;
 import com.music.fmv.R;
 import com.music.fmv.core.BaseActivity;
 import com.music.fmv.services.Player;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * User: vitaliylebedinskiy
  * Date: 8/1/13
  * Time: 10:33 AM
  */
 public class PlayerActivity extends BaseActivity {
     private static final SimpleDateFormat TIME_SD = new SimpleDateFormat("mm.ss");
 
     private View prev;
     private View pausePlay;
     private View next;
     private View shuffle;
     private View loop;
 
     private TextView songName;
     private TextView songOwner;
     private TextView curTime;
     private TextView fullTime;
     private SeekBar currProgress;
 
     private ImageView songCover;
     private View imageContainer;
 
     private Player playerManager;
     private boolean trackingInTouchMode;
 
 
     private Drawable pause;
     private Drawable play;
     private Drawable loopNorm;
     private Drawable loopAct;
     private Drawable shuffleNorm;
     private Drawable shuffleAct;
 
     private RefreshTimer refresher;
 
     @Override
     protected void onCreated(Bundle state) {
         setContentView(R.layout.player_activity);
         initViews();
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         playerManager = mCore.getPlayerManager().getPlayer();
         playerManager.setPlayerStatusListener(statusListener);
         refresher = new RefreshTimer(1 * 60 * 1000, 500);
         refresher.start();
         currProgress.setMax(playerManager.getDuration());
         fullTime.setText(TIME_SD.format(new Date(playerManager.getDuration())));
         currProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
             @Override
             public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                 if (fromUser) {
                     playerManager.seek(progress);
                 }
             }
 
             @Override
             public void onStartTrackingTouch(SeekBar seekBar) {
                 trackingInTouchMode = true;
             }
 
             @Override
             public void onStopTrackingTouch(SeekBar seekBar) {
                 trackingInTouchMode = false;
             }
         });
     }
 
     public void initViews() {
         shuffle = findViewById(R.id.shuffle);
         prev = findViewById(R.id.prev);
         pausePlay = findViewById(R.id.play_pause);
         next = findViewById(R.id.next);
         loop = findViewById(R.id.loop);
 
         shuffle.setOnClickListener(controlsListener);
         prev.setOnClickListener(controlsListener);
         pausePlay.setOnClickListener(controlsListener);
         next.setOnClickListener(controlsListener);
         loop.setOnClickListener(controlsListener);
 
         songCover = (ImageView) findViewById(R.id.song_image);
         imageContainer = findViewById(R.id.image_container);
 
         songName = (TextView) findViewById(R.id.song_name);
         songOwner = (TextView) findViewById(R.id.song_owner);
         fullTime = (TextView) findViewById(R.id.full_time);
         curTime = (TextView) findViewById(R.id.current_time);
         currProgress = (SeekBar) findViewById(R.id.curr_progress);
 
 
         pause = getResources().getDrawable(R.drawable.player_pause_selector);
         play = getResources().getDrawable(R.drawable.player_play_selector);
         loopNorm = getResources().getDrawable(R.drawable.player_loop_selector);
         loopAct = getResources().getDrawable(R.drawable.ic_audio_repeat_down);
         shuffleNorm = getResources().getDrawable(R.drawable.player_shuffle_selector);
         shuffleAct = getResources().getDrawable(R.drawable.ic_audio_shuffle_down);
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         playerManager.setPlayerStatusListener(null);
         if (refresher != null) {
             refresher.cancel();
             refresher = null;
         }
     }
 
     @Override
     public void onWindowFocusChanged(boolean hasFocus) {
         super.onWindowFocusChanged(hasFocus);
         int width = imageContainer.getWidth();
         int height = imageContainer.getHeight();
         int minimum = width < height ? width : height;
         LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(minimum, minimum);
         songCover.setLayoutParams(params);
     }
 
     private View.OnClickListener controlsListener = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             switch (v.getId()) {
                 case R.id.next:
                     playerManager.next();
                     break;
                 case R.id.shuffle:
                     playerManager.shuffle();
                     break;
                 case R.id.play_pause:
                     playerManager.pause();
                     break;
                 case R.id.prev:
                     playerManager.previous();
                     break;
                 case R.id.loop:
                     playerManager.loop();
             }
         }
     };
 
     private void checkStatus() {
         if (playerManager.isPlaying()) {
             pausePlay.setBackground(pause);
         } else pausePlay.setBackground(play);
         curTime.setText(TIME_SD.format(new Date(playerManager.getProgress())));
         if (!trackingInTouchMode) {
             currProgress.setProgress(playerManager.getProgress());
         }
     }
 
     private Player.PlayerStatusListener statusListener = new Player.PlayerStatusListener() {
         @Override
         public void onControllCallBack() {
             if (playerManager.isPlaying()) {
                 pausePlay.setBackground(pause);
             } else pausePlay.setBackground(play);
 
             if (playerManager.isLooping()) {
                 loop.setBackground(loopAct);
             } else loop.setBackground(loopNorm);
 
             if (playerManager.isShuffle()) {
                 shuffle.setBackground(shuffleAct);
             } else shuffle.setBackground(shuffleNorm);
         }
 
         @Override
         public void onNewSong() {
             int duration = playerManager.getDuration();
             currProgress.setMax(duration);
             fullTime.setText(TIME_SD.format(new Date(duration)));
         }
 
         @Override
         public void onBuffering(int progress) {
             currProgress.setSecondaryProgress(progress);
         }
     };
 
     private class RefreshTimer extends CountDownTimer {
         public RefreshTimer(long millisInFuture, long countDownInterval) {
             super(millisInFuture, countDownInterval);
         }
 
         @Override
         public void onTick(long l) {
             checkStatus();
         }
 
         @Override
         public void onFinish() {
             refresher = null;
             refresher = new RefreshTimer(1 * 60 * 1000, 500);
             refresher.start();
         }
     }
 }
