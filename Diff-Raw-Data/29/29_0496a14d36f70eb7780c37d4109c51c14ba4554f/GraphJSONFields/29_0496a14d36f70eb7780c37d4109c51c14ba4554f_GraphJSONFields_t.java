 package org.triple_brain.module.model.json.graph;
 
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 import org.triple_brain.module.model.graph.Edge;
 import org.triple_brain.module.model.graph.SubGraph;
 import org.triple_brain.module.model.graph.Vertex;
 
 /**
  * Copyright Mozilla Public License 1.1
  */
 public class GraphJSONFields {
     public static final String VERTICES = "vertices";
     public static final String EDGES = "edges";
 
     public static JSONObject toJson(SubGraph graph) {
         JSONObject graphAsJson = new JSONObject();
        try {
             graphAsJson.put(
                     VERTICES,
                     verticesAsJsonFromGraph(graph)
             );
             graphAsJson.put(
                     EDGES,
                     edgesAsJsonFromGraph(graph)
             );
        } catch (JSONException e) {
             throw new RuntimeException(e);
         }
         return graphAsJson;
     }
 
    private static JSONObject verticesAsJsonFromGraph(SubGraph graph) {
        JSONObject vertices = new JSONObject();
        try {
            for (Vertex vertex : graph.vertices()) {
                vertices.put(
                        vertex.id(),
                        VertexJsonFields.toJson(vertex)
                );
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
         }
         return vertices;
     }
 
    private static JSONArray edgesAsJsonFromGraph(SubGraph graph) {
         JSONArray edges = new JSONArray();
        for (Edge edge : graph.edges()) {
             edges.put(
                     EdgeJsonFields.toJson(edge)
             );
         }
         return edges;
     }
 }
