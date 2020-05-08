 package com.project944.cov.extplayer;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JButton;
 
 import org.bff.slimserver.SlimDatabase;
 import org.bff.slimserver.SlimPlayer;
 import org.bff.slimserver.SlimPlaylist;
 import org.bff.slimserver.SlimServer;
 import org.bff.slimserver.events.PlayerChangeEvent;
 import org.bff.slimserver.events.PlayerChangeListener;
 import org.bff.slimserver.exception.SlimConnectionException;
 import org.bff.slimserver.exception.SlimException;
 import org.bff.slimserver.musicobjects.SlimAlbum;
 import org.bff.slimserver.musicobjects.SlimSong;
 
 import com.project944.cov.CoverDetails;
import com.project944.cov.TrackDetails;
 import com.project944.cov.sources.SlimCoverSource;
 
 public class SlimPlayerInterface implements PlayerInterface {
     
     private SlimServer server;
     private SlimDatabase db;
     private SlimPlayer player;
     private JButton playButton;
     private Timer timer;
 
     public SlimPlayerInterface(SlimServer server, SlimPlayer player) {
         this.server = server;
         this.db = new SlimDatabase(server);
         this.player = player;
     }
     
     public void setPlayPauseButton(final JButton button) {
         this.playButton = button;
         this.player.addPlayerChangeListener(new PlayerChangeListener() {
             public void playerChanged(PlayerChangeEvent event) {
                 fixPlayButton();
             }
         });
         this.timer = new Timer();
         this.timer.schedule(new TimerTask() {
             @Override
             public void run() {
                 fixPlayButton();
             }
         }, 5000, 5000);
         fixPlayButton();
     }
 
     protected void fixPlayButton() {
         try {
             if ( player.isPlaying() ) {
                 playButton.setText("||");
             } else {
                 playButton.setText(">");
             }
         } catch (SlimConnectionException e) {
             System.out.println("Failed to work out if player is playing with "+e);
             e.printStackTrace();
         }
     }
 
     public void enqueueAlbum(CoverDetails cover, boolean playImmediately) {
         SlimPlaylist playList = new SlimPlaylist(player);
         SlimAlbum album = findAlbum(cover);
         if ( album != null ) {
             try {
                 if ( !isPlaying() || playImmediately ) {
                    boolean wantNext = playList.getItemCount() > 0;
                     playList.insertAlbum(album);
                    if ( wantNext ) {
                        playList.playNext();
                    }
                 } else {
                    List<TrackDetails> tracks = cover.getTrackNames();
                    for (TrackDetails track : tracks) {
                        enqueueTrack(cover, track.getTitle(), false);
                    }
                    //playList.insertAlbum(album);
                 }
             } catch (SlimException e) {
                 e.printStackTrace();
             }
         }
         playIfStopped(playImmediately);
     }
 
     public void enqueueTrack(CoverDetails cover, String trackName, boolean playImmediately) {
         SlimPlaylist playList = new SlimPlaylist(player);
         SlimAlbum album = findAlbum(cover);
         List<SlimSong> songs = new ArrayList<SlimSong>(db.listSongsForAlbum(album));
         String cleanedTrackName = new String(trackName.getBytes());
         for (SlimSong song : songs) {
             if ( cleanedTrackName.equals(new String(song.getTitle().getBytes())) ) {
                 try {
                     if ( !isPlaying() || playImmediately ) {
                         playList.insertItem(song);
                         playList.playNext();
                     } else {
                         playList.addItem(song);
                     }
                     playIfStopped(playImmediately);
                     return;
                 } catch (SlimException e) {
                     e.printStackTrace();
                 }
             }
         }
         for (SlimSong song : songs) {
             System.out.println("Checked ["+song.getTitle()+"]");
         }
         System.out.println("Failed to match track ["+trackName+"] in album "+cover.getAlbum());
     }
 
     private void playIfStopped(boolean playImmediately) {
         try {
             if ( !player.isPlaying() ) {
                 player.play();
             }
         } catch (Exception e) {
             System.out.println("Failed to start playing with "+e);
             e.printStackTrace();
         }
     }
     
     public boolean isPlaying() {
         try {
             return player.isPlaying();
         } catch (SlimConnectionException e) {
             System.out.println("Failed to get isPlaying with "+e);
             return false;
         }
     }
     
     public void play() {
         try {
             player.play();
         } catch (SlimConnectionException e) {
             System.out.println("Failed to make play with "+e);
         }
     }
     
     public void pause() {
         try {
             player.pause();
         } catch (SlimConnectionException e) {
             System.out.println("Failed to make pause with "+e);
         }
     }
     
     public void playPrev() {
         try {
             SlimPlaylist playList = new SlimPlaylist(player);
             playList.playPrevious();
         } catch (Exception e) {
             System.out.println("Failed to playPrev with "+e);
             e.printStackTrace();
         }
     }
     
     public void playNext() {
         try {
             SlimPlaylist playList = new SlimPlaylist(player);
             playList.playNext();
         } catch (Exception e) {
             System.out.println("Failed to playNext with "+e);
             e.printStackTrace();
         }
     }
     
     public void clearPlayQueue() {
         try {
             SlimPlaylist playList = new SlimPlaylist(player);
             playList.clear();
         } catch (Exception e) {
             System.out.println("Failed to clear playlist with "+e);
             e.printStackTrace();
         }
     }
     
     private SlimAlbum findAlbum(CoverDetails cover) {
         Collection<SlimAlbum> albums = db.getAlbums();
         for (SlimAlbum album : albums) {
             if ( cover.getAlbum().equals(album.getTitle()) ) {
                 String artist = SlimCoverSource.getArtistForAlbum(db, album);
                 if ( cover.isVariousArtists() || cover.getArtist().equals(artist) ) {
                     return album;
                 }
             }
         }
         return null;
     }
     
 }
