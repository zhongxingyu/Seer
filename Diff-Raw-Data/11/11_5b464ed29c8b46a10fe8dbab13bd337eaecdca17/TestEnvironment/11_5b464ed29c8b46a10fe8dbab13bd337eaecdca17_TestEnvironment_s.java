 /*
  * Copyright (c) 2009 Jens Scheffler (appenginefan.com)
  * 
  * Licensed under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the
  * License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in
  * writing, software distributed under the License is
  * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
  * CONDITIONS OF ANY KIND, either express or implied. See
  * the License for the specific language governing
  * permissions and limitations under the License.
  */
 package com.appenginefan.toolkit.unittests;
 
 import com.google.apphosting.api.ApiProxy;
 
 /**
  * A dummy test environment, similar to
  * 
  * http://code.google.com/appengine/docs/java/howto/
  * unittesting.html#Establishing_The_Execution_Environment
  * 
  */
 class TestEnvironment implements ApiProxy.Environment {
   public String getAppId() {
     return "Unit Tests";
   }
 
   public String getVersionId() {
     return "1.0";
   }
 
   public void setDefaultNamespace(String s) {
   }
 
   public String getRequestNamespace() {
     return "gmail.com";
   }
 
   public String getDefaultNamespace() {
     return "gmail.com";
   }
 
   public String getAuthDomain() {
     return null;
   }
 
   public boolean isLoggedIn() {
     return false;
   }
 
   public String getEmail() {
     return null;
   }
 
   public boolean isAdmin() {
     return false;
   }
 }
