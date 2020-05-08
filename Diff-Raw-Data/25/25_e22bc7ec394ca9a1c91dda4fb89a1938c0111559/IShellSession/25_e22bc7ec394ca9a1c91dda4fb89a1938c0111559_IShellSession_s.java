 package com.buglabs.bug.base.pub;
 
 import java.io.IOException;
 
 
 /**
  * This is a shell session that's accessible from Java.  An instance of this service
  * represents a separate shell process running concurrently with the Java program.
  * 
  * @author kgilmer
  *
  */
 public interface IShellSession {
 
 	/**
 	 * @param command
 	 * @return stdout of command
 	 * @throws IOException if any data on stderr is produced.
 	 */
 	public String execute(String command) throws IOException;
 
 	/**
 	 * Execute a command and pass in a ICommandResponseHandler to deal with the output.
 	 * @param command
 	 * @param handler
 	 * @throws IOException
 	 */
 	public void execute(String command, ICommandResponseHandler handler) throws IOException;
 	
 	/**
 	 * Terminate the session and free any resources.
 	 */
 	public void dispose();
 	
 	/**
 	 * Interrupt any running programs.
 	 */
 	public void interrupt();
 }
