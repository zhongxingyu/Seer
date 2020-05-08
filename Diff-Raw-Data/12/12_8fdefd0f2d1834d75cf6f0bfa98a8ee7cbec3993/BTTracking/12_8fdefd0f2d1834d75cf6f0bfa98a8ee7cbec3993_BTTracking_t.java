 package uk.ac.bristol.dundry.dao;
 
 import com.turn.ttorrent.client.Client;
 import com.turn.ttorrent.client.SharedTorrent;
 import com.turn.ttorrent.common.Torrent;
 import com.turn.ttorrent.tracker.TrackedTorrent;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.nio.file.DirectoryStream;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.security.NoSuchAlgorithmException;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import javax.annotation.PreDestroy;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Tracks trackedTorrents and purges clients. TTorrent implements this, of
  * course, but uses its own http server.
  *
  * @author Damian Steer <d.steer@bris.ac.uk>
  */
 public class BTTracking {
 
     final static Logger log = LoggerFactory.getLogger(BTTracking.class);
     final Thread purger;
     private final ConcurrentMap<String, TrackedTorrent> trackedTorrents;
     private final ConcurrentMap<String, Client> clients;
     private final ConcurrentMap<Path, Boolean> seenTorrentFiles;
     private final InetAddress address;
     private final String publishBase;
     private final boolean seedTorrents;
 
     public BTTracking(String publishBase, boolean seedTorrents) throws IOException, NoSuchAlgorithmException {
         log.info("Starting up bittorrent. Seed torrents? {}", seedTorrents);
         this.publishBase = publishBase;
         this.seedTorrents = seedTorrents;
         address = InetAddress.getLocalHost();
         log.info("Address is {}", address.getHostAddress());
         trackedTorrents = new ConcurrentHashMap<>();
         clients = new ConcurrentHashMap<>();
         seenTorrentFiles = new ConcurrentHashMap<>();
         loadNewTorrents(Paths.get(publishBase));
         purger = new Thread(new Purger(), "Torrent client purger");
         purger.setDaemon(true);
         purger.start(); // may not be a good idea? Could quartz do this work instead?
     }
 
     public ConcurrentMap<String, TrackedTorrent> getTorrents() {
         return trackedTorrents;
     }
 
     @PreDestroy
     public void destroy() throws InterruptedException {
         log.info("Shutting down tracker");
 
         purger.interrupt();
         purger.join();
 
         for (Client client : clients.values()) {
             client.stop();
         }
     }
     
     /**
      * Seed a torrent from a file
      * @param torrentFile The location of the torrent file
      * @param base The location of the content to be shared
      * @throws IOException
      * @throws NoSuchAlgorithmException 
      */
     public void addTorrent(Path torrentFile, Path base) throws IOException, NoSuchAlgorithmException {
         log.info("Adding torrent {}", torrentFile);
         
         // Ensure we are using real paths -- no relative and / or symlink
         // nonsense
         torrentFile = torrentFile.toRealPath();
         base = base.toRealPath();
         
         log.debug("Loading torrent <{}> using base <{}>", torrentFile, base);
         
         // Load as a seeder
         Torrent torrent = Torrent.load(torrentFile.toFile(), base.toFile(), true);
 
         SharedTorrent shared = new SharedTorrent(torrent, base.toFile(), true);
 
         if (!shared.isSeeder()) {
             log.warn("Torrent {} is not seeding. Skipping.", torrentFile);
             return;
         }
 
         TrackedTorrent tracked = new TrackedTorrent(torrent);
         
         seenTorrentFiles.put(torrentFile.toAbsolutePath(), Boolean.TRUE);
         trackedTorrents.put(tracked.getHexInfoHash(), tracked);
 
         if (seedTorrents) {
             Client client = new Client(address, shared);
             clients.put(torrent.getHexInfoHash(), client);
             client.share();
         
             log.info("Seeding {}", torrentFile);
         }
     }
 
     /**
      * Look for, load, and serve trackedTorrents in base
      *
      * @param base Place to look
      * @return
      */
     private void loadNewTorrents(Path base)
             throws IOException, NoSuchAlgorithmException {
         log.debug("Looking for new torrents in {}", base);
         
         if (!Files.exists(base)) {
             log.warn("Cannot scan <{}> for new torrents. Doesn't exist.", base);
             return;
         }
         
         try (DirectoryStream<Path> stream = Files.newDirectoryStream(base)) {
             for (Path pubDir : stream) {
                 // Find a torrent for this dir if possible
                 Path torrentFile = findTorrent(pubDir, pubDir, base);
                 
                 // Check real path to avoid relative and sym link issues
                 if (torrentFile != null && 
                         !seenTorrentFiles.containsKey(torrentFile.toRealPath())) {
                    try {
                        // Real path-ify to avoid case where base contains symlinks
                        addTorrent(torrentFile, pubDir.toRealPath().getParent());
                    } catch (Exception e) {
                        // Log issues and move on
                        log.error("Issue loading {}: {}", torrentFile, e);
                    }
                 }
             }
         }
     }
 
     private Path findTorrent(Path forDir, Path... locations) throws IOException {
         Path toFind = Paths.get(forDir.getFileName().toString() + ".torrent");
 
         for (Path location : locations) {
             Path candidate = location.resolve(toFind);
             if (Files.exists(candidate)) {
                 return candidate;
             }
         }
 
         return null;
     }
    
     class Purger implements Runnable {
 
         @Override
         public void run() {
             while (true) {
                 try {
                     Thread.sleep(30 * 1000);
                 } catch (InterruptedException ex) {
                     log.info("Purger shutting down");
                     return;
                 }
 
                 log.debug("checking unfresh peers");
                 for (TrackedTorrent torrent : trackedTorrents.values()) {
                     torrent.collectUnfreshPeers();
                 }
                 
                 try {
                     loadNewTorrents(Paths.get(publishBase));
                 } catch ( IOException | NoSuchAlgorithmException ex) {
                     log.error("Issue finding new torrents", ex);
                 }
             }
         }
     }
 }
