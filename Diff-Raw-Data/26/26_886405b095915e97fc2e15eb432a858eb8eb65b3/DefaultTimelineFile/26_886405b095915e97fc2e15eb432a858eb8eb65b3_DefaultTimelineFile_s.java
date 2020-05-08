 package com.cafeform.iumfs.twitterfs.files;
 
 import com.cafeform.iumfs.twitterfs.Account;
 import com.cafeform.iumfs.twitterfs.TwitterFactoryAdapter;
 import static com.cafeform.iumfs.twitterfs.files.AbstractNonStreamTimelineFile.getWaitSec;
 import com.cafeform.iumfs.utilities.Util;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import twitter4j.Paging;
 import twitter4j.ResponseList;
 import twitter4j.Status;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import static java.util.logging.Level.*;
 import java.util.logging.Logger;
 
 /**
  * Represents non-streaming timelines. That includs mentions, home and
  * retweets_of_me but not user timeline.
  *
  * @author kaizawa
  */
 public class DefaultTimelineFile extends AbstractNonStreamTimelineFile
 {
    private static final Logger logger = 
             Logger.getLogger(DefaultTimelineFile.class.getName());
 
     public DefaultTimelineFile (
             Account account,
             String pathname)
     {
         super(account, pathname);
         interval = calculateIntervalByName(getName());
         if (autoUpdateEnabled)
         {
             readAhead();
             startAutoUpdateThread();
         }
     }
 
     /**
      * Initialise routine for this type of file
      */
     private void readAhead ()
     {
         try
         {
             /*
              * If not stream timeline, read 1 page(20 tweets)
              * as initial read.
              */
             getTimeline();
         } catch (TwitterException ex)
         {
             log(logger, INFO, "Cannot get " + getName() + " timeline: " + 
                     ex.getErrorMessage(), ex);
         }
     }
 
     @Override
     protected ResponseList<Status> getTimeline (Paging paging)
             throws TwitterException
     {
         Twitter twitter = TwitterFactoryAdapter.getInstance(getUsername());
         ResponseList<Status> statuses = null;
         String name = getName();
 
         switch (name)
         {
             case "mentions":
                 statuses = twitter.getMentionsTimeline(paging);
                 break;
             case "home":
                 // For home TL, stream API should be used.
                 statuses = twitter.getHomeTimeline(paging);
                 break;
             case "retweets_of_me":
                 statuses = twitter.getRetweetsOfMe(paging);
                 break;
             default:
                 log(logger, SEVERE, "Unknown timeline(\"" + name
                         + "\") specified.");
                 System.exit(1);
         }
 
         return statuses;
     }
 
     final protected void startAutoUpdateThread ()
     {
         // TODO: each time line should use same thread pool
         ScheduledExecutorService executor;
 
         // Invokde timeline retrieving thread. 
         Runnable timelineUpdater = new Runnable()
         {
             @Override
             public void run ()
             {
                 try
                 {
                     getTimeline();
                 } 
                 catch (TwitterException ex)
                 {
                     if (ex.exceededRateLimitation())
                     {
                         // User Timeline rate limit exceeds.                                            
                         // wait for reset time.                                            
                         long waitSec = getWaitSec(ex.getRateLimitStatus());
                        log(logger, INFO,
                                getAccount().getUsername()
                                + " exceeds rate limit for "
                                + getName()
                                + " timeline. wait for " + waitSec
                                + " sec.");
                        
                         Util.sleep(waitSec * 1000);
                     }
                     else 
                     {
                        log(logger, INFO, getAccount().getUsername() + 
                            ": Cannot get " + getName() + " timeline.", ex);
                     }
                 }
             }
         };
 
         executor = Executors.newSingleThreadScheduledExecutor();
         // retrieve periodically
         executor.scheduleAtFixedRate(
                 timelineUpdater,
                 getInterval(),
                 getInterval(),
                 TimeUnit.MILLISECONDS);
     }
 
     /**
      * Calculate rate limit based on type of timeline
      *
      * @return the interval
      */
     public long getInterval ()
     {
         return interval;
     }
 }
