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
    UNAUTH_ECITIZEN("UNAUTH_ECITIZEN"),
     
     /**
      * For authenticated ecitizens.
      */
    ECITIZEN("ECITIZEN"), 
     
     /**
      * For authenticated agents with {@link SiteProfile#AGENT agent profile} on current site.
      */
    AGENT("AGENT"), 
     
     /**
      * The union of {@link #ECITIZEN} and {@link #AGENT}.
      */
    ECITIZEN_AGENT("ECITIZEN_AGENT"), 
     
     /**
      * For authenticated agents with {@link SiteProfile#ADMIN admin profile} on current site.
      */
    ADMIN("ADMIN"), 
     
     /** 
      * For super administrators (ie administrators of the whole platform, typically cron jobs :-) ).
      */
    SUPER_ADMIN("SUPER_ADMIN");
    
    private final String value;

    ContextType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
 }
