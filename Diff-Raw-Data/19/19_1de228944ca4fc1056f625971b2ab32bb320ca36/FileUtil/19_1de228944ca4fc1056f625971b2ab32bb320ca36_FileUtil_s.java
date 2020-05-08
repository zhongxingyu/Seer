 package de.skuzzle.polly.installer.util;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipOutputStream;
 
 
 public class FileUtil {
     
     public final static FileFilter ALL_FILES = new FileFilter() {
         @Override
         public boolean accept(File pathname) {
             return true;
         }
     };
     
     
     
     public final static FileFilter BACKUP_FILES = new FileFilter() {
         @Override
         public boolean accept(File pathname) {
             return pathname.isDirectory() || pathname.getName().endsWith(".bak");
         }
     };
     
 
     private FileUtil() {}
     
     
     
     /**
      * Extracts a zip file into a given folder.
      * 
      * @param zip
      *          The zip file to extract.
      * @param to
      *          The destination. This must be a directory.
      * @return
      *          A list of all extracted files.
      * @throws ZipException
      *          If extracting fails.
      * @throws IOException
      *          If destination is no directory or an IO error occurs during extraction. 
      */
     public static List<File> unzip (File zip, File to) throws ZipException, IOException {
         if (to.exists() && !to.isDirectory()) {
             throw new IOException("destination must be a directory");
         } else if (!to.exists()) {
             to.mkdirs();
         }
         
         List<File> result = new LinkedList<File>();
         ZipFile zipFile = new ZipFile(zip);
         
         Enumeration<? extends ZipEntry> entries = zipFile.entries();
         
         while (entries.hasMoreElements()) {
             ZipEntry entry = entries.nextElement();
             
             BufferedInputStream in =  new BufferedInputStream(
                 zipFile.getInputStream(entry));
             
             File next = new File(to, entry.getName());
             result.add(next);
             if (!next.isDirectory()) {
                 next.getParentFile().mkdirs();
             }
             
             if (entry.isDirectory()) {
                 next.mkdirs();
                 continue;
             }
             BufferedOutputStream o = new BufferedOutputStream(
                 new FileOutputStream(next));
 
             byte[] buffer = new byte[2048];
             int bytes = in.read(buffer);
             while (bytes != -1) {
                 o.write(buffer, 0, bytes);
                 bytes = in.read(buffer);
             }
             o.flush();
             o.close();
             in.close();
         }
         zipFile.close();
         return result;
     }
     
     
     
     public static void zip(List<File> files, File destination) throws IOException {
         if (destination.isDirectory()) {
             throw new IOException("destination must not be a directory");
         }
         ZipOutputStream out = new ZipOutputStream(new FileOutputStream(destination));
         
         for (File file : files) {
             FileUtil.zip(file, out);
         }
         
         out.flush();
         out.close();
     }
     
     
     
     public static void zip(File source, File destination) throws IOException {
         if (!source.exists()) {
             throw new FileNotFoundException(source.getAbsolutePath());
         } else if (!source.isDirectory()) {
             throw new IOException("source must be a directory");
         }
         FileUtil.zip(Arrays.asList(source.listFiles()), destination);
     }
     
     
     
     private static void zip(File source, ZipOutputStream out) throws IOException {
         if (source.isDirectory()) {
             for (File file : source.listFiles()) {
                 FileUtil.zip(file, out);
             }
         } else {
             byte[] buffer = new byte[1024];
             FileInputStream in = new FileInputStream(source);
             out.putNextEntry(new ZipEntry(source.getName()));
             
             int len;
             while ((len = in.read(buffer)) > 0) {
                 out.write(buffer, 0, len);
             }
             out.closeEntry();
             in.close();
         }
     }
     
     
     
     public static void deleteFilesRecursive(File destination, FileFilter filter, boolean backup) {
         if (destination.isDirectory()) {
             for (File f : destination.listFiles(filter)) {
                 FileUtil.deleteFilesRecursive(f, filter, backup);
             }
         } else {
             destination.delete();
             destination.deleteOnExit();
         }
     }
 
     
     
     public static void deleteRecursive(File destination) {
         FileUtil.deleteRecursive(destination, FileUtil.ALL_FILES);
     }
    
     
     
     public static void deleteRecursive(File destination, FileFilter filter) {
         if (destination.isDirectory()) {
             for (File f : destination.listFiles(filter)) {
                 FileUtil.deleteRecursive(f, filter);
             }
         }
         destination.delete();
         destination.deleteOnExit();
     }
     
     
     
     public static List<File> copyContent(File source, File destination) throws IOException {
         if (!source.isDirectory()) {
             throw new IOException("source must be a directory");
         }
         List<File> sources = Arrays.asList(source.listFiles());
         return FileUtil.copy(sources, destination);
     }
     
     
     
     public static List<File> copy(List<File> sources, File destination) 
             throws IOException {
         if (destination.exists() && !destination.isDirectory()) {
             throw new IOException("destination must be a directory");
         }
         List<File> result = new LinkedList<File>();
         for (File file : sources) {
             result.addAll(FileUtil.copy(file, destination));
         }
         return result;
     }
     
     
     
     public static List<File> copy(File source, File destination) throws IOException {
         return FileUtil.copy(source, destination, FileUtil.ALL_FILES);
     }
     
     
     
     public static List<File> copy(File source, File destination, FileFilter filter) 
                 throws IOException {
         if (source.isDirectory() && destination.exists() && !destination.isDirectory()) {
             throw new IOException("Can not move a directory into a file");
         } else if (!source.isDirectory() && destination.isDirectory()) {
             destination = new File(destination, source.getName());
         } /*else if (!source.isDirectory() && !destination.isDirectory()) {
             String path = destination.getAbsolutePath();
             path = path.substring(0, path.length() - destination.getName().length());
             destination = new File(path);
         }*/
         
         List<File> copiedFiles = new LinkedList<File>();
         FileUtil.copy(source, destination, filter, copiedFiles);
         return copiedFiles;
     }
     
     
     
     private static void copy(File source, File destination, FileFilter filter, 
             List<File> copied) throws IOException {
         if (source.isDirectory()) {
             //if directory not exists, create it
            if(!destination.exists()) {
                destination.mkdir();
            }
 
            //list all the directory contents
            File[] files = source.listFiles(filter);
 
            for (File file : files) {
               //construct the src and dest file structure
               File srcFile = new File(source, file.getName());
               File destFile = new File(destination, file.getName());
               //recursive copy
               FileUtil.copy(srcFile, destFile, filter, copied);
            }
 
        } else {
            //if file, then copy it
            //Use bytes stream to support all file types
            InputStream in = new FileInputStream(source);
            OutputStream o = new FileOutputStream(destination); 
 
            byte[] buffer = new byte[1024];
 
            int length;
            //copy the file content in bytes 
            while ((length = in.read(buffer)) > 0){
               o.write(buffer, 0, length);
            }
            o.flush();
            in.close();
            o.close();
        }
        copied.add(destination);
     }
     
     
     
     public static boolean waitFor(String file) {
         return FileUtil.waitFor(new File(file), 4);
     }
     
     
     
     public static boolean waitFor(File file, int attempts) {
         int i = 0;
         while (file.exists() && !file.canWrite() && i++ < attempts) {
             try {
                 Thread.sleep(250);
             } catch (InterruptedException ignore) {}
         }
         
         return !file.exists() || file.canWrite();
     }
     
     
     
     public static File createTempDirectory(String prefix, String suffix) 
                 throws IOException {
         final File temp = File.createTempFile(prefix, suffix);
 
         if(!(temp.delete())) {
             throw new IOException("Could not delete temp file: " + 
                 temp.getAbsolutePath());
         }
 
         if(!(temp.mkdir())) {
             throw new IOException("Could not create temp directory: " + 
                 temp.getAbsolutePath());
         }
         return temp;
     }
     
     
     
     public static File createTempDirectory() throws IOException {
         return FileUtil.createTempDirectory("polly_update_", 
             Long.toString(System.nanoTime()));
     }
     
     
     
     public static String escapePath(String path) {
         return path.replace('\\', File.separatorChar).replace('/', File.separatorChar);
     }
     
     
     
     public static File backupFor(File file) throws IOException {
         if (file.isDirectory()) {
             return FileUtil.createTempDirectory("polly_backup_", 
                 Long.toString(System.nanoTime()));
         } else {
             return File.createTempFile("polly_backup_", 
                 Long.toString(System.nanoTime()));
         }
     }
     
     
     
     public static void listFolder(File folder, FileAction action) throws IOException {
         FileUtil.listFolder(folder, FileUtil.ALL_FILES, action);
     }
 
     
     
     public static void listFolder(File folder, FileFilter filter, FileAction action) throws IOException {
         action.exec(folder);
         if (folder.isDirectory()) {
             for (File file : folder.listFiles(filter)) {
                 listFolder(file, filter, action);
             }
         }
     }
     
     
     
    public static void deleteList(List<File> files) {
        boolean exists = false;
         for (File file : files) {
             if (!file.isDirectory() && file.exists()) {
                 file.deleteOnExit();
                exists |= !file.delete();
             }
         }
         for (File file : files) {
             if (file.isDirectory() && file.exists()) {
                 file.deleteOnExit();
                 file.delete();
                exists |= !file.delete();
             }
         }
     }
     
     
     
     public static void safeDeletePaths(List<String> paths, int retries) {
         List<File> files = new LinkedList<File>();
         for (String path : paths) {
             files.add(new File(path));
         }
         FileUtil.safeDeleteList(files, retries);
     }
     
     
     
     public static void safeDeleteList(List<File> files, int retries) {
         for (int i = 0; i < retries; ++i) {
             FileUtil.deleteList(files);
         }
     }
     
     
     
     public static File getDirectory(File file) {
         if (file.isDirectory()) {
             return file;
         } else {
             String path = file.getAbsolutePath();
             path = path.substring(0, path.length() - file.getName().length());
             return new File(path);
         }
     }
 }
