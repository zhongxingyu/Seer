 /**
  * Copyright 2012 Alex Jones
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.    
  *
  * @author unclealex72
  *
  */
 
 package uk.co.unclealex.music.sync;
 
 import java.io.PrintWriter;
 import java.nio.charset.Charset;
 import java.util.List;
 
 import com.google.common.base.Charsets;
 import com.google.common.collect.Lists;
 
 import uk.co.unclealex.process.ProcessCallback;
 import uk.co.unclealex.process.stream.PrintWriterStandardInputStreamSupplier;
 import uk.co.unclealex.process.stream.StandardInputSupplier;
 
 /**
  * A {@link ProcessCallback} and {@link StandardInputSupplier} that reads output
  * from and writes input to the <code>sync.py</code> command.
  * 
  * @author alex
  * 
  */
 public class GtkPodExecutor extends PrintWriterStandardInputStreamSupplier implements ProcessCallback {
 
   /**
    * The object to use as a lock to wait for output.
    */
   private final Object lock = new Object();
 
   /**
    * The current output from the command. This is reset with each command
    * execution.
    */
   private final List<String> output = Lists.newArrayList();
 
   /**
    * Instantiates a new gtk pod process callback.
    *
    * @param streamCharset the stream charset
    */
   public GtkPodExecutor(Charset streamCharset) {
     super(Charsets.UTF_8);
   }
 
   /**
    * Execute a command.
    * 
    * @param command
    *          The command to execute.
    * @return The lines returned by the <code>sync.py</code> up to but excluding
    *         the <code>OK</code> marker.
    */
   public List<String> executeCommand(String command) {
     List<String> output = getOutput();
     output.clear();
     PrintWriter standardInputStream = getStandardInputStream();
     standardInputStream.println(command);
     standardInputStream.flush();
     try {
      getLock().wait();
     }
     catch (InterruptedException e) {
       // Ignore.
     }
     return output;
   }
 
   /**
    * Finish.
    */
   public void finish() {
     getStandardInputStream().close();
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void lineWritten(String line) {
     line = line.trim();
     if ("OK".equals(line)) {
      getLock().notifyAll();
     }
     else {
       getOutput().add(line);
     }
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void errorLineWritten(String line) {
     // Do nothing.
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void processFinished() {
     // Do nothing.
   }
 
   /**
    * Gets the object to use as a lock to wait for output.
    *
    * @return the object to use as a lock to wait for output
    */
   public Object getLock() {
     return lock;
   }
 
   /**
    * Gets the current output from the command.
    *
    * @return the current output from the command
    */
   public List<String> getOutput() {
     return output;
   }
 
 }
