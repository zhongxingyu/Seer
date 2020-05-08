 package feedreader.fetch;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
import java.net.URLConnection;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.zip.GZIPInputStream;
 
 import javax.xml.namespace.QName;
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.XMLEvent;
 
 import common.DateUtils;
 import feedreader.Feed;
 import feedreader.FeedItem;
 
 /**
  * Loads RSS feeds
  * @author jared.pearson
  */
 public class FeedLoader {
 	private static final String RSS_TAG = "rss";
 	private static final String ITEM_TAG = "item";
 	private static final String TITLE_TAG = "title";
 	private static final String LINK_TAG = "link";
 	private static final String DESCRIPTION_TAG = "description";
 	private static final String PUBDATE_TAG = "pubDate";
 	private static final String GUID_TAG = "guid";
 	
 	public Feed loadFromUrl(final String urlAddress) throws IOException, XMLStreamException {
 		Feed feed = null;
 		InputStream inputStream = null;
 		try {
 			URL url = new URL(urlAddress);
			URLConnection urlConnection = url.openConnection();
 			urlConnection.setRequestProperty("Accept", "application/rss+xml;q=1.0,application/xml;q=0.9");
 			urlConnection.setRequestProperty("Accept-Charset", "utf-8");
 			urlConnection.setRequestProperty("Accept-Encoding", "gzip;q=1.0");
 			
 			inputStream = urlConnection.getInputStream();
			if(urlConnection instanceof HttpURLConnection && ((HttpURLConnection)urlConnection).getResponseCode() != 200) {
				throw new RuntimeException("Response code returned: " + ((HttpURLConnection)urlConnection).getResponseCode());
 			}
 			if("gzip".equals(urlConnection.getContentEncoding())) {
 				inputStream = new GZIPInputStream(inputStream);
 			}
 			
 			XMLInputFactory inputFactory = XMLInputFactory.newFactory();
 			XMLEventReader eventReader = inputFactory.createXMLEventReader(inputStream);
 			
 			while(eventReader.hasNext()) {
 				XMLEvent nextEvent = eventReader.nextEvent();
 				
 				if(isStartElement(nextEvent, RSS_TAG)) {
 					String version = nextEvent.asStartElement().getAttributeByName(QName.valueOf("version")).getValue();
 					if(!version.equals("2.0")) {
 						throw new RuntimeException("Unsupported version: " + version);
 					}
 					
 					feed = parseRss(eventReader, nextEvent);
 					feed.setUrl(urlAddress);
 				}
 			}
 		} finally {
 			if(inputStream != null) {
 				inputStream.close();
 			}
 		}
 		return feed;
 	}
 	
 	private Feed parseRss(XMLEventReader eventReader, XMLEvent event) throws XMLStreamException {
 		String title = null;
 		ArrayList<FeedItem> entries = new ArrayList<FeedItem>();
 		
 		while(eventReader.hasNext()) {
 			XMLEvent nextEvent = eventReader.nextEvent();
 			if(isEndElement(nextEvent, RSS_TAG)) {
 				break;
 			}
 			
 			if(isStartElement(nextEvent, TITLE_TAG)) {
 				title = getText(eventReader);
 			} else if(isStartElement(nextEvent, ITEM_TAG)) {
 				entries.add(parseItem(eventReader, nextEvent));
 			}
 		}
 		
 		Feed feed = new Feed();
 		feed.setTitle(title);
 		feed.setItems(entries);
 		
 		for(FeedItem item : entries) {
 			item.setFeed(feed);
 		}
 		
 		return feed;
 	}
 	
 	private FeedItem parseItem(XMLEventReader eventReader, XMLEvent event) throws XMLStreamException {
 		String title = null;
 		String link = null;
 		String description = null;
 		Date pubDate = null;
 		String guid = null;
 		
 		while(eventReader.hasNext()) {
 			XMLEvent nextEvent = eventReader.nextEvent();
 			if(isEndElement(nextEvent, ITEM_TAG)) {
 				break;
 			}
 			
 			if(isStartElement(nextEvent, TITLE_TAG)) {
 				title = getText(eventReader);
 			} else if(isStartElement(nextEvent, LINK_TAG)) {
 				link = getText(eventReader);
 			} else if(isStartElement(nextEvent, DESCRIPTION_TAG)) {
 				description = getText(eventReader);
 			} else if(isStartElement(nextEvent, PUBDATE_TAG)) {
 				pubDate = getTextAsDate(eventReader);
 			} else if(isStartElement(nextEvent, GUID_TAG)) {
 				guid = getText(eventReader);
 			}
 		}
 		
 		return new FeedItem(title, link, description, pubDate, guid);
 	}
 	
 	private String getText(XMLEventReader eventReader) throws XMLStreamException {
 		XMLEvent nextEvent = eventReader.nextEvent();
 		if(nextEvent.isEndElement()) {
 			return null;
 		}
 		
 		String text = null;
 		if(nextEvent.isCharacters()) {
 			text = nextEvent.asCharacters().getData();
 		}
 		return text;
 	}
 	
 	private Date getTextAsDate(XMLEventReader eventReader) throws XMLStreamException {
 		String text = getText(eventReader);
 		if(text == null || text.trim().length() == 0) {
 			return null;
 		}
 		
 		Date pubDate = null;
 		try {
 			pubDate = DateUtils.parseRfc0822Date(text);
 		} catch (ParseException exc) {
 			//TODO: add specific parsing exception instead of generic runtime
 			throw new RuntimeException(exc);
 		}
 		
 		return pubDate;
 	}
 	
 	private boolean isStartElement(XMLEvent event, String tag) {
 		return event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(tag);
 	}
 	
 	private boolean isEndElement(XMLEvent event, String tag) {
 		return event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(tag);
 	}
 	
 }
