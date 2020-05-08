 package net.kokkeli.player;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import net.kokkeli.ISettings;
 import net.kokkeli.data.ILogger;
 import net.kokkeli.data.LogSeverity;
 import net.kokkeli.data.PlayList;
 import net.kokkeli.data.Track;
 import net.kokkeli.data.db.NotFoundInDatabase;
 import net.kokkeli.data.services.IPlaylistService;
 import net.kokkeli.data.services.ServiceException;
 
 import com.google.inject.Inject;
 import com.sun.jna.Native;
 import com.sun.jna.NativeLibrary;
 
 import uk.co.caprica.vlcj.binding.LibVlc;
 import uk.co.caprica.vlcj.component.AudioMediaPlayerComponent;
 import uk.co.caprica.vlcj.player.MediaMeta;
 import uk.co.caprica.vlcj.player.MediaPlayer;
 import uk.co.caprica.vlcj.runtime.RuntimeUtil;
 
 public class VlcPlayer implements IPlayer {
     private final PlayerComponent player;
     
     private final IPlaylistService playlistService;
     
     private Queue<String> trackQueue = new LinkedList<String>();
     private long currentPlaylistId = 0;
     private boolean playListPlaying = false;
     
     /**
      * Creates new Player
      * @param libLocation Location of 
      */
     @Inject
     public VlcPlayer(ISettings setting, ILogger logger, IPlaylistService playlistService){
         
         String vlcLibName = RuntimeUtil.getLibVlcLibraryName();
         NativeLibrary.addSearchPath(vlcLibName, setting.getVlcLocation());
         Native.loadLibrary(vlcLibName, LibVlc.class);
         
         player = new PlayerComponent(logger);
         this.playlistService = playlistService;
     }
     
     public void playFile(String file) throws InterruptedException{
         player.getMediaPlayer().playMedia(file);
     }
 
     @Override
     public void play() throws ServiceException {
         player.play();
     }
     
     @Override
     public void pause() {
     }
 
     @Override
     public String getTitle() {
         return player.currentTitle();
     }
 
     @Override
     public long getCurrentPlaylistId() throws NotPlaylistPlayingException {
         if (!playListPlaying)
             throw new NotPlaylistPlayingException("No playlist playing.");
         return currentPlaylistId;
     }
 
     @Override
     public boolean playlistPlaying() {
         return playListPlaying;
     }
     
     /**
      * Adds file to queue.
      * @param file
      */
     @Override
     public void addToQueue(String file){
         trackQueue.add(file);
     }
     
     @Override
     public void selectPlaylist(long id) throws NotFoundInDatabase, ServiceException {
         playlistService.getPlaylist(id);
         playListPlaying = true;
         currentPlaylistId = id;
     }
     
     /**
      * Extended AudioMediaPlayerComponent to match needs of this system.
      * @author Hekku2
      */
     private class PlayerComponent extends AudioMediaPlayerComponent{
         private final ILogger logger;
         private int trackPointer = 0;
         
         public PlayerComponent(ILogger logger){
             this.logger = logger;
         }
 
         public void play() throws ServiceException {
             String file = trackQueue.poll();
             if (file == null && playListPlaying){
                 try {
                     PlayList list = playlistService.getPlaylist(currentPlaylistId);
                     
                     //TODO Some randomization implementation
                     ArrayList<Track> tracks = list.getItems();
                     if (tracks.size() == 0){
                         return;
                     }
                     
                    if (tracks.size() < trackPointer){
                         trackPointer = 0;
                     }
                     
                     Track chosen = tracks.get(trackPointer++);
                     player.getMediaPlayer().playMedia(chosen.getLocation());
                     return;
                 } catch (NotFoundInDatabase e) {
                     logger.log("For some reason, playlist is playing but there is no playlist in database matching given id " + currentPlaylistId,LogSeverity.ERROR);
                     //Suppress, nothing can be done.
                     return;
                 }
             }
                 
             player.getMediaPlayer().playMedia(file);
         }
         
         @Override
         public void opening(MediaPlayer mediaPlayer) {
             super.opening(mediaPlayer);
             logger.log("Opening media.", LogSeverity.TRACE);
         }
         
         @Override
         public void stopped(MediaPlayer mediaPlayer) {
             super.stopped(mediaPlayer);
             logger.log("Media stopped.", LogSeverity.TRACE);
         }
         
         public void finished(MediaPlayer player){
             logger.log("Track finished.", LogSeverity.TRACE);
             if (playListPlaying || !trackQueue.isEmpty()){
                 try {
                     play();
                 } catch (ServiceException e) {
                     logger.log("Unable to choose item for playing.", LogSeverity.ERROR);
                     //TODO implement somekind of callback so error is shown to user.
                 }
             }
         }
         
         @Override
         public void playing(MediaPlayer mediaPlayer) {
             super.playing(mediaPlayer);
             logger.log("Playing media.", LogSeverity.TRACE);
         }
         
         @Override
         public void error(MediaPlayer mediaPlayer) {
             //This can happen, media is corrupted or for some reason cannot be played.
             logger.log("Failed to play media.", LogSeverity.ERROR);
             try {
                 play();
             } catch (ServiceException e) {
                 logger.log("Unable to choose item for playing.", LogSeverity.ERROR);
                 //TODO implement somekind of callback so error is shown to user.
             }
         }
         
         /**
          * Returns current title, or null if there is nothing playing
          * @return
          */
         public String currentTitle(){
             try {
                 //TODO Take this from variable or something.
                 MediaMeta meta = getMediaPlayer().getMediaMeta();
                 return meta.getArtist() + " - " + meta.getTitle();
             } catch (IllegalStateException e) {
                 return null;
             }
         }
     }
 }
