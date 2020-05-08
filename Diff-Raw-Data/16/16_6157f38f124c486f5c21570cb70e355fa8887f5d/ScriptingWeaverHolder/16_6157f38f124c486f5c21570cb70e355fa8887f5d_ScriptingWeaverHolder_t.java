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
 package org.apache.myfaces.scripting.core.util;
 
 import org.apache.myfaces.scripting.api.ScriptingConst;
 import org.apache.myfaces.scripting.api.ScriptingWeaver;
 
 /**
  * @author werpu
  *
  * Facade which holds multiple weavers
  * and implements a chain of responsibility pattern
  * on them
  */
 public class ScriptingWeaverHolder implements ScriptingWeaver {
 
     ScriptingWeaver _groovyWeaver = null;
     ScriptingWeaver _javaWeaver = null;
 
     public ScriptingWeaverHolder(ScriptingWeaver ... weavers) {
         _groovyWeaver = weavers[0];
         _javaWeaver = weavers[1];
 
     }
 
     @Override
     public void appendCustomScriptPath(String scriptPaths) {
         throw new RuntimeException("Method not supported from this facade");
     }
 
     @Override
     public Object reloadScriptingInstance(Object o) {
         int objectType = ProxyUtils.getEngineType(o.getClass());
         switch (objectType) {
             case ScriptingConst.ENGINE_TYPE_GROOVY:
                 return this._groovyWeaver.reloadScriptingInstance(o);
            case ScriptingConst.ENGINE_TYPE_JAVA: //java
                 return this._javaWeaver.reloadScriptingInstance(o);
            default: return o;
         }
     }
 
     @Override
     public Class reloadScriptingClass(Class aclass) {
         int objectType = ProxyUtils.getEngineType(aclass);
         switch (objectType) {
             case ScriptingConst.ENGINE_TYPE_GROOVY:
                 return this._groovyWeaver.reloadScriptingClass(aclass);
            case ScriptingConst.ENGINE_TYPE_JAVA: //java
                 return this._javaWeaver.reloadScriptingClass(aclass);
            default: return aclass;
         }
     }
 
     @Override
     public Class loadScriptingClassFromName(String className) {
         //we try to load from the chain, we cannot determine the engine type upfront here
         Class retVal = this._groovyWeaver.loadScriptingClassFromName(className);
         if (retVal == null) {
             return this._javaWeaver.loadScriptingClassFromName(className);
         }
         return retVal;
     }
 }
