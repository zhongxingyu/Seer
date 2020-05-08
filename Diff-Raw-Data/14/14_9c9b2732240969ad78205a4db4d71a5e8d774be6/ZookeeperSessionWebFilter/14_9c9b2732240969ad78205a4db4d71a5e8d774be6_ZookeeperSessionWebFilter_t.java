 package ru.ttk.hypergate.session.filter;
 
 import com.google.common.io.Closeables;
 import com.netflix.curator.framework.CuratorFramework;
 import com.netflix.curator.framework.CuratorFrameworkFactory;
 import com.netflix.curator.retry.ExponentialBackoffRetry;
 import com.netflix.curator.test.Timing;
 import com.netflix.curator.utils.DebugUtils;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.io.IOException;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 /**
  *
  */
 public class ZookeeperSessionWebFilter implements Filter {
 
     private CuratorFramework curatorClientInstance;
 
     static final String ZOOKEEPER_PREFIX = "ZKP";
 
     private static final ConcurrentMap<String, String> mapOriginalSessions = new ConcurrentHashMap<String, String>(1000);
     private static final ConcurrentMap<String, ZookeeperHttpSession> mapSessions = new ConcurrentHashMap<String, ZookeeperHttpSession>(1000);
 
     private static final String ZOOKEEPER_SESSION_ATTRIBUTE_SEPARATOR = "::" + ZOOKEEPER_PREFIX + "::";
     private static final String ZOOKEEPER_REQUEST = "*zookeeper-request";
     private static final String ZOOKEEPER_SESSION_COOKIE_NAME = "zookeeper.sessionId";
 
     private String sessionCookieName = ZOOKEEPER_SESSION_COOKIE_NAME;
 
     protected ServletContext servletContext;
     private boolean deferredWrite = false;
 
     private String sessionCookieDomain = null;
     private boolean sessionCookieHttpOnly = false;
     private boolean sessionCookieSecure = false;
     private String zooKeeperMapPath = "/zookeeper.map.default";
 
     private static final LocalEntry NULL_ENTRY = new LocalEntry();
 
     private static ZooKeeperHashMap zooKeeperHashMap;
 
     private Properties properties;
     protected FilterConfig filterConfig;
 
 
     public ZookeeperSessionWebFilter() {
     }
 
     public ZookeeperSessionWebFilter(Properties properties) {
         this();
         this.properties = properties;
     }
 
     @Override
     public void init(FilterConfig filterConfig) throws ServletException {
         this.filterConfig = filterConfig;
         this.servletContext = filterConfig.getServletContext();
 
         if (properties == null) {
             properties = new Properties();
         }
 
         String connectionString = null;
         String zooKeeperHost = getParam("zookeeper-host");
         if (zooKeeperHost != null) {
             connectionString = zooKeeperHost;
         }
 
         zooKeeperMapPath = getParam("zookeeper-map-path");
         if (zooKeeperMapPath == null) {
             zooKeeperMapPath = "/baloo.session.map.EMPTY";
         }
 
         deferredWrite = false;
 

        String cookieDomain = getParam("cookie-domain");
        if (cookieDomain != null) {
            sessionCookieDomain = cookieDomain;
        }




         System.setProperty(DebugUtils.PROPERTY_DONT_LOG_CONNECTION_ISSUES, "true");
         // Zookeeper Curator
         ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3); // new RetryOneTime(1)
 
         int connectionTimeoutMs = new Timing().connection();
         int sessionTimeoutMs = new Timing().session();
 
 
         if(connectionString == null){
             String host = "localhost";
             int port = 2181;
             connectionString = host + ":" + port;
         }
 
         Timing timing = new Timing();
         System.out.println("timing.session() - " + timing.session());
         System.out.println("timing.connection() - " + timing.connection());
 
         curatorClientInstance = CuratorFrameworkFactory.builder()
                 .connectString(connectionString)
                 .retryPolicy(retryPolicy)
                 .connectionTimeoutMs(connectionTimeoutMs)
                 .sessionTimeoutMs(sessionTimeoutMs)
                 .build();
 
         curatorClientInstance.start();
 
         zooKeeperHashMap = new ZooKeeperHashMap(curatorClientInstance, zooKeeperMapPath);
 
     }
 
     private String getSessionCookie(final RequestWrapper requestWrapper) {
         final Cookie[] cookies = requestWrapper.getCookies();
         if (cookies != null) {
             for (final Cookie cookie : cookies) {
                 final String name = cookie.getName();
                 final String value = cookie.getValue();
                 if (name.equalsIgnoreCase(sessionCookieName)) {
                     return value;
                 }
             }
         }
         return null;
     }
 
     @Override
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
         if (!(request instanceof HttpServletRequest)) {
             chain.doFilter(request, response);
         } else {
 
             if (request instanceof RequestWrapper) {
                 System.out.println("Request is instance of RequestWrapper! Continue...");
                 chain.doFilter(request, response);
                 return;
             }
 
             HttpServletRequest httpRequest = (HttpServletRequest) request;
             RequestWrapper existingRequest = (RequestWrapper) request.getAttribute(ZOOKEEPER_REQUEST);
 
             final ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) response);
             final RequestWrapper requestWrapper = new RequestWrapper(httpRequest, responseWrapper);
 
             if (existingRequest != null) {
                 requestWrapper.setZookeeperSession(existingRequest.zookeeperSession, existingRequest.requestedSessionId);
             }
             chain.doFilter(requestWrapper, responseWrapper);
 
             if (existingRequest != null) return;
 
             ZookeeperHttpSession session = requestWrapper.getSession(false);
             if (session != null && session.isValid()) {
                 if (session.sessionChanged() || !deferredWrite) {
                     System.out.println("PUTTING SESSION " + session.getId());
                     session.sessionDeferredWrite();
                 }
             }
         }
     }
 
     private ZookeeperHttpSession createNewSession(RequestWrapper requestWrapper, String existingSessionId) {
 
         String id = existingSessionId != null ? existingSessionId : generateSessionId();
 
         if (requestWrapper.getOriginalSession(false) != null) {
             System.out.println("Original session exists!!!");
         }
 
         HttpSession originalSession = requestWrapper.getOriginalSession(true);
 
         // private ZookeeperHttpSession(String id, HttpSession originalSession, ZookeeperSessionWebFilter zookeeperSessionWebFilter, boolean deferredWrite) {
         ZookeeperHttpSession zookeeperSession = new ZookeeperHttpSession(id, originalSession, ZookeeperSessionWebFilter.this, deferredWrite);
         mapSessions.put(zookeeperSession.getId(), zookeeperSession);
 
         String oldZookeeperSessionId = mapOriginalSessions.put(originalSession.getId(), zookeeperSession.getId());
         if (oldZookeeperSessionId != null) {
             System.out.println("!!! Overriding an existing zookeeper SessionId " + oldZookeeperSessionId);
         }
 
         System.out.println("Created new session with id: " + id);
         System.out.println(mapSessions.size() + " is sessions.size and originalSessions.size: " + mapOriginalSessions.size());
 
         addSessionCookie(requestWrapper, id);
         if (deferredWrite) {
             loadZooKeeperSession(zookeeperSession);
         }
 
         return zookeeperSession;
     }
 
     private String extractAttributeKey(String key) {
         return key.substring(key.indexOf(ZOOKEEPER_SESSION_ATTRIBUTE_SEPARATOR) + ZOOKEEPER_SESSION_ATTRIBUTE_SEPARATOR.length());
     }
 
     private void loadZooKeeperSession(ZookeeperHttpSession zooKeeperSession) {
         Set<Map.Entry<String, Object>> entrySet = getZooKeeperMap().entrySet(zooKeeperSession.getId());
 
         Map<String, LocalEntry> cache = zooKeeperSession.localCache;
 
         for (Map.Entry<String, Object> entry : entrySet) {
 
             String attributeKey = extractAttributeKey(entry.getKey());
             LocalEntry cacheEntry = cache.get(attributeKey);
             if (cacheEntry == null) {
                 cacheEntry = new LocalEntry();
                 cache.put(attributeKey, cacheEntry);
             }
 
             System.out.println("Storing " + attributeKey + " on session " + zooKeeperSession.getId());
 
             cacheEntry.value = entry.getValue();
             cacheEntry.dirty = false;
         }
     }
 
     public static void destroyOriginalSession(HttpSession originalSession) {
         String zookeeperSessionId = mapOriginalSessions.remove(originalSession.getId());
         if (zookeeperSessionId != null) {
             ZookeeperHttpSession zookeeperSession = mapSessions.remove(zookeeperSessionId);
             if (zookeeperSession != null) {
                 zookeeperSession.zookeeperSessionWebFilter.destroySession(zookeeperSession, false);
             }
         }
     }
 
     @Override
     public void destroy() {
         if (this.curatorClientInstance != null) {
             Closeables.closeQuietly(this.curatorClientInstance);
         }
     }
 
     private IZooKeeperMap getZooKeeperMap() {
         return zooKeeperHashMap;
     }
 
 
     private void destroySession(ZookeeperHttpSession session, boolean removeGlobalSession) {
         System.out.println("Destroying local session: " + session.getId());
 
         mapSessions.remove(session.getId());
         mapOriginalSessions.remove(session.originalSession.getId());
         session.destroy();
 
         // #GLOBAL
         if (removeGlobalSession) {
             System.out.println("Destroying cluster session: " + session.getId() + " => Ignore-timeout: true");
             IZooKeeperMap zooKeeperMap = this.getZooKeeperMap();
             zooKeeperMap.delete(session.getId());
         }
     }
 
     private void prepareReloadingSession(ZookeeperHttpSession zookeeperHttpSession) {
         if (deferredWrite && zookeeperHttpSession != null) {
             Map<String, LocalEntry> cache = zookeeperHttpSession.localCache;
             for (LocalEntry cacheEntry : cache.values()) {
                 cacheEntry.reload = true;
             }
         }
     }
 
 
     private class ResponseWrapper extends HttpServletResponseWrapper {
         public ResponseWrapper(final HttpServletResponse original) {
             super(original);
         }
     }
 
 
     private ZookeeperHttpSession getSessionWithId(final String sessionId) {
         ZookeeperHttpSession session = mapSessions.get(sessionId);
         if (session != null && !session.isValid()) {
             destroySession(session, true);
             session = null;
         }
         return session;
     }
 
 
     private class RequestWrapper extends HttpServletRequestWrapper {
         ZookeeperHttpSession zookeeperSession = null;
 
         final ResponseWrapper response;
 
         String requestedSessionId;
 
         public RequestWrapper(final HttpServletRequest request,
                               final ResponseWrapper response) {
             super(request);
             this.response = response;
             request.setAttribute(ZOOKEEPER_REQUEST, this);
         }
 
         public void setZookeeperSession(ZookeeperHttpSession zookeeperSession, String requestedSessionId) {
             this.zookeeperSession = zookeeperSession;
             this.requestedSessionId = requestedSessionId;
         }
 
         HttpSession getOriginalSession(boolean create) {
             return super.getSession(create);
         }
 
         @Override
         public RequestDispatcher getRequestDispatcher(final String path) {
             final ServletRequest original = getRequest();
             return new RequestDispatcher() {
                 public void forward(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
                     original.getRequestDispatcher(path).forward(RequestWrapper.this, servletResponse);
                 }
 
                 public void include(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
                     original.getRequestDispatcher(path).include(RequestWrapper.this, servletResponse);
                 }
             };
         }
 
         public String fetchZookeeperSessionId() {
             if (requestedSessionId != null) {
                 return requestedSessionId;
             }
             requestedSessionId = getSessionCookie(this);
             if (requestedSessionId != null) {
                 return requestedSessionId;
             }
             requestedSessionId = getParameter(ZOOKEEPER_SESSION_COOKIE_NAME);
             return requestedSessionId;
         }
 
         @Override
         public HttpSession getSession() {
             return getSession(true);
         }
 
         @Override
         public ZookeeperHttpSession getSession(final boolean create) {
             if (zookeeperSession != null && !zookeeperSession.isValid()) {
                 System.out.println("Session is invalid!");
                 destroySession(zookeeperSession, true);
                 zookeeperSession = null;
             }
             if (zookeeperSession == null) {
                 HttpSession originalSession = getOriginalSession(false);
                 if (originalSession != null) {
                     String zooKeeperSessionId = mapOriginalSessions.get(originalSession.getId());
                     if (zooKeeperSessionId != null) {
                         zookeeperSession = mapSessions.get(zooKeeperSessionId);
                     }
                     if (zookeeperSession == null) {
                         mapOriginalSessions.remove(originalSession.getId());
                         originalSession.invalidate();
                     }
                 }
             }
 
             if (zookeeperSession != null)
                 return zookeeperSession;
 
             final String requestedSessionId = this.fetchZookeeperSessionId();
             if (requestedSessionId != null) {
                 zookeeperSession = getSessionWithId(requestedSessionId);
                 if (zookeeperSession == null) {
                     final Boolean existing = (Boolean) getZooKeeperMap().get(requestedSessionId);
                     if (existing != null && existing) {
                         // we already have the session in the cluster loading it...
                         zookeeperSession = createNewSession(RequestWrapper.this, requestedSessionId);
                     }
                 }
             }
 
             if (zookeeperSession == null && create) {
                 zookeeperSession = createNewSession(RequestWrapper.this, null);
             }
 
             // #GLOBAL
             if (deferredWrite) {
                 prepareReloadingSession(zookeeperSession);
             }
 
             return zookeeperSession;
         }
     } // END of RequestWrapper
 
 
 
 
     private static class LocalEntry {
         private Object value = null;
         volatile boolean dirty = false;
         volatile boolean reload = false;
         boolean removed = false;
 
 
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (!(o instanceof LocalEntry)) return false;
 
             LocalEntry that = (LocalEntry) o;
 
             if (dirty != that.dirty) return false;
             if (reload != that.reload) return false;
             if (removed != that.removed) return false;
             if (value != null ? !value.equals(that.value) : that.value != null) return false;
 
             return true;
         }
 
         @Override
         public int hashCode() {
             int result = value != null ? value.hashCode() : 0;
             result = 31 * result + (dirty ? 1 : 0);
             result = 31 * result + (reload ? 1 : 0);
             result = 31 * result + (removed ? 1 : 0);
             return result;
         }
     }
 
     private class ZookeeperHttpSession implements HttpSession {
 
         private final Map<String, LocalEntry> localCache;
 
         private final boolean deferredWrite;
         volatile boolean valid = true;
         final String id;
         final HttpSession originalSession;
         final ZookeeperSessionWebFilter zookeeperSessionWebFilter;
 
         private ZookeeperHttpSession(String id, HttpSession originalSession, ZookeeperSessionWebFilter zookeeperSessionWebFilter, boolean deferredWrite) {
             this.id = id;
             this.originalSession = originalSession;
             this.zookeeperSessionWebFilter = zookeeperSessionWebFilter;
             this.localCache = new ConcurrentHashMap<String, LocalEntry>();
             this.deferredWrite = deferredWrite;
         }
 
         void destroy() {
             valid = false;
         }
 
         public boolean isValid() {
             return this.valid;
         }
 
         public boolean sessionChanged() {
             if (!deferredWrite) {
                 return false;
             }
             for (Map.Entry<String, LocalEntry> entry : localCache.entrySet()) {
                 if (entry.getValue().dirty) {
                     return true;
                 }
             }
             return false;
         }
 
         @Override
         public long getCreationTime() {
             return this.originalSession.getCreationTime();
         }
 
         @Override
         public String getId() {
             return this.id;
         }
 
         @Override
         public long getLastAccessedTime() {
             return this.originalSession.getLastAccessedTime();
         }
 
         @Override
         public ServletContext getServletContext() {
             return servletContext;
         }
 
         @Override
         public void setMaxInactiveInterval(int interval) {
             this.originalSession.setMaxInactiveInterval(interval);
         }
 
         @Override
         public int getMaxInactiveInterval() {
             return this.originalSession.getMaxInactiveInterval();
         }
 
         @Override
         public HttpSessionContext getSessionContext() {
             return originalSession.getSessionContext();
         }
 
         private String buildAttributeName(String name) {
             String attributeName = id + ZOOKEEPER_SESSION_ATTRIBUTE_SEPARATOR + name;
             System.out.println("buildAttributeName:" + attributeName);
             return attributeName;
         }
 
         @Override
         public Object getAttribute(String name) {
             IZooKeeperMap zooKeeperMap = getZooKeeperMap();
 
             // #GLOBAL
             if (deferredWrite) {
                 LocalEntry cacheEntry = localCache.get(name);
                 if (cacheEntry == null || cacheEntry.reload) {
                     // #GLOBAL
                     Object value = zooKeeperMap.get(buildAttributeName(name));
                     if (value == null) {
                         //cacheEntry = NULL_ENTRY;
                         cacheEntry = new LocalEntry();
                     } else {
                         cacheEntry = new LocalEntry();
                         cacheEntry.value = value;
                         cacheEntry.reload = false;
                     }
 //                    localCache.put(name, NULL_ENTRY);
                     localCache.put(name, cacheEntry);
                 }
                 //return cacheEntry != NULL_ENTRY ? cacheEntry.value : null;
                 return !cacheEntry.equals(NULL_ENTRY) ? cacheEntry.value : null;
             }
             return zooKeeperMap.get(buildAttributeName(name));
         }
 
         @Override
         public Object getValue(String name) {
             return this.getAttribute(name);
         }
 
         @Override
         public Enumeration<String> getAttributeNames() {
             final Set<String> keys = selectKeys();
             return new Enumeration<String>() {
                 private final String[] elements = keys.toArray(new String[keys.size()]);
                 private int index = 0;
 
                 @Override
                 public boolean hasMoreElements() {
                     return index < elements.length;
                 }
 
                 @Override
                 public String nextElement() {
                     return elements[index++];
                 }
             };
         }
 
         @Override
         public String[] getValueNames() {
             final Set<String> keys = selectKeys();
             return keys.toArray(new String[keys.size()]);
         }
 
         @Override
         public void setAttribute(String name, Object value) {
             if (name == null) {
                 throw new NullPointerException("name must not be null");
             }
             if (value == null) {
                 //throw new IllegalArgumentException("value must not be null");
                 System.out.println("\n\n\n\n\n" +
                         " value must not be null for NAME:" + name +
                         "\n\n\n\n\n");
 //                throw new IllegalArgumentException("value must not be null");
             }
             if (deferredWrite) {
                 LocalEntry entry = localCache.get(name);
                 if (entry == null) {
                     entry = new LocalEntry();
                     localCache.put(name, entry);
                 }
                 entry.value = value;
                 entry.dirty = true;
             } else {
                 getZooKeeperMap().put(buildAttributeName(name), value);
             }
         }
 
         @Override
         public void putValue(String name, Object value) {
             setAttribute(name, value);
         }
 
         @Override
         public void removeAttribute(String name) {
 //            if (deferredWrite) {
             LocalEntry entry = localCache.get(name);
             if (entry != null) {
                 entry.value = null;
                 entry.removed = true;
                 // dirty needs to be set as last value for memory visibility reasons!
                 entry.dirty = true;
             }
 //            } else {
 //                getClusterMap().delete(buildAttributeName(name));
 //            }
         }
 
         @Override
         public void removeValue(String name) {
             removeAttribute(name);
         }
 
         @Override
         public void invalidate() {
             this.originalSession.invalidate();
         }
 
         @Override
         public boolean isNew() {
             return originalSession.isNew();
         }
 
         private Set<String> selectKeys() {
 //            if (!deferredWrite) {
 //                return getClusterMap().keySet(new SessionAttributePredicate(id));
 //            }
             Set<String> keys = new HashSet<String>();
             Iterator<Map.Entry<String, LocalEntry>> iterator = localCache.entrySet().iterator();
             while (iterator.hasNext()) {
                 Map.Entry<String, LocalEntry> entry = iterator.next();
                 if (!entry.getValue().removed) {
                     keys.add(entry.getKey());
                 }
             }
             return keys;
         }
 
         private void sessionDeferredWrite() {
             IZooKeeperMap zooKeeperMap = getZooKeeperMap();
 
             if (deferredWrite) {
 
                 Iterator<Map.Entry<String, LocalEntry>> iterator = localCache.entrySet().iterator();
                 while (iterator.hasNext()) {
 
                     Map.Entry<String, LocalEntry> entry = iterator.next();
                     if (entry.getValue().dirty) {
 
                         LocalEntry cacheEntry = entry.getValue();
                         if (cacheEntry.removed) {
                             zooKeeperMap.delete(buildAttributeName(entry.getKey()));
                             iterator.remove();
                         } else {
                             zooKeeperMap.put(buildAttributeName(entry.getKey()), cacheEntry.value);
                             cacheEntry.dirty = false;
                         }
                     }
 
                 }
 
             }
 
             if (!zooKeeperMap.containsKey(id)) {
                 zooKeeperMap.put(id, Boolean.TRUE);
             }
         }
 
     } // ZOOKEEPER SESSION
 
 
     private static synchronized String generateSessionId() {
         String id = UUID.randomUUID().toString();
 
         StringBuilder sb = new StringBuilder(ZOOKEEPER_PREFIX);
 
         char[] chars = id.toCharArray();
 
         for (char c : chars) {
             if (c != '-') {
                 if (Character.isLetter(c)) {
                     sb.append(Character.toUpperCase(c));
                 } else
                     sb.append(c);
             }
         }
         return sb.toString();
     }
 
     private void addSessionCookie(final RequestWrapper requestWrapper, final String sessionId) {
         final Cookie sessionCookie = new Cookie(sessionCookieName, sessionId);
         String path = requestWrapper.getContextPath();
         if ("".equals(path)) {
             path = "/";
         }

        // FORCE TO USE FOR ALL APPLICATIONS
        path = "/";

         sessionCookie.setPath(path);
         sessionCookie.setMaxAge(-1);
         if (sessionCookieDomain != null) {
             sessionCookie.setDomain(sessionCookieDomain);
         }
         try {
             sessionCookie.setHttpOnly(sessionCookieHttpOnly);
         } catch (NoSuchMethodError e) {
         }
         sessionCookie.setSecure(sessionCookieSecure);
 
         // Actually a strange assignment
         requestWrapper.response.addCookie(sessionCookie);
     }
 
 
 
     private String getParam(String name) {
         if (properties != null && properties.containsKey(name)) {
             return properties.getProperty(name);
         } else {
             return filterConfig.getInitParameter(name);
         }
     }
 }
