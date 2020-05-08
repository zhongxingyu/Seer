 package scheduler;
 import java.util.LinkedList;
 //import java.util.concurrent.LinkedBlockingQueue;
 
 public class FCFS {
 	/**@param processQ - Contains the processes in the order of their delays, and the order they were encountered.
 	 * @param cycle - The current cycle of this set of process executions.**/
 	LinkedList<Process> processQ = new LinkedList<Process>();
 	LinkedList<Process> readyQ = new LinkedList<Process>();
 	LinkedList<Process> tempQ;
 	private int cycle;
 	//A semaphore for the processor.
 	private boolean unlocked;
 	private boolean unlockNextCycle;
 	int numberTerminated = 0;
 	int numberOfProcesses;
 
 	
 	public FCFS(int numberOfProcesses){
 		cycle = 0;
 		unlocked = true;
 		this.numberOfProcesses = numberOfProcesses;
 	}
 	
 	public void offer(Process newProcess){
 		processQ.offer(newProcess);
 	}
 	
 	public void runCycle(){
 		Process currentProcess;
 		int state;
 		tempQ = new LinkedList<Process>(processQ);
 		printCycle();
 		processQ.clear();
 		
 		
 		while(!tempQ.isEmpty()){
 			currentProcess = tempQ.poll();
 			state = currentProcess.state;
 		
 			switch(state){
 				case Process.UNSTARTED: checkArrivalOf(currentProcess); break;
 				case Process.READY: checkReadyToRun(currentProcess); break;
 				case Process.RUNNING: checkRunningToBlock(currentProcess); break;
 				case Process.BLOCKED: checkBlockedToReady(currentProcess); break;
 				case Process.TERMINATED: processQ.offer(currentProcess); break;
 			}
 		}
 		/* The function call below will only set the process to running if the
 		 * semaphore is still unlocked after going through all processes.*/
 		checkReadyQueue();
 		cycle++;
 		if(unlockNextCycle)
 			unlocked = true;
 	}
 	
 	public void checkArrivalOf(Process currentProcess){
 		/*If the processes' time has arrived, set the process to ready.  If not, 
 		 *place back into the queue.*/
 		if((currentProcess.arrivalTime - this.cycle) == 0){
 			currentProcess.setState(Process.READY);
 			//Set to ready and insert into ready queue.
 			readyQ.offer(currentProcess);
 			/*If this process is ready, and the semaphore is unlocked, then go 
 			 * ahead and run it.  After setting to running return from this function.*/
 			if(unlocked){
 				checkReadyToRun(currentProcess);
 				return;
 			}
 			processQ.offer(currentProcess);
 		}
 		else
 			//If the process is not ready yet, place back into the queue.
 			processQ.offer(currentProcess);
 	}
 	
 	public boolean checkReadyToRun(Process currentProcess){
 		//Simple method checks if a process is running before running the new process.
 		if(unlocked){
 			//Is this process at the head of the readyQ?
 			if(currentProcess.equals(readyQ.peek())){
 				//If it is, then set this process to RUNNING, and leave it on the readyQ.
 				currentProcess.setState(Process.RUNNING);
 				//Lock the semaphore
 				unlocked = false;
 				//Run one cycle
 				currentProcess.reduceBurst();
 				currentProcess.reduceCPU();
 				//unlock the semaphore for the next cycle if this processes' burst is 1.
 				if(currentProcess.remainingBurst == 0)
 					unlockNextCycle = true;
 			}
 			processQ.offer(currentProcess);
 			return true;
 		}
 		else
 			processQ.offer(currentProcess);
 		return false;
 	}
 	
 	public void checkReadyQueue(){
 		if(unlocked && !readyQ.isEmpty()){
 			Process currentProcess = readyQ.peek();
 			//Is this process at the head of the readyQ?
 			//If it is, then set this process to RUNNING, and leave it on the readyQ.
 			currentProcess.setState(Process.RUNNING);
 			//Lock the semaphore
 			unlocked = false;
 			//Run one cycle
 			currentProcess.reduceBurst();
 			currentProcess.reduceCPU();
 			//unlock the semaphore for the next cycle if this processes' burst is 1.
 			if(currentProcess.remainingBurst == 0)
 				unlockNextCycle = true;
 		}
 	}
 	
 
 	public void checkRunningToBlock(Process currentProcess){
 		if(currentProcess.remainingBurst > 1){ // >1 or >0?  Come back and check this later
 			currentProcess.reduceBurst();
 			currentProcess.reduceCPU();
 			processQ.offer(currentProcess);
 		}
 		else if(currentProcess.remainingCPU < 1){
 			//No more CPU time needed.  Process finished.
 			currentProcess.setState(Process.TERMINATED);
 			//Remove the process from the ready queue.
 			readyQ.poll();
 			//Place back in the processQ for printing of state.
 			processQ.offer(currentProcess);
 			numberTerminated++;
 			//Unlock the semaphore
 			unlocked = true;
 		}
 		else{//Burst time has run out, block this process.
 			currentProcess.setState(Process.BLOCKED);
 			currentProcess.reduceBurst();
 			//Remove from the readyQ.
 			readyQ.poll();
 			processQ.offer(currentProcess);
 			//Unlock the semaphore
 			unlocked = true;
 		}
 			
 	}
 	
 	public void checkBlockedToReady(Process currentProcess){
 		if(currentProcess.remainingBurst > 1){
 			currentProcess.reduceBurst();
 			processQ.offer(currentProcess);
 		}
 		else{
 			currentProcess.setState(Process.READY);
 			//Place the process on the ready queue.  Wait its turn to run.
 			readyQ.offer(currentProcess);
 			checkReadyToRun(currentProcess);
 		}		
 	}
 	
 	public void printCycle(){
 		Process currentProcess;
 		String cycleLine = "";
 		LinkedList<Process> printQ = new LinkedList<Process>(processQ);
  
 		//Print the print queue.
 		while((currentProcess = printQ.poll()) != null){
 			cycleLine += " \t" + currentProcess.getStateString() + " " 
 			+ currentProcess.remainingBurst;
 		}
 		//Restore the print queue from its copy.
  
 		System.out.println("Before cycle " + cycle + ": " + cycleLine);
 	}
 	
 	//Indicates that all processes have finished running.
 	public boolean isFinished(){
 		if(numberTerminated == numberOfProcesses)
 			return true;
 		else
 			return false;
 	}
 }
