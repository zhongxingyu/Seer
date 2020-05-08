 package ro.utcluj.ecsb;
 
 import org.apache.log4j.Logger;
 import org.uncommons.watchmaker.framework.EvolutionEngine;
 import org.uncommons.watchmaker.framework.termination.GenerationCount;
 import ro.utcluj.ecsb.evaluation.EcsbModelEvaluator;
 import ro.utcluj.ecsb.population.EcsbIndividual;
 import ro.utcluj.ecsb.utils.EcsbFactory;
 import ro.utcluj.ecsb.utils.EcsbUtils;
 
 import java.io.IOException;
 import java.util.Properties;
 
 public class ECSB {
 
     final private EvolutionEngine<EcsbIndividual> engine;
     final private int populationCount;
     final private int eliteCount;
     final private int numberOfGenerations;
     final private EcsbModelEvaluator validator;
 
     public ECSB(EvolutionEngine<EcsbIndividual> engine, int numberOfGenerations, int eliteCount, int populationCount, EcsbModelEvaluator validator) {
         this.engine = engine;
         this.numberOfGenerations = numberOfGenerations;
         this.eliteCount = eliteCount;
         this.populationCount = populationCount;
         this.validator = validator;
     }
 
 
     public static void main(String[] args) {
         try {
             EcsbUtils.initLogger("test");
             final Properties configuration = EcsbUtils.loadConfiguration(ECSB.class.getResource("decsb.properties"));
            //configuration.setProperty("dataset_path",args[0]);
             final ECSB ecsb = new EcsbFactory(configuration).setUpECSB();
             ecsb.runEvolutionaryCostSensitiveBalancing();
 
         } catch (IOException e) {
             Logger.getLogger("ECSBLog").error("Unable to load configuration file.");
         }
 
     }
 
     public void runEvolutionaryCostSensitiveBalancing() {
 
         EcsbIndividual bestIndividual = engine.evolve(populationCount, eliteCount, new GenerationCount(numberOfGenerations));
 
         Logger.getLogger("ECSBLog").info("best individual: \t" + bestIndividual);
 
         validator.logIndividualEvaluation(bestIndividual);
     }
 
 }
