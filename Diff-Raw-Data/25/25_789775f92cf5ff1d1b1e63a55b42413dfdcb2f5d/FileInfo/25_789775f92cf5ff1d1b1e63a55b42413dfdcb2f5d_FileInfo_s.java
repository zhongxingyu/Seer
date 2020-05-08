 /*
  * Copyright 2009 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.jstestdriver;
 
 import com.google.jstestdriver.hooks.FileInfoScheme;
 import com.google.jstestdriver.model.HandlerPathPrefix;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Represents a test resource. The filePath is considered the canonical identifier
  * for a resource.
  * 
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  */
 public class FileInfo implements Cloneable {
   private static final Logger logger = LoggerFactory.getLogger(FileInfo.class);
 
   private String filePath;
   private Long timestamp;
   private transient boolean isPatch;
   private boolean serveOnly;
   private List<FileInfo> patches;
   private String data;
 
   private long length;
 
   private String displayPath;
 
   public FileInfo() {
   }
 
   public FileInfo(String filePath, long timestamp, long length,
       boolean isPatch, boolean serveOnly, String data, String displayPath) {
     this.filePath = filePath;
     this.timestamp = timestamp;
     this.length = length;
     this.isPatch = isPatch;
     this.serveOnly = serveOnly;
     this.data = data;
     this.displayPath = displayPath;
   }
 
   public String getData() {
     return data == null ? "" : data;
   }
 
   public void setData(String data) {
     this.data = data;
   }
 
   public List<FileInfo> getPatches() {
     if (patches != null) {
       return new LinkedList<FileInfo>(patches);
     }
     return new LinkedList<FileInfo>();
   }
 
   public void addPatch(FileInfo patch) {
     if (patches == null) {
       patches = new LinkedList<FileInfo>();
     }
     this.patches.add(patch);
   }
 
   public long getLength() {
     return length;
   }
 
   public boolean isServeOnly() {
     return serveOnly;
   }
 
   /** Gets the path of a file. The path may be relative. */
   public String getFilePath() {
     return filePath;
   }
 
   public long getTimestamp() {
     return timestamp;
   }
 
   public void setTimestamp(long timestamp) {
     this.timestamp = timestamp;
   }
 
   public boolean isPatch() {
     return isPatch;
   }
 
   public boolean isWebAddress() {
     return filePath.startsWith("http://") || filePath.startsWith("https://");
   }
 
   public boolean canLoad() {
     return !isWebAddress();
   }
 
   public boolean isLoaded() {
     return data != null;
   }
 
   public File toFile(File basePath) {
     return new File(filePath);
   }
 
   @Override
   public int hashCode() {
     final int prime = 31;
     int result = 1;
     result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
     result = prime * result + (serveOnly ? 1231 : 1237);
     return result;
   }
 
   @Override
   public boolean equals(Object obj) {
     if (!(obj instanceof FileInfo)) {
       return false;
     }
     FileInfo other = (FileInfo) obj;
     if (filePath == null) {
       if (other.filePath != null){
         return false;
       }
     }
     if (!filePath.equals(other.filePath)){
       return false;
     }
     if (serveOnly != other.serveOnly){
       return false;
     }
     return true;
   }
 
   public FileInfo load(String data, long timestamp) {
     return new FileInfo(filePath, timestamp, length, isPatch, serveOnly, data, displayPath);
   }
 
   /** Translates the FileInfo into a lightweight FileSrc object. */
   public FileSource toFileSource(HandlerPathPrefix prefix, Set<FileInfoScheme> schemes) {
     for (FileInfoScheme scheme : schemes) {
       if (scheme.matches(filePath)) {
         return new FileSource(displayPath, filePath, this.getTimestamp(), length);
       }
     }
     return new FileSource(prefix.prefixPath("/test/" + this.getDisplayPath()), filePath, this.getTimestamp(), length);
   }
 
   @Override
   public String toString() {
     if (logger.isDebugEnabled() || logger.isTraceEnabled()) {
       return "\n\tFileInfo [filePath=" + filePath + ", length=" + length + ", patches=" + patches
           + ", serveOnly=" + serveOnly + ", timestamp=" + timestamp + "]";
     }
     return "\n\tFileInfo[" + this.getDisplayPath() + "]";
   }
 
   /**
    * @param resolvedPath The resolved absolute path of the file.
    * @param displayPath The path to send to the browser for debugging and display.
    * @param timestamp The timestamp of the file.
    * @return An updated FileInfo.
    */
   public FileInfo fromResolvedPath(String resolvedPath, String displayPath, long timestamp) {
     return new FileInfo(resolvedPath, timestamp,
       length, isPatch, serveOnly, data, displayPath);
   }
 
   /**
    * Provides a unique identifier to reference this FileInfo in the browser.
    */
   public String getDisplayPath() {
     // remove relative path markers, as they won't resolve properly in the browser.
     return displayPath;
   }
 
   /**
    * Loads a file from the file system using a reader.
    * @param reader The file reader to pull from the file system.
    * @param basePath The base path
    * @return The loaded file info.
    */
   public FileInfo loadFile(FileReader reader, File basePath) {
     if (!this.canLoad()) {
       return this;
     }
     StringBuilder fileContent = new StringBuilder();
     fileContent.append(reader.readFile(filePath));
     List<FileInfo> patches = this.getPatches();
     if (patches != null) {
       for (FileInfo patch : patches) {
         fileContent.append(reader.readFile(patch.getFilePath()));
       }
     }
     return load(fileContent.toString(), timestamp);
   }
 
   @SuppressWarnings("unused")
   @Override
   protected Object clone() throws CloneNotSupportedException {
     return new FileInfo(filePath, timestamp, length, isPatch, serveOnly, data, displayPath);
   }
 
   /**
    * Tests to see if a file is a proper replacement: different timestamp, length.
    * Also returns false if the paths don't match.
    */
   public boolean shouldReplaceWith(FileInfo file) {
     if (!filePath.equals(file.getFilePath())) {
       logger.trace("paths not equal {} {}", getDisplayPath(), file.getDisplayPath());
       return false;
     }
     if (getTimestamp() != file.getTimestamp()) {
       logger.trace("replace {} because {} != {}", new Object[]{getDisplayPath(), getTimestamp(), file.getTimestamp()});
       return true;
     }
    if (getLength() != file.getLength()) {
       logger.trace("replace {} because {} != {}", new Object[]{getDisplayPath(), getLength(), file.getLength()});
       return true;
    }
     return false;
   }
 }
