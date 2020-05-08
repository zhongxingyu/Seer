 package com.sun.identity.admin.model;
 
 import com.sun.identity.entitlement.EntitlementSubject;
 import java.io.Serializable;
 
 public class AndSubjectType 
    extends ExpressionSubjectType
     implements Serializable {
 
     public ViewSubject newViewSubject() {
         AndViewSubject avs = new AndViewSubject();
         avs.setSubjectType(this);
 
         return avs;
     }
 
     public ViewSubject newViewSubject(EntitlementSubject es, SubjectFactory stf) {
         // TODO
         return null;
     }
 
 }
