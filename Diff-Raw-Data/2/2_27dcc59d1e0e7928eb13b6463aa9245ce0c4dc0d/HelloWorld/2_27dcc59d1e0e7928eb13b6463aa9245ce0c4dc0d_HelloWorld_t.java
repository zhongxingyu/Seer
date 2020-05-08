 /**
  * Copyright 2012 StackMob
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.stackmob.example;
 
 import java.net.HttpURLConnection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.callinize.crm.SugarCRM;
 import com.stackmob.core.customcode.CustomCodeMethod;
 import com.stackmob.core.rest.ProcessedAPIRequest;
 import com.stackmob.core.rest.ResponseToProcess;
 import com.stackmob.sdkapi.SDKServiceProvider;
 
 public class HelloWorld implements CustomCodeMethod {
 
   @Override
   public String getMethodName() {
     return "hello_world";
   }
 
   @Override
   public List<String> getParams() {
     return new ArrayList<String>();
   }
 
   @Override
   public ResponseToProcess execute(ProcessedAPIRequest request, SDKServiceProvider serviceProvider) {
     Map<String, Object> map = new HashMap<String, Object>();
    map.put("msg", "Hello, world!333");
     map.put("ContactName",  new SugarCRM().getBeanTest() );
     return new ResponseToProcess(HttpURLConnection.HTTP_OK, map);
   }
 
 }
