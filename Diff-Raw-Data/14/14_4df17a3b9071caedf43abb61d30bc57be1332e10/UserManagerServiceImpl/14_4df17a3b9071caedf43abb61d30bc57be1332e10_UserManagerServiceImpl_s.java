 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.services.user.impl;
 
 import org.gridlab.gridsphere.core.mail.MailMessage;
 import org.gridlab.gridsphere.core.mail.MailUtils;
 import org.gridlab.gridsphere.portlet.PortletGroup;
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.PortletRole;
 import org.gridlab.gridsphere.portlet.User;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portlet.impl.SportletUser;
 import org.gridlab.gridsphere.portlet.impl.SportletUserImpl;
 import org.gridlab.gridsphere.portlet.impl.SportletRole;
 import org.gridlab.gridsphere.portlet.service.PortletServiceNotFoundException;
 import org.gridlab.gridsphere.portlet.service.PortletServiceUnavailableException;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceConfig;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceFactory;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceProvider;
 import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
 import org.gridlab.gridsphere.services.security.acl.AccessControlManagerService;
 import org.gridlab.gridsphere.services.security.acl.AccessControlService;
 import org.gridlab.gridsphere.services.user.AccountRequest;
 import org.gridlab.gridsphere.services.user.UserManagerService;
 import org.gridlab.gridsphere.services.user.PermissionDeniedException;
 
 import javax.mail.MessagingException;
 import java.util.*;
 
 /**
  * The UserManagerService manages users and account requests. Thru the UserManagerService
  * new portal accounts can be requested and granted or denied. Role objects can be retrieved
  * and removed.
  */
 public class UserManagerServiceImpl implements PortletServiceProvider, UserManagerService {
 
     private static PortletLog log = SportletLog.getInstance(UserManagerServiceImpl.class);
     private static PortletServiceFactory factory = SportletServiceFactory.getInstance();
     private static AccessControlService aclService = null;
     private static AccessControlManagerService aclManagerService = null;
 
     // This is the in-memory representation of user and account request data
     protected static UserData userData = null;
 
     // This is the list of newly created AccountRequestImpl objects -- don't need to make persistent
     private static List newAccountRequests = new Vector();
 
     // This is the list of users that are logged in
     private static List activeUsers;
 
     // This is to make our life easier
     private static Map usersHash = new Hashtable();
 
     // This is the list required by PersistenceManager
     private static List users = new Vector();
 
     private static int uniqueID = 0;
 
     /**
      * Initializes the portlet service.
      * The init method is invoked by the portlet container immediately after a portlet service has
      * been instantiated and before it is passed to the requestor.
      *
      * @param config the service configuration
      * @throws PortletServiceUnavailableException if an error occurs during initialization
      */
     public void init(PortletServiceConfig config) throws PortletServiceUnavailableException {
         try {
             // This is the in-memory representation of user data
             ///userData = UserDataManager.getUserData();
             aclService = (AccessControlService) factory.createPortletService(AccessControlService.class, config.getServletConfig(), true);
             aclManagerService = (AccessControlManagerService) factory.createPortletService(AccessControlManagerService.class, config.getServletConfig(), true);
             log.info("in init()");
         } catch (PortletServiceNotFoundException e) {
             throw new PortletServiceUnavailableException("Unable to find portlet services: AccessControlService and AccessControlManagerService");
         }
 
         // Load structures
         load();
     }
 
     /**
      * The destroy method is invoked by the portlet container to destroy a portlet service.
      * This method must free all resources allocated to the portlet service.
      */
     public void destroy() {
         log.info("in destroy()");
 
         // save structures to DB
         save();
     }
 
     protected void load() {
 
     }
 
     protected void save() {
 
     }
 
     /**
      * Create a new account request. A unique ID is assigned to this request, that can't be modified by the client.
      * When an account request is submitted, the ID is checked
      */
     public AccountRequest createAccountRequest() {
         AccountRequestImpl newacct = new AccountRequestImpl();
         newAccountRequests.add(newacct);
         newacct.setID(uniqueID);
         uniqueID++;
         return newacct;
     }
 
     /**
      * Submit the account request to the queue for administrative approval
      */
     public void submitAccountRequest(AccountRequest request) throws PortletServiceException {
 
         int i;
 
         // add request to list
         Iterator it = newAccountRequests.iterator();
         while (it.hasNext()) {
             AccountRequestImpl a = (AccountRequestImpl) it.next();
             if (a.getID() == request.getID()) {
                 userData.addAccountRequest(a);
                 newAccountRequests.remove(a);
             }
         }
 
         // mail the super user
         String newAcctSubject = "GridSphere: New Role Request";
         String newAcctBody = "New user request from: " + request.toString();
         String portalRoot = "gridsphere-admin@gridlab.org";
 
         String newGroupSubject = "GridSphere: New Group Addition Request";
         String newGroupBody = "New user request to join group from: " + request.toString();
 
         MailMessage superMsg = new MailMessage(newAcctSubject, newAcctBody, request.getEmailAddress(), "");
         MailMessage adminMsg = new MailMessage(newGroupSubject, newGroupBody, request.getEmailAddress(), "");
 
         try {
             // notify super users of new account
             Iterator rootIt = aclService.getSuperUsers().iterator();
             while (rootIt.hasNext()) {
                 String sender = (String) rootIt.next();
                 superMsg.setSender(sender);
                 MailUtils.sendMail(superMsg, "localhost");
             }
 
             // notify admins of groups user want to joins and super about account request
             List groups = request.getDesiredGroups();
             if (groups != null) {
 
                 for (i = 0; i < groups.size(); i++) {
                     // get admins
                     PortletGroup group = (PortletGroup) groups.get(i);
                     Iterator adminIt = aclService.getUsersInGroup(SportletRole.getAdminRole(), group).iterator();
 
                     while (adminIt.hasNext()) {
                         String sender = (String) adminIt.next();
                         adminMsg.setSender(sender);
                         MailUtils.sendMail(adminMsg, "localhost");
                     }
                 }
             }
         } catch (MessagingException e) {
             log.error("Failed to mail message to: " + request.getEmailAddress(), e);
         }
 
     }
 
     /**
      * Administrators can retrieve all pending account request
      */
     public List getAccountRequests() {
         return userData.getAccountRequests();
     }
 
     /**
      * Approve a new or modified account request
      */
     public void approveAccountRequest(User approver, AccountRequest request, MailMessage mailMessage)
             throws PermissionDeniedException {
 
         // See if group exists or not
         // add request to list
         int userid = request.getID();
         AccountRequestImpl requestImpl = null;
         Iterator it = userData.getAccountRequests().iterator();
         while (it.hasNext()) {
             requestImpl = (AccountRequestImpl) it.next();
             if (requestImpl.getID() == userid) {
                 SportletUser user = makeNewUser(requestImpl);
                 usersHash.put(user.getID(), user);
                 userData.removeAccountRequest(requestImpl);
                 break;
             }
         }
 
         // Mail the user
         try {
             if (mailMessage != null)
                 MailUtils.sendMail(mailMessage, "localhost");
         } catch (MessagingException e) {
             log.error("Unable to send mail: ", e);
         }
     }
 
     /**
      * Deny a new or modified account request
      */
     public void denyAccountRequest(User denier, AccountRequest request, MailMessage mailMessage)
             throws PermissionDeniedException {
 
         int userid = request.getID();
         AccountRequestImpl requestImpl = null;
         Iterator it = userData.getAccountRequests().iterator();
         while (it.hasNext()) {
             requestImpl = (AccountRequestImpl) it.next();
             if (requestImpl.getID() == userid) {
                 userData.removeAccountRequest(requestImpl);
                 break;
             }
         }
 
         // Mail the user
         try {
             if (mailMessage != null)
                 MailUtils.sendMail(mailMessage, "localhost");
         } catch (MessagingException e) {
             log.error("Unable to send mail: ", e);
         }
 
     }
 
     /**
      * Approve a new or modified account group request
      */
     public void approveGroupRequest(User approver, AccountRequest request, PortletGroup group, MailMessage mailMessage)
             throws PermissionDeniedException {
         if (!userData.getAccountRequests().contains(request)) return;
 
         // first see if user exists or if user is still in account request stage
         int userid = request.getID();
 
         // See if group exists or not
         // add request to list
         Iterator it = newAccountRequests.iterator();
         while (it.hasNext()) {
             AccountRequestImpl a = (AccountRequestImpl) it.next();
             if (a.getID() == userid) {
                a.addApprovedGroup(group);
             }
         }
 
         // Mail the user
         try {
             if (mailMessage != null)
                 MailUtils.sendMail(mailMessage, "localhost");
         } catch (MessagingException e) {
             log.error("Unable to send mail: ", e);
         }
     }
 
     /**
      * Deny a new or modified account group request
      */
     public void denyGroupRequest(User denier, AccountRequest request, PortletGroup group, MailMessage mailMessage)
             throws PermissionDeniedException {
 
         if (!userData.getAccountRequests().contains(request)) return;
 
         // first see if user exists or if user is still in account request stage
         int userid = request.getID();
 
         // See if group exists or not
         // add request to list
         Iterator it = newAccountRequests.iterator();
         while (it.hasNext()) {
             AccountRequestImpl a = (AccountRequestImpl)it.next();
             if (a.getID() == userid) {
                a.addApprovedGroup(group);
             }
         }
 
         // Mail the user
         try {
             if (mailMessage != null)
                 MailUtils.sendMail(mailMessage, "localhost");
         } catch (MessagingException e) {
             log.error("Unable to send mail: ", e);
         }
     }
 
     /**
      * Modify an existing user account. Changes must be approved
      */
     public AccountRequest changeAccountRequest(User user) {
         // get user from DB
         // create an AccountRequestImpl from User
         // when modifications are made to account request they go thru submission process again
         return null;
     }
 
     /**
      * loginUser retrieves a new user
      */
     public User loginUser(String userName) {
         return null;
     }
 
     /**
      * logoffUser release user information and serializes to DB
      */
     public void logoffUser(User user) {
 
     }
 
     /**
      * Save user to DB
      */
     public void saveUser(String userName) {
 
     }
 
     /**
      * Remove a user permanently!
      */
     public void removeUser(User approver, String userName) throws PermissionDeniedException {
 
     }
 
     /**
      * Checks to see if account exists for a user
      */
     public boolean existsUser(String userName) {
         return false;
     }
 
     /**
      * Return a list of all portal users
      */
     public List getAllUsers() {
         return null;
     }
 
     /**
      * Return a list of users currently logged in
      */
     public List getActiveUsers() {
         return null;
     }
 
     /**
      * Creates a new SportletUser from an AccountRequestImpl
      */
     protected SportletUser makeNewUser(AccountRequestImpl requestImpl) {
         SportletUserImpl newuser = new SportletUserImpl();
         newuser.setEmailAddress(requestImpl.getEmailAddress());
         newuser.setFamilyName(requestImpl.getFamilyName());
         newuser.setFullName(requestImpl.getFullName());
         newuser.setGivenName(requestImpl.getGivenName());
         newuser.setID("" + requestImpl.getID());
         newuser.setUserID(requestImpl.getUserID());
         return newuser;
     }
 
     protected SportletUser modifyExistingUser(AccountRequestImpl requestImpl) {
         //SportletUserImpl user = getUserFromDatabase
         // don't fuck with ID of AccountRequestImpl
         // Conserve user attributes
         // user.setEmailAddress(requestImpl.getEmailAddress())
         // change user in database
         // return user
         return null;
     }
 
 }
