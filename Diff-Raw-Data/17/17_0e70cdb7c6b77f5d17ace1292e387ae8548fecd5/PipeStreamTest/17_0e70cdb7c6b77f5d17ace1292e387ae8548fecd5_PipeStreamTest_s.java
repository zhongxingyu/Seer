 /**
  * Copyright (C) 2011 Ovea <dev@ovea.com>
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
 package com.ovea.network.pipe;
 
 import com.mycila.junit.concurrent.Concurrency;
 import com.mycila.junit.concurrent.ConcurrentJunitRunner;
 import com.mycila.junit.rule.TimeRule;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.runner.JUnitCore;
 import org.junit.runner.RunWith;
 import org.mockito.Matchers;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.*;
 
 /**
  * @author Mathieu Carbou (mathieu.carbou@gmail.com)
  */
 @RunWith(ConcurrentJunitRunner.class)
 @Concurrency(15)
 public final class PipeStreamTest {
 
     @Test
     public void test_identity() throws Exception {
         writer.start();
         Pipe pipe1 = new PipeStream("1", new ByteArrayInputStream("".getBytes()), new ByteArrayOutputStream());
         Pipe pipe2 = new PipeStream("1", new ByteArrayInputStream("".getBytes()), new ByteArrayOutputStream());
 
         assertEquals(pipe1, pipe2);
         assertEquals(pipe1.hashCode(), pipe2.hashCode());
         assertEquals(pipe1.name(), pipe2.name());
         assertEquals(pipe1.toString(), pipe2.toString());
 
         assertEquals(pipe1.connect(), pipe2.connect());
         assertEquals(pipe1.connect().hashCode(), pipe2.hashCode());
         assertEquals(pipe1.connect().toString(), pipe2.connect().toString());
 
         pipe1.connect().interrupt();
         pipe2.connect().interrupt();
     }
 
     @Test
     public void test_connect_handle() throws Exception {
         writer.start();
         PipeConnection connection = pipe.connect();
         try {
             connection.await();
             verify(listener, times(0)).onInterrupt(eq(pipe));
             verify(listener, times(1)).onClose(eq(pipe));
             verify(listener, times(1)).onConnect(eq(pipe));
             verify(listener, times(0)).onBroken(eq(pipe), Matchers.<BrokenPipeException>any());
         } catch (InterruptedException e) {
             fail(e.getMessage());
         } catch (BrokenPipeException e) {
             fail(e.getMessage());
         }
     }
 
     @Test
     public void test_connect_handle_double() throws Exception {
         writer.start();
         PipeConnection connection = pipe.connect();
         try {
             connection.await();
             connection.await();
             verify(listener, times(0)).onInterrupt(eq(pipe));
             verify(listener, times(1)).onClose(eq(pipe));
             verify(listener, times(1)).onConnect(eq(pipe));
             verify(listener, times(0)).onBroken(eq(pipe), Matchers.<BrokenPipeException>any());
         } catch (InterruptedException e) {
             fail(e.getMessage());
         } catch (BrokenPipeException e) {
             fail(e.getMessage());
         }
     }
 
     @Test
     public void test_connect_handle_timeout_ok() throws Exception {
         writer.start();
         PipeConnection connection = pipe.connect();
         try {
             connection.await(5, TimeUnit.SECONDS);
             verify(listener, times(0)).onInterrupt(eq(pipe));
             verify(listener, times(1)).onClose(eq(pipe));
             verify(listener, times(1)).onConnect(eq(pipe));
             verify(listener, times(0)).onBroken(eq(pipe), Matchers.<BrokenPipeException>any());
         } catch (TimeoutException e) {
             fail(e.getMessage());
         } catch (InterruptedException e) {
             fail(e.getMessage());
         } catch (BrokenPipeException e) {
             fail(e.getMessage());
         }
     }
 
     @Test
     public void test_connect_handle_timeout_ok_double() throws Exception {
         writer.start();
         PipeConnection connection = pipe.connect();
         try {
             connection.await(5, TimeUnit.SECONDS);
             connection.await(5, TimeUnit.SECONDS);
             verify(listener, times(0)).onInterrupt(eq(pipe));
             verify(listener, times(1)).onClose(eq(pipe));
             verify(listener, times(1)).onConnect(eq(pipe));
             verify(listener, times(0)).onBroken(eq(pipe), Matchers.<BrokenPipeException>any());
         } catch (TimeoutException e) {
             fail(e.getMessage());
         } catch (InterruptedException e) {
             fail(e.getMessage());
         } catch (BrokenPipeException e) {
             fail(e.getMessage());
         }
     }
 
     @Test
     public void test_connect_handle_timedout() throws Exception {
         writer.start();
         try {
             pipe.connect().await(1, TimeUnit.SECONDS);
             fail();
         } catch (TimeoutException e) {
             verify(listener, times(0)).onInterrupt(eq(pipe));
             verify(listener, times(0)).onClose(eq(pipe));
             verify(listener, times(1)).onConnect(eq(pipe));
             verify(listener, times(0)).onBroken(eq(pipe), Matchers.<BrokenPipeException>any());
         } catch (InterruptedException e) {
             fail(e.getMessage());
         } catch (BrokenPipeException e) {
             fail(e.getMessage());
         }
     }
 
     @Test
     public void test_connect_handle_timedout_double() throws Exception {
         writer.start();
         try {
             pipe.connect().await(500, TimeUnit.MILLISECONDS);
             fail();
         } catch (TimeoutException e) {
             try {
                 pipe.connect().await(500, TimeUnit.MILLISECONDS);
                 fail();
             } catch (TimeoutException e1) {
                 verify(listener, times(0)).onInterrupt(eq(pipe));
                 verify(listener, times(0)).onClose(eq(pipe));
                 verify(listener, times(1)).onConnect(eq(pipe));
                 verify(listener, times(0)).onBroken(eq(pipe), Matchers.<BrokenPipeException>any());
             } catch (Exception e1) {
                 fail();
             }
         } catch (InterruptedException e) {
             fail(e.getMessage());
         } catch (BrokenPipeException e) {
             fail(e.getMessage());
         }
     }
 
     @Test
     public void test_connect_handle_interrupted() throws Exception {
         Thread t = new Thread() {
             @Override
             public void run() {
                 try {
                     pipe.connect().await(5, TimeUnit.SECONDS);
                     fail();
                 } catch (TimeoutException e) {
                     fail(e.getMessage());
                 } catch (InterruptedException e) {
                     verify(listener, times(1)).onInterrupt(eq(pipe));
                     verify(listener, times(0)).onClose(eq(pipe));
                     verify(listener, times(1)).onConnect(eq(pipe));
                     verify(listener, times(0)).onBroken(eq(pipe), Matchers.<BrokenPipeException>any());
                 } catch (BrokenPipeException e) {
                     fail(e.getMessage());
                 }
             }
         };
         writer.start();
         t.start();
         Thread.sleep(500);
         t.interrupt();
         t.join();
     }
 
     @Test
     public void test_connect_handle_interrupt_pipe() throws Exception {
         Thread t = new Thread() {
             @Override
             public void run() {
                 try {
                     pipe.connect().await();
                     fail();
                 } catch (InterruptedException e) {
                     verify(listener, times(1)).onInterrupt(eq(pipe));
                     verify(listener, times(0)).onClose(eq(pipe));
                     verify(listener, times(1)).onConnect(eq(pipe));
                     verify(listener, times(0)).onBroken(eq(pipe), Matchers.<BrokenPipeException>any());
                 } catch (BrokenPipeException e) {
                     fail(e.getMessage());
                 }
             }
         };
         t.start();
         writer.start();
         Thread.sleep(500);
         pipe.connect().interrupt();
         t.join();
     }
 
     @Test
     public void test_connect_handle_interrupt() throws Exception {
         writer.start();
         PipeConnection connection = pipe.connect();
         Thread.sleep(1000);
         connection.interrupt();
         verify(listener, times(1)).onInterrupt(eq(pipe));
         verify(listener, times(0)).onClose(eq(pipe));
         verify(listener, times(1)).onConnect(eq(pipe));
         verify(listener, times(0)).onBroken(eq(pipe), Matchers.<BrokenPipeException>any());
     }
 
     @Test
     public void test_connect_handle_interrupt_double() throws Exception {
         writer.start();
         PipeConnection connection = pipe.connect();
         Thread.sleep(1000);
         connection.interrupt();
         connection.interrupt();
         verify(listener, times(1)).onInterrupt(eq(pipe));
         verify(listener, times(0)).onClose(eq(pipe));
         verify(listener, times(1)).onConnect(eq(pipe));
         verify(listener, times(0)).onBroken(eq(pipe), Matchers.<BrokenPipeException>any());
     }
 
     /////////////////////////////////////
 
     PipedOutputStream out = new PipedOutputStream();
     ByteArrayOutputStream baos = new ByteArrayOutputStream();
     PipeListener listener = mock(PipeListener.class);
     Pipe pipe;
     Thread writer = new Thread() {
         @Override
         public void run() {
             try {
                 for (int i = 0; i < 4 && !Thread.interrupted(); i++) {
                     out.write("Hello world !".getBytes());
                     Thread.sleep(500);
                 }
                 out.close();
             } catch (Exception e) {
                 //e.printStackTrace(System.out);
                 Thread.currentThread().interrupt();
             }
         }
     };
 
     @Rule
     public TimeRule timeRule = new TimeRule();
 
     @Before
     public void pipeSetup() throws Exception {
        pipe = new PipeStream(new PipedInputStream(out), baos).listenedBy(new PipeListeners(listener));
         assertFalse(pipe.isOpened());
     }
 
     @After
     public void pipeVerif() throws Exception {
        if(pipe.isOpened() || pipe.isReady()) {
             pipe.connect().interrupt();
         }
         for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
             if (entry.getKey().getName().equals("pipe-" + pipe.name() + "-thread")) {
                 fail(entry.getKey().getName());
             }
         }
         assertFalse(pipe.isOpened());
         if (pipe.isClosed()) {
             assertEquals("Hello world !Hello world !Hello world !Hello world !", new String(baos.toByteArray()));
         }
     }
 
     public static void main(String[] args) {
         JUnitCore.main(PipeStreamTest.class.getName());
     }
 }
