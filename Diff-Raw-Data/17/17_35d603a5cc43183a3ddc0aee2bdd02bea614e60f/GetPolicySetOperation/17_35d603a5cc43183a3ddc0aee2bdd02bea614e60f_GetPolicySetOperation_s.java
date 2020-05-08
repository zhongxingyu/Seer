 package org.glite.authz.pap.authz.policymanagement;
 
 import org.glite.authz.pap.authz.BasePAPOperation;
 import org.glite.authz.pap.authz.PAPPermission;
 import org.glite.authz.pap.authz.PAPPermission.PermissionFlags;
 import org.glite.authz.pap.distribution.PAPManager;
 import org.glite.authz.pap.repository.PAPContainer;
 import org.glite.authz.pap.repository.exceptions.NotFoundException;
 import org.opensaml.xacml.policy.PolicySetType;
 
 
 public class GetPolicySetOperation extends BasePAPOperation <PolicySetType>{
 
     String alias;
     String policySetId;
     
      
     protected GetPolicySetOperation( String alias, String policySetId ) {
 
         this.alias = alias;
         this.policySetId = policySetId;
     
     }
 
     public static GetPolicySetOperation instance(String alias, String policySetId) {
 
         return new GetPolicySetOperation(alias, policySetId);
     }
     
     @Override
     protected PolicySetType doExecute() {
 
         PAPContainer papContainer = PAPManager.getInstance().getPAPContainer(alias);
         
         if (!papContainer.hasPolicySet(policySetId))
             throw new NotFoundException("PolicySet '" + policySetId + "' not found.");
         
         PolicySetType policySet = papContainer.getPolicySet(policySetId);
         
         return policySet;
         
     }
 
     @Override
     protected void setupPermissions() {
 
         addRequiredPermission( PAPPermission.of( PermissionFlags.POLICY_READ_LOCAL ) );
         
     }
     
     
 
 }
