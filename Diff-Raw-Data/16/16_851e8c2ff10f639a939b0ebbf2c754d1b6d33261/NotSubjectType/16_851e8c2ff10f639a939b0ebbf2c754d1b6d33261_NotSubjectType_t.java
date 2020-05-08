 package com.sun.identity.admin.model;
 
 import com.sun.identity.entitlement.EntitlementSubject;
 import com.sun.identity.entitlement.NotSubject;
 import java.io.Serializable;
 
 public class NotSubjectType 
    extends SubjectType
     implements Serializable {
 
     public ViewSubject newViewSubject() {
         NotViewSubject nvs = new NotViewSubject();
         nvs.setSubjectType(this);
 
         return nvs;
     }
 
     public ViewSubject newViewSubject(EntitlementSubject es, SubjectFactory stf) {
         assert(es instanceof NotSubject);
         NotSubject ns = (NotSubject)es;
         EntitlementSubject nottedSubject = ns.getESubject();
 
         NotViewSubject nvs = (NotViewSubject)newViewSubject();
 
         ViewSubject nottedViewSubject = stf.getViewSubject(nottedSubject);
         nvs.addViewSubject(nottedViewSubject);
 
         return nvs;
     }
 }
