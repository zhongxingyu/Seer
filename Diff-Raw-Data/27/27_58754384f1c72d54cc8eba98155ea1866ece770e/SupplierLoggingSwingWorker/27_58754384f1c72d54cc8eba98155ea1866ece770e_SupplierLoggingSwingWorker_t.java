 /*
  * Copyright (c) 2006-2015 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.components;
 
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.function.Consumer;
 import java.util.function.Supplier;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import static com.dmdirc.util.LogUtils.USER_ERROR;

 /**
  * {@link LoggingSwingWorker} that runs a {@link Supplier}.
  */
 public class SupplierLoggingSwingWorker<T, V> extends LoggingSwingWorker<T, V> {
 
     private static final Logger LOG = LoggerFactory.getLogger(SupplierLoggingSwingWorker.class);
     private final Consumer<T> doneConsumer;
     private final Consumer<List<V>> processConsumer;
     private final Supplier<T> backgroundSupplier;
 
     /**
      * Creates a new logging swing worker.
      *
      * @param backgroundSupplier The supplier to call as the background task, off the EDT.
      */
     public SupplierLoggingSwingWorker(
             final Supplier<T> backgroundSupplier) {
         this(backgroundSupplier, result -> {});
     }
 
     /**
      * Creates a new logging swing worker.
      *
      * @param backgroundSupplier The supplier to call as the background task, off the EDT.
      * @param doneConsumer       The consumer called when the background task is complete
      */
     public SupplierLoggingSwingWorker(
             final Supplier<T> backgroundSupplier,
             final Consumer<T> doneConsumer) {
         this(backgroundSupplier, doneConsumer, chunks -> {});
     }
 
     /**
      * Creates a new logging swing worker.
      *
      * @param backgroundSupplier The supplier to call as the background task, off the EDT.
      * @param doneConsumer       The consumer called when the background task is complete
      * @param processConsumer    The consumer called to process results of the background task
      *                           as it progresses
      */
     public SupplierLoggingSwingWorker(
             final Supplier<T> backgroundSupplier,
             final Consumer<T> doneConsumer,
             final Consumer<List<V>> processConsumer) {
         this.backgroundSupplier = backgroundSupplier;
         this.doneConsumer = doneConsumer;
         this.processConsumer = processConsumer;
     }
 
     @Override
     protected T doInBackground() throws Exception {
         return backgroundSupplier.get();
     }
 
     @Override
     protected void process(final List<V> chunks) {
         processConsumer.accept(chunks);
     }
 
     @Override
     protected void done() {
         try {
             handleDone(get());
         } catch (InterruptedException ex) {
             //Ignore
         } catch (ExecutionException ex) {
            LOG.warn(USER_ERROR, ex.getMessage(), ex);
         }
     }
 
     public void handleDone(final T value) {
         doneConsumer.accept(value);
     }
 }
