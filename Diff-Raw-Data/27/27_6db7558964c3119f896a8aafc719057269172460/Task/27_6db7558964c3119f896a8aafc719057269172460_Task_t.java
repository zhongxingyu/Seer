 /**
  * Copyright 2010 Eric Taix
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  * 
  */
 package com.bigpupdev.synodroid.data;
 
 import java.io.Serializable;
 
 import com.bigpupdev.synodroid.server.SimpleSynoServer;
 
 /**
  * A simple data container for a torrent. This class is used to display 'general' information about a torrent file.<br/>
  * 
  * @author Eric Taix (eric.taix at gmail.com)
  */
 public class Task implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	// The server from which is torrent is retrieved
 	public transient SimpleSynoServer server;
 
 	// The unique ID of the torrent
 	public int taskId;
 	// The filename
 	public String fileName;
 	// Total size
 	public String totalSize;
 	// Creator's name
 	public String creator;
 	// Upload rate
 	public String uploadRate;
 	// Upload Progress
 	public int uploadProgress = -1;
 	// Upload progress timeStamp
 	public long uploadTimestamp;
 	// Download rate
 	public String downloadRate;
 	// Current size downloaded
 	public String downloadSize;
 	// download Progress
 	public int downloadProgress;
 	// Status
 	public String status;
 	// Time left
 	public String eta;
 	// Task is an outside url
 	public boolean outside_url = false;
 	// Flag to know if this task is a torrent.
 	// CAREFUL: this attribut is not retrieved from the getAll JSON method.
 	// First you have to call getTaskDetail to retrieve this information and set
 	// manually this attribut. In fact this attribut
 	// is a convenient way to store this value for the next activity
 	public boolean isTorrent = true;
 	// Flag to know if this task is a NZB
 	// CAREFUL: this attribut is not retrieved from the getAll JSON method.
 	// First you have to call getTaskDetail to retrieve this information and set
 	// manually this attribut. In fact this attribut
 	// is a convenient way to store this value for the next activity
 	public boolean isNZB;
 	// The original link
 	// CAREFUL: this attribut is not retrieved from the getAll JSON method.
 	// It is filled when clicking to the get original file link, by using the
 	// "details.url" value
 	public String originalLink;
 	
 	public boolean selected = false;
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + taskId;
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Task other = (Task) obj;
 		if (taskId != other.taskId)
 			return false;
 		return true;
 	}
 
 	public TaskStatus getStatus() {
 		TaskStatus taskStat;
 		try {
 			taskStat = TaskStatus.valueOf(status);
 		} catch (Exception e) {
			if (status.startsWith("TASK_EXTRACTING")){
				taskStat = TaskStatus.TASK_EXTRACTING;
			}
			else{
				taskStat = TaskStatus.TASK_UNKNOWN;
			}
 		}
 
 		return taskStat;
 	}
 	
 	public String getStrID(){
 		return ""+taskId;
 	}
 }
