 package em.test;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Random;
 
 import com.google.common.base.Charsets;
 import com.google.common.io.Files;
 
 import data.Dataset;
 import data.I2b2Dataset;
 import data.Split;
 import em.implementation.EmAlgorithm;
 
 public class EmWithTrueUnlabeled {
 
   public static void main(String[] args) throws IOException {
     
     File file = new File(Constants.OUTFILE);
     if(file.exists()) {
       System.out.println(Constants.OUTFILE + " already exists... deleting...");
       file.delete();
     }
     
     for(int numLabeled = Constants.STEP; numLabeled < Constants.MAXLABELED; numLabeled += Constants.STEP) {
       double labeledOnlyAccuracy = testEm(numLabeled, 0);
       double labeledAndUnlabeledAccuracy = testEm(numLabeled, Constants.ITERATIONS);
       String out = String.format("%d %.4f %.4f\n", numLabeled, labeledOnlyAccuracy, labeledAndUnlabeledAccuracy);
       Files.append(out, file, Charsets.UTF_8);
     }
   }
   
   public static double testEm(int numberOfLabeledExamples, int iterations) throws FileNotFoundException {
 
     // load labeled data
     I2b2Dataset dataset = new I2b2Dataset();
     dataset.loadCSVFile(Constants.DATAFILE, Constants.LABELFILE);
     dataset.normalize();
     dataset.makeAlphabets();
     
     // load unlabeled data
     I2b2Dataset unlabeled = new I2b2Dataset();
     unlabeled.loadFromCSVFile(Constants.DATAFILE, Constants.LABELFILE, Constants.UNLABELED);
     dataset.normalize();
     
     Split[] splits = dataset.split(Constants.FOLDS);
     double cumulativeAccuracy = 0;
 
     for(int fold = 0; fold < Constants.FOLDS; fold++) {
       Dataset labeled = new Dataset();
       Dataset nontest = splits[fold].getPoolSet();
       Dataset test = splits[fold].getTestSet();
 
       labeled.add(nontest.popRandom(numberOfLabeledExamples, new Random(100)));
 
       double accuracy = EmAlgorithm.runAndEvaluate(
           labeled, 
           unlabeled,
           test, 
           dataset.getLabelAlphabet(), 
           dataset.getFeatureAlphabet(),
          iterations,
          1.0);
       cumulativeAccuracy = cumulativeAccuracy + accuracy;
     }
 
     double averageAccuracy = cumulativeAccuracy / Constants.FOLDS;
     
     return averageAccuracy;
   }
 }
