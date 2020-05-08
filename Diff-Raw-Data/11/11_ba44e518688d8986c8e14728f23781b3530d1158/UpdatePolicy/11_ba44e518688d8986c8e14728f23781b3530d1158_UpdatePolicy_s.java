 package org.glite.authz.pap.ui.cli.policymanagement;
 
 import java.io.File;
 import java.rmi.RemoteException;
 import java.util.List;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.glite.authz.pap.encoder.EncodingException;
 import org.glite.authz.pap.encoder.PolicyFileEncoder;
 import org.glite.authz.pap.ui.cli.CLIException;
 import org.opensaml.xacml.XACMLObject;
 import org.opensaml.xacml.policy.PolicyType;
 
 public class UpdatePolicy extends PolicyManagementCLI {
     
     private static final String USAGE = "<policyId> <file> [options]";
     private static final String[] commandNameValues = { "update-policy-from-file", "up" };
     private static final String DESCRIPTION = "Update the policy identified by \"policyId\" with the new policy " +
     		"defined in \"file\"";
     private PolicyFileEncoder policyFileEncoder = new PolicyFileEncoder();
     
     public UpdatePolicy() {
         super(commandNameValues, USAGE, DESCRIPTION, null);
     }
     
     @Override
     protected int executeCommand(CommandLine commandLine) throws CLIException, ParseException,
             RemoteException {
         
         String[] args = commandLine.getArgs();
         
         if (args.length != 3)
             throw new ParseException("Wrong number of arguments.");
     
         String policyId = args[1];
         String fileName = args[2];
         
         if (!xacmlPolicyMgmtClient.hasPolicy(policyId)) {
             System.out.println("Error: policId \"" + policyId + "\" does not exists.");
             return ExitStatus.FAILURE.ordinal();
         }
         
         File file = new File(fileName);
         if (!file.exists()) {
            System.out.println("Error: file " + file.getAbsolutePath() + "does not exists.");
             return ExitStatus.FAILURE.ordinal();
         }
         
         XACMLPolicyCLIUtils.initOpenSAML();
         List<XACMLObject> policyList;
         
         try {
             policyList = policyFileEncoder.parse(file);
         } catch (EncodingException e) {
            System.out.println("Syntax error. Skipping file:" + fileName);
             System.out.println(e.getMessage());
             return ExitStatus.FAILURE.ordinal();
         }
         
         PolicyType policy = null;
         int npolicies = 0;
         
         for (XACMLObject xacmlObject:policyList) {
             if (xacmlObject instanceof PolicyType) {
                 policy = (PolicyType) xacmlObject;
                 npolicies++;
             }
         }
         
         if (npolicies == 0) {
            System.out.println("Error: no policies has been defined in file " + fileName);
             return ExitStatus.FAILURE.ordinal();
         }
         
         if (npolicies > 1) {
            System.out.println("Error: more than one policy has been defined in file " + fileName);
             return ExitStatus.FAILURE.ordinal();
         }
         
         policy.setPolicyId(policyId);
         xacmlPolicyMgmtClient.updatePolicy(policy);
         
         if (verboseMode)
             System.out.println("Success: policy has been updated.");
         
         return ExitStatus.SUCCESS.ordinal();
         
     }
     
     @Override
     protected Options defineCommandOptions() {
         return null;
     }
     
 }
