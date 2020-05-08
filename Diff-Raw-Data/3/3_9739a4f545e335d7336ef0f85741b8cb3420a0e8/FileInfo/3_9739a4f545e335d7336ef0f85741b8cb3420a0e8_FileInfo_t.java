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
 
 import java.io.File;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import com.google.jstestdriver.hooks.FileInfoScheme;
 import com.google.jstestdriver.model.HandlerPathPrefix;
 
 /**
  * Represents a test resource.
  * 
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  */
 public class FileInfo {
 
   public static final String SEPARATOR_CHAR = "/";
 
   private String filePath;
   private Long timestamp;
   private transient boolean isPatch;
   private boolean serveOnly;
   private List<FileInfo> patches;
   private String data;
 
   private long length;
 
   public FileInfo() {
   }
 
   public FileInfo(String fileName, long timestamp, long length,
       boolean isPatch, boolean serveOnly, String data) {
     this.filePath = fileName;
     this.timestamp = timestamp;
     this.length = length;
     this.isPatch = isPatch;
     this.serveOnly = serveOnly;
     this.data = data;
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
 
   /** Gets the absolute file name by appending the specified base path to a relative path. */
   public String getAbsoluteFileName(File basePath) {
     return getPath(basePath, filePath);
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
 
   public File toFile(File basePath) {
     return new File(getPath(basePath, filePath));
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
     return new FileInfo(filePath, timestamp, length, isPatch, serveOnly, data);
   }
 
   /** Translates the FileInfo into a lightwieght FileSrc object. */
   public FileSource toFileSource(HandlerPathPrefix prefix, Set<FileInfoScheme> schemes) {
     for (FileInfoScheme scheme : schemes) {
       if (scheme.matches(filePath)) {
         return new FileSource(filePath, this.getTimestamp());
       }
     }
     return new FileSource(prefix.prefixPath("/test/" + this.getDisplayPath()), this.getTimestamp());
   }
 
   @Override
   public String toString() {
     return "FileInfo [filePath=" + filePath + ", length=" + length + ", patches=" + patches
         + ", serveOnly=" + serveOnly + ", timestamp=" + timestamp + "]";
   }
 
   /** Formats the specified path to use {@link #SEPARATOR_CHAR} as the path separator. */
   public static final String formatFileSeparator(String path) {
     return path.replaceAll("\\\\", SEPARATOR_CHAR);
   }
 
   /**
    * Gets a path containing the base dir and relative file path. The file separators in the path
    * will be formatted to use {@link #SEPARATOR_CHAR} as the path separator.
    */
   public static String getPath(File dir, String path) {
   // Don't prepend the directory if the path is already absolute
     if (new File(path).isAbsolute()) {
       return path;
     }
 
     String dirPath = formatFileSeparator(dir.getPath());
 
     if (!dirPath.endsWith(SEPARATOR_CHAR)) {
       dirPath = dirPath + SEPARATOR_CHAR;
     }
 
     return dirPath + path;
   }
 
   /**
    * @param resolvedPath The resolved absolute path of the file.
    * @param timestamp The timestamp of the file.
    * @return An updated FileInfo.
    */
   public FileInfo fromResolvedPath(String resolvedPath, long timestamp) {
     return new FileInfo(resolvedPath, timestamp,
       length, isPatch, serveOnly, null);
   }
 
   /**
    * Provides a unique identifier to reference this FileInfo in the browser.
    */
   public String getDisplayPath() {
    // remove relative path markers, as they won't resolve properly in the browser.
    return filePath.replace("..", "_");
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
     fileContent.append(reader.readFile(this.getAbsoluteFileName(basePath)));
     List<FileInfo> patches = this.getPatches();
     if (patches != null) {
       for (FileInfo patch : patches) {
         fileContent.append(reader.readFile(patch.getAbsoluteFileName(basePath)));
       }
     }
     return new FileInfo(filePath,
                         timestamp,
                         length,
                         isPatch,
                         serveOnly,
                         fileContent.toString());
   }
 }
