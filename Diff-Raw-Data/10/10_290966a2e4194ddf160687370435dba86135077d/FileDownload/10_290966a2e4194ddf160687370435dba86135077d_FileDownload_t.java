 package com.dv.common.network.http.download;
 
 import java.io.DataInputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Handles downloading remote resources.
  * @author dan
  */
 public class FileDownload 
 {
     
     /**
      * Logger for this class
      */
     private static final Logger logger = Logger.getLogger(FileDownload.class.getName());
     
     /**
      * URL of the resource
      */
     private URL url;
     
     /**
      * Size of the remote resource
      */
     private int totalBytes;
     
     /**
      * The current byte that we are downloading. For example, if a file is 1000
      * bytes long, and this value is 25. Then we are what this means is we are
      * currently downloading byte 25 out of 1000.
      */
     private int currentByte;
     
     /**
      * Connection to the remote resource
      */
     private URLConnection connection;
     
     /**
      * Stream to the data of the remote resouce
      */
     private DataInputStream dis;
     
    /**
     * Download listeners. These are used for call backs while downloading files
     */
     private List<DownloadListener> downloadListeners;
     
     
    /**
      * Convienence function to downloads a file
      * @param url The url of the file
      * @return The data of the file
      * @throws Exception If something has gone wrong during the download process
      */
     public static byte[] download(String url) throws Exception
     {
         FileDownload fd = new FileDownload(url);
         return fd.download();
     }
     
     /**
      * Convienence function to downloads a file
      * @param url The url of the file
      * @return The data of the file
      * @throws Exception If something has gone wrong during the download process
      */
     public static byte[] download(URL url) throws Exception
     {
        FileDownload fd = new FileDownload(url);
        return fd.download();
     }
     
     
     /**
      * Constructor
      * @param url The url location of the resource to download
      * @throws Exception If something has gone wrong during the download process
      */
     public FileDownload(String url) throws Exception
     {
         this(new URL(url));
         downloadListeners = new ArrayList<DownloadListener>();
     }
     
     /**
      * Constructor
      * @param url URL that points to the resource to download
      * @throws Exception If something has gone wrong during the download process
      */
     public FileDownload(URL url) throws Exception
     {
         this.url = url;
         establishConnection();
         getStream();
         if(connection == null || dis == null)
         {
             String message = "Unable to download remote resource";
             logger.log(Level.WARNING, message);
             throw new Exception(message);
         }
     }
     
     /**
      * Downloads the data from the remote resource
      * @return The bytes that make up the remote resource or null if there is an
      * error
      */
     public byte[] download()
     {  
         byte[] data = new byte[totalBytes];
         for (int i = 0; i < data.length; i++) 
         {
             try 
             {
                 currentByte = i;
                 data[i] = dis.readByte();
 
                 //if there are listeners, see if the need to be notified
                if (downloadListeners != null && downloadListeners.isEmpty() == false)
                 {
                     notifyListeners();
                 }
             } 
             catch (Exception e) 
             {
                 logger.log(Level.SEVERE, "Could not read byte from url", e);
                 return null;
             }
         }
        if (downloadListeners != null) {
            for (DownloadListener listener : downloadListeners) {
                listener.done();
            }
         }
         return data;
     }
     
     /**
      * Gets the size of the remote file.
      * @return The size of the remote file
      */
     public int getTotalBytesOfFile()
     {
         return this.totalBytes;
     }
     
     /**
      * Gets the total number of bytes downloaded
      * @return The number of bytes that have been downloaded
      */
     public int getCurrentByteOfFile()
     {
         return this.currentByte;
     }
     
     /**
      * Adds a Download listener
      * @param listener The download listener to be added
      */
     public void addDownLoadListener(DownloadListener listener)
     {
         downloadListeners.add(listener);
     }
     
     /**
      * Removes a download listener
      * @param listener The download listener to be removed
      */
     public void removeDownloadListener(DownloadListener listener)
     {
         downloadListeners.remove(listener);
     }
     
     /**
      * Establishes a connection to the remote resource
      */
     private void establishConnection()
     {
         connection = null;
         try
         {
             connection = url.openConnection();
             if(connection == null)
             {
                 logger.log(Level.WARNING, "Could not open a connection to : " + url);
             }
             totalBytes = connection.getContentLength();
         }
         catch(Exception e)
         {
             logger.log(Level.SEVERE, "Could not open a connection to : " + url , e);
         }
     }
     
     /**
      * Gets a stream to the remote resource
      */
     private void getStream()
     {
         dis = null;
         try
         {
             dis = new DataInputStream(url.openStream());
             if(dis == null)
             {
                 logger.log(Level.WARNING, "Could not open a stream to url: " + url);
                 return;
             }
         }
         catch(Exception e)
         {
             logger.log(Level.SEVERE, "Could not open a stream to url: " + url, e);
         }
     }
     
     /**
      * Notifies any listeners that data has been retrieved
      */
     private void notifyListeners()
     {
         for (DownloadListener listener : downloadListeners) 
         {
             if (currentByte - listener.getLastNotified() >= listener.getNotifyInterval()) 
             {
                 DownloadEvent event = new DownloadEvent(currentByte, totalBytes);
                 listener.byteRecieved(event);
                 listener.setLastNotified(currentByte);
             }
         }
     }
     
     
     /**
      * Simple example of how to use this class
      * @param args Command line args
      * @throws Exception  If something goes wrong
      */
     public static void main(String [] args) throws Exception
     {
         FileDownload fd = new FileDownload("http://media.comicvine.com/uploads/7/71707/1438334-tuxedo_cat_ra.png");
         int size = fd.getTotalBytesOfFile();
         int x = (int)(size/(double)10.0); //so we get notified every 10 percent of the file downloaded
         fd.addDownLoadListener(new DownloadListener(x) {
             @Override
             public void byteRecieved(DownloadEvent e) {
                 System.out.println(e.getCurentByte() + " : " + e.getTotalBytes());
             }
             
             public void done()
             {
                 System.out.println("DONE");
             }
         });
         byte[] data = fd.download();
     }
 }
