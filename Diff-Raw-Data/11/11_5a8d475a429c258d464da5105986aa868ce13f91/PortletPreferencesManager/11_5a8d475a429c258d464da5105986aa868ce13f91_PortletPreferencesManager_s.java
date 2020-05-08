 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlet.jsrimpl;
 
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerFactory;
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerRdbms;
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.User;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portletcontainer.jsrimpl.JSRApplicationPortletImpl;
 
 import javax.portlet.PreferencesValidator;
 
 /**
  * The <code>PortletPreferencesManager</code> provides a a singleton implementation of the <code>PortletDataManager</code>
  * used for loading and storing <code>PortletData</code>.
  */
 public class PortletPreferencesManager {
 
     private static PortletLog log = SportletLog.getInstance(PortletPreferencesManager.class);
     private static PersistenceManagerRdbms pm = null;
     private static PortletPreferencesManager instance = null;
 
     /**
      * Default instantiation is disallowed
      */
     private PortletPreferencesManager() {
         pm = PersistenceManagerFactory.createGridSphereRdbms();    
     }
 
     /**
      * Returns an instance of a <code>PortletDataManager</code>
      *
      * @return an instance of a <code>PortletDataManager</code>
      */
     public static synchronized PortletPreferencesManager getInstance() {
         if (instance == null) {
             instance = new PortletPreferencesManager();
         }
         return instance;
     }
 
     /**
      * Returns the users portlet data for the specified portlet
      *
      * @param appPortlet the JSR application portlet
      * @param user       the <code>User</code>
      * @return the PortletPreferences for this portlet or null if none exists.
      */
     public javax.portlet.PortletPreferences getPortletPreferences(JSRApplicationPortletImpl appPortlet, User user, ClassLoader loader, boolean isRender) {
         PortletPreferencesImpl prefs = null;
         String portletID = appPortlet.getApplicationPortletID();
 
        String command =
                "select u from " + PortletPreferencesImpl.class.getName() + " u where u.userId='" + user.getID() + "' and u.portletId='" + portletID + "'";

         // get persistence prefs if it exists
         org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.PortletPreferences prefDesc = appPortlet.getPortletPreferences();
 
         try {
            prefs = (PortletPreferencesImpl) pm.restore(command);
             if (prefs == null) {
                 // we have no prefs in the db so create one from the xml...
                 log.debug("No prefs exist-- storing prefs for user: " + user.getID() + " portlet: " + portletID);
                 prefs = new PortletPreferencesImpl();
                 prefs.setPortletId(portletID);
                 prefs.setUserId(user.getID());
             } else {
                 log.debug("Retrieved prefs for user: " + user.getID() + " portlet: " + portletID);
             }
             prefs.setPersistenceManager(pm);
             prefs.init(prefDesc);
             if (prefDesc != null) {
                 org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.PreferencesValidator v = prefDesc.getPreferencesValidator();
                 if (v != null) {
                     String validatorClass = v.getContent();
                     if (validatorClass != null) {
                         try {
                             PreferencesValidator validator = (PreferencesValidator) Class.forName(validatorClass, true, loader).newInstance();
                             prefs.setValidator(validator);
                         } catch (Exception e) {
                             log.error("Unable to create validator: " + validatorClass + "! ",  e);
                         }
                     }
                 }
             }
             prefs.setRender(isRender);
         } catch (Exception e) {
             log.error("Error attempting to restore persistent preferences: ", e);
         }
         return prefs;
     }
 
 }
