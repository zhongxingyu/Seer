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
 package com.google.eclipse.javascript.jstestdriver.ui.runner;
 
 import com.google.jstestdriver.ResetAction;
 import com.google.jstestdriver.ResponseStream;
 import com.google.jstestdriver.ResponseStreamFactory;
 import com.google.jstestdriver.TestResultGenerator;
 
 /**
  * @author shyamseshadri@gmail.com (Shyam Seshadri)
  */
 public class EclipseResponseStreamFactory implements ResponseStreamFactory {
 
   public ResponseStream getEvalActionResponseStream() {
     return null;
   }
 
   public ResponseStream getResetActionResponseStream() {
    return new ResetAction.ResetActionResponseStream();
   }
 
   public ResponseStream getRunTestsActionResponseStream(String browserId) {
     return new EclipseRunTestsResponseStream(new TestResultGenerator());
   }
 
   public ResponseStream getDryRunActionResponseStream() {
     return new EclipseDryRunActionResponseStream();
   }
 }
