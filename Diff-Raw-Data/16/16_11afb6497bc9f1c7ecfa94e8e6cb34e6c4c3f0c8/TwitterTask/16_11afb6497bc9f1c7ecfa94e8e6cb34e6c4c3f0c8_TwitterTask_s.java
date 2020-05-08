 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package twitterapplication;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.Mongo;
 import com.mongodb.MongoException;
import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.List;
 import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
 import twitter4j.Query;
 import twitter4j.QueryResult;
 import twitter4j.Status;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.UserMentionEntity;
 import twitter4j.conf.ConfigurationBuilder;
 
 /**
  *
  * @author Muhammad.Saifuddin
  */
 public class TwitterTask {
 
     private static ConfigurationBuilder cb;
     private DB db;
     private DBCollection items;
 
     public TwitterTask() {
         try {
             // on constructor load initialize MongoDB and load collection
             initMongoDB();
             items = db.getCollection("tweetColl");
         } catch (MongoException ex) {
             System.out.println("MongoException :" + ex.getMessage());
         }
 
 
     }
 
     /**
      * static block used to construct a connection with tweeter with twitter4j
      * configuration with provided settings. This configuration builder will be
      * used for next search action to fetch the tweets from twitter.com.
      */
     static {
         cb = new ConfigurationBuilder();
         cb.setDebugEnabled(true);
         cb.setOAuthConsumerKey("*********************");
         cb.setOAuthConsumerSecret("*************************");
         cb.setOAuthAccessToken("*************************");
         cb.setOAuthAccessTokenSecret("*************************");
     }
 
     public static void main(String[] args) {
 
         TwitterTask taskObj = new TwitterTask();
         taskObj.loadMenu();
     }
 
     public void loadMenu() {
 
         System.out.println("========================================\n\tTwitter Task\n========================================");
         System.out.println("1 - Load 100 Tweets & save into Mongo DB");
         System.out.println("2 - Load Top 5 Retweet");
         System.out.println("3 - Load Top 5 mentioned");
         System.out.println("4 - Load Top 5 followed");
         System.out.println("5 - Exit");
 
         System.out.print("Please enter your selection:\t");
 
         Scanner scanner = new Scanner(System.in);
         int selection = scanner.nextInt();
 
         if (selection == 1) {
             getTweetByQuery(true);
         } else if (selection == 2) {
             getTopRetweet();
         } else if (selection == 3) {
             getTopMentioned();
         } else if (selection == 4) {
             getTopfollowed();
         } else if (selection == 5) {
             db.dropDatabase();
             System.exit(0);
         } else {
             System.out.println("Wrong Selection Found..\n\n");
             loadMenu();
         }
     }
 
     /**
      * initMongoDB been called in constructor so every object creation this
      * initialize MongoDB.
      */
     public void initMongoDB() throws MongoException {
         try {
             System.out.println("Connecting to Mongo DB..");
             Mongo mongo;
             mongo = new Mongo("127.0.0.1");
             db = mongo.getDB("tweetDB");
         } catch (UnknownHostException ex) {
             System.out.println("MongoDB Connection Errro :" + ex.getMessage());
         }
     }
 
     /**
      * void getTweetByQuery method used to fetch records from twitter.com using
      * Query class to define query for search param with record count.
      * QueryResult persist result from twitter and provide into the list to
      * iterate records 1 by one and later on item.insert is call to store this
      * BasicDBObject into MongoDB items Collection.
      *
      * @param url an absolute URL giving the base location of the image
      * @see BasicDBObject, DBCursor, TwitterFactory, Twitter
      */
     public void getTweetByQuery(boolean loadRecords) {
         if (cb != null) {
             TwitterFactory tf = new TwitterFactory(cb.build());
             Twitter twitter = tf.getInstance();
             try {
                 Query query = new Query("java");
                 query.setCount(50);
                 QueryResult result;
                 result = twitter.search(query);
                 System.out.println("Getting Tweets...");
                 List<Status> tweets = result.getTweets();
 
                 for (Status tweet : tweets) {
                     BasicDBObject basicObj = new BasicDBObject();
                     basicObj.put("user_name", tweet.getUser().getScreenName());
                     basicObj.put("retweet_count", tweet.getRetweetCount());
                     basicObj.put("tweet_followers_count", tweet.getUser().getFollowersCount());
 
                     UserMentionEntity[] mentioned = tweet.getUserMentionEntities();
                     basicObj.put("tweet_mentioned_count", mentioned.length);
                     basicObj.put("tweet_ID", tweet.getId());
                     basicObj.put("tweet_text", tweet.getText());
 
                     if (mentioned.length > 0) {
 //                    System.out.println("Mentioned length " + mentioned.length + " Mentioned: " + mentioned[0].getName());
                     }
                     try {
                         items.insert(basicObj);
                     } catch (Exception e) {
                         System.out.println("MongoDB Connection Error : " + e.getMessage());
                         loadMenu();
                     }
                 }
                 // Printing fetched records from DB.
                 if (loadRecords) {
                     getTweetsRecords();
                 }
 
             } catch (TwitterException te) {
                 System.out.println("te.getErrorCode() " + te.getErrorCode());
                 System.out.println("te.getExceptionCode() " + te.getExceptionCode());
                 System.out.println("te.getStatusCode() " + te.getStatusCode());
                 if (te.getStatusCode() == 401) {
                     System.out.println("Twitter Error : \nAuthentication credentials (https://dev.twitter.com/pages/auth) were missing or incorrect.\nEnsure that you have set valid consumer key/secret, access token/secret, and the system clock is in sync.");
                 } else {
                     System.out.println("Twitter Error : " + te.getMessage());
                 }
 
 
                 loadMenu();
             }
         } else {
             System.out.println("MongoDB is not Connected! Please check mongoDB intance running..");
         }
     }
 
     /**
      * void method print fetched top retweet records from preloaded items
      * collection with the help of BasicDBObject class defined sort with desc
      * with fixed limit 10.
      *
      * @see BasicDBObject, DBCursor
      */
     public void getTopRetweet() {
 
        if (items.count() > 0) {
             getTweetByQuery(false);
         }
 
         BasicDBObject query = new BasicDBObject();
         query.put("retweet_count", -1);
         DBCursor cursor = items.find().sort(query).limit(10);
         System.out.println("items length " + items.count());
         while (cursor.hasNext()) {
             System.out.println(cursor.next());
         }
         loadMenu();
     }
 
     /**
      * void method print fetched top followed records from preloaded items
      * collection with the help of BasicDBObject class defined sort with desc
      * with fixed limit 10.
      *
      * @see BasicDBObject, DBCursor
      */
     public void getTopfollowed() {
 
        if (items.count() > 0) {
             getTweetByQuery(false);
         }
 
         BasicDBObject query = new BasicDBObject();
         query.put("tweet_followers_count", -1);
         DBCursor cursor = items.find().sort(query).limit(10);
 
         while (cursor.hasNext()) {
             System.out.println(cursor.next());
         }
         loadMenu();
     }
 
     /**
      * void method print fetched top mentioned records from preloaded items
      * collection with the help of BasicDBObject class defined sort with desc.
      *
      * @see BasicDBObject, DBCursor
      */
     public void getTopMentioned() {
 
        if (items.count() > 0) {
             getTweetByQuery(false);
         }
 
         BasicDBObject query = new BasicDBObject();
         query.put("tweet_mentioned_count", -1);
         DBCursor cursor = items.find().sort(query).limit(10);
 
        if (cursor.length() > 0) {
             while (cursor.hasNext()) {
                 System.out.println(cursor.next());
             }
         } else {
         }
         loadMenu();
     }
 
     /**
      * void method print fetched records from mongodb This method use the
      * preloaded items (Collection) for fetching records and print them on
      * console.
      */
     public void getTweetsRecords() {
         BasicDBObject fields = new BasicDBObject("_id", true).append("user_name", true).append("tweet_text", true);
         DBCursor cursor = items.find(new BasicDBObject(), fields);
 
         while (cursor.hasNext()) {
             System.out.println(cursor.next());
         }
         loadMenu();
     }
     /*public void getTweetsStatistics() {
      BasicDBObject query = new BasicDBObject();
      query.put("retweet_count", -1);
      query.put("tweet_followers_count", -1);
      query.put("tweet_mentioned_count", -1);
      DBCursor cursor = items.find().sort(query).limit(10);
 
      while (cursor.hasNext()) {
      System.out.println(cursor.next());
      }
      }*/
 }
