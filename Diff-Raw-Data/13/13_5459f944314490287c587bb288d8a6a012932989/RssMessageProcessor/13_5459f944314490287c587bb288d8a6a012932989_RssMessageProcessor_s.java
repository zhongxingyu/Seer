 /**
  * 
  */
 package net.mysocio.connection.rss;
 
 import java.util.List;
 
 import net.mysocio.data.management.MessagesManager;
 import net.mysocio.data.management.camel.UserMessageProcessor;
 import net.mysocio.data.messages.rss.RssMessage;
 import net.mysocio.utils.rss.RssUtils;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.code.morphia.annotations.Transient;
 import com.sun.syndication.feed.synd.SyndContent;
 import com.sun.syndication.feed.synd.SyndEntryImpl;
 import com.sun.syndication.feed.synd.SyndFeed;
 
 /**
  * @author Aladdin
  *
  */
 public class RssMessageProcessor extends UserMessageProcessor {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6216755406044921125L;
 	@Transient
 	static final Logger logger = LoggerFactory.getLogger(RssMessageProcessor.class);
 	private String url;
 	
 	public void process() throws Exception {
 		if (logger.isDebugEnabled()){
 			logger.debug("Trying to get messages for feed: " + url);
 		}
 		SyndFeed feed = RssUtils.buldFeed(url);
 		if (feed == null){
 			return;
 		}
 		List<SyndEntryImpl> entries = feed.getEntries();
     	if (logger.isDebugEnabled()){
 			logger.debug("Got " + entries.size() + " messages for feed: " + feed.getTitle());
 		}
     	processMessages(feed, entries);
 	}
 
 	protected void processMessages(SyndFeed feed, List<SyndEntryImpl> entries) throws Exception {
 		for (SyndEntryImpl entry : entries) {
     		RssMessage message = new RssMessage();
     		message.setFeedTitle(feed.getTitle());
     		String language = feed.getLanguage();
     		if (language != null){
     			message.setLanguage(language);
     		}
     		processMessage(entry, message);
 		}
 	}
 
 	protected void processMessage(SyndEntryImpl entry, RssMessage message) throws Exception {
		message.setLink(entry.getLink());
 		if (entry.getPublishedDate() != null){
 			message.setDate(entry.getPublishedDate().getTime());
 		}else{
 			if (entry.getUpdatedDate() != null){
 				message.setDate(entry.getUpdatedDate().getTime());
 			}else{
 				message.setDate(System.currentTimeMillis());
 			}
 		}
 		String title = entry.getTitle();
 		if (title == null){
 			title = "";
 		}
 		message.setTitle(title);
 		SyndContent description = entry.getDescription();
 		if (description == null){
 			//if message has no text - we ignore it
 			return;
 		}
 		String text = description.getValue();
 		message.setText(text);
 		
 		if (logger.isDebugEnabled()){
 			logger.debug("Message title: " + title);
 			logger.debug("Message text: " + text);
 		}
 		try {
 			MessagesManager.getInstance().storeMessage(message);
 		} catch (Exception e) {
 			//if it's duplicate message - we ignore it
 		}
 		addMessageForTag(message, RssMessage.class, url);
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result + ((url == null) ? 0 : url.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (!super.equals(obj))
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		RssMessageProcessor other = (RssMessageProcessor) obj;
 		if (url == null) {
 			if (other.url != null)
 				return false;
 		} else if (!url.equals(other.url))
 			return false;
 		return true;
 	}
 }
