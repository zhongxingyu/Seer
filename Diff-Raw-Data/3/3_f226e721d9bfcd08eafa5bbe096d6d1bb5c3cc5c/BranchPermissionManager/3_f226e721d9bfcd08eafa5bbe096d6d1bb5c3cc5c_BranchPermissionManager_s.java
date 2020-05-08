 package org.jtalks.poulpe.logic;
 
import org.jtalks.common.service.security.AclManager;
 import org.jtalks.poulpe.model.dao.BranchDao;
 import org.jtalks.poulpe.model.entity.Branch;
 import org.jtalks.poulpe.service.security.BasicAclBuilder;
 import org.jtalks.poulpe.service.security.JtalksPermission;
 
 /**
  * @author stanislav bashkirtsev
  */
 public class BranchPermissionManager {
     private final AclManager aclManager;
     private final BranchDao branchDao;
 
     public BranchPermissionManager(AclManager aclManager, BranchDao branchDao) {
         this.aclManager = aclManager;
         this.branchDao = branchDao;
     }
 
     public BranchPermissionManager allow(Branch branch, JtalksPermission... permissions) {
         BasicAclBuilder aclBuilder = new BasicAclBuilder(aclManager);
         aclBuilder.grant(permissions).on(branch);
         return this;
     }
 
     public BranchPermissionManager restrict(Branch branch, JtalksPermission... permissions) {
         return this;
     }
 
     public BranchPermissionManager revoke(Branch branch, JtalksPermission... permissions) {
         return this;
     }
 }
