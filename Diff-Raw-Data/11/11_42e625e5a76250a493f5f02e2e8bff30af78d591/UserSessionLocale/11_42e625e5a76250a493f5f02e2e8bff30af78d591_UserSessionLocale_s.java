 package com.aciertoteam.i18n;
 
 import java.io.Serializable;
 import java.util.Locale;
 
 /**
  * User session locale holder. TODO get locale from user country
  * 
  * @author ishestiporov
  */
 public class UserSessionLocale implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     private boolean initialized = false;
 
     private Locale defaultLocale = Locale.getDefault();
 
     private Locale locale;
 
     public void setLocale(Locale locale) {
         this.locale = locale;
         this.initialized = true;
     }
 
     public Locale getLocale() {
         return locale;
     }
 
     /**
      * Returns user set locale or the default one if user session locale is not
      * set.
     * 
     * @return
      */
     public Locale getResolvedLocale() {
         return locale != null ? locale : defaultLocale;
     }
 
     public Locale getDefaultLocale() {
         return defaultLocale;
     }
 
     public void setDefaultLocale(Locale defaultLocale) {
         this.defaultLocale = defaultLocale;
     }
 
     public boolean isInitialized() {
         return initialized;
     }
 }
