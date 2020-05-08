 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.shared.content;
 
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.exceptions.FxNoAccessException;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.security.*;
 import com.flexive.shared.structure.FxEnvironment;
 import com.flexive.shared.structure.FxPropertyAssignment;
 import com.flexive.shared.structure.FxType;
 import com.flexive.shared.value.FxNoAccess;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Permission Utilities
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class FxPermissionUtils {
     private static final Log LOG = LogFactory.getLog(FxPermissionUtils.class);
 
     public static final byte PERM_MASK_INSTANCE = 0x01;
     public static final byte PERM_MASK_PROPERTY = 0x02;
     public static final byte PERM_MASK_STEP = 0x04;
     public static final byte PERM_MASK_TYPE = 0x08;
 
     /**
      * Permission check for (new) contents
      *
      * @param ticket         calling users ticket
      * @param ownerId        owner of the content to check
      * @param permission     permission to check
      * @param type           used type
      * @param stepACL        step ACL
      * @param contentACL     content ACL
      * @param throwException should exception be thrown
      * @return access granted
      * @throws FxNoAccessException if not accessible for calling user
      */
     public static boolean checkPermission(UserTicket ticket, long ownerId, ACL.Permission permission, FxType type, long stepACL,
                                           long contentACL, boolean throwException) throws FxNoAccessException {
         if (ticket.isGlobalSupervisor() || !type.usePermissions() || FxContext.get().getRunAsSystem())
             return true;
         boolean typeAllowed = !type.useTypePermissions();
         boolean stepAllowed = !type.useStepPermissions();
         boolean contentAllowed = !type.useInstancePermissions();
         for (ACLAssignment assignment : ticket.getACLAssignments()) {
             if (!typeAllowed && assignment.getAclId() == type.getACL().getId())
                 typeAllowed = assignment.getPermission(permission, ownerId, ticket.getUserId());
             if (!stepAllowed && assignment.getAclId() == stepACL)
                 stepAllowed = assignment.getPermission(permission, ownerId, ticket.getUserId());
             if (!contentAllowed && assignment.getAclId() == contentACL)
                 contentAllowed = assignment.getPermission(permission, ownerId, ticket.getUserId());
         }
         if (throwException && !(typeAllowed && stepAllowed && contentAllowed)) {
             List<String> lacking = new ArrayList<String>(3);
             if (!typeAllowed)
                 addACLName(lacking, ticket.getLanguage(), type.getACL().getId());
             if (!stepAllowed)
                 addACLName(lacking, ticket.getLanguage(), stepACL);
             if (!contentAllowed)
                 addACLName(lacking, ticket.getLanguage(), contentACL);
             String[] params = new String[lacking.size() + 1];
             params[0] = "ex.acl.name." + permission.toString().toLowerCase();
             for (int i = 0; i < lacking.size(); i++)
                 params[i + 1] = lacking.get(i);
             throw new FxNoAccessException("ex.acl.noAccess.extended." + lacking.size(), (Object[]) params);
         }
         return typeAllowed && stepAllowed && contentAllowed;
     }
 
     /**
      * Permission check for existing contents
      *
      * @param ticket         calling users ticket
      * @param permission     permission to check
      * @param si             security info of the content to check
      * @param throwException should exception be thrown
      * @return access granted
      * @throws FxNoAccessException if access denied and exception should be thrown
      */
     public static boolean checkPermission(UserTicket ticket, ACL.Permission permission, FxContentSecurityInfo si, boolean throwException) throws FxNoAccessException {
         if (!si.usePermissions() || ticket.isGlobalSupervisor() || FxContext.get().getRunAsSystem())
             return true;
         boolean typeAllowed = !si.useTypePermissions();
         boolean stepAllowed = !si.useStepPermissions();
         boolean contentAllowed = !si.useInstancePermissions();
         boolean propertyAllowed = true;
 
         List<String> lacking = (throwException ? new ArrayList<String>(3) : null);
 
         for (ACLAssignment assignment : ticket.getACLAssignments()) {
             if (!typeAllowed && assignment.getAclId() == si.getTypeACL())
                 typeAllowed = assignment.getPermission(permission, si.getOwnerId(), ticket.getUserId());
             if (!stepAllowed && assignment.getAclId() == si.getStepACL())
                 stepAllowed = assignment.getPermission(permission, si.getOwnerId(), ticket.getUserId());
             if (!contentAllowed && assignment.getAclId() == si.getContentACL())
                 contentAllowed = assignment.getPermission(permission, si.getOwnerId(), ticket.getUserId());
             if (permission == ACL.Permission.DELETE) {
                 //property permissions are only checked for delete operations since no
                 //exception should be thrown when ie loading as properties are wrapped in
                 //FxNoAccess values or set to read only
                 if (si.usePermissions() && si.getUsedPropertyACL().size() > 0 && assignment.getACLCategory() == ACL.Category.STRUCTURE) {
                     for (long propertyACL : si.getUsedPropertyACL())
                         if (propertyACL == assignment.getAclId())
                             if (!assignment.getPermission(permission, si.getOwnerId(), ticket.getUserId())) {
                                 propertyAllowed = false;
                                 addACLName(lacking, ticket.getLanguage(), propertyACL);
                             }
                 }
             }
         }
         if (throwException && !(typeAllowed && stepAllowed && contentAllowed && propertyAllowed)) {
             if (!typeAllowed)
                 addACLName(lacking, ticket.getLanguage(), si.getTypeACL());
             if (!stepAllowed)
                 addACLName(lacking, ticket.getLanguage(), si.getStepACL());
             if (!contentAllowed)
                 addACLName(lacking, ticket.getLanguage(), si.getContentACL());
             String[] params = new String[lacking.size() + 1];
             params[0] = "ex.acl.name." + permission.toString().toLowerCase();
             for (int i = 0; i < lacking.size(); i++)
                 params[i + 1] = lacking.get(i);
             throw new FxNoAccessException("ex.acl.noAccess.extended." + lacking.size(), (Object[]) params);
         }
         return typeAllowed && stepAllowed && contentAllowed && propertyAllowed;
     }
 
     /**
      * Get the translation for an ACL name in the requested language and add it to the given list
      *
      * @param list list to add the name
      * @param lang language
      * @param acl  id of the acl
      */
     private static void addACLName(List<String> list, FxLanguage lang, long acl) {
         String name;
         try {
            name = CacheAdmin.getEnvironment().getACL(acl).getLabel().getTranslation(lang);
         } catch (Exception e) {
             name = "#" + acl;
         }
         if (list == null)
             list = new ArrayList<String>(3);
         if (!list.contains(name))
             list.add(name);
     }
 
     /**
      * Process a contents property and wrap FxValue's in FxNoAccess or set them to readonly where appropriate
      *
      * @param ticket       calling users ticket
      * @param securityInfo needed security information
      * @param content      the content to process
      * @param type         the content's FxType
      * @param env          environment
      * @throws FxNotFoundException         on errors
      * @throws FxInvalidParameterException on errors
      * @throws FxNoAccessException         on errors
      */
     public static void wrapNoAccessValues(UserTicket ticket, FxContentSecurityInfo securityInfo, FxContent content, FxType type, FxEnvironment env) throws FxNotFoundException, FxInvalidParameterException, FxNoAccessException {
         if (!type.usePropertyPermissions() || ticket.isGlobalSupervisor() || FxContext.get().getRunAsSystem())
             return; //invalid call, nothing to process ...
         List<String> xpaths = content.getAllPropertyXPaths();
         FxPropertyData pdata;
         List<Long> noAccess = new ArrayList<Long>(5);
         List<Long> readOnly = new ArrayList<Long>(5);
         for (long aclId : securityInfo.getUsedPropertyACL()) {
             if (!ticket.mayReadACL(aclId, securityInfo.getOwnerId()))
                 noAccess.add(aclId);
             else if (!ticket.mayEditACL(aclId, securityInfo.getOwnerId()))
                 readOnly.add(aclId);
         }
         for (String xpath : xpaths) {
             pdata = content.getPropertyData(xpath);
             if (pdata.getValue() instanceof FxNoAccess)
                 continue;
             ACL propACL = ((FxPropertyAssignment) env.getAssignment(pdata.getAssignmentId())).getACL();
             if (noAccess.contains(propACL.getId())) {
                 //may not read => wrap in a FxNoAccess value
                 pdata.setValue(new FxNoAccess(ticket, pdata.getValue()));
             } else if (readOnly.contains(propACL.getId())) {
                 //may not edit => set readonly
                 pdata.getValue().setReadOnly();
             }
         }
     }
 
     /**
      * Unwrap all FxNoAccess values to their original values.
      * Must be called as supervisor to work ...
      *
      * @param content the FxContent to process
      * @throws FxNotFoundException         on errors
      * @throws FxInvalidParameterException on errors
      * @throws FxNoAccessException         on errors
      */
     public static void unwrapNoAccessValues(FxContent content) throws FxNotFoundException, FxInvalidParameterException, FxNoAccessException {
         List<String> xpaths = content.getAllPropertyXPaths();
         FxPropertyData pdata;
         for (String xpath : xpaths) {
             pdata = content.getPropertyData(xpath);
             if (pdata.getValue() instanceof FxNoAccess)
                 pdata.setValue(((FxNoAccess) pdata.getValue()).getWrappedValue());
         }
     }
 
     /**
      * Check if the calling user has the requested permission for all properties in this content.
      * Call only if the assigned type uses propery permissions!
      *
      * @param content content to check
      * @param perm    requested permission
      * @throws FxNotFoundException         on errors
      * @throws FxInvalidParameterException on errors
      * @throws FxNoAccessException         on errors
      */
     public static void checkPropertyPermissions(FxContent content, ACL.Permission perm) throws FxNotFoundException, FxInvalidParameterException, FxNoAccessException {
         final UserTicket ticket = FxContext.get().getTicket();
         List<String> xpaths = content.getAllPropertyXPaths();
         FxPropertyData pdata;
         for (String xpath : xpaths) {
             pdata = content.getPropertyData(xpath);
             if (pdata.getValue() instanceof FxNoAccess || pdata.isEmpty() || pdata.getValue().isReadOnly())
                 continue; //dont touch NoAccess or readonly values
             if (perm == ACL.Permission.EDIT && !ticket.mayEditACL(((FxPropertyAssignment) pdata.getAssignment()).getACL().getId(), content.getLifeCycleInfo().getCreatorId()))
                 throw new FxNoAccessException("ex.acl.noAccess.property.edit", xpath);
             else
             if (perm == ACL.Permission.CREATE && !ticket.mayCreateACL(((FxPropertyAssignment) pdata.getAssignment()).getACL().getId(), content.getLifeCycleInfo().getCreatorId()))
                 throw new FxNoAccessException("ex.acl.noAccess.property.create", xpath);
             else
             if (perm == ACL.Permission.READ && !ticket.mayReadACL(((FxPropertyAssignment) pdata.getAssignment()).getACL().getId(), content.getLifeCycleInfo().getCreatorId()))
                 throw new FxNoAccessException("ex.acl.noAccess.property.read", xpath);
             else
             if (perm == ACL.Permission.DELETE && !ticket.mayDeleteACL(((FxPropertyAssignment) pdata.getAssignment()).getACL().getId(), content.getLifeCycleInfo().getCreatorId()))
                 throw new FxNoAccessException("ex.acl.noAccess.property.delete", xpath);
         }
     }
 
     /**
      * Encode permissions for use in FxType
      *
      * @param useInstancePermissions instance
      * @param usePropertyPermissions property
      * @param useStepPermissions     (workflow)step
      * @param useTypePermissions     type
      * @return encoded permissions
      */
     public static byte encodeTypePermissions(boolean useInstancePermissions, boolean usePropertyPermissions, boolean useStepPermissions,
                                              boolean useTypePermissions) {
         byte perm = 0;
         if (useInstancePermissions)
             perm = PERM_MASK_INSTANCE;
         if (usePropertyPermissions)
             perm |= PERM_MASK_PROPERTY;
         if (useStepPermissions)
             perm |= PERM_MASK_STEP;
         if (useTypePermissions)
             perm |= PERM_MASK_TYPE;
         return perm;
     }
 
     /**
      * Get a human readable form of bit coded permissions
      *
      * @param bitCodedPermissions permissions
      * @return human readable form
      */
     public static String toString(byte bitCodedPermissions) {
         StringBuilder res = new StringBuilder(30);
         if ((bitCodedPermissions & PERM_MASK_TYPE) == PERM_MASK_TYPE)
             res.append(",Type");
         if ((bitCodedPermissions & PERM_MASK_STEP) == PERM_MASK_STEP)
             res.append(",Step");
         if ((bitCodedPermissions & PERM_MASK_PROPERTY) == PERM_MASK_PROPERTY)
             res.append(",Property");
         if ((bitCodedPermissions & PERM_MASK_INSTANCE) == PERM_MASK_INSTANCE)
             res.append(",Instance");
         if (res.length() > 0)
             res.deleteCharAt(0);
         return res.toString();
     }
 
     /**
      * Get a users permission for a given instance ACL
      *
      * @param acl       instance ACL
      * @param type      used type
      * @param stepACL   step ACL
      * @param createdBy owner
      * @param mandator  mandator
      * @return array of permissions in the order edit, relate, delete, export and create
      * @throws com.flexive.shared.exceptions.FxNoAccessException
      *          if no read access if permitted
      */
     public static PermissionSet getPermissions(long acl, FxType type, long stepACL, long createdBy, long mandator) throws FxNoAccessException {
         final UserTicket ticket = FxContext.get().getTicket();
         final boolean _system = FxContext.get().getRunAsSystem() || ticket.isGlobalSupervisor();
         //throw exception if read is forbidden
         checkPermission(ticket, createdBy, ACL.Permission.READ, type, stepACL, acl, true);
         // check for supervisor permissions
         if (_system || ticket.isMandatorSupervisor() && mandator == ticket.getMandatorId() ||
                 !type.usePermissions() /*|| ticket.isInGroup((int) UserGroup.GROUP_OWNER) && createdBy == ticket.getUserId()*/) {
             return new PermissionSet(true, true, true, true, true);
         }
         // get permission matrix from ACL assignments
         return new PermissionSet(
                 checkPermission(ticket, createdBy, ACL.Permission.EDIT, type, stepACL, acl, false),
                 checkPermission(ticket, createdBy, ACL.Permission.RELATE, type, stepACL, acl, false),
                 checkPermission(ticket, createdBy, ACL.Permission.DELETE, type, stepACL, acl, false),
                 checkPermission(ticket, createdBy, ACL.Permission.EXPORT, type, stepACL, acl, false),
                 checkPermission(ticket, createdBy, ACL.Permission.CREATE, type, stepACL, acl, false)
         );
     }
 
     /**
      * Throw an exception if the calling user is not in the given roles
      *
      * @param ticket calling user
      * @param roles  Roles to check
      * @throws com.flexive.shared.exceptions.FxNoAccessException
      *          on errors
      */
     public static void checkRole(UserTicket ticket, Role... roles) throws FxNoAccessException {
         if (ticket.isGlobalSupervisor())
             return;
         for (Role role : roles)
             if (!ticket.isInRole(role)) {
                 throw new FxNoAccessException(LOG, "ex.role.notInRole", role.getName());
             }
     }
 
     /**
      * Check if the requested FxType is available. A FxNotFoundException will be thrown if the FxType's state is
      * <code>TypeState.Unavailable</code>, if <code>allowLocked</code> is <code>true</code> and the FxType's state is
      * <code>TypeState.Locked</code> a FxNoAccessException will be thrown.
      *
      * @param typeId      requested type id to check
      * @param allowLocked allow a locked state?
      * @throws FxApplicationException on errors
      * @see com.flexive.shared.structure.TypeState
      */
     public static void checkTypeAvailable(long typeId, boolean allowLocked) throws FxApplicationException {
         FxType check = CacheAdmin.getEnvironment().getType(typeId);
         switch (check.getState()) {
             case Available:
                 return;
             case Locked:
                 if (allowLocked)
                     return;
                 throw new FxNoAccessException("ex.structure.type.locked", check.getName(), check.getId());
             case Unavailable:
                 throw new FxNotFoundException("ex.structure.type.unavailable", check.getName(), check.getId());
         }
     }
 
     /**
      * Check if the mandator with the requested id exists and is active.
      * Will throw a FxNotFoundException if inactive or not existant.
      *
      * @param id requested mandator id
      * @throws FxNotFoundException if inactive or not existant
      */
     public static void checkMandatorExistance(long id) throws FxNotFoundException {
         Mandator m = CacheAdmin.getEnvironment().getMandator(id);
         if (!m.isActive())
             throw new FxNotFoundException("ex.structure.mandator.notFound.notActive", m.getName(), id);
     }
 }
