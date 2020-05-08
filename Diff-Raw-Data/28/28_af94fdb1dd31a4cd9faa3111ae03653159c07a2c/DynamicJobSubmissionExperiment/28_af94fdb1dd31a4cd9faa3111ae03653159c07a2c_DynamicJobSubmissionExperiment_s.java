 package nl.tudelft.cloud_computing_project.experimental.allocation_benchmarks;
 
 import java.util.Random;
 
 import nl.tudelft.cloud_computing_project.experimental.SetupTestDataSet;
 
 public class DynamicJobSubmissionExperiment {
 
 	public static void main(String [] args) {
		dynamicJobSubmissionExperiment(1000);
 	}
 	
 	public static void dynamicJobSubmissionExperiment(int JobsToSubmitNumber) {
 		
 		int submittedJobsNumber = 0;
 		int sleepMinutes;
 		Random randomGenerator = new Random();
 		
 		while (submittedJobsNumber != JobsToSubmitNumber) {
 			
			SetupTestDataSet.addjobs(50);
			System.out.println("Added 50 jobs");
 			submittedJobsNumber += 50;
 			
 			try {
				sleepMinutes = randomGenerator.nextInt(7);
 				System.out.println("Sleep for: " + (sleepMinutes) + " min");
 				Thread.sleep((sleepMinutes) * 60000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			
 		}
 		
 	}
 	
 }
