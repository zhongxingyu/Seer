 /*
  * Copyright 2013 The Last Crusade ContactLastCrusade@gmail.com
  * 
  * This file is part of SoundStream.
  * 
  * SoundStream is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SoundStream is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SoundStream.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.lastcrusade.soundstream.service;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.os.Binder;
 import android.os.IBinder;
 import android.util.Log;
 
 import com.lastcrusade.soundstream.R;
 import com.lastcrusade.soundstream.audio.AudioPlayerWithEvents;
 import com.lastcrusade.soundstream.audio.IPlayer;
 import com.lastcrusade.soundstream.audio.RemoteAudioPlayer;
 import com.lastcrusade.soundstream.audio.SingleFileAudioPlayer;
 import com.lastcrusade.soundstream.manager.PlaylistDataManager;
 import com.lastcrusade.soundstream.model.Playlist;
 import com.lastcrusade.soundstream.model.PlaylistEntry;
 import com.lastcrusade.soundstream.model.SongMetadata;
 import com.lastcrusade.soundstream.service.MessagingService.MessagingServiceBinder;
 import com.lastcrusade.soundstream.service.MusicLibraryService.MusicLibraryServiceBinder;
 import com.lastcrusade.soundstream.util.BroadcastIntent;
 import com.lastcrusade.soundstream.util.BroadcastRegistrar;
 import com.lastcrusade.soundstream.util.IBroadcastActionHandler;
 import com.lastcrusade.soundstream.util.SongMetadataUtils;
 import com.lastcrusade.soundstream.util.Toaster;
 
 /**
  * This service is responsible for holding the play queue and sending songs to
  * the SingleFileAudioPlayer
  */
 public class PlaylistService extends Service {
 
 
     /**
      * Broadcast action used to toggle playing and pausing the current song.
      * 
      * This will control the playlist service and music control.
      * 
      */
     public static final String ACTION_PLAY_PAUSE = PlaylistService.class
             .getName() + ".action.PlayPause";
 
     /**
      * Broadcast action used to pause the current song.
      * 
      * This will control the playlist service and music control.
      * 
      */
     public static final String ACTION_PAUSE = PlaylistService.class
             .getName() + ".action.Pause";
     
     /**
      * Broadcast action used to skip the current song.
      * 
      * This will control the playlist service and music control.
      * 
      */
     public static final String ACTION_SKIP = PlaylistService.class
             .getName() + ".action.Skip";
 
     /**
      * Broadcast action sent when the Audio Player service is paused.
      */
     public static final String ACTION_PAUSED_AUDIO = PlaylistService.class
             .getName() + ".action.PausedAudio";
 
     /**
      * Broadcast action sent when the Audio Player service starts playing.
      */
     public static final String ACTION_PLAYING_AUDIO = PlaylistService.class
             .getName() + ".action.PlayingAudio";
 
     /**
      * Broadcast action sent when the Audio Player service is asked to skip a
      * song.
      */
     public static final String ACTION_SKIPPING_AUDIO = PlaylistService.class
             .getName() + ".action.SkippingAudio";
 
     /**
      * Broadcast action sent when the playlist gets updated
      */
     public static final String ACTION_PLAYLIST_UPDATED = PlaylistService.class + ".action.PlaylistUpdated";
     
     public static final String ACTION_SONG_REMOVED     = PlaylistService.class + ".action.SongRemoved";
     
     public static final String ACTION_SONG_ADDED     = PlaylistService.class + ".action.SongAdded";
     
 
     public static final String ACTION_SONG_PLAYING     = PlaylistService.class + ".action.SongPlaying";
     public static final String EXTRA_SONG              = PlaylistService.class + ".extra.Song";
 
     private static final String TAG = PlaylistService.class.getName();
 
     /**
      * Class for clients to access. Because we know this service always runs in
      * the same process as its clients, we don't need to deal with IPC.
      */
     public class PlaylistServiceBinder extends Binder implements
             ILocalBinder<PlaylistService> {
         public PlaylistService getService() {
             return PlaylistService.this;
         }
     }
 
     private BroadcastRegistrar    registrar;
     private IPlayer               mThePlayer;
     private SingleFileAudioPlayer mAudioPlayer; //TODO remove this when we add stop to IPlayer
     private Playlist              mPlaylist;
 
     private Thread                mDataManagerThread;
     private PlaylistDataManager   mDataManager;
 
     private PlaylistEntry currentEntry;
     private boolean isLocalPlayer;
 
     private ServiceLocator<MusicLibraryService> musicLibraryLocator;
     private ServiceLocator<MessagingService> messagingServiceLocator;
 
     @Override
     public IBinder onBind(Intent intent) {
         messagingServiceLocator = new ServiceLocator<MessagingService>(
                 this, MessagingService.class, MessagingServiceBinder.class);
 
         //create the local player in a separate variable, and use that
         // as the player until we see a host connected
         this.mAudioPlayer  = new SingleFileAudioPlayer(this, messagingServiceLocator);
         //Assume we are local until we connect to a host
         isLocalPlayer      = true;
         this.mThePlayer    = new AudioPlayerWithEvents(this.mAudioPlayer, this);
         this.mPlaylist     = new Playlist();
         
         musicLibraryLocator = new ServiceLocator<MusicLibraryService>(
                 this, MusicLibraryService.class, MusicLibraryServiceBinder.class);
 
         registerReceivers();
         
         //start the data manager by default...it is disabled when
         // a host is connected
         startDataManager();
         return new PlaylistServiceBinder();
     }
 
     @Override
     public boolean onUnbind(Intent intent) {
         unregisterReceivers();
         messagingServiceLocator.unbind();
         return super.onUnbind(intent);
     }
 
     /**
      * Register intent receivers to control this service
      * 
      */
     private void registerReceivers() {
         this.registrar = new BroadcastRegistrar();
         this.registrar
         .addAction(SingleFileAudioPlayer.ACTION_SONG_FINISHED, new IBroadcastActionHandler() {
 
             @Override
             public void onReceiveAction(Context context, Intent intent) {
                 //NOTE: this is an indicator that the song data can be deleted...therefore, we don't
                 //want to set the flag until after the song has been played
                 if (currentEntry != null) {
                     currentEntry.setPlayed(true);
                     getMessagingService()
                         .sendSongStatusMessage(currentEntry);
                     currentEntry = null;
                 }
                 // automatically play the next song, but only if we're not paused
                 if (!mThePlayer.isPaused()) {
                     play();
                 }
                 new BroadcastIntent(ACTION_PLAYLIST_UPDATED).send(PlaylistService.this);
             }
         })
         .addAction(ConnectionService.ACTION_HOST_CONNECTED, new IBroadcastActionHandler() {
             
             @Override
             public void onReceiveAction(Context context, Intent intent) {
                 mThePlayer = new AudioPlayerWithEvents(
                         new RemoteAudioPlayer(
                                 PlaylistService.this,
                                 messagingServiceLocator),
                         context
                 );
                 isLocalPlayer = false;
                 stopDataManager();
             }
         })
         .addAction(ConnectionService.ACTION_HOST_DISCONNECTED, new IBroadcastActionHandler() {
             
             @Override
             public void onReceiveAction(Context context, Intent intent) {
                 mThePlayer = new AudioPlayerWithEvents(mAudioPlayer, PlaylistService.this);
                 isLocalPlayer = true;
                 currentEntry = null;
                 startDataManager();
             }
         })
         .addAction(ConnectionService.ACTION_GUEST_CONNECTED, new IBroadcastActionHandler() {
             @Override
             public void onReceiveAction(Context context, Intent intent) {
                 getMessagingService().sendPlaylistMessage(mPlaylist.getSongsToPlay());
             }
         })
         .addAction(MessagingService.ACTION_PAUSE_MESSAGE, new IBroadcastActionHandler() {
 
             @Override
             public void onReceiveAction(Context context, Intent intent) {
                 pause();
             }
         })
         .addAction(MessagingService.ACTION_PLAY_MESSAGE, new IBroadcastActionHandler() {
             
             @Override
             public void onReceiveAction(Context context, Intent intent) {
                 play();
             }
         })
         .addAction(MessagingService.ACTION_SKIP_MESSAGE, new IBroadcastActionHandler() {
             
             @Override
             public void onReceiveAction(Context context, Intent intent) {
                 skip();
             }
         })
         .addAction(MessagingService.ACTION_ADD_TO_PLAYLIST_MESSAGE, new IBroadcastActionHandler() {
 
             @Override
             public void onReceiveAction(Context context, Intent intent) {
                 if (!isLocalPlayer) {
                     Log.wtf(TAG, "Received AddToPlaylistMessage on guest...these messages are only for hosts");
                 }
                 String macAddress = intent.getStringExtra(MessagingService.EXTRA_ADDRESS);
                 long   songId     = intent.getLongExtra(  MessagingService.EXTRA_SONG_ID,
                                                           SongMetadata.UNKNOWN_SONG);
                 
                 SongMetadata song = getMusicLibraryService().lookupSongByAddressAndId(macAddress, songId);
                 if (song != null) {
                     addSong(song);
                 } else {
                     Log.wtf(TAG, "Song with mac address " + macAddress + " and id " + songId + " not found.");
                 }
             }
         })
         .addAction(MessagingService.ACTION_BUMP_SONG_ON_PLAYLIST_MESSAGE, new IBroadcastActionHandler() {
 
             @Override
             public void onReceiveAction(Context context, Intent intent) {
                 if (!isLocalPlayer) {
                     Log.wtf(TAG, "Received BumpSongOnPlaylist on guest...these messages are only for hosts");
                 }
                 String macAddress = intent.getStringExtra(MessagingService.EXTRA_ADDRESS);
                 long   songId     = intent.getLongExtra(  MessagingService.EXTRA_SONG_ID,
                                                           SongMetadata.UNKNOWN_SONG);
                 int entryId         = intent.getIntExtra( MessagingService.EXTRA_ENTRY_ID, 0);
                 
                 SongMetadata song = getMusicLibraryService().lookupSongByAddressAndId(macAddress, songId);
                 
                 PlaylistEntry entry = mPlaylist.findEntryBySongAndId(song, entryId);
                 if (entry != null) {
                     bumpSong(entry);
                 } else {
                     Log.e(TAG, "Attempting to bump a song that is not in our playlist: " + song);
                 }
             }
         })
         .addAction(MessagingService.ACTION_REMOVE_FROM_PLAYLIST_MESSAGE, new IBroadcastActionHandler() {
 
             @Override
             public void onReceiveAction(Context context, Intent intent) {
                 if (!isLocalPlayer) {
                     Log.wtf(TAG, "Received AddToPlaylistMessage on guest...these messages are only for hosts");
                 }
                 String macAddress = intent.getStringExtra(MessagingService.EXTRA_ADDRESS);
                 long   songId     = intent.getLongExtra(  MessagingService.EXTRA_SONG_ID,
                                                           SongMetadata.UNKNOWN_SONG);
                 int entryId         = intent.getIntExtra( MessagingService.EXTRA_ENTRY_ID, 0);
                 
                 //NOTE: only remove if its not the currently playing song.
                 //TODO: may need a better message back to the remote fan
                 SongMetadata song = getMusicLibraryService().lookupSongByAddressAndId(macAddress, songId);
                 PlaylistEntry entry = mPlaylist.findEntryBySongAndId(song, entryId);
                 if (!isCurrentEntry(entry)) {
                     removeSong(entry);
                 }
                 getMessagingService().sendPlaylistMessage(mPlaylist.getSongsToPlay());
             }
         })
         .addAction(MessagingService.ACTION_PLAYLIST_UPDATED_MESSAGE, new IBroadcastActionHandler() {
 
             @Override
             public void onReceiveAction(Context context, Intent intent) {
                 if (isLocalPlayer) {
                     Log.wtf(TAG, "Received PlaylistUpdateMessage as host...these messages are only for guests");
                 }
                 List<PlaylistEntry> newList =
                         intent.getParcelableArrayListExtra(MessagingService.EXTRA_PLAYLIST_ENTRY);
                 mPlaylist.clear();
                 for (PlaylistEntry entry : newList) {
                     mPlaylist.add(entry);
                 }
                 new BroadcastIntent(ACTION_PLAYLIST_UPDATED).send(PlaylistService.this);
             }
         })
         .addAction(MessagingService.ACTION_SONG_STATUS_MESSAGE, new IBroadcastActionHandler() {
             
             @Override
             public void onReceiveAction(Context context, Intent intent) {
                 if (isLocalPlayer) {
                     Log.wtf(TAG, "Received SongStatusMessage as host...these messages are only for guests");
                 }
                 String macAddress = intent.getStringExtra(MessagingService.EXTRA_ADDRESS);
                 long   songId     = intent.getLongExtra(  MessagingService.EXTRA_SONG_ID, SongMetadata.UNKNOWN_SONG);
                 int    entryId    = intent.getIntExtra(   MessagingService.EXTRA_ENTRY_ID, 0);
                 boolean loaded    = intent.getBooleanExtra(MessagingService.EXTRA_LOADED, false);
                 boolean played    = intent.getBooleanExtra(MessagingService.EXTRA_PLAYED, false);
 
                 SongMetadata song = getMusicLibraryService().lookupSongByAddressAndId(macAddress, songId);
                if (song == null) {
                     PlaylistEntry entry = mPlaylist.findEntryBySongAndId(song, entryId);
                     if (entry != null) {
                         entry.setLoaded(loaded);
                         entry.setPlayed(played);
                         // send an intent to the fragments that the playlist is updated
                         new BroadcastIntent(ACTION_PLAYLIST_UPDATED).send(PlaylistService.this);
                     } else {
                         Log.e(TAG, "Attempting to update information about a song that is not in our playlist: " + song);
                     }
                 } else {
                     Log.e(TAG, "MusicLibraryService cannot find song");
                 }
             }
         })
         .addAction(PlaylistService.ACTION_PAUSE, new IBroadcastActionHandler() {
             
             @Override
             public void onReceiveAction(Context context, Intent intent) {
                 pause();
             }
         })
         .addAction(PlaylistService.ACTION_PLAY_PAUSE, new IBroadcastActionHandler() {
             
             @Override
             public void onReceiveAction(Context context, Intent intent) {
                 if (mThePlayer.isPaused()) {
                     play();
                 } else {
                     pause();
                 }
             }
         })
         .addAction(PlaylistService.ACTION_SKIP, new IBroadcastActionHandler() {
             
             @Override
             public void onReceiveAction(Context context, Intent intent) {
                 skip();
             }
         })
         .addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY, new IBroadcastActionHandler() {
 
             @Override
             public void onReceiveAction(Context context, Intent intent) {
                 pause();
             }
         })
         .register(this);
     }
 
     protected void startDataManager() {
         if (mDataManager == null) {
             mDataManager       = new PlaylistDataManager(PlaylistService.this, messagingServiceLocator);
             mDataManagerThread = new Thread(mDataManager, PlaylistDataManager.class.getSimpleName() + " Thread");
             mDataManagerThread.start();
         }
     }
 
     protected void stopDataManager() {
         if (mDataManager != null) {
             mDataManager.stopLoading();
             mDataManager = null;
             mDataManagerThread = null;
         }
     }
 
     private void unregisterReceivers() {
         this.registrar.unregister();
     }
 
     private boolean isCurrentEntry(PlaylistEntry entry) {
         //currentSong == null before play is started, and for a brief moment between songs
         // (It's nulled out when the ACTION_SONG_FINISHED method is called,
         // and repopulated in setSong)
         return currentEntry != null && SongMetadataUtils.isTheSameEntry(entry, currentEntry);
     }
 
     public boolean isPlaying() {
         return this.mThePlayer.isPlaying();
     }
 
     public void play() {
         if (this.mThePlayer.isPaused()) {
             this.mThePlayer.resume();
         } else {
             boolean play = true;
             if(isLocalPlayer) {
                 play = setNextSong();
             }
             //we have stuff to play...play it and send a notification
             if (play) {
                 this.mThePlayer.play();
             }
         }
     }
 
     /**
      * Helper method to manage all of the things we need to do to set a song
      * to play locally (e.g. on the host).
      */
     private boolean setNextSong() {
         if (!isLocalPlayer) {
             throw new IllegalStateException("Cannot call setSong when using a remote player");
         }
         boolean songSet = false;
         //only 
         if (mPlaylist.size() > 0) {
             PlaylistEntry song = mPlaylist.getNextAvailableSong();
             //we've reached the end of the playlist...reset it to the beginning and try again
             if (song == null) {
                 resetPlaylist();
                 song = mPlaylist.getNextAvailableSong();
                 Toaster.iToast(this, getString(R.string.playlist_finished));
             }
             //still no available music..this means we're waiting for data to come in
             //...display a warning, but don't play.
             if (song == null) {
                 //TODO: instead of this, we may want to repost a message to wait for the next song to be available
                 //stop the player
                 this.mAudioPlayer.stop();
                 //pop up the notice
                 Toaster.iToast(this, getString(R.string.no_available_songs));
             } else {
                 //we have a song available to play...play it!
                 this.currentEntry = song;
                 this.mAudioPlayer.setSong(song);
                 //the song has been set...indicate this in the return value
                 songSet = true;
             }
         } else {
             Toaster.iToast(this, getString(R.string.playlist_empty));
         }
         return songSet;
     }
 
     /**
      * 
      */
     private void resetPlaylist() {
         mPlaylist.reset();
         if (isLocalPlayer) {
             //we may need to re-add entries to the data manager, for remote
             // loading
             for (PlaylistEntry entry : mPlaylist.getSongsToPlay()) {
                 //add all of the entries to the load queue
                 Log.i(TAG, entry + " is loaded? " + entry.isLoaded());
                 mDataManager.addToLoadQueue(entry);
             }
         }
         //send a message to the guests with the new playlist
         getMessagingService().sendPlaylistMessage(mPlaylist.getSongsToPlay());
     }
 
     public void clearPlaylist() {
         mPlaylist.clear();
         new BroadcastIntent(ACTION_PLAYLIST_UPDATED).send(this);
     }
 
     public void pause() {
         this.mThePlayer.pause();
     }
 
     public void skip() {
         this.mThePlayer.skip();
     }
 
     public void addSong(SongMetadata metadata) {
         addSong(new PlaylistEntry(metadata));
     }
 
     public void addSong(PlaylistEntry entry) {
         //NOTE: the entries are shared between the playlist and the data loader...the loader
         // will load data into the same objects that are held in the playlist
         
         mPlaylist.add(entry);
         if (isLocalPlayer) {
             mDataManager.addToLoadQueue(entry);
         }
         new BroadcastIntent(ACTION_SONG_ADDED)
             .putExtra(EXTRA_SONG, entry)
             .send(this);
         // send an intent to the fragments that the playlist is updated
         new BroadcastIntent(ACTION_PLAYLIST_UPDATED).send(this);
 
         if (isLocalPlayer) {
             //send a message to the guests with the new playlist
             getMessagingService().sendPlaylistMessage(mPlaylist.getSongsToPlay());
         } else {
             //send a message to the host to add this song
             getMessagingService().sendAddToPlaylistMessage(entry);
         }
     }
     
     public void removeSong(PlaylistEntry entry) {
        
         if (entry != null) {
             mPlaylist.remove(entry);
             //broadcast the fact that a song has been removed
             new BroadcastIntent(ACTION_SONG_REMOVED)
                 .putExtra(EXTRA_SONG, entry)
                 .send(this);
             
             //broadcast the fact that the playlist has been updated
             new BroadcastIntent(ACTION_PLAYLIST_UPDATED).send(this);
     
             if (isLocalPlayer) {
                 //send a message to the guests with the new playlist
                 getMessagingService().sendPlaylistMessage(mPlaylist.getSongsToPlay());
             } else {
                 //send a message to the host to remove this song
                 getMessagingService().sendRemoveFromPlaylistMessage(entry);
             }
         } else {
             Log.e(TAG, "Attempting to remove a song that is not in our playlist: " + entry);
         }
     }
 
     public List<PlaylistEntry> getPlaylistEntries() {
         return Collections.unmodifiableList(new ArrayList<PlaylistEntry>(mPlaylist.getSongsToPlay()));
     }
 
     private IMessagingService getMessagingService() {
         MessagingService messagingService = null;
         try {
             messagingService = this.messagingServiceLocator.getService();
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }
         return messagingService;
     }
 
     public void bumpSong(PlaylistEntry entry){
         mPlaylist.bumpSong(entry);
         
         new BroadcastIntent(ACTION_PLAYLIST_UPDATED).send(this);
         
         if (isLocalPlayer) {
             //send a message to the guests with the new playlist
             getMessagingService().sendPlaylistMessage(mPlaylist.getSongsToPlay());
         } else {
             //send a message to the host to remove this song
             getMessagingService().sendBumpSongOnPlaylistMessage(entry);
         }
     }
     
     public PlaylistEntry getCurrentEntry(){
         return currentEntry;
     }
     
     public MusicLibraryService getMusicLibraryService() {
         MusicLibraryService musicLibraryService = null;
         try {
             musicLibraryService = this.musicLibraryLocator.getService();
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }
         return musicLibraryService;
     }
 
 }
