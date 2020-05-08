 /*
  * Copyright (c) 2013 Allogy Interactive.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.allogy.coffeecan.statements;
 
 import com.allogy.coffeecan.Identifiable;
 import com.fasterxml.jackson.annotation.JsonProperty;
 
 import javax.validation.constraints.NotNull;
 
 public class Activity extends StatementObject implements Identifiable
 {
     public static final String OBJECT_TYPE = "Activity";
     @Deprecated
     public static final String ObjectType = OBJECT_TYPE;
 
     @NotNull
    private final String id;
 
     private ActivityDefinition definition;
 
     public Activity(@JsonProperty("id") String id)
     {
         this.id = id;
         objectType = OBJECT_TYPE;
     }
 
     @JsonProperty("id")
     public String getID()
     {
         return id;
     }
 
     public ActivityDefinition getDefinition()
     {
         return definition;
     }
 
     public void setDefinition(ActivityDefinition definition)
     {
         this.definition = definition;
     }
 }
