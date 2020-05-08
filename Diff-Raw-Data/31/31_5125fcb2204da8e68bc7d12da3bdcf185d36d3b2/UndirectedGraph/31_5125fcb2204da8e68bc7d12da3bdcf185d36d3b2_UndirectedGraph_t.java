 /*
  * DvRLib - Generic
  * Duncan van Roermund, 2010
  * UndirectedGraph.java
  */
 
 package dvrlib.generic;
 
 public class UndirectedGraph extends Graph {
    public UndirectedGraph(int vertexCount) {
       super(vertexCount);
    }
 
    /**
     * Returns true if there is an edge between vertices a and b, false otherwise.
     * O(1).
     */
    @Override
    public boolean hasEdge(int a, int b) {
       return (a < b ? super.hasEdge(a, b) : super.hasEdge(b, a));
    }
 
    /**
     * Returns the Edge between vertices a and b.
     * O(1).
     */
    @Override
    public Edge getEdge(int a, int b) {
       return (a < b ? super.getEdge(a, b) : super.getEdge(b, a));
    }
 
    /**
     * Adds an edge between vertices a and b.
     * @return true if the edge was removed, false otherwise.
     * O(e).
     */
    @Override
    public boolean addEdge(int a, int b) {
       if(a == b)
          return false;
       else if(a < b)
          return super.addEdge(a, b);
       else
          return super.addEdge(b, a);
    }
 
    /**
     * Removes the edge between vertices a and b.
     * O(1).
     */
    @Override
    public void removeEdge(int a, int b) {
       if(a < b)
          super.removeEdge(a, b);
       else
          super.removeEdge(b, a);
    }
 
    @Override
   protected String printEdge(int a, int b) {
      return (a < b ? super.printEdge(a, b) : "-");
    }
 }
