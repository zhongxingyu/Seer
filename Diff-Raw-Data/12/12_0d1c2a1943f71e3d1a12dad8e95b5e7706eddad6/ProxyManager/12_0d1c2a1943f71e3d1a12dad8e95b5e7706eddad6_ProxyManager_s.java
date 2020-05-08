 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.web.model;
 
 import java.lang.ref.Reference;
 import java.lang.ref.SoftReference;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.management.MBeanServerConnection;
 import javax.management.MalformedObjectNameException;
 import javax.management.ObjectName;
 
 import org.springframework.jmx.access.MBeanProxyFactoryBean;
 
 public class ProxyManager {
 
     private final MBeanServerConnection server;
     private final Map<ObjectName, Reference<Object>> proxies = new ConcurrentHashMap<ObjectName, Reference<Object>>();
 
     public ProxyManager(MBeanServerConnection server) {
         this.server = server;
     }
 
     @SuppressWarnings("unchecked")
     public<T> T getProxy(ObjectName name, Class<T> type) {
         Reference r = proxies.get(name);
         T proxy = (r != null) ? (T) r.get() : null;
         if (proxy == null) {
             MBeanProxyFactoryBean factory = new MBeanProxyFactoryBean();
             factory.setServer(server);
             try {
                factory.setObjectName(name.toString());
             } catch (MalformedObjectNameException e) {
                 throw new IllegalStateException(e);
             }
             factory.setProxyInterface(type);
             factory.setUseStrictCasing(false);
             factory.afterPropertiesSet();
             proxy = (T) factory.getObject();
             proxies.put(name, new SoftReference<Object>(proxy));
         }
         return proxy;
     }
     
 }
