 // Copyright 2006-2008 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.ref_send.promise;
 
 import static org.joe_e.reflect.Proxies.proxy;
 
 import java.io.Serializable;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.Proxy;
 
 import org.joe_e.Equatable;
 import org.joe_e.Immutable;
 import org.joe_e.JoeE;
 import org.joe_e.Selfless;
 import org.joe_e.Struct;
 import org.joe_e.Token;
 import org.joe_e.array.ConstArray;
 import org.joe_e.reflect.Proxies;
 import org.joe_e.reflect.Reflection;
 import org.joe_e.var.Milestone;
 import org.ref_send.type.Typedef;
 
 /**
  * The eventual operator.
  * <p>This class decorates an event loop with methods implementing the core
  * eventual control flow statements needed for defensive programming. The
  * primary aim of these new control flow statements is preventing plan
  * interference.</p>
  * <p>The implementation of a public method can be thought of as a plan in
  * which an object makes a series of state changes based on a list of
  * invocation arguments and the object's own current state. As part of
  * executing this plan, the object may need to notify other objects of the
  * changes in progress. These other objects may have their own plans to
  * execute, based on this notification. Plan interference occurs when
  * execution of these other plans interferes with execution of the original
  * plan.</p>
  * <h3>Plan interference</h3>
  * <p>Interleaving plan execution is vulnerable to many kinds of interference.
  * Each kind of interference is explained below, using the following example
  * code:</p>
  * <pre>
  * public final class
  * Account {
  *
  *  private int balance;
  *  private final ArrayList&lt;Receiver&lt;Integer&gt;&gt; observers;
  *
  *  Account(final int initial) {
  *      balance = initial;
  *      observers = new ArrayList&lt;Receiver&lt;Integer&gt;&gt;();
  *  }
  *
  *  public void
  *  observe(final Receiver&lt;Integer&gt; observer) {
  *      if (null == observer) {
  *          throw new NullPointerException();
  *      }
  *      observers.add(observer);
  *  }
  *
  *  public int
  *  getBalance() { return balance; }
  *
  *  public void
  *  setBalance(final int newBalance) {
  *      balance = newBalance;
  *      for (final Receiver&lt;Integer&gt; observer : observers) {
  *          observer.apply(newBalance);
  *      }
  *  }
  * }
  * </pre>
  * <h4>Unanticipated termination</h4>
  * <p>A method can terminate execution of its plan by throwing an exception.
  * The plan may be terminated because it would violate one of the object's
  * invariants or because the request is malformed. Unfortunately, throwing an
  * exception may terminate not just the current plan, but also any other
  * currently executing plans. For example, if one of the observers throws a
  * {@link RuntimeException} from its <code>apply()</code> implementation, the
  * remaining observers are not notified of the new account balance. These other
  * observers may then continue operating using stale data about the account
  * balance.</p>
  * <h4>Nested execution</h4>
  * <p>When a method implementation invokes a method on another object, it
  * temporarily suspends progress on its own plan to let the called method
  * execute its plan. When the called method returns, the calling method
  * resumes its own plan where it left off. Unfortunately, the called method
  * may have changed the application state in such a way that resuming the
  * original plan no longer makes sense.  For example, if one of the observers
  * invokes <code>setBalance()</code> in its <code>apply()</code> implementation,
  * the remaining observers will first be notified of the balance after the
  * update, and then be notified of the balance before the update. Again, these
  * other observers may then continue operating using stale data about the
  * account balance.</p>
  * <h4>Interrupted transition</h4>
  * <p>A called method may also initiate an unanticipated state transition in
  * the calling object, while the current transition is still incomplete.  For
  * example, in the default state, an <code>Account</code> is always ready to
  * accept a new observer; however, this constraint is temporarily not met when
  * the observer list is being iterated over. An observer could catch the
  * <code>Account</code> in this transitional state by invoking
  * <code>observe()</code> in its <code>apply()</code> implementation. As a
  * result, a {@link java.util.ConcurrentModificationException} will be thrown
  * when iteration over the observer list resumes. Again, this exception prevents
  * notification of the remaining observers.</p>
  * <h3>Plan isolation</h3>
  * <p>The above plan interference problems are only possible because execution
  * of one plan is interleaved with execution of another. Interleaving plan
  * execution can be prevented by scheduling other plans for future execution,
  * instead of allowing them to preempt execution of the current plan. This
  * class provides control flow statements for scheduling future execution and
  * receiving its results.</p>
  * <h3>Naming convention</h3>
  * <p>Since the control flow statements defined by this class schedule future
  * execution, instead of immediate execution, they behave differently from the
  * native control flow constructs in the Java language. To make the difference
  * between eventual and immediate execution readily recognized by programmers
  * when scanning code, some naming conventions are proposed. By convention, an
  * instance of {@link Eventual} is held in a variable named "<code>_</code>".
  * Additional ways of marking eventual operations with the '<code>_</code>'
  * character are specified in the documentation for the methods defined by
  * this class. All of these conventions make eventual control flow
  * statements distinguishable by the character sequence "<code>_.</code>".
  * Example uses are also shown in the method documentation for this class. The
  * '<code>_</code>' character should only be used to identify eventual
  * operations so that a programmer can readily identify operations that are
  * expected to be eventual by looking for the <b><code>_.</code></b>
  * pseudo-operator.</p>
  * @see <a href="http://www.erights.org/talks/thesis/">Section 13.1
  *      "Sequential Interleaving Hazards" of "Robust Composition: Towards a
  *      Unified Approach to Access Control and Concurrency Control"</a>
  */
 public class
 Eventual implements Serializable {
     static private final long serialVersionUID = 1L;
 
     /**
      * {@link Local} permission
      */
     protected final Token local;
 
     /**
      * raw event loop
      */
     private   final Receiver<Promise<?>> enqueue;
     
     /**
      * URI for the event loop
      */
     protected final String here;
     
     /**
      * debugging output
      */
     public    final Log log;
     
     /**
      * destruct the vat
      * <p>
      * call like: <code>destruct.apply(null)</code>
      * </p>
      */
     public    final Receiver<?> destruct;
 
     /**
      * Constructs an instance.
      * @param local     {@link Local} permission
      * @param enqueue   raw event loop
      * @param here      URI for the event loop
      * @param log       {@link #log}
      * @param destruct  {@link #destruct}
      */
     public
     Eventual(final Token local, final Receiver<Promise<?>> enqueue,
              final String here, final Log log, final Receiver<?> destruct) {
         this.local = local;
         this.enqueue = enqueue;
         this.here = here;
         this.log = log;
         this.destruct = destruct;
     }
 
     /**
      * Constructs an instance.
      * @param enqueue   raw event loop
      */
     public
     Eventual(final Receiver<Promise<?>> enqueue) {
         this(new Token(), enqueue, "", new Log(), cast(Receiver.class,
                 new Rejected<Receiver<?>>(new NullPointerException())));
     }
 
     // org.ref_send.promise.Eventual interface
 
     /**
      * Registers an observer on a promise.
      * <p>
      * The <code>observer</code> will be notified of the <code>promise</code>'s
      * state at most once, in a future event loop turn. If there is no referent,
      * the <code>observer</code>'s {@link Do#reject reject} method will be
      * called with the reason; otherwise, the {@link Do#fulfill fulfill} method
      * will be called with either an immediate reference for a local referent,
      * or an {@linkplain #cast eventual reference} for a remote referent. For
      * example:
      * </p>
      * <pre>
     * import static org.ref_send.promise.Resolved.ref;
     * &hellip;
      * final Promise&lt;Account&gt; mine = &hellip;
      * final Promise&lt;Integer&gt; balance =
      *     _.when(mine, new Do&lt;Account,Integer&gt;() {
      *         public Integer
      *         fulfill(final Account x) { return x.getBalance(); }
      *     });
      * </pre>
      * <p>
      * A <code>null</code> <code>promise</code> argument is treated like a
      * rejected promise with a reason of {@link NullPointerException}.
      * </p>
      * <p>
      * Multiple observers registered on the same promise will be notified in
      * the same order as they were registered.
      * </p>
      * <p>
      * This method will not throw an {@link Exception}. Neither the
      * <code>promise</code>, nor the <code>observer</code>, argument will be
      * given the opportunity to execute in the current event loop turn.
      * </p>
      * @param <P> <code>observer</code>'s parameter type
      * @param <R> <code>observer</code>'s return type
      * @param promise   observed promise
      * @param observer  observer, MUST NOT be <code>null</code>
      * @return promise for the observer's return value
      */
     public final <P,R extends Serializable> Promise<R>
     when(final Promise<P> promise, final Do<P,R> observer) {
         try {
             return when(Object.class, promise, observer);
         } catch (final Exception e) { throw new Error(e); }
     }
 
     /**
      * Registers an observer on a promise.
      * <p>
      * The implementation behavior is the same as that documented for the
      * promise based {@link #when(Promise, Do) when} statement.
      * </p>
      * @param <P> <code>observer</code>'s parameter type
      * @param <R> <code>observer</code>'s return type
      * @param promise   observed promise
      * @param observer  observer, MUST NOT be <code>null</code>
      * @return promise, or {@linkplain #cast eventual reference}, for the
      *         <code>observer</code>'s return, or <code>null</code> if the
      *         <code>observer</code>'s return type is <code>Void</code>
      */
     public final <P,R> R
     when(final Promise<P> promise, final Do<P,R> observer) {
         try {
             final Class<?> R = Typedef.raw(Local.output(Object.class,observer));
             return cast(R, when(R, promise, observer));
         } catch (final Exception e) { throw new Error(e); }
     }
 
     /**
      * Registers an observer on a promise.
      * <p>
      * The implementation behavior is the same as that documented for the
      * promise based {@link #when(Promise, Do) when} statement.
      * </p>
      * @param <P> <code>observer</code>'s parameter type
      * @param promise   observed promise
      * @param observer  observer, MUST NOT be <code>null</code>
      */
     public final <P> void
     when(final Promise<P> promise, final Do<P,Void> observer) {
         try {
             when(Void.class, promise, observer);
         } catch (final Exception e) { throw new Error(e); }
     }
     
     /**
      * Registers an observer on an {@linkplain #cast eventual reference}.
      * <p>
      * The implementation behavior is the same as that documented for the
      * promise based {@link #when(Promise, Do) when} statement.
      * </p>
      * @param <P> <code>observer</code>'s parameter type
      * @param <R> <code>observer</code>'s return type
      * @param reference observed reference
      * @param observer  observer, MUST NOT be <code>null</code>
      * @return promise for the observer's return value
      */
     public final <P,R extends Serializable> Promise<R>
     when(final P reference, final Do<P,R> observer) {
         try {
             return when(Object.class, ref(reference), observer);
         } catch (final Exception e) { throw new Error(e); }
     }
     
     /**
      * Registers an observer on an {@linkplain #cast eventual reference}.
      * <p>
      * The implementation behavior is the same as that documented for the
      * promise based {@link #when(Promise, Do) when} statement.
      * </p>
      * @param <P> <code>observer</code>'s parameter type
      * @param <R> <code>observer</code>'s return type
      * @param reference observed reference
      * @param observer  observer, MUST NOT be <code>null</code>
      * @return promise, or {@linkplain #cast eventual reference}, for the
      *         <code>observer</code>'s return, or <code>null</code> if the
      *         <code>observer</code>'s return type is <code>Void</code>
      */
     public final <P,R> R
     when(final P reference, final Do<P,R> observer) {
         try {
             final Class<?> R = Typedef.raw(Local.output(null != reference ?
                     reference.getClass() : Object.class, observer));
             return cast(R, when(R, ref(reference), observer));
         } catch (final Exception e) { throw new Error(e); }
     }
 
     /**
      * Registers an observer on an {@linkplain #cast eventual reference}.
      * <p>
      * The implementation behavior is the same as that documented for the
      * promise based {@link #when(Promise, Do) when} statement.
      * </p>
      * @param <P> <code>observer</code>'s parameter type
      * @param reference observed reference
      * @param observer  observer, MUST NOT be <code>null</code>
      */
     public final <P> void
     when(final P reference, final Do<P,Void> observer) {
         try {
             when(Void.class, ref(reference), observer);
         } catch (final Exception e) { throw new Error(e); }
     }
     
     protected final <P,R> Promise<R>
     when(final Class<?> R, final Promise<P> p, final Do<P,R> observer) {
         final Promise<R> r;
         final Do<P,?> forwarder;
         if (void.class == R || Void.class == R) {
             r = null;
             forwarder = observer;
         } else {
             final Channel<R> x = defer();
             r = x.promise;
             forwarder = new Compose<P,R>(observer, x.resolver);
         }
         trust(p).when(forwarder);
         return r;
     }
 
     private final <T> Local<T>
     trust(final Promise<T> untrusted) {
         return null == untrusted ?
             new Enqueue<T>(this, new Rejected<T>(new NullPointerException())) :
         Local.trusted(local, untrusted) ?
             (Local<T>)untrusted :
         new Enqueue<T>(this, untrusted);
     }
 
     /**
      * number of tasks enqueued
      * <p>
      * This variable is only incremented and should never be allowed to wrap.
      * </p>
      */
     private long tasks;
 
     static private final class
     Enqueue<T> extends Local<T> {
         static private final long serialVersionUID = 1L;
 
         final Promise<T> untrusted;
 
         Enqueue(final Eventual _, final Promise<T> untrusted) {
             super(_, _.local);
             this.untrusted = untrusted;
         }
 
         public int
         hashCode() { return 0x174057ED; }
 
         public boolean
         equals(final Object x) {
             return x instanceof Enqueue<?> &&
                 _.equals(((Enqueue<?>)x)._) &&
                 (null != untrusted
                     ? untrusted.equals(((Enqueue<?>)x).untrusted)
                     : null == ((Enqueue<?>)x).untrusted);
         }
 
         public T
         call() throws Exception { return untrusted.call(); }
 
         public void
         when(final Do<T,?> observer) {
             final long id = ++_.tasks;
             if (0 == id) { throw new AssertionError(); }
             class Sample extends Struct implements Promise<Void>, Serializable {
                 static private final long serialVersionUID = 1L;
 
                 public Void
                 call() throws Exception {
                     // AUDIT: call to untrusted application code
                     try {
                         sample(untrusted, observer, _.log, _.here + "#t" + id);
                     } catch (final Exception e) {
                         _.log.problem(e);
                         throw e;
                     }
                     return null;
                 }
             }
             _.enqueue.apply(new Sample());
             _.log.sent(_.here + "#t" + id);
         }
     }
     
     static private final Method fulfill;
     static {
         try {
             fulfill = Reflection.method(Do.class, "fulfill", Object.class);
         } catch (final NoSuchMethodException e) {throw new NoSuchMethodError();}
     }
     
     static private final Method reject;
     static {
         try {
             reject = Reflection.method(Do.class, "reject", Exception.class);
         } catch (final NoSuchMethodException e) {throw new NoSuchMethodError();}
     }
     
     static private <P,R> R
     sample(final Promise<P> promise, final Do<P,R> observer,
            final Log log, final String message) throws Exception {
         final P a;
         try {
             a = promise.call();
             ref(a).call();      // ensure the called value is not one that is
                                 // expected to be handled as a rejection
         } catch (final Exception reason) {
             final Class<?> c = (observer instanceof Compose<?,?>
                 ? ((Compose<?,?>)observer).block : observer).getClass();
             log.got(message, c, reject);
             return observer.reject(reason);
         }
         final Method m;
         final Class<?> c; {
             final Do<?,?> inner = observer instanceof Compose<?,?> ?
                     ((Compose<?,?>)observer).block : observer;
             if (inner instanceof Invoke<?>) {
                 m = ((Invoke<?>)inner).method;
                 c = a.getClass();
             } else {
                 m = fulfill; 
                 c = inner.getClass();
             }
         }
         log.got(message, c, m);
         return observer.fulfill(a);
     }
 
     /**
      * A registered promise observer.
      * @param <T> referent type
      */
     static private final class
     When<T> implements Equatable, Serializable {
         static private final long serialVersionUID = 1L;
 
         long condition;             // id for the corresponding promise
         long message;               // id for this when block
         Do<T,?> observer;           // client's when block code
         Promise<When<T>> next;      // next when block registered on the promise
     }
     
     /**
      * number of when blocks created
      * <p>
      * This variable is only incremented and should never be allowed to wrap.
      * </p>
      */
     private long whens;
     
     /**
      * pool of previously used when blocks
      * <p>
      * When blocks are recycled so that environments providing orthogonal
      * persistence don't accumulate lots of dead objects.
      * </p>
      */
     private Promise<When<?>> whenPool;
     
     private final @SuppressWarnings("unchecked") <T> Promise<When<T>>
     allocWhen(final long condition) {
         final long message = ++whens;
         if (0 == message) { throw new AssertionError(); }
         
         final Promise<When<T>> r;
         final When<T> block;
         if (null == whenPool) {
             block = new When<T>();
             r = ref(block);
         } else {
             r = (Promise)whenPool;
             block = (When)near(r);
             whenPool = (Promise)block.next;
             block.next = null;
         }
         block.condition = condition;
         block.message = message;
         return r;
     }
     
     private final @SuppressWarnings("unchecked") void
     freeWhen(final Promise pBlock, final When block) {
         block.condition = 0;
         block.message = 0;
         block.observer = null;
         block.next = (Promise)whenPool;
         whenPool = pBlock;
     }
     
     private final class
     Forward<T> extends Struct implements Promise<Void>, Serializable {
         static private final long serialVersionUID = 1L;
 
         private final boolean ignored;          // resolution ignored so far?
         private final long condition;           // id of corresponding promise
         private final Promise<T> value;         // resolved value of promise
         private final Promise<When<T>> pending; // when block to run
         
         Forward(final boolean ignored, final long condition,
                 final Promise<T> value, final Promise<When<T>> pending) {
             this.ignored = ignored;
             this.condition = condition;
             this.value = value;
             this.pending = pending;
         }
 
         /**
          * Notifies the next observer of the resolved value.
          */
         public Void
         call() throws Exception {
             final long message;
             final Do<T,?> observer;
             final Promise<When<T>> next; {
                 final When<T> block;
                 try {
                     block = pending.call();
                 } catch (final Exception e) {
                     /*
                      * There was a problem loading the saved when block. Ignore
                      * it and all subsequent when blocks on this promise.
                      */
                     log.problem(e);
                     throw e;
                 }
                 if (condition != block.condition) { return null; } // been done
                 
                 // free the block, thus ensuring it is not run again
                 message     = block.message;
                 observer    = block.observer;
                 next        = block.next;
                 freeWhen(pending, block);
             }
             
             if (null != next) {
                 enqueue.apply(new Forward<T>(false, condition, value, next));
                 try {
                     if (Local.trusted(local, value)) {
                         log.got(here + "#w" + message, null, null);
                         ((Local<T>)value).when(observer);
                     } else {
                         // AUDIT: call to untrusted application code
                         sample(value, observer, log, here + "#w" + message);
                     }
                 } catch (final Exception e) {
                     log.problem(e);
                     throw e;
                 }
             } else if (ignored && value instanceof Rejected<?>) {
                 final Exception e = ((Rejected<?>)value).reason;
                 log.got(here + "#w" + message, null, null);
                 log.sentIf(here + "#w" + message, here + "#p" + condition);
                 log.problem(e);
                 throw e;
             }
             return null;
         }
     }
     
     private final class
     State<T> extends Milestone<Promise<T>> {
         static private final long serialVersionUID = 1L;
         
         private final long condition;           // id of this promise
         private       Promise<When<T>> back;    // observer list sentinel
         
         State(final long condition, final Promise<When<T>> back) {
             this.condition = condition;
             this.back = back;
         }
         
         protected void
         observe(final Do<T,?> observer) {
             final When<T> block = near(back);
             if (condition == block.condition) {
                 log.sentIf(here+"#w"+block.message, here+"#p"+condition);
                 block.observer = observer;
                 back = block.next = allocWhen(condition);
             } else {
                 /**
                  * Promise is already resolved and all previously registered
                  * when blocks run. Start a new when block chain and kick off a
                  * new when block running task.
                  */
                 back = allocWhen(condition);
                 enqueue.apply(new Forward<T>(false, condition, get(), back));
                 observe(observer);
             }
         }
     }
     
     static private final class
     Tail<T> extends Local<T> {
         static private final long serialVersionUID = 1L;
 
         private final Promise<State<T>> state;      // promise's mutable state
 
         Tail(final Eventual _, final State<T> state) {
             super(_, _.local);
             this.state = new Fulfilled<State<T>>(false, state);
         }
 
         public int
         hashCode() { return 0x3EFF7A11; }
 
         public boolean
         equals(final Object x) {
             return x instanceof Tail<?> && state.equals(((Tail<?>)x).state);
         }
 
         public T
         call() throws Exception {
             final Promise<T> value = state.call().get();
             if (null == value) { throw new Unresolved(); }
             return value.call();
         }
 
         public void
         when(final Do<T,?> observer) { near(state).observe(observer); }
     }
     
     private final class
     Head<T> extends Struct implements Resolver<T>, Serializable {
         static private final long serialVersionUID = 1L;
 
         private final long condition;           // id of corresponding promise
         private final Promise<State<T>> state;  // promise's mutable state
         private final Promise<When<T>> front;   // first when block to run
         
         Head(final long condition, final State<T> state,
                                    final Promise<When<T>> front) {
             this.condition = condition;
             this.state = new Fulfilled<State<T>>(true, state);
             this.front = front;
 
             /*
              * The resolver only holds a weak reference to the promise's mutable
              * state, allowing it to be garbage collected even if the resolver
              * is still held. This implementation takes advantage of a common
              * pattern in which a when block is registered on a promise as soon
              * as it is created, but no other reference to the promise is
              * retained. Combined with the recycling of when blocks, this common
              * pattern generates no dead objects. Much of the implementation's
              * complexity is in service to this goal.
              */
         }
         
         public void
         apply(final T r) { resolve(null == r ? null : ref(r)); }
 
         public void
         reject(final Exception reason) { resolve(new Rejected<T>(reason)); }
         
         public void
         resolve(Promise<T> p) {
             if (p instanceof Fulfilled<?>) {
                 p = ((Fulfilled<T>)p).getState(); 
                 log.fulfilled(here + "#p" + condition);
             } else if (null == p) {
                 p = new Inline<T>(null);
                 log.rejected(here + "#p" + condition, null);
             } else if (p instanceof Rejected<?>) {
                 log.rejected(here + "#p" + condition, ((Rejected<T>)p).reason);
             } else {
                 log.resolved(here + "#p" + condition);
             }
             enqueue.apply(new Forward<T>(true, condition, p, front));
             try {
                 final State<T> cell = state.call();
                 if (null != cell) { cell.set(p); }
             } catch (final Exception e) {}
         }
 
         public void
         progress() { log.progressed(here + "#p" + condition); }
     }
     
     /**
      * number of promises {@linkplain #defer created}
      * <p>
      * This variable is only incremented and should never be allowed to wrap.
      * </p>
      */
     private long deferrals;
 
     /**
      * Creates a promise in the unresolved state.
      * <p>
      * The return from this method is a ( {@linkplain Promise promise},
      * {@linkplain Resolver resolver} ) pair. The promise is initially in the
      * unresolved state and can only be resolved by the resolver once. If the
      * promise is {@linkplain Resolver#apply fulfilled}, the promise will
      * forever refer to the provided referent. If the promise, is
      * {@linkplain Resolver#reject rejected}, the promise will forever be in the
      * rejected state, with the provided reason. If the promise is
      * {@linkplain Resolver#resolve resolved}, the promise will forever be in
      * the same state as the provided promise. After this initial state
      * transition, all subsequent invocations of either {@link Resolver#apply
      * fulfill}, {@link Resolver#reject reject} or {@link Resolver#resolve
      * resolve} are silently ignored. Any {@linkplain Do observer}
      * {@linkplain #when registered} on the promise will only be notified after
      * the promise is resolved.
      * </p>
      * @param <T> referent type
      * @return ( {@linkplain Promise promise}, {@linkplain Resolver resolver} )
      */
     public final <T> Channel<T>
     defer() {
         final long condition = ++deferrals;
         if (0 == condition) { throw new AssertionError(); }
         final Promise<When<T>> front = allocWhen(condition);
         final State<T> state = new State<T>(condition, front);
         return new Channel<T>(new Tail<T>(this, state),
                               new Head<T>(condition, state, front));
     }
 
     /**
      * Ensures a reference is an eventual reference.
      * <p>
      * An eventual reference queues invocations, instead of processing them
      * immediately. Each queued invocation will be processed, in order, in a
      * future event loop turn.
      * </p>
      * <p>
      * Use this method to vet received arguments. For example:
      * </p>
      * <pre>
      * import static org.joe_e.ConstArray.array;
      *
      * public final class
      * Account {
      *
      *     private final Eventual _;
      *     private int balance;
      *     private ConstArray&lt;Receiver&lt;Integer&gt;&gt; observer_s;
      *
      *     public
      *     Account(final Eventual _, final int initial) {
      *         this._ = _;
      *         balance = initial;
      *         observer_s = array();
      *     }
      *
      *     public void
      *     observe(final Receiver&lt;Integer&gt; observer) {
      *         // Vet the received arguments.
      *         final Receiver&lt;Integer&gt; observer_ = _.<b>_</b>(observer);
      *
      *         // Use the <em>vetted</em> arguments.
      *         observer_s = observer_s.with(observer_);
      *     }
      *
      *     public int
      *     getBalance() { return balance; }
      *
      *     public void
      *     setBalance(final int newBalance) {
      *          balance = newBalance;
      *          for (final Receiver&lt;Integer&gt; observer_ : observer_s) {
      *              // Schedule future execution of notification.
      *              observer_.apply(newBalance);
      *          }
      *     }
      * }
      * </pre>
      * <p>
      * By convention, the return from this method is held in a variable whose
      * name is suffixed with an '<code>_</code>' character. The main part of the
      * variable name should use Java's camelCaseConvention. A list of eventual
      * references is suffixed with "<code>_s</code>". This naming convention
      * creates the appearance of a new operator in the Java language, the
      * eventual operator: "<code><b>_.</b></code>".
      * </p>
      * <p>
      * If this method returns successfully, the returned eventual reference
      * will not throw an {@link Exception} on invocation of any of the methods
      * defined by its type, provided the invoked method's return type is either
      * <code>void</code>, an {@linkplain Proxies#isImplementable allowed} proxy
      * type or assignable from {@link Promise}. Invocations on the eventual
      * reference will not give the <code>referent</code>, nor any of the
      * invocation arguments, an opportunity to execute in the current event loop
      * turn.
      * </p>
      * <p>
      * Invocations of methods defined by {@link Object} are <strong>not</strong>
      * queued, and so can cause plan interference, or throw an exception.
      * </p>
      * <p>
      * This method will not throw an {@link Exception}.
      * </p>
      * @param <T> referent type, MUST be an
      *            {@linkplain Proxies#isImplementable allowed} proxy type
      * @param referent  immediate or eventual reference,
      *                  MUST be non-<code>null</code>
      * @return corresponding eventual reference
      * @throws Error    <code>null</code> <code>referent</code> or
      *                  <code>T</code> not an
      *                  {@linkplain Proxies#isImplementable allowed} proxy type
      */
     public final <T> T
     _(final T referent) {
         if (referent instanceof Proxy) {
             try {
                 final Object handler = Proxies.getHandler((Proxy)referent);
                 if ((null != handler && Rejected.class == handler.getClass()) ||
                     Local.trusted(local, handler)) { return referent; }
             } catch (final Exception e) {}
         }
         try {
           return cast(referent.getClass(), new Enqueue<T>(this, ref(referent)));
         } catch (final Exception e) { throw new Error(e); }
     }
 
     /**
      * Casts a promise to a specified type.
      * <p>
      * For example,
      * </p>
      * <pre>
      *  final Channel&lt;Receiver&lt;Integer&gt;&gt; x = _.defer();
      *  final Receiver&lt;Integer&gt; r_ = cast(Receiver.class, x.promise); 
      * </pre>
      * @param <T> referent type to implement
      * @param type      referent type to implement
      * @param promise   promise for the referent
      * @return reference of corresponding type
      * @throws ClassCastException   no cast to <code>type</code>
      */
     static public @SuppressWarnings("unchecked") <T> T
     cast(final Class<?> type,final Promise<T> promise)throws ClassCastException{
         return (T)(Void.class == type || void.class == type ?
                 null :
             Float.class == type || float.class == type ?
                 Float.NaN :
             Double.class == type || double.class == type ?
                 Double.NaN :
             null == promise ?
                 cast(type, new Rejected<T>(new NullPointerException())) :
             type.isInstance(promise) ?
                 promise :
             Selfless.class == type ?
                 proxy((InvocationHandler)promise, Selfless.class) :
             type.isInterface() ?
                 proxy((InvocationHandler)promise, type, Selfless.class) :
             proxy((InvocationHandler)promise, ifaces(type)));
     }
     
     /**
      * Lists the proxy interfaces for a concrete type.
      * @param concrete  type to mimic
      */
     static private Class<?>[]
     ifaces(final Class<?> concrete) {
         // build the list of types to implement
         Class<?>[] types = virtualize(concrete);
         boolean selfless = false;
         for (final Class<?> i : types) {
             selfless = Selfless.class.isAssignableFrom(i);
             if (selfless) { break; }
         }
         if (!selfless) {
             final int n = types.length;
             System.arraycopy(types, 0, types = new Class[n + 1], 0, n);
             types[n] = Selfless.class;
         }
         return types;
     }
 
     /**
      * Lists the allowed interfaces implemented by a type.
      * @param base  base type
      * @return allowed interfaces implemented by <code>base</code>
      */
     static private Class<?>[]
     virtualize(final Class<?> base) {
         Class<?>[] r = base.getInterfaces();
         int i = r.length;
         final Class<?> parent = base.getSuperclass();
         if (null != parent && Object.class != parent) {
             final Class<?>[] p = virtualize(parent);
             if (0 != p.length) {
                 System.arraycopy(r, 0, r = new Class<?>[i + p.length], 0, i);
                 System.arraycopy(p, 0, r, i, p.length);
             }
         }
         while (i-- != 0) {
             final Class<?> type = r[i];
             if (type == Serializable.class ||
                     !Proxies.isImplementable(type) ||
                     JoeE.isSubtypeOf(type, Immutable.class) ||
                     JoeE.isSubtypeOf(type, Equatable.class)) {
                 final Class<?>[] x = virtualize(r[i]);
                 final Class<?>[] c = r;
                 r = new Class<?>[c.length - 1 + x.length];
                 System.arraycopy(c, 0, r, 0, i);
                 System.arraycopy(x, 0, r, i, x.length);
                 System.arraycopy(c, i + 1, r, i + x.length, c.length - (i+1));
             }
         }
         return r;
     }
     
     /**
      * Gets a corresponding {@linkplain Promise promise}.
      * <p>
      * This method is the inverse of {@link #cast cast}; it gets the
      * corresponding {@linkplain Promise promise} for a given reference.
      * </p>
      * <p>
      * This method will not throw an {@link Exception}.
      * </p>
      * @param <T> referent type
      * @param referent immediate or eventual reference
      */
     static public @SuppressWarnings("unchecked") <T> Promise<T>
     ref(final T referent) {
         if (referent instanceof Promise) { return (Promise<T>)referent; }
         if (referent instanceof Proxy) {
             try {
                 final Object handler = Proxies.getHandler((Proxy)referent);
                 if (handler instanceof Promise) {
                     return handler instanceof Enqueue
                         ? ((Enqueue<T>)handler).untrusted : (Promise<T>)handler;
                 }
             } catch (final Exception e) {}
         }
         try {
             if (null == referent)   { throw new NullPointerException(); }
             if (referent instanceof Double) {
                 final Double d = (Double)referent;
                 if (d.isNaN())      { throw new ArithmeticException(); }
                 if (d.isInfinite()) { throw new ArithmeticException(); }
             } else if (referent instanceof Float) {
                 final Float f = (Float)referent;
                 if (f.isNaN())      { throw new ArithmeticException(); }
                 if (f.isInfinite()) { throw new ArithmeticException(); }
             }
             return new Fulfilled<T>(false, referent);
         } catch (final Exception e) {
             return reject(e);
         }
     }
     
     /**
      * Gets a corresponding immediate reference.
      * <p>
      * This method is the inverse of {@link #_(Object) _}; it gets the
      * corresponding immediate reference for a given eventual reference.
      * </p>
      * <p>
      * This method will not throw an {@link Exception}.
      * </p>
      * @param <T> referent type
      * @param reference possibly eventual reference for a local referent
      * @return corresponding immediate reference
      */
     static public <T> T
     near(final T reference) { return near(ref(reference)); }
 
     /**
      * Gets a corresponding immediate reference.
      * <p>
      * This method is the inverse of {@link #ref ref}.
      * </p>
      * <p>
      * This method will not throw an {@link Exception}.
      * </p>
      * @param <T> referent type
      * @param promise   a promise
      * @return {@linkplain #cast corresponding} reference
      */
     static public <T> T
     near(final Promise<T> promise) {
         try {
             return promise.call();
         } catch (final Exception e) { throw new Error(e); }
     }
     
     /**
      * Constructs a rejected {@linkplain Promise promise}.
      * @param <T> referent type
      * @param reason    rejection reason
      */
     static public <T> Promise<T>
     reject(final Exception reason) { return new Rejected<T>(reason); }
     
     /**
      * Creates a sub-vat.
      * <p>
      * All created vats will be destructed when this vat is
      * {@linkplain Vat#destruct destructed}.
      * </p>
      * <p>
      * The <code>maker</code> MUST have a method with signature:
      * </p>
      * <pre>
      * static public R
      * make({@link Eventual} _, &hellip;)
      * </pre>
      * <p>
      * All of the parameters in the make method are optional, but MUST appear
      * in the order shown if present.
      * </p>
      * <p>
      * This method will not throw an {@link Exception}. None of the arguments
      * will be given the opportunity to execute in the current event loop turn.
      * </p>
      * @param <R> return type, MUST be either an interface, or a {@link Promise}
      * @param label     optional vat label,
      *                  if <code>null</code> a label will be generated
      * @param maker     constructor class
      * @param optional  more arguments for <code>maker</code>'s make method
      * @return sub-vat permissions, including a promise for the object returned
      *         by the <code>maker</code>
      */
     public <R> Vat<R>
     spawn(final String label, final Class<?> maker, final Object... optional) {
         /**
          * The default implementation just calls the make method in a separate
          * event loop turn.
          */
         final Invoke<Class<?>> invoke;
         try {
             Method make = null;
             for (final Method m : Reflection.methods(maker)) {
                 if ("make".equals(m.getName()) &&
                         Modifier.isStatic(m.getModifiers())) {
                     make = m;
                     break;
                 }
             }
             final Class<?>[] paramv = make.getParameterTypes();
             final ConstArray.Builder<Object> argv =
                 ConstArray.builder(paramv.length);
             if (0 != paramv.length && Eventual.class == paramv[0]) {
                 argv.append(this);
             }
             for (final Object x : optional) { argv.append(x); }
             invoke = new Invoke<Class<?>>(make, argv.snapshot());
         } catch (final Exception e) { throw new Error(e); }
         final Receiver<?> destruct = cast(Receiver.class, null);
         final @SuppressWarnings("unchecked") R top = (R)when(maker, invoke); 
         return new Vat<R>(top, destruct);
     }
 
     // Debugging assistance
 
     /**
      * Causes a compile error for code that attempts to create an
      * {@linkplain #cast eventual reference} of a concrete type.
      * <p>
      * If you encounter a compile error because your code is linking to this
      * method, insert an explicit cast to the
      * {@linkplain Proxies#isImplementable allowed} proxy type. For example,
      * </p>
      * <kbd>_._(this).apply(null);</kbd>
      * <p>becomes:</p>
      * <kbd>_._((Receiver&lt;?&gt;)this).apply(null);</kbd>
      * @param x ignored
      * @throws AssertionError   always thrown
      */
     public final <T extends Serializable> void
     _(final T x) { throw new AssertionError(); }
 
     /**
      * Causes a compile error for code that attempts to cast a promise to a
      * concrete type.
      * <p>
      * If you encounter a compile error because your code is linking to this
      * method, replace the specified concrete type with an
      * {@linkplain Proxies#isImplementable allowed} proxy type. For example,
      * </p>
      * <kbd>final Observer o_ = _.cast(Observer.class, op);</kbd>
      * <p>becomes:</p>
      * <kbd>final Receiver&lt;?&gt; o_ = _.cast(Receiver.class, op);</kbd>
      * @param <R> referent type to implement
      * @param type      ignored
      * @param promise   ignored
      * @throws AssertionError   always thrown
      */
     static public <R extends Serializable> void
     cast(final Class<R> type,
          final Promise<?> promise) { throw new AssertionError();}
 }
