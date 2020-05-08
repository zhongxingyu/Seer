 package dk.itu.kf04.g4tw.model;
 
 import dk.itu.kf04.g4tw.util.DynamicArray;
import dk.itu.kf04.g4tw.util.MinPQ;
 
 import java.util.Iterator;
 import java.util.PriorityQueue;
 /**
  *
  */
 public abstract class DijkstraEdge implements Iterable<DijkstraEdge>, Comparable<DijkstraEdge> {
 
     public abstract double getLength();
 
     private DynamicArray<DijkstraEdge> edges = new DynamicArray<DijkstraEdge>();
 
     public void addEdge(DijkstraEdge e){
            edges.add(e);
     }
 
     public int compareTo(DijkstraEdge o) {
         return Double.compare(this.getLength(), o.getLength());
     }
 
     public Iterator<DijkstraEdge> iterator() {
         return new Iterator<DijkstraEdge>() {
             private int n = 0;
             public boolean hasNext() {
                 return n < edges.length() - 1;
             }
 
             public DijkstraEdge next() {
                 return edges.get(n++);
             }
 
             public void remove() {
                 throw new UnsupportedOperationException("Deletion not permitted");
             }
         };
     }
 }
