 package com.gnuton.newshub.utils;
 
 import android.annotation.TargetApi;
 import android.os.Build;
 import android.util.Log;
 import android.util.Xml;
 
 import com.gnuton.newshub.db.RSSEntryDataSource;
 import com.gnuton.newshub.types.RSSEntry;
 import com.gnuton.newshub.types.RSSFeed;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 
 /**
  * Created by gnuton on 6/6/13.
  */
 public class XMLFeedParser {
     private static final String TAG=XMLFeedParser.class.getName();
     private static final String xmlNamespace = null; // No namespace
     private final RSSEntryDataSource mEds;
 
     public XMLFeedParser(RSSEntryDataSource eds) {
         mEds = eds;
     }
 
     public RSSFeed parseXML(RSSFeed feed){
         if (feed != null && feed.xml == null)
             return feed;
 
         try {
             try {
                 return parseRSSBuffer(feed);
             } catch (XmlPullParserException e) {
                 try {
                     return parseAtomBuffer(feed);
                 } catch (XmlPullParserException e1) {
                     parseUnknownBuffer(feed);
                     e1.printStackTrace();
                 }
                 e.printStackTrace();
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         // Clean
         feed.xml= null;
 
         return feed;
     }
 
     /**
      * Useful for debuggin'.
      * @param feed
      * @return
      */
     private RSSFeed parseUnknownBuffer(RSSFeed feed) throws IOException {
         String xml = feed.xml;
 
         try {
             XmlPullParser xpp = Xml.newPullParser();
             xpp.setInput(new StringReader(xml));
 
             int eventType = xpp.getEventType();
             while (eventType != XmlPullParser.END_DOCUMENT) {
                 if(eventType == XmlPullParser.START_DOCUMENT) {
                     System.out.println("Start document");
                 } else if(eventType == XmlPullParser.START_TAG) {
                     System.out.println("Start tag "+xpp.getName());
                 } else if(eventType == XmlPullParser.END_TAG) {
                     System.out.println("End tag "+xpp.getName());
                 } else if(eventType == XmlPullParser.TEXT) {
                     //System.out.println("Text "+xpp.getText());
                 }
                 eventType = xpp.next();
             }
             System.out.println("End document");
         } catch (XmlPullParserException e) {
             e.printStackTrace();
         }
         return feed;
     }
     private RSSFeed parseAtomBuffer(RSSFeed feed) throws XmlPullParserException, IOException {
         String xml = feed.xml;
         List entries = new ArrayList();
         if (xml == null) {
             Log.e(TAG, "XML Buffer is empty");
             return feed;
         }
         XmlPullParser xpp = Xml.newPullParser();
 
         xpp.setInput(new StringReader(xml));
         xpp.nextTag();
         xpp.require(XmlPullParser.START_TAG, xmlNamespace, "feed");
 
         while (xpp.next() != XmlPullParser.END_TAG) {
             if (xpp.getEventType() != XmlPullParser.START_TAG)
                 continue;
 
             if (xpp.getName().equals("entry")) {
                 entries.add(parseAtomEntry(xpp, feed.id));
             } else {
                 skip(xpp);
             }
         }
 
         Log.d(TAG, "PARSED " + entries.size() + " ENTRIES");
         feed.entries = entries;
         return feed;
     }
 
     private RSSFeed parseRSSBuffer(RSSFeed feed) throws XmlPullParserException, IOException {
         String xml = feed.xml;
         List entries = new ArrayList();
         if (xml == null) {
             Log.e(TAG, "XML Buffer is empty");
             return feed;
         }
 
         Log.d(TAG, "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" + xml);
         XmlPullParser xpp = Xml.newPullParser();
         xpp.setInput(new StringReader(xml));
         xpp.nextTag();
         xpp.require(XmlPullParser.START_TAG, xmlNamespace, "rss");
         xpp.nextTag();
         String name = xpp.getName();
         Log.d(TAG, "NAME " + name);
         xpp.require(XmlPullParser.START_TAG, xmlNamespace, "channel"); // or feed
         while (xpp.next() != XmlPullParser.END_TAG) {
             if (xpp.getEventType() != XmlPullParser.START_TAG)
                 continue;
 
             if (xpp.getName().equals("item")) {
                 entries.add(parseRSSEntry(xpp, feed.id));
             } else {
                 skip(xpp);
             }
         }
 
         Log.d(TAG, "PARSED " + entries.size() + " ENTRIES");
         feed.entries = entries;
         return feed;
     }
     @TargetApi(Build.VERSION_CODES.FROYO)
     private RSSEntry parseAtomEntry(XmlPullParser xpp, int feedID) throws IOException, XmlPullParserException {
         xpp.require(XmlPullParser.START_TAG, xmlNamespace, "entry");
         String title = null;
         String description = null;
         String content = null;
         String link = null;
         Calendar publishedData = null;
 
         while (xpp.next() != XmlPullParser.END_TAG) {
             if (xpp.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             } else if (xpp.getEventType() != XmlPullParser.END_TAG && xpp.getName() == "document") {
                 break;
             }
 
             String name = xpp.getName();
             Log.d(TAG, "NAME " + name);
 
             if (name.equals("title")) {
                 title = readTagText(xpp, "title");
             } else if (name.equals("content")) {
                 description = readTagText(xpp, "content");
             } else if (name.equals("link")) {
                 link = readAtomLink(xpp);
             } else if (name.equals("updated")) {
                 String dateString = readTagText(xpp, "updated");
                 try {
                     publishedData = DatatypeFactory.newInstance().newXMLGregorianCalendar(dateString).toGregorianCalendar();
                 } catch (DatatypeConfigurationException e) {
                     publishedData = new GregorianCalendar();
                 }
 
 
             } else {
                 skip(xpp);
             }
         }
 
         return (RSSEntry) mEds.create(
                 new String[] {
                         Integer.toString(feedID),
                         title,
                         description,
                         link,
                         content,
                        String.valueOf(publishedData.getTimeInMillis()),
                        String.valueOf(0) // Not read
                 });
     }
 
     /**
      *
      * @param xpp
      * @param feedID
      * @return RSSEntry pointing to the entry into the DB
      * @throws IOException
      * @throws XmlPullParserException
      */
     @TargetApi(Build.VERSION_CODES.FROYO)
     private RSSEntry parseRSSEntry(XmlPullParser xpp, int feedID) throws IOException, XmlPullParserException {
         xpp.require(XmlPullParser.START_TAG, xmlNamespace, "item");
         String title = null;
         String description = null;
         String content = null;
         String link = null;
         Calendar publishedData = GregorianCalendar.getInstance(); // Avoid crashes if data is not parsed correctly
 
         while (xpp.next() != XmlPullParser.END_TAG) {
             if (xpp.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             }
             String name = xpp.getName();
             Log.d(TAG, "NAME " + name);
 
             if (name.equals("title")) {
                 title = readTagText(xpp, "title");
             } else if (name.equals("description")) {
                 description = readTagText(xpp, "description");
             } else if (name.equals("link")) {
                 link = readTagText(xpp, "link");
             } else if (name.toLowerCase().equals("pubdate")) {
                 String dateString = null;
                 try {
                     dateString = readTagText(xpp, "pubDate");
                 } catch (XmlPullParserException e) {
                     dateString = readTagText(xpp, "pubdate");
                 }
 
                 String[] formatStrings = {
                         "dd MMM yyyy HH:mm:ss Z",
                         "EEE, dd MMM yyyy HH:mm:ss zzz",
                         "MM/dd/yy",
                         "MM/dd/yyyy",
                         "MM/dd/yy HH:mm",
                         "MM/dd/yyyy HH:mm",
                         "MM/dd/yy HH:mm:ss",
                         "MM/dd/yyyy HH:mm:ss"
                 };
 
                 for (String formatString : formatStrings) {
                     DateFormat formatter= new SimpleDateFormat(formatString);
                     try {
                         publishedData = parseRSSDate(dateString, formatter);
                         Log.d(TAG, "PublishedDate=" + publishedData.toString());
                         break;
                     } catch (ParseException e) {
                         e.printStackTrace();
                     } catch (DatatypeConfigurationException e) {
                         e.printStackTrace();
                     }
                 }
             } else {
                 skip(xpp);
             }
         }
 
         return (RSSEntry) mEds.create(
                 new String[] {
                         Integer.toString(feedID),
                         title,
                         description,
                         link,
                         content,
                         String.valueOf(publishedData.getTimeInMillis()),
                         String.valueOf(0) // Not read
                 });
     }
 
     private String readAtomLink(XmlPullParser xpp) throws IOException, XmlPullParserException {
         String link = "";
         xpp.require(XmlPullParser.START_TAG, xmlNamespace, "link");
         String tag = xpp.getName();
         String relType = xpp.getAttributeValue(null, "rel");
         if (tag.equals("link")) {
             if (relType.equals("alternate")){
                 link = xpp.getAttributeValue(null, "href");
                 xpp.nextTag();
             }
         }
         xpp.require(XmlPullParser.END_TAG, xmlNamespace, "link");
         return link;
     }
 
     @TargetApi(Build.VERSION_CODES.FROYO)
     private Calendar parseRSSDate(String dateString, DateFormat formatter) throws ParseException, DatatypeConfigurationException {
         Date date = formatter.parse(dateString);
         GregorianCalendar c = new GregorianCalendar();
         c.setTime(date);
         return c;
     }
 
     private String readTagText(XmlPullParser xpp, String tag) throws IOException, XmlPullParserException {
         xpp.require(XmlPullParser.START_TAG, xmlNamespace, tag);
         String text = "";
         if (xpp.next() == XmlPullParser.TEXT) {
             text = xpp.getText();
             xpp.nextTag();
         }
         xpp.require(XmlPullParser.END_TAG, xmlNamespace, tag);
         return text;
     }
 
     private void skip(XmlPullParser xpp) throws XmlPullParserException, IOException {
         if (xpp.getEventType() != XmlPullParser.START_TAG) {
             throw new IllegalStateException();
         }
         int depth = 1;
         while (depth != 0) {
             switch (xpp.next()) {
                 case XmlPullParser.END_TAG:
                     depth--;
                     break;
                 case XmlPullParser.START_TAG:
                     depth++;
                     break;
             }
         }
     }
 }
