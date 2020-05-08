 /*
  * Copyright 2013 Qubell, Inc.
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
 
 package com.qubell.services;
 
 import java.util.Map;
 
 /**
  * Additional settings, used for instance launch
  * @author Alex Krupnov
  */
 public class LaunchSettings {
     private String environmentId;
    //Default of zero makes instance immortal
    private long destroyInterval = -1;
     private Map<String, Object> parameters;
 
     /**
      * Inits setting with empty vales
      */
     public LaunchSettings() {
     }
 
     /**
      * Initializes settings with parameters
      * @param parameters launch parameters (optional)
      */
     public LaunchSettings(Map<String, Object> parameters) {
         this.parameters = parameters;
     }
 
     /**
      * Initializes settings with parameters, destroy interval and  environment id
      * @param parameters launch parameters (optional)
     * @param destroyInterval instance destroy interval in milliseconds (optional), 0 for immortal instance
      * @param environmentId environment id to be used (optional)
      */
     public LaunchSettings(String environmentId, long destroyInterval, Map<String, Object> parameters) {
         this.environmentId = environmentId;
         this.destroyInterval = destroyInterval;
         this.parameters = parameters;
     }
 
     /**
      * Initializes settings with parameters and  environment id
      * @param parameters launch parameters (optional)
      * @param environmentId environment id to be used (optional)
      */
     public LaunchSettings(String environmentId, Map<String, Object> parameters) {
         this.environmentId = environmentId;
         this.parameters = parameters;
     }
 
     /**
      * @return environment id to be used for launch or null
      */
     public String getEnvironmentId() {
         return environmentId;
     }
 
     /**
      * @return destroy interval for instance, -1 for default value
      */
     public long getDestroyInterval() {
         return destroyInterval;
     }
 
     /**
      * Extra parameters to be used for launch
      * @return parameters map
      */
     public Map<String, Object> getParameters() {
         return parameters;
     }
 }
