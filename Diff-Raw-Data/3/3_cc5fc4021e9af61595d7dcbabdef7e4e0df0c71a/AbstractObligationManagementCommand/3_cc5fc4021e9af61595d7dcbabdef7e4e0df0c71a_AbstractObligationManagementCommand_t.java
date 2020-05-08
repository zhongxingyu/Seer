 /**
  * Copyright (c) Members of the EGEE Collaboration. 2006-2009.
  * See http://www.eu-egee.org/partners/ for details on the copyright holders.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.glite.authz.pap.ui.cli.policymanagement;
 
 import java.rmi.RemoteException;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.glite.authz.pap.common.xacml.utils.ObligationsHelper;
 import org.glite.authz.pap.common.xacml.wizard.ObligationWizard;
 import org.glite.authz.pap.common.xacml.wizard.PolicySetWizard;
 import org.glite.authz.pap.common.xacml.wizard.PolicyWizard;
 import org.opensaml.xacml.policy.ObligationType;
 import org.opensaml.xacml.policy.ObligationsType;
 import org.opensaml.xacml.policy.PolicySetType;
 import org.opensaml.xacml.policy.PolicyType;
 
 public abstract class AbstractObligationManagementCommand extends PolicyManagementCLI {
 
 	protected static final String LONG_DESCRIPTION = "Note that obligations can be added/removed only from resource and action policies.";
 
 	protected static final String USAGE = "<policyId> <obligationId>";
 	
 	protected String papAlias = null;
 	protected String policyId = null;
 	protected String obligationId = null;
 
 	public AbstractObligationManagementCommand(String[] commandNameValues,
 			String usage, String description, String longDescription) {
 		super(commandNameValues, usage, description, longDescription);
 		
 	}
 	
 	
 	protected void parseCommandLine(CommandLine commandLine) throws ParseException{
 	
 		String[] args = commandLine.getArgs();
 
 		if (args.length < 3) {
 			throw new ParseException("Wrong number of arguments. Usage: "
 					+ USAGE);
 		}
 		
 		policyId = args[1];
 		obligationId = args[2];
 		
 		if (commandLine.hasOption(OPT_PAPALIAS_LONG)) {
 			papAlias = commandLine.getOptionValue(OPT_PAPALIAS_LONG);
 		}
 	}
 
 	protected void addObligationToPolicy(PolicyType policy){
 		
 		ObligationsType obligations = policy.getObligations();
 		
 		if (obligations == null)
 			obligations = ObligationsHelper.build();
 		
 		obligations.getObligations().add(buildObligation());
		policy.setObligations(obligations);
 		
 	}
 	
 	protected boolean removeObligations(ObligationsType ob){
 		
 		if (ob == null)
 			return false;
 		
 		ObligationType toBeRemoved = null;
 		
 		for (ObligationType o: ob.getObligations()){
 			if (o.getObligationId().equals(obligationId)){
 				toBeRemoved = o;
 				break;
 			}
 		}
 		
 		if (toBeRemoved!= null){
 			ob.getObligations().remove(toBeRemoved);
 			return true;
 		}
 		
 		return false;
 		
 	}
 	
 	protected boolean removeObligationFromPolicy(PolicyType policy){
 		
 		return removeObligations(policy.getObligations());
 	}
 	
 	protected boolean removeObligationFromPolicySet(PolicySetType policySet){
 		return removeObligations(policySet.getObligations());
 	}
 	
 	
 	protected boolean updatePolicy(PolicyType policy) throws RemoteException{
 		String oldPolicyVersion = policy.getVersion();
 		PolicyWizard.increaseVersion(policy);
 		return xacmlPolicyMgmtClient.updatePolicy(papAlias, oldPolicyVersion, policy);
 		
 	}
 	
 	protected boolean updatePolicySet(PolicySetType policySet) throws RemoteException{
 		String oldPolicySetVersion  = policySet.getVersion();
 		PolicySetWizard.increaseVersion(policySet);
 		return xacmlPolicyMgmtClient.updatePolicySet(papAlias, oldPolicySetVersion, policySet);
 		
 	}
 	protected void addObligationToPolicySet(PolicySetType policySet){
 		
 		ObligationsType obligations = policySet.getObligations();
 		
 		if (obligations == null)
 			obligations = ObligationsHelper.build();
 		
 		obligations.getObligations().add(buildObligation());
		policySet.setObligations(obligations);
 	}
 	
 	
 	protected ObligationType buildObligation(){
 		
 		ObligationWizard owiz = new ObligationWizard(obligationId);
 		return owiz.getXACML();
 	}
 
 
 	@SuppressWarnings("static-access")
 	@Override
 	protected Options defineCommandOptions() {
 		Options options = new Options();
 	    options.addOption(OptionBuilder.hasArg(true)
 	                                   .withDescription(OPT_PAPALIAS_DESCRIPTION)
 	                                   .withLongOpt(OPT_PAPALIAS_LONG)
 	                                   .create());
 	    return options;
 	}
 	
 	
 
 }
