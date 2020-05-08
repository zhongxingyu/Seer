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
 public class DeltaCrossover {
 
     final private static String CONFIGURATION_FILE = "decsb.properties";
 
     private Properties configuration;
 
     public DeltaCrossover(Properties configuration) {
         this.configuration = configuration;
     }
 
     @Parameterized.Parameters
     public static Collection<Object[]> data() {
 
         Collection<Object[]> data = new ArrayList<Object[]>();
         try {
             final Properties tempConfiguration = Utils.loadConfiguration(CONFIGURATION_FILE);
 
             String filename = tempConfiguration.getProperty("dataset_path");
 
            Utils.initLogger("delta_crossover_" + filename.substring(filename.lastIndexOf("/") + 1, filename.length() - 5) + ".txt");
 
             for (int crossoverPoints = 1; crossoverPoints < 23; crossoverPoints += 4) {
                 for (EcsbClassifiers classifier : EcsbClassifiers.values()) {
                     for (Metric metric : Metric.values()) {
                         tempConfiguration.setProperty("crossover_points", String.valueOf(crossoverPoints));
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
