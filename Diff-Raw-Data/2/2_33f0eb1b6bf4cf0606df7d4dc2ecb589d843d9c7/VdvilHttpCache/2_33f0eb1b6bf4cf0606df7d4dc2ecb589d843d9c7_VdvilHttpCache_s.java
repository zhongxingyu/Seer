 package org.codehaus.httpcache4j.cache;
 
 import no.lau.vdvil.cache.VdvilCache;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.codehaus.httpcache4j.HTTPRequest;
 import org.codehaus.httpcache4j.HTTPResponse;
 import org.codehaus.httpcache4j.client.HTTPClientResponseResolver;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.*;
 import java.net.URI;
 
 /**
  * Wrapper class around HttpCache4J to make it more usable for VDvil usage
  */
 public class VdvilHttpCache implements VdvilCache{
 
     Logger log =  LoggerFactory.getLogger(VdvilHttpCache.class);
     String storeLocation = "/tmp/vdvil";
     HTTPCache persistentcache = new HTTPCache(new PersistentCacheStorage(1000, new File(storeLocation), "vaudeville"), HTTPClientResponseResolver.createMultithreadedInstance());
 
     /*
     To avoid instantiating the old fashioned way
      */
     private VdvilHttpCache() { }
     public static VdvilHttpCache create() {
         return new VdvilHttpCache();
     }
 
     /**
      * A shorthand for fetching files if they have been downloaded to disk
      * Used by testing purposes
      *
      * @param url to the file
      * @return the file or null if empty
      */
     public File fetchFromInternetOrRepository(String url, String checksum) throws FileNotFoundException {
         fetchAsStream(url);
         File locationOnDisk = fileLocation(url);
         if(existsInRepository(locationOnDisk, checksum))
             return locationOnDisk;
         else
             throw new FileNotFoundException(url + " could not be downloaded and retrieved in repository");
     }
 
     /**
      * @param url location of file to download
      * @return the file or null if file not found. Not a good thing to do!
      */
     public InputStream fetchAsStream(String url) {
         File locationOnDisk = fileLocation(url);
         if(locationOnDisk.exists() && locationOnDisk.canRead()) {
             log.debug(url + " File already in cache: " + locationOnDisk.getAbsolutePath());
             try {
                 return new FileInputStream(locationOnDisk);
             } catch (FileNotFoundException e) {
                 log.error("This should not happen - the file has already been checked that it is located on disk!", e);
                 throw new RuntimeException("Should not have happened - File has been verified that it is on disk", e);
             }
         }
         else {
             log.info("Did not find " + url + " in cache, downloading");
             return download(url).getPayload().getInputStream();
         }
     }
 
     public File fileLocation(String url) {
         String urlChecksum = DigestUtils.md5Hex(url);
         return new File(storeLocation + "/files/" + urlChecksum + "/default");
     }
 
     public boolean existsInRepository(File pathToFileOnDisk, String checksum) {
         return pathToFileOnDisk.exists() && pathToFileOnDisk.canRead() && validateChecksum(pathToFileOnDisk, checksum);
     }
 
 /*
     public void removeFromCache(String url) {
         throw new RuntimeException("Not implemented yet");
     }
     */
 
     public boolean accepts(String url) {
        return !url.startsWith("http://");
     }
 
     public String mimeType(String url) {
         return download(url).getPayload().getMimeType().toString();
     }
 
     private HTTPResponse download(String url) {
         log.info("Downloading from " +  url + " to cache: " + url);
         HTTPRequest fileRequest = new HTTPRequest(URI.create(url));
         HTTPResponse fileResponse = persistentcache.doCachedRequest(fileRequest);
         if(null != fileResponse.getETag()) {
             log.debug("ET Tag description " + fileResponse.getETag().getDescription());
         } else {
             log.error(url + " missing ET Tag");
         }
         return fileResponse;
     }
 
     /**
      * Calculates the checksum of the Url to find where it is located in cache, then validates the file on disk with the checksum
      * @param file location of the file to verify
      * @param checksum to check the file with
      * @return whether the file validates with the checksum
      */
     boolean validateChecksum(File file, String checksum) {
         try {
             String fileChecksum = DigestUtils.md5Hex(new FileInputStream(file));
             if(fileChecksum.equals(checksum)) {
                 log.debug("Checksum of file on disk matched the provided checksum");
                 return true;
             } else {
                 log.error("Checksums did not match, expected {}, was {}", checksum, fileChecksum);
                 return false;
             }
         } catch (IOException e) {
             return false;
         }
     }
 }
