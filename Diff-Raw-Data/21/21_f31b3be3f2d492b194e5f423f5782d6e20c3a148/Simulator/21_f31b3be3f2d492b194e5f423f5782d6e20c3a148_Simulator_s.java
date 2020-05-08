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
 
 
 public class Simulator {
 
 	private final SpringMassSystem system;
 	private final SimulationClock simulationClock;
 	private final SimulationParameters parameters;
 	
 	public Simulator(final SimulationParameters parameters) 
 	{
 		this.parameters = parameters;
 		this.system = new SpringMassSystemFactory().create(parameters);
 
 		this.simulationClock = new SimulationClock(parameters) {
 
 			private long tickCounter = 0;
 			
 			@Override
 			protected void tick() 
 			{
 				long stepTime = -System.currentTimeMillis();
 				
 				system.step();
 				
 				stepTime += System.currentTimeMillis();
 				
 				tickCounter++;
				if ( parameters.isDebugPerformance() && (tickCounter%30) == 0 ) {
					System.out.println("Simulation time: "+stepTime+" ms");
 				}
 			}
 			
 		};
 		simulationClock.start();		
 	}
 	
 	public void start() 
 	{
 		simulationClock.startClock();
 	}
 	
 	public boolean isRunning() {
 		return simulationClock.isClockRunning();
 	}
 	
 	public void destroy() 
 	{
 		simulationClock.destroy();
 		try 
 		{
             system.destroy();
         } 
 		catch (InterruptedException e) 
         {
             Thread.currentThread().interrupt();
         }
 	}
 	
 	public void stop() {
 		simulationClock.stopClock();
 	}	
 	
 	public SpringMassSystem getSpringMassSystem() {
 		return system;
 	}
 	
 	public SimulationParameters getSimulationParameters() {
 		return parameters;
 	}
 }
