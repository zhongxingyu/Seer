 package net.praqma.util.execute;
 
 public class AbnormalProcessTerminationException extends RuntimeException {
 	
 	private int exitValue = 0;
 	private String command;
 
 	public AbnormalProcessTerminationException( String s, String command ) {
 		super( s );
 		this.command = command;
 	}
 	
 	public AbnormalProcessTerminationException( String s, String command, int exitValue ) {
 		super( s );
 		
 		this.exitValue = exitValue;
 		this.command = command;
 	}
 	
	public AbnormalProcessTerminationException( String s, String command, Exception e ) {
		super( s, e );
		this.command = command;
	}
	
	public AbnormalProcessTerminationException( String s, String command, int exitValue, Exception e ) {
		super( s, e );
		
		this.exitValue = exitValue;
		this.command = command;
	}
	
 	public int getExitValue() {
 		return exitValue;
 	}
 	
 	public String getCommand() {
 		return command;
 	}
 
 }
