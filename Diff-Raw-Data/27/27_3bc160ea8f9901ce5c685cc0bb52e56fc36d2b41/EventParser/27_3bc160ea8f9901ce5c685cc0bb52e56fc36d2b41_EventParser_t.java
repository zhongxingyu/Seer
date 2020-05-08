 package com.dev.campus.event;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectOutputStream;
 import java.net.URL;
 import java.text.ParseException;
 import java.util.AbstractMap.SimpleEntry;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import com.dev.campus.CampusUB1App;
 import com.dev.campus.util.TimeExtractor;
 import com.dev.campus.event.Feed.FeedType;
 
 public class EventParser {
 
 	private XmlPullParser mParser;
 
 	private Category mCategory;
 	private EventsActivity mContext;
 	
 	private ArrayList<Event> mEvents;
 	private ArrayList<Date> mEventDates;
 
 	public EventParser(EventsActivity context) throws XmlPullParserException {
 		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
 		factory.setNamespaceAware(false);
 		mParser = factory.newPullParser();
 		mContext = context;
 	}
 	
 	public ArrayList<Event> getEvents() {
 		return mEvents;
 	}
 
 	private void setInput(Feed feed) throws IOException, XmlPullParserException {
 		URL url = new URL(feed.getUrl());
 		InputStream stream = url.openStream();
 		mParser.setInput(stream, null);
 	}
 
 	public void parseEvents(Category category) throws XmlPullParserException, IOException, ParseException {
 		mCategory = category;
 		ArrayList<Event> events = new ArrayList<Event>();
 		ArrayList<Date> dates = new ArrayList<Date>();
 		for (Feed feed : mCategory.getFeeds()) {
			if ((feed.getType().equals(FeedType.UB1_FEED) && CampusUB1App.persistence.isSubscribedUB1())
			 || (feed.getType().equals(FeedType.LABRI_FEED) && CampusUB1App.persistence.isSubscribedLabri())) {
 				setInput(feed);
 				Event event = new Event();
 				Date buildDate = new Date(0);
 
 				int eventType = mParser.getEventType();
 				while (eventType != XmlPullParser.END_DOCUMENT) {
 					if (eventType == XmlPullParser.START_TAG) {
 						if (mParser.getName().equals("lastBuildDate")) {
 							buildDate = TimeExtractor.createDate(mParser.nextText(), "EEE, d MMM yyyy HH:mm:ss Z");
 							dates.add(buildDate);
 						}
 						if (mParser.getName().equals("item")) {
 							event = new Event();
 						} 
 						if (mParser.getName().equals("title")) {
 							event.setTitle(mParser.nextText());
 						} 
 						if (mParser.getName().equals("description")) {
 							String description = mParser.nextText();
 							event.setDescription(description);
 							if (feed.getType().equals(FeedType.LABRI_FEED))
 								event.setDetails(description);
 						} 
 						if (mParser.getName().equals("content:encoded")) {
 							event.setDetails(mParser.nextText());
 						}
 						if (mParser.getName().equals("pubDate")) {
 							Date d = null;
							String text = mParser.nextText();
							d = TimeExtractor.getCorrectDate(text, event.getDetails());
							event.setDate(d);
 						}
 					}
 					else if (eventType == XmlPullParser.END_TAG) {
 						if (mParser.getName().equals("item")) {
 							event.setCategory(mCategory.toString());
 							event.setSource(feed.getType());
 							if (!event.getTitle().equals("")) {
 								events.add(event);
 							}
 						}
 					}
 					eventType = mParser.nextToken();
 				}
 			}
 		}
 
 		mEvents = events;
 		mEventDates = dates;
 	}
 
 	public boolean isLatestVersion(Category category, List<Date> dates) throws IOException, XmlPullParserException, ParseException {
 		int i = 0;
 		for (Feed feed : category.getFeeds()) {
			if ((feed.getType().equals(FeedType.UB1_FEED) && CampusUB1App.persistence.isSubscribedUB1())
			 || (feed.getType().equals(FeedType.LABRI_FEED) && CampusUB1App.persistence.isSubscribedLabri())) {
 				setInput(feed);
 				Date buildDate;
 				
 				int eventType = mParser.getEventType();
 				while (eventType != XmlPullParser.END_DOCUMENT) {
 					if (eventType == XmlPullParser.START_TAG) {
 						if (mParser.getName().equals("lastBuildDate")) {
 							buildDate = TimeExtractor.createDate(mParser.nextText(), "EEE, d MMM yyyy HH:mm:ss Z");
 							if (dates.get(i++).getTime() != buildDate.getTime())
 								return false;
 						}
 					}
 					eventType = mParser.nextToken();
 				}
 			} else {
 				//No build dates in HTML pages, so always update
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public void saveEvents() throws XmlPullParserException {
 		ObjectOutputStream oos = null;
 		try {
 			File history = new File(mContext.getHistoryPath());
 			history.getParentFile().createNewFile();
 			FileOutputStream fout = new FileOutputStream(history);
 			oos = new ObjectOutputStream(fout);
 			
 			SimpleEntry<ArrayList<Event>, ArrayList<Date>> map = new SimpleEntry<ArrayList<Event>, ArrayList<Date>>(mEvents, mEventDates);
 			oos.writeObject(map);
 		} catch (FileNotFoundException ex) {
 			ex.printStackTrace();  
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		} finally {
 			try {
 				if (oos != null) {
 					oos.flush();
 					oos.close();
 				}
 			} catch (IOException ex) {
 				ex.printStackTrace();
 			}
 		}
 	}
 }
