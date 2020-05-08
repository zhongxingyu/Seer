 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.pitt.isp.sverchkov.bnvis;
 
 import edu.pitt.isp.sverchkov.bn.BayesNet;
 import edu.pitt.isp.sverchkov.combinatorics.Assignments;
 import java.util.*;
 
 /**
  *
  * @author YUS24
  */
 public class BNNodeModelImpl implements BNNodeModel {
     
     private final String name;
     private final String[] values;
     
     private final List<CPTRow> cpt = new ArrayList<>();
     
     public BNNodeModelImpl( BayesNet<String,String> net, String node ){
         name = node;
         values = net.values( node ).toArray( new String[0] );
         
         Map<String,Collection<String>> setMap = new HashMap<>();
         for( String parent : net.parents(node) )
            setMap.put(parent, net.values(parent));
         
         // Make CPTs
         for( Map<String,String> assignment : new Assignments<>( setMap ) ){
             double[] p = new double[values.length];
             for( int i=0; i<values.length; i++ )
                 p[i] = net.probability(Collections.singletonMap(node, values[i]), assignment);
             cpt.add( new SimpleCPTRow( assignment, p ) );
         }
     }
 
     @Override
     public String name() {
         return name;
     }
 
     @Override
     public Collection<String> values() {
         return Arrays.asList(values);
     }
 
     @Override
     public Iterable<? extends CPTRow> activeCPTS() {
         return cpt;
     }
     
     private static class SimpleCPTRow implements CPTRow {
         
         private final Map<String,String> assignment;
         private final List<Double> probabilities;
         
         public SimpleCPTRow( Map<String,String> a, double[] d ){
             
             assignment = Collections.unmodifiableMap( a );
             
             Double[] wrapper = new Double[d.length];
             for( int i=0; i<d.length; i++ )
                 wrapper[i] = new Double( d[i] );
             probabilities = Collections.unmodifiableList( Arrays.asList(wrapper) );
             
         }
 
         @Override
         public Map<String, String> parentAssignment() {
             return assignment;
         }
 
         @Override
         public Iterator<Double> iterator() {
             return probabilities.iterator();
         }
     }
 }
