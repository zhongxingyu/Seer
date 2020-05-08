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
 package org.apache.myfaces.scripting.jsf2.resources;
 
 import org.apache.myfaces.application.DefaultResourceHandlerSupport;
 import org.apache.myfaces.application.ResourceHandlerSupport;
 import org.apache.myfaces.resource.ClassLoaderResourceLoader;
 import org.apache.myfaces.resource.ExternalContextResourceLoader;
 import org.apache.myfaces.resource.ResourceLoader;
 
 import javax.faces.context.FacesContext;
 
 /**
  * impl specific handler support which attaches
  * the source resource loader upfront before
  * checking the other ones
  * <p/>
  * a delegate is used to limit the binding between
  * the "parent" class and the child class
  * in this case it simply makes sense to avoid any internal sideeffects
 * between getResourceLoaders and the other methods (there arent any in the
 * current implementation, but we cannot rely on it fully for future implementations)
  */
 public class SourceResourceHandlerSupport implements ResourceHandlerSupport {
 
     private ResourceLoader[] _supportResourceLoaders;
     private ResourceHandlerSupport _defaultSupport = new DefaultResourceHandlerSupport();
 
     @Override
     /**
      * Delivers a list of resource loaders in a binding order
      * of the resource lookup algorithms
      *
      * @return a list of resource loaders with following order,
      *              <ul>
      *                  <li>source lookup paths if present</li>
      *                  <li>/resources directory</li>
      *                  <li>META-INF/resources directory</li>
      *              </ul>
      */
     public ResourceLoader[] getResourceLoaders() {
         if (_supportResourceLoaders == null) {
             //The ExternalContextResourceLoader has precedence over
             //ClassLoaderResourceLoader, so it goes first.
             _supportResourceLoaders = new ResourceLoader[]{
                     new SourceResourceLoader("/resources"),
                     new ExternalContextResourceLoader("/resources"),
                     new ClassLoaderResourceLoader("META-INF/resources")
             };
         }
         return _supportResourceLoaders;
     }
 
     public String calculateResourceBasePath(FacesContext facesContext) {
         return _defaultSupport.calculateResourceBasePath(facesContext);
     }
 
     public boolean isExtensionMapping() {
         return _defaultSupport.isExtensionMapping();
     }
 
     public String getMapping() {
         return _defaultSupport.getMapping();
     }
 
     public long getStartupTime() {
         return _defaultSupport.getStartupTime();
     }
 
     public long getMaxTimeExpires() {
         return _defaultSupport.getMaxTimeExpires();
     }
 }
