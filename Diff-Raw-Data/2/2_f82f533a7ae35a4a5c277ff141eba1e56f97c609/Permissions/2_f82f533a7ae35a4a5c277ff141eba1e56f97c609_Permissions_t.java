 /**
  * SAHARA Scheduling Server
  *
  * Schedules and assigns local laboratory rigs.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2010, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 12th March 2010
  */
 package au.edu.uts.eng.remotelabs.schedserver.permissions.intf;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.hibernate.Session;
 
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.DataAccessActivator;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.ResourcePermissionDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.UserAssociationDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.UserClassDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.UserDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.UserLockDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.RequestCapabilities;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.ResourcePermission;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Rig;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.RigType;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.User;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.UserAssociation;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.UserAssociationId;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.UserClass;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.UserLock;
 import au.edu.uts.eng.remotelabs.schedserver.logger.Logger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.impl.UserAdmin;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.impl.UserClassAdmin;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.AddAcademicPermission;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.AddAcademicPermissionResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.AddPermission;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.AddPermissionResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.AddUser;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.AddUserAssociation;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.AddUserAssociationResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.AddUserClass;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.AddUserClassResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.AddUserLock;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.AddUserLockResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.AddUserResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.BulkAddUserClassUsers;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.BulkAddUserClassUsersResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.DeleteAcademicPermission;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.DeleteAcademicPermissionResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.DeletePermission;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.DeletePermissionResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.DeleteUser;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.DeleteUserAssociation;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.DeleteUserAssociationResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.DeleteUserClass;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.DeleteUserClassResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.DeleteUserLock;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.DeleteUserLockResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.DeleteUserResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.EditAcademicPermission;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.EditAcademicPermissionResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.EditPermission;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.EditPermissionResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.EditUser;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.EditUserClass;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.EditUserClassResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.EditUserResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetAcademicPermission;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetAcademicPermissionResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetAcademicPermissionsForAcademic;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetAcademicPermissionsForAcademicResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetAcademicPermissionsForUserClass;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetAcademicPermissionsForUserClassResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetPermission;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetPermissionResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetPermissionsForUser;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetPermissionsForUserClass;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetPermissionsForUserClassResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetPermissionsForUserResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetUser;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetUserClass;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetUserClassResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetUserClasses;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetUserClassesForUser;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetUserClassesForUserResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetUserClassesResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetUserResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetUsersInUserClass;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetUsersInUserClassResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.OperationRequestType;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.OperationResponseType;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.PermissionType;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.PermissionWithLockListType;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.PermissionWithLockType;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.PersonaType;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.ResourceClass;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.ResourceIDType;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.UnlockUserLock;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.UnlockUserLockResponse;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.UserAssociationType;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.UserClassIDType;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.UserClassType;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.UserIDType;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.UserLockIDUserPermSequence;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.UserLockType;
 import au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.UserType;
 
 /**
  * The Permissions SOAP interface implementation.
  */
 public class Permissions implements PermissionsSkeletonInterface
 {
     /** Logger. */
     private Logger logger;
     
     public Permissions()
     {
         this.logger = LoggerActivator.getLogger();
     }
 
     @Override
     public GetUserResponse getUser(GetUser request)
     {
         /** Request parameters. */
         UserIDType userReq = request.getGetUser();
         String ns = userReq.getUserNamespace(), nm = userReq.getUserName();
         long id = this.getIdentifier(userReq.getUserID());
         this.logger.debug("Received get user request with id=" + id + ", namespace=" + ns + " and name=" + nm + '.');
         
         /** Response parameters. */
         GetUserResponse resp = new GetUserResponse();
         UserType userResp = new UserType();
         resp.setGetUserResponse(userResp);
         userResp.setPersona(PersonaType.NOTFOUND);
         
         UserDao dao = new UserDao();
         User user;
         if (id > 0 && (user = dao.get(id)) != null)
         {
             userResp.setUserID(String.valueOf(user.getId()));
             userResp.setNameNamespace(user.getNamespace(), user.getName());
             userResp.setUserQName(user.getNamespace() + UserIDType.QNAME_DELIM + user.getName());
             userResp.setPersona(PersonaType.Factory.fromValue(user.getPersona()));
         }
         else if (ns != null && nm != null && (user = dao.findByName(ns, nm)) != null)
         {
             userResp.setUserID(String.valueOf(user.getId()));
             userResp.setNameNamespace(user.getNamespace(), user.getName());
             userResp.setUserQName(user.getNamespace() + UserIDType.QNAME_DELIM + user.getName());
             userResp.setPersona(PersonaType.Factory.fromValue(user.getPersona()));
         }
         
         dao.closeSession();
         return resp;
     }
 
     @Override
     public AddUserResponse addUser(AddUser addUser)
     {
         /* Request parameters. */
         UserType userReq = addUser.getAddUser();
         String nm = userReq.getUserName(), ns = userReq.getUserNamespace(), persona = userReq.getPersona().getValue();
         this.logger.debug("Received request to add user with name=" + nm + ", namespace=" + ns
                 + " and persona=" + persona + '.');
         
         /* Response parameters. */
         AddUserResponse resp = new AddUserResponse();
         OperationResponseType op = new OperationResponseType();
         op.setSuccessful(false);
         resp.setAddUserResponse(op);
         
         /* Check if the requestor is authorised. */
         if (!this.checkPermission(userReq))
         {
             op.setFailureCode(1);
             op.setFailureReason("Permission denied.");
         }
         /* Check the request parameters. */
         else if (nm == null || ns == null || persona == null)
         {
             op.setFailureCode(2);
             op.setFailureReason("Mandatory parameter not provided.");
         }
         else
         {
             UserAdmin admin = new UserAdmin(DataAccessActivator.getNewSession());
             if (admin.addUser(ns, nm, persona))
             {
                 op.setSuccessful(true);
             }
             else
             {
                 op.setFailureCode(3);
                 op.setFailureReason(admin.getFailureReason());
             }
             admin.closeSession();
         }
 
         return resp;
     }
     
     @Override
     public EditUserResponse editUser(EditUser request)
     {
         /* Request parameters. */
         UserType userReq = request.getEditUser();
         String ns = userReq.getUserNamespace(), name = userReq.getUserName(), persona = userReq.getPersona().getValue();
         long id = this.getIdentifier(userReq.getUserID());
         this.logger.debug("Received request to edit user with id=" + id + ", namespace=" + ns + ", name=" + name 
                 + ", persona=" + persona + '.');
         
         /* Response parameters. */
         EditUserResponse resp = new EditUserResponse();
         OperationResponseType op = new OperationResponseType();
         resp.setEditUserResponse(op);
         op.setSuccessful(false);
         
         UserAdmin userAdmin = new UserAdmin(DataAccessActivator.getNewSession());
         
         /* Check if the user is authorised. */
         if (!this.checkPermission(userReq))
         {
             op.setFailureCode(1);
             op.setFailureReason("Permission denied.");            
         }
         else if (id > 0)
         {
             if (userAdmin.editUser(id, ns, name, persona))
             {
                 op.setSuccessful(true);
             }
             else
             {
                 op.setFailureCode(3);
                 op.setFailureReason(userAdmin.getFailureReason());
             }
         }
         else if (ns != null && name != null)
         {
             if (userAdmin.editUser(ns, name, persona))
             {
                 op.setSuccessful(true);
             }
             else
             {
                 op.setFailureCode(3);
                 op.setFailureReason(userAdmin.getFailureReason());
             }
         }
         else
         {
             op.setFailureCode(2);
             op.setFailureReason("Mandatory parameter not provided.");
         }
         
         userAdmin.closeSession();
         return resp;
     }
     
     @Override
     public DeleteUserResponse deleteUser(DeleteUser request)
     {
         /* Request parameters. */
         UserIDType userReq = request.getDeleteUser();
         String ns = userReq.getUserNamespace(), name = userReq.getUserName();
         long id = this.getIdentifier(userReq.getUserID());
         this.logger.debug("Received delete user with id=" + id + ", namespace=" + ns + ", name=" + name + '.');
         
         /* Response parameters. */
         DeleteUserResponse resp = new DeleteUserResponse();
         OperationResponseType op = new OperationResponseType();
         resp.setDeleteUserResponse(op);
         op.setSuccessful(false);
         
         UserAdmin admin = new UserAdmin(DataAccessActivator.getNewSession());
         if (!this.checkPermission(userReq))
         {
             op.setFailureCode(1);
             op.setFailureReason("Permission denied");
         }
         else if (id != 0)
         {
             if (admin.deleteUser(id))
             {
                 op.setSuccessful(true);
             }
             else
             {
                 op.setFailureCode(3);
                 op.setFailureReason(admin.getFailureReason());
             }
         }
         else if (ns != null && name != null)
         {
             if (admin.deleteUser(ns, name))
             {
                 op.setSuccessful(true);
             }
             else
             {
                 op.setFailureCode(3);
                 op.setFailureReason(admin.getFailureReason());
             }
         }
         else
         {
             op.setFailureCode(2);
             op.setFailureReason("Mandatory parameter not supplied");
         }
         
         admin.closeSession();
         return resp;
     }
 
     @Override
     public GetUserClassResponse getUserClass(GetUserClass request)
     {
         /* Request parameters. */
         UserClassIDType uCReq = request.getGetUserClass();
         String name = uCReq.getUserClassName();
         long id = uCReq.getUserClassID();
         this.logger.debug("Received get user class request with id=" + id + ", name=" + name + '.');
         
         /* Response parameters. */
         GetUserClassResponse resp = new GetUserClassResponse();
         UserClassType uc = new UserClassType();
         resp.setGetUserClassResponse(uc);
         
         UserClassDao dao = new UserClassDao();
         UserClass cls;
         if ((id > 0 && (cls = dao.get(id)) != null) || (name != null && (cls = dao.findByName(name)) != null))
         {
             uc.setUserClassID(cls.getId().intValue());
             uc.setUserClassName(cls.getName());
             uc.setIsActive(cls.isActive());
             uc.setIsKickable(cls.isKickable());
             uc.setIsQueuable(cls.isQueuable());
             uc.setIsUserLockable(cls.isUsersLockable());
             uc.setPriority(cls.getPriority());
         }
             
         dao.closeSession();
         return resp;
     }
     
     @Override
     public AddUserClassResponse addUserClass(AddUserClass request)
     {
         /* Request parameters. */
         UserClassType ucReq = request.getAddUserClass();
         String name = ucReq.getUserClassName();
         int pri = ucReq.getPriority();
         this.logger.debug("Received add user request with name=" + name + ", priority=" + pri + ", active=" + 
                 ucReq.getIsActive() + ", kickable=" + ucReq.getIsKickable() + ", queueable=" + ucReq.getIsQueuable() +
                 ", lockable=" + ucReq.getIsUserLockable() + '.');
         
         /* Response parameters. */
         AddUserClassResponse resp = new AddUserClassResponse();
         OperationResponseType op = new OperationResponseType();
         resp.setAddUserClassResponse(op);
         op.setSuccessful(false);
         
         if (!this.checkPermission(ucReq))
         {
             op.setFailureCode(1);
             op.setFailureReason("Permission denied.");
         }
         else if (name == null)
         {
             op.setFailureCode(2);
             op.setFailureReason("Mandatory parameter not supplied.");
         }
         else
         {
             UserClassAdmin admin = new UserClassAdmin(DataAccessActivator.getNewSession());
             if (admin.addUserClass(name, pri, ucReq.getIsActive(), ucReq.getIsQueuable(), ucReq.getIsKickable(), 
                     ucReq.getIsUserLockable()))
             {
                 op.setSuccessful(true);
             }
             else
             {
                 op.setFailureCode(3);
                 op.setFailureReason(admin.getFailureReason());
             }
             admin.closeSession();
         }
         
         return resp;
     }
     
     @Override
     public EditUserClassResponse editUserClass(EditUserClass request)
     {
         /* Request parameters. */
         UserClassType ucReq = request.getEditUserClass();
         String name = ucReq.getUserClassName();
         int id = ucReq.getUserClassID(), pri = ucReq.getPriority();
         this.logger.debug("Received edit user request with id=" + id + ", name=" + name + ", priority=" + pri + ", active=" + 
                 ucReq.getIsActive() + ", kickable=" + ucReq.getIsKickable() + ", queueable=" + ucReq.getIsQueuable() +
                 ", lockable=" + ucReq.getIsUserLockable() + '.');
         
         /* Response parameters. */
         EditUserClassResponse resp = new EditUserClassResponse();
         OperationResponseType op = new OperationResponseType();
         resp.setEditUserClassResponse(op);
         op.setSuccessful(false);
         
         UserClassAdmin admin = new UserClassAdmin(DataAccessActivator.getNewSession());
         if (!this.checkPermission(ucReq))
         {
             op.setFailureCode(1);
             op.setFailureReason("Permission denied.");
         }
         else if (id > 0)
         {
             if (admin.editUserClass(id, name, pri, ucReq.getIsActive(), ucReq.getIsQueuable(), ucReq.getIsKickable(), 
                     ucReq.getIsUserLockable()))
             {
                 op.setSuccessful(true);
             }
             else
             {
                 op.setFailureCode(3);
                 op.setFailureReason(admin.getFailureReason());
             }
         }
         else if (name != null)
         {
             
             if (admin.editUserClass(name, pri, ucReq.getIsActive(), ucReq.getIsQueuable(), ucReq.getIsKickable(), 
                     ucReq.getIsUserLockable()))
             {
                 op.setSuccessful(true);
             }
             else
             {
                 op.setFailureCode(3);
                 op.setFailureReason(admin.getFailureReason());
             }
         }
         else
         {
             op.setFailureCode(2);
             op.setFailureReason("Mandatory parameter not provided");
         }
         
         admin.closeSession();
         return resp;
     }
     
     @Override
     public DeleteUserClassResponse deleteUserClass(DeleteUserClass request)
     {
         /* Request parameters. */
         UserClassIDType usClRequest = request.getDeleteUserClass();
         String name = usClRequest.getUserClassName();
         int id = usClRequest.getUserClassID();
         this.logger.debug("Received delete user request with id=" + id + ", name=" + name + ".");
         
         /* Response parameters. */
         DeleteUserClassResponse resp = new DeleteUserClassResponse();
         OperationResponseType op = new OperationResponseType();
         resp.setDeleteUserClassResponse(op);
         op.setSuccessful(false);
         
         UserClassAdmin admin = new UserClassAdmin(DataAccessActivator.getNewSession());
         if (!this.checkPermission(usClRequest))
         {
             op.setFailureCode(1);
             op.setFailureReason("Permission denied");
         }
         else if (id > 0)
         {
             if (admin.deleteUserClass(id))
             {
                 op.setSuccessful(true);
             }
             else
             {
                 op.setFailureCode(3);
                 op.setFailureReason(admin.getFailureReason());
             }
         }
         else if (name != null)
         {
             if (admin.deleteUserClass(name))
             {
                 op.setSuccessful(true);
             }
             else
             {
                 op.setFailureCode(3);
                 op.setFailureReason(admin.getFailureReason());
             }
         }
         else
         {
             op.setFailureCode(2);
             op.setFailureReason("Mandatory parameter not provided");
         }
         
         admin.closeSession();
         return resp;
     }
     
     @Override
     public AddUserAssociationResponse addUserAssociation(AddUserAssociation request)
     {
         /* Request parameters. */
         UserAssociationType assocReq = request.getAddUserAssociation();
         UserIDType uId = assocReq.getUser();
         UserClassIDType uClsId = assocReq.getUserClass();
         String unm = uId.getUserName(), uns = uId.getUserNamespace(), cnm = uClsId.getUserClassName();
         long userId = 0, classId = uClsId.getUserClassID();
         try
         {
             userId = Integer.parseInt(uId.getUserID());
         }
         catch (NumberFormatException ex) { /* Not going to use ID them. */ }
         this.logger.debug("Received add user association request for user id=" + userId + ", user name=" + unm +
                 ", user namespace" + uns + ", class id=" + classId + ", class name=" + cnm + ".");
         
         /* Response parameters. */
         AddUserAssociationResponse resp = new AddUserAssociationResponse();
         OperationResponseType op = new OperationResponseType();
         resp.setAddUserAssociationResponse(op);
         op.setSuccessful(false);
         
         UserClassDao classDao = new UserClassDao();
         UserDao userDao = new UserDao(classDao.getSession());
         UserAssociationDao assocDao = new UserAssociationDao(classDao.getSession());
         User user;
         UserClass userClass;
         if (!this.checkPermission(assocReq))
         {
             op.setFailureCode(1);
             op.setFailureReason("Permission denied");
         }
         else if (((userId > 0 && (user = userDao.get(userId)) != null) || // Load user from ID
                  (uns != null && unm != null && (user = userDao.findByName(uns, unm)) != null)) && // Load user from namespace & name
                 ((classId > 0 && (userClass = classDao.get(classId)) != null) || // Load class from ID
                  (cnm != null && (userClass = classDao.findByName(cnm)) != null))) // Load class from name
         {
             UserAssociationId assocId = new UserAssociationId(user.getId(), userClass.getId());
             if (assocDao.get(assocId) == null)
             {
                 UserAssociation assoc = new UserAssociation();
                 assoc.setId(assocId);
                 assoc.setUser(user);
                 assoc.setUserClass(userClass);
                 assocDao.persist(assoc);
                 op.setSuccessful(true);
             }
             else
             {
                 op.setFailureCode(3);
                 op.setFailureReason("Association already exists.");
             }
         }
         else
         {
             op.setFailureCode(2);
             op.setFailureReason("A mandatory parameter was not provided");
         }
         
         classDao.closeSession();
         return resp;
     }
     
     @Override
     public DeleteUserAssociationResponse deleteUserAssociation(DeleteUserAssociation request)
     {
         /* Request parameters. */
         UserAssociationType assocReq = request.getDeleteUserAssociation();
         UserIDType uId = assocReq.getUser();
         UserClassIDType uClsId = assocReq.getUserClass();
         String unm = uId.getUserName(), uns = uId.getUserNamespace(), cnm = uClsId.getUserClassName();
         long userId = 0, classId = uClsId.getUserClassID();
         try
         {
             userId = Integer.parseInt(uId.getUserID());
         }
         catch (NumberFormatException ex) { /* Not going to use ID them. */ }
         this.logger.debug("Received delete user association request for user id=" + userId + ", user name=" + unm +
                 ", user namespace" + uns + ", class id=" + classId + ", class name=" + cnm + ".");
         
         /* Response parameters. */
         DeleteUserAssociationResponse resp = new DeleteUserAssociationResponse();
         OperationResponseType op = new OperationResponseType();
         resp.setDeleteUserAssociationResponse(op);
         op.setSuccessful(false);
         
         UserClassDao classDao = new UserClassDao();
         UserDao userDao = new UserDao(classDao.getSession());
         UserAssociationDao assocDao = new UserAssociationDao(classDao.getSession());
         User user;
         UserClass userClass;
         if (!this.checkPermission(assocReq))
         {
             op.setFailureCode(1);
             op.setFailureReason("Permission denied");
         }
         else if (((userId > 0 && (user = userDao.get(userId)) != null) || // Load user from ID
                   (uns != null && unm != null && (user = userDao.findByName(uns, unm)) != null)) && // Load user from namespace & name
                  ((classId > 0 && (userClass = classDao.get(classId)) != null) || // Load class from ID
                   (cnm != null && (userClass = classDao.findByName(cnm)) != null))) // Load class from name
         {
             assocDao.delete(new UserAssociationId(user.getId(), userClass.getId()));
             op.setSuccessful(true);
         }
         else
         {
             op.setFailureCode(2);
             op.setFailureReason("A mandatory parameter was not provided");
         }
         
         classDao.closeSession();
         return resp;
     }
 
     
     /* (non-Javadoc)
      * @see au.edu.uts.eng.remotelabs.schedserver.permissions.intf.PermissionsSkeletonInterface#addAcademicPermission(au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.AddAcademicPermission)
      */
     @Override
     public AddAcademicPermissionResponse addAcademicPermission(AddAcademicPermission request)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     /* (non-Javadoc)
      * @see au.edu.uts.eng.remotelabs.schedserver.permissions.intf.PermissionsSkeletonInterface#addPermission(au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.AddPermission)
      */
     @Override
     public AddPermissionResponse addPermission(AddPermission request)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     /* (non-Javadoc)
      * @see au.edu.uts.eng.remotelabs.schedserver.permissions.intf.PermissionsSkeletonInterface#addUserLock(au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.AddUserLock)
      */
     @Override
     public AddUserLockResponse addUserLock(AddUserLock request)
     {
         /* Request parameters. */
         
         /* Response parameters. */
         return null;
     }
 
     /* (non-Javadoc)
      * @see au.edu.uts.eng.remotelabs.schedserver.permissions.intf.PermissionsSkeletonInterface#bulkAddUserClassUsers(au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.BulkAddUserClassUsers)
      */
     @Override
     public BulkAddUserClassUsersResponse bulkAddUserClassUsers(BulkAddUserClassUsers request)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     /* (non-Javadoc)
      * @see au.edu.uts.eng.remotelabs.schedserver.permissions.intf.PermissionsSkeletonInterface#deleteAcademicPermission(au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.DeleteAcademicPermission)
      */
     @Override
     public DeleteAcademicPermissionResponse deleteAcademicPermission(DeleteAcademicPermission request)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     /* (non-Javadoc)
      * @see au.edu.uts.eng.remotelabs.schedserver.permissions.intf.PermissionsSkeletonInterface#deletePermission(au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.DeletePermission)
      */
     @Override
     public DeletePermissionResponse deletePermission(DeletePermission request)
     {
         // TODO Auto-generated method stub
         return null;
     }
     
 
     /* (non-Javadoc)
      * @see au.edu.uts.eng.remotelabs.schedserver.permissions.intf.PermissionsSkeletonInterface#deleteUserLock(au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.DeleteUserLock)
      */
     @Override
     public DeleteUserLockResponse deleteUserLock(DeleteUserLock request)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     /* (non-Javadoc)
      * @see au.edu.uts.eng.remotelabs.schedserver.permissions.intf.PermissionsSkeletonInterface#editAcademicPermission(au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.EditAcademicPermission)
      */
     @Override
     public EditAcademicPermissionResponse editAcademicPermission(EditAcademicPermission request)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     /* (non-Javadoc)
      * @see au.edu.uts.eng.remotelabs.schedserver.permissions.intf.PermissionsSkeletonInterface#editPermission(au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.EditPermission)
      */
     @Override
     public EditPermissionResponse editPermission(EditPermission request)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     /* (non-Javadoc)
      * @see au.edu.uts.eng.remotelabs.schedserver.permissions.intf.PermissionsSkeletonInterface#getAcademicPermission(au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetAcademicPermission)
      */
     @Override
     public GetAcademicPermissionResponse getAcademicPermission(GetAcademicPermission request)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     /* (non-Javadoc)
      * @see au.edu.uts.eng.remotelabs.schedserver.permissions.intf.PermissionsSkeletonInterface#getAcademicPermissionsForAcademic(au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetAcademicPermissionsForAcademic)
      */
     @Override
     public GetAcademicPermissionsForAcademicResponse getAcademicPermissionsForAcademic(
             GetAcademicPermissionsForAcademic request)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     /* (non-Javadoc)
      * @see au.edu.uts.eng.remotelabs.schedserver.permissions.intf.PermissionsSkeletonInterface#getAcademicPermissionsForUserClass(au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetAcademicPermissionsForUserClass)
      */
     @Override
     public GetAcademicPermissionsForUserClassResponse getAcademicPermissionsForUserClass(
             GetAcademicPermissionsForUserClass request)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     /* (non-Javadoc)
      * @see au.edu.uts.eng.remotelabs.schedserver.permissions.intf.PermissionsSkeletonInterface#getPermission(au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetPermission)
      */
     @Override
     public GetPermissionResponse getPermission(GetPermission request)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public GetPermissionsForUserResponse getPermissionsForUser(GetPermissionsForUser request)
     {
         /* Request parameters. */
         UserIDType uid = request.getGetPermissionsForUser();
         String ns = uid.getUserNamespace(), nm = uid.getUserName();
         long id = this.getIdentifier(uid.getUserID());
         this.logger.debug("Received get permissions for user with id=" + id + ", namespace=" + ns + ", name=" + nm + '.');
         
         /* Response parameters. */
         GetPermissionsForUserResponse resp = new GetPermissionsForUserResponse();
         PermissionWithLockListType permList = new PermissionWithLockListType();
         resp.setGetPermissionsForUserResponse(permList);
         
         /* 1) Load user. */
         UserDao userDao = new UserDao();
         User user = null;
         if (id > 0) user = userDao.get(id);
         else if (ns != null && nm != null) user = userDao.findByName(ns, nm);
 
         if (user == null)
         {
             this.logger.debug("User not found for getting permissions, id is " + id + ", namespce is " + ns + 
                     " and name is " + nm + '.');
             userDao.closeSession();
             return resp;
         }
         
         /* Get a list of resources that are locked. */
         List<Long> lockedResources = new ArrayList<Long>();
         for (UserLock lock : user.getUserLocks())
         {
             if (lock.isIsLocked()) lockedResources.add(lock.getResourcePermission().getId());
         }
         
         /* For each of the user classes the user is a member of, add all its resource permissions. */
         for (UserAssociation assoc : user.getUserAssociations())
         {
             UserClass userClass = assoc.getUserClass();
             if (!userClass.isActive()) continue;
             for (ResourcePermission resPerm : userClass.getResourcePermissions())
             {
                 PermissionWithLockType permWithLock = new PermissionWithLockType();
                 PermissionType perm = new PermissionType();
                 perm.setPermissionID(resPerm.getId().intValue());
                 permWithLock.setPermission(perm);
 
                 /* Add user class. */
                 UserClassIDType userClassIdType = new UserClassIDType();
                 userClassIdType.setUserClassID(userClass.getId().intValue());
                 userClassIdType.setUserClassName(userClass.getName());
                 perm.setUserClass(userClassIdType);
                 
                 /* Add resource. */
                 ResourceIDType resourceIdType = new ResourceIDType();
                 perm.setResource(resourceIdType);
                 if (ResourcePermission.RIG_PERMISSION.equals(resPerm.getType()))
                 {
                     Rig rig = resPerm.getRig();
                     if (rig == null)
                     {
                         this.logger.warn("Incorrect configuration of a rig resource permission with " +
                                 "id " + resPerm.getId() + ", as no rig is set.");
                         continue;
                     }
                     
                     perm.setResourceClass(ResourceClass.RIG);
                     resourceIdType.setResourceID(rig.getId().intValue());
                     resourceIdType.setResourceName(rig.getName());
                 }
                 else if (ResourcePermission.TYPE_PERMISSION.equals(resPerm.getType()))
                 {
                     RigType rigType = resPerm.getRigType();
                     if (rigType == null)
                     {
                         this.logger.warn("Incorrect configuration of a rig type resource permission with " +
                                 "id " + resPerm.getId() + ", as no rig type is set.");
                         continue;
                     }
                     
                     perm.setResourceClass(ResourceClass.TYPE);
                     resourceIdType.setResourceID(rigType.getId().intValue());
                     resourceIdType.setResourceName(rigType.getName());
                 }
                 else if (ResourcePermission.CAPS_PERMISSION.equals(resPerm.getType()))
                 {
                     RequestCapabilities caps = resPerm.getRequestCapabilities();
                     if (caps == null)
                     {
                         this.logger.warn("Incorrect configuration of a request capabilities resource permission with " +
                                 "id " + resPerm.getId() + ", as no request capabilities are set.");
                         continue;
                     }
                     perm.setResourceClass(ResourceClass.CAPABILITY);
                     resourceIdType.setResourceID(caps.getId().intValue());
                     resourceIdType.setResourceName(caps.getCapabilities());
                 }
                 else
                 {
                     this.logger.warn("Incorrect configuration of a resource permission with id " + resPerm.getId() + 
                             ". It has an unknown resource type " + resPerm.getType() + ". It should be one of " +
                            "'RIG', 'TYPE' or 'CAPABILITY'.");
                 }
 
                 /* Add information about permission. */
                 perm.setSessionDuration(resPerm.getSessionDuration());
                 perm.setExtensionDuration(resPerm.getExtensionDuration());
                 perm.setAllowedExtensions(resPerm.getAllowedExtensions());
                 perm.setQueueActivityTmOut(resPerm.getQueueActivityTimeout());
                 perm.setSessionActivityTmOut(resPerm.getSessionActivityTimeout());
                 Calendar start = Calendar.getInstance();
                 start.setTime(resPerm.getStartTime());
                 perm.setStart(start);
                 Calendar expiry = Calendar.getInstance();
                 expiry.setTime(resPerm.getExpiryTime());
                 perm.setExpiry(expiry);
                 perm.setDisplayName(resPerm.getDisplayName());
                 
                 /* Add if the resource permission is locked. */
                 permWithLock.setIsLocked(lockedResources.contains(resPerm.getId()));
                 permList.addPermission(permWithLock);
             }
         }
         
         userDao.closeSession();
         return resp;
     }
 
     /* (non-Javadoc)
      * @see au.edu.uts.eng.remotelabs.schedserver.permissions.intf.PermissionsSkeletonInterface#getPermissionsForUserClass(au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetPermissionsForUserClass)
      */
     @Override
     public GetPermissionsForUserClassResponse getPermissionsForUserClass(GetPermissionsForUserClass request)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     /* (non-Javadoc)
      * @see au.edu.uts.eng.remotelabs.schedserver.permissions.intf.PermissionsSkeletonInterface#getUserClasses(au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetUserClasses)
      */
     @Override
     public GetUserClassesResponse getUserClasses(GetUserClasses request)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     /* (non-Javadoc)
      * @see au.edu.uts.eng.remotelabs.schedserver.permissions.intf.PermissionsSkeletonInterface#getUserClassesForUser(au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetUserClassesForUser)
      */
     @Override
     public GetUserClassesForUserResponse getUserClassesForUser(GetUserClassesForUser request)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     /* (non-Javadoc)
      * @see au.edu.uts.eng.remotelabs.schedserver.permissions.intf.PermissionsSkeletonInterface#getUsersInUserClass(au.edu.uts.eng.remotelabs.schedserver.permissions.intf.types.GetUsersInUserClass)
      */
     @Override
     public GetUsersInUserClassResponse getUsersInUserClass(GetUsersInUserClass request)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public UnlockUserLockResponse unlockUserLock(UnlockUserLock request)
     {
         /* Request parameters. */
         UserLockType lockReq = request.getUnlockUserLock();
         String key = lockReq.getLockKey();
         int lockId = lockReq.getUserLockID();
         UserLockIDUserPermSequence seq = lockReq.getUserIDPermissionsSequence();
         
         /* Response parameters. */
         UnlockUserLockResponse resp = new UnlockUserLockResponse();
         OperationResponseType op = new OperationResponseType();
         resp.setUnlockUserLockResponse(op);
         op.setSuccessful(false);
         
         UserLockDao dao = new UserLockDao();
         if (!this.checkPermission(lockReq))
         {
             this.logger.warn("Failed unlocking user permission because the requestor does not have permission.");
             op.setFailureCode(1);
             op.setFailureReason("Permission denied.");
         }
         else if (key == null)
         {
             this.logger.warn("Failed unlocking user lock because the no lock key was supplied.");
             op.setFailureCode(2);
             op.setFailureReason("Mandatory parameter not provided.");
         }
         else if (lockId > 0)
         {
             this.logger.debug("Received unlock user lock request with lock identifier=" + lockId + ", lock key=" + key + '.');
             UserLock lock = dao.get(Long.valueOf(lockId));
             if (lock == null)
             {
                 this.logger.warn("Fail unlocking user lock (id=" + lockId + ") because the lock was not found.");
                 op.setFailureCode(3);
                 op.setFailureReason("User lock not found.");
             }
             else if (lock.getLockKey().equals(key))
             {
                 lock.setIsLocked(false);
                 dao.flush();
                 op.setSuccessful(true);
             }
             else
             {
                 this.logger.warn("Fail unlocking user lock because the supplied lock key was incorrect " +
                 		"(supplied=" + key + ", actual=" + lock.getLockKey() + ").");
                 op.setFailureCode(3);
                 op.setFailureReason("Provided key does not match.");
             }
         }
         else if (seq != null)
         {
             UserIDType uid = seq.getUserID();
             long pId = seq.getPermissionID().getPermissionID();
             this.logger.debug("Received unlock user lock request with permission id=" + seq.getPermissionID().getPermissionID() +
                         ", user id=" + uid.getUserID() + ", user namespace= " + uid.getUserNamespace() + 
                         ", user name=" + uid.getUserName()  + ", lock key=" + key + '.');
 
             User user = this.getUserFromUserID(seq.getUserID(), dao.getSession());
             ResourcePermission perm = new ResourcePermissionDao(dao.getSession()).get(pId);
             UserLock lock;
             
             if (user == null || perm == null || (lock = dao.findLock(user, perm)) == null)
             {                
                 this.logger.warn("Fail unlocking user lock (permission id=" + seq.getPermissionID().getPermissionID() +
                         ", user id= " + uid.getUserID() + ", user namespace= " + uid.getUserNamespace() + 
                         ", user name=" + uid.getUserName()  + ") because the lock was not found.");
                 op.setFailureCode(3);
                 op.setFailureReason("User lock not found.");
             }
             else if (lock.getLockKey().equals(key))
             {
                 lock.setIsLocked(false);
                 dao.flush();
                 op.setSuccessful(true);
             }
             else
             {
                 this.logger.warn("Fail unlocking user lock because the supplied lock key was incorrect " +
                 		"(supplied=" + key + ", actual=" + lock.getLockKey() + ").");
                 op.setFailureCode(3);
                 op.setFailureReason("Provided key does not match.");
             }    
         }
         else
         {
             this.logger.warn("Failed unlocking user lock because the no lock key was supplied.");
             op.setFailureCode(2);
             op.setFailureReason("Mandatory parameter not provided.");
         }
         
         dao.closeSession();
         return resp;
     }
 
     /**
      * Checks whether the request has the specified permission.
      */
     private boolean checkPermission(OperationRequestType req)
     {
         // TODO check request permissions.
         return true;
     }
     
     /**
      * Gets the user identified by the user id type. 
      * 
      * @param uid user identity 
      * @param ses database session
      * @return user or null if not found
      */
     private User getUserFromUserID(UserIDType uid, Session ses)
     {
         UserDao dao = new UserDao(ses);
         User user;
         long recordId = this.getIdentifier(uid.getUserID());
         String ns = uid.getUserNamespace(), nm = uid.getUserName();
         
         if (recordId > 0 && (user = dao.get(recordId)) != null)
         {
             return user;
         }
         else if (ns != null && nm != null && (user = dao.findByName(ns, nm)) != null)
         {
             return user;
         }
         
         return null;
     }
     
     /**
      * Converts string identifiers to a long.
      * 
      * @param idStr string containing a long  
      * @return long or 0 if identifier not valid
      */
     private long getIdentifier(String idStr)
     {
         if (idStr == null) return 0;
         
         try
         {
             return Long.parseLong(idStr);
         }
         catch (NumberFormatException nfe)
         {
             return 0;
         }
     }
 }
