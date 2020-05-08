 package org.aap.media.crawler;
 
 public class ReadTest {
 	  public static void main(String[] args) {
 	    //RSSReader parser = new RSSReader("http://www.vogella.com/article.rss");
 		//RSSReader parser = new RSSReader("http://www.thehindu.com/news/national/?service=rss");
 	    //Feed feed = parser.readFeed();
 	    //System.out.println(feed);
 	    //for (FeedMessage message : feed.getMessages()) {
 	      //System.out.println(message);
 	    //}
		AllRSSReader allReader = new AllRSSReader();
		allReader.readAllRSS();
 	  }
 	} 
