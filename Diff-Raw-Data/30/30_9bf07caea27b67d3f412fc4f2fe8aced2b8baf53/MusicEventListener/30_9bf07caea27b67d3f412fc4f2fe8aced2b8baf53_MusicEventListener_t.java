 package com.adrian.music.events.listeners;
 
 import com.adrian.music.events.NewSongPlayingEvent;
 import com.adrian.music.events.PlayerStartPlaying;
 import com.adrian.music.notifications.launcher.NotificationLauncher;
 import com.adrian.music.player.SongInfo;
 import com.google.common.eventbus.Subscribe;
 
 import java.net.MalformedURLException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: lordfire
  * Date: 1/09/13
  * Time: 11:47
  * To change this template use File | Settings | File Templates.
  */
 public class MusicEventListener {
 
 
    @Subscribe
     public void handleNewSongPlaying(NewSongPlayingEvent event){
 
        //TODO Mejorar que no llegue vacia
       if(event.getTrack().getArtist()!="" || event.getTrack().getName()!="")
       {
           Thread notificationThread;
           notificationThread = new Thread(new NotificationLauncher(event.getTrack()));
           notificationThread.start();
       }
    }
 
 
     @Subscribe
     public void handlePlayerStartPlaying(PlayerStartPlaying event)
     {
         //Acciones a realizar cuando el player comience a sonar
 
         //Se lanza el hilo que trabaja los metadatos y notificaciones
 
         Thread infoTrhead = null;
         try {
 
 
             infoTrhead = new Thread(new SongInfo(event.getStreamUrl(),event.getLine()));
 
 
         } catch (MalformedURLException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         infoTrhead.start();
 
     }
 }
