 /**
  *  Copyright (C) 2009 Progress Software, Inc. All rights reserved.
  *  http://fusesource.com
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.fusesource.meshkeeper.util.internal;
 
 import static org.fusesource.meshkeeper.util.internal.IOSupport.close;
 import static org.fusesource.meshkeeper.util.internal.IOSupport.copy;
 
 import java.io.*;
 import java.util.zip.ZipOutputStream;
 import java.util.zip.ZipEntry;
 
 /** 
  * FileUtils
  * <p>
  * Description:
  * </p>
  * @author cmacnaug
  * @version 1.0
  */
 public class FileSupport {
 
     static final int DELETION_ATTEMPTS = Integer.getInteger("file.deletion.attempts", 5);
     static final long DELETION_SLEEP = Long.getLong("file.deletion.sleep", 200);
     static final long ROUNDUP_MILLIS = 1999;
     
     public static void recursiveDelete(String srcDir) throws IOException {
         //String srcFileName = "";
         String[] fileList;
 
         //Just delete and return if a file is specified:
         File srcFile = new File(srcDir);
 
         //Check to make sure that we aren't deleting a root or first level directory:
         checkDirectoryDepth(srcFile.getAbsolutePath(), "Directory depth is too shallow to risk recursive delete for path: " + srcFile.getAbsolutePath()
                 + " directory depth should be at least 2 levels deep.", 2);
 
         if (!srcFile.exists()) {
         } else if (srcFile.isFile()) {
             int retries = 0;
             while (!srcFile.delete()) {
                 if (retries > 20) {
                     throw new IOException("ERROR: Unable to delete file: " + srcFile.getAbsolutePath());
                 }
                 retries++;
             }
         } else {
             fileList = srcFile.list();
             // Copy parts from cd to installation directory
             for (int j = 0; j < fileList.length; j++) {
                 //Format file names
                 recursiveDelete(srcDir + File.separator + fileList[j]);
             }
             //Finally once all leaves are deleted delete this node:
             int retries = 0;
 
             while (!srcFile.delete()) {
                 if (retries > 20) {
                     throw new IOException("ERROR: Unable to delete directory. Not empty?");
                 }
                 retries++;
             }
         }
     }//private void recursiveDelete(String dir)
     
     
     private static void checkDirectoryDepth(String path, String message, int minDepth) throws IOException {
         int depth = 0;
         int index = -1;
         if (path.startsWith(File.separator + File.separator)) {
             depth -= 2;
         } else if (path.startsWith(File.separator)) {
             depth--;
         }
 
         while (true) {
             index = path.indexOf(File.separator, index + 1);
             if (index == -1) {
                 break;
             } else {
                 depth++;
             }
         }
 
         if (minDepth > depth)
             throw new IOException(message);
     }
 
 
     /**
      * @param file the file or directory to recusively delete
      * @return true if the directory was deleted.
      */
     public static boolean recursiveDelete(File file) {
         boolean rc = true;
 
         if (file.isDirectory()) {
             File[] files = file.listFiles();
             for (int i = 0; i < files.length; i++) {
                 rc &= recursiveDelete(files[i]);
             }
         }
         
         for (int i = 0; i < DELETION_ATTEMPTS; i++) {
             boolean success = file.delete();
 
             if (success) {
                 break;
             }
             try {
                 Thread.sleep(DELETION_SLEEP);
             } catch (InterruptedException e) {
                 Thread.currentThread().interrupt();
                 break;
             }
         }
 
         return rc;
 
     }
     
 
 
     public static void write(InputStream source, File target) throws IOException {
         FileOutputStream os = new FileOutputStream(target);
         try {
             IOSupport.copy(source, os);
         } finally {
             IOSupport.close(os);
         }
     }
     
     static public byte[] read(File file, long pos, int length) throws IOException {
         RandomAccessFile is = new RandomAccessFile(file, "r");
         try {
             long remaining = is.length()-pos;
             if( remaining < 0) {
                 remaining = 0;
             }
             byte rc[] = new byte[(int) Math.min(remaining, length)];
             if( rc.length == 0 ) {
                 return rc;
             }
             is.seek(pos);
             is.readFully(rc);
             return rc;
         } finally {
             is.close();
         }
     }
 
     
     public static File jar(File source) throws IOException {
         File tempJar = File.createTempFile("temp", ".jar");
         jar(source, tempJar);
         return tempJar;
     }
 
     public static void jar(File source, File target) throws IOException {
         ZipOutputStream os = new ZipOutputStream(new FileOutputStream(target));
         try {
             os.setMethod(ZipOutputStream.DEFLATED);
             os.setLevel(5);
             recusiveJar(os, source, null);
         } finally {
             close(os);
         }
     }
 
     private static void recusiveJar(ZipOutputStream os, File source, String jarpath) throws IOException {
         String prefix = "";
         if (jarpath != null) {
             ZipEntry entry = new ZipEntry(jarpath);
             entry.setTime(source.lastModified() + ROUNDUP_MILLIS);
             os.putNextEntry(entry);
             prefix = jarpath + "/";
         }
 
         if (source.isDirectory()) {
             for (File file : source.listFiles()) {
                 recusiveJar(os, file, prefix + file.getName());
             }
         } else {
             FileInputStream is = new FileInputStream(source);
             try {
                 copy(is, os);
            } finally {
                 close(is);
             }
         }
     }
 
 }
