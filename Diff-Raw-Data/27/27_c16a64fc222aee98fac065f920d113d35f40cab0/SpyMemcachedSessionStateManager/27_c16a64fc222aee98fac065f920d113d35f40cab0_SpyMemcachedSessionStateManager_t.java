 package jp.troter.servlet.httpsession.spi.impl;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 
 import jp.troter.servlet.httpsession.spi.SessionStateManager;
 import jp.troter.servlet.httpsession.spi.SpyMemcachedInitializer;
 import jp.troter.servlet.httpsession.state.DefaultSessionState;
 import jp.troter.servlet.httpsession.state.SessionState;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class SpyMemcachedSessionStateManager extends SessionStateManager {
 
     private static Logger log = LoggerFactory.getLogger(SpyMemcachedSessionStateManager.class);
 
     private static final String KEY_PREFIX = "httpsession";
 
     protected SpyMemcachedInitializer initializer;
 
     @Override
     public SessionState loadState(String sessionId) {
         Map<String, Object> attributes = new HashMap<String, Object>();
         long lastAccessedTime = new Date().getTime();
         try {
            Object obj = getInitializer().getMemcachedClient().get(key(sessionId));
             if (obj == null) { return new DefaultSessionState(); }
             Cell cell = (Cell) obj;
             attributes.putAll(cell.getAttributes());
             lastAccessedTime = cell.getLastAccessedTime();
         } catch (RuntimeException e) {
             log.warn("Memcached exception occurred at get method. session_id=" + sessionId, e);
             removeState(sessionId);
         }
 
         return new DefaultSessionState(attributes, lastAccessedTime, false);
     }
 
     @Override
     public void saveState(String sessionId, SessionState sessionState) {
         Map<String, Object> attributes = new HashMap<String, Object>();
         for (Enumeration<?> e = sessionState.getAttributeNames(); e.hasMoreElements();) {
             String name = (String)e.nextElement();
             Object value = sessionState.getAttribute(name);
             if (value == null) { continue; }
             attributes.put(name, value);
         }
 
         Cell cell = new Cell(attributes, sessionState.getCreationTime());
         try {
            getInitializer().getMemcachedClient().set(key(sessionId), getTimeoutSecond(), cell);
         } catch (RuntimeException e) {
             log.warn("Memcached exception occurred at set method. session_id=" + sessionId, e);
         }
     }
 
     @Override
     public void removeState(String sessionId) {
         try {
            getInitializer().getMemcachedClient().delete(key(sessionId));
         } catch (RuntimeException e) {
             log.warn("Memcached exception occurred at delete method. session_id=" + sessionId, e);
         }
     }
 
     @Override
     public int getTimeoutSecond() {
        return getInitializer().getTimeoutSecond();
     }
 
     protected String key(String sessionId) {
         return KEY_PREFIX + "/" + sessionId;
     }
 
    protected SpyMemcachedInitializer getInitializer() {
         if (initializer == null) {
             initializer = SpyMemcachedInitializer.newInstance();
         }
         return initializer;
     }
 
     protected static class Cell implements Serializable {
         private static final long serialVersionUID = 1L;
 
         Map<String, Object> attributes;
         long lastAccessedTime;
 
         public Cell(Map<String, Object> attributes, long lastAccessedTime) {
             this.attributes = attributes;
             this.lastAccessedTime = lastAccessedTime;
         }
 
         public Map<String, Object> getAttributes() {
             return attributes;
         }
 
         public long getLastAccessedTime() {
             return lastAccessedTime;
         }
     }
 }
