 package com.pk.cwierkacz.twitter;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormatter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import twitter4j.Paging;
 import twitter4j.Query;
 import twitter4j.QueryResult;
 import twitter4j.Status;
 import twitter4j.StatusUpdate;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.auth.AccessToken;
 
 import com.google.common.collect.ImmutableList;
 import com.pk.cwierkacz.model.dao.TweetDao;
 import com.pk.cwierkacz.model.dao.TwitterAccountDao;
 import com.pk.cwierkacz.twitter.attachment.TweetAttachments;
 import com.pk.cwierkacz.twitter.converters.TweetConverter;
 import com.pk.cwierkacz.utils.DateUtil;
 
 /**
  * Class which represents twitter account - using this account we can for
  * example compose new tweet or search tweet
  * 
  * @author radek
  * 
  */
 public class TwitterAccount
 {
     private static final Logger LOGGER = LoggerFactory.getLogger(TwitterAccount.class);
     private static final DateTimeFormatter DATE_FORMATTER = DateUtil.formatterYyyyMMddUTC();
 
     protected final TwitterAccountDao account;
     protected Twitter twitter;
     protected TweetConverter tweetConverter = new TweetConverter();
 
     public TwitterAccountDao getAccount( ) {
         return account;
     }
 
     /**
      * Initialize a twitter account
      * 
      * @param user
      *            owner of this account
      * @throws TwitterAuthenticationException
      */
     public TwitterAccount( TwitterAccountDao account ) throws TwitterAuthenticationException {
         authenticate(account);
         this.account = account;
     }
 
     /**
      * Method which compose new tweet in Tweeter account
      * 
      * @param msg
      *            textual representation of tweet
      * @throws TwitterActionException
      */
     public TweetDao composeNewTweet( String msg ) throws TwitterActionException {
         try {
             Status stat = twitter.updateStatus(msg);
             return tweetConverter.toTweet(stat);
         }
         catch ( TwitterException e ) {
             LOGGER.error(e.getMessage());
             throw new TwitterActionException(e.getMessage());
         }
     }
 
     /**
      * Method which compose new tweet in Tweeter account
      * 
      * @param msg
      *            textual representation of tweet
      * @param attachments
      *            tweet attachments f.ex. image
      * @throws TwitterActionException
      */
     public TweetDao composeNewTweet( String msg, TweetAttachments attachments ) throws TwitterActionException {
         return customComposeNewTweet(msg, null, attachments);
     }
 
     /**
      * Method which compose new reply tweet in Tweeter account
      * 
      * @param msg
      *            textual representation of tweet
      * @param mainTweetId
      *            identifier of tweet for which we reply
      * @throws TwitterActionException
      */
     public TweetDao composeNewReplyTweet( String msg, TweetDao mainTweet ) throws TwitterActionException {
         return customComposeNewTweet(msg, mainTweet, TweetAttachments.empty());
     }
 
     /**
      * Method which compose new reply tweet in Tweeter account
      * 
      * @param msg
      *            textual representation of tweet
      * @param mainTweetId
      *            identifier of tweet for which we reply
      * @param attachments
      *            tweet attachments f.ex. image
      * @throws TwitterActionException
      */
     public TweetDao composeNewReplyTweet( String msg, TweetDao mainTweet, TweetAttachments attachments ) throws TwitterActionException {
         return customComposeNewTweet(msg, mainTweet, attachments);
     }
 
     protected TweetDao customComposeNewTweet( String msg, TweetDao toReplyTweet, TweetAttachments attachments ) throws TwitterActionException {
         try {
             StatusUpdate update;
             if ( toReplyTweet == null ) {
                 update = new StatusUpdate(msg);
             }
             else {
                 String msgWithTarget = "@" + toReplyTweet.getCreatorName() + " " + msg;
                 update = new StatusUpdate(msgWithTarget);
                 update.setInReplyToStatusId(toReplyTweet.getExternalId());
             }
             if ( attachments.haveImage() )
                 update.setMedia(attachments.getImage().getAttachment());
 
             Status stat = twitter.updateStatus(update);
             return tweetConverter.toTweet(stat);
         }
         catch ( TwitterException e ) {
             LOGGER.error(e.getMessage());
             throw new TwitterActionException(e.getMessage());
         }
     }
 
     /**
      * Method which compose new re tweet in Tweeter account
      * 
      * @param msg
      *            textual representation of tweet
      * @param mainTweetId
      *            identifier of tweet for which we create reTweet
      * @throws TwitterActionException
      */
     public TweetDao composeNewReTweet( TweetDao mainTweet ) throws TwitterActionException {
         try {
             if ( mainTweet.getCreator().getId() == account.getId() )
                 throw new TwitterActionException("You cannot retweet himself");
 
             Status stat = twitter.retweetStatus(mainTweet.getExternalId());
             return tweetConverter.toTweet(stat);
         }
         catch ( TwitterException e ) {
             LOGGER.error(e.getMessage());
             throw new TwitterActionException(e.getMessage());
         }
     }
 
     /**
      * Method which delete existing tweet
      * 
      * @param tweet
      *            which will be deleted
      * @throws TwitterActionException
      */
     public void deleteTweet( TweetDao tweet ) throws TwitterActionException {
         try {
             twitter.destroyStatus(tweet.getId());
         }
         catch ( TwitterException e ) {
             LOGGER.error(e.getMessage(), e);
             throw new TwitterActionException(e.getMessage());
         }
     }
 
     private ImmutableList<TweetDao> statusesToTweets( List<Status> statuses, int size ) {
         ImmutableList.Builder<TweetDao> builder = ImmutableList.builder();
         for ( int i = 0; i < size; i++ ) {
             builder.add(tweetConverter.toTweet(statuses.get(i)));
         }
         ImmutableList<TweetDao> tweets = builder.build();
         return tweets;
     }
 
     /**
      * Method which get all tweets from home time line
      * 
      * @param count
      *            number of tweets which we try get tweets
      * @throws TwitterActionException
      */
     public TweetsResult getTweetsFromHomeTimeline( int count ) throws TwitterActionException {
         return getTweetsFromTimeline(count, TimeLine.Home);
     }
 
     /**
      * Method which get all tweets from home user line
      * 
      * @param count
      *            number of tweets which we try get tweets
      * @throws TwitterActionException
      */
     public TweetsResult getTweetsFromUserTimeline( int count ) throws TwitterActionException {
         return getTweetsFromTimeline(count, TimeLine.User);
     }
 
     /**
      * Method which get all for user which was retweeted
      * 
      * @param count
      *            number of re tweets which we try get
      * @throws TwitterActionException
      */
     public TweetsResult getRetweeted( int count ) throws TwitterActionException {
         return getTweetsFromTimeline(count, TimeLine.Re);
     }
 
     /**
      * Method which get all tweets from mentions line
      * 
      * @param count
      *            number of tweets which we try get tweets
      * @throws TwitterActionException
      */
     public TweetsResult getTweetsFromMentionsTimeline( int count ) throws TwitterActionException {
         return getTweetsFromTimeline(count, TimeLine.Mentions);
     }
 
     protected TweetsResult getTweetsFromTimeline( int count, TimeLine timeLine ) throws TwitterActionException {
         try {
             int c = count / 20 + 1;
             List<Status> statuses = new ArrayList<Status>();
             List<Status> partialStatuses = null;
             for ( int i = 0; i < c && ( partialStatuses == null || partialStatuses.size() == 20 ); i++ ) {
                 partialStatuses = getStatuses(timeLine, new Paging(i + 1, 20));
                 statuses.addAll(partialStatuses);
             }
             return new TweetsResult(partialStatuses.size() == 20, statusesToTweets(statuses, count));
         }
         catch ( TwitterException e ) {
             LOGGER.error(e.getMessage(), e);
             throw new TwitterActionException(e.getMessage());
         }
     }
 
     private boolean currentDateBefore( Date created, DateTime since ) {
         DateTime current = DateUtil.convertDateUTC(created);
         return current.isBefore(since);
     }
 
     /**
      * Method which get all tweets from home time line
      * 
      * @param since
      *            date since which we try get tweets
      * @return
      * @throws TwitterActionException
      */
     public TweetsResult getTweetsFromHomeTimeline( DateTime since ) throws TwitterActionException {
         return getTweetsFromTimeline(since, TimeLine.Home);
     }
 
     /**
      * Method which get all tweets from home user line
      * 
      * @param since
      *            date since which we try get tweets
      * @return
      * @throws TwitterActionException
      */
     public TweetsResult getTweetsFromUserTimeline( DateTime since ) throws TwitterActionException {
         return getTweetsFromTimeline(since, TimeLine.User);
     }
 
     /**
      * Method which get all tweets from mentions time line
      * 
      * @param since
      *            date since which we try get tweets
      * @return
      * @throws TwitterActionException
      */
     public TweetsResult getTweetsFromMentionsTimeline( DateTime since ) throws TwitterActionException {
         return getTweetsFromTimeline(since, TimeLine.Mentions);
     }
 
     /**
      * Method which get all for user which was retweeted
      * 
      * @param since
      *            date since which we try get tweets
      * @throws TwitterActionException
      */
     public TweetsResult getRetweeted( DateTime since ) throws TwitterActionException {
         return getTweetsFromTimeline(since, TimeLine.Re);
     }
 
     protected TweetsResult getTweetsFromTimeline( DateTime since, TimeLine timeLine ) throws TwitterActionException {
         try {
             List<Status> statuses = new ArrayList<Status>();
             List<Status> partialStatuses = null;
             for ( int i = 0; ( partialStatuses == null || partialStatuses.size() == 20 ) &&
                              ( statuses.size() == 0 || !currentDateBefore(statuses.get(statuses.size() - 1)
                                                                                   .getCreatedAt(), since) ); i++ ) {
                 partialStatuses = getStatuses(timeLine, new Paging(i + 1, 20));
                 statuses.addAll(partialStatuses);
             }
             int count = statuses.size();
             for ( int i = statuses.size() - 1; i >= 0 &&
                                                currentDateBefore(statuses.get(i).getCreatedAt(), since); i-- ) {
                 count--;
             }
 
             return new TweetsResult(partialStatuses.size() == 20, statusesToTweets(statuses, count));
         }
         catch ( TwitterException e ) {
             LOGGER.error(e.getMessage(), e);
             throw new TwitterActionException(e.getMessage());
         }
     }
 
     /**
      * Method which get all tweets from home time line
      * 
      * @param sinceTweet
      *            last tweet since which we try get tweets
      * @return
      * @throws TwitterActionException
      */
     public TweetsResult getTweetsFromHomeTimeline( TweetDao sinceTweet ) throws TwitterActionException {
         return getTweetsFromTimeline(sinceTweet, TimeLine.Home);
     }
 
     /**
      * Method which get all tweets from user time line
      * 
      * @param sinceTweet
      *            last tweet since which we try get tweets
      * @return
      * @throws TwitterActionException
      */
     public TweetsResult getTweetsFromUserTimeline( TweetDao sinceTweet ) throws TwitterActionException {
         return getTweetsFromTimeline(sinceTweet, TimeLine.User);
     }
 
     /**
      * Method which get all tweets from mentions time line
      * 
      * @param sinceTweet
      *            last tweet since which we try get tweets
      * @return
      * @throws TwitterActionException
      */
     public TweetsResult getTweetsFromMentionsTimeline( TweetDao sinceTweet ) throws TwitterActionException {
         return getTweetsFromTimeline(sinceTweet, TimeLine.Mentions);
     }
 
     /**
      * Method which get all for user which was retweeted
      * 
      * @param since
      *            last tweet since which we try get tweets
      * @throws TwitterActionException
      */
     public TweetsResult getRetweeted( TweetDao sinceTweet ) throws TwitterActionException {
         return getTweetsFromTimeline(sinceTweet, TimeLine.Re);
     }
 
     protected TweetsResult getTweetsFromTimeline( TweetDao sinceTweet, TimeLine timeLine ) throws TwitterActionException {
         try {
             List<Status> statuses = new ArrayList<Status>();
             List<Status> partialStatuses = null;
 
             for ( int i = 0; ( partialStatuses == null || partialStatuses.size() == 20 ); i++ ) {
                 partialStatuses = getStatuses(timeLine, new Paging(i + 1, 20, sinceTweet.getExternalId()));
                 statuses.addAll(partialStatuses);
             }
 
             int count = statuses.size();
             for ( int i = statuses.size() - 1; i >= 0 &&
                                                currentDateBefore(statuses.get(i).getCreatedAt(),
                                                                  sinceTweet.getCratedDate()); i-- ) {
                 count--;
             }
 
             return new TweetsResult(partialStatuses.size() == 20, statusesToTweets(statuses, count));
         }
         catch ( TwitterException e ) {
             LOGGER.error(e.getMessage(), e);
             throw new TwitterActionException(e.getMessage());
         }
     }
 
     /**
      * Method which get all retweets for tweet
      * 
      * @param retweeted
      *            tweet for which we get re tweets
      * @throws TwitterActionException
      */
     public TweetsResult getRetweets( TweetDao retweeted ) throws TwitterActionException {
         return getRetweets(retweeted, null);
     }
 
     /**
      * Method which get all retweets for tweet
      * 
      * @param retweeted
      *            tweet for which we get re tweets
      * @param since
      *            date since which we try get re tweets
      * @throws TwitterActionException
      */
     public TweetsResult getRetweets( TweetDao retweeted, DateTime since ) throws TwitterActionException {
         try {
             List<Status> stats = twitter.getRetweets(retweeted.getExternalId());
             ImmutableList.Builder<TweetDao> builder = ImmutableList.builder();
             for ( int i = 0; i < stats.size(); i++ ) {
                if ( since != null && !currentDateBefore(stats.get(i).getCreatedAt(), since) )
                     builder.add(tweetConverter.toTweet(stats.get(i)));
             }
             return TweetsResult.full(builder.build());
         }
         catch ( TwitterException e ) {
             LOGGER.error(e.getMessage());
             throw new TwitterActionException(e.getMessage());
         }
     }
 
     /**
      * Method which get all tweets from mentions and user timeline
      * 
      * @param sinceTweet
      *            last tweet since which we try get tweets
      * @return
      * @throws TwitterActionException
      */
     public TweetsResult getTweetsFromMentionsAndUserTimeline( TweetDao sinceTweet ) throws TwitterActionException {
         TweetsResult res1 = getTweetsFromTimeline(sinceTweet, TimeLine.Mentions);
         TweetsResult res2 = getTweetsFromTimeline(sinceTweet, TimeLine.User);
 
         return res1.add(res2);
     }
 
     /**
      * Method which get all tweets from mentions and user timeline
      * 
      * @param since
      *            date since which we try get tweets
      * @return
      * @throws TwitterActionException
      */
     public TweetsResult getTweetsFromMentionsAndUserTimeline( DateTime since ) throws TwitterActionException {
         TweetsResult res1 = getTweetsFromTimeline(since, TimeLine.Mentions);
         TweetsResult res2 = getTweetsFromTimeline(since, TimeLine.User);
 
         return res1.add(res2);
     }
 
     /**
      * Method which get all tweet matching to criteria
      * 
      * @param criteria
      * @throws TwitterActionException
      */
     @Deprecated
     //not tested - probably unnecessary method
     public ImmutableList<TweetDao> searchTweets( QueryCriteria criteria ) throws TwitterActionException {
 
         return searchTweets(prepereQuery(criteria));
     }
 
     protected Query prepereQuery( QueryCriteria criteria ) {
 
         String fromString = "";
         if ( criteria.getFrom() != null )
             fromString = String.format("from:%s", criteria.getFrom().getAccountName());
 
         String toString = "";
         if ( criteria.getTo() != null )
             toString = String.format("to:%s", criteria.getTo().getAccountName());
 
         String sinceString = "";
         if ( criteria.getSinceDate() != null )
             sinceString = String.format("since:%s", DATE_FORMATTER.print(criteria.getSinceDate()));
 
         String untilString = "";
         if ( criteria.getUntilDate() != null )
             untilString = String.format("until:%s", DATE_FORMATTER.print(criteria.getUntilDate()));
 
         String queryString = fromString + " " + toString + " " + sinceString + " " + untilString;
 
         return new Query(queryString);
     }
 
     protected ImmutableList<TweetDao> searchTweets( Query query ) throws TwitterActionException {
         ImmutableList.Builder<TweetDao> builder = ImmutableList.builder();
         QueryResult result;
 
         try {
             do {
                 result = twitter.search(query);
                 for ( Status status : result.getTweets() ) {
                     builder.add(tweetConverter.toTweet(status));
                 }
             }
             while ( ( query = result.nextQuery() ) != null );
 
             return builder.build();
         }
         catch ( TwitterException e ) {
             LOGGER.error(e.getMessage(), e);
             throw new TwitterActionException(e.getMessage());
         }
     }
 
     protected void authenticate( TwitterAccountDao account ) throws TwitterAuthenticationException {
         TwitterFactory factory = new TwitterFactory();
 
         if ( account.isOAuthAvailable() ) {
             AccessToken token = new AccessToken(account.getAccessToken(), account.getAccessTokenSecret());
             twitter = factory.getInstance(token);
         }
         else {
             throw new TwitterAuthenticationException("oAuth authentication impossible");
         }
     }
 
     @Deprecated
     //twitter validation is more sophisticated so we must rely on twitter response
     protected void validateTweetMsg( String msg ) throws TwitterActionException {
         if ( msg.length() > 140 )
             throw new TwitterActionException("Size of tweeter message can't be grather than 140");
     }
 
     //TODO refactor this method : if-else
     private List<Status> getStatuses( TimeLine timeLine, Paging page ) throws TwitterException {
         List<Status> partialStatuses = null;
         if ( timeLine == TimeLine.Home )
             partialStatuses = twitter.getHomeTimeline(page);
         else if ( timeLine == TimeLine.User )
             partialStatuses = twitter.getUserTimeline(page);
         else if ( timeLine == TimeLine.Mentions )
             partialStatuses = twitter.getMentionsTimeline(page);
         else if ( timeLine == TimeLine.Re )
             partialStatuses = twitter.getRetweetsOfMe(page);
         else
             throw new UnsupportedOperationException("Time Line " + timeLine + " not supported");
 
         return partialStatuses;
     }
 
     enum TimeLine {
         Home,
         User,
         Mentions,
         Re
     }
 }
