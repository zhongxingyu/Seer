 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.PriorityQueue;
 import java.util.Queue;
 
 class SchedSim {
 
 	private static int maxProcesses; 			// cap on total processes for simulation
 	private static int maxCPUbursts; 			// cap on total CPU bursts per process
 	private static int quantum = 10;			// RR quantum
 	private static LinkedList<Integer> quanta = new LinkedList<Integer>();	// Array of MLFQ quanta
 
 	private enum Algorithm { // algorithm to use for entire run of simulation
 		FCFS, SJF, SRTF, RR, MLFQ
 	}	
 	private static Algorithm algorithm;
 
 	//Data structures for event simulation
 	private static Queue<Event> eventQueue = new PriorityQueue<Event>();		//The event queue. Holds arrivals, IO returns, bursts, etc
 	private static LinkedList<Process> processTable = new LinkedList<Process>();
 	private static Queue<Process> newProcesses = new LinkedList<Process>();		//The new process list for the simulator.
 	private static Queue<Process> outputList = new LinkedList<Process>();
 	private static Queue<Process> IOQueue = new LinkedList<Process>(); 			//IOQueue. 
 	private static PriorityQueue<Process> readyQueue;							//Ready Queue. This queue's comparator function changes to match SJF, SRTF
 	private static LinkedList<Process> FCFSQueue = new LinkedList<Process>();
 	private static Queue<Process> RRreadyQueue = new LinkedList<Process>();
 	private static LinkedList<Queue<Process>> MLFQLevels= new LinkedList<Queue<Process>>();
 	static int procCount = 0;
 
 	public static void main(String [] args){
 		//Args: filename maxProcesses maxCPUbursts algorithm quanta [quantum]
 		if(!parseRandomFile(args)){//Parse the file passed into the program. Return if parsing fails
 			return;
 		}
 		//If parsing was successful...
 		
 		switch(algorithm){
 		case FCFS:
			System.out.println("FCFS completed in " + runFCFS() + " seconds");
 			output();
 			break;
 		case SJF:
 			System.out.println("Read successful. Starting SJF simulation");
 			readyQueue = new PriorityQueue<Process>(1, new Comparator<Process>() {//Set up ready queue to sort by shortest total runtime
 				public int compare(Process p1, Process p2) {
 					return (int)((p1.timetocomplete)-(p2.timetocomplete));
 				}
 			});
 			System.out.println("SJF completed in " + runSimulation() + " seconds");
 			output();
 			break;
 		case SRTF:
 			System.out.println("Read successful. Starting SRTF simulation");
 			readyQueue = new PriorityQueue<Process>(1, new Comparator<Process>() {
 				public int compare(Process p1, Process p2) {
 					return (int)((p1.timetocomplete - p1.completedTime)-(p2.timetocomplete - p2.completedTime));	// Sort by shortest time remaining
 				}
 			});
 			System.out.println("SRTF completed in " + runSimulation() + " seconds");
 			output();
 			break;
 		case RR:
 			System.out.println("Read successful. Starting RR simulation");
 			System.out.println("RR completed in " + runRR(quantum) + " seconds");
 			output();
 			break;
 		case MLFQ:
 			for(int i=0; i<quanta.size(); i++)
 				MLFQLevels.add(new LinkedList<Process>());
 			System.out.println("Read successful. Starting RR simulation");
 			System.out.println("RR completed in " + runMLFQ(quanta) + " seconds");
 			output();
 			break;
 		}//End switch
 	}
 
 	//The simulation method for first-come first-serve scheduling
 	//This is different from SJF and SRTF in that it does not try to ensure that the CPU is always working
 	//As such, run times will be longer than any other type of scheduler
 	private static double runFCFS(){
 		double runtime = 0;			//Represents the current time. Is changed every time an event comes in
 		double iodonetime = 0;		//Represents the time that the IO device will finish all of the jobs in the io queue.
 		Process cpuProcess = null;	//The process currently on the CPU
 		Process ioProcess = null;	//The process currently being processed by the IO device
 									//Note: FCFS runs one process to completion before starting another, so either cpuProcess or ioProcess will be null at any given point in the simulation
 		
 		while(!(eventQueue.isEmpty())){
 			Event nextEvent = eventQueue.remove();
 			runtime = nextEvent.time;
 			switch(nextEvent.type){
 				case ARRIVAL:															// > Arrival
 					Process enqueueProcess = newProcesses.remove();						// Remove the next process from the new queue and place it on the ready queue
 					processTable.add(enqueueProcess);
 					enqueueProcess.state = Process.State.READY;
 					FCFSQueue.add(enqueueProcess);
 					if(cpuProcess == null && ioProcess == null){						//If it is the first process to arrive
 						cpuProcess = FCFSQueue.remove();
 						cpuProcess.waitTime += runtime - cpuProcess.waitStart;
 						cpuProcess.state = Process.State.RUNNING;
 						eventQueue.add(new Event(Event.Type.CPU_DONE, runtime + cpuProcess.lengthOfCPUbursts[cpuProcess.currentBurst]));
 					}
 					break;
 					
 				case CPU_DONE:																// > CPU Burst Completion
 					if(cpuProcess.currentBurst == cpuProcess.lengthOfCPUbursts.length -1) {	// If the last burst has completed
 						cpuProcess.state = Process.State.TERMINATED;
 						cpuProcess.completionTime = runtime;
 						processTable.remove(cpuProcess);
 					} else {																// If this is not the last burst
 						cpuProcess.state = Process.State.WAITING;
 						cpuProcess.waitStart = runtime;
 						iodonetime = runtime;
 						eventQueue.add(new Event(Event.Type.IO_DONE, iodonetime + cpuProcess.lengthOfIObursts[cpuProcess.currentBurst]));
 						iodonetime += cpuProcess.lengthOfIObursts[cpuProcess.currentBurst];
 						ioProcess = cpuProcess;		//Move the active process to waiting on the IO device
 					}
 					cpuProcess = null;
 					break;
 					
 				case IO_DONE:															// > I/O Burst Completion
 					ioProcess.currentBurst++;
 					ioProcess.state = Process.State.READY;
 					cpuProcess = ioProcess;
 					ioProcess = null;
 					eventQueue.add(new Event(Event.Type.CPU_DONE, runtime + cpuProcess.lengthOfCPUbursts[cpuProcess.currentBurst]));
 					break;
 				default:
 					System.out.println("Invalid Event Type");
 					return runtime;
 			}
 			
 			if(cpuProcess == null && ioProcess == null && !(FCFSQueue.isEmpty())){	//	 If the computer is not doing any work, start a new process on the CPU
 				cpuProcess = FCFSQueue.remove();
 				cpuProcess.waitTime += runtime - cpuProcess.waitStart;
 				eventQueue.add(new Event(Event.Type.CPU_DONE, runtime + cpuProcess.lengthOfCPUbursts[cpuProcess.currentBurst]));
 			}
 			ps(runtime);
 		}
 		return runtime;
 	}
 	
 	private static double preemption(Process currProc, Process otherProc, double runtime) {
 		int res = readyQueue.comparator().compare(currProc, otherProc);
 		if(res == 0 || res < 0)	// Current process has higher priority => No preemption
 			return -1;
 		System.out.println("PREEMPTION!");
 		// Current process has lower priority => Preemption
 		double timeLeft = 0;
 		Queue<Event> temp_eventQueue = new PriorityQueue<Event>();	// Can't iterate over PQ so will remove, check & add back later
 		for(int i=0; i<eventQueue.size(); i++) {
 			Event e = eventQueue.remove();
 			timeLeft = e.time - runtime;
 			if(e.type == Event.Type.CPU_DONE){
 				System.out.println("Removing event:" + e.time + " " + e.type);
 				break;
 			}
 			temp_eventQueue.add(e);
 		}
 		while(!temp_eventQueue.isEmpty())
 			eventQueue.add(temp_eventQueue.remove());					// Add back valid events
 		return timeLeft;
 	}
 	
 	//This is the method that runs the simulation for both the SJF and SRTF schedulers
 	//The only difference between the two is the way processes are pulled out of the ready queue
 	//In SJF, they are sorted by total runtime, whereas in SRTF, they are sorted by total remaining runtime
 	//This method assumes readyQueue has been set up to sort by either of the two methods above.
 	private static double runSimulation(){
 		double runtime = 0;			//Represents the current time. Is changed every time an event comes in
 		double iodonetime = 0;		//Represents the time that the IO device will finish all of the jobs in the io queue.
 		Process cpuProcess = null;	//The process being handled by the CPU
 		Process ioProcess = null;	//The process the IO device is serving
 		while(!(eventQueue.isEmpty())){	//While there are events to process in the simulation
 			Event nextEvent = eventQueue.remove();
 			runtime = nextEvent.time;
 			switch(nextEvent.type){
 				case ARRIVAL:															// > Arrival
 					Process enqueueProcess = newProcesses.remove();
 					processTable.add(enqueueProcess);
 					if(cpuProcess == null){									// CPU Idle => Run Process
 						cpuProcess = enqueueProcess;
 						cpuProcess.state = Process.State.RUNNING;
 						Event e = new Event(Event.Type.CPU_DONE, runtime + cpuProcess.lengthOfCPUbursts[cpuProcess.currentBurst]);
 						eventQueue.add(new Event(Event.Type.CPU_DONE, runtime + cpuProcess.lengthOfCPUbursts[cpuProcess.currentBurst]));
 						System.out.println("Adding event:" + e.time + " " + e.type);
 					} else {
 						double res = preemption(cpuProcess, enqueueProcess, runtime);	// >0 (Preemption, res=time left of cpu burst)
 						if(res > -1) {
 							cpuProcess.lengthOfCPUbursts[cpuProcess.currentBurst] = res;	// Update preempted CPU burst length
 							cpuProcess.state = Process.State.READY;								// Put cpuProcess into ready queue
 							readyQueue.add(cpuProcess);
 							cpuProcess = enqueueProcess;										// Switch CPU Process
 							cpuProcess.state = Process.State.RUNNING;							// Update State
 							Event e = new Event(Event.Type.CPU_DONE, runtime + cpuProcess.lengthOfCPUbursts[cpuProcess.currentBurst]);
 							System.out.println("Adding event:" + e.time + " " + e.type);
 							eventQueue.add(new Event(Event.Type.CPU_DONE, runtime + cpuProcess.lengthOfCPUbursts[cpuProcess.currentBurst]));	// Create event
 						} else {
 							enqueueProcess.state = Process.State.READY;		// Put in ready queue
 							readyQueue.add(enqueueProcess);
 						}
 					}
 					break;
 					
 				case CPU_DONE:															// > CPU Burst Completion
 					if(cpuProcess.currentBurst == cpuProcess.lengthOfCPUbursts.length-1) {  // Last CPU Burst => Process Terminated
 						cpuProcess.state = Process.State.TERMINATED;
 						cpuProcess.completionTime = runtime;
 						processTable.remove(cpuProcess);
 					} else {															//	Not last burst
 						cpuProcess.state = Process.State.WAITING;
 						IOQueue.add(cpuProcess);
 						if(iodonetime < runtime){										//	If the IO device is not busy, set the time it will complete all jobs in IO queue to the current runtime (job can't complete before current event)
 							iodonetime = runtime;
 						}
 						Event e = new Event(Event.Type.IO_DONE, iodonetime + cpuProcess.lengthOfIObursts[cpuProcess.currentBurst]);
 						System.out.println("Adding event:" + e.time + " " + e.type);
 						eventQueue.add(new Event(Event.Type.IO_DONE, iodonetime + cpuProcess.lengthOfIObursts[cpuProcess.currentBurst]));
 						iodonetime += cpuProcess.lengthOfIObursts[cpuProcess.currentBurst];
 					}
 					cpuProcess = null;
 					if(!readyQueue.isEmpty()) {
 						cpuProcess = readyQueue.remove();
 						cpuProcess.state = Process.State.RUNNING;
 						Event e = new Event(Event.Type.CPU_DONE, runtime + cpuProcess.lengthOfCPUbursts[cpuProcess.currentBurst]);
 						System.out.println("Adding event:" + e.time + " " + e.type);
 						eventQueue.add(new Event(Event.Type.CPU_DONE, runtime + cpuProcess.lengthOfCPUbursts[cpuProcess.currentBurst]));
 					}
 					break;
 					
 				case IO_DONE:															// > I/O Burst Completion
 					ioProcess = IOQueue.remove();
 					ioProcess.currentBurst++;
 					if(cpuProcess == null) {								// CPU Idle => Run Process
 						cpuProcess = ioProcess;
 						cpuProcess.state = Process.State.RUNNING;
 						Event e = new Event(Event.Type.CPU_DONE, runtime + cpuProcess.lengthOfCPUbursts[cpuProcess.currentBurst]);
 						System.out.println("Adding event:" + e.time + " " + e.type);
 						eventQueue.add(new Event(Event.Type.CPU_DONE, runtime + cpuProcess.lengthOfCPUbursts[cpuProcess.currentBurst]));
 					} else {
 						double res = preemption(cpuProcess, ioProcess, runtime);	// >0 (Preemption, res=time left of cpu burst)
 						if(res > -1) {
 							cpuProcess.lengthOfCPUbursts[cpuProcess.currentBurst] = res;	// Update preempted CPU burst length
 							cpuProcess.state = Process.State.READY;								// Put cpuProcess into ready queue
 							readyQueue.add(cpuProcess);
 							cpuProcess = ioProcess;										// Switch CPU Process
 							cpuProcess.state = Process.State.RUNNING;							// Update State
 							Event e = new Event(Event.Type.CPU_DONE, runtime + cpuProcess.lengthOfCPUbursts[cpuProcess.currentBurst]);
 							System.out.println("Adding event:" + e.time + " " + e.type);
 							eventQueue.add(new Event(Event.Type.CPU_DONE, runtime + cpuProcess.lengthOfCPUbursts[cpuProcess.currentBurst]));	// Create event
 						} else {
 							ioProcess.state = Process.State.READY;		// Put in ready queue
 							readyQueue.add(ioProcess);
 						}
 					}
 					break;
 					
 				default:																// > Default (Should never occur)
 					System.out.println("Invalid Event Type");							// Invalid Event
 					return runtime;
 			}//End switch
 			ps(runtime);
 		}//End while (Event queue is empty)
 		return runtime;
 	}
 	
 	private static double runRR(int quantum){
 		double runtime = 0;			//Represents the current time. Is changed every time an event comes in
 		Process cpuProcess = null;	//The process being handled by the CPU
 		Process ioProcess = null;	//The process the IO device is serving
 		
 		while(!eventQueue.isEmpty()){	//While there are events to process in the simulation
 			Event nextEvent = eventQueue.remove();
 			runtime = nextEvent.time;
 			switch(nextEvent.type){
 				case ARRIVAL:															// > Arrival
 					Process enqueueProcess = newProcesses.remove();
 					enqueueProcess.id = procCount;
 					procCount++;
 					processTable.add(enqueueProcess);
 					enqueueProcess.state = Process.State.READY;
 					RRreadyQueue.add(enqueueProcess);
 					break;
 					
 				case CPU_DONE:															// > CPU Slice Completion
 					if(cpuProcess.lengthOfCPUbursts[cpuProcess.currentBurst] <= quantum) { // CPU burst finished
 						runtime += cpuProcess.lengthOfCPUbursts[cpuProcess.currentBurst];
 						if(cpuProcess.currentBurst == cpuProcess.lengthOfCPUbursts.length-1) { // If the last CPU burst for this process has finished
 							cpuProcess.state = Process.State.TERMINATED;
 							cpuProcess.completionTime = runtime;
 							processTable.remove(cpuProcess);
 						} else {															//	Not last burst
 							cpuProcess.state = Process.State.WAITING;
 							IOQueue.add(cpuProcess);
 						}
 					} else {																// CPU burst did not finish
 						runtime += quantum;
 						cpuProcess.lengthOfCPUbursts[cpuProcess.currentBurst] -= quantum; 	// Update CPU burst length
 						cpuProcess.state = Process.State.READY;
 						RRreadyQueue.add(cpuProcess);
 					}
 					cpuProcess = null;
 					break;
 					
 				case IO_DONE:															// > I/O Burst Completion
 					ioProcess.currentBurst++;
 					ioProcess.state = Process.State.READY;
 					RRreadyQueue.add(ioProcess);
 					ioProcess = null;
 					break;
 					
 				default:																// > Default (Should never occur)
 					System.out.println("Invalid Event Type");							// Invalid Event
 					return runtime;
 			}//End switch
 			
 			if(ioProcess == null && !(IOQueue.isEmpty())) {							//	 If there is another job that can be placed on the I/O Device
 				ioProcess = IOQueue.remove();
 				eventQueue.add(new Event(Event.Type.IO_DONE, runtime + ioProcess.lengthOfIObursts[ioProcess.currentBurst]));
 			}
 			if(cpuProcess == null && !(RRreadyQueue.isEmpty())) {							//	 If there is another job that can be placed on the CPU
 				cpuProcess = RRreadyQueue.remove();
 				cpuProcess.state = Process.State.RUNNING;
 				if(cpuProcess.lengthOfCPUbursts[cpuProcess.currentBurst] > quantum)
 					eventQueue.add(new Event(Event.Type.CPU_DONE, runtime + quantum));
 				else 
 					eventQueue.add(new Event(Event.Type.CPU_DONE, runtime + cpuProcess.lengthOfCPUbursts[cpuProcess.currentBurst]));
 			}
 			ps(runtime);
 		}//End 
 		return runtime;
 	}
 	
 	private static double runMLFQ(LinkedList<Integer> quanta){
 		double runtime = 0;			//Represents the current time. Is changed every time an event comes in
 		Process cpuProcess = null;	//The process being handled by the CPU
 		Process ioProcess = null;	//The process the IO device is serving
 		
 		while(!eventQueue.isEmpty()){	//While there are events to process in the simulation
 			Event nextEvent = eventQueue.remove();
 			runtime = nextEvent.time;
 			switch(nextEvent.type){
 				case ARRIVAL:															// > Arrival
 					Process enqueueProcess = newProcesses.remove();
 					enqueueProcess.id = procCount;
 					procCount++;
 					processTable.add(enqueueProcess);
 					enqueueProcess.state = Process.State.READY;
 					MLFQLevels.get(enqueueProcess.level).add(enqueueProcess);
 					break;
 					
 				case CPU_DONE:															// > CPU Slice Completion
 					if(cpuProcess.lengthOfCPUbursts[cpuProcess.currentBurst] <= quanta.get(cpuProcess.level)) { // CPU burst finished
 						runtime += cpuProcess.lengthOfCPUbursts[cpuProcess.currentBurst];
 						if(cpuProcess.currentBurst == cpuProcess.lengthOfCPUbursts.length-1) { // If the last CPU burst for this process has finished
 							cpuProcess.state = Process.State.TERMINATED;
 							cpuProcess.completionTime = runtime;
 							processTable.remove(cpuProcess);
 						} else {															//	Not last burst
 							cpuProcess.state = Process.State.WAITING;
 							cpuProcess.level = 0;										    // Reset to top level because of I/O
 							IOQueue.add(cpuProcess);
 						}
 					} else {																// CPU burst did not finish
 						runtime += quanta.get(cpuProcess.level);
 						cpuProcess.lengthOfCPUbursts[cpuProcess.currentBurst] -= quanta.get(cpuProcess.level); 	// Update CPU burst length
 						cpuProcess.level++;													// Bump down level
 						cpuProcess.state = Process.State.READY;
 						MLFQLevels.get(cpuProcess.level).add(cpuProcess);
 					}
 					cpuProcess = null;
 					break;
 					
 				case IO_DONE:															// > I/O Burst Completion
 					ioProcess.currentBurst++;
 					ioProcess.state = Process.State.READY;
 					MLFQLevels.get(ioProcess.level).add(ioProcess);
 					ioProcess = null;
 					break;
 					
 				default:																// > Default (Should never occur)
 					System.out.println("Invalid Event Type");							// Invalid Event
 					return runtime;
 			}//End switch
 			
 			if(ioProcess == null && !(IOQueue.isEmpty())) {							//	 If there is another job that can be placed on the I/O Device
 				ioProcess = IOQueue.remove();
 				eventQueue.add(new Event(Event.Type.IO_DONE, runtime + ioProcess.lengthOfIObursts[ioProcess.currentBurst]));
 			}
 			if(cpuProcess == null) {							//	 If CPU is idle
 				Queue<Process> queue = null;
 				for(int i=0; i<MLFQLevels.size(); i++) {		// Check for highest priority queue with job
 					  queue = MLFQLevels.get(i);
 					  if(!queue.isEmpty())
 						  break;
 				}
 				if(!queue.isEmpty()){
 					cpuProcess = queue.remove();
 					cpuProcess.state = Process.State.RUNNING;
 					if(cpuProcess.lengthOfCPUbursts[cpuProcess.currentBurst] > quanta.get(cpuProcess.level))
 						eventQueue.add(new Event(Event.Type.CPU_DONE, runtime + quanta.get(cpuProcess.level)));
 					else 
 						eventQueue.add(new Event(Event.Type.CPU_DONE, runtime + cpuProcess.lengthOfCPUbursts[cpuProcess.currentBurst]));
 				}
 			}
 			ps(runtime);
 		}//End 
 		return runtime;
 	}
 	
 	private static void output() {
 		double avgCompletionTime = 0;
 		double avgWaitTime = 0;
 		int numProc = outputList.size();
 		for(int i=0; i<numProc; i++) {
 			Process p = outputList.remove();
 			avgCompletionTime += p.completionTime;
 			avgWaitTime += p.waitTime;
 		}
 		System.out.println("Average Completion Time = " + (double)(avgCompletionTime/numProc));
 		System.out.println("Average Wait Time = " + (double)(avgWaitTime/numProc));
 	}
 	
 	private static void ps(double runtime) {
 		Iterator<Process> itr = processTable.iterator();
 		System.out.println("Runtime: " + runtime);
 		System.out.println("---------------");
 		System.out.println("Process Table:");
 		while(itr.hasNext()) {
 			Process p = itr.next();
 			System.out.println(p.id + "\t" + p.state);	
 		}
 		System.out.println("---------------");
 		System.out.print("Ready Queue: ");
 		switch(algorithm) {
 			case FCFS: 
				itr = FCFSQueue.iterator();
				break;
 			case SJF:
 			case SRTF:
 				itr = readyQueue.iterator();
 				break;
 			case RR: 
 				itr = RRreadyQueue.iterator();
 				break;
 			default:
 				itr = null;
 		}
 		while(itr.hasNext()) {
 			Process p = itr.next();
 			System.out.print(p.id +",");	
 		}
 		System.out.println();
 		System.out.println("---------------");
 		System.out.print("IO Queue: ");
 		itr = IOQueue.iterator();
 		while(itr.hasNext()) {
 			Process p = itr.next();
 			System.out.print(p.id +",");	
 		}
 		System.out.println();
 		System.out.println("---------------");
 		System.out.println();
 	}
 
 	private static boolean parseRandomFile(String [] args){
 		FileInputStream binaryFile = null;
 		String simulationType;									//The type of simulated scheduler being run
 		//Here is where the random file is parsed and new tasks are generated
 		try {
 			binaryFile = new FileInputStream(new File(args[0]));
 			double nextArrivalTime = 0;							//When the next process arrives after the one being read from the file
 			maxProcesses = Integer.parseInt(args[1]);
 			maxCPUbursts = Integer.parseInt(args[2]);
 			if(args.length>4) {
 				if(args[4].charAt(0) != '[') {
 					quantum = Integer.parseInt(args[4]);		// RR quantum
 				}
 				else {											// MLFQ quanta array [quanata1,quanta2,...]
 					String[] qua = (args[4].substring(1, args[4].length()-1)).split(",");
 					for(int i=0; i<qua.length; i++) {
 						quanta.add(Integer.parseInt(qua[i]));
 					}
 				}
 			}
 			simulationType = args[3];							//MLFQ, FCFS, SJF, SRTF, etc
 
 			for(int i = 0; i < maxProcesses;i++){	//Get the information for each process from the random file
 				Process randomProcess = new Process();
 				randomProcess.timetocomplete = 0;
 				Event newProcessArrival = new Event(Event.Type.ARRIVAL, nextArrivalTime);
 				eventQueue.add(newProcessArrival);
 				nextArrivalTime += ((double)((int)binaryFile.read() & 0xff))/10;						//Set up the nextArrivalTime for the next process. This is the time that the next process arrives *after* the current one arrives
 
 				randomProcess.cpubursts = (((int)binaryFile.read() & 0xff) % maxCPUbursts) + 1;		//Set up the number of CPU bursts this process will have. Called 'n'
 				randomProcess.lengthOfCPUbursts = new double[randomProcess.cpubursts];					//There will be "n" bursts
 				randomProcess.lengthOfIObursts = new double[randomProcess.cpubursts - 1];				//There are "n" - 1 IO Bursts (one between each CPU burst)
 
 				//Populate arrays with the duration of IO and CPU bursts. 
 				//Times for each are produced from sequential bytes from random file
 				for(int j = 0; j < randomProcess.cpubursts; j++){									//Read in the size of each cpu burst
 					randomProcess.lengthOfCPUbursts[j] = ((double)((int)binaryFile.read() & 0xff))/25.6;
 					randomProcess.timetocomplete += randomProcess.lengthOfCPUbursts[j];
 				}
 				for(int j = 0; j < randomProcess.cpubursts - 1; j++){								//Read in the size of each IO burst
 					randomProcess.lengthOfIObursts[j] = ((double)((int)binaryFile.read() & 0xff))/25.6;
 					randomProcess.timetocomplete += randomProcess.lengthOfIObursts[j];
 				}
 				randomProcess.state = Process.State.NEW;
 				randomProcess.id = procCount;
 				procCount++;
 				newProcesses.add(randomProcess);		//Enqueue the process
 				outputList.add(randomProcess);			//Used for output purposes
 			}
 			binaryFile.close();							//Close the FileInputStream
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;			//Read failed
 		}
 		//Set the value of the enum algorithm (Currently supports FCFS, SJF, and SRTF)
 		if(simulationType.compareTo("FCFS") == 0){
 			algorithm = Algorithm.FCFS;
 		}
 		if(simulationType.compareTo("SJF") == 0){
 			algorithm = Algorithm.SJF;
 		}
 		if(simulationType.compareTo("SRTF") == 0){
 			algorithm = Algorithm.SRTF;
 		}
 		if(simulationType.compareTo("RR") == 0){
 			algorithm = Algorithm.RR;
 		}
 		if(simulationType.compareTo("MLFQ") == 0){
 			algorithm = Algorithm.MLFQ;
 		}
 		return true;//Read was successful
 	}
 }
