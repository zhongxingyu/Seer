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
 
 package rewrite.org.apache.myfaces.extensions.scripting.engine;
 
 import rewrite.org.apache.myfaces.extensions.scripting.common.util.ClassUtils;
 import rewrite.org.apache.myfaces.extensions.scripting.context.Configuration;
 import rewrite.org.apache.myfaces.extensions.scripting.context.WeavingContext;
 import rewrite.org.apache.myfaces.extensions.scripting.engine.api.CompilationException;
 import rewrite.org.apache.myfaces.extensions.scripting.engine.api.ScriptingEngine;
 import rewrite.org.apache.myfaces.extensions.scripting.engine.compiler.JSR199Compiler;
 import rewrite.org.apache.myfaces.extensions.scripting.monitor.ClassResource;
 
 import javax.servlet.ServletContext;
 import java.io.File;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import static rewrite.org.apache.myfaces.extensions.scripting.common.ScriptingConst.*;
 
 /**
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  */
 
 public class EngineJava extends BaseEngine implements ScriptingEngine
 {
 
     Logger log = Logger.getLogger(this.getClass().getName());
 
     @Override
     public void init(ServletContext context)
     {
         initPaths(context, INIT_PARAM_CUSTOM_JAVA_LOADER_PATHS, JAVA_SOURCE_ROOT);
     }
 
     /**
      * full compile will be called cyclicly
      * from the startup and daemon thread
      */
     public void compile()
     {
         WeavingContext context = WeavingContext.getInstance();
         Configuration configuration = context.getConfiguration();
         JSR199Compiler compiler = new JSR199Compiler();
         File targetDir = configuration.getCompileTarget();
         Collection<String> sourceDirs = configuration.getSourceDirs(getEngineType());
         for (String sourceRoot : sourceDirs)
         {
             try
             {
                 compiler.compile(new File(sourceRoot), targetDir, ClassUtils.getContextClassLoader());
             }
             catch (CompilationException e)
             {
                 log.severe(e.getMessage());
                 e.printStackTrace();
             }
         }
     }
 
     public void scanDependencies()
     {
        log.info("[EXT-SCRIPTING] starting dependency scan");
         JavaDependencyScanner scanner = new JavaDependencyScanner();
         scanner.scanPaths();
        log.info("[EXT-SCRIPTING] ending dependency scan");
     }
 
     //-------------------------------------------------------------------------------------
 
     @Override
     public int getEngineType()
     {
         return ENGINE_TYPE_JSF_JAVA;
     }
     
     public String getEngineTypeAsStr() {
         return "Java";
     }
 
     @Override
     public String getFileEnding()
     {
         return "java";
     }
 
 }
