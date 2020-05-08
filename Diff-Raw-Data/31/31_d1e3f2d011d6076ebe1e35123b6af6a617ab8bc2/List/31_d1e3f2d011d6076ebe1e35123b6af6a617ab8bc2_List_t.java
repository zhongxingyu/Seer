 package jcolin.commands.testcase;
 
 import jcolin.Shell;
 import jcolin.commands.Command;
 import jcolin.consoles.Console;
 
 public class List extends Command {	
 	private static final String[] COMMAND_NAMES = { "list" };
 
 	public List() {
 		super(COMMAND_NAMES);
 	}
 
     public int numArgs() {
     	return 0;    		
     }
 
     public String commandLine() {
    		return name();
     }
 
     public String description(boolean detailed) {
         return name() + ": Lists all the testcases";
     }
 
     public Command clone(String[] args, int index) {
    	return new List();
     }
 
     public void execute(Shell shell, Object model, Console console) {
     }
 }
