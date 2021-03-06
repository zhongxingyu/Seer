 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2008, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package org.jboss.xnio;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Iterator;
 import java.util.concurrent.TimeUnit;
 
 /**
  * An abstract base class for {@code IoFuture} objects.  Used to easily produce implementations.
  *
  * @param <T> the type of result that this operation produces
  */
 public abstract class AbstractIoFuture<T> implements IoFuture<T> {
     private Object lock = new Object();
     private Status status = Status.WAITING;
     private Object result;
     private List<Notifier<T>> notifierList;
 
     /**
      * Construct a new instance.
      */
     protected AbstractIoFuture() {
     }
 
     /**
      * {@inheritDoc}
      */
     public Status getStatus() {
         synchronized (lock) {
             return status;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Status await() {
         synchronized (lock) {
             boolean intr = false;
             try {
                 while (status == Status.WAITING) try {
                     lock.wait();
                 } catch (InterruptedException e) {
                     intr = true;
                 }
             } finally {
                 if (intr) {
                     Thread.currentThread().interrupt();
                 }
             }
             return status;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Status await(final TimeUnit timeUnit, long time) {
         if (time < 0L) {
             time = 0L;
         }
         long duration = timeUnit.toMillis(time);
         long deadline = duration + System.currentTimeMillis();
         if (deadline < 0L) {
             deadline = Long.MAX_VALUE;
         }
         synchronized (lock) {
             boolean intr = false;
             try {
                 while (status == Status.WAITING) try {
                     lock.wait(duration);
                 } catch (InterruptedException e) {
                     intr = true;
                 } finally {
                     duration = deadline - System.currentTimeMillis();
                     if (duration <= 0L) {
                         return Status.TIMED_OUT;
                     }
                 }
             } finally {
                 if (intr) {
                     Thread.currentThread().interrupt();
                 }
             }
             return status;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Status awaitInterruptibly() throws InterruptedException {
         synchronized (lock) {
             while (status == Status.WAITING) {
                 lock.wait();
             }
             return status;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Status awaitInterruptibly(final TimeUnit timeUnit, long time) throws InterruptedException {
         if (time < 0L) {
             time = 0L;
         }
         long duration = timeUnit.toMillis(time);
         long deadline = duration + System.currentTimeMillis();
         if (deadline < 0L) {
             deadline = Long.MAX_VALUE;
         }
         synchronized (lock) {
             while (status == Status.WAITING) {
                 lock.wait(duration);
                 duration = deadline - System.currentTimeMillis();
                 if (duration <= 0L) {
                     return Status.TIMED_OUT;
                 }
             }
             return status;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings({"unchecked"})
    public T get() throws IOException {
         synchronized (lock) {
             switch (await()) {
                 case DONE: return (T) result;
                 case FAILED: throw (IOException) result;
                 default: throw new IllegalStateException("Unexpected state " + status);
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings({"unchecked"})
    public T getInterruptibly() throws IOException, InterruptedException {
         synchronized (lock) {
             switch (awaitInterruptibly()) {
                 case DONE: return (T) result;
                 case FAILED: throw (IOException) result;
                 default: throw new IllegalStateException("Unexpected state " + status);
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public IOException getException() throws IllegalStateException {
         synchronized (lock) {
             if (status == Status.FAILED) {
                 return (IOException) result;
             } else {
                 throw new IllegalStateException("getException() when state is not FAILED");
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void addNotifier(final Notifier<T> notifier) {
         synchronized (lock) {
             if (status == Status.WAITING) {
                 if (notifierList == null) {
                     notifierList = new ArrayList<Notifier<T>>();
                 }
                 notifierList.add(notifier);
             } else {
                 runNotifier(notifier);
             }
         }
     }
 
     private final void runAllNotifiers() {
         if (notifierList != null) {
             Iterator<Notifier<T>> it = notifierList.iterator();
             while (it.hasNext()) {
                 Notifier<T> notifier = it.next();
                 runNotifier(notifier);
                 it.remove();
             }
             notifierList = null;
         }
     }
 
     /**
      * Set the exception for this operation.  Any threads blocking on this instance will be unblocked.
      *
      * @param exception the exception to set
      * @return {@code false} if the operation was already completed, {@code true} otherwise
      */
     protected boolean setException(IOException exception) {
         synchronized (lock) {
             if (status == Status.WAITING) {
                 status = Status.FAILED;
                 result = exception;
                 runAllNotifiers();
                 lock.notifyAll();
                 return true;
             } else {
                 return false;
             }
         }
     }
 
     /**
      * Set the result for this operation.  Any threads blocking on this instance will be unblocked.
      *
      * @param result the result to set
      * @return {@code false} if the operation was already completed, {@code true} otherwise
      */
     protected boolean setResult(T result) {
         synchronized (lock) {
             if (status == Status.WAITING) {
                 status = Status.DONE;
                 this.result = result;
                 runAllNotifiers();
                 lock.notifyAll();
                 return true;
             } else {
                 return false;
             }
         }
     }
 
     /**
      * Acknowledge the cancellation of this operation.
      *
      * @return {@code false} if the operation was already completed, {@code true} otherwise
      */
     protected boolean finishCancel() {
         synchronized (lock) {
             if (status == Status.WAITING) {
                 status = Status.CANCELLED;
                 runAllNotifiers();
                 lock.notifyAll();
                 return true;
             } else {
                 return false;
             }
         }
     }
 
     /**
      * Cancel an operation.  The actual cancel may be synchronous or asynchronous.  Implementors will use this method
      * to initiate the cancel; use the {@link #finishCancel()} method to indicate that the cancel was successful.  If
      * cancellation is not supported, this method may be a no-op.
      *
      * @return this {@code IoFuture} instance
      */
     public abstract IoFuture<T> cancel();
 
     /**
      * Run a notifier.  Implementors will run the notifier, preferably in another thread.
      *
      * @param notifier the notifier to run
      */
     protected abstract void runNotifier(Notifier<T> notifier);
 }
