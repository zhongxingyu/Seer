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
  * interface for a bean handler
  * which in the long run will allow
  * to hook different frameworks into the core
  * (aka Mojarra, MyFaces)
  */
 public interface BeanHandler {
 
     /**
      * scans all bean dependencies according to
      * their IoC information stored by the runtime
     * (in our case the MyFaces runtime config)
      * and adds those into our backward referencing dependency map
      * to add further dependency information on IoC level
      * (we can have IoC dependencies which are bound by object
      * types, this is a corner case but it still can happen)
      */
     public void scanDependencies();
 
     /**
      * refreshes all managed beans,
      * Application, Session,Request and Custom beans
      * <p/>
      * internally a check is performed whether the refresh has to be done or not
      */
     public void refreshAllManagedBeans();
 
     /**
      * refreshes all personal scoped beans (aka beans which
      * have an assumed lifecycle <= session)
      * <p/>
      * This is needed for multiuser purposes because if one user alters some beans
      * other users have to drop their non application scoped beans as well!
      * <p/>
      * internally a check is performed whether the refresh has to be performed or not
      */
     public void personalScopeRefresh();
 }
