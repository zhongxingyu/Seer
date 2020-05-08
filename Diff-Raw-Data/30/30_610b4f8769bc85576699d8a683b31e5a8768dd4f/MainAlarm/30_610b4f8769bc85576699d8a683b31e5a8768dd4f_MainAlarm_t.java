 package com.github.browep.wegu;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import com.github.browep.wegu.util.Log;
 
 import javax.print.attribute.standard.Media;
 import java.beans.Visibility;
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 
 public class MainAlarm extends WeguActivity
 {
     LinearLayout mLinearLayout;
     Vibrator v;
     MediaPlayer mMediaPlayer;
 
     private static final String HOSTNAME = "http://wegu.heroku.com";
     private static final String NEXT_IMAGE_PATH = "/images/next";
     private Button offButton;
 
     private Drawable getDrawableFromUrl(String url) {
 		try {
 			InputStream is = (InputStream) this.fetch(url);
 			Drawable d = Drawable.createFromStream(is, "src");
 			return d;
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 			return null;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
     public String convertStreamToString(InputStream is)
             throws IOException {
         /*
          * To convert the InputStream to String we use the
          * Reader.read(char[] buffer) method. We iterate until the
          * Reader return -1 which means there's no more data to
          * read. We use the StringWriter class to produce the string.
          */
         if (is != null) {
             Writer writer = new StringWriter();
 
             char[] buffer = new char[1024];
             try {
                 Reader reader = new BufferedReader(
                         new InputStreamReader(is, "UTF-8"));
                 int n;
                 while ((n = reader.read(buffer)) != -1) {
                     writer.write(buffer, 0, n);
                 }
             } finally {
                 is.close();
             }
             return writer.toString();
         } else {
             return "";
         }
     }
 
     public String fetchContents(String address) {
         try {
             InputStream is = (InputStream) this.fetch(address);
             return convertStreamToString(is);
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             return null;
         }
 
 
     }
 
 	public Object fetch(String address) throws MalformedURLException,IOException {
 		URL url = new URL(address);
 		Object content = url.getContent();
 		return content;
 	}
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
 
         super.onCreate(savedInstanceState);
         Log.i("ALARM FIRE!!!!");
 
        if(dao.getBooleanPreference(Constants.DO_ALARM)){
            Log.i("do_alarm was set to true, doing alarm");
            dao.setBooleanPreference(Constants.DO_ALARM,false);
        } else {
            Log.i("do_alarm was set to false, doing welcome screen");
            Intent welcomeIntent = new Intent(getApplicationContext(), Welcome.class);
            welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(welcomeIntent);
            finish();
            return;

        }


         if(getIntent().getExtras() != null){
             Log.i("we have NOT null extras: " + getIntent().getExtras());
             Log.i(Constants.FROM_ALARM + " was  " + getIntent().getExtras().getBoolean(Constants.FROM_ALARM));
         }else{
             Log.i("extras was null");
         }
 
         // this was not from the alarm, go back to the welcome screen
 //        if(getIntent().getExtras() == null || !getIntent().getExtras().getBoolean(Constants.FROM_ALARM)){
 //            Log.i("Alarm was not started from the alarm receiver, showing welcome");
 
 //            Intent welcomeIntent = new Intent(getApplicationContext(),Welcome.class);
 //            welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 //            getApplicationContext().startActivity(welcomeIntent);
 //            finish();
 //            return;
 //        }else{
 //            Log.i("Alarm was from alarm receiver.  Firing alarm");
 //        }
 
 
 
         final Window win = getWindow();
         win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                 | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
         // Turn on the screen unless we are being launched from the AlarmAlert
         // subclass.
         if (!getIntent().getBooleanExtra(Constants.SCREEN_OFF, false)) {
             win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                     | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                     | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
         }
 
 //        mLinearLayout = findViewById(R.layout.main);
         setContentView(R.layout.main);
 
 
 	    // Instantiate an ImageView and define its properties
 	    ImageView i = (ImageView) findViewById(R.id.image_view);
         String url = fetchContents(HOSTNAME + NEXT_IMAGE_PATH);
         i.setImageDrawable(getDrawableFromUrl(url));
 
         // setup the off button
         offButton = (Button) findViewById(R.id.alarm_off);
         offButton.setOnClickListener(new View.OnClickListener(){
             public void onClick(View view) {
                 v.cancel();
                 mMediaPlayer.stop();
                 offButton.setVisibility(View.INVISIBLE);
             }
         });
 
         // vibrate the phone
         List<Long> _pattern = new LinkedList<Long>();
         for (int j = 0; j < 10; j++) {
             _pattern.add(300L);
             _pattern.add(500L);
             _pattern.add(300L);
             _pattern.add(500L);
             _pattern.add(300L);
             _pattern.add(1000L);
         }
 
         long[] pattern = new long[_pattern.size()];
         for(int j =0;j<_pattern.size();j++)
             pattern[j] = _pattern.get(j);
 
         v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
         v.vibrate(pattern, -1);
 
         // play sound
 
         try {
             Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
             mMediaPlayer = new MediaPlayer();
             mMediaPlayer.setDataSource(this, alert);
             final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
             if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                 mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                 mMediaPlayer.setLooping(true);
                 mMediaPlayer.prepare();
                 mMediaPlayer.start();
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
 
     }
 }
