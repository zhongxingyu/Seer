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
 package org.apache.myfaces.scripting.core;
 
 import org.apache.myfaces.scripting.api.ScriptingConst;
 import org.apache.myfaces.scripting.api.ScriptingWeaver;
 import org.apache.myfaces.scripting.core.util.ProxyUtils;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author werpu
  *         <p/>
  *         Facade which holds multiple weavers
  *         and implements a chain of responsibility pattern
  *         on them
  */
 public class CoreWeaver implements Serializable, ScriptingWeaver {
 
     List<ScriptingWeaver> _weavers = new ArrayList<ScriptingWeaver>();
 
     public CoreWeaver(ScriptingWeaver... weavers) {
         for (ScriptingWeaver weaver : weavers) {
             _weavers.add(weaver);
         }
     }
 
     public void appendCustomScriptPath(String scriptPaths) {
         throw new RuntimeException("Method not supported from this facade");
     }
 
     public Object reloadScriptingInstance(Object o) {
         if (o.getClass().getName().contains("TestBean2")) {
             System.out.println("Debugpoint found");
         }
 
         for (ScriptingWeaver weaver : _weavers) {
             if (weaver.isDynamic(o.getClass())) {
                 return weaver.reloadScriptingInstance(o);
             }
         }
         return o;
 
     }
 
     public Class reloadScriptingClass(Class aclass) {
         if (aclass.getName().contains("TestBean2")) {
             System.out.println("Debugpoint found");
         }
 
         for (ScriptingWeaver weaver : _weavers) {
             if (weaver.isDynamic(aclass)) {
                 return weaver.reloadScriptingClass(aclass);
             }
         }
         return aclass;
 
     }
 
     public Class loadScriptingClassFromName(String className) {
         for (ScriptingWeaver weaver : _weavers) {
             Class retVal = weaver.loadScriptingClassFromName(className);
             if (retVal != null) {
                 return retVal;
             }
         }
         return null;
     }
 
     public int getScriptingEngine() {
         return ScriptingConst.ENGINE_TYPE_ALL;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public boolean isDynamic(Class clazz) {
         for (ScriptingWeaver weaver : _weavers) {
             if (weaver.isDynamic(clazz)) {
                 return true;
             }
         }
         return false;
     }
 
    public ScriptingWeaver getWeaverInstance(Class weaverClass) {
         for (ScriptingWeaver weaver : _weavers) {
            ScriptingWeaver retVal = weaver.getWeaverInstance(weaverClass);
            if(retVal != null) {
                return retVal;
            }
         }
         return null;   
    }
 }
 
