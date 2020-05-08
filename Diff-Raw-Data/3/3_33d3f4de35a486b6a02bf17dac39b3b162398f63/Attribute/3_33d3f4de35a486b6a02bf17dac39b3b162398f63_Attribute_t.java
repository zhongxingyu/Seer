 /*
  * Copyright 2012 Canoo Engineering AG.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.canoo.dolphin.core;
 
 public interface Attribute extends Observable {
     String QUALIFIER_PROPERTY   = "qualifier";
     String DIRTY_PROPERTY       = "dirty";
     String BASE_VALUE           = "baseValue";
     String VALUE                = "value";
     String TAG                  = "tag";
 
     Object getValue();
 
     void setValue(Object value);
 
     String getPropertyName();
 
     String getQualifier();
 
     long getId();
 
     void setId(long id);
 
     Tag getTag();
 
     void syncWith(Attribute source);
 
     boolean isDirty();
 
     Object getBaseValue();
 

    // todo dk: add rebase to BasePresentationModel or to facade

     /** setting the base value to the current value, effectively providing a new base for "dirty" calculations */
     void rebase();
 
     /** setting the current value back to the last known base, which is the base value */
     void reset();
 }
