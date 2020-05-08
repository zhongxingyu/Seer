 package com.gent.azureus.plugins;
 
 import org.gudy.azureus2.plugins.PluginInterface;
 import org.gudy.azureus2.plugins.download.Download;
 import org.gudy.azureus2.plugins.download.DownloadAnnounceResult;
 import org.gudy.azureus2.plugins.download.DownloadScrapeResult;
 import org.gudy.azureus2.plugins.logging.LoggerChannel;
 import org.gudy.azureus2.plugins.torrent.Torrent;
 import org.gudy.azureus2.plugins.torrent.TorrentAnnounceURLListSet;
 
 import java.net.URL;
 
 /**
  * Created by IntelliJ IDEA.
  * User: rgravener
  * Date: 12/13/10
  * Time: 1:12 AM
  * To change this template use File | Settings | File Templates.
  */
public class TehconnectionRunnable implements Runnable {
 
     private final PluginInterface api;
 
     private boolean run = true;
 
     private int seedLimit = 4;
 
     private static final String TEHCONNECTION_HOST = "tehconnection.eu";
 
     private LoggerChannel log;
 
     public TehconnectionRunnable(PluginInterface api) {
         this.api = api;
         log = api.getLogger().getChannel("tehconnection");
     }
 
     private void pause(Download download, int seedCount) {
         if (!download.isPaused()) {
             download.pause();
             log.log(String.format("%s seeds, downloading has been paused", seedCount));
         }
     }
 
     private void resume(Download download, int seedCount) {
         if (download.isPaused()) {
             download.resume();
             log.log(String.format("%s seeds, downloading has been resumed", seedCount));
         }
     }
 
     public void run() {
 
         while (run) {
             try {
                 Download[] downloads = api.getDownloadManager().getDownloads();
                 for (Download download : downloads) {
                     Torrent torrent = download.getTorrent();
                     if (!isTehConnection(torrent)) {
                         continue;
                     }
                     DownloadAnnounceResult announce = download.getLastAnnounceResult();
                     int seedCount = announce.getSeedCount();
                     if (seedCount > seedLimit) {
                         pause(download, announce.getSeedCount());
                         continue;
                     } else {
                         resume(download, announce.getSeedCount());
                     }
                     DownloadScrapeResult scrape = download.getLastScrapeResult();
                     seedCount = scrape.getSeedCount();
                     if (scrape.getResponseType() == DownloadScrapeResult.RT_SUCCESS) {
                         if (seedCount > seedLimit) {
                             pause(download, scrape.getSeedCount());
                         } else {
                             resume(download, scrape.getSeedCount());
                         }
                     }
                 }
                 try {
                     Thread.sleep(15000);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     private boolean isTehConnection(Torrent torrent) {
         if (torrent.getAnnounceURL() != null) {
             if (torrent.getAnnounceURL().getHost().equalsIgnoreCase(TEHCONNECTION_HOST)) {
                 return true;
             }
         }
         if (torrent.getAnnounceURLList().getSets().length != 0) {
             TorrentAnnounceURLListSet[] set = torrent.getAnnounceURLList().getSets();
             for (TorrentAnnounceURLListSet listSet : set) {
                 for (URL url : listSet.getURLs()) {
                     if (url.getHost().equalsIgnoreCase(TEHCONNECTION_HOST)) {
                         return true;
                     }
                 }
             }
         }
         return false;
     }
 }
