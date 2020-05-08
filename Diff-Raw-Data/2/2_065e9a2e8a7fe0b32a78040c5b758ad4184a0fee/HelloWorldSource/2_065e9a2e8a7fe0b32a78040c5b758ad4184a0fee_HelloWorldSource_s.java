 /**
  * Licensed to Cloudera, Inc. under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  Cloudera, Inc. licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package helloworld;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.omg.CORBA.portable.InputStream;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.cloudera.flume.conf.Context;
 import com.cloudera.flume.conf.SourceFactory.SourceBuilder;
 import com.cloudera.flume.core.Event;
 import com.cloudera.flume.core.EventImpl;
 import com.cloudera.flume.core.EventSource;
 import com.cloudera.util.Clock;
 import com.cloudera.util.Pair;
 import com.google.common.base.Preconditions;
 
 /**
  * Simple Source that generates a "hello world!" event every 3 seconds.
  */
 public class HelloWorldSource extends EventSource.Base {
   static final Logger LOG = LoggerFactory.getLogger(HelloWorldSource.class);
   
   private String helloWorld;
   private Process process;
   private String command = "/home/bharath/workspace/flume/flume_metrics/poll_hadoop_status2.py";
   java.io.InputStream inputStream;
 
   public HelloWorldSource(String command) {
     this.command = command;
   }
 
   public HelloWorldSource() {}
 
   private int bytesToLong(byte[] bytes) {
      int length = bytes.length;
      int value = 0;
      for (int i = 0; i < length; i++) {
        value += (bytes[i] & 0xff) << (8 * i);
      }
      return value;
   }
 
   void readExactlyNBytes(java.io.InputStream stream, byte[] bytes, int n, boolean blockOnEOF)
       throws IOException, InterruptedException {
     int bytesRead = 0;
       try {
         while (bytesRead < n) {
           int numRead = stream.read(bytes, bytesRead, n - bytesRead);
   
           if (numRead == -1) {
             // If the process is no longer running, then throw an error
             try {
               int exitVal = process.exitValue();
               // The process has terminated. Throw an error.
               throw new IOException("Process terminated unexpectedly");
 
             } catch (IllegalThreadStateException e) {
               // Do nothing, since the process is still running
             }
             if(blockOnEOF) {
               // If we must block on EOF, wait a while and try again.
               Clock.sleep(5000);
             } else {
               throw new IOException("Unexpected end of File");
             }
           } else {
             bytesRead += numRead;
           }          
         }
       } catch (InterruptedException e) {
         throw new InterruptedException("Clock interrupted");
       }
 
       if (bytesRead != n) {
         throw new IOException("Unexpected end of Input");
       }
   }
   @Override
   public void open() throws IOException {
     // Initialized the source
     helloWorld = "Hello World!";
     process = Runtime.getRuntime().exec(command);
     inputStream = process.getInputStream();
   }
 
   @Override
   public Event next() throws IOException {
     try {
       // Next returns the next event, blocking if none available.
       byte[] length_bytes = new byte[4];
       readExactlyNBytes(inputStream, length_bytes, 4, true);
 
       int length = bytesToLong(length_bytes);
       byte[] bytes = new byte[4 + length];
       bytes[0] = length_bytes[0];
       bytes[1] = length_bytes[1];
       bytes[2] = length_bytes[2];
       bytes[3] = length_bytes[3];
 
       readExactlyNBytes(inputStream, bytes, length, false);
 
       // TODO(bharath): Parse proto from bytearray.
       Thread.sleep(3000);
       return new EventImpl(bytes);
     } catch (InterruptedException e) {
      throw new IOException(e.getMessage(), e);
     }
 
   }
 
   @Override
   public void close() throws IOException {
     // Cleanup
     helloWorld = null;
   }
 
   public static SourceBuilder builder() {
     // construct a new parameterized source
     return new SourceBuilder() {
       @Override
       public EventSource build(Context ctx,String... argv) {
         if (argv.length == 1) {
           return new HelloWorldSource(argv[0]);
         } else{
           return new HelloWorldSource();
         } 
       }
     };
   }
 
   /**
    * This is a special function used by the SourceFactory to pull in this class
    * as a plugin source.
    */
   public static List<Pair<String, SourceBuilder>> getSourceBuilders() {
     List<Pair<String, SourceBuilder>> builders =
       new ArrayList<Pair<String, SourceBuilder>>();
     builders.add(new Pair<String, SourceBuilder>("helloWorldSource", builder()));
     return builders;
   }
 }
