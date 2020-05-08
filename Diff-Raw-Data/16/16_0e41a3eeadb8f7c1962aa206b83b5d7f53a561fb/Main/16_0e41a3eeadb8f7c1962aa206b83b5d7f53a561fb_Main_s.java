 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 
 public class Main {
 
 	private static final int masterPort = 4444;
 	private static int[] toSort = {1,4,2,5,3};
 	private static int N = 3;
 	
 	// where results are saved
 	private static long timeTakenTotal;// from master's prespective (including sending and receiving array)
 	private static long longestNodeTime; // from node's perspective
 	
 	private static Node[] nodes;
 	
 	/**
 	 * This method coordinates and initiates the sort.  It creates the master, creates the nodes, and starts them all. Results are saved in timeTakenTotal and longestNodeTime fields.
 	 */
 	private static void start(int[] toSort, int N, boolean debuggingOn){	
 		
 		//System.out.println("N: " + N);
 		
 		// get master hostname
 		String masterHostname = "";
 		try {
 			masterHostname = InetAddress.getLocalHost().getHostName();
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 		
 		// start the master
 		Master master = new Master(N, masterPort, toSort, debuggingOn);
 		master.start();
 		
 		// create and start the nodes
 		int port = masterPort;
 		nodes = new Node[N];
 		for(int i = 0; i < N; i++){
 			nodes[i] = new Node(++port, masterHostname, masterPort, debuggingOn);
 			nodes[i].start();
 		}
 		
 		// wait for master to finish and get result
 		try {
 			master.join();
 //			System.out.println(master.getTimeTaken() + "ms");
 //			System.out.println("node time: " + master.getLongestNodeTime() + "ms");
 //			System.out.println();
 			Main.timeTakenTotal = master.getTimeTaken();
 			Main.longestNodeTime = master.getLongestNodeTime();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * This is a helper method to generate a random array of given size.
 	 * @param size
 	 * @return
 	 */
 	private static int[] generateRandomArray(int size){
 		Random r = new Random();
 		
 		int[] array = new int[size];
 		
 		int max = size > Integer.MAX_VALUE? Integer.MAX_VALUE: size;
 		
 		for(int i = 0; i < size; i++){
 			array[i] = r.nextInt(max);
 		}
 		
 		return array;
 	}
 	
 	/**
 	 * This method runs a number of tests, printing out the arraysize, N, and time taken total.
 	 * @param arraySize
 	 * @param sizesOfNToTest
 	 */
 	private static void runTest(int arraySize, int[] sizesOfNToTest, int numRepeats){
 		// generate array
 		toSort = generateRandomArray(arraySize);
 		
 		System.out.println("Initiating test");
 		System.out.println("Array size: " + toSort.length);
 		System.out.println("==================");
 		
 		// do collections.sort()
 		List<Integer> copy = new ArrayList<Integer>();
 		for(int i: toSort){
 			copy.add(i);
 		}
 		long start = System.currentTimeMillis();
 		Collections.sort(copy);
 		long timeTaken = System.currentTimeMillis() - start;
 		System.out.println("Collections.sort());");
 		System.out.println(timeTaken + "ms\r\n");
 					
 		// do tests and average results
 		long averageTotal = 0;
 		long averageNode = 0;
 		for(int N: sizesOfNToTest){
 			for(int i = 0; i < numRepeats; i++){
 				start(toSort, N, false);
 				averageTotal += timeTakenTotal;
 				averageNode += longestNodeTime;	
 			}
 			averageTotal = averageTotal / numRepeats;
 			averageNode = averageNode / numRepeats;
 			
 			System.out.println("N: " + N);
			System.out.println("master time: " + averageTotal + "ms");
			System.out.println("node time: " + averageNode + "ms");
 			System.out.println();
 		}
 	}
 	
 	/**
 	 * The main method.
 	 * @param args
 	 */
 	public static void main(String[] args){
 		
 		int[] NToTest = {1,2,3,4,5}; // values of N to test at
 		
//		runTest(20, NToTest, 5);
//		runTest(200, NToTest, 5);
//		runTest(2000, NToTest, 5);
//		runTest(10000, NToTest, 5);
//		runTest(20000, NToTest, 5);
 		runTest(40000, NToTest, 5);
 	}
 }
