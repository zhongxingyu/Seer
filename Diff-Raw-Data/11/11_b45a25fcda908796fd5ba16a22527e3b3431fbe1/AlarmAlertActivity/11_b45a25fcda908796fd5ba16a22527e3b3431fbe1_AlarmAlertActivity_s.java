 package com.tools.tvguide.activities;
 
 import java.util.HashMap;
 
 import com.tools.tvguide.R;
 import com.tools.tvguide.managers.AppEngine;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Service;
 import android.content.DialogInterface;
 import android.media.MediaPlayer;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.view.KeyEvent;
 
 public class AlarmAlertActivity extends Activity 
 {
 	private MediaPlayer mMediaPlayer;
 	private Vibrator	mVibrator;
 	
     public void onCreate(Bundle SavedInstanceState) 
     {
         super.onCreate(SavedInstanceState);
        mMediaPlayer = MediaPlayer.create(this, getDefaultRingtoneUri(RingtoneManager.TYPE_ALARM));
         mVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
         
         startMakingNoisy();
         
         final String channelId = getIntent().getStringExtra("channel_id") == null ? "" : getIntent().getStringExtra("channel_id");
         final String channelName = getIntent().getStringExtra("channel_name") == null ? "" : getIntent().getStringExtra("channel_name");
         final String program = getIntent().getStringExtra("program") == null ? "" : getIntent().getStringExtra("program");
         final String day = getIntent().getStringExtra("day") == null ? "1" : getIntent().getStringExtra("day");
         AlertDialog dialog = new AlertDialog.Builder(AlarmAlertActivity.this).setIcon(R.drawable.clock)
                 .setTitle(channelName)
                 .setMessage(program)
                 .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() 
                 {
                     @Override
                     public void onClick(DialogInterface dialog, int which) 
                     {
                         stopMakingNoisy();
                         if (AppEngine.getInstance().getContext() == null)
                             AppEngine.getInstance().setContext(AlarmAlertActivity.this);    // 需要设置，否则会有空指针的异常
                         
                         HashMap<String, Object> info = new HashMap<String, Object>();
                         info.put("channel_id", channelId);
                         info.put("channel_name", channelName);
                         info.put("program", program);
                         info.put("day", day);
                         AppEngine.getInstance().getAlarmHelper().notifyAlarmListeners(info);
                         AppEngine.getInstance().getAlarmHelper().removeAlarm(channelId, channelName, program, Integer.valueOf(day).intValue());
                         AlarmAlertActivity.this.finish();
                     }
                 }).create();
         dialog.setCancelable(false);
         dialog.setOnKeyListener(new DialogInterface.OnKeyListener() 
         {       
             @Override
             public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) 
             {
                 if (keyCode == KeyEvent.KEYCODE_SEARCH)
                     return true;
                 else
                     return false;
             }
         });
         dialog.show();
     }
     
     @Override
     public void onDestroy()
     {
         super.onDestroy();
         resetAllAlarms();
     }
     
     public Uri getDefaultRingtoneUri(int type)
     {
         return RingtoneManager.getActualDefaultRingtoneUri(this, type);
     }
     
     private void startMakingNoisy()
     {
         if (mMediaPlayer != null)
         {
             mMediaPlayer.setLooping(true);
             mMediaPlayer.start();
         }
         if (mVibrator != null)
         {
             mVibrator.vibrate(new long[]{600, 1000, 600, 1000, 600, 1000}, 0);
         }
     }
     
     private void stopMakingNoisy()
     {
     	if (mMediaPlayer != null)
     	    mMediaPlayer.stop();
     	if (mVibrator != null)
     	    mVibrator.cancel();
     }
     
     // 重新计算闹铃时间
     private void resetAllAlarms()
     {
         if (AppEngine.getInstance().getContext() == null)
             AppEngine.getInstance().setContext(AlarmAlertActivity.this);
         AppEngine.getInstance().getAlarmHelper().resetAllAlarms();
     }
 }
