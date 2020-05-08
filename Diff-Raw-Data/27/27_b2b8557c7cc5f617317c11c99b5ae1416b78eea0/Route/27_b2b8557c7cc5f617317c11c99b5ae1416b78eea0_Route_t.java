 package org.neo4j.training.backend;
 
 import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TransactionFailureException;
 import org.slf4j.Logger;
 import org.neo4j.kernel.lifecycle.LifecycleException;
 import spark.HaltException;
 import spark.Request;
 import spark.Response;
 
 import javax.servlet.http.HttpServletRequest;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLEncoder;
 import java.util.Map;
 
 /**
  * @author mh
  * @since 08.04.12
  */
 abstract class Route extends spark.Route {
 
     private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(Route.class);
 
     Route(String path) {
         super(path);
     }
     
     public String stop(int status, String message) {
         halt(status, message);
         return message;
     }
 
     protected String getSessionId(Request request) {
         return SessionService.getSessionId(request.raw());
     }
 
     @Override
     public Object handle(Request request, Response response) {
         Transaction tx = null;
         try {
             doBefore(request,response);
             Neo4jService service = service(request);
             tx = service.begin();
             Object result = doHandle(request, response, service);
             tx.success();
             return result;
         } catch (LifecycleException lce) {
             fail(tx);
             reset(request);
             SessionService.cleanSessions();
             return handleException(lce);
         } catch (OutOfMemoryError oom) {
             fail(tx);
             reset(request);
             SessionService.cleanSessions();
             return handleException(oom);
         } catch (HaltException he) {
             fail(tx);
             throw he;
         } catch (Exception e) {
             fail(tx);
             return handleException(e);
         } finally {
            if (tx!=null) {
                try {
                    tx.finish();
                } catch (TransactionFailureException tfe) {
                    LOG.warn("Could not commit transaction",tfe.getMessage());
                }
            }
         }
     }
 
     private void fail(Transaction tx) {
         if (tx!=null) tx.failure();
     }
 
     private Object handleException(Throwable e) {
         e.printStackTrace();
         halt(500, e.getMessage());
         return e.getMessage();
     }
 
     protected abstract Object doHandle(Request request, Response response, Neo4jService service) throws Exception;
     protected void doBefore(Request request, Response response) {
     }
 
     protected String param(Map input, String param, String defaultValue) {
         if (input==null) return defaultValue;
         String data = (String) input.get(param);
         if (data == null || data.isEmpty()) {
             data = defaultValue;
         } else {
             LOG.warn(param+": "+data);
         }
         return data;
     }
 
     protected long trace(String msg, long time) {
         long now = System.currentTimeMillis();
         LOG.warn("## " + msg + " took: " + (now - time) + " ms.");
         return now;
     }
 
     protected void reset(Request request) {
         SessionService.reset(request.raw());
     }
 
     protected Neo4jService service(Request request) {
         return SessionService.getService(request.raw(),true);
     }
 }
