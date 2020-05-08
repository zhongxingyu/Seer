 package com.sohail.alam.http.server;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import static com.sohail.alam.http.common.LoggerManager.LOGGER;
 
 /**
  * User: Sohail Alam
  * Version: 1.0.0
  * Date: 21/9/13
  * Time: 8:37 PM
  */
 public class LocalFileFetcher {
     public static final LocalFileFetcher FETCHER = new LocalFileFetcher();
 
     /**
      * Instantiates a new Local file fetcher.
      */
     private LocalFileFetcher() {
         LOGGER.trace("LocalFileFetcher Constructor Initialized");
     }
 
     /**
      * Normalize a given relative path String to get the actual path String
      *
      * @param path the path
      *
      * @return the normalized path
      */
     private String normalizePath(String path) {
         String normalizedPath = new String();
         // Make Sure Path Starts with a slash (/)
         if (!path.startsWith("/")) {
             path = "/" + path;
         }
         // Make sure path does not ends in "/"
         if (path.endsWith("/")) {
             path = path.substring(0, path.lastIndexOf("/"));
         }
         // ./www/somePath
         if (path.startsWith("/" + ServerProperties.PROP.webappPath())) {
             normalizedPath = "." + path;
         } else {
             normalizedPath = "./www" + path;
         }
         LOGGER.debug("Normalizing Path '{}' to '{}'", path, normalizedPath);
         return normalizedPath;
     }
 
     /**
      * Get bytes of the file.
      *
      * @param path     the relative path of the file, which will be normalized automatically
      * @param callback the callback
      *
      * @return the byte[] containing the file data
      */
     public <T extends LocalFileFetcherCallback> void fetch(String path, T callback) {
         final byte[] fileBytes;
         final File file = new File(this.normalizePath(path));
         FileInputStream is = null;
         try {
             is = new FileInputStream(file);
             fileBytes = new byte[is.available()];
             is.read(fileBytes);
             LOGGER.debug("File '{}' Fetched Successfully - {} bytes", path, fileBytes.length);
             callback.fetchSuccess(path, fileBytes);
         } catch (FileNotFoundException e) {
             LOGGER.debug("Exception Caught: {}", e.getMessage());
            callback.fileNotFound(path, e);
         } catch (IOException e) {
             LOGGER.debug("Exception Caught: {}", e.getMessage());
             callback.exceptionCaught(path, e);
         } finally {
             if (is != null) {
                 try {
                     is.close();
                 } catch (IOException ignored) {
                     LOGGER.debug("IO Exception Caught while closing Input Stream in finally, Nothing can be done here");
                 }
             }
         }
     }
 
     /**
      * The interface Local file fetcher callback which must be used to receive callbacks
      */
     public interface LocalFileFetcherCallback {
         /**
          * Fetch success is called when a file is read successfully and
          * the data is ready to be delivered.
          *
          * @param path the path from which the file was read (Normalized Path)
          * @param data the data as byte array
          */
         public void fetchSuccess(String path, byte[] data);
 
         /**
          * Fetch failed is called whenever there was an error reading the file
          *
          * @param path  the path from which the file was to be read (Normalized Path)
          * @param cause the throwable object containing the cause
          */
         public void fileNotFound(String path, Throwable cause);
 
 
         /**
          * Exception caught.
          *
          * @param path  the path
          * @param cause the cause
          */
         public void exceptionCaught(String path, Throwable cause);
     }
 }
