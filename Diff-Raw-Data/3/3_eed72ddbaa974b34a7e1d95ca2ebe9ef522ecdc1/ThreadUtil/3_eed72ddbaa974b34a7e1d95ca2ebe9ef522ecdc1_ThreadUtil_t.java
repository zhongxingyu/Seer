 /**
  * $RCSfile$
  * $Revision: 11616 $
  * $Date: 2010-02-09 07:40:11 -0500 (Tue, 09 Feb 2010) $
  *
  * Copyright 2011 Glenn Maynard
  *
  * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.jivesoftware.smack.util;
 
 import java.util.concurrent.locks.Condition;
 
 public class ThreadUtil {
     /**
      * Join a thread without observing interrupts.
      * <p>
      * If an InterruptedException occurs while joining the thread, it will be deferred
      * until the join completes.  This allows reliably joining a thread that's expected
      * to exit immediately without race conditions due to thread interruption.  
      * 
      * @param thread The thread to join.
      */
     static public void uninterruptibleJoin(Thread thread) {
        if(Thread.currentThread() == thread)
            throw new RuntimeException("Can't join a thread from within itself");

         boolean interrupted = false;
         while(true) {
             try {
                 thread.join();
                 break;
             } catch(InterruptedException e) {
                 // Defer interruptions until we're done.
                 interrupted = true;
                 continue;
             }
         }
         if(interrupted)
             Thread.currentThread().interrupt();
     }
 
     /**
      * Wait for a {@link Condition} to be signalled without observing interrupts.
      * <p>
      * See {@link #uninterruptibleJoin} for interrupt behavior.
      */
     static public void uninterruptibleWait(Condition obj) {
         boolean interrupted = false;
         while(true) {
             try {
                 obj.await();
                 break;
             } catch (InterruptedException e) {
                 // Defer interruptions until we're done.
                 interrupted = true;
                 continue;
             }
         }
         if(interrupted)
             Thread.currentThread().interrupt();
     }
 
     /**
      * Wait for an object's monitor to be notified without observing interrupts.
      * <p>
      * See {@link #uninterruptibleJoin} for interrupt behavior.
      */
     static public void uninterruptibleMonitorWait(Object obj) {
         boolean interrupted = false;
         while(true) {
             try {
                 obj.wait();
                 break;
             } catch (InterruptedException e) {
                 // Defer interruptions until we're done.
                 interrupted = true;
                 continue;
             }
         }
         if(interrupted)
             Thread.currentThread().interrupt();
     }
 };
