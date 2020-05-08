 package grails.plugins.sitemapper.impl;
 
 import grails.plugins.sitemapper.EntryWriter;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Date;
 
 /**
  * Responsible for printing sitemap
  * entries as XML to output stream.
  *
  * @author Kim A. Betti
  */
 final class XmlEntryWriter implements EntryWriter {
 
   private final SitemapDateUtils dateUtils;
   private final PrintWriter writer;
   private final String serverUrl;
 
   private final static String URL_OPEN = "<url>";
   private final static String URL_CLOSE = "</url>\n";
 
   public final static String LOCATION_TAG = "loc";
   public final static String LAST_MOD_TAG = "lastmod";
   public final static String CHANGE_FREQ_TAG = "changefreq";
   public final static String PRIORITY_TAG = "priority";
 
   public XmlEntryWriter(PrintWriter writer, String serverUrl) {
     this.dateUtils = new SitemapDateUtils();
     this.serverUrl = serverUrl;
     this.writer = writer;
   }
 
   @Override
   public void addEntry(String location, Date modifiedAt) throws IOException {
     writer.print(URL_OPEN);
     printLocation(location);
     printLastModification(modifiedAt);
     writer.print(URL_CLOSE);
   }
 
   @Override
  public void addEntry(String location, Date modifiedAt, String changeFrequency, double priority) throws IOException {
     writer.print(URL_OPEN);
     printLocation(location);
     printLastModification(modifiedAt);
     printChangeFrequency(changeFrequency);
     printPriority(priority);
     writer.print(URL_CLOSE);
   }
 
   private void printLocation(String locationUrl) throws IOException {
     printTag(LOCATION_TAG, serverUrl + locationUrl);
   }
 
   private void printLastModification(Date modifiedAt) throws IOException {
     printTag(LAST_MOD_TAG, dateUtils.formatForSitemap(modifiedAt));
   }
 
   private void printChangeFrequency(String changeFrequency) throws IOException {
     printTag(CHANGE_FREQ_TAG, changeFrequency);
   }
 
  private void printPriority(double priority) throws IOException {
     printTag(PRIORITY_TAG, priority + "");
   }
 
   private void printTag(final String tagName, final String value) throws IOException {
     writer.print(String.format("<%s>%s</%1$s>", tagName, value));
   }
 
 }
