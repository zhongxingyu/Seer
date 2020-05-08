 package com.cs301w01.meatload.controllers;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 import android.content.Context;
 
 import com.cs301w01.meatload.model.DBManager;
 
 /**
  * Mediates between DBManager and Skindex by providing lists of Albums and Tags.
  * @author Isaac Matichuk
  * @see DBManager
  * @see Skindex
  */
 public class MainManager implements FController {
 
 	Context context;
 	
     public MainManager() {
     	//dbMan = new DBManager(context);
     }
     
     public void setContext(Context context){
     	this.context = context;
     }
     
     /**
      * Creates and invokes an instance of the DBManager class to return HashMaps corresponding to
      * the table of albums in the database.  
      * @return All albums as ArrayList of HashMaps
      */
     public ArrayList<HashMap<String, String>> getAllAlbums() {
     	DBManager dbMan = new DBManager();
     	ArrayList<HashMap<String, String>> albums = new ArrayList<HashMap<String,String>>();
     	HashMap<String, String> map = new HashMap<String,String>();
    	map.put("name", "All Photos");
    	map.put("numPhotos", Integer.toString(dbMan.getTotalPhotos()));
     	albums.add(map);
     	albums.addAll(dbMan.selectAllAlbums());
     	return albums;
     }
     
     /**
      * @deprecated
      * @return ArrayList of HashMaps containing tags as Strings
      */
     public ArrayList<HashMap<String, String>> getAllTags() {
     	return null;
     }
     
     public void addAlbum(String albumName, Collection<String> tags) {
     	new DBManager().insertAlbum(albumName, tags);
     }
 }
