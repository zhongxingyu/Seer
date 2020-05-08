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
 package org.apache.myfaces.scripting.api;
 
 /**
  * @author werpu
  * @date: 15.08.2009
  * <p/>
  * Central interface to the scripting layer
  * this class is a weaver which allows to trigger
  * the scripting layer in various situations
  * of the JSF interception points
  *
  * The scripting weaver replaces the classloader for those instances
  * because custom classloaders are inherently problematic in web containers
  */
 public interface ScriptingWeaver  {
 
   /**
      * appends a custom script search path to the original one
       * @param scriptPaths
      */
    public void appendCustomScriptPath(String  scriptPaths); 
 
 
     /**
      * @param o
      * @return reloads an existing objects with its attributes
      *         and assigns the reloaded class to the new object
      *         <p/>
      *         note, the new object must not be the same as the original one
      *         it can be a shallow clone with a new class instead
      */
     public Object reloadScriptingInstance(Object o);
 
     /**
      * reloads an existing class if needed
      * if no new class exists the original class is given back
      *
      * @param aclass the class which is likely to be reloaded
      * @return   a new class or the same if no refresh has to be performed
      */
     public Class reloadScriptingClass(Class aclass);
 
     /**
      *  loads a scripting class from a given className
      * note, this method probably will be dropped in the long
      * run
      * @param className the classname including the package
      * @return  a class instance of the file
      */
     public Class loadScriptingClassFromName(String className);
 
 
     /**
      * returns the engine type for this weaver
      * 
      * @return
      */
     public int getScriptingEngine();
 
 
     /**
      * checks wether a given class can be reloaded
      * from this weaver or not
      *
      * @param clazz
      * @return
      */
     public boolean isDynamic(Class clazz);
 
 }
