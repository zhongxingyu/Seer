 package com.worthsoln.tenancy;
 
 import com.worthsoln.patientview.model.Tenancy;
 import com.worthsoln.utils.LegacySpringUtils;
 
 
 /**
  *      todo spring bean?
  */
 public class TenancyUtils {
 
     public static String rewriteTenancyUriRemoveContext(String uri) {
 
         String result = uri;
 
         // if there is no tenancy then skip
         if (LegacySpringUtils.getSecurityUserManager().isLoggedInToTenancy() && uri != null) {
 
             // if there is a tenancy then check for the tenancy context and strip off
             Tenancy tenancy = LegacySpringUtils.getSecurityUserManager().getLoggedInTenancy();
 
             String tenancyContext = "/" + tenancy.getContext();
 
             if (uri.startsWith(tenancyContext)) {
                 // remove and forward
                 result = uri.substring(tenancyContext.length());
             }
         }
 
         return result;
     }
 
     public static String rewriteTenancyUrlAddContext(String uri) {
 
        Tenancy tenancy = LegacySpringUtils.getSecurityUserManager().getLoggedInTenancy();
 
        String context = tenancy != null ? "/" + tenancy.getContext() : "";
 
        return context + uri;
     }
 }
