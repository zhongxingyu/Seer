 package com.brotherlogic.memory;
 
 import java.io.IOException;
 
 import com.brotherlogic.memory.db.DBFactory;
 import com.brotherlogic.memory.db.DownloadQueue;
 import com.brotherlogic.memory.feeds.GitEventFeedReader;
import com.brotherlogic.memory.feeds.UntappdFeedReader;
 
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
     * @throws IOException
     *            if something goes wrong
     */
    public static void main(final String[] args) throws IOException
    {
       CommandLineUpdate clu = new CommandLineUpdate();
       clu.run();
    }
 
    /**
     * Runs the stuff
     * 
     * @throws IOException
     *            if something goes wrong
     */
    public void run() throws IOException
    {
       // Start up the download queue
       DownloadQueue queue = DBFactory.buildInterface().getDownloadQueue();
       Thread downloadThread = new Thread(queue);
       downloadThread.start();
 
      UntappdFeedReader reader = new UntappdFeedReader("brotherlogic");
      reader.update();
 
       GitEventFeedReader reader2 = new GitEventFeedReader("brotherlogic");
       reader2.update();
 
       queue.slowStop();
    }
 }
