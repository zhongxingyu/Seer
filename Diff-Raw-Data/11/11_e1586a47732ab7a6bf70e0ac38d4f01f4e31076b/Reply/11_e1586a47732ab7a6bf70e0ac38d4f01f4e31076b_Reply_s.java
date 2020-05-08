 package evaluationserver.server.execution;
 
 public enum Reply {
 
 	ACCEPTED("AC", "Accepted", true),
 	COMPILE_ERROR("CE", "Compile error", false),
 	PRESENTATION_ERROR("PE", "Presentation error", false),
 	WRONG_ANSWER("WA", "Wrong answer", false),
 	RUNTIME_ERROR("RE", "Runtime error", false),
 	TIME_LIMIT_EXCEEDED("TE", "Time limit exceeded", false),
 	MEMORY_LIMIT_EXCEEDED("ME", "Memory limit exceeded", false),
 	OUTPUT_LIMIT_EXCEEDED("OL", "Output limit exceeded", false),
	RESTRICTED_FUNCTION("RF", "Restricted function", false);
 	
 	private final String code;
 	private final String name;
 	private final boolean accepting;
 
 	private Reply(String code, String name, boolean accepting) {
 		this.code = code;
 		this.name = name;
 		this.accepting = accepting;
 	}
 
 	public boolean isAccepting() {
 		return accepting;
 	}
 
 	public String getCode() {
 		return code;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public String toString() {
 		return name;
 	}
 	
 	public static Reply fromCode(String code) throws IllegalArgumentException {
 		code = code.toUpperCase().trim();
 		for(Reply r : Reply.values())
 			if(r.getCode().equals(code))
 				return r;
 		throw new IllegalArgumentException("Invalid result code " + code);
 	}
 	
 }
