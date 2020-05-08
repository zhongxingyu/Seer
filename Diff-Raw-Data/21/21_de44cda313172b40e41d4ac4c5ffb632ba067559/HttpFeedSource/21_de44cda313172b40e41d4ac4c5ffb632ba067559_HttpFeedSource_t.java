 package com.example;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Glenn Bech
  */
 class HttpFeedSource implements FeedSource {
 
    protected static final String URL = "http://rss.cnn.com/rss/edition_world.rss";
 
     public List<RSSItem> getFeed() {
         List<RSSItem> itemList = new ArrayList<RSSItem>();
 
         NewsParser parser = new NewsParser(URL);
         parser.parse();
         NewsParser.RssFeed feed = parser.getFeed();
 
         for (NewsParser.Item i : feed.getItems()) {
            itemList.add(new RSSItem(i.getTitle(), i.getTitle()));
         }
         return itemList;
     }
 }
