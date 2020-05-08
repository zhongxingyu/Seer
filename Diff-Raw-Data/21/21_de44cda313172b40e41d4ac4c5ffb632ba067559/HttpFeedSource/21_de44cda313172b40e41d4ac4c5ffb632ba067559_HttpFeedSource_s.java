 package com.example;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Glenn Bech
  */
 class HttpFeedSource implements FeedSource {
 
    protected static final String URL = "http://apod.nasa.gov/apod.rss";
 
     public List<RSSItem> getFeed() {
         List<RSSItem> itemList = new ArrayList<RSSItem>();
 
         NewsParser parser = new NewsParser(URL);
         parser.parse();
         NewsParser.RssFeed feed = parser.getFeed();
 
         for (NewsParser.Item i : feed.getItems()) {
            itemList.add(new RSSItem(i.getTitle(), i.getUrl()));
         }
         return itemList;
     }
 }
