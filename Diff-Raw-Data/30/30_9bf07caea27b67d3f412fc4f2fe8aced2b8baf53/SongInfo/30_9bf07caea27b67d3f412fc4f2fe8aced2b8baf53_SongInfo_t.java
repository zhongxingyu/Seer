 package com.adrian.music.player;
 
 import com.adrian.music.events.NewSongPlayingEvent;
 import com.adrian.music.events.eventsBus.MusicEventBus;
 import com.adrian.music.models.Track;
 import com.adrian.music.streamMeta.IcyStreamMeta;
 
 import javax.sound.sampled.SourceDataLine;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 /**
  * Created with IntelliJ IDEA.
  * User: lordfire
  * Date: 1/09/13
  * Time: 11:07
  * To change this template use File | Settings | File Templates.
  */
 public class SongInfo implements Runnable{
 
     String streamingURL;
     URL url;
     SourceDataLine line;
     String nowPlaying = "null";
     IcyStreamMeta metaData;
 
     public SongInfo(String streamingURL, SourceDataLine line) throws MalformedURLException {
         this.streamingURL = streamingURL;
         this.line = line;
 
         url = new URL(streamingURL);
         metaData = new IcyStreamMeta(url);
     }
 
 
 
 
     @Override
     public void run() {
 
          int intentos=3;
 
         while(!line.isOpen() && intentos>0){
 
             //Se espera una conexion con line
             try {
                 Thread.sleep(3000);
                 intentos--;
             } catch (InterruptedException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
 
         }
 
 
         while(line.isOpen()){
             String artist;
             String title;
             try {
 
                 metaData.refreshMeta();
 
 
                 try {
 
                     title = metaData.getTitle();
 
                 }catch (Exception e){
                    title = "";
                 }
 
                try {
 
                  artist = metaData.getArtist();
 
                 }catch (Exception e){
                  artist = "";
                 }
 
                 if(!nowPlaying.contentEquals(title))
                 {
                        nowPlaying=title;
 
                     Track track = new Track();
                     track.setName(title);
                     track.setArtist(artist);
 
                     MusicEventBus.getMusicEventBus().post(new NewSongPlayingEvent(track));
                 }
 
 
                 Thread.sleep(5000);
 
             } catch (IOException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             } catch (InterruptedException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
 
         }
 
     }
 
 }
