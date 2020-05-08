 /*  Copyright (C) 2012  Nicholas Wright
 	
 	part of 'Aid', an imageboard downloader.
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package filter;
 
 import gui.BlockListDataModel;
 import gui.Stats;
 import io.AidDAO;
 import io.AidTables;
 import io.ThumbnailLoader;
 
 import java.awt.Image;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Logger;
 
 import javax.swing.DefaultListModel;
 
 import net.GetHtml;
 import board.Post;
 
 /**
  * Class used to filter thread contents based on Post content and Filenames,
  * as well as checking File Hashes, performing cache look up's and checking
  * the threads status.
  */
 public class Filter implements FilterModifiable{
 	private static Logger logger = Logger.getLogger(Filter.class.getName());
 	private static int FILTER_UPDATER_INTERVAL = 60*1000; // one minute
	private final String LOCATION_TAG = "DL_CLIENT";
 	private int filterNr = 0;	// filter item counter
 
 	private BlockListDataModel blocklistModel;
 	private DefaultListModel<String> fileNameModel;
 	private DefaultListModel<String> postContentModel;
 	private ThumbnailLoader thumbLoader;
 
 	private AidDAO sql;
 	private Timer filterUpdateTimer = new Timer("Filter update daemon", true);
 	
 	public Filter(AidDAO sql, BlockListDataModel blockListModel,DefaultListModel<String> fileNameModel, DefaultListModel<String> postContentModel, ThumbnailLoader thumbLoader){
 		this.sql = sql;
 		this.blocklistModel = blockListModel;
 		this.fileNameModel = fileNameModel;
 		this.postContentModel = postContentModel;
 		this.thumbLoader = thumbLoader;
 	}
 	
 	public boolean loadFilter(String path){
 		return loadFilter(new File(path));
 	}
 	
 	public void startUpdater(){
 		filterUpdateTimer.schedule(new FilterUpdater(), 0, FILTER_UPDATER_INTERVAL);
 	}
 	
 	public boolean loadFilter(File file){
 		FileInputStream fis;
 		try {
 			fis = new FileInputStream(file);
 			return loadFilter(fis);
 		} catch (FileNotFoundException e) {
 			logger.severe("Unable to load "+file.toString());
 		}
 		return false;
 		
 	}
 	
 	/**
 	 * Load filter data from a File.
 	 */
 	@SuppressWarnings("unchecked")
 	public boolean loadFilter(InputStream is){
 		try{
 			if(is == null)
 				return false;
 			ObjectInputStream o = new ObjectInputStream(is);
 			DefaultListModel<String> tmpPostContentModel = (DefaultListModel<String>)o.readObject();
 			DefaultListModel<String> tmpFileNameModel = (DefaultListModel<String>)o.readObject();
 			o.close();
 			
 			for(Object obj : tmpFileNameModel.toArray()){
 				fileNameModel.addElement((String)obj);
 			}
 			
 			for(Object obj : tmpPostContentModel.toArray()){
 				postContentModel.addElement((String)obj);
 			}
 		}catch (IOException io) { 
 			logger.warning("Error when loading file: "+io.getMessage());
 			return false;		} catch (ClassNotFoundException e) {
 			logger.warning("Could not locate class: "+e.getMessage());
 			return false;
 		}
 		return true;
 	}
 
 	public boolean saveFilter(File file){
 		return saveFilter(file.toString());
 	}
 	
 	/**
 	 * Save all filter items to disk.
 	 */
 	public boolean saveFilter(String path){
 		try{
 			FileOutputStream file = new FileOutputStream(path);
 			ObjectOutputStream o = new ObjectOutputStream( file );  
 			o.writeObject(postContentModel);
 			o.writeObject(fileNameModel);
 			o.close();
 			logger.info("Saved filter to "+path);
 			return true;
 		}catch ( IOException e ) { 
 			logger.severe("Error when saving file: "+e.getMessage()); 
 			return false;}
 	}
 
 	public void addFileNameFilterItem(String item){
 		if(! fileNameModel.contains(item))
 			fileNameModel.addElement(item);
 	}
 
 	public void addPostContentFilterItem(String item){
 		if(! postContentModel.contains(item))
 			postContentModel.addElement(item);
 	}
 
 	public void removeFileNameFilterItem(String item){
 		fileNameModel.removeElement(item);
 	}
 
 	public void removePostContentFilterItem(String item){
 		postContentModel.removeElement(item);
 	}
 
 	/**
 	 * Returns the filter state of the Item.
 	 * If the item is not in the Filter, unknown is returned.
 	 * 
 	 * @param urlToTest The URL to check against the database.
 	 */
 	public FilterState getFilterState(URL urlToTest){
 
 		FilterState state = FilterState.UNKNOWN;
 
 		state = sql.getFilterState(urlToTest.toString());
 		return state;
 	}
 
 	/**
 	 * Returns the number of items in the Filter.
 	 * 
 	 * @return Number of items in the filter.
 	 */
 	public int getSize(){
 		int size = sql.size(AidTables.Filter);
 		return size;
 	}
 
 	/**
 	 * Returns the number of items with the status "pending".
 	 * 
 	 * @return Number of "Pending" filter items.
 	 */
 	public int getPending(){
 		int pending = sql.getPending();
 		return pending;
 	}
 
 	/**
 	 * Adds a new item to the filter list.
 	 * @param filteritem FilterItem to add.
 	 */
 	public void reviewThread(FilterItem filterItem){
 		sql.addFilter(filterItem.getUrl().toString(),  filterItem.getBoard(), filterItem.getReason(), filterItem.getState());
 		blocklistModel.addElement(filterItem);
 		filterNr++;
 		Stats.setFilterSize(filterNr);
 	}
 
 	/**
 	 * Set the filter item to "allow".
 	 * Files in this thread will be processed.
 	 * 
 	 * @param url URL to allow.
 	 */
 	public void setAllow(URL url){
 		sql.updateState(url.toString(), FilterState.ALLOW);
 		filterNr--;
 		Stats.setFilterSize(filterNr);
 	}
 
 	/**
 	 * Set the filter item to "deny".
 	 * Files in this thread will not be processed.
 	 */
 	public void setDeny(URL url){
 		sql.updateState(url.toString(), FilterState.DENY);
 		filterNr--;
 		Stats.setFilterSize(filterNr);
 	}
 	
 	/**
 	 * Check all pending items if they still exist (that the thread they
 	 * reference has not 404'd).<br/>
 	 * Non existing items will be removed from the database and the GUI-list.
 	 */
 	public void refreshList(){
 		Thread t = new RefreshList();
 		t.start();
 	}
 
 	/**
 	 * Will check a post to see if it contains blocked content / names.
 	 * 
 	 * @param p Post to check
 	 * @return Reason if blocked, otherwise null
 	 */
 	public String checkPost(Post p){
 		// filter out unwanted content (File Name Check)
 		if(p.hasImage()){
 			for (Object detail : fileNameModel.toArray()){
 				if (p.getImageName().toLowerCase().contains((String)detail)){
 					return "file name, "+(String)detail;
 				}
 			}
 		}
 
 		// filter out unwanted content (Post content check)
 		if(p.hasComment()){
 			for (Object detail : postContentModel.toArray()){
 				if (p.getComment().toLowerCase().contains((String)detail))
 					return "post content, "+(String)detail;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Check to see if the URL is in the cache.
 	 * 
 	 * @param url URL to check.
 	 * @return true if found, else false.
 	 */
 	public boolean isCached(URL url){
 		boolean known = sql.isCached(url);
 		return known;
 	}
 	/**
 	 * Adds the URL to the cache or updates the existing timestamp.
 	 * @param url URL to add.
 	 */
 	public void cache(URL url){
 		sql.addCache(url);
 		Stats.setCacheSize(sql.size(AidTables.Cache));
 	}
 	
 	/**
 	 * Remove all cache entries with timestamps older than 3 hours.<br/>
 	 */
 	public void pruneCache(){
 		Calendar exp = Calendar.getInstance();
 		exp.add(Calendar.HOUR, -3);
 
 		sql.pruneCache(exp.getTimeInMillis()); //keys that are older than 3 Hour
 		Stats.setCacheSize(sql.size(AidTables.Cache));
 	}
 	
 	/**
 	 * Checks if the hash has been recorded.
 	 * @param hash Hash to check
 	 * @return true if found else false.<br/>
 	 * Returns true on error.
 	 */
 	public boolean exists(String hash){
 		boolean exists = sql.isDnw(hash)||sql.isHashed(hash);
 		return exists;
 	}
 	
 	public void addIndex(String hash, String path, int size) throws SQLException{
 				sql.addIndex(hash, path, size, LOCATION_TAG);
 	}
 	
 	/**
 	 * Check if the hash is blacklisted.
 	 * @param hash Hash to check
 	 * @return true if found.
 	 * Returns false on error.
 	 */
 	public boolean isBlacklisted(String hash){
 		boolean blocked = sql.isBlacklisted(hash);
 		if(blocked){
 			//remove that hash from other tables
 			sql.delete(AidTables.Fileindex, hash);
 			sql.delete(AidTables.Dnw, hash);
 		}
 		return blocked;
 	}
 	
 	/**
 	 * Fetch thumbnail data from database.
 	 * @param url URL of the page thumbs to load.
 	 * @return Array of Binary data.
 	 */
 	public ArrayList<Image> getThumbs(String url){
 		return thumbLoader.getThumbs(url);
 	}
 	
 	public void downloadThumbs(String url, ArrayList<Post> postList){
 		thumbLoader.downloadThumbs(url, postList);
 	}
 
 	/**
 	 * Attempts to connect to the URL.
 	 * If it exists, update the FilterItem timestamp, else delete it.
 	 * 
 	 * @param mySql An active mySql connection
 	 * @param url The URL to be checked
 	 * @return true if valid, else false<br/>
 	 * Returns false on error.
 	 */
 	private boolean refreshFilterItem(URL url){
 		String currString = url.toString();
 
 		try {
 			if (new GetHtml().getResponse(currString) == 404){
 				sql.delete(AidTables.Filter, currString);
 				return false;
 			}else{
 				sql.updateFilterTimestamp(currString);
 				return true;
 			}
 		} catch (MalformedURLException e2) {
 			logger.warning("Refresh invalid URL: "+currString);
 		} catch (Exception e) {
 			logger.warning("Refresh failed,  Reason: "+e.getMessage());
 		}
 		return false;
 	}
 	
 	/**
 	 * Thread for updating the pending item list.
 	 */
 	class RefreshList extends Thread{
 		@Override
 		public void run() {
 			LinkedList<FilterItem> filterList = new LinkedList<>();
 			filterNr = 0;
 			filterList.addAll(sql.getPendingFilters());
 			blocklistModel.clear();
 			for(FilterItem fi : filterList){
 				if(refreshFilterItem(fi.getUrl())){
 					blocklistModel.addElement(fi);
 					filterNr++;
 				}
 			}
 			Stats.setFilterSize(filterNr);
 		}
 	}
 	
 	class FilterUpdater extends TimerTask{
 		@Override
 		public void run(){
 			
 			String currString = sql.getOldestFilter();
 			if(currString == null){
 				return;
 			}
 			
 			try {
 				refreshFilterItem(new URL(currString));
 			} catch (MalformedURLException e) {
 				logger.warning("Filter refresh failed due to "+e.getMessage());
 			}
 		}
 	}
 }
