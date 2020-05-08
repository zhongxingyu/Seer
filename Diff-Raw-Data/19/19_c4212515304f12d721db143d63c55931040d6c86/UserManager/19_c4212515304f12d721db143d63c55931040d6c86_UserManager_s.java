 package org.wyona.security.core.api;
 
 /**
  * Manages users.
  */
 public interface UserManager {
 
     /**
      * Gets all users in no particular order.
      * 
      * @return array of users, empty array if no users exist.
      * @throws AccessManagementException
      */
     User[] getUsers() throws AccessManagementException;
 
     /**
      * Gets all users in no particular order, whereas provides a parameter to tell the implementation to refresh possibly cached entries
      * 
      * @return array of users, empty array if no users exist.
      * @throws AccessManagementException
      */
     User[] getUsers(boolean refresh) throws AccessManagementException;
 
     /**
      * Gets the user with the given id.
      * 
      * @param id
      * @return user or null if no user with the given id exists 
      * @throws AccessManagementException
      */
     User getUser(String id) throws AccessManagementException;
 
     /**
      * Gets the user with the given id, whereas provides a parameter to tell the implementation to refresh a possible cached entry
      * 
      * @param id
      * @param refresh
      * @return user or null if no user with the given id exists 
      * @throws AccessManagementException
      */
     User getUser(String id, boolean refresh) throws AccessManagementException;
 
     /**
      * Creates a new user. The new user will be saved automatically.
      * 
      * @param id
      * @param name
      * @param email
      * @param password
      *            password in cleartext
      * @return new user
      * @throws AccessManagementException
      *             if a user with the given id already exists or if something
      *             else goes wrong.
      */
     User createUser(String id, String name, String email, String password)
             throws AccessManagementException;
 
     /**
      * Permanently deletes the user and deletes all group memberships of the
      * user.
      * 
      * @param id
      * @throws AccessManagementException
      *             if no user with the given id exists or if something else goes
      *             wrong.
      */
     void removeUser(String id) throws AccessManagementException;
 
     /**
      * Indicates whether a user with the given id exists.
      * 
      * @param id
      * @return true if a user with the given id exists.
      * @throws AccessManagementException
      */
     boolean existsUser(String id) throws AccessManagementException;
 }
