 /*
  * Copyright 2010 TranceCode
  *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
package org.trancecode.core;
 
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
