 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.shared;
 
 import com.google.common.collect.Lists;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.*;
 import java.nio.ByteBuffer;
 import java.nio.channels.Channels;
 import java.nio.channels.FileChannel;
 import java.nio.channels.ReadableByteChannel;
 import java.nio.channels.WritableByteChannel;
 import java.util.List;
 
 
 /**
  * Utilities for file system access
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @since 3.1
  */
 public class FxFileUtils {
     private static final Log LOG = LogFactory.getLog(FxFileUtils.class);
 
     /**
      * Expand the path by replacing '~' with the user home directory and fix file separator chars
      *
      * @param path the path to expand
      * @return expanded path
      */
     public static String expandPath(String path) {
         //fix file separators
         if (File.separatorChar == '/')
             path = path.replace('\\', '/');
         else
             path = path.replace('/', '\\');
         //expand ~ to user home
         if (path.indexOf("~") >= 0)
             path = path.replaceAll("~", System.getProperty("user.home"));
         return path;
     }
 
     /**
      * Remove the given file
      *
      * @param fileName file to remove
      */
     public static void removeFile(String fileName) {
         if (StringUtils.isEmpty(fileName))
             return;
         removeFile(new File(fileName));
     }
 
     /**
      * Remove the given file
      *
      * @param file file to remove
      */
     public static void removeFile(File file) {
         if (file == null || !file.exists())
             return;
         if (!file.delete())
             file.deleteOnExit();
     }
 
     /**
      * Copy data from source to destination nio channel
      *
      * @param source      source channel
      * @param destination destination channel
      * @return total number of bytes copied
      * @throws java.io.IOException on errors
      */
     public static long copyNIOChannel(ReadableByteChannel source, WritableByteChannel destination) throws IOException {
         ByteBuffer xferBuffer = ByteBuffer.allocateDirect(4096);
         long count = 0, read, written;
         while (true) {
             read = source.read(xferBuffer);
             if (read < 0)
                 return count;
             xferBuffer.flip();
             written = destination.write(xferBuffer);
             if (written > 0) {
                 count += written;
                 if (xferBuffer.hasRemaining())
                     xferBuffer.compact();
                 else
                     xferBuffer.clear();
             } else {
                 while (xferBuffer.hasRemaining()) {
                     try {
                         Thread.sleep(5);
                     } catch (InterruptedException e) {
                         LOG.warn(e);
                     }
                     written = destination.write(xferBuffer);
                     if (written > 0) {
                         count += written;
                         if (xferBuffer.hasRemaining())
                             xferBuffer.compact();
                     }
                 }
                 if (!xferBuffer.hasRemaining())
                     xferBuffer.clear();
             }
         }
     }
 
     /**
      * Copy a file
      *
      * @param source      source file
      * @param destination destination file
      * @return success
      */
     public static boolean copyFile(File source, File destination) {
         FileChannel sourceChannel = null;
         FileChannel destinationChannel = null;
         try {
             sourceChannel = new FileInputStream(source).getChannel();
             destinationChannel = new FileOutputStream(destination).getChannel();
             // don't use transferTo because it fails for large files under windows - http://bugs.sun.com/view_bug.do?bug_id=6431344
             return copyNIOChannel(sourceChannel, destinationChannel) == source.length() &&
                     destination.length() == source.length();
         } catch (IOException e) {
             LOG.error(e, e);
             return false;
         } finally {
             try {
                 if (sourceChannel != null) sourceChannel.close();
             } catch (IOException e) {
                 LOG.error(e);
             }
             try {
                 if (destinationChannel != null) destinationChannel.close();
             } catch (IOException e) {
                 LOG.error(e);
             }
         }
     }
 
     /**
      * Copy the content of an InputStream to a file
      *
      * @param expectedSize    expected size of the stream
      * @param sourceStream    source
      * @param destinationFile destination
      * @return copy was successful and sizes match
      */
     public static boolean copyStream2File(long expectedSize, InputStream sourceStream, File destinationFile) {
         ReadableByteChannel sourceChannel = null;
         FileChannel destinationChannel = null;
         try {
             sourceChannel = Channels.newChannel(sourceStream);
             destinationChannel = new FileOutputStream(destinationFile).getChannel();
             return copyNIOChannel(sourceChannel, destinationChannel) == expectedSize &&
                     destinationFile.length() == expectedSize;
         } catch (IOException e) {
             LOG.error(e, e);
             return false;
         } finally {
             try {
                 if (sourceChannel != null) sourceChannel.close();
             } catch (IOException e) {
                 LOG.error(e);
             }
             try {
                 if (destinationChannel != null) destinationChannel.close();
             } catch (IOException e) {
                 LOG.error(e);
             }
         }
     }
 
     /**
      * Remove a directory and all its sub directories and files
      *
      * @param dir directory to remove recursively
      */
     public static void removeDirectory(String dir) {
         if (StringUtils.isEmpty(dir))
             return;
         removeDirectory(new File(dir));
     }
 
     /**
      * Remove a directory and all its sub directories and files
      *
      * @param dir directory to remove recursively
      */
     public static void removeDirectory(File dir) {
         if (dir == null || !dir.exists())
             return;
         if (dir.isDirectory())
             for (File file : dir.listFiles())
                 removeDirectory(file);
         removeFile(dir);
     }
 
     /**
      * Compare if two files match
      *
      * @param file1 first file to compare
      * @param file2 second file to compare
      * @return match
      */
     public static boolean fileCompare(File file1, File file2) {
         return file2.length() == file1.length();
     }
 
     /**
      * Load a file into a byte array
      *
      * @param file the file to load
      * @return byte[]
      * @throws IOException on errors
      */
     public static byte[] getBytes(File file) throws IOException {
         InputStream is = null;
         if (file.length() > Integer.MAX_VALUE)
             throw new IOException("File " + file.getAbsolutePath() + " is too large!");
 
         byte[] bytes = new byte[(int) file.length()];
         try {
             is = new FileInputStream(file);
 
             int curr = 0;
             int read;
             while (curr < bytes.length && (read = is.read(bytes, curr, bytes.length - curr)) >= 0)
                 curr += read;
             if (curr < bytes.length)
                 throw new IOException("Failed to fully read " + file.getAbsolutePath() + "!");
         } finally {
             if (is != null) is.close();
         }
         return bytes;
     }
 
     /**
      * Load a file and base64 encode it
      *
      * @param file the file to load
      * @return base64 encoded file content
      * @throws IOException on errors
      */
     public static String loadBase64Encoded(File file) throws IOException {
         if (file == null || !file.exists() || file.length() == 0)
             return "";
         return FxFormatUtils.encodeBase64(getBytes(file));
     }
 
     /**
      * List the contents of the given directory recursively.
      *
      * @param root  the root directory
      * @return      the contents of the directory
      * @since 3.1
      */
     public static List<File> listRecursive(File root) {
         final List<File> result = Lists.newArrayList();
         for (File file : root.listFiles()) {
             if (file.isDirectory()) {
                 result.addAll(listRecursive(file));
             } else {
                 result.add(file);
             }
         }
         return result;
     }
 }
