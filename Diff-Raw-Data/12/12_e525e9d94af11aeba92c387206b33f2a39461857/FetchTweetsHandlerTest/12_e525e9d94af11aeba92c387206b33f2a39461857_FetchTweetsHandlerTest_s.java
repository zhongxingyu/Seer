 package com.pk.cwierkacz.processor.handlers;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.pk.cwierkacz.http.Action;
 import com.pk.cwierkacz.http.Status;
 import com.pk.cwierkacz.http.request.Request;
 import com.pk.cwierkacz.http.request.RequestImpl;
 import com.pk.cwierkacz.http.response.FetchTweetsResponse;
 import com.pk.cwierkacz.model.ApplicationData;
 import com.pk.cwierkacz.model.dao.TweetDao;
 import com.pk.cwierkacz.model.service.ServiceRepo;
 import com.pk.cwierkacz.model.service.TweetService;
 import com.pk.cwierkacz.twitter.TwitterActionException;
 import com.pk.cwierkacz.twitter.TwitterAuthenticationException;
 
 public class FetchTweetsHandlerTest extends PopulateData
 {
 
     FetchTweetsHandler fetchTweetsHandler;
     protected TweetService tweetService;
 
     private static final Logger LOGGER = LoggerFactory.getLogger(PublishTweetAccountTest.class);
 
     @Before
     public void setUp( ) throws TwitterAuthenticationException {
         fetchTweetsHandler = new FetchTweetsHandler();
         tweetService = ServiceRepo.getInstance().getService(TweetService.class);
         populateUserAndAccount();
     }
 
     @Test
     public void fetchTweetForAccounts( ) throws TwitterActionException, InterruptedException {
         DateTime now = new DateTime();
         DateTime startDate = now.minusMillis(now.getMillisOfSecond());
         List<TweetDao> tweets = new ArrayList<TweetDao>();
         for ( int i = 0; i < 2; i++ ) {
             System.out.println("adding tweet ......");
             TweetDao t1 = twitterAccount.composeNewTweet("FetchTweetsHandlerTest Tweet " +
                                                          new Date().getTime());
             Thread.sleep(1000);
             System.out.println("adding tweet ......");
             TweetDao t2 = twitterAccount2.composeNewTweet("FetchTweetsHandlerTest Tweet " +
                                                           new Date().getTime());
             Thread.sleep(1000);
             System.out.println("adding tweet ......");
             TweetDao t3 = twitterAccount2.composeNewReplyTweet("FetchTweetsHandlerTest Tweet " +
                                                                        new Date().getTime(),
                                                                t1);
 
             Thread.sleep(1000);
 
             tweetService.save(t1);
             tweetService.save(t2);
             tweetService.save(t3);
             tweets.add(t1);
             tweets.add(t2);
             tweets.add(t3);
         }
 
         for ( int i = 0; i < 2; i++ ) {
             System.out.println("adding tweet ......");
             TweetDao t1 = twitterAccount.composeNewTweet("FetchTweetsHandlerTest Tweet " +
                                                          new Date().getTime());
             Thread.sleep(1000);
             System.out.println("adding tweet ......");
             TweetDao t2 = twitterAccount2.composeNewTweet("FetchTweetsHandlerTest Tweet " +
                                                           new Date().getTime());
             Thread.sleep(1000);
             System.out.println("adding tweet ......");
             TweetDao t3 = twitterAccount.composeNewReplyTweet("FetchTweetsHandlerTest Tweet " +
                                                                       new Date().getTime(),
                                                               t2);
 
             Thread.sleep(1000);
             tweets.add(t1);
             tweets.add(t2);
             tweets.add(t3);
         }
 
         ApplicationData appData = new ApplicationData();
         List<String> accounts = new ArrayList<String>();
         accounts.add(username);
         accounts.add(username2);
 
         Request request = RequestImpl.create(Action.FETCHTWEETS).buildFetchRequest(accounts,
                                                                                    10,
                                                                                    startDate,
                                                                                    -1,
                                                                                    -1);
         appData.setRequest(request);
 
         fetchTweetsHandler.handle(appData);
 
         assertNotNull(appData.getResponse());
         System.out.println("msg: " + appData.getResponse().getMessage());
         assertEquals(Status.OK, appData.getResponse().getStatus());
         FetchTweetsResponse response = (FetchTweetsResponse) appData.getResponse();
 
         assertEquals(2, response.getUsersTweeter().size());
         assertTrue(response.getUsersTweeter().containsKey(twitterAccountDao.getId()));
         assertTrue(response.getUsersTweeter().containsKey(twitterAccountDao2.getId()));
         assertTrue(response.getUsersTweeter().containsValue(twitterAccountDao.getAccountName()));
         assertTrue(response.getUsersTweeter().containsValue(twitterAccountDao2.getAccountName()));
 
         assertEquals(10, response.getTweets().size());
 
         for ( int i = 0; i < 10; i++ ) {
             assertEquals(tweets.get(11 - i), response.getTweets().get(i));
         }
 
     }
 
     @Test
     public void fetchReplyTweetForAccounts( ) throws TwitterActionException, InterruptedException {
         DateTime now = new DateTime();
         DateTime startDate = now.minusMillis(now.getMillisOfSecond());
         List<TweetDao> tweets = new ArrayList<TweetDao>();
 
         TweetDao mainTweet = twitterAccount.composeNewTweet("FetchTweetsHandlerTest Main Tweet " +
                                                             new Date().getTime());
         tweetService.save(mainTweet);
         System.out.println("adding tweet ......");
         Thread.sleep(1000);
 
         for ( int i = 0; i < 2; i++ ) {
             System.out.println("adding tweet ......");
             TweetDao t1 = twitterAccount.composeNewReplyTweet("FetchTweetsHandlerTest Tweet " +
                                                                       new Date().getTime(),
                                                               mainTweet);
             Thread.sleep(1000);
             System.out.println("adding tweet ......");
             TweetDao t2 = twitterAccount2.composeNewReplyTweet("FetchTweetsHandlerTest Tweet " +
                                                                        new Date().getTime(),
                                                                mainTweet);
             Thread.sleep(1000);
 
             tweetService.save(t1);
             tweetService.save(t2);
             tweets.add(t1);
             tweets.add(t2);
         }
 
         for ( int i = 0; i < 2; i++ ) {
             System.out.println("adding tweet ......");
             TweetDao t1 = twitterAccount.composeNewReplyTweet("FetchTweetsHandlerTest Tweet " +
                                                                       new Date().getTime(),
                                                               mainTweet);
             Thread.sleep(1000);
             System.out.println("adding tweet ......");
             TweetDao t2 = twitterAccount2.composeNewReplyTweet("FetchTweetsHandlerTest Tweet " +
                                                                        new Date().getTime(),
                                                                mainTweet);
             Thread.sleep(1000);
             tweets.add(t1);
             tweets.add(t2);
         }
 
         ApplicationData appData = new ApplicationData();
         List<String> accounts = new ArrayList<String>();
         accounts.add(username);
 
         Request request = RequestImpl.create(Action.FETCHTWEETS, token).buildFetchRequest(accounts,
                                                                                           -1,
                                                                                           startDate,
                                                                                           mainTweet.getId(),
                                                                                           -1);
         appData.setRequest(request);
 
         fetchTweetsHandler.handle(appData);
 
         assertNotNull(appData.getResponse());
         System.out.println("msg: " + appData.getResponse().getMessage());
         assertEquals(Status.OK, appData.getResponse().getStatus());
         FetchTweetsResponse response = (FetchTweetsResponse) appData.getResponse();
 
         assertEquals(2, response.getUsersTweeter().size());
         assertTrue(response.getUsersTweeter().containsKey(twitterAccountDao.getId()));
         assertTrue(response.getUsersTweeter().containsValue(twitterAccountDao.getAccountName()));
 
         assertEquals(9, response.getTweets().size());
 
         assertEquals(mainTweet, response.getTweets().get(8));
 
         for ( int i = 0; i < 8; i++ ) {
             assertEquals(tweets.get(7 - i), response.getTweets().get(i));
         }
     }
 
     @Test
     public void fetchRewteetsForAccounts( ) throws TwitterActionException, InterruptedException {
 
         TweetDao mainTweet = twitterAccount.composeNewTweet("FetchTweetsHandlerTest Main Tweet " +
                                                             new Date().getTime());
         tweetService.save(mainTweet);
         System.out.println("adding tweet ......");
         Thread.sleep(1000);
 
         DateTime now = new DateTime();
         DateTime startDate = now.minusMillis(now.getMillisOfSecond());
         List<TweetDao> tweets = new ArrayList<TweetDao>();
 
         System.out.println("adding tweet ......");
         TweetDao t2a = twitterAccount2.composeNewReTweet(mainTweet);
         Thread.sleep(1000);
         tweets.add(t2a);
 
         ApplicationData appData = new ApplicationData();
         List<String> accounts = new ArrayList<String>();
         accounts.add(username);
 
         Request request = RequestImpl.create(Action.FETCHTWEETS, token).buildFetchRequest(accounts,
                                                                                           -1,
                                                                                           startDate,
                                                                                           -1,
                                                                                           mainTweet.getId());
 
         appData.setRequest(request);
 
         fetchTweetsHandler.handle(appData);
 
         assertNotNull(appData.getResponse());
         System.out.println("msg: " + appData.getResponse().getMessage());
         assertEquals(Status.OK, appData.getResponse().getStatus());
         FetchTweetsResponse response = (FetchTweetsResponse) appData.getResponse();
 
        assertEquals(1, response.getUsersTweeter().size());
         assertTrue(response.getUsersTweeter().containsKey(twitterAccountDao.getId()));
         assertTrue(response.getUsersTweeter().containsValue(twitterAccountDao.getAccountName()));
 
         assertEquals(2, response.getTweets().size());
 
         assertEquals(tweets.get(0), response.getTweets().get(0));
        assertEquals(mainTweet, response.getTweets().get(0));
 
     }
 
 }
