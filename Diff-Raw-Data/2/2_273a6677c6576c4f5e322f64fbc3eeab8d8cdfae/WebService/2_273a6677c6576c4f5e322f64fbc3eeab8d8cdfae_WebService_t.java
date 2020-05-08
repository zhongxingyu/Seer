 package no.feide.moria.authorization;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.logging.Logger;
 
 /**
  * Represents a web service. A web service has a name, id, url and
  * attributes. The atteributes are flattened (for optimization) from a
  * set of profiles, allowed and denied attributes.
  */
 public class WebService {
 
     /** Used for logging. */
     private static Logger log = Logger.getLogger(WebService.class.toString());
     
     /** A unique id */
     private String id;
 
     /** 
      * List of attributes that the web service is associated with.
      * Each profile is connected with a set of attributes and the web
      * serivce are allowed to use all attributes in it's profiles.
      * Overridden by allowedAttributes and deniedAttributes. 
      */
     private HashMap profiles = new HashMap();
 
     /** List of attributes that the web service is allowe to use.
      * Overridden by deniedAttributes. */
     private HashMap allowedAttributes = new HashMap();
     
     /** 
      * List of attributes that the web service is prohibited from
      * using. These overrides both the attributes given from the
      * profiles and the web service's allowedAttributes. 
      */
     private HashMap deniedAttributes = new
     HashMap(); 
 
     /** Combined list of attributes based on: all profiles attributes
      * + allowedAttributes - deniedAttributes. */
     private HashMap attributes = new HashMap(); 
 
     /** Name of web service */
     private String name; 
 
     /** Home page URL for web service. Used for creating hyperlinks (together
      * with the name of the web service). */
     private String url;
     
 
 
     /**
      * Constructor
      * @param id Unique id for the web service. 
      */ 
     protected WebService(String id) {
         this.id = id;
     }
 
 
 
     /**
      * Check all if all the requested attributes are legal for this
      * web service.
      * @param requestedAttributes Names of all requested attributes.
      */
     public boolean allowAccessToAttributes(String requestedAttributes[]) {
         log.finer("allowAccessToAttributes(String[])");
         
         boolean allow = true;
         for (int i = 0; i < requestedAttributes.length; i++) {
             if (!attributes.containsKey(requestedAttributes[i])) {
                log.warning("Service "+id+" can access attributes "+attributes.keySet()+" only, not ["+requestedAttributes[i]+']');
                 allow = false;
                 break;
             }
         }
         return allow;
     }
 
 
 
 
     /**
      * Check attributes for use with SSO. If all attributes are
      * registered in the web services's attributes list and all
      * attributes are allowed to use with SSO, then so be it.
      * @param requestedAttributes The names of all requested attributes
      */
     public boolean allowSsoForAttributes(String requestedAttributes[]) {
         boolean allow = true;
         for (int i = 0; i < requestedAttributes.length; i++) {
             String attrName = requestedAttributes[i];
             if (!attributes.containsKey(attrName) || !((Attribute) attributes.get(attrName)).allowSso()) {
                 allow = false;
                 break;
             }
         }
         return allow;
     }
 
 
 
     
     /**
      * Flatten all attributes into one HashMap (profiles.attributes +
      * allowedAttributes - deniedAttributes
      */
     protected void generateAttributeList(HashMap allAttributes) {
         log.finer("generateAttributeList(HashMap)");
 
         
         /* Profiles */
         for (Iterator profIt = profiles.keySet().iterator(); profIt.hasNext();) {
             Profile profile = (Profile) profiles.get((String)profIt.next());
             HashMap profileAttrs = profile.getAttributes();
 
             alterAttributes(allAttributes, profileAttrs, true);
         }
 
         /* Allowed attributes */
         alterAttributes(allAttributes, allowedAttributes, true);
 
         /* Denied attributes */
         alterAttributes(allAttributes, deniedAttributes, false);
 
         /* Delete old datastructure to release memory. */
         allowedAttributes = null;
         deniedAttributes = null;
         profiles = null;
     }
 
     
 
     /** 
      * Adds or removes attributes from the flattened datastructure.
      * @param allAttributes The hashmap to add or remove from
      * @param changes The hashmap with the changes to be committed
      * @param add true=add, false=remove
      */
     private void alterAttributes(HashMap allAttributes, HashMap changes, boolean add) {
         log.finer("alterAttributes(HashMap)");
 
         for (Iterator attrIt = changes.keySet().iterator(); attrIt.hasNext();) {
                 String attrName = (String) attrIt.next();
 
                 /* Add */
                 if (add) {
                     Attribute addAttr = (Attribute) changes.get(attrName);
                     Attribute origAttr = (Attribute) allAttributes.get(attrName);
                     Attribute existingAttr = (Attribute) attributes.get(attrName);
                     /* If attribute's secLevel is higher than the
                      * previously defined, then set it to the new
                      * value. */
                     int secLevel = origAttr.getSecLevel();
                     if (addAttr.getSecLevel() > secLevel)
                         secLevel = addAttr.getSecLevel();
 
                     attributes.put(attrName, new Attribute(attrName, (addAttr.allowSso() && origAttr.allowSso()), secLevel));
                 }
                 
                 /* Remove */
                 else {
                     attributes.remove(attrName);
                 }
         }
     }
 
 
 
     /**
      * Return name of security level for a given set of attributes.
      * @param requestedAttributes Names of all requested attributes.
      */
     public String secLevelNameForAttributes(String requestedAttributes[]) {
         int highestLevel = 1;
 
         for (int i = 0; i < requestedAttributes.length; i++) {
             String attrName = requestedAttributes[i];
             int attrSecLevel = ((Attribute) attributes.get(attrName)).getSecLevel();
             if (attributes.containsKey(attrName) &&  attrSecLevel > highestLevel) {
                 highestLevel = attrSecLevel;
             }
         }
         return Attribute.secLevelName(highestLevel);
     }
 
 
 
     /**
      * Set web service's allowed attributes
      * @param allowed Allowed attributes
      */
     protected void setAllowedAttributes(HashMap allowed) {
         log.finer("setAllowedAttributes(HashMap)");
         allowedAttributes = allowed;
     }
 
     /**
      * Set web service's denied attributes
      * @param denied Denied attributes
      */
     protected void setDeniedAttributes(HashMap denied) {
         log.finer("setDeniedAttributes(HashMap)");
         deniedAttributes = denied;
     }
 
     /**
      * Set web service's profiles
      * @param profiles Profiles associated with the web service
      */
     protected void setProfiles(HashMap profiles) {
         log.finer("setProfiles(HashMap)");
         this.profiles = profiles;
     }
 
     /**
      * Set web service's name
      * @param name The name of the web service
      */
     protected void setName(String name) {
         log.finer("setName(String)");
         this.name = name;
     }
     
     /**
      * Set web service's home page url
      * @param url URL for the home page of the web service
      */
     protected void setUrl(String url) {
         log.finer("setUrl(String)");
         this.url = url;
     }
 
     /**
      * Get home page URL
      */
     public String getUrl() {
         log.finer("getUrl()");
         return url;
     }
         
     /**
      * Get web service name
      */
     public String getName() {
         log.finer("getName()");
         return name;
     }
 
     /**
      * List of all attributes a web service is allowed to use.
      */
     public HashMap getAttributes() {
         log.finer("getAttributes()");
         return attributes;
     }
 
 
     /**
      * Get web service's unique id.
      */
     public String getId() {
         log.finer("getId()");
         return id;
     }
 
 
 
 }
