 package com.rodcastro.karaokesonglist.models;
 
 import com.rodcastro.karaokesonglist.utils.Settings;
 import com.rodcastro.karaokesonglist.ui.LoadingBarListener;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.TreeSet;
 import com.rodcastro.karaokesonglist.visitors.SongLoaderVisitor;
 
 /**
  *
  * @author rodcastro
  */
 public class SongRepository {
 
     private SongLoaderVisitor visitor;
     private TreeSet<Song> songs;
     private List<Song> invalidSongs;
     private TreeSet<Pack> packs;
     private LoadingBarListener listener;
     private String path;
 
     public SongRepository(String path) {
         this.path = path;
         songs = new TreeSet<Song>();
         packs = new TreeSet<Pack>();
         invalidSongs = new ArrayList<Song>();
     }
 
     public void loadSongs() {
         if (songs.isEmpty()) {
             reloadSongs();
         }
     }
 
     public void reloadSongs() {
         Thread loader = new Thread(new Runnable() {
             public void run() {
                 SongRepository.this.visitor = new SongLoaderVisitor(Settings.getWorkingPath());
                 SongRepository.this.visitor.setListener(listener);
                 SongRepository.this.visitor.loadSongs();
                 SongRepository.this.songs = visitor.getSongs();
                 SongRepository.this.packs = visitor.getPacks();
                 SongRepository.this.invalidSongs = visitor.getInvalidSongs();
                 if (listener != null)
                     listener.onFinish();
             }
         });
         loader.start();
     }
 
     public int getSongCount() {
         return songs.size();
     }
 
     public Song findSong(int uniqueId) {
         Song mock = new Song(uniqueId);
         return songs.floor(mock);
     }
 
     public String[][] findSongs(String search, boolean searchName, boolean searchArtist, boolean searchPack) {
         Iterator<Song> iterator = songs.iterator();
         List<Song> results = new ArrayList<Song>();
         Song current;
         while (iterator.hasNext()) {
             current = iterator.next();
             if (searchName && current.getFormattedSongName().toUpperCase().contains(search.toUpperCase())) {
                 results.add(current);
             } else if (searchArtist && current.getFormattedArtist().toUpperCase().contains(search.toUpperCase())) {
                 results.add(current);
            } else if (searchPack && current.getPack().getFullName().toUpperCase().contains(search.toUpperCase())) {
                 results.add(current);
             }
         }
         String[][] array = new String[results.size()][];
         for (int i = 0; i < results.size(); i++) {
             Song song = results.get(i);
             String[] row = new String[4];
             if (song.getPack() != null) {
                 row[2] = song.getPack().getFullName();
             } else {
                 row[2] = song.getFileName();
             }
             row[0] = song.getFormattedSongName();
             row[1] = song.getFormattedArtist();
             row[3] = song.getUniqueId() + "";
             array[i] = row;
         }
         return array;
     }
 
     public String[][] findInvalidSongs(String search) {
         List<Song> results = new ArrayList<Song>();
         for (Song song : invalidSongs) {
             if (song.getFileName().toUpperCase().contains(search.toUpperCase())) {
                 results.add(song);
             }
         }
         String[][] array = new String[results.size()][];
         for (int i = 0; i < results.size(); i++) {
             Song song = results.get(i);
             String[] row = new String[1];
             row[0] = song.getFileName();
             array[i] = row;
         }
         return array;
     }
 
     public LoadingBarListener getListener() {
         return listener;
     }
 
     public void setListener(LoadingBarListener listener) {
         this.listener = listener;
         if (visitor != null) {
             visitor.setListener(listener);
         }
     }
 }
