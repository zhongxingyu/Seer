 package ro.utcluj.ecsb.test;
 
 import org.apache.log4j.Logger;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import ro.utcluj.ecsb.ECSB;
 import ro.utcluj.ecsb.metrics.Metric;
 import ro.utcluj.ecsb.utils.EcsbClassifiers;
 import ro.utcluj.ecsb.utils.EcsbEvolutionObserver;
 import ro.utcluj.ecsb.utils.EcsbFactory;
 import ro.utcluj.ecsb.utils.Utils;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Properties;
 
 @RunWith(value = Parameterized.class)
 public class DeltaPopulation {
 
     final private static String CONFIGURATION_FILE = "decsb.properties";
 
     private Properties configuration;
 
     public DeltaPopulation(Properties configuration) {
         this.configuration = configuration;
     }
 
     @Parameterized.Parameters
     public static Collection<Object[]> data() {
 
         Collection<Object[]> data = new ArrayList<Object[]>();
         try {
             final Properties tempConfiguration = Utils.loadConfiguration(CONFIGURATION_FILE);
 
             String filename = tempConfiguration.getProperty("dataset_path");
 
            Utils.initLogger("delta_pop_" + filename.substring(filename.lastIndexOf("/") + 1, filename.length() - 5) + ".txt");
 
             for (int populationSize = 10; populationSize <= 50; populationSize += 10) {
                 for (EcsbClassifiers classifier : EcsbClassifiers.values()) {
                     for (Metric metric : Metric.values()) {
                         tempConfiguration.setProperty("population_count", String.valueOf(populationSize));
                         tempConfiguration.setProperty("base_classifier", classifier.getClassName());
                         tempConfiguration.setProperty("fitness_metric", metric.getMetricName());
                         data.add(new Object[]{tempConfiguration.clone()});
                     }
                 }
             }
         } catch (IOException e) {
             Logger.getLogger("ECSBLog").error("Unable to load configuration file.");
         }
 
         return data;
     }
 
     @Test
     public void classifierVariationTest() {
         ECSB engine = new EcsbFactory(configuration).setUpECSB();
 
         Logger.getLogger("ECSBLog").info(Utils.propertiesToString(configuration));
         Logger.getLogger("ECSBLog").info(EcsbEvolutionObserver.getHeader());
 
         engine.runEvolutionaryCostSensitiveBalancing();
     }
 }
