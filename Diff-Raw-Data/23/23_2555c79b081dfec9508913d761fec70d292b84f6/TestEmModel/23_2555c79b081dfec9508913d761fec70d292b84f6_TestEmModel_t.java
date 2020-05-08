 package em.thyme;
 
 import java.io.IOException;
 import java.util.HashSet;
 
import semsup.eval.Constants;
 import data.Dataset;
 import em.implementation.EmModel;
 
 public class TestEmModel {
 
   public static void main(String[] args) throws IOException {
 
     Dataset trainSet = new Dataset();
     trainSet.loadCSVFile("/Users/Dima/Boston/Data/Thyme/Vectors/DocTimeRel/train.txt");
     Dataset testSet = new Dataset();
     testSet.loadCSVFile("/Users/Dima/Boston/Data/Thyme/Vectors/DocTimeRel/test.txt");
 
     trainSet.makeAlphabets();
     trainSet.makeVectors();
     testSet.setAlphabets(trainSet.getLabelAlphabet(), trainSet.getFeatureAlphabet());
     testSet.makeVectors();
     
     trainSet.setInstanceClassProbabilityDistribution(new HashSet<String>(trainSet.getLabelAlphabet().getStrings()));
    EmModel classifier = new EmModel(trainSet.getLabelAlphabet(), Constants.defaultLambda);
     classifier.train(trainSet);
     double accuracy = classifier.test(testSet);
 
     System.out.format("accuracy: %.4f\n", accuracy);
   }
 }
