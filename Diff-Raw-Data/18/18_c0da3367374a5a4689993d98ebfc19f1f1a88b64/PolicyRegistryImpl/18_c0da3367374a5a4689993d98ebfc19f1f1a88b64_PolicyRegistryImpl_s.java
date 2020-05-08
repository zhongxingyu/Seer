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
 package org.apache.neethi;
 
 import java.util.HashMap;
 
 /**
  * Provides a default implementation of PolicyRegistry interface.
  */
 public class PolicyRegistryImpl implements PolicyRegistry {
     
     private PolicyRegistry parent = null;
     
     private HashMap reg = new HashMap();
     
     public PolicyRegistryImpl() {
     }
     
     public PolicyRegistryImpl(PolicyRegistry parent) {
         this.parent = parent;
     }
     
     public Policy lookup(String key) {
         Policy policy = (Policy) reg.get(key);
         
         if (policy == null && parent != null) {
             return parent.lookup(key);
         }
        return null;
     }
 
     public void register(String key, Policy policy) {
         reg.put(key, policy);
     }
 }
