 /*
  * Copyright 2001-2004 The Apache Software Foundation.
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
 package org.apache.ws.secpolicy11.builders;
 
 import java.util.List;
 
 import javax.xml.namespace.QName;
 
 import org.apache.axiom.om.OMAttribute;
 import org.apache.axiom.om.OMElement;
 import org.apache.neethi.Assertion;
 import org.apache.neethi.AssertionBuilderFactory;
 import org.apache.neethi.Policy;
 import org.apache.neethi.PolicyComponent;
 import org.apache.neethi.PolicyEngine;
 import org.apache.neethi.builders.AssertionBuilder;
 import org.apache.ws.secpolicy.SP11Constants;
 import org.apache.ws.secpolicy.SPConstants;
 import org.apache.ws.secpolicy.model.UsernameToken;
 
 public class UsernameTokenBuilder implements AssertionBuilder {
 
 
     public Assertion build(OMElement element, AssertionBuilderFactory factory) throws IllegalArgumentException {
         UsernameToken usernameToken = new UsernameToken(SPConstants.SP_V11);
 
         OMAttribute attribute = element.getAttribute(SP11Constants.INCLUDE_TOKEN);
 
         if(attribute != null) {
             int inclusion = SP11Constants.getInclusionFromAttributeValue(attribute.getAttributeValue());
             usernameToken.setInclusion(inclusion);
         }
 
         OMElement policyElement = element.getFirstElement();
 
        if (policyElement != null && policyElement.getQName().equals(org.apache.neethi.Constants.Q_ELEM_POLICY)) {
 
             Policy policy = PolicyEngine.getPolicy(element.getFirstElement());
             policy = (Policy) policy.normalize(false);
 
             for (List<PolicyComponent> alternative: policy.getAlternatives()) {
                 processAlternative(alternative, usernameToken);
                 break; //since there should be only one alternative
             }
         }
 
         return usernameToken;
     }
 
     public QName[] getKnownElements() {
         return new QName[] {SP11Constants.USERNAME_TOKEN};
     }
 
     private void processAlternative(List<PolicyComponent> assertions,
     								UsernameToken parent)
     {
         for (Object element : assertions) {
             Assertion assertion = (Assertion) element;
             QName qname = assertion.getName();
 
             if (SP11Constants.WSS_USERNAME_TOKEN10.equals(qname)) {
                 parent.setUseUTProfile10(true);
 
             } else if (SP11Constants.WSS_USERNAME_TOKEN11.equals(qname)) {
                 parent.setUseUTProfile11(true);
             }
         }
     }
 }
