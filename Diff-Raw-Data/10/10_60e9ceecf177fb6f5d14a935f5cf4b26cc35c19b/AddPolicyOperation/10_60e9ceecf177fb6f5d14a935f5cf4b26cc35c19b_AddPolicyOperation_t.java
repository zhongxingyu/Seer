 package org.glite.authz.pap.authz.policymanagement;
 
 import org.glite.authz.pap.authz.BasePAPOperation;
 import org.glite.authz.pap.authz.PAPPermission;
 import org.glite.authz.pap.authz.PAPPermission.PermissionFlags;
 import org.glite.authz.pap.common.xacml.wizard.PolicyWizard;
 import org.glite.authz.pap.distribution.PAPManager;
 import org.glite.authz.pap.repository.PAPContainer;
 import org.opensaml.xacml.policy.PolicyType;
 
 public class AddPolicyOperation extends BasePAPOperation<String> {
 
     String policySetId;
     String policyIdPrefix;
     PolicyType policy;
 
     protected AddPolicyOperation(String policySetId, String policyIdPrefix, PolicyType policy) {
 
        this.policySetId = policySetId;
         this.policyIdPrefix = policyIdPrefix;
         this.policy = policy;
     }
 
     public static AddPolicyOperation instance(String policySetId, String policyIdPrefix, PolicyType policy) {
         return new AddPolicyOperation(policySetId, policyIdPrefix, policy);
     }
 
     protected String doExecute() {
 
         String policyId = null;
 
         PAPContainer localPAP = PAPManager.getInstance().getLocalPAPContainer();
 
        if (!localPAP.hasPolicySet(policySetId)) {
            log.warn(String.format("Policy not added because PolicySetId \"%s\" does not exists.", policySetId));
             return null;
        }
 
         policyId = PolicyWizard.generateId(policyIdPrefix);
         policy.setPolicyId(policyId);
 
         localPAP.addPolicy(policySetId, policy);
        
        log.info(String.format("Added policy (policyId=\"%s\")", policyId));
 
         return policyId;
 
     }
 
     @Override
     protected void setupPermissions() {
 
         addRequiredPermission(PAPPermission.of(PermissionFlags.POLICY_WRITE));
 
     }
 
 }
