 package edu.ntnu.idi;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.mahout.cf.taste.common.TasteException;
 import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
 import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
 import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
 import org.apache.mahout.cf.taste.impl.eval.AbstractDifferenceRecommenderEvaluator;
 import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator;
 import org.apache.mahout.cf.taste.impl.eval.RMSRecommenderEvaluator;
 import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
 import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
 import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
 import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
 import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
 import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;
 import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
 import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
 import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
 import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
 import org.apache.mahout.cf.taste.model.DataModel;
 import org.apache.mahout.cf.taste.model.Preference;
 import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
 import org.apache.mahout.cf.taste.recommender.RecommendedItem;
 import org.apache.mahout.cf.taste.recommender.Recommender;
 import org.apache.mahout.cf.taste.similarity.UserSimilarity;
 import org.apache.mahout.common.RandomUtils;
 import org.apache.mahout.math.hadoop.similarity.cooccurrence.measures.LoglikelihoodSimilarity;
 
 
 public class Mahout {
 	
 	public static List<RecommendedItem> getRecommendations(int numberOfRatings, int userId, Recommender recommender) throws TasteException{
 		Recommender cachingRecommender = new CachingRecommender(recommender);
 		
 		List<RecommendedItem> recommendations = cachingRecommender.recommend(userId, numberOfRatings);
 	
 		return recommendations;
 	}
 	
 	public static void printRecommendations(List<RecommendedItem> recommendations) {
 		System.out.println("");
 		
 		for (RecommendedItem recommendedItem : recommendations) {
 			System.out.format("Item recommended : %d with value : %f \n", recommendedItem.getItemID(), 
 					recommendedItem.getValue());
 		}
 		
 	}
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 * @throws TasteException 
 	 */
 	public static void main(String[] args) throws TasteException, IOException  {
 //		DataModel model = new GroupLensDataModel(new File("data/movielens-1m/ratings.dat.gz"));
 		
 		// creates a generic recommender with the ratings set from data and a neighborhood size of 50
 //		GenericUserBasedRecommender recommender = Algorithms.userBasedRecommender(model, 50, 
 //				new PearsonCorrelationSimilarity(model));
 //		GenericItemBasedRecommender recommender = Algorithms.itemBasedRecommender("data/movielens-1m/ratings.dat.gz", "pearson");
 //		SlopeOneRecommender recommender = Algorithms.slopeOneRecommender("data/movielens-1m/ratings.dat.gz");
 		
 		// uses the above recommender to get 20 recommendations (top-20) for user with id 33
 //		List<RecommendedItem> recommendations = getRecommendations(20, 33, recommender);
 		
 		// prints the recommendations to console
 //		printRecommendations(recommendations);
 		
 //		System.out.println("");
 		
 		/*
 		double[][] aadResults = AverageAbsoluteDifferenceEvaluation("nearestN");
 		double[][] rmsResults = RootMeanSquareEvaluation("nearestN");
 
 		System.out.println("Average Absolute  Difference Evaluation with nearest N neighborhood");
 		printEvaluations(aadResults);
 
 		System.out.println("");
 		
 		System.out.println("Root Mean Square with nearest N neighborhood");
 		printEvaluations(rmsResults);
 		*/
 		
 		String[][] papResults = precisionAndRecallEvaluation("nearestN");
 		System.out.println("Precision and recall with nearest N neighborhood");
 		printPrecisionAndRecallEvaluations(papResults);
 		
 		}
 	
 	public static double[][] doEvaluation(RecommenderEvaluator recommenderEvaluator, 
 			String neighborhoodType) throws IOException, TasteException {
 		DataModel model = new GroupLensDataModel(new File("data/movielens-1m/ratings.dat.gz"));
 		int neighborhoodSize = 1;
 		double[][] results = new double[4][8];
 		UserSimilarity[] similarityMetrics = {	new PearsonCorrelationSimilarity(model),
 												new EuclideanDistanceSimilarity(model),
 												new LogLikelihoodSimilarity(model),
 												new TanimotoCoefficientSimilarity(model)
 											};
 		
 		if( neighborhoodType.equals("nearestN") ) {
 			for (int i = 0; i < results.length; i++) {
 				for (int j = 0; j < results[0].length; j++) {
 					results[i][j] = Evaluation.evaluateNearestNNeighborhoodUserRecommender(model, neighborhoodSize, similarityMetrics[i], 
 							recommenderEvaluator, 0.9, 0.1);
 					neighborhoodSize = neighborhoodSize * 2;
 				}
 				neighborhoodSize = 1;
 				break;
 			}
 		} else {
 			results = new double[4][6];
 			double threshold = 0.95; 
 			
 			for (int i = 0; i < results.length; i++) {
 				for (int j = 0; j < results[0].length; j++) {
 					results[i][j] = Evaluation.evaluateThresholdNeighborhoodUserRecommender(model, threshold, 
 							similarityMetrics[i], recommenderEvaluator, 0.9, 0.1);
 					threshold -= 0.05;
 				}
 				threshold = 0.95;
 			}
 		}
 		
 		return results;
 	}
 	
 	public static double[][] AverageAbsoluteDifferenceEvaluation(String neighborhoodType) throws IOException, TasteException{
 		return doEvaluation(new AverageAbsoluteDifferenceRecommenderEvaluator(), neighborhoodType);
 	}
 	
 	public static double[][] RootMeanSquareEvaluation(String neighborhoodType) throws IOException, TasteException{
 		return doEvaluation(new RMSRecommenderEvaluator(), neighborhoodType);
 	}
 	
 	public static String[][] precisionAndRecallEvaluation(String neighborhoodType) throws IOException, TasteException{
		DataModel model = new GroupLensDataModel(new File("data/movielens-1m/ratings.dat.gz"));
 		int neighborhoodSize = 1;
 		String[][] results = new String[4][8];
 		UserSimilarity[] similarityMetrics = {	new PearsonCorrelationSimilarity(model),
 												new EuclideanDistanceSimilarity(model),
 												new LogLikelihoodSimilarity(model),
 												new TanimotoCoefficientSimilarity(model)
 											};
 		
 		if( neighborhoodType.equals("nearestN") ) {
 			for (int i = 0; i < results.length; i++) {
 				for (int j = 0; j < results[0].length; j++) {
					results[i][j] = Evaluation.evaluatePrecisionAndRecallWithNearestN(model, neighborhoodSize, similarityMetrics[i], 10);
 					neighborhoodSize = neighborhoodSize * 2;
 				}
 				neighborhoodSize = 1;
 				break;
 			}
 		} else {
 			results = new String[4][6];
 			double threshold = 0.95; 
 			
 			for (int i = 0; i < results.length; i++) {
 				for (int j = 0; j < results[0].length; j++) {
 					results[i][j] = Evaluation.evaluatePrecisionAndRecallWithThreshold(model, threshold, similarityMetrics[i], 10);
 					threshold -= 0.05;
 				}
 				threshold = 0.95;
 			}
 		}
 		
 		return results;
 	}
 	
 	public static void printEvaluations(double[][] evaluations) {
 		String[] similarityMetrics = {"Pearson", "Euclidean", "Log-likelihood", "Tanimoto"};
 		
 		for (int i = 0; i < evaluations.length; i++) {
 			System.out.print(similarityMetrics[i] + " : ");
 			for (int j = 0; j < evaluations[0].length; j++) {
 				System.out.print(evaluations[i][j] + " , ");
 			}
 			System.out.println("");
 		}
 	}
 	
 	public static void printPrecisionAndRecallEvaluations(String[][] evaluations) {
 		String[] similarityMetrics = {"Pearson", "Euclidean", "Log-likelihood", "Tanimoto"};
 			
 			for (int i = 0; i < evaluations.length; i++) {
 				System.out.print(similarityMetrics[i] + " : ");
 				for (int j = 0; j < evaluations[0].length; j++) {
 					System.out.print(evaluations[i][j] + " , ");
 				}
 				System.out.println("");
 			}
 	}
 	
 }
