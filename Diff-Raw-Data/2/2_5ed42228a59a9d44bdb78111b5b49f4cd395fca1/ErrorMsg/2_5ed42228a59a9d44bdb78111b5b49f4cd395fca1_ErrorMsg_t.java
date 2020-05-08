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
 	,
 	NO_FILE_RETRIEVAL_URI("A file posted without content *must* contain a statement with 'omnom:fileRetrievalURI'.")
 	,
 	NO_FILE_AND_NO_METADATA("Form contains neither file data nor file metadata.")
 	,
 	NO_RESOURCE_OF_CLASS("No resource of this class was found.")
 	,
 	MORE_THAN_ONE_RESOURCE("There was more than one resource of the required type in the request.")
 	,
 	REQUIRED_PARAM_MISSING("Required input parameter is missing.")
 	,
 	ILLEGAL_PARAMETER_VALUE("Bad Value for this parameter.")
	,
	NOT_FOUND("Resource not found.")
 	;
 	
 	private String message;
 	
 	private ErrorMsg(String msg) { this.message = msg; }
 	public String getMessage() { return message; }
 	
 	@Override
 	public String toString() {
 		return name() + ": " + this.getMessage();
 	}
 
 }
