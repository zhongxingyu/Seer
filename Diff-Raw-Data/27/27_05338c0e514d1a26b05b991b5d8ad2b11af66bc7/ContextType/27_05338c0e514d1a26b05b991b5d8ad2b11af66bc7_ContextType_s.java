 package fr.cg95.cvq.security.annotation;
 
 import fr.cg95.cvq.business.authority.SiteProfile;
 
 public enum ContextType {
 
     /**
      * For unauthenticated ecitizens : 
      * <ul>
      *   <li>while creating an home folder</li>
      *   <li>while issuing an unregistered request</li>
      * </ul>
      */
    UNAUTH_ECITIZEN,
     
     /**
      * For authenticated ecitizens.
      */
    ECITIZEN, 
     
     /**
      * For authenticated agents with {@link SiteProfile#AGENT agent profile} on current site.
      */
    AGENT, 
     
     /**
      * The union of {@link #ECITIZEN} and {@link #AGENT}.
      */
    ECITIZEN_AGENT, 
     
     /**
      * For authenticated agents with {@link SiteProfile#ADMIN admin profile} on current site.
      */
    ADMIN, 
     
     /** 
      * For super administrators (ie administrators of the whole platform, typically cron jobs :-) ).
      */
    SUPER_ADMIN;
 }
