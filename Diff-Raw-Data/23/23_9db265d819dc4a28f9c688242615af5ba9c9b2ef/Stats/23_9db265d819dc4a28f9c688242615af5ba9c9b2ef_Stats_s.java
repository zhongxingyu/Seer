 /*  Copyright (C) 2011  Nicholas Wright
 	
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
 package gui;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicInteger;
 
 /**
  * Convenience class for GUI information.
  */
 public class Stats {
 	private static List<StatListener> statListeners = new LinkedList<>();
 
 	private static int cacheSize;
 	private static long bytesSaved, bytesDiscarded;
 	private static int filterSize;
 	private static String pageQueueSize, fileQueueSize;
 	private static AtomicInteger timeGraphValue;
 	
 	static{
 		cacheSize = -1;
 		bytesDiscarded = 0;
 		bytesSaved = 0;
 		filterSize = 0;
 		timeGraphValue = new AtomicInteger();
 		
 		pageQueueSize = "";
 		fileQueueSize = "";
 	}
 
 	public static void addStatListener(StatListener listener) {
 		synchronized(statListeners) {
 			if(!statListeners.contains(listener)) {
 				statListeners.add(listener);
 			}
 		}
 	}
 
 	public static void removeStatListener(StatListener listener) {
 		synchronized(statListeners) {
 			statListeners.remove(listener);
 		}		
 	}
 
 	private static void statChanged(String stat) {
 		synchronized(statListeners) {
 			for(StatListener listener : statListeners) {
 				listener.statChanged(stat);
 			}
 		}
 	}
 
 	// Update stats
 	
 	public static void resetStats(){
 		bytesSaved = 0;
 		bytesDiscarded = 0;
		statChanged("resetLog");
 	}
 	
 	public static void saveBytes(long saved){
 		bytesSaved += saved;
 		statChanged("savedBytes");
 	}
 	
 	public static void discardBytes(long discarded){
 		bytesDiscarded += discarded;
 		statChanged("discardedBytes");
 	}
 	
 	public static void setCacheSize(int size){
 		Stats.cacheSize = size;
 		statChanged("cacheSize");
 	}
 	
 	public static void setFilterSize(int size){
 		filterSize = size;
 		statChanged("filterSize");
 	}
 	
 	public static void setPageQueueState(String state){
 		pageQueueSize = state;
 		statChanged("pageQueueSize");
 	}
 	
 	public static void setFileQueueState(String state){
 		fileQueueSize = state;
 		statChanged("fileQueueSize");
 	}
 	
 	public static void addTimeGraphValue(int value){
 		timeGraphValue.addAndGet(value);
 		statChanged("timeGraphValue");
 	}
 	
 	// Fetch stats
 	
 	public static double getBytesSaved(){
 		return bytesSaved;
 	}
 	
 	public static double getBytesDiscarded(){
 		return bytesDiscarded;
 	}
 	
 	public static int getCacheSize(){
 		return cacheSize;
 	}
 	
 	public static int getFilterSize(){
 		return filterSize;
 	}
 	
 	public static String getPageQueueState(){
 		return pageQueueSize;
 	}
 	
 	public static String getFileQueueState(){
 		return fileQueueSize;
 	}
 	
 	public static int getTimeGraphValue(){
 		int value = timeGraphValue.getAndSet(0);
 		return value;
 	}
 }
