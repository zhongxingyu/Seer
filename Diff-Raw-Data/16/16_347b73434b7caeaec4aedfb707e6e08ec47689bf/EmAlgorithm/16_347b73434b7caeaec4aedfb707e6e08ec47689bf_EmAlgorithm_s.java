 package em;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Random;
 
 import data.Dataset;
 import data.I2b2Dataset;
 import data.Split;
 
 public class EmAlgorithm {
   
   public static void main(String[] args) throws IOException {
 
     final int FOLDS = 5; // number of folds
    final int ITERATIONS = 0; // number of iterations
     final int LABELED = 20; // number of labeled examples
     
     I2b2Dataset dataset = new I2b2Dataset();
     dataset.loadCSVFile("/home/dima/active/ibd/data/data.txt", "/home/dima/active/ibd/data/labels-cd.txt");
     dataset.makeAlphabets();
 
     Split[] splits = dataset.split(FOLDS, new Random(100));
     double cumulativeAccuracy = 0;
     
     for(int fold = 0; fold < FOLDS; fold++) {
       
       Dataset labeled = new Dataset();
       Dataset unlabeled = splits[fold].getPoolSet();
       Dataset test = splits[fold].getTestSet();
       
       labeled.add(unlabeled.popRandom(LABELED, new Random(100)));
       labeled.setInstanceProbabilityDistribution(new HashSet<String>(dataset.getLabelAlphabet().getStrings()));
       
       labeled.setAlphabets(dataset.getLabelAlphabet(), dataset.getFeatureAlphabet());
       labeled.makeVectors();
 
       EmModel em = new EmModel(dataset.getLabelAlphabet());
       em.train(labeled);
       
       for(int iteration = 0; iteration < ITERATIONS; iteration++) {
         
         // E-step
         unlabeled.setAlphabets(dataset.getLabelAlphabet(), dataset.getFeatureAlphabet());
         unlabeled.makeVectors();
         em.label(unlabeled);
 
         // M-step
         Dataset labeledPlusUnlabeled = new Dataset(labeled.getInstances(), unlabeled.getInstances());
         labeledPlusUnlabeled.setAlphabets(dataset.getLabelAlphabet(), dataset.getFeatureAlphabet());
         labeledPlusUnlabeled.makeVectors();
         em.train(labeledPlusUnlabeled);
       }
       
       test.setAlphabets(dataset.getLabelAlphabet(), dataset.getFeatureAlphabet());
       test.makeVectors();
       double accuracy = em.test(test);
       
       cumulativeAccuracy = cumulativeAccuracy + accuracy;
     }
     
     double accuracy = cumulativeAccuracy / FOLDS;
     System.out.println("average accuracy: " + accuracy);
   }
 }
