 package org.apache.felix.ipojo.everest.servlet;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import org.apache.felix.ipojo.annotations.*;
 import org.apache.felix.ipojo.everest.impl.DefaultRequest;
 import org.apache.felix.ipojo.everest.services.*;
 import org.osgi.service.http.HttpService;
 import org.osgi.service.http.NamespaceException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import static org.apache.felix.ipojo.everest.servlet.HttpUtils.*;
 
 /**
  * The Everest servlet.
  */
 @Component(immediate = true)
 @Instantiate
 public class EverestServlet extends HttpServlet {
 
 
     public static final String EVEREST_SERVLET_PATH = "/everest";
 
     @Requires
     private EverestService everest;
 
     @Bind
     public void bindHttp(HttpService service, Map properties) throws ServletException, NamespaceException {
         service.registerServlet(EVEREST_SERVLET_PATH, this, null, null);
     }
 
     @Unbind
     public void unbindHttp(HttpService service) throws ServletException, NamespaceException {
         service.unregister(EVEREST_SERVLET_PATH);
     }
 
     @Override
     protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         System.out.println("Everest servlet called");
 
         // Translate request
         DefaultRequest request = translate(req);
         if (request == null) {
             ObjectNode node = JsonUtils.get().newObject();
             node.put("error", "The HTTP request cannot be translated to Everest");
             notimplemented(node).wrap(resp);
             return;
         }
 
         HttpResult result = null;
         try {
             Resource resource = everest.process(request);
             if (!isHead(req)) {
                 // PUT => CREATION
                 if (isPut(req)) {
                     result = created(toJSON(req, resource));
                 } else {
                     result = ok(toJSON(req, resource));
                 }
             } else {
                 result = ok();
             }
             // Compute and add location
             result.location(toURL(req, resource.getPath()));
 
         } catch (IllegalActionOnResourceException e) {
             ObjectNode node = JsonUtils.get().newObject();
             node.put("error", "illegal action on resource");
             node.put("path", request.path().toString());
             node.put("action", request.action().toString());
             node.put("message", e.getMessage());
             result = notallowed(node);
         } catch (ResourceNotFoundException e) {
             ObjectNode node = JsonUtils.get().newObject();
             node.put("error", "resource not found");
             node.put("path", request.path().toString());
             node.put("action", request.action().toString());
             node.put("message", e.getMessage());
             result = notfound(node);
         }
 
         result.wrap(resp);
     }
 
     /**
      * Computes the HTTP url of the given path.
      * The url is computed thanks to the request.
      * @param  request the HTTP Request
      * @param path the path
      * @return the URL pointing to the given path
      */
     private String toURL(HttpServletRequest request, Path path) {
         return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() +
                 EVEREST_SERVLET_PATH + "/" + path.toString();
     }
 
    protected JsonNode toJSON(HttpServletRequest request, Resource resource) throws IOException {
        ObjectNode root = toJSON(resource);
 
         // Relations
         // Relations are indexed by their name
         ObjectNode relations = JsonUtils.get(request).newObject();
         for (Relation relation : resource.getRelations()) {
             relations.put(relation.getName(), JsonUtils.get(request).toJson(relation));
         }
 
         root.put("__relations", relations);
         return root;
     }
 
     protected ObjectNode toJSON(Resource resource) throws IOException {
         ObjectNode root = JsonUtils.get().newObject();
 
         // Metadata
         for (Map.Entry<String, Object> entry : resource.getMetadata().entrySet()) {
             String k = entry.getKey();
            if (k == null) {
                k = "null";
            }
             root.put(k, JsonUtils.get().toJson(entry.getValue()));
         }
 
       return root;
     }
 
     public static DefaultRequest translate(HttpServletRequest request) {
         System.out.println("Path info : " + request.getPathInfo());
         Path path = Path.from(request.getPathInfo());
 
         Action action;
         if (isGet(request)  || isHead(request)) {
             action = Action.READ;
         } else if (isPut(request)) {
             action = Action.CREATE;
         } else if (isPost(request)) {
             action = Action.UPDATE;
         } else if (isDelete(request)) {
             action = Action.DELETE;
         } else {
             return null; // Unsupported request.
         }
 
         Map<String, ?> params = flat(request.getParameterMap());
 
         return new DefaultRequest(action, path, params); // TODO Detect JSON.
     }
 
     public static Map<String, ?> flat(Map<String, String[]> params) {
         if (params == null) {
             return Collections.emptyMap();
         }
         LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
         for (Map.Entry<String, String[]> entry : params.entrySet()) {
             if (entry.getValue() == null) {
                 // No value.
                 map.put(entry.getKey(), null);
             } else if (entry.getValue().length == 0) {
                 map.put(entry.getKey(), Boolean.TRUE.toString());
             } else if (entry.getValue().length == 1) {
                 // Scalar parameter.
                 map.put(entry.getKey(), entry.getValue()[0]);
             } else if (entry.getValue().length > 1) {
                 // Translate to list
                 map.put(entry.getKey(), Arrays.asList(entry.getValue()));
             }
         }
         return map;
     }
 }
