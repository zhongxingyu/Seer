 package eu.nanairo_reader.business.service;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import android.util.Log;
 
 import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndContent;
 import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;
 import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
 import com.google.code.rome.android.repackaged.com.sun.syndication.io.FeedException;
 import com.google.code.rome.android.repackaged.com.sun.syndication.io.SyndFeedInput;
 import com.google.code.rome.android.repackaged.com.sun.syndication.io.XmlReader;
 
 import eu.nanairo_reader.business.exception.RssParsingException;
 import eu.nanairo_reader.business.vo.FeedItem;
 import eu.nanairo_reader.business.vo.FeedResult;
 
 public class RssParsingServiceImpl implements RssParsingService {
 	@Override
 	public FeedResult getFeedResult(String rss) throws RssParsingException{
 		Log.i("rss start", rss);
 
 		FeedResult result = new FeedResult();
 		List<FeedItem> feedItemList = new ArrayList<FeedItem>();
 		try {
 			URL feedUrl = new URL(rss);
 			SyndFeedInput input = new SyndFeedInput();
 			SyndFeed feed = input.build(new XmlReader(feedUrl));
 			
 			List<Object> entries = feed.getEntries();
 			Iterator iterator = entries.listIterator();
 			while (iterator.hasNext()) {
 				FeedItem feedItem = new FeedItem();
 				SyndEntry ent = (SyndEntry) iterator.next();
 
 				feedItem.setTitle(ent.getTitle());
 				feedItem.setLink(ent.getUri());
 				feedItem.setPublishedDate(ent.getPublishedDate());
 
 				if(ent.getContents().size() > 0){
 					SyndContent contents = (SyndContent) ent.getContents().get(0);
 					feedItem.setContent(contents.getValue());
 				}
 
 				feedItemList.add(feedItem);
 			}
 			result.setFeedItemList(feedItemList);
 
 		} catch (MalformedURLException e) {
 			// TODO RSSパース例外を考える
 			e.printStackTrace();
			throw new RuntimeException("rss", e);
 		} catch (IllegalArgumentException e) {
 			// TODO RSSパース例外を考える
 			e.printStackTrace();
			throw new RuntimeException("rss", e);
 		} catch (FeedException e) {
 			// TODO RSSパース例外を考える
 			e.printStackTrace();
			throw new RuntimeException("rss", e);
 		} catch (IOException e) {
 			// TODO RSSパース例外を考える
 			e.printStackTrace();
			throw new RuntimeException("rss", e);
 		}
 
 		Log.i("rss end", rss);
 		return result;
 	}
 }
