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
 
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import radlab.rain.Timing;
 import radlab.rain.load.LoadDefinition;
 import radlab.rain.load.LoadManager;
 import radlab.rain.operation.Generator;
 import radlab.rain.operation.Operation;
 
 public abstract class Agent extends Thread implements IAgent {
 	private static Logger logger = LoggerFactory.getLogger(Agent.class);
 
 	// Identifier (strategies are numbered ascending)
 	protected long targetId;
 	protected long id;
 
 	// Track configuration
 	protected Timing timing;
 
 	// Reference to the load manager
 	protected LoadManager loadManager;
 
 	// The generator used to create operations for this thread
 	protected Generator generator;
 
 	// Thread state is used to block some threads in order to adapt the
 	// active number of load generating units
 	public enum ThreadStates {
 		Initialized, Active, Inactive
 	}
 
 	// The current state of this thread
 	protected ThreadStates threadState = ThreadStates.Initialized;
 
 	// Determine whether we async requests should be limited/throttled down to a max of x/sec
 	protected long sendNextRequest = -1;
 	protected LoadDefinition lastLoadUnit = null;
 
 	// The shared pool of worker threads
 	protected ExecutorService executorService = Executors.newCachedThreadPool();
 
 	/**
 	 * Create new load generating unit
 	 */
 	public Agent(long targetId, long id) {
 		this.targetId = targetId;
 		this.id = id;
 		setName("Agent-" + targetId + "(" + id + ")");
 	}
 
 	public void dispose() {
 		this.generator.dispose();
 	}
 
 	private void runAsyncOperation(Operation operation) {
 		LoadDefinition loadUnit = operation.getGeneratedDuringProfile();
 
 		// Load unit unspecified - execute operation immediately
 		if (loadUnit == null) {
 			executorService.submit(operation);
 			return;
 		}
 
 		// No rate limiting - execute operation immediately
 		long aggRatePerSec = loadUnit.openLoopMaxOpsPerSec;
 		if (aggRatePerSec == 0) {
 			executorService.submit(operation);
 			return;
 		}
 
 		// Rate limited send
 		long activeUsers = loadUnit.numberOfUsers;
 		long now = System.currentTimeMillis();
 
 		// Is it time to send the request?
 		if (now >= sendNextRequest) {
 			double myRate = (aggRatePerSec) / (double) activeUsers;
 			if (myRate <= 0)
 				myRate = 1000.0;
 			double waitIntervalMsecs = (1000.0 / myRate);
 			sendNextRequest = System.currentTimeMillis() + (long) waitIntervalMsecs;
 
 			// Submit operation
			operation.preExecute(); 
 			executorService.submit(operation);
 		} else {
 			long sleepTime = sendNextRequest - now;
 			try {
 				Thread.sleep(sleepTime);
 			} catch (InterruptedException e) {
 				logger.error(getName() + " interrupted from sleep", e);
 			}
 
 			// Submit operation
			operation.preExecute();
 			executorService.submit(operation);
 		}
 	}
 
 	private void runSyncOperation(Operation operation) {
		operation.preExecute();
 		operation.run();
 	}
 
 	@Override
 	public void doOperation(Operation operation) {
 		// Set the time the operation was queued (not how long it takes).
 		operation.setTimeQueued(System.currentTimeMillis());
 
 		if (!operation.getAsync()) { // Synchronous mode
 			runSyncOperation(operation);
 		} else { // Asynchronous mode
 			runAsyncOperation(operation);
 		}
 	}
 
 	@Override
 	public void setTiming(Timing timing) {
 		this.timing = timing;
 	}
 
 	@Override
 	public void setLoadManager(LoadManager loadManager) {
 		this.loadManager = loadManager;
 	}
 
 	@Override
 	public void setGenerator(Generator generator) {
 		this.generator = generator;
 	}
 }
