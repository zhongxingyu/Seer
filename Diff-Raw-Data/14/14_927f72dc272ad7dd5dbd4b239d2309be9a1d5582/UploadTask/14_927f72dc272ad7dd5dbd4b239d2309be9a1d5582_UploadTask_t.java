 //
 // Treasure Data Bulk-Import Tool in Java
 //
 // Copyright (C) 2012 - 2013 Muga Nishizawa
 //
 //    Licensed under the Apache License, Version 2.0 (the "License");
 //    you may not use this file except in compliance with the License.
 //    You may obtain a copy of the License at
 //
 //        http://www.apache.org/licenses/LICENSE-2.0
 //
 //    Unless required by applicable law or agreed to in writing, software
 //    distributed under the License is distributed on an "AS IS" BASIS,
 //    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //    See the License for the specific language governing permissions and
 //    limitations under the License.
 //
 package com.treasure_data.td_import.upload;
 
 import java.io.File;
 
 import com.treasure_data.td_import.source.Source;
 
 public class UploadTask extends UploadTaskBase {
     public String sessName;
     public String partName;
 
     // unit testing
     public boolean isTest = false;
     public byte[] testBinary = null;
 
     public UploadTask(String sessName, Source source) {
         super(source);
         this.sessName = sessName;
         int lastSepIndex = fileName.lastIndexOf(File.separatorChar);
         this.partName = fileName.substring(lastSepIndex + 1,
                 fileName.length()).replace('.', '_');
     }
 
 //    public UploadTask(String sessName, String fileName, long size) {
 //        super(fileName, size);
 //        this.sessName = sessName;
 //        int lastSepIndex = fileName.lastIndexOf(File.separatorChar);
 //        this.partName = fileName.substring(lastSepIndex + 1,
 //                fileName.length()).replace('.', '_');
 //    }
 
     @Override
     public boolean equals(Object obj) {
         if (! (obj instanceof UploadTask)) {
             return false;
         }
 
         UploadTask t = (UploadTask) obj;
        return t.sessName.equals(sessName) && t.fileName.equals(fileName) && t.partName.equals(partName);
     }
 
 }
