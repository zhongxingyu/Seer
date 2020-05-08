 package net.contextfw.web.commons.cloud.storage;
 
 import java.util.UUID;
 
 import javax.servlet.http.HttpServletRequest;
 
 import net.contextfw.web.application.WebApplication;
 import net.contextfw.web.application.WebApplicationHandle;
 import net.contextfw.web.application.configuration.Configuration;
 import net.contextfw.web.application.configuration.SettableProperty;
 import net.contextfw.web.application.internal.configuration.Property;
 import net.contextfw.web.application.scope.ScopedWebApplicationExecution;
 import net.contextfw.web.application.scope.WebApplicationStorage;
 import net.contextfw.web.commons.cloud.CloudDatabase;
 import net.contextfw.web.commons.cloud.mongo.MongoBase;
 import net.contextfw.web.commons.cloud.mongo.MongoExecution;
 import net.contextfw.web.commons.cloud.serializer.Serializer;
 
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
 
     private static final int INITIAL_TRESHOLD = 100;
 
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
     public static final Property<Double> THROTTLE_CURVE = 
             Configuration.createProperty(Double.class, 
                     MongoWebApplicationStorage.class + "throttleCurve");
     
     private static final String KEY_HANDLE = "handle";
     private static final String KEY_REMOTE_ADDR = "remoteAddr";
     private static final String KEY_VALID_THROUGH = "validThrough";
     private static final String KEY_LOCKED = "locked";
     private static final String KEY_APPLICATION = "application";
 
     private final boolean throttle;
     private final int throttleTreshold;
     private final boolean logThrottle;
     private final double throttleCurve;
     private final String collection;
 
     @Inject
     private Serializer serializer;
     
     @Inject
     public MongoWebApplicationStorage(@CloudDatabase DB db, 
                                       Configuration configuration) {
         
         super(db, configuration.get(Configuration.REMOVAL_SCHEDULE_PERIOD));
         throttle = configuration.getOrElse(THROTTLE, false);
         throttleTreshold = configuration.getOrElse(THROTTLE_TRESHOLD, INITIAL_TRESHOLD);
         logThrottle = configuration.getOrElse(THROTTLE_LOG, false);
         throttleCurve = configuration.getOrElse(THROTTLE_CURVE, INITIAL_CURVE);
         collection = configuration.getOrElse(COLLECTION_NAME, "pages");
     }
 
     private void throttle(String remoteAddr) {
         if (throttle) {
             long count = getPages().count(o(KEY_REMOTE_ADDR, remoteAddr));
             if (count > throttleTreshold) {
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
     
     private long getPageCount() {
         return getPages().count();
     }
     
     private void create(WebApplicationHandle handle, 
                             String remoteAddr, 
                             WebApplication application, 
                             long validThrough) {
         application.setHandle(handle);
         
         BasicDBObject doc = new BasicDBObject();
         
         doc.put(KEY_HANDLE, handle.toString());
         doc.put(KEY_REMOTE_ADDR, remoteAddr);
         doc.put(KEY_VALID_THROUGH, validThrough);
         doc.put(KEY_LOCKED, false);
         
         getPages().insert(doc);
     }
 
     private void update(WebApplicationHandle handle, 
                         WebApplication application,
                         Long validThrough) { 
 
         DBObject query = o(KEY_HANDLE, handle.toString());
         BasicDBObjectBuilder updateBuilder = b();
         
         if (validThrough != null) {
             updateBuilder.add(KEY_VALID_THROUGH, validThrough);
         }
         updateBuilder.add(KEY_APPLICATION, serializer.serialize(application));
         getPages().update(query, o("$set", updateBuilder.get()));
     }
         
     private WebApplication load(DBObject obj) {
         if (obj != null) {
             return (WebApplication) serializer.unserialize((byte[]) obj.get("application"));
         } else {
             return null;
         }
     }
     
     private DBCollection getPages() {
         return getDb().getCollection(collection);
     }
     
     private void removeExpiredPages() {
         removeExpiredObjects(getPages());
     }
 
     private WebApplicationHandle createHandle() {
         return new WebApplicationHandle(UUID.randomUUID().toString());
     }
 
     @Override
    public void initialize(WebApplication application, 
                            HttpServletRequest request,
                            long validThrough,
                            final ScopedWebApplicationExecution execution) {
         String remoteAddr = request.getRemoteAddr();
         
         removeExpiredPages();
         throttle(remoteAddr);
         
         final WebApplicationHandle handle = createHandle();
         
         create(handle, remoteAddr, application, validThrough);
         
         executeExclusive(getPages(), handle.toString(), null, new MongoExecution<Void>() {
             public Void execute(DBObject object) {
                WebApplication application = load(object);
                 try {
                     execution.execute(application);
                 } finally {
                     update(handle, application, null);
                 }
                 return null;
             }
         });
     }
 
     @Override
     public void update(final WebApplicationHandle handle, 
                        HttpServletRequest request, 
                        long validThrough,
                        final ScopedWebApplicationExecution execution) {
         
         String remoteAddr = request.getRemoteAddr();
         removeExpiredPages();
         throttle(remoteAddr);
         
         executeExclusive(getPages(), handle.toString(), null, new MongoExecution<Void>() {
             public Void execute(DBObject object) {
                 WebApplication application = load(object);
                 try {
                     execution.execute(application);
                 } finally {
                     update(handle, application, null);
                 }
                 return null;
             }
         });
     }
 
     @Override
     public void execute(final WebApplicationHandle handle,
                         final ScopedWebApplicationExecution execution) {
         
         
         executeExclusive(getPages(), handle.toString(), null, new MongoExecution<Void>() {
             public Void execute(DBObject object) {
                 WebApplication application = load(object);
                 try {
                     execution.execute(application);
                 } finally {
                     update(handle, application, null);
                 }
                 return null;
             }
         });
     }
 
     @Override
     public void refresh(WebApplicationHandle handle, HttpServletRequest request, long validThrough) {
         BasicDBObject query = new BasicDBObject();
         query.put(KEY_HANDLE, handle.toString());
         query.put(KEY_REMOTE_ADDR, request.getRemoteAddr());
        query.put(KEY_VALID_THROUGH, new BasicDBObject("$lte", System.currentTimeMillis()));
        getPages().update(query, new BasicDBObject("$set", 
                new BasicDBObject(KEY_VALID_THROUGH, validThrough)));        
     }
 
     @Override
     public void remove(WebApplicationHandle handle, HttpServletRequest request) {
         BasicDBObject query = new BasicDBObject(KEY_HANDLE, handle.toString());
         query.put(KEY_REMOTE_ADDR, request.getRemoteAddr());
         getPages().remove(query);
     }
 }
