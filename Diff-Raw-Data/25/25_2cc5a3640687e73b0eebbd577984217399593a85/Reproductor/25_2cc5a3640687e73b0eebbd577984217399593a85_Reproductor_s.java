 package com.melchor629.musicote;
 
 import java.util.ArrayList;
 
 import com.melchor629.musicote.scrobbler.Auth;
 import com.melchor629.musicote.scrobbler.Peticiones;
 import com.melchor629.musicote.scrobbler.Scrobble;
 
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.os.IBinder;
 import android.os.Looper;
 import android.os.PowerManager;
 import android.preference.PreferenceManager;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.app.TaskStackBuilder;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * Reproductor del Musicote 0.1
  * TODO Añadir interfaz gráfica para manejar el reproductor
  * @author melchor
  */
 public class Reproductor extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
 
     /**
      * Static variable for the Media Player
      */
     static MediaPlayer reproductor = new MediaPlayer();
 
     public volatile static String url;
     public volatile static String tit;
     public volatile static String art;
     public volatile static boolean paused;
 
     private coso cosa;
     private NotificationManager nm;
     private volatile static ArrayList<String[]> playlist;
     private volatile boolean True = true;
 
     public volatile static double a = -1;
 
     public int onStartCommand (Intent intent, int flags, int StartID){
         playlist = new ArrayList<String[]>();
         String [] eso = addSong(intent.getStringExtra("titulo"), intent.getStringExtra("artista"), intent.getStringExtra("archivo"));
         initMediaPlayer(eso);
         return START_STICKY;
     }
 
     /**
      * When the service is ready, start playing the song
      * @param urla <i>URL for the file</i>
      * @param titulo <i>Title of the song</i>
      * @param artista <i>Song' Artist</i>
      */
     public void initMediaPlayer(String[] song){
     	tit = song[0];
     	art = song[1];
     	url = song[2];
     	
     	reproductor = newPlayer(song);
     	
         notification();
     }
     
     /**
      * Configura un reproductor
      */
     private MediaPlayer newPlayer(String[] song) {
     	String url = song[2];
         MediaPlayer reproductor = new MediaPlayer(); // initialize it here
         reproductor.setAudioStreamType(AudioManager.STREAM_MUSIC);
         try {
             reproductor.setDataSource(url.replace(" ", "%20"));
         } catch (Exception e) {
             Log.e("Reproductor","Error al descargar: "+ e.toString());
             if(e.toString().equals("(1, -1004"))
             	Toast.makeText(this, "No se ha podido descargar la canción", Toast.LENGTH_LONG).show();
             Intent in = new Intent(getApplicationContext(), Reproductor.class);
             stopService(in);
         }
         reproductor.setOnPreparedListener(this);
         reproductor.setOnCompletionListener(this);
         reproductor.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
         reproductor.prepareAsync(); // prepare async to not block main thread
         return reproductor;
     }
     
     /**
      * Notificación de la canción
      */
     public void notification() {
     	int mID = 1;
 
         NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
         notification
                 .setSmallIcon(R.drawable.altavoz)
                 .setContentTitle("Musicote")
                 .setContentText(getResources().getString(R.string.playing)+" "+tit+" "+getResources().getString(R.string.playing_of)+" "+art)
                 .setOngoing(true);
         
        NotificationCompat.InboxStyle inbox = new NotificationCompat.InboxStyle();
        inbox.setBigContentTitle(getResources().getString(R.string.playing)+" "+tit+" "+getResources().getString(R.string.playing_of)+" "+art);
        for(int i = 0; i < playlist.size(); i++) {
        	if(i == 0)
        		inbox.addLine("Y después..."); //TODO translate this
        	else
        		inbox.addLine(playlist.get(i)[0] + this.getResources().getString(R.string.playing_of) + playlist.get(i)[1]);
         }
        notification.setStyle(inbox);
 
         Intent resultIntent = new Intent(this, MainActivity.class);
 
         TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
         stackBuilder.addParentStack(MainActivity.class);
         stackBuilder.addNextIntent(resultIntent);
         PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
         notification.setContentIntent(resultPendingIntent);
         nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
         nm.cancel(mID);
         nm.notify(mID, notification.build());
     }
 
     /**
      * Se llama cuando el reproductor está listo
      */
     public void onPrepared(final MediaPlayer player) {
         final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
         if(pref.getBoolean("lastact", false)==true){
             Auth auth = new Auth(pref.getString("usuario", null), pref.getString("contraseña", null));
             auth.getSK();
             Scrobble scr = new Scrobble(tit, art);
             scr.nowPlaying();
         }else{
             Log.d("Scrobbler", "Nada de Scrobblings...");
         }
 
         new Thread(new Runnable(){@Override public void run(){
         	cosa = new coso();
         	True = true;
         	player.start();
         	cosa.run(player, pref, nm);
         }}).start();
     }
     
     /**
      * Pause or resume the song
      */
     public static void pause(){
         try{
             if(reproductor.isPlaying()){
                 reproductor.pause();
                 paused = true;
             }else{
                 reproductor.start();
                 paused = false;
             }
          }catch(IllegalStateException e){
             Log.d("Reproductor", "Se ha invocado el pause, aunque el reproductor está cerrado");
         }
     }
     
     /**
      * Add a song into playlist
      * @return eso A String[] for use, if you want
      */
     public static String[] addSong(String titulo, String artista, String urle) {
     	if(playlist == null)
     		playlist = new ArrayList<String[]>();
 		String[] eso = new String[] {titulo, artista, urle};
 		playlist.add(eso);
 		int SongID = playlist.indexOf(eso);
 		Log.d("Reproductor", titulo + " añadida a la lista de reproducción con ID " + SongID);
 		return eso;
     }
     
     /**
      * Comprueba si hay otra canción después de la actual
      */
     public boolean isNextSong() {
     	try {
     		Log.d("Reproductor", "Left "+(playlist.size()-1)+" "+(playlist.size() > 1 ? "Hay una siguiente canción" : "No hay una siguiente canción"));
     		return playlist.size() > 1;
     	} catch(java.lang.IndexOutOfBoundsException e) {
     		Log.d("Reproductor", "No hay una siguiente canción");
     		return false;
     	}
     }
     
     /**
      * Pasa a la siguiente canción
      */
     public void nextSong() {
     	playlist.remove(0);
     	Log.d("Reproductor", "Siguiente canción");
     	reproductor.release();
     	reproductor = null;
     	initMediaPlayer(playlist.get(0));
     }
     
     @Override
     public void onCompletion(MediaPlayer mp) {
     	Log.d("Reproductor", "Canción lista");
     	True = false;
     	tit = art = url = null;
     	if(isNextSong()){
             nextSong();
     	} else {
     		onDestroy();
     	}
     }
 
     /* (non-Javadoc)
      * @see android.app.Service#onBind(android.content.Intent)
      */
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
    @Override
    public void onDestroy() {
 	   if(!cosa.getState().toString().equals("NEW"))
 		   cosa.interrupt();
        if (reproductor != null)
            reproductor.release();
        nm.cancelAll();
        a = -1;
     }
 
     /**
      * Class for manage the current position of the song, and send the scrobbling
      * @author melchor9000
      *
      */
     class coso extends Thread {
         public void run(MediaPlayer player, SharedPreferences pref, NotificationManager nm){
             Looper.prepare();
             try{
             	boolean o = true;
             	int count = playlist.size();
 
             	while(True) {
                     try{
                         a = (player.getCurrentPosition()/(player.getDuration()/100d));
                         if((int)a==50 && o){
                             if(pref.getBoolean("lastact", false) == true){
                             	o = false;
                                 Scrobble scr = new Scrobble(tit, art);
                                 int e = scr.scrobble();
                                 if(e != 0)
                                 	Toast.makeText(Reproductor.this, "Last.FM: "+Peticiones.errorM[e], Toast.LENGTH_LONG).show();
                             	Log.d("Scrobbler", "Error: "+e+"\nMessage: "+Peticiones.errorM[e]);
                             }
                         }
                         
                         Thread.sleep(100);
                         
                     	if(player.getCurrentPosition() >= player.getDuration()){
                     		a = 0;
                     		player.stop();
                         	True = false;
                         	this.finalize();
                         	this.interrupt();
                         }
                     	
                     	if(count != playlist.size()) {
                     		notification();
                     		count = playlist.size();
                     	}
                     }catch (Exception e){
                     	if(True)
                     		Log.e("Reproductor", "No se sabe porqué pero se ha cerrado...\n"+e.getMessage());
                         True = false;
                         this.finalize();
                     }
                 }
             } catch (IllegalStateException e){
                 nm.cancelAll();
                 a = -1;
                 this.interrupt();
                 Log.d("Reproductor", "Se ha detectado que el reproductor se ha cerrado, esto tambien se cierra");
             } catch (Exception e){
                 Log.e("Reproductor", "Ha habido un error en el \"coso\": "+e.toString());
             } catch(Throwable e) {
             	Log.e("Reproductor", "Ha habido un error en el \"coso\": "+e.toString());
             }
         }
     }
 }
