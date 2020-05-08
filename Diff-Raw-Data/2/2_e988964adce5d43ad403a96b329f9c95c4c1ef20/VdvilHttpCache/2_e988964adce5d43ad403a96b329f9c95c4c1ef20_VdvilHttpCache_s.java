 package org.codehaus.httpcache4j.cache;
 
 import no.lau.vdvil.cache.CacheFacade;
 import org.codehaus.httpcache4j.HTTPRequest;
 import org.codehaus.httpcache4j.HTTPResponse;
 import org.codehaus.httpcache4j.client.HTTPClientResponseResolver;
 import java.io.*;
 import java.net.URI;
 import java.net.URL;
 
 /**
  * Wrapper class around HttpCache4J to make it more usable for VDvil usage
  */
 public class VdvilHttpCache extends CacheFacade {
 
 
     HTTPCache persistentcache = new HTTPCache(new PersistentCacheStorage(1000, new File(storeLocation()), "vaudeville"), HTTPClientResponseResolver.createMultithreadedInstance());
 
     /*
     To avoid instantiating the old fashioned way
      */
     private VdvilHttpCache() { }
     public static VdvilHttpCache create() {
         return new VdvilHttpCache();
     }
 
     String storeLocation() {
         return "/tmp/vdvil";
     }
 
     /**
      * A shorthand for fetching files if they have been downloaded to disk
      * Used by testing purposes
      *
      * @param url to the file
      * @return the file or null if empty
      */
     public File fetchFromInternetOrRepository(URL url, String checksum) throws IOException {
        fetchAsStream(url);//Load to cache
         File locationOnDisk = fileLocation(url);
         if(existsInRepository(locationOnDisk, checksum))
             return locationOnDisk;
         else
             throw new FileNotFoundException(url + " could not be downloaded and retrieved in repository. Checksum was:" + checksum);
     }
 
     /**
      * @param url location of file to download
      * @return the file or null if file not found. Not a good thing to do!
      */
     public InputStream fetchAsStream(URL url) throws IOException {
         return new FileInputStream(fetchFromInternetOrRepository(url, null));
     }
 
     enum accepts { HTTP, HTTPS }
 
     public boolean accepts(URL url) {
         try {
             accepts.valueOf(url.getProtocol().toUpperCase());
             return true;
         } catch (Exception e) {
             return false;
         }
     }
 
     @Deprecated
     public String mimeType(URL url) {
         return download(url).getPayload().getMimeType().toString();
     }
 
     private HTTPResponse download(URL url) {
         log.info("Downloading from " +  url + " to cache: " + url);
         HTTPRequest fileRequest = new HTTPRequest(URI.create(url.toString()));
         HTTPResponse fileResponse = persistentcache.doCachedRequest(fileRequest);
         if(null != fileResponse.getETag()) {
             log.debug("ET Tag description " + fileResponse.getETag().getDescription());
         } else {
             log.error(url + " missing ET Tag");
         }
         return fileResponse;
     }
 }
