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
 import com.flexive.shared.value.FxValue;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.util.*;
 
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
      * @deprecated use {@link #checkPermission(com.flexive.shared.security.UserTicket, long, com.flexive.shared.security.ACLPermission, com.flexive.shared.structure.FxType, long, java.util.Collection, boolean)}
      */
     @Deprecated
     public static boolean checkPermission(UserTicket ticket, long ownerId, ACLPermission permission, FxType type, long stepACL,
                                           long contentACL, boolean throwException) throws FxNoAccessException {
         return checkPermission(ticket, ownerId, permission, type, stepACL, Arrays.asList(contentACL), throwException);
     }
 
     /**
      * Permission check for (new) contents
      *
      * @param ticket         calling users ticket
      * @param ownerId        owner of the content to check
      * @param permission     permission to check
      * @param type           used type
      * @param stepACL        step ACL
      * @param contentACLs     content ACL(s)
      * @param throwException should exception be thrown
      * @return access granted
      * @throws FxNoAccessException if not accessible for calling user
      * @since 3.1
      */
     public static boolean checkPermission(UserTicket ticket, long ownerId, ACLPermission permission, FxType type, long stepACL,
                                           Collection<Long> contentACLs, boolean throwException) throws FxNoAccessException {
         if (ticket.isGlobalSupervisor() || !type.isUsePermissions() || FxContext.get().getRunAsSystem())
             return true;
         boolean typeAllowed = !type.isUseTypePermissions();
         boolean stepAllowed = !type.isUseStepPermissions();
         boolean contentAllowed = !type.isUseInstancePermissions();
         for (ACLAssignment assignment : ticket.getACLAssignments()) {
             if (!typeAllowed && assignment.getAclId() == type.getACL().getId())
                 typeAllowed = assignment.getPermission(permission, ownerId, ticket.getUserId());
             if (!stepAllowed && assignment.getAclId() == stepACL)
                 stepAllowed = assignment.getPermission(permission, ownerId, ticket.getUserId());
             if (!contentAllowed && contentACLs.contains(assignment.getAclId()))
                 contentAllowed = assignment.getPermission(permission, ownerId, ticket.getUserId());
         }
         if (throwException && !(typeAllowed && stepAllowed && contentAllowed)) {
             Set<String> lacking = new HashSet<String>(3);
             if (!typeAllowed)
                 addACLName(lacking, ticket.getLanguage(), type.getACL().getId());
             if (!stepAllowed)
                 addACLName(lacking, ticket.getLanguage(), stepACL);
             if (!contentAllowed) {
                 for (Long acl : contentACLs) {
                     addACLName(lacking, ticket.getLanguage(), acl);
                 }
             }
             throw noAccess(permission, lacking);
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
     public static boolean checkPermission(UserTicket ticket, ACLPermission permission, FxContentSecurityInfo si, boolean throwException) throws FxNoAccessException {
         if (ticket.isGlobalSupervisor() || FxContext.get().getRunAsSystem())
             return true;
         final boolean checkLock;
         switch(permission) {
             case CREATE:
             case DELETE:
             case EDIT:
                 checkLock = true;
                 break;
             default:
                 checkLock = false;
         }
         if( checkLock && si.getLock().isLocked() && !si.getLock().isUnlockable()) {
             if( si.getLock().getUserId() != ticket.getUserId() && !ticket.isMandatorSupervisor()) {
                 if( throwException )
                     throw new FxNoAccessException("ex.lock.content.locked.noAccess", si.getPk());
                 return false;
             }
         }
         if(!si.usePermissions())
             return true;
         boolean typeAllowed = !si.useTypePermissions();
         boolean stepAllowed = !si.useStepPermissions();
         boolean contentAllowed = !si.useInstancePermissions();
         boolean propertyAllowed = true;
 
         final Set<String> lacking = (throwException ? new HashSet<String>(3) : null);
 
         for (ACLAssignment assignment : ticket.getACLAssignments()) {
             if (!typeAllowed && assignment.getAclId() == si.getTypeACL())
                 typeAllowed = assignment.getPermission(permission, si.getOwnerId(), ticket.getUserId());
             if (!stepAllowed && assignment.getAclId() == si.getStepACL())
                 stepAllowed = assignment.getPermission(permission, si.getOwnerId(), ticket.getUserId());
             if (!contentAllowed && si.getContentACLs().contains(assignment.getAclId()))
                 contentAllowed = assignment.getPermission(permission, si.getOwnerId(), ticket.getUserId());
        }
        if (permission == ACLPermission.DELETE && si.usePermissions() && !si.getUsedPropertyACLs().isEmpty()) {
            //property permissions are only checked for delete operations since no
            //exception should be thrown when ie loading as properties are wrapped in
            //FxNoAccess values or set to read only

            // in contrast to content ACLs, a user must have the permission on *all* property ACLs
            // since they are attached to different properties
            for (long propertyACL : si.getUsedPropertyACLs()) {
                if (!ticket.mayDeleteACL(propertyACL, si.getOwnerId())) {
                    propertyAllowed = false;
                    addACLName(lacking, ticket.getLanguage(), propertyACL);
                 }
             }
         }

         if (throwException && !(typeAllowed && stepAllowed && contentAllowed && propertyAllowed)) {
             if (!typeAllowed)
                 addACLName(lacking, ticket.getLanguage(), si.getTypeACL());
             if (!stepAllowed)
                 addACLName(lacking, ticket.getLanguage(), si.getStepACL());
             if (!contentAllowed) {
                 for (Long aclId : si.getContentACLs()) {
                     addACLName(lacking, ticket.getLanguage(), aclId);
                 }
             }
             throw noAccess(permission, lacking);
         }
         return typeAllowed && stepAllowed && contentAllowed && propertyAllowed;
     }
 
     private static FxNoAccessException noAccess(ACLPermission permission, Collection<String> lacking) {
         String[] params = new String[lacking.size() + 1];
         params[0] = "ex.acl.name." + permission.toString().toLowerCase();
         int index = 1;
         final int displayedNames = Math.min(lacking.size(), 4);
         for (String name : lacking) {
             params[index++] = name;
             if (index > displayedNames) {
                 break;
             }
         }
         return new FxNoAccessException("ex.acl.noAccess.extended." + displayedNames, (Object[]) params);
     }
 
     /**
      * Get the translation for an ACL name in the requested language and add it to the given list
      *
      * @param list list to add the name
      * @param lang language
      * @param acl  id of the acl
      */
     private static void addACLName(Set<String> list, FxLanguage lang, long acl) {
         if (list != null) {
             list.add(CacheAdmin.getEnvironment().getACL(acl).getDisplayName());
         }
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
         if (!type.isUsePropertyPermissions() || ticket.isGlobalSupervisor() || FxContext.get().getRunAsSystem())
             return; //invalid call, nothing to process ...
         List<String> xpaths = content.getAllPropertyXPaths();
         FxPropertyData pdata;
         List<Long> noAccess = new ArrayList<Long>(5);
         List<Long> readOnly = new ArrayList<Long>(5);
         for (long aclId : securityInfo.getUsedPropertyACLs()) {
             if (!ticket.mayReadACL(aclId, securityInfo.getOwnerId()))
                 noAccess.add(aclId);
             else if (!ticket.mayEditACL(aclId, securityInfo.getOwnerId()))
                 readOnly.add(aclId);
         }
         if (noAccess.size() == 0 && readOnly.size() == 0)
             return; //nothing to do
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
      * @param content  the FxContent to process
      * @param original the original content to get the wrapped values from
      * @throws FxNotFoundException         on errors
      * @throws FxInvalidParameterException on errors
      * @throws FxNoAccessException         on errors
      */
     public static void unwrapNoAccessValues(FxContent content, FxContent original) throws FxNotFoundException, FxInvalidParameterException, FxNoAccessException {
         List<String> xpaths = content.getAllPropertyXPaths();
         FxPropertyData pdata;
         for (String xpath : xpaths) {
             pdata = content.getPropertyData(xpath);
             if (pdata.getValue() instanceof FxNoAccess)
                 pdata.setValue(original.getValue(((FxNoAccess) pdata.getValue()).getOriginalXPath()));
         }
     }
 
     /**
      * Check if the calling user has the requested permission for all properties in this content.
      * Call only if the assigned type uses propery permissions!
      * Delete permission can not be checked using this method since it can't be determined if a property has been removed!
      *
      * @param content content to check
      * @param perm    requested permission
      * @throws FxNotFoundException         on errors
      * @throws FxInvalidParameterException on errors
      * @throws FxNoAccessException         on errors
      */
     public static void checkPropertyPermissions(FxContent content, ACLPermission perm) throws FxNotFoundException, FxInvalidParameterException, FxNoAccessException {
         final UserTicket ticket = FxContext.getUserTicket();
         List<String> xpaths = content.getAllPropertyXPaths();
         FxPropertyData pdata;
         for (String xpath : xpaths) {
             pdata = content.getPropertyData(xpath);
             checkPropertyPermission(pdata.getValue(), xpath, ticket, content.getLifeCycleInfo().getCreatorId(), ((FxPropertyAssignment) pdata.getAssignment()).getACL().getId(), perm);
         }
     }
 
     /**
      * Check propery permissions for delta updates
      *
      * @param creatorId content instance creator
      * @param delta     delta changes
      * @param perm      permisson to check
      * @throws FxNoAccessException if not accessible for the calling user
      */
     public static void checkPropertyPermissions(long creatorId, FxDelta delta, ACLPermission perm) throws FxNoAccessException {
         if (!delta.changes())
             return;
         final UserTicket ticket = FxContext.getUserTicket();
         for (FxDelta.FxDeltaChange add : delta.getAdds())
             checkPropertyPermission(((FxPropertyData) add.getNewData()).getValue(), add.getXPath(), ticket, creatorId,
                     ((FxPropertyAssignment) add.getNewData().getAssignment()).getACL().getId(), perm);
         for (FxDelta.FxDeltaChange rem : delta.getRemoves())
             checkPropertyPermission(((FxPropertyData) rem.getOriginalData()).getValue(), rem.getXPath(), ticket, creatorId,
                     ((FxPropertyAssignment) rem.getOriginalData().getAssignment()).getACL().getId(), perm);
         for (FxDelta.FxDeltaChange upd : delta.getUpdates())
             if (!upd.isPositionChangeOnly())
                 checkPropertyPermission(((FxPropertyData) upd.getNewData()).getValue(), upd.getXPath(), ticket, creatorId,
                         ((FxPropertyAssignment) upd.getOriginalData().getAssignment()).getACL().getId(), perm);
     }
 
     /**
      * Check a single property permission
      *
      * @param value     the affected value
      * @param xpath     xpath of the property
      * @param ticket    calling users ticket
      * @param creatorId creator of the content instance
      * @param aclId     acl id to check
      * @param perm      permission to check
      * @throws FxNoAccessException if not accessible for the calling user
      */
     protected static void checkPropertyPermission(FxValue value, String xpath, UserTicket ticket, long creatorId, long aclId, ACLPermission perm) throws FxNoAccessException {
         if (value instanceof FxNoAccess || value.isEmpty() || value.isReadOnly())
             return; //dont touch NoAccess or readonly values
         if (perm == ACLPermission.EDIT && !ticket.mayEditACL(aclId, creatorId))
             throw new FxNoAccessException("ex.acl.noAccess.property.edit", xpath);
         else if (perm == ACLPermission.CREATE && !ticket.mayCreateACL(aclId, creatorId))
             throw new FxNoAccessException("ex.acl.noAccess.property.create", xpath);
         else if (perm == ACLPermission.READ && !ticket.mayReadACL(aclId, creatorId))
             throw new FxNoAccessException("ex.acl.noAccess.property.read", xpath);
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
      * Gets the permission set union of all given ACLs.
      *
      * @param aclIds    instance ACLs
      * @param type      used type
      * @param stepACL   step ACL
      * @param createdBy owner
      * @param mandator  mandator
      * @return array of permissions in the order edit, relate, delete, export and create
      * @throws com.flexive.shared.exceptions.FxNoAccessException
      *          if no read access if permitted
      * @since 3.1
      */
     public static PermissionSet getPermissionUnion(Collection<Long> aclIds, FxType type, long stepACL, long createdBy, long mandator) {
         PermissionSet result = new PermissionSet(false, false, false, false, false);
         for (long aclId : aclIds) {
             try {
                 result = result.union(getPermissions(aclId, type, stepACL, createdBy, mandator));
             } catch (FxNoAccessException e) {
                 // no read access, ignore it since we're starting with an empty permission set anyway
             }
         }
         return result;
     }
 
     /**
      * Gets the permission set intersection of all given ACLs.
      *
      * @param aclIds    instance ACLs
      * @param type      used type
      * @param stepACL   step ACL
      * @param createdBy owner
      * @param mandator  mandator
      * @return array of permissions in the order edit, relate, delete, export and create
      * @throws com.flexive.shared.exceptions.FxNoAccessException
      *          if no read access if permitted
      * @since 3.1
      */
     public static PermissionSet getPermissionIntersection(Collection<Long> aclIds, FxType type, long stepACL, long createdBy, long mandator) throws FxNoAccessException {
         PermissionSet result = new PermissionSet(true, true, true, true, true);
         for (long aclId : aclIds) {
             result = result.intersect(getPermissions(aclId, type, stepACL, createdBy, mandator));
         }
         return result;
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
         final UserTicket ticket = FxContext.getUserTicket();
         final List<Long> acls = Arrays.asList(acl);
         final boolean _system = FxContext.get().getRunAsSystem() || ticket.isGlobalSupervisor();
         //throw exception if read is forbidden
         checkPermission(ticket, createdBy, ACLPermission.READ, type, stepACL, acls, true);
         // check for supervisor permissions
         if (_system || ticket.isMandatorSupervisor() && mandator == ticket.getMandatorId() ||
                 !type.isUsePermissions() /*|| ticket.isInGroup((int) UserGroup.GROUP_OWNER) && createdBy == ticket.getUserId()*/) {
             return new PermissionSet(true, true, true, true, true);
         }
         // get permission matrix from ACL assignments
         return new PermissionSet(
                 checkPermission(ticket, createdBy, ACLPermission.EDIT, type, stepACL, acls, false),
                 checkPermission(ticket, createdBy, ACLPermission.RELATE, type, stepACL, acls, false),
                 checkPermission(ticket, createdBy, ACLPermission.DELETE, type, stepACL, acls, false),
                 checkPermission(ticket, createdBy, ACLPermission.EXPORT, type, stepACL, acls, false),
                 checkPermission(ticket, createdBy, ACLPermission.CREATE, type, stepACL, acls, false)
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
                 throw new FxNoAccessException("ex.role.notInRole", role.getName());
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
 
     /**
      * Checks if a UserTicket contains at least one of a given List of (content) ACLs
      *
      * @param ticket        the UserTicket
      * @param ACLIds a List of (content) ACL ids
      * @return returns true if a match can be found
      */
     public static boolean currentUserInACLList(UserTicket ticket, List<Long> ACLIds) {
         for (Long id : ACLIds) {
             for (ACLAssignment a : ticket.getACLAssignments()) {
                 if (a.getAclId() == id)
                     return true;
             }
         }
         return false;
     }
 }
