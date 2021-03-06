 /*
  * Copyright 2010-2011 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.codehaus.griffon.runtime.core;
 
 import griffon.core.GriffonMvcArtifact;
 
 import java.util.Map;
 import java.util.List;
 import java.util.Collections;
 
 import groovy.lang.Closure;
 
import griffon.util.GriffonExceptionHandler;
 import org.codehaus.griffon.runtime.util.GriffonApplicationHelper;
 
 /**
  * Base implementation of the GriffonMvcArtifact interface.
  *
  * @author Andres Almiray
  *
  * @since 0.9.1
  */
 public abstract class AbstractGriffonMvcArtifact extends AbstractGriffonArtifact implements GriffonMvcArtifact {
     public void mvcGroupInit(Map<String, Object> args) {
         // empty
     }
 
     public void mvcGroupDestroy() {
         // empty
     }
 
     public Map<String, ?> buildMVCGroup(String mvcType) {
         return GriffonApplicationHelper.buildMVCGroup(getApp(), Collections.emptyMap(), mvcType, mvcType);
     }
 
     public Map<String, ?> buildMVCGroup(String mvcType, String mvcName) {
         return GriffonApplicationHelper.buildMVCGroup(getApp(), Collections.emptyMap(), mvcType, mvcName);
     }
 
     public Map<String, ?> buildMVCGroup(Map<String, Object> args, String mvcType) {
         return GriffonApplicationHelper.buildMVCGroup(getApp(), args, mvcType, mvcType);
     }
 
     public Map<String, ?> buildMVCGroup(Map<String, Object> args, String mvcType, String mvcName) {
         return GriffonApplicationHelper.buildMVCGroup(getApp(), args, mvcType, mvcName);
     }
 
     public List<?> createMVCGroup(String mvcType) {
         return (List<?>) GriffonApplicationHelper.createMVCGroup(getApp(), mvcType);
     }
 
     public List<?> createMVCGroup(Map<String, Object> args, String mvcType) {
         return (List<?>) GriffonApplicationHelper.createMVCGroup(getApp(), args, mvcType);
     }
 
     public List<?> createMVCGroup(String mvcType, Map<String, Object> args) {
         return (List<?>) GriffonApplicationHelper.createMVCGroup(getApp(), args, mvcType);
     }
 
     public List<?> createMVCGroup(String mvcType, String mvcName) {
         return (List<?>) GriffonApplicationHelper.createMVCGroup(getApp(), mvcType, mvcName);
     }
 
     public List<?> createMVCGroup(Map<String, Object> args, String mvcType, String mvcName) {
         return (List<?>) GriffonApplicationHelper.createMVCGroup(getApp(), args, mvcType, mvcName);
     }
 
     public List<?> createMVCGroup(String mvcType, String mvcName, Map<String, Object> args) {
         return (List<?>) GriffonApplicationHelper.createMVCGroup(getApp(), args, mvcType, mvcName);
     }
 
     public void destroyMVCGroup(String mvcName) {
         GriffonApplicationHelper.destroyMVCGroup(getApp(), mvcName);
     }
 
     public void withMVCGroup(String mvcType, Closure handler) {
         withMVCGroup(mvcType, mvcType, Collections.<String, Object>emptyMap(), handler);
     }
 
     public void withMVCGroup(String mvcType, String mvcName, Closure handler) {
         withMVCGroup(mvcType, mvcName, Collections.<String, Object>emptyMap(), handler);
     }
 
     public void withMVCGroup(String mvcType, Map<String, Object> args, Closure handler) {
         withMVCGroup(mvcType, mvcType, args, handler);
     }
 
     public void withMVCGroup(String mvcType, String mvcName, Map<String, Object> args, Closure handler) {
         try {
             List<?> group = createMVCGroup(mvcType, mvcName, args);
             handler.call(group.toArray(new Object[3]));
         } finally {
             try {
                 destroyMVCGroup(mvcName);
             } catch(Exception x) {
                if(getLog().isWarnEnabled()) getLog().warn("Could not destroy group ["+mvcName+"] of type "+mvcType, GriffonExceptionHandler.sanitize(x));
             }
         }
     }
 }
