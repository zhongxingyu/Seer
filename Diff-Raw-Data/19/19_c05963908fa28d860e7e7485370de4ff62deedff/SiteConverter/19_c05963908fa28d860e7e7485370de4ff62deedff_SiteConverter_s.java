 package com.cee.news.server.content;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.cee.news.client.content.FeedData;
 import com.cee.news.client.content.SiteData;
 import com.cee.news.model.EntityKey;
 import com.cee.news.model.Feed;
 import com.cee.news.model.Site;
 
 public class SiteConverter {
 
 	private SiteConverter() {}
 	
 	public static FeedData createFromFeed(final Feed feed) {
 		FeedData data = new FeedData();
 		data.setIsNew(true);
 		data.setIsActive(feed.isActive());
 		data.setLocation(feed.getLocation());
 		data.setTitle(feed.getTitle());
		data.setLanguage(EntityKey.get(feed.getLanguage()));
 		return data;
 	}
 	
 	public static SiteData createFromSite(final Site site) {
 		SiteData data = new SiteData();
 		data.setIsNew(true);
 		data.setDescription(site.getDescription());
 		data.setLocation(site.getLocation());
 		data.setName(site.getName());
 		data.setTitle(site.getTitle());
		data.setLanguage(EntityKey.get(site.getLanguage()));
 		List<FeedData> feeds = new ArrayList<FeedData>();
 		for (Feed feed : site.getFeeds()) {
 			feeds.add(createFromFeed(feed));
 		}
 		data.setFeeds(feeds);
 		return data;
 	}
 	
 	public static Feed createFromFeedData(FeedData data) {
 		Feed feed = new Feed();
 		feed.setActive(data.getIsActive());
 		feed.setLocation(data.getLocation());
 		feed.setTitle(data.getTitle());
 		feed.setLanguage(data.getLanguage().getKey());
 		return feed;
 	}
 	
 	public static Site createFromSiteData(SiteData data) {
 		Site site = new Site();
 		site.setDescription(data.getDescription());
 		site.setLocation(data.getLocation());
 		site.setName(data.getName());
 		site.setTitle(data.getTitle());
 		site.setLanguage(data.getLanguage().getKey());
 		List<Feed> feeds = new ArrayList<Feed>();
 		for (FeedData feed : data.getFeeds()) {
 			feeds.add(createFromFeedData(feed));
 		}
 		site.setFeeds(feeds);
 		return site;
 	}
 	
 }
