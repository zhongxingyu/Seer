 package com.hsun324.ftplite;
 
 import java.io.IOException;
 
 public abstract class FTPCommand {
 	protected final Object executionSync = new Object();
 	private boolean executed = false;
 	public final void setExecuted() {
 		executed = true;
 	}
 	public final boolean isExecuted() {
 		return executed;
 	}
 	public final void push(FTPState state) throws IOException {
 		if (isExecuted()) throw new IllegalStateException();
 		execute(state);
 		if (!canBeReused()) setExecuted();
 	}
 	
 	public boolean canBeReused() {
 		return false;
 	}
 
 	public void execute(FTPState state) throws IOException {
 		StringBuilder builder = new StringBuilder(getCommandContent(state));
 		System.out.println("> " + builder.toString());
 		state.client.getCommandStream().write(builder.append(FTPCharset.NEWLINE).toString().getBytes(FTPCharset.ASCII));
 	}
 	public String getCommandContent(FTPState state) {
 		return "NOOP";
 	}
 	
 	public void quitExecution() { }
 	
 	public boolean isValidContext(FTPState state) {
 		return true;
 	}
 	public final boolean pushResponse(FTPState state, FTPResponse response) {
 		synchronized (executionSync) {
 			FTPFuture future = state.currentFuture;
 			if (future == null) return false;
 			
 			FTPResult result = handleResponse(state, response);
 			if (result != null) {
				future.setResult(result);
 				return true;
 			}
 			return false;
 		}
 	}
 	public abstract FTPResult handleResponse(FTPState state, FTPResponse response);
 }
