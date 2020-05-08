 package em.eval;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.List;
 import java.util.Random;
 
 import semsup.eval.Configuration;
 import semsup.eval.Constants;
 import semsup.eval.Evaluation;
 
 import com.google.common.base.Charsets;
 import com.google.common.io.Files;
 
 import data.Alphabet;
 import data.Dataset;
 import data.I2b2Dataset;
 import data.Split;
 import em.implementation.EmAlgorithm;
 
 public class EvaluatePhenotype extends Thread {
 
   public String phenotype;
 
   public EvaluatePhenotype(String phenotype) {
     this.phenotype = phenotype;
   }
 
   public void run() {
 
     File file = new File(Constants.outputDir + phenotype + ".txt");
     if(file.exists()) {
       System.out.println(file.getName() + " already exists... deleting...");
       file.delete();
     }
 
     for(int labeled = Constants.step; labeled < Constants.maxLabeled; labeled += Constants.step) {
       // for a fixed number of labeled examples, create configurations with varying number of unlabeled examples 
       List<Configuration> configurations = Configuration.createConfigurations(phenotype, labeled);
 
       StringBuilder output = new StringBuilder();
       output.append(String.format("%-3d ", labeled));
 
       for(Configuration configuration : configurations) {
         double accuracy;
         if(configuration.numUnlabeled == 0) {
           accuracy = Evaluation.evaluateBaseline(configuration);
         } else {
           accuracy = evaluate(configuration);
         }
         output.append(String.format("%.4f ", accuracy));
       }
 
       output.append("\n");      
       try {
         Files.append(output, file, Charsets.UTF_8);
       } catch (IOException e) {
         System.err.println("could not write output file!");
       }
     }
   }
 
   /**
    * Evaluate a configuration. Return n-fold CV accuracy.
    */
   public double evaluate(Configuration configuration) {
 
     I2b2Dataset dataset = new I2b2Dataset();
     I2b2Dataset unlabeled = new I2b2Dataset();
     try {
       dataset.loadCSVFile(configuration.dataPath, configuration.labelPath);
       unlabeled.loadFromCSVFile(configuration.dataPath, configuration.labelPath, configuration.numUnlabeled);
     } catch (FileNotFoundException e) {
       System.err.println("data file not found!");
     }
     if(configuration.sourceLabels != null) {
       // no need to remap unlabeled since there are no labels
       dataset.mapLabels(configuration.sourceLabels, configuration.targetLabel);
     }
     // make alphabets now, after labels were potentially remapped 
     dataset.makeAlphabets();
 
     Split[] splits = dataset.split(Constants.folds);
     double cumulativeAccuracy = 0;
 
     for(int fold = 0; fold < Constants.folds; fold++) {
       Dataset labeled = new Dataset();
       Dataset nontest = splits[fold].getPoolSet();
       Dataset test = splits[fold].getTestSet();
       labeled.add(nontest.popRandom(configuration.numLabeled, new Random(Constants.rndSeed)));
       double lambda = findBestLambda(labeled, unlabeled, dataset.getLabelAlphabet(), dataset.getFeatureAlphabet());
       double accuracy = EmAlgorithm.runAndEvaluate(
           labeled, 
           unlabeled,
           test, 
           dataset.getLabelAlphabet(), 
           dataset.getFeatureAlphabet(),
           configuration.numIterations,
           lambda);
       cumulativeAccuracy = cumulativeAccuracy + accuracy;
     }
 
     return cumulativeAccuracy / Constants.folds;
   }
   
   /**
   * Search for best lambda using labeled training data.
    */
   public double findBestLambda(Dataset labeled, Dataset unlabeled, Alphabet labelAlphabet, Alphabet featureAlphabet) {
     
     double[] lambdas = {0.0, 0.25, 0.5};
     
    double bestLambda = 1;
    double bestAccuracy = -1;
     for(double lambda : lambdas) {
       double accuracy = evaluateLambda(labeled, unlabeled, labelAlphabet, featureAlphabet, lambda);
       if(accuracy > bestAccuracy) {
         bestAccuracy = accuracy;
         bestLambda = lambda;
       }
     }
     
     return bestLambda;
   }
   
   /**
    * Evaluate a specific value of lambda using n-fold CV.
    */
   public double evaluateLambda(Dataset labeled, Dataset unlabeled, Alphabet labelAlphabet, Alphabet featureAlphabet, double lambda) {
     
    final int folds = 5;
    final int iterations = 15;
     
     Split[] splits = labeled.split(folds);
     double cumulativeAccuracy = 0;
     
     for(int fold = 0; fold < folds; fold++) {      
       double accuracy = EmAlgorithm.runAndEvaluate(
           splits[fold].getPoolSet(), 
           unlabeled,
           splits[fold].getTestSet(), 
           labelAlphabet,
           featureAlphabet,
           iterations,
           lambda);
       cumulativeAccuracy = cumulativeAccuracy + accuracy;
     }
    
     return cumulativeAccuracy / folds;
   }
 }
