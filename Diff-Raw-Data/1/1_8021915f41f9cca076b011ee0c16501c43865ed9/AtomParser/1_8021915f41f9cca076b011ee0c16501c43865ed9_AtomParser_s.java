 package gr.dsigned.atom.parser;
 
 import gr.dsigned.atom.domain.AtomCategory;
 import gr.dsigned.atom.domain.AtomEntry;
 import gr.dsigned.atom.domain.AtomFeed;
 import gr.dsigned.atom.domain.AtomLink;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.HashMap;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 public class AtomParser {
 
 	private AtomFeed feed = new AtomFeed();
 	private boolean parseDates = true;
 	private final HashMap<String, String> attMap = new HashMap<String, String>();
 	protected static final SimpleDateFormat ISO8601_DATE_FORMATS[] = new SimpleDateFormat[] { new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz"), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"),
 			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'") };
 	private static final String FEED_TAG = "feed";
 	private static final String TITLE_TAG = "title";
 	private static final String LINK_TAG = "link";
 	private static final String SUBTITLE_TAG = "subtitle";
 	private static final String ICON_TAG = "icon";
 	private static final String UPDATED_TAG = "updated";
 	private static final String PUBLISHED_TAG = "published";
 	private static final String ENTRY_TAG = "entry";
 	private static final String CATEGORY_TAG = "category";
 	private static final String ID_TAG = "id";
 	private static final String SUMMARY_TAG = "summary";
 	private static final String CONTENT_TAG = "content";
 	private static final String CONTENT_ENCODED_TAG = "content:encoded";
 	private static final String CONTENT_ENCODED_ALT_TAG = "encoded";
 	private static final String AUTHOR_TAG = "author";
 	private static final String NAME_TAG = "name";
 	
 
 	public AtomFeed parse(InputStream in, long timeOut) {
 		AtomParserTimeOut apt = new AtomParserTimeOut(in);
 		new Thread(apt).start();
 		long startTime = System.currentTimeMillis();
 		long elapsedTime = System.currentTimeMillis();
 		while(apt.getFeed() == null && elapsedTime - startTime < timeOut){
 			elapsedTime = System.currentTimeMillis();
 		}
 		return apt.getFeed();
 	}
 
 	public AtomFeed parse(InputStream in) throws XmlPullParserException, IOException {
 		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
 		factory.setNamespaceAware(true);
 		XmlPullParser parser = factory.newPullParser();
 		parser.setInput(in, null);
 		int eventType = parser.getEventType();
 		while (eventType != XmlPullParser.END_DOCUMENT) {
 			if (eventType == XmlPullParser.START_TAG) {
 				String startTag = parser.getName();
 				if (FEED_TAG.equals(startTag)) {
 					processFeed(parser);
 				}
 			}
 			eventType = parser.next();
 		}
 		return feed;
 	}
 
 	private void processFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
 		int eventType = parser.getEventType();
 		while (eventType != XmlPullParser.END_DOCUMENT) {
 			if (eventType == XmlPullParser.START_TAG) {
 				String startTag = parser.getName();
 				if (TITLE_TAG.equals(startTag)) {
 					feed.setmTitle(parser.nextText());
 				} else if (LINK_TAG.equals(startTag)) {
 					getAttributeMap(parser);
 					if (attMap.containsKey("alternate")) {
 						feed.setmURL(attMap.get("href"));
 					}
 				} else if (SUBTITLE_TAG.equals(startTag)) {
 					feed.setSubtitle(parser.nextText());
 				} else if (ICON_TAG.equals(startTag)) {
 					feed.setIcon(parser.nextText());
 				} else if (UPDATED_TAG.equals(startTag)) {
 					String updated = parser.nextText();
 					if (parseDates) {
 						try {
 							updated = updated.trim();
 							feed.setUpdated(ISO8601_DATE_FORMATS[0].parse(updated));
 						} catch (ParseException pe1) {
 							try {
 								feed.setUpdated(ISO8601_DATE_FORMATS[1].parse(updated));
 							} catch (ParseException pe2) {
 								try {
 									feed.setUpdated(ISO8601_DATE_FORMATS[2].parse(updated));
 								} catch (ParseException pe3) {
 									// no big deal, stay silent
 								}
 							}
 						}
 					}
 					feed.setUpdatedString(updated);
 				} else if (ENTRY_TAG.equals(startTag)) {
 					processEntry(parser);
 				}
 
 			} else if (eventType == XmlPullParser.END_TAG) {
 				String endTag = parser.getName();
 				if (FEED_TAG.equals(endTag)) {
 					break;
 				}
 			}
 			eventType = parser.next();
 		}
 	}
 
 	private void processEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
 		int eventType = parser.getEventType();
 		AtomEntry entry = new AtomEntry();
 		while (eventType != XmlPullParser.END_DOCUMENT) {
 			if (eventType == XmlPullParser.START_TAG) {
 				String startTag = parser.getName();
 				if (TITLE_TAG.equals(startTag)) {
 					entry.setTitle(parser.nextText());
 				} else if (LINK_TAG.equals(startTag)) {
 					getAttributeMap(parser);
 					AtomLink link = new AtomLink();
 					link.setRel(attMap.get("rel"));
 					link.setHref(attMap.get("href"));
 					link.setTitle(attMap.get("title"));
 					link.setType(attMap.get("type"));
 					link.setVia(attMap.get("via"));
 					entry.addLink(link);
 				} else if (CATEGORY_TAG.equals(startTag)) {
 					getAttributeMap(parser);
 					AtomCategory cat = new AtomCategory();
 					cat.setLabel(attMap.get("label"));
 					cat.setTerm(attMap.get("term"));
 					entry.addCatogory(cat);
 				} else if (AUTHOR_TAG.equals(startTag)) {
 					processAuthor(entry, parser);
 				} else if (ID_TAG.equals(startTag)) {
 					entry.setId(parser.nextText());					
 				} else if(PUBLISHED_TAG.equals(startTag)){
 					String published = parser.nextText();
 					if (parseDates) {
 						try {
 							published = published.trim();
 							entry.setPublished(ISO8601_DATE_FORMATS[0].parse(published));
 						} catch (ParseException pe1) {
 							try {
 								entry.setPublished(ISO8601_DATE_FORMATS[1].parse(published));
 							} catch (ParseException pe2) {
 								try {
 									entry.setPublished(ISO8601_DATE_FORMATS[2].parse(published));
 								} catch (ParseException pe3) {
 									// no big deal, stay silent
 								}
 							}
 						}
 					}
 					entry.setPublishedString(published);
 				} else if (UPDATED_TAG.equals(startTag)) {
 					String updated = parser.nextText();
 					if (parseDates) {
 						try {
 							updated = updated.trim();
 							entry.setUpdated(ISO8601_DATE_FORMATS[0].parse(updated));
 						} catch (ParseException pe1) {
 							try {
 								entry.setUpdated(ISO8601_DATE_FORMATS[1].parse(updated));
 							} catch (ParseException pe2) {
 								try {
 									entry.setUpdated(ISO8601_DATE_FORMATS[2].parse(updated));
 								} catch (ParseException pe3) {
 									// no big deal, stay silent
 								}
 							}
 						}
 					}
 					entry.setUpdatedString(updated);
 				} else if (SUMMARY_TAG.equals(startTag)) {
 					entry.setSummary(parser.nextText());
 				} else if (CONTENT_ENCODED_TAG.equals(startTag) || CONTENT_TAG.equals(startTag) || CONTENT_ENCODED_ALT_TAG.equals(startTag)) {
 					entry.setContent(parser.nextText());
 				}
 
 			} else if (eventType == XmlPullParser.END_TAG) {
 				String endTag = parser.getName();
 				if (ENTRY_TAG.equals(endTag)) {
 					feed.addEnty(entry);
 					break;
 				}
 			}
 			eventType = parser.next();
 		}
 	}
 	
 	private void processAuthor(AtomEntry entry, XmlPullParser parser) throws XmlPullParserException, IOException {
 		int eventType = parser.getEventType();
 		String authorName = "";
 		while (eventType != XmlPullParser.END_DOCUMENT) {
 			if (eventType == XmlPullParser.START_TAG) {
 				String startTag = parser.getName();
 				if (NAME_TAG.equals(startTag)) {
 					authorName = parser.nextText();
 				} 
 			} else if (eventType == XmlPullParser.END_TAG) {
 				String endTag = parser.getName();
 				if (AUTHOR_TAG.equals(endTag)) {
 					entry.getAuthors().add(authorName);
 					break;
 				}
 			}
 			eventType = parser.next();
 		}
 	}
 
 	private void getAttributeMap(XmlPullParser parser) {
 		attMap.clear();
 		for (int i = 0; i < parser.getAttributeCount(); i++) {
 			attMap.put(parser.getAttributeName(i), parser.getAttributeValue(i));
 		}
 	}
 
 	public void setParseDates(boolean parseDates) {
 		this.parseDates = parseDates;
 	}
 
 	public class AtomParserTimeOut implements Runnable {
 		private InputStream in;
 		private AtomFeed feed;
 
 		public AtomParserTimeOut(InputStream in) {
 			this.in = in;	
 		}
 
 		public void run() {
 			AtomParser parser = new AtomParser();
 			try {
 				feed = parser.parse(in);
 			} catch (Exception e) {
 				System.out.println(e);
 			}
 		}
 
 		public synchronized AtomFeed getFeed() {
 			return feed;
 		}
 	}
 
 }
