 package edu.uci.ics.crawler4j.cache;
 
 import java.util.Map;
import java.util.Set;
 import edu.uci.ics.crawler4j.crawler.Page;
 
 public interface ICacheProvider {
 	/*
 	 * This function is called to see if a cached version of this page exists.
 	 * Null means no cached version exists.
 	 */
 	public CachedPage getCachedPage(String url);
 	
 	/*
 	 * This is called to potentially load cached links from the cache provider. If
 	 * links are not cached, returning null will cause the default parser to reparse
 	 * the page content.
 	 */
 	public Map<String, String> getCachedLinks(Page page);
 }
