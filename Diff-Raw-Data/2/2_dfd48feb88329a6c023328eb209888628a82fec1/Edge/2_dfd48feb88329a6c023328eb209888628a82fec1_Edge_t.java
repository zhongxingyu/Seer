 package graph;
 import DList.*;
 
 public class Edge implements Comparable {
     public Vertex v1;
     public Vertex v2;
     int weight;
     Edge partner; 
     DListNode<Edge> container;
  
    /**
     * Edge constructor
     *
     * @param v1 the first vertex incident to the edge  
     * @param v2 the second vertex incident to the edge
     * @param container the node containing this edge
    **/
     public Edge(Vertex v1, Vertex v2, int weight, DListNode<Edge> container){
       this.v1 = v1;
       this.v2 = v2;
       this.container = container;
       this.weight = weight;
       this.partner = null;
     }
 
     public void setPartner(Edge partner){
       this.partner = partner;
     }
 
     public Edge getPartner(){
       return partner;
     }
 
     public VertexPair vertices(){
       return new VertexPair(v1,v2);
     }
 
     /**
      *  compareTo() compares this edge with another object o and returns
      *  a numerical representation of their weight differences. 
      *  This follows the properties outlined in the Java API such that:
      *   -Object o must be an edge; if not, return -255. This is done
      *      rather than throwing a ClassCastException.
      *   -If object o has a weight greater than this edge: -1
      *   -If object o has a weight less    than this edge:  1
      *   -If object o has a weight equal   to   this edge:  0
      *  
      *  @param o the Object to be compared to.
      *  @return the numerical representation of the ordering of this and o.
      **/
     public int compareTo(Object o) {
        if(!(o instanceof Edge)) {
             return -255;
         } else if (this.weight == ((Edge)o).weight) {
             return 0;
         } else if(this.weight > ((Edge)o).weight) {
             return 1;
         } else {  //o must be greater than this
             return -1;
         }
     }
 }
