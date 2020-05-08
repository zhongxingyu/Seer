 /*
  * Copyright 2011 Google Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.jstestdriver.config;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 
 /**
  * The source of the configuration.
  *
  * @author Cory Smith (corbinrsmith@gmail.com) 
  */
 public interface ConfigurationSource {
 
   /**
   * @return The parent directory of the configuration, used as the default base path.
    */
   File getParentFile();
 
   /**
    * Parses the Configuration from the source.
    * @param basePath The base path for all the test resources.
    * @param configParser the parser of the configuration.
    * @return The parsed configuration object.
    * @throws FileNotFoundException If the configuration file does not exist.
    */
   Configuration parse(File basePath, ConfigurationParser configParser) throws FileNotFoundException;
 
   /** Returns the name of the current configuration. */
   String getName();
 }
