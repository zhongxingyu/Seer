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
 package io;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.logging.Logger;
 
 import javax.activity.InvalidActivityException;
 
 import net.DownloadItem;
 import net.FileLoader;
 import net.PageLoadException;
 import filter.Filter;
 import gui.Log;
 import gui.Stats;
 
 public class ImageLoader extends FileLoader {
 private static final Logger LOGGER = Logger.getLogger(ImageLoader.class.getName());
 
 private FileWriter fileWriter;
 private Filter filter;
 
 private final int TIME_GRAPH_FACTOR = 1; // factor used for scaling DataGraph output
 
 	public ImageLoader(FileWriter fileWriter, Filter filter, File workingDir, int imageQueueWorkers) {
 		super(workingDir, imageQueueWorkers);
 		this.fileWriter = fileWriter;
 		this.filter = filter;
 	}
 
 	@Override
 	protected boolean beforeFileAdd(URL url, String fileName) {
 		if(filter.isCached(url)){	// has the file been downloaded recently?
 			filter.cache(url);		// if it has, update cache timestamp
 			return false;
 		}
 		return true;
 	}
 	
 	@Override
 	protected void afterFileAdd(URL url, String fileName) {
 		updateFileQueueState();
 	}
 	
 	private void updateFileQueueState(){
 		Stats.setFileQueueState("FileQueue: "+downloadList.size()+" - "+"? / "+fileQueueWorkers);
 		// queue size  - active workers / pool size
 	}
 	
 	@Override
 	protected void afterClearQueue() {
 		updateFileQueueState();
 	}
 	
 	@Override
 	protected void afterProcessItem(DownloadItem ii) {
 		updateFileQueueState();
 	}
 	
 	@Override
 	protected void afterFileDownload(byte[] data, File fullpath, URL url) {
 		if(data != null){
 			try {
 				fileWriter.add(fullpath, data.clone());
 			} catch (InvalidActivityException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
			filter.cache(url);;	//add URL to cache
			Stats.addTimeGraphValue((int)((data.length/1024)*TIME_GRAPH_FACTOR)); // add data to the download graph
 		}
 	}
 	
 	@Override
 	protected void onPageLoadException(PageLoadException ple) {
 		int responseCode = Integer.parseInt(ple.getMessage());
 
 		// the file was unavailable
 		if(responseCode == 404 || responseCode == 500){
 			LOGGER.warning("got a 404 or 500 response for " + ple.getUrl()); // to prevent future attempts to load the file
			try {filter.cache(new URL(ple.getUrl()));
			} catch (MalformedURLException e) {
				LOGGER.warning("could not add URL to cache " + ple.getMessage());
			} 
 		}
 
 		if(responseCode == 503){
 //			LOGGER.severe("IP was banned for too many connections"); // :_(  thats what you get if you use too short intervals
 //			System.exit(3);
 		}else{
 			LOGGER.info("GetBinary(size) http code "+ple.getMessage());
 		}
 	}
 }
