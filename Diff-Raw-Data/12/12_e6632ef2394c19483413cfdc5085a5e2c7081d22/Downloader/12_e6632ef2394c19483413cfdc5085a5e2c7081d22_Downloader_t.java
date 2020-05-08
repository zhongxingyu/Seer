 package com.brotherlogic.beer.actions;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 
 public class Downloader
 {
    private static Downloader singleton;
 
    public synchronized static Downloader getInstance()
    {
       if (singleton == null)
          singleton = new Downloader();
       return singleton;
    }
 
    int dlCount = 1;
 
    // Wait one second between downloads
    long lastDownload = 0;
 
   long lastDownloadFail = 0;

    long WAIT_TIME = 1000;
 
    /**
     * Blocking constructor
     */
    private Downloader()
    {
 
    }
 
    public String download(String url)
    {
       System.out.println("Download: " + url);
 
       long timeToWait = WAIT_TIME - (System.currentTimeMillis() - lastDownload);
       if (timeToWait > 0)
          try
          {
             Thread.sleep(timeToWait);
          }
          catch (InterruptedException e)
          {
             e.printStackTrace();
          }
 
       try
       {
         // Return null if we've broken the untappd servers
         if (System.currentTimeMillis() - 1000 * 60 * 60 < lastDownloadFail)
         {
            System.out.println("Untappd Broken!");
            return null;
         }

          StringBuffer buffer = new StringBuffer();
          BufferedReader reader = new BufferedReader(
                new InputStreamReader(new URL(url).openStream()));
          for (String line = reader.readLine(); line != null; line = reader.readLine())
             buffer.append(line);
          lastDownload = System.currentTimeMillis();
 
          return buffer.toString();
       }
       catch (IOException e)
       {
          // e.printStackTrace();
       }
 
      lastDownloadFail = System.currentTimeMillis();
       return null;
    }
 
 }
