 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package etc.aloe;
 
 import etc.aloe.controllers.LabelingController;
 import etc.aloe.cscw2013.EvaluationImpl;
 import etc.aloe.cscw2013.FeatureExtractionImpl;
 import etc.aloe.cscw2013.LabelMappingImpl;
 import etc.aloe.cscw2013.ResolutionImpl;
 import etc.aloe.cscw2013.ThresholdSegmentation;
 import etc.aloe.data.EvaluationReport;
 import etc.aloe.data.FeatureSpecification;
 import etc.aloe.data.MessageSet;
 import etc.aloe.data.Model;
 import etc.aloe.data.SegmentSet;
 import etc.aloe.processes.Segmentation;
 import java.io.File;
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.Option;
 
 /**
  *
  * @author michael
  */
 public class AloeLabel extends Aloe {
 
     @Argument(index = 0, usage = "input CSV file containing messages", required = true, metaVar = "INPUT_CSV")
     private File inputCSVFile;
 
     @Argument(index = 1, usage = "output directory (contents may be overwritten)", required = true, metaVar = "OUTPUT_DIR")
     private void setOutputDir(File dir) {
         this.outputDir = dir;
         dir.mkdir();
 
         outputCSVFile = new File(dir, FileNames.OUTPUT_CSV_NAME);
         outputEvaluationReportFile = new File(dir, FileNames.OUTPUT_EVALUTION_REPORT_NAME);
     }
     private File outputDir;
     private File outputCSVFile;
     private File outputEvaluationReportFile;
    @Option(name = "--model", aliases = {"-m"}, usage = "use an existing model file", required = true, metaVar="MODEL_FILE")
     private File inputModelFile;
    @Option(name = "--features", aliases = {"-f"}, usage = "use an existing feature specification file", required = true, metaVar="FEATURES_FILE")
     private File inputFeatureSpecFile;
     @Option(name = "--fp-cost", usage = "the cost of a false positive (default 1)")
     private double falsePositiveCost = 1;
     @Option(name = "--fn-cost", usage = "the cost of a false negative (default 1)")
     private double falseNegativeCost = 1;
 
     @Override
     public void printUsage() {
        System.err.println("java -jar aloe.jar Label INPUT_CSV OUTPUT_DIR -m MODEL_FILE -f FEATURES_FILE [options...]");
     }
 
     @Override
     public void run() {
         System.out.println("== Preparation ==");
 
         Segmentation segmentation = new ThresholdSegmentation(this.segmentationThresholdSeconds, segmentationByParticipant);
         segmentation.setSegmentResolution(new ResolutionImpl());
 
         LabelingController labelingController = new LabelingController();
         labelingController.setFeatureExtractionImpl(new FeatureExtractionImpl());
         labelingController.setEvaluationImpl(new EvaluationImpl(falsePositiveCost, falseNegativeCost));
         labelingController.setMappingImpl(new LabelMappingImpl());
 
         MessageSet messages = this.loadMessages(dateFormatString, inputCSVFile);
         FeatureSpecification spec = this.loadFeatureSpecification(inputFeatureSpecFile);
         Model model = this.loadModel(inputModelFile);
 
         SegmentSet segments = segmentation.segment(messages);
 
         labelingController.setModel(model);
         labelingController.setSegmentSet(segments);
         labelingController.setFeatureSpecification(spec);
         labelingController.run();
 
         EvaluationReport evalReport = labelingController.getEvaluationReport();
 
         System.out.println("== Saving Output ==");
 
         saveEvaluationReport(evalReport, outputEvaluationReportFile);
         saveMessages(messages, outputCSVFile);
 
         System.out.println("Testing Report:");
         System.out.println(evalReport);
         System.out.println("---------");
     }
 }
