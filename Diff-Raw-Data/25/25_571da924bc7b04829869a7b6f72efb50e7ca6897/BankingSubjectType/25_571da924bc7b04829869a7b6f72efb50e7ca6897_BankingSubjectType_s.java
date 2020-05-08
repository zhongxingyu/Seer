 package com.sun.identity.admin.model;
 
 import com.sun.identity.entitlement.BankingSubject;
 import com.sun.identity.entitlement.BankingSubject.Banker;
 import com.sun.identity.entitlement.EntitlementSubject;
 import java.io.Serializable;
 
 public class BankingSubjectType
     extends SubjectType
     implements Serializable {
 
     public ViewSubject newViewSubject() {
         ViewSubject vs = new BankingViewSubject();
         vs.setSubjectType(this);
 
         return vs;
     }
 
     public ViewSubject newViewSubject(String name) {
         ViewSubject vs = new BankingViewSubject();
         vs.setSubjectType(this);
         vs.setName(Banker.valueOf(name).toString());
 
         return vs;
     }
 
     public ViewSubject newViewSubject(EntitlementSubject es, SubjectFactory stf) {
         assert(es instanceof BankingSubject);
         BankingSubject bs = (BankingSubject)es;
 
         BankingViewSubject bvs = (BankingViewSubject)newViewSubject();
         bvs.setName(bs.getID());
 
         return bvs;
     }
 }
