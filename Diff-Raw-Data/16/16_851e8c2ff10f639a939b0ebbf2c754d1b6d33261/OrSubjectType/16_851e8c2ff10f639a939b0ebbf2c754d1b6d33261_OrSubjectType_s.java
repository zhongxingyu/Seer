 package com.sun.identity.admin.model;
 
 import com.sun.identity.entitlement.EntitlementSubject;
 import com.sun.identity.entitlement.OrSubject;
 import java.io.Serializable;
 
 public class OrSubjectType 
    extends ExpressionSubjectType
     implements Serializable {
 
     public ViewSubject newViewSubject() {
         OrViewSubject ovs = new OrViewSubject();
         ovs.setSubjectType(this);
 
         return ovs;
     }
 
     public ViewSubject newViewSubject(EntitlementSubject es, SubjectFactory stf) {
         assert(es instanceof OrSubject);
         OrSubject os = (OrSubject)es;
 
         OrViewSubject ovs = (OrViewSubject)newViewSubject();
 
         for (EntitlementSubject childEs: os.getESubjects()) {
             ViewSubject vs = stf.getViewSubject(childEs);
             ovs.addViewSubject(vs);
         }
 
         return ovs;
     }
 }
