 package com.googlecode.prmf;
 
//TODO change this to a Javadoc comment. (i.e. start with /** end with */)
// In fact, add Javadocs throughout the code :P

/*
  * This class 'should' create a thread that will sleep for some time,
  * then before ending, it will interrupt the inputThread!!
  */
 
 public class TimerThread  implements Runnable{
 	Thread timer, inputThread;
	//TODO don't hardcode the length of the day, set it in constructor.
	// (You can still fall onto a default value if it's not given.)
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
			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		//TODO this won't actually do anything unless the inputThread knows
 		// to check for interrupts.
 		inputThread.interrupt();
 	}
 }
