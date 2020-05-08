 package eu.dm2e.ws;
 
 public enum ErrorMsg {
 	
 	NO_TOP_BLANK_NODE("No top blank node was found")
 	,
 	BAD_RDF("Could not parse RDF. Make sure the syntax is valid and the right content-type was sent.")
 	,
 	NO_JOB_STATUS("No job status was sent.")
 	,
 	INVALID_JOB_STATUS("Invalid job status. Must be one of [NOT_STARTED, STARTED, FINISHED, FAILED]")
 	,
	INVALID_LOG_LEVEL("Invalid log level. Must be one of [TRACE, DEBUG, INFO, WARN, FATAL].")
 	;
 	
 	private String message;
 	
 	private ErrorMsg(String msg) { this.message = msg; }
 	public String getMessage() { return message; }
 
 }
