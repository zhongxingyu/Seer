 package com.hlidskialf.android.tonepicker;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.os.Bundle;
 import android.widget.Button;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.content.Intent;
 import android.content.Context;
 import android.view.View;
 import android.media.AudioManager;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.view.Menu;
 import android.view.MenuItem;
 
 import android.provider.Settings;
 
 public class TonePickerSplash extends Activity
 {
   public static final int REQUEST_RINGTONE = 42;
   public static final int REQUEST_NOTIFICATION = 43;
 
   private AudioManager mAudioManager;
   private MenuItem mAboutItem;
 
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.splash);
     
     mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
 
     initVolumeSlider(R.id.volume_ringer, AudioManager.STREAM_RING);
     initVolumeSlider(R.id.volume_music, AudioManager.STREAM_MUSIC);
     initVolumeSlider(R.id.volume_call, AudioManager.STREAM_VOICE_CALL);
     initVolumeSlider(R.id.volume_system, AudioManager.STREAM_SYSTEM);
     initVolumeSlider(R.id.volume_alarm, AudioManager.STREAM_ALARM);
 
     //TextView tv = (TextView)findViewById(R.id.splash_title);
     //tv.setText( getString(R.string.splash_title, getString(R.string.app_version)));
 
     Button button;
 
     button = (Button) findViewById(R.id.splash_button_ringtone);
     button.setOnClickListener(new Button.OnClickListener() {
       public void onClick(View v) {
         Intent i = new Intent(android.media.RingtoneManager.ACTION_RINGTONE_PICKER);
         String existing = Settings.System.getString(getContentResolver(), Settings.System.RINGTONE);
         if (existing != null) 
           i.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(existing));
         i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
         startActivityForResult(i, REQUEST_RINGTONE);
       }
     });
     button = (Button) findViewById(R.id.splash_button_notification);
     button.setOnClickListener(new Button.OnClickListener() {
       public void onClick(View v) {
         Intent i = new Intent(android.media.RingtoneManager.ACTION_RINGTONE_PICKER);
         String existing = Settings.System.getString(getContentResolver(), Settings.System.NOTIFICATION_SOUND);
         if (existing != null) 
           i.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(existing));
         i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
         startActivityForResult(i, REQUEST_NOTIFICATION);
       }
     });
 
 /*
     button = (Button) findViewById(R.id.splash_button_ok);
     button.setOnClickListener(new Button.OnClickListener() { 
       public void onClick(View v) { finish(); } 
     });
     */
 
   }
 
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) 
   {
     if (resultCode != RESULT_OK || data == null) return;
 
     Uri u = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
 
     if (u == null) return;
 
     if (requestCode == REQUEST_RINGTONE) {
       Settings.System.putString(getContentResolver(), Settings.System.RINGTONE, u.toString());
     }
     else
     if (requestCode == REQUEST_NOTIFICATION) {
       Settings.System.putString(getContentResolver(), Settings.System.NOTIFICATION_SOUND, u.toString());
     }
   }
 
   private void initVolumeSlider(int seekbar_id, int stream_id)
   {
     final int stream = stream_id;
     SeekBar sb = (SeekBar)findViewById(seekbar_id);
    sb.setProgress( volumeToProgress(mAudioManager, stream) );
     sb.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
       public void onProgressChanged(SeekBar sb, int progress, boolean touch) {
        int vol = (progress >= 98) ? mAudioManager.getStreamMaxVolume(stream) : 
                                     progressToVolume(mAudioManager, stream, progress) ;

        if (vol != mAudioManager.getStreamVolume(stream) ) {
          mAudioManager.setStreamVolume(stream, vol, AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
         }
       }
       public void onStartTrackingTouch(SeekBar sb) {}
       public void onStopTrackingTouch(SeekBar sb) {}
     });
   }
  private static int volumeToProgress(AudioManager mgr, int stream)
  {
    return (int)(((float)mgr.getStreamVolume(stream) / (float)mgr.getStreamMaxVolume(stream)) * 100);
  }
  private static int progressToVolume(AudioManager mgr, int stream, int progress)
  {
    return (int)((float)mgr.getStreamMaxVolume(stream) *( (float)progress/(float)100));
  }
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     super.onCreateOptionsMenu(menu);
     mAboutItem = menu.add(0, 0, 0, R.string.about);
     mAboutItem.setIcon(android.R.drawable.ic_menu_info_details);
 
     return true;
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     if (item.equals(mAboutItem)) {
       View v = getLayoutInflater().inflate(R.layout.about_dialog,null);
       AlertDialog dia = new AlertDialog.Builder(this)
           .setTitle(R.string.about_title)
           .setView(v)
           .setPositiveButton(android.R.string.ok,null)
           .show();
     }
 
     return super.onOptionsItemSelected(item);
   }
  
 
 }
