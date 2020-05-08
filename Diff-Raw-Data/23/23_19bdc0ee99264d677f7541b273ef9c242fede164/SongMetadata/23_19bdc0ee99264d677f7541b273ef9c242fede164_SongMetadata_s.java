 package com.lastcrusade.fanclub.model;
 
 public class SongMetadata {
	
     private long id;
 
     private String title;
 
     private String artist;
 
     private String album;
    
    private String username;	// Bluetooth id of person who added song to playlist
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getArtist() {
         return artist;
     }
 
     public void setArtist(String artist) {
         this.artist = artist;
     }
 
     public String getAlbum() {
         return album;
     }
 
     public void setAlbum(String album) {
         this.album = album;
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
 }
