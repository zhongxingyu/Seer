 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlet.impl;
 
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceFactory;
 import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
 import org.gridlab.gridsphere.portletcontainer.descriptor.*;
 import org.gridlab.gridsphere.services.security.acl.AccessControlService;
 
 import java.util.*;
 
 /**
  * The SportletSettings class provides the portlet with its dynamic configuration.
  * The configuration holds information about the portlet that is valid per concrete portlet for all users,
  * and is maintained by the administrator. The portlet can therefore only read the dynamic configuration.
  * Only when the portlet is in CONFIGURE mode, it has write access to the dynamic configuration data
  */
 public class SportletSettings implements PortletSettings {
 
     private Hashtable store = new Hashtable();
     private List roleList = new Vector();
     private List groupList = new Vector();
    private List langList = new Vector();
 
     /**
      * SportletSettings constructor
      * Create a PortletSettings object from a PortletApplication deployment descriptor object
      *
      * @param portletApp the PortletApplication deployment descriptor information
      * @param knownGroups a list of known groups obtained from the AccessControlService
      * @param knownRoles a list of known roles obtained from the AccessControlService
      */
     public SportletSettings(ConcretePortletApplication portletApp, List knownGroups, List knownRoles) {
 
         ConcretePortletInfo portlet = portletApp.getConcretePortletInfo();
 
        langList = portlet.getLanguageList();
 
         // Stick <config-param> in store
         Iterator configParamsIt = portletApp.getConfigParamList().iterator();
         while (configParamsIt.hasNext()) {
             ConfigParam configParam = (ConfigParam)configParamsIt.next();
             store.put(configParam.getParamName(), configParam.getParamValue());
         }
 
         // Get groups list
         List groups = portlet.getGroupList();
 
         // Make sure groups exist
         while (knownGroups.iterator().hasNext()) {
             PortletGroup pg = (PortletGroup)knownGroups.iterator().next();
             while (groups.iterator().hasNext()) {
                 if (pg.getName().equalsIgnoreCase((String)groups.iterator().next())) {
                     groupList.add(pg);
                     break;
                 }
             }
         }
 
         // groupList should at least contain BASE group if empty
         if (groupList.isEmpty()) {
             groupList.add(SportletGroup.getBaseGroup());
         }
 
         // Get roles list
         List roles = portlet.getRoleList();
         // Make sure roles exist
         while (knownRoles.iterator().hasNext()) {
             PortletRole pr = (PortletRole)knownRoles.iterator().next();
             while (roles.iterator().hasNext()) {
                 if (pr.getRoleName().equalsIgnoreCase((String)roles.iterator().next())) {
                     roleList.add(pr);
                     break;
                 }
             }
         }
 
         // roleList should at least contain GUEST role if empty
         if (roleList.isEmpty()) {
             roleList.add(SportletRole.getGuestRole());
         }
     }
 
     /**
      * Returns the value of the attribute with the given name, or null if no such attribute exists.
      *
      * @param name the name of the attribute
      * @return the value of the attribute
      */
     public String getAttribute(String name) {
         return (String) store.get(name);
     }
 
     /**
      * Returns an enumeration of all available attributes names.
      *
      * @return an enumeration of all available attributes names
      */
     public Enumeration getAttributeNames() {
         return store.keys();
     }
 
     /**
      * Returns the title of this window for the provided locale, or null if none exists.
      *
      * @param locale the locale-centric title
      * @param client the given client
      * @return the title of the portlet
      */
     public String getTitle(Locale locale, Client client) {
        Iterator it = langList.iterator();
        while (it.hasNext()) {
            LanguageInfo langInfo = (LanguageInfo)it.next();
            if (langInfo.getLocale().equals(locale.toString())) {
                 return langInfo.getTitle();
             }
         }
         return null;
     }
 
     /**
      * Returns the short title of this window for the provided locale, or null if none exists.
      *
      * @param locale the locale-centric title
      * @param client the given client
      * @return the title of the portlet
      */
     public String getTitleShort(Locale locale, Client client) {
         while (langList.iterator().hasNext()) {
             LanguageInfo langInfo = (LanguageInfo)langList.iterator().next();
             if (langInfo.getLocale().equals(locale)) {
                 return langInfo.getTitleShort();
             }
         }
         return null;
     }
 
     /**
      * Returns the description of this window for the provided locale, or null if none exists.
      *
      * @param locale the locale-centric title
      * @param client the given client
      * @return the title of the portlet
      */
     public String getDescription(Locale locale, Client client) {
         while (langList.iterator().hasNext()) {
             LanguageInfo langInfo = (LanguageInfo)langList.iterator().next();
             if (langInfo.getLocale().equals(locale)) {
                 return langInfo.getDescription();
             }
         }
         return null;
     }
 
     /**
      * Returns the keywords of this window for the provided locale, or null if none exists.
      *
      * @param locale the locale-centric title
      * @param client the given client
      * @return the title of the portlet
      */
     public String getKeywords(Locale locale, Client client) {
         while (langList.iterator().hasNext()) {
             LanguageInfo langInfo = (LanguageInfo)langList.iterator().next();
             if (langInfo.getLocale().equals(locale)) {
                 return langInfo.getKeywords();
             }
         }
         return null;
     }
 
     /**
      * Returns the list of supported groups
      * NOTE: THIS IS NOT PART OF THE WPS PORTLET API 4.1
      *
      * @return the list of supported PortletGroup objects
      */
     public List getSupportedGroups() {
         return groupList;
     }
 
     /**
      * Returns the list of supported roles
      * NOTE: THIS IS NOT PART OF THE WPS PORTLET API 4.1
      *
      * @return the list of supported PortletRole objects
      */
     public List getSupportedRoles() {
         return roleList;
     }
 
     /**
      * Removes the attribute with the given name.
      *
      * @param name the attribute name
      *
      * @throws AccessDeniedException if the caller isn't authorized to access this data object
      */
     public void removeAttribute(String name) throws AccessDeniedException {
         store.remove(name);
     }
 
     /**
      * Sets the attribute with the given name and value.
      *
      * @param name the attribute name
      * @param value the attribute value
      *
      * @throws AccessDeniedException if the caller isn't authorized to access this data object
      */
     public void setAttribute(String name, String value) throws AccessDeniedException {
         store.put(name, value);
     }
 
     /**
      * Stores all attributes.
      *
      * @throws AccessDeniedException if the caller isn't authorized to access this data object
      */
     public void store() throws AccessDeniedException {
         // XXX: FILL ME IN
     }
 
     /**
      * Loads all attributes
      */
     protected void load() {
         // XXX: FILL ME IN
     }
 
 
 }
