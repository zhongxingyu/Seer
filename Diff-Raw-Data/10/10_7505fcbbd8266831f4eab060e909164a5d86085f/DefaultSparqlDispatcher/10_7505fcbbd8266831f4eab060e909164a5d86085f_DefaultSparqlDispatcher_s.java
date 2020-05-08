 package com.computas.sublima.query.impl;
 
 import com.computas.sublima.query.SparqlDispatcher;
 import com.computas.sublima.query.service.CachingService;
 import com.computas.sublima.query.service.SettingsService;
 import net.spy.memcached.MemcachedClient;
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 /**
  * This component queries RDF triple stores using Sparql. It is threadsafe.
  */
 public class DefaultSparqlDispatcher implements SparqlDispatcher {
   private static Logger logger = Logger.getLogger(DefaultSparqlDispatcher.class);
 
   /**
    * This method takes all SPARQL requests and identifies the corrent receiver.
    *
    * @param query
    * @return Object - based on what method it forwards to. Must be cased in cases
    *         where the returning object is not derived from java.lang.Object
    */
   public Object query(String query) {
     // Mostly a nasty hack for backwards compatibility
     // ASK has been fixed in the AdminService
     // DESCRIBE and CONSTRUCT will both return RDF/XML so we don't need to distringuish them now.
     Pattern p = Pattern.compile("SELECT");
     Matcher m = p.matcher(query);
     if (m.matches()) {
       return query(query, "SELECT");
     } else {
       return query(query, "DESCRIBE");
     }
   }
 
   /**
    * This method takes all SPARQL requests and identifies the corrent receiver.
    *
    * @param query
    * @param queryType whether the query is a DECSRIBE, CONSTRUCT or SELECT
    * @return Object - based on what method it forwards to. Must be cased in cases
    *         where the returning object is not derived from java.lang.Object
    */
 
   public Object query(String query, String queryType) {
     String result = null;
     HttpURLConnection con = null;
 
     // Configure memcached if available.
     CachingService cache = new CachingService();
     MemcachedClient memcached = cache.connect();
 
     String url = SettingsService.getProperty("sublima.sparql.endpoint");
     logger.info("SPARQLdispatcher executing.\n" + query + "\n");
     String cacheString = query.replaceAll("\\s+", " ") + SettingsService.getProperty("sublima.base.url");
     String cacheKey = String.valueOf(cacheString.hashCode()); // We could parse the query first to get a better key
     //  logger.trace("SPARQLdispatcher hashing for use as key.\n" + cacheString + "\n");
     Object fromCache = cache.get(memcached, cacheKey);
 
     if (fromCache == null) {
       logger.debug("SPARQLdispatcher found nothing in the cache.");
       try {
         URL u = new URL(url + "?query=" + URLEncoder.encode(query, "UTF-8"));
         logger.debug("SPARQLdispatcher connected to the triplestore.");
         long connecttime = System.currentTimeMillis();
         con = (HttpURLConnection) u.openConnection();
         if ("SELECT".equals(queryType) || "ASK".equals(queryType)) {
           con.setRequestProperty("Accept", "application/sparql-results+xml");
         }
 
         result = IOUtils.toString(con.getInputStream());
         long requesttime = System.currentTimeMillis() - connecttime;
         logger.info("SPARQLdispatcher got results from the triplestore. Query took " + requesttime + " ms.");
         if (cache.useMemcached()) {
           memcached.set(cacheKey, 60 * 60 * 24 * 30, result);
         }
       } catch (Exception e) {
         logger.error("SPARQLdispatcher got " + e.toString() + " while talking to the endpoint, with message: " + e.getMessage());
         con.disconnect();
         e.printStackTrace();
       }
       con.disconnect();
     } else {
       logger.debug("SPARQLdispatcher using cache.");
       result = fromCache.toString();
     }
     cache.close(memcached);
     System.gc();
     return result;
   }
 
   public String getResultsAsJSON(String query) {
     String result = null;
     HttpURLConnection con = null;
 
     // Configure memcached if available.
     CachingService cache = new CachingService();
     MemcachedClient memcached = cache.connect();
 
     String url = SettingsService.getProperty("sublima.sparql.directendpoint");
     logger.info("SPARQLdispatcher executing.\n" + query + "\n");
     String cacheString = query.replaceAll("\\s+", " ") + SettingsService.getProperty("sublima.base.url");
     String cacheKey = String.valueOf(cacheString.hashCode()); // We could parse the query first to get a better key
     //  logger.trace("SPARQLdispatcher hashing for use as key.\n" + cacheString + "\n");
     Object fromCache = cache.get(memcached, cacheKey);
 
     if (fromCache == null) {
       logger.debug("SPARQLdispatcher found nothing in the cache.");
       try {
         URL u = new URL(url + "?query=" + URLEncoder.encode(query, "UTF-8") + "&format=" + URLEncoder.encode("application/sparql-results+json", "UTF-8"));
         logger.debug("SPARQLdispatcher connected to the triplestore.");
         long connecttime = System.currentTimeMillis();
         con = (HttpURLConnection) u.openConnection();
         con.setRequestProperty("Accept", "application/sparql-results+json");
 
         result = IOUtils.toString(con.getInputStream());
         long requesttime = System.currentTimeMillis() - connecttime;
         logger.info("SPARQLdispatcher got results from the triplestore. Query took " + requesttime + " ms.");
         if (cache.useMemcached()) {
           memcached.set(cacheKey, 60 * 60 * 24 * 30, result);
         }
       } catch (Exception e) {
         logger.error("SPARQLdispatcher got " + e.toString() + " while talking to the endpoint, with message: " + e.getMessage());
         con.disconnect();
         e.printStackTrace();
       }
       con.disconnect();
     } else {
       logger.debug("SPARQLdispatcher using cache.");
       result = fromCache.toString();
     }
     cache.close(memcached);
     System.gc();
     return result;
   }
 }
