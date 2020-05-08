 package org.italiangrid.voms.clients;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.italiangrid.voms.VOMSError;
 import org.italiangrid.voms.clients.options.CLIOption;
 import org.italiangrid.voms.clients.options.CommonOptions;
 import org.italiangrid.voms.clients.options.ProxyInitOptions;
 import org.italiangrid.voms.clients.util.MessageLogger;
 import org.italiangrid.voms.clients.util.OptionsFileLoader;
 import org.italiangrid.voms.clients.util.VersionProvider;
 
 public abstract class AbstractCLI {
 	
 	public static final String DEFAULT_TMP_PATH = "/tmp";
 
 	/** The CLI options **/
 	protected Options cliOptions;
 	
 	/** The CLI options parser **/
 	protected CommandLineParser cliParser = new GnuParser();
 	
 	/** The parsed command line **/
 	protected CommandLine commandLine = null;
 	
 	/** The name of this command **/
 	protected String commandName;
 	
 	/** The logger used to print messages **/
 	protected MessageLogger logger;
 	
 	/** Be quiet when logging **/
 	boolean isQuiet = false;
 	
 	/** Be verbose when logging **/
 	boolean isVerbose = false;
 	
 	protected AbstractCLI(String commandName) {
 		this.commandName = commandName;
 	}
 	
 	protected final void displayUsage(){
 		
 		int lineWidth = 120;
 		String header = "options:";
 		String footer = "";
 
 		HelpFormatter helpFormatter = new HelpFormatter();
 		helpFormatter.printHelp(lineWidth, commandName+ " [options]", header,
 				cliOptions, footer);
 		
 	}
 	
 	protected final void parseOptionsFromCommandLine(String[] args){
 
 	  try {
 
 	    commandLine = cliParser.parse(cliOptions, args);
 	    if(commandLineHasOption(CommonOptions.CONF)) {
 	      parseOptionsFromFile(getOptionValue(CommonOptions.CONF));
 	    }
 		  
 	    setVerbosityFromCommandLine();
 			displayVersionIfRequested();
 			displayHelpIfRequested();
 
 		} catch (ParseException e) {
 			
 			System.err.println("Error parsing command line arguments: "
 					+ e.getMessage());
 			displayUsage();
 			System.exit(1);
 		}
 	}
 	
 	/**
 	 * Parse options from a file. Reload commandLine that after calling 
 	 * this method contains both options coming from the command line and
 	 * from the file.
 	 * 
 	 * @param optionFileName the options file
 	 * @throws ParseException if there an error parsing the options
 	 */
 	private void parseOptionsFromFile(String optionFileName) throws ParseException {
 
 	  List<String> options= OptionsFileLoader.loadOptions(optionFileName);
 	  options.addAll(commandLine.getArgList());
 	  
 	  commandLine = cliParser.parse(cliOptions, options.toArray(new String[0]));
 	}
 
   protected final void displayVersion(){
 		VersionProvider.displayVersionInfo(commandName);
 	}
 	
 	protected final void initOptions(List<CLIOption> options){
 	
 		cliOptions = new Options();
 		
 		for (CLIOption o: options)
 			cliOptions.addOption(o.getOption());
 	}
 
 
 
 	protected final boolean commandLineHasOption(CLIOption option) {
 	
 		return commandLine.hasOption(option.getLongOptionName());
 	}
 
 	/**
 	 * Checks the command line to see whether the display of this CLI
 	 * 
 	 * @param line
 	 */
 	protected final void displayHelpIfRequested() {
 	
 		if (commandLineHasOption(CommonOptions.HELP) 
 				|| commandLineHasOption(CommonOptions.USAGE)){
 			displayUsage();
 			System.exit(0);
 		}
 	}
 	
 	protected final void displayVersionIfRequested(){
 		if (commandLineHasOption(CommonOptions.VERSION)){
 			displayVersion();
 		}
 	}
 
 	protected final String getOptionValue(CLIOption option) {
 	
 		if (commandLineHasOption(option))
 			return commandLine.getOptionValue(option.getLongOptionName());
 		
 		return null;
 	}
 
 	protected final List<String> getOptionValues(CLIOption option) {
 	
 		if (commandLineHasOption(option)) {
 	
 			String[] values = commandLine.getOptionValues(option.getLongOptionName());
 			return Arrays.asList(values);
 		}
 	
 		return null;
 	}
 
 	protected final void setVerbosityFromCommandLine() {
 		
 		if (commandLineHasOption(CommonOptions.DEBUG))
 			isVerbose = true;
 		
 		if (commandLineHasOption(ProxyInitOptions.QUIET_MODE))
 			isQuiet = true;
 			
 		if (isVerbose && isQuiet)
 			throw new VOMSError("Try to understand us: this command cannot be verbose and quiet at the same time!");
 		
 		if (isVerbose)
 			logger = new MessageLogger(MessageLogger.VERBOSE);
 		
 		if (isQuiet)
 			logger = new MessageLogger(MessageLogger.QUIET);
 		
 		if (!isVerbose && !isQuiet)
 			logger = new MessageLogger();
 	}
 	
 	protected abstract void execute();
 }
