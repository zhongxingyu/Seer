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
 package org.apache.myfaces.extensions.scripting.loaders.groovy;
 
import org.apache.myfaces.extensions.scripting.groovyloader.core.StandardGroovyReloadingStrategy;
 import org.apache.myfaces.extensions.scripting.api.*;
 import org.apache.myfaces.extensions.scripting.core.util.Cast;
 import org.apache.myfaces.extensions.scripting.core.util.ClassUtils;
 import org.apache.myfaces.extensions.scripting.core.util.ReflectUtil;
 import org.apache.myfaces.extensions.scripting.loaders.groovy.compiler.GroovyCompilerFacade;
 
 import javax.servlet.ServletContext;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * A standard groovy weaver which isolates the weaving behavior
  */
 public class GroovyScriptingWeaver extends BaseWeaver {
 
     org.apache.myfaces.extensions.scripting.loaders.groovy.DynamicClassIdentifier _identifier = new org.apache.myfaces.extensions.scripting.loaders.groovy.DynamicClassIdentifier();
 
     final Logger _logger = Logger.getLogger(GroovyScriptingWeaver.class.getName());
 
     /**
      * helper to allow initial compiler classpath scanning
      *
      * @param servletContext servlet context to be passed down
      */
     @SuppressWarnings("unused")
     public GroovyScriptingWeaver(ServletContext servletContext) {
         super(ScriptingConst.GROOVY_FILE_ENDING, ScriptingConst.ENGINE_TYPE_JSF_GROOVY);
         init();
 
     }
 
     public GroovyScriptingWeaver() {
         super(ScriptingConst.FILE_EXTENSION_GROOVY, ScriptingConst.ENGINE_TYPE_JSF_GROOVY);
         init();
     }
 
     private void init() {
         //init classpath removed we can resolve that over the
         //url classloader at the time myfaces is initialized
         try {
             Class scanner = ClassUtils.getContextClassLoader().loadClass("org.apache.myfaces.extensions.scripting.jsf2.annotation.GenericAnnotationScanner");
             this._annotationScanner = (ClassScanner) ReflectUtil.instantiate(scanner, new Cast(ScriptingWeaver.class, this));
 
         } catch (ClassNotFoundException e) {
             //we do nothing here
             //generic annotation scanner can be missing in jsf1 environments
             _logger.log(Level.FINER, "", e);
         }
 
         this._dependencyScanner = new GroovyDependencyScanner(this);
         this._reloadingStrategy = new StandardGroovyReloadingStrategy();
         this._reloadingStrategy.setWeaver(this);
     }
 
     protected String getLoadingInfo(String file) {
         return "[EXT-SCRIPTING] Loading Groovy file:" + file;
     }
 
     public boolean isDynamic(Class clazz) {
         return _identifier.isDynamic(clazz);  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     protected DynamicCompiler instantiateCompiler() {
         return new GroovyCompilerFacade();
     }
 
     /**
      * checks outside of the request scope for changes and taints the corresponding engine
      */
     public void scanForAddedClasses() {
         _dependencyScanner.scanAndMarkChange();
     }
 
 }
