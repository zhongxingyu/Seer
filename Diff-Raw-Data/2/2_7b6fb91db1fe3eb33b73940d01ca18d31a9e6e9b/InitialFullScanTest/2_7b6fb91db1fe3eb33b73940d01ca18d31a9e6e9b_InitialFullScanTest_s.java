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
 
 package org.apache.myfaces.extensions.scripting.core.context;
 
 import org.apache.myfaces.extensions.scripting.core.api.ScriptingConst;
 import org.apache.myfaces.extensions.scripting.core.api.WeavingContext;
 import org.apache.myfaces.extensions.scripting.core.common.util.FileUtils;
 import org.apache.myfaces.extensions.scripting.core.engine.FactoryEngines;
 import org.apache.myfaces.extensions.scripting.core.engine.api.ScriptingEngine;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.Enumeration;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 /**
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  */
 
 public class InitialFullScanTest
 {
     FactoryEngines factory = null;
         
         
         @Before
         public void init() throws Exception {
             factory = FactoryEngines.getInstance();
             factory.init();
         }
     
         @Test
         public void testInitialFullScan() {
             try
             {
                 ScriptingEngine javaEngine = factory.getEngine(ScriptingConst.ENGINE_TYPE_JSF_JAVA);
                 ScriptingEngine groovyEngine = factory.getEngine(ScriptingConst.ENGINE_TYPE_JSF_GROOVY);
                 ScriptingEngine scalaEngine = factory.getEngine(ScriptingConst.ENGINE_TYPE_JSF_SCALA);
                 ScriptingEngine jrubyEngine = factory.getEngine(ScriptingConst.ENGINE_TYPE_JSF_JRUBY);
 
                 ClassLoader loader = this.getClass().getClassLoader();
                 String canonicalPackageName = this.getClass().getPackage().getName().replaceAll("\\.", FileUtils.getFileSeparatorForRegex());                Enumeration<URL> enumeration = loader.getResources(canonicalPackageName);
                 javaEngine.getSourcePaths().clear();
                 groovyEngine.getSourcePaths().clear();
                 //TODO source not binary dirs
                 while(enumeration.hasMoreElements()) {
                     URL currentDir = enumeration.nextElement();
                     String currentDirStr = currentDir.getFile();
                     currentDirStr = currentDirStr.replaceAll("%5c", FileUtils.getFileSeparatorForRegex());
                     currentDirStr = currentDirStr.replaceAll("target/test\\-classes",
                             "src/main/java");
                     currentDirStr = currentDirStr.replaceAll("target/classes",
                                                 "src/main/java");
                    currentDirStr = currentDirStr.replaceAll("context", "api");
                     javaEngine.getSourcePaths().add(currentDirStr);
                     groovyEngine.getSourcePaths().add(currentDirStr);
                     scalaEngine.getSourcePaths().add(currentDirStr);
                 }
                 //we now scan for the files
                 WeavingContext.getInstance().fullScan();
                 assertTrue("watched resources must have java files", javaEngine.getWatchedResources().size() > 0);
                 javaEngine.getSourcePaths().clear();;
                 groovyEngine.getSourcePaths().clear();
                 scalaEngine.getSourcePaths().clear();
                 jrubyEngine.getSourcePaths().clear();
 
             }
             catch (IOException e)
             {
                 fail(e.getMessage());
             }
 
         }
 }
