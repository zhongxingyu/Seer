 /*
  * Copyright 2013 Eediom Inc.
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
  */
 package org.araqne.logdb;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.UUID;
 
 import org.araqne.api.FieldOption;
 import org.araqne.confdb.CollectionName;
 
 @CollectionName("saved_results")
 public class SavedResult {
 	@FieldOption(nullable = false)
 	private String guid = UUID.randomUUID().toString();
 
 	@FieldOption(nullable = false)
 	private String title;
 
 	@FieldOption(nullable = false)
 	private String owner;
 
 	@FieldOption(nullable = false)
 	private String queryString;
 
 	@FieldOption(name = "storage", nullable = false)
 	private String storageName = "v2";
 
 	@FieldOption(skip = true)
 	private String indexPath;
 
 	@FieldOption(skip = true)
 	private String dataPath;
 
 	@FieldOption(nullable = false)
 	private long fileSize;
 
 	@FieldOption(nullable = false)
 	private long rowCount;
 
 	@FieldOption(nullable = false)
 	private Date created = new Date();
 
 	public String getGuid() {
 		return guid;
 	}
 
 	public void setGuid(String guid) {
 		this.guid = guid;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getOwner() {
 		return owner;
 	}
 
 	public void setOwner(String owner) {
 		this.owner = owner;
 	}
 
 	public String getQueryString() {
 		return queryString;
 	}
 
 	public void setQueryString(String queryString) {
 		this.queryString = queryString;
 	}
 
 	public String getStorageName() {
 		return storageName;
 	}
 
 	public void setStorageName(String type) {
 		this.storageName = type;
 	}
 
 	public String getIndexPath() {
 		return indexPath;
 	}
 
 	public void setIndexPath(String indexPath) {
 		this.indexPath = indexPath;
 	}
 
 	public String getDataPath() {
 		return dataPath;
 	}
 
 	public void setDataPath(String dataPath) {
 		this.dataPath = dataPath;
 	}
 
 	public long getFileSize() {
 		return fileSize;
 	}
 
 	public void setFileSize(long fileSize) {
 		this.fileSize = fileSize;
 	}
 
 	public long getRowCount() {
 		return rowCount;
 	}
 
 	public void setRowCount(long rowCount) {
 		this.rowCount = rowCount;
 	}
 
 	public Date getCreated() {
 		return created;
 	}
 
 	public void setCreated(Date created) {
 		this.created = created;
 	}
 
 	@Override
 	public String toString() {
 		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		return "guid=" + guid + ", title=" + title + ", owner=" + owner + ", query=" + queryString + ", storage=" + storageName
 				+ ", index_path=" + indexPath + ", data_path=" + dataPath + ", file_size=" + fileSize + ", rows=" + rowCount
 				+ ", created=" + df.format(created);
 	}
 
 }
