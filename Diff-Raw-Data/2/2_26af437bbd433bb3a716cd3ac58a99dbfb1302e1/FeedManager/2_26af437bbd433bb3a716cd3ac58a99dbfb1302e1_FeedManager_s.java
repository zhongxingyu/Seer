 package com.example.mad2013_itslearning;
 
 import java.util.ArrayList;
 import org.mcsoxford.rss.RSSFeed;
 import org.mcsoxford.rss.RSSItem;
 import android.util.Log;
 
 /**
  * @author marcusmansson
  * 
  * FeedManager is responsible for managing several feeds, fetching 
  * them sequentially and creating article objects of the items   
  * found.
  * 
  * FeedManager will return all articles to the registered 
  * FeedCompleteListener when done.
  * 
  * Usage:
  *  
  * FeedManager fm = new FeedManager(this);
  *  
  * fm.addFeedURL(url); // for all urls you want to process, then
  *  
  * fm.processFeeds();
  * 
  */
 public class FeedManager implements FeedDownloadTask.FeedCompleteListener
 {
 	private final String TAG = "RSSTEST";
 	private FeedDownloadTask downloadTask;
 	private FeedManagerDoneListener callbackHandler;
 	private ArrayList<Article> articleList;
 	private ArrayList<String> feedList;
 	private int feedQueueCounter;
 
 	/*
	 * the listener must implement this methods
 	 */
 	public interface FeedManagerDoneListener
 	{
 		public void onFeedManagerDone(ArrayList<Article> articles);
 		public void onFeedManagerProgress(int progress, int max);
 	}
 
 	public FeedManager(FeedManagerDoneListener callbackHandler)
 	{
 		try
 		{
 			this.callbackHandler = (FeedManagerDoneListener) callbackHandler;
 		}
 		catch (ClassCastException e)
 		{
 			throw new ClassCastException(callbackHandler.toString() 
 					+ " must implement FeedManagerDoneListener");
 		}
 
 		articleList = new ArrayList<Article>();
 		feedList = new ArrayList<String>();
 		feedQueueCounter = 0;
 	}
 
 	public void addFeedURL(String url) // throws MalformedURLException
 	{
 		/*
 		 *  even though we use simple strings, we should throw  
 		 *  exception if the string is not a valid url for sake 
 		 *  of finding errors
 		try
 		{
 			URL test = new URL(url);
 		}
 		catch (MalformedURLException e)
 		{
 			throw e;
 		}
 		 */
 
 		this.feedList.add(url);
 	}
 
 	public int queueSize()
 	{
 		return feedList.size();
 	}
 	
 	public void removeFeedURL(String url)
 	{
 		feedList.remove(url);
 	}
 	
 	/**
 	 * prepare for re-processing of feeds list;
 	 * clears article list and resets feed queue
 	 * 
 	 */
 	public void reset() {
 		feedQueueCounter = 0;
 		articleList.clear();
 	}
 	
 	
 	public ArrayList<Article> getArticles()
 	{
 		return articleList;
 	}
 	
 	@Override
 	public void onFeedComplete(RSSFeed feed)
 	{
 		
 		if (downloadTask.hasException())
 		{
 			Log.e(TAG, downloadTask.getException().toString());
 		}
 		else
 		{
 			Article article;
 			for (RSSItem rssItem : feed.getItems())
 			{
 				article = new Article(rssItem);
 				articleList.add(article);
 				article.setArticleCourseCode(Integer.toString(feedQueueCounter));
 			}
 		}
 		
 		if (feedQueueCounter < this.feedList.size())
 		{
 			/*
 			 *  process next feed in queue
 			 */
 			processFeeds();
 		}
 		else
 		{
 			/*
 			 * all feeds have been read so let's reset counter. 
 			 * this way it's possible to call processFeeds() 
 			 * again if we just want to refresh articleList.
 			 */
 			feedQueueCounter = 0;
 
 	        /*
 			 *  return the complete list of articles to the listener
 			 *  when all items in the feed queue are processed
 			 */
 			callbackHandler.onFeedManagerDone(getArticles());
 		}
 	}
 
 	/**
 	 * downloads articles from one feed at a time, you must add feeds 
 	 * using addFeedURL(String url) before calling this method
 	 */
 	public void processFeeds()
 	{
 		if (this.feedList.isEmpty())
 		{
 			Log.e(TAG, "Feed list is empty, nothing to do!");
 			return;
 		}
 
 		/*
 		 * notify the UI of update
 		 */
 		callbackHandler.onFeedManagerProgress(feedQueueCounter + 1, feedList.size());
 						
 		/* 
 		 * there can only be one task at any time and it can only be used once
 		 */
 		downloadTask = new FeedDownloadTask(this);
 		
 		/* 
 		 * we want to process the next url in queue but not pop it from the queue,
 		 * in case we want to get all feeds again later (i.e. to refresh), that's
 		 * why we use a counter to keep track of where in the queue we are 
 		 */
 		downloadTask.execute(feedList.get(feedQueueCounter++));
 	}
 }
