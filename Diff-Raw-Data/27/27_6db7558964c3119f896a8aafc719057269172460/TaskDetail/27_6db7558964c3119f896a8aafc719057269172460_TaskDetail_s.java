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
 
 /**
  * The detail of a task. Every attributs are public.
  * 
  * @author Eric Taix (eric.taix at gmail.com)
  */
 @SuppressWarnings("serial")
 public class TaskDetail implements Serializable {
 	// Is it a torrent
 	public boolean isTorrent;
 	// Is it a NZB
 	public boolean isNZB;
 
 	// The current status of this task
 	public String status;
 
 	// The current upload speed
 	public double speedUpload = 0;
 	// The current download speed
 	public double speedDownload = 0;
 
 	// The filename
 	public String fileName;
 	// The file size in bytes
 	public long fileSize;
 	// The destination directory
 	public String destination;
 	// The original file's URL
 	public String url;
 	// The username
 	public String userName;
 
 	// The current pieces downloaded
 	public Long piecesCurrent;
 	// The total number of pieces
 	public Long piecesTotal;
 
 	// The number of bytes uploaded
 	public long bytesUploaded;
 	// The number of bytes downloaded
 	public long bytesDownloaded;
 
 	// The sedding time elapsed
 	public long seedingElapsed;
 
 	// The creation date in second since 1 jan 1970
 	public String creationDate;
 	// The start date of seeding in second since 1 jan 1970
 	public String seedingDate;
 	// The seeding interval
 	public int seedingInterval;
 	// The current seeding ratio
 	public int seedingRatio;
 
 	// The current peers
 	public Long peersCurrent;
 	// The total number of peers
 	public Long peersTotal;
 
 	// The unique ID of this task
 	public int taskId;
 
 	// The number of seeders
 	public Long seeders;
 	// The number of leechers
 	public Long leechers;
 	public int bytesRatio;
 
 	public TaskStatus getStatus() {
 		TaskStatus taskStat;
 		try {
 			taskStat = TaskStatus.valueOf(status);
 		} catch (Exception e) {
			taskStat = TaskStatus.TASK_UNKNOWN;
 		}
 
 		return taskStat;
 	}
 }
