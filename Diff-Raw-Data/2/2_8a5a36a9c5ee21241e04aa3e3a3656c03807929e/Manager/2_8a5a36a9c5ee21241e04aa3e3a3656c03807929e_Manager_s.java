 package org.cloudname.copkg;
 
 import org.cloudname.copkg.util.Unzip;
 import org.cloudname.copkg.util.Traverse;
 
 import com.ning.http.client.Response;
 import com.ning.http.client.SimpleAsyncHttpClient;
 import com.ning.http.client.consumers.FileBodyConsumer;
 import com.ning.http.client.simple.HeaderMap;
 import com.ning.http.client.simple.SimpleAHCTransferListener;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.nio.ByteBuffer;
 import java.util.concurrent.ExecutionException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.concurrent.Future;
 
 /**
  * Package Manager - provides methods for downloading, unpacking and
  * installing copkg packages.
  *
  * TODO(borud): things here throw exceptions willy-nilly.  This has to
  *   be cleaned up.  It is okay for the command line utility, but as
  *   part of other programs this won't do.
  *
  * @author borud
  */
 public class Manager {
     private static final Logger log = Logger.getLogger(Manager.class.getName());
 
     private static final int REQUEST_TIMEOUT_MS = (5 * 60 * 1000);
     private static final int MAX_RETRY_ON_IOEXCEPTION = 5;
     private static final int MAX_CONNECTIONS_PER_HOST = 3;
     private static final int MAX_NUM_REDIRECTS = 3;
 
     private static final String UNPACK_DIR_SUFFIX = "unpack";
     private static final String REMOVE_DIR_SUFFIX = "remove";
 
     private Configuration config;
 
     /**
      * Create a package manager for a given base package directory.
      *
      * @param config the configuration for the package manager.
      */
     public Manager (final Configuration config) {
         this.config = config;
     }
 
     /**
      * Download package into the download directory.
      *
      * <p>For library use this method needs a better API for
      * communicating back a bit more than just the return code.
      *
      * @param coordinate Package Coordinate of the package we wish to download.
      * @throws Exception fails on any and all exceptions.
      */
     public int download(PackageCoordinate coordinate) throws Exception {
         final String downloadFilename = config.downloadFilenameForCoordinate(coordinate);
 
         final File destinationFile = new File(downloadFilename);
         final File destinationDir = destinationFile.getParentFile();
 
         // Ensure directories exist
         destinationDir.mkdirs();
 
         final String url = coordinate.toUrl(config.getPackageBaseUrl());
 
         log.fine("destination dir  = " + destinationDir.getAbsolutePath());
         log.fine("destination file = " + destinationFile.getAbsolutePath());
 
         SimpleAHCTransferListener listener = new SimpleAHCTransferListener() {
                 private long last = System.currentTimeMillis();
 
                 @Override public void onBytesReceived(String url, long amount, long current, long total) {
                     // Only output progress once every 1000 milliseconds
                     long now = System.currentTimeMillis();
                     if ((now - last) > 1000) {
                         last = now;
                         long percent = (amount * 100) / total;
                         log.info(" - Received " + amount + " of " + total + " bytes (" + percent + "%)");
                     }
                 }
 
                 @Override public void onBytesSent(String url, long amount, long current, long total) {}
                 @Override public void onCompleted(String url, int statusCode, String statusText) {}
                 @Override public void onHeaders(String url, HeaderMap headers) {}
                 @Override public void onStatus(String url, int statusCode, String statusText) {}
             };
 
         // Make client
         SimpleAsyncHttpClient client = new SimpleAsyncHttpClient.Builder()
             .setRequestTimeoutInMs(REQUEST_TIMEOUT_MS)
             .setFollowRedirects(true)
             .setCompressionEnabled(true)
             .setMaximumNumberOfRedirects(MAX_NUM_REDIRECTS)
             .setMaxRequestRetry(MAX_RETRY_ON_IOEXCEPTION)
             .setMaximumConnectionsPerHost(MAX_CONNECTIONS_PER_HOST)
             .setUrl(url)
             .setListener(listener)
             .build();
 
         try {
             Response response = client.get(new FileBodyConsumer(new RandomAccessFile(destinationFile, "rw"))).get();
 
             // If the response code indicates anything other than 200 we
             // will end up with a file that contains junk.  We have to
             // make sure we delete it.
             if (response.getStatusCode() != 200) {
                 destinationFile.delete();
                 log.warning("Download failed. Status = " + response.getStatusCode() + ", msg = " + response.getStatusText());
             }
             return response.getStatusCode();
 
         } finally {
             client.close();
         }
     }
 
     /**
      * Install a package given by coordinate.
      *
      * @param coordinate the coordinate of the package we wish to
      *   install.
      * @throws Exception fails on any and all exceptions.
      */
     public void install(PackageCoordinate coordinate) throws Exception {
         File targetDir = new File(config.getPackageDir()
                                   + File.separatorChar
                                   + coordinate.getPathFragment());
 
         // If the target directory exists, we assume the package is
         // installed and bail early
         if (targetDir.exists()) {
             log.warning("Target dir " + targetDir.getAbsolutePath() + " exists.  Already installed?");
             return;
         }
 
         // Fetch the file from the package repository
         int response = download(coordinate);
         if (response != 200) {
             log.warning("Download failed with code HTTP response " + response + " for " + coordinate);
             return;
         }
 
         // Make sure we have the download file
         File downloadFile = new File(config.downloadFilenameForCoordinate(coordinate));
         if (! downloadFile.exists()) {
             log.warning("Couldn't find downloaded file " + downloadFile.getAbsolutePath());
             return;
         }
 
         // Create a directory for unpacking.
         //
         // TODO(borud): this is where we want to add some form of
         // dotlocking later to make it possible to run concurrent
         // installs from different processes as long as they operate
         // on different packages.  This is preferable to having a
         // master lock.
         File unpackDir = new File(targetDir.getAbsolutePath()
                                   + "---"
                                   + UNPACK_DIR_SUFFIX);
 
         log.fine("Unpacking " + downloadFile + " into " + unpackDir);
         unpackDir.mkdirs();
 
         // Now unzip the file into the unpack dir
         Unzip.unzip(downloadFile, unpackDir);
 
         // Move into place.  On unixen this is atomic.
         if (! unpackDir.renameTo(targetDir)) {
             log.warning("Unable to rename from " + unpackDir.getAbsolutePath()
                         + " to " + targetDir.getAbsolutePath());
             return;
         }
 
         log.info("Installed " + coordinate.toString() + " into " + targetDir);
     }
 
     /**
      * Uninstall package.  Renames the enclosing directory before
      * proceeding to delete it so that the directory is not allowed to
      * exist in a half removed state (recursive delete of directory
      * trees is not atomic).  Removes as much of the path as possible.
      *
      * @param coordinate the coordinate we wish to uninstall
      */
     public void uninstall(PackageCoordinate coordinate) throws Exception {
         File targetDir = new File(config.getPackageDir()
                                   + File.separatorChar
                                   + coordinate.getPathFragment());
 
         File removeDir = new File(targetDir.getAbsolutePath()
                                   + "---"
                                   + REMOVE_DIR_SUFFIX);
 
         // First check if there is a leftover removeDir and nuke it
         if (removeDir.exists()) {
             log.info("Found incomplete uninstall.  Cleaning up.");
             new Traverse() {
                 @Override public void after(final File f) {
                     f.delete();
                     log.info("Removed " + f.getAbsolutePath());
                 }
             };
         }
 
         if (! targetDir.exists()) {
             log.warning("Target dir " + targetDir.getAbsolutePath() + " does not exist");
             return;
         }
 
         // Renaming is atomic which means we can rename and then take
         // our time removing the files recursively.
         if (! targetDir.renameTo(removeDir)) {
             log.warning("Unable to rename " + targetDir.getAbsolutePath() + " to " + removeDir.getAbsolutePath());
         }
 
         // Traverse the directory and recursively delete everything in it.
         new Traverse() {
             @Override public void after(final File f) {
                 if (! f.delete()) {
                     log.warning("Failed to delete " + f.getAbsolutePath());
                     return;
                 }
                log.info("Deleted " + f.getAbsolutePath());
             }
         }.traverse(removeDir);
 
         log.info("Uninstalled " + coordinate.toString());
     }
 }
