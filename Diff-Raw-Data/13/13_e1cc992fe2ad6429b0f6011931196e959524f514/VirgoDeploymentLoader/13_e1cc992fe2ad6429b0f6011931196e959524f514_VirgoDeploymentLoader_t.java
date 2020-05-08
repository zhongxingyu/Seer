 /*******************************************************************************
  * Copyright (c) 2012 SAP AG
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   SAP AG - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.web.enterprise.openejb.deployer;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Collections;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import org.apache.openejb.OpenEJBException;
 import org.apache.openejb.config.AppModule;
 import org.apache.openejb.config.DeploymentLoader;
 
 /**
  * 
  * The purpose of this class is to enable proper recognition of packed web apps that use EJBs during deployment
  * <p />
  *
  */
 public class VirgoDeploymentLoader extends DeploymentLoader {
     
     private static final String VIRGO_ROOT_APPLICATION_RESERVED_MODULE_ID = "virgoRootApplicationReservedModuleID";
     private final String webContextPath;
     
     public VirgoDeploymentLoader(String webContextPath) {
         super();
         this.webContextPath = webContextPath;
     }
 
     @Override
     public AppModule load(File arg0) throws OpenEJBException {
         //Sets the web context path as the moduleId, leaving the original logic unchanged
         AppModule result = super.load(arg0);
         result.setModuleId(createModuleIDFromWebContextPath());
         return result;
     }
     
     private String createModuleIDFromWebContextPath() {
         if (this.webContextPath.equals("")) {
           return VIRGO_ROOT_APPLICATION_RESERVED_MODULE_ID;
         }
         // remove the slash at the beginning of each webContextPath
         return this.webContextPath.substring(1);
       }
     
     @Override
     protected String getContextRoot() {
         return webContextPath;
     }
 
     @Override
     protected Map<String, URL> getWebDescriptors(File warFile) throws IOException {
         //Fixes a bug in OpenEjb that prevents recognising web modules when deploying packed web apps
         Map<String, URL> descriptors = new TreeMap<String, URL>();
         if (warFile.isFile()) {
             URL jarURL = new URL("jar", "", -1, warFile.toURI().toURL() + "!/");
            JarFile jarFile = null;
             try {
                jarFile = new JarFile(warFile);
                 for (JarEntry entry : Collections.list(jarFile.entries())) {
                     String entryName = entry.getName();
                     if (!entry.isDirectory() && entryName.startsWith("WEB-INF/") && entryName.indexOf('/', "WEB-INF".length()) > 0) {
                         descriptors.put(entryName, new URL(jarURL, entry.getName()));
                     }
                 }
             } catch (IOException e) {
                 // most likely an invalid jar file
            } finally {
            	try {
            		if (jarFile != null) {
            			jarFile.close();
            		}
            	} catch (IOException e) {
            		// do nothing
            	}
             }
         }
         descriptors.putAll(super.getWebDescriptors(warFile));
         return descriptors;
     }
 }
