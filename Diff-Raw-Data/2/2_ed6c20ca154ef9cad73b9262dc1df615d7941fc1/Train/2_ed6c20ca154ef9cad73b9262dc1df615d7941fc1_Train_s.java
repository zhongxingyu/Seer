 /**
  * Implement the GOR secondary structure prediction method as discussed in the
  * papers/slides on the homepage. Start with GOR I and advance to GOR III&IV,
  * finally implement GOR V. The secondary structure elements must be predicted
  * in three states H=Helix, E=Sheet and C=Coil. For every sequence position, you
  * should also: print out the probability for the position to be Helix, Sheet,
  * and Coil. The GOR algorithm splits into two parts: - training and prediction
  * Thus implement two binaries, one for each task (see Specifications).
  *
  *
  * Your secondary structure prediction program must be executable from command
  * line. Exact specifications are given in section 5 of this task sheet. For our
  * evaluations, it is necessary for you to precisely follow these
  * specifications!
  *
  *
  * Postprocessing Certain secondary structures do not make sense (e.g. CCCHCCC).
  * You may want to implement a postprocessing to remove such occurences from
  * your predictions. It may be interesting to evaluate GOR with postprocessing
  * against GOR without postprocessing.
  *
  *
  * java -jar train.jar --db <dssp-file> path to training file --method
  * <gor1|gor3|gor4> method --model <model-file> model file output
  *
  * For GORV, a multiple alignment must be given by the parameter --maf. If
  * called with no (or wrong) parameters, your command line tools must output
  * these help texts. The output of predict.jar must be written to stdout in all
  * cases.
  */
 package de.bioinformatikmuenchen.pg4.ssp.ssptrain;
 
 import java.io.File;
 import java.io.IOException;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.PosixParser;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import de.bioinformatikmuenchen.pg4.common.util.IO;
 
 /**
  * SSP Train
  *
  */
 public class Train {
 
     /**
      * enum for possible training methods
      */
     public enum TrainingMethods {
 
         GOR1, GOR3, GOR4
     };
 
     /**
      *
      * @param args
      */
     public static void main(String[] args) {
 
         //
         // get params from args to start a new training campain
         //
 
         // get command options
         final Options opts = new Options();
         opts.addOption("d", "db", true, "dssp path to train method (database)")
                 .addOption("m", "method", true, "method to train <gor1|gor3|gor4>")
                 .addOption("l", "model", true, "output file")
                 .addOption("w", "window", true, "windowSize")
                 .addOption("p", "splitWindow", true, "split window at position");
         // 
         // Parse these options with PosixParser
         // 
         final CommandLineParser cmdLinePosixParser = new PosixParser();
         CommandLine commandLine = null;
         try {
             commandLine = cmdLinePosixParser.parse(opts, args);
         } catch (ParseException parseException) {    // checked exception
             System.err.println(
                     "Encountered exception while parsing using PosixParser:\n"
                     + parseException.getMessage());
             printUsageAndQuit();
         }
 
         String dbFile = "";
         if(commandLine.hasOption("db")) {
             String inputDb = commandLine.getOptionValue("db");
             dbFile = IO.isExistingReadableFileOrQuit(inputDb, "File " + inputDb + " isn't a valid File or doesn't exist!");
         } else {
             System.err.println("No db File spec!");
             printUsageAndQuit();
         }
         
         String modelFile = "";
         if(commandLine.hasOption("model")) {
             String inputModel = commandLine.getOptionValue("model");
             if(IO.isValidFilePathOrName(inputModel) && (!new File(inputModel).isDirectory())) modelFile = inputModel;
             else {
                 System.err.println("Invalid model file given!");
                 printUsageAndQuit();
             }
         } else {
             System.err.println("No model File spec!");
             printUsageAndQuit();
         }
 
         String method = "";
         TrainingMethods tmethod = TrainingMethods.GOR1;
         // third check if model is valid
         if (commandLine.hasOption("method")) {
             // option method has been declared => check if valid
             method = commandLine.getOptionValue("method");
             // method to TrainingMethods
             if ("GOR1".equalsIgnoreCase(method)) {
                 tmethod = TrainingMethods.GOR1;
             } else if ("GOR3".equalsIgnoreCase(method)) {
                 tmethod = TrainingMethods.GOR3;
             } else if ("GOR4".equalsIgnoreCase(method)) {
                 tmethod = TrainingMethods.GOR4;
             } else {
                 System.err.println("ERROR INVALID OR MISSING METHOD!");
                 printUsageAndQuit();
             }
         } else {
             System.err.println("ERROR METHOD NOT SPEC!");
             printUsageAndQuit();
         }
         
         if(commandLine.hasOption("window")) {
            Data.trainingWindowSize = Integer.parseInt(commandLine.getOptionValue("method"));
         }
         
         if(commandLine.hasOption("splitWindow")) {
             Data.prevInWindow = Integer.parseInt(commandLine.getOptionValue("splitWindow"));
         }
 
         // create a new trainer and let him do his job
         Trainer myTrainer;
         if (tmethod == Train.TrainingMethods.GOR1) {
             myTrainer = new TrainerGor1();
         } else if (tmethod == Train.TrainingMethods.GOR3) {
             myTrainer = new TrainerGor3();
         } else {
             myTrainer = new TrainerGor4();
         }
 
         // let the Trainer train
         try {
             System.out.println("Init training ...");
             myTrainer.init();
             System.out.println("Parse and train ...");
             myTrainer.parseFileAndTrain(new File(dbFile));  // give the trainer the data he requires
             File inputFile = new File(modelFile);
             System.out.println("Write data to " + inputFile.getAbsolutePath() + " ...");
             myTrainer.writeMatrixToFile(inputFile);
         } catch(RuntimeException e) {
             System.err.println("Trainingerror: " + e.getMessage());
             System.exit(1);
         }
 
     }
 
     public static void printUsageAndQuit() {
         System.err.println(
                 "Usage: java -jar train.jar --db <dssp-file> --method <gor1|gor3|gor4> --model <model file>\n"
                 + " Options:\n"
                 + "  --db <dssp-file>\tpath to training file\n"
                 + "  --method <gor1|gor3|gor4>\tmethod\n"
                 + "  --model < model file>\tmodel file output\n");
         System.exit(1);
     }
     
 }
