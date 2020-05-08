 package com.michalkazior.simplemusicplayer;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 public class Song implements Parcelable {
 	private static int nextId = 0;
 	private int id;
 	private String path;
 	private String title; // todo
 
 	public static final Parcelable.Creator<Song> CREATOR = new Creator<Song>() {

 		@Override
 		public Song createFromParcel(Parcel source) {
 			return new Song(source);
 		}
 
 		@Override
 		public Song[] newArray(int size) {
 			return new Song[size];
 		}
 	};
 
 	public Song() {
 	}
 
 	public Song(String path, String title) {
 		this.id = nextId++;
 		this.path = path;
 		this.title = title;
 	}
 
 	private Song(Parcel in) {
 		id = in.readInt();
 		path = in.readString();
 		title = in.readString();
 	}
 
 	public Song(Song song) {
 		id = song.id;
 		path = song.path;
 		title = song.title;
 	}
 
 	public Song spawn() {
 		Song song = new Song();
 		song.id = nextId++;
 		song.path = this.path;
 		song.title = this.title;
 		return song;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public String getPath() {
 		return path;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	@Override
 	public int describeContents() {
 		return 0;
 	}
 
 	@Override
 	public void writeToParcel(Parcel dest, int flags) {
 		dest.writeInt(id);
 		dest.writeString(path);
 		dest.writeString(title);
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		return (o instanceof Song) && ((Song) o).id == this.id;
 	}
 
 	public static boolean equals(Song a, Song b) {
 		return a != null && b != null && a.getId() == b.getId();
 	}
 }
