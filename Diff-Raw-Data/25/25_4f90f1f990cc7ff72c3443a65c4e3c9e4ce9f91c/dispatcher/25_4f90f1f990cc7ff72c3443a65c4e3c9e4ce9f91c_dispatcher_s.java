 import java.util.ArrayList;
 import java.util.PriorityQueue;
 
 /**
 	@author Tom Ravenscroft
  	@version version 1.0
  */
 
 // TODO: Review the ageing methods.
 // TODO: Make the CPU run to the completion of a quantum, even if the task finishes in the middle.
 
 public class dispatcher{
 	
 	private ArrayList<Proc> results;
 	private int time;
 	private queue_1 HPQ;
 	private queue_2 LPQ;
 	private Proc[] test;
 	private int quantum1;
 	private int quantum2;
 	private boolean debug;
 	private boolean verbose;
 	private boolean taskRunning;
 	
 	/**
 	 *
 	 */
 	public dispatcher(int q1, int q2){
 		this.quantum1 = q1;
 		this.quantum2 = q2;
 		this.reset();
 	}
 	
 	/**
 	 * 
 	 */
 	public void reset(){
 		this.time = 0;
 		this.taskRunning = false;
 		HPQ = new queue_1();
 		LPQ = new queue_2();
 	}
 	
 	/**
 	 * 
 	 */
 	public ArrayList<Proc> run(Proc[] test, boolean debug, boolean verbose){
 		
 // ---------------------------------------------------------------------------------------------------------
 // Initialise a new test
 // ---------------------------------------------------------------------------------------------------------
 			
 			// Debug
 			this.debug = debug;
 			this.verbose = verbose;
 			
 			// Set the time to 0 and empty the queues.
 			this.reset();	
 				
 			// Assign the test to run.
 			this.test = test;
 			
 			//  Declare the list of results.
 			results = new ArrayList<Proc>();
 		
 			// Processes handled by the dispatcher.
 			Proc nextTask = null;
 			Proc currentTask = null;
 
 			// Count the order in which processes arrive.
 			int nextProc = 0;
 
 			// Flag to indicate which queue is active. T = HPQ, F = LPQ.
 			boolean highPriority = false;
 			
 // ---------------------------------------------------------------------------------------------------------
 // Run test
 // ---------------------------------------------------------------------------------------------------------			
 			
 			// Execute while there are processes still to finish.
 			while(!HPQ.isEmpty() || !LPQ.isEmpty() || nextProc < test.length || taskRunning){	
 
 // ---------------------------------------------------------------------------------------------------------
 // Add newly arrived processes to queues
 // ---------------------------------------------------------------------------------------------------------
 				
 				while(nextProc < test.length && test[nextProc].creationTime == time){
 
 					// Check the priority threshold.
 					if(test[nextProc].priority > 3){
 
 						HPQ.add(test[nextProc]);	
 						
 						System.out.print(debug ? "Added  " + test[nextProc].name + " to high priority queue.\n\n" : "");
 					}
 					else{
 
 						LPQ.add(test[nextProc]);	
 						System.out.print(debug ? "Added  " + test[nextProc].name + " to low priority queue.\n\n" : "");
 					}
 					nextProc ++;
 				}
 				
 // ---------------------------------------------------------------------------------------------------------
 // Execute process
 // ---------------------------------------------------------------------------------------------------------
 				
 				// Check to see which queue has access.
 				// Get the next process from the active queue and run it.
 				if(!HPQ.isEmpty()){
 
 					// Preempt any low priority task that is still running.
 					if(taskRunning){
 						
 						currentTask.currentQuantum = 0;
 						currentTask.age = 0;
 						LPQ.add(currentTask);
 					}
 					
 					if(verbose) { printQueues(HPQ,LPQ); }
 					
 					nextTask = HPQ.remove();
 					highPriority = true;
 					
 					// Run the next task.
 					currentTask = cpuExecute(nextTask, quantum1, highPriority);
 				}
 				
 				// Continue running active low priorit task.
 				else if(taskRunning){
 					
 					if(verbose) { printQueues(HPQ,LPQ); }
 					nextTask = currentTask;
 					
 					// Run the process.
 					currentTask = cpuExecute(nextTask, quantum2, highPriority);
 				}
 				
 				// Run the next low priority task.
 				else if(!LPQ.isEmpty()){
 					
 					if(verbose) { printQueues(HPQ,LPQ); }
 					nextTask = LPQ.remove();
 					highPriority = false;
 					
 					// Run the process.
 					currentTask = cpuExecute(nextTask, quantum2, highPriority);
 				}
 
 // ---------------------------------------------------------------------------------------------------------
 // Check if process finished. Re-add process to queue.
 // ---------------------------------------------------------------------------------------------------------
 				
 				// Check if the process has finished.
 				if(currentTask.runTime < currentTask.burstTime){
 					
 					
 					// Task originially came from the high priority queue.
 					if(highPriority){
 					
 						taskRunning = false;
 						currentTask.currentQuantum = 0;
 						currentTask.age = 0;
 							
 						// Task is moving from HPQ to HPQ
 						if(currentTask.priority > 3){
 					
 							HPQ.add(currentTask);
 						}
 						
 						// Task is moving from HPQ to LPQ.
 						else 
 							LPQ.add(currentTask);
 					}
 					
 					// Task originally came from the low priority queue.
 					if(!highPriority){
 						
 						if(currentTask.currentQuantum == quantum2){
 							
 							taskRunning = false;
 							currentTask.currentQuantum = 0;
 							currentTask.age = 0;
 							
 							// Task has is moving from LPQ to HPQ.
 							if(currentTask.priority > 3){
 								
 								HPQ.add(currentTask);
 							}
 						
 							// Task is moving from LPQ to LPQ.
 							else
 								LPQ.add(currentTask);
 						}
 						else 
 							taskRunning = true;
 							System.out.print(verbose ? "Not finished the current task.\n" : "");
 					}
 				}
 				else{
 					taskRunning = false;
 					System.out.print(debug ? "\n ***** Task: " + currentTask.name + " done @ t = " + time + "\n\n" : "");
 					currentTask.endTime = time;
 					results.add(currentTask);
 					
 					// Account for the fact that the CPU must complete the quantum even if the process finishes.
 					if(!highPriority){
 						if(currentTask.currentQuantum > 0 && currentTask.currentQuantum < quantum2){
 						}
 					}
 				}
 
 // ---------------------------------------------------------------------------------------------------------
 // Update priorities in LPQ
 // ---------------------------------------------------------------------------------------------------------
 
 				// Update the process priorities before running anything.
 				if(!LPQ.isEmpty()) {  
 
 					LPQ = this.updatePriorities(LPQ);
					int i = 0;
 					
					while(i < LPQ.size()) {
 
						if(LPQ.get(i).priority > 3){ 
 
							System.out.print(debug ? "///// LPQ raising priority of: " + LPQ.get(i).name + "\n" : "");
							HPQ.add(LPQ.remove(i)); 
 						}
						else i++;
 					} 
 				}				
 				
 // ---------------------------------------------------------------------------------------------------------
 // Print queue information.
 // ---------------------------------------------------------------------------------------------------------
 
 				// Print queue sizes.
 				System.out.print(verbose ? "HPQ size: " + HPQ.size() + "\n" : "");
 				System.out.print(verbose ? "LPQ size: " + LPQ.size() + "\n\n" : "");
 			}
 
 			// Returns the list of finished processes.
 			return results;
 	}
 	
 	/**
 	 * Update the priorities in the low priority queue (every 8 runs of other processes).
 	 */
 	public queue_2 updatePriorities(queue_2 q){
 		
 		for(int i = 0; i < q.size(); i++){
 
 			q.get(i).age = time - q.get(i).lastRunTime;
 
			if(q.get(i).age >= 140) {  
 
				//System.out.print(debug ? "///// LPQ raising priority of: " + q.get(i).name + "\n" : "");
 				q.get(i).priority++;
 			}
 		}
 		return q;
 	}
 	
 	/**
 	 * 
 	 */
 	public Proc cpuExecute(Proc p, int q, boolean hp){
 
 		taskRunning = true;
 
 		// Set the process' initial ready time.
 		// TODO: Check if this is the correct ready time.
 		if(p.readyTime == -1) { p.readyTime = time; }
 
 		// Update the running process' time to completion (single unit for Q2 to support preemption).
 		if(hp) { 
 			p.runTime  += q; 
 			p.currentQuantum += q;
 			}
 		else { 
 			p.runTime++; 
 			p.currentQuantum++;
 		}
 
 		p.lastRunTime = time;
 
 		// Update the time (single unit for Q2 to support preemption).
 		if(hp) { time += q; }
 		else { time++; }
 
 		p.order = ++Proc.numProcs;
 
 		// Print the current task in the CPU.
 		if(verbose) { printTask(p); }
 
 		// Post processing for tasks from high priority queue.
 		if(hp){
 
 			// Decrease the priority of the task every 3 runs.
 			if(p.runTime % 30 == 0){ 
 
 				System.out.print(debug ? "----- Reducing priority of: " + p.name + "\n" : "");
 				p.priority--; 
 				}
 		}
 	
 		System.out.print(verbose ? "Current quantum is: " + p.currentQuantum + "/" + q + "\n": "");
 		
 		return p;
 	}
 
 	/**
 	 * 
 	 */
 	public void printTask(Proc t){
 
 		// Print some useful information.
 		System.out.println("Time: " + time);
 		System.out.println("Moved " + t.name + " from queue to CPU.");
 		System.out.println("Priority: " + t.priority);
 		System.out.println("Executed for : " + t.runTime + "/" +t.burstTime + " cycles.");
 	}
 	
 	/**
 	 * 
 	 */
 	public void printQueues(queue_1 Q1, queue_2 Q2){
 	
 		System.out.println();
 		System.out.println("Queues are as follows:");
 		System.out.print("HPQ: ");
 		HPQ.printQueue();
 		System.out.println();
 		System.out.print("LPQ: ");
 		LPQ.printQueue();
 		System.out.println();
 	}
 }
