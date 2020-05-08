 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2013 Per Cederberg. All rights reserved.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the BSD license.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  * See the RapidContext LICENSE.txt file for more details.
  */
 
 package org.rapidcontext.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * A set of utility methods for handling files.
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public abstract class FileUtil {
 
     /**
      * The base temporary directory to use.
      */
     private static File tempDir = null;
 
     /**
     * Attempts to resolve a file to a canonical file.
     *
     * @param file           the file to resolve
     *
     * @return the canonical file, or
     *         the original file if the resolution failed
     */
    public static File canonical(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException ignore) {
            return file;
        }
    }

    /**
      * Copies a file or a directory. Directories are copied
      * recursively and file modification dates are kept. If the
      * executable bit is set on the source file, that bit will also
      * be set on the destination file.
      *
      * @param src            the source file or directory
      * @param dst            the destination file or directory
      *
      * @throws IOException if some file couldn't be copied
      */
     @SuppressWarnings("resource")
     public static void copy(File src, File dst) throws IOException {
         if (src.isDirectory()) {
             dst.mkdirs();
             File[] files = src.listFiles();
             for (int i = 0; i < files.length; i++) {
                 copy(files[i], new File(dst, files[i].getName()));
             }
         } else {
             copy(new FileInputStream(src), dst);
             dst.setExecutable(src.canExecute());
         }
         dst.setLastModified(src.lastModified());
     }
 
     /**
      * Copies the data from an input stream to a destination file.
      * If the file exists, it will be overwritten. The destination
      * file parent directory must have been created or the copy
      * will fail. The input stream will be closed by this function.
      *
      * @param is             the input stream to read
      * @param dst            the destination file
      *
      * @throws IOException if the input stream couldn't be read or if
      *             the destination file couldn't be written
      */
     @SuppressWarnings("resource")
     public static void copy(InputStream is, File dst) throws IOException {
         copy(is, new FileOutputStream(dst));
     }
 
     /**
      * Copies the data from an input stream to an output stream. Both
      * the streams will be closed when this function returns.
      *
      * @param is             the input stream to read
      * @param os             the output stream to write
      *
      * @throws IOException if the input stream couldn't be read or if
      *             the output stream couldn't be written
      */
     public static void copy(InputStream is, OutputStream os)
     throws IOException {
 
         byte[]  buffer = new byte[16384];
         int     size;
 
         try {
             do {
                 size = is.read(buffer);
                 if (size > 0) {
                     os.write(buffer, 0, size);
                 }
             } while (size > 0);
         } finally {
             try {
                 is.close();
             } catch (IOException ignore) {
                 // Do nothing
             }
             try {
                 os.close();
             } catch (IOException ignore) {
                 // Do nothing
             }
         }
     }
 
     /**
      * Deletes a file or a directory. This function will delete all
      * files and sub-directories inside a directory recursively.
      *
      * @param file           the file or directory to delete
      *
      * @throws IOException if some files couldn't be deleted
      *
      * @see #deleteFiles(File)
      */
     public static void delete(File file) throws IOException {
         if (file != null && file.isDirectory()) {
             deleteFiles(file);
         }
         if (file != null && !file.delete()) {
             throw new IOException("failed to delete " + file);
         }
     }
 
     /**
      * Deletes all files in a directory, but leaving the directory
      * otherwise unmodified. This function will delete any
      * sub-directories recursively.
      *
      * @param dir            the directory to clean
      *
      * @throws IOException if some files couldn't be deleted
      *
      * @see #delete(File)
      */
     public static void deleteFiles(File dir) throws IOException {
         if (dir != null && dir.isDirectory()) {
             File[] files = dir.listFiles();
             if (files != null) {
                 for (int i = 0; i < files.length; i++) {
                     delete(files[i]);
                 }
             }
         }
     }
 
     /**
      * Deletes all empty directories in a directory, but leaves the
      * directory itself unmodified. This method will remove any empty
      * directories recursively, making it possible to remove a tree
      * of empty directories.
      *
      * @param dir            the directory to clean
      *
      * @throws IOException if some files couldn't be deleted
      *
      * @see #deleteFiles(File)
      */
     public static void deleteEmptyDirs(File dir) throws IOException {
         File[] files = dir.listFiles();
         if (files != null) {
             for (int i = 0; i < files.length; i++) {
                 if (files[i].isDirectory()) {
                     deleteEmptyDirs(files[i]);
                     File[] subfiles = files[i].listFiles();
                     if (subfiles == null || subfiles.length == 0) {
                         delete(files[i]);
                     }
                 }
             }
         }
     }
 
     /**
      * Creates a new temporary directory. If a temporary directory
      * has been set, it will be used. Otherwise the default temporary
      * directory will be used.
      *
      * @param prefix         the directory name prefix
      * @param suffix         the directory name suffix
      *
      * @return a new empty temporary directory
      *
      * @throws IOException if the temporary directory couldn't be created
      */
     public static File tempDir(String prefix, String suffix) throws IOException {
         File dir = File.createTempFile(prefix, suffix, tempDir);
         dir.delete();
         dir.mkdir();
         dir.deleteOnExit();
         return dir;
     }
 
     /**
      * Sets the temporary directory to use.
      *
      * @param dir            the new temporary directory
      */
     public static void setTempDir(File dir) {
         tempDir = dir;
     }
 
     /**
      * Creates a new temporary file. If a temporary directory has
      * been set, it will be used. Otherwise the default temporary
      * directory will be used. The generated file is guaranteed to
      * keep the same file extension (suffix) as the specified one.
      *
      * @param prefix         the file name prefix
      * @param suffix         the file name suffix (extension)
      *
      * @return a new empty temporary file
      *
      * @throws IOException if the temporary file couldn't be created
      */
     public static File tempFile(String prefix, String suffix) throws IOException {
         File  file = null;
 
         if (tempDir != null) {
             file = new File(tempDir, prefix + suffix);
             if (file.exists()) {
                 file = null;
             } else {
                 file.createNewFile();
             }
         }
         if (file == null) {
             file = File.createTempFile(prefix, suffix, tempDir);
         }
         file.deleteOnExit();
         return file;
     }
 
     /**
      * Creates a new temporary file. If a temporary directory has
      * been set, it will be used. Otherwise the default temporary
      * directory will be used. The generated file is guaranteed to
      * keep the same file extension as the desired file name.
      *
      * @param name           the desired file name
      *
      * @return a new empty temporary file
      *
      * @throws IOException if the temporary file couldn't be created
      */
     public static File tempFile(String name) throws IOException {
         String  prefix = StringUtils.substringBeforeLast(name, ".");
         String  suffix = StringUtils.substringAfterLast(name, ".");
         File    file = null;
 
         if (prefix == null || prefix.length() <= 0) {
             prefix = "file";
         }
         if (suffix != null && suffix.length() > 0) {
             suffix = "." + suffix;
         }
         if (tempDir != null) {
             file = new File(tempDir, name);
             if (file.exists()) {
                 file = null;
             } else {
                 file.createNewFile();
             }
         }
         if (file == null) {
             file = File.createTempFile(prefix, suffix, tempDir);
         }
         file.deleteOnExit();
         return file;
     }
 }
