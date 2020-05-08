 package visibilityGraph;
 
 public class GraphException extends Exception{
     public GraphException(String str){super(str);}
 }
 
 class InvalidGraphNumberOfDimensionsException extends GraphException{
     public InvalidGraphNumberOfDimensionsException(){super("Invalid number of node dimensions provided during graph construction.");}
 }
 
 class EmptyNodeSetException extends GraphException{
     public EmptyNodeSetException(){super("The Node set provided for graph construction is empty.");}
 }
 
 class ZeroProjectedClustersException extends GraphException{
    public ZeroProjectedClustersException(){super("No octree leaf nodes intersectwith ray.");}
 }
