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
 package com.google.jstestdriver;
 
 import com.google.inject.Inject;
 import com.google.jstestdriver.DryRunAction.DryRunActionResponseStream;
 import com.google.jstestdriver.EvalAction.EvalActionResponseStream;
 import com.google.jstestdriver.ResetAction.ResetActionResponseStream;
 
 /**
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  * 
  */
 public class DefaultResponseStreamFactory implements ResponseStreamFactory {
 
   private final ResponsePrinterFactory responsePrinterFactory;
 
   @Inject
  public DefaultResponseStreamFactory(ResponsePrinterFactory responsePrinterFactory) {
     this.responsePrinterFactory = responsePrinterFactory;
   }
 
   public ResponseStream getRunTestsActionResponseStream(String browserId) {
     String testSuiteName = String.format("com.google.jstestdriver.%s", browserId);
     TestResultPrinter printer =
        responsePrinterFactory.getResponsePrinter(String.format("TEST-%s.xml", testSuiteName));
 
     printer.open(testSuiteName);
     RunTestsActionResponseStream responseStream = new RunTestsActionResponseStream(
         new TestResultGenerator(), printer);
 
     return responseStream;
   }
 
   public ResponseStream getDryRunActionResponseStream() {
     return new DryRunActionResponseStream();
   }
 
   public ResponseStream getEvalActionResponseStream() {
     return new EvalActionResponseStream();
   }
 
   public ResponseStream getResetActionResponseStream() {
     return new ResetActionResponseStream();
   }
 }
