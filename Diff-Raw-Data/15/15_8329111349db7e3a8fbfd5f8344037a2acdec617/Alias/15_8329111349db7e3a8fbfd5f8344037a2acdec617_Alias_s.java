 /**
  * The Subversion Authentication Parse Module (SAPM for short).
  *
  * Copyright (c) 2010, 2011 by SoftwareEntwicklung Beratung Schulung (SoEBeS)
  * Copyright (c) 2010, 2011 by Karl Heinz Marbaise
  *
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.soebes.subversion.sapm;
 
 /**
  * This represents an alias.
  *
  * [aliases]
  * alias_user = xyz
  *
  * name = alias_user
  * definition = xyz
  *
  * search for name => ?
  *
  * @author Karl Heinz Marbaise
  *
  */
 public class Alias implements IPrincipal {
 
     private String name;
     private String definition;
 
     public Alias() {
         setName(null);
         setDefinition(null);
     }
 
     public Alias(String name, String definition) {
         super();
         setName(name);
         setDefinition(definition);
     }
 
     @Override
     public String getName() {
         return this.name;
     }
 
     @Override
     public void setName(String name) {
         this.name = name;
     }
 
     /* (non-Javadoc)
      * @see com.soebes.subversion.sapm.IPrincipal#contains(java.lang.String)
      */
     public boolean contains(String name) {
         return getName().equals(name);
     }
 
     public void setDefinition(String definition) {
         this.definition = definition;
     }
 
     public String getDefinition() {
         return definition;
     }
 
     public String toString() {
         return "&" + getName();
     }
 
 }
