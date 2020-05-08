 package mlproject;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import mlproject.models.Issue;
 import mlproject.predictors.estimators.OriginalEstimator;
 import mlproject.predictors.estimators.TimePolynomialEstimator;
 import mlproject.predictors.estimators.TimePredictorSeasonal;
 import mlproject.testing.DataLoader;
 import mlproject.testing.SalesEstimateTester;
 
 /**
  * Predicting Sales based on the date. 
  * @author mes592
  */
 public class ProjectFindBestTimeEstimator {
 	
 	public static void main(String[] args){
 		Collection<Issue> issues = Project.loadIssues();
 
 		DataLoader loader = new DataLoader(issues, 0); //% test samples.
 		
 		//These are predictors that predict log sales based only on the date
 		List<ISalesPredictor> predictors = getPredictors();	
 
         
 		SalesEstimateTester tester = new SalesEstimateTester();
         final Map<ISalesPredictor, Double> results = new HashMap<ISalesPredictor, Double>();
 		System.out.println("Testing Sales Estimators Predictors");
 		for(ISalesPredictor predictor: predictors) {
 		   results.put(predictor, tester.testPredictor(predictor, loader));
 		}
 		
 		//Print out the results.
 		for(ISalesPredictor predictor: predictors) {
 		     Double thisResult = results.get(predictor);
 	         System.out.println(" - Average Loss = " + thisResult);
 		}
 		
         Collections.sort(predictors, new Comparator<ISalesPredictor>() {
             @Override public int compare(ISalesPredictor p1, ISalesPredictor p2) {
                 double l1 = results.get(p1);
                 double l2 = results.get(p2);
                 return Utils.sign(l1 - l2);
             }
         });
     
         System.out.println("Predictors in Order of Average Loss");
         for(ISalesPredictor predictor: predictors) {
             System.out.println(predictor.name() + " (" + results.get(predictor) + ")");
         }
         System.out.println("");		        
 	}
 
 	public static List<ISalesPredictor> getPredictors() {
 		List<ISalesPredictor> ps = new ArrayList<ISalesPredictor>();
 
 		ps.add(new TimePolynomialEstimator(0));
 		ps.add(new TimePolynomialEstimator(1));
 		ps.add(new TimePolynomialEstimator(2));
 		ps.add(new TimePolynomialEstimator(3));
 		ps.add(new TimePredictorSeasonal(2, 0));
 		ps.add(new TimePredictorSeasonal(2, 2));
 		ps.add(new TimePredictorSeasonal(2, 4));
 		ps.add(new TimePredictorSeasonal(2, 8));
 		ps.add(new TimePredictorSeasonal(1, 8.15));
		ps.add(new TimePredictorSeasonal(2, 8.15));  //**This is the best one!!!!
 		ps.add(new TimePredictorSeasonal(3, 8.15));
 		ps.add(new TimePredictorSeasonal(2, 16));
 		ps.add(new TimePredictorSeasonal(2, 32));
 		ps.add(new TimePredictorSeasonal(2, 64));
 		ps.add(new OriginalEstimator());
 		
 		return ps;
 
 	}
 	
 }
