 package me.hujin.rss.parser;
 
 import java.io.InputStream;
 import java.util.List;
 import java.util.ListIterator;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import me.hujin.rss.storage.FeedDataSource;
 import me.hujin.rss.storage.ItemDataSource;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.xml.sax.InputSource;
 import org.xml.sax.XMLReader;
 
 import android.content.Context;
 import android.util.Log;
 
 
 public class FeedParser {
 	
 	private IParserListener listener;
 	
 	private FeedParserHandler handler; 
 	
 	private SAXParser parser;
 	
 	private ClientConnectionManager connManager;
 	
 	private long lastBuildDate = 0;
 	
 	private long lastItemDate = 0;
 	
 	private Context context;
 	
 	private ItemDataSource itemDataSource;
 	
 	public FeedParser(Context context) {
 		this.context = context;
 		itemDataSource = new ItemDataSource(context);
 	}
 	
 	public void init() {
 		if(parser != null) {
 			return;
 		}
 		
 		if(listener == null) {
 			listener = new ParserListener(lastBuildDate, lastItemDate);
 		}
 		handler = new FeedParserHandler(this, listener);
 	}
 	
 	public FeedParser setLastBuildDate(long lastBuildDate) {
 		this.lastBuildDate = lastBuildDate;
 		return this;
 	}
 	
 	public FeedParser setLastItemDate(long lastItemDate) {
 		this.lastItemDate = lastItemDate;
 		return this;
 	}
 	
 	public void load(String url) {
 		init();
 		HttpClient client = new DefaultHttpClient();
 		HttpGet request = new HttpGet(url);
 		connManager = client.getConnectionManager();
 		try {
 			HttpResponse response = client.execute(request);
 			
 			InputStream stream = response.getEntity().getContent();
 			
 			SAXParserFactory factory = SAXParserFactory.newInstance();
 			try {
 				parser = factory.newSAXParser();
 			} catch (ParserConfigurationException e) {
 				e.printStackTrace();
 			}
 			XMLReader xmlreader = parser.getXMLReader();
 			xmlreader.setContentHandler(handler);
 			InputSource is = new InputSource(stream);
 			
 			xmlreader.parse(is);
 			
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		filterByDate();
 		filterByLink();
 	}
 	
 	protected void filterByLink() {
 		List<RSSItem> items = handler.getFeed().getItems();
 		if(items.size() == 0) return;
 		
 		itemDataSource.open();
 		ListIterator<RSSItem> iter = items.listIterator();
 		while(iter.hasNext()) {
 			RSSItem item = iter.next();
 			if(itemDataSource.isExist(item.getLink())) {
 				iter.remove();
 			}
 		}
 		itemDataSource.close();
 	}
 	
 	/**
 	 * remove some old feed.
 	 */
 	protected void filterByDate() {
 		RSSFeed rssFeed = handler.getFeed();
 		System.out.println("process feed");
 		if(!rssFeed.valid()) return;
 		System.out.println("lastBuildDate: " + lastBuildDate);
 		System.out.println("lastItemDate: " + lastItemDate);
 		if(lastBuildDate <= 0) return;
 		
 		if(rssFeed.getLastBuildTimestamp() <= lastBuildDate) {
 			rssFeed.removeAll();
 		}else {
 			
 			List<RSSItem> items = rssFeed.getItems();
 			
 			ListIterator<RSSItem> iter = items.listIterator();
 			while(iter.hasNext()) {
 				RSSItem item = iter.next();
 				if(item.getTimestamp() <= lastItemDate) {
 					iter.remove();
 				}
 			}
 		}
 			
 	}
 	
 	
 	/**
 	 * stop feed parse and disconnect http connection.
 	 */
 	public void stop() {
 		connManager.shutdown();
 	}
 	
 	/**
 	 * get rss feed object.
 	 */
 	public RSSFeed getFeed() {
 		return handler.getFeed();
 	}
 	
 	
 	private class ParserListener implements IParserListener {
 
 		private long lastItemTimestamp;
 		
 		private long lastBuildTimestamp;
 		
 		public ParserListener(long lastBuildTimestamp, long lastItemTimestamp) {
 			this.lastItemTimestamp = lastItemTimestamp;
 			this.lastBuildTimestamp = lastBuildTimestamp;
 		}
 		
 		public boolean onMetaReceive(RSSFeed feed) {
 			if(feed.getLastBuildTimestamp() <= lastBuildTimestamp) {
 				return true;
 			}
 			return false;
 		}
 
 		public boolean onItemReceive(RSSItem item) {
 			if(item.getTimestamp() <= lastItemTimestamp) {
 				return true;
 			}
 			return false;
 		}
 
 		public void onEnd(RSSFeed feed) {
 			
 		}
 	}
 }
