 /*
  * Copyright 2004-2009 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.teeda.extension.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.disk.DiskFileItem;
 
 /**
  * @author koichik
  */
 public class UploadedFileImpl implements UploadedFile {
 
    private static final long serialVersionUID = 1L;

     protected final FileItem fileItem;
 
     /**
      * インスタンスを構築します。
      * 
      * @param fileItem Apache Commons FileUploadの<code>FileItem</code>
      */
     public UploadedFileImpl(final FileItem fileItem) {
         this.fileItem = fileItem;
     }
 
     public String getName() {
         final String name = fileItem.getName();
         final File file = new File(name);
         return file.getName();
     }
 
     public String getOriginalName() {
         return fileItem.getName();
     }
 
     public String getContentType() {
         return fileItem.getContentType();
     }
 
     public long getSize() {
         return fileItem.getSize();
     }
 
     public boolean isInMemory() {
         return fileItem.isInMemory();
     }
 
     public byte[] get() {
         return fileItem.get();
     }
 
     public String getString() {
         return fileItem.getString();
     }
 
     public String getString(final String encoding)
             throws UnsupportedEncodingException {
         return fileItem.getString(encoding);
     }
 
     public InputStream getInputStream() throws IOException {
         return fileItem.getInputStream();
     }
 
     public void write(final File file) throws Exception {
         fileItem.write(file);
     }
 
     public File getStoreLocation() {
         if (fileItem.isInMemory()) {
             return null;
         }
         return ((DiskFileItem) fileItem).getStoreLocation();
     }
 
     public void delete() {
         fileItem.delete();
     }
 
 }
