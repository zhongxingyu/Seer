 package eu.nanairo_reader.business.service;
 
 import java.util.ArrayList;
 import java.util.List;
 
import eu.nanairo_reader.bean.FeedResult;
 import eu.nanairo_reader.bean.FeedItem;
 import eu.nanairo_reader.business.exception.RssParsingException;
 
 public class RssParsingServiceMock implements RssParsingService {
 	@Override
 	public FeedResult getFeedResult(String rss) throws RssParsingException {
		if(!rss.startsWith("h")){
 			throw new RssParsingException();
 		}
 
 		FeedResult result = new FeedResult();
 
 		result.setTitle("Test Title");
 
 		List<FeedItem> feedItemList = new ArrayList<FeedItem>();
 
 		FeedItem feedItem = new FeedItem();
 		feedItem.setTitle("title1");
 		feedItem.setContent("a<br>b");
 		feedItem.setUri("http://tbn17.com/archives/1764915.html");
 		feedItemList.add(feedItem);
 
 		FeedItem feedItem2 = new FeedItem();
 		feedItem2.setTitle("title2");
 		feedItem2.setContent("aa<br>bb");
 		feedItem2.setUri("http://tbn17.com/archives/1764916.html");
 		feedItemList.add(feedItem2);
 
 		result.setFeedItemList(feedItemList);
 
 		return result;
 	}
 }
