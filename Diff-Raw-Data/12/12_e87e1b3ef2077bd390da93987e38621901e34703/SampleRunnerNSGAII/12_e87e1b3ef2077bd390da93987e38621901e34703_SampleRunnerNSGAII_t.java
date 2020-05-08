 package org.kevoree.modeling.genetic.democloud.evolutionary;
 
 import org.cloud.Cloud;
 import org.kevoree.modeling.genetic.democloud.CloudPopulationFactory;
 import org.kevoree.modeling.genetic.democloud.fitnesses.*;
 
 import org.kevoree.modeling.genetic.democloud.mutators.AddNodeMutator;
 import org.kevoree.modeling.genetic.democloud.mutators.RemoveNodeMutator;
 import org.kevoree.modeling.genetic.democloud.mutators.RemoveSoftwareMutator;
 import org.kevoree.modeling.genetic.democloud.mutators.CloneNodeMutator;
 
 import org.kevoree.modeling.genetic.democloud.mutators.AddSoftwareMutator;
 import org.kevoree.modeling.genetic.democloud.mutators.SmartMutator.AddSmartMutator;
 import org.kevoree.modeling.genetic.democloud.mutators.SmartMutator.RemoveSmartMutator;
 
 import org.kevoree.modeling.optimization.api.mutation.MutationSelectionStrategy;
 import org.kevoree.modeling.optimization.api.solution.Solution;
 import org.kevoree.modeling.optimization.engine.genetic.GeneticAlgorithm;
 import org.kevoree.modeling.optimization.engine.genetic.GeneticEngine;
 import org.kevoree.modeling.optimization.framework.SolutionPrinter;
 import org.kevoree.modeling.optimization.executionmodel.ExecutionModel;
 import org.kevoree.modeling.optimization.util.ExecutionModelExporter;
 import org.kevoree.modeling.optimization.web.Server;
 import org.kevoree.modeling.optimization.api.metric.ParetoMetrics;
 
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
        engine.setAlgorithm(GeneticAlgorithm.HypervolumeMOEA);
 
 
        // engine.desactivateOriginAware();
 
         engine.addOperator(new AddNodeMutator());
         engine.addOperator(new RemoveNodeMutator());
         engine.addOperator(new AddSoftwareMutator());
 
         engine.addOperator(new CloneNodeMutator());
         engine.addOperator(new RemoveSoftwareMutator());
         engine.addOperator(new AddSmartMutator());
         engine.addOperator(new RemoveSmartMutator());
 
         engine.addFitnessFuntion(new CloudCostFitness());
         engine.addFitnessFuntion(new CloudSimilarityFitness());
         engine.addFitnessFuntion(new CloudLatencyFitness());
         engine.addFitnessFuntion(new CloudRedundancyFitness());
 
 
         engine.setMutationSelectionStrategy(MutationSelectionStrategy.DARWIN);
 
 
 
        engine.setMaxGeneration(1000);
         engine.setPopulationFactory(new CloudPopulationFactory().setSize(10));
 
        engine.setAlgorithm(GeneticAlgorithm.EpsilonNSGII);
         engine.addParetoMetric(ParetoMetrics.HYPERVOLUME);
        // engine.addParetoMetric(ParetoMetrics.MIN_MEAN);
 
 
 
       //  engine.addFitnessMetric(new CloudCostFitness(), ParetoFitnessMetrics.MIN);
        // engine.addFitnessMetric(new CloudCostFitness(), ParetoFitnessMetrics.MAX);
 
 
         List<Solution<Cloud>> result = engine.solve();
         for (Solution sol : result) {
             //SolutionPrinter.instance$.print(sol, System.out);
         }
 
         System.out.println(engine.getMutationSelector().toString());
 
 
 
         engine.setMutationSelectionStrategy(MutationSelectionStrategy.RANDOM);
 
         engine.solve();
 
 
         ExecutionModel model = engine.getExecutionModel();
       //  ExecutionModelExporter.instance$.exportMetrics(model,new File("results"));
 
 
         Server.instance$.serveExecutionModel(model);
 
 
 
 
     }
 }
