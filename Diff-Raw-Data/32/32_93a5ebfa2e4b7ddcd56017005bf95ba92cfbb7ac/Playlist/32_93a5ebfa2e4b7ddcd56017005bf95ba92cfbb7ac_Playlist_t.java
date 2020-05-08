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
 
 package com.lastcrusade.soundstream.model;
 
 import java.util.ArrayList;
 import java.util.Deque;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.util.Log;
 
 /**
  * A data structure for holding the playlist.  It keeps track of two queues of PlaylistEntry
  * objects; the seam between them represents the current play position.
  * 
  * @author Jesse Rosalia
  *
  */
 public class Playlist {
     
     private final static String TAG = Playlist.class.getName();
     
     private Deque<PlaylistEntry> playedList;
     private Deque<PlaylistEntry> musicList;
 
     public Playlist() {
         playedList = new LinkedList<PlaylistEntry>();
         musicList  = new LinkedList<PlaylistEntry>();
     }
 
     public void add(PlaylistEntry entry) {
         musicList.add(entry);
     }
 
     public void clear() {
         playedList.clear();
         musicList.clear();
     }
     
     public PlaylistEntry findEntryBySongAndId(SongMetadata song, int entryId){
         PlaylistEntry found = null;
         for(PlaylistEntry entry: getSongsToPlay()){
             if(SongMetadataUtils.isTheSameSong(entry, song) && entry.getEntryId() == entryId){
                 found = entry;
                 break;
             }
         }
         return found;
     }
 
     public PlaylistEntry remove(PlaylistEntry entry) {
         boolean success;
         PlaylistEntry removeEntry = entry;
 
         if(musicList.contains(entry)) {
             success = musicList.remove(entry);
         } else if(playedList.contains(entry)){
             success = playedList.remove(entry);
         } else {
             success = false; 
             Log.wtf(TAG, "Asked to remove unknown object");
         }
 
         if(!success){
             removeEntry = null;
         }
         return removeEntry;
     }
 
     public List<PlaylistEntry> getSongsToPlay() {
         List<PlaylistEntry> songsToPlay = new ArrayList<PlaylistEntry>();
         songsToPlay.addAll(playedList);
         songsToPlay.addAll(musicList);
         return songsToPlay;
     }
 
     public int size(){
         return playedList.size() + musicList.size();
     }
 
     public PlaylistEntry getNextAvailableSong() {
         PlaylistEntry nextAvail = null;
         for (PlaylistEntry entry : musicList) {
             if (entry.isLoaded()) {
                 nextAvail = entry;
                 break;
             }
         }
         
         if (nextAvail != null) {
             musicList.remove(nextAvail);
             playedList.add(nextAvail);
         }
         return nextAvail;
     }
 
     public void reset() {
         playedList.addAll(musicList);
         musicList = playedList;
         playedList = new LinkedList<PlaylistEntry>();
         //reset the play status on all of the entries
         for (PlaylistEntry entry : musicList) {
             entry.setPlayed(false);
         }
     }
     
     public void bumpSong(PlaylistEntry entry){
         if(musicList.contains(entry)){
             //remove the entry from the queue
             musicList.remove(entry);
             musicList.push(entry);
         }
     }
 }
