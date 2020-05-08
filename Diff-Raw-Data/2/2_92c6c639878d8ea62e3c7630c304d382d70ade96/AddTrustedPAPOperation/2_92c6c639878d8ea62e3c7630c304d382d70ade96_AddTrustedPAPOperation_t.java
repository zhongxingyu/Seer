 package org.glite.authz.pap.authz.papmanagement;
 
 import org.glite.authz.pap.authz.BasePAPOperation;
 import org.glite.authz.pap.authz.PAPPermission;
 import org.glite.authz.pap.authz.PAPPermission.PermissionFlags;
 import org.glite.authz.pap.common.PAP;
 import org.glite.authz.pap.distribution.PAPManager;
 import org.glite.authz.pap.repository.exceptions.AlreadyExistsException;
 import org.glite.authz.pap.services.pap_management.axis_skeletons.PAPData;
 
 
 public class AddTrustedPAPOperation extends BasePAPOperation <Boolean> {
 
     PAP pap;
     
     
     protected AddTrustedPAPOperation(PAPData pap) {
 
         this.pap = new PAP(pap);
         
     }
     
     public static AddTrustedPAPOperation instance(PAPData pap) {
 
         return new AddTrustedPAPOperation(pap);
     }
     
     
     @Override
     protected Boolean doExecute() {
         
         log.info("Adding PAP: " + pap);
         
         try {
             
             PAPManager.getInstance().addPAP( pap );
             
         } catch (AlreadyExistsException e) {
             return false;
         }
         
         return true;
         
     }
 
     @Override
     protected void setupPermissions() {
 
        addRequiredPermission( PAPPermission.of( PermissionFlags.CONFIGURATION_WRITE ) );
 
     }
 
 }
