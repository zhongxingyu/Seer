 /*
  *    Copyright (c) 2006 LiXiao.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package com.thoughtworks.fireworks.adapters.psi;
 
 import com.intellij.codeInsight.AnnotationUtil;
 import com.intellij.psi.PsiClass;
 import com.intellij.psi.PsiMethod;
 import com.thoughtworks.fireworks.adapters.ProjectAdapter;
 
 public class PsiClassAdapter {
     private final PsiClass psiClass;
 
     public PsiClassAdapter(PsiClass psiClass) {
         this.psiClass = psiClass;
     }
 
     public void jumpToMethod(String methodName) {
         if (psiClass == null) {
             return;
         }
         navigationElement().navigate(PsiUtils.findMethod(psiClass, methodName));
     }
 
     private NavigationElementAdapter navigationElement() {
         return new NavigationElementAdapter(psiClass.getNavigationElement());
     }
 
     public boolean isJUnitTestCase(ProjectAdapter project) {
        if (psiClass == null || !project.matchesExpectedTestCaseNameRegex(psiClass.getQualifiedName())) {
             return false;
         }
 
         PsiMethod[] methods = psiClass.getMethods();
         for (int i = 0; i < methods.length; i++) {
             if (hasAnnotationOfOrgJunitTest(methods[i])) {
                 return true;
             }
         }
         try {
             return psiClass.isInheritor(PsiUtils.getTestCasePsiClass(project), true);
         } catch (ClassNotFoundException e) {
             return false;
         }
     }
 
     private boolean hasAnnotationOfOrgJunitTest(PsiMethod method) {
         return AnnotationUtil.isAnnotated(method, org.junit.Test.class.getName(), true);
     }
 
 }
