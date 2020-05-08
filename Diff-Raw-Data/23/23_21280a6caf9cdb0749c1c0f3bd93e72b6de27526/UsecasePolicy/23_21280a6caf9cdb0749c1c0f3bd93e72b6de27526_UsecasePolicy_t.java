 package org.wyona.security.core;
 
 import org.wyona.security.core.api.Identity;
 import org.wyona.security.core.GroupPolicy;
 
 import java.util.Vector;
 
 /**
  *
  */
 public class UsecasePolicy {
 
     private String name;
 
     private Vector identities = null;
     private Vector gps = null;
 
     /**
      *
      */
     public UsecasePolicy(String name) {
         this.name = name;
         identities = new Vector();
         gps = new Vector();
     }
 
     /**
      *
      */
     public String getName() {
         return name;
     }
 
     /**
      *
      */
     public void addIdentity(Identity identity) {
         identities.add(identity);
     }
 
     /**
      *
      */
     public Identity[] getIdentities() {
         Identity[] ids = new Identity[identities.size()];
         for (int i = 0; i < ids.length; i++) {
             ids[i] = (Identity) identities.elementAt(i);
         }
         return ids;
     }
 
     /**
      *
      */
     public void addGroupPolicy(GroupPolicy groupPolicy) {
         gps.add(groupPolicy);
     }
 
     /**
      *
      */
    public GroupPolicy[] getGroupPolicies() {
         GroupPolicy[] gs = new GroupPolicy[gps.size()];
         for (int i = 0; i < gs.length; i++) {
             gs[i] = (GroupPolicy) gps.elementAt(i);
         }
         return gs;
     }
 }
