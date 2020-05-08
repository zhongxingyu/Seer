 package org.glite.authz.pap.ui.cli.policymanagement;
 
 import java.rmi.RemoteException;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.glite.authz.pap.services.pap_management.axis_skeletons.PAPData;
 import org.glite.authz.pap.ui.cli.CLIException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ListPAPPolicies extends PolicyManagementCLI {
 
     private static final String[] commandNameValues = { "list-pap-policies", "lpp" };
     private static final String DESCRIPTION = "List cached policies of the trusted PAPs speficied as argument. "
             + "If no \"pap_alias\" are specified then cached policies for all the remote trusted PAPs are listed";
     private static final Logger log = LoggerFactory.getLogger(ListPAPPolicies.class);
     private static final String USAGE = "[pap_alias] [options]";
 
     private boolean getPoliciesOneByOne = false;
     private String[] papAliasArray;
     private String[] papInfoArray;
     private boolean plainFormat = false;
     private boolean showBlacklist = true;
 
     private boolean showServiceclass = true;
     private boolean xacmlOutput = false;
 
     public ListPAPPolicies() {
         super(commandNameValues, USAGE, DESCRIPTION, null);
     }
 
    private int listPAPPolicies(String papAlias) {
 
         PAPPolicyIterator policyIter = new PAPPolicyIterator(xacmlPolicyMgmtClient, papAlias, !getPoliciesOneByOne);
 
         try {
 
             policyIter.init();
 
             if (policyIter.getNumberOfPolicies() == 0) {
                 System.out.println("No policies has been found.");
                 return ExitStatus.SUCCESS.ordinal();
             }
 
         } catch (RemoteException e) {
             System.out.println(String.format("Error retrieving cache for PAP \"%s\": %s", papAlias, e.getMessage()));
             log.error("Error retrieving cache of PAP " + papAlias, e);
             return ExitStatus.FAILURE.ordinal();
         }
 
         boolean policiesFound;
 
         if (xacmlOutput || plainFormat)
             policiesFound = ListPolicies.listUsingPlaingFormat(policyIter, xacmlOutput, true, true, showBlacklist,
                     showServiceclass);
         else
             policiesFound = ListPolicies.listUsingGroupedFormat(policyIter, true, true, showBlacklist, showServiceclass, false);
 
         if (!policiesFound)
             System.out.println(ListPolicies.noPoliciesFoundMessage(true, true, showBlacklist, showServiceclass));
 
         return ExitStatus.SUCCESS.ordinal();
 
     }
 
     @SuppressWarnings("static-access")
     @Override
     protected Options defineCommandOptions() {
         Options options = new Options();
 
         options.addOption(OptionBuilder.hasArg(false).withDescription(OPT_SHOW_XACML_DESCRIPTION)
                 .withLongOpt(OPT_SHOW_XACML_LONG).create());
 
         options.addOption(OptionBuilder.hasArg(false).withDescription(OPT_PLAIN_FORMAT_DESCRIPTION).withLongOpt(
                 OPT_PLAIN_FORMAT_LONG).create());
 
         return options;
     }
 
     @Override
     protected int executeCommand(CommandLine commandLine) throws CLIException, ParseException, RemoteException {
 
         String[] args = commandLine.getArgs();
 
         if (args.length == 1) {
             PAPData[] papDataArray = papMgmtClient.listTrustedPAPs();
             int size = papDataArray.length;
             papAliasArray = new String[size];
             papInfoArray = new String[size];
             for (int i = 0; i < size; i++) {
                 papAliasArray[i] = papDataArray[i].getAlias();
                 papInfoArray[i] = String.format("%s (%s:%s):", papDataArray[i].getAlias(), papDataArray[i].getHostname(),
                         papDataArray[i].getPort());
             }
         } else {
             int size = args.length - 1;
             papAliasArray = new String[size];
             papInfoArray = new String[size];
             for (int i = 0; i < size; i++) {
                 papAliasArray[i] = args[i + 1];
                 papInfoArray[i] = String.format("%s: ", papAliasArray[i]);
             }
         }
 
         if (commandLine.hasOption(OPT_SHOW_XACML_LONG))
             xacmlOutput = true;
 
         if (commandLine.hasOption(OPT_PLAIN_FORMAT_LONG))
             plainFormat = true;
 
         if (commandLine.hasOption(OPT_BLACKLIST))
             showServiceclass = false;
 
         if (commandLine.hasOption(OPT_SERVICECLASS))
             showBlacklist = false;
 
         if (commandLine.hasOption(OPT_LIST_ONE_BY_ONE))
             getPoliciesOneByOne = true;
 
         XACMLPolicyCLIUtils.initOpenSAML();
 
         boolean failure = false;
         boolean partialSuccess = false;
 
        for (String papAlias : papAliasArray) {
            if (ExitStatus.SUCCESS.ordinal() == listPAPPolicies(papAlias)) {
                 failure = false;
                 partialSuccess = true;
             } else {
                 failure = true;
             }
         }
 
         if (failure && !partialSuccess) {
             return ExitStatus.FAILURE.ordinal();
         }
 
         if (failure && partialSuccess) {
             return ExitStatus.PARTIAL_SUCCESS.ordinal();
         }
 
         return ExitStatus.SUCCESS.ordinal();
     }
 
 }
