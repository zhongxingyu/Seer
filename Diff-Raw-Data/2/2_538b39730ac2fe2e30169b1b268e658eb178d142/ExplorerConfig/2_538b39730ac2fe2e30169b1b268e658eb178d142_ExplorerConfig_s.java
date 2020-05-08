 /*
  * Copyright (C) 2010 Google Inc.
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
 
 package com.google.api.explorer.client;
 
 import com.google.common.collect.ImmutableSet;
 
 import java.util.Set;
 
 /**
  * Clearinghouse for Explorer configuration data.
  *
  * @author jasonhall@google.com (Jason Hall)
  */
 public class ExplorerConfig {
 
   /**
    * Services which should explicitly not appear in the Explorer for one reason
    * or another.
    */
   public static final Set<String> SERVICE_BLACKLIST =
      ImmutableSet.of("chromewebstore", "latitude");
 
   /**
    * APIs which do not require a key to access, and so will fail if one is
    * given.
    */
   public static final Set<String> PUBLIC_ONLY_APIS = ImmutableSet.of("discovery");
 
 }
