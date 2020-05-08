 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.myfaces.scripting.servlet;
 
 import org.apache.myfaces.groovyloader.core.GroovyWeaver;
 import org.apache.myfaces.scripting.api.ScriptingWeaver;
 import org.apache.myfaces.scripting.core.util.ProxyUtils;
 import org.apache.myfaces.scripting.core.util.ScriptingWeaverHolder;
 import org.apache.myfaces.shared_impl.util.ClassLoaderExtension;
 import org.apache.myfaces.javaloader.core.JavaScriptingWeaver;
 import org.apache.commons.lang.StringUtils;
 
 import javax.servlet.ServletContext;
 
 /**
  * a custom chainloader which adds a groovy loading
  * facility to our myfaces loading plugin system
  *
  * @author Werner Punz
  */
 public class CustomChainLoader extends ClassLoaderExtension {
 
     /*
        * servlet context init var for additional chain loader paths which have
        * higher priority than the default ones 
        */
     static String CUSTOM_LOADER_PATHS = "org.apache.myfaces.scripting.groovy.LOADER_PATHS";
     static String CUSTOM_JAVA_LOADER_PATHS = "org.apache.myfaces.scripting.java.LOADER_PATHS";
 
 
     String classRoot = "";
     String scriptingRoot = "";
     ScriptingWeaver scriptingWeaver = null;
     private static final String GROOVY_SOURCE_ROOT = "/WEB-INF/groovy/";
     private static final String JAVA_SOURCE_ROOT = "/WEB-INF/java/";
 
     public CustomChainLoader(ServletContext servletContext) {
         ScriptingWeaver groovyWeaver = new GroovyWeaver();
         ScriptingWeaver javaWeaver = new JavaScriptingWeaver();
 
         //this.scriptingWeaver = new GroovyWeaver();
 
         String contextRoot = servletContext.getRealPath(GROOVY_SOURCE_ROOT);
 
         contextRoot = contextRoot.trim();
         //TODO still needed?
         if (!contextRoot.endsWith("/") && !contextRoot.endsWith("\\"))
             contextRoot += "/";
         scriptingRoot = contextRoot;
 
         setupScriptingPatsh(servletContext, groovyWeaver, CUSTOM_JAVA_LOADER_PATHS);
 
         contextRoot = servletContext.getRealPath(JAVA_SOURCE_ROOT);
         contextRoot = contextRoot.trim();
 
         scriptingRoot = contextRoot;
         setupScriptingPatsh(servletContext, javaWeaver, CUSTOM_JAVA_LOADER_PATHS);
 
 
         this.scriptingWeaver = new ScriptingWeaverHolder(groovyWeaver, javaWeaver);
         //we have to store it because our filter
         //does not trigger upon initialisation
         ProxyUtils.setWeaver(this.scriptingWeaver);
     }
 
     private void setupScriptingPatsh(ServletContext servletContext, ScriptingWeaver weaver, String initParams) {
         String additionalLoaderPaths;
         additionalLoaderPaths = servletContext.getInitParameter(initParams);
         appendAdditionalPaths(additionalLoaderPaths, weaver);
         weaver.appendCustomScriptPath(scriptingRoot);
         weaver.appendCustomScriptPath(classRoot);
     }
 
     private void appendAdditionalPaths(String additionalLoaderPaths, ScriptingWeaver workWeaver) {
         if(!StringUtils.isBlank(additionalLoaderPaths)) {
             String [] additionalPaths = additionalLoaderPaths.split(",");
             for(String path: additionalPaths) {
                  workWeaver.appendCustomScriptPath(path);
             }
         }
     }
 
     public Class forName(String name) {
 
         if (name.startsWith("java.")) /*the entire java namespace is reserved so no use to do a specific classloading check here*/
             return null;
         if (name.startsWith("javax.")) /*the entire java namespace is reserved so no use to do a specific classloading check here*/
             return null;
         else if (name.startsWith("com.sun")) /*internal java specific namespace*/
             return null;
 
         return scriptingWeaver.loadScriptingClassFromName(name);
     }
 
     public ScriptingWeaver getScriptingWeaver() {
         return scriptingWeaver;
     }
 
     public void setScriptingWeaver(ScriptingWeaver scriptingWeaver) {
         this.scriptingWeaver = scriptingWeaver;
     }
 
 }
