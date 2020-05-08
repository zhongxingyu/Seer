 /**
  * This class contains a lot of public variables that can be updated by other
  * classes during a simulation, to collect information about the run.
  */
 public class Statistics {
 	private static long processesCompleted = 0;
 	private static long processesCreated = 0;
 	private static long processesAccepted = 0;
 	private static long processesForceChanged = 0;
 	private static long processesIoOperations = 0;
 	private static long processesMemoryWaitTotal = 0;
 	private static long processesCpuWaitTotal = 0;
 	private static long processesIoWaitTotal = 0;
 	private static long processesTotalTimeInSystem = 0;
 	
 	private static long processesPlacedInCpuQueue = 0;
 	private static long processesPlacedInIOQueue = 0;
 
 	private static long memoryQueueLengthTime = 0;
 	private static long memoryQueueLengthLargest = 0;
 
 	private static long cpuQueueLengthTime = 0;
 	private static long cpuQueueLengthLargest = 0;
 	private static long cpuIdleTime = 0;
 	private static long cpuActiveTime = 0;
 
 	private static long ioQueueLengthTime = 0;
 	private static long ioQueueLengthLargest = 0;
 	private static long ioIdleTime = 0;
 	private static long ioActiveTime = 0;
 	
 	/**
 	 * Increment the number of processes completed
 	 */
 	public static void processCompleted() {
 		processesCompleted++;
 	}
 
 	/**
 	 * Increment the number of processes created
 	 */
 	public static void processCreated() {
 		processesCreated++;
 	}
 	
	public static void processAccepted() {
 		processesAccepted++;
 	}
 	
 	/**
 	 * Increment the number of processes forced changed
 	 */
 	public static void processForceChange() {
 		processesForceChanged++;
 	}
 	
 	/**
 	 * Increment the number of processes forced changed
 	 */
 	public static void processesIoOperations() {
 		processesIoOperations++;
 	}
 
 	/**
 	 * Increment the number of processes forced changed
 	 */
 	public static void processesPlacedInCpuQueue() {
 		processesPlacedInCpuQueue++;
 	}
 
 	/**
 	 * Increment the number of processes forced changed
 	 */
 	public static void processesPlacedInIOQueue() {
 		processesPlacedInIOQueue++;
 	}
 	
 	/**
 	 * Increment how long all processes have been waiting
 	 * 
 	 * @param time - 
 	 */
 	public static void processesTotalTimeInSystem(long time) {
 		processesTotalTimeInSystem += time;
 	}
 
 	/**
 	 * Increment how long all processes have been waiting
 	 * 
 	 * @param time - 
 	 */
 	public static void processMemoryWait(long time) {
 		processesMemoryWaitTotal += time;
 	}
 
 	/**
 	 * Increment how long all processes have been waiting
 	 * 
 	 * @param time - 
 	 */
 	public static void processCPUWait(long time) {
 		processesCpuWaitTotal += time;
 	}
 	
 	/**
 	 * Increment how long all processes have been waiting
 	 * 
 	 * @param time - 
 	 */
 	public static void processIOWait(long time) {
 		processesIoWaitTotal += time;
 	}
 
 	/**
 	 * 
 	 * @param length - 
 	 * @param time - 
 	 */
 	public static void memoryQueueLengthTime(long length, long time) {
 		memoryQueueLengthTime += length * time;
 	}
 	
 	/**
 	 * 
 	 * @param length
 	 */
 	public static void memoryQueueLenght(long length) {
 		if (length > memoryQueueLengthLargest) {
 			memoryQueueLengthLargest = length;
 		}
 	}
 	
 	/**
 	 * 
 	 * @param length
 	 * @param time
 	 */
 	public static void cpuQueueLengthTime(long length, long time) {
 		cpuQueueLengthTime += length * time;
 	}
 	
 	/**
 	 * 
 	 * @param length
 	 */
 	public static void cpuQueueLength(long length) {
 		if (length > cpuQueueLengthLargest) {
 			cpuQueueLengthLargest = length;
 		}
 	}
 	
 	/**
 	 * 
 	 * @param time
 	 */
 	public static void cpuIdleTime(long time) {
 		cpuIdleTime += time;
 	}
 	
 	/**
 	 * 
 	 * @param time
 	 */
 	public static void cpuActiveTime(long time) {
 		cpuActiveTime += time;
 	}
 	
 	/**
 	 * 
 	 * @param length
 	 * @param time
 	 */
 	public static void ioQueueLengthTime(long length, long time) {
 		ioQueueLengthTime += length * time;
 	}
 	
 	/**
 	 * 
 	 * @param length
 	 */
 	public static void ioQueueLength(long length) {
 		if (length > ioQueueLengthLargest) {
 			ioQueueLengthLargest = length;
 		}
 	}
 
 	/**
 	 * 
 	 * @param time
 	 */
 	public static void ioIdleTime(long time) {
 		ioIdleTime += time;
 	}
 	
 	/**
 	 * 
 	 * @param time
 	 */
 	public static void ioActiveTime(long time) {
 		ioActiveTime += time;
 	}
 	
 	public static float getCpuUtilization() {
 		return (float) 100/SystemClock.getTime() * cpuActiveTime;
 	}
 	
 	public static float getIoUtilization() {
 		return (float) 100/SystemClock.getTime() * ioActiveTime;
 	}
 	
 	public static float getProcessThroughput() {
 		long s = (long) SystemClock.getTime()/1000;
 		return (float) processesCompleted/s;
 	}
 	
 	/**
 	 * Prints out a report summarizing all collected data about the simulation.
 	 * 
 	 * @param simulationLength The number of milliseconds that the simulation
 	 *            covered.
 	 */
 	public static void printReport(long simulationLength) {
 		System.out.println();
 		System.out.println("Simulation statistics:");
 		System.out.println();
 		System.out.println("Number of completed processes:                                " + processesCompleted);
 		System.out.println("Number of created processes:                                  " + processesCreated);
 		System.out.println("Number of (forced) process switches:                          " + processesForceChanged);
 		System.out.println("Number of processed I/O operations:                           " + processesIoOperations);
 		System.out.println("Average throughput (processes per second):                    " + getProcessThroughput());
 		System.out.println();
 		System.out.println("CPU processing time:                                          " + cpuActiveTime+" ms");
 		System.out.println("Utilization:                                                  " + getCpuUtilization()+"%");
 		System.out.println();
 		System.out.println("IO processing time:                                           " + ioActiveTime+" ms");
 		System.out.println("Utilization:                                                  " + getIoUtilization()+"%");
 		System.out.println();
 		System.out.println("Largest occuring memory queue length:                         " + memoryQueueLengthLargest);
 		System.out.println("Average memory queue length:                                  " + (float) memoryQueueLengthTime / simulationLength);
 		System.out.println("Largest occuring CPU queue length:                            " + cpuQueueLengthLargest);
 		System.out.println("Average CPU queue length:                                     " + (float) cpuQueueLengthTime / simulationLength);
 		System.out.println("Largest occuring IO queue length:                             " + ioQueueLengthLargest);
 		System.out.println("Average IO queue length:                                      " + (float) ioQueueLengthTime / simulationLength);
 		if (processesCompleted > 0) {
 			System.out.println();
 			System.out.println("Average # of times a process has been placed in memory queue: " + 1);
 			System.out.println("Average # of times a process has been placed in CPI queue:    " + (float) processesPlacedInCpuQueue / processesCompleted);
 			System.out.println("Average # of times a process has been placed in IO queue:     " + (float) processesPlacedInIOQueue / processesCompleted);
 			System.out.println();
 			System.out.println("Average time spent in system per process:                     " + (long) processesTotalTimeInSystem / processesCompleted + " ms");
 			System.out.println("Average time spent waiting for memory per process:            " + (long) processesMemoryWaitTotal / processesCompleted + " ms");
 			System.out.println("Average time spent waiting for CPU per process:               " + (long) processesCpuWaitTotal / processesCompleted + " ms");
 			System.out.println("Average time spent processing per process:                    " + (long) cpuActiveTime / processesCompleted + " ms");
 			System.out.println("Average time spent waiting for I/O per process:               " + (long) processesIoWaitTotal / processesCompleted + " ms");
 			System.out.println("Average time spent in O/O per process:                        " + (long) ioActiveTime / processesCompleted + " ms");
 		}
 	}
 }
