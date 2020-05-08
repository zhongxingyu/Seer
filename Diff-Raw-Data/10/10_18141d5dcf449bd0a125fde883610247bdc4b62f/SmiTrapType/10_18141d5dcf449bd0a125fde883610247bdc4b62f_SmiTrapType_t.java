 /*
  * Copyright 2012 The OpenNMS Group, Inc.
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
 
 package org.jsmiparser.smi;
 
 import java.util.List;
 
 import org.jsmiparser.phase.xref.XRefProblemReporter;
import org.jsmiparser.util.token.IntegerToken;
 import org.jsmiparser.util.token.IdToken;
 
 public class SmiTrapType extends SmiOidMacro {
 
     protected SmiType m_type;
     private IdToken m_enterprise;
     private List<IdToken> m_variableTokens;
     private String m_description;
    private IntegerToken m_specificType;
 
     public SmiTrapType(IdToken idToken, SmiModule module) {
         super(idToken, module);
     }
 
     public SmiType getType() {
         return m_type;
     }
 
     public void setType(SmiType type) {
         m_type = type;
     }
 
     public void resolveReferences(XRefProblemReporter reporter) {
     	// TODO What, if anything, do we need to do here for TRAP-TYPE?
         //m_type = m_type.resolveThis(reporter, null);
     }
 
     public String getDescription() {
         return m_description;
     }
 
     public void setDescription(String description) {
         m_description = description;
     }
 
     public List<IdToken> getVariableTokens() {
         return m_variableTokens;
     }
     
     public void setVariableTokens(List<IdToken> variableTokens) {
     	m_variableTokens = variableTokens;
     }
     
     public IdToken getEnterprise() {
     	return m_enterprise;
     }
     
     public void setEnterprise(IdToken enterprise) {
     	m_enterprise = enterprise;
     }
     
    public IntegerToken getSpecificType() {
     	return m_specificType;
     }
     
    public void setSpecificType(IntegerToken specificType) {
     	m_specificType = specificType;
     }
 }
