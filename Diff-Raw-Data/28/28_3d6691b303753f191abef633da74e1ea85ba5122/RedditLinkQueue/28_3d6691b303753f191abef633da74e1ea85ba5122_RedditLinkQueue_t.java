 package net.acuttone.reddimg;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.text.Html;
 import android.util.Log;
 
 public class RedditLinkQueue {
 
 	private List<RedditLink> links;
 	private List<String> lastT3List;
 	private List<String> subredditsList;
 	private int lastRequestedIndex;
 	private int nextSubredditIndex;
 	
 	public RedditLinkQueue() {
 		initSubreddits();
 	}
 	
 	public synchronized void initSubreddits() {
 		links = new ArrayList<RedditLink>();
 		lastT3List = new ArrayList<String>();
 		subredditsList = new ArrayList<String>();
 
 		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(RedditApplication.instance());
 		String mode = sp.getString(PrefsActivity.SUBREDDIT_MODE_KEY, PrefsActivity.SUBREDDITMODE_FRONTPAGE);
 		if (PrefsActivity.SUBREDDITMODE_MINE.equals(mode) &&
 			RedditApplication.instance().getRedditClient().isLoggedIn()) {
 			subredditsList = RedditApplication.instance().getRedditClient().getMySubreddits();
 		} else if (PrefsActivity.SUBREDDITMODE_MANUAL.equals(mode)) {
 			subredditsList = SubredditsPickerActivity.getSubredditsFromPref(RedditApplication.instance());
 		}
 
 		if (subredditsList.isEmpty()) {
 			subredditsList.add("");
 		}
 		for (String s : subredditsList) {
 			lastT3List.add("");
 		}
 		lastRequestedIndex = 0;
 		nextSubredditIndex = 0;
 	}
 
 	public synchronized RedditLink get(int index) {
 		lastRequestedIndex = index;
 		if(index >= links.size()) {
 			return null;
 		} else {
 			return links.get(index);
 		}
 	}
 	
 	public RedditLink getForPrefetch(int index) {
 		// links does not need synch here since it is accessed only by the prefetching thread
 		if(index >= links.size()) {
 			getNewLinks();
 		}
 		return index >= links.size() ? null : links.get(index);
 	}
 
 	private void getNewLinks() {
 		String subreddit = subredditsList.get(nextSubredditIndex);
 		String lastT3 = lastT3List.get(nextSubredditIndex);
 		Log.d(RedditApplication.APP_NAME, "Fetching links from " + (subreddit.length() == 0 ? "reddit front page" : subreddit));
		List<RedditLink> newLinks = new ArrayList<RedditLink>();
		lastT3 = RedditApplication.instance().getRedditClient().getLinks(newLinks, subreddit, lastT3);
 		if (lastT3 != null && !lastT3.equals("")) {
 			lastT3List.set(nextSubredditIndex, lastT3);
 		}
 
 		nextSubredditIndex++;
 		if (nextSubredditIndex >= subredditsList.size()) {
 			nextSubredditIndex = 0;
 		}
 
 		synchronized (links) {
 			for (RedditLink l : newLinks) {
 				if (links.contains(l) == false) {
 					links.add(l);
 				}
 			}
 		}
 	}
 
 	public synchronized int getLastRequestedIndex() {
 		return lastRequestedIndex;
 	}
 
 	public synchronized void removeUrl(String targetUrl) {
 		for(Iterator<RedditLink> iter = links.iterator(); iter.hasNext();) {
 			if(iter.next().getUrl().equals(targetUrl)) {
 				iter.remove();
 				break;
 			}
 		}
 	}
 	
 }
