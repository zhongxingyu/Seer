 package com.funkymonkeysoftware.adm.checker;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.Observable;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.AsyncTask;
 
 import com.funkymonkeysoftware.adm.DownloadsDBOpenHelper;
 import com.funkymonkeysoftware.adm.download.HTTPChecker;
 import com.funkymonkeysoftware.adm.download.LinkChecker;
 
 public class CheckerModel extends Observable{
 	
 	/**
 	 * Instance of the SQL database that should be used for the checker
 	 */
 	private DownloadsDBOpenHelper dbhelper;
 	
 	/**
 	 * Keep a list of selected URLs i.e. that the user has tapped
 	 */
 	private LinkedList<CheckerLink> checkerLinks;
 	
 	
 	public CheckerModel(Context c){
 		//set up connection to the SQLite database
 		dbhelper = new DownloadsDBOpenHelper(c);
 		
 		//set up the checkerLinks map
 		checkerLinks = new LinkedList<CheckerLink>();
 	}
 	
 	/**
 	 * Add a new URL to the database
 	 * 
 	 * This method will add a given URL to the downloads database provided
 	 * that the url is not a duplicate.
 	 * 
 	 * @param url
 	 */
 	public void addURL(String url) {
 		
 		SQLiteDatabase db = dbhelper.getWritableDatabase();
 		
 		Cursor c = db.rawQuery("SELECT * FROM downloads WHERE url=?", 
 				new String[]{url});
 		
 		if(c.moveToFirst()) return;
 
 		//otherwise carry out the insert
 		ContentValues values = new ContentValues();
 		values.put("url", url);
 		values.put("status", "unchecked");
 		db.insert("downloads", null, values);
 	}
 	
 	/**
 	 * Select links from the database and keep in the model
 	 * 
 	 * @throws MalformedURLException 
 	 */
 	public void loadLinks() throws MalformedURLException{
 		SQLiteDatabase db = dbhelper.getReadableDatabase();
 		
 		//clear the list of links first
 		checkerLinks = new LinkedList<CheckerLink>();
 		
 		Cursor c = db.rawQuery("SELECT * FROM downloads WHERE " +
 				"status='unchecked' OR status='online' OR " +
 				"status='offline'", null);
 		
 		while(c.moveToNext()){
 			CheckerLink l = new CheckerLink(c.getString(1), c.getString(2), true);
 			checkerLinks.add(l);
 		}
 	}
 	
 	public int getLinkCount(){
 		return checkerLinks.size();
 	}
 	
 	/**
 	 * Return a collection of CheckerLink objects represented by this model
 	 * 
 	 * @return
 	 */
 	public Collection<CheckerLink> getLinks(){
 		return new LinkedList<CheckerLink>(checkerLinks);
 	}
 	
 	/**
 	 * Find urls with offline status and select them
 	 */
 	public void selectOffline(){
 		for(CheckerLink l : checkerLinks) {
 			if(l.getStatus().equals("offline"))
 				l.setSelected(true);
 			else
 				l.setSelected(false);
 		}
 	}
 	
 	/**-
 	 * Remove urls that have been selected for deletion
 	 */
 	public void removeSelected(){
 		
 		LinkedList<String> removeURLS = new LinkedList<String>();
 		
 		int i=0;
 		
 		for( CheckerLink link : getLinks()) {
 			
 			if(link.isSelected()){
 				removeURLS.add(link.getURL().toString());
				checkerLinks.remove(i);
 			}
 			
 			i++;
 		}
 		
 		if(removeURLS.size() > 0){
 
 			String where = "(";
 			
 			for(i=0; i < removeURLS.size() - 1; i++){
 				where += "?,";
 			}
 			//add the last questionmark with no comma
 			where += "? )";
 
 			SQLiteDatabase db = dbhelper.getWritableDatabase();
 			
 			db.delete("downloads", "url IN " + where, 
 					removeURLS.toArray(new String[removeURLS.size()]));
 			
 			//close the database
 			db.close();
 		}
 		
 	}
 	
 	public void checkLinks() {
 		LinkCheckerTask lc = new LinkCheckerTask();
 		
 		//run the link checker on the links
 		lc.execute();
 	}
 	
 	/**
 	 * Either select or deselect a known URL
 	 * 
 	 * @param url The URL that should exist in the hashmap
 	 * @param selected whether this element is selected or not
 	 */
 	public void selectURL(String url, boolean selected){
 		
 		//look up the link in the list
 		for(CheckerLink l : checkerLinks){
 			
 			if(l.getURL().equals(url)){
 				//update the selected element#
 				l.setSelected(selected);
 				return;
 			}
 		}
 	}
 
 	/**
 	 * This class  runs URLCheckers over all the URLs
 	 * 
 	 * @author James Ravenscroft
 	 *
 	 */
 	private class LinkCheckerTask extends AsyncTask<Void, Integer, Void> {
 
 		protected Void doInBackground(Void... params) {
 			
 			LinkChecker chk = new HTTPChecker();
 			
 			int linkCount = checkerLinks.size();
 			int currentLink = 1;
 			
 			for(CheckerLink l : checkerLinks){
 				try{
 					l.setStatus(chk.checkURL(l.getURL()));
 				} catch (IOException e) {
 					//if there was an IO exception, assume it to be offline
 					l.setStatus("offline");
 				}
 				
 				publishProgress(currentLink*100/linkCount);
 				currentLink++;
 			}
 			
 			return null;
 		}
 		
 		/**
 		 * When an update to the progress is made, update the shiny!
 		 * 
 		 */
 		protected void onProgressUpdate(Integer... progress){
 			setChanged();
 			notifyObservers(progress[0]);
 		}
 		
 		/**
 		 * Method executed when all links have been checked.
 		 * 
 		 * @param result <p>An array of strings that map directly to 
 		 * 					each of the input URLs</p>
 		 */
 		protected void onPostExecute(Void result){
 			SQLiteDatabase db = dbhelper.getWritableDatabase();
 			
 			//iterate through all links and update them in the database
 			for(CheckerLink l : checkerLinks){
 				ContentValues values = new ContentValues();
 				values.put("status", l.getStatus());
 				db.update("downloads", values, "url=?", new String[]{l.getURL().toString()});
 			}
 			
 			//tell the model observers the check is complete
 			setChanged();
 			notifyObservers(101);
 		}
 
 
 	}
 
 	
 }
