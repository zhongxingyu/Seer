 package org.wyona.security.core.api;
 
 /**
  * Manages groups.
  */
 public interface GroupManager {
     /**
      * Get all groups in no particular order.
      * 
     * XXX: this does not scale UI-wise for many users: cf. {@link UserManager#getUsers()} for rationale.
     *  
      * @return array of groups, empty array if there are no groups.
      * @throws AccessManagementException
      */
     Group[] getGroups() throws AccessManagementException;
 
     /**
      * Gets the group with the given id.
      * 
      * @param id
      * @return group or null if no group with the given id exists 
      * @throws AccessManagementException
      */
     Group getGroup(String id) throws AccessManagementException;
 
     /**
      * Creates a new group.
     * The group is saved automatically.
      * 
      * @param id
      * @param name
      * @return the new group.
      * @throws AccessManagementException
      *             if a group with the given id exists already or something else
      *             goes wrong.
      */
     Group createGroup(String id, String name) throws AccessManagementException;
 
     /**
      * Permanently deletes the group and deletes all memberships of the group.
      * 
      * @param id
      * @throws AccessManagementException
      *             if no group with the given id exists or something else goes
      *             wrong.
      */
     void removeGroup(String id) throws AccessManagementException;
 
     /**
      * Indicates whether a group with the given id exists.
      * 
      * @param id
      * @return true if a group with the given id exist.
      * @throws AccessManagementException
      */
     boolean existsGroup(String id) throws AccessManagementException;
 }
