 package com.crowdstore.models.users;
 
 import java.util.Locale;
 
 /**
  * @author fcamblor
  */
 public class AuthenticatedUser extends User {
     private Locale locale;
     // TODO: Add available rights to authenticated user
 
    public AuthenticatedUser(UserIdentity identity) {
        super(identity);
    }

     /**
      * @deprecated Use either LocaleContextHolder#getLocale() or AppThreadedInfos#getLocale() instead !
      *             This method should only be called when updating current locale in spring !
      *             Note : We cannot set an "accessibleLocale" flag+check here because we don't want to throw exception
      *             when serializing (in JSON) current user's locale. So the @deprecated flag is the better solution to avoid calling
      *             this method directly in code.
      */
     @Deprecated
     public Locale getLocale() {
         return locale;
     }
 
     public AuthenticatedUser setLocale(Locale locale) {
         this.locale = locale;
         return this;
     }
 }
