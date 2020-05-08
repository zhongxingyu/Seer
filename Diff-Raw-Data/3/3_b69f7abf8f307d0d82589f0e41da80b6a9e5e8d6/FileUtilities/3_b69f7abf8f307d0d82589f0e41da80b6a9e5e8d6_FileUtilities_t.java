 package org.jboss.pressgang.ccms.utils.common;
 
 import java.awt.*;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A collection of static methods for working with local files.
  *
  * @author Matthew Casperson
  */
 public class FileUtilities {
     private static final Logger LOG = LoggerFactory.getLogger(FileUtilities.class);
 
     /**
      * @param file The file to be read
      * @return The contents of the file as a String
      */
     public static String readFileContents(final File file) {
         try {
            final byte[] contents = readFileContentsAsByteArray(file);
            return new String(contents, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * @param file The file to be read
      * @return The contents of the file as a byte array
      */
     public static byte[] readFileContentsAsByteArray(final File file) {
         if (!file.exists()) return null;
 
         InputStream is = null;
 
         try {
             is = new FileInputStream(file);
 
             // Get the size of the file
             final long length = file.length();
 
             // You cannot create an array using a long type.
             // It needs to be an int type.
             // Before converting to an int type, check
             // to ensure that file is not larger than Integer.MAX_VALUE.
             if (length > Integer.MAX_VALUE) {
                 throw new IOException("File is too large: " + file.getName());
             }
 
             // Create the byte array to hold the data
             final byte[] bytes = new byte[(int) length];
 
             // Read in the bytes
             int offset = 0;
             int numRead = 0;
             while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                 offset += numRead;
             }
 
             // Ensure all the bytes have been read in
             if (offset < bytes.length) {
                 throw new IOException("Could not completely read file " + file.getName());
             }
 
             return bytes;
         } catch (final Exception ex) {
             LOG.error("An error occurred while reading the file contents", ex);
         } finally {
             try {
                 if (is != null) is.close();
             } catch (final Exception ex) {
                 LOG.error("Failed to close the FileInputStream", ex);
             }
         }
 
         return null;
     }
 
     /**
      * Gets the extension for a file.
      *
      * @param fileName The name of the file. eg Image.jpg
      * @return The filename extension for the file if it has one, otherwise null.
      */
     public static String getFileExtension(final String fileName) {
         if (fileName == null) return null;
 
         final int lastPeriodIndex = fileName.lastIndexOf(".");
         // make sure there is an extension, and that the filename doesn't end with a period
         if (lastPeriodIndex != -1 && lastPeriodIndex < fileName.length() - 1) {
             final String extension = fileName.substring(lastPeriodIndex + 1);
             return extension;
         }
 
         return null;
     }
 
     /**
      * Copies a file or directory from one location to another location on a local system.<br /><br /> Note: The destination location must
      * exist, otherwise a IOException will be thrown.
      *
      * @param src  The Source File or Directory to be copied to the dest location.
      * @param dest The Destination location to copy the Source content to.
      * @throws IOException Thrown if the destination location doesn't exist or there write permissions for the destination.
      */
     public static void copyFile(final File src, final File dest) throws IOException {
         if (src.isDirectory()) {
             // if the directory does not exist, create it
             if (!dest.exists()) {
                 dest.mkdir();
             }
 
             // get all the directory contents
             final String files[] = src.list();
 
             // Copy all the folders/files in the directory
             for (final String file : files) {
                 // construct the src and dest file structure
                 final File srcFile = new File(src, file);
                 final File destFile = new File(dest, file);
                 // recursive copy
                 copyFile(srcFile, destFile);
             }
         } else {
             // if its a file, then copy it
             final InputStream in = new FileInputStream(src);
             final OutputStream out = new FileOutputStream(dest);
 
             byte[] buffer = new byte[1024];
 
             int length;
             // copy the file contents
             while ((length = in.read(buffer)) > 0) {
                 out.write(buffer, 0, length);
             }
 
             // Clean up
             in.close();
             out.close();
         }
     }
 
     /**
      * Delete a directory and all of its sub directories/files
      *
      * @param dir The directory to be deleted.
      * @return True if the directory was deleted otherwise false if an error occurred.
      */
     public static boolean deleteDir(final File dir) {
         // Delete the contents of the directory first
         if (!deleteDirContents(dir)) return false;
 
         // The directory is now empty so delete it
         return dir.delete();
     }
 
     /**
      * Delete the contents of a directory and all of its sub directories/files
      *
      * @param dir The directory whose content is to be deleted.
      * @return True if the directories contents were deleted otherwise false if an error occurred.
      */
     public static boolean deleteDirContents(final File dir) {
         if (dir.isDirectory()) {
             final String[] children = dir.list();
             for (final String aChildren : children) {
                 final File child = new File(dir, aChildren);
                 if (child.isDirectory()) {
                     // Delete the sub directories
                     if (!deleteDir(child)) {
                         return false;
                     }
                 } else {
                     // Delete a single file in the directory
                     if (!child.delete()) {
                         return false;
                     }
                 }
             }
         }
         return true;
     }
 
     /**
      * Opens a file using the OS default program for the file type, using the Java Desktop API.
      *
      * @param file The file to be opened.
      * @throws IOException Thrown if there is an issue opening the file.
      * @throws IllegalStateException Thrown if the Desktop API isn't supported.
      */
     public static void openFile(final File file) throws IOException {
         // Check that the file is a file
         if (!file.isFile()) throw new IOException("Passed file is not a file.");
 
         // Check that the Desktop API is supported
         if (!Desktop.isDesktopSupported()) {
             throw new IllegalStateException("Desktop is not supported");
         }
 
         final Desktop desktop = Desktop.getDesktop();
 
         // Check that the open functionality is supported
         if (!desktop.isSupported(Desktop.Action.OPEN)) {
             throw new IllegalStateException("Desktop doesn't support the open action");
         }
 
         // Open the file
         desktop.open(file);
     }
 
     /**
      * Save the data, represented as a String to a file
      *
      * @param file     The location/name of the file to be saved.
      * @param contents The data that is to be written to the file.
      * @throws IOException
      */
     public static void saveFile(final File file, final String contents, final String encoding) throws IOException {
         saveFile(file, contents.getBytes(encoding));
     }
 
     /**
      * Save the data, represented as a byte array to a file
      *
      * @param file         The location/name of the file to be saved.
      * @param fileContents The data that is to be written to the file.
      * @throws IOException
      */
     public static void saveFile(final File file, byte[] fileContents) throws IOException {
         if (file.isDirectory()) {
             throw new IOException("Unable to save file contents as a directory.");
         }
 
         final FileOutputStream fos = new FileOutputStream(file);
         fos.write(fileContents);
         fos.flush();
         fos.close();
     }
 }
