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
 import java.io.Writer;
 import java.io.Reader;
 import java.io.Closeable;
 import java.io.IOException;
 
 /**
  * A ClosingPipe is a link between an input stream and an output stream or a
  * reader or writer: The pipe, once started, will asynchronously redirect all
  * data received from the input source to the output source until it is paused,
  * stopped or the input source is empty. When the input source has been closed,
  * or the pipe has been stopped, a ClosingPipe will close the output source
  * afterwards.
 * <p>
  * Common usage of pipes includes asynchronous zipping of data from two data
  * sources, redirecting output and input to subprocesses and for loggers you
  * want to just pipe from and input source.
  *
  * @author Jean Niklas L'orange
  * @since <code>com.hypirion.io 0.2.0</code>
  */
 public class ClosingPipe extends Pipe {
 
     protected final Thread closer;
 
     /**
      * Creates a new closing pipe, which redirects data from the stream
      * <code>in</code> to the stream <code>out</code> once started.
      *
      * @param in the input stream to read from.
      * @param out the output stream to write out to.
      */
     public ClosingPipe(InputStream in, OutputStream out) {
         this(in, out, DEFAULT_BUFFER_SIZE);
     }
 
     /**
      * Creates a new closing pipe with buffer size <code>bufsize</code>, which
      * redirects data from the stream <code>in</code> to the stream
      * <code>out</code> once started.
      *
      * @param in the input stream to read from.
      * @param out the output stream to write out to.
      * @param bufsize the buffer size of the pipe.
      */
     public ClosingPipe(InputStream in, OutputStream out, int bufsize) {
         super(in, out, bufsize);
         closer = setupCloser(out);
     }
 
     /**
      * Creates a new closing pipe, which redirects data from the reader
      * <code>in</code> to the writer <code>out</code> once started.
      *
      * @param in the reader to read from.
      * @param out the writer to write out to.
      */
     public ClosingPipe(Reader in, Writer out) {
         this(in, out, DEFAULT_BUFFER_SIZE);
     }
 
     /**
      * Creates a new closing pipe with buffer size <code>bufsize</code>, which
      * redirects data from the reader <code>in</code> to the writer
      * <code>out</code> once started.
      *
      * @param in the reader to read from.
      * @param out the writer to write out to.
      * @param bufsize the buffer size of the pipe.
      */
     public ClosingPipe(Reader in, Writer out, int bufsize) {
         super(in, out, bufsize);
         closer = setupCloser(out);
     }
 
     private Thread setupCloser(Closeable out) {
         Runnable pt = new OutCloser(out);
         Thread closer = new Thread(pt);
         closer.setName(String.format("ClosingPipeCloser %d", this.hashCode()));
         closer.setDaemon(true);
         closer.start();
         return closer;
     }
 
     private class OutCloser implements Runnable {
         private final Closeable out;
 
         public OutCloser(Closeable out) {
             this.out = out;
         }
 
         @Override
         public void run() {
             try {
                 ClosingPipe.this.join();
                 out.close();
             }
             catch (InterruptedException ie) {
                 run(); // We're not giving up that easily.
             }
             catch (IOException ioe) {
                 // The closable is somehow broken, leave it be.
             }
         }
     }
 }
