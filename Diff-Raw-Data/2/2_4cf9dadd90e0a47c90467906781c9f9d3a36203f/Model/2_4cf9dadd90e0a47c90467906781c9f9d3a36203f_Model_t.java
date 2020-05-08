 // Copyright 2006 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.model;
 
 import java.io.FileNotFoundException;
 
 import org.ref_send.promise.Promise;
 import org.ref_send.promise.eventual.Loop;
 import org.web_send.graph.Framework;
 
 /**
  * A persistent application model.
  * <p>
  * This class implements the abstraction described by a {@link Framework}.
  * </p>
  */
 public abstract class
 Model {
 
     /**
      * indicates a {@linkplain #enter transaction} may modify existing state
      */
     static public final boolean change = false;
 
     /**
      * indicates a {@linkplain #enter transaction} only queries existing state,
      * and does not persist any new selfish objects
      */
     static public final boolean extend = true;
 
     /**
      * Schedules deferred {@linkplain #enter access} to this model.
      */
     public final Loop<Service> service;
 
     /**
      * Constructs an instance.
      * @param service   {@link #service}
      */
     protected
     Model(final Loop<Service> service) {
         this.service = service;
     }
 
     /**
      * Processes a transaction within this model.
      * <p>
      * The implementation MUST ensure only one transaction is active in the
      * model at any time. An invocation from another thread MUST block until the
      * model becomes available. A recursive invocation from the same thread MUST
      * throw an {@link Exception}.
      * </p>
      * <p>
      * If {@linkplain Transaction#run invocation} of the <code>body</code>
      * causes an {@link Error}, the transaction MUST be aborted. When a
      * transaction is aborted, all modifications to objects in the model MUST be
      * discarded. For subsequent transactions, it MUST be as if the aborted
      * transaction was never attempted.
      * </p>
      * <p>
      * The implementation MUST NOT rely on the <code>extend</code> argument
      * accurately describing the behavior of the <code>body</code> argument.
      * If {@link #extend} is specified, the implementation MUST check that the
      * constraints are met; if not, the transaction MUST be aborted.
      * {@linkplain Root#link Linking} a new {@link Root root} value is
      * considered a modification.
      * </p>
      * <p>
      * The <code>body</code> MUST NOT retain references to any of the objects
      * in the model beyond completion of the transaction. The model
      * implementation can rely on the <code>body</code> being well-behaved in
      * this respect. An identifier for an object in the model may be retained
      * across transactions by either {@linkplain Root#link linking}, or
      * {@linkplain Root#export exporting} it.
      * </p>
      * <p>
      * If invocation of this method returns normally, all modifications to
      * objects in the model MUST be committed. Only if the current transaction
      * commits will the {@linkplain Root#effect enqueued} {@link Effect}s be
      * {@linkplain Transaction#run executed}; otherwise, the implementation
     * MUST discard them. The effects MUST be executed in the same order as
      * they were enqueued. Effects from a subsequent transaction MUST NOT be
      * executed until all effects from the current transaction have been
      * executed. An {@link Effect} MUST NOT access objects in the model, but may
      * schedule additional effects.
      * </p>
      * @param <R> <code>body</code>'s return type
      * @param extend either {@link #change} or {@link #extend}
      * @param body transaction body
      * @return promise for <code>body</code>'s return
      * @throws FileNotFoundException model no longer exists
      * @throws Exception problem completing the transaction, which may or may
      *                   not be committed
      */
     public abstract <R> Promise<R>
     enter(boolean extend, Transaction<R> body) throws Exception;
 }
