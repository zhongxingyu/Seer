 import java.util.ArrayList;
 import java.util.PriorityQueue;
 
 /**
 	Priority Queues Driver
  	@author Tom Ravenscroft
 	@version version 1.0
 	@see @link dispatcher.java Process dispatcher
 	@see @link queue_1.java High priority queue
 	@see @link queue_2.java Low priority queue
  */
 
 // TODO: Add a "wait" condition when no processes have arrived yet.
 // TODO: Reset the age when a process is promoted.
 // TODO: Ready time calcualtion should be firstRun - arrivalTime. (Not just the firstRun time).
 
 public class driver {
 	
 	/*
 		Driver method
 	*/
 	public static void main(String[] args) {
 	
 		// Splash
 		System.out.println("\nDriver online ...");
 		System.out.println("-v => verbose mode");
 		System.out.println("-d => debug mode");
 		System.out.println("-dv => verbose debug mode");
 			
 // ---------------------------------------------------------------------------------------------------------
 // Variables
 // ---------------------------------------------------------------------------------------------------------
 
 		// Test files.
 		String file1 = "../tests/test1.txt";
 		String file2 = "../tests/test2.txt";
 		String file3 = "../tests/test3.txt";
 		String file4 = "../tests/test4.txt";
 		Proc[] test1;
 		Proc[] test2;
 		Proc[] test3;
 		Proc[] test4;
 
 		// Dispatcher variables.
 		int quantum1 = 10;
 		int quantum2 = 20;
 		boolean debug = false;
 		boolean verbose = false;
 	
 // ---------------------------------------------------------------------------------------------------------
 // Handle flags
 // ---------------------------------------------------------------------------------------------------------
 
 		if(args.length > 0)
 		{
 			System.out.println();
 			if(args[0].equals("-d") || args[0].equals("-dv") || args[0].equals("-vd")){
 				System.out.println("***** DEBUG MODE *****");
 				debug = true;
 			}
 			
 			if((args[0].equals("-v") || args[0].equals("-dv") || args[0].equals("-vd"))){
 				System.out.println("***** VERBOSE MODE *****");
 				verbose = true;
 			}
 			
 			if(args.length > 1){
 				
 				if(!debug && args[1].equals("-d")){
 					System.out.println("***** DEBUG MODE *****");
 					debug = true;
 				}
 				
 				if(!verbose && args[1].equals("-v")){
 					System.out.println("***** VERBOSE MODE *****");
 					verbose = true;
 				}
 			}
 			System.out.println();
 		}
 		
 // ---------------------------------------------------------------------------------------------------------
 // Run tests
 // ---------------------------------------------------------------------------------------------------------		
 		
 		testParser reader = new testParser();
 
 		test1 = reader.parseTests(file1);
 		//test2 = reader.parseTests(file2);
 		//test3 = reader.parseTests(file3);
 		//test4 = reader.parseTests(file4);
 		
 		reader.testFileParse(test1); System.out.println();
 		//reader.testFileParse(test2); System.out.println();
 		//reader.testFileParse(test3); System.out.println();
 		//reader.testFileParse(test4); System.out.println();
 		
 		dispatcher scheduler = new dispatcher(quantum1, quantum2);
 		
 		ArrayList<Proc> results1 = scheduler.run(test1, debug, verbose);
 		//ArrayList<Proc> results2 = scheduler.run(test2, debug, verbose);
 		//ArrayList<Proc> results3 = scheduler.run(test3, debug, verbose);
 		//ArrayList<Proc> results4 = scheduler.run(test4, debug, verbose);		
 				
 		printResults(results1);
 		//printResults(results2);
 		//printResults(results3);
 		//printResults(results4);
 	}
 
 	/**
 	 @param r List of completed processes.
 	 */
 	public static void printResults(ArrayList<Proc> r){
 		
 		// Stats for individual proceses.
 		int lifeSpan;
 		int arrival;
 		int finish;
 		int run;
 		int waiting;
 		int ready;
 		
 		// Stats for the whole simulation.
 		int totalProcesses = r.size();
 		int maxTurnAroundTime = 0;
 		int avgTurnAroundTime = 0;
 		int maxWaitingTime = 0;
 		int averageWaitingTime = 0;
 		
 		// Print stats about each process.
 		System.out.println("\n-----------------------------------------------------");
 		System.out.printf("%4s %7s %7s %7s %7s %7s\n","Name","Arrive","End", "Ready", "Run", "Wait");
 		System.out.println("-----------------------------------------------------");
 		
 		for (int i = 0; i < r.size(); i++) {
 			
 			// Calculate metrics.
 			lifeSpan = r.get(i).endTime - r.get(i).creationTime;
 			run = r.get(i).runTime;
 			arrival = r.get(i).creationTime;
 			finish = r.get(i).endTime;
 			ready = r.get(i).readyTime;
			waiting = finish - arrive - run;
 
 			// Update max turnaround time.
 			if(finish - arrival > maxTurnAroundTime) { maxTurnAroundTime = finish - arrival; }
 			
 			// Update max wait time.
 			if(ready + waiting > maxWaitingTime) { maxWaitingTime =  ready + waiting; }
 			
 			// Update average turnaround time,
 			avgTurnAroundTime += (finish - arrival);
 			
 			// Update average waiting time.
 			averageWaitingTime += (ready + waiting);
 
 			// Print process statistics.
 			System.out.printf("%4s %7d %7d %7d %7d %7d\n", r.get(i).name, arrival, finish, ready, run, waiting);
 		}
 		
 		System.out.println("-----------------------------------------------------");
 			
 			// Finalise statistics.
 			avgTurnAroundTime /= totalProcesses;
 			averageWaitingTime /= totalProcesses;
 			
 			// Print stats about the whole simulation.
 			System.out.printf("%25s : %5d \n","Total Processes", totalProcesses);
 			System.out.printf("%25s : %5d \n","Max Turn Around Time", maxTurnAroundTime);
 			System.out.printf("%25s : %5d \n","Avg Turn Around Time", avgTurnAroundTime);
 			System.out.printf("%25s : %5d \n","Max Waiting Time", maxWaitingTime);
 			System.out.printf("%25s : %5d \n","Average Waiting Time", averageWaitingTime);
 			System.out.println();
 	}
 }
