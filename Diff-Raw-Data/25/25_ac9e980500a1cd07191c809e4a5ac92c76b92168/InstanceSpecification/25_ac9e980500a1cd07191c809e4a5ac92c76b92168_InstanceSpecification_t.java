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
 
 /**
  * Specification for {@link Instance} used to start a new one
  * @author Alex Krupnov
  */
 public class InstanceSpecification {
     private Application application;
     private String instanceName;
     private String version;
 
     /**
      * Initializes Instance Specification with target application
      * @param application target application to use
      */
     public InstanceSpecification(Application application) {
         this.application = application;
     }
 
     /**
     * Initializes Instance Specification with target application and its version
     * @param application target application to use
     * @version version of application to launch
     */
    public InstanceSpecification(Application application, String version) {
        this.application = application;
        this.version = version;
    }

    /**
      * Initializes Instance Specification with target application, friendly name and version
      * @param application target application to use
      * @param instanceName desired instance friendly name (optional)
      * @param version desired app version (optional)
      */
     public InstanceSpecification(Application application, String instanceName, String version) {
         this.application = application;
         this.instanceName = instanceName;
         this.version = version;
     }
 
     /**
      * Application to use for instance launch
      * @return app data
      */
     public Application getApplication() {
         return application;
     }
 
     /**
      * Instance friendly name
      * @return name or null
      */
     public String getInstanceName() {
         return instanceName;
     }
 
     /**
      * Version of application to use
      * @return version or null
      */
     public String getVersion() {
         return version;
     }
 }
