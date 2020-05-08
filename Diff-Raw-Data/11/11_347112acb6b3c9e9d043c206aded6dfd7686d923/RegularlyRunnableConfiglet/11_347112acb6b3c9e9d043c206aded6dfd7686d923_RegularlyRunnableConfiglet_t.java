 package de.zib.gndms.kit.configlet;
 
 /*
  * Copyright 2008-2011 Zuse Institute Berlin (ZIB)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 
 import org.jetbrains.annotations.NotNull;
 
 import java.io.Serializable;
 
 
 /**
  * This abstract class extends the <tt>RunnableConfiglet</tt> class by a loop, which calls {@link #threadRun_()} concurrently.
  * An initial delay before the first execution and a delay after every execution can be denoted.
  *
  * Loop starts as soon as {@link #init(org.apache.commons.logging.Log, String, java.io.Serializable)} } is invoked.
  *
  *
  * @author  try ste fan pla nti kow zib
  * @version $Id$
  *
  *          User: stepn Date: 06.11.2008 Time: 18:30:51
  */
 public abstract class RegularlyRunnableConfiglet extends RunnableConfiglet {
 
     /**
      * delay in miliseconds after that the {@code run_()}'s loop-Method executes the first iteration.
      */
     private long initDelay;
     /**
      * delay in miliseconds after that the {@code run_()}'s loop-Method executes every iteration.
      */
     private long delay;
     /**
      * flag needed to stop loop
      */
     private volatile boolean stop;
 
 	@Override
 	protected synchronized void threadInit() {
 		configDelays();
 	}
 
 
 	@Override
 	public synchronized void update(@NotNull final Serializable data) {
 		super.update(data);
 		configDelays();
 	}
 
     /**
      * Sets the {@code delay} and {@code initDelay} according to the values as set in the current configuration. If not set,
      * {@code initDelay} will be set to 3 seconds and {@code delay} will be set to 5 seconds.Should be invoked by {@link RegularlyRunnableConfiglet#update(java.io.Serializable)} 
      */
 	private void configDelays() {
 		setInitDelay(getMapConfig().getLongOption("initialDelay", 3000L));
 		setDelay(getMapConfig().getLongOption("delay", 5000L));
 	}
 
 
     /**
      * Loop which invokes {@link RegularlyRunnableConfiglet#threadRun()} after {@code delay} seconds. Do not call this method directly !
      * Will be invoked by {@code RunnableConfiglet}'s {@code run()}-Method to run concurrently.
      */
     @Override
 	public void run_() {
		try { Thread.sleep(getInitDelay()); }
 		catch (InterruptedException e) { /* intentional */ }
 		while (! stop) {
			threadRun();
			try { Thread.sleep(getDelay()); }
 			catch (InterruptedException e) { /* intentional */ }
 		}
 
 	}
 
     /**
      *  This method is invoked by {@link RegularlyRunnableConfiglet#run_()}'s loop, will be executed concurrently  
      */
 	protected abstract void threadRun();
 
 
     @Override
 	protected void threadStop() {
 		stop = true;
 		getThread().interrupt();
 	}
 
     /**
      * Returns the initial delay for {@code run-()}'s loop in miliseconds
      *
      * @return the initial delay for {@code run-()}'s loop in miliseconds
      */
 	public synchronized long getInitDelay() {
 		return initDelay;
 	}
 
     /**
      * Sets the value for the inital Delay in miliseconds, after that {@code threadRun()} will be invoked for the first time}
      *
      * @param initDelayParam the value for the inital delay in miliseconds, after that {@code threadRun()} will be invoked for the first time}
      */
 	public synchronized void setInitDelay(final long initDelayParam) {
 		initDelay = initDelayParam;
 	}
 
     /**
     * Returns the delay for {@code run-()}'s loop in miliseconds
      *
      * @return the delay for {@code run-()}'s loop in miliseconds
      */
 	public synchronized long getDelay() {
 		return delay;
 	}
 
     /**
      * Sets the value for the delay in miliseconds, after that {@code threadRun()} will do the next interation in the loop
      *
      * @param delayParam the value for the delay in miliseconds, after that {@code threadRun()} will do the next interation in the loop
      */
 	public synchronized void setDelay(final long delayParam) {
 		delay = delayParam;
 	}
 }
