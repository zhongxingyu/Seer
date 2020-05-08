 /*
  *   Copyright 2012 George Norman
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 
 package com.thruzero.domain.service.impl;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.log4j.Logger;
 
 import com.sun.syndication.feed.synd.SyndContent;
 import com.sun.syndication.feed.synd.SyndContentImpl;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndEntryImpl;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.feed.synd.SyndFeedImpl;
 import com.sun.syndication.io.SyndFeedInput;
 import com.sun.syndication.io.SyndFeedOutput;
 import com.sun.syndication.io.XmlReader;
 import com.thruzero.common.core.infonode.InfoNodeElement;
 import com.thruzero.common.core.utils.DateTimeUtilsExt;
 import com.thruzero.domain.model.RssFeed;
 import com.thruzero.domain.model.RssFeed.NewsEntry;
 import com.thruzero.domain.service.RssFeedService;
 
 /**
  * Implementation of RssFeedService that reads RSS feeds from various news sources plus writes/publishes feeds 
  * generated from clients of this service.
  * <p>
  * The requested RSS feeds will be read and cached locally, so that subsequent requests return the cached results.
  * Each feed in the cache will be refreshed according to its refresh rate.
  *
  * @author George Norman
  */
 public class SimpleRssFeedService implements RssFeedService {
   private static final Logger logger = Logger.getLogger(SimpleRssFeedService.class);
 
   private final ConcurrentHashMap<String, RssFeed> feedCache = new ConcurrentHashMap<String, RssFeed>();
 
   @Override
   // TODO-p1(george). Add support for multiple users (so every user can have their own title, refresh-rate, etc).
   public RssFeed readRssFeed(int maxEntries, String feedUrl, int refreshRate, boolean includeImage) {
     // TODO-p1(george). Need to clean out stale URLs
     RssFeed result = feedCache.get(feedUrl);
 
     if (result != null && result.getRefreshPoint() < System.currentTimeMillis()) {
       result = null;
     }
 
     if (result == null) {
      long refreshPoint = refreshRate * 60 * 60 * 1000 + System.currentTimeMillis();

       try {
         URL feedSource = new URL(feedUrl);
         SyndFeedInput input = new SyndFeedInput();
         HttpURLConnection connection = (HttpURLConnection)feedSource.openConnection();
         connection.setRequestMethod("GET");
         connection.setAllowUserInteraction(false);
         connection.setConnectTimeout(4000);
         connection.setReadTimeout(4000);
         connection.connect();
         
         SyndFeed feed = input.build(new XmlReader(feedSource));
         @SuppressWarnings("unchecked")
         List<SyndEntry> syndEntries = feed.getEntries();
         List<NewsEntry> newsEntries = new ArrayList<NewsEntry>();
         int count = 0;
 
         for (SyndEntry syndEntry : syndEntries) {
           String description = syndEntry.getDescription() == null ? "" : syndEntry.getDescription().getValue();
           newsEntries.add(new NewsEntry(syndEntry.getTitle(), syndEntry.getLink(), description, includeImage));
           if (++count >= maxEntries) {
             break;
           }
         }
 
         result = new RssFeed(feedUrl, feed.getPublishedDate(), newsEntries, refreshPoint, "");
 
         feedCache.put(feedUrl, result);
       } catch (MalformedURLException e) {
        result = new RssFeed(feedUrl, null, null, refreshPoint, "RSS Feed URL is malformed: " + feedUrl);
       } catch (Exception e) {
        result = new RssFeed(feedUrl, null, null, refreshPoint, "RSS Feed could not be read: "+ e.getClass().getSimpleName() + " [using url: " + feedUrl + "]" );
       }
     }
 
     return result;
   }
 
   @Override
   public void clearReadCache() {
     feedCache.clear();
   }
 
   @Override
   public void writeRssFeed(InfoNodeElement rssFeedNode, Writer writer) {
     // create the RSS feed to be rendered as output
     SyndFeed syndFeed = new SyndFeedImpl();
 
     syndFeed.setFeedType("rss_2.0");
     syndFeed.setTitle(rssFeedNode.getAttributeValue("title"));
     syndFeed.setLink(rssFeedNode.getAttributeValue("link"));
     syndFeed.setDescription(rssFeedNode.getAttributeValue("description"));
 
     Date feedPublishedDate = DateTimeUtilsExt.stringToDate(rssFeedNode.getAttributeValue("publishedDate"), new Date(0));
     syndFeed.setPublishedDate(feedPublishedDate);
 
     // get entries and add each to syndFeed
     InfoNodeElement entriesNode = (InfoNodeElement)rssFeedNode.getChild("entries");
     @SuppressWarnings("unchecked")
     List<InfoNodeElement> entryNodeList = entriesNode.getChildren();
     List<SyndEntry> syndEntryList = new ArrayList<SyndEntry>();
 
     for (InfoNodeElement entryNode : entryNodeList) {
       SyndEntry syndEntry = new SyndEntryImpl();
       syndEntry.setTitle(entryNode.getAttributeValue("title"));
       syndEntry.setLink(entryNode.getAttributeValue("link"));
 
       Date entryPublishedDate = DateTimeUtilsExt.stringToDate(entryNode.getAttributeValue("publishedDate"), new Date(0));
       syndEntry.setPublishedDate(entryPublishedDate);
 
       SyndContent description = new SyndContentImpl();
       description.setType("text/html");
       description.setValue(entryNode.getText());
       syndEntry.setDescription(description);
 
       syndEntryList.add(syndEntry);
     }
     syndFeed.setEntries(syndEntryList);
 
     // write the feed
     SyndFeedOutput syndFeedOutput = new SyndFeedOutput();
     try {
       syndFeedOutput.output(syndFeed, writer);
       writer.flush();
     } catch (Exception e) {
       throw new RuntimeException("Error - failed to write feed output.", e);
     } finally {
       try {
         writer.close();
       } catch (IOException e) {
         logger.error("Failed to close the Feed publisher writer.", e);
       }
     }
   }
 }
