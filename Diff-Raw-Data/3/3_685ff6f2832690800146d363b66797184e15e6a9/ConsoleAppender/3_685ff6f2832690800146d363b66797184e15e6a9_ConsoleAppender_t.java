 package net.praqma.util.debug.appenders;
 
 import java.io.PrintWriter;
 
 public class ConsoleAppender extends Appender {
 	
 	public ConsoleAppender() {
 		super( new PrintWriter( System.out ) );
		setTemplate( "[%level] %space %message%newline" );
 	}	
 }
