 package com.mulesoft.module.dropbox.model;
 
 import java.io.Serializable;
 import java.util.List;
 
 /**
  * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.md file.
  */
 public class Item implements Serializable {
    private static final long serialVersionUID = -1L;

     /*
         Represents metadata for a file, check DB docs at:
         https://www.dropbox.com/developers/core/docs#metadata
 
         {
             "size": "225.4KB",
             "rev": "35e97029684fe",
             "hash": "35e97029684fe",
             "thumb_exists": false,
             "bytes": 230783,
             "modified": "Tue, 19 Jul 2011 21:55:38 +0000",
             "client_mtime": "Mon, 18 Jul 2011 18:04:35 +0000",
             "path": "/Getting_Started.pdf",
             "is_dir": false,
             "is_deleted": false,
             "icon": "page_white_acrobat",
             "root": "dropbox",
             "mime_type": "application/pdf",
             "revision": 220823
         }
      */
 
     /**
      * A human-readable description of the file size (translated by locale).
      */
     private String size;
 
     /**
      * A unique identifier for the current revision of a file. This field is the same rev as elsewhere in the API and can be used to detect changes and avoid conflicts.
      */
     private String rev;
 
     /**
      * A folder's hash is useful for indicating changes to the folder's contents in later calls to /metadata. This is roughly the folder equivalent to a file's rev.
      */
     private String hash;
 
     /**
      * True if the file is an image that can be converted to a thumbnail via the /thumbnails call.
      */
     private Boolean thumbExists;
 
     /**
      * The file size in bytes.
      */
     private Long bytes;
 
     /**
      * The last time the file was modified on Dropbox, in the standard date format (not included for the root folder).
      */
     private String modified;
 
     /**
      * For files, this is the modification time set by the desktop client when the file was added to Dropbox, in the standard date format. Since this time is not verified (the Dropbox server stores whatever the desktop client sends up), this should only be used for display purposes (such as sorting) and not, for example, to determine if a file has changed or not.
      */
     private String clientMtime;
 
     /**
      * Returns the canonical path to the file or directory.
      */
     private String path;
 
     /**
      * Whether the given entry is a folder or not.
      */
     private Boolean isDir;
 
     /**
      * Whether the given entry is deleted (only included if deleted files are being returned).
      */
     private Boolean isDeleted;
 
     /**
      * The name of the icon used to illustrate the file type in Dropbox's icon library.
      */
     private String icon;
 
     /**
      * The root or top-level folder depending on your access level. All paths returned are relative to this root level. Permitted values are either dropbox or app_folder.
      */
     private String root;
 
     /**
      * The file's mime type
      */
     private String mimeType;
 
     /**
      * A deprecated field that semi-uniquely identifies a file. Use rev instead.
      */
     private Long revision;
 
     /**
      * If the requested Item is a folder in Dropbox, return the folder's content
      */
     private List<Item> contents;
 
     public String getSize() {
         return size;
     }
 
     public void setSize(String size) {
         this.size = size;
     }
 
     public String getRev() {
         return rev;
     }
 
     public void setRev(String rev) {
         this.rev = rev;
     }
 
     public String getHash() {
         return hash;
     }
 
     public void setHash(String hash) {
         this.hash = hash;
     }
 
     public Boolean getThumbExists() {
         return thumbExists;
     }
 
     public void setThumbExists(Boolean thumbExists) {
         this.thumbExists = thumbExists;
     }
 
     public Long getBytes() {
         return bytes;
     }
 
     public void setBytes(Long bytes) {
         this.bytes = bytes;
     }
 
     public String getModified() {
         return modified;
     }
 
     public void setModified(String modified) {
         this.modified = modified;
     }
 
     public String getClientMtime() {
         return clientMtime;
     }
 
     public void setClientMtime(String clientMtime) {
         this.clientMtime = clientMtime;
     }
 
     public String getPath() {
         return path;
     }
 
     public void setPath(String path) {
         this.path = path;
     }
 
     public Boolean getDir() {
         return isDir;
     }
 
     public void setDir(Boolean dir) {
         isDir = dir;
     }
 
     public Boolean getDeleted() {
         return isDeleted;
     }
 
     public void setDeleted(Boolean deleted) {
         isDeleted = deleted;
     }
 
     public String getIcon() {
         return icon;
     }
 
     public void setIcon(String icon) {
         this.icon = icon;
     }
 
     public String getRoot() {
         return root;
     }
 
     public void setRoot(String root) {
         this.root = root;
     }
 
     public String getMimeType() {
         return mimeType;
     }
 
     public void setMimeType(String mimeType) {
         this.mimeType = mimeType;
     }
 
     public Long getRevision() {
         return revision;
     }
 
     public void setRevision(Long revision) {
         this.revision = revision;
     }
 
     public List<Item> getContents() {
         return contents;
     }
 
     public void setContents(List<Item> contents) {
         this.contents = contents;
     }
 }
