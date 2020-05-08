 package org.agileinsider.concordion.junit;
 
 /*
  * Copyright 2011 Mark Barnes (mark@agileinsider.org)
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 import org.junit.runners.model.FrameworkMethod;
 
 import java.lang.annotation.Annotation;
 
 public class SpecificationFrameworkMethod extends FrameworkMethod {
     private final String specificationDescription;
 
     public SpecificationFrameworkMethod(String specificationDescription) {
         super(null);
         this.specificationDescription = specificationDescription;
     }
 
     public String getName() {
         return specificationDescription;
     }
 
     public Annotation[] getAnnotations() {
         return new Annotation[0];
     }
 
     public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
         return null;
     }
 
     @Override
     public boolean equals(Object o) {
        if (!SpecificationFrameworkMethod.class.isInstance(o)) {
            return false;
        }
        return specificationDescription != null && specificationDescription.equals(((SpecificationFrameworkMethod) o).specificationDescription);
     }
 
     @Override
     public int hashCode() {
        return (specificationDescription != null ? specificationDescription.hashCode() : 0);
     }
 }
