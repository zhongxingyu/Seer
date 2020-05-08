 package com.cafeform.iumfs.twitterfs.files;
 
 import com.cafeform.iumfs.twitterfs.Account;
 import com.cafeform.iumfs.twitterfs.TwitterFactoryAdapter;
 import static com.cafeform.iumfs.twitterfs.files.TwitterFsFile.logger;
 import java.util.logging.Level;
 import twitter4j.Paging;
 import twitter4j.ResponseList;
 import twitter4j.Status;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 
 /**
  * Represents Authenticated user timeline and friends timeline 
  * which is affected by statuses/user_timeline rate limit
  *
  * @author kaizawa
  */
 public class UserTimelineFileImpl extends AbstractNonStreamTimelineFile
 implements UserTimelineFile
 {
     public UserTimelineFileImpl (Account account, String pathname)
     {
         super(account, pathname);
     }
 
     @Override
     protected ResponseList<Status> getTimeLine (Paging paging)
             throws TwitterException
     {
         Twitter twitter = TwitterFactoryAdapter.getInstance(getUsername());
         ResponseList<Status> statuses = null;
         String name = getName();
 
         switch (name)
         {
             case "user":
                 statuses = twitter.getUserTimeline(paging);
                 break;
             default:
                 if (getPath().startsWith(FollowersDirectory.PATH_NAME + "/" ) ||
                        getPath().startsWith(FriendsDirectory.PATH_NAME + "/" ))
                 {
                     statuses = twitter.getUserTimeline(name, paging);
                 } else
                 {
                     logger.warning("Unknown timeline file " + getPath() +
                             " specified.");
                     System.exit(1);
                 }
         }
         return statuses;
     }
     
     /**
      * @return the interval
      */
     public static long calculateInterval()
     {
         long val;
 
         // Need to take MAX_PAGES into account, since API would be called 
         // MAX_PAGES times per each trial.
         val = (RATE_LIMIT_WINDOW * 60 / USER_RATE_LIMIT) * 1000 * MAX_PAGES;
         logger.log(Level.FINE, "Calculate interval for user timeline is " + val);
         return val;
     }
     
     // Compare object by its Name (not pathname)
     @Override
     public boolean equals(Object obj)
     {
         if (obj instanceof UserTimelineFileImpl)
         {
             return this.getName().endsWith(((UserTimelineFileImpl)obj).getName());
         }
         return false;
     }
 
     @Override
     public int hashCode ()
     {
         int hash = 3;
         return hash;
     }
 }
