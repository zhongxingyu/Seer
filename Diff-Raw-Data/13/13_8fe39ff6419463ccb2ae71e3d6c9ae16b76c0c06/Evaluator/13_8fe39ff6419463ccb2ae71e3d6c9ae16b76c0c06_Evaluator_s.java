 package edu.ntnu.idi.goldfish;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.mahout.cf.taste.common.TasteException;
 import org.apache.mahout.cf.taste.eval.IRStatistics;
 import org.apache.mahout.cf.taste.eval.RecommenderIRStatsEvaluator;
 import org.apache.mahout.cf.taste.impl.eval.GenericRecommenderIRStatsEvaluator;
 import org.apache.mahout.cf.taste.model.DataModel;
 import org.apache.mahout.cf.taste.recommender.Recommender;
 import org.apache.mahout.common.distance.DistanceMeasure;
 
 public class Evaluator {
 
 	static long getRandomUser(DataModel dataModel) throws TasteException {
 		Iterator<Long> it = dataModel.getUserIDs();
 		return it.next();
 	}
 	
 	double relevanceThreshold = GenericRecommenderIRStatsEvaluator.CHOOSE_THRESHOLD;
 	
 	public Evaluator() {	
 	}
 	
 	public void evaluateClustered(int numClusters, DistanceMeasure dm, List<Evaluation> evaluations, EvaluationResults results, DataModel dataModel, double test) throws TasteException, IOException, InterruptedException, ClassNotFoundException {
 
 		StopWatch.start("clustering");
		DataModel[] dataModels = UserClusterer.clusterUsers(dataModel, numClusters, dm);
 		StopWatch.stop("clustering");
 		
 		int[] clusterSizes = new int[numClusters];
 		for(int i=0; i<numClusters; i++) {
 			clusterSizes[i] = dataModels[i].getNumUsers();
 		}
 		
 		System.out.format("Clustered in %s. %d clusters: %s \n", StopWatch.str("clustering"), numClusters, Arrays.toString(clusterSizes));
 		
 		StopWatch.start("clustereval");
 //		EvaluationResults clusterResults = new EvaluationResults();
 		for(int i=0; i<dataModels.length; i++) {
 			evaluateUnclustered(evaluations, results, dataModels[i], test);
 //			results.add(Result.getAverage(clusterResults));
 		}
		System.out.format("Evaluated %d configurations in %s \n", evaluations.size(), StopWatch.str("clustereval"));
 	}
 	
 	public void evaluateUnclustered(List<Evaluation> evaluations, EvaluationResults results, DataModel dataModel, double test) throws IOException, TasteException {
		System.out.format("Starting evaluation with %d configurations (%d users, %d items)\n", evaluations.size(), dataModel.getNumUsers(), dataModel.getNumItems());
 		
 		StopWatch.start("totaleval");
 		for(Evaluation evaluation : evaluations) {
 			results.add(evaluate(evaluation, dataModel, test, getRandomUser(dataModel)));
 		}
		System.out.format("Evaluated %d configurations in %s \n", evaluations.size(), StopWatch.str("totaleval"));
		
 	}
 	
 	Result evaluate(Evaluation evaluation, DataModel dataModel, double testFrac, long userID) throws TasteException {
 //		RMSE = new RMSRecommenderEvaluator();
 //		AAD = new AverageAbsoluteDifferenceRecommenderEvaluator();
 		RecommenderIRStatsEvaluator irEvaluator = new GenericRecommenderIRStatsEvaluator();
 		
 		double rmse = 0;
 		double aad = 0;
 		
 		// recTime
 		long recTime;
 		
 		StopWatch.start("evaluate");
 //		rmse = RMSE.evaluate(recommender.getBuilder(), null, dataModel, 1 - testFrac, test);
 //		aad = AAD.evaluate(recommender.getBuilder(), null, dataModel, 1 - testFrac, test);
 		IRStatistics stats = irEvaluator.evaluate(evaluation.getBuilder(), null, dataModel, null, evaluation.getTopN(), relevanceThreshold, testFrac);
 
 		StopWatch.start("build");
 		Recommender recommender = evaluation.getBuilder().buildRecommender(dataModel);
 		StopWatch.stop("build");
 		
 		recTime = getRecommendationTiming(recommender, 20, 10, userID);
 
//		System.out.format("%3ds: %s/%.2f (%s)\n", StopWatch.str("evaluate"), evaluation.toString(), evaluation.getKTL(), evaluation.getSimilarity());
 		return new Result(evaluation, rmse, aad, stats.getPrecision(), stats.getRecall(), StopWatch.get("build"), recTime);
 	}
 	
 	public long getRecommendationTiming(org.apache.mahout.cf.taste.recommender.Recommender rec, int N, int howMany, long userID) throws TasteException {
 		long totalDuration = 0;
 		for(int i=0; i< N; i++) {
 			StopWatch.start("recommend");
 			rec.recommend(userID, howMany);
 			totalDuration += StopWatch.get("recommend");
 		}
 		return totalDuration / N;
 	}
 	
 }
