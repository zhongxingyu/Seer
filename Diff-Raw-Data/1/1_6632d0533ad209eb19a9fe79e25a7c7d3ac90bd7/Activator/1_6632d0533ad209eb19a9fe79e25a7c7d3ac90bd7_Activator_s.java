 /**
  * Licensed to the Austrian Association for Software Tool Integration (AASTI)
  * under one or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information regarding copyright
  * ownership. The AASTI licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.openengsb.labs.delegation.service.internal;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Enumeration;
 
 import org.openengsb.labs.delegation.service.Constants;
 import org.openengsb.labs.delegation.service.DelegationUtil;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleEvent;
 import org.osgi.framework.BundleListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Activator implements BundleActivator {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);
 
     @Override
     public void start(BundleContext context) {
         BundleListener bundleListener = new BundleListener() {
             @Override
             public void bundleChanged(BundleEvent event) {
                 if (event.getType() == BundleEvent.STARTED) {
                     handleBundleInstall(event.getBundle());
                 }
             }
         };
         context.addBundleListener(bundleListener);
         for (Bundle b : context.getBundles()) {
             if (b.getState() == Bundle.ACTIVE) {
                 handleBundleInstall(b);
             }
         }
     }
 
     private synchronized void handleBundleInstall(Bundle b) {
         LOGGER.info("injecting ClassProvider-Service into bundle {}.", b.getSymbolicName());
         Enumeration<String> keys = b.getHeaders().keys();
         while (keys.hasMoreElements()) {
             String key = keys.nextElement();
             if (key.equals(Constants.PROVIDED_CLASSES)) {
                 String providedClassesString = (String) b.getHeaders().get(key);
                 Collection<String> providedClasses = parseProvidedClasses(providedClassesString);
                 DelegationUtil.registerClassProviderForBundle(b, providedClasses);
             } else if (key.startsWith(Constants.PROVIDED_CLASSES)) {
                 String context = key.replaceFirst(Constants.PROVIDED_CLASSES + "\\-", "");
                 String providedClassesString = (String) b.getHeaders().get(key);
                 Collection<String> providedClasses = parseProvidedClasses(providedClassesString);
                 DelegationUtil.registerClassProviderForBundle(b, providedClasses, context);
             }
         }
     }
 
     private Collection<String> parseProvidedClasses(String providedClassesString) {
         String[] providedClassesArray = providedClassesString.split(",");
         Collection<String> providedClassesList = new ArrayList<String>();
         for (String p : providedClassesArray) {
             providedClassesList.add(p.trim());
         }
         return providedClassesList;
     }
 
     @Override
     public void stop(BundleContext context) throws Exception {
         // TODO Auto-generated method stub
 
     }
 
 }
