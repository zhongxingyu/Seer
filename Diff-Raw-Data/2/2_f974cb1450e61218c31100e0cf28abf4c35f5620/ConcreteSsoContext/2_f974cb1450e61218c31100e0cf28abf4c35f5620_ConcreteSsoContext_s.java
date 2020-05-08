 /*
  * Copyright 2012 Herald, Southeast University.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package cn.edu.seu.herald.sso.domain;
 
 import cn.edu.seu.herald.session.Session;
 import cn.edu.seu.herald.sso.SsoServiceConstants;
 import java.util.Enumeration;
 import java.util.Stack;
 
 /**
  *
  * @author rAy
  */
 public class ConcreteSsoContext implements SingleSignOnContext {
 
     private Session session;
 
     public ConcreteSsoContext(Session session) {
         this.session = session;
     }
 
     @Override
     public StudentUser getLogOnStudentUser() {
         int cardNumber = (Integer) session.getAttribute(
                 SsoServiceConstants.CARD_NUMBER_NODE_NAME);
         String studentId = (String) session.getAttribute(
                 SsoServiceConstants.STUDENT_ID_NODE_NAME);
         String fullName = (String) session.getAttribute(
                 SsoServiceConstants.STUDENT_FULL_NAME_NODE_NAME);
         StudentUser sUser = new StudentUser();
         sUser.setCardNumber(cardNumber);
         sUser.setStudentId(studentId);
         sUser.setFullName(fullName);
         return sUser;
     }
 
     @Override
     public Object getAttribute(String name) {
         String sessionAttributeName =
                 SsoServiceConstants.SSO_CONTEXT_PROPERTIES_PREFIX + name;
         return session.getAttribute(sessionAttributeName);
     }
 
     @Override
     public Enumeration<String> getAttributeNames() {
         Stack<String> stack = new Stack<String>();
         Enumeration<String> sessioEnum = session.getAttributeNames();
         while (sessioEnum.hasMoreElements()) {
             String name = sessioEnum.nextElement();
             boolean isSsoPropertyNode = (name != null) && name.startsWith(
                     SsoServiceConstants.SSO_CONTEXT_PROPERTIES_PREFIX);
             if (isSsoPropertyNode) {
                 String ssoPropertyName = name.substring(
                         SsoServiceConstants.SSO_CONTEXT_PROPERTIES_PREFIX
                         .length());
                stack.add(name);
             }
         }
         return stack.elements();
     }
 
 }
