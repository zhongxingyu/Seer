 /*
  * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
  * @team sonicteam
  * @version $Id$
  */
 
 package org.gridlab.gridsphere.services.security.acl.impl2;
 
 import org.gridlab.gridsphere.services.security.acl.AccessControlService;
 import org.gridlab.gridsphere.portlet.User;
 import org.gridlab.gridsphere.portlet.PortletGroup;
 import org.gridlab.gridsphere.portlet.PortletRole;
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.portlet.service.PortletServiceUnavailableException;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceProvider;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceConfig;
 import org.gridlab.gridsphere.core.persistence.*;
 import org.gridlab.gridsphere.core.persistence.castor.Result;
 import org.gridlab.gridsphere.core.persistence.castor.Query;
 import org.gridlab.gridsphere.core.persistence.castor.Transaction;
 import org.gridlab.gridsphere.core.persistence.castor.PersistenceManager;
 
 import java.util.List;
 import java.util.Vector;
 
 public class AccessControlServiceImpl  implements AccessControlService, PortletServiceProvider {
 
     protected transient static PortletLog cat = SportletLog.getInstance(AccessControlServiceImpl.class);
 
     PersistenceInterface pm = null;
 
     public AccessControlServiceImpl() throws PersistenceException {
         super();
 
         try {
             pm = new PersistenceManager("/Users/wehrens/gridsphere/webapps/WEB-INF/conf/database.xml","portal");
          } catch (ConfigurationException e) {
             cat.error("Configuration Error "+e);
             throw new PersistenceException();
         }
         cat.info("AccessControlServiceImpl constructor done ");
     }
 
     public void init(PortletServiceConfig config) throws PortletServiceUnavailableException {
     }
 
     public void destroy() {
     }
 
     /**
      * Returns list of super users
      */
     public List getSuperUsers() throws  PortletServiceException {
         return null;
     }
 
     /**
      * Checks if a user has a particular role in a group
      *
      * @param user the User object
      * @param group the PortletGroup
      * @param role the PortletRole
      * @return true if the user has the specified role in the specified group, false otherwise
      */
     public boolean hasRoleInGroup(User user, PortletGroup group, PortletRole role) throws PortletServiceException{
 
         String oql = "select r from org.gridlab.gridpshere.services.security.acl.impl2.UserRole r where r.userid="+
                 user.getID()+" and r.groupid="+group.getID()+" and r.roleid="+role.getID();
 
         return false;
     }
 
     /**
      * Return the list of users associated with a particular group and possessing the specified role
      *
      * @param role the PortletRole
      * @param group the PortletGroup
      * @return the list of users associated with a particular group and possessing the specified role
      */
     public List getUsersInGroup(PortletRole role, PortletGroup group) throws PortletServiceException{
 
         String command =
             "select u from org.gridlab.gridsphere.services.security.acl.impl2.UserACL u where u.RoleID=\""+role.getID()+
             " and u.GroupID="+group.getID();
 
 
 
         return null;
     }
 
     /**
      * Return a list of PortletRole objects for a user in a group
      *
      * @param user the User object
      * @param group the PortletGroup
      * @return a list of PortletRole objects
      */
     public List getRolesInGroup(User user, PortletGroup group) throws PortletServiceException {
         return null;
     }
 
     /**
      * Return a list of PortletGroup objects
      *
      * @return a list of PortletGroup objects
      */
     public List getAllGroups() throws PortletServiceException {
 
         String command =
             "select g from org.gridlab.gridsphere.services.security.acl.impl2.Groups g";
         Vector v = new Vector();
         try {
             pm.begin();
             Query query = pm.getQuery();
             Result res = query.execute(command);
             while (res.hasMore()) {
                 v.add(res.next());
             }
             pm.commit();
         } catch (PersistenceException e) {
             cat.error("Transaction error "+e);
             throw new PortletServiceException();
         }
         return v;
     }
 
     /**
      * Returns a list of PortletGroup objects associated with a user
      *
      * @param user the User object
      * @return the list of PortletGroup objects
      */
     public List getGroups(User user) throws PortletServiceException{
 
         String command =
             "select u from org.gridlab.gridsphere.services.security.acl.impl2.UserACL u where u.ID='"+user.getID()+"'";
 
 
         return null;
     }
 
     /**
      * Check to see if a user is in a group
      *
      * @param user the User object
      * @param group the PortletGroup
      * @return true if the user in the PortletGroup, false otherwise
      */
     public boolean isUserInGroup(User user, PortletGroup group) throws PortletServiceException{
 
         String command =
             "select u from org.gridlab.gridsphere.services.security.acl.impl2.UserACL u where u.UserID=\""+user.getID()+
             "\" and u.GroupID="+group.getID();
 
         cat.info("string:"+command);
         try {
             pm.begin();
             Query query = pm.getQuery();
             Result res = query.execute(command);
             if (res.hasMore()) {
                 pm.close();
                 return true;
             } else {
                 pm.close();
                 return false;
             }
         } catch (PersistenceException e) {
             pm.close();
             throw new PortletServiceException(e.toString());
         }
 
     }
 
     public List getAllRoles() {
         return null;
     }
 }
 
