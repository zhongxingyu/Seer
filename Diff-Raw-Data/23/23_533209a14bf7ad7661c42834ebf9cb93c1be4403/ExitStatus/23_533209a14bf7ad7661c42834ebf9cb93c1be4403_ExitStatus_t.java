 /*
  * Copyright 2006-2007 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.springframework.batch.repeat;
 
 import java.io.Serializable;
 
 /**
  * Value object used to carry information about the status of a
  * {@link RepeatOperations}.
  * 
  * @author Dave Syer
  * 
  */
 public class ExitStatus implements Serializable {
 
 	/**
 	 * Convenient constant value for when we detect that processing is underway.
 	 * Used mainly by asynchronous launchers.
 	 */
 	public static final ExitStatus RUNNING = new ExitStatus(true, "RUNNING");
 
 	/**
 	 * Convenient constant value representing unknown state - assumed continuable.
 	 */
 	public static final ExitStatus UNKNOWN = new ExitStatus(true, "UNKNOWN");
 
 	/**
 	 * Convenient constant value representing unfinished processing.
 	 */
 	public static final ExitStatus CONTINUABLE = new ExitStatus(true, "CONTINUABLE");
 
 	/**
 	 * Convenient constant value representing finished processing.
 	 */
 	public static final ExitStatus FINISHED = new ExitStatus(false, "COMPLETED");
 
 	/**
 	 * Convenient constant value representing finished processing with an error.
 	 */
 	public static final ExitStatus FAILED = new ExitStatus(false, "FAILED");
 
 	private final boolean continuable;
 
 	private final String exitCode;
 
 	private final String exitDescription;
 
 	public ExitStatus(boolean continuable) {
 		this(continuable, "", "");
 	}
 
 	public ExitStatus(boolean continuable, String exitCode) {
 		this(continuable, exitCode, "");
 	}
 
 	public ExitStatus(boolean continuable, String exitCode,
 			String exitDescription) {
 		super();
 		this.continuable = continuable;
 		this.exitCode = exitCode;
 		this.exitDescription = exitDescription;
 	}
 
 	/**
 	 * Flag to signal that processing can continue. This is distinct from any
 	 * flag that might indicate that a batch is complete, or terminated, since a
 	 * batch might be only a small part of a larger whole, which is still not
 	 * finished.
 	 * 
 	 * @return true if processing can continue.
 	 */
 	public boolean isContinuable() {
 		return continuable;
 	}
 
 	/**
 	 * Getter for the exit code (defaults to blank).
 	 * 
 	 * @return the exit code.
 	 */
 	public String getExitCode() {
 		return exitCode;
 	}
 
 	/**
 	 * Getter for the exit description (defaults to blank)
 	 * 
 	 * @return
 	 */
 	public String getExitDescription() {
 		return exitDescription;
 	}
 
 	/**
 	 * Create a new {@link ExitStatus} with a logical combination of the
 	 * continuable flag.
 	 * 
 	 * @param continuable
 	 *            true if the caller thinks it is safe to continue.
 	 * @return a new {@link ExitStatus} with {@link #isContinuable()} the
 	 *         logical and of the current value and the argument provided.
 	 */
 	public ExitStatus and(boolean continuable) {
 		return new ExitStatus(this.continuable && continuable, this.exitCode, this.exitDescription);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 		return "continuable=" + continuable + ";exitCode=" + exitCode
 				+ ";exitDescription=" + exitDescription;
 	}
 	
 	/**
 	 * Compare the fields one by one.
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	public boolean equals(Object obj) {
 		if (obj==null) {
 			return false;
 		}
 		return toString().equals(obj.toString());
 	}
 
 	/**
 	 * Add an exit code to an existing {@link ExitStatus}.
 	 * 
 	 * @param code the code to add
 	 * @return a new {@link ExitStatus} with the same properties but a new exit code.
 	 */
 	public ExitStatus addExitCode(String code) {
 		return new ExitStatus(continuable, code, exitDescription);
 	}
 
	/**
	 * Check if this status represents a running process.
	 * 
	 * @return tru eif the exit code is "RUNNING" or "UNKNOWN"
	 */
	public boolean isRunning() {
		return "RUNNING".equals(this.exitCode) || "UNKNOWN".equals(this.exitCode);
	}

 }
