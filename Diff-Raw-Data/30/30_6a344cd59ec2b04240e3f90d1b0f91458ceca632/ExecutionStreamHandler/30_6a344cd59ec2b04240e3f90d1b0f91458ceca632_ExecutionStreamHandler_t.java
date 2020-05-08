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
import java.io.IOException;
import java.io.OutputStream;
 
 /**
  * @author <a href="mailto:stanley.shyiko@gmail.com">Stanley Shyiko</a>
  */
 class ExecutionStreamHandler extends PumpStreamHandler {
 
    /**
     * Used instead of null as a workaround to NameNode/DataNode freezes on Mac OS X.
     */
    private static final NullOutputStream NULL_OUTPUT_STREAM = new NullOutputStream();

     public ExecutionStreamHandler(boolean suppressOutput) {
         this(suppressOutput, null);
     }
 
     public ExecutionStreamHandler(boolean suppressOutput, String input) {
         super(
                suppressOutput ? NULL_OUTPUT_STREAM : System.out,
                suppressOutput ? NULL_OUTPUT_STREAM : System.err,
                 input == null ? null : new ByteArrayInputStream(input.getBytes())
         );
     }

    private static class NullOutputStream extends OutputStream {

        public void write(byte[] b, int off, int len) throws IOException {
        }

        public void write(int b) throws IOException {
        }
    }
 }
