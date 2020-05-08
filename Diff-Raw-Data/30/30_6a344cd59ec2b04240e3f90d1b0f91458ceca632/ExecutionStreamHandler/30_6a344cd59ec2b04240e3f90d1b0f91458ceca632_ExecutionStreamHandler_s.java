 /*
  * Copyright 2012 Stanley Shyiko
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
 package com.github.shyiko.hmp;
 
 import org.apache.commons.exec.PumpStreamHandler;
 
 import java.io.ByteArrayInputStream;
 
 /**
  * @author <a href="mailto:stanley.shyiko@gmail.com">Stanley Shyiko</a>
  */
 class ExecutionStreamHandler extends PumpStreamHandler {
 
     public ExecutionStreamHandler(boolean suppressOutput) {
         this(suppressOutput, null);
     }
 
     public ExecutionStreamHandler(boolean suppressOutput, String input) {
         super(
                suppressOutput ? null : System.out,
                suppressOutput ? null : System.err,
                 input == null ? null : new ByteArrayInputStream(input.getBytes())
         );
     }
 }
