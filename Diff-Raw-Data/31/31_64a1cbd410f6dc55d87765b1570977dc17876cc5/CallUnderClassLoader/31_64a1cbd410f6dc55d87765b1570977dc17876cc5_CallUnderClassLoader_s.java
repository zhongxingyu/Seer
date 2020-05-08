 /*
  * This file is part of liferay-clojure-integration.
  *
  * liferay-clojure-integration is free software: you can redistribute it and/or
  * modify it under the terms of the GNU General Public License as published by the
  * Free Software Foundation, either version 3 of the License, or (at your option)
  * any later version.
  *
  * liferay-clojure-integration is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * liferay-clojure-integration. If not, see <http://www.gnu.org/licenses/>.
  */
 package au.com.permeance.liferay.clojure;
 
 import java.util.concurrent.Callable;
 
 /**
  * This class provides a wrapper for the {@link Callable} interface which allows the call to be executed under a
  * different {@link ClassLoader}.
  *
  * @param <V> the result type of method <tt>call</tt>
  *
 * @see {@link Callable}
 * @see {@link Thread#getContextClassLoader()}
 * @see {@link Thread#setContextClassLoader(ClassLoader)}
  */
 public class CallUnderClassLoader<V> implements Callable<V> {
 
     /**
      * Stores the real callable to invoke under the {@link #callClassLoader}.
      */
     private final transient Callable<V> callable;
 
     /**
      * Stores the class loader to invoke the {@link #callable} under.
      */
     private final transient ClassLoader callClassLoader;
 
     /**
      * Creates a new instance of CallUnderClassLoader storing the supplied {@link Callable} and {@link ClassLoader} for invocation.
      *
      * @param callable        the {@link Callable} to invoke.
      * @param callClassLoader the {@link ClassLoader} to invoke the callable under.
      */
     public CallUnderClassLoader(final Callable<V> callable,
                                 final ClassLoader callClassLoader) {
         this.callable = callable;
         this.callClassLoader = callClassLoader;
     }
 
     /**
      * Sets the current thread's {@link ClassLoader} to {@link #callClassLoader} and then invokes {@link #callable},
      * setting the original {@link ClassLoader} back after invocation.
      *
      * @return the result of invoking {@link java.util.concurrent.Callable#call()} on {@link #callable}.
      *
      * @throws Exception if invoking {@link java.util.concurrent.Callable#call()} on {@link #call()} throws it.
     * @see {@link Thread#currentThread()}
     * @see {@link Thread#setContextClassLoader(ClassLoader)}
     * @see {@link Thread#getContextClassLoader()}
      */
     @Override
     @SuppressWarnings("PMD.SignatureDeclareThrowsException")
     public final V call() throws Exception {
         @SuppressWarnings("PMD.DoNotUseThreads")
         final Thread thread = Thread.currentThread();
         @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
         final ClassLoader threadClassLoader = thread.getContextClassLoader();
         thread.setContextClassLoader(callClassLoader);
         try {
             return callable.call();
         }
         finally {
             thread.setContextClassLoader(threadClassLoader);
         }
     }
 
 }
