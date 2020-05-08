 package mlproject;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import mlproject.abstractMath.VectorMaker;
 import mlproject.abstractMath.impl.EuclideanMetric;
 import mlproject.abstractMath.vectorMaker.AverageColorVectorMaker;
 import mlproject.abstractMath.vectorMaker.ColorHistogramVectorMaker;
 import mlproject.abstractMath.vectorMaker.PolynomialVectorMaker;
 import mlproject.dataimport.Importer;
 import mlproject.models.Issue;
 import mlproject.predictors.ExpectedSalesPredictor;
 import mlproject.predictors.KMeansPredictor;
 import mlproject.predictors.KNearestNeighbour;
 import mlproject.predictors.LinearRegressionPredictor;
 import mlproject.predictors.LogisticRegressionPredictor;
 import mlproject.predictors.SumOfGaussianPredictor;
 import mlproject.predictors.estimators.TimePredictorSeasonal;
 import mlproject.testing.BatchPredictionResults;
 import mlproject.testing.DataLoader;
 import mlproject.testing.DataSetType;
 import mlproject.testing.PredictorTester;
 
 public class Project {
 	
 	public final static ISalesPredictor expectedSalesPredictor = new TimePredictorSeasonal(2, 8.15);
 	
 	public static void main(String[] args){
 		Collection<Issue> issues = loadIssues();
 		expectedSalesPredictor.Train(issues);
 		
 		double totalDev = 0;
 		double totalSqDev = 0;
 		for(Issue issue: issues) {
 			double prediction = expectedSalesPredictor.Predict(issue);
 			double deviation = prediction - issue.getLogSales();
 			totalDev += Math.abs(deviation);
 			totalSqDev += deviation*deviation;
 		}
 		System.out.println("Average Absolute Error: " + totalDev/issues.size());
		System.out.println("Standard Deviation: " + Math.sqrt(totalSqDev/issues.size()));
 		
 //		try{
 //			File f = new File("/Users/matthew/mapping.txt");
 //			BufferedWriter o = new BufferedWriter(new FileWriter(f));
 //			for(Issue issue: issues){
 //				o.write(issue.imageFile + " " + issue.getDirection() + "\n");
 //			}
 //			o.close();
 //		}catch(Exception e){}
 //
 //		if(true) return;
 //		
 
 		System.out.println("done loading issues");
 		
 		Issue predictMe = new Issue();
		predictMe.date = new Date(System.currentTimeMillis());
 		System.out.println("Current Date Prediction: " + expectedSalesPredictor.Predict(predictMe));
 		System.out.println();
 		try {
 			predictMe.extractImageFeatures("./data/");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		DataLoader loader = new DataLoader(issues, 0); //% test samples.
 
 		//Remove this to actually run things
 		//if (true) return;
 		
 		List<ISalesPredictor> fastPredictors = getFastPredictors();
 		System.out.println("fetched " + fastPredictors.size() + " fast predictor combinations");
 
 		List<ISalesPredictor> slowPredictors = getSlowPredictors();
 		System.out.println("fetched " + slowPredictors.size() + " slow predictor combinations");
 		
 		List<ISalesPredictor> allPredictors = new ArrayList<ISalesPredictor>();
         allPredictors.addAll(fastPredictors);
         allPredictors.addAll(slowPredictors);
        double total = 0.0;
         double result = 0.0;
         for(ISalesPredictor predictor: allPredictors) {
             predictor.Train(issues);
             
             result = predictor.Predict(predictMe);
             if(result > 0) result = 1.0;
             if(result <= 0) result = -1.0;
             System.out.println("predicted " + result);
             total += result;
         }
         total = total / allPredictors.size();
         System.out.println("average: " + total);
         
         
			
		        // 
        
         PredictorTester tester = new PredictorTester(expectedSalesPredictor);
         final Map<ISalesPredictor, Map<DataSetType, BatchPredictionResults>> results = 
         	new HashMap<ISalesPredictor, Map<DataSetType, BatchPredictionResults>>();
 		System.out.println("Testing Fast Predictors");
 		for(ISalesPredictor predictor: fastPredictors) {
 		   results.put(predictor, tester.testPredictor(predictor, loader));
 		}
 		       
 		       System.out.println("Testing Slow Predictors");
 		       for(ISalesPredictor predictor: slowPredictors) {
 		        results.put(predictor, tester.testPredictorShort(predictor, loader, 50));
 		       }
 		
 
 		// 
 		//        //Print out the results.
 		        for(ISalesPredictor predictor: allPredictors) {
 		            Map<DataSetType, BatchPredictionResults> thisResult = results.get(predictor);
 		            System.out.println(predictor.name());
 		            for(DataSetType dst: thisResult.keySet()) {
 		                System.out.println(dst.toString() + " data: ");
 		                System.out.println(" - Average Loss = " + thisResult.get(dst).averageLoss);
 		                System.out.println(" - Direction Success: " + thisResult.get(dst).numCorrectDirection + " / " + thisResult.get(dst).totalChecked);
 		            }
 		            System.out.println("");
 		        }
 		// 
 		        Collections.sort(allPredictors, new Comparator<ISalesPredictor>() {
 		            @Override public int compare(ISalesPredictor p1, ISalesPredictor p2) {
 		                double l1 = results.get(p1).get(DataSetType.TEST).averageLoss;
 		                double l2 = results.get(p2).get(DataSetType.TEST).averageLoss;
 		                return Utils.sign(l1 - l2);
 		            }
 		        });
 		    
 		        System.out.println("Predictors in Order of Average Loss");
 		        for(ISalesPredictor predictor: allPredictors) {
 		            System.out.println(predictor.name() + " (" + results.get(predictor).get(DataSetType.TEST).averageLoss + ")");
 		        }
 		        System.out.println("");
 		        
 		        Collections.sort(allPredictors, new Comparator<ISalesPredictor>() {
 		            @Override public int compare(ISalesPredictor p1, ISalesPredictor p2) {
 		                double l1 = results.get(p1).get(DataSetType.TEST).directionalSuccessRate();
 		                double l2 = results.get(p2).get(DataSetType.TEST).directionalSuccessRate();
 		                return Utils.sign(l2 - l1);
 		            }
 		        });
 		        
 		        System.out.println("Predictors in Order of Directional Success");
 		        for(ISalesPredictor predictor: allPredictors) {
 		            System.out.println(predictor.name() + " (" + results.get(predictor).get(DataSetType.TEST).directionalSuccessRate() + ")");
 		        }
 		        System.out.println("");
 		        
 	}
 	
 	static public Collection<Issue> loadIssues() {
 		Collection<Issue> issues = null;
 		try {
 			System.out.println("Loading issues from csv....");
 			
 			File[] images = null;
 			
 			File testEnv = new File("/Users/matthew/");
 			issues = Importer.getIssues("./data/ns.csv");
 //			if (testEnv.exists()) {
 //				issues = Importer.getIssues("/Users/matthew/Downloads/Consolidated.csv");
 //				images = Importer.getImages("/Users/matthew/Pictures/cover_images/");
 //			} else {
 //				//issues = Importer.getIssues("/home/mes592/Desktop/Consolidated.csv");
 //				issues = Importer.getIssues("/home/mes592/New Scientist.csv");
 //				images = Importer.getImages("/home/mes592/images/cover_images/");
 //			}
 			
 			for(Issue i : issues){
 				try{
 					i.extractImageFeatures("./data/images/");
 				}catch(IOException e){
 					System.out.println("could not get image data for issue " + i.dateString);
 				}catch(Exception e){
 					System.out.println(e);
 				}
 				
 				
 			}
 			
 			
 
 //			HashMap<File, Date> dateMappings = Importer.extractIssueDates(images);
 //			System.out.println("Extracting image features...");
 //			Set<String> errorSet = new HashSet<String>();
 //			for(Issue issue: issues) {
 //				System.out.print(".");
 //				for(File image : images){
 //					if(issue.shouldOwn(dateMappings.get(image))){
 //						try {
 //							issue.extractImageFeatures(image.getAbsolutePath());
 //							//System.out.println("Log Odds RGB avg: " + issue.logOddsAvgRed + " " + issue.logOddsAvgGreen + " " + issue.logOddsAvgBlue);
 //						} catch(IOException e){
 //							errorSet.add(image.getName() + " Issue: " + issue.Issue);
 //						}
 //						break;
 //					}
 //				}
 //			}
 			
 //			System.out.println("Errors in the following images:");
 //			for(String err: errorSet) System.out.println(err);
 			System.out.println();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return issues;
 	}
 	
 	private static List<ISalesPredictor> getSlowPredictors() {
 		List<VectorMaker<Issue>> vectorMakers = VectorMakerLists.getBaseVMs();
 		List<ISalesPredictor> slowPredictors = new ArrayList<ISalesPredictor>();
 		
 		return slowPredictors; //Return nothing
 		/*
 		double[] ridges = {0.2, 0.1, .05, .02};
 		
 		for(VectorMaker<Issue> vectorMaker: vectorMakers) {
 			for(int k = 2; k < 5; k++) {
 				slowPredictors.add(new KMeansPredictor(k, vectorMaker, "VectorMaker: " + vectorMaker.name()));
 				slowPredictors.add(new KNearestNeighbour(new EuclideanMetric(vectorMaker), k,""));
 			}
 
 			for(double ridge : ridges){
 				//slowPredictors.add(new LogisticRegressionPredictor(ridge, vectorMaker));
 				slowPredictors.add(new LinearRegressionPredictor(ridge, vectorMaker));
 			}
 
 		}
 		
 		return slowPredictors;*/
 	}
 
 	public static List<ISalesPredictor> getFastPredictors() {
 		List<VectorMaker<Issue>> vectorMakers = VectorMakerLists.getBaseVMs();
 		List<ISalesPredictor> fastPredictors = new ArrayList<ISalesPredictor>();
 
 		VectorMaker<Issue> acvm = new AverageColorVectorMaker();
 		VectorMaker<Issue> chvm = new ColorHistogramVectorMaker();
 		
 		//double[] standardDevs = {0.1, 0.2, 0.5, 1, 2, 5, 10, 30, 100};
 		double[] standardDevs = {0.5,  2, 10};
 		for(int i = 0; i < standardDevs.length; i++) {
 			fastPredictors.add(new SumOfGaussianPredictor(acvm, standardDevs[i], expectedSalesPredictor));
 			fastPredictors.add(new SumOfGaussianPredictor(chvm, standardDevs[i], expectedSalesPredictor));
 		}
 		
 		//double[] ridges = {0.5, 0.2, 0.1, 0.01, 0.001};
 		double[] ridges = {0.2, 0.1, 0.005};
 		
 		fastPredictors.add(new KMeansPredictor(3, acvm, "VectorMaker: " + acvm.name(), expectedSalesPredictor));
 		fastPredictors.add(new KMeansPredictor(5, acvm, "VectorMaker: " + acvm.name(), expectedSalesPredictor));
 
 		fastPredictors.add(new KMeansPredictor(3, chvm, "VectorMaker: " + chvm.name(), expectedSalesPredictor));
 		fastPredictors.add(new KMeansPredictor(5, chvm, "VectorMaker: " + chvm.name(), expectedSalesPredictor));
 
 		for(VectorMaker<Issue> vectorMaker: vectorMakers) {
 			for(int k = 2; k < 5; k++) {
 				//fastPredictors.add(new KMeansPredictor(k, vectorMaker, "VectorMaker: " + vectorMaker.name(), expectedSalesPredictor));
 				fastPredictors.add(new KNearestNeighbour(new EuclideanMetric(vectorMaker), k, "Euclidean", expectedSalesPredictor));
 			}
 			
 			for(double ridge : ridges){
 				fastPredictors.add(new LogisticRegressionPredictor(ridge, vectorMaker, expectedSalesPredictor));
 				//fastPredictors.add(new LinearRegressionPredictor(ridge, vectorMaker, expectedSalesPredictor));
 			}
 		}
 		
 		fastPredictors.add(new LogisticRegressionPredictor(0.2, 
 			new PolynomialVectorMaker<Issue>(3, acvm), expectedSalesPredictor));
 	
 		fastPredictors.add(new LogisticRegressionPredictor(0.2, chvm, expectedSalesPredictor));
 
 		//VectorMaker<Issue> pvm = new PolynomialVectorMaker<Issue>(3, chvm);
 		//fastPredictors.add(new LinearRegressionPredictor(0.2, pvm, expectedSalesPredictor));
 
 		fastPredictors.add(new ExpectedSalesPredictor(expectedSalesPredictor));
 		
 		return fastPredictors;
 
 	}
 	
 }
