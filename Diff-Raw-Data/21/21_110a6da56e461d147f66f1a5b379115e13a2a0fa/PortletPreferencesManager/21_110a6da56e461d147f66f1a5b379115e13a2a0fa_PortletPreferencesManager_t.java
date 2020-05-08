 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlet.jsrimpl;
 
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerFactory;
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerRdbms;
 import org.gridlab.gridsphere.portlet.GuestUser;
 import org.gridlab.gridsphere.portlet.User;
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portletcontainer.jsrimpl.JSRApplicationPortletImpl;
 
 import javax.portlet.PortletPreferences;
 
 /**
  * The <code>PortletPreferencesManager</code> provides a a singleton implementation of the <code>PortletDataManager</code>
  * used for loading and storing <code>PortletData</code>.
  */
 public class PortletPreferencesManager {
 
     private static PortletLog log = SportletLog.getInstance(PortletPreferencesManager.class);
     private static PersistenceManagerRdbms pm = PersistenceManagerFactory.createGridSphereRdbms();
     private static PortletPreferencesManager instance = new PortletPreferencesManager();
 
     /**
      * Default instantiation is disallowed
      */
     private PortletPreferencesManager() {
     }
 
     /**
      * Returns an instance of a <code>PortletDataManager</code>
      *
      * @return an instance of a <code>PortletDataManager</code>
      */
     public static PortletPreferencesManager getInstance() {
         return instance;
     }
 
     /**
      * Returns the users portlet data for the specified portlet
      *
      * @param appPortlet the JSR application portlet
      * @param user               the <code>User</code>
      * @return the PortletPreferences for this portlet or null if none exists.
      */
     public javax.portlet.PortletPreferences getPortletPreferences(JSRApplicationPortletImpl appPortlet, User user, ClassLoader loader) {
 
         if (user instanceof GuestUser) return null;
 
         String portletID = appPortlet.getApplicationPortletID();
         String command =
                 "select u from " + PortletPreferencesImpl.class.getName() + " u where u.userId='" + user.getID() + "' and u.portletId='" + portletID + "'";
 
         // get persistence prefs if it exists
         PortletPreferencesImpl prefs = null;
         try {
             prefs = (PortletPreferencesImpl) pm.restore(command);
             if (prefs == null) {
                 // we have no prefs in the xml so create one in the db...
                 log.debug("No prefs exist-- storing prefs for user: " + user.getID() + " portlet: " + portletID);
                 org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.PortletPreferences prefDesc = appPortlet.getPortletPreferences();
                 prefs = new PortletPreferencesImpl(prefDesc, loader);
                prefs.setPersistenceManager(pm);
                 prefs.setPortletId(portletID);
                 prefs.setUserId(user.getID());
                 prefs.store();
             } else {
                prefs.setPersistenceManager(pm);
                 log.debug("Retrieved prefs for user: " + user.getID() + " portlet: " + portletID);
             }
         } catch (Exception e) {
             log.error("Error attempting to restore persistent preferences: ", e);
         }
         return prefs;
     }
 
 }
