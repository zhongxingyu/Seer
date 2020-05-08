 package ch.bergturbenthal.image.provider.service;
 
 import java.io.File;
 import java.lang.ref.WeakReference;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
import java.util.WeakHashMap;
 import java.util.concurrent.Future;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.client.RestTemplate;
 
 import android.util.Log;
 import ch.bergturbenthal.image.data.model.AlbumDetail;
 import ch.bergturbenthal.image.data.model.AlbumEntry;
 import ch.bergturbenthal.image.data.model.AlbumImageEntry;
 import ch.bergturbenthal.image.data.model.AlbumList;
 import ch.bergturbenthal.image.provider.util.DummyFuture;
 
 public class ServerConnection {
   private static interface ConnectionCallable<V> {
     ResponseEntity<V> call(URL baseUrl) throws Exception;
   }
 
   private String instanceId;
   private final AtomicReference<Collection<URL>> connections = new AtomicReference<Collection<URL>>(Collections.<URL> emptyList());
   private final AtomicReference<WeakReference<Map<String, String>>> albumIds = new AtomicReference<WeakReference<Map<String, String>>>();
   private final RestTemplate restTemplate = new RestTemplate(true);
  private final Map<String, AlbumDetail> albumDetailCache = new WeakHashMap<String, AlbumDetail>();
 
   public AlbumDetail getAlbumDetail(final String albumName) {
    final AlbumDetail cachedValue = albumDetailCache.get(albumName);
    if (cachedValue != null)
      return cachedValue;
     final String albumId = resolveAlbumName(albumName);
     final AlbumDetail albumDetail = callOne(new ConnectionCallable<AlbumDetail>() {
 
       @Override
       public ResponseEntity<AlbumDetail> call(final URL baseUrl) throws Exception {
         return restTemplate.getForEntity(baseUrl.toExternalForm() + "/albums/" + albumId + ".json", AlbumDetail.class);
       }
     });
     final Map<String, String> entryIdMap = new HashMap<String, String>();
     for (final AlbumImageEntry entry : albumDetail.getImages()) {
       entryIdMap.put(entry.getName(), entry.getId());
     }
    albumDetailCache.put(albumName, albumDetail);
     return albumDetail;
   }
 
   public String getInstanceId() {
     return instanceId;
   }
 
   public Collection<String> listAlbums() {
     return collectAlbums().keySet();
   }
 
   public Future<Boolean> readThumbnail(final String albumName, final String file, final File tempFile, final File targetFile) {
     final String albumId = resolveAlbumName(albumName);
     return new DummyFuture<Boolean>(Boolean.TRUE);
   }
 
   public void setInstanceId(final String instanceId) {
     this.instanceId = instanceId;
   }
 
   public void updateServerConnections(final Collection<URL> value) {
     connections.set(value);
   }
 
   private <V> V callOne(final ConnectionCallable<V> callable) {
     Throwable t = null;
     for (final URL connection : connections.get()) {
       try {
         final ResponseEntity<V> response;
         response = callable.call(connection);
         if (response != null && response.hasBody())
           return response.getBody();
       } catch (final Throwable ex) {
         if (t != null)
           Log.w("Server-connection", "Exception while calling server " + instanceId, t);
         t = ex;
       }
     }
     if (t != null)
       throw new RuntimeException("Cannot connect to server " + instanceId, t);
     else
       throw new RuntimeException("Cannot connect to server " + instanceId + ", no valid connection found");
   }
 
   private synchronized Map<String, String> collectAlbums() {
     final WeakReference<Map<String, String>> reference = albumIds.get();
     if (reference != null) {
       final Map<String, String> cachedMap = reference.get();
       if (cachedMap != null)
         return cachedMap;
     }
     final Map<String, String> ret = new HashMap<String, String>();
     final AlbumList albums = callOne(new ConnectionCallable<AlbumList>() {
 
       @Override
       public ResponseEntity<AlbumList> call(final URL baseUrl) throws Exception {
         return restTemplate.getForEntity(baseUrl.toExternalForm() + "/albums.json", AlbumList.class);
       }
     });
     for (final AlbumEntry entry : albums.getAlbumNames()) {
       ret.put(entry.getName(), entry.getId());
     }
     albumIds.set(new WeakReference<Map<String, String>>(ret));
     return ret;
   }
 
   private String resolveAlbumName(final String albumName) {
     return collectAlbums().get(albumName);
   }
 }
