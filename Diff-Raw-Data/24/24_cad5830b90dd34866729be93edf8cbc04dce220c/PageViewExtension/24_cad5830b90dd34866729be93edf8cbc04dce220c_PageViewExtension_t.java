 package com.whysearchtwice.rexster.extension;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 import com.tinkerpop.blueprints.Graph;
 import com.tinkerpop.blueprints.Vertex;
 import com.tinkerpop.rexster.RexsterResourceContext;
 import com.tinkerpop.rexster.extension.ExtensionDefinition;
 import com.tinkerpop.rexster.extension.ExtensionDescriptor;
 import com.tinkerpop.rexster.extension.ExtensionNaming;
 import com.tinkerpop.rexster.extension.ExtensionPoint;
 import com.tinkerpop.rexster.extension.ExtensionResponse;
 import com.tinkerpop.rexster.extension.HttpMethod;
 import com.tinkerpop.rexster.extension.RexsterContext;
 
 @ExtensionNaming(name = PageViewExtension.NAME, namespace = AbstractParsleyExtension.NAMESPACE)
 public class PageViewExtension extends AbstractParsleyExtension {
     public static final String NAME = "pageView";
 
     @ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH, method = HttpMethod.POST)
     @ExtensionDescriptor(description = "create a new vertex in the graph")
     public ExtensionResponse createNewVertex(@RexsterContext RexsterResourceContext context, @RexsterContext Graph graph) {
         JSONObject attributes = context.getRequestObject();
 
         // Create the new Vertex
         Vertex newVertex = graph.addVertex(null);
         updateVertexProperties(newVertex, attributes);
 
         // Return the id of the new Vertex
         Map<String, String> map = new HashMap<String, String>();
         map.put("id", newVertex.getId().toString());
 
         // Create an edge to the Predecessor or Parent if needed
         try {
             if (attributes.has("predecessor")) {
                 boolean result = createEdge(graph, newVertex, attributes.getString("predecessor"), "successorTo", "predecessorTo");
                 map.put("predecessor", (result) ? "predecessor created successfully" : "predecessor could not be created");
             }
             if (attributes.has("parent")) {
                 boolean result = createEdge(graph, newVertex, attributes.getString("parent"), "childOf", "parentOf");
                 map.put("parent", (result) ? "parent created successfully" : "parent could not be created");
             }
         } catch (JSONException e) {
             e.printStackTrace();
            return ExtensionResponse.error("Unable to create edge between vertex and parent or predecessor");
         }
 
         // Link to the device the pageView came from
         try {
             Vertex device = getDeviceVertex(graph, attributes, map);
 
             graph.addEdge(null, newVertex, device, "viewedOn");
             graph.addEdge(null, device, newVertex, "viewed");
         } catch (JSONException e) {
             e.printStackTrace();
         }
 
         // Link to the domain of the page URL
         try {
             if (attributes.has("pageUrl")) {
                 Vertex domainVertex = findOrCreateDomainVertex(graph, extractDomain(attributes.getString("pageUrl")));
                 graph.addEdge(null, newVertex, domainVertex, "under");
                 graph.addEdge(null, domainVertex, newVertex, "over");
             }
         } catch (URISyntaxException e) {
             e.printStackTrace();
         } catch (JSONException e) {
             e.printStackTrace();
         }
 
         return ExtensionResponse.ok(map);
     }
 
     @ExtensionDefinition(extensionPoint = ExtensionPoint.VERTEX, method = HttpMethod.POST)
     @ExtensionDescriptor(description = "update an existing vertex in the graph")
     public ExtensionResponse updateVertex(@RexsterContext RexsterResourceContext context, @RexsterContext Vertex vertex) {
        if(vertex == null) {
            return ExtensionResponse.error("Invalid vertex, can not update");
        }
        
         updateVertexProperties(vertex, context.getRequestObject());
 
         // Map to store the results
         Map<String, String> map = new HashMap<String, String>();
         map.put("message", "vertex updated");
 
         return ExtensionResponse.ok(map);
     }
 
     private boolean createEdge(Graph graph, Vertex v1, String v2id, String message1, String message2) {
         Vertex v2 = graph.getVertex(v2id);
         if (v2 != null) {
             graph.addEdge(null, v1, v2, message1);
             graph.addEdge(null, v2, v1, message2);
             return true;
         } else {
             return false;
         }
     }
 
     private void updateVertexProperties(Vertex v, JSONObject attributes) {
         Iterator keysIter = attributes.keys();
 
         // For any property that exists in the map, update it
         while (keysIter.hasNext()) {
             try {
                 String key = (String) keysIter.next();
 
                 if (key.equals("type") || key.equals("pageUrl") || key.equals("userId") || key.equals("deviceId")) {
                     String value = (String) attributes.get(key);
                     v.setProperty(key, value);
                 } else if (key.equals("pageOpenTime") || key.equals("pageCloseTime")) {
                     Long value = (Long) attributes.get(key);
                     v.setProperty(key, value);
                 } else if (key.equals("tabId") || key.equals("windowId")) {
                     int value = (Integer) attributes.get(key);
                     v.setProperty(key, value);
                 } else {
                     // Ignore the property for now
                 }
             } catch (JSONException e1) {
                 e1.printStackTrace();
             }
         }
     }
 
     private Vertex getDeviceVertex(Graph graph, JSONObject attributes, Map<String, String> httpReturnObject) throws JSONException {
         if (attributes.has("deviceGuid")) {
             return graph.getVertex(attributes.get("deviceGuid"));
         }
 
         // Create a new Device
         Vertex device = graph.addVertex(null);
         httpReturnObject.put("deviceGuid", device.getId().toString());
 
         Vertex user;
         if (attributes.has("userGuid")) {
             user = graph.getVertex(attributes.get("userGuid"));
         } else {
             // Create a new User
             user = graph.addVertex(null);
             httpReturnObject.put("userGuid", user.getId().toString());
         }
 
         // Connect new device to user
         graph.addEdge(null, user, device, "owns");
         graph.addEdge(null, device, user, "ownedBy");
 
         return device;
     }
 
     private String extractDomain(String pageUrl) throws URISyntaxException {
         URI uri = new URI(pageUrl);
         String domain = uri.getHost();
         return domain.startsWith("www.") ? domain.substring(4) : domain;
     }
 
     private Vertex findOrCreateDomainVertex(Graph graph, String domain) {
         Iterator<Vertex> iter = graph.getVertices("domain", domain).iterator();
         if (iter.hasNext()) {
             return iter.next();
         } else {
             Vertex newVertex = graph.addVertex(null);
             newVertex.setProperty("domain", domain);
             return newVertex;
         }
     }
 }
