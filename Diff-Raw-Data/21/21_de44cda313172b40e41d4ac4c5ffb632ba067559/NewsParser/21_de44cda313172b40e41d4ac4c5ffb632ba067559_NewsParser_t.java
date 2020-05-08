 package com.example;
 
 import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
 import org.xml.sax.helpers.DefaultHandler;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import java.io.InputStream;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Locale;
 
 class NewsParser extends DefaultHandler {
 
     public static SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
     private String urlString;
     private RssFeed rssFeed;
     private StringBuilder text;
     private Item item;
     private boolean imgStatus;
 
     public NewsParser(String url) {
         this.urlString = url;
         this.text = new StringBuilder();
     }
 
     public void parse() {
 
         InputStream urlInputStream = null;
         SAXParserFactory spf;
         SAXParser sp;
 
         try {
             URL url = new URL(this.urlString);
             urlInputStream = url.openConnection().getInputStream();
            InputSource is = new InputSource(urlInputStream);
            is.setEncoding("ISO-8859-1");

             spf = SAXParserFactory.newInstance();
             if (spf != null) {
                 sp = spf.newSAXParser();
                sp.parse(is, this);
             }
         } catch (Exception e) {
             throw new RuntimeException(e);
         } finally {
             try {
                 if (urlInputStream != null) urlInputStream.close();
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
     }
 
     public RssFeed getFeed() {
         return (this.rssFeed);
     }
 
     public void startElement(String uri, String localName, String qName,
                              Attributes attributes) {
         if (qName.equalsIgnoreCase("channel"))
             this.rssFeed = new RssFeed();
         else if (qName.equalsIgnoreCase("item") && (this.rssFeed != null)) {
             this.item = new Item();
             this.rssFeed.addItem(this.item);
         } else if (qName.equalsIgnoreCase("image") && (this.rssFeed != null))
             this.imgStatus = true;
     }
 
     public void endElement(String uri, String localName, String qName) {
         if (this.rssFeed == null)
             return;
 
         if (qName.equalsIgnoreCase("item"))
             this.item = null;
 
         else if (qName.equalsIgnoreCase("image"))
             this.imgStatus = false;
 
         else if (qName.equalsIgnoreCase("title")) {
             if (this.item != null) this.item.title = this.text.toString().trim();
             else if (this.imgStatus) this.rssFeed.imageTitle = this.text.toString().trim();
             else this.rssFeed.title = this.text.toString().trim();
         } else if (qName.equalsIgnoreCase("link")) {
             if (this.item != null) this.item.link = this.text.toString().trim();
             else if (this.imgStatus) this.rssFeed.imageLink = this.text.toString().trim();
             else this.rssFeed.link = this.text.toString().trim();
         } else if (qName.equalsIgnoreCase("description")) {
             if (this.item != null) this.item.description = this.text.toString().trim();
             else this.rssFeed.description = this.text.toString().trim();
         } else if (qName.equalsIgnoreCase("url") && this.imgStatus)
             this.rssFeed.imageUrl = this.text.toString().trim();
 
         else if (qName.equalsIgnoreCase("language"))
             this.rssFeed.language = this.text.toString().trim();
 
         else if (qName.equalsIgnoreCase("generator"))
             this.rssFeed.generator = this.text.toString().trim();
 
         else if (qName.equalsIgnoreCase("copyright"))
             this.rssFeed.copyright = this.text.toString().trim();
 
         else if (qName.equalsIgnoreCase("pubDate") && (this.item != null)) {
             try {
                 this.item.pubDate = sdf.parse(this.text.toString().trim());
             } catch (ParseException e) {
                 throw new RuntimeException();
             }
         } else if (qName.equalsIgnoreCase("category") && (this.item != null))
             this.rssFeed.addItem(this.text.toString().trim(), this.item);
 
         this.text.setLength(0);
     }
 
     public void characters(char[] ch, int start, int length) {
         this.text.append(ch, start, length);
     }
 
     public static class RssFeed {
         public String title;
         public String description;
         public String link;
         public String language;
         public String generator;
         public String copyright;
         public String imageUrl;
         public String imageTitle;
         public String imageLink;
 
         private ArrayList<Item> items;
         private HashMap<String, ArrayList<Item>> category;
 
         public void addItem(Item item) {
             if (this.items == null)
                 this.items = new ArrayList<Item>();
             this.items.add(item);
         }
 
         public void addItem(String category, Item item) {
             if (this.category == null)
                 this.category = new HashMap<String, ArrayList<Item>>();
             if (!this.category.containsKey(category))
                 this.category.put(category, new ArrayList<Item>());
             this.category.get(category).add(item);
         }
 
         public ArrayList<Item> getItems() {
             return items;
         }
     }
 
    public static class Item implements Comparable<Item> {
         public String title;
         public String description;
         public String link;
         public Date pubDate;
         private String url;
 
         public String toString() {
             return (this.title + ": " + this.pubDate + "n" + this.description);
         }
 
         public int compareTo(Item o) {
             return (int) (o.pubDate.getTime() - pubDate.getTime());
         }
 
         public Date getPubDate() {
             return pubDate;
         }
 
         public String getDescription() {
             return description;
         }
 
 
         public String getTitle() {
             return title;
         }
 
         public String getUrl() {
             return link;
         }
     }
 }
 
