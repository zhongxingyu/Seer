 package com.tjh.swivel.model;
 
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import vanderbilt.util.Block;
 import vanderbilt.util.Block2;
 import vanderbilt.util.Lists;
 import vanderbilt.util.Maps;
 import vanderbilt.util.PopulatingMap;
 
 import javax.script.ScriptException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 @SuppressWarnings("unchecked")
 public class Configuration {
     public static final String SHUNT_NODE = "^shunt";
     public static final String STUB_NODE = "^stub";
     public static final String STUBS_MAP_KEY = "stubs";
     public static final String SHUNT_MAP_KEY = "shunt";
     public static final UriHandlersPopulator POPULATOR = new UriHandlersPopulator();
 
     public static Logger LOGGER = Logger.getLogger(Configuration.class);
 
     protected final Map<String, Map<String, Object>> uriHandlers = new PopulatingMap<String, Map<String, Object>>(
             new ConcurrentHashMap<String, Map<String, Object>>(), POPULATOR);
 
     //<editor-fold desc="query">
     public RequestHandler findRequestHandler(HttpRequestBase request, String matchedPath) {
         RequestHandler result = null;
         LOGGER.debug("checking for handlers at " + matchedPath);
         if (uriHandlers.containsKey(matchedPath)) {
             Map<String, Object> stringObjectMap = uriHandlers.get(matchedPath);
             result = findStub(stringObjectMap, request);
             if (result == null) {
                 if (stringObjectMap.containsKey(Configuration.SHUNT_NODE)) {
                     result = (RequestHandler) stringObjectMap.get(Configuration.SHUNT_NODE);
                     if (result != null) {
                         LOGGER.debug("found shunt " + result);
                     }
                 }
             } else {
                 LOGGER.debug("found stub " + result);
             }
         }
         return result;
     }
 
     RequestHandler findStub(Map<String, Object> stringObjectMap, final HttpUriRequest request) {
         return Lists.find((Collection<StubRequestHandler>) stringObjectMap.get(Configuration.STUB_NODE),
                 new Block<StubRequestHandler, Boolean>() {
                     @Override
                     public Boolean invoke(StubRequestHandler requestHandler) {
                         if (Level.DEBUG.equals(LOGGER.getEffectiveLevel())) {
                             LOGGER.debug("Checking Stub " + requestHandler);
                         }
                         return requestHandler.matches(request);
                     }
                 });
     }
 
     public Collection<StubRequestHandler> getStubs(String localPath, final List<Integer> stubIds) {
         return Lists.select(getStubRequestHandlers(localPath), new Block<StubRequestHandler, Boolean>() {
             @Override
             public Boolean invoke(StubRequestHandler stubRequestHandler) {
                 return stubIds.isEmpty() || stubIds.contains(stubRequestHandler.getId());
             }
         });
     }
     //</editor-fold>
 
     //<editor-fold desc="modify">
     public void removeStub(URI localUri, final int stubHandlerId) {
         String path = localUri.getPath();
         Map<String, Object> handlerMap = uriHandlers.get(path);
         List<StubRequestHandler> handlers = (List<StubRequestHandler>) handlerMap
                 .get(Configuration.STUB_NODE);
         StubRequestHandler target = Lists.find(handlers, new Block<StubRequestHandler, Boolean>() {
             @Override
             public Boolean invoke(StubRequestHandler stubRequestHandler) {
                 return stubRequestHandler.getId() == stubHandlerId;
             }
         });
         LOGGER.debug(String.format("Removing <%1$s> from path <%2$s>", target, localUri));
         handlers.remove(target);
 
         if (handlers.isEmpty()) {
             clean(path, handlerMap, Configuration.STUB_NODE);
         }
     }
 
     public void setShunt(URI localURI, ShuntRequestHandler requestHandler) {
         LOGGER.debug(String.format("Setting shunt <%1$s> at <%2$s>", requestHandler, localURI));
         uriHandlers.get(localURI.getPath()).put(Configuration.SHUNT_NODE, requestHandler);
     }
 
     public void addStub(URI localUri, StubRequestHandler stubRequestHandler) {
         LOGGER.debug(String.format("Adding stub <%1$s> to <%2$s>", stubRequestHandler, localUri));
         ((List) uriHandlers.get(localUri.getPath())
                 .get(Configuration.STUB_NODE))
                 .add(0, stubRequestHandler);
     }
 
     public void deleteShunt(URI localURI) {
         String path = localURI.getPath();
         clean(path, uriHandlers.get(path), Configuration.SHUNT_NODE);
     }
 
     public void replaceStub(URI localURI, int stubId, StubRequestHandler stubRequestHandler) {
         List<StubRequestHandler> stubRequestHandlers = getStubRequestHandlers(localURI.getPath());
         int stubIndex = -1;
         for (int i = 0; i < stubRequestHandlers.size(); i++) {
             StubRequestHandler requestHandler = stubRequestHandlers.get(i);
             if (requestHandler.getId() == stubId) {
                 stubIndex = i;
                 break;
             }
         }
 
         stubRequestHandlers.set(stubIndex, stubRequestHandler);
     }
 
     public void removePath(URI localUri) { uriHandlers.remove(localUri.toString()); }
 
     public void reset() { uriHandlers.clear(); }
     //</editor-fold>
 
     //<editor-fold desc="marshalling">
     public void load(Map<String, Map<String, Object>> configMap) {
         final Map<String, Map<String, Object>> newConfig = new PopulatingMap<String, Map<String, Object>>(
                 POPULATOR);
         Maps.eachPair(configMap, new Block2<String, Map<String, Object>, Object>() {
             @Override
             public Object invoke(String uri, Map<String, Object> shuntsAndStubs) {
                 Map<String, Object> nodeMap = newConfig.get(uri);
                 loadShunt((Map<String, String>) shuntsAndStubs.get(Configuration.SHUNT_MAP_KEY), nodeMap);
                 loadStubs((List<Map<String, Object>>) shuntsAndStubs.get(Configuration.STUBS_MAP_KEY), nodeMap);
                 return null;
             }
         });
 
         synchronized (this) {
             reset();
             uriHandlers.putAll(newConfig);
         }
     }
 
     public Map<String, Map<String, Object>> toMap() {
         final HashMap<String, Map<String, Object>> result =
                 new HashMap<String, Map<String, Object>>(uriHandlers.size());
         Maps.eachPair(uriHandlers, new Block2<String, Map<String, Object>, Object>() {
             @Override
             public Object invoke(String path, Map<String, Object> handlerMap) {
                 Map<String, Object> stubsAndShunt = new HashMap<String, Object>();
                if (handlerMap.containsKey(STUB_NODE)) {
                     stubsAndShunt.put(STUBS_MAP_KEY, Lists.collect((List<StubRequestHandler>) handlerMap.get(STUB_NODE),
                             new Block<StubRequestHandler, Map<String, Object>>() {
                                 @Override
                                 public Map<String, Object> invoke(StubRequestHandler stubRequestHandler) {
                                     return stubRequestHandler.toMap();
                                 }
                             }));
                 }
                 if (handlerMap.containsKey(SHUNT_NODE)) {
                     stubsAndShunt.put(SHUNT_MAP_KEY, ((ShuntRequestHandler) handlerMap.get(SHUNT_NODE)).toMap());
                 }
                 result.put(path, stubsAndShunt);
                 return null;
             }
         });
         return result;
     }
     //</editor-fold>
 
     private List<StubRequestHandler> getStubRequestHandlers(String path) {
         return (List<StubRequestHandler>) uriHandlers.get(path).get(Configuration.STUB_NODE);
     }
 
     private void clean(String path, Map<String, Object> handlerMap, String nodeType) {
         Object shuntRequestHandler = handlerMap.remove(nodeType);
         if (handlerMap.isEmpty()) {
             uriHandlers.remove(path);
         }
 
         LOGGER.debug(String.format("Removed <%1$s> from <%2$s>", shuntRequestHandler, path));
     }
 
     private void loadStubs(List<Map<String, Object>> stubDescriptions, Map<String, Object> nodeMap) {
         if (stubDescriptions != null && !stubDescriptions.isEmpty()) {
             ((List<StubRequestHandler>) nodeMap
                     .get(Configuration.STUB_NODE))
                     .addAll(Lists.collect(stubDescriptions, new Block<Map<String, Object>, StubRequestHandler>() {
                         @Override
                         public StubRequestHandler invoke(Map<String, Object> stubDescription) {
                             try {
                                 return AbstractStubRequestHandler.createStubFor(stubDescription);
                             } catch (ScriptException e) {
                                 throw new RuntimeException(e);
                             }
                         }
                     }));
         }
     }
 
     private void loadShunt(Map<String, String> shuntDescription, Map<String, Object> nodeMap) {
         try {
             if (shuntDescription != null) {
                 nodeMap.put(Configuration.SHUNT_NODE, new ShuntRequestHandler(shuntDescription));
             }
         } catch (MalformedURLException e) {
             throw new RuntimeException(e);
         }
     }
 }
