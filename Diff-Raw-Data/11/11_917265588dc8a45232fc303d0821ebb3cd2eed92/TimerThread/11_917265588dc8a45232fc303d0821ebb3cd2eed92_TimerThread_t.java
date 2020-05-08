 /*
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>
  */
 package com.googlecode.prmf.corleone.game.util;
 
 import com.googlecode.prmf.corleone.connection.IOThread;
 
 /**
  * This class 'should' create a thread that will sleep for some time,
  * then before ending, it will interrupt the inputThread!!
  */
 
 public class TimerThread  implements Runnable{
 	private Thread timer;
 	private IOThread inputThread;
 	private final int daytime;
 
 	public TimerThread(IOThread inputThread, int daytime) {
 		this.daytime = daytime;
 		this.inputThread = inputThread;
 		timer = new Thread(this, "Timer");
 	}
 	
 	public TimerThread(IOThread inputThread) {
		this(inputThread, 40*1000);
 	}
 	
 	public void run()
 	{
 		try {
 			Thread.sleep(daytime);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 		// TODO instead of returning if the thread is interrupted why not just
 		// say to send the message and cease the timer if the thread is NOT
 		// interrupted
 		if(Thread.interrupted())
 			return;
 
 		inputThread.sendMessage(inputThread.getChannel(), "The town was not able to reach a consensus.");
 		inputThread.ceaseTimer();
 	}
 	
 	// TODO this method should not exist
 	public Thread getTimer()
 	{
 		return timer;
 	}
 }
 
