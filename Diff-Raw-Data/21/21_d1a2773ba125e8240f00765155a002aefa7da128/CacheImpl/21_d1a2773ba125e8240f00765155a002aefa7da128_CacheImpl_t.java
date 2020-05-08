 package org.xbrlapi.cache;
 
 /**
  * Translates 
  * 1. original URLs into cache File objects or cache URL objects
  * 2. cache URLs into original URL objects
  * The translation from cache URLs to original URLs is a hack that
  * enables relative URLs in cached files to be identified as such an
  * resolved to obtain the original URL of the resource identified
  * by the relative URL.
  * This class also provides a method for testing if a URL is a cache URL.
  * @author Geoffrey Shuetrim (geoff@galexy.net) 
  */
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.xbrlapi.utilities.XBRLException;
 
 public class CacheImpl {
 
 	Logger logger = Logger.getLogger(CacheImpl.class);
 	
     /**
      * Root of the local document cache.
      */
     private File cacheRoot;
     
     /**
      * The map of local URLs to use in place of
      * original URLs.  The original URL points to the 
      * local URL in the map that is used.
      * TODO Implement the cache map as an object that can be chosen via dependency injection.
      */
     private Map<String,String> urlMap = null;      
 
     /**
      * Constructs a URL translator for usage with a local cache location.
      * @param cacheRoot
      * @throws XBRLException if the cacheRoot is null or does not exist.
      */
 	public CacheImpl(File cacheRoot) throws XBRLException {
 		if (cacheRoot == null) throw new XBRLException("The cache root is null.");
 		if (! cacheRoot.exists()) throw new XBRLException("The cache " + cacheRoot + " does not exist.");
 		this.cacheRoot = cacheRoot;
 	}
     
     /**
      * Constructs a URL translator for usage with a local cache location.
      * @param cacheRoot
 	 * @param urlMap The map from original URLs to local URLs.
      * @throws XBRLException if the cacheRoot is null or does not exist
      * or if any of the objects in the list of URLs is not a java.net.URL object.
      */
 	public CacheImpl(File cacheRoot, Map<String,String> urlMap) throws XBRLException {
 		this(cacheRoot);		
 		this.urlMap = urlMap;
 	}	
 	
     /**
      * Tests if a URL is a URL of a resource in the local cache.
      * @param url The URL to be tested to see if it identifies a 
      * resource in the local cache.
      * @return true if and only if the URL is for a resource in the 
      * local cache.
      * @throws XBRLException if the URL status as a cache URL cannot be determined.
      */
     public boolean isCacheURL(URL url) throws XBRLException {
     	logger.debug(System.currentTimeMillis() + " Checking if " + url + " is in the cache.");
     	
     	if (! url.getProtocol().equals("file")) {
         	logger.debug(System.currentTimeMillis() + " Protocol is wrong so not in cache.");
     		return false;
     	}

     	try {
     	    // TODO Make this test work for windows paths.
     	    logger.debug("The canonical path to the cache root is: " + cacheRoot.getCanonicalPath());
             logger.debug("The path for the URL being tested is: " + url.getPath());

            String urlPath = "";
            try {
                urlPath = new File(url.getPath()).getCanonicalPath();
                logger.debug("Canonicalised URL path is: " + urlPath);
            } catch (Exception couldNotCanonicaliseURLPath) {
                logger.debug(System.currentTimeMillis() + " Could not canonicalise URL Path " + url.getPath() + " so we do not have a cache URL.");
                return false;
            }

            if (urlPath.startsWith(cacheRoot.getCanonicalPath().toString())) {
                logger.debug(System.currentTimeMillis() + " Path is right so is in cache.");
                return true;
            }

     	} catch (Exception e) {
     		throw new XBRLException("The cannonical cache root path cannot be determined.",e);
     	}
     	logger.debug(System.currentTimeMillis() + " Path is wrong so not in cache.");    	
     	return false;
     }
     
     /**
      * TODO Modify to use the java.net.URLEncoder and java.net.URLDecoder classes.
      * Adds the resource at the original URL to the cache if it is not already cached.
      * @param url The URL to be translated into a cache URL (if necessary).
      * @return the cache URL corresponding to the provided URL.
      * @throws MalformedURLException if the cache file does not map to a URL.
      * @throws XBRLException if the resource cannot be cached.
      */
     public URL getCacheURL(URL url) throws XBRLException {
 
     	logger.debug(System.currentTimeMillis() + " About to get the cache URL for " + url);
     	
         try {
         	// First determine the original URL
         	URL originalURL = url;
         	if (isCacheURL(url)) {
         		originalURL = getOriginalURL(url);
     		} else {
     			if (urlMap != null) {
     	         	logger.debug(System.currentTimeMillis() + " About to check URL against URL map.");
     	        	if (urlMap.containsKey(url.toString())) {
 	        	        originalURL = new URL(urlMap.get(url.toString()));
     	        	}
     	        	logger.debug(System.currentTimeMillis() + " Done checking URL against URL map.");
     			}
     		}
         	
         	// Second determine the cache file from the original URL
         	// so that the caching status can be checked.
         	File cacheFile = getCacheFile(originalURL);
     		if (! cacheFile.exists()) {
     			copyToCache(originalURL,cacheFile);
     		}
     
     		logger.debug(System.currentTimeMillis() + " Got the cache URL " + cacheFile.toURI().toURL());
     
     		if (! cacheFile.exists()) {
     			logger.info(System.currentTimeMillis() + " " + originalURL + " could not be cached.");
     			return originalURL;
     		} else {
     			return cacheFile.toURI().toURL();
     		}
         } catch (MalformedURLException e) {
             throw new XBRLException(url + " is a malformed URL.", e);
         }
     }
     
 
     
     /**
      * @param url The URL to be translated into an original URL (if necessary).
      * @return the original (non-cache) URL corresponding to the provided URL.
      * @throws XBRLException if a caching operation fails 
      * or if a cache file cannot be translated into a URL.
      */
     public URL getOriginalURL(URL url) throws XBRLException {
     	
     	logger.debug(System.currentTimeMillis() + " About to get the original URL for " + url);
     	
     	// Just return the url if it is not a cache URL
     	if (! isCacheURL(url)) {
     		logger.debug(System.currentTimeMillis() + " Returning the URL as it is already original.");
     		return url;
     	}
 
 		String path = url.getPath().substring(1);
 		String filename = url.getFile();
 
 		// Translate file separator into slashes 
 		path = path.replace(File.separatorChar,'/');
 
 		// Eliminate the cacheRoot part of the path
 		try {
 			path = path.replace(cacheRoot.getCanonicalPath().toString().substring(1),"").substring(1);
 		} catch (IOException e) {
 			throw new XBRLException("The original URL could not be determined for " + url);
 		}
 		
 		// Retrieve the protocol
 		String[] components = path.split("/");
 		String protocol = components[0];
 		String authority = components[1];
 		if (authority.equals("null")) authority = null;
 		int port = new Integer(components[2]).intValue();
 		path = "";
 		for (int i=3; i<components.length; i++) {
 			path = path + "/" + components[i];
 		}
 
 		try {
 			URL originalURL = new URL(protocol, authority, port, path);
 	    	logger.debug(System.currentTimeMillis() + " Got the original URL " + originalURL);
 			return originalURL;
 		} catch (MalformedURLException e) {
 			throw new XBRLException("Malformed original URL.",e);
 		}
 
     }
     
     /**
      * TODO Consider using StringTokeniser for this transform.
      * Gets the cache file for an original URL.
      * @param url The URL to obtain the cache file for,
      * @return The File for the provided URL.
      */
     public File getCacheFile(URL url) {
     	
 /*        
  * Usage of the StringTokeniser
  * String localFile=null;
  * StringTokenizer st=new StringTokenizer(url.getFile(), "/");
  * while (st.hasMoreTokens()) localFile=st.nextToken();
  * fos = new FileOutputStream(localFile);
 */    	
     	logger.debug(System.currentTimeMillis() + " Getting the cache file for " + url);
     	String relativeLocation = getRelativeLocation(url);
     	logger.debug(System.currentTimeMillis() + " Done getting the cache file " + (new File(cacheRoot,relativeLocation)));
     	return new File(cacheRoot,relativeLocation);
     }
     
     /**
      * Copy the original resource into the local cache if the resource exists and is
      * able to be copied into the cache and does nothing otherwise.  Thus, caching fails
      * silently.
      * @param originalURL the URL of the resource to be copied into the cache.
      * @param cacheFile The file to be used to store the cache version of the resource.
      */
     public void copyToCache(URL originalURL, File cacheFile) {
     	
     	logger.info("Attempting to cache: " + originalURL);
     	
     	// If necessary, create the directory to contain the cached resource
 		File parent = cacheFile.getParentFile();
 		if (parent != null) parent.mkdirs();
 		
 		try {
 
 			// Establish the connection to the original CacheURLImpl data source
 		    URLConnection urlCon =  originalURL.openConnection();
 	        
 		    BufferedInputStream bis = new BufferedInputStream(urlCon.getInputStream());
 		    
 		    // Establish the connection to the destination file
 		    FileOutputStream fos = new FileOutputStream(cacheFile);
 		    BufferedOutputStream bos = new BufferedOutputStream(fos);
 	
 		    // Write the source file to the destination file
 		    int bite = bis.read();
 		    while (bite != -1) {
 				bos.write(bite);
 				bite = bis.read();
 		    }
 	
 		    // Clean up the reader and writer
 		    bos.flush();
 		    bis.close();
 		    bos.close();
     
 		} catch (java.net.NoRouteToHostException e) {
 		    ;
 		} catch (FileNotFoundException e) {
 			 ;
 		} catch (IOException e) {
 			 ;
 		}
     }
     
     /**
      * Copy the original resource into the local cache.
      * @param originalURL the URL of the resource to be copied into the cache.
      * @param xml The XML to store in the cache at the given URL.
      * @throws XBRLException if the resource cannot be copied into the local cache.
      */
     public void copyToCache(URL originalURL, String xml) throws XBRLException {
     	
     	logger.debug("Attempting to cache a string XML document using : " + originalURL);
 
     	File cacheFile = this.getCacheFile(originalURL);
     	
     	logger.debug("The cache file is : " + cacheFile.toString());
     	
     	// If necessary, create the directory to contain the cached resource
 		File parent = cacheFile.getParentFile();
 		if (parent != null) parent.mkdirs();
 
 		try {
 	        FileWriter out = new FileWriter(cacheFile);
 	        out.write(xml);
 	        out.close();		
 		} catch (IOException e) {
 			 throw new XBRLException("The String resource could not be cached.",e);
 		}
     }    
     
     /**
      * Get the location of the resource relative to the cache root that
      * is implied by the supplied URL.
      * @param url The URL to analyse to determine the implied relative path
      * to the resource in the local cache.
      * @return the path to the locally cached resource that is implied by the
      * URL.
      */
     private String getRelativeLocation(URL url) {
     	
     	logger.debug(System.currentTimeMillis() + " Getting the relative location for " + url);
     	
 		String protocol = url.getProtocol();
 		String authority = url.getAuthority();
 		if (authority != null) {
 	        if (authority.contains(":")) 
 	            authority = authority.substring(0,authority.indexOf(":"));
 		}
 		int port = url.getPort();
 		String path = url.getPath().substring(1);
 		
 		// Make default ports explicit.
 		if (port == -1) port = url.getDefaultPort();
 		String portValue = (new Integer(port)).toString();
 
 		// Translate slashes into the local file separator 
 		path = path.replace('/',File.separatorChar);
 		
 		String relativeLocation = protocol;
 		relativeLocation = relativeLocation.concat(File.separator+authority);
 		relativeLocation = relativeLocation.concat(File.separator+portValue);
 		relativeLocation = relativeLocation.concat(File.separator+path);
 
     	logger.debug(System.currentTimeMillis() + " Got relative location " + relativeLocation);
 		
 		return relativeLocation;
     	
     }
     
 
     
     /**
      * Delete a resource from the cache.
      * @param url The original or the cache URL.
      * @throws XBRLException
      */
     public void purge(URL url) throws XBRLException {
     	URL originalURL = getOriginalURL(url);
 		File file = this.getCacheFile(url);
 		file.delete();
         logger.info("Purged " + file);
     }
     
 }
