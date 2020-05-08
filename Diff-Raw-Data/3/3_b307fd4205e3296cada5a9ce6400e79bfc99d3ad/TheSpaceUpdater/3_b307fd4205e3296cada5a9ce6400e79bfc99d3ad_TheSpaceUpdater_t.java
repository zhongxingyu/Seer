 package org.atlasapi.remotesite.space;
 
 import com.google.common.base.Throwables;
import com.metabroadcast.common.http.RequestLimitingSimpleHttpClient;
 import com.metabroadcast.common.http.SimpleHttpClient;
 import com.metabroadcast.common.http.SimpleHttpRequest;
 import com.metabroadcast.common.scheduling.ScheduledTask;
 import com.metabroadcast.common.time.SystemClock;
 import com.metabroadcast.common.time.Timestamp;
 import com.metabroadcast.common.time.Timestamper;
 import org.atlasapi.persistence.content.ContentGroupResolver;
 import org.atlasapi.persistence.content.ContentGroupWriter;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.remotesite.HttpClients;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 
 /**
  */
 class TheSpaceUpdater extends ScheduledTask {
 
     private final Timestamper timestamper = new SystemClock();
     private final ContentResolver contentResolver;
     private final ContentWriter contentWriter;
     private final ContentGroupResolver groupResolver;
     private final ContentGroupWriter groupWriter;
     private final AdapterLog log;
     private final String url;
     private SimpleHttpClient client;
 
     public TheSpaceUpdater(ContentResolver contentResolver, ContentWriter contentWriter, ContentGroupResolver groupResolver, ContentGroupWriter groupWriter, AdapterLog log, String keystore, String password, String url) throws Exception {
         this.contentResolver = contentResolver;
         this.contentWriter = contentWriter;
         this.groupResolver = groupResolver;
         this.groupWriter = groupWriter;
         this.log = log;
         this.url = url;
         if (keystore == null || keystore.isEmpty() || password == null) {
             this.client = new RequestLimitingSimpleHttpClient(HttpClients.webserviceClient(), 10);
         } else {
             this.client = new RequestLimitingSimpleHttpClient(HttpClients.httpsClient(this.getClass().getClassLoader().getResource(keystore), password), 10);
         }
     }
 
     @Override
     public void runTask() {
         try {
             Timestamp start = timestamper.timestamp();
             log.record(new AdapterLogEntry(AdapterLogEntry.Severity.INFO).withDescription("TheSpace update started from " + url).withSource(getClass()));
 
             TheSpaceItemsProcessor itemsProcessor = new TheSpaceItemsProcessor(url, client, log, contentResolver, contentWriter);
             TheSpacePlaylistsProcessor playlistsProcessor = new TheSpacePlaylistsProcessor(url, client, log, contentResolver, contentWriter, groupResolver, groupWriter);
             JsonNode items = client.get(new SimpleHttpRequest<JsonNode>(url + "/items.json", new JSonNodeHttpResponseTransformer(new ObjectMapper())));
             JsonNode playlists = client.get(new SimpleHttpRequest<JsonNode>(url + "/items/playlists.json", new JSonNodeHttpResponseTransformer(new ObjectMapper())));
             itemsProcessor.process(items);
             playlistsProcessor.process(playlists);
 
             Timestamp end = timestamper.timestamp();
             log.record(new AdapterLogEntry(AdapterLogEntry.Severity.INFO).withDescription("TheSpace update completed in " + start.durationTo(end).getStandardSeconds() + " seconds").withSource(getClass()));
         } catch (Exception e) {
             log.record(new AdapterLogEntry(AdapterLogEntry.Severity.ERROR).withCause(e).withSource(getClass()).withDescription("Exception when processing TheSpace data."));
             Throwables.propagate(e);
         }
     }
 }
