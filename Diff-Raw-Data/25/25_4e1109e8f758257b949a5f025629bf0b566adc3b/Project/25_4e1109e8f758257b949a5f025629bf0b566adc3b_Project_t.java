 /**
  * Copyright 2010 OpenEngSB Division, Vienna University of Technology
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.openengsb.opencit.core.projectmanager.model;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.openengsb.core.common.Domain;
 
 public class Project implements Serializable {
 
    private Map<String, String> services;
 
     public enum State {
             OK,
             IN_PROGRESS,
             FAILURE;
     }
 
     private State state;
     private String id;
 
     public Project() {
 
     }
 
     public Project(String id) {
         this.id = id;
     }
 
     public String getId() {
         return id;
     }
 
     public State getState() {
         return state;
     }
 
     public void setState(State state) {
         this.state = state;
     }
 
     /**
      * 
      * the created services, key is the type, and value the id of the service
      */
     public Map<Class<? extends Domain>, String> getServices() {
        if (services == null) {
            return new HashMap<Class<? extends Domain>, String>();
        }
         Map<Class<? extends Domain>, String> map = new HashMap<Class<? extends Domain>, String>(services.size());
         for (Entry<String, String> entry : services.entrySet()) {
             map.put(getClass(entry.getKey()), entry.getValue());
         }
         return map;
     }
 
     @SuppressWarnings("unchecked")
     private Class<? extends Domain> getClass(String key) {
         try {
             return (Class<? extends Domain>) getClass().getClassLoader().loadClass(key);
         } catch (ClassNotFoundException cnfe) {
             throw new RuntimeException(cnfe);
         }
     }
 
     public void addService(Class<? extends Domain> type, String id) {
        if (services == null) {
            services = new HashMap<String, String>();
        }
         services.put(type.getName(), id);
     }
 }
