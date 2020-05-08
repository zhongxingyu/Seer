 package com.github.jkschoen.jsma.examples;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.List;
 
 import com.github.jkschoen.jsma.SmugMugAPI;
 import com.github.jkschoen.jsma.misc.SmugMugException;
 import com.github.jkschoen.jsma.model.Album;
 import com.github.jkschoen.jsma.model.Token;
 
 public class AlbumExample {
 	/*
 	 * APP_NAME is what ever you want it to be. SmugMug asks you give it
 	 * unique name so that when a user is having a problem they will
 	 * be able to at least identify the app causing the issue.
 	 */
 	private static String APP_NAME=UserConfig.APP_NAME;
 	/*
 	 * CONSUMER_SECRET and CONSUMER_KEY come are given to you by SmugMug
 	 * when you request an API key. See http://www.smugmug.com/hack/apikeys
 	 * for information on Applying for an API Key.
 	 */
 	private static String CONSUMER_SECRET=UserConfig.CONSUMER_SECRET;
 	private static String CONSUMER_KEY=UserConfig.CONSUMER_KEY;
 	
 	/*
 	 * To get the OAUTH_TOKEN_ID and OAUTH_TOKEN_SECRET run the AuthExample.
 	 */
 	private static String OAUTH_TOKEN_ID=UserConfig.OAUTH_TOKEN_ID;
 	private static String OAUTH_TOKEN_SECRET=UserConfig.OAUTH_TOKEN_SECRET;
 
 	public static void main(String[] args) throws IOException,
 			URISyntaxException, SmugMugException {
 		SmugMugAPI smugmug = new SmugMugAPI(
 				APP_NAME,
 				CONSUMER_SECRET,
 				CONSUMER_KEY,
 				new Token(OAUTH_TOKEN_ID,
 						OAUTH_TOKEN_SECRET));
 
 		//get the albums and print them out.
 		System.out.println("test smugmug.albums.get");
 		List<Album> albums = get(smugmug);
 		
 		//lets pick one and print it out
 		System.out.println("\n\ntest smugmug.albums.getInfo");
 		getInfo(smugmug, albums.get(0));
 		
 		//lets create an album
 		System.out.println("\n\ntest smugmug.albums.create");
 		Album album = createAlbum(smugmug);
 		
 		//TODO: show how to add comments, get comments, change settings
 		
 		//lets delete the album
 		System.out.println("\n\ntest smugmug.albums.delete");
 		deleteAlbum(smugmug, album);
 		
 	}
 	
 	public static List<Album> get(SmugMugAPI smugmug) throws SmugMugException{
		List<Album> albums = smugmug.albums().get();
 		System.out.println("SmugMug albums in account:");
 		for (Album album : albums) {
 			System.out.println("  "+album.toString());
 		}
 		return albums;
 	}
 	
 	public static void getInfo(SmugMugAPI smugmug, Album album) throws SmugMugException{
		Album found = smugmug.albums().getInfo(album.getId(), album.getKey());
 		System.out.println("Getting the details of Album:");
 		System.out.println("  Find  => "+album.toString());
 		System.out.println("  Found => "+found.toString());
 	}
 	
 	public static Album createAlbum(SmugMugAPI smugmug) throws SmugMugException{
 		Album album = new Album();
 		album.setTitle("Test");
		smugmug.albums().create(album);
 		System.out.println("  Created  => "+album.toString());
 		return album;
 	}
 	
 	public static void deleteAlbum(SmugMugAPI smugmug, Album album) throws SmugMugException {
		smugmug.albums().delete(album.getId(), album.getKey());
 		System.out.println("Album Deleted!");
 	}
 }
