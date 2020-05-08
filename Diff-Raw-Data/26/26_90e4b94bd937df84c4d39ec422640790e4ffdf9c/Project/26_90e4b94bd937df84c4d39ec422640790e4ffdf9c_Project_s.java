 package mlproject;
 
 import java.io.IOException;
 import java.util.Collection;
 
 import mlproject.abstractMath.impl.EuclideanMeasure;
 import mlproject.abstractMath.impl.EuclideanMeasureWeighted;
 import mlproject.dataimport.Importer;
 import mlproject.models.*;
 import mlproject.predictors.*;
 import mlproject.testing.*;
 
 
 
 public class Project {
 	
 	public static void main(String[] args){		
 		Collection<Issue> issues = null;
 		try {
 			issues = Importer.getIssues("/Users/matthew/Downloads/Consolidated.csv");
 			for(Issue issue: issues) {
 				System.out.println(issue.Issue);
 				System.out.println(issue.date);
 				System.out.println(issue.heading);
 				System.out.println(issue.astronomyAndCosmology? "*****************": "Not astonomy");
 				System.out.println("");
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
		DataLoader loader = new DataLoader(issues, 30);
 		
 // JUST FOR DEBUGGING
 //		System.out.println("# of issues: " + issues.size());
 //		System.out.println("# of test issues: " + loader.getTestData().size());
 //		System.out.println("# of training issues: " + loader.getTrainingData().size());
 //		
 		KNearestNeighbour knn = new KNearestNeighbour(new EuclideanMeasure(), 10); 
 		KNearestNeighbour knnW = new KNearestNeighbour(new EuclideanMeasureWeighted(), 10);
 		ExpectedSalesPredictor expSales = new ExpectedSalesPredictor();
 		
 		
 		
 		TestResults tester = new TestResults();
		System.out.println("10 nearest neighbor loss: " + tester.testPredictor(knn, loader, 200, 50));
		System.out.println("10 nearest neighbor weighted loss: " + tester.testPredictor(knnW, loader, 200, 50));
		System.out.println("current loss: " + tester.testPredictor(expSales, loader, 200, 50));
 	}
 	
 }
