 package com.tinkerpop.blueprints.pgm.impls.hollow;
 
 import com.tinkerpop.blueprints.pgm.Edge;
 import com.tinkerpop.blueprints.pgm.Vertex;
 import com.tinkerpop.blueprints.pgm.impls.StringFactory;
 import com.tinkerpop.blueprints.pgm.impls.hollow.util.HollowEdgeSequence;
 
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * @author Daniel Margo (http://www.eecs.harvard.edu/~dmargo)
  */
 public class HollowVertex extends HollowElement implements Vertex {
     private HollowGraph graph = null;
     protected long vid = -1;
 
     protected HollowVertex(final HollowGraph graph) {
 		this.vid = 0;
 		this.graph = graph;
     }
 
    protected HollowVertex(final HollowGraph graph, final Object id) {
     	if(!(id instanceof Long))
    		throw new IllegalArgumentException("HollowGraph: " + id + " is not a valid Vertex ID.");
     	
     	this.vid = ((Long) id).longValue();
 		this.graph = graph;
     }
     
     public HollowVertex(final HollowGraph graph, final long vid) {
     	this.vid = vid;
     	this.graph = graph;
     }
 
     protected void remove() {
         this.vid = -1;
         this.graph = null;
     }
     
     public Object getId() {
     	return this.vid != -1 ? new Long(this.vid) : null;
     }
 
     public Iterable<Edge> getOutEdges(final String... labels) {
     	if (labels.length == 0)
         	return new HollowEdgeSequence(this.graph, this.vid, true);
     	else
     		return new HollowEdgeSequence(this.graph, this.vid, true, labels);
     }
 
     public Iterable<Edge> getInEdges(final String... labels) {
     	if (labels.length == 0)
         	return new HollowEdgeSequence(this.graph, this.vid, false);
     	else
     		return new HollowEdgeSequence(this.graph, this.vid, false, labels);
     }
 
     public Object getProperty(final String propertyKey) {
 		return new Object();
     }
 
     public Set<String> getPropertyKeys() {
 		return new HashSet<String>();
     }
     
     public void setProperty(final String propertyKey, final Object value) {}
 
     public Object removeProperty(final String propertyKey) {
     	return new Object();
     }
     
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
 
         final HollowVertex other = (HollowVertex) obj;
         return (this.vid == other.vid);
     }
 
     public int hashCode() {
         return (new Long(this.vid)).hashCode();
     }
     
     public String toString() {
         return StringFactory.vertexString(this);
     }
 }
