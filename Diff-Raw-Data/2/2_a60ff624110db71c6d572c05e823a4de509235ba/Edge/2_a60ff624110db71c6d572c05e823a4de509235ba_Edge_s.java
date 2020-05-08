 package com.github.haw.ai.gkap.graph;
 
 /**
  * @author Till Theis <till.theis@haw-hamburg.de>
  * @author Patrick Detlefsen <patrick.detlefsen@haw-hamburg.de>
  * @author Benjamin Rexin <benjamin.rexin@haw-hamburg.de>
  */
 public interface Edge<E,V> {
     /**
      * @return the content associated with the edge
      */
     public E content();
 
     /**
      * @return the vertices connected by the edge
      */
    public Collection<vertices<V>> vertices();
 
     /**
      * @return true if the vertex 'to' is reachable from the vertex 'from'
      *         through the edge - otherwise false
      */
     public boolean isReachable(Vertex<V> from, Vertex<V> to);
 }
