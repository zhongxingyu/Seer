 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.model;
 
 import java.sql.Timestamp;
 import org.jamwiki.utils.WikiLogger;
 
 /**
  * Provides an object representing a version of a file uploaded to the Wiki.
  */
 public class WikiFileVersion {
 
 	private Integer authorId = null;
 	private String authorIpAddress = null;
 	private int fileId = -1;
 	private long fileSize = -1;
 	private int fileVersionId = -1;
	private String mimeType = null;
 	private String uploadComment = null;
 	private Timestamp uploadDate = new Timestamp(System.currentTimeMillis());
 	private String url = null;
 	private static final WikiLogger logger = WikiLogger.getLogger(WikiFileVersion.class.getName());
 
 	/**
 	 *
 	 */
 	public WikiFileVersion() {
 	}
 
 	/**
 	 *
 	 */
 	public Integer getAuthorId() {
 		return this.authorId;
 	}
 
 	/**
 	 *
 	 */
 	public void setAuthorId(Integer authorId) {
 		this.authorId = authorId;
 	}
 
 	/**
 	 *
 	 */
 	public String getAuthorIpAddress() {
 		return this.authorIpAddress;
 	}
 
 	/**
 	 *
 	 */
 	public void setAuthorIpAddress(String authorIpAddress) {
 		this.authorIpAddress = authorIpAddress;
 	}
 
 	/**
 	 *
 	 */
 	public int getFileId() {
 		return this.fileId;
 	}
 
 	/**
 	 *
 	 */
 	public void setFileId(int fileId) {
 		this.fileId = fileId;
 	}
 
 	/**
 	 *
 	 */
 	public long getFileSize() {
 		return this.fileSize;
 	}
 
 	/**
 	 *
 	 */
 	public void setFileSize(long fileSize) {
 		this.fileSize = fileSize;
 	}
 
 	/**
 	 *
 	 */
 	public int getFileVersionId() {
 		return this.fileVersionId;
 	}
 
 	/**
 	 *
 	 */
 	public void setFileVersionId(int fileVersionId) {
 		this.fileVersionId = fileVersionId;
 	}
 
 	/**
	 *
 	 */
 	public String getMimeType() {
		return this.mimeType;
 	}
 
 	/**
 	 *
 	 */
 	public void setMimeType(String mimeType) {
 		this.mimeType = mimeType;
 	}
 
 	/**
 	 *
 	 */
 	public String getUploadComment() {
 		return this.uploadComment;
 	}
 
 	/**
 	 *
 	 */
 	public void setUploadComment(String uploadComment) {
 		this.uploadComment = uploadComment;
 	}
 
 	/**
 	 *
 	 */
 	public Timestamp getUploadDate() {
 		return this.uploadDate;
 	}
 
 	/**
 	 *
 	 */
 	public void setUploadDate(Timestamp uploadDate) {
 		this.uploadDate = uploadDate;
 	}
 
 	/**
 	 *
 	 */
 	public String getUrl() {
 		return this.url;
 	}
 
 	/**
 	 *
 	 */
 	public void setUrl(String url) {
 		this.url = url;
 	}
 }
