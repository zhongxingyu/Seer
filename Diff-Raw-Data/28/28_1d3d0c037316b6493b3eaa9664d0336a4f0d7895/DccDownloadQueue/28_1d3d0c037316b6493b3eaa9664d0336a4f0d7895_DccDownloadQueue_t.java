 /*
  * Project: xdccBee
  * Copyright (C) 2009 snert@snert-lab.de,
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.snertlab.xdccBee.irc;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 
 import org.jibble.pircbot.DccFileTransfer;
 
 
 /**
  * @author snert
  *
  */
 public class DccDownloadQueue {
 	
 	private static DccDownloadQueue dccDownloadQueue;
 	private HashMap<String, DccDownload> mapDownloadQueue;
 	
 	public static DccDownloadQueue getInstance(){
 		if(dccDownloadQueue==null){
 			dccDownloadQueue = new DccDownloadQueue();
 		}
 		return dccDownloadQueue;
 	}
 	
 	public DccDownloadQueue(){
 		this.mapDownloadQueue = new HashMap<String, DccDownload>();
 	}
 	
 	public void addToQueue(DccDownload dccDownload){
 		mapDownloadQueue.put(dccDownload.getKey(), dccDownload);
 	}
 	
 	public List<DccDownload> getListDccDownloadsImmutable(){
 		return Collections.unmodifiableList( new ArrayList<DccDownload>( mapDownloadQueue.values() ) );
 	}
 
 	public DccDownload getDccDownload(DccFileTransfer dccFileTransfer) {
 		List<DccDownload> listDccDownloads = getListDccDownloadsImmutable();
 		for (DccDownload dccDownload : listDccDownloads) {
 			if( dccDownload.matchDccFileTransfer(dccFileTransfer) ){
 				return dccDownload;
 			}
 		}
 		return null;
 	}
	
	public boolean containsDccDownload(DccDownload dccDownload){
		return mapDownloadQueue.containsKey(dccDownload.getKey());
	}

	public DccDownload getDccDownload(String key) {
		return mapDownloadQueue.get(key);
	}
 }
