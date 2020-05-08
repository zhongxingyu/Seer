 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package etc.aloe;
 
 import etc.aloe.controllers.InteractiveController;
 import etc.aloe.cscw2013.FeatureExtractionImpl;
 import etc.aloe.cscw2013.LabelMappingImpl;
 import etc.aloe.data.FeatureSpecification;
 import etc.aloe.data.MessageSet;
 import etc.aloe.data.Model;
 import java.io.File;
 import java.text.SimpleDateFormat;
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.Option;
 
 /**
  *
  * @author michael
  */
 public class AloeInteractive extends Aloe {
 
     @Argument(index = 0, usage = "output directory (contents may be overwritten)", required = true, metaVar = "OUTPUT_DIR")
     private void setOutputDir(File dir) {
         this.outputDir = dir;
         dir.mkdir();
 
         outputCSVFile = new File(dir, FileNames.OUTPUT_CSV_NAME);
     }
     private File outputDir;
     private File outputCSVFile;
    @Option(name = "--model", aliases = {"-m"}, usage = "use an existing model file", required = true, metaVar="MODEL_FILE")
     private File inputModelFile;
    @Option(name = "--features", aliases = {"-f"}, usage = "use an existing feature specification file", required = true, metaVar="FEATURES_FILE")
     private File inputFeatureSpecFile;
 
     @Override
     public void printUsage() {
        System.err.println("java -jar aloe.jar Interactive OUTPUT_DIR -m MODEL_FILE -f FEATURES_FILE [options...]");
     }
 
     @Override
     public void run() {
         System.out.println("== Preparation ==");
 
         InteractiveController interactiveController = new InteractiveController();
         interactiveController.setFeatureExtractionImpl(new FeatureExtractionImpl());
         interactiveController.setMappingImpl(new LabelMappingImpl());
 
         FeatureSpecification spec = this.loadFeatureSpecification(inputFeatureSpecFile);
         Model model = this.loadModel(inputModelFile);
 
         interactiveController.setModel(model);
         interactiveController.setFeatureSpecification(spec);
         interactiveController.run();
 
         System.out.println();
         System.out.println("== Saving Output ==");
 
         MessageSet messages = interactiveController.getMessageSet();
         messages.setDateFormat(new SimpleDateFormat(dateFormatString));
         saveMessages(messages, outputCSVFile);
     }
 }
