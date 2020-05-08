 /**
 *   Main.java
 *
 *   Copyright (c) 2013 Vladimir Brigant
 *   This software is distributed under the terms of the GNU General Public License.
 */
 
 package cassp;
 
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.CmdLineException;
 
 import java.io.*;
 import java.util.Properties;
 
 import cassp.utils.*;
 import cassp.config.*;
 
 
 
 /**
  * Main class
  */
 public class Main {
     static Logger logger = Logger.getLogger(Main.class);
 
     // configuration paths
     static String confPath = "./src/cassp/config/test.config";
     static String logPath = "./src/cassp/config/log.config";
 
 
     public static void main(String[] args) {
         // logger configuration
         PropertyConfigurator.configure(Main.logPath);
 
         // simulator configuration
         SimConfig config = new SimConfig(Main.confPath);
         // command line arguments have higher priority
         CmdLineParser parser = new CmdLineParser(config);
 
         try{
             parser.parseArgument(args);
         }catch(CmdLineException e){
             parser.printUsage(System.err);
             System.exit(2);
         }
 
         CASSP simulator = new CASSP(config);
         System.out.println(config.toString());
 
 
         if (config.getCVFolds() > 1){
             simulator.crossValidate(config.getCVFolds());
             //simulator.createEvolutionImage("evolution_best_rule");
             //simulator.createAccClassesImage("accuracy_best_rule");
             //simulator.createReliabImage("reliability_best_rule");
         }
 
         if (config.getTrainMode() != SimConfig.NO_TRAINING &&
                 config.getCVFolds() <= 1){
             simulator.train();
             simulator.createEvolutionImage("evolution");
             simulator.createAccClassesImage("accuracy_train");
             simulator.createReliabImage("reliability_train");
         }
 
         if (config.getTestMode() != SimConfig.NO_TESTING){
             simulator.loadRule();
             double acc = simulator.test();
             logger.info("Test accuracy: " + acc);
             simulator.createAccClassesImage("accuracy_test");
             simulator.createReliabImage("reliability_test");
         }
 
         String toPredict = config.getToPredict();
 
         if (toPredict != null){
             simulator.loadRule();
             String predicted = null;
 
             File f = new File(toPredict);
             if (f.exists()) {
                 predicted = simulator.predict(f);
             }
             else{
                 predicted = simulator.predict(toPredict);
             }
             logger.info("predicted: \n" + predicted);
         }

        System.exit(0);
     }
 }
