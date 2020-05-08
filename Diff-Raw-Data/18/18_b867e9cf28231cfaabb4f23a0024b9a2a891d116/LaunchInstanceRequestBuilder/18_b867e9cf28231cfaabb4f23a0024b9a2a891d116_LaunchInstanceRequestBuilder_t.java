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
 
 package com.qubell.services.ws;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * Builder for launch instance request, see {@link ApplicationService#launch(String, String, String, String, long, java.util.Map)}
  * @author Alex Krupnov
  */
 public class LaunchInstanceRequestBuilder extends AbstractRequestBuilder<LaunchInstanceRequestBuilder> {
     private static final String VERSION_KEY = "version";
     private static final String ENVIRONMENT_KEY = "environmentId";
     private static final String DESTROY_INTERVAL_KEY = "destroyInterval";
     private static final String INSTANCE_NAME_KEY = "instanceName";
 
     public LaunchInstanceRequestBuilder() {
     }
 
     /**
      * Adds version (if not null)
      * @param value of version
      * @return builder itself
      */
     public LaunchInstanceRequestBuilder addVersion(String value) {
         if (!StringUtils.isBlank(value)) {
             targetFields.put(VERSION_KEY, value);
         }
         return this;
     }
 
     /**
      * Adds environment id (if not null)
      * @param value value of env id
      * @return builder itself
      */
     public LaunchInstanceRequestBuilder addEnvironmentId(String value) {
         if (!StringUtils.isBlank(value)) {
             targetFields.put(ENVIRONMENT_KEY, value);
         }
         return this;
     }
 
     /**
     * Adds destroy interval (if valid)
     * Valid destroy interval lies between -1 (never destroyed) to maximum long value
      * @param value value of interval
      * @return builder itself
      */
     public LaunchInstanceRequestBuilder addDestroyInterval(long value) {
        if (value >= -1) {
             targetFields.put(DESTROY_INTERVAL_KEY, value);
         }
         return this;
     }
 
     /**
      * Adds instance friendly name (if not null)
      * @param value friendly name
      * @return builder itself
      */
     public LaunchInstanceRequestBuilder addInstanceName(String value) {
         if (!StringUtils.isBlank(value)) {
             targetFields.put(INSTANCE_NAME_KEY, value);
         }
         return this;
     }
 
 }
