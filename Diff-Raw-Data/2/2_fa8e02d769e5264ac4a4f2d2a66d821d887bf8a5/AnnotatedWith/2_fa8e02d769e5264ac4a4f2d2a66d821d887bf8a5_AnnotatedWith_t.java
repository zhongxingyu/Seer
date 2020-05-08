 /*
  *    Copyright 2010 The Meiyo Team
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.googlecode.meiyo.filter;
 
 import java.lang.annotation.Annotation;
 
 /**
  * A filter that verifies the class found is annotated with the given annotation.
  *
  * @version $Id$
  */
 final class AnnotatedWith implements Filter {
 
     /**
      * The annotation has to be searched on classes.
      */
     private final Annotation annotation;
 
     /**
      * Crates a new annotation filter.
      *
      * @param annotation the annotation has to be searched on classes.
      */
     public AnnotatedWith(Annotation annotation) {
         this.annotation = annotation;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean matches(Class<?> clazz) {
         Annotation fromElement = clazz.getAnnotation(this.annotation.annotationType());
        return this.annotation.equals(fromElement);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String toString() {
         return "annotatedWith("
                 + this.annotation
                 + ")";
     }
 
 }
