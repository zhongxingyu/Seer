 /**
  * Copyright 2012 Tobias Gierke <tobias.gierke@code-sourcery.de>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package de.codesourcery.springmass.springmass;
 
 import java.util.concurrent.CountDownLatch;
 
 public abstract class SimulationClock extends Thread 
 {
 	private final Object LOCK = new Object();
 	private final SimulationParameters parameters;
 
 	private boolean runSimulation = false;
 	private volatile boolean terminate;
 	private final CountDownLatch threadTermination = new CountDownLatch(1);
 
 	public SimulationClock(SimulationParameters parameters) {
 		setDaemon(true);
 		this.parameters = parameters;
 	}
 
 	public void stopClock() 
 	{
 		synchronized( LOCK ) 
 		{
 			if ( runSimulation ) 
 			{
 				runSimulation = false;
 			} 
 		}
 	}
 
 	public void destroy() 
 	{
 		terminate = true;
 		synchronized( LOCK ) 
 		{
 			LOCK.notifyAll();
 		}
 		try 
 		{
 			threadTermination.await();
 		} 
 		catch (InterruptedException e) 
 		{
 			Thread.currentThread().interrupt();
 		}
 	}
 
 	public void startClock() 
 	{
 		synchronized( LOCK ) 
 		{
 			if ( ! runSimulation ) 
 			{
 				runSimulation = true;
 				LOCK.notifyAll();
 			}
 		}
 	}	
 
 	public boolean isClockRunning() {
 		synchronized( LOCK ) 
 		{
 			return runSimulation;
 		}
 	}
 
 	@Override
 	public void run() 
 	{
 		try {
 			while(true) 
 			{
 				synchronized (LOCK) 
 				{
 					while( ! runSimulation && ! terminate) 
 					{
 						try {
 							System.out.println("Simulation thread sleeping");
 							LOCK.wait();
 							System.out.println("Simulation thread woke up");
 						} catch (InterruptedException e) {
 						}
 					}
 				}
 				
 				if ( terminate ) {
 					break;
 				}
 
 				tick();
 
 				try {
 					Thread.sleep(parameters.getFrameSleepTime());
 				} 
 				catch (InterruptedException e) {
 				}
 			}
 		} finally {
 			threadTermination.countDown();
 		}
 	}
 
 	protected abstract void tick();
 }
