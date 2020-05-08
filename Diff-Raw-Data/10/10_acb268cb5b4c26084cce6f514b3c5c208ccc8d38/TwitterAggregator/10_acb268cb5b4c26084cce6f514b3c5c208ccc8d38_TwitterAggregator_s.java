 package org.codefromhell.wicket.aggregator.twitter;
 
 import twitter4j.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class TwitterAggregator {
     public List<Tweet> searchTweets(String query) {
         List<Tweet> tweets = new ArrayList<Tweet>();
         try {
             Twitter twitter = new TwitterFactory().getInstance();
            Query twQuery = new Query("#onedaytalk");
             QueryResult result = twitter.search(twQuery);
            // System.out.println("hits:" + result. getTotal());
             tweets = result.getTweets();
         } catch (TwitterException e) {
             e.printStackTrace();
         }
         return tweets;
     }
 }
