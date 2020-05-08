 package adam.betts.tools;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 
 import adam.betts.calculations.IPGDatabase;
 import adam.betts.programs.Program;
 import adam.betts.programs.ProgramReader;
 import adam.betts.utilities.DefaultOptions;
 
 public class MainWCETAnalyser
 {
 
     private static Options options;
     private static Option observedWCETOption;
     private static Option expandContextsOption;
     private static Option incrementalWCETOption;
 
     private static String programFileName;
 
     public static void main (String[] args)
     {
         addOptions();
         parseCommandLine(args);
         run();
     }
 
     private static void addOptions ()
     {
         options = new Options();
         DefaultOptions.addDefaultOptions(options);
         DefaultOptions.addProgramOption(options);
         DefaultOptions.addRootOption(options, true);
         DefaultOptions.addInstrumentationProfileOption(options, true, 1);
         DefaultOptions.addTraceFileOption(options);
         DefaultOptions.addOutFileOption(options);
         DefaultOptions.addUDrawDirectoryOption(options);
         DefaultOptions.addIPEOptions(options);
 
         observedWCETOption = new Option("O", "observed", false,
                 "Perform a WCET computation of the observed paths in the trace.");
         observedWCETOption.setRequired(false);
         options.addOption(observedWCETOption);
 
         expandContextsOption = new Option("e", "expand", false,
                 "Consider all contexts expanded in the WCET computation.");
         expandContextsOption.setRequired(false);
         options.addOption(expandContextsOption);
 
         incrementalWCETOption = new Option("I", "incremental", false,
                 "Do a WCET computation after each run in the trace.");
         incrementalWCETOption.setRequired(false);
         options.addOption(incrementalWCETOption);
     }
 
     private static void parseCommandLine (String[] args)
     {
         final String toolName = "wcet.jar";
         CommandLineParser parser = new GnuParser();
         HelpFormatter formatter = new HelpFormatter();
         formatter.setWidth(128);
         CommandLine line = null;
         try
         {
             line = parser.parse(options, args);
 
             if (line.hasOption(DefaultOptions.helpOption.getOpt()))
             {
                 formatter.printHelp(toolName, options);
                 System.exit(1);
             }
             else
             {
                 /*
                  * Set the global variables according to the command-line
                  * parameters
                  */
                 DefaultOptions.setDefaultOptions(line);
                 DefaultOptions.setRootOption(line);
                 DefaultOptions.setInstrumentationProfileOption(line);
                 DefaultOptions.setTraceFileOption(line);
                 DefaultOptions.setOutFileOption(line);
                 DefaultOptions.setUDrawDirectoryOption(line);
                 DefaultOptions.setIPETOptions(line);
 
                 programFileName = line
                         .getOptionValue(DefaultOptions.programFileOption
                                 .getOpt());
 
                 Globals.observedWCET = line.hasOption(observedWCETOption
                         .getOpt());
                 Globals.expandContexts = line.hasOption(expandContextsOption
                         .getOpt());
                 Globals.incrementalWCET = line.hasOption(incrementalWCETOption
                         .getOpt());
             }
         }
         catch (ParseException e)
         {
             System.out.println(e.getMessage());
             formatter.printHelp(toolName, options);
             System.exit(1);
         }
     }
 
     private static void run ()
     {
         Program program = new Program();
        new ProgramReader(program, programFileName, false);
         new IPGDatabase(program);
     }
 
     public static class Globals
     {
 
         protected static boolean observedWCET;
         protected static boolean expandContexts;
         protected static boolean incrementalWCET;
 
         public final static boolean doObservedWCET ()
         {
             return observedWCET;
         }
 
         public final static boolean expandContexts ()
         {
             return expandContexts;
         }
 
         public final static boolean doIncrementalWCET ()
         {
             return incrementalWCET;
         }
     }
 }
