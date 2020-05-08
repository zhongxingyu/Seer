 package eu.nanairo_reader.business.service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.horrabin.horrorss.RssChannelBean;
 import org.horrabin.horrorss.RssFeed;
 import org.horrabin.horrorss.RssItemBean;
 import org.horrabin.horrorss.RssParser;
 
 import android.util.Log;
 import eu.nanairo_reader.business.exception.RssParsingException;
 import eu.nanairo_reader.business.vo.FeedItem;
 import eu.nanairo_reader.business.vo.FeedResult;
 
 public class RssParsingService2Impl implements RssParsingService {
 	@Override
 	public FeedResult getFeedResult(String url) throws RssParsingException {
 		Log.i("rss start", url);
 
 		RssParser rss = new RssParser();
 
 		try {
 			FeedResult result = new FeedResult();
 			List<FeedItem> feedItemList = new ArrayList<FeedItem>();
 			RssFeed feed = rss.load(url);
 
 			RssChannelBean channel = feed.getChannel();
 			result.setTitle(channel.getTitle());
 
 			List<RssItemBean> items = feed.getItems();
 			for (RssItemBean item : items) {
 				FeedItem feedItem = new FeedItem();
 				feedItem.setTitle(item.getTitle());
 				feedItem.setLink(item.getLink());
 				feedItem.setPublishedDate(item.getPubDate());
 				feedItem.setContent(item.getDescription());
 				feedItemList.add(feedItem);
 			}
 			result.setFeedItemList(feedItemList);
 
 			Log.i("rss end", url);
 			return result;
 
 		} catch (Exception e) {
			// Something to do if an exception occurs
			throw new RuntimeException("rss", e);
 		}
 	}
 }
