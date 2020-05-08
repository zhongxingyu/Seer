 package org.glite.authz.pap.ui.cli;
 
 import java.io.PrintWriter;
 import java.rmi.RemoteException;
 import java.util.Collection;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.glite.authz.pap.client.ServiceClient;
 import org.glite.authz.pap.client.ServiceClientFactory;
 import org.glite.authz.pap.common.PAP;
 
 public abstract class ServiceCLI {
     
     public static enum ExitStatus {
        SUCCESS,                // value = 0
        PARTIAL_SUCCESS,        // value = 1
        FAILURE,                // value = 2
        INITIALIZATION_ERROR,   // value = 3
        PARSE_ERROR,            // value = 4
        REMOTE_EXCEPTION        // value = 5
     }
     
     private static final HelpFormatter helpFormatter = new HelpFormatter();
     private static final String OPT_CERT = "cert";
     private static final String OPT_CERT_DESCRIPTION = "Specifies non-standard user certificate.";
     private static final String OPT_CERT_LONG = "cert";
     private static final String OPT_HOST = "host";
     private static final String OPT_HOST_DESCRIPTION = "Specifies the target PAP hostname (default is localhost). " +
     		"This option defines the PAP endpoint to be contacted as follows: https://hostname:port/pap/services";
     private static final String OPT_HOST_LONG = "host";
     private static final String OPT_KEY = "key";
     private static final String OPT_KEY_DESCRIPTION = "Specifies non-standard user private key.";
     private static final String OPT_KEY_LONG = "key";
     private static final String OPT_PASSWORD = "password";
     private static final String OPT_PASSWORD_DESCRIPTION = "Specifies the password used to decrypt the user's private key.";
     private static final String OPT_PASSWORD_LONG = "password";
     private static final String OPT_PORT = "p";
     private static final String OPT_PORT_DESCRIPTION = "Specifies the port on which the target PAP is listening " +
     		"(default is " + PAP.DEFAULT_PORT + ")";
     private static final String OPT_PORT_LONG = "port";
     private static final String OPT_URL = "url";
     private static final String OPT_URL_LONG = "url";
     private static final String OPT_VERBOSE = "v";
     private static final String OPT_VERBOSE_DESCRIPTION = "Verbose mode.";
     private static final String OPT_VERBOSE_LONG = "verbose";
     protected static final String DEFAULT_SERVICE_URL = "https://%s:%s%s";
     
     protected static final String OPT_HELP = "h";
     protected static final String OPT_HELP_DESCRIPTION = "Print this message.";
     protected static final String OPT_HELP_LONG = "help";
     protected static final String OPT_PRIVATE_LONG = "private";
     protected static final String OPT_PUBLIC_LONG = "public";
     
     protected static final CommandLineParser parser = new GnuParser();
     private String[] commandNameValues;
     private Options commandOptions;
     private String descriptionText;
     private Options globalOptions;
     private String longDescriptionText;
     private Options options;
     private final ServiceClient serviceClient;
     private String usageText;
     protected boolean verboseMode = false;
     
     @SuppressWarnings( "unchecked" )
     public ServiceCLI(String[] commandNameValues, String usage, String description,
             String longDescription) {
         
         ServiceClientFactory serviceClientFactory = ServiceClientFactory.getServiceClientFactory();
         serviceClient = serviceClientFactory.createServiceClient();
         
         helpFormatter.setWidth(80);
         helpFormatter.setLeftPadding(4);
         
         this.commandNameValues = commandNameValues;
         this.usageText = usage;
         this.descriptionText = description;
         this.longDescriptionText = longDescription;
         
         commandOptions = defineCommandOptions();
         if (commandOptions == null)
             commandOptions = new Options();
         
         commandOptions.addOption(OPT_HELP, OPT_HELP_LONG, false, OPT_HELP_DESCRIPTION);
 
         globalOptions = defineGlobalOptions();
         
         options = new Options();
         Collection<Option> optionsList = commandOptions.getOptions();
         for (Option opt : optionsList) {
             options.addOption(opt);
         }
         
         optionsList = globalOptions.getOptions();
         for (Option opt : optionsList) {
             options.addOption(opt);
         }
     }
     
     public boolean commandMatch(String command) {
         for (String value : commandNameValues) {
             if (value.equals(command))
                 return true;
         }
         return false;
     }
     
     public int execute(String[] args) throws ParseException, HelpMessageException, RemoteException {
         
         CommandLine commandLine = parser.parse(options, args);
         
         if (commandLine.hasOption(OPT_HELP))
             throw new HelpMessageException();
         
         if (commandLine.hasOption(OPT_URL)) {
             
             serviceClient.setTargetEndpoint(commandLine.getOptionValue(OPT_URL));
             
         } else {
             
             String host = PAP.DEFAULT_HOST;
             String port = PAP.DEFAULT_PORT;
             
             if (commandLine.hasOption(OPT_HOST))
                 host = commandLine.getOptionValue(OPT_HOST);
             
             if (commandLine.hasOption(OPT_PORT))
                 port = commandLine.getOptionValue(OPT_PORT);
             
             serviceClient.setTargetEndpoint(String.format(DEFAULT_SERVICE_URL, host, port, PAP.DEFAULT_SERVICES_ROOT_PATH));
             
         }
         
         if (commandLine.hasOption(OPT_CERT)) 
             serviceClient.setClientCertificate(commandLine.getOptionValue(OPT_CERT));
         
         
         if (commandLine.hasOption(OPT_KEY))
             serviceClient.setClientPrivateKey(commandLine.getOptionValue(OPT_KEY));
         
         if (commandLine.hasOption(OPT_PASSWORD))
             serviceClient.setClientPrivateKeyPassword(commandLine.getOptionValue(OPT_PASSWORD));
         
         if (commandLine.hasOption(OPT_VERBOSE))
             verboseMode = true;
         
         return executeCommandService(commandLine, serviceClient);
         
     }
     
     public String[] getCommandNameValues() {
         return commandNameValues;
     }
     
     public ServiceClient getServiceClient() {
         return serviceClient;
     }
     
     public void printHelpMessage(PrintWriter pw) {
         String syntax = commandNameValues[0] + " " + usageText;
         
         helpFormatter.printUsage(pw, helpFormatter.getWidth(), syntax);
         
         if (descriptionText != null) {
             pw.println();
             helpFormatter.printWrapped(pw, helpFormatter.getWidth(), descriptionText);
         }
         
         if (longDescriptionText != null) {
             pw.println();
             helpFormatter.printWrapped(pw, helpFormatter.getWidth(), longDescriptionText);
         }
         
         // command specific options
         pw.println();
         helpFormatter.printWrapped(pw, helpFormatter.getWidth(), "Valid options:");
         helpFormatter.printOptions(pw, helpFormatter.getWidth(), commandOptions, helpFormatter
                 .getLeftPadding(), helpFormatter.getDescPadding());
         
         // global options
         pw.println();
         helpFormatter.printWrapped(pw, helpFormatter.getWidth(), "Global options:");
         helpFormatter.printOptions(pw, helpFormatter.getWidth(), globalOptions, helpFormatter
                 .getLeftPadding(), helpFormatter.getDescPadding());
     }
     
     @SuppressWarnings("static-access")
     private Options defineGlobalOptions() {
         
         Options options = new Options();
         
         // TODO: OPT_URL and (OPT_HOST, OPT_PORT) are mutually exclusive options. Use OptionGroup.
         options.addOption(OptionBuilder.hasArg().withLongOpt(OPT_URL_LONG)
                 .withDescription("Specifies the target PAP endpoint (default: "
                         + String.format(DEFAULT_SERVICE_URL, PAP.DEFAULT_HOST, PAP.DEFAULT_PORT, PAP.DEFAULT_SERVICES_ROOT_PATH) + ").").create(OPT_URL));
         options.addOption(OptionBuilder.hasArg().withLongOpt(OPT_HOST_LONG)
                 .withDescription(OPT_HOST_DESCRIPTION).create(OPT_HOST));
         options.addOption(OptionBuilder.hasArg().withLongOpt(OPT_PORT_LONG)
                 .withDescription(OPT_PORT_DESCRIPTION).create(OPT_PORT));
         
         options.addOption(OptionBuilder.hasArg().withLongOpt(OPT_CERT_LONG)
                 .withDescription(OPT_CERT_DESCRIPTION).create(OPT_CERT));
         options.addOption(OptionBuilder.hasArg().withLongOpt(OPT_KEY_LONG)
                 .withDescription(OPT_KEY_DESCRIPTION).create(OPT_KEY));
         options.addOption(OptionBuilder.hasArg().withLongOpt(OPT_PASSWORD_LONG)
                 .withDescription(OPT_PASSWORD_DESCRIPTION).create(OPT_PASSWORD));
         options.addOption(OptionBuilder.withLongOpt(OPT_VERBOSE_LONG)
                 .withDescription(OPT_VERBOSE_DESCRIPTION).create(OPT_VERBOSE));
         
         return options;
     }
     
     protected abstract Options defineCommandOptions();
     
     protected abstract int executeCommandService(CommandLine commandLine,
             ServiceClient serviceClient) throws CLIException, ParseException, RemoteException;
 
     protected void printErrorMessage(String msg) {
         System.out.println(msg);
     }
     
     protected void printOutputMessage(String msg) {
         System.out.println(msg);
     }
     
     protected void printVerboseMessage(String msg) {
         if (verboseMode)
             System.out.println(msg);
     }
     
 }
