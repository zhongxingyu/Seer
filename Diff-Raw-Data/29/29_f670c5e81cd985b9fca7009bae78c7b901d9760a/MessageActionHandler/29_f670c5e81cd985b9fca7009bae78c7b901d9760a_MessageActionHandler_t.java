 package eai.msejdf.jbpm.actions;
 
 import org.jbpm.graph.def.ActionHandler;
 import org.jbpm.graph.exe.ExecutionContext;
 
 public class MessageActionHandler implements ActionHandler {
 
 	private static final long serialVersionUID = 1L;
 	
 	/**
 	 * The message member gets its value from the configuration in the 
 	 * processdefinition. The value is injected directly by the engine. 
 	 */
 	String message;
 
 	/**
 	 * A message process variable is assigned the value of the message
 	 * member. The process variable is created if it doesn't exist yet.
 	 */
 	public void execute(ExecutionContext context) throws Exception {
		System.out.println("MessageActionHandler...");
 		System.out.println("MessageActionHandler: " + message);
		System.out.println("MessageActionHandler...Complete");
 	}
 
 }
