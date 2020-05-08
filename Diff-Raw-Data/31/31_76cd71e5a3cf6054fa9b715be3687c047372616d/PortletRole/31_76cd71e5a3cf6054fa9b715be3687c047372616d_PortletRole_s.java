 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlet;
 
 import java.io.Serializable;
 import java.util.Comparator;
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 /**
  * The <code>PortletRole</code> describes the supported portlet roles used
  * by the portal. In general, <code>Group</code>s contain <code>User</codes
  * with a single <code>PortleRole</code>
  * <p/>
  * Over time, we plan on allowing users to have more than one role
  *
  * @see org.gridlab.gridsphere.portlet.PortletGroup
  */
 public class PortletRole implements Serializable, Comparator, Cloneable {
 
     private String oid = null;
     private int priority;
     private String roleName = null;
 
     private static final int GUEST_ROLE = 1;
     private static final int USER_ROLE = 2;
     private static final int ADMIN_ROLE = 3;
     private static final int SUPER_ROLE = 4;
 
     private static final String GUEST_ROLE_STRING = "GUEST";
     private static final String USER_ROLE_STRING = "USER";
     private static final String ADMIN_ROLE_STRING = "ADMIN";
     private static final String SUPER_ROLE_STRING = "SUPER";
 
     public static final PortletRole GUEST = new PortletRole(GUEST_ROLE_STRING, GUEST_ROLE);
     public static final PortletRole USER = new PortletRole(USER_ROLE_STRING, USER_ROLE);
     public static final PortletRole ADMIN = new PortletRole(ADMIN_ROLE_STRING, ADMIN_ROLE);
     public static final PortletRole SUPER = new PortletRole(SUPER_ROLE_STRING, SUPER_ROLE);
 
     public PortletRole() {
 
     }
 
     /**
      * Constructs an instance of PortletRole
      */
     public PortletRole(String roleName, int priority) {
         this.roleName = roleName;
         this.priority = priority;
     }
 
     /**
      * Return the appropriate <code>PortletRole</code> obtained by parsing
      * the <code>String</code> portlet role name
      *
      * @param portletRole a portlet role name
      * @throws IllegalArgumentException if the <code>String</code> does
      *                                  not match any of the pre-defined roles
      */
     public static PortletRole toPortletRole(String portletRole)
             throws IllegalArgumentException {
         if (portletRole.equalsIgnoreCase("GUEST")) {
             return GUEST;
         } else if (portletRole.equalsIgnoreCase("USER")) {
             return USER;
         } else if (portletRole.equalsIgnoreCase("ADMIN")) {
             return ADMIN;
         } else if (portletRole.equalsIgnoreCase("SUPER")) {
             return SUPER;
         } else {
             throw new IllegalArgumentException("Unable to create PortletRole corresponding to: " + portletRole);
         }
     }
 
     /**
      * Return the appropriate <code>PortletRole</code> obtained by parsing
      * the <code>int</code> portlet role priority
      *
     * @param portletRole a portlet role name
      * @throws IllegalArgumentException if the <code>int</code> does
      *                                  not match any of the pre-defined roles
      */
     public static PortletRole toPortletRole(int priority)
             throws IllegalArgumentException {
         if (priority == GUEST_ROLE) {
             return GUEST;
         } else if (priority == USER_ROLE) {
             return USER;
         } else if (priority == ADMIN_ROLE) {
             return ADMIN;
         } else if (priority == SUPER_ROLE) {
             return SUPER;
         } else {
             throw new IllegalArgumentException("Unable to create PortletRole corresponding to: " + priority);
         }
     }
 
     /**
      * Returns a locale-specific <code>String</code> representation of
      * the portlet role
      *
      * @return the locale-specific role expressed as a <code>String</code>
      */
     public String getText(Locale locale) {
         ResourceBundle bundle = ResourceBundle.getBundle("gridsphere.resources.Portlet", locale);
         String key = toString();
         String value = bundle.getString(key);
         return value;
     }
 
     public String getOid() {
         return oid;
     }
 
     public void setOid(String oid) {
         this.oid = oid;
     }
 
     /**
      * Returns a unique id for this role
      *
      * @return a unique id for this role
      */
     public int getID() {
         return priority;
     }
 
     public void setID(int id) {
         this.priority = id;
     }
 
     /**
      * Returns the role name
      *
      * @return the role name
      */
     public String getName() {
         return roleName;
     }
 
     public void setName(String roleName) {
         this.roleName = roleName;
     }
 
     /**
      * Returns <code>true</code> if this role is <code>GUEST</code>, <code>false otherwise</code>
      *
      * @return <code>true</code> if this role is <code>GUEST</code>, <code>false otherwise</code>
      */
     public boolean isGuest() {
         return (priority == GUEST_ROLE) ? true : false;
     }
 
     /**
      * Returns <code>true</code> if this role is <code>USER</code>, <code>false otherwise</code>
      *
      * @return <code>true</code> if this role is <code>USER</code>, <code>false otherwise</code>
      */
     public boolean isUser() {
         return (priority == USER_ROLE) ? true : false;
     }
 
     /**
      * Returns <code>true</code> if this role is <code>ADMIN</code>, <code>false otherwise</code>
      *
      * @return <code>true</code> if this role is <code>ADMIN</code>, <code>false otherwise</code>
      */
     public boolean isAdmin() {
         return (priority == ADMIN_ROLE) ? true : false;
     }
 
     /**
      * Returns <code>true</code> if this role is <code>SUPER</code>, <code>false otherwise</code>
      *
      * @return <code>true</code> if this role is <code>SUPER</code>, <code>false otherwise</code>
      */
     public boolean isSuper() {
         return (priority == SUPER_ROLE) ? true : false;
     }
 
     public String toString() {
         return roleName;
     }
 
     public Object clone() throws CloneNotSupportedException {
         PortletRole r = (PortletRole) super.clone();
         r.roleName = this.roleName;
         r.priority = this.priority;
         return r;
     }
 
     public int compare(Object left, Object right) {
         int leftID = ((PortletRole) left).getID();
         int rightID = ((PortletRole) right).getID();
         int result;
         if (leftID < rightID) {
             result = -1;
         } else if (leftID > rightID) {
             result = 1;
         } else {
             result = 0;
         }
         return result;
     }
 
     public boolean equals(Object object) {
         if (object != null && (object.getClass().equals(this.getClass()))) {
             PortletRole portletRole = (PortletRole) object;
             return (priority == portletRole.getID());
         }
         return false;
     }
 
     public int hashCode() {
         return priority;
     }
 
     private Object readResolve() {
         PortletRole r = PortletRole.GUEST;
         switch (priority) {
             case GUEST_ROLE:
                 r = PortletRole.GUEST;
                 break;
             case USER_ROLE:
                 r = PortletRole.USER;
                 break;
             case ADMIN_ROLE:
                 r = PortletRole.ADMIN;
                 break;
             case SUPER_ROLE:
                 r = PortletRole.SUPER;
                 break;
         }
         return r;
     }
 
 }
