 package org.javaan;
 
 import java.util.Arrays;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.javaan.commands.ListClasses;
import org.javaan.commands.FindEntryMethods;
 import org.javaan.commands.ListDuplicates;
 import org.javaan.commands.ListInterfaces;
 
 /**
  * Javaan Command Line Client
  * @author behnkea
  */
 public class JavaanCli {
 	
 	private static final String HELP_COMMAND = "javaan <command> <files> <options>";
 	private static final String HELP_DESCRIPTION = 
 			  "javaan is a java byte code analyser for static code analysis.\n"
 			+ "Use javaan <command> --help to display help message for a specific command.\n"
 			+ "The command is followed by a list of jar files which should be processed\n"
 			+ "and the command options.";
 	
 	private static final String EXCEPTION_MISSING_FILES = "No file list provided";
 	private static final String EXCEPTION_UNKNOWN_COMMAND = "Unknown command: %s";
 	private static final String EXCEPTION_COULD_NOT_PARSE = "Could not parse command line arguments: %s";
	private static final String EXCEPTION_COMMAND = "Could not process command: %s";
 	
 	private final CommandMap commands;
 	
 	private final String[] args;
 	
 	public JavaanCli(String[] args, CommandMap commands) {
 		this.commands = commands;
 		this.args = args;
 	}
 
 	public static void main(String[] args) {
 		CommandMap commands = new CommandMap();
 		commands.addCommand(new ListClasses());
 		commands.addCommand(new ListDuplicates());
 		commands.addCommand(new ListInterfaces());
 		System.exit(new JavaanCli(args, commands).execute());
 	}
 	
 	private void setLoggerLevel(Level level) {
 		Logger logger = LogManager.getLogManager().getLogger("");
 		Handler[] handlers = logger.getHandlers();
 		for (Handler handler : handlers) {
 			handler.setLevel(level);
 		}
 		logger.setLevel(level);
 	}
 
 	public int execute() {
 		if (args.length < 1) {
 			printUsage();
 			return ReturnCodes.errorParse.getValue();
 		}
 		Command command = commands.getCommand(args[0]);
 		if (command == null) {
 			System.out.println(String.format(EXCEPTION_UNKNOWN_COMMAND, args[0]));
 			printUsage();
 			return ReturnCodes.errorParse.getValue();
 		}
 		Options options = new Options();
 		options.addOption("h", "help", false, "Display help information for this command");
 		options.addOption("v", "verbose", false, "Log verbose output");
 		options = command.buildCommandLineOptions(options);
 		try {
 			CommandLine cl = new GnuParser().parse(options, args);
 			if (cl.hasOption("h")) {
 				printUsage(command, options);
 				return 0;
 			}
 			
 			if (cl.hasOption("v")) {
 				setLoggerLevel(Level.FINEST);
 			} else {
 				setLoggerLevel(Level.SEVERE);
 			}
 			
 			String[] params = cl.getArgs();
 			if (params.length < 2) {
 				System.out.println(EXCEPTION_MISSING_FILES);
 				printUsage();
 				return ReturnCodes.errorParse.getValue();
 			}
 			String[] files = Arrays.copyOfRange(params, 1, params.length);
 			return command.execute(cl, files).getValue();
 		} catch(ParseException e) {
 			System.out.println(String.format(EXCEPTION_COULD_NOT_PARSE, e.getMessage()));
 			printUsage(command, options);
 			return ReturnCodes.errorParse.getValue();
 		} catch (Exception e) {
			System.out.println(String.format(EXCEPTION_COMMAND, e.getMessage()));
 			return ReturnCodes.errorCommand.getValue();
 		}
 	}
 	
 	private void printUsage() {
 		System.out.println();
 		System.out.println(HELP_COMMAND);
 		System.out.println();
 		System.out.println(HELP_DESCRIPTION);
 		System.out.println();
 		commands.printHelp();
 		System.out.println();
 	}
 	
 	private void printUsage(Command command, Options options) {
 		new HelpFormatter().printHelp(command.getHelpCommandLine(), options);
 	}
 }
