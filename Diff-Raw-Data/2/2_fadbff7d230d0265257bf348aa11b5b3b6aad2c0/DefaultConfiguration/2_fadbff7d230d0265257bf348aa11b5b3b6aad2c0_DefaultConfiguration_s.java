 /*
  * Copyright 2009 Google Inc.
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
 
 import com.google.jstestdriver.FileInfo;
 import com.google.jstestdriver.Flags;
 import com.google.jstestdriver.PathResolver;
 import com.google.jstestdriver.Plugin;
 import com.google.jstestdriver.model.HandlerPathPrefix;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 /**
  * @author corysmith@google.com (Cory Smith)
  */
 public class DefaultConfiguration implements Configuration{
 
   public static final long DEFAULT_TEST_TIMEOUT = 2 * 60 * 60;
 
   public Set<FileInfo> getFilesList() {
     return Collections.<FileInfo>emptySet();
   }
 
   public List<Plugin> getPlugins() {
     return Collections.<Plugin>emptyList();
   }
 
   public String getServer(String flagValue, int port, HandlerPathPrefix handlerPrefix) {
     if (flagValue != null && !flagValue.isEmpty()) {
       return handlerPrefix.suffixServer(flagValue);
     }
 
     if (port == -1) {
       throw new RuntimeException("Oh Snap! No server defined!");
     }
    return handlerPrefix.suffixServer(String.format("http://%s:%d", "127.0.0.1"));
   }
 
   public Configuration resolvePaths(PathResolver resolver, Flags flags) {
     return this;
   }
 
   public long getTestSuiteTimeout() {
     return DEFAULT_TEST_TIMEOUT; // two hours. Should be enough to debug.
   }
 
   public List<FileInfo> getTests() {
     return Collections.<FileInfo>emptyList();
   }
 }
