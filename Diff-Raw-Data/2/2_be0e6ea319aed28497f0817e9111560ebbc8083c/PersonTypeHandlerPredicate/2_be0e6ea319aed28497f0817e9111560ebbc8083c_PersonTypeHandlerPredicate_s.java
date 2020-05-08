 /*
 * Copyright 2010-2011 the original author or authors.
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
 package org.springextensions.db4o.example;
 
 import com.db4o.reflect.ReflectClass;
 import com.db4o.typehandlers.TypeHandlerPredicate;
 import org.springframework.util.Assert;
 
 /**
  * author: olli
  */
 public class PersonTypeHandlerPredicate implements TypeHandlerPredicate {
 
     public PersonTypeHandlerPredicate(Class clazz) {
         Assert.isTrue(Person.class == clazz);
     }
 
     @Override
     public boolean match(ReflectClass candidate) {
         final ReflectClass reflectClass = candidate.reflector().forClass(Person.class);
         return candidate == reflectClass;
     }
 
 }
