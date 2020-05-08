 package edu.ntnu.idi;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.mahout.cf.taste.common.TasteException;
 import org.apache.mahout.cf.taste.eval.IRStatistics;
 import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
 import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
 import org.apache.mahout.cf.taste.eval.RecommenderIRStatsEvaluator;
 import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
 import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator;
 import org.apache.mahout.cf.taste.impl.eval.GenericRecommenderIRStatsEvaluator;
 import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
 import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
 import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
 import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
 import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
 import org.apache.mahout.cf.taste.model.DataModel;
 import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
 import org.apache.mahout.cf.taste.recommender.Recommender;
 import org.apache.mahout.cf.taste.similarity.UserSimilarity;
 import org.apache.mahout.common.RandomUtils;
 
 public class Evaluation {
 	
 	public static void evaluateRecommender() throws IOException, TasteException {
 		RandomUtils.useTestSeed();
 		DataModel model = new GroupLensDataModel(new File("data/movielens-1m/ratings.dat.gz"));
 		
 		RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
 		
 		RecommenderBuilder recommenderBuilder = new RecommenderBuilder() {
 			
 			public Recommender buildRecommender(DataModel model) throws TasteException {
 				UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
 				UserNeighborhood neighborhood = new NearestNUserNeighborhood(100, similarity, model);
 				return new GenericUserBasedRecommender(model, neighborhood, similarity);
 				}
 			};
 		
 		double score = evaluator.evaluate(recommenderBuilder, null, model, 0.8, 0.2);
 		System.out.println(score);
 	}
 	
 	public static double evaluateNearestNNeighborhoodUserRecommender(DataModel dataModel, final int neighborhoodSize, 
 			final UserSimilarity userSimilarity, RecommenderEvaluator evaluator, double trainSet,
 			double testSet) throws IOException, TasteException {
 		
 		RandomUtils.useTestSeed();
 		
 		RecommenderEvaluator recommenderEvaluator = evaluator;
 		DataModel model = dataModel;
 		
 		RecommenderBuilder recommenderBuilder = new RecommenderBuilder() {
 			
 			public Recommender buildRecommender(DataModel model) throws TasteException {
 				UserSimilarity similarity = userSimilarity;
 				UserNeighborhood neighborhood = new NearestNUserNeighborhood(neighborhoodSize, similarity, model);
 				return new GenericUserBasedRecommender(model, neighborhood, similarity);
 			}
 		};
 		
 		return recommenderEvaluator.evaluate(recommenderBuilder, null, model, trainSet, testSet);
 	}
 	
 	public static double evaluateThresholdNeighborhoodUserRecommender(DataModel dataModel, final double threshold, 
 			final UserSimilarity userSimilarity, RecommenderEvaluator evaluator, double trainSet,
 			double testSet) throws IOException, TasteException {
 		
 		RandomUtils.useTestSeed();
 		
 		RecommenderEvaluator recommenderEvaluator = evaluator;
 		DataModel model = dataModel;
 		
 		RecommenderBuilder recommenderBuilder = new RecommenderBuilder() {
 			
 			public Recommender buildRecommender(DataModel model) throws TasteException {
 				UserSimilarity similarity = userSimilarity;
 				UserNeighborhood neighborhood = new ThresholdUserNeighborhood(threshold, similarity, model);
 				return new GenericUserBasedRecommender(model, neighborhood, similarity);
 			}
 		};
 		
 		return recommenderEvaluator.evaluate(recommenderBuilder, null, model, trainSet, testSet);
 	}
 	
 	public static String evaluatePrecisionAndRecallWithThreshold(DataModel dataModel, final double threshold, 
 			final UserSimilarity userSimilarity, int at) throws IOException, TasteException {
 		
 		RandomUtils.useTestSeed();
 		
 		RecommenderIRStatsEvaluator evaluator = new GenericRecommenderIRStatsEvaluator();
 		DataModel model = dataModel;
 		
 		RecommenderBuilder recommenderBuilder = new RecommenderBuilder() {
 			
 			public Recommender buildRecommender(DataModel model) throws TasteException {
 				UserSimilarity similarity = userSimilarity;
 				UserNeighborhood neighborhood = new ThresholdUserNeighborhood(threshold, similarity, model);
 				return new GenericUserBasedRecommender(model, neighborhood, similarity);
 			}
 		};
 		
 		IRStatistics stats = evaluator.evaluate(recommenderBuilder, null, model, null, at, 
 				GenericRecommenderIRStatsEvaluator.CHOOSE_THRESHOLD, 1.0);
 		
 		String results = "Precision: " + stats.getPrecision() + " , recall: " + stats.getRecall();
 		return results;
 	}
 	
 	public static String evaluatePrecisionAndRecallWithNearestN(DataModel dataModel, final int neighborhoodSize, 
 			final UserSimilarity userSimilarity, int at) throws IOException, TasteException {
 		
 		RandomUtils.useTestSeed();
 		
 		RecommenderIRStatsEvaluator evaluator = new GenericRecommenderIRStatsEvaluator();
 		DataModel model = dataModel;
 		
 		RecommenderBuilder recommenderBuilder = new RecommenderBuilder() {
 			
 			public Recommender buildRecommender(DataModel model) throws TasteException {
 				UserSimilarity similarity = userSimilarity;
 				UserNeighborhood neighborhood = new NearestNUserNeighborhood(neighborhoodSize, similarity, model);
 				return new GenericUserBasedRecommender(model, neighborhood, similarity);
 			}
 		};
 		
 		IRStatistics stats = evaluator.evaluate(recommenderBuilder, null, model, null, at, 
				GenericRecommenderIRStatsEvaluator.CHOOSE_THRESHOLD, 0.5);
 		
 		String results = "Precision: " + stats.getPrecision() + " , recall: " + stats.getRecall(); 
 		return results;
 	}
 	
 	public static RecommenderEvaluator selectEvaluator (String evaluator) {
 		RecommenderEvaluator recEv = null;
 		if (evaluator.equals("AverageAbsoluteDifference")) {
 			recEv = new AverageAbsoluteDifferenceRecommenderEvaluator();
 		}
 		
 		return recEv;
 	}
 
 }
