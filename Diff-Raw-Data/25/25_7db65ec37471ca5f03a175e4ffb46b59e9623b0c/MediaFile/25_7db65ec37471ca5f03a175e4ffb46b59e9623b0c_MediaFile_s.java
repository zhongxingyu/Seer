 package com.music.model;
 
 import android.provider.MediaStore;
 
 public class MediaFile {
 	
	public final static String Cols_ID = "ID";
 	public final static String	Cols_PATH =	"PATH";
 	public final static String Cols_DURATION = "DURATION";
 	public final static String Cols_TYPE = "TYPE";
 	public final static String Cols_PICTURE = "PICTURE";
 	public final static String Cols_TITLE = "TITLE";
 	public final static String Cols_SIZE =	"SIZE";
 	public final static String Cols_ARTIST = "ARTIST";
     public final static String Cols_GENER = "GENER";
     public final static String Cols_ALBUM = "ALBUM";
 							
 	
 	private String id;
 	private String path;
 	private String duration;
 	private String type;
 	private String picture;
 	private String title;
 	private String size;
 	private String artist;
 	private String album;
 	
 	
 	public String getId() {
 		return id;
 	}
 	public void setId(String id) {
 		this.id = id;
 	}
 	
 	
 	public String getPath() {
 		return path;
 	}
 	public void setPath(String path) {
 		this.path = path;
 	}
 	
 	
 	public String getDuration() {
 		return duration;
 	}
 	public void setDuration(String duration) {
 		this.duration = duration;
 	}
 	
 	
 	public String getType() {
 		return type;
 	}
 	public void setType(String type) {
 		this.type = type;
 	}
 	
 	
 	public String getPicture() {
 		return picture;
 	}
 	public void setPicture(String picture) {
 		this.picture = picture;
 	}
 	
 	
 	public String getTitle() {
 		return title;
 	}
 	public void setTitle(String title) {
 		this.title = title;
 	}
 	
 	
 	public String getSize() {
 		return size;
 	}
 	public void setSize(String size) {
 		this.size = size;
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
 	
 						
 }
