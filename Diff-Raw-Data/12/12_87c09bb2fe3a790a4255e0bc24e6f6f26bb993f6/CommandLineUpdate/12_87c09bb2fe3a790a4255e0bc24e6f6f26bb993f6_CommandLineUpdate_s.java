 package com.brotherlogic.memory;
 
 import com.brotherlogic.memory.db.DBFactory;
 import com.brotherlogic.memory.db.DownloadQueue;
import com.brotherlogic.memory.feeds.discogs.DiscogsFeedReader;
 
 /**
  * Test bed for updating the database from the command line
  * 
  * @author simon
  * 
  */
 public class CommandLineUpdate
 {
    /**
     * Main method
     * 
     * @param args
     *           CL Params are not used
     * @throws Exception
     *            if something goes wrong
     */
    public static void main(final String[] args) throws Exception
    {
       CommandLineUpdate clu = new CommandLineUpdate();
       clu.run();
    }
 
    /**
     * Runs the stuff
     * 
     * @throws Exception
     *            if something goes wrong
     */
    public void run() throws Exception
    {
       // Start up the download queue
       DownloadQueue queue = DBFactory.buildInterface().getDownloadQueue();
       Thread downloadThread = new Thread(queue);
       downloadThread.start();
 
       // UntappdFeedReader reader = new UntappdFeedReader("brotherlogic");
       // reader.update();
 
      // GitEventFeedReader reader2 = new GitEventFeedReader("brotherlogic");
      // reader2.update();
 
      DiscogsFeedReader dfr = new DiscogsFeedReader();
      dfr.update();
 
       queue.slowStop();
    }
 }
