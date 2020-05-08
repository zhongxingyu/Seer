 package fnug.config;
 
 import fnug.resource.Bundle;
 import fnug.resource.Resource;
 
 /*
  Copyright 2010 Martin Algesten
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  */
 
 /**
  * Default implementation of {@link BundleConfig}
  * 
  * @author Martin Algesten
  * 
  */
 public class DefaultBundleConfig implements BundleConfig {
 
     private Resource configResource;
     private String name;
     private String basePath;
     private String[] jsLintArgs;
     private int checkModifiedInterval;
     private String[] jsCompileArgs;
     private String[] files;
 
     /**
      * Constructs setting all configurations.
      * 
      * @param configResource
      *            Resource that built this instance.
      * @param name
      *            See {@link #name()}
      * @param basePath
      *            See {@link #basePath()}
      * @param jsLintArgs
     *            See {@link #jsLint()}
     * @param checkModified
      *            See {@link #checkModifiedInterval()}
      * @param jsCompileArgs
      *            See {@link #jsCompileArgs()}
      * @param files
      *            See {@link #files()}
      */
     public DefaultBundleConfig(Resource configResource, String name, String basePath, String[] jsLintArgs,
             int checkModifiedInterval, String[] jsCompileArgs, String[] files) {
         this.configResource = configResource;
         if (!Bundle.BUNDLE_ALLOWED_CHARS.matcher(name).matches()) {
             throw new IllegalArgumentException("Bundle name must match: " + Bundle.BUNDLE_ALLOWED_CHARS.toString());
         }
         this.name = name;
         this.basePath = basePath;
         this.jsLintArgs = jsLintArgs;
         this.checkModifiedInterval = checkModifiedInterval;
         this.jsCompileArgs = jsCompileArgs;
         this.files = files;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Resource configResource() {
         return configResource;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String name() {
         return name;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String basePath() {
         return basePath;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String[] jsLintArgs() {
         return jsLintArgs;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int checkModifiedInterval() {
         return checkModifiedInterval;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String[] jsCompileArgs() {
         return jsCompileArgs;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String[] files() {
         return files;
     }
 
 }
