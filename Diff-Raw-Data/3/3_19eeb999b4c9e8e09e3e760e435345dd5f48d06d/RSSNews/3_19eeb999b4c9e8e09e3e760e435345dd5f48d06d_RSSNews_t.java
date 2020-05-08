 /*
  * @author <a href="oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
  * @version $Id$
  */
 
 package org.gridlab.gridsphere.portlets.core.beans;
 
 import org.jdom.JDOMException;
 import org.jdom.Element;
 import org.jdom.Document;
 import org.jdom.input.SAXBuilder;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Vector;
 import java.util.List;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 public class RSSNews {
 
     private static RSSNews instance = new RSSNews();
     private int fetchinterval = 60;         // in seconds
 
     Vector feeds = new Vector();
 
     private RSSNews() {
 
         super();
         this.add("http://www.xml.com/2002/12/18/examples/rss20.xml.txt","xml.com feed examples");
         this.add("http://www.snipsnap.org/exec/rss","SnipSnap.org");
         this.add("http://diveintomark.org/xml/rss.xml","DiveIntoMark");
         this.add("http://weblog.infoworld.com/udell/rss.xml","weblog");
         this.add("http://rss.com.com/2547-12-0-5.xml","CNet");
 
     }
 
     public Document getRSSFeed(RSSFeed feed) {
         Document doc = new Document(new Element("rss"));
 
         if ((System.currentTimeMillis()-feed.getLastfetched()>1000*getFetchinterval()) || feed.getLastfetched()==0) {
             try {
                 SAXBuilder builder = new SAXBuilder(false);
                 URL feedurl = new URL(feed.getUrl());
                 doc = builder.build(feedurl);
                 feed.setFeed(doc);
                 System.out.println("CACHED TIME :"+feed.getLastfetched());
                 System.out.println("CURRENT TIME: "+System.currentTimeMillis());
                 long diff = System.currentTimeMillis()-feed.getLastfetched();
                 System.out.println("DIFF :"+diff);
                 feed.setLastfetched(System.currentTimeMillis());
                 System.out.println("FETCHINTERVALL: "+getFetchinterval());
                 System.out.println("############################# Fetched feed from :"+feed.getUrl());
             } catch (MalformedURLException e) {
             } catch (JDOMException e) {
             }
         } else {
             doc = feed.getFeed();
         }
         return doc;
 
     }
 
     public Document getRSSFeed(String url) {
         return getRSSFeed(getFeed(url));
     }
 
     public String getHTML(String url) {
         return getHTML(getFeed(url));
     }
 
     public String getHTML(RSSFeed feed) {
         Document doc = getRSSFeed(feed);
         String version = "unknown";
         Element root = doc.getRootElement();
         String result = new String();
 
         version = root.getAttributeValue("version");
         result = result + ("RSS feed from URL: "+feed.getUrl());
 
         try {
             if (version.equals("2.0") || version.equals("3.14159265359")) {
                 List items = root.getChild("channel").getChildren("item");
                 Iterator it = items.iterator();
                 result = result +"<ul>";
                 while (it.hasNext()) {
                     Element item = (Element)it.next();
                     String title = item.getChild("title").getText();
                     String link = item.getChild("link").getText();
                     String desc = item.getChild("description").getText();
                     result = result +"<li><a target=\"_new\" href=\""+link+"\">"+title+"</a><br/>"+desc+"</li>";
                 }
                 result = result +"</ul>";
             } else {
                 result = result +"<br/>Unsupported RSS feed version ("+version+")";
             }
         } catch (Exception e) {
             result = result +"<br/>This is a not a supported RSS feed.";
             System.out.println("============> "+e);
         }
 
         return result;
 
     }
 
     public static RSSNews getInstance() {
         return instance;
     }
 
     public void add(String url, String label) {
         RSSFeed news = new RSSFeed(url, label);
         feeds.add(news);
     }
 
     public void delete(String url) {
         Iterator it = feeds.iterator();

         while (it.hasNext()) {
             RSSFeed feed = (RSSFeed)it.next();
             if (feed.getUrl().equals(url)) {
                 feeds.remove(feed);
                return;
             }
         }
     }
 
     public Iterator iterator() {
         return feeds.iterator();
     }
 
     public RSSFeed getFeed(String url) {
         Iterator it = feeds.iterator();
         while (it.hasNext()) {
             RSSFeed feed = (RSSFeed)it.next();
             if (feed.getUrl().equals(url)) {
                 return feed;
             }
         }
         return null;
     }
 
     public int getFetchinterval() {
         return fetchinterval;
     }
 
     public void setFetchinterval(int fetchinterval) {
         this.fetchinterval = fetchinterval;
     }
 }
