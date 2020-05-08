 package em.eval;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 /**
  * A single configuration represents an semi-supervised learning experiment
  * in which there is a fixed number of labeled examples and a fixed number of
  * unlabeled examples.
  * 
  * @author dmitriy dligach
   */
 public class Configuration {
   
   public String dataPath;
   public String labelPath;
   
   public int numLabeled;
   public int numUnlabeled;
   public int numIterations;
   
   public Set<String> sourceLabels; // labels to be remapped
   public String targetLabel; // label with which to remap
   
   public Configuration(
       String pathToData,
       String pathToLabels,
       int numLabeledExamples,
       int numUnlabeledExamples, 
       int emIterations, 
       Set<String> sourceLabelSet,
       String targetLabel) {
     this.dataPath = pathToData;
     this.labelPath = pathToLabels;
     this.numLabeled = numLabeledExamples;
     this.numUnlabeled = numUnlabeledExamples;
     this.numIterations = emIterations;
     this.sourceLabels = sourceLabelSet;
     this.targetLabel = targetLabel;
   }
 
   /**
    * Generate a list of configurations for a given size of the labeled set.
    * TODO: This method should probably find a different home.
    */
   public static List<Configuration> createConfigurations(String phenotype, int numLabeledExamples) {
 
     List<Configuration> configurations = new ArrayList<Configuration>();
     String dataFile;
     String labelFile;
     Set<String> sourceLabelSet;
     String targetLabel;
 
     if(phenotype.equals("cd")) {
       dataFile = Constants.cdData;
       labelFile = Constants.cdLabels;
       sourceLabelSet = null;
       targetLabel = null;
     } else if(phenotype.equals("uc")) {
       dataFile = Constants.ucData;
       labelFile = Constants.ucLabels;
       sourceLabelSet = null;
       targetLabel = null;
     } else if(phenotype.equals("ms")) {
       dataFile = Constants.msData;
       labelFile = Constants.msLabels;
       sourceLabelSet = Constants.msSourceLabels;
       targetLabel = Constants.msTargetLabel;
     } else if(phenotype.equals("t2d")) {
      dataFile = Constants.t2dData;
       labelFile = Constants.t2dLabels;
       sourceLabelSet = Constants.t2dSourceLabels;
       targetLabel = Constants.t2dTargetLabel;
     } else {
       throw new IllegalArgumentException("Bad phenotype!");
     }
 
     // make a baseline configuration (labeled data only)
     Configuration configuration0 = new Configuration(
         dataFile,
         labelFile,
         numLabeledExamples,
         0,
         0,
         sourceLabelSet,
         targetLabel);
     configurations.add(configuration0);
     
     // make the rest of configurations
     for(int numUnlabeledExamples : Constants.unlabeledSizes) {
       Configuration configuration = new Configuration(
           dataFile,
           labelFile,
           numLabeledExamples,
           numUnlabeledExamples,
           Constants.iterations,
           sourceLabelSet,
           targetLabel);
       configurations.add(configuration);
     }
     
     return configurations;
   }
 }
 
