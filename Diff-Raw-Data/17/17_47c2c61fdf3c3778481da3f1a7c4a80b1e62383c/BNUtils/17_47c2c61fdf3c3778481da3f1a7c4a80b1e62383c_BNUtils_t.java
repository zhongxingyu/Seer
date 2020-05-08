 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.pitt.isp.sverchkov.bn;
 
 import java.util.*;
 
 /**
  *
  * @author YUS24
  */
 public class BNUtils {
     
     public static <N> List<N> nodesInTopOrder( BayesNet<N,?> net ){
         List<N> result = new ArrayList<>(net.size());
         
         // Get a local representation of the graph
         Map<N,Set<N>> graphMap = new HashMap<>();
         for( N node : net ){
             Set<N> parents = new HashSet<>( net.parents( node ) );
             graphMap.put( node, parents );
         }
         
         // Init parentless set
         Set<N> orphans = new HashSet<>();
        {
            Set<N> removeSet = new HashSet<>();
            for( Map.Entry<N,Set<N>> node : graphMap.entrySet() )
                if( node.getValue().isEmpty() )
                    orphans.add( node.getKey() );
            
            graphMap.keySet().removeAll( removeSet );
        }
         
         // The meat
         while( !orphans.isEmpty() ){
             
             // Get an orphan node
             N node = orphans.iterator().next();
             orphans.remove( node );
             result.add( node );
             
            // Remove the node and its edges from the graph
            graphMap.remove( node );
             for( Map.Entry<N,Set<N>> entry : graphMap.entrySet() ){
                 entry.getValue().remove( node );
                 if( entry.getValue().isEmpty() ){
                     orphans.add( entry.getKey() );
                 }
             }
         }
         
         // Optional sanity check could go here
         
         return result;
     }
 }
