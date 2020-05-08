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
 package org.apache.servicemix.jbi.framework;
 
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.management.JMException;
 import javax.management.MBeanAttributeInfo;
 import javax.management.MBeanOperationInfo;
 
 import org.apache.servicemix.jbi.deployment.ClassPath;
 import org.apache.servicemix.jbi.management.AttributeInfoHelper;
 import org.apache.servicemix.jbi.management.MBeanInfoProvider;
import org.apache.xbean.classloader.JarFileClassLoader;
 
 public class SharedLibrary implements SharedLibraryMBean, MBeanInfoProvider {
 
     private org.apache.servicemix.jbi.deployment.SharedLibrary library;
     private File installationDir;
     private ClassLoader classLoader;
     
     public SharedLibrary(org.apache.servicemix.jbi.deployment.SharedLibrary library,
                          File installationDir) {
         this.library = library;
         this.installationDir = installationDir;
         this.classLoader = createClassLoader();
     }
 
     /**
      * @return the library
      */
     public org.apache.servicemix.jbi.deployment.SharedLibrary getLibrary() {
         return library;
     }
     
     public ClassLoader getClassLoader() {
         return this.classLoader;
     }
     
     protected ClassLoader createClassLoader() {
         boolean parentFirst = library.isParentFirstClassLoaderDelegation();
         // Make the current ClassLoader the parent
         ClassLoader parent = getClass().getClassLoader();       
         
         ClassPath cp = library.getSharedLibraryClassPath();
         String[] classPathNames = cp.getPathElements();
         URL[] urls = new URL[classPathNames.length];
         for (int i = 0; i < classPathNames.length; i++) {
             File file = new File(installationDir, classPathNames[i]);
             try {
                 urls[i] = file.toURL();
             } catch (MalformedURLException e) {
                 throw new IllegalArgumentException(classPathNames[i], e);
             }
         }
         return new JarFileClassLoader(
                         getName(), 
                         urls, 
                         parent, 
                         !parentFirst, 
                         new String[0], 
                         new String[] { "java.", "javax." });
     }
 
     public String getDescription() {
         return library.getIdentification().getDescription();
     }
 
     public String getName() {
         return library.getIdentification().getName();
     }
 
     public Object getObjectToManage() {
         return this;
     }
 
     public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
         AttributeInfoHelper helper = new AttributeInfoHelper();
         helper.addAttribute(getObjectToManage(), "name", "name of the service unit");
         helper.addAttribute(getObjectToManage(), "description", "description of the service unit");
         return helper.getAttributeInfos();
     }
 
     public MBeanOperationInfo[] getOperationInfos() throws JMException {
         return null;
     }
 
     public String getSubType() {
         return null;
     }
 
     public String getType() {
         return "SharedLibrary";
     }
 
     public void setPropertyChangeListener(PropertyChangeListener l) {
         // We do not fire property events, so need to keep
         // a reference
     }
 
 }
