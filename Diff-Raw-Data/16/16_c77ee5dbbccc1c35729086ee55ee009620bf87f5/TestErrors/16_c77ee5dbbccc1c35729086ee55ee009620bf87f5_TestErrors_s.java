 /*
  * Copyright 2010 Google Inc.
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
 
 import java.io.CharArrayWriter;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.util.List;
 
 /**
  * Represents all errors that occurred during a test run. 
  * @author corbinrsmith@gmail.com (Cory Smith)
  */
 public class TestErrors extends RuntimeException {
 
   private final List<Throwable> causes;
   private final String message;
 
   public TestErrors(String message, List<Throwable> causes) {
     super(message);
     this.message = message;
     this.causes = causes;
   }
 
   @Override
   public void printStackTrace(PrintStream s) {
     synchronized (s) {
       s.println(this);
       StackTraceElement[] trace = getStackTrace();
       for (int i=0; i < trace.length; i++) {
           s.println("\tat " + trace[i]);
       }
 
       s.println("Caused by:");
       for (Throwable cause : causes) {
        s.append(cause.toString());
       }
     }
   }
 
   @Override
   public void printStackTrace(PrintWriter s) {
     synchronized (s) {
       s.println(this);
       StackTraceElement[] trace = getStackTrace();
       for (int i=0; i < trace.length; i++) {
           s.println("\tat " + trace[i]);
       }
 
       for (Throwable cause : causes) {
         s.println("Caused by:");
         s.println("\tat" + cause.toString());
       }
     }
   }
 
   @Override
   public String toString() {
    final CharArrayWriter writer = new CharArrayWriter();
    writer.append("\nTest Errors:\n\tat");
    writer.append(message);
    return writer.toString();
   }
 }
