 package com.lmax.disruptor;
 
 /**
  * Convenience class for handling the batching semantics of consuming entries from a {@link RingBuffer}
  * and delegating the available {@link AbstractEntry}s to a {@link BatchHandler}.
  *
  * If the {@link BatchHandler} also implements {@link LifecycleAware} it will be notified just after the thread
  * is started and just before the thread is shutdown.
  *
  * @param <T> Entry implementation storing the data for sharing during exchange or parallel coordination of an event.
  */
 public final class BatchConsumer<T extends AbstractEntry>
     implements Consumer
 {
     private final ConsumerBarrier<T> consumerBarrier;
     private final BatchHandler<T> handler;
     private ExceptionHandler exceptionHandler = new FatalExceptionHandler();
 
     public long p1, p2, p3, p4, p5, p6, p7;  // cache line padding
     private volatile boolean running = true;
     public long p8, p9, p10, p11, p12, p13, p14; // cache line padding
     private volatile long sequence = -1L;
     public long p15, p16, p17, p18, p19, p20; // cache line padding
 
     /**
      * Construct a batch consumer that will automatically track the progress by updating its sequence when
      * the {@link BatchHandler#onAvailable(AbstractEntry)} method returns.
      *
      * @param consumerBarrier on which it is waiting.
      * @param handler is the delegate to which {@link AbstractEntry}s are dispatched.
      */
     public BatchConsumer(final ConsumerBarrier<T> consumerBarrier,
                          final BatchHandler<T> handler)
     {
         this.consumerBarrier = consumerBarrier;
         this.handler = handler;
     }
 
     /**
      * Construct a batch consumer that will rely on the {@link SequenceTrackingHandler}
     * to callback via the {@link BatchConsumer.SequenceTrackerCallback} when it has 
     * completed with a sequence within a batch.  Sequence will be updated at the end of
     * a batch regardless.
      *
      * @param consumerBarrier on which it is waiting.
      * @param entryHandler is the delegate to which {@link AbstractEntry}s are dispatched.
      */
     public BatchConsumer(final ConsumerBarrier<T> consumerBarrier,
                          final SequenceTrackingHandler<T> entryHandler)
     {
         this.consumerBarrier = consumerBarrier;
         this.handler = entryHandler;
 
         entryHandler.setSequenceTrackerCallback(new SequenceTrackerCallback());
     }
 
     @Override
     public long getSequence()
     {
         return sequence;
     }
 
     @Override
     public void halt()
     {
         running = false;
         consumerBarrier.alert();
     }
 
     /**
      * Set a new {@link ExceptionHandler} for handling exceptions propagated out of the {@link BatchConsumer}
      *
      * @param exceptionHandler to replace the existing exceptionHandler.
      */
     public void setExceptionHandler(final ExceptionHandler exceptionHandler)
     {
         if (null == exceptionHandler)
         {
             throw new NullPointerException();
         }
 
         this.exceptionHandler = exceptionHandler;
     }
 
     /**
      * Get the {@link ConsumerBarrier} the {@link Consumer} is waiting on.
      *
       * @return the barrier this {@link Consumer} is using.
      */
     public ConsumerBarrier<? extends T> getConsumerBarrier()
     {
         return consumerBarrier;
     }
 
     /**
      * It is ok to have another thread rerun this method after a halt().
      */
     @Override
     public void run()
     {
         running = true;
         T entry = null;
 
         if (LifecycleAware.class.isAssignableFrom(handler.getClass()))
         {
             ((LifecycleAware)handler).onStart();
         }
 
         while (running)
         {
             try
             {
                 final long nextSequence = sequence + 1;
                 final long availableSeq = consumerBarrier.waitFor(nextSequence);
 
                 for (long i = nextSequence; i <= availableSeq; i++)
                 {
                     entry = consumerBarrier.getEntry(i);
                     handler.onAvailable(entry);
                 }
 
                 handler.onEndOfBatch();
                sequence = entry.getSequence();
             }
             catch (final AlertException ex)
             {
                 // Wake up from blocking wait and check if we should continue to run
             }
             catch (final Exception ex)
             {
                 exceptionHandler.handle(ex, entry);
                sequence = entry.getSequence();
             }
         }
 
         if (LifecycleAware.class.isAssignableFrom(handler.getClass()))
         {
             ((LifecycleAware)handler).onShutdown();
         }
     }
 
     /**
      * Used by the {@link BatchHandler} to signal when it has completed consuming a given sequence.
      */
     public final class SequenceTrackerCallback
     {
         /**
          * Notify that the handler has consumed up to a given sequence.
          *
          * @param sequence that has been consumed.
          */
         public void onCompleted(final long sequence)
         {
             BatchConsumer.this.sequence = sequence;
         }
     }
 }
