 package com.pathfinder.internal;
 
 import com.google.common.base.Function;
 import com.pathfinder.api.TransitEdge;
 import com.pathfinder.api.TransitPath;
 import org.junit.Test;
 
 import java.util.List;
 
 import static com.google.common.collect.Lists.transform;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.core.Is.is;
 
 public class DijkstraGraphTraversalTest {
 
     private final DijkstraGraphTraversal dijkstraGraphTraversal = new DijkstraGraphTraversal();
 
     @Test
     public void test() throws Exception{
         TransitPath path = dijkstraGraphTraversal.findShortestPath("CNSHA", "USCHI", null).get(0);
 
         List<TransitEdge> edges = path.getTransitEdges();
         
         String pathAsString = transform(edges, intoString()).toString();
 
        assertThat(pathAsString, is("[CNSHA => CNHKG, CNHKG => NLRTM, NLRTM => USNYC, USNYC => USCHI]"));
     }
 
     private Function<TransitEdge, String> intoString(){
         return new Function<TransitEdge, String>(){
             public String apply(TransitEdge edge) {
                 return edge.getFromUnLocode() + " => " + edge.getToUnLocode();
             }
         };
     }
 }
