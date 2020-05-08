 package em.implementation;
 
 import java.util.HashSet;
 
 import data.Alphabet;
 import data.Dataset;
 
 public class EmAlgorithm {
 
   public static double runAndEvaluate(
                           Dataset labeled, 
                           Dataset unlabeled, 
                           Dataset test, 
                           Alphabet labelAlphabet, 
                           Alphabet featureAlphabet,
                           int iterations) {
     
     labeled.setInstanceClassProbabilityDistribution(new HashSet<String>(labelAlphabet.getStrings()));
     labeled.setAlphabets(labelAlphabet, featureAlphabet);
     labeled.makeVectors();
 
     EmModel em = new EmModel(labelAlphabet);
     em.train(labeled);
     
     for(int iteration = 0; iteration < iterations; iteration++) {
       // E-step
       unlabeled.setAlphabets(labelAlphabet, featureAlphabet);
       unlabeled.makeVectors();
       em.label(unlabeled);
 
       // M-step
       Dataset labeledPlusUnlabeled = new Dataset(labeled.getInstances(), unlabeled.getInstances());
       labeledPlusUnlabeled.setAlphabets(labelAlphabet, featureAlphabet);
       labeledPlusUnlabeled.makeVectors();
       em.train(labeledPlusUnlabeled);
     }
    
     test.setAlphabets(labelAlphabet, featureAlphabet);
     test.makeVectors();
     double accuracy = em.test(test);
 
     return accuracy;
   }
 }
