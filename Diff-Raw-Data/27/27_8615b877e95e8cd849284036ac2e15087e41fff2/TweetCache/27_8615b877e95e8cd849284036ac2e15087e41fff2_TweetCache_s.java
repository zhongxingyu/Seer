 package com.vaguehope.senkyou.twitter;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicReference;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 import java.util.logging.Logger;
 
 import twitter4j.Paging;
 import twitter4j.ResponseList;
 import twitter4j.Status;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.vaguehope.senkyou.Config;
 import com.vaguehope.senkyou.model.Tweet;
 import com.vaguehope.senkyou.model.TweetList;
 
 public class TweetCache {
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 	
 	private static final Logger LOG = Logger.getLogger(TweetCache.class.getName());
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 //	Cache.
 	
 	private AtomicReference<TweetList> homeTimeline = new AtomicReference<TweetList>();
 	private final ReadWriteLock homeTimelineLock = new ReentrantReadWriteLock();
 	
 	private AtomicReference<TweetList> mentionsTimeline = new AtomicReference<TweetList>();
 	private final ReadWriteLock mentionsTimelineLock = new ReentrantReadWriteLock();
 	
 	private final LoadingCache<Long, Tweet> tweetCache;
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 //	Config.
 	
 	private final String username;
 	private final Twitter twitter;
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 
 	public TweetCache (String username) throws ExecutionException {
 		this.username = username;
 		this.twitter = TwitterFactory.getTwitter(username);
 		
 		this.tweetCache = CacheBuilder.newBuilder()
 				.maximumSize(Config.USER_TWEET_CACHE_COUNT_MAX)
 				.softValues()
 				.expireAfterAccess(Config.USER_TWEET_CACHE_AGE_MAX, TimeUnit.MINUTES)
 				.build(new TweetLoader(this.twitter));
 	}
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 	
 	public TweetList getHomeTimeline (int minCount) throws TwitterException {
 		return getTweetList(this.username, this.twitter, TwitterFeeds.HOME_TIMELINE, this.homeTimelineLock, this.homeTimeline, minCount, Config.MAX_HOME_TIMELINE_AGE);
 	}
 	
 	public TweetList getMentions (int minCount) throws TwitterException {
 		return getTweetList(this.username, this.twitter, TwitterFeeds.MENTIONS, this.mentionsTimelineLock, this.mentionsTimeline, minCount, Config.MAX_MENTIONS_AGE);
 	}
 	
 	public TweetList getLastTweetHomeTimeline (int searchDepth) throws TwitterException {
 		TweetList source = getHomeTimeline(searchDepth);
 		TweetList target = new TweetList();
 		target.setTime(source.getTime());
 		copyFirstTweetOfEachUser(source, target);
 		return target;
 	}
 	
 	/**
 	 * TODO Pass in list form last call to reuse tips where needed.
 	 * 
 	 * @param searchDepth How fat back in user's time-line to search for tips to threads.
 	 * @param maxThreads Stop searching after fixing this many threads.
 	 */
 	public TweetList getThreads (int searchDepth, int maxThreads) throws TwitterException, ExecutionException {
 		TweetList mentions = getMentions(Config.TWEET_FETCH_PAGE_SIZE);
 		TweetList timeline = getHomeTimeline(searchDepth);
 		
 		/* This is not quite right yet.
 		 * Map well end up with less than intended threads.
 		 * Might need to do iterative searches for tips as the tree is built.
 		 */
 		
 		Map<Long, Tweet> tree = Maps.newHashMap(); // Will contain everything in output.
 		SortedSet<Tweet> tips = Sets.newTreeSet(Tweet.Comp.NEWEST_FIRST);
		findTips(mentions, tips, maxThreads);
		findTips(timeline, tips, maxThreads);
		Set<Tweet> heads = buildTweetTree(tips, tree, this.tweetCache);
 		attachStrays(tree, mentions);
 		attachStrays(tree, timeline);
 		
 		TweetList ret = new TweetList();
 		limitedCopy(heads, ret, maxThreads);
 		return ret;
 	}
 
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 //	Static implementation.
 	
 	private static void copyFirstTweetOfEachUser (TweetList source, TweetList target) {
 		Set<String> users = new HashSet<String>();
 		for (Tweet t : source.getTweets()) {
 			String u = t.getUser();
 			if (!users.contains(u)) {
 				target.addTweet(t);
 				users.add(u);
 			}
 		}
 	}
 	
 	private static TweetList getTweetList (String u, Twitter t, TwitterFeeds feed, ReadWriteLock lock, AtomicReference<TweetList> list, int minCount, long maxAge) throws TwitterException {
 		lock.readLock().lock();
 		try {
 			if (expired(list.get(), maxAge)) {
 				lock.readLock().unlock();
 				lock.writeLock().lock();
 				try {
 					if (expired(list.get(), maxAge)) {
 						TweetList timeline = fetchTwitterFeed(u, t, feed, minCount);
 						list.set(timeline);
 					}
 				}
 				finally {
 					lock.readLock().lock();
 					lock.writeLock().unlock();
 				}
 			}
 			return list.get();
 		}
 		finally {
 			lock.readLock().unlock();
 		}
 	}
 	
 	private static boolean expired (TweetList list, long maxAge) {
 		return list == null || list.getTime() + maxAge < System.currentTimeMillis();
 	}
 	
 	/**
 	 * TODO Pass in list form last call to reuse where possible.
 	 */
 	private static TweetList fetchTwitterFeed (String u, Twitter t, TwitterFeed feed, int minCount) throws TwitterException {
 		long startTime = System.currentTimeMillis();
 		
 		TweetList ret = new TweetList();
 		int page = 1; // First page is 1.
 		while (ret.tweetCount() < minCount) {
 			Paging paging = new Paging(page, Config.TWEET_FETCH_PAGE_SIZE);
 			ResponseList<Status> timelinePage = feed.getTweets(t, paging);
 			if (timelinePage.size() < 1) break;
 			addTweetsToList(ret, timelinePage);
 			page++;
 		}
 		
 		LOG.info("Fetched " + feed.getName() + " for " + u + " in " + (System.currentTimeMillis() - startTime) + " millis.");
 		return ret;
 	}
 	
 	private static interface TwitterFeed {
 		public String getName ();
 		public ResponseList<Status> getTweets (Twitter t, Paging paging) throws TwitterException;
 	}
 	
 	private static enum TwitterFeeds implements TwitterFeed {
 		HOME_TIMELINE {
 			@Override public String getName () {return "home timeline"; }
 			@Override public ResponseList<Status> getTweets (Twitter t, Paging paging) throws TwitterException { return t.getHomeTimeline(paging); }
 		},
 		MENTIONS {
 			@Override public String getName () {return "mentions"; }
 			@Override public ResponseList<Status> getTweets (Twitter t, Paging paging) throws TwitterException { return t.getMentions(paging); }
 		};
 		@Override public abstract String getName ();
 		@Override public abstract ResponseList<Status> getTweets (Twitter t, Paging paging) throws TwitterException;
 	}
 	
 	protected static Tweet fetchTweet (Twitter t, long id) throws TwitterException {
 		long startTime = System.currentTimeMillis();
 		
 		Status s = t.showStatus(id);
 		Tweet tweet = convertTweet(s);
 		
 		LOG.info("Fetched tweet " + id + " in " + (System.currentTimeMillis() - startTime) + " millis.");
 		return tweet;
 		
 	}
 	
 	private static class TweetLoader extends CacheLoader<Long, Tweet> {
 		
 		private final Twitter t;
 		
 		public TweetLoader (Twitter t) {
 			this.t = t;
 		}
 		
 		@Override
 		public Tweet load (Long id) throws TwitterException {
 			return fetchTweet(this.t, id.longValue());
 		}
 		
 	}
 	
 	private static void addTweetsToList (TweetList list, ResponseList<Status> tweets) {
 		for (Status status : tweets) {
 			Tweet tweet = convertTweet(status);
 			list.addTweet(tweet);
 		}
 	}
 
 	private static Tweet convertTweet (Status s) {
 		Tweet t = new Tweet();
 		t.setId(s.getId());
 		t.setCreatedAt(s.getCreatedAt());
 		t.setInReplyId(s.getInReplyToStatusId());
 		t.setUser(s.getUser().getScreenName());
 		t.setName(s.getUser().getName());
 		t.setBody(s.getText());
 		return t;
 	}
 	
	private static void findTips (TweetList timeline, Collection<Tweet> out, int limit) {
 		for (Tweet t : timeline.getTweets()) {
			if (t.getInReplyId() > 0) {
				if (out.size() < limit) {
					out.add(t);
				}
			}
 		}
 	}
 	
	private static Set<Tweet> buildTweetTree (Set<Tweet> tips, Map<Long, Tweet> tree, LoadingCache<Long, Tweet> cache) throws ExecutionException {
 		Set<Tweet> heads = Sets.newLinkedHashSet(); // Keep the order they are added in.
 		for (Tweet tip : tips) {
 			tree.put(Long.valueOf(tip.getId()), tip);
 			Tweet tweet = tip;
			search: while (true) {
 				Tweet parent = findTweet(tweet.getInReplyId(), tree, cache);
 				if (parent != null) {
 					parent.addReply(tweet);
 					tweet = parent;
 				}
 				else {
 					heads.add(tweet);
					break search;
 				}
 			}
 		}
 		return heads;
 	}
 	
 	/**
 	 * I have no idea if this is actually useful.
 	 * But it sort of makes sense at the moment.
 	 */
 	private static void attachStrays (Map<Long, Tweet> tree, TweetList list) {
 		int n = 0;
 		for (Tweet t : list.getTweets()) {
 			if (t.getInReplyId() > 0) {
 				Long inReplyId = Long.valueOf(t.getInReplyId());
 				Tweet parent = tree.get(inReplyId);
 				if (parent != null) {
 					if (parent.addReply(t)) {
 						n++;
 						if (!tree.containsKey(inReplyId)) {
 							tree.put(inReplyId, t);
 						}
 					}
 				}
 			}
 		}
 		LOG.info("Attached " + n + " strays."); // Does this method even find anything?
 	}
 	
 	private static Tweet findTweet(long id, Map<Long, Tweet> tree, LoadingCache<Long, Tweet> cache) throws ExecutionException {
 		if (id < 1) return null;
 		Long lid = Long.valueOf(id);
 		Tweet t = tree.get(lid);
 		if (t != null) return t;
 		t = cache.get(lid);
 		if (t != null) tree.put(lid, t);
 		return t;
 	}
 	
 	private static void limitedCopy (Set<Tweet> heads, TweetList ret, int max) {
 		int n = 0;
 		for (Tweet head : heads) {
 			ret.addTweet(head);
 			n++;
 			if (n >= max) break;
 		}
 	}
 	
 }
