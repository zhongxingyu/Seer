 package de.unihalle.sim.main;
 
 import org.apache.commons.cli.BasicParser;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 
 public class BeeCommandLineParser {
 
	Options _options;
	CommandLine _commandLine = null;
 
 	private static final int DEFAULT_NUMBER_OF_GROUPS = 1;
 	private static final int DEFAULT_GROUP_SIZE = 1;
 
 	private BeeCommandLineParser(String[] args) {
 		_options = new Options();
 		_options.addOption("h", "help", false, "usage information");
		_options.addOption("n", "number", true, "number of hive groups (int)");
		_options.addOption("s", "size", true, "number of hives in each group (int)");
 		_options.addOption("c", "controls", false, "display control panel");
 		_options.addOption("g", "gui", false, "display graphical user interface");
 		_options.addOption("r", "report", false, "generate report (into report.csv)");

 		CommandLineParser commandLineParser = new BasicParser();
 
 		try {
 			_commandLine = commandLineParser.parse(_options, args);
 		} catch (Exception e) {
 			displayHelp();
 		}
 
 		if (_commandLine.hasOption("h")) {
 			displayHelp();
 		}
 	}
 
 	public static BeeCommandLineParser parse(String[] args) {
 		return new BeeCommandLineParser(args);
 	}
 
 	private void displayHelp() {
 		HelpFormatter helpFormatter = new HelpFormatter();
 		helpFormatter.printHelp("java -jar BeeSimulation.jar <command> [<arg>]", _options);
 		System.exit(1);
 	}
 
 	public boolean showGui() {
 		return _commandLine.hasOption("g");
 	}
 
 	public boolean showControls() {
 		return _commandLine.hasOption("c");
 	}
 
 	public boolean generateReport() {
 		return _commandLine.hasOption("r");
 	}
 
 	public int getNumberOfGroups() {
 		String n = _commandLine.getOptionValue("n");
 		int numberOfGroups = DEFAULT_NUMBER_OF_GROUPS;
 		if (n != null) {
 			try {
 				numberOfGroups = Integer.parseInt(n);
 			} catch (Exception e) {
 				displayHelp();
 			}
 		}
 		return numberOfGroups;
 	}
 
 	public int getGroupSize() {
 		String s = _commandLine.getOptionValue("s");
 		int groupSize = DEFAULT_GROUP_SIZE;
 		if (s != null) {
 			try {
 				groupSize = Integer.parseInt(s);
 			} catch (Exception e) {
 				displayHelp();
 			}
 		}
 		return groupSize;
 	}
 }
