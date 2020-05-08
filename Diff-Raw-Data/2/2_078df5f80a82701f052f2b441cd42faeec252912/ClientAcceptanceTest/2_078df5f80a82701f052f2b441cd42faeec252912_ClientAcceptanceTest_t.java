 package com.turn.ttorrent.client;
 
 import com.turn.ttorrent.common.Torrent;
 import com.turn.ttorrent.testutil.WaitFor;
 import com.turn.ttorrent.tracker.TrackedTorrent;
 import com.turn.ttorrent.tracker.Tracker;
 import org.junit.Test;
 
 import java.io.File;
 import java.net.InetAddress;
 import java.net.URI;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 public final class ClientAcceptanceTest {
 
     @Test public void canPublishFromClient() throws Exception {
         final InetAddress localHost = InetAddress.getLocalHost();
         Tracker tracker = new Tracker(localHost);
         tracker.announce(TrackedTorrent.load(new File("test_resources/torrents/file2.jar.torrent")));
         tracker.start();
 
         Client unit = new Client(localHost, sharedCompleteTorrent());
         unit.share();
         waitForTorrents(tracker, 1);
         assertThat(tracker.getTrackedTorrents().size(), is(1));
     }
 
     private SharedTorrent sharedCompleteTorrent() throws Exception {
         String folder = "test_resources/files";
         File testFile = new File(folder + "/file2.jar");
        Torrent testTorrent = Torrent.create(testFile, new URI("http://localhost:6969/announce"), "Test Person");
         File parentFiles = new File(folder);
         return new SharedTorrent(testTorrent, parentFiles);
     }
 
     private void waitForTorrents(final Tracker tracker, final int numberOfTorrents) {
         new WaitFor(10) {
             @Override
             protected boolean condition() {
                 return (tracker.getTrackedTorrents().size() == numberOfTorrents);
             }
         };
     }
 
 }
