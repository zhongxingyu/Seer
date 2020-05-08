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
 import com.whysearchtwice.container.PageView;
 
 @ExtensionNaming(name = PageViewExtension.NAME, namespace = AbstractParsleyExtension.NAMESPACE)
 public class PageViewExtension extends AbstractParsleyExtension {
     public static final String NAME = "pageView";
 
     @ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH, method = HttpMethod.POST)
     @ExtensionDescriptor(description = "create a new vertex in the graph")
     public ExtensionResponse createNewVertex(@RexsterContext RexsterResourceContext context, @RexsterContext Graph graph) {
         JSONObject attributes = context.getRequestObject();
         System.out.println("--- A NEW REQUEST ---");
         System.out.println(attributes.toString());
 
         // Check that the device is valid before doing anything else
         Vertex device;
        try {
            device = graph.getVertex(attributes.get("deviceGuid"));
            if (device == null || device.getProperty("type") != "device") {
                return ExtensionResponse.error("Invalid deviceGuid, please recreate");
             }
        } catch (JSONException e) {
            return ExtensionResponse.error("JSON Exception");
         }
 
         // Create the new Vertex
         Vertex newVertex = graph.addVertex(null);
 
         try {
             updateVertexProperties(newVertex, attributes);
         } catch (JSONException e) {
             return ExtensionResponse.error("Unable to set properties on new vertex");
         }
 
         // Return the id of the new Vertex
         Map<String, String> map = new HashMap<String, String>();
         map.put("id", newVertex.getId().toString());
 
         // Create an edge to the Predecessor or Parent if needed
         try {
             if (attributes.has("predecessor")) {
                 boolean result = createEdge(graph, newVertex, attributes.getString("predecessor"), "successorTo");
                 map.put("predecessor", (result) ? "predecessor created successfully" : "predecessor could not be created");
             }
             if (attributes.has("parent")) {
                 boolean result = createEdge(graph, newVertex, attributes.getString("parent"), "childOf");
                 map.put("parent", (result) ? "parent created successfully" : "parent could not be created");
             }
         } catch (JSONException e) {
             return ExtensionResponse.error("Unable to create edge between vertex and parent or predecessor");
         }
 
         // Link to the device the pageView came from
         graph.addEdge(null, device, newVertex, "viewed");
 
         // Link to the domain of the page URL
         try {
             if (attributes.has("pageUrl")) {
                 Vertex domainVertex = findOrCreateDomainVertex(graph, extractDomain(attributes.getString("pageUrl")));
                 graph.addEdge(null, newVertex, domainVertex, "under");
             }
         } catch (URISyntaxException e) {
             return ExtensionResponse.error("URI Syntax Exception");
         } catch (JSONException e) {
             return ExtensionResponse.error("JSON Exception");
         }
 
         return ExtensionResponse.ok(map);
     }
 
     @ExtensionDefinition(extensionPoint = ExtensionPoint.VERTEX, method = HttpMethod.POST)
     @ExtensionDescriptor(description = "update an existing vertex in the graph")
     public ExtensionResponse updateVertex(@RexsterContext RexsterResourceContext context, @RexsterContext Vertex vertex) {
         if (vertex == null) {
             return ExtensionResponse.error("Invalid vertex, can not update");
         }
 
         try {
             updateVertexProperties(vertex, context.getRequestObject());
         } catch (JSONException e) {
             return ExtensionResponse.error("Cannot merge properties into existing vertex");
         }
 
         // Map to store the results
         Map<String, String> map = new HashMap<String, String>();
         map.put("message", "vertex updated");
 
         return ExtensionResponse.ok(map);
     }
 
     private boolean createEdge(Graph graph, Vertex v1, String v2id, String message1) {
         Vertex v2 = graph.getVertex(v2id);
         if (v2 != null) {
             graph.addEdge(null, v1, v2, message1);
             return true;
         } else {
             return false;
         }
     }
 
     private void updateVertexProperties(Vertex v, JSONObject attributes) throws JSONException {
         PageView newAttributes = new PageView(attributes);
         newAttributes.mergeIntoVertex(v);
     }
 
     private String extractDomain(String pageUrl) throws URISyntaxException {
         try {
             URI uri = new URI(pageUrl);
             String domain = uri.getHost();
             return domain.startsWith("www.") ? domain.substring(4) : domain;
         } catch (Exception e) {
             return "special";
         }
 
     }
 
     private Vertex findOrCreateDomainVertex(Graph graph, String domain) {
         Iterator<Vertex> iter = graph.getVertices("domain", domain).iterator();
         if (iter.hasNext()) {
             return iter.next();
         } else {
             Vertex newVertex = graph.addVertex(null);
             newVertex.setProperty("type", "domain");
             newVertex.setProperty("domain", domain);
             return newVertex;
         }
     }
 }
