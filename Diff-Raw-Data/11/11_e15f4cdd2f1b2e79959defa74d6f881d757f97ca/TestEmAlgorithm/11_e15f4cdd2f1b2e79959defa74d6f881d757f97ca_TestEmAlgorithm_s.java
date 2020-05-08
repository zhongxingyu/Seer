 package em.thyme;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Random;
 
 import com.google.common.base.Charsets;
 import com.google.common.io.Files;
 
import semsup.eval.Constants;
 import data.Dataset;
 import em.implementation.EmAlgorithm;
 
 public class TestEmAlgorithm {
 
  public static final int STEP = 50;
  public static final int MAXLABELED = 3000;
  public static final int NUMUNLABELED = 100;
   public static final int ITERATIONS = 25;
   public static final int RNDSEED = 100;
  public static final double LAMBDA = 0.5;
   public static final String TRAINSET = "/Users/Dima/Boston/Data/Thyme/Vectors/EventTimeContains/train.txt";
   public static final String TESTSET = "/Users/Dima/Boston/Data/Thyme/Vectors/EventTimeContains/test.txt";
   public static final String OUTFILE = "/Users/Dima/Boston/Out/unlabeled";
 
   public static void main(String[] args) throws IOException {
     
     File outFile = new File(OUTFILE + NUMUNLABELED + "lambda" + LAMBDA +  ".txt");
     if(outFile.exists()) {
       System.out.println(outFile.getName() + " already exists... deleting...");
       outFile.delete();
     }
     
     for(int numberOfLabeledExamples = STEP; numberOfLabeledExamples < MAXLABELED; numberOfLabeledExamples += STEP) {
       double labeledOnlyAccuracy = testEm(numberOfLabeledExamples, 0);
       double labeledAndUnlabeledAccuracy = testEm(numberOfLabeledExamples, ITERATIONS);
       String out = String.format("%d %.4f %.4f\n", numberOfLabeledExamples, labeledOnlyAccuracy, labeledAndUnlabeledAccuracy);
       Files.append(out, outFile, Charsets.UTF_8);
     }
   }
 
   public static double testEm(int numberOfLabeledExamples, int iterations) throws FileNotFoundException {
 
     Dataset pool = new Dataset();
     pool.loadCSVFile(TRAINSET);    
     pool.makeAlphabets();
 
     Dataset test = new Dataset();
     test.loadCSVFile(TESTSET);
 
     Dataset labeled = new Dataset();
     labeled.add(pool.popRandom(numberOfLabeledExamples, new Random(RNDSEED)));
 
     Dataset unlabeled = new Dataset();
     unlabeled.add(pool.popRandom(NUMUNLABELED, new Random(RNDSEED)));
 
     double accuracy = EmAlgorithm.runAndEvaluate(
         labeled, 
         unlabeled, 
         test, 
         pool.getLabelAlphabet(), 
         pool.getFeatureAlphabet(),
         iterations,
         LAMBDA);
 
     return accuracy;
   }
 }
