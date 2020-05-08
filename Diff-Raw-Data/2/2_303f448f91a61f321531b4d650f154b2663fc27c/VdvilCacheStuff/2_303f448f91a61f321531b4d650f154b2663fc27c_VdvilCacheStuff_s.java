 package org.codehaus.httpcache4j.cache;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.commons.httpclient.HttpClient;
 import org.codehaus.httpcache4j.HTTPRequest;
 import org.codehaus.httpcache4j.HTTPResponse;
 import org.codehaus.httpcache4j.client.HTTPClientResponseResolver;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.*;
 import java.net.URI;
 
 /**
  * Wrapper class around HttpCache4J to make it more usable for VDvil usage
  * TODO Make function for invalidatingAllCaches
  */
 public class VdvilCacheStuff {
 
     final Logger log =  LoggerFactory.getLogger(VdvilCacheStuff.class);
     public final HTTPCache persistentcache;
     final File storeLocation;
     final String storeName = "vaudeville";
 
     /**
      * @param location Where to persist the cache files
      */
     public VdvilCacheStuff(File location) {
         storeLocation = location;
         persistentcache = new HTTPCache(new PersistentCacheStorage(1000, location, storeName), new HTTPClientResponseResolver(new HttpClient()));
     }
 
     /**
      * @param url location of stream to download
      * @return InputStream
      */
     public InputStream fetchAsInputStream(String url) {
         File fileInRepository = fetchFromRepository(url);
         if (fileInRepository != null) {
             try {
                 return new FileInputStream(fileInRepository);
             } catch (FileNotFoundException e) { /* If file not found - will continue to download it */}
         }
         HTTPRequest request = new HTTPRequest(URI.create(url));
         HTTPResponse response = persistentcache.doCachedRequest(request);
         return response.getPayload().getInputStream();
     }
 
     /**
      * First performs a validation. Then, tries to download from local file repo before trying to download from the web
      * If the file was downloaded from the internet, perform a second validation to confirm that the file was downloaded successfully
      * @param url location of file to download
      * @param checksum to validate against
      * @return the downloaded file, null if not found
      * @throws java.io.FileNotFoundException if the file could not be downloaded from url of found in cache
      */
     public File fetchAsFile(String url, String checksum) throws FileNotFoundException {
         if (validateChecksum(url, checksum)) {
             log.debug("{} located on disk with correct checksum", url);
             return fetchAsFile(url);
         } else {
            log.info("File failed to pass checksum. Retrying downloading from the URL");
             File fileDownloadedFromTheInternet = downloadFromInternetAsFile(url);
             if(validateChecksum(url, checksum)) {
                 return fileDownloadedFromTheInternet;
             } else {
                 throw new FileNotFoundException("Could not download " + url + " to cache. File in cache failed validation with checksum " + checksum);
             }
         }
     }
 
     /**
      * @param url location of file to download
      * @return the file or null if file not found. Not a good thing to do!
      */
     public File fetchAsFile(String url) {
         File fileInRepository = fetchFromRepository(url);
         if (fileInRepository != null) {
             log.debug("File already in cache: " + url);
             return fileInRepository;
         }
         else {
             return downloadFromInternetAsFile(url);
         }
     }
 
     File downloadFromInternetAsFile(String url) {
         log.info("Downloading " + url + " to cache");
         HTTPRequest fileRequest = new HTTPRequest(URI.create(url));
         HTTPResponse fileResponse = persistentcache.doCachedRequest(fileRequest);
         if (fileResponse.getPayload() instanceof CleanableFilePayload) {
             CleanableFilePayload cleanableFilePayload = (CleanableFilePayload) fileResponse.getPayload();
             return cleanableFilePayload.getFile();
         } else
             return null;
     }
 
     /**
      * A shorthand for fetching files if they have been downloaded to disk
      *
      * @param url to the file
      * @return the file or null if empty
      */
     File fetchFromRepository(String url) {
         String urlChecksum = DigestUtils.md5Hex(url);
         File locationOnDisk = new File(storeLocation + "/files/" + urlChecksum + "/default");
         if (locationOnDisk.exists() && locationOnDisk.canRead())
             return locationOnDisk;
         else
             return null;
     }
 
     /**
      * Calculates the checksum of the Url to find where it is located in cache, then validates the file on disk with the checksum
      * @param url Where the file is located on the web
      * @param checksum to check the file with
      * @return whether the file validates with the checksum
      */
     boolean validateChecksum(String url, String checksum) {
         String urlChecksum = DigestUtils.md5Hex(url);
         File locationOnDisk = new File(storeLocation + "/files/" + urlChecksum + "/default");
         try {
             String fileChecksum = DigestUtils.md5Hex(new FileInputStream(locationOnDisk));
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
