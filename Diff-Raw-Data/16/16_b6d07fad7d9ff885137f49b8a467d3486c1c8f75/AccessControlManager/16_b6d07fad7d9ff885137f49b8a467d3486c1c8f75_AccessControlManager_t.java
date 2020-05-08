 /*
  * Licensed to the Sakai Foundation (SF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The SF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 package org.sakaiproject.nakamura.api.lite.accesscontrol;
 
 import org.sakaiproject.nakamura.api.lite.StorageClientException;
 import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
 
 import java.util.Map;
 
 /**
  * Sparse Access Control manager that allows management and enforcement of ACLs
  */
 public interface AccessControlManager {
 
     /**
      * Get an ACL at an object of a defined type. Do not look at parent objects
      * 
      * @param objectType
      *            the object type @see {@link Security}
      * @param objectPath
      *            the path to the object
      * @return the ACL in raw stored format.
      * @throws StorageClientException
      * @throws AccessDeniedException
      */
     Map<String, Object> getAcl(String objectType, String objectPath) throws StorageClientException,
             AccessDeniedException;
 
     /**
      * Set and ACL using ACL modifications operating on the existing ACL at teh
      * objectPath.
      * 
      * @param objectType
      *            the object type @see {@link Security}
      * @param objectPath
      *            the path to the object
      * @param aclModifications
      *            an array of AclModifications applied in order.
      * @throws StorageClientException
      * @throws AccessDeniedException
      */
     void setAcl(String objectType, String objectPath, AclModification[] aclModifications)
             throws StorageClientException, AccessDeniedException;
 
     /**
      * Check the current user has the permission on the object. If the object
      * exists within a structure the permission may be granted or denied by
      * parent objects.
      * 
      * @param objectType
      *            the object type @see {@link Security}
      * @param objectPath
      *            the path to the object
      * @param permission
      *            the permission (may be aggregated) to check.
      * @throws AccessDeniedException
      *             will be thrown if the user doesnt have the permission.
      *             Exception contains details.
      * @throws StorageClientException
      */
     void check(String objectType, String objectPath, Permission permission)
             throws AccessDeniedException, StorageClientException;
 
     /**
      * @return the current user ID associated with this AccessControlManager
      */
     String getCurrentUserId();
 
     /**
      * Does the authorizable have the permission
      * 
      * @param authorizable
      *            the authorizable to check
      * @param objectType
      *            the object type @see {@link Security}
      * @param objectPath
      *            the path to the object
      * @param permission
      *            the permission set to check.
      * @return true if the user has permissions
      */
     boolean can(Authorizable authorizable, String objectType, String objectPath,
             Permission permission);
 
     /**
      * Get the aggregate permissions for the object for the current user.
      * 
      * @param objectType
      *            the object type @see {@link Security}
      * @param objectPath
      *            the path to the object
      * @return an array of permissions granted at for the current user on the
      *         object.
      * @throws StorageClientException
      */
    Permission[] getPermissions(String objectType, String objectPath) throws StorageClientException;
 
     /**
      * Finds all principals with the matching permissions explicitly grantent by the supplied path or parent paths.
       * @param objectType
      *            the object type @see {@link Security}
      * @param objectPath
      *            the path to the object
      * @param permission the permission bitmap to search for
      * @param granted true if the permission search is for granted permissions, false if for denied.
      * @return an array of principals that have the permission granted.
      * @throws StorageClientException 
      */
     String[] findPrincipals(String objectType, String objectPath, int permission, boolean granted) throws StorageClientException;
 
 }
