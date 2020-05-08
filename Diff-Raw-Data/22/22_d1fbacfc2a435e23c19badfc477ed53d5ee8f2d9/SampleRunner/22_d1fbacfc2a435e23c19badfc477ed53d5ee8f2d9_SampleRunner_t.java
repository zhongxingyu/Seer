 package org.kevoree.modeling.genetic.democloud;
 
 import org.cloud.Cloud;
 
 import org.cloud.serializer.JSONModelSerializer;
 import org.kevoree.modeling.genetic.democloud.fitnesses.CloudAdaptationCostFitness;
 import org.kevoree.modeling.genetic.democloud.fitnesses.CloudCostFitness;
import org.kevoree.modeling.genetic.democloud.fitnesses.CloudLatencyFitness;
 import org.kevoree.modeling.genetic.democloud.fitnesses.CloudRedundancyFitness;
 import org.kevoree.modeling.genetic.democloud.mutators.AddNodeMutator;
 import org.kevoree.modeling.genetic.democloud.mutators.RemoveNodeMutator;
 import org.kevoree.modeling.optimization.api.OptimizationEngine;
 import org.kevoree.modeling.optimization.api.solution.Solution;
 import org.kevoree.modeling.optimization.engine.genetic.GeneticEngine;
 import org.kevoree.modeling.optimization.executionmodel.ExecutionModel;
 import org.kevoree.modeling.optimization.framework.SolutionPrinter;
 import org.kevoree.modeling.optimization.util.ExecutionModelExporter;
 
 
 
 
 
 
 import java.io.File;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: duke
  * Date: 07/08/13
  * Time: 16:00
  */
 public class SampleRunner {
 
 
     public static void main(String[] args) throws Exception {
 
         OptimizationEngine<Cloud> engine = new GeneticEngine<Cloud>();
         engine.addOperator(new AddNodeMutator());
         engine.addOperator(new RemoveNodeMutator());
         engine.addFitnessFuntion(new CloudCostFitness());
         engine.addFitnessFuntion(new CloudRedundancyFitness());
        engine.addFitnessFuntion(new CloudLatencyFitness());
         engine.addFitnessFuntion(new CloudAdaptationCostFitness());
 
         engine.setMaxGeneration(100);
         engine.setPopulationFactory(new DefaultCloudPopulationFactory().setSize(10));
 
         List<Solution<Cloud>> result = engine.solve();

         for (Solution sol : result) {
            SolutionPrinter.instance$.simplePrint(sol, System.out);
         }
 
 
         ExecutionModel model = engine.getExecutionModel();
         ExecutionModelExporter.instance$.exportMetrics(model,new File("results"));
 
 
         JSONModelSerializer saver = new JSONModelSerializer();
 
 
 
         }
 
 
 
 }
