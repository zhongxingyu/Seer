 package org.neo4j.training.backend;
 
 import org.neo4j.rest.graphdb.RestAPI;
 import org.neo4j.rest.graphdb.RestAPIFacade;
 import org.slf4j.Logger;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.Map;
 import java.util.Scanner;
 
 import static org.neo4j.helpers.collection.MapUtil.map;
 import static org.neo4j.helpers.collection.MapUtil.store;
 import static org.neo4j.training.backend.Util.join;
 
 /**
 * @author mh
 * @since 30.05.12
 */
 public class BackendService {
 
     private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(BackendService.class);
 
     private final GraphStorage storage;
 
     public BackendService() {
         this.storage = createGraphStorage();
     }
 
     protected GraphStorage createGraphStorage() {
         final String restUrl = System.getenv("NEO4J_URL");
         RestAPI api = createRestApi(restUrl);
         GraphStorage storage = null;
         if (api!=null) storage = new GraphStorage(api);
         log("Graph Storage " + restUrl + "storage" + storage);
         return storage;
     }
 
     protected static RestAPI createRestApi(String restUrl) {
         if (restUrl==null) return null;
         if (!restUrl.contains("/db/data")) restUrl += "/db/data";
         String login = System.getenv("NEO4J_LOGIN");
         String password = System.getenv("NEO4J_PASSWORD");
         if (login != null && password != null) return new RestAPIFacade(restUrl, login, password);
 
         try {
             URL url = new URL(restUrl);
             String userInfo = url.getUserInfo();
             if (userInfo==null || userInfo.trim().isEmpty()) return new RestAPIFacade(restUrl);
             return new RestAPIFacade(restUrl, userInfo.split(":")[0],userInfo.split(":")[1]);
         } catch (MalformedURLException e) {
             LOG.error("Invalid storage url "+restUrl,e);
             return null;
         }
     }
 
     private void log(String msg) {
         LOG.warn(msg);
     }
 
     public Map<String, Object> execute(Neo4jService service, String init, String query, String version) {
         if (version!=null) service.setVersion(version);
         boolean initial = init != null;
         if (dontInitialize(service) || init==null || init.equalsIgnoreCase("none")) init=null;
         if ("none".equalsIgnoreCase(query)) query=null;
         final Map<String, Object> data = map("init", init, "query", query,"version",service.getVersion());
         long start = System.currentTimeMillis(), time = start;
         try {
             time = trace("service", time);
             if (init != null && service.isMutatingQuery(init)) {
                 final CypherQueryExecutor.CypherResult result = service.initCypherQuery(init);
                 if (result.getQuery() != null) data.put("init", result.getQuery());
             }
             if (initial) {
                 service.setInitialized();
             }
             time = trace("graph", time);
             CypherQueryExecutor.CypherResult result = null;
             if (query!=null) {
                 result = service.cypherQuery(query);
                 data.put("json", result.getJson());
                 data.put("columns", result.getColumns());
                 data.put("stats", result.getQueryStatistics());
                 if (result.getQuery()!=null) data.put("query",result.getQuery());
             }
             time = trace("cypher", time);
             data.put("visualization", service.cypherQueryViz(result));
             trace("viz", time);
         } catch (Exception e) {
            e.printStackTrace();
            data.put("error", e.toString());
         }
         time = trace("all", start);
         data.put("time", time-start);
         return data;
     }
 
     private boolean dontInitialize(Neo4jService service) {
         return !service.doesOwnDatabase() || service.isInitialized();
     }
 
     protected long trace(String msg, long time) {
         long now = System.currentTimeMillis();
         log("## " + msg + " took: " + (now - time) + " ms.");
         return now;
     }
 
 
     public String shortenUrl(String uri) {
         try {
             final InputStream stream = (InputStream) new URL("http://tinyurl.com/api-create.php?url=" + URLEncoder.encode(uri, "UTF-8")).getContent();
             final String shortUrl = new Scanner(stream).useDelimiter("\\z").next();
             stream.close();
             return shortUrl;
         } catch (IOException ioe) {
             return null;
         }
     }
 
     public Map<String, Object> execute(Neo4jService service, GraphInfo info) {
         if (!info.hasRoot()) {
             service.deleteReferenceNode();
         }
         final Map<String, Object> result = this.execute(service, info.getInit(), info.getQuery(), info.getVersion());
         result.put("message",info.getMessage());
         return result;
     }
 
     protected String param(Map input, String param, String defaultValue) {
         if (input==null) return defaultValue;
         String data = (String) input.get(param);
         if (data == null || data.isEmpty()) {
             data = defaultValue;
         } else {
             log(param+": "+data);
         }
         return data;
     }
 
     public Map<String, Object> init(Neo4jService service, Map<String,Object> input, String session) {
         input.put("init",param(input,"init",null));
         input.put("query",param(input,"query",null));
         Map<String, Object> result = execute(service, GraphInfo.from(input));
         service.setShutdownHook(createShutdownHook(service, session));
         return result;
     }
 
     Map<String, Object> init(final Neo4jService service, String id, final String session) {
         GraphInfo info = findForSessionOrId(id, session);
         final Map<String, Object> result;
         if (info!=null) {
             result = execute(service, info.getInit(), info.getQuery(), info.getVersion());
             result.put("message",info.getMessage());
             result.put("history",info.getHistory());
         } else {
             result = execute(service, GraphInfo.from(map()));
             result.put("error","Graph not found for id " + id+ " rendering default");
         }
         service.setShutdownHook(createShutdownHook(service, session));
         return result;
     }
 
     private GraphInfo findForSessionOrId(String id, String session) {
         if (storage == null) return null;
         GraphInfo info = storage.find(session);
         if (info != null) return info;
         return storage.find(id);
     }
 
     private Neo4jService.ShutdownHook createShutdownHook(final Neo4jService service, final String session) {
         return new Neo4jService.ShutdownHook() {
             String initialState = service.exportToCypher();
 
             public void shutdown(Neo4jService neo4jService) {
                 String currentState = neo4jService.exportToCypher();
                 if (currentState.equals(initialState)) return;
 
                 if (storage==null) return;
 
                 GraphInfo existingState = storage.find(session);
                 String history = join(service.getHistory(), ";");
                 if (existingState==null) {
                     storage.create(new GraphInfo(session, currentState, "none", "none").withHistory(history).noRoot());
                 }
                 else {
                     String newHistory = existingState.getQuery() + ";" + history;
                     storage.update(new GraphInfo(session, currentState, "none", "none").withHistory(newHistory).noRoot());
                 }
             }
         };
     }
 
     public Map<String, Object> save(String id, String init) {
         if (storage==null) {
             return map("error","no storage configured");
         }
         GraphInfo existingState = storage.find(id);
         GraphInfo info = new GraphInfo(id, init, "none", "none").noRoot();
         Map<String, Object> result = info.toMap();
         if (existingState==null) {
             GraphInfo newInfo = storage.create(info);
             if (newInfo == null) result.put("error","error during create");
             else result = newInfo.toMap();
             result.put("action","create");
         } else {
             storage.update(info);
             result.put("action", "update");
         }
         return result;
     }
 
     public boolean delete(String id) {
         try {
             if (storage != null) {
                 storage.delete(id);
                 return true;
             }
         } catch(Exception e) {
             log(e.getMessage());
         }
         return false;
     }
 }
