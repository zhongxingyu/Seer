 package org.kevoree.modeling.genetic.genetictest.fitnesses;
 
 
 import org.genetictest.BinaryString;
 import org.genetictest.MyBoolean;
 import org.kevoree.modeling.optimization.api.GenerationContext;
 import org.kevoree.modeling.optimization.api.fitness.FitnessFunction;
 import org.kevoree.modeling.optimization.api.fitness.FitnessOrientation;
 
 /**
  * Created with IntelliJ IDEA.
  * User: duke
  * Date: 05/09/13
  * Time: 17:52
  */
 
 public class DecimalEnumeratelFitness implements FitnessFunction<BinaryString> {
 
     public static double MAX=10.0;
    public static int total=0;

 
 
     @Override
     public double evaluate(BinaryString model, GenerationContext<BinaryString> cloudGenerationContext) {
         double result = 0;
         double power=1;
         for (MyBoolean b : model.getValues()) {
             if(b.getValue())
                 result+=power;
             power=power*2;
         }
        total++;
         return result;
     }
     @Override
     public double min() {
 
         return 0.0;
     }
     public double max() {
 
         return Math.pow(2,MAX)-1;
     }
     public FitnessOrientation orientation() {
 
         return FitnessOrientation.MAXIMIZE;
     }
 }
