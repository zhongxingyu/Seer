 /***************************************************************************
 *                                                                          *
 *  Organization: Lawrence Livermore National Lab (LLNL)                    *
 *   Directorate: Computation                                               *
 *    Department: Computing Applications and Research                       *
 *      Division: S&T Global Security                                       *
 *        Matrix: Atmospheric, Earth and Energy Division                    *
 *       Program: PCMDI                                                     *
 *       Project: Earth Systems Grid Federation (ESGF) Data Node Software   *
 *  First Author: Gavin M. Bell (gavin@llnl.gov)                            *
 *                                                                          *
 ****************************************************************************
 *                                                                          *
 *   Copyright (c) 2009, Lawrence Livermore National Security, LLC.         *
 *   Produced at the Lawrence Livermore National Laboratory                 *
 *   Written by: Gavin M. Bell (gavin@llnl.gov)                             *
 *   LLNL-CODE-420962                                                       *
 *                                                                          *
 *   All rights reserved. This file is part of the:                         *
 *   Earth System Grid Federation (ESGF) Data Node Software Stack           *
 *                                                                          *
 *   For details, see http://esgf.org/esg-node/                             *
 *   Please also read this link                                             *
 *    http://esgf.org/LICENSE                                               *
 *                                                                          *
 *   * Redistribution and use in source and binary forms, with or           *
 *   without modification, are permitted provided that the following        *
 *   conditions are met:                                                    *
 *                                                                          *
 *   * Redistributions of source code must retain the above copyright       *
 *   notice, this list of conditions and the disclaimer below.              *
 *                                                                          *
 *   * Redistributions in binary form must reproduce the above copyright    *
 *   notice, this list of conditions and the disclaimer (as noted below)    *
 *   in the documentation and/or other materials provided with the          *
 *   distribution.                                                          *
 *                                                                          *
 *   Neither the name of the LLNS/LLNL nor the names of its contributors    *
 *   may be used to endorse or promote products derived from this           *
 *   software without specific prior written permission.                    *
 *                                                                          *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS    *
 *   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT      *
 *   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS      *
 *   FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL LAWRENCE    *
 *   LIVERMORE NATIONAL SECURITY, LLC, THE U.S. DEPARTMENT OF ENERGY OR     *
 *   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,           *
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT       *
 *   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF       *
 *   USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND    *
 *   ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,     *
 *   OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT     *
 *   OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF     *
 *   SUCH DAMAGE.                                                           *
 *                                                                          *
 ***************************************************************************/
 
 /**
    Description:
 
 **/
 package esg.node.core;
 
 import java.util.concurrent.Future;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 import java.util.concurrent.ExecutionException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.impl.*;
 
 public abstract class ESGCallableFutureEvent<T> extends ESGCallableEvent implements Future<T> {
 
     private static final Log log = LogFactory.getLog(ESGCallableFutureEvent.class);
 
     private volatile T result = null;
     private volatile boolean cancelled = false;
     private final CountDownLatch countDownLatch;
     
     public ESGCallableFutureEvent(Object source) { this(source,null,null); }
     public ESGCallableFutureEvent(Object source, String message) { this(source,null,message); }
     public ESGCallableFutureEvent(Object source, Object data, String message) { 
         super(source, data, message); 
         countDownLatch = new CountDownLatch(1);
     }
 
     @Override
     public boolean cancel(final boolean mayInterruptIfRunning) {
         if (isDone()) {
             return false;
         } else {
             countDownLatch.countDown();
             cancelled = true;
             return !isDone();
         }
     }
 
     @Override
     public T get() throws InterruptedException, ExecutionException {
         countDownLatch.await();
         return result;
     }
 
     @Override
     public T get(final long timeout, final TimeUnit unit)
             throws InterruptedException, ExecutionException, TimeoutException {
         countDownLatch.await(timeout, unit);
         return result;
     }
 
     @Override
     public boolean isCancelled() {
         return cancelled;
     }
 
     @Override
     public boolean isDone() {
         return countDownLatch.getCount() == 0;
     }
     
     public void setResult(T result) {
         log.trace("setResult to: "+result);
         this.result = result;
         countDownLatch.countDown();
     }
 
    @SuppressWarnings("unchecked")
     public boolean doCall(DataNodeComponent contextComponent) {
         log.trace("doCall");
         boolean handled = false;
         if( (handled = this.call(contextComponent)) ) {
             log.trace(" return value from \"call\" is "+handled);
             setResult((T)getData());
         }else{
             log.trace("This event still has work to do: handled = "+handled);
         }
         return handled;
     }
 
 }
