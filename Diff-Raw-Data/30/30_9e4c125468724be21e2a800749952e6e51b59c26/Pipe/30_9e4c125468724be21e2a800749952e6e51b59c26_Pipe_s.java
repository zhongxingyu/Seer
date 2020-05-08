 /*
  * Copyright (c) 2013 Jean Niklas L'orange. All rights reserved.
  *
  * The use and distribution terms for this software are covered by the
  * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
  * which can be found in the file LICENSE at the root of this distribution.
  *
  * By using this software in any fashion, you are agreeing to be bound by
  * the terms of this license.
  *
  * You must not remove this notice, or any other, from this software.
  */
 
 package com.hypirion.io;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.IOException;
 import java.io.Writer;
 import java.io.Reader;
 
 /**
  * A Pipe is a link between an input stream and an output stream or a reader or
  * writer: The pipe, once started, will asynchronously redirect all data
  * received from the input source to the output source until it is paused,
  * stopped or the input source is empty.
 *
  * Common usage of pipes includes asynchronous zipping of data from two data
  * sources, redirecting output and input to subprocesses and for loggers you
  * want to just pipe from and input source.
  *
  * @author Jean Niklas L'orange
  * @since <code>com.hypirion.io 0.2.0</code>
  */
 public class Pipe {
 
     /**
      * The default size of a pipe's buffer.
      * @since <code>com.hypirion.io 0.2.0</code>
      */
     public static final int DEFAULT_BUFFER_SIZE = 1024;
 
     private final Thread pumper;
     private final Object lock;
     private volatile boolean currentlyRunning, stopped;
 
     /**
      * Creates a new pipe, which redirects data from the stream <code>in</code>
      * to the stream <code>out</code> once started.
      *
      * @param in the input stream to read from.
      * @param out the output stream to write out to.
      */
     public Pipe(InputStream in, OutputStream out) {
         this(in, out, DEFAULT_BUFFER_SIZE);
     }
 
     /**
      * Creates a new pipe with buffer size <code>bufsize</code>, which redirects
      * data from the stream <code>in</code> to the stream <code>out</code> once
      * started.
      *
      * @param in the input stream to read from.
      * @param out the output stream to write out to.
      * @param bufsize the buffer size of the pipe.
      */
     public Pipe(InputStream in, OutputStream out, int bufsize) {
         Runnable pt = new PipeOutputStream(in, out, bufsize);
         lock = new Object();
         currentlyRunning = false;
         stopped = false;
         pumper = new Thread(pt);
         pumper.setName(String.format("PipeThread %d", this.hashCode()));
         pumper.setDaemon(true);
         pumper.start();
     }
 
     /**
      * Creates a new pipe, which redirects data from the reader <code>in</code>
      * to the writer <code>out</code> once started.
      *
      * @param in the reader to read from.
      * @param out the writer to write out to.
      */
     public Pipe(Reader in, Writer out) {
         this(in, out, DEFAULT_BUFFER_SIZE);
     }
 
     /**
      * Creates a new pipe with buffer size <code>bufsize</code>, which redirects
      * data from the reader <code>in</code> to the writer <code>out</code> once
      * started.
      *
      * @param in the reader to read from.
      * @param out the writer to write out to.
      * @param bufsize the buffer size of the pipe.
      */
     public Pipe(Reader in, Writer out, int bufsize) {
         Runnable pt = new PipeWriter(in, out, bufsize);
         lock = new Object();
         currentlyRunning = false;
         stopped = false;
         pumper = new Thread(pt);
         pumper.setName(String.format("PipeThread %d", this.hashCode()));
         pumper.setDaemon(true);
         pumper.start();
     }
 
     /**
      * Waits for this pipe to finish piping. This happens when this pipe is
      * stopped and the last blocking read has finished.
      *
      * @exception InterruptedException if this thread is interrupted while
      * waiting.
      *
      * @see #stop()
      * @see #stop(boolean)
      */
     public void join() throws InterruptedException {
         pumper.join();
     }
 
     /**
      * Starts up this pipe. If this pipe has not yet been started or has been
      * paused, this method will (re)start the pipe. When this pipe is stopped,
      * this method will do nothing.
      */
     public synchronized void start() {
         if (!stopped) {
             synchronized (lock) {
                 currentlyRunning = true;
                 lock.notify(); // Wake up the pumper if it's waiting.
             }
         }
     }
 
     /**
      * Pauses this pipe. Will block until the current blocking read by this pipe
      * has finished, regardless of whether this pipe has been stopped or not. If
      * this pipe has already been paused or stopped, and there is no blocking
      * read waiting, will immediatly return.
      *
      * @exception InterruptedException if this thread is interrupted while
      * waiting.
      *
      * @see #pause(boolean)
      */
     public synchronized void pause() throws InterruptedException {
         pause(true);
     }
 
     /**
      * Pauses this pipe. If <code>block</code> is true, will wait until the
      * current blocking read by this pipe has finished. Will otherwise notify
      * this pipe and return immediately.
      *
      * @param block whether to wait for the blocking read (if any) or not.
      *
      * @exception InterruptedException if this thread is interrupted while
      * waiting.
      */
     public synchronized void pause(boolean block) throws InterruptedException {
         if (!stopped) {
             synchronized (lock) {
                 currentlyRunning = false;
                 lock.notify();
                 if (block) {
                     lock.wait();
                     // Wait for signal from pumper, which will start after we
                     // release the lock
                 }
             }
         } else if (block) {
             join();
         }
     }
 
     /**
      * Stops this pipe. Will wait until the last blocking read has finished.
      *
      * @exception InterruptedException if this thread is interrupted while
      * waiting.
      *
      * @see #stop(boolean)
      */
     public synchronized void stop() throws InterruptedException {
         stop(true);
     }
 
     /**
      * Stops this pipe. Will wait until the last blocking read has finished. If
      * <code>block</code> is true, will wait until the current blocking read by
      * this pipe has finished. Will otherwise notify this pipe and return
      * immediately.
      *
      * @param block whether to wait for the blocking read (if any) or not.
      *
      * @exception InterruptedException if this thread is interrupted while
      * waiting.
      */
     public synchronized void stop(boolean block) throws InterruptedException {
         if (!stopped) {
             synchronized (lock) {
                 currentlyRunning = false;
                 stopped = true;
                 lock.notify();
             }
         }
         if (block) {
             join();
         }
     }
 
     private class PipeOutputStream implements Runnable {
         private final byte[] data;
         private final InputStream in;
         private final OutputStream out;
 
         public PipeOutputStream(InputStream in, OutputStream out, int bufsize) {
             data = new byte[bufsize];
             this.in = in;
             this.out = out;
         }
 
         @Override
         public void run() {
             try {
                 outer:
                 while (true) {
                     synchronized (lock) {
                         while (!currentlyRunning) {
                             if (stopped) {
                                 break outer;
                             }
                             lock.wait();
                             lock.notify();
                         }
                     }
                     int count = in.read(data);
                     if (count < 0) {
                         break;
                     }
                     out.write(data, 0, count);
                     out.flush();
                 }
             }
             catch (Exception e) {
                 // Die silently for now.
             }
         }
     }
 
     private class PipeWriter implements Runnable {
         private final char[] data;
         private final Reader in;
         private final Writer out;
 
         public PipeWriter(Reader in, Writer out, int bufsize) {
             data = new char[bufsize];
             this.in = in;
             this.out = out;
         }
 
         @Override
         public void run() {
             try {
                 outer:
                 while (true) {
                     synchronized (lock) {
                         while (!currentlyRunning) {
                             if (stopped) {
                                 break outer;
                             }
                             lock.wait();
                             lock.notify();
                         }
                     }
                     int count = in.read(data);
                     if (count < 0) {
                         break;
                     }
                     out.write(data, 0, count);
                     out.flush();
                 }
             }
             catch (Exception e) {
                 // Die silently for now.
             }
         }
     }
 }
