 package com.zabozhanov.chilly.chilly_player;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Intent;
 import android.media.MediaPlayer;
 import android.os.AsyncTask;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.Toast;
 import com.zabozhanov.chilly.MyActivity;
 import com.zabozhanov.chilly.R;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 /**
  * Created with IntelliJ IDEA.
  * User: denis
  * Date: 23.11.12                               ц
  * Time: 10:13
  * To change this template use File | Settings | File Templates.
  */
 
 public class ChillyService extends Service implements MediaPlayer.OnPreparedListener {
 
     private String _stream_url = "http://www.chilloungestation.com:8000/chilloungestation-playlist";
     private final String _pageUrl = "http://www.chilloungestation.com:8000/";
 
     private MediaPlayer _player = null;
     private int NOTIFICATION = 1234;
     private NotificationManager _nm;
 
     private Boolean _paused = false;
 
     private Boolean _isPreparing = true;
 
     private ChillyDelegate _delegate = null;
     public ChillyDelegate get_delegate() {
         return _delegate;
     }
 
     public void playPause() {
         if (_paused) {
             play();
         } else {
             pause();
         }
     }
 
     public void setDelegate(ChillyDelegate delegate) {
         _delegate = delegate;
 
         if (_isPreparing) {
             _delegate.preparing();
             return;
         }
 
         if (_player.isPlaying()) {
             _delegate.playing();
         } else {
             _delegate.paused();
         }
     }
 
     private void play() {
         _player.start();
         _delegate.playing();
         _paused = false;
     }
     protected void pause() {
         _player.pause();
         _delegate.paused();
         _paused = true;
     }
 
 
     @Override
     public void onPrepared(MediaPlayer mediaPlayer) {
         _isPreparing = false;
         if (!_player.isPlaying()) {
             play();
             _songUpdater.run();
         }
     }
 
     public void initPlayback(ChillyDelegate delegate) {
 
         this._delegate = delegate;
 
         if (_player == null) {
 
             _player = new MediaPlayer();
             try {
                 _player.setDataSource(_stream_url);
             } catch (IOException e) {
             }
 
             _delegate.preparing();
             _player.setOnPreparedListener(this);
             _player.prepareAsync();
         } else {
 
             _delegate.preparing();
         }
     }
 
     private class DownloadHtmlTask extends AsyncTask<Void, Void, Void>
     {
         private String currentTrack;
         private String getCurrentTrack() {
 
             HttpClient httpClient = new DefaultHttpClient();
             HttpGet httpGet = new HttpGet(_pageUrl);
             try {
                 HttpResponse response = httpClient.execute(httpGet);
                 HttpEntity httpEntity = response.getEntity();
                 InputStream inputStream = httpEntity.getContent();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                 StringBuilder sb = new StringBuilder();
                 String line = null;
                 while ((line = reader.readLine()) != null) {
                     sb.append(line + "\n");
                 }
                 String resultString = sb.toString();
 
                 inputStream.close();
                 return resultString;
 
             } catch (IOException e) {
                 e.printStackTrace();
             }
             return null;
         }
 
         @Override
         protected Void doInBackground(Void... voids) {
             String html = getCurrentTrack();
             int findIndex = html.indexOf("http://www.chilloungestation.com</a></td>");
             if (findIndex > -1) {
 
                 String findStr = "streamdata";
                 findIndex = html.indexOf(findStr, findIndex) + findStr.length()+2;
                 html = html.substring(findIndex);
                 currentTrack = html.substring(0, html.indexOf("<"));
             }
             return null;
         }
 
         @Override
         protected void onPostExecute(Void aVoid) {
             if (currentTrack != null) {
                 ChillyService.this._delegate.setCurrentTrack(currentTrack);
             }
             currentTrack = null;
         }
     }
 
     private Handler updateSongHandler = new Handler();
     Runnable _songUpdater = new Runnable() {
         @Override
         public void run() {
             new DownloadHtmlTask().execute();
            updateSongHandler.postDelayed(_songUpdater, 40L * 1000);
         }
     };
 
     public class ChillyBinder extends Binder {
         public ChillyService getService() {
             return ChillyService.this;
         }
     }
 
 
     @Override
     public void onCreate() {
         _nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         Log.i("LocalService", "Received start id " + startId + ": " + intent);
         return START_STICKY;
     }
 
     @Override
     public void onDestroy() {
         _nm.cancel(NOTIFICATION);
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return _Binder;
     }
 
     private final IBinder _Binder = new ChillyBinder();
 }
