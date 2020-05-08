 package edu.se.se441.threads;
 
 import java.util.ArrayList;
 import java.util.concurrent.CountDownLatch;
 
 public class Clock extends Thread{
 	
 	private CountDownLatch startSignal;
 	private long startTime;	// When the simulation starts.
 	private ArrayList<Long> timeRegistry;
 	
 	public Clock(CountDownLatch startSignal){
 		this.startSignal = startSignal;
 		timeRegistry = new ArrayList();
 	}
 	
 	public void run(){
 		try {
 			// Starting all threads at the same time (clock == 0 / "8:00AM").
 			startSignal.await();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 		// Set the start time of the simulation.
 		startTime = System.currentTimeMillis();
		while(this.getTime() <= 4800){
 			for(Long t : timeRegistry){
 				if(t >= this.getTime()){
 					notifyAll();
 				}
 			}
 		}
 	}
 	
 	/**
 	 * @return The time of day in ms (0ms is 8:00AM)
 	 */
 	public long getTime(){
 		return System.currentTimeMillis() - startTime;
 	}
 
 	public void addTimeEvent(long timeOfEvent){
 		timeRegistry.add(timeOfEvent);
 	}
 }
