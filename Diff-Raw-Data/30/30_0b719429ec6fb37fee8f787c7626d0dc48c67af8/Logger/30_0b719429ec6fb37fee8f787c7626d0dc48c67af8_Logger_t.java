 package net.frontlinesms.messaging;
 
 import java.io.*;
 
 /** Quick stub of a logger to send things to the command line.  You'll want to replace this with something proper. */
 class Logger {
 	/** Set this to <code>true</code> if you want logging to be printed. */
 	private static final boolean DO_PRINTING = true;
 	/** Description of this logger */
 	private final String description;
 	
 	/** Create a new logger for the supplied class */
 	public Logger(Class<?> clazz) {
 		this.description = clazz.getSimpleName();
 	}
 
 	public void trace(String s) { out("TRACE", s); }
 	
 	public void debug(String s) { out("DEBUG", s); }
 	
 	public void info(String s) { out("INFO", s); }
 	public void info(String message, Throwable t) { out("INFO", message, t); }
 
 	public void warn(String message, Throwable t) { out("WARN", message, t); }
 	
 	private void out(String level, String message) { out(level, message, null); }
 	private void out(String level, String message, Throwable t) {
 		if(!DO_PRINTING) return;
 		PrintStream out = t == null ? System.out : System.err;
 		out.println("[" + Thread.currentThread().getName() + " : " + description + "] " + level + ": " + message);
 		if(t != null) t.printStackTrace();
 	}
 }
