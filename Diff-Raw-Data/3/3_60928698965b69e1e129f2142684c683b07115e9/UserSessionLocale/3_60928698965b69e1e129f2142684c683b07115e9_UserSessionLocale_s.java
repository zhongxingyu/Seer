/**
 *
 */
 package com.aciertoteam.common.i18n;
 
 import java.io.Serializable;
 import java.util.Locale;
 
 /**
  * User session locale holder.
  * 
  * @author ishestiporov
  */
 public class UserSessionLocale implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     private Locale defaultLocale = Locale.getDefault();
 
     private Locale locale;
 
     public void setLocale(Locale locale) {
         this.locale = locale;
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
 
 }
