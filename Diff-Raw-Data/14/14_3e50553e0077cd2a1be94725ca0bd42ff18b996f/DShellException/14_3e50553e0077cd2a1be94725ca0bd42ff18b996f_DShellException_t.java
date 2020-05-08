 package org.GreenTeaScript.DShell;
 
 public class DShellException extends Exception {
 	private static final long serialVersionUID = 1L;
 
 	public DShellException(String message) {
 		super(message);
 	}
	
	@Override public Throwable fillInStackTrace() {
		return this;
	}
 	// this exception is raised by GtSubProc.runCommand 
 	// when syscall trace is disabled
 }
