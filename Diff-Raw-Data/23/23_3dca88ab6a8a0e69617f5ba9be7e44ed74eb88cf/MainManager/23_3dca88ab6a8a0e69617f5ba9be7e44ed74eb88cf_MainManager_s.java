 package com.cs301w01.meatload.controllers;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 import android.content.Context;
 
 import com.cs301w01.meatload.model.Album;
 import com.cs301w01.meatload.model.DBManager;
 import com.cs301w01.meatload.model.Photo;
 import com.cs301w01.meatload.model.Tag;
 
 
 /**
  * Created by IntelliJ IDEA.
  * User: Derek
  * Date: 3/3/12
  * Time: 1:14 PM
  * To change this template use File | Settings | File Templates.
  */
 
 
 public class MainManager implements FController{
 
	DBManager dbMan;
 	
    public MainManager(Context context){
    	dbMan = new DBManager(context);
     }
     
     public ArrayList<HashMap<String,String>> getAllAlbums(){
     	ArrayList<HashMap<String,String>> albums = new ArrayList<HashMap<String,String>>();
     	HashMap<String,String> map = new HashMap<String,String>();
     	map.put("name", "All Photos");
     	map.put("numPhotos", Integer.toString(dbMan.getTotalPhotos()));
     	albums.add(map);
     	albums.addAll(dbMan.selectAllAlbums());
     	return albums;
     }
     
     public Collection<Tag> getAllTags(){
    	return dbMan.selectAllTags();
     }
     
     public void addAlbum(String albumName, Collection<String> tags){
    	dbMan.insertAlbum(albumName, tags);
     }
 }
