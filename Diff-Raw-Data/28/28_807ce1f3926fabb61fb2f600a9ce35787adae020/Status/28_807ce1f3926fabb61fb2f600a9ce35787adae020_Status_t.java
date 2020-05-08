 package localServices;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.Map;
 
 public enum Status {
 
 	Success(0),
 	
 	// File errors
 	FileDoesNotExist(10),
 	FileAlreadyExists(11),
 	FileTooLarge(30),
 	ErrorReadingFile(31),
 	ErrorWritingFile(32),
 	
 	// Network errors
 	OperationTimeout(20),
 	PendingRequest(21),
 	
 	// Transaction errors.
 	TransactionAborted(30),
	TransactionCommitted(31),
	InvalidTransaction(32),
	FileIsLocked(33);
 
 	
 	private static final Map<Integer,Status> lookup 
 		= new HashMap<Integer, Status>();
 
 	static {
 	    for(Status s : EnumSet.allOf(Status.class))
 	         lookup.put(s.getCode(), s);
 	}
 
 	private int code;
 
 	private Status(int code) {
 	    this.code = code;
 	}
 
 	public int getCode() { return code; }
 
 	public static Status parseInt(int code) { 
 	    return lookup.get(code); 
 	}
 }
