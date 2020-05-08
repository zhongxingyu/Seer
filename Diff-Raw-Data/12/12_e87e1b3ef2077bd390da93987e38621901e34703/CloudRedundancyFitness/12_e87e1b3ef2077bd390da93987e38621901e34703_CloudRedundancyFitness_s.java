 package org.kevoree.modeling.genetic.democloud.fitnesses;
 
 import org.cloud.Cloud;
 import org.cloud.VirtualNode;
 import org.cloud.impl.DefaultCloudFactory;
 import org.kevoree.modeling.api.trace.TraceSequence;
 import org.kevoree.modeling.genetic.democloud.Requirements;
 import org.kevoree.modeling.optimization.api.GenerationContext;
 import org.kevoree.modeling.optimization.api.fitness.FitnessFunction;
 import org.kevoree.modeling.optimization.api.fitness.FitnessOrientation;
 
 /**
  * Created with IntelliJ IDEA.
  * User: donia.elkateb
  * Date: 10/2/13
  * Time: 9:27 AM
  * To change this template use File | Settings | File Templates.
  */
 public class CloudRedundancyFitness implements FitnessFunction<Cloud> {
 
 
 
 
     private DefaultCloudFactory cloudfactory = new DefaultCloudFactory();
 
 
     @Override
     public double evaluate(Cloud model, GenerationContext<Cloud> cloudGenerationContext) {
     {
         int size =1;
 
         Requirements redundancy  =new Requirements();
         double count =redundancy.Redunduncy(model) ;
 
 
 
         for(VirtualNode vnode : model.getNodes())
         {
             size = size + vnode.getSoftwares().size();
 
         }
 
 
         double redundancyeval =   (count/size) ;
         //System.out.println("redunduncy is"+ size )  ;
        return redundancyeval;
     }
 
 }
 
     @Override
     public double min() {
 
         return 0.0;
     }
     public double max() {
 
         return 1.0;
     }
     public FitnessOrientation orientation() {
 
         return FitnessOrientation.MINIMIZE;
     }
 
 }
