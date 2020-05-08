 package scheduler;
 
 import java.io.*;
 
 /**
  * This is what we'll run to mark your code.
  * 
  * @author Frank Hutter
  */
 
 public class SchedulerTester {
 
 	/**
 	 * Java doesn't really have a good way of timing other than the system timer, so other CPU load will be a factor. Don't encode a DVD while you're
 	 * running this.
 	 * 
 	 * @param args
 	 * @throws Exception 
 	 */
 	public static void main(String[] args) throws Exception {
 		//=== We'll use the following 2 evaluators.
 		Evaluator evaluators[] = new Evaluator[2];
 		evaluators[0] = new OrigEvaluator();
 		evaluators[1] = new MyEvaluator();
 		
 		//=== We'll use the following 2 schedulers.
 		Scheduler[] schedulers = new Scheduler[2];
 		schedulers[0] = new GreedyDescentWithRestartsScheduler();
 		schedulers[1] = new FavouriteSLSScheduler();
 		
 		for (int i = 0; i < evaluators.length; i++) {
 			Evaluator evaluator = evaluators[i];
 			if (i==0) {
 				System.out.println("Using original evaluator\n");
 			} else {
 				System.out.println("Using improved evaluator\n");
 			}
 			
 			for (int j = 0; j < schedulers.length; j++) {
 				if (j==0) {
 					System.out.println("Running Greedy Descent with Restarts\n");
 				} else {
 					System.out.println("Running improved method\n");
 				}
 				Scheduler scheduler = schedulers[j];
 				
 				System.out.println("Authors:\n" + scheduler.authorsAndStudentIDs());
 	
 
 				// Easy instance with a single student taking a single course 
 				test(scheduler, evaluator, "instances/singleStudent1.txt", 0, 1, true);
 				
 				// Easy feasible instances with 4 rooms, 3 courses, 3 students, 2 timeslots
 				test(scheduler, evaluator, "instances/simple_1.txt", 0, 1, false);
 				test(scheduler, evaluator, "instances/simple_2.txt", 0, 1, false);
 				test(scheduler, evaluator, "instances/simple_3.txt", 0, 1, false);
 				test(scheduler, evaluator, "instances/simple_4.txt", 0, 1, false);
 				test(scheduler, evaluator, "instances/simple_5.txt", 0, 1, false);
 	
 				// Easy infeasible instance with 4 rooms, 3 courses, 3 students, 2 timeslots 
 				test(scheduler, evaluator, "instances/threeStudentsInfeasible.txt", 1, 5, true);
 				
 				// Small instances with 10 rooms, 10 courses, 30 students, 9 timeslots
 				test(scheduler, evaluator, "instances/small_1.txt", 0, 1, false);
 				test(scheduler, evaluator, "instances/small_2.txt", 0, 1, false);
 				test(scheduler, evaluator, "instances/small_3.txt", 0, 1, false);
 				test(scheduler, evaluator, "instances/small_4.txt", 0, 1, false);
 				test(scheduler, evaluator, "instances/small_5.txt", 0, 1, false);
 					
 
 				// Medium instances with 10 rooms, 30 courses, 250 students, 20 timeslots
 				test(scheduler, evaluator, "instances/medium_1.txt", 0, 5, false);
 				test(scheduler, evaluator, "instances/medium_2.txt", 0, 5, false);
 				test(scheduler, evaluator, "instances/medium_3.txt", 0, 5, false);
 				test(scheduler, evaluator, "instances/medium_4.txt", 0, 5, false);
 				test(scheduler, evaluator, "instances/medium_5.txt", 0, 5, false);
 				 
 				
 				// Large instances with 10 rooms, 50 courses, 500 students, 20 timeslots
 				test(scheduler, evaluator, "instances/large_1.txt", 0, 10, false);
 				test(scheduler, evaluator, "instances/large_2.txt", 0, 10, false);
 				test(scheduler, evaluator, "instances/large_3.txt", 0, 10, false);
 				test(scheduler, evaluator, "instances/large_4.txt", 0, 10, false);
 				test(scheduler, evaluator, "instances/large_5.txt", 0, 10, false);
 			      
 			}
 		}
 		
 		System.out.println("Tests finished");
 	}
 
 	/**
 	 * @param scheduler the scheduler;
 	 * @param instanceFilename name of the scheduling instance;
 	 * @param bestSolutionQual the best solution cost possible (or -1 if not revealed)
 	 * @param allowedTime the time allowed for SLS
 	 * @param verbose printing found solution and true solution quality
 	 * 
 	 * @throws IOException
 	 */
 	private static void test(Scheduler scheduler, Evaluator evaluator, String instanceFilename, int bestSolutionQual, float allowedTime, boolean verbose) throws IOException {
 		int numRuns = 5;	//=== For benchmarking SLS algorithms, we would usually use many more runs. 
 							//=== Here, we only use 5 runs per instance so we can run it for every submission.
 		long time = System.currentTimeMillis();
 		try {
 			System.out.println("Instance '" + instanceFilename + "': ");
 			SchedulingInstance instance = new SchedulingInstance(instanceFilename);
			int sumOfCosts = 0;
 			double avgCost;
 			
 			if (verbose){
 				//=== Just do one run and output its solution for debugging.
 				numRuns = 1;
 				ScheduleChoice[] foundSchedule = scheduler.schedule(evaluator, instance, System.currentTimeMillis(), allowedTime);
 				avgCost = new OrigEvaluator().violatedConstraints(instance, foundSchedule);
 
 				System.out.println("Best found schedule: ");
 				printSchedule(foundSchedule);
 			} else {
 				//=== Do multiple runs and measure the average cost of the found schedules.
 				for (int run=0; run<numRuns; run++){
 					ScheduleChoice[] foundSchedule = scheduler.schedule(evaluator, instance, System.currentTimeMillis(), allowedTime);
 					sumOfCosts += new OrigEvaluator().violatedConstraints(instance, foundSchedule);
 				}
 				avgCost = sumOfCosts / (numRuns+0.0);
 			}
 			
 			//System.out.print("Time: " + (System.currentTimeMillis() - time) / 1000 + "s, average solution cost found: " + avgCost);
 			System.out.print("Time: " + (System.currentTimeMillis() - time) + "ms, average solution cost found: " + avgCost);
 			if (avgCost <= bestSolutionQual + 0.001){
 				System.out.println("; optimal");					
 			} else {
 				System.out.println("; suboptimal (best solution cost is " + bestSolutionQual + ")");					
 			}
 
 		} catch (Exception e) {
 			System.out.println("Instance '" + instanceFilename + "': crashed " + e);
 		}
 		long actualTime = (System.currentTimeMillis() - time) / 1000;
 		if (actualTime > allowedTime*numRuns + 5){
 			System.out.println("SLS did not stop in time. It was allowed " + allowedTime + " seconds, but required " + actualTime + "\n");			
 		}
 	}
 
 	
 	/* You might like this function for debugging */
 	public static void printSchedule(ScheduleChoice[] s) {
 		for (int i = 0; i < s.length; i++) {
 			System.out.println("Course " + i + ":\t " + s[i].toString());
 		}
 	}
 }
