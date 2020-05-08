 package irc;
 
 public class Message implements Comparable<Message> {
 
 	private MessageTarget source;
 	private MessageTarget target;
 
 	private String command;
 
 	private String msg = "";
 
 	private String[] args;
 
 	private Priority priority;
 
 	private MessageCode code;
 
 	private MessageType type;
 
 	private String raw = "";
 
	Message() {}
 
 	/** 
 	 * Package private setters...
 	 */
 	void setArgs(String[] args) {
 		this.args = args;
 	}
 	
 	void setCommand(String command) {
 		this.command = command;
 	}
 
 	void setSource(MessageTarget source) {
 		this.source = source;
 	}
 
 	void setTarget(MessageTarget target) {
 		this.target = target;
 	}
 	
 	void setPriority(Priority priority) {
 		this.priority = priority;
 	}
 	
 	void setMessage(String msg) {
 		this.msg = msg;
 	}
 	
 	void setCode( MessageCode code ) {
 		this.code = code;
 	}
 
 	void setType( MessageType type ) {
 		this.type = type;
 	}
 
 	void setRaw(String raw) {
 		this.raw = raw;
 	}
 
 	public int numArgs() {
 		return args.length;
 	}
 
 	public String getArg(int n) {
 		return args[n];
 	}
 
 	public String[] getArgs() {
 		return args;
 	}
 
 	public String getCommand() {
 		return command;
 	}
 
 	public String getMessage() {
 		return msg;
 	}
 
 	public String getRaw() {
 		return raw;
 	}
 
 	public MessageTarget getSource() {
 		return source;
 	}
 
 	public MessageTarget getTarget() {
 		return target;
 	}
 
 	public Priority getPriority() {
 		return priority;
 	}
 
 	public MessageType getType() {
 		return type;
 	}
 
 	public MessageCode getCode() {
 		return code;
 	}
 
 	public int compareTo(Message m) {
		return priority.getValue() - m.priority.getValue();
 	}
 
 /*
 	public String toString() {
 		return source + " " + command + " " + target + " :"	+msg;
 	}
 */
 }
