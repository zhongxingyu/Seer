 package nl.adben.android.rssreader;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParser;
 
 import android.util.Log;
 import android.util.Xml;
 
 /**
  * Parse rss feeds using XmlPullParser for android 
  * 
  * @author abenedetti
  */
 public class RssXmlPullParser {
 	// names of the XML tags
 	static final String CHANNEL = "channel";
 	static final String PUB_DATE = "pubDate";
 	static final String DESCRIPTION = "description";
 	static final String LINK = "link";
 	static final String TITLE = "title";
 	static final String ITEM = "item";
 
 	public List<Entry> parse(InputStream in) {
 		List<Entry> messages = null;
 		XmlPullParser parser = Xml.newPullParser();
 		try {
 			// auto-detect the encoding from the stream
 			parser.setInput(in, null);
 			int eventType = parser.getEventType();
 			Entry currentEntry = null;
 			boolean done = false;
 			while (eventType != XmlPullParser.END_DOCUMENT && !done) {
 				String name = null;
 				switch (eventType) {
 				case XmlPullParser.START_DOCUMENT:
 					messages = new ArrayList<Entry>();
 					break;
 				case XmlPullParser.START_TAG:
 					name = parser.getName();
 					if (name.equalsIgnoreCase(ITEM)) {
 						currentEntry = new Entry();
 					} else if (currentEntry != null) {
 						if (name.equalsIgnoreCase(LINK)) {
 							currentEntry.setLink(parser.nextText());
 						} else if (name.equalsIgnoreCase(DESCRIPTION)) {
 							currentEntry.setDescription(parser.nextText());
						} else if (name.equalsIgnoreCase(PUB_DATE)) {
							currentEntry.setDate(parser.nextText());
 						} else if (name.equalsIgnoreCase(TITLE)) {
 							currentEntry.setTitle(parser.nextText());
 						}
 					}
 					break;
 				case XmlPullParser.END_TAG:
 					name = parser.getName();
 					if (name.equalsIgnoreCase(ITEM) && currentEntry != null) {
 						messages.add(currentEntry);
 					} else if (name.equalsIgnoreCase(CHANNEL)) {
 						done = true;
 					}
 					break;
 				}
 				eventType = parser.next();
 			}
 		} catch (Exception e) {
 			Log.e("NewsYCombinatorParser::PullFeedParser", e.getMessage(), e);
 			throw new RuntimeException(e);
 		}
 		return messages;
 	}
 
 }
