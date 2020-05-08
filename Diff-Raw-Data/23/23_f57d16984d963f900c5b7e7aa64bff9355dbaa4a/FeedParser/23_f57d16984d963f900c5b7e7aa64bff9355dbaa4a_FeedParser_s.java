 package info.matthewwardrop.scholarley.feeder.arxiv;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.net.Uri;
 import android.util.Log;
 import android.util.Xml;
 
 public class FeedParser {
 	// We don't use namespaces
 	private static final String ns = null;
 	
 	public static class ArxivDocument {
         public String title;
         public String link;
         public String attachment;
         public String doc_abstract;
         public List<String> authors = new ArrayList<String>();
         public String arxiv_id;
         public String institution;
     }
 	   
     public List<ArxivDocument> parse(InputStream in) throws XmlPullParserException, IOException {
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
     
     private List<ArxivDocument> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
     	List<ArxivDocument> entries = new ArrayList<ArxivDocument>();
 
         parser.require(XmlPullParser.START_TAG, ns, "feed");
         while (parser.next() != XmlPullParser.END_TAG) {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             }
             String name = parser.getName();
             // Starts by looking for the entry tag
             if (name.equals("entry")) {
                 entries.add(readEntry(parser));
             } else {
                 skip(parser);
             }
         }  
         return entries;
     }
     
     // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
     // to their respective "read" methods for processing. Otherwise, skips the tag.
     private ArxivDocument readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
         parser.require(XmlPullParser.START_TAG, ns, "entry");
         
         ArxivDocument doc = new ArxivDocument();
         
         while (parser.next() != XmlPullParser.END_TAG) {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             }
             String name = parser.getName();
             if (name.equals("title")) {
                 doc.title = readTitle(parser);
             } else if (name.equals("summary")) {
                 doc.doc_abstract = readSummary(parser);
             } else if (name.equals("link") && parser.getAttributeValue(null, "rel").equals("alternate")) {
                 doc.link = readLink(parser);
            } else if (name.equals("link") && parser.getAttributeValue(null, "rel").equals("related")) {
             	doc.attachment = readAttachmentLink(parser);
             } else if (name.equals("author")) {
             	doc.authors.add(readAuthor(parser));
             } else {
                 skip(parser);
             }
         }
         
         if (doc.link != null) {
         	doc.arxiv_id = Uri.parse(doc.link).getLastPathSegment();
         }
         
         return doc;
     }
 
     // Processes title tags in the feed.
     private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
         parser.require(XmlPullParser.START_TAG, ns, "title");
         String title = readText(parser);
         parser.require(XmlPullParser.END_TAG, ns, "title");
         return title;
     }
     
     private String readAuthor(XmlPullParser parser)  throws IOException, XmlPullParserException {
     	parser.require(XmlPullParser.START_TAG, ns, "author");
     	String author = null;
     	do {
     		parser.next();
     		if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("name")) {
     			author = readText(parser);
     		}
     	} while (parser.getName() == null || !parser.getName().equals("author"));
     	parser.require(XmlPullParser.END_TAG, ns, "author");
         return author;
     }
       
     // Processes link tags in the feed.
     private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
         String link = "";
         parser.require(XmlPullParser.START_TAG, ns, "link");
         String tag = parser.getName();
         String relType = parser.getAttributeValue(null, "rel");  
         if (tag.equals("link")) {
             if (relType.equals("alternate")){
                 link = parser.getAttributeValue(null, "href");
                 parser.nextTag();
             } 
         }
         parser.require(XmlPullParser.END_TAG, ns, "link");
         return link;
     }
     
  // Processes link tags in the feed.
     private String readAttachmentLink(XmlPullParser parser) throws IOException, XmlPullParserException {
         String link = "";
         parser.require(XmlPullParser.START_TAG, ns, "link");
         String tag = parser.getName();
         String relType = parser.getAttributeValue(null, "rel");  
         if (tag.equals("link")) {
             if (relType.equals("related")){
                 link = parser.getAttributeValue(null, "href");
                 parser.nextTag();
             } 
         }
         Log.e("LINK",link);
         parser.require(XmlPullParser.END_TAG, ns, "link");
         return link;
     }
 
     // Processes summary tags in the feed.
     private String readSummary(XmlPullParser parser) throws IOException, XmlPullParserException {
         parser.require(XmlPullParser.START_TAG, ns, "summary");
         String summary = readText(parser).replaceAll("\n", " ").replaceAll("\\s{2,}", " ");
         parser.require(XmlPullParser.END_TAG, ns, "summary");
         return summary;
     }
 
     // For the tags title and summary, extracts their text values.
     private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
         String result = "";
         if (parser.next() == XmlPullParser.TEXT) {
             result = parser.getText();
             parser.nextTag();
         }
         return result;
     }
     
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
