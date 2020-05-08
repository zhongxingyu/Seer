 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath" 
  * exception as provided by Sun in the License file that accompanied 
  * this code.
  */
 package org.jdesktop.wonderland.server;
 
 import com.sun.sgs.app.AppContext;
 import com.sun.sgs.app.ClientSession;
 import com.sun.sgs.app.DataManager;
 import com.sun.sgs.app.ManagedObject;
 import com.sun.sgs.app.ManagedReference;
 import com.sun.sgs.app.NameNotBoundException;
 import com.sun.sgs.app.Task;
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 import org.jdesktop.wonderland.common.auth.WonderlandIdentity;
 import org.jdesktop.wonderland.server.auth.ClientIdentityManager;
 import org.jdesktop.wonderland.server.comms.WonderlandClientID;
 
 /**
  * Manages the entire set of users logged into the system.
  *
  * @author paulby
  */
 @ExperimentalAPI
 public class UserManager implements ManagedObject, Serializable {
     
     private HashMap<WonderlandClientID, ManagedReference<UserMO>> clientToUser =
 	    new HashMap<WonderlandClientID, ManagedReference<UserMO>>();
 
     private HashSet<ManagedReference<UserListener>> userListeners = new HashSet();
     
     /**
      * Name used in binding this object in DataManager
      **/
     private static final String BINDING_NAME="USER_MANAGER";
     
     private int userLimit = Integer.MAX_VALUE;
 
     /**
      * Creates a new instance of UserManager
      */
     private UserManager() {
     }
     
     static void initialize() {
         UserManager mgr = new UserManager();
         AppContext.getDataManager().setBinding(BINDING_NAME, mgr);
     }
     
     /**
      * Return singleton user manager
      * @return the user manager
      */
     public static UserManager getUserManager() {
         return (UserManager) AppContext.getDataManager().getBinding(UserManager.BINDING_NAME);                
     }
 
     /**
      * Return the user with the given userID
      *
      * @return reference to the UserGLO
      */
     public UserMO getUser(WonderlandClientID clientID) {
         ManagedReference<UserMO> userRef = 
                 clientToUser.get(clientID);
         if (userRef == null) {
             return null;
         }
         
         return userRef.get();
     }
     
     /**
      * Return the UserMO object associated with the unique userName
      *
      * @return UserMO object for username, or null if no such user
      */
     public static UserMO getUserMO(String username) {
         String userObjName = "user_"+username;
         UserMO user=null;
         
         DataManager dataMgr = AppContext.getDataManager();
         try {
             user = (UserMO) dataMgr.getBinding(userObjName);
         } catch(NameNotBoundException ex) {
             user = null;
         }
         
         return user;
     }
     
     private UserMO createUserMO(String username) {
         WonderlandIdentity identity = AppContext.getManager(ClientIdentityManager.class).getClientID();
         UserMO ret = new UserMO(identity);
         AppContext.getDataManager().setBinding("user_" + identity.getUsername(), ret);
         return ret;
     }
 
     /**
      * Returns true if the user with the specified userName is currently logged in, false otherwise.
      */
     public boolean isLoggedIn(String userName) {
         UserMO user = getUserMO(userName);
         if (user==null) {
             return false;
         }
         
         return user.isLoggedIn();
     }
     
     /**
       * Return a Collection of all users currently logged in
      *
      * @return collection of ManagedReferences to UserGLO's
      */
     public Collection<ManagedReference<UserMO>> getAllUsers() {
         return clientToUser.values();
     }
     
     /**
      * Log the user in from the specificed session. 
      * @param session 
      */
     public void login(WonderlandClientID clientID) {
         DataManager dm = AppContext.getDataManager();
         
         // find the user object from the database, create it if necessary
         UserMO user = getUserMO(clientID.getSession().getName());
         if (user==null) {
             user = createUserMO(clientID.getSession().getName());
         }
         
         // user is now logged in
         user.login(clientID);
         
         // add this session to our map
         clientToUser.put(clientID, user.getReference());
     }
     
     /**
     * Log user out of s÷pecified session
      * @param session
      */
     public void logout(WonderlandClientID clientID) {
         // make sure there is a user
         UserMO user = getUser(clientID);
         assert(user!=null);
         if (user != null) {
             user.logout(clientID);
         }
         
         clientToUser.remove(clientID);
         notifyUserListeners(clientID);
     }
 
     /**
      * Notify listeners that client is logging out
      * @param clientID
      */
     private void notifyUserListeners(WonderlandClientID clientID) {
         for(ManagedReference<UserListener> listener : userListeners) {
             AppContext.getTaskManager().scheduleTask(new UserListenerNotifier(listener, clientID));
         }
     }
 
     /**
      * Return a Collection of all avatars for currently logged in users
      *
      * @return Collection of ManagedReferences to AvatarCellGLO's
      */
 //    public Collection<ManagedReference> getAllAvatars() {
 //        return uidToAvatarRef.values();
 //    }
     
     /**
      *  Return total number of users currently logged in
      * 
      *  @return total number of users currently logged in
      **/
     public int getUserCount() {
         return clientToUser.size();
     }
     
     /**
      *  Get the maximum number of users allowed on the server
      * 
      *  @return the maximum number of concurrent users this world will
      * allow to log in.
      */
     public int getUserLimit() {
         return userLimit;
     }
 
     /**
      *  Set the maximum number of users allowed on the server
      * 
      * @param userLimit the maximum number of concurrent users this world will
      * allow to log in.
      */
     public void setUserLimit(int userLimit) {
         this.userLimit = userLimit;
     }
 
     /**
      * Add a listener to the set of listeners which are notified when a user
      * logs out
      * @param listener
      */
     public void addUserListener(UserListener listener) {
         userListeners.add(AppContext.getDataManager().createReference(listener));
     }
 
     /**
      * Removed a UserListener listener
      * 
      * @param listener
      */
     public void removeUserListener(UserListener listener) {
         userListeners.remove(AppContext.getDataManager().createReference(listener));
     }
 }
