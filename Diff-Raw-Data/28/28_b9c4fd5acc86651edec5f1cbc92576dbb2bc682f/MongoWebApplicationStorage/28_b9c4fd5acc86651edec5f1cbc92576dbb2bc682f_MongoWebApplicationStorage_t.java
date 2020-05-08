 package net.contextfw.web.commons.cloud.storage;
 
 import java.util.UUID;
 
 import javax.servlet.http.HttpServletRequest;
 
import net.contextfw.web.application.PageHandle;
 import net.contextfw.web.application.WebApplication;
 import net.contextfw.web.application.WebApplicationException;
 import net.contextfw.web.application.configuration.Configuration;
 import net.contextfw.web.application.configuration.SettableProperty;
 import net.contextfw.web.application.scope.ScopedWebApplicationExecution;
 import net.contextfw.web.application.scope.WebApplicationStorage;
 import net.contextfw.web.commons.cloud.binding.CloudDatabase;
 import net.contextfw.web.commons.cloud.internal.mongo.ExceptionSafeExecution;
 import net.contextfw.web.commons.cloud.internal.mongo.MongoBase;
 import net.contextfw.web.commons.cloud.internal.serializer.Serializer;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.mongodb.BasicDBObject;
 import com.mongodb.BasicDBObjectBuilder;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 
 @Singleton
 public class MongoWebApplicationStorage extends MongoBase implements WebApplicationStorage {
 
     private static final double INITIAL_CURVE = 1.3;
     
     private static final int TRY_OUTS = 100;
 
     private static final int INITIAL_TRESHOLD = 100;
     
     private static final int SLEEP_PERIOD = 100;
     
     private static final Logger LOG = LoggerFactory
             .getLogger(MongoWebApplicationStorage.class);
     
     /**
      * Collection to hold pages
      */
     public static final SettableProperty<String> COLLECTION_NAME = 
             Configuration.createProperty(String.class, 
                     MongoWebApplicationStorage.class + ".collection");
     
     /**
      * Informs whether throttling should be used
      */
     public static final SettableProperty<Boolean> THROTTLE = 
             Configuration.createProperty(Boolean.class, 
                     MongoWebApplicationStorage.class + "throttle");
     
     /**
      * Informs how many concurrent page scopes must exists for certain IP-address
      * before throttling is used.
      */
     public static final SettableProperty<Integer> THROTTLE_TRESHOLD = 
             Configuration.createProperty(Integer.class, 
                     MongoWebApplicationStorage.class + "throttleTreshold");
     
     /**
      * Informs whether throttling should be logged
      */
     public static final SettableProperty<Boolean> THROTTLE_LOG = 
             Configuration.createProperty(Boolean.class, 
                     MongoWebApplicationStorage.class + "throttleLog");
     
     /**
      * Informs how fast the throttling will increase when page scopes increases. 
      * 
      * <p>The algorithm is following:</p>
      * <pre>
      *   Math.pow(getPageCount(), THROTTLE_CURVE);
      * </pre>
      */
     public static final SettableProperty<Double> THROTTLE_CURVE = 
             Configuration.createProperty(Double.class, 
                     MongoWebApplicationStorage.class + "throttleCurve");
     
     private static final String KEY_HANDLE = "handle";
     private static final String KEY_REMOTE_ADDR = "remoteAddr";
     private static final String KEY_VALID_THROUGH = "validThrough";
     private static final String KEY_LOCKED = "locked";
     private static final String KEY_APPLICATION = "application";
 
     private static final DBObject APPLICATION_FIELDS = new BasicDBObject(KEY_APPLICATION, 1);
     
     private final boolean throttle;
     private final int throttleTreshold;
     private final boolean logThrottle;
     private final double throttleCurve;
     private final String collection;
 
     private final Serializer serializer;
     
     @Inject
     public MongoWebApplicationStorage(@CloudDatabase DB db, 
                                       Configuration configuration,
                                       Serializer serializer) {
         
         super(db, configuration.get(Configuration.REMOVAL_SCHEDULE_PERIOD));
         throttle = configuration.getOrElse(THROTTLE, false);
         throttleTreshold = configuration.getOrElse(THROTTLE_TRESHOLD, INITIAL_TRESHOLD);
         logThrottle = configuration.getOrElse(THROTTLE_LOG, false);
         throttleCurve = configuration.getOrElse(THROTTLE_CURVE, INITIAL_CURVE);
         collection = configuration.getOrElse(COLLECTION_NAME, "pages");
         this.serializer = serializer;
         setIndexes(getCollection());
     }
     
     @edu.umd.cs.findbugs.annotations.SuppressWarnings(
             value="SWL_SLEEP_WITH_LOCK_HELD", 
             justification="Throttle is meant to be slow")
     private void throttle(String remoteAddr) {
         if (throttle) {
             long count = getCollection().count(o(KEY_REMOTE_ADDR, remoteAddr));
             if (count > throttleTreshold) {
                 synchronized (this) {
                     try {
                         long sleep = (long) Math.pow(getPageCount(), throttleCurve);
                         if (logThrottle) {
                             LOG.info("Throttling {} for {} ms", remoteAddr, sleep);
                         }
                         
                         Thread.sleep(sleep);
                     } catch (InterruptedException e) {
                     }    
                 }
             }
         }
     }
     
     private long getPageCount() {
         return getCollection().count();
     }
     
     private void create(PageHandle handle, 
                         String remoteAddr, 
                         WebApplication application, 
                         long validThrough) {
         
         application.setHandle(handle);
         
         BasicDBObject doc = new BasicDBObject();
         
         doc.put(KEY_HANDLE, handle.toString());
         doc.put(KEY_REMOTE_ADDR, remoteAddr);
         doc.put(KEY_VALID_THROUGH, validThrough);
         doc.put(KEY_LOCKED, true);
         
         getCollection().insert(doc);
     }
 
     private WebApplication load(DBObject obj) {
         if (obj != null) {
             return (WebApplication) serializer.unserialize((byte[]) obj.get(KEY_APPLICATION));
         } else {
             return null;
         }
     }
     
     @Override
     protected DBCollection getCollection() {
         return getDb().getCollection(collection);
     }
     
     private void removeExpiredPages() {
         removeExpiredObjects();
     }
 
     private PageHandle createHandle() {
         return new PageHandle(UUID.randomUUID().toString());
     }
 
     @Override
     public void initialize(final WebApplication application, 
                            HttpServletRequest request,
                            long validThrough,
                            final ScopedWebApplicationExecution execution) {
         String remoteAddr = request.getRemoteAddr();
         
         removeExpiredPages();
         throttle(remoteAddr);
         
         final PageHandle handle = createHandle();
         
         create(handle, remoteAddr, application, validThrough);
         
         executeExclusive(handle.toString(), remoteAddr, validThrough,  application, execution);
     }
 
     @Override
     public void update(final PageHandle handle, 
                        HttpServletRequest request, 
                        long validThrough,
                        final ScopedWebApplicationExecution execution) {
         String remoteAddr = request.getRemoteAddr();
         removeExpiredPages();
         throttle(remoteAddr);
         
         executeExclusive(handle.toString(), remoteAddr, validThrough, null, execution);
     }
 
     @Override
     public void execute(final PageHandle handle,
                         final ScopedWebApplicationExecution execution) {
         
         executeExclusive(handle.toString(), null, null, null, execution);
     }
 
     @Override
     public void refresh(PageHandle handle, HttpServletRequest request, long validThrough) {
         
         DBObject query = b()
                 .add(KEY_HANDLE, handle.toString())
                 .add(KEY_REMOTE_ADDR, request.getRemoteAddr())
                 .add(KEY_VALID_THROUGH, o("$gte", System.currentTimeMillis())).get();
         
         getCollection().update(query, o("$set", o(KEY_VALID_THROUGH, validThrough)));        
     }
 
     @Override
     public void remove(PageHandle handle, HttpServletRequest request) {
         
         DBObject query = b()
                 .add(KEY_HANDLE, handle.toString())
                 .add(KEY_REMOTE_ADDR, request.getRemoteAddr()).get();
         
         getCollection().remove(query);
     }
     
     private DBObject openExclusive(String handle) {
 
         BasicDBObjectBuilder queryBuilder = b()
                 .add(KEY_HANDLE, handle)
                 .add(KEY_LOCKED, false);
 
         DBObject update = o("$set", o(KEY_LOCKED, true));
         DBObject query = queryBuilder.get();
         
         for (int i = 0; i < TRY_OUTS; i++) {
             DBObject rv = getCollection().findAndModify(
                     query, 
                     APPLICATION_FIELDS, 
                     null,
                     false,
                     update,
                     true,
                     false);
             if (rv == null) {
                 try {
                     Thread.sleep(SLEEP_PERIOD);
                 } catch (InterruptedException e) {
                 }
             } else {
                 return rv;
             }
         }
         return getCollection().findOne(
                 o(KEY_HANDLE, handle), 
                 APPLICATION_FIELDS);
     }
     
     private void closeExclusive(final String handle,
                                 final Long newValidThrough,
                                 final WebApplication application) {
         
         executeAsync(new ExceptionSafeExecution() {
             public void execute() throws Exception {
                 
                 DBObject query = o(KEY_HANDLE, handle);
                 
                 BasicDBObjectBuilder updateBuilder = b();
                 updateBuilder.push("$set");
                 updateBuilder.add(KEY_LOCKED, false);
                 
                 if (newValidThrough != null) {
                     updateBuilder.add(KEY_VALID_THROUGH, newValidThrough);
                 }
                 
                 if (application != null) {
                     updateBuilder.add(KEY_APPLICATION, serializer.serialize(application));
                 }
                 updateBuilder.pop();
                 getCollection().update(query, updateBuilder.get());
             }
         });
     }    
     
     private void executeExclusive(String handle,
                                   String remoteAddr,
                                   Long newValidthrough,
                                   WebApplication givenApplication,
                                   final ScopedWebApplicationExecution execution) {
 
         WebApplication application = givenApplication != null ?
                 givenApplication : loadExclusive(handle, remoteAddr);
 
         try {
             execution.execute(application);
         } finally {
             closeExclusive(handle, newValidthrough, application);
         }
     }
     
     private WebApplication loadExclusive(String handle, String remoteAddr) {
         
         DBCollection collection = getCollection();
 
         BasicDBObjectBuilder queryBuilder = b().add(KEY_HANDLE, handle);
 
         if (remoteAddr != null) {
             queryBuilder.add(KEY_REMOTE_ADDR, remoteAddr);
         }
 
         queryBuilder
             .push(KEY_VALID_THROUGH)
             .add("$gte", System.currentTimeMillis())
             .pop();
         
         DBObject query = queryBuilder.get();
 
         boolean exists = collection.count(query) == 1;
         
         if (exists) {
             return load(openExclusive(handle));
         } else {
             return null;
         }
        
     }
 
     @Override
     public void storeLarge(PageHandle handle, String key, Object obj) {
         
         if (handle == null) {
             throw new IllegalArgumentException("Handle cannot be null");
         } else if (StringUtils.isBlank(key)) {
             throw new IllegalArgumentException("Key cannot be null or blank!");
         }
         
         DBObject query = b()
                 .add(KEY_HANDLE, handle.toString())
                 .add(KEY_VALID_THROUGH, o("$gte", System.currentTimeMillis())).get();
         
         DBObject update;
         
         if (obj == null) {
             update = o("$unset", o("large_" + key, 1));
         } else {
             update = o("$set", o("large_" + key, serializer.serialize(obj)));
         }
         
         if (getCollection().update(query, update).getN() != 1) {
             throw new WebApplicationException("Page scope does not exist");
         }
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public <T> T loadLarge(PageHandle handle, String key, Class<T> type) {
         if (handle == null) {
             throw new IllegalArgumentException("Handle cannot be null");
         } else if (StringUtils.isBlank(key)) {
             throw new IllegalArgumentException("Key cannot be null or blank!");
         }
         
         DBObject query = b()
                 .add(KEY_HANDLE, handle.toString())
                 .add(KEY_VALID_THROUGH, o("$gte", System.currentTimeMillis())).get();
         
         
         DBObject field = o("large_" + key, 1);
         DBObject obj = getCollection().findOne(query, field);
         
         if (obj == null) {
             throw new WebApplicationException("Page scope does not exist");
         }
         
         byte[] data = (byte[]) obj.get("large_" + key);
         
         return data == null ? null : (T) serializer.unserialize(data);
     }
 }
