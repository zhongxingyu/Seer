 package com.epam.memegen;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.api.datastore.Query.FilterPredicate;
 import com.google.appengine.api.datastore.Query.SortDirection;
 import com.google.appengine.api.memcache.ConsistentErrorHandler;
 import com.google.appengine.api.memcache.Expiration;
 import com.google.appengine.api.memcache.InvalidValueException;
 import com.google.appengine.api.memcache.MemcacheService;
 import com.google.appengine.api.memcache.MemcacheService.IdentifiableValue;
 import com.google.appengine.api.memcache.MemcacheService.SetPolicy;
 import com.google.appengine.api.memcache.MemcacheServiceException;
 import com.google.appengine.api.memcache.MemcacheServiceFactory;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParseException;
 import com.google.gson.stream.JsonWriter;
 import org.apache.commons.lang3.StringEscapeUtils;
 
 /**
  * Memcache contains JSONs for:
  * <li>Every meme by its long id.
  * <li>All memes by key "ALL".
  * <li>Last meme Date by key "LAST_TS".
  */
 public class MemeDao {
   private static final Logger logger = Logger.getLogger(MemeDao.class.getName());
 
   private static final String KIND = "Meme";
 
   private static final String LAST_TS = "LAST_TS";
   private static final String ALL = "ALL";
 
   private final Key allKey = KeyFactory.createKey(KIND, "ALL");
 
   private final MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
   private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
   Expiration expiration = Expiration.byDeltaSeconds(666); // 11 minutes
 
   public MemeDao() {
     memcache.setErrorHandler(new ConsistentErrorHandler() {
       @Override
       public void handleServiceError(MemcacheServiceException ex) {
         logger.log(Level.WARNING, "MemcacheServiceException", ex);
       }
 
       @Override
       public void handleDeserializationError(InvalidValueException ivx) {
         throw ivx;
       }
     });
   }
 
   public static String getMemcacheKey(HttpServletRequest req) {
     String key = req.getRequestURI() + req.getQueryString();
     return key;
   }
 
   public String getAllAsJson(HttpServletRequest req) throws IOException {
     String sinceS = req.getParameter("since");
     Long since = sinceS == null ? null : Long.parseLong(sinceS);
     String top = req.getParameter("top");
 
     // Lookup memcache
     if (since != null) {
       Long lastTs = (Long) memcache.get(LAST_TS);
       if (lastTs != null && lastTs <= since) {
         // User asked for memes younger than the youngest.
         return "[]";
       }
     } else if (top == null) {
       String json = (String) memcache.get(ALL);
       if (json != null) {
         return json;
       }
     }
 
     Date youngest = null;
     Query q = new Query(KIND, allKey);
     q.addSort("date", SortDirection.DESCENDING);
 
     if (since != null) {
      q.setFilter(new FilterPredicate("date", FilterOperator.GREATER_THAN, since));
     }
 
     FetchOptions options = FetchOptions.Builder.withPrefetchSize(1000);
     int limit = Integer.MAX_VALUE;
     if (top != null) {
       limit = Integer.parseInt(top);
     }
 
     PreparedQuery prepared = datastore.prepare(q);
     Iterable<Entity> iterable = prepared.asIterable(options);
 
     StringWriter out = new StringWriter();
     JsonWriter w = new JsonWriter(out);
     // Remember that embedding json into welcome page needs escaping.
     w.setIndent("  ");
     w.beginArray();
     for (Entity entity : iterable) {
       if (entity.getProperty("deleted") != null) {
         continue;
       }
       if (limit-- <= 0) {
         break;
       }
       Date date = (Date) entity.getProperty("date");
       if (youngest == null || youngest.before(date)) {
         youngest = date;
       }
       toJson(entity, w);
     }
     w.endArray();
     w.close();
     String value = out.toString();
     if (top == null && since == null) {
       memcache.put(ALL, value, expiration);
     }
     if (youngest != null) {
       memcache.put(LAST_TS, youngest.getTime(), expiration);
     }
     return value;
   }
 
 
   public String getAsJson(long id) throws IOException {
     // Lookup memcache
     String json = (String) memcache.get(id);
     if (json != null) {
       return json;
     }
 
     Key key = KeyFactory.createKey(allKey, KIND, id);
     Entity entity;
     try {
       entity = datastore.get(key);
       StringWriter out = new StringWriter(1000);
       JsonWriter w = new JsonWriter(out);
       w.setIndent("  ");
       toJson(entity, w);
       json = out.toString();
     } catch (EntityNotFoundException e) {
       return null;
     }
     memcache.put(id, json);
     return json;
   }
 
   private String toJson(Entity meme) throws IOException {
     StringWriter sw = new StringWriter(1000);
     JsonWriter w = new JsonWriter(sw);
     w.setIndent("  ");
     toJson(meme, w);
     return sw.toString();
   }
 
   private void toJson(Entity meme, JsonWriter w) throws IOException {
     w.beginObject();
 
     long id = meme.getKey().getId();
     w.name("id").value(id);
 
     String fileName = (String) meme.getProperty("fileName");
     String src = "/image/" + id;
     if (fileName != null) {
       fileName = StringEscapeUtils.escapeHtml4(fileName);
       src = src + "/" + fileName;
     }
 
     BlobKey blobKey = (BlobKey) meme.getProperty("blobKey");
     if (blobKey == null) {
       throw new IllegalStateException();
     }
     String escaped = StringEscapeUtils.escapeHtml4(blobKey.getKeyString());
     w.name("blobKey").value(escaped);
     w.name("src").value("/image/meme" + id + "?blobKey=" + escaped);
 
     Date date = (Date) meme.getProperty("date");
     if (date != null) w.name("timestamp").value(date.getTime());
 
     String topText = (String) meme.getProperty("topText");
     String centerText = (String) meme.getProperty("centerText");
     String bottomText = (String) meme.getProperty("bottomText");
     if (topText != null) {
       w.name("top").value(StringEscapeUtils.escapeHtml4(topText));
     }
     if (centerText != null) {
       w.name("center").value(StringEscapeUtils.escapeHtml4(centerText));
     }
     if (bottomText != null) {
       w.name("bottom").value(StringEscapeUtils.escapeHtml4(bottomText));
     }
     w.endObject();
   }
 
   public String create(JsonElement jsonElement) throws IOException {
     String top = null;
     String center = null;
     String bottom = null;
     String blobKey = null;
 
     try {
       JsonObject jsonObject = jsonElement.getAsJsonObject();
       JsonElement topJE = jsonObject.get("top");
       JsonElement centerJE = jsonObject.get("center");
       JsonElement bottomJE = jsonObject.get("bottom");
       JsonElement blobKeyJE = jsonObject.get("blobKey");
       if (topJE != null && topJE.isJsonPrimitive()) {
         top = topJE.getAsString();
       }
       if (centerJE != null && centerJE.isJsonPrimitive()) {
         center = centerJE.getAsString();
       }
       if (bottomJE != null && bottomJE.isJsonPrimitive()) {
         bottom = bottomJE.getAsString();
       }
       if (blobKeyJE != null && blobKeyJE.isJsonPrimitive()) {
         blobKey = blobKeyJE.getAsString();
       }
     } catch (JsonParseException e) {
       throw new IllegalArgumentException(e);
     } catch (ClassCastException e) {
       throw new IllegalArgumentException(e);
     } catch (IllegalStateException e) {
       throw new IllegalArgumentException(e);
     } catch (UnsupportedOperationException e) {
       throw new IOException(e);
     }
 
     if (blobKey == null) {
       throw new IllegalArgumentException("No 'blobKey' param");
     }
 
     return create(blobKey, top, center, bottom);
   }
 
   public String create(String blobKey, String top, String center, String bottom)
       throws IOException {
     DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
     Entity entity = new Entity(KIND, allKey);
     entity.setProperty("blobKey", new BlobKey(blobKey));
     Date justCreatedDate = new Date();
     entity.setProperty("date", justCreatedDate);
     if (!Util.isNullOrEmpty(top)) {
       entity.setProperty("topText", top);
     }
     if (!Util.isNullOrEmpty(center)) {
       entity.setProperty("centerText", center);
     }
     if (!Util.isNullOrEmpty(bottom)) {
       entity.setProperty("bottomText", bottom);
     }
 
     Key key = datastore.put(entity);
 
     // Put to memcache.
     String json = toJson(entity);
     memcache.put(key.getId(), json);
 
     memcache.delete(ALL);
 
     // Set LAST_TS, taking care of for race conditions.
     long timestamp = justCreatedDate.getTime();
     boolean result = false;
     int i = 0;
     while (!result) {
       if (i++ > 50) {
         logger.severe("Infinite loop");
         break;
       }
       IdentifiableValue ident = memcache.getIdentifiable(LAST_TS);
       if (ident != null) {
         Long lastDateInMemcache = (Long) ident.getValue();
         if (lastDateInMemcache != null && lastDateInMemcache >= timestamp) {
           break;
         }
         result = memcache.putIfUntouched(LAST_TS, ident, timestamp);
       } else {
         result = memcache.put(LAST_TS, timestamp, expiration, SetPolicy.ADD_ONLY_IF_NOT_PRESENT);
       }
     }
 
     return json;
   }
 
   public void delete(long id) throws EntityNotFoundException, IOException {
     DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
     Key key = KeyFactory.createKey(allKey, KIND, id);
     Entity entity;
     entity = datastore.get(key);
     entity.setProperty("deleted", true);
     datastore.put(entity);
     memcache.delete(id);
     memcache.delete(LAST_TS);
     memcache.delete(ALL);
   }
 }
