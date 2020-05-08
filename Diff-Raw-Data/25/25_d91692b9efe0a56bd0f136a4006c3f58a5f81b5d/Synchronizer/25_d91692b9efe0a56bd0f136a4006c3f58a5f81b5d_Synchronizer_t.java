 package org.dodgybits.android.shuffle.server.tracks;
 
 import android.content.ContentResolver;
 import android.content.res.Resources;
 import android.text.TextUtils;
 import android.util.Log;
 import android.util.Xml;
 import org.dodgybits.android.shuffle.model.TracksCompatible;
 import org.dodgybits.android.shuffle.service.Progress;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import java.io.StringReader;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Base class for handling synchronization, template method object.
  *
  * @author Morten Nielsen
  */
 public abstract class Synchronizer<Entity extends TracksCompatible> {
     private static final String cTag = "Synchronizer";
     
     protected ContentResolver contentResolver;
     protected Resources resources;
     protected WebClient client;
     protected android.content.Context activity;
     protected final TracksSynchronizer tracksSynchronizer;
     protected SimpleDateFormat simpleDateFormat;
     private int basePercent;
 
     public Synchronizer(ContentResolver contentResolver,
             TracksSynchronizer tracksSynchronizer, WebClient client,
             Resources resources, android.content.Context activity,
             int basePercent) {
         this.contentResolver = contentResolver;
         this.tracksSynchronizer = tracksSynchronizer;
         this.client = client;
         this.resources = resources;
         this.activity = activity;
         this.basePercent = basePercent;
         simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
     }
 
     public void synchronize() {
         tracksSynchronizer.reportProgress(Progress.createProgress(basePercent,
                 readingLocalText()));
         Map<String, Entity> localContexts = getShuffleEntities(contentResolver,
                 resources);
 
         tracksSynchronizer.reportProgress(Progress.createProgress(basePercent,
                 readingRemoteText()));
         Map<Long, Entity> remoteContexts = getTrackEntities();
         int startCounter = localContexts.size() + 1;
         int count = 0;
         for (Entity localContext : localContexts.values()) {
             count++;
             int percent = basePercent
                     + Math.round(((count * 100) / startCounter) * 0.33f);
             tracksSynchronizer.reportProgress(Progress.createProgress(percent,
                     processingText()));
             synchronizeSingle(remoteContexts, localContext);
 
         }
 
         for (Entity remoteContext : remoteContexts.values()) {
             saveLocalEntityFromRemote(remoteContext);
         }
 
         tracksSynchronizer.reportProgress(Progress.createProgress(
                 basePercent + 33, stageFinishedText()));
     }
 
     protected abstract String readingRemoteText();
 
     protected abstract String processingText();
 
     protected abstract String readingLocalText();
 
     protected abstract String stageFinishedText();
 
     protected abstract void saveLocalEntityFromRemote(Entity remoteContext);
 
     protected abstract String endIndexTag();
 
     protected abstract String entityIndexUrl();
 
     protected abstract boolean removeLocalEntity(Entity entity);
 
     protected abstract void saveLocalEntity(Entity entity);
 
     protected abstract Map<String, Entity> getShuffleEntities(
             ContentResolver contentResolver, Resources resources);
     
     protected abstract Entity createMergedLocalEntity(Entity localContext,
             Entity newContext);
     
     protected abstract String createEntityUrl(Entity localContext);
 
     protected abstract String createDocumentForEntity(Entity localContext);
 
     protected abstract Entity parseSingleEntity(XmlPullParser parser)
         throws ParseException;
     
     protected Map<Long, Entity> getTrackEntities() {
         Map<Long, Entity> contexts = new HashMap<Long, Entity>();
         String tracksContextXml;
         try {
             tracksContextXml = client.getUrlContent(entityIndexUrl());
         } catch (WebClient.ApiException e) {
             Log.w(cTag, e);
             return contexts;
         }
 
         XmlPullParser parser = Xml.newPullParser();
         try {
             parser.setInput(new StringReader(tracksContextXml));
 
             int eventType = parser.getEventType();
             boolean done = false;
             while (eventType != XmlPullParser.END_DOCUMENT && !done) {
                 Entity context = parseSingleEntity(parser);
                 if (null != context)
                     contexts.put(context.getTracksId(), context);
                 eventType = parser.getEventType();
                 String name = parser.getName();
                 switch (eventType) {
                 case XmlPullParser.END_TAG:
                     if (name.equalsIgnoreCase(endIndexTag())) {
                         done = true;
                     }
                     break;
                 }
             }
         } catch (ParseException e) {
             Log.w(cTag, e);
         } catch (XmlPullParserException e) {
             Log.w(cTag, e);
         }
         return contexts;
     }
 
     private Entity findEntityByLocalName(Collection<Entity> remoteContexts,
             Entity localContext) {
         Entity foundContext = null;
         for (Entity context : remoteContexts)
             if (context.getLocalName().equals(localContext.getLocalName())) {
                 foundContext = context;
             }
         return foundContext;
     }
     
     private void synchronizeSingle(Map<Long, Entity> remoteEntities,
             Entity localEntity) {
         if (localEntity.getTracksId() == null) {
 
             Entity newContext = findEntityByLocalName(remoteEntities.values(),
                     localEntity);
             if (newContext != null)
                 remoteEntities.remove(newContext.getTracksId());
             else
                 newContext = createEntityInTracks(localEntity);
 
             if (newContext != null) {
                 saveLocalEntity(createMergedLocalEntity(localEntity, newContext));
 
             }
             return;
         }
         Entity remoteEntity = remoteEntities.get(localEntity.getTracksId());
         if (remoteEntity != null) {
             handleRemoteEntity(localEntity, remoteEntity);
             remoteEntities.remove(remoteEntity.getTracksId());
         } else {
             removeLocalEntity(localEntity);
         }
     }
 
     private void handleRemoteEntity(Entity localContext, Entity remoteContext) {
 
         if (remoteContext.getModified() == localContext.getModified())
             return;
 
         if (remoteContext.getModified() > localContext
                 .getModified()) {
             saveLocalEntity(createMergedLocalEntity(localContext, remoteContext));
         } else {
             updateTracks(localContext);
         }
 
     }
 
     private void updateTracks(Entity localContext) {
         String document = createDocumentForEntity(localContext);
         try {
             client.putContentToUrl(createEntityUrl(localContext), document);
         } catch (WebClient.ApiException ignored) {
             Log.w(cTag, ignored);
         }
     }
 
     private Entity createEntityInTracks(Entity entity) {
         String document = createDocumentForEntity(entity);
         try {
             String location = client.postContentToUrl(entityIndexUrl(),
                     document);
             if (!TextUtils.isEmpty(location.trim())) {
                 Long id = parseIdFromLocation(location);
                 entity.setTracksId(id);
             }
         } catch (WebClient.ApiException ignored) {
             Log.w(cTag, ignored);
         }
         return entity;
     }
 
     private Long parseIdFromLocation(String location) {
         String[] parts = location.split("/");
         String document = parts[parts.length - 1];
         return Long.parseLong( document );
     }
 
 }
