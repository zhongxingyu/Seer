 package edu.ntnu.idi.goldfish;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import org.apache.mahout.cf.taste.common.TasteException;
 import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
 import org.apache.mahout.cf.taste.model.DataModel;
 
 import edu.ntnu.idi.goldfish.MemoryBased.Similarity;
 
 
 public class Main {
 	
 	// disable Mahout logging output
 	static { System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog"); }
 	
 	/**
 	 * @param args
 	 * @throws IOException 
 	 * @throws TasteException 
 	 */
 	public static void main(String[] args) throws IOException, TasteException {
 		
 		//DataModel model = new GroupLensDataModel(new File("data/movielens-1m/ratings.dat.gz"));
		DataModel dataModel = new GroupLensDataModel(new File("data/sample100/ratings.dat"));
 		
 		Evaluator evaluator = new Evaluator(dataModel);
 		
 		
 		
 		/*
 		 * MEMORY-based evaluation 
 		 * 
 		 * Evaluate KNN and Threshold based neighborhood models.
 		 * Try all different similarity metrics found in ModelBased.Similarity.
 		 */
 		for(Similarity similarity : Similarity.values()) {
 			
 			// KNN: try different neighborhood sizes
 			for(int K = 7; K >= 1; K -= 1) {
 				evaluator.add(new KNN(similarity, K));			
 			}
 			
 			// THRESHOLD: try different thresholds
 			for(double T = 0.70; T <= 1.00; T += 0.05) {
 				evaluator.add(new Threshold(similarity, T));
 			}
 		}
 		
 		evaluator.add(new SVD());
 
 		ArrayList<EvaluationResult> results = evaluator.evaluateAll();
 		
 		// sort on RMSE (lower is better)
 		Collections.sort(results, new Comparator<EvaluationResult>() {  
 			public int compare(EvaluationResult a, EvaluationResult b) {
 				return (a.RMSE > b.RMSE) ? -1 : (a.RMSE < b.RMSE) ? 1 : 0;
 			}
 		});
 		
 		for(EvaluationResult res : results) {
 			System.out.println(res);
 		}
 
 		/**
 		 * MODEL-based evaluation
 		 */
 		
 		// clustering models (KMeans ... EM?)
 		
 		// latent semantic models (Matrix Factorizations, etc..)
 		
 		
 		
 	}
 }
