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
 package org.apache.servicemix.jbi.deployment;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.servicemix.jbi.config.spring.XBeanProcessor;
 import org.apache.servicemix.jbi.util.FileUtil;
 import org.apache.xbean.spring.context.ResourceXmlApplicationContext;
 import org.springframework.core.io.UrlResource;
 
 /**
  * @version $Revision: 359151 $
  */
 public class DescriptorFactory {
 
     public static final String DESCRIPTOR_FILE = "META-INF/jbi.xml";
 
     private static Log log = LogFactory.getLog(DescriptorFactory.class);
 
     /**
      * Build a jbi descriptor from a file archive
      * 
      * @param descriptorFile
      *            path to the jbi descriptor, or to the root directory
      * @return the Descriptor object
      */
     public static Descriptor buildDescriptor(File descriptorFile) {
         if (descriptorFile.isDirectory()) {
             descriptorFile = new File(descriptorFile, DESCRIPTOR_FILE);
         }
         if (descriptorFile.isFile()) {
             try {
                 return buildDescriptor(descriptorFile.toURL());
             } catch (MalformedURLException e) {
                 throw new RuntimeException("There is a bug here...", e);
             }
         }
         return null;
     }
 
     /**
      * Build a jbi descriptor from the specified URL
      * 
      * @param url
      *            url to the jbi descriptor
      * @return the Descriptor object
      */
     public static Descriptor buildDescriptor(URL url) {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         try {
             Thread.currentThread().setContextClassLoader(DescriptorFactory.class.getClassLoader());
             ResourceXmlApplicationContext context = new ResourceXmlApplicationContext(
                             new UrlResource(url), 
                             Arrays.asList(new Object[] { new XBeanProcessor() }));
             Descriptor descriptor = (Descriptor) context.getBean("jbi");
             checkDescriptor(descriptor);
             return descriptor;
         } finally {
             Thread.currentThread().setContextClassLoader(cl);
         }
     }
 
     /**
      * Check validity of the JBI descriptor
      * 
      * @param descriptor
      *            the descriptor to check
      * @throws Exception
      *             if the descriptor is not valid
      */
     public static void checkDescriptor(Descriptor descriptor) {
         List violations = new ArrayList();
 
         if (descriptor.getVersion() != 1.0) {
            violations.add("JBI descriptor version should be set to '1.0' but is " + descriptor.getVersion());
         }
 
         if (descriptor.getComponent() != null) {
             checkComponent(violations, descriptor.getComponent());
         } else if (descriptor.getServiceAssembly() != null) {
             checkServiceAssembly(violations, descriptor.getServiceAssembly());
         } else if (descriptor.getServices() != null) {
             checkServiceUnit(violations, descriptor.getServices());
         } else if (descriptor.getSharedLibrary() != null) {
             checkSharedLibrary(violations, descriptor.getSharedLibrary());
         } else {
             violations.add("The jbi descriptor does not contain any informations");
         }
 
         if (violations.size() > 0) {
             throw new RuntimeException("The JBI descriptor is not valid, please correct these violations "
                             + violations.toString());
         }
     }
 
     /**
      * Checks that the component is valid
      * 
      * @param violations
      *            A list of violations that the check can add to
      * 
      * @param component
      *            The component descriptor that is being checked
      */
     private static void checkComponent(List violations, Component component) {
         if (component.getIdentification() == null) {
             violations.add("The component has not identification");
         } else {
             if (isBlank(component.getIdentification().getName())) {
                 violations.add("The component name is not set");
             }
         }
         if (component.getBootstrapClassName() == null) {
             violations.add("The component has not defined a boot-strap class name");
         }
         if (component.getBootstrapClassPath() == null || component.getBootstrapClassPath().getPathElements() == null) {
             violations.add("The component has not defined any boot-strap class path elements");
         }
     }
 
     /**
      * Checks that the service assembly is valid
      * 
      * @param violations
      *            A list of violations that the check can add to
      * 
      * @param serviceAssembly
      *            The service assembly descriptor that is being checked
      */
     private static void checkServiceAssembly(List violations, ServiceAssembly serviceAssembly) {
         if (serviceAssembly.getIdentification() == null) {
             violations.add("The service assembly has not identification");
         } else {
             if (isBlank(serviceAssembly.getIdentification().getName())) {
                violations.add("The service assembly name is not set"); 
             }
         }
     }
 
     /**
      * Checks that the service unit is valid
      * 
      * @param violations
      *            A list of violations that the check can add to
      * 
      * @param services
      *            The service unit descriptor that is being checked
      */
     private static void checkServiceUnit(List violations, Services services) {
         // TODO Auto-generated method stub
         
     }
 
     /**
      * Checks that the shared library is valid
      * 
      * @param violations
      *            A list of violations that the check can add to
      * 
      * @param sharedLibrary
      *            The shared library descriptor that is being checked
      */
     private static void checkSharedLibrary(List violations, SharedLibrary sharedLibrary) {
         if (sharedLibrary.getIdentification() == null) {
             violations.add("The shared library has not identification");
         } else {
             if (isBlank(sharedLibrary.getIdentification().getName())) {
                violations.add("The shared library name is not set"); 
             }
         }
     }
 
     /**
      * Retrieves the jbi descriptor as a string
      * 
      * @param descriptorFile
      *            path to the jbi descriptor, or to the root directory
      * @return the contents of the jbi descriptor
      */
     public static String getDescriptorAsText(File descriptorFile) {
         if (descriptorFile.isDirectory()) {
             descriptorFile = new File(descriptorFile, DESCRIPTOR_FILE);
         }
         if (descriptorFile.isFile()) {
             try {
                 ByteArrayOutputStream os = new ByteArrayOutputStream();
                 InputStream is = new FileInputStream(descriptorFile);
                 FileUtil.copyInputStream(is, os);
                 return os.toString();
             } catch (Exception e) {
                 log.debug("Error reading jbi descritor: " + descriptorFile, e);
             }
         }
         return null;
     }
 
     /**
      * <p>Checks if a String is whitespace, empty ("") or null.</p>
      *
      * <pre>
      * StringUtils.isBlank(null)      = true
      * StringUtils.isBlank("")        = true
      * StringUtils.isBlank(" ")       = true
      * StringUtils.isBlank("bob")     = false
      * StringUtils.isBlank("  bob  ") = false
      * </pre>
      *
      * @param str  the String to check, may be null
      * @return <code>true</code> if the String is null, empty or whitespace
      * 
      * Copied from org.apache.commons.lang.StringUtils#isBlanck
      */
     private static boolean isBlank(String str) {
         int strLen;
         if (str == null || (strLen = str.length()) == 0) {
             return true;
         }
         for (int i = 0; i < strLen; i++) {
             if ((Character.isWhitespace(str.charAt(i)) == false)) {
                 return false;
             }
         }
         return true;
     }
 
 }
