 package org.glite.authz.pap.ui.cli.papmanagement;
 
 import java.rmi.RemoteException;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.glite.authz.pap.common.Pap;
 
 public class AddPap extends PAPManagementCLI {
 
     private static final String[] commandNameValues = { "add-pap", "apap" };
     private static final String DESCRIPTION = "Add a remote or local pap.\n"
             + "<alias> is a friendly name (it has to be unique) used to identify the pap\n"
             + "<endpoint> endpoint of the PAP in the following format: [<protocol>://]<host>:[<port>/[path]]\n";
     private static final String LONG_DESCRIPTION = "Default protocol is: " + Pap.DEFAULT_PROTOCOL + "\n"
             + "Default port is: " + Pap.DEFAULT_PORT + "\n"
             + "Default path is: " + Pap.DEFAULT_SERVICES_ROOT_PATH + "\n"
     		+ "Example:\n" + "\t pap-admin " + commandNameValues[0]
             + " cnaf_pap test.cnaf.infn.it \"/C=IT/O=INFN/OU=Host/L=CNAF/CN=test.cnaf.infn.it\"";
     private static final String LOPT_PRIVATE = "private";
     private static final String LOPT_PUBLIC = "public";
     private static final String USAGE = "[options] <alias> [<endpoint> <dn>]";
 
     public AddPap() {
         super(commandNameValues, USAGE, DESCRIPTION, LONG_DESCRIPTION);
     }
 
     @SuppressWarnings("static-access")
     @Override
     protected Options defineCommandOptions() {
         Options options = new Options();
         options.addOption(OptionBuilder.hasArg(false)
                                        .withDescription("Set the pap as public (allow to distribute its policies)")
                                        .withLongOpt(LOPT_PUBLIC)
                                        .create());
         options.addOption(OptionBuilder.hasArg(false)
                                        .withDescription("Set the pap as private (default)")
                                        .withLongOpt(LOPT_PRIVATE)
                                        .create());
         options.addOption(OptionBuilder.hasArg(false)
                                        .withDescription(OPT_LOCAL_DESCRIPTION)
                                        .withLongOpt(OPT_LOCAL_LONG)
                                        .create(OPT_LOCAL));
         options.addOption(OptionBuilder.hasArg(false)
                                        .withDescription(OPT_REMOTE_DESCRIPTION)
                                        .withLongOpt(OPT_REMOTEL_LONG)
                                        .create(OPT_REMOTE));
         return options;
     }
 
     @Override
     protected int executeCommand(CommandLine commandLine) throws ParseException, RemoteException {
         String[] args = commandLine.getArgs();
 
         if ((args.length != 4) && (args.length != 2)) {
             throw new ParseException("Wrong number of arguments");
         }
 
         boolean isLocal;
         boolean isPublic = false;
 
         if (args.length != 2) {
             isLocal = false;
         } else {
             isLocal = true;
         }
 
         if (commandLine.hasOption(OPT_LOCAL)) {
             isLocal = true;
         }
 
         if (commandLine.hasOption(OPT_REMOTE)) {
             isLocal = false;
         }
 
         if (commandLine.hasOption(LOPT_PUBLIC)) {
             isPublic = true;
         }
 
         String alias = args[1];
         String protocol = null;
         String dn = null;
         String host = null;
         String port = null;
         String path = null;
 
         if (args.length != 2) {
             protocol = getProtocol(args[2]);
             host = getHostname(args[2]);
             port = getPort(args[2]);
             path = getPath(args[2]);
             dn = args[3];
         }
         
         Pap pap = new Pap(alias, isLocal, dn, host, port, path, protocol, isPublic);
 
         String msg = "Adding pap: ";
 
         if (verboseMode) {
             System.out.println(msg + pap.toFormattedString(0, msg.length()));
         }
 
         if (papMgmtClient.exists(pap.getAlias())) {
             System.out.println("pap already exists.");
             return ExitStatus.FAILURE.ordinal();
         }
 
         papMgmtClient.addPap(pap);
 
         if (verboseMode) {
            System.out.println("Success: new pap has been added.");
         }
 
         return ExitStatus.SUCCESS.ordinal();
     }
 
     protected static String getHostname(String endpoint) {
 
         int start = endpoint.indexOf("://");
         
         if (start == -1) {
             start = 0;
         } else {
             start +=3;
         }
         
         int end = endpoint.indexOf(':', start);
         
         if (end == -1) {
             end = endpoint.length();
         }
 
         return endpoint.substring(start, end);
     }
     
     protected static String getPath(String endpoint) {
         int start = endpoint.indexOf("://");
         
         if (start == -1) {
             start = 0;
         } else {
             start +=3;
         }
         
         start = endpoint.indexOf('/', start);
         
         
         if (start == -1) {
             return null;
         }
         
         int end = endpoint.length();
         
         return endpoint.substring(start, end);
     }
     
     protected static String getPort(String endpoint) {
         int start = endpoint.indexOf("://");
         
         if (start == -1) {
             start = 0;
         } else {
             start +=3;
         }
         
         start = endpoint.indexOf(':', start);
         
         int end = endpoint.indexOf('/', start);
         
         if (start == -1) {
             return null;
         }
         
         if (end == -1) {
             end = endpoint.length();
         }
         
         return endpoint.substring(start, end);
     }
     
     protected static String getProtocol(String endpoint) {
 
         int index = endpoint.indexOf("://");
 
         if (index == -1) {
             return null;
         }
         return endpoint.substring(0, index);
     }
 }
