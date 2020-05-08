 package ex3.search;
 
 import ex3.PRMGraph;
import ex3.PRMUtil;
 import ex3.Vertex;
 import java.util.LinkedList;
 
 public interface SearchAlgorithm {
 
     /* Calculate the shortest path through the graph from one vertex to another. */
    public LinkedList<Vertex> shortestPath(Vertex v1, Vertex v2, PRMGraph graph, PRMUtil util);
 
 }
 
