 /**
  * Used to evaluate the user-based CF algorithm
  * 
  * Michael O'Mahony
  * 20/01/2011
  */
 
 package alg.ub;
 
 import java.io.File;
 
 import alg.ub.neighbourhood.*;
 import alg.ub.predictor.*;
 import similarity.metric.*;
 import util.evaluator.Evaluator;
 import util.reader.DatasetReader;
 
 public class ExecuteUB
 {
 	public static void main(String[] args)
 	{
 		// configure the user-based CF algorithm - set the predictor, neighbourhood and similarity metric ...
 		Predictor predictor = new MeanPredictor();
		Neighbourhood neighbourhood = new NearestNeighbourhood(53);
 		SimilarityMetric metric = new Cosine();
 		
 		// set the paths and filenames of the item file, train file and test file ...
 		String itemFile = "FRT dataset" + File.separator + "r.item";
 		String trainFile = "FRT dataset" + File.separator + "r.train";
 		String testFile = "FRT dataset" + File.separator + "r.probe";
 		
 		// set the path and filename of the output file ...
 		String outputFile = "results" + File.separator + "predictions.txt";
 		
 		////////////////////////////////////////////////
 		// Evaluates the CF algorithm (do not change!!):
 		// - the RMSE (if actual ratings are available) and coverage are output to screen
 		// - the output file is created
 		DatasetReader reader = new DatasetReader(itemFile, trainFile, testFile);
 		UserBasedCF ubcf = new UserBasedCF(predictor, neighbourhood, metric, reader);
 		Evaluator eval = new Evaluator(ubcf, reader.getTestData());
 		eval.writeResults(outputFile);
 		Double RMSE = eval.getRMSE();
 		if(RMSE != null) System.out.println("RMSE: " + RMSE);
 		double coverage = eval.getCoverage();
 		System.out.println("coverage: " + coverage + "%");
 	}
 }
 	
