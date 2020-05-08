 package com.vaguehope.senkyou.twitter;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
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
 import com.vaguehope.senkyou.Config;
 import com.vaguehope.senkyou.model.ThreadList;
 import com.vaguehope.senkyou.model.Tweet;
 import com.vaguehope.senkyou.model.TweetList;
 
 public class TweetCache {
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 	
 	private static final Logger LOG = Logger.getLogger(TweetCache.class.getName());
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 //	Cache.
 	
 	private AtomicReference<TweetList> homeTimeline = new AtomicReference<TweetList>();
 	private final ReadWriteLock homeTimelineLock = new ReentrantReadWriteLock();
 	
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
 		return getTweetList(this.username, this.twitter, this.homeTimelineLock, this.homeTimeline, minCount, Config.MAX_HOME_TIMELINE_AGE);
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
 	public ThreadList getThreads (int searchDepth, int maxThreads) throws TwitterException, ExecutionException {
 		TweetList timeline = getHomeTimeline(searchDepth);
 		
 		// TODO detect overlapping threads.
 		// TODO use mentions feed to find threads.
 		// TODO detect splitting threads.
 		
 		List<Tweet> tips = new LinkedList<Tweet>();
 		for (Tweet t : timeline.getTweets()) {
 			if (t.getInReplyId() > 0) {
 				tips.add(t);
 				if (tips.size() >= maxThreads) break;
 			}
 		}
 		
 		ThreadList ret = new ThreadList();
 		for (Tweet tip : tips) {
 			TweetList thread = getThreadFromTip(tip);
 			ret.addThread(thread);
 		}
 		
 		return ret;
 	}
 	
 	public TweetList getThreadFromTip (Tweet tip) throws ExecutionException {
 		if (tip.getInReplyId() <= 0) throw new IllegalArgumentException("Tip is not a reply.");
 		
 		List<Tweet> thread = new ArrayList<Tweet>(); // TODO right list type?
 		Tweet head = tip;
 		while (head != null) {
 			thread.add(head);
 			long inReplyId = head.getInReplyId();
 			if (inReplyId > 0) {
 				head = this.tweetCache.get(Long.valueOf(inReplyId));
 			}
 			else {
 				break;
 			}
 		}
 		
 		Collections.reverse(thread);
 		TweetList ret = new TweetList();
 		ret.addAdd(thread);
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
 	
 	private static TweetList getTweetList (String u, Twitter t, ReadWriteLock lock, AtomicReference<TweetList> list, int minCount, long maxAge) throws TwitterException {
 		lock.readLock().lock();
 		try {
 			if (expired(list.get(), maxAge)) {
 				lock.readLock().unlock();
 				lock.writeLock().lock();
 				try {
 					if (expired(list.get(), maxAge)) {
 						TweetList timeline = fetchHomeTimeline(u, t, minCount);
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
 	private static TweetList fetchHomeTimeline (String u, Twitter t, int minCount) throws TwitterException {
 		long startTime = System.currentTimeMillis();
 		
 		TweetList ret = new TweetList();
 		int page = 1; // First page is 1.
 		while (ret.tweetCount() < minCount) {
 			Paging paging = new Paging(page, Config.TWEET_FETCH_PAGE_SIZE);
 			ResponseList<Status> timelinePage = t.getHomeTimeline(paging);
 			if (timelinePage.size() < 1) break;
 			addTweetsToList(ret, timelinePage);
 			page++;
 		}
 		
 		LOG.info("Fetched home timeline for " + u + " in " + (System.currentTimeMillis() - startTime) / 1000 + " seconds.");
 		return ret;
 	}
 	
 	protected static Tweet fetchTweet (Twitter t, long id) throws TwitterException {
 		long startTime = System.currentTimeMillis();
 		
 		Status s = t.showStatus(id);
 		Tweet tweet = convertTweet(s);
 		
 		LOG.info("Fetched tweet " + id + " in " + (System.currentTimeMillis() - startTime) / 1000 + " seconds.");
 		return tweet;
 		
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
 	
 }
