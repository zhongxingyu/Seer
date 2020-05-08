 package edu.ntnu.idi.goldfish;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.mahout.cf.taste.common.TasteException;
 import org.apache.mahout.cf.taste.eval.IRStatistics;
 import org.apache.mahout.cf.taste.eval.RecommenderIRStatsEvaluator;
 import org.apache.mahout.cf.taste.impl.eval.GenericRecommenderIRStatsEvaluator;
 import org.apache.mahout.cf.taste.model.DataModel;
 import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.common.RandomWrapper;
 import org.apache.mahout.common.distance.DistanceMeasure;
 
 public class Evaluator {
 
 	static long getRandomUser(DataModel dataModel) throws TasteException {
		RandomWrapper random = RandomUtils.getRandom();
 		Iterator<Long> it = dataModel.getUserIDs();
		while (it.hasNext()) {
			if (random.nextDouble() >= 0.1) {
				continue;
			}
			return it.next();
		}
		return 0;
 	}
 	
 	double relevanceThreshold = GenericRecommenderIRStatsEvaluator.CHOOSE_THRESHOLD;
 	
 	public Evaluator() {	
 	}
 	
 	public void evaluateClustered(int numClusters, DistanceMeasure measure, List<Evaluation> evaluations, EvaluationResults results, DataModel dataModel, double test) throws TasteException, IOException, InterruptedException, ClassNotFoundException {
 
 		DataModel[] dataModels = KMeansWrapper.clusterUsers(dataModel, numClusters, measure, 0.5, 10, true, 0.0, true);
 		
 		StopWatch.start("totaleval");
 		EvaluationResults clusterResults;
 		for(Evaluation evaluation : evaluations) {
 			long user = getRandomUser(dataModel);
 			clusterResults = new EvaluationResults();
 			StopWatch.start("cluster-evaluation");
 			for(int i=0; i<dataModels.length; i++) {
 				clusterResults.add(evaluate(evaluation, dataModel, test, user));
 			}
 			Result average = Result.getAverage(clusterResults);
 			System.err.format(average+" (in %s) \n", StopWatch.str("cluster-evaluation"));
 			results.add(average);
 		}
 		System.out.format("Evaluated %d clusters with %d configurations in %s\n", numClusters, evaluations.size(), StopWatch.str("totaleval"));
 	}
 	
 	public void evaluateUnclustered(List<Evaluation> evaluations, EvaluationResults results, DataModel dataModel, double test) throws IOException, TasteException {
 		System.out.format("Starting evaluation of %d configurations (%d users, %d items) \n", evaluations.size(), dataModel.getNumUsers(), dataModel.getNumItems());
 		StopWatch.start("totaleval");
 		for(Evaluation evaluation : evaluations) {
 			results.add(evaluate(evaluation, dataModel, test, getRandomUser(dataModel)));
 		}
 		System.out.format("Evaluated %d configurations (%d users, %d items) in %s \n", evaluations.size(), dataModel.getNumUsers(), dataModel.getNumItems(), StopWatch.str("totaleval"));
 	}
 	
 	Result evaluate(Evaluation evaluation, DataModel dataModel, double testFrac, long userID) throws TasteException {
 		StopWatch.start("evaluate");
 		
 //		RMSRecommenderEvaluator RMSE = new RMSRecommenderEvaluator();
 //		AAD = new AverageAbsoluteDifferenceRecommenderEvaluator();
         RecommenderIRStatsEvaluator irEvaluator = new GenericRecommenderIRStatsEvaluator();
 		
 		double rmse = 0;
 		double aad = 0;
 		
 		// recTime
 		long recTime;
 		
 //		rmse = RMSE.evaluate(recommender.getBuilder(), null, dataModel, 1 - testFrac, test);
 //		aad = AAD.evaluate(recommender.getBuilder(), null, dataModel, 1 - testFrac, test);
         IRStatistics stats = irEvaluator.evaluate(evaluation.getBuilder(), null, dataModel, null, evaluation.getTopN(), relevanceThreshold, testFrac);
 
 		StopWatch.start("build");
 		Recommender recommender = evaluation.getBuilder().buildRecommender(dataModel);
 		StopWatch.stop("build");
 		
 		recTime = getRecommendationTiming(recommender, 20, 10, userID);
 
 		Result result = new Result(evaluation, rmse, aad, stats.getPrecision(), stats.getRecall(), StopWatch.get("build"), recTime);
 
 		System.out.format("%s | Evaluated in %s \n", result, StopWatch.str("evaluate"));
 		return result;
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
