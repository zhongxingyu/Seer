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
 
 import javax.portlet.PortletPreferences;
 
 /**
  * The <code>PortletPreferencesManager</code> provides a a singleton implementation of the <code>PortletDataManager</code>
  * used for loading and storing <code>PortletData</code>.
  */
 public class PortletPreferencesManager {
 
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
      * @param defaultPreferences a default portlet preferences obtained from the portlet descriptor
      * @param user               the <code>User</code>
      * @param portletID          the concrete portlet id
      * @return the PortletPreferences for this portlet or null if none exists.
      */
     public PortletPreferences getPortletPreferences(PortletPreferences defaultPreferences, User user, String portletID) {
 
         if (user instanceof GuestUser) return null;
 
         String command =
                "select u from " + PersistencePreference.class.getName() + " u where u.userId='" + user.getID() + "' and u.portletId='" + portletID + "'";
 
         // get sportlet data if it exists
         PersistencePreference prefs = null;
         try {
             prefs = (PersistencePreference) pm.restore(command);
 
             PortletPreferencesImpl portletprefdefault = null;
             if (prefs == null) {
                 // try get one from the xml
 
                 // your part jason, to get from xml here
 
                 // we have no prefs in the xml so create one in the db...
                 if (portletprefdefault == null) {
                     prefs = new PersistencePreference(pm);
                     prefs.setPortletId(portletID);
                     prefs.setUserId(user.getID());
                 } else {
                     // we got one from xml, save it to db for the user
                     prefs = new PersistencePreference(pm, portletprefdefault);
                 }
 
                 prefs.store();
             }
 
         } catch (Exception e) {
             return defaultPreferences;
         }
         return prefs;
     }
 
     public void setPortletPreferences(PortletPreferences prefs) {
 
     }
 
 }
