 package com.epam.memegen;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.datastore.DatastoreFailureException;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
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
  * <li>Popular memes by key "POPULAR".
  * <li>Top memes by key "TOP".
  * <li>Last meme Date by key "LAST_TS".
  */
 public class MemeDao {
   private static final Logger logger = Logger.getLogger(MemeDao.class.getName());
 
   private static final String KIND = "Meme";
 
   public static final String LAST_TS = "LAST_TS";
   public static final String ALL = "ALL";
   public static final String POPULAR = "POPULAR";
   public static final String TOP = "TOP";
 
   private final Key allKey = KeyFactory.createKey(KIND, "ALL");
 
   private final MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
   private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
   private final Util util = new Util();
 
   private final Expiration expiration = Expiration.byDeltaSeconds(666); // 11 minutes
 
   public MemeDao() {
     memcache.setErrorHandler(new ConsistentErrorHandler() {
       public void handleServiceError(MemcacheServiceException ex) {
         logger.log(Level.WARNING, "MemcacheServiceException", ex);
       }
 
       public void handleDeserializationError(InvalidValueException ivx) {
         throw ivx;
       }
     });
   }
 
   public static String getMemcacheKey(HttpServletRequest req) {
     String key = req.getRequestURI() + req.getQueryString();
     return key;
   }
 
   public String getAllAsJson(HttpServletRequest req, String filter) throws IOException {
     if (!util.isAuthenticated()) {
       return "[]";
     }
     String sinceS = req.getParameter("since");
     Long since;
     try {
       since = sinceS == null ? null : Long.parseLong(sinceS);
     } catch (NumberFormatException e) {
       since = null;
     }
     String limitS = req.getParameter("top");
     Integer limit;
     try {
       limit = limitS == null ? null : Integer.parseInt(limitS);
     } catch (NumberFormatException e) {
       limit = null;
     }
 
     // Lookup memcache
     if (since != null) {
       Long lastTs = (Long) memcache.get(LAST_TS);
       if (lastTs != null && lastTs <= since) {
         // User asked for memes younger than the youngest.
         return "[]";
       }
     } else if (limit == null) {
       String json = null;
       if (filter.equals("all")) {
         json = (String) memcache.get(ALL);
       } else if (filter.equals("popular")) {
         json = (String) memcache.get(POPULAR);
       } else if (filter.equals("top")) {
         json = (String) memcache.get(TOP);
       }
       if (json != null) {
         return json;
       }
     }
 
     Date youngest = null;
     Query q = new Query(KIND, allKey);
     if (filter.equals("all")) {
       q.addSort("date", SortDirection.DESCENDING);
       q.setFilter(FilterOperator.EQUAL.of("deleted", false));
     }
 
     if (filter.equals("popular")) {
       q.addSort("date", SortDirection.DESCENDING);
       q.setFilter(CompositeFilterOperator.and(
           FilterOperator.EQUAL.of("deleted", false),
           FilterOperator.EQUAL.of("isPositive", true)));
     }
 
     if (filter.equals("top")) {
       q.addSort("rating", SortDirection.DESCENDING);
       q.setFilter(FilterOperator.EQUAL.of("deleted", false));
     }
 
     if (since != null) {
       q.setFilter(new FilterPredicate("date", FilterOperator.GREATER_THAN, new Date(since)));
     }
 
     FetchOptions options = FetchOptions.Builder.withPrefetchSize(1000);
     if (limit != null) {
       options.limit(limit);
     }
 
     PreparedQuery prepared = datastore.prepare(q);
     Iterable<Entity> iterable = prepared.asIterable(options);
 
     StringWriter out = new StringWriter();
     JsonWriter w = new JsonWriter(out);
     // Remember that embedding json into welcome page needs escaping.
     w.setIndent("  ");
     w.beginArray();
     for (Entity entity : iterable) {
       Date date = (Date) entity.getProperty("date");
       if (youngest == null || youngest.before(date)) {
         youngest = date;
       }
       toJson(entity, w);
     }
     w.endArray();
     w.close();
     String value = out.toString();
     if (limit == null && since == null) {
       if (filter.equals("all")) {
         memcache.put(ALL, value, expiration);
       } else if (filter.equals("popular")) {
         memcache.put(POPULAR, value, expiration);
       } else if (filter.equals("top")) {
         memcache.put(TOP, value, expiration);
       }
     }
     if (youngest != null) {
       memcache.put(LAST_TS, youngest.getTime(), expiration);
     }
     return value;
   }
 
 
   public String getAsJson(long id) throws IOException {
     if (!util.isAuthenticated()) {
       return "{}";
     }
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
 
     if (meme.hasProperty("rating")) {
      long rating = (Long) meme.getProperty("rating");
       w.name("rating").value(rating);
     }
 
     w.endObject();
   }
 
   public String create(JsonElement jsonElement) throws IOException {
     if (!util.isAuthenticated()) {
       return "{}";
     }
 
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
 
   private String create(String blobKey, String top, String center, String bottom)
       throws IOException {
     DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
     Entity entity = new Entity(KIND, allKey);
     entity.setProperty("deleted", false);
     entity.setProperty("blobKey", new BlobKey(blobKey));
     Date justCreatedDate = new Date();
     entity.setProperty("date", justCreatedDate);
     entity.setProperty("rating", 0);
     entity.setProperty("isPositive", true);
     if (!Util.isNullOrEmpty(top)) {
       entity.setProperty("topText", top);
     }
     if (!Util.isNullOrEmpty(center)) {
       entity.setProperty("centerText", center);
     }
     if (!Util.isNullOrEmpty(bottom)) {
       entity.setProperty("bottomText", bottom);
     }
 
     Key key;
     try {
       key = datastore.put(entity);
     } catch (DatastoreFailureException e) {
       // May be like "the id allocated for a new entity was already in use, please try again".
       // I think retry once is OK.
       key = datastore.put(entity);
     }
 
     // Put to memcache.
     String json = toJson(entity);
     memcache.put(key.getId(), json);
 
     memcache.delete(ALL);
     memcache.delete(POPULAR);
     memcache.delete(TOP);
 
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
     if (!util.isAuthenticated()) {
       return;
     }
 
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
