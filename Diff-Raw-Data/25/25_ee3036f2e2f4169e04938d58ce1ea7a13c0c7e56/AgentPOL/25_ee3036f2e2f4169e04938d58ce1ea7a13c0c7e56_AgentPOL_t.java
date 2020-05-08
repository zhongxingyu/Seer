 /*
  * Copyright (c) 2010, Regents of the University of California
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  *  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of California, Berkeley
  * nor the names of its contributors may be used to endorse or promote
  * products derived from this software without specific prior written
  * permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package radlab.rain.agent;
 
 import java.util.Random;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import radlab.rain.UnexpectedDeathHandler;
 import radlab.rain.load.LoadDefinition;
 import radlab.rain.operation.Generator;
 import radlab.rain.operation.IOperation;
 import radlab.rain.operation.OperationExecution;
 import radlab.rain.scoreboard.IScoreboard;
 
 /**
  * Provides the main loop of the agent thread.
  */
 public class AgentPOL extends Agent {
 	private static Logger logger = LoggerFactory.getLogger(AgentPOL.class);
 
 	// Scoreboard reference
 	protected IScoreboard scoreboard;
 
 	// The generator used to create operations for this thread
 	private Generator generator;
 
 	// The probability of using open loop vs. closed loop
 	private double openLoopProbability;
 
 	// The random number generator used to decide which loop to use
 	private Random random = new Random();
 
 	// Statistic: number of synchronous and asynchronous operations run
 	private long synchOperationsCount = 0;
 	private long asynchOperationsCount = 0;
 
 	// Interrupted
 	private boolean interrupted = false;
 
 	public AgentPOL(long targetId, long id) {
 		super(targetId, id);
 		Thread.setDefaultUncaughtExceptionHandler(new UnexpectedDeathHandler());
 	}
 
 	/**
 	 * Decides whether this thread is active or not. The number of threads is equal to the maximum number of generators.
 	 * The actual number of threads varies over time. This is achieved by blocking some threads. If a thread is blocked
 	 * is decided by this function.
 	 */
 	private boolean isActive() {
 		LoadDefinition loadProfile = loadManager.getCurrentLoadProfile();
 		return (id < loadProfile.getNumberOfUsers());
 	}
 
 	@Override
 	public void interrupt() {
 		interrupted = true;
 	}
 
 	@Override
 	public void dispose() {
 		this.generator.dispose();
 	}
 
 	private void triggerNextOperation(int lastOperationIndex) throws InterruptedException {
 		threadState = ThreadStates.Active;
 
 		// Generate next operation using the attached generator
 		IOperation nextOperation = generator.nextRequest(lastOperationIndex);
 
 		// Execute operation
 		if (nextOperation != null) {
 			// Set operation references
 			nextOperation.setLoadDefinition(loadManager.getCurrentLoadProfile());
 
			// Prepare the operation
 			nextOperation.prepare();
 
 			// Update last operation index.
 			lastOperationIndex = nextOperation.getOperationIndex();
 
 			// EXECUTE OPERATION
 			// Decide whether to do things open or closed (throw a coin)
 			double randomDouble = random.nextDouble();
 			if (randomDouble <= openLoopProbability)
 				doAsyncOperation(nextOperation);
 			else
 				doSyncOperation(nextOperation);
 		}
 	}
 
 	/**
 	 * Load generator loop. Runs the generator for each cycle and executes the returned operation.
 	 */
 	public void run() {
 		logger.info("New agent thread " + super.id);
 
 		try {
 			// Sleep until its time to start
 			sleepUntil(timing.start);
 
 			// Last executed operation (required to run markov chains)
 			int lastOperationIndex = -1;
 
 			// Check if benchmark is still running
 			while (System.currentTimeMillis() <= timing.endRun && !interrupted) {
 				// If generator is not active
 				if (!isActive()) {
 					threadState = ThreadStates.Inactive;
 					// Sleep for 1 second and check active state again
 					Thread.sleep(1000);
 				} else { // Generator is active

					// IMPORTANT: Next operation is triggered here
 					triggerNextOperation(lastOperationIndex);

 				}
 			}
 
 			logger.info("Agent ended - interrupted: " + interrupted);
 
 		} catch (InterruptedException ie) {
 			logger.error("Load generation thread interrupted exiting!");
 		} catch (Exception e) {
 			logger.error("Load generation thread died by exception! Reason: " + e.toString());
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Runs the provided operation asynchronously and sleeps this thread on the cycle time.
 	 */
 	private void doAsyncOperation(IOperation operation) throws InterruptedException {
 		// Update operation counters
 		asynchOperationsCount++;
 
 		// Calculate timings
 		long cycleTime = generator.getCycleTime();
 		long now = System.currentTimeMillis();
 
 		// Wait after operation execution
 		cycleTime = waitUntil(now, cycleTime);
 
 		// Set async flag
 		operation.setAsync(true);
 
 		// Trigger operation
 		doOperation(operation);
 
 		// Save the cycle time - if we're in the steady state
 		scoreboard.dropOffWaitTime(now, operation.getOperationName(), cycleTime);
 	}
 
 	private final class DropoffHandler implements Runnable {
 
 		private final IOperation wrapped;
 
 		DropoffHandler(IOperation wrapped) {
 			this.wrapped = wrapped;
 		}
 
 		@Override
 		public void run() {
 			OperationExecution result = wrapped.run();
 			scoreboard.dropOffOperation(result);
 		}
 	}
 
 	@Override
 	protected void submitAsyncOperation(IOperation operation) {
 		DropoffHandler handler = new DropoffHandler(operation);
 		executorService.submit(handler);
 	}
 
 	@Override
 	protected void runSyncOperation(IOperation operation) {
 		OperationExecution result = operation.run();
 		scoreboard.dropOffOperation(result);
 	}
 
 	/**
 	 * Runs the provided operation synchronously and sleeps this thread on the think time.
 	 */
 	private void doSyncOperation(IOperation operation) throws InterruptedException {
 		// Update operation counters
 		synchOperationsCount++;
 
 		// Configure operation
 		operation.setAsync(false);
 
 		// Trigger operation
 		doOperation(operation);
 
 		// Calculate timings
 		long thinkTime = generator.getThinkTime();
 		long now = System.currentTimeMillis();
 
 		// Wait after operation execution
 		thinkTime = waitUntil(now, thinkTime);
 
 		// Save the think time
 		scoreboard.dropOffWaitTime(now, operation.getOperationName(), thinkTime);
 	}
 
 	private long waitUntil(long now, long deltaTime) throws InterruptedException {
 		long wakeUpTime = now + deltaTime;
 		if (wakeUpTime > timing.endRun) {
 			if (now < timing.start) {
 				deltaTime = timing.startSteadyState - now;
 				sleepUntil(timing.startSteadyState);
 			} else {
 				deltaTime = timing.endRun - now;
 				sleepUntil(timing.endRun);
 			}
 		} else {
 			sleepUntil(wakeUpTime);
 		}
 
 		return deltaTime;
 	}
 
 	private void sleepUntil(long time) throws InterruptedException {
 		long preRunSleep = time - System.currentTimeMillis();
 		if (preRunSleep > 0)
 			Thread.sleep(preRunSleep);
 	}
 
 	public void setOpenLoopProbability(double openLoopProbability) {
 		this.openLoopProbability = openLoopProbability;
 	}
 
 	@Override
 	public void setScoreboard(IScoreboard scoreboard) {
 		this.scoreboard = scoreboard;
 	}
 
 	@Override
 	public void setGenerator(Generator generator) {
 		this.generator = generator;
 	}
 }
