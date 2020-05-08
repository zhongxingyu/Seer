 package org.wyona.security.core.api;
 
 import org.apache.log4j.Category;
 
 /**
  *
  */
 public class Identity implements java.io.Serializable {
 
     private static Category log = Category.getInstance(Identity.class);
     
     protected String username;
     protected String[] groupnames;
 
     /**
      * Identity is WORLD
      */
     public Identity() {
         username = null;
         groupnames = null;
     }
 
     /**
      *
      */
     public Identity(String username, String[] groupnames) {
         this.username = username;
         this.groupnames = groupnames;
     }
     
     public Identity(User user) {
         try {
             this.username = user.getID();
             Group[] groups = user.getGroups();
             groupnames = new String[groups.length];
             for (int i=0; i<groups.length; i++) {
                 groupnames[i] = groups[i].getID();
             }
         } catch (AccessManagementException e) {
             log.error(e, e);
             throw new RuntimeException(e.getMessage(), e);
         }
     }
 
     /**
      * 
      */
     public String getUsername() {
         return username;
     }
 
 /* WARNING: This method leads to problems re serialization within a clustered environment!
     public User getUser() {
         return user;
     }
 */
     
     /**
      * 
      */
     public String[] getGroupnames() {
         // For security reasons a copy instead the reference is being returned
         if (groupnames != null) {
             String[] copy = new String[groupnames.length];
             for (int i = 0; i < groupnames.length; i++) {
                 copy[i] = groupnames[i];
             }
             return copy;
         } else {
             return null;
         }
     }
     
     /**
      *
      */
     private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
         out.defaultWriteObject();
         // TODO: Does this actually make sense?!
         //out.defaultObject(username);
         //out.defaultObject(groupnames);
     }
 
     /**
      *
      */
     private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
         in.defaultReadObject();
         // TODO: Does this actually make sense?!
         //username = (String) in.readObject();
         //groupnames = (String[]) in.readObject();
     }
     
     public String toString() {
        if (getUsername() == null) return "WORLD";
         return getUsername();
     }
 }
