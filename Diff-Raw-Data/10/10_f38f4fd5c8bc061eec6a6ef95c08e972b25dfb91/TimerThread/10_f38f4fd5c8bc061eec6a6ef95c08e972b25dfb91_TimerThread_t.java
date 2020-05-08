 package com.googlecode.prmf;
 
/**
  * This class 'should' create a thread that will sleep for some time,
  * then before ending, it will interrupt the inputThread!!
  */
 
 public class TimerThread  implements Runnable{
 	Thread timer, inputThread;
 	final int daytime; // 3 minutes
 
 	public TimerThread(Thread inputThread, int daytime)
 	{
 		this.daytime = daytime;
 		this.inputThread = inputThread;
 		timer = new Thread(this);
 		timer.start(); 
 	}
 	
 	public TimerThread(Thread inputThread)
 	{
 		this(inputThread, 3*60000);
 	}
 	public void run()
 	{
 		try {
 			
 			Thread.sleep(daytime);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		//TODO this won't actually do anything unless the inputThread knows
 		// to check for interrupts.
 		inputThread.interrupt();
 	}
 }
