 package com.cloudbees.sdk.commands;
 
 import com.cloudbees.api.BeesClient;
 import com.cloudbees.sdk.cli.BeesClientFactory;
 import com.cloudbees.sdk.cli.ICommand;
 import com.cloudbees.sdk.cli.Verbose;
 import com.staxnet.repository.LocalRepository;
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 
 import javax.inject.Inject;
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 /**
  * @author Fabian Donze
  */
 public abstract class Command extends ICommand {
     private String commandName;
 
     private String[] args;
 
     private String help;
 
     private String output;
 
     private Boolean showAll;
 
     private CommandLineParser parser;
 
     private List<CommandOption> options;
 
     private CommandLine line;
 
     private boolean addDefaultOptions;
 
     private File localRepository;
 
     private String description;
 
     private List<String> parameters;
 
     private int argumentExpected;
     
     @Inject
     BeesClientFactory beesClientFactory;
 
     @Inject
     private Verbose verbose;
 
     public Command() {
         addDefaultOptions = true;
         LocalRepository lr = new LocalRepository();
         localRepository = new File(lr.getRepositoryPath());
        parameters = new ArrayList<String>();
         argumentExpected = 0;
     }
 
     @Override
     public int run(List<String> args) throws Exception {
         init("bees"/*TODO:get rid of this*/, args.get(0), args.toArray(new String[args.size()]));
 
         // Add shutdown hook
         Runtime.getRuntime().addShutdownHook(new Thread() {
             public void run() {
                 Command.this.stop();
             }
         });
 
         return run();
     }
 
     @Override
     public void printHelp(List<String> args) {
         try {
             init("bees"/*TODO:get rid of this*/, args.get(0), args.toArray(new String[args.size()]));
         } catch (Exception e) {
             // the use of the exception in the init method doesn't differentiate anticipated problems
             // (such as user error on parameter values) from unanticipated bugs in code. The former
             // should be handled without stack trace, later should result in a stack trace.
             // since we can' differentiate them here, I'm err-ing on the ease of diagnostics and
             // reporting it as unanticipated bug in code
             throw new Error(e);
         }
         printHelp();
     }
 
     public void init(String commandPrefix, String commandName, String[] args) throws Exception {
         this.commandName = commandName;
         this.args = new String[args.length-1];
         System.arraycopy(args, 1, this.args, 0, this.args.length);
         help = commandPrefix + " " + commandName;
 
         // create the command line parser
         parser = new GnuParser();
 
         // create the Options
         options = new ArrayList<CommandOption>();
         addOption("all", "showAll", false, "Show all options", true);
         if (addDefaultOptions) {
             addOption("k", "key", true, "CloudBees API key");
             addOption("s", "secret", true, "CloudBees API secret");
             addOption(null, "server", true, "API server", true);
             addOption(null, "proxyHost", true, "API server proxy host", true);
             addOption(null, "proxyPort", true, "API server proxy port", true);
             addOption(null, "proxyUser", true, "API server proxy user name", true);
             addOption(null, "proxyPassword", true, "API server proxy password", true);
             addOption("v", "verbose", false, "verbose output");
             addOption("o", "output", true, "output format [txt | json | xml]  (Default: 'txt')");
         }
 
         boolean success;
         try {
             success = preParseCommandLine();
         } catch (Exception e) {
             printHelp(help);
             throw e;
         }
         if (!success) {
             printHelp(help);
             return;
         }
 
         try {
             success = parseCommandLine();
         } catch (Exception e) {
             printHelp(help);
             throw e;
         }
         if (!success) {
             printHelp(help);
             return;
         }
 
         if (!isHelp(args))
             initDefaults(getConfigProperties());
 
     }
 
     public int run() throws Exception {
         boolean success = false;
         try {
             if (postParseCommandLine()) {
                 success = postParseCheck();
             }
         } catch (Exception e) {
             printHelp(help);
             throw e;
         }
         if (!success) {
             printHelp(help);
             return 0;
         }
 
         if (!execute()) {
             printHelp(help);
         }
 
         return getResultCode();
     }
 
     public void printHelp() {
         printHelp(help);
     }
 
     public void stop() {
     }
 
     /**
      * This method is call before parsing the command line.
      * This is the place to add command line options
      * @return true if successful, false otherwise
      */
     protected boolean preParseCommandLine() {
         return true;
     }
 
     /**
      * This method is the main command execution method.
      * @return true if successful, false otherwise
      */
     protected abstract boolean execute() throws Exception;
 
     /**
      * This method is call by the help command.
      * This is the place to define the command arguments usage.
      * No need to return the options, they will be automatically added to the help message
      * @return usage String
      */
     protected String getUsageMessage() {
         return "";
     }
 
     /**
      * This method is call after parsing the command line.
      * This is the place to parse additional command arguments
      * @return true if successful, false otherwise
      */
     protected boolean postParseCommandLine() {
         List otherArgs = getCommandLine().getArgList();
         if (otherArgs.size() > 0) {
             for (int i=0; i<otherArgs.size(); i++) {
                 String str = (String)otherArgs.get(i);
                 addParameter(str);
             }
         }
 
         return true;
     }
 
     /**
      * This method is call after postParseCommandLine.
      * This is the place to validate all inputs
      * @return true if successful, false otherwise
      */
     protected boolean postParseCheck() {
         boolean ok = parameters.size() >= argumentExpected;
         if (!ok)
             throw new IllegalArgumentException("Expected parameters: " + argumentExpected + " found: " + parameters.size());
         return ok;
     }
 
     protected void setArgumentExpected(int argumentExpected) {
         this.argumentExpected = argumentExpected;
     }
 
     protected int getArgumentExpected() {
         return argumentExpected;
     }
 
     protected boolean parseCommandLine() throws Exception {
         boolean ok = true;
         // parse the command line arguments
         line = getParser().parse(getOptions(true), getArgs());
         for (Option option: line.getOptions()) {
 //                System.out.println("Option: " + option);
             String str = option.getLongOpt();
             if (str == null)
                 str = option.getOpt();
             str = str.replace("-","");
             if (option.hasArg()) {
                 Class[] types = {String.class};
                 Method method = getMethod(str, "", types);
                 if (method == null) {
                     method = getMethod(str, "Option", types);
                 }
                 method.invoke(this, option.getValue());
             } else {
                 Class[] types = {Boolean.class};
                 Method method = getMethod(str, "", types);
                 if (method == null) {
                     method = getMethod(str, "Option", types);
                 }
                 method.invoke(this, Boolean.TRUE);
             }
         }
         return ok;
     }
 
     protected List<String> getParameters() {
         return parameters;
     }
 
     protected void addParameter(String parameter) {
         int length=parameter.length();
         if (parameter.charAt(0) == '"' && parameter.charAt(length-1) == '"')
             parameter = parameter.substring(1, length-1);
         parameters.add(parameter);
     }
 
     protected int isParameter(String str) {
         boolean endQuote = true;
         int length = str.length();
         for (int i=0; i<length; i++) {
             char c = str.charAt(i);
             if (c == '"') endQuote = !endQuote;
             if (c == '=' && endQuote)
                 return i;
         }
         return -1;
     }
 
     protected int getResultCode() {
         return 0;
     }
 
     protected boolean hasDefaultOptions() {
         return addDefaultOptions;
     }
 
     protected void setAddDefaultOptions(boolean addDefaultOptions) {
         this.addDefaultOptions = addDefaultOptions;
     }
 
     protected File getLocalRepository() {
         return localRepository;
     }
 
     protected void setLocalRepository(File localRepository) {
         this.localRepository = localRepository;
     }
 
     private Method getMethod(String str, String postFix, Class[] types) {
         try {
             String methodName = "set" + str.substring(0,1).toUpperCase() + str.substring(1) + postFix;
 //            System.out.println("Method: " + methodName);
             Method method = getClass().getMethod(methodName, types);
             method.setAccessible(true);
             return method;
         } catch (NoSuchMethodException ignored) {}
         return null;
     }
 
     protected CommandLineParser getParser() {
         return parser;
     }
 
     private Options getOptions(boolean all) {
         Options opts = new Options();
         for (CommandOption commandOption: options) {
             if (showAllOption() || all || !commandOption.isHidden())
                 opts.addOption(commandOption.getOption());
         }
         return opts;
     }
 
     /**
      * Add an command line option. The option long name must have a corresponding public setter.
      * For example: --Xtension must have a setter called setXtension(String)
      * If the option has no argument (hasArg: false), the setter parameter will be a Boolean (Boolean.TRUE)
      * @param opt   the option short name  (-x)
      * @param longOpt  the option long name (--Xtension)
      * @param hasArg  true if the option has an argument, false otherwise
      * @param description  description of the option (used by help)
      */
     protected void addOption(String opt, String longOpt, boolean hasArg, String description) {
         addOption(opt, longOpt, hasArg, description, false);
     }
     /**
      * Add an command line option. The option long name must have a corresponding public setter.
      * For example: --Xtension must have a setter called setXtension(String)
      * If the option has no argument (hasArg: false), the setter parameter will be a Boolean (Boolean.TRUE)
      * @param opt   the option short name  (-x)
      * @param hasArg  true if the option has an argument, false otherwise
      * @param description  description of the option (used by help)
      */
     protected void addOption(String opt, boolean hasArg, String description) {
         addOption(opt, null, hasArg, description, false);
     }
     /**
      * Add an command line option. The option long name must have a corresponding public setter.
      * For example: --Xtension must have a setter called setXtension(String)
      * If the option has no argument (hasArg: false), the setter parameter will be a Boolean (Boolean.TRUE)
      * @param opt   the option short name  (-x)
      * @param longOpt  the option long name (--Xtension)
      * @param hasArg  true if the option has an argument, false otherwise
      * @param description  description of the option (used by help)
      * @param hide  true to hide the option from help
      */
     protected void addOption(String opt, String longOpt, boolean hasArg, String description, boolean hide) {
         options.add(new CommandOption(opt, longOpt, hasArg, description, hide));
     }
 
     protected void removeOption(String opt) {
         for (Iterator<CommandOption> it = options.iterator(); it.hasNext();) {
             CommandOption commandOption = it.next();
             if (commandOption.getOption().getOpt() != null && commandOption.getOption().getOpt().equals(opt)) {
                 it.remove();
             } else
             if (commandOption.getOption().getLongOpt() != null && commandOption.getOption().getLongOpt().equals(opt)) {
                 it.remove();
             }
         }
     }
 
     protected CommandLine getCommandLine() {
         return line;
     }
 
     public String getCommandName() {
         return commandName;
     }
 
     public String[] getArgs() {
         return args;
     }
 
     protected String getKey() {
         return beesClientFactory.key;
     }
 
     public void setKey(String key) {
         beesClientFactory.key = key;
     }
 
     protected String getSecret() {
         return beesClientFactory.secret;
     }
 
     public void setSecret(String secret) {
         beesClientFactory.secret = secret;
     }
 
     protected String getServer() {
         return beesClientFactory.server;
     }
 
     public void setServer(String server) {
         beesClientFactory.server = server;
     }
 
     public void setProxyHost(String proxyHost) {
         beesClientFactory.proxyHost = proxyHost;
     }
 
     public void setProxyPort(String proxyPort) {
         beesClientFactory.proxyPort = proxyPort;
     }
 
     public void setProxyUser(String proxyUser) {
         beesClientFactory.proxyUser = proxyUser;
     }
 
     public void setProxyPassword(String proxyPassword) {
         beesClientFactory.proxyPassword = proxyPassword;
     }
 
     protected String getProxyHost() {
         return beesClientFactory.proxyHost;
     }
 
     protected String getProxyPort() {
         return beesClientFactory.proxyPort;
     }
 
     protected String getProxyUser() {
         return beesClientFactory.proxyUser;
     }
 
     protected String getProxyPassword() {
         return beesClientFactory.proxyPassword;
     }
 
     protected String getApiserver() {
         return beesClientFactory.apiServer;
     }
 
     public boolean isVerbose() {
         return verbose.isVerbose();
     }
 
     public void setVerbose(Boolean verbose) {
         this.verbose.setVerbose(verbose!=null && verbose);
     }
 
     protected boolean showAllOption() {
         return showAll == null ? false : showAll;
     }
 
     public void setShowAll(Boolean showAll) {
         this.showAll = showAll;
     }
 
     public String getOutput() {
         if (output == null)
             return "txt";
         else
             output = output.trim().toLowerCase();
 
         return output;
     }
 
     public void setOutput(String output) {
         this.output = output;
     }
 
     protected boolean isTextOutput() {
         return getOutput().equals("txt");
     }
 
     // TODO: remove this after revisiting Init command
     protected Properties getConfigProperties() {
         return beesClientFactory.getConfigProperties();
     }
 
     protected void setApiserver(String apiserver) {
         beesClientFactory.apiServer = apiserver;
     }
 
     protected void initDefaults(Properties properties) {
     }
 
     protected void printHelp(String command) {
         if (options.size() > 0)
             command += " [options]";
         command += " " + getUsageMessage();
         if (getDescription() != null)
             System.out.println(getDescription());
         HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp(command, getOptions(false));
     }
 
 
     protected BeesClient getBeesClient() throws IOException {
         return beesClientFactory.get();
     }
 
     protected <T extends BeesClient> T getStaxClient(Class<T> type) throws IOException {
         return beesClientFactory.get(type);
     }
     
     private boolean isHelp(String[] args) {
          return (args != null && args.length > 0 && args[0].equalsIgnoreCase("help"));
      }
 
     protected void printOutput(Object o, Class... classes) {
         XStream xstream = null;
         String format = getOutput();
         if (format.equals("json"))
             xstream = new XStream(new JsonHierarchicalStreamDriver());
         else if (format.equals("xml"))
             xstream = new XStream();
         else
             System.out.println(o.toString());
 
         if (xstream != null) {
             xstream.setMode(XStream.NO_REFERENCES);
             for (Class c : classes) {
                 xstream.processAnnotations(c);
             }
             System.out.println(xstream.toXML(o));
         }
     }
 
     private String getDescription() {
         return null;    // TODO
     }
 
 }
