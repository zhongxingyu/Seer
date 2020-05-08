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

    public PaperAbstract(int index, Integer[] inReferences, 
			 Integer[] outReferences, 
			 Integer[] abstractWords) {
 	Map<Integer, Integer> tf = new HashMap<Integer, Integer>();
 	for (Integer word : abstractWords) {
 	    if (!tf.containsKey(word))
 		tf.put(word, 0);
 	    tf.put(word, tf.get(word) + 1);
 	}
	this(index, inReferences, outReferences, tf);
     }
 
     public PaperAbstract(int index, Integer[] inReferences,
 			 Integer[] outReferences, Map<Integer, Integer> tf) {
 	this.index = index;
 	this.inReferences = inReferences;
 	this.outReferences = outReferences;
 	this.tf = tf;
     }
 
     /**
      * Generates tf depending on training or testing.  This function
      * must be called before we can use this paper in clustering
      * methods.
      */
     public void generateTf(double percentUsed, Terms.Term[] terms, 
 			   boolean test){
 	Random randGen = Main.getRandomGenerator();
 	trainingTf = new HashMap<Integer, Integer>();
 	testingTf = test ? new HashMap<Integer, Integer>() : null;
 
 	for (Integer word : tf.keySet()) {
 	    if (terms != null) terms[word].addDoc(this, test);
 	    if (test && randGen.nextDouble() > percentUsed) {
 		testingTf.put(word, tf.get(word));
 	    } else {
 		trainingTf.put(word, tf.get(word));
 		if (!test && terms != null)
 		    terms[word].totalCount += tf.get(word);
 	    }
 	}
 
     	for (Map.Entry<Integer, Integer> entry : trainingTf.entrySet()) {
 	    norm += entry.getValue() * entry.getValue();
     	}
     	norm = Math.sqrt(norm);
     }
 
     public Integer getTrainingTf(Integer word) {
 	return trainingTf == null ? 0 : 
 	    (trainingTf.get(word) == null ? 0 : trainingTf.get(word));
     }
 
     public Set<Integer> getTrainingWords() {
 	return trainingTf.keySet();
     }
 
     public Integer getTestingTf(Integer word) {
 	return testingTf == null ? 0 : 
 	    (testingTf.get(word) == null? 0 : testingTf.get(word));
     }
 
     public Set<Integer> getTestingWords() {
 	return testingTf.keySet();
     }
 
     public double getNorm() { return norm; }
 
     public boolean isTest() { return testingTf != null; }
     
     public double similarity(PaperAbstract a){
     	double dist = 0.0;
 
 	for (Map.Entry<Integer, Integer> entry : trainingTf.entrySet()) {
 	    int wordId = entry.getKey();
 	    int count = entry.getValue();
 	    if (a.trainingTf.containsKey(wordId)) {
 		dist += count * a.trainingTf.get(wordId);
 	    }
     	}
     	return dist/(a.norm * norm);
     }
 
     public boolean equals(Object obj) {
 	return obj instanceof PaperAbstract &&
 	    this.index == ((PaperAbstract)obj).index;
     }
 }
