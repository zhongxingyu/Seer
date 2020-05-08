 package semsup.eval;
 
 import java.io.FileNotFoundException;
 import java.util.HashSet;
 import java.util.Random;
 
 import data.Dataset;
 import data.I2b2Dataset;
 import data.Split;
 import em.implementation.EmModel;
 
 public class Evaluation {
 
   /**
    * Use labeled data only.
    */
   public static double evaluateBaseline(Configuration configuration) {
 
     I2b2Dataset dataset = new I2b2Dataset();
     try {
       dataset.loadCSVFile(configuration.dataPath, configuration.labelPath);
     } catch (FileNotFoundException e) {
       System.err.println("could not load data: " + configuration.dataPath);
     }
     if(configuration.sourceLabels != null) {
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
 
       labeled.setInstanceClassProbabilityDistribution(new HashSet<String>(dataset.getLabelAlphabet().getStrings()));
       labeled.setAlphabets(dataset.getLabelAlphabet(), dataset.getFeatureAlphabet());
       labeled.makeVectors();
       
      EmModel classifier = new EmModel(dataset.getLabelAlphabet());
       classifier.train(labeled);
  
       test.setAlphabets(dataset.getLabelAlphabet(), dataset.getFeatureAlphabet());
       test.makeVectors();
       double accuracy = classifier.test(test);
 
       cumulativeAccuracy += accuracy;
     }
 
     return cumulativeAccuracy / Constants.folds;    
   }  
 }
