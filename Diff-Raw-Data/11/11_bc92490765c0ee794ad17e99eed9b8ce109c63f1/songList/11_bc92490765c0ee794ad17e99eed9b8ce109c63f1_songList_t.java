 package com.example.djhero;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.util.Log;
 
 @SuppressLint("UseValueOf")
 public class songList{
 	public List<Song> Songs = new ArrayList<Song>();
 	
 	public songList(String songliststring){
 		String[] parsed = songliststring.split("\\|");
 		for (int i=0; i<parsed.length; i++){
 			Songs.add(new Song(parsed[i]));
 		}
 	}
 	
	public void getListNames(){
 		for (int i=0; i<Songs.size(); i++){
 			Log.i ("song", Songs.get(i).Title);
 		}
 	}
 }
