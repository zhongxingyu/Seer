 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.pitt.isp.sverchkov.bn;
 
 import java.util.Map;
 
 /**
  *
  * @author YUS24
  */
 public interface MutableBayesNet<N,V> extends BayesNet<N,V> {
    void setCPT( Map<N,V> parentAssignment, Map<V,Double> conditionalProbabilities );
 }
