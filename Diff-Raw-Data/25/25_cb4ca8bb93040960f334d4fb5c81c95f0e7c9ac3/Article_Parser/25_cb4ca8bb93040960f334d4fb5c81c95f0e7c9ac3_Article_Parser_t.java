 package com.media_moderator.article_data;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.util.Log;
 import android.util.Xml;
 
 public class Article_Parser {
 	
     private static final String ns = null;
     //TODO find way to pass source to parser
     public String Article_Source = null;
 	
 	public List<Article> parse(InputStream in, String source) throws XmlPullParserException, IOException {
 		Article_Source = source;
         try {
             XmlPullParser parser = Xml.newPullParser();
             parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
             parser.setInput(in, null);
             parser.nextTag();
             return readFeed(parser);
         } finally {
             in.close();
         }
     }
 
     private List<Article> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
         List<Article> entries = new ArrayList<Article>();
 
        parser.require(XmlPullParser.START_TAG, ns, "rss");
        parser.nextTag();
        parser.require(XmlPullParser.START_TAG, ns, "channel");
 
         while (parser.next() != XmlPullParser.END_TAG) {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             }
             String name = parser.getName();
             // Starts by looking for the entry tag
             if (name.equals("item")) {
                 entries.add(readArticle(parser));
             } else {
                 skip(parser);
             }
         }  
         return entries;
     }
       
     // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
     // to their respective "read" methods for processing. Otherwise, skips the tag.
     private Article readArticle(XmlPullParser parser) throws XmlPullParserException, IOException {
         parser.require(XmlPullParser.START_TAG, ns, "item");
 
         String title = null;
         String summary = null;
         String link = null;
         while (parser.next() != XmlPullParser.END_TAG) {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             }
             String name = parser.getName();
             if (name.equals("title")) {
                 title = readTitle(parser);
             } 
             else if (name.equals("description")) {
                 summary = readDescription(parser);
            } else if (name.equals("guid")) {
                 link = readLink(parser);
             }
             else {
                 skip(parser);
             }
         }
         Log.i("parser", title);
         return new Article(title, summary, link, Article_Source);
     }
 
     // Processes title tags in the feed.
     private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
         parser.require(XmlPullParser.START_TAG, ns, "title");
         String title = readText(parser);
         parser.require(XmlPullParser.END_TAG, ns, "title");
         return title;
     }
       
     // Processes link tags in the feed.
    //TODO MAKE SIURE IT READS CORRECT GUID LINK
     private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "guid");
         String link = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "guid");
         return link;
     }
 
     // Processes summary tags in the feed.
     private String readDescription(XmlPullParser parser) throws IOException, XmlPullParserException {
         parser.require(XmlPullParser.START_TAG, ns, "description");
         String summary = readText(parser);
         parser.require(XmlPullParser.END_TAG, ns, "description");
         return summary;
     }
 
     // For the tags title and summary, extracts their text values.
     // issue: does not handle special characters like ' correctly
     private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
         String result = "";
         if (parser.next() == XmlPullParser.TEXT) {
             result = parser.getText();
             parser.nextTag();
         }
         return result;
     }
     
     //when there is a tag we don't care about, skip it
     private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
         if (parser.getEventType() != XmlPullParser.START_TAG) {
             throw new IllegalStateException();
         }
         int depth = 1;
         while (depth != 0) {
             switch (parser.next()) {
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
