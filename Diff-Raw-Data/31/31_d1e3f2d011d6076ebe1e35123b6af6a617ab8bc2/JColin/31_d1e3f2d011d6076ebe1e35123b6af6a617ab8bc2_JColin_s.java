 package jcolin;
 
 import jcolin.commands.CommandFactory;
 import jcolin.consoles.Console;
 import jcolin.consoles.ConsoleFactory;
 
 public class JColin {
 	public int start(ModelFactory modelFactory, String[] args) {
 	    CommandFactory commandFactory = new CommandFactory();
 	    Console console = ConsoleFactory.buildConsole(commandFactory);
 		Shell shell = new Shell(console, commandFactory, modelFactory, args);
 		return shell.execute();
 	}
 	
 	static public void main(String[] args) {
 		JColin jcolin = new JColin();
		int returnCode = jcolin.start(null, args);
 		System.exit(returnCode);
 	}	
 }
