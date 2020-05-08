 package jcolin;
 
 import jcolin.cli.CLI;
 import jcolin.cli.CLIBuilder;
 import jcolin.commands.CommandFactory;
 import jcolin.consoles.Console;
 import jcolin.consoles.ConsoleFactory;
 import jcolin.shell.Shell;
 
 public class JColin {
 	public int start(ModelFactory modelFactory, String[] args) {
 		try {
 			Console console = ConsoleFactory.buildConsole();
 			CLI cli = new CLIBuilder(console).build();
 
 			CommandFactory commandFactory = new CommandFactory();
 			commandFactory.initialise(cli);
 			
 			console.initialise(commandFactory);
 
 			Shell shell = new Shell(console, commandFactory, modelFactory, 
 					args, cli.getToolName(), cli.getVersion(), cli.getPrompt());
 			return shell.execute();
 			
 		} catch (Exception e) {
 			System.out.printf("Exception Detected: %s\n", e.getMessage());
 			return 1;
 		}		
 	}
 	
 	static public void main(String[] args) {
 		JColin jcolin = new JColin();	
 		int returnCode = jcolin.start(new ModelFactory() {			
 			@Override
 			public Object createModel() {
 				return null;
 			}
 		}, args);
 		System.exit(returnCode);
 	}
 }
