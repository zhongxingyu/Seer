 package org.triple_brain.module.model;
 
 import org.triple_brain.module.model.graph.Vertex;
 
 import java.util.Iterator;
 
 /*
 * Copyright Mozilla Public License 1.1
 */
 public interface WholeGraph {
     public Iterator<Vertex> getAllVertices();
 }
