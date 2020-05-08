 package jhttpcrowler;
 
 import jhttpcrowler.core.Engine;
 import jhttpcrowler.core.impl.EngineFactoryImpl;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Main class to run jhttpcrowler from console.
  *
  * @author Sergey Prilukin
  */
 public class Runner {
 
     public static final String SCRIPT_OPTION_SHORT_NAME = "s";
     public static final String SCRIPT_OPTION_LONG_NAME = "script";
     public static final String SCRIPT_OPTION_DESCRIPTION = "Path to the script";
     public static final String SCRIPT_OPTION_ARG_NAME = "script";
     public static final String SCRIPT_TYPE_OPTION_SHORT_NAME = "t";
     public static final String SCRIPT_TYPE_OPTION_LONG_NAME = "type";
     public static final String SCRIPT_TYPE_OPTION_DESCRIPTION = "Type of the script. for example: ruby, groovy";
     public static final String SCRIPT_TYPE_OPTION_ARG_NAME = "scriptType";
 
    public static final String SYNTAX = "java jhttpcrowler.jar";
     public static final String HEADER = "Jhttpcrowler command-line client, version 0.1";
     public static final String FOOTER = "--END--";
 
     public static final String FILE_ENCODING = "utf-8";
 
     private static Options options;
     static {
         //Script options
         Option script = new Option(SCRIPT_OPTION_SHORT_NAME, SCRIPT_OPTION_LONG_NAME, true, SCRIPT_OPTION_DESCRIPTION);
         script.setArgs(1);
         script.setOptionalArg(false);
         script.setArgName(SCRIPT_OPTION_ARG_NAME);
         script.setRequired(true);
 
         //Script type option
         Option scriptType = new Option(SCRIPT_TYPE_OPTION_SHORT_NAME,
                 SCRIPT_TYPE_OPTION_LONG_NAME, true, SCRIPT_TYPE_OPTION_DESCRIPTION);
         scriptType.setArgs(1);
         scriptType.setOptionalArg(true);
         scriptType.setArgName(SCRIPT_TYPE_OPTION_ARG_NAME);
 
         //Create options
         options = new Options();
         options.addOption(script);
         options.addOption(scriptType);
     }
 
     private static final Map<String, String> engineNamesByExtensionMap = new HashMap<String, String>();
     static {
         engineNamesByExtensionMap.put("groovy", "groovyShell");
         engineNamesByExtensionMap.put("rb", "ruby");
         engineNamesByExtensionMap.put("js", "javaScript");
         engineNamesByExtensionMap.put("py", "python");
     }
 
     private static void printHelp() {
         PrintWriter writer = new PrintWriter(System.out);
         HelpFormatter helpFormatter = new HelpFormatter();
         helpFormatter.printHelp(writer, 80, SYNTAX, HEADER,
                 options, 3, 5, FOOTER, true);
         writer.flush();
     }
 
     private static CommandLine parseCommandLine(Options options, String[] arguments) throws ParseException {
         CommandLineParser parser = new PosixParser();
         return parser.parse(options, arguments);
     }
 
     private static String getEngineName(CommandLine commandLine) {
         if (commandLine.hasOption(SCRIPT_TYPE_OPTION_SHORT_NAME)) {
             return commandLine.getOptionValue(SCRIPT_TYPE_OPTION_SHORT_NAME);
         } else {
             File scriptFile = new File(commandLine.getOptionValue(SCRIPT_OPTION_SHORT_NAME));
             String name = scriptFile.getName();
 
             if (name.lastIndexOf(".") > -1) {
                 String extension = name.substring(name.lastIndexOf(".") + 1, name.length());
                 return engineNamesByExtensionMap.get(extension);
             } else {
                 return null;
             }
         }
     }
 
     private static void run(CommandLine commandLine) throws Exception {
         String engineName = getEngineName(commandLine);
         if (engineName == null) {
             throw new IllegalArgumentException("\r\nCould not determine engine for passed script. Please specify it manually using -t option. " +
                     "See command-line options");
         }
 
         Engine engine = EngineFactoryImpl.getInstance().getEngine(engineName);
         engine.execute(new File(commandLine.getOptionValue(SCRIPT_OPTION_SHORT_NAME)));
     }
 
     public static void main(String[] args) throws Exception {
         try {
             System.setProperty("file.encoding", FILE_ENCODING);
             run(parseCommandLine(options, args));
         } catch (ParseException e) {
             printHelp();
         }
     }
 }
