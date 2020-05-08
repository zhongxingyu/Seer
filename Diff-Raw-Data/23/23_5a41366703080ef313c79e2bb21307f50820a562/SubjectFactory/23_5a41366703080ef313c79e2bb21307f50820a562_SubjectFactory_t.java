 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2009 Sun Microsystems Inc. All Rights Reserved
  *
  * The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: SubjectFactory.java,v 1.5 2009-07-24 21:22:43 farble1670 Exp $
  */
 
 package com.sun.identity.admin.model;
 
import com.sun.identity.admin.ManagedBeanResolver;
 import com.sun.identity.admin.dao.SubjectDao;
 import com.sun.identity.entitlement.EntitlementSubject;
 import java.io.Serializable;
 import java.util.Map;
 
 public class SubjectFactory implements Serializable {
     private Map<String,SubjectType> entitlementSubjectToSubjectTypeMap;
     private Map<String,SubjectDao> viewSubjectToSubjectDaoMap;
     private Map<String,SubjectType> viewSubjectToSubjectTypeMap;
     private Map<SubjectType,SubjectContainer> subjectTypeToSubjectContainerMap;
 
     private SubjectType getSubjectType(EntitlementSubject es) {
         String className = es.getClass().getName();
         return entitlementSubjectToSubjectTypeMap.get(className);
     }
 
     public SubjectDao getSubjectDao(ViewSubject vs) {
         String className = vs.getClass().getName();
         return viewSubjectToSubjectDaoMap.get(className);
     }
 
     public ViewSubject getViewSubject(EntitlementSubject es) {
         if (es == null) {
             return null;
         }
         
         SubjectType st = getSubjectType(es);
         assert(st != null);
         ViewSubject vs = st.newViewSubject(es, this);
 
         return vs;
     }
 
     public SubjectType getSubjectType(String className) {
         SubjectType st = viewSubjectToSubjectTypeMap.get(className);
         return st;
     }
 
     public SubjectContainer getSubjectContainer(SubjectType st) {
         SubjectContainer sc = subjectTypeToSubjectContainerMap.get(st);
         return sc;
     }
 
     public void setViewSubjectToSubjectDaoMap(Map<String, SubjectDao> viewSubjectToSubjectDaoMap) {
         this.viewSubjectToSubjectDaoMap = viewSubjectToSubjectDaoMap;
     }
 
     public void setEntitlementSubjectToSubjectTypeMap(Map<String, SubjectType> entitlementSubjectToSubjectTypeMap) {
         this.entitlementSubjectToSubjectTypeMap = entitlementSubjectToSubjectTypeMap;
     }
 
     public void setViewSubjectToSubjectTypeMap(Map<String, SubjectType> viewSubjectToSubjectTypeMap) {
         this.viewSubjectToSubjectTypeMap = viewSubjectToSubjectTypeMap;
     }
 
     public void setSubjectTypeToSubjectContainerMap(Map<SubjectType, SubjectContainer> subjectTypeToSubjectContainerMap) {
         this.subjectTypeToSubjectContainerMap = subjectTypeToSubjectContainerMap;
     }

    public static SubjectFactory getInstance() {
        ManagedBeanResolver mbr = new ManagedBeanResolver();
        SubjectFactory sf = (SubjectFactory)mbr.resolve("subjectFactory");
        return sf;
    }
 }
