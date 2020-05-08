 package org.processmanagement.processes;
 
 import java.util.*;
 //import java.lang.Math;
 
 public class PRandom {
 	final int MAX_PROCESS_NUMBER = 100;
 	/**
 	 * Generates a random integer in range between min and max
 	 * 
 	 * @param min lower limit
 	 * @param max upper limit
 	 * @return the random integer
 	 */
 	public int randomInt(int min, int max)
 	{
 		//new random object
 		Random rand = new Random();
 		//generate a random number between min and max both being
 		//inclusive
 		int randomNum = rand.nextInt(max - min + 1) + min;
 		
 		return randomNum;
 	}	
 
 	public ArrayList<Process> genProcesses() {
 		// create a random num between 1 and MAX_PROCESS_NUMBER
 		System.out.println("How many processes should be generated?: ");
 		Scanner input = new Scanner(System.in);
 		int processNum = input.nextInt();
 		// int processNum = randomInt(1, MAX_PROCESS_NUMBER);
 		ArrayList<Process> processList = new ArrayList<Process>();
 		// populate a list with the generated number of processes
		for (int i = 0; i <= processNum; i++) {
			processList.add(randomProcess(i + 1));
 		}
 		return processList;
 	}
 	private Process randomProcess(int count){
 		
 		//set random data
 		int burst = randomInt(0, 100);
 		int priority = randomInt(0, 5);
 		int arrivalTime = randomInt(0, 10);
 		String name = "P"+count;
 		
 		//generate a new process
 		Process proc = new Process(burst, arrivalTime, priority,name);
 		
 		//return the process
 		return proc;
 				
 	}
 	private Process randomIOProcess(int count){
 		//set random data
 		//(int burst, int arrivalTime, int priority, int burstBeforeIO, int ioTime)
 		int burst = randomInt(0, 100);
 		int priority = randomInt(0, 5);
 		int arrivalTime = randomInt(0, 10);
 		int burstSegment = randomInt(1, burst/5);//can't be larger than 1/5 the total burst
 		int ioTime = randomInt(1,5);//small maximum io time seems realistic
 		int ioSegment = randomInt(1, ioTime/5);//can't be larger than 1/5 the total IO time
 		String name = "P"+count;
 		
 		//generate new process
 		Process proc = new Process(burst,arrivalTime,priority,burstSegment,ioTime,ioSegment,name);
 		
 		//return the process
 		return proc;
 	}
 }
