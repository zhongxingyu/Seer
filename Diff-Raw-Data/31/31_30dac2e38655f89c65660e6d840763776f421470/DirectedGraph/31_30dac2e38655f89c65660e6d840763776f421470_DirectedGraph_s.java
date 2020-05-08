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
 
    public void removeVertex(TVertexData key) {
         vertices.remove(key);
     }
 
    public void addEdge(TVertexData u, TVertexData v, TEdgeData data) {
         vertices.get(u).addEdge(v, data);
     }
 
    public void removeEdge(TVertexData u, TVertexData v) {
         vertices.get(u).removeEdge(v);
 
         // need to remove it from all nodes. Not happy with this,
         // but being directed there is no good way to find it's connections.
         // maybe worth making it undirected.
         for (Vertex<TVertexData, TEdgeData> n : this.vertices.values()) {
             n.removeEdge(v);
         }
     }
 
    public void getVertex(TVertexData key) {
         vertices.get(key);
     }
 
    public Vertex<TVertexData, TEdgeData> createAndAddVertex(TVertexData key, TVertexData vertexData) {
         Vertex<TVertexData, TEdgeData> vert = new Vertex<TVertexData, TEdgeData>(vertexData);
        this.addVertex(key, vert);
         return vert;
     }
 }
