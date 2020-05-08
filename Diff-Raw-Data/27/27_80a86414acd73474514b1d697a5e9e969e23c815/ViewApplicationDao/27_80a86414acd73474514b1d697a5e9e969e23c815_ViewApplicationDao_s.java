 package com.sun.identity.admin.dao;
 
 import com.sun.identity.admin.ManagedBeanResolver;
 import com.sun.identity.admin.model.ViewApplication;
 import com.sun.identity.admin.model.ViewApplicationType;
 import com.sun.identity.entitlement.Application;
 import com.sun.identity.entitlement.ApplicationManager;
 import com.sun.identity.entitlement.EntitlementException;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 
 public class ViewApplicationDao implements Serializable {
     private ViewApplicationTypeDao viewApplicationTypeDao;
 
     public void setViewApplicationTypeDao(ViewApplicationTypeDao viewApplicationTypeDao) {
         this.viewApplicationTypeDao = viewApplicationTypeDao;
     }
 
     public Map<String, ViewApplication> getViewApplications() {
         Map<String,ViewApplication> viewApplications = new HashMap<String, ViewApplication>();
 
         ManagedBeanResolver mbr = new ManagedBeanResolver();
         Map<String,ViewApplicationType> entitlementApplicationTypeToViewApplicationTypeMap = (Map<String,ViewApplicationType>)mbr.resolve("entitlementApplicationTypeToViewApplicationTypeMap");
         // TODO: realm
         for (String name : ApplicationManager.getApplicationNames("/")) {
             // TODO: realm
             Application a = ApplicationManager.getApplication("/", name);
             // application type
             ViewApplicationType vat = entitlementApplicationTypeToViewApplicationTypeMap.get(a.getApplicationType().getName());
             if (vat == null) {
                 // TODO: log
                 continue;
             }
 
            ViewApplication va = new ViewApplication(a, vat);
             viewApplications.put(va.getName(), va);
         }
 
         return viewApplications;
     }
 
     public void setViewApplication(ViewApplication va) {
        // TODO: realm
         try {
             Application a = va.toApplication(viewApplicationTypeDao);
             ApplicationManager.saveApplication("/", a);
         } catch (EntitlementException ee) {
             throw new RuntimeException(ee);
         }
     }
 }
