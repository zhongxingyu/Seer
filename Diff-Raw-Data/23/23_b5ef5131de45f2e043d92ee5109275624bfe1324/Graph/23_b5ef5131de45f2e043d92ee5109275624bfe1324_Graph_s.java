 import java.util.ArrayList;
 import java.util.Iterator;
 
 class Graph { 
   ArrayList<Vertex> vertices;
 
   public Graph(int numVertices) {
     vertices = new ArrayList<Vertex>(numVertices+1);
     for(int i = 0; i <= numVertices; i++) {
       vertices.add(new Vertex(i));
     }
   }
 
  public void addEdge(int from, int to) {
     Vertex fromVertex = this.vertices.get(from);
     Vertex toVertex = this.vertices.get(to);
    EdgeNode edge = new EdgeNode(from, to, 0);
     fromVertex.edges.add(edge);
    toVertex.edges.add(edge);
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
   int flow;
 
  public EdgeNode(int from, int to, int flow) {
     this.from = from;
     this.to = to;
     this.flow = flow;
   }
 }
 
