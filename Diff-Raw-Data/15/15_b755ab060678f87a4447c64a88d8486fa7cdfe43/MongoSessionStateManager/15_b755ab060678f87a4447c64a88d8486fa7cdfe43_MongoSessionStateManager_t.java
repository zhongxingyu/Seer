 package jp.troter.servlet.httpsession.spi.impl;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 
 import jp.troter.servlet.httpsession.spi.MongoDBInitializer;
 import jp.troter.servlet.httpsession.spi.SessionStateManager;
 import jp.troter.servlet.httpsession.spi.SessionValueSerializer;
 import jp.troter.servlet.httpsession.state.DefaultSessionState;
 import jp.troter.servlet.httpsession.state.SessionState;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.WriteResult;
 
 public class MongoSessionStateManager extends SessionStateManager {
 
     private static Logger log = LoggerFactory.getLogger(MongoSessionStateManager.class);
 
     private static final String SESSION_COLLECTION_NAME = "httpsession";
 
     private static final String ATTRIBUTES_KEY = "attributes";
 
     private static final String LAST_ACCESSED_TIME_KEY = "last_accessed_time";
 
     protected MongoDBInitializer initializer;
 
     protected SessionValueSerializer serializer;
 
     @Override
     public SessionState loadState(String sessionId) {
         Map<String, Object> attributes = new HashMap<String, Object>();
         long lastAccessedTime = new Date().getTime();
         try {
             DBObject obj = findBySessionId(sessionId);
             if (obj == null) { new DefaultSessionState(); }
 
             lastAccessedTime = ((Long)obj.get(getLastAccessedTimeKey())).longValue();
             if (lastAccessedTime > getTimeoutTime()) { return new DefaultSessionState(); }
 
             attributes.putAll(toDeserializeMap((DBObject)obj.get(getAttributesKey())));
         } catch (RuntimeException e) {
             log.warn("MongoDB exception occurred at find method. session_id=" + sessionId, e);
             removeState(sessionId);
         }
 
         return new DefaultSessionState(attributes, lastAccessedTime, false);
     }
 
     protected Map<String, Object> toDeserializeMap(DBObject rawAttributes) {
         Map<String, Object> attributes = new HashMap<String, Object>();
         for (String name : rawAttributes.keySet()) {
             try {
                 byte[] objectData = (byte[])rawAttributes.get(name);
                 attributes.put(name, getSessionValueSerializer().deserialize(objectData));
             } catch (RuntimeException e) {
                 log.warn("undeserialize object name: " + name, e);
             }
         }
         return attributes;
     }
 
     @Override
     public void saveState(String sessionId, SessionState sessionState) {
         BasicDBObject attributes = new BasicDBObject();
         for (Enumeration<?> e = sessionState.getAttributeNames(); e.hasMoreElements();) {
             String name = (String)e.nextElement();
             Object value = sessionState.getAttribute(name);
             if (value == null) { continue; }
             try {
                 attributes.put(name, getSessionValueSerializer().serialize((Serializable)value));
             } catch (RuntimeException x) {
                 log.warn("unserialize object name: " + name, x);
             }
         }
 
         try {
             insert(sessionId, attributes, sessionState.getCreationTime());
         } catch (RuntimeException e) {
             log.warn("MongoDB exception occurred at insert method. session_id=" + sessionId, e);
         }
     }
 
     @Override
     public void removeState(String sessionId) {
         try {
             remove(sessionId);
         } catch (RuntimeException e) {
             log.warn("MongoDB exception occurred at insert method. session_id=" + sessionId, e);
         }
     }
 
     @Override
     public int getTimeoutSecond() {
         return getMongoDBInitializer().getSessionTimeout();
     }
 
     protected DBObject findBySessionId(String sessionId) {
         BasicDBObject query = new BasicDBObject();
        query.put("session_id", sessionId);
         DBCollection coll = getSessionCollection();
         DBCursor cur = coll.find(query);
         while(cur.hasNext()) {
             return cur.next();
         }
         return null;
     }
 
     protected WriteResult insert(String sessionId, BasicDBObject attributes, long creationTime) {
         BasicDBObject session = new BasicDBObject();
        session.put("session_id", sessionId);
         session.put(getAttributesKey(), attributes);
         session.put(getLastAccessedTimeKey(), creationTime);
         return getSessionCollection().insert(session);
     }
 
     protected WriteResult remove(String sessionId) {
         DBObject obj = findBySessionId(sessionId);
         if (obj == null) { return null; }
         return getSessionCollection().remove(obj);
     }
 
     protected DBCollection getSessionCollection() {
        return getMongoDBInitializer().getDB().getCollection(getSessionCollectionName());
     }
 
     protected String getSessionCollectionName() {
         return SESSION_COLLECTION_NAME;
     }
 
     protected String getAttributesKey() {
         return ATTRIBUTES_KEY;
     }
 
     protected String getLastAccessedTimeKey() {
         return LAST_ACCESSED_TIME_KEY;
     }
 
     protected MongoDBInitializer getMongoDBInitializer() {
         if (initializer == null) {
             initializer = MongoDBInitializer.newInstance();
         }
         return initializer;
     }
 
     protected SessionValueSerializer getSessionValueSerializer() {
         if (serializer == null) {
             serializer = SessionValueSerializer.newInstance();
         }
         return serializer;
     }
 }
