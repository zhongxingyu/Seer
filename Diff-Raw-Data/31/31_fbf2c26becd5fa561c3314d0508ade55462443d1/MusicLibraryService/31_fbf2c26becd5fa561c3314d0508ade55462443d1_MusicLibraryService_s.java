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
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
 import android.util.Log;
 
 import com.lastcrusade.soundstream.CustomApp;
 import com.lastcrusade.soundstream.library.MediaStoreWrapper;
 import com.lastcrusade.soundstream.library.SongNotFoundException;
 import com.lastcrusade.soundstream.model.SongMetadata;
 import com.lastcrusade.soundstream.service.MessagingService.MessagingServiceBinder;
 import com.lastcrusade.soundstream.util.AlphabeticalComparator;
 import com.lastcrusade.soundstream.util.BluetoothUtils;
 import com.lastcrusade.soundstream.util.BroadcastIntent;
 import com.lastcrusade.soundstream.util.BroadcastRegistrar;
 import com.lastcrusade.soundstream.util.IBroadcastActionHandler;
 import com.lastcrusade.soundstream.util.SongMetadataUtils;
 
 public class MusicLibraryService extends Service {
     
     private static String TAG = MusicLibraryService.class.getSimpleName();
 
     /**
      * Broadcast action sent when the MusicLibrary gets or loses music
      *
      */
     public static final String ACTION_LIBRARY_UPDATED = MusicLibraryService.class
             .getName() + ".action.LibraryUpdated";
 
     /**
      * A map of keys to array positions, and an array of song metadata.
      * 
      * This lets us maintain the order in which things are added, but also allows
      * us to quickly account for duplicates/replace with updated data.
      * 
      * This is also the solution to a common interview question :-)
      * 
      */
     private Map<String, Integer> metadataMap  = new HashMap<String, Integer>();
     private List<SongMetadata>   metadataList = new ArrayList<SongMetadata>();
     
     private final Object metadataMutex = new Object();
     
     private BroadcastRegistrar registrar;
 
     private String myMacAddress;
 
     private ServiceLocator<MessagingService> messagingServiceLocator;
 
     public class MusicLibraryServiceBinder extends Binder implements
         ILocalBinder<MusicLibraryService> {
         public MusicLibraryService getService() {
             // Return this instance of LocalService so clients can call public methods
             return MusicLibraryService.this;
         }
     }
 
     @Override
     public void onCreate() {
         //load the local songs and set the mac address, so the metadata objects
         // can live in the library
         List<SongMetadata> metadataList = (new MediaStoreWrapper(this)).list();
         this.myMacAddress = BluetoothUtils.getLocalBluetoothMAC();
         for (SongMetadata song : metadataList) {
             song.setMacAddress(this.myMacAddress);
         }
         
         //update the library with the local songs
         updateLibrary(metadataList, false);
 
         messagingServiceLocator = new ServiceLocator<MessagingService>(
                 this, MessagingService.class, MessagingServiceBinder.class);
 
         registerReceivers();
     }
 
     @Override
     public void onDestroy() {
         unregisterReceivers();
         super.onDestroy();
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return new MusicLibraryServiceBinder();
     }
     
     /**
      * 
      */
     private void registerReceivers() {
         this.registrar = new BroadcastRegistrar();
         this.registrar
             .addAction(MessagingService.ACTION_LIBRARY_MESSAGE, new IBroadcastActionHandler() {
                 
                 @Override
                 public void onReceiveAction(Context context, Intent intent) {
                     List<SongMetadata> remoteMetas = intent.getParcelableArrayListExtra(MessagingService.EXTRA_SONG_METADATA);
                     updateLibrary(remoteMetas, true);
                 }
             })
             .addAction(MessagingService.ACTION_REQUEST_SONG_MESSAGE, new IBroadcastActionHandler() {
                 
                 @Override
                 public void onReceiveAction(Context context, Intent intent) {
                     String fromAddr = intent.getStringExtra(MessagingService.EXTRA_ADDRESS);
                     long   songId   = intent.getLongExtra(  MessagingService.EXTRA_SONG_ID, SongMetadata.UNKNOWN_SONG);
 
                     if (songId == SongMetadata.UNKNOWN_SONG) {
                         Log.wtf(TAG, "REQUEST_SONG_MESSAGE action received without a valid song id");    
                     } else {
                         sendSongData(fromAddr, songId);
                     }
                 }
             })
             .addAction(ConnectionService.ACTION_GUEST_DISCONNECTED, new IBroadcastActionHandler() {
 
                 @Override
                 public void onReceiveAction(Context context, Intent intent) {
                     String macAddress = intent.getStringExtra(ConnectionService.EXTRA_GUEST_ADDRESS);
                     removeLibraryForAddress(macAddress, true);
                 }
             })
             .register(this);
     }
 
     private void unregisterReceivers() {
         this.registrar.unregister();
     }
     /** Methods for clients */
 
     public List<SongMetadata> getLibrary() {
         synchronized(metadataMutex) {
             //unmodifiable copy, for safety
             return Collections.unmodifiableList(new ArrayList<SongMetadata>(metadataList));
         }
     }
 
     public List<SongMetadata> getMyLibrary() {
         //unmodifiable copy, for safety
         return Collections.unmodifiableList(getMyModifiableLibrary());
     }
 
     private List<SongMetadata> getMyModifiableLibrary() {
         synchronized(metadataMutex) {
             List<SongMetadata> myLibrary = new ArrayList<SongMetadata>();
             //look thru the library, and pull out songs with "my" mac address
             for (SongMetadata meta : metadataList) {
                 if (meta.getMacAddress().equals(this.myMacAddress)) {
                     myLibrary.add(meta);
                 }
             }
             return myLibrary;
         }
     }
 
     /**
      * Update the library with the additional songs passed in.
      * 
      * NOTE: This should not be called by an outside user.  It is package protected to allow us to unit test
      * it, but generally speaking, the library gets updated from network messages and the onCreate method.
      * 
      * @param additionalSongs
      * @param notify
      */
     void updateLibrary(Collection<SongMetadata> additionalSongs, boolean notify) {
         synchronized(metadataMutex) {
             for (SongMetadata song : additionalSongs) {
                 String key = SongMetadataUtils.getUniqueKey(song);
                 if (metadataMap.containsKey(key)) {
                     //song already exists, replace the existing entry in the list with the new data.
                     metadataList.set(metadataMap.get(key), song);
                 } else {
                     //new song to add to the list...add it, and store the position in the map
                     int nextInx = metadataList.size();
                     metadataList.add(song);
                     metadataMap.put(key, nextInx);
                 }
             }
             
             /*
              * by default we want to order alphabetically
              * when we have more options, this can be moved elsewhere
              * and governed by some type of flag.
              */
             orderAlphabetically();
         }
         if (notify) {
             notifyLibraryUpdated();
         }
     }
 
     /**
      * Notify that the library was updated.  This includes
      * sending an intent to the system, and sending the library out
      * to the guests.
      */
     private void notifyLibraryUpdated() {
         new BroadcastIntent(ACTION_LIBRARY_UPDATED).send(this);
         //send the updated library to all the guests out there
         if (getMessagingService() != null) {
             getMessagingService().sendLibraryMessageToGuests(getLibrary());
         }
     }
 
     /**
      * Remove all songs that belong to the specified mac address.
      * 
      * NOTE: This should not be called by an outside user.  It is package protected to allow us to unit test
      * it, but generally speaking, the library gets updated from network messages and the onCreate method.
      * 
      * @param additionalSongs
      * @param notify
      */
     void removeLibraryForAddress(String macAddress, boolean notify) {
         synchronized(metadataMutex) {
             //remove the songs for the specified address by assembling a new list
             // and map with all songs except those for that address
             List<SongMetadata>   newList = new ArrayList<SongMetadata>();
             Map<String, Integer> newMap  = new HashMap<String, Integer>();
             for (SongMetadata song : metadataList) {
                 if (!song.getMacAddress().equals(macAddress)) {
                     int nextInx = newList.size();
                     newList.add(song);
                     String key = SongMetadataUtils.getUniqueKey(song);
                     newMap.put(key, nextInx);
                 }
             }
             //replace THE list and map with the new structures
             metadataList = newList;
             metadataMap = newMap;
         }
         if (notify) {
             notifyLibraryUpdated();
         }
     }
 
     /**
      * Orders the song metadata and related map alphabetically by Artist,
      * Album, and Title
      */
     private void orderAlphabetically(){
         synchronized(metadataMutex) {
             //sort the metadata alphabetically
             Collections.sort(metadataList, new AlphabeticalComparator());
             
             //recreate the map
             Map<String, Integer> newMap  = new HashMap<String, Integer>();
             for(int i=0; i<metadataList.size(); i++){
                 String key = SongMetadataUtils.getUniqueKey(metadataList.get(i));
                 newMap.put(key, i);
             }
             metadataMap = newMap;
         }
     }
 
     public SongMetadata lookupSongByAddressAndId(String address, long songId) {
         synchronized(metadataMutex) {
             //TODO: remove use of bluetoothutils...replace with reference to userlist or some other way
             // of getting "my" address
             String key = SongMetadataUtils.getUniqueKey(address, songId);
             Integer inx = metadataMap.get(key);
             return inx != null ? metadataList.get(inx) : null;
         }
     }
     private SongMetadata lookupMySongById(long songId) {
         synchronized(metadataMutex) {
             //TODO: remove use of bluetoothutils...replace with reference to userlist or some other way
             // of getting "my" address
             String key = SongMetadataUtils.getUniqueKey(BluetoothUtils.getLocalBluetoothMAC(), songId);
             Integer inx = metadataMap.get(key);
             return inx != null ? metadataList.get(inx) : null;
         }
     }
     private void sendSongData(String fromAddr, long songId) {
         MediaStoreWrapper msw = new  MediaStoreWrapper(MusicLibraryService.this);
         try {
             SongMetadata song = lookupMySongById(songId);
             String filePath   = msw.getSongFilePath(song);
             File   songFile   = new File(filePath);
             getMessagingService().sendTransferSongMessage(fromAddr, songId, songFile.getName(), songFile.getCanonicalPath());
         } catch (SongNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private byte[] loadFile(File file) throws IOException {
         FileInputStream fis = new FileInputStream(file);
         byte[] bytes = new byte[fis.available()];
         fis.read(bytes);
         return bytes;
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
 
     public void clearExternalMusic() {
        metadataList = getMyModifiableLibrary();
        updateLibrary(metadataList, false);
         new BroadcastIntent(ACTION_LIBRARY_UPDATED).send(this);
     }
 }
