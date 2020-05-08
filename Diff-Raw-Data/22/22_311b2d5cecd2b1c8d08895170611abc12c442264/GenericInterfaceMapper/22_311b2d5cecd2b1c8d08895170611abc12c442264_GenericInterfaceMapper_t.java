 package org.rosenvold.spring.convention.interfacemappers;
 /*
  * Copyright 2011 the original author or authors.
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
 
 import org.rosenvold.spring.convention.CandidateEvaluator;
 import org.rosenvold.spring.convention.InterfaceToImplementationMapper;
 
 /**
  * @author Kristian Rosenvold
  */
 public abstract class GenericInterfaceMapper implements InterfaceToImplementationMapper {
     private final CandidateEvaluator candidateEvaluator;
 
     protected GenericInterfaceMapper(CandidateEvaluator candidateEvaluator) {
         this.candidateEvaluator = candidateEvaluator;
     }
 
     @Override
     public Class getBeanClass(Class aClass) {
         final String s = getRemappedName(aClass);
        if (s == null) return null;
         final Class prospect = resolveClass(s);
        if (prospect == null) return null;
         return candidateEvaluator.isBean(prospect) ? prospect : null;
     }
 
     abstract String getRemappedName(Class aClass);
 
 
     protected Class resolveClass(final String beanName) {
         try {
             return Class.forName(beanName);
         } catch (ClassNotFoundException ex) {
             return null;
         }
     }
 
 
 }
