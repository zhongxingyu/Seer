 /**
  * 
  */
 package net.mysocio.connection.readers.rss;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.mysocio.data.management.JdoDataManager;
 import net.mysocio.data.rss.RssMessage;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.sun.syndication.feed.synd.SyndEntryImpl;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.io.SyndFeedInput;
 import com.sun.syndication.io.XmlReader;
 
 /**
  * @author Aladdin
  *
  */
 public class RSSReader{
	private static final Logger logger = LoggerFactory.getLogger(JdoDataManager.class);
 	private SyndFeed feed;
 	
 	public List<RssMessage> getMessages(String url) {
 		List<SyndEntryImpl> entries = getFeed(url).getEntries();
 		List<RssMessage> messages = new ArrayList<RssMessage>();
 		for (SyndEntryImpl entry : entries) {
 			RssMessage message = new RssMessage();
 			message.setLink(entry.getUri());
 			message.setTitle(entry.getTitle());
 			message.setText(entry.getDescription().getValue());
 			message.setPublishedDate(entry.getPublishedDate().getTime());
 			messages.add(message);
 		}
 		return messages;
 	}
 	
 	/**
 	 * Lazy feed initialization
 	 * @return
 	 */
 	private SyndFeed getFeed(String url){
 		if (feed == null){
 			SyndFeedInput input = new SyndFeedInput();
 			try {
 				feed = input.build(new XmlReader(new URL(url)));
 			} catch (Exception e) {
 				logger.error("Error getting feed" + url,e);
 			}
 		}
 		return feed;
 	}
 }
