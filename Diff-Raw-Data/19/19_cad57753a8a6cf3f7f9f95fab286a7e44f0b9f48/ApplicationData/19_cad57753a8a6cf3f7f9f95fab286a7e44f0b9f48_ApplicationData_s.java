 package com.bibounde.gaemvnrepo.web;
 
 import java.io.Serializable;
 import java.util.Locale;
 
 import com.vaadin.Application;
 import com.vaadin.service.ApplicationContext.TransactionListener;
 
 /**
  * Thread local implementation of https://vaadin.com/book/-/page/advanced.global.html
  * @author clement
  *
  */
 public class ApplicationData implements TransactionListener, Serializable {
 
     private Application application;
     private Locale locale;
     private SerializabelResourceBundle resourceBundle;
 
     private static ThreadLocal<ApplicationData> instance = new ThreadLocal<ApplicationData>();
 
     public ApplicationData(Application application) {
         this.application = application;
         instance.set(this);
     }
 
     @Override
     public void transactionStart(Application application, Object transactionData) {
         if (this.application == application) {
             instance.set(this);
         }
     }
 
     @Override
     public void transactionEnd(Application application, Object transactionData) {
         if (this.application == application) {
             instance.set(null);
         }
     }
 
     public static void initLocale(Locale locale) {
         instance.get().locale = locale;
         instance.get().resourceBundle = new SerializabelResourceBundle("messages", locale);
     }
     
     public static Locale getLocale() {
         return instance.get().locale;
     }
     
     public static String getMessage(String key) {
         return instance.get().resourceBundle.getString(key);
        //return ResourceBundleUtil.INSTANCE.getResourceBundle(instance.get().locale).getString(key);
        //return ResourceBundleUtil.INSTANCE.getString(instance.get(), key);
     }
 
 }
