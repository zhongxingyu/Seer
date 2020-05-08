 /*
  *  Copyright 2006 The Apache Software Foundation
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package org.apache.geronimo.genesis.plugins.tools;
 
 import org.apache.geronimo.plugin.MojoSupport;
 
 import org.apache.commons.lang.SystemUtils;
 
 import org.apache.maven.plugin.MojoFailureException;
 
 /**
  * Forces the build to fail if the version of Java is not compatible.
  *
  * @goal require-java-version
  * @phase validate
  *
  * @version $Rev$ $Date$
  */
 public class RequireJavaVersionMojo
     extends MojoSupport
 {
     /**
      * Specify the required version of Java (1.1, 1.2, 1.3, 1.4, 1.5).
      *
      * Can specify a suffix of '+' to allow any version equal to or newer or '*'
      * to allow versions in the same group.
      *
      * For example, version=1.4+ would be allowed on a JDK 1.5 VM, version=1.5*
      * would allow any JDK 1.5, but not JDK 1.6.
      *
      * @parameter
      * @required
      */
     private String version = null;
     
     /**
      * Flag to skip the version check.
      *
      * @parameter default-value="false"
      */
     private boolean skip = false;
     
     protected void doExecute() throws Exception {
         if (skip) {
             log.warn("Skipping Java version check");
         }
         
         version = version.trim();
         
         if (version.endsWith("*")) {
             version = version.substring(0, version.length() - 1).trim();
             
             log.debug("Checking Java version is in the same group as: " + version);
             
             String tmp = SystemUtils.JAVA_VERSION_TRIMMED;
             if (!tmp.startsWith(version)) {
                 throw new MojoFailureException("This build requires Java version " + version + 
                     " or a greater version in the same group, found version: " + 
                     SystemUtils.JAVA_VERSION_FLOAT);
             }
         }
         else if (version.endsWith("+")) {
             version = version.substring(0, version.length() - 1).trim();
             
             log.debug("Checking Java version is greater than: " + version);
             
             float tmp = Float.parseFloat(version);
            if (tmp >= SystemUtils.JAVA_VERSION_FLOAT) {
                 throw new MojoFailureException("This build requires Java version " + version + 
                     " or greater, found version: " + SystemUtils.JAVA_VERSION_FLOAT);
             }
         }
         else {
             log.debug("Checking Java version is equal to: " + version);
             
             float tmp = Float.parseFloat(version);
             
             if (tmp != SystemUtils.JAVA_VERSION_FLOAT) {
                 throw new MojoFailureException("This build requires Java version " + version + 
                     ", found version: " + SystemUtils.JAVA_VERSION_FLOAT);
             }
         }
     }
 }
