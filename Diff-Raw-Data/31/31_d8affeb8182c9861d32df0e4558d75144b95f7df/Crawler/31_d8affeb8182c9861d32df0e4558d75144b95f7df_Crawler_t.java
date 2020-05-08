 package com.jreddit.botkernel;
 
 import java.io.*;
 import java.util.*;
 
 import com.omrlnr.jreddit.*;
 
 /**
  *
  * Reddit Bot Crawler thread.
  *
  */
 public class Crawler implements Runnable {
 
     //
     // A list of listeners registered with this crawler.
     //
     private List<CrawlerListener> _listeners = new ArrayList<CrawlerListener>();
 
     //
     // A list of match criteria.
     //
     private List<CrawlerMatchCriteria> _criteria = 
                                     new ArrayList<CrawlerMatchCriteria>();
 
     //
     // Cache Submission comment count as an optimization when crawling.
     //
     protected Map<String, Long> _submissionCommentCount = 
                                         new HashMap<String, Long>();
 
     //
     // An object used when synchronizing modification to the listeners
     //
     private Object _lock = new Object();
 
     //
     // A list of subs to crawl 
     //
     private List<String> _subs;
 
     //
     // Submissions types to check when crawling.
     //
     Submissions.ListingType[] _listingTypes;
 
     //
     // Time to sleep between crawls.
     //
     private int _sleepTime;
 
     //
     // Limit of items to retrieve during crawl.
     // TODO make this more configurable.
     //
     private int _limit;
 
     private User _user;
 
     private String _name;
 
     private boolean _shuffle = false;
 
     private boolean _shutdown = false;
 
 
     /**
      * Create a new Crawler.
      *
      * @param user          The user to run the crawl as.
      * @param name          The name of this crawler.
      * @param subs          A list of subreddits this crawler will crawl.
      * @param listingTypes  The type of listings to check for in the
      *                      crawled subs.
      * @param limit         The number of item in each sub to crawl.
      * @param sleepTime     The time in seconds to sleep between crawls.
      *
      */
     public Crawler( User user,
                     String name,
                     List<String> subs,
                     Submissions.ListingType[] listingTypes,
                     int limit,
                     int sleepTime ) {
 
         _user = user;
         _name = name;
         _subs = subs;
         _listingTypes = listingTypes;
         _limit = limit;
         _sleepTime = sleepTime;
     }
 
     public String getName() { return _name; }
 
     /**
      * Add a listener interested in receiving notifications 
      * from this crawler.
      *
      * @param listener  The listener to add.
      *
      */
     public void addListener(CrawlerListener listener) {
         synchronized(_lock) {
             _listeners.add(listener);
         }
     }
 
     /**
      *
      * Remove a listener no longer interested in receiving notifications
      * from this crawler.
      *
      * @param listener  The listener to remove.
      *
      */
     public void removeListener(CrawlerListener listener) {
         synchronized(_lock) {
             _listeners.remove(listener);
         }
     }
 
 
     /**
      *
      * Add a match criteria to this crawler.
      *
      */
     public void addMatchCriteria(CrawlerMatchCriteria criteria) {
         synchronized(_lock) {
             _criteria.add(criteria);
         }
     }
 
     /**
      *
      * Checks of the specified subreddit is in this crawler's
      * searched subreddits.
      */
     public boolean containsSubreddit(String s) {
         synchronized(_lock) {
             for(String sub: _subs) {
                 if(sub.equals(s)) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     /**
      *
      *  Remove a match criteria from this crawler.
      *
      */
     public void removeMatchCriteria(CrawlerMatchCriteria criteria) {
         synchronized(_lock) {
             _criteria.remove(criteria);
         }
     }
 
     /**
      * Specify if we want the crawler to randomly shuffle the
      * subreddits crawled before each crawl.
      */
     public void setShuffle(boolean b) {
         _shuffle = b;
     }
 
     /**
      *
      * Perform the crawk.
      *
      */
     public void run() {
 
         log("Crawler " + _name + " running...");
 
         while(true) {
 
             if(_shutdown) {
                 log("Crawler " + getName() + " shutting down...");
                 return;
             }
 
             try {
 
                 _user.connect();
 
             } catch(IOException ioe) {
                 log("Error cannot connect user for crawl.");
                 sleep(_sleepTime);
                 continue;
             }
 
             log("Total subreddits to crawl:   " + _subs.size());
             
             if(_shuffle) {
                 //
                 // Random shuffle subreddit list.
                 //
                 Collections.shuffle(
                                 _subs, 
                                 new Random(System.currentTimeMillis()) );
             }
 
             //
             // Time ourselves.
             //
             Date startTime = new Date();
 
             //
             // Optimization.
             // 
             // Do not crawl if there are no listeners.
             //
             if(_listeners.size() == 0) {
                 log("No listeners. Crawler sleeping...");
                 sleep(_sleepTime);
                 continue;
             }
 
             // 
             // Find any new matches
             //
             for(int i = 0; i < _subs.size(); i++) {
                     
                 Date currentTime = new Date();
                 String subreddit = _subs.get(i);
 
                 log("Crawler start:      " + 
                             BotKernel.DATE_FORMAT.format(startTime));
                 log("Crawler current:    " + 
                             BotKernel.DATE_FORMAT.format(currentTime));
                 log("Checking subreddit: " + (i+1) + 
                             " / " + _subs.size() + 
                             " (" + subreddit + ")" );
 
 
                 for(Submissions.ListingType listingType: _listingTypes) {
 
                     try {
 
                         //
                         // Go find game requests in the given subreddit
                         //
                         doCrawl(subreddit, listingType);
                  
                     } catch(RateLimitException rle) {
 
                         log("Caught RateLimitException: " + 
                                                 rle.getMessage());
 
                         int sleepSecs = rle.getRetryTime();
     
                         log("Crawler sleeping " + sleepSecs + 
                                 " seconds to recover from " +
                                 "rate limit exception...");
 
                         sleep(sleepSecs);
             
                     } catch(IOException ioe) {
                         ioe.printStackTrace();
                         log("Crawler exception");
                     }
                 }
             }
 
             //
             // Crawler default sleep
             //                
             log("Crawler sleeping...");
             sleep(_sleepTime);
 
         }
     }
 
     protected void sleep(int seconds) {
         try {
             for(int i = 0; i <  seconds; i++) {
                 if(_shutdown) {
                     return;
                 }
                 Thread.sleep(1000);
             }
         } catch (InterruptedException ie) {
             throw new RuntimeException(ie);
         }
     }
 
     /**
      *
      * Call to indicate that this crawler should shut down.
      *
      */
     public void shutdown() {
         _shutdown = true;
     }
 
     /**
      * Check list of subreddits for new game requests and
      * return a list of all game requesting comments found.
      */
     private void doCrawl(   String subreddit,
                             Submissions.ListingType listingType) 
                                                     throws IOException {
         List<Thing> ret = new ArrayList<Thing>();
 
         log("Checking for crawl matches in subreddit: " + subreddit);
         log("Current user: \n" + _user);
 
         List<Submission> submissions = 
                                 Submissions.getSubmissions(
                                                 _user,
                                                 subreddit,
                                                 listingType, 
                                                 _limit,
                                                 (String)null,
                                                 (String)null );
 
         // log("Submissions: " + submissions.size());
 
         for(Submission submission: submissions) {
 
             // log("Checking for crawl matches in submission: " + 
             //    submission.getTitle() +
             //    " (" + submission.getName() + ")" +
             //    " (" + submission.getSubreddit() + ")" );
 
             //
             // Optimization
             //
             // Cache the number of comments in the submission and only
             // check the submission if the number has changed since it
             // was cached.
             //
             Long numComments = 
                         _submissionCommentCount.get(submission.getName());
             if(numComments == null) {
                 long l = submission.getNumComments();
                 numComments = new Long(l);
                 _submissionCommentCount.put(submission.getName(), numComments);
             } else {
                 //
                 // See if we need to skip this.
                 //
                 if(numComments.longValue() == submission.getNumComments()) {
                     // No new comments. Skip this.
                     // log("No new comments. Skipping " + submission.getName());
                     continue;
                 }
                _submissionCommentCount.put(
                                    submission.getName(), 
                                    new Long(submission.getNumComments()) );
             }
 
            log("Checking for crawl matches in submission: " + 
                submission.getTitle() +
                " (" +  submission.getName() + " in " +
                        submission.getSubreddit() + ")" +
                " (cached count " + 
                    _submissionCommentCount.get(submission.getName()) + 
                    " actual " + submission.getNumComments() + ")"); 


             //
             // Check the submission itself to see if we have a match
             //
 
             List<CrawlerListener> copyListeners = 
                                     new ArrayList<CrawlerListener>();
             List<CrawlerMatchCriteria> copyCriteria = 
                                     new ArrayList<CrawlerMatchCriteria>();
             synchronized(_lock) {
                 // Copy our lists of listeners and criteria so we
                 // don't get concurrent modification exceptions
                 // if listeners want to remove themselves 
                 // or their criteria as their response to a match
  
                 for(CrawlerListener listener: _listeners) {
                     copyListeners.add(listener);
                 }
 
                 for(CrawlerMatchCriteria criteria: _criteria) {
                     copyCriteria.add(criteria);
                 }
             }    
 
             //
             // Now do the matches against the copies. Listeners can
             // remove themselves when called if they like.
             //
             for(CrawlerMatchCriteria criteria: copyCriteria) {
                 CrawlerListener listener = criteria.getCrawlerListener();
                 if( copyListeners.contains(listener) &&
                     criteria.match(submission)  ) {
  
                     //
                     //
                     // log("Found match in submission " + submission.getName());
 
                     listener.handleCrawlerEvent(submission);
                 }
             }
 
             //
             // Check the replies to the submission
             //
             List<Comment> comments = Comments.getComments(
                                                         _user,
                                                         submission );
             log("Comments: " + comments.size());
 
             recursiveCommentCheck(comments);
 
         }
 
     }
 
     /**
      *
      * Recursively check comments for game requests.
      */
     private void recursiveCommentCheck( List<Comment> comments ) 
                                             throws IOException {
         if(comments == null) {
             return;
         }
 
         for(Comment comment: comments) {
 
             //
             // Check the comment to see if we have a match
             //
 
             List<CrawlerListener> copyListeners = 
                                     new ArrayList<CrawlerListener>();
             List<CrawlerMatchCriteria> copyCriteria = 
                                     new ArrayList<CrawlerMatchCriteria>();
             synchronized(_lock) {
                 //
                 // Copy our lists of listeners and criteria.
                 // Operate on the copies.
                 //
  
                 for(CrawlerListener listener: _listeners) {
                     copyListeners.add(listener);
                 }
                 for(CrawlerMatchCriteria criteria: _criteria) {
                     copyCriteria.add(criteria);
                 }
             }    
 
             //
             // Run matchers against the comment
             //
             for(CrawlerMatchCriteria criteria: copyCriteria) {
                 CrawlerListener listener = criteria.getCrawlerListener();
                 if( copyListeners.contains(listener) &&
                     criteria.match(comment)  ) {
    
                     //
                     //
                     // log("Found match in comment " + comment.getName());
 
                     listener.handleCrawlerEvent(comment);
                 }
             }
 
             List<Comment> replies = comment.getReplies();
             recursiveCommentCheck(replies);
         }
     }
 
     protected static void log(String s) {
         BotKernel.getBotKernel().log(s);
     }
 
 }
