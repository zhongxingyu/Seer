 // Copyright (c) 2012, Daniel Andersen (dani_ande@yahoo.dk)
 // All rights reserved.
 //
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions are met:
 //
 // 1. Redistributions of source code must retain the above copyright notice, this
 //    list of conditions and the following disclaimer.
 // 2. Redistributions in binary form must reproduce the above copyright notice,
 //    this list of conditions and the following disclaimer in the documentation
 //    and/or other materials provided with the distribution.
 // 3. The name of the author may not be used to endorse or promote products derived
 //    from this software without specific prior written permission.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 // ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 // DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 // ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 // LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 // ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 // (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 // SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package com.trollsahead.qcumberless.util;
 
 import java.io.*;
 import java.nio.channels.FileChannel;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 public class FileUtil {
     public static String[] getFeatureFiles(File[] files) {
         return traverseDirectory(files, ".feature").toArray(new String[0]);
     }
 
     public static List<String> getFeatureFiles(String directory) {
         return traverseDirectory(new File[] {new File(directory)}, ".feature");
     }
 
     public static List<String> getHistoryFiles(String directory) {
         return traverseDirectory(new File[] {new File(directory)}, ".history");
     }
 
     public static List<String> traverseDirectory(File[] files, String suffix) {
         List<String> foundFiles = new LinkedList<String>();
         if (files == null) {
             return foundFiles;
         }
         for (File file : files) {
             if (file.isDirectory()) {
                 foundFiles.addAll(traverseDirectory(file.listFiles(), suffix));
             } else if (file.getAbsolutePath().endsWith(suffix)) {
                 foundFiles.add(file.getAbsolutePath());
             }
         }
         return foundFiles;
     }
 
     public static void close(Closeable closeable) {
         try {
             closeable.close();
         } catch (Exception e) {
             // Ignore!
         }
     }
 
     public static String getPath(String filename) {
         if (Util.isEmpty(filename)) {
             return filename;
         }
         if (!filename.contains("/")) {
            return new File(".").getAbsolutePath();
         }
         return filename.substring(0, filename.lastIndexOf("/"));
     }
 
     public static String addSlashToPath(String path) {
         if (Util.isEmpty(path)) {
             return "";
         } else {
             return path.endsWith("/") ? path : (path + "/");
         }
     }
 
     public static String removeTrailingSlash(String filename) {
         if (!Util.isEmpty(filename) && filename.startsWith("/")) {
             return filename.substring(1);
         } else {
             return filename;
         }
     }
     
     public static File writeToFile(String filename, StringBuilder sb) {
         BufferedWriter out = null;
         try {
             File file = new File(filename);
             out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
             out.write(sb.toString());
             return file;
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         } finally {
             FileUtil.close(out);
         }
     }
 
     public static String removePathFromFilename(String filename) {
         if (Util.isEmpty(filename)) {
             return filename;
         }
         int slashIndex = filename.lastIndexOf("/");
         int backslashIndex = filename.lastIndexOf("\\");
         int index = Math.max(slashIndex, backslashIndex);
         if (index != -1) {
             return filename.substring(index + 1);
         }
         return filename;
     }
 
     public static String removePostfixFromFilename(String filename) {
         if (Util.isEmpty(filename)) {
             return filename;
         }
         int dotIndex = filename.lastIndexOf(".");
         if (dotIndex != -1) {
             return filename.substring(0, dotIndex);
         }
         return filename;
     }
 
     public static String prettyFilenameDateAndTime(Date date) {
         return new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss").format(date);
     }
 
     public static String prettyFilenameDate(Date date) {
         return new SimpleDateFormat("yyyy-MM-dd").format(date);
     }
 
     public static String prettyFilenameTime(Date date) {
         return new SimpleDateFormat("HH_mm_ss").format(date);
     }
 
     public static String toFilename(String str) {
         return str
                 .replaceAll("\\s", "_")
                 .replaceAll(":", "_");
     }
     
     public static String randomFilename(String dir) {
         String name = Integer.toString((int) (Math.random() * (float) Integer.MAX_VALUE));
         return addSlashToPath(dir) + name;
     }
 
     public static void copyFile(File sourceFile, File destFile) {
         try {
             if (!destFile.exists()) {
                 destFile.createNewFile();
             }
             FileChannel source = null;
             FileChannel destination = null;
 
             try {
                 source = new FileInputStream(sourceFile).getChannel();
                 destination = new FileOutputStream(destFile).getChannel();
                 destination.transferFrom(source, 0, source.size());
             } finally {
                 close(source);
                 close(destination);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public static void deleteFile(String filename) {
         File file = new File(filename);
         if (file.exists()) {
             file.delete();
         }
     }
 
     public static void deleteFilesInDir(String dir) {
         if (Util.isEmpty(dir)) {
             return;
         }
         File file = new File(dir);
         if (!file.exists()) {
             return;
         }
         for (File f : file.listFiles()) {
             f.delete();
         }
     }
 }
