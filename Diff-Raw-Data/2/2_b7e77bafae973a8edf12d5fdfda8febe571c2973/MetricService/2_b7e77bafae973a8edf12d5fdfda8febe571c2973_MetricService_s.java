 /*
  * Copyright 2013 OW2 Chameleon
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package org.ow2.chameleon.metric;
 
 import org.ow2.chameleon.metric.converters.ConverterRegistry;
 import org.ow2.chameleon.metric.systems.SI;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.TreeMap;
 
 /**
  * The entry point of the metric-service system.
  * It stored all system of units and the converter registry.
  * <p/>
  * It must be a singleton. On OSGi, do not use this class directly, use the exposed metric service.
  */
 public class MetricService {
 
     /**
      * Stores the unique metric-service singleton.
      */
     private static MetricService singleton;
     private TreeMap<String, SystemOfUnits> systems;
     private ConverterRegistry registry;
 
     protected MetricService() {
         systems = new TreeMap<String, SystemOfUnits>();
         registry = new ConverterRegistry();
     }
 
     public static MetricService getInstance() {
         synchronized (MetricService.class) {
             if (singleton == null) {
                 singleton = new MetricService();
             }
             return singleton;
         }
     }
 
     public synchronized List<SystemOfUnits> getSystemsOfUnits() {
         if (systems.isEmpty()) {
             initialize();
         }
         return new ArrayList<SystemOfUnits>(systems.values());
     }
 
     private void initialize() {
         SI si = new SI();
         systems.put(si.getName(), si);
     }
 
     public synchronized SystemOfUnits getSystemOfUnits(String name) {
         if (systems.isEmpty()) {
             initialize();
         }
         return systems.get(name);
     }
 
     public synchronized void addSystemOfUnits(SystemOfUnits system) {
         if (systems.isEmpty()) {
             initialize();
         }
         if (!systems.containsKey(system.getName())) {
             systems.put(system.getName(), system);
         }
     }
 
     public synchronized void removeSystemOfUnits(SystemOfUnits system) {
        systems.remove(system);
     }
 
     public ConverterRegistry getConverterRegistry() {
         return registry;
     }
 }
