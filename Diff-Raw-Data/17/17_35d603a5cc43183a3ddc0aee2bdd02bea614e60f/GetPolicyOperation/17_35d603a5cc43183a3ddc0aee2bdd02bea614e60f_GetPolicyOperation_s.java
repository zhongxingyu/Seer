 package org.glite.authz.pap.authz.policymanagement;
 
 import org.glite.authz.pap.authz.BasePAPOperation;
 import org.glite.authz.pap.authz.PAPPermission;
 import org.glite.authz.pap.authz.PAPPermission.PermissionFlags;
 import org.glite.authz.pap.distribution.PAPManager;
 import org.glite.authz.pap.repository.PAPContainer;
 import org.glite.authz.pap.repository.exceptions.NotFoundException;
 import org.opensaml.xacml.policy.PolicyType;
 
 
 public class GetPolicyOperation extends BasePAPOperation <PolicyType>{
 
     String alias;
     String policyId;
     
     protected GetPolicyOperation(String alias, String policyId) {
         this.alias = alias;
         this.policyId = policyId;
     }
     
 
     public static GetPolicyOperation instance(String alias, String policyId) {
         return new GetPolicyOperation(alias, policyId);
     }
     
     
     @Override
     protected PolicyType doExecute() {
 
         PAPContainer papContainer = PAPManager.getInstance().getPAPContainer(alias);
         
         if (!papContainer.hasPolicy(policyId)) {
             throw new NotFoundException("Policy '" + policyId + "' not found.");
         }
         
         PolicyType policy = papContainer.getPolicy(policyId);
         
         return policy;
     }
 
 
     @Override
     protected void setupPermissions() {
 
         addRequiredPermission( PAPPermission.of( PermissionFlags.POLICY_READ_LOCAL ) );
         
     }
 
 }
