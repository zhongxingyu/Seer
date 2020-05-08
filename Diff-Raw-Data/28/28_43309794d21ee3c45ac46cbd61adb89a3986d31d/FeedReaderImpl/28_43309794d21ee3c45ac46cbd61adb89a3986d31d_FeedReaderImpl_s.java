 /*
  * Copyright 2009 OW2 Chameleon
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.ow2.chameleon.syndication.rome;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Dictionary;
 import java.util.List;
 import java.util.Properties;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Invalidate;
 import org.apache.felix.ipojo.annotations.Property;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.ServiceProperty;
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventAdmin;
 import org.ow2.chameleon.syndication.FeedEntry;
 import org.ow2.chameleon.syndication.FeedReader;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.sun.syndication.feed.synd.SyndCategory;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.io.FeedException;
 import com.sun.syndication.io.SyndFeedInput;
 import com.sun.syndication.io.XmlReader;
 
 /**
  * Feed Reader Implementation. This implementation retrieved feed entries on
  * startup and periodically.
  */
 @Component(name = "org.ow2.chameleon.syndication.rome.reader", immediate = true)
 @Provides
 public class FeedReaderImpl implements FeedReader {
 
     /**
      * Feed URL.
      */
     private URL m_url;
 
     /**
      * Feed Title (Service Property, computed from the read feed).
      */
     @ServiceProperty(name = FeedReader.FEED_TITLE_PROPERTY)
     private String m_title;
 
     /**
      * Feed URL (String). This is exposed as a service property.
      */
     @ServiceProperty(name = FeedReader.FEED_URL_PROPERTY)
     String m_feedUrl;
 
     /**
      * Number of recent items, retrieved by
      * {@link FeedReader#getRecentEntries()}. 20 by default.
      */
     @Property(name = "feed.recent", value = "20")
     private int m_recentCount;
 
     /**
      * Polling time in ms. By default, 1 hour.
      */
     @Property(name = "feed.period", value = "3600000")
     private int m_period;
 
     /**
      * Event Admin (optional dependency).
      */
     @Requires(optional = true)
     private EventAdmin m_ea;
 
     /**
      * Internal Feed.
      */
     private SyndFeed m_feed;
 
     /**
      * Thread Pool for the polling.
      */
     private ScheduledExecutorService m_pool;
 
     /**
      * More recent entry time.
      */
     private volatile Date m_lastDate;
 
     /**
      * Logger.
      */
     private Logger m_logger = LoggerFactory.getLogger(this.toString());
 
     /**
      * Creates a {@link FeedReaderImpl}. For testing purpose.
      * @param url the url
      * @param period the period
      * @param count the number of recent item
      * @throws FeedException if the feed cannot be read
      * @throws IOException
      */
     public FeedReaderImpl(URL url, int period, int count) throws FeedException,
             IOException {
         m_url = url;
         m_pool = Executors.newScheduledThreadPool(1);
         m_recentCount = count;
         m_period = period;
         if (period != -1) {
             m_pool.scheduleAtFixedRate(new Runnable() {
 
                 public void run() {
                     try {
                         m_logger.info("Reading " + m_url);
                         load();
                     } catch (Exception e) {
                         m_logger.error("Cannot read the feed " + m_url, e);
                     }
                 }
             }, m_period, m_period, TimeUnit.MILLISECONDS);
         }
 
         load();
         m_title = m_feed.getTitle();
     }
 
     /**
      * Creates a {@link FeedReaderImpl}. This constructor is used by iPOJO
      * @throws FeedException if the feed cannot be read
      * @throws IOException if there is an IO issue
      */
     public FeedReaderImpl() throws FeedException, IOException {
         m_pool = Executors.newScheduledThreadPool(1);
         m_pool.scheduleAtFixedRate(new Runnable() {
 
             public void run() {
                 try {
                     m_logger.info("Reading " + m_url);
                     load();
                 } catch (Exception e) {
                     m_logger.error("Cannot read the feed " + m_url, e);
                 }
             }
         }, m_period, m_period, TimeUnit.MILLISECONDS);
     }
 
     /**
      * Sets the URL. (Mandatory property)
      * @param u the url
      * @throws FeedException if the feed cannot be read correctly
      * @throws IOException if the feed cannot be read correctly
      */
     @Property(name = "feed.url", mandatory = true)
     public void setUrl(String u) throws FeedException, IOException {
         m_url = new URL(u);
         m_feedUrl = u;
         load();
         m_title = m_feed.getTitle();
     }
 
     /**
      * Reads the feed.
      * @throws FeedException if the feed cannot be read correctly
      * @throws IOException if the feed cannot be read correctly
      */
     private synchronized void load() throws FeedException, IOException {
         SyndFeedInput input = new SyndFeedInput();
        m_feed = input.build(new XmlReader(m_url));

        // Look for new entries
        for (FeedEntry entry : getRecentEntries()) {
            if (m_lastDate == null
                    || m_lastDate.before(entry.publicationDate())) {
                postEvent(entry);
             }
        }
        if (getLastEntry() != null) {
            m_lastDate = getLastEntry().publicationDate();
         }
     }
 
     /**
      * Gets the list of entries.
      * @return the list of entries
      * @see org.ow2.chameleon.syndication.FeedReader#getEntries()
      */
     @SuppressWarnings("unchecked")
     public synchronized List<FeedEntry> getEntries() {
         List<SyndEntry> entries = m_feed.getEntries();
         List<FeedEntry> result = new ArrayList<FeedEntry>();
         for (SyndEntry e : entries) {
             result.add(new FeedEntryImpl(e));
         }
         return result;
     }
 
     /**
      * Gets the feed url.
      * @return the feed url
      * @see org.ow2.chameleon.syndication.FeedReader#getURL()
      */
     public String getURL() {
         return m_url.toExternalForm();
     }
 
     /**
      * Gets the last entry if any.
      * @return the last entry of <code>null</code> if the feed is empty
      * @see org.ow2.chameleon.syndication.FeedReader#getLastEntry()
      */
     @SuppressWarnings("unchecked")
     public synchronized FeedEntry getLastEntry() {
         List<SyndEntry> entries = m_feed.getEntries();
         if (entries.isEmpty()) {
             return null;
         } else {
             return new FeedEntryImpl(entries.get(0));
         }
     }
 
     /**
      * Gets the recent entries.
      * @return the list of entries.
      * @see org.ow2.chameleon.syndication.FeedReader#getRecentEntries()
      */
     @SuppressWarnings("unchecked")
     public synchronized List<FeedEntry> getRecentEntries() {
         List<SyndEntry> entries = m_feed.getEntries();
         List<FeedEntry> result = new ArrayList<FeedEntry>();
         int count = 0;
         for (SyndEntry e : entries) {
             if (count == m_recentCount) {
                 return result;
             }
             result.add(new FeedEntryImpl(e));
             count++;
         }
         return result;
     }
 
     /**
      * Gets the feed title.
      * @return the title
      * @see org.ow2.chameleon.syndication.FeedReader#getTitle()
      */
     public String getTitle() {
         return m_title;
     }
 
     /**
      * Stop method. Stops the polling.
      */
     @Invalidate
     public void stop() {
         m_pool.shutdownNow();
     }
 
     /**
      * Sends an event on the event admin.
      * @param entry the entry to send
      */
     @SuppressWarnings("unchecked")
     public void postEvent(FeedEntry entry) {
         if (m_ea == null) {
             return;
         } else {
             Dictionary data = new Properties();
             if (entry.author() != null) {
                 data.put(FeedReader.ENTRY_AUTHOR_KEY, entry.author());
             }
             if (entry.categories() != null && !entry.categories().isEmpty()) {
                 data.put(FeedReader.ENTRY_CATEGORIES_KEY, entry.categories());
             }
             if (entry.content() != null) {
                 data.put(FeedReader.ENTRY_CONTENT_KEY, entry.content());
             }
             if (entry.publicationDate() != null) {
                 data.put(FeedReader.ENTRY_DATE_KEY, entry.publicationDate());
             }
             if (entry.title() != null) {
                 data.put(FeedReader.ENTRY_TITLE_KEY, entry.title());
             }
             if (entry.url() != null) {
                 data.put(FeedReader.ENTRY_URL_KEY, entry.url());
             }
             if (m_feed.getTitle() != null) {
                 data.put(FeedReader.FEED_TITLE_KEY, m_feed.getTitle());
             }
             if (m_feedUrl != null) {
                 data.put(FeedReader.FEED_URL_KEY, m_feedUrl);
             }
 
             Event event = new Event(FeedReader.NEW_ENTRY_TOPIC, data);
             m_ea.postEvent(event);
         }
     }
 
     /**
      * Implementation of Feed Entry. These Feed Entries are unmodifiable
      */
     private class FeedEntryImpl implements FeedEntry {
 
         /**
          * Author.
          */
         private final String m_author;
 
         /**
          * Content.
          */
         private final String m_content;
 
         /**
          * Date.
          */
         private final Date m_date;
 
         /**
          * Title.
          */
         private final String m_title;
 
         /**
          * Entry URL.
          */
         private final String m_url;
 
         /**
          * Entry categories.
          */
         private final List<String> m_categories;
 
         /**
          * Creates a {@link FeedEntryImpl} from a {@link SyndEntry}.
          * @param e the {@link SyndEntry}
          */
         public FeedEntryImpl(SyndEntry e) {
             m_author = e.getAuthor();
             m_content = e.getDescription().getValue();
             Date d = e.getPublishedDate();
             if (d == null) {
                 m_date = e.getUpdatedDate();
             } else {
                 m_date = d;
             }
             m_title = e.getTitle();
             m_url = e.getLink();
 
             List<SyndCategory> categories = e.getCategories();
             m_categories = new ArrayList<String>();
             for (SyndCategory cat : categories) {
                 m_categories.add(cat.getName());
             }
         }
 
         /**
          * Gets the entry author.
          * @return the entry author
          * @see org.ow2.chameleon.syndication.FeedEntry#author()
          */
         public String author() {
             return m_author;
         }
 
         /**
          * Gets the categories.
          * @return the categories.
          * @see org.ow2.chameleon.syndication.FeedEntry#categories()
          */
         public List<String> categories() {
             return Collections.unmodifiableList(m_categories);
         }
 
         /**
          * Gets the content.
          * @return the content.
          * @see org.ow2.chameleon.syndication.FeedEntry#content()
          */
         public String content() {
             return m_content;
         }
 
         /**
          * Gets the publication date.
          * @return the publication date.
          * @see org.ow2.chameleon.syndication.FeedEntry#publicationDate()
          */
         public Date publicationDate() {
             return m_date;
         }
 
         /**
          * Gets the title.
          * @return the title
          * @see org.ow2.chameleon.syndication.FeedEntry#title()
          */
         public String title() {
             return m_title;
         }
 
         /**
          * Gets the URL.
          * @return the url
          * @see org.ow2.chameleon.syndication.FeedEntry#url()
          */
         public String url() {
             return m_url;
         }
 
         /**
          * Unsupported method.
          * @param t the author
          * @return N/A
          * @see org.ow2.chameleon.syndication.FeedEntry#author(java.lang.String)
          */
         public FeedEntry author(String t) {
             throw new UnsupportedOperationException(
                     "Cannot modify a read only feed entry");
         }
 
         /**
          * Unsupported method.
          * @param t the category.
          * @return N/A
          * @see org.ow2.chameleon.syndication.FeedEntry#category(java.lang.String)
          */
         public FeedEntry category(String t) {
             throw new UnsupportedOperationException(
                     "Cannot modify a read only feed entry");
         }
 
         /**
          * Unsupported method.
          * @param t the content
          * @return N/A
          * @see org.ow2.chameleon.syndication.FeedEntry#content(java.lang.String)
          */
         public FeedEntry content(String t) {
             throw new UnsupportedOperationException(
                     "Cannot modify a read only feed entry");
         }
 
         /**
          * Unsupported method.
          * @param t the title
          * @return N/A
          * @see org.ow2.chameleon.syndication.FeedEntry#title(java.lang.String)
          */
         public FeedEntry title(String t) {
             throw new UnsupportedOperationException(
                     "Cannot modify a read only feed entry");
         }
 
         /**
          * Unsupported method.
          * @param t the url
          * @return N/A
          * @see org.ow2.chameleon.syndication.FeedEntry#url(java.lang.String)
          */
         public FeedEntry url(String t) {
             throw new UnsupportedOperationException(
                     "Cannot modify a read only feed entry");
         }
 
     }
 
 }
