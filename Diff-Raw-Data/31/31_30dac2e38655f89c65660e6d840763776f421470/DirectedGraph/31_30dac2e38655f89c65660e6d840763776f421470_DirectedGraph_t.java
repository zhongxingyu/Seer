 package mtp.robots.milkshake.analytics;
 
 import java.util.concurrent.*;
 
 public class DirectedGraph<TVertexData, TEdgeData> {
     ConcurrentHashMap<TVertexData, Vertex<TVertexData, TEdgeData>> vertices
             = new ConcurrentHashMap<TVertexData, Vertex<TVertexData, TEdgeData>>();
 
     public int vertexCount() { return this.vertices.size(); }
 
     public int totalEdgeCount() {
         int count = 0;
         for (Vertex<TVertexData, TEdgeData> n : this.vertices.values()) {
            count += n.getEdgeCount();
         }
         return count;
     }
 
     public void addVertex(TVertexData key, Vertex<TVertexData, TEdgeData> vertex) {
        vertices.putIfAbsent(key, vertex);
     }
 
    public void removeVertex(TVertexData key) throws Exception {
        checkExistence(key, "key doesn't exist");
         vertices.remove(key);
     }
 
    public void addEdge(TVertexData u, TVertexData v, TEdgeData data) throws Exception {
        checkUVExistance(u,v);
         vertices.get(u).addEdge(v, data);
     }
 
    public void removeEdge(TVertexData u, TVertexData v) throws Exception {
        checkUVExistance(u,v);
         vertices.get(u).removeEdge(v);
 
         // need to remove it from all nodes. Not happy with this,
         // but being directed there is no good way to find it's connections.
         // maybe worth making it undirected.
         for (Vertex<TVertexData, TEdgeData> n : this.vertices.values()) {
             n.removeEdge(v);
         }
     }
 
    public void getVertex(TVertexData key) throws Exception {
        checkExistence(key, "key doesn't exist");
         vertices.get(key);
     }
 
    public Vertex<TVertexData, TEdgeData> createAndAddVertex(TVertexData vertexData) {
         Vertex<TVertexData, TEdgeData> vert = new Vertex<TVertexData, TEdgeData>(vertexData);
        this.addVertex(vertexData, vert);
         return vert;
     }

    private void checkUVExistance(TVertexData u, TVertexData v) throws Exception {
        checkExistence(u, "u is not in graph");
        checkExistence(v, "v is not in graph");
    }

    private void checkExistence(TVertexData n, String errMessage) throws Exception {
        if (!vertices.containsKey(n)) throw new Exception(errMessage);
    }
 }
