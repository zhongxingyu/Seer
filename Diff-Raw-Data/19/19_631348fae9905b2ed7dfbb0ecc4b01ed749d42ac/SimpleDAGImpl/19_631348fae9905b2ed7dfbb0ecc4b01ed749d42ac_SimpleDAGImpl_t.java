 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.pitt.isp.sverchkov.graph;
 
import java.io.File;
 import java.util.*;
 
 /**
  *
  * @author YUS24
  */
 public class SimpleDAGImpl<N> implements DAG<N> {
     
     private final Map<N,Set<N>> parentMap;

     public SimpleDAGImpl(){
         parentMap = new HashMap<>();
     }
     
     public SimpleDAGImpl( DAG<N> source ){
         this();
         for( N node : source )
             parentMap.put( node, new HashSet<>( source.parents(node) ) );
     }
 
     @Override
     public int size() {
         return parentMap.size();
     }
 
     @Override
     public Collection<N> parents(N node) {
         return parentMap.get( node );
     }
 
     @Override
     public Iterator<N> iterator() {
         return parentMap.keySet().iterator();
     }
 
     public void addArc(N node, N parent) {
         Set<N> parentSet = parentMap.get( node );
         if( parentSet == null ){
             parentSet = new HashSet<>();
             parentMap.put( node, parentSet );
         }
         parentSet.add( parent );
     }
     
 }
