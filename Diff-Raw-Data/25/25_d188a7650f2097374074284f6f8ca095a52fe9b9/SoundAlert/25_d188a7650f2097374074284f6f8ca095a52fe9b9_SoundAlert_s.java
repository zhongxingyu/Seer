 package com.kinnack.nthings.model;
 
import java.io.StringReader;

import com.kinnack.nthings.R;

 import android.content.Context;
 import android.content.SharedPreferences;
 import android.media.AudioManager;
 import android.media.ToneGenerator;
 import android.os.Vibrator;
 
 // XXX This is a total hack in need of the state(ful?) pattern
 public class SoundAlert {
     boolean enabled = false;
     private ToneGenerator _toneGenerator;
     private Context _context;
     public SoundAlert(SharedPreferences preferences_, Context context_) {
         _context = context_;
         String soundSettingKey = context_.getResources().getString(R.string.sound_setting_key);
         enabled = preferences_.getBoolean(soundSettingKey, true);
         if (enabled) _toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
     }
     
     public void progressBeep() {
         if (enabled) _toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
     }
     
     public void finishedBeep() {
         if (enabled) _toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK);
     }
     
     public void vibrate() {
         if (!enabled) return;
         Vibrator vibrator = (Vibrator)_context.getSystemService(Context.VIBRATOR_SERVICE);
         vibrator.vibrate(new long[]{0,500,250,500}, -1);
     }
     
     public void cleanup() {
         if (enabled) _toneGenerator.release();
         _context = null;
    }
    
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if(_toneGenerator != null) _toneGenerator.release();
        _context = null;
         _toneGenerator = null;
     }
 }
