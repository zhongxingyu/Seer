 import java.util.ArrayList;
 import java.util.HashMap;
 
 class Graph {
   ArrayList<Vertex> vertices;
   HashMap<EdgeNode, Integer> edges;
 
   public Graph(int numVertices) {
     vertices = new ArrayList<Vertex>(numVertices+1);
     for(int i = 0; i <= numVertices; i++) {
       vertices.add(new Vertex(i));
     }
     edges = new HashMap<EdgeNode, Integer>();
   }
 
   public void addEdge(int from, int to, int capacity) {
     Vertex fromVertex = this.vertices.get(from);
     Vertex toVertex = this.vertices.get(to);
     EdgeNode edge = new EdgeNode(from, to, 0, capacity);
    edge.reverse = new EdgeNode(to, from, 0, capacity);
     edge.reverse.forward = edge;
     fromVertex.edges.add(edge);
     toVertex.edges.add(edge.reverse);
     edges.put(edge, 1);
   }
 
   public void printGraph() {
     Vertex curr;
     for(int i = 1; i < vertices.size(); i++) {
       curr = vertices.get(i);
       System.out.println(curr + " key: " + curr.key);
     }
   }
 }
 
 class Vertex {
   ArrayList<EdgeNode> edges;
   int key;
 
   public Vertex(int k) {
     this.edges = new ArrayList<EdgeNode>();
     this.key = k;
   }
 }
 
 class EdgeNode {
   int from;
   int to;
   int capacity;
   int flow;
   EdgeNode reverse;
   EdgeNode forward;
 
   public EdgeNode(int from, int to, int flow, int capacity) {
     this.from = from;
     this.to = to;
     this.flow = flow;
     this.capacity = capacity;
   }
 
   public boolean isBackward() {
       return this.reverse == null;
   }
 
   public boolean isForward() {
 	  return this.forward == null;
   }
 }
 
