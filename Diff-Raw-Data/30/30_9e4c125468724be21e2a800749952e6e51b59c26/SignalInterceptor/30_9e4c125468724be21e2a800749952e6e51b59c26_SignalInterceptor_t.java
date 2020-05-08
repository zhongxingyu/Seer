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
 
 import java.util.concurrent.Callable;
 
 /**
  * The SignalInterceptor class is a static collection of methods used to
  * intercept POSIX signals before they are sent to their original signal
  * handlers. Signal interceptors may be chained: If one attach the handler
  * <code>A</code> to signal <code>X</code>, then attach the handler
  * <code>B</code> to <code>X</code> as well, then all raised signals of type
  * <code>X</code> will first call <code>B</code>, then <code>A</code>, then the
  * original handler.
  * <p>
  * Signal handlers are {@link java.util.concurrent.Callable}s, which return
  * booleans. If the signal handler returns <code>true</code>, the call chain
  * will continue. If the signal handler returns <code>false</code> or throws an
  * Exception, the call chain will stop (and no Exception will be thrown). If the
  * signal handler throws an Error, the Error will not be caught.
  *
  * @author Jean Niklas L'orange
  * @since <code>com.hypirion.io 0.3.0</code>
  */
 
 public class SignalInterceptor {
 
     /**
      * Registers the {@link java.util.concurrent.Callable} object to the POSIX
      * signal <code>signame</code>. The POSIX signal must be in upper case,
      * with the beginning <code>SIG</code> part omitted.
      * <p>
      * E.g. an interception to <code>SIGINT</code> be converted to the symbol
      * <code>"INT"</code>.
      *
      * @param signame the POSIX signal name with <code>SIG</code> omitted.
      * @param fn the <code>Callable</code> to call. The <code>Callable</code>
      * should return either <code>true</code> or <code>false</code>.
     * @exception SignalInterceptorException if there is no signal with the name
     * <code>signame</code>, or if the JVM doesn't implement the most common JVM
     * signal handling facilities.
      */
     public static void register(String signame, Callable fn)
         throws SignalInterceptorException {
         try {
             SignalInterceptorHelper.register(signame, fn);
         } catch (Throwable e) { // Catching Linkage errors etc. here
             throw new SignalInterceptorException(signame, e);
         }
     }
 
     /**
      * Registers the {@link java.util.concurrent.Callable} object to the POSIX
      * signal <code>signame</code>. If successful, will return true. If the
      * registering fails, returns false. The POSIX signal must be in upper case,
      * with the beginning <code>SIG</code> part omitted.
      * <p>
      * E.g. an interception to <code>SIGINT</code> be converted to the symbol
      * <code>"INT"</code>.
      *
      * @param signame the POSIX signal name with <code>SIG</code> omitted.
      * @param fn the <code>Callable</code> to call. The <code>Callable</code>
      * should return either <code>true</code> or <code>false</code>.
      */
     public static boolean tryRegister(String signame, Callable fn) {
         try {
             register(signame, fn);
             return true;
         } catch (SignalInterceptorException sie) {
             return false;
         }
     }
 }
