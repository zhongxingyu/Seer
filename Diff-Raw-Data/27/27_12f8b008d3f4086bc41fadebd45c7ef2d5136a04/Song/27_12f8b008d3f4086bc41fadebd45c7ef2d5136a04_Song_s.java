 package com.group15.djhero;
 
 public class Song {
 	public int id;
 	public String Title;
 	public int Length;
 	public String artist;
 	
 	public Song(){
 		id = 0;
		Title = "List of Songs";
 		Length = 0;
 		artist = "artist";
 	}
 	
 	public Song(String songstring){
 		String[] parsedstring = songstring.split(":");
 		id = Integer.parseInt(parsedstring[0]);
 		Title = parsedstring[1].replaceAll(".WAV", "");
 		Length = Integer.parseInt(parsedstring[2]);
 		artist = parsedstring[3];
 	}
 }
