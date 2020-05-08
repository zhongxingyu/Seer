 package org.triple_brain.module.model.graph;
 
 import com.google.inject.Inject;
 import org.junit.Test;
 import org.triple_brain.module.model.WholeGraph;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 /*
 * Copyright Mozilla Public License 1.1
 */
 public class WholeGraphTest extends AdaptableGraphComponentTest {
 
     @Inject
     WholeGraph wholeGraph;
 
     @Test
     public void there_are_no_duplicates_in_vertices(){
         assertTrue(wholeGraph.getAllVertices().hasNext());
         Set<Vertex> visitedVertices = new HashSet<Vertex>();
         Iterator<Vertex> vertexIterator = wholeGraph.getAllVertices();
         while(vertexIterator.hasNext()){
             Vertex vertex = vertexIterator.next();
             if(visitedVertices.contains(vertex)){
                 fail();
             }
             visitedVertices.add(vertex);
         }
     }
 }
