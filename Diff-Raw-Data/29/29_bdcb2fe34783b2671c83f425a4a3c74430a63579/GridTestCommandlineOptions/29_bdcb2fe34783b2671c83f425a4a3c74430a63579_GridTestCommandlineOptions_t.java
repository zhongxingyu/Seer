 package org.vpac.grisu.client.gridTests;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 
 public class GridTestCommandlineOptions {
 	
 	private CommandLine line = null;
 	private HelpFormatter formatter = new HelpFormatter();
 	private Options options = null;
 	
 	private String[] fqans;
 	private String[] gridTestNames = new String[]{};
 	private String[] excludes = new String[]{};
 	private String[] includes = new String[]{};
 	private String myproxyUsername;
 	private String url;
 	private String output;
 	private int timeout = 240;
 	private boolean list = false;
 	private int threads = 5;
 	
 	public String[] getFqans() {
 		return fqans;
 	}
 	
 	public int getSimultaneousThreads() {
 		return threads;
 	}
 
 	public String[] getGridTestNames() {
 		return gridTestNames;
 	}
 
 	public String getMyproxyUsername() {
 		return myproxyUsername;
 	}
 	
 	public String getServiceInterfaceUrl() {
 		return url;
 	}
 	
 	public String getOutput() {
 		return output;
 	}
 	
 	public String[] getExcludes() {
 		return excludes;
 	}
 	
 	public String[] getIncludes() {
 		return includes;
 	}
 	
 	public int getTimeout() {
 		return timeout;
 	}
 	
 	public boolean listTests() {
 		return list;
 	}
 
 
 	public GridTestCommandlineOptions(String[] args) {
 		this.formatter.setLongOptPrefix("--");
 		this.formatter.setOptPrefix("-");
 		this.options = getOptions();
 		parseCLIargs(args);
 	}
 	
 	private void parseCLIargs(String[] args) {
 		
 		// create the parser
 		CommandLineParser parser = new PosixParser();
 		try {
 			// parse the command line arguments
 			line = parser.parse(this.options, args);
 		} catch (ParseException exp) {
 			// oops, something went wrong
 			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
 			formatter.printHelp("grisu-client", this.options);
 			System.exit(1);
 		}
 		
 		String[] arguments = line.getArgs();
 		
 		if ( arguments.length > 0 ) {
 			if ( arguments.length == 1 ) {
 				System.err.println("Unknown argument: "+arguments[0]);
 			} else {
 				StringBuffer buf = new StringBuffer();
 				for ( String arg : arguments ) {
 					buf.append(arg+" ");
 				}	
 				System.err.println("Unknown argument: "+buf.toString());
 			}
 			formatter.printHelp("grisu-grid-test", this.options);
 			System.exit(1);
 		}
 		
 		if ( line.hasOption("help") ) {
 			formatter.printHelp("grisu-grid-test", this.options);
 			System.exit(0);
 		}
 		
 		if ( line.hasOption("list") ) {
 			list = true;
 		}
 		
 		if (line.hasOption("tests")) {
 			gridTestNames = line.getOptionValue("tests").split(",");
 		}
 		
 		if (!line.hasOption("vos")) {
 			fqans = new String[]{};
 		} else {
 			fqans = line.getOptionValue("vos").split(",");
 		}
 		
 		if (line.hasOption("username")) {
 			myproxyUsername = line.getOptionValue("username");
 		}
 		
 		if (line.hasOption("output")) {
 			output = line.getOptionValue("output");
 		}
 		
 		if (line.hasOption("exclude")) {
 			excludes = line.getOptionValue("exclude").split(","); 
 		}
 		if (line.hasOption("include")) {
 			includes = line.getOptionValue("include").split(","); 
 		}
 		
 		if (line.hasOption("cancel")) {
 			try {
 				timeout = Integer.parseInt(line.getOptionValue("cancel"));
 			} catch (Exception e) {
 				System.err.println("Cancel value not an integer.");
 				formatter.printHelp("grisu-grid-test", this.options);
 				System.exit(1);
 			}
 		}
 		if (line.hasOption("simultaneousThreads")) {
 			try {
 				threads = Integer.parseInt(line.getOptionValue("simultaneousThreads"));
 			} catch (Exception e) {
 				System.err.println("SimultaneousThreads value is not an integer.");
 				formatter.printHelp("grisu-grid-test", this.options);
 				System.exit(1);
 			}
 		}
 		
 	}
 	
 
 	// helper methods
 
 	// option with long name, no arguments
 	private static  Option createOption(String longName,String description){
 		return OptionBuilder.withLongOpt(longName).withDescription(description).create();
 	}
 
 	// option with long name, has arguments
 	private  static Option createOptionWithArg(String longName, String description){
 		return OptionBuilder.withArgName(longName).hasArg().withLongOpt(longName).withDescription(description).create();
 	}
 
 	// option with long name, short name, no arguments
 	private static Option createOption(String  longName, String shortName, String description){
 		return OptionBuilder.withLongOpt(longName).withDescription(description).create(shortName);
 	}
 
 	// option with  long name,short name and argument
 	private static Option  createOptionWithArg(String longName, String shortName, String description){
 		return OptionBuilder.withArgName(longName).hasArg().withLongOpt(longName).withDescription(description).create(shortName);
 	}
 	
 	private static Options getOptions() {
 		
 		Options options = null;
 		
 		// common options
		Option apps = createOptionWithArg("tests", "t", "the names of the tests to run (seperated with a comma). If not specified, all tests will run.");				    
 		Option myProxyUsername = createOptionWithArg("username", "u", "the myproxy username to use");
 		Option fqan = createOptionWithArg("vos", "v", "the vos to use, seperated with a comma");
 		Option outputFile = createOptionWithArg("output", "o", "the output file");
 		Option exclude = createOptionWithArg("exclude", "e", "(comma-seperated) filters to exclude certain hostnames/queues. Only used if the \"include\" option wasn't specified");
 		Option include = createOptionWithArg("include", "i", "(comma-seperated) filters to only include certain hostnames");
 		Option timeoutInMinutes = createOptionWithArg("cancel", "c", "timeout in minutes after which all jobs that aren't finished are getting killed (default: 240)");
 		Option list = createOption("list", "l", "list all available tests");
 		Option threads = createOptionWithArg("simultaneousThreads", "s", "how many jobs to submit at once. Default is 5 (which is recommended)");
 		Option help = createOption("help", "h", "this help text");
 		
 		options = new Options();
 		options.addOption(apps);
 		options.addOption(myProxyUsername);
 		options.addOption(fqan);
 		options.addOption(outputFile);
 		options.addOption(include);
 		options.addOption(exclude);
 		options.addOption(timeoutInMinutes);
 		options.addOption(list);
 		options.addOption(threads);
 		options.addOption(help);
 		return options;
 	}
 	
 }
