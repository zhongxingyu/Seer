 package org.kevoree.modeling.genetic.democloud.fitnesses;
 
 import org.cloud.*;
 import org.kevoree.modeling.optimization.api.GenerationContext;
 import org.kevoree.modeling.optimization.api.fitness.GaussianFitnessFunction;
 
 /**
  * Created with IntelliJ IDEA.
  * User: donia.elkateb
  * Date: 10/2/13
  * Time: 9:27 AM
  */
 public class CloudLatencyFitness extends GaussianFitnessFunction<Cloud> {
 
     public double evaluate(Cloud model, GenerationContext<Cloud> cloudGenerationContext) {
         double latency = 0;
         for (VirtualNode node : model.getNodes()) {
             for (Software soft : node.getSoftwares()) {
                 latency = latency + soft.getLatency();
             }
         }
         return (latency / (model.getNodes().size()));
     }
 
 
     @Override
     public double min() {
         return 0.0;
     }
 
     public double max() {
 
         return 100.0;
     }
 
     @Override
     public double target() {
         return 50;
     }
 
     @Override
    public double variance() {
         return 5;
     }
 }
