 package org.xbrlapi.cache;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.xbrlapi.utilities.XBRLException;
 
 /**
  * Translates 
  * 1. original URIs into cache File objects or cache URI objects
  * 2. cache URIs into original URI objects
  * The translation from cache URIs to original URIs is a hack that
  * enables relative URIs in cached files to be identified as such an
  * resolved to obtain the original URI of the resource identified
  * by the relative URI.
  * This class also provides a method for testing if a URI is a cache URI.
  * @author Geoffrey Shuetrim (geoff@galexy.net) 
  */
 public class CacheImpl {
 
 	Logger logger = Logger.getLogger(CacheImpl.class);
 	
     /**
      * Root of the local document cache.
      */
     private File cacheRoot;
     
     /**
      * The map of local URIs to use in place of
      * original URIs.  The original URI points to the 
      * local URI in the map that is used.
      */
     private Map<String,String> uriMap = null;      
 
     /**
      * Constructs a URI translator for usage with a local cache location.
     * @param cacheRoot The root directory for the cache.
     * @throws XBRLException if the cacheRoot is null or does not exist or cannot be
     * written to or read from.
      */
 	public CacheImpl(File cacheRoot) throws XBRLException {
 		if (cacheRoot == null) throw new XBRLException("The cache root is null.");
 		if (! cacheRoot.exists()) throw new XBRLException("The cache " + cacheRoot + " does not exist.");
		if (! cacheRoot.canWrite()) throw new XBRLException("The cache " + cacheRoot + " cannot be written to.");
        if (! cacheRoot.canRead()) throw new XBRLException("The cache " + cacheRoot + " cannot be read.");
 		this.cacheRoot = cacheRoot;
 	}
     
     /**
      * Constructs a URI translator for usage with a local cache location.
      * @param cacheRoot
 	 * @param uriMap The map from original URIs to local URIs.
      * @throws XBRLException if the cacheRoot is null or does not exist
      */
 	public CacheImpl(File cacheRoot, Map<String,String> uriMap) throws XBRLException {
 		this(cacheRoot);
 		this.uriMap = uriMap;
 	}	
 	
     /**
      * Tests if a URI is a URI of a resource in the local cache.
      * @param uri The URI to be tested to see if it identifies a 
      * resource in the local cache.
      * @return true if and only if the URI is for a resource in the 
      * local cache.
      * @throws XBRLException if the URI status as a cache URI cannot be determined.
      */
     public boolean isCacheURI(URI uri) throws XBRLException {
     	logger.debug("Checking if " + uri + " is in the cache.");
     	
     	if (! uri.getScheme().equals("file")) {
         	logger.debug("Protocol is wrong so not in cache.");
     		return false;
     	}
 
     	try {
     	    // TODO Make this test work for windows paths.
     	    logger.debug("The canonical path to the cache root is: " + cacheRoot.getCanonicalPath());
             logger.debug("The path component of the URI being tested is: " + uri.getPath());
 
             String uriPath = "";
             try {
                 uriPath = new File(uri.getPath()).getCanonicalPath();
                 logger.debug("Canonicalised URI path is: " + uriPath);
             } catch (Exception couldNotCanonicaliseURIPath) {
                 logger.debug("Could not canonicalise URI Path " + uri.getPath() + " so we do not have a cache URI.");
                 return false;
             }
 
             if (uriPath.startsWith(cacheRoot.getCanonicalPath().toString())) {
                 logger.debug("Path is right so is in cache.");
                 return true;
             }
 
     	} catch (Exception e) {
     		throw new XBRLException("The canonical cache root path cannot be determined.",e);
     	}
     	logger.debug("Path is wrong so not in cache.");    	
     	return false;
     }
     
     /**
      * TODO Modify to use the java.net.URIEncoder and java.net.URIDecoder classes.
      * Adds the resource at the original URI to the cache if it is not already cached.
      * @param uri The URI to be translated into a cache URI (if necessary).
      * @return the cache URI corresponding to the provided URI.
      * @throws XBRLException if the resource cannot be cached.
      */
     public URI getCacheURI(URI uri) throws XBRLException {
 
     	logger.debug("About to get the cache URI for " + uri);
     	
         try {
         	// First determine the original URI
         	URI originalURI = uri;
         	if (isCacheURI(uri)) {
         		originalURI = getOriginalURI(uri);
     		} else {
     			if (uriMap != null) {
     	        	if (uriMap.containsKey(uri.toString())) {
 	        	        originalURI = new URI(uriMap.get(uri.toString()));
     	        	}
     			}
     		}
         	
         	// Second determine the cache file from the original URI
         	// so that the caching status can be checked.
         	File cacheFile = getCacheFile(originalURI);
         	logger.debug("The cache file is " + cacheFile);
     		if (! cacheFile.exists()) {
     			copyToCache(originalURI,cacheFile);
     		}
     
     		logger.debug("Got the cache URI " + cacheFile.toURI());
     
     		if (! cacheFile.exists()) {
     			logger.info(originalURI + " could not be cached.");
     			return originalURI;
     		}
 			return cacheFile.toURI();
         } catch (URISyntaxException e) {
             throw new XBRLException(uri + " is a malformed URI.", e);
         }
     }
     
 
     
     /**
      * @param uri The URI to be translated into an original URI (if necessary).
      * @return the original (non-cache) URI corresponding to the provided URI.
      * @throws XBRLException if a caching operation fails 
      * or if a cache file cannot be translated into a URI.
      */
     public URI getOriginalURI(URI uri) throws XBRLException {
     	
     	logger.debug("About to get the original URI for " + uri);
     	
     	// Just return the URI if it is not a cache URI
     	if (! isCacheURI(uri)) {
     		logger.debug("Returning the URI as it is already original.");
     		return uri;
     	}
 
 		String path = uri.getPath();
 		
 		try {
 		    path = (new File(path)).getCanonicalPath();
 		} catch (IOException e) {
 		    throw new XBRLException("Canonical path could not be obtained from the URI.",e);
 		}
 
 		// Eliminate the cacheRoot part of the path
 		try {
 			path = path.replace(cacheRoot.getCanonicalPath().toString().substring(1),"").substring(1);
 		} catch (IOException e) {
 			throw new XBRLException("The original URI could not be determined for " + uri);
 		}
 
         // Translate file separator into slashes 
         path = path.replace(File.separatorChar,'/');
 		
         path = path.substring(1);
         
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
 			URI originalURI = new URI(protocol, null,authority, port, path,null,null);
 	    	logger.debug("Got the original URI " + originalURI);
 			return originalURI;
 		} catch (URISyntaxException e) {
 			throw new XBRLException("Malformed original URI.",e);
 		}
 
     }
     
     /**
      * TODO Consider using StringTokeniser for this transform.
      * Gets the cache file for an original URI.
      * @param uri The URI to obtain the cache file for,
      * @return The File for the provided URI.
      */
     public File getCacheFile(URI uri) {
 
         
         /*        
          * Usage of the StringTokeniser
          * String localFile=null;
          * StringTokenizer st=new StringTokenizer(uri.getFile(), "/");
          * while (st.hasMoreTokens()) localFile=st.nextToken();
          * fos = new FileOutputStream(localFile);
         */      
         
     	logger.debug("Getting the cache file for " + uri);
     	String relativeLocation = getRelativeLocation(uri);
     	File cacheFile = new File(cacheRoot,relativeLocation);
     	logger.debug("Done getting the cache file " + cacheFile);
     	return cacheFile;
     }
     
     /**
      * Copy the original resource into the local cache if the resource exists and is
      * able to be copied into the cache and does nothing otherwise.  Thus, caching fails
      * silently.
      * @param originalURI the URI of the resource to be copied into the cache.
      * @param cacheFile The file to be used to store the cache version of the resource.
      */
     public void copyToCache(URI originalURI, File cacheFile) {
     	
     	logger.debug("Attempting to cache: " + originalURI);
         logger.debug("Cache file is: " + cacheFile);
     	
     	// If necessary, create the directory to contain the cached resource
 		File parent = cacheFile.getParentFile();
 		if (parent != null) parent.mkdirs();
 		
 		try {
 
 			// Establish the connection to the original CacheURIImpl data source
 		    InputStream inputStream = null;
 		    
 		    if (originalURI.getScheme().equals("file")) {
 	            String path = originalURI.getPath();
                 File f = new File(path);
 	            inputStream = new FileInputStream(f);
 		    } else {
                 inputStream =  originalURI.toURL().openConnection().getInputStream();
 		    }
 		    
 		    BufferedInputStream bis = new BufferedInputStream(inputStream);
 		    
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
             logger.debug("Done with caching the file.");
 
 		} catch (java.net.NoRouteToHostException e) {
 		    logger.debug(e.getMessage());
 		} catch (FileNotFoundException e) {
             logger.debug(e.getMessage());
 		} catch (IOException e) {
             logger.debug(e.getMessage());
 		}
     }
     
     /**
      * Copy the original resource into the local cache.
      * @param originalURI the URI of the resource to be copied into the cache.
      * @param xml The XML to store in the cache at the given URI.
      * @throws XBRLException if the resource cannot be copied into the local cache.
      */
     public void copyToCache(URI originalURI, String xml) throws XBRLException {
     	
     	logger.debug("Attempting to cache a string XML document using : " + originalURI);
 
     	File cacheFile = this.getCacheFile(originalURI);
     	
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
      * is implied by the supplied URI.
      * @param uri The URI to analyse to determine the implied relative path
      * to the resource in the local cache.
      * @return the path to the locally cached resource that is implied by the
      * URI.
      */
     private String getRelativeLocation(URI uri) {
     	
     	logger.debug("Getting the relative location for " + uri);
     	
 		String scheme = uri.getScheme();
 		logger.debug("URI scheme is " + scheme);
         int port = uri.getPort();
         logger.debug("port is " + port);
         String path = uri.getPath().substring(1);
         logger.debug("Path is " + path);
 		String authority = uri.getAuthority();
 		if (authority != null) {
 	        if (authority.contains(":")) authority = authority.substring(0,authority.indexOf(":"));
 		}
 		logger.debug("Authority is " + authority);
 		
 		// Make default ports explicit.
 		String portValue = (new Integer(port)).toString();
 
 		// Translate slashes into the local file separator 
 		path = path.replace('/',File.separatorChar);
 		
 		String relativeLocation = scheme;
 		relativeLocation = relativeLocation.concat(File.separator+authority);
 		relativeLocation = relativeLocation.concat(File.separator+portValue);
 		relativeLocation = relativeLocation.concat(File.separator+path);
 
     	logger.debug("Got relative location " + relativeLocation);
 		
 		return relativeLocation;
     	
     }
     
 
     
     /**
      * Delete a resource from the cache.
      * @param uri The original or the cache URI.
      */
     public void purge(URI uri) {
     	//URI originalURI = getOriginalURI(uri);
 		File file = this.getCacheFile(uri);
 		file.delete();
         logger.info("Purged " + file);
     }
     
 }
