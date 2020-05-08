 package com.greyzone.indexsearch.impl;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.greyzone.domain.movie.Movie;
 import com.greyzone.domain.tv.Episode;
 import com.greyzone.domain.tv.Show;
 import com.greyzone.indexsearch.IndexSearcher;
 import com.greyzone.settings.ApplicationSettings;
 import com.greyzone.util.FuzzyStringUtils;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.io.SyndFeedInput;
 
 @Service
 public class NzbsOrgSearcher implements IndexSearcher {
 
     @Autowired
     private ApplicationSettings settings;
 
     private List<SyndEntry>     rssList               = null;
     private long                lastUpdate            = 0;
     private static final long   minTimeBetweenUpdates = 1000 * 60 * 5;
 
     @Override
     public String getIndexId(Movie movie) {
         return null;
     }
 
     @Override
     public List<Episode> getIndexIds(Show show, List<Episode> episodes) {
 
         List<Episode> result = new ArrayList<Episode>();
         updateFeedIfNeeded();
 
         try {
 
             for (Episode episode : episodes) {
		
                 for (SyndEntry entry : rssList) {
                     String title = entry.getTitle();
		    boolean foundEntry = false;
                     if (FuzzyStringUtils.fuzzyMatch(episode, title)) {
                         //episode.setNzbFile(getNzbFile(entry.getUri())); //removed this because sabnzbd fetches the file itself
                         episode.setNzbFileUri(entry.getUri());
                         result.add(episode);
			
			// Only add once
			foundEntry = true;
                     }

		    if (foundEntry)
			break;
                 }
             }
 
             return result;
 
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     private void updateFeedIfNeeded() {
         long timeSinceLastUpdate = System.currentTimeMillis() - lastUpdate;
 
         if (rssList == null || (timeSinceLastUpdate > minTimeBetweenUpdates)) {
             try {
                 rssList = new ArrayList<SyndEntry>();
 
                 URL rss = new URL(settings.getNzbsOrgFeed());
 
                 SyndFeedInput input = new SyndFeedInput();
                 SyndFeed feed = input.build(new InputStreamReader(rss.openStream()));
 
                 for (Object entryObj : feed.getEntries()) {
                     SyndEntry entry = (SyndEntry) entryObj;
                     rssList.add(entry);
                 }
 
                 lastUpdate = System.currentTimeMillis();
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
     }
 
     private byte[] getNzbFile(String uri) throws Exception {
 
         URL url = new URL(uri);
 
         ByteArrayOutputStream bais = new ByteArrayOutputStream();
         InputStream is = null;
         try {
             is = url.openStream();
             byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
             int n;
 
             while ((n = is.read(byteChunk)) > 0) {
                 bais.write(byteChunk, 0, n);
             }
         } finally {
             if (is != null) {
                 is.close();
             }
         }
 
         return bais.toByteArray();
 
     }
 
 }
