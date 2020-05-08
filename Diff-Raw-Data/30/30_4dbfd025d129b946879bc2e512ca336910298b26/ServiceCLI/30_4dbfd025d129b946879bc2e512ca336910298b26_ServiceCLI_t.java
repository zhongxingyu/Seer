 package org.glite.authz.pap.ui.cli;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Reader;
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
 import org.bouncycastle.openssl.PEMReader;
 import org.glite.authz.pap.client.ServiceClient;
 import org.glite.authz.pap.client.ServiceClientFactory;
 import org.glite.authz.pap.common.Pap;
 import org.glite.authz.pap.common.exceptions.PAPException;
 import org.glite.authz.pap.common.utils.Utils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public abstract class ServiceCLI {
 
     public static enum ExitStatus {
         // Attention keep that order because elements ordinal number is used.
         SUCCESS, // value = 0
         PARTIAL_SUCCESS, // value = 1
         FAILURE, // value = 2
         INITIALIZATION_ERROR, // value = 3
         PARSE_ERROR, // value = 4
         REMOTE_EXCEPTION
         // value = 5
     }
 
     private static final Logger log = LoggerFactory.getLogger(ServiceCLI.class);
 
     private static final HelpFormatter helpFormatter = new HelpFormatter();
 
     private static final String OPT_PROXY_DESCRIPTION = "Specifies a user proxy to be used for authentication.";
     private static final String OPT_PROXY_LONG = "proxy";
 
     private static final String OPT_CERT_DESCRIPTION = "Specifies non-standard user certificate.";
     private static final String OPT_CERT_LONG = "cert";
 
     private static final String OPT_HOST_DESCRIPTION = "Specifies the target PAP hostname (default is localhost). "
             + "This option defines the PAP endpoint to be contacted as follows: https://arg:port"
             + Pap.DEFAULT_SERVICES_ROOT_PATH;
     private static final String OPT_HOST_LONG = "host";
 
     private static final String OPT_KEY_DESCRIPTION = "Specifies non-standard user private key.";
     private static final String OPT_KEY_LONG = "key";
 
     private static final String OPT_PORT = "p";
     private static final String OPT_PORT_DESCRIPTION = "Specifies the port on which the target PAP is listening "
             + "(default is " + Pap.DEFAULT_PORT + ")";
     private static final String OPT_PORT_LONG = "port";
 
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
     private static Options globalOptions;
     private String longDescriptionText;
     private Options options;
     private final ServiceClient serviceClient;
     private String usageText;
     protected boolean verboseMode = false;
 
     static {
         globalOptions = defineGlobalOptions();
     }
 
     @SuppressWarnings("unchecked")
     public ServiceCLI(String[] commandNameValues, String usage, String description, String longDescription) {
 
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
 
     public static Options getGlobalOptions() {
         return globalOptions;
     }
 
     @SuppressWarnings("static-access")
     private static Options defineGlobalOptions() {
 
         Options options = new Options();
 
         // TODO: OPT_URL and (OPT_HOST, OPT_PORT) are mutually exclusive
         // options. Use OptionGroup.
         options.addOption(OptionBuilder.hasArg(true)
                                        .withLongOpt(OPT_URL_LONG)
                                        .withDescription("Specifies the target PAP endpoint (default: "
                                                + String.format(DEFAULT_SERVICE_URL,
                                                                Pap.DEFAULT_HOST,
                                                                Pap.DEFAULT_PORT,
                                                                Pap.DEFAULT_SERVICES_ROOT_PATH) + ").")
                                        .withArgName("url")
                                        .create());
         options.addOption(OptionBuilder.hasArg(true)
                                        .withLongOpt(OPT_HOST_LONG)
                                        .withDescription(OPT_HOST_DESCRIPTION)
                                        .withArgName("hostname")
                                        .create());
         options.addOption(OptionBuilder.hasArg(true)
                                        .withLongOpt(OPT_PORT_LONG)
                                        .withDescription(OPT_PORT_DESCRIPTION)
                                        .create(OPT_PORT));
         options.addOption(OptionBuilder.hasArg(true)
                                        .withLongOpt(OPT_PROXY_LONG)
                                        .withDescription(OPT_PROXY_DESCRIPTION)
                                        .withArgName("file")
                                        .create());
         options.addOption(OptionBuilder.hasArg(true)
                                        .withLongOpt(OPT_CERT_LONG)
                                        .withDescription(OPT_CERT_DESCRIPTION)
                                        .withArgName("file")
                                        .create());
         options.addOption(OptionBuilder.hasArg(true)
                                        .withLongOpt(OPT_KEY_LONG)
                                        .withDescription(OPT_KEY_DESCRIPTION)
                                        .withArgName("file")
                                        .create());
         options.addOption(OptionBuilder.hasArg(false)
                                        .withLongOpt(OPT_VERBOSE_LONG)
                                        .withDescription(OPT_VERBOSE_DESCRIPTION)
                                        .create(OPT_VERBOSE));
         return options;
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
 
         if (commandLine.hasOption(OPT_HELP)) {
             throw new HelpMessageException();
         }
 
         if (commandLine.hasOption(OPT_VERBOSE)) {
             verboseMode = true;
         }
 
         if (commandLine.hasOption(OPT_URL_LONG)) {
 
             serviceClient.setTargetEndpoint(commandLine.getOptionValue(OPT_URL_LONG));
 
         } else {
 
             String host = Pap.DEFAULT_HOST;
             String port = Pap.DEFAULT_PORT;
 
             if (commandLine.hasOption(OPT_HOST_LONG)) {
                 host = commandLine.getOptionValue(OPT_HOST_LONG);
             }
 
             if (commandLine.hasOption(OPT_PORT)) {
                 port = commandLine.getOptionValue(OPT_PORT);
 
                 try {
                     Integer.valueOf(port);
                 } catch (NumberFormatException e) {
                     throw new ParseException(String.format("Invalid port number \"%s\" (option -%s, --%s)",
                                                            port,
                                                            OPT_PORT,
                                                            OPT_PORT_LONG));
                 }
             }
 
             serviceClient.setTargetEndpoint(String.format(DEFAULT_SERVICE_URL,
                                                           host,
                                                           port,
                                                           Pap.DEFAULT_SERVICES_ROOT_PATH));
 
         }
 
         boolean credentialsNotRetrieved = true;
 
         if (commandLine.hasOption(OPT_PROXY_LONG)) {
             serviceClient.setClientProxy(commandLine.getOptionValue(OPT_PROXY_LONG));
             credentialsNotRetrieved = false;
         }
 
         if (commandLine.hasOption(OPT_CERT_LONG)) {
             if (commandLine.hasOption(OPT_PROXY_LONG)) {
                 throw new ParseException(String.format("Conflicting options --%s and --%s.",
                                                        OPT_PROXY_LONG,
                                                        OPT_CERT_LONG));
             } else {
                 serviceClient.setClientCertificate(commandLine.getOptionValue(OPT_CERT_LONG));
                 credentialsNotRetrieved = false;
             }
         }
 
         if (commandLine.hasOption(OPT_KEY_LONG)) {
             if (commandLine.hasOption(OPT_PROXY_LONG)) {
                 throw new ParseException(String.format("Conflicting options --%s and --%s.",
                                                        OPT_PROXY_LONG,
                                                        OPT_KEY_LONG));
             } else {
                 serviceClient.setClientPrivateKey(commandLine.getOptionValue(OPT_KEY_LONG));
                 credentialsNotRetrieved = false;
             }
         }
 
         if (credentialsNotRetrieved) {
 
             // 1. if running as root take the cert /etc/grid-security/hostcert.pem
             // 2. check the env variable X509_USER_PROXY
             // 3. check the env variable X509_USER_CERT (and X509_USER_KEY)
             // 4. check the proxy /tmp/x509up_u<id_utente>
             // 5. check the cert $HOME/.globus/usercert.pem and key $HOME/.globus/userkey.pem
 
             String euid = getEUID();
 
             if (euid == null) {
 
                 log.error("Cannot enstabilish user's effective user id.");
 
                 throw new PAPException(String.format("Cannot enstabilish user's effective user id, please use the --%s or --%s, --%s options.",
                                                      OPT_PROXY_LONG,
                                                      OPT_CERT_LONG,
                                                      OPT_KEY_LONG));
             }
 
             String messageString = null;
 
             if ("0".equals(euid)) {
                 // user: root
 
                if (setCertFromEnvironment()) {

                    messageString = String.format("Connecting to %s using %s and %s (from environment X509_USER_CERT and X509_USER_KEY)",
                                                  serviceClient.getTargetEndpoint(),
                                                  serviceClient.getClientCertificate(),
                                                  serviceClient.getClientPrivateKey());

                } else {

                    serviceClient.setClientCertificate("/etc/grid-security/hostcert.pem");
                    serviceClient.setClientPrivateKey("/etc/grid-security/hostkey.pem");

                    messageString = String.format("Connecting to %s using %s and %s",
                                                  serviceClient.getTargetEndpoint(),
                                                  serviceClient.getClientCertificate(),
                                                  serviceClient.getClientPrivateKey());
                }
             } else {
 
                 if (setProxyFromEnvironment()) {
 
                     messageString = String.format("Connecting to %s using proxy (from environment X509_USER_PROXY)  %s",
                                                   serviceClient.getTargetEndpoint(),
                                                   serviceClient.getClientProxy());
 
                 } else if (setCertFromEnvironment()) {
 
                     messageString = String.format("Connecting to %s using %s and %s (from environment X509_USER_CERT and X509_USER_KEY)",
                                                   serviceClient.getTargetEndpoint(),
                                                   serviceClient.getClientCertificate(),
                                                   serviceClient.getClientPrivateKey());
 
                 } else if (setProxyFromStandardLocation(euid)) {
                     messageString = String.format("Connecting to %s using proxy %s",
                                                   serviceClient.getTargetEndpoint(),
                                                   serviceClient.getClientProxy());
 
                 } else if (setCertFromHomeDir()) {
 
                     messageString = String.format("Connecting to %s using %s and %s",
                                                   serviceClient.getTargetEndpoint(),
                                                   serviceClient.getClientCertificate(),
                                                   serviceClient.getClientPrivateKey());
 
                 } else {
                     throw new ParseException(String.format("Unable to find a certificate or a proxy, please specify a proxy file with option --%s or certificate and key with options --%s and --%s",
                                                            OPT_PROXY_LONG,
                                                            OPT_CERT_LONG,
                                                            OPT_KEY_LONG));
                 }
 
             }
 
             log.info(messageString);
 
             if (verboseMode) {
                 System.out.println(messageString);
             }
         }
 
         // Ask for certificate password if needed. The default private key (getClientPrivateKey() ==
         // null)
         // is a host certificate key which doesn't need the password
         if (serviceClient.getClientPrivateKey() != null) {
             try {
 
                 Reader reader = new FileReader(serviceClient.getClientPrivateKey());
 
                 PEMReader pm = new PEMReader(reader, new PasswordFinderImpl());
 
                 char[] password = null;
 
                 try {
                     pm.readObject();
                 } catch (IOException e) {
                     // doesn't matter certificate stuff is managed later.
                     // the purpose of this is just to set the password (if needed).
                 }
 
                 password = PasswordFinderImpl.getTypedPassword();
 
                 if (password != null) {
                     serviceClient.setClientPrivateKeyPassword(new String(password));
                 }
 
             } catch (FileNotFoundException e) {
                 throw new CLIException(e);
             }
         }
 
         return executeCommandService(commandLine, serviceClient);
     }
 
     public String[] getCommandNameValues() {
         return commandNameValues;
     }
 
     public ServiceClient getServiceClient() {
         return serviceClient;
     }
 
     public void printHelpMessage(PrintWriter pw) {
         String syntax = String.format("pap-admin [global-options] %s %s", commandNameValues[0], usageText);
 
         pw.println();
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
         helpFormatter.printOptions(pw,
                                    helpFormatter.getWidth(),
                                    commandOptions,
                                    helpFormatter.getLeftPadding(),
                                    helpFormatter.getDescPadding());
 
         // global options
         pw.println();
         helpFormatter.printWrapped(pw, helpFormatter.getWidth(), "Global options:");
         helpFormatter.printOptions(pw,
                                    helpFormatter.getWidth(),
                                    globalOptions,
                                    helpFormatter.getLeftPadding(),
                                    helpFormatter.getDescPadding());
     }
 
     protected abstract Options defineCommandOptions();
 
     protected abstract int executeCommandService(CommandLine commandLine, ServiceClient serviceClient)
             throws CLIException, ParseException, RemoteException;
 
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
 
     /**
      * Return the effective user ID.
      * 
      * @return the effective user ID or <code>null</code> if the effective user ID couldn't be
      *         established.
      */
     private String getEUID() {
 
         String euid = System.getenv("EUID");
 
         if (!Utils.isDefined(euid)) {
             String euidProperty = System.getProperty("effectiveUserId");
 
             if (Utils.isDefined(euidProperty)) {
                 euid = euidProperty;
             } else {
                 euid = null;
             }
         }
 
         return euid;
     }
 
     /**
      * Set the certificate and the key in {@link ServiceCLI#serviceClient} using
      * ~/.globus/user{cert,key}.pem.
      * 
      * @return <code>true</code> if cert and key were found, <code>false</code> otherwise.
      * 
      */
     private boolean setCertFromHomeDir() throws ParseException {
 
         String baseDir = System.getProperty("user.home") + File.separator + ".globus" + File.separator;
 
         String cert = baseDir + "usercert.pem";
         String key = baseDir + "userkey.pem";
 
         File file = new File(cert);
 
         if (!file.exists()) {
             return false;
         }
 
         file = new File(key);
 
         if (!file.exists()) {
             return false;
         }
 
         serviceClient.setClientCertificate(cert);
         serviceClient.setClientPrivateKey(key);
 
         return true;
     }
 
     /**
      * Set the certificate the the key in {@link ServiceCLI#serviceClient} using the envirinment
      * variables X509_USER_CERT and X509_USER_KEY.
      * 
      * @return <code>true</code> if the env variables are set, <code>false</code> otherwise.
      * @throws ParseException if only one of X509_USER_CERT or X509_USER_KEY is set.
      * 
      */
     private boolean setCertFromEnvironment() throws ParseException {
         String x509UserCert = System.getenv("X509_USER_CERT");
         String x509UserKey = System.getenv("X509_USER_KEY");
 
         if (!Utils.isDefined(x509UserCert) && (!Utils.isDefined(x509UserKey))) {
             return false;
         }
 
         if (!(Utils.isDefined(x509UserCert) && Utils.isDefined(x509UserKey))) {
             if (Utils.isDefined(x509UserCert)) {
                 throw new ParseException("Trying to use environment variable X509_USER_CERT for certificate but environment variable X509_USER_KEY is not set.");
             }
             throw new ParseException("Trying to use environment variable X509_USER_KEY for private key but environment variable X509_USER_CERT is not set.");
         }
 
         serviceClient.setClientCertificate(x509UserCert);
         serviceClient.setClientPrivateKey(x509UserKey);
 
         return true;
     }
 
     /**
      * Set the proxy in {@link ServiceCLI#serviceClient} using the location defined in the
      * environment variable X509_USER_PROXY.
      * 
      * @return <code>true</code> if the env variable is set, <code>false</code> otherwise.
      */
     private boolean setProxyFromEnvironment() {
 
         String x509UserProxy = System.getenv("X509_USER_PROXY");
 
         if (Utils.isDefined(x509UserProxy)) {

             serviceClient.setClientProxy(x509UserProxy);
             return true;

         } else {
             return false;
         }
     }
 
     /**
      * Set the proxy in {@link ServiceCLI#serviceClient} using the standard location (i.e. /tmp).
      * 
      * @param euid the effective user ID to build the proxy file name.
      * @return <code>true</code> the proxy was found, <code>false</code> otherwise.
      */
     private boolean setProxyFromStandardLocation(String euid) {
 
         String proxy = "/tmp/x509up_u" + euid;
 
         File file = new File(proxy);
 
         if (!file.exists()) {
             return false;
         }
 
         serviceClient.setClientProxy(proxy);
 
         return true;
     }
 }
