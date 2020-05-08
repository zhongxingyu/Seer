 package com.lastcrusade.soundstream.manager;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Set;
 
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 import com.lastcrusade.soundstream.CustomApp;
 import com.lastcrusade.soundstream.library.MediaStoreWrapper;
 import com.lastcrusade.soundstream.library.SongNotFoundException;
 import com.lastcrusade.soundstream.model.PlaylistEntry;
 import com.lastcrusade.soundstream.model.SongMetadata;
 import com.lastcrusade.soundstream.service.IMessagingService;
 import com.lastcrusade.soundstream.service.MessagingService;
 import com.lastcrusade.soundstream.service.PlaylistService;
 import com.lastcrusade.soundstream.service.ServiceLocator;
 import com.lastcrusade.soundstream.service.ServiceNotBoundException;
 import com.lastcrusade.soundstream.util.BroadcastIntent;
 import com.lastcrusade.soundstream.util.BroadcastRegistrar;
 import com.lastcrusade.soundstream.util.IBroadcastActionHandler;
 import com.lastcrusade.soundstream.util.SongMetadataUtils;
 
 public class PlaylistDataManager implements Runnable {
 
     private static final String TAG = PlaylistDataManager.class.getName();
 
     private Context context;
     private ServiceLocator<MessagingService> messagingServiceLocator;
     private Queue<PlaylistEntry> loadQueue = new LinkedList<PlaylistEntry>();
     private Set<PlaylistEntry> remotelyLoaded = new HashSet<PlaylistEntry>();
     private Thread stoppingThread;
     private boolean running;
     private BroadcastRegistrar registrar;
 
     private long maxBytesToLoad = 512 * 1024 * 1024; //512MB default max bytes
     private long bytesRequested = 0;
     private float loadFactor    = 0.5f;
     
     private final Object entryMutex = new Object();
 
     public PlaylistDataManager(Context context, ServiceLocator<MessagingService> messagingServiceLocator) {
         this.context                 = context;
         this.messagingServiceLocator = messagingServiceLocator;
     }
 
     @Override
     public void run() {
         registerReceivers();
         running = true;
         try {
             while (running) {
                 //first, clean up any already played remote files...this frees up space
                 // to request new files.
                 clearOldLoadedFiles();
 
                 boolean loaded = false;
                 //next, see if we can start loading any additional files
                 while (!loadQueue.isEmpty() &&
                         loadQueue.peek().getFileSize() < (maxBytesToLoad - bytesRequested)) {
                     PlaylistEntry entry = loadQueue.poll();
                     if (entry.isLocalFile()) {
                         //if its local, just load the file path and remove the entry
                         loadLocal(entry);
                         loaded = true;
                     } else {
                         //for remote entries, we need to request the remote file, and also
                         // keep track of the bytes requested, and the remote entries, so we
                         // can avoid overloading the host, and we can clean up after ourselves
                         loadRemote(entry);
                         bytesRequested += entry.getFileSize();
                         remotelyLoaded.add(entry);
                     }
                 }
                 if (loaded) {
                     new BroadcastIntent(PlaylistService.ACTION_PLAYLIST_UPDATED).send(context);
                 }
                 pauseForNextRun();
             }
         } finally {
             //clean up on the way out
             unregisterReceivers();
             if (this.stoppingThread != null) {
                 synchronized(this.stoppingThread) {
                     this.stoppingThread.notify();
                 }
             }
         }
     }
 
     /**
      * Pause the thread before running through the data clear/load process
      */
     private void pauseForNextRun() {
         try {
             int pauseInMS = 1000; //1 second pause
             Thread.sleep(pauseInMS);
         } catch (InterruptedException e) {
         }
     }
 
     /**
      * 
      */
     private void clearOldLoadedFiles() {
         //NOTE: only do this if we need to...to minimize network traffic/playback issues
         if (bytesRequested > maxBytesToLoad * loadFactor) {
             Set<PlaylistEntry> toRemove = new HashSet<PlaylistEntry>();
             long toRemoveBytes = 0;
             for (PlaylistEntry entry : remotelyLoaded) {
                 synchronized(entryMutex) {
                     if (entry.isPlayed()) {
                         //indicate the entry isnt loaded, so the playlist wont try and play it
                         entry.setLoaded(false);
                         toRemove.add(entry);
                         toRemoveBytes += entry.getFileSize();
                     }
                 }
             }
             
             deleteTempFileData(toRemove);
             remotelyLoaded.removeAll(toRemove);
             bytesRequested -= toRemoveBytes;
         }
     }
 
     private void registerReceivers() {
         this.registrar = new BroadcastRegistrar();
         this.registrar
             .addAction(MessagingService.ACTION_TRANSFER_SONG_MESSAGE, new IBroadcastActionHandler() {
                 
                 @Override
                 public void onReceiveAction(Context context, Intent intent) {
                     String fromAddr     = intent.getStringExtra(MessagingService.EXTRA_ADDRESS);
                     long   songId       = intent.getLongExtra(  MessagingService.EXTRA_SONG_ID, SongMetadata.UNKNOWN_SONG);
                     String fileName     = intent.getStringExtra(MessagingService.EXTRA_SONG_FILE_NAME);
                     String tempFilePath = intent.getStringExtra(MessagingService.EXTRA_SONG_TEMP_FILE);
                     if (songId == SongMetadata.UNKNOWN_SONG) {
                        Log.wtf(TAG, "TRANSFER_SONG_MESSAGE action received without a valid song id"); 
                     } else {
                         saveTempFileData(fromAddr, songId, fileName, tempFilePath);
                         new BroadcastIntent(PlaylistService.ACTION_PLAYLIST_UPDATED).send(context);
                     }
                 }
             })
             .register(this.context);
     }
 
     private void unregisterReceivers() {
         this.registrar.unregister();
     }
 
     private void deleteTempFileData(Collection<PlaylistEntry> entries) {
         for (PlaylistEntry entry : entries) {
             Log.i(TAG, "Deleting data for entry " + entry);
             File file = new File(entry.getFilePath());
             this.context.deleteFile(file.getName());
             entry.setFilePath(null);
         }
     }
 
     protected void saveTempFileData(String fromAddr, long songId, String fileName, String fileDataPath) {
         PlaylistEntry entry = findSongByAddressAndId(fromAddr, songId);
         if (entry == null) {
             throw new IllegalStateException("Unable to save data for a song entry that doesnt exist");
         }
         //build a composite name from the macAddress
         String compositeFileName = String.format("%s_%s", SongMetadataUtils.getUniqueKey(fromAddr, songId), fileName);
         try {
             //copy the data from the temp file to the permanent file.
             FileInputStream  fis = new FileInputStream(fileDataPath);
             FileOutputStream fos = this.context.openFileOutput(compositeFileName, Context.MODE_PRIVATE);
 
             //8k buffer works well.
             int bufSize = 8192;
             byte[] buffer = new byte[bufSize];
             int read;
             while ((read = fis.read(buffer)) > 0) {
                 fos.write(buffer, 0, read);
             }
             fos.close();
             //set the file path in the playlist entry, which allows the file to be played
             String filePath = this.context.getFileStreamPath(compositeFileName).getCanonicalPath();
             entry.setFilePath(filePath);
             
             //NOTE: THIS IS A HACK.  This ultimately belongs down in MessagingService, where the temp file is
             // created, but this was put here for Alpha testing so we don't fill up our tester's phones.
             new File(fileDataPath).delete();
 
         } catch (IOException e) {
             this.context.deleteFile(compositeFileName);
             //TODO: set flag to indicate file is broken
         }
     }
 
     private PlaylistEntry findSongByAddressAndId(String fromAddr, long songId) {
         PlaylistEntry found = null;
         for (PlaylistEntry entry : this.remotelyLoaded) {
             if (entry.getMacAddress().equals(fromAddr) && entry.getId() == songId) {
                 found = entry;
             }
         }
         return found;
     }
 
     public void addToLoadQueue(PlaylistEntry entry) {
         //if the entry isnt already loaded, add it to the load queue
         //NOTE: if it is loaded, we can assume its already in remotelyLoaded
         synchronized(entryMutex) {
             if (!entry.isLoaded()) {
 //                if (this.remotelyLoaded.contains(entry)) {
 //                    PlaylistEntry existing = findSongByAddressAndId(entry.getMacAddress(), entry.getId());
 //                    
 //                }
                 if (!this.loadQueue.contains(entry) && !this.remotelyLoaded.contains(entry)) {
                     this.loadQueue.add(entry);
                 }
             } else {
                 if (!remotelyLoaded.contains(entry)) {
                     Log.wtf(TAG, "Entry " + entry + " loaded but not managed.  WTF");
                 }
             }
         }
     }
     
     private void loadLocal(PlaylistEntry entry) {
         MediaStoreWrapper msw = new  MediaStoreWrapper(this.context);
         try {
             String filePath = msw.getSongFilePath(entry);
             entry.setFilePath(filePath);
         } catch (SongNotFoundException e) {
             e.printStackTrace();
         }
     }
     
     private void loadRemote(PlaylistEntry entry) {
         getMessagingService().sendRequestSongMessage(entry.getMacAddress(), entry.getId());
     }
 
     public void stopLoading() {
         this.stoppingThread = Thread.currentThread();
         this.running = false;
         synchronized(this.stoppingThread) {
             try {
                 //wait for the thread to stop, or for 1.5 seconds.  this number was selected to correspond
                 // with the delay in the run function, plus some time to let the thread die.
                 int waitTimeInMS = 1500;
                 this.stoppingThread.wait(waitTimeInMS);
             } catch (InterruptedException e) {
                 //fall thru, nothing to do
             }
         }
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
 }
