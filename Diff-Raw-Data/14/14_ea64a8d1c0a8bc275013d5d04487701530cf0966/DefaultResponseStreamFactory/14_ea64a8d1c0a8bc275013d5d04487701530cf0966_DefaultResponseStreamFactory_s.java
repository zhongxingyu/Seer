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
 import com.google.inject.Provider;
 import com.google.inject.name.Named;
 import com.google.jstestdriver.DryRunAction.DryRunActionResponseStream;
 import com.google.jstestdriver.EvalAction.EvalActionResponseStream;
 import com.google.jstestdriver.ResetAction.ResetActionResponseStream;
 import com.google.jstestdriver.hooks.TestResultListener;
 
 import java.io.PrintStream;
 
 /**
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  * 
  */
 public class DefaultResponseStreamFactory implements ResponseStreamFactory {
 
   private final Provider<TestResultListener> resultListener;
   private final FailureAccumulator accumulator;
   private final PrintStream out;
 
   @Inject
   public DefaultResponseStreamFactory(Provider<TestResultListener> responsePrinterFactory,
                                       FailureAccumulator accumulator,
                                       @Named("outputStream") PrintStream out) {
     this.resultListener = responsePrinterFactory;
     this.accumulator = accumulator;
     this.out = out;
   }
 
   @Override
   public ResponseStream getRunTestsActionResponseStream(String browserId) {
     TestResultListener listener = resultListener.get();
 
     RunTestsActionResponseStream responseStream = new RunTestsActionResponseStream(
         new TestResultGenerator(), listener, accumulator);
 
     return responseStream;
   }
 
   @Override
   public ResponseStream getDryRunActionResponseStream() {
     return new DryRunActionResponseStream(out, resultListener.get());
   }
 
   @Override
   public ResponseStream getEvalActionResponseStream() {
     return new EvalActionResponseStream(out);
   }
 
   @Override
   public ResponseStream getResetActionResponseStream() {
     return new ResetActionResponseStream(out);
   }
 }
