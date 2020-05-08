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
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.InterruptedIOException;
 import java.util.UUID;
 import java.util.concurrent.*;
 import java.util.concurrent.atomic.AtomicReference;
 
 /**
  * @author Mathieu Carbou (mathieu.carbou@gmail.com)
  */
 public abstract class PipeSkeleton<IN extends Closeable, OUT extends Closeable> implements Pipe {
 
     private static enum State {READY, OPENED, CLOSED, INTERRUPTED, BROKEN}
 
     private static final PipeListener EMPTY = new PipeListenerAdapter();
 
     private final AtomicReference<State> state = new AtomicReference<State>(State.READY);
     private final String name;
 
     private PipeConnection connection;
     private PipeListener listener;
     private IN from;
     private OUT to;
 
     protected PipeSkeleton(String name, IN from, OUT to) {
         if (from == null) throw new IllegalArgumentException("Missing origin endpoint");
         if (to == null) throw new IllegalArgumentException("Missing destination endpoint");
         if (name == null) throw new IllegalArgumentException("Missing pipe name");
         this.from = from;
         this.to = to;
         this.name = name;
     }
 
     protected PipeSkeleton(IN from, OUT to) {
         this(UUID.randomUUID().toString(), from, to);
     }
 
     @Override
     public final String name() {
         return name;
     }
 
     @Override
     public final String toString() {
         return name;
     }
 
     @Override
     public final boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
         PipeSkeleton that = (PipeSkeleton) o;
         return name.equals(that.name);
     }
 
     @Override
     public final int hashCode() {
         return name.hashCode();
     }
 
     @Override
     public final Pipe listenedBy(PipeListener listener) {
         if (listener == null) throw new IllegalArgumentException("Listener cannot be null");
         this.listener = new OncePipeListener(listener);
         return this;
     }
 
     @Override
     public final PipeConnection connect() {
         if (state.compareAndSet(State.READY, State.OPENED)) {
             connection = new Connection<IN, OUT>(this);
         }
         return connection;
     }
 
     @Override
     public final boolean isReady() {
         return state.get() == State.READY;
     }
 
     @Override
     public final boolean isOpened() {
         return state.get() == State.OPENED;
     }
 
     @Override
     public final boolean isClosed() {
         return state.get() == State.CLOSED;
     }
 
     @Override
     public final boolean isBroken() {
         return state.get() == State.BROKEN;
     }
 
     @Override
     public final boolean isInterrupted() {
         return state.get() == State.INTERRUPTED;
     }
 
     protected final boolean canCopy() {
         return !Thread.interrupted() && isOpened();
     }
 
     protected abstract void copy(IN from, OUT to) throws IOException, BrokenPipeException;
 
     private PipeListener listener() {
         PipeListener l = listener;
         return l == null ? EMPTY : l;
     }
 
     private static final class Connection<IN extends Closeable, OUT extends Closeable> implements PipeConnection {
 
         private final PipeSkeleton<IN, OUT> pipe;
         private final FutureTask<Object> task;
         private Thread copier;
 
         private Connection(final PipeSkeleton<IN, OUT> pipe) {
             this.pipe = pipe;
             this.task = new FutureTask<Object>(new Callable<Object>() {
                 @Override
                 public Object call() throws Exception {
                     pipe.listener().onConnect(pipe);
                     try {
                         pipe.copy(pipe.from, pipe.to);
                     } catch (InterruptedIOException e) {
                         closeStreams(State.INTERRUPTED);
                         throw new InterruptedException(e.getMessage());
                     } catch (BrokenPipeException e) {
                         closeStreams(State.BROKEN, e);
                         throw e;
                     } catch (IOException e) {
                         BrokenPipeException bpe = new BrokenPipeException(e);
                         closeStreams(State.BROKEN, bpe);
                         throw bpe;
                     }
                     return Boolean.TRUE;
                 }
             }) {
                 @Override
                 protected void done() {
                     if (isCancelled()) {
                         closeStreams(State.INTERRUPTED);
                     } else {
                         closeStreams(State.CLOSED);
                     }
                 }
             };
             copier = new Thread(task, "pipe-" + pipe.name + "-thread");
             copier.start();
         }
 
         @Override
         public final boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
             Connection handle = (Connection) o;
             return pipe.equals(handle.pipe);
         }
 
         @Override
         public final int hashCode() {
             return pipe.hashCode();
         }
 
         @Override
         public final String toString() {
             return pipe.toString();
         }
 
         @Override
         public final Pipe pipe() {
             return pipe;
         }
 
         @Override
         public void interrupt() {
             closeStreams(State.INTERRUPTED);
         }
 
         @Override
         public void await(long time, TimeUnit unit) throws InterruptedException, TimeoutException, BrokenPipeException {
             try {
                 task.get(time, unit);
                closeStreams(State.CLOSED);
             } catch (ExecutionException e) {
                 Throwable t = e;
                 if (e.getCause() != null) {
                     t = e.getCause();
                 }
                 if (t instanceof RuntimeException)
                     throw (RuntimeException) t;
                 if (t instanceof BrokenPipeException)
                     throw (BrokenPipeException) t;
                 if (t instanceof InterruptedException)
                     throw (InterruptedException) t;
                 if (t instanceof Error)
                     throw (Error) t;
                 RuntimeException re = new RuntimeException(t.getMessage(), e);
                 re.setStackTrace(t.getStackTrace());
                 throw re;
             } catch (InterruptedException e) {
                 Thread.currentThread().interrupt();
                 closeStreams(State.INTERRUPTED);
                 throw e;
             } catch (CancellationException e) {
                 closeStreams(State.INTERRUPTED);
                 throw new InterruptedException();
             }
         }
 
         @Override
         public void await() throws InterruptedException, BrokenPipeException {
             try {
                 task.get();
                closeStreams(State.CLOSED);
             } catch (ExecutionException e) {
                 Throwable t = e;
                 if (e.getCause() != null) {
                     t = e.getCause();
                 }
                 if (t instanceof RuntimeException)
                     throw (RuntimeException) t;
                 if (t instanceof BrokenPipeException)
                     throw (BrokenPipeException) t;
                 if (t instanceof InterruptedException)
                     throw (InterruptedException) t;
                 if (t instanceof Error)
                     throw (Error) t;
                 RuntimeException re = new RuntimeException(t.getMessage(), e);
                 re.setStackTrace(t.getStackTrace());
                 throw re;
             } catch (InterruptedException e) {
                 Thread.currentThread().interrupt();
                 closeStreams(State.INTERRUPTED);
                 throw e;
             } catch (CancellationException e) {
                 closeStreams(State.INTERRUPTED);
                 throw new InterruptedException();
             }
         }
 
         private void closeStreams(State end, BrokenPipeException... e) {
             if (pipe.state.compareAndSet(State.OPENED, end) || pipe.state.compareAndSet(State.READY, end)) {
                 if (end == State.INTERRUPTED && copier != Thread.currentThread()) {
                     task.cancel(true);
                     copier.interrupt();
                 }
                 try {
                     pipe.from.close();
                 } catch (Exception ignored) {
                 }
                 try {
                     pipe.to.close();
                 } catch (Exception ignored) {
                 }
                 pipe.from = null;
                 pipe.to = null;
                 if (copier != Thread.currentThread()) {
                     try {
                         copier.join();
                     } catch (InterruptedException e1) {
                         Thread.currentThread().interrupt();
                     }
                 }
                 copier = null;
                 switch (end) {
                     case INTERRUPTED:
                         pipe.listener().onInterrupt(pipe());
                         break;
                     case CLOSED:
                         pipe.listener().onClose(pipe());
                         break;
                     case BROKEN:
                         pipe.listener().onBroken(pipe(), e[0]);
                         break;
                 }
             }
         }
     }
 }
