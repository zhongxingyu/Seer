 package plusone.utils;
 
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import plusone.Main;
 import plusone.utils.TrainingPaper;
 import plusone.utils.PredictionPaper;
 
 public class PaperAbstract implements TrainingPaper, PredictionPaper {
 
     public final Integer index;
     public final Integer[] inReferences;
     public final Integer[] outReferences;
 
     private Map<Integer, Integer> trainingTf;
     private Map<Integer, Integer> testingTf;
     private Map<Integer, Integer> tf;
     public double norm;
     
   /*  protected static Map<Integer, Integer> makeTf(Integer[] abstractWords) {
 	Map<Integer, Integer> tf = new HashMap<Integer, Integer>();
 	for (Integer word : abstractWords) {
 	    if (!tf.containsKey(word))
 	    	tf.put(word, 1);
 	    else
 	    	tf.put(word, tf.get(word) + 1);
 	}
 	return tf;
     }*/
 
     public PaperAbstract(int index, Integer[] inReferences, 
 			 Integer[] outReferences, 
 			 Integer[] abstractWords) {
     	this.index = index;
     	this.inReferences = inReferences;
     	this.outReferences = outReferences;
     	this.tf = new HashMap<Integer, Integer>();
     	for (Integer word : abstractWords) {
     	    if (!tf.containsKey(word))
     	    	tf.put(word, 1);
     	    else
     	    	tf.put(word, tf.get(word) + 1);
     	}
     }
 
 /*    public PaperAbstract(int index, Integer[] inReferences,
 			 Integer[] outReferences, Map<Integer, Integer> tf) {
 	this.index = index;
 	this.inReferences = inReferences;
 	this.outReferences = outReferences;
 	this.tf = tf;
     }*/
 
     /**
      * Generates tf depending on training or testing.  This function
      * must be called before we can use this paper in clustering
      * methods.
      */
     public void generateTf(double testWordpercent, Term[] terms, 
 			   boolean test){
 	Random randGen = Main.getRandomGenerator();
 	trainingTf = new HashMap<Integer, Integer>();
 	testingTf = test ? new HashMap<Integer, Integer>() : null;
 	norm = 0;
 
 	for (Integer word : tf.keySet()) {
 	    if (terms != null) terms[word].addDoc(this, test);
 	    int freq= tf.get(word);
 	    if (test && randGen.nextDouble() < testWordpercent) {
 		testingTf.put(word, freq);
 	    } else {
 		trainingTf.put(word, freq);
 		norm+=freq*freq;
 		if (!test && terms != null)
 		    terms[word].totalCount += tf.get(word);
 	    }
 	}
 
     	norm = Math.sqrt(norm);
     }
 
     public Integer getTrainingTf(Integer word) {
 	return trainingTf == null ? 0 : 
 	    (trainingTf.containsKey(word) ? trainingTf.get(word):0);
     }
 
     public Set<Integer> getTrainingWords() {
 	return trainingTf.keySet();
     }
 
     public Integer getTestingTf(Integer word) {
 	return testingTf == null ? 0 : 
	    (testingTf.containsKey(word)? testingTf.get(word)：0);
     }
 
     public Set<Integer> getTestingWords() {
 	return testingTf.keySet();
     }
 
     public double getNorm() { return norm; }
 
     public boolean isTest() { return testingTf != null; }
     
     public double similarity(PaperAbstract a){
     	double sim = 0.0;
 
 	for (Map.Entry<Integer, Integer> entry : trainingTf.entrySet()) {
 	    int wordId = entry.getKey();
 	    int count = entry.getValue();
 		sim += count * a.getTrainingTf(wordId);
     	}
     	return sim/(a.getNorm() * norm);
     }
 
     public boolean equals(Object obj) {
 	return obj instanceof PaperAbstract &&
 	    this.index == ((PaperAbstract)obj).index;
     }
 }
