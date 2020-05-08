 package com.cs301w01.meatload.controllers;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import com.cs301w01.meatload.model.Album;
 import com.cs301w01.meatload.model.querygenerators.AlbumQueryGenerator;
 
 import android.content.Context;
 
 /**
  * Stores the Controller logic used for mediating with the AlbumQueryGenerator class.
  * 
  * @author Blake Bouchard
  * @see AlbumQueryGenerator
  */
 public class AlbumManager {
 	public Context context;
 	public int MAX_ALBUM_NAME_LENGTH = 20;
 
 	public AlbumManager(Context context) {
 		this.context = context;
 	}
 	
 	/**
 	 * Creates and invokes an instance of the DBManager class to return HashMaps
 	 * corresponding to the table of albums in the database.
 	 * 
 	 * @return All albums as ArrayList of HashMaps
 	 */
 	public ArrayList<Album> getAllAlbums() {
 		return new AlbumQueryGenerator(context).selectAllAlbums();
 	}
 	
	public String addAlbum(String albumName) {
 
 		albumName = albumName.trim();
 		if (albumName.length() > MAX_ALBUM_NAME_LENGTH) {
 			albumName = albumName.substring(0, MAX_ALBUM_NAME_LENGTH);
 		} else if (albumName.length() == 0) {
 			// handle empty album name
 			return "";
 		}
 
 		if (albumExists(albumName)) {
 			// album name already exists
 			return "";
 		}
 
 		new AlbumQueryGenerator(context).insertAlbum(albumName);
 		return albumName;
 		// length() == 0 if adding album failed, else returns name of added
 		// album truncated to max Album name # chars
 	}
 
 	public Album getAlbumByName(String albumName) {
 		return new AlbumQueryGenerator(context).getAlbumByName(albumName);
 	}
 
 	public int getMaxAlbumName() {
 		return MAX_ALBUM_NAME_LENGTH;
 	}
 	
 	/**
 	 * Takes an ArrayList of albums and returns an array containing the names of
 	 * all the albums
 	 * 
 	 * @param albums
 	 *            an ArrayList of albums to be converted
 	 * @return Returns an array of strings of all the names of the albums given
 	 */
 	public String[] albumsToStrings(ArrayList<Album> albums) {
 		String albumNames[] = new String[albums.size()];
 		int i = 0;
 		for (Album currAlbum : albums) {
 			albumNames[i] = currAlbum.getName();
 			i++;
 		}
 		return albumNames;
 	}
 
 	public boolean albumExists(String name) {
 		ArrayList<Album> testList = getAllAlbums();
 		Album testAlbum;
 		Iterator<Album> listIter = testList.iterator();
 
 		name = name.trim();
 
 		if (name.length() > MAX_ALBUM_NAME_LENGTH) {
 			name = name.substring(0, MAX_ALBUM_NAME_LENGTH);
 		}
 
 		while (listIter.hasNext()) {
 			testAlbum = listIter.next();
 			if (testAlbum.getName().equals(name)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 	
 	/**
 	 * In the database changes the name of an old album to a newly provided string.
 	 * @param newAlbumName new name
 	 * @param album The old album to be modified
 	 */
	public void changeAlbumName(String newAlbumName, Album album) {
 		AlbumQueryGenerator aG = new AlbumQueryGenerator(this.context);
		aG.updateAlbumName(album.getName(), newAlbumName);
 	}
 	
 	/**
 	 * Deletes the album specified by album id.  Will delete all associated photos.
 	 * @param aid ID of album to delete.
 	 */
 	public void deleteAlbum(int aid) {
 		new AlbumQueryGenerator(context).deleteAlbumByID(aid);
 	}
 }
