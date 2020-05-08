 /*
  * Copyright 2010 TranceCode
  *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */package org.trancecode.core;
 
 import java.util.concurrent.TimeUnit;
 
 /**
  * Utility methods related to {@link Thread} and {@link ThreadLocal}.
  * 
  * @author Herve Quiroz
  */
 public final class TcThreads
 {
     private TcThreads()
     {
         // No instantiation
     }
 
     public static boolean sleep(final long duration, final TimeUnit timeUnit)
     {
         try
         {
             Thread.sleep(timeUnit.toMillis(duration));
         }
         catch (final InterruptedException e)
         {
             return true;
         }
 
         return false;
     }
 
     public static boolean join(final Thread thread)
     {
         try
         {
             thread.join();
         }
         catch (final InterruptedException e)
         {
             return true;
         }
 
         return false;
     }
 
     /**
      * Sets the value of a {@link ThreadLocal} and return the previous value.
      */
     public static <T> T set(final ThreadLocal<T> threadLocal, final T value)
     {
         return set(threadLocal, value, null);
     }
 
     /**
      * Sets the value of a {@link ThreadLocal} and return the previous value, or
      * the specified default value if {@code null}.
      */
     public static <T> T set(final ThreadLocal<T> threadLocal, final T value, final T defaultValue)
     {
         final T oldValue = threadLocal.get();
         threadLocal.set(value);
         if (oldValue == null)
         {
             return defaultValue;
         }
 
         return oldValue;
     }
 }
