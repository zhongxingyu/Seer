 package org.glite.authz.pap.authz.highlevelpolicymanagement;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.glite.authz.pap.authz.BasePAPOperation;
 import org.glite.authz.pap.authz.PAPPermission;
 import org.glite.authz.pap.authz.PAPPermission.PermissionFlags;
 import org.glite.authz.pap.common.xacml.TypeStringUtils;
 import org.glite.authz.pap.common.xacml.utils.PolicySetHelper;
 import org.glite.authz.pap.common.xacml.wizard.AttributeWizard;
 import org.glite.authz.pap.common.xacml.wizard.PolicyWizard;
 import org.glite.authz.pap.common.xacml.wizard.TargetWizard;
 import org.glite.authz.pap.distribution.PAPManager;
 import org.glite.authz.pap.repository.PAPContainer;
 import org.glite.authz.pap.services.highlevel_policy_management.axis_skeletons.UnbanResult;
 import org.opensaml.xacml.policy.PolicySetType;
 import org.opensaml.xacml.policy.PolicyType;
 
 public class UnbanOperation extends BasePAPOperation<UnbanResult> {
 
     private final AttributeWizard actionAttributeWizard;
     private final AttributeWizard bannedAttributeWizard;
     private final AttributeWizard resourceAttributeWizard;
 
     protected UnbanOperation(AttributeWizard bannedAttributeWizard, AttributeWizard resourceAttributeWizard,
             AttributeWizard actionAttributeWizard) {
 
         this.bannedAttributeWizard = bannedAttributeWizard;
         this.resourceAttributeWizard = resourceAttributeWizard;
         this.actionAttributeWizard = actionAttributeWizard;
     }
 
     public static UnbanOperation instance(AttributeWizard bannedAttributeWizard, AttributeWizard resourceAttributeWizard,
             AttributeWizard actionAttributeWizard) {
         return new UnbanOperation(bannedAttributeWizard, resourceAttributeWizard, actionAttributeWizard);
     }
 
     protected UnbanResult doExecute() {
 
 		UnbanResult unbanResult = new UnbanResult();
 		unbanResult.setConflictingPolicies(new String[0]);
 
 		PAPContainer localPAP = PAPManager.getInstance().getDefaultPAPContainer();
 
 		List<PolicySetType> targetPolicySetList = getTargetPolicySetList(localPAP);
 
 		if (targetPolicySetList.isEmpty()) {
 			unbanResult.setStatusCode(1);
 			return unbanResult;
 		}
 
 		PolicyType targetPolicy = null;
 		
 		for (PolicySetType targetPolicySet : targetPolicySetList) {
 			targetPolicy = getTargetPolicy(localPAP, targetPolicySet);
 			if (targetPolicy != null) {
 				break;
 			}
 		}
 		
 		if (targetPolicy == null) {
 			unbanResult.setStatusCode(1);
 			return unbanResult;
 		}
 		
 		PolicyWizard policyWizard = new PolicyWizard(targetPolicy);
 		TypeStringUtils.releaseUnneededMemory(targetPolicy);
 
 		if (policyWizard.removeDenyRuleForAttribute(bannedAttributeWizard)) {
 
 			if (policyWizard.getNumberOfRules() == 0) {
 				
 				localPAP.removePolicyAndReferences(policyWizard.getPolicyId());
 				
 			} else {
 				String oldVersion = policyWizard.getVersionString();
 				policyWizard.increaseVersion();
 				localPAP.updatePolicy(oldVersion, policyWizard.getXACML());
 			}
 
 			unbanResult.setStatusCode(0);
 			return unbanResult;
 		}
 
 		unbanResult.setStatusCode(1);
 		return unbanResult;
 	}
 
     @Override
     protected void setupPermissions() {
         addRequiredPermission(PAPPermission.of(PermissionFlags.POLICY_WRITE, PermissionFlags.POLICY_READ_LOCAL));
     }
     
     private PolicyType getTargetPolicy(PAPContainer papContainer, PolicySetType targetPolicySet) {
     	
 		List<String> policyIdList = PolicySetHelper.getPolicyIdReferencesValues(targetPolicySet);
 		TypeStringUtils.releaseUnneededMemory(targetPolicySet);
 
 		TargetWizard policyTargetWizard = new TargetWizard(actionAttributeWizard);
 		
 		for (String policyId : policyIdList) {
 
 			PolicyType repositoryPolicy = papContainer.getPolicy(policyId);
 
 			if (policyTargetWizard.isEquivalent(repositoryPolicy.getTarget())) {
 				return repositoryPolicy;
 			}
 			TypeStringUtils.releaseUnneededMemory(repositoryPolicy);
 		}
 		return null;
     }
 
     private List<PolicySetType> getTargetPolicySetList(PAPContainer papContainer) {
     	
     	List<PolicySetType> targetPolicySetList = new LinkedList<PolicySetType>();
 
         TargetWizard policySetTargetWizard = new TargetWizard(resourceAttributeWizard);
 
         PolicySetType rootPAPPolicySet = papContainer.getPAPRootPolicySet();
 
         List<String> policySetIdList = PolicySetHelper.getPolicySetIdReferencesValues(rootPAPPolicySet);
 
         TypeStringUtils.releaseUnneededMemory(rootPAPPolicySet);
 
         for (String policySetId : policySetIdList) {
             
             PolicySetType policySet = papContainer.getPolicySet(policySetId);
             
             if (policySetTargetWizard.isEquivalent(policySet.getTarget())) {
                 targetPolicySetList.add(policySet);
             }
             
             TypeStringUtils.releaseUnneededMemory(policySet);
         }
         return targetPolicySetList;
     }
 }
