 package org.jboss.jdf.example.ticketmonster.services;
 
 import static org.jboss.jdf.example.ticketmonster.model.MediaType.IMAGE;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
 import javax.inject.Named;
import javax.persistence.EntityManager;
 
 import org.jboss.jdf.example.ticketmonster.model.MediaItem;
 import org.jboss.jdf.example.ticketmonster.model.MediaType;
import org.jboss.jdf.example.ticketmonster.rest.MediaService;
 import org.jboss.jdf.example.ticketmonster.util.Base64;
 import org.jboss.jdf.example.ticketmonster.util.Reflections;
 
 @Named @RequestScoped
 public class MediaManager {
     
     private static final File tmpDir;

    @Inject
    EntityManager entityManager;
     
     static {
         tmpDir = new File(System.getProperty("java.io.tmpdir"), "org.jboss.jdf.examples.ticket-monster");
         if (tmpDir.exists()) {
             if (tmpDir.isFile())
                 throw new IllegalStateException(tmpDir.getAbsolutePath() + " already exists, and is a file. Remove it.");
         } else {
             tmpDir.mkdir();
         }
     }
     
     private final Map<MediaItem, MediaPath> cache;
     
     public MediaManager() {
         
         this.cache = new HashMap<MediaItem, MediaPath>();
     }
     
     private OutputStream getCachedOutputStream(String fileName) {
         try {
              return new FileOutputStream(getCachedFile(fileName));
         } catch (FileNotFoundException e) {
             throw new IllegalStateException("Error creating cached file", e);
         }
     }
     
     public File getCachedFile(String fileName) {
         return new File(tmpDir, fileName);
     }
     
     public MediaPath getPath(MediaItem mediaItem) {
         if (cache.containsKey(mediaItem)) {
             return cache.get(mediaItem);
         } else {
             MediaPath mediaPath = createPath(mediaItem);
             cache.put(mediaItem, mediaPath);
             return mediaPath;
         }
     }
     
     private MediaPath createPath(MediaItem mediaItem) {
         if (!mediaItem.getMediaType().isCacheable()) {
             if (checkResourceAvailable(mediaItem)) {
                 return new MediaPath(mediaItem.getUrl(), false, mediaItem.getMediaType());
             } else {
                 return createCachedMedia(Reflections.getResource("not_available.jpg").toExternalForm(), IMAGE);
             }
         } else {
             return createCachedMedia(mediaItem);
         }   
     }
     
     private boolean checkResourceAvailable(MediaItem mediaItem) {
         URL url = null;
         try {
              url = new URL(mediaItem.getUrl());
         } catch (MalformedURLException e) {}
         
         if (url != null) {
             try {
                 URLConnection connection = url.openConnection();
                 if (connection instanceof HttpURLConnection) {
                     return ((HttpURLConnection) connection).getResponseCode() == HttpURLConnection.HTTP_OK;
                 } else {
                     return connection.getContentLength() > 0;
                 }
             } catch (IOException e) {}
         }
         return false;
     }
     
     private String getCachedFileName(String url) {
         return Base64.encodeToString(url.getBytes(), false);
     }
     
     private boolean alreadyCached(String cachedFileName) {
         File cache = getCachedFile(cachedFileName);
         if (cache.exists()) {
             if (cache.isDirectory()) {
                 throw new IllegalStateException(cache.getAbsolutePath() + " already exists, and is a directory. Remove it.");
             }
             return true;
         } else {
             return false;
         }
     }
     
     private MediaPath createCachedMedia(String url, MediaType mediaType) {
         String cachedFileName = getCachedFileName(url);
         if (!alreadyCached(cachedFileName)) {
             URL _url = null;
             try {
                  _url = new URL(url);
             } catch (MalformedURLException e) {
                 throw new IllegalStateException("Error reading URL " + url);
             }
             
             try {
                 InputStream is = null;
                 OutputStream os = null;
                 try {
                      is = new BufferedInputStream(_url.openStream());
                      os = new BufferedOutputStream(getCachedOutputStream(cachedFileName));
                     while (true) {
                         int data = is.read();
                         if (data == -1)
                             break;
                         os.write(data);
                     }
                 } finally {
                     if (is != null)
                         is.close();
                     if (os != null)
                         os.close();
                 }
             } catch (IOException e) {
                 throw new IllegalStateException("Error caching " + mediaType.getDescription(), e);
             }
         }
         return new MediaPath(cachedFileName, true, mediaType);
     }
     
     private MediaPath createCachedMedia(MediaItem mediaItem) {
         return createCachedMedia(mediaItem.getUrl(), mediaItem.getMediaType());
     }
 
    public File getCachedFile(Long mediaItemId) {
        return getCachedFile(getPath(entityManager.find(MediaItem.class, mediaItemId)).getUrl());
    }
 }
