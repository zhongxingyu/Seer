 package org.neo4j.training.backend;
 
 import static org.neo4j.helpers.collection.MapUtil.map;
 import static spark.Spark.delete;
 import static spark.Spark.get;
 import static spark.Spark.post;
 
 import java.util.Map;
 
 import org.slf4j.Logger;
 
 import spark.Request;
 import spark.Response;
 import spark.servlet.SparkApplication;
 
 import com.google.gson.Gson;
 
 public class BackendApplication implements SparkApplication {
 
     private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(BackendApplication.class);
 
     private BackendService backendService;
 
     @Override
     public void init() {
         SessionService.setDatabaseInfo(BackendFilter.getDatabase());
         backendService = new BackendService();
         Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
             @Override
             public void uncaughtException(Thread thread, Throwable throwable) {
                 SessionService.cleanSessions();
                 System.gc();
             }
         });
 
         post(new Route("/backend/cypher") {
             protected Object doHandle(Request request, Response response, Neo4jService service) {
                 final String query = request.body();
                 if (query!=null && !query.isEmpty()) {
                     LOG.warn( "cypher: "+query );
                 }
                return new Gson().toJson(backendService.execute(service, null, query, null));
             }
         });
         post(new Route("/backend/cypher/:id") {
             protected Object doHandle(Request request, Response response, Neo4jService service) {
                 if (!service.isInitialized()) {
                     String id = request.params("id");
                     Map<String, Object> result = backendService.init(service, id, getSessionId(request));
                     if (result.containsKey("error")) {
                        return new Gson().toJson(result);
                     }
                 }
                 String query = request.body();
                 if (query!=null && !query.isEmpty()) {
                     LOG.warn( "cypher: "+query );
                 } else {
                     query = "none";
                 }
                return new Gson().toJson(backendService.execute(service, null, query, null));
             }
         });
         post(new Route("/backend/graph/:id") {
             protected Object doHandle(Request request, Response response, Neo4jService service) {
                 String id = request.params("id");
                 String init = request.body();
                 final Map<String, Object> result = backendService.save(id, init);
                return new Gson().toJson(result);
             }
         });
         delete(new Route("/backend/graph/:id") {
             protected Object doHandle(Request request, Response response, Neo4jService service) {
                 String id = request.params("id");
                 boolean result = backendService.delete(id);
                 SessionService.cleanSession(id);
                 return result;
             }
         });
         post( new Route( "/backend/version" )
         {
             protected Object doHandle( Request request, Response response, Neo4jService service )
             {
                 final String version = request.body();
                 service.setVersion( version );
                return new Gson().toJson( map("version", service.getVersion()) );
             }
         } );
         post(new Route("/backend/init") {
             @Override
             protected void doBefore(Request request, Response response) {
                 Neo4jService service = SessionService.getService(request.raw(),true);
                 if (service.isInitialized()) {
                     reset(request);
                 }
             }
 
             protected Object doHandle(Request request, Response response, Neo4jService service) {
                 final Map input = requestBodyToMap(request);
                 final Map<String, Object> result;
                 final String id = param(input, "id", null);
                 if (id != null) {
                     result = backendService.init(service, id, getSessionId(request));
                 } else {
                     result = backendService.init(service, input, getSessionId(request));
                 }
                return new Gson().toJson(result);
             }
 
         });
         get(new Route("/backend/visualization") {
             protected Object doHandle(Request request, Response response, Neo4jService service) {
                 String query = request.queryParams("query");
                return new Gson().toJson(service.cypherQueryViz(query));
             }
         });
         delete(new Route("/backend") {
             protected Object doHandle(Request request, Response response, Neo4jService service) {
                 reset(request);
                 return "deleted";
             }
         });
     }
 
     private Map requestBodyToMap(Request request) {
        Map result = new Gson().fromJson(request.body(), Map.class);
         return result!=null ? result : map();
     }
 }
