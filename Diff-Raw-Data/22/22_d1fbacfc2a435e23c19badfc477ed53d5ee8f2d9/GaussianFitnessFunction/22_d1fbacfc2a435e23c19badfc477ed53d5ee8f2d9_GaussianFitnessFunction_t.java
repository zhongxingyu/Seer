 package org.kevoree.modeling.optimization.api.fitness;
 
 import org.kevoree.modeling.api.KMFContainer;
 
 /**
  * Created by duke on 4/7/14.
  */
 public abstract class GaussianFitnessFunction<A extends KMFContainer> extends FitnessFunction<A> {
 
     public abstract double target();
 
    public abstract double std();
 
 }
