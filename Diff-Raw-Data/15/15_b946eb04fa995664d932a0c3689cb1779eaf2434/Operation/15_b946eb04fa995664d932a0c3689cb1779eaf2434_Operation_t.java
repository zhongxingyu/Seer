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
 
 package radlab.rain.operation;
 
 import radlab.rain.load.LoadDefinition;
 import radlab.rain.scoreboard.IScoreboard;
 
 /**
  * The Operation class is a encapsulation of "tasks to be done". An operation contains all features of the state
  * necessary to execute at an arbitrary point in time (i.e. immediately or in the future). It follows the Command
  * pattern (GoF). It implements <code>Runnable</code> in case another thread has to execute it.
  */
 public abstract class Operation implements Runnable {
 
 	// Describes the operation
 	protected int operationIndex = -1;
 	protected String operationName;
 	protected String operationRequest;
 
 	// Describes who generated the operation and when (during what interval)
 	protected String generatedBy;
 
 	// Load definition in effect when this operation was generated/initialized
 	private LoadDefinition generatedDuringLoadDefinition;
 	protected long profileStartTime = -1;
 
 	// Reference to the generator that created this operation
 	protected Generator generatedByGenerator;
 
 	// Used to collect execution metrics
 	protected IScoreboard scoreboard;
 
 	// Identifies the generator within the track
 	private long generatorId = -1;
 
 	// Statistics
 	private long timeQueued;
 	private long timeStarted;
 	private long timeFinished;
 
 	private long thinkTimeUsed; // Track how much think time we used
 	private long cycleTimeUsed; // Track how much cycle delays we took advantage of
 
 	// Synchron or asynchron execution mode
 	private boolean async = false;
 	protected boolean enforceSync = false; // if order of operation execution is important
 
 	// Outcome of executing the operation
 	protected boolean failed = true;
 	protected Throwable failureReason;
 
 	// Counts number of actions (like http requests)
 	protected long numberOfActionsPerformed;
 
 	public Operation(IScoreboard scoreboard) {
 		this.scoreboard = scoreboard;
 	}
 
 	/**
 	 * This method is used to run this operation. By default, it records any metrics when executing. This can be
 	 * overridden to make a single call to <code>execute()</code> for more fine-grained control. This method must catch
 	 * any <code>Throwable</code>s.
 	 */
 	public void run() {
 		// Invoke the pre-execute hook here before we start the clock to time the operation's execution
 		preExecute();
 		setTimeStarted(System.currentTimeMillis());
 
 		try {
 			execute();
 		} catch (Throwable e) {
 			setFailed(true);
 			setFailureReason(e);
 		} finally {
 			setTimeFinished(System.currentTimeMillis());
 
 			// Invoke the post-execute hook here after we stop the clock to time the
 			// operation's execution
 			postExecute();
 
 			// Dump operation results into the scoreboard
 			OperationExecution result = new OperationExecution(this);
 			scoreboard.dropOffOperation(result);
 
 			// Run cleanup
 			cleanup();
 		}
 	}
 
 	public abstract void prepare();
 
	protected void preExecute() {
 	}
 
 	public abstract void execute() throws Throwable;
 
	protected void postExecute() {
 	}
 
 	public abstract void cleanup();
 
 	public void trace() {
 		numberOfActionsPerformed++;
 	}
 
 	public void trace(String msg) {
 		numberOfActionsPerformed++;
 	}
 
 	/**
 	 * Getter & Setters
 	 */
 
 	public int getOperationIndex() {
 		return this.operationIndex;
 	}
 
 	public String getOperationName() {
 		return this.operationName;
 	}
 
 	public long getTimeQueued() {
 		return this.timeQueued;
 	}
 
 	public void setTimeQueued(long val) {
 		this.timeQueued = val;
 	}
 
 	public long getTimeStarted() {
 		return this.timeStarted;
 	}
 
 	public void setTimeStarted(long val) {
 		this.timeStarted = val;
 	}
 
 	public long getTimeFinished() {
 		return this.timeFinished;
 	}
 
 	public void setTimeFinished(long val) {
 		this.timeFinished = val;
 	}
 
 	public long getThinkTimeUsed() {
 		return this.thinkTimeUsed;
 	}
 
 	public void setThinkTimeUsed(long val) {
 		this.thinkTimeUsed = val;
 	}
 
 	public long getCycleTimeUsed() {
 		return this.cycleTimeUsed;
 	}
 
 	public void setCycleTimeUsed(long val) {
 		this.cycleTimeUsed = val;
 	}
 
 	public boolean getAsync() {
 		return this.async;
 	}
 
 	public void setAsync(boolean val) {
 		this.async = val;
 	}
 
 	public String getGeneratedBy() {
 		return this.generatedBy;
 	}
 
 	public void setGeneratedBy(String val) {
 		this.generatedBy = val;
 	}
 
 	public LoadDefinition getGeneratedDuringProfile() {
 		return this.generatedDuringLoadDefinition;
 	}
 
 	public void setGeneratedByGenerator(Generator generator) {
 		this.generatedByGenerator = generator;
 	}
 
 	public void setGeneratedDuringProfile(LoadDefinition val) {
 		// Save the load profile
 		this.generatedDuringLoadDefinition = val;
 
 		// Save the time started now since the load manager thread updates this
 		// field - we can then use timestarted+intervalduration
 		// to see whether the operation finished during the interval
 		this.profileStartTime = val.getTimeStarted();
 	}
 
 	public long getProfileStartTime() {
 		return this.profileStartTime;
 	}
 
 	public boolean isFailed() {
 		return this.failed;
 	}
 
 	public void setFailed(boolean failed) {
 		this.failed = failed;
 	}
 
 	public Throwable getFailureReason() {
 		return this.failureReason;
 	}
 
 	public void setFailureReason(Throwable t) {
 		this.failureReason = t;
 	}
 
 	public long getActionsPerformed() {
 		return numberOfActionsPerformed;
 	}
 
 	public void setActionsPerformed(long val) {
 		this.numberOfActionsPerformed = val;
 	}
 
 	public long getGeneratorThreadID() {
 		return this.generatorId;
 	}
 
 	public void setGeneratorThreadID(long val) {
 		this.generatorId = val;
 	}
 
 }
