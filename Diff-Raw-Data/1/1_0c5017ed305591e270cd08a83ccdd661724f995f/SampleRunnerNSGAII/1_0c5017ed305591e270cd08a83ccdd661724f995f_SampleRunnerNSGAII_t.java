 package org.kevoree.modeling.genetic.democloud.evolutionary;
 
 import org.cloud.Cloud;
 import org.kevoree.modeling.genetic.democloud.CloudPopulationFactory;
 import org.kevoree.modeling.genetic.democloud.fitnesses.CloudCostFitness;
 import org.kevoree.modeling.genetic.democloud.fitnesses.CloudLatencyFitness;
 
 import org.kevoree.modeling.genetic.democloud.fitnesses.CloudRedundancyFitness;
 import org.kevoree.modeling.genetic.democloud.mutators.AddNodeMutator;
 import org.kevoree.modeling.genetic.democloud.mutators.RemoveNodeMutator;
 import org.kevoree.modeling.genetic.democloud.mutators.RemoveSoftwareMutator;
 import org.kevoree.modeling.genetic.democloud.mutators.CloneNodeMutator;
 
 import org.kevoree.modeling.genetic.democloud.mutators.AddSoftwareMutator;
 import org.kevoree.modeling.genetic.democloud.mutators.SmartMutator.AddSmartMutator;
 import org.kevoree.modeling.genetic.democloud.mutators.SmartMutator.RemoveSmartMutator;
 import org.kevoree.modeling.optimization.api.Solution;
 import org.kevoree.modeling.optimization.engine.genetic.GeneticAlgorithm;
 import org.kevoree.modeling.optimization.engine.genetic.GeneticEngine;
 import org.kevoree.modeling.optimization.framework.SolutionPrinter;
 import org.kevoree.modeling.optimization.executionmodel.ExecutionModel;
 import org.kevoree.modeling.optimization.util.ExecutionModelExporter;
 
 import org.kevoree.modeling.optimization.api.ParetoFitnessMetrics;
 import org.kevoree.modeling.optimization.api.ParetoMetrics;
 
 import java.io.File;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: donia.elkateb
  * Date: 10/10/13
  * Time: 9:33 AM
  * To change this template use File | Settings | File Templates.
  */
 public class SampleRunnerNSGAII {
 
 
 
     public static void main(String[] args) throws Exception {
 
         GeneticEngine<Cloud> engine = new GeneticEngine<Cloud>();
 
         engine.desactivateOriginAware();
 
         engine.addOperator(new AddNodeMutator());
         engine.addOperator(new RemoveNodeMutator());
         engine.addOperator(new AddSoftwareMutator());
 
         engine.addOperator(new CloneNodeMutator());
         engine.addOperator(new RemoveSoftwareMutator());
         engine.addOperator(new AddSmartMutator());
         engine.addOperator(new RemoveSmartMutator());
 
         engine.addFitnessFuntion(new CloudCostFitness());
         engine.addFitnessFuntion(new CloudLatencyFitness());
         engine.addFitnessFuntion(new CloudRedundancyFitness());
         //engine.addFitnessFuntion(new CloudAdaptationCostFitness());
 
         engine.setMaxGeneration(100);
         engine.setPopulationFactory(new CloudPopulationFactory().setSize(10));
 
         engine.setAlgorithm(GeneticAlgorithm.NSGAII);
         engine.addFitnessMetric(new CloudCostFitness(), ParetoFitnessMetrics.Min);
         engine.addFitnessMetric(new CloudCostFitness(), ParetoFitnessMetrics.Max);
         engine.addParetoMetric(ParetoMetrics.Mean);
 
         List<Solution<Cloud>> result = engine.solve();
 
         SolutionPrinter printer = new SolutionPrinter();
         for (Solution sol : result) {
             printer.print(sol, System.out);
         }
 
         ExecutionModel model = engine.getExecutionModel();
         ExecutionModelExporter.instance$.exportMetrics(model,new File("results"));
 
     }
 }
