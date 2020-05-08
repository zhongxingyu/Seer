 package org.joushou.FiveProxy;
 
 import java.lang.String;
 
 class Settings {
   public static final int listenPort = 4001; // Port to listen for clients on
   public static final String username = "fiveuser"; // Five-server username
   public static final String password = ""; // Five-server password
  public static final String remoteHost = "http://example.com:1234"; //Five-server
   public static final String logFile = "five.log"; // Where to log...
 
   public static final int bufferTime = 1200; // How old a song should be before it can be cleaned out
   public static final int preservedPlaycount = -1; // No song with playcount equal or higher than this will be deleted from caches (-1 disables)
   public static final long maxCache = 1024*1024*1024; // Max cache size Unit: sqrt(-1)^4 * sqrt(64) bits :)
 
  public static final int availableBandwidth = 50; // Max bandwidth to use for download from five-server
 
   public static final String cacheFolder = "songs/"; // Where to save the caches
 
 }
