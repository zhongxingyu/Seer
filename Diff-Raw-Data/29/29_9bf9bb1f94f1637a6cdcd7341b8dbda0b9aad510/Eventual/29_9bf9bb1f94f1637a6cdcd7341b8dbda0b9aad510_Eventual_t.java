 // Copyright 2006-2008 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.ref_send.promise;
 
 import static org.joe_e.reflect.Proxies.proxy;
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.Proxy;
 
 import org.joe_e.Equatable;
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
  *  private final ArrayList&lt;Observer&gt; observers;
  *
  *  Account(final int initial) {
  *      balance = initial;
  *      observers = new ArrayList&lt;Observer&gt;();
  *  }
  *
  *  public void
  *  observe(final Observer observer) throws NullPointerException {
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
  *      for (final Observer observer : observers) {
  *          observer.currentBalance(newBalance);
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
  * {@link RuntimeException} from its <code>currentBalance()</code>
  * implementation, the remaining observers are not notified of the new account
  * balance. These other observers may then continue operating using stale data
  * about the account balance.</p>
  * <h4>Nested execution</h4>
  * <p>When a method implementation invokes a method on another object, it
  * temporarily suspends progress on its own plan to let the called method
  * execute its plan. When the called method returns, the calling method
  * resumes its own plan where it left off. Unfortunately, the called method
  * may have changed the application state in such a way that resuming the
  * original plan no longer makes sense.  For example, if one of the observers
  * invokes <code>setBalance()</code> in its <code>currentBalance()</code>
  * implementation, the remaining observers will first be notified of the
  * balance after the update, and then be notified of the balance before the
  * update. Again, these other observers may then continue operating using
  * stale data about the account balance.</p>
  * <h4>Interrupted transition</h4>
  * <p>A called method may also initiate an unanticipated state transition in
  * the calling object, while the current transition is still incomplete.  For
  * example, in the default state, an <code>Account</code> is always ready to
  * accept a new observer; however, this constraint is temporarily not met when
  * the observer list is being iterated over. An observer could catch the
  * <code>Account</code> in this transitional state by invoking
  * <code>observe()</code> in its <code>currentBalance()</code> implementation.
  * As a result, a {@link java.util.ConcurrentModificationException} will be
  * thrown when iteration over the observer list resumes. Again, this exception
  * prevents notification of the remaining observers.</p>
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
 Eventual implements Receiver<Promise<?>>, Serializable {
     static private final long serialVersionUID = 1L;
 
     /**
      * {@link Deferred} permission
      */
     protected final Token deferred;
 
     /**
      * raw {@link #run event loop}
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
      * call like: <code>destruct.run(null)</code>
      * </p>
      */
     public    final Receiver<?> destruct;
 
     /**
      * Constructs an instance.
      * @param deferred  {@link Deferred} permission
      * @param enqueue   raw {@link #run event loop}
      * @param here      URI for the event loop
      * @param log       {@link #log}
      * @param destruct  {@link #destruct}
      */
     public
     Eventual(final Token deferred, final Receiver<Promise<?>> enqueue,
              final String here, final Log log, final Receiver<?> destruct) {
         this.deferred = deferred;
         this.enqueue = enqueue;
         this.here = here;
         this.log = log;
         this.destruct = destruct;
     }
 
     /**
      * Constructs an instance.
      * @param enqueue   raw {@link #run event loop}
      */
     public
     Eventual(final Receiver<Promise<?>> enqueue) {
         this(new Token(), enqueue, "", new Log(), new Rejected<Receiver<?>>(
                 new NullPointerException())._(Receiver.class));
     }
 
    // org.ref_send.promise.Receiver interface
 
     /**
      * number of tasks {@link #run enqueued}
      * <p>
      * This variable is only incremented and should never be allowed to wrap.
      * </p>
      */
     private long tasks;
     
     static private final Method call;
     static {
         try {
             call = Reflection.method(Promise.class, "call");
         } catch (final NoSuchMethodException e) {throw new NoSuchMethodError();}
     }
     
     /**
      * Schedules a task for execution in a future turn.
      * <p>
      * The implementation preserves the <i>F</i>irst <i>I</i>n <i>F</i>irst
      * <i>O</i>ut ordering of tasks, meaning the tasks will be
      * {@linkplain Promise#call executed} in the same order as they were
      * enqueued.
      * </p>
      */
     public final void
     run(final Promise<?> task) {
         if (null == task) { return; }
         
         final long id = ++tasks;
         if (0 == id) { throw new AssertionError(); }
         class TaskX extends Struct implements Promise<Void>, Serializable {
             static private final long serialVersionUID = 1L;
 
             public Void
             call() throws Exception {
                 log.got(here + "#t" + id, task.getClass(), call);
                 try { task.call(); } catch (final Exception e) {log.problem(e);}
                 return null;
             }
         }
         enqueue.run(new TaskX());
         log.sent(here + "#t" + id);
     }
 
    // org.ref_send.promise.Eventual interface
 
     /**
      * Registers an observer on a promise.
      * <p>
      * The <code>observer</code> will be notified of the <code>promise</code>'s
      * state at most once, in a future {@linkplain #run event loop} turn. If
      * there is no referent, the <code>observer</code>'s
      * {@link Do#reject reject} method will be called with the reason;
      * otherwise, the {@link Do#fulfill fulfill} method will be called with
      * either an immediate reference for a local referent, or an
      * {@linkplain #cast eventual reference} for a remote referent. For example:
      * </p>
      * <pre>
      * import static org.ref_send.promise.Resolved.ref;
      * &hellip;
      * final Promise&lt;Account&gt; mine = &hellip;
      * final Promise&lt;Integer&gt; balance =
      *     _.when(mine, new Do&lt;Account,Promise&lt;Integer&gt;&gt;() {
      *         public Promise&lt;Integer&gt;
      *         fulfill(final Account x) { return ref(x.getBalance()); }
      *     });
      * </pre>
      * <p>
      * A <code>null</code> <code>promise</code> argument is treated like an
      * instance of {@link Rejected} with a {@link Rejected#reason reason} of
      * {@link NullPointerException}.
      * </p>
      * <p>Multiple observers registered on the same promise will be notified in
      * the same order as they were registered.</p>
      * <p>
      * This method will not throw an {@link Exception}. Neither the
      * <code>promise</code>, nor the <code>observer</code>, argument will be
      * given the opportunity to execute in the current event loop turn.
      * </p>
      * @param <T> referent type
      * @param <R> <code>observer</code>'s return type, MUST be {@link Void}, an
      *            {@linkplain Proxies#isImplementable allowed} proxy type, or
      *            assignable from {@link Promise} 
      * @param promise   observed promise
      * @param observer  observer, MUST NOT be <code>null</code>
      * @return promise, or {@linkplain #cast eventual reference}, for the
      *         <code>observer</code>'s return, or <code>null</code> if the
      *         <code>observer</code>'s return type is <code>Void</code>
      * @throws Error    invalid <code>observer</code> argument  
      */
     public final <T,R> R
     when(final Promise<T> promise, final Do<T,R> observer) {
         return when(Object.class, promise, observer);
     }
     
     protected final <T,R> R
     when(final Class<?> T, final Promise<T> p, final Do<T,R> observer) {
         try {
             final R r;
             final Do<T,?> forwarder;
             final Class<?> R = Typedef.raw(Deferred.output(T, observer));
             if (void.class == R || Void.class == R) {
                 r = null;
                 forwarder = observer;
             } else {
                 final Channel<R> x = defer();
                 r = cast(R, x.promise);
                 forwarder = new Compose<T,R>(observer, x.resolver);
             }
             trust(p instanceof Detachable ? ((Detachable<T>)p).getState() : p).
                 when(forwarder);
             return r;
         } catch (final Exception e) { throw new Error(e); }
     }
 
     private final <T> Deferred<T>
     trust(final Promise<T> untrusted) {
         return null == untrusted
             ? new Enqueue<T>(this, new Rejected<T>(new NullPointerException()))
         : Deferred.trusted(deferred, untrusted)
             ? (Deferred<T>)untrusted
         : new Enqueue<T>(this, untrusted);
     }
 
     static private final class
     Enqueue<T> extends Deferred<T> {
         static private final long serialVersionUID = 1L;
 
         final Promise<T> untrusted;
 
         Enqueue(final Eventual _, final Promise<T> untrusted) {
             super(_, _.deferred);
             this.untrusted = untrusted;
         }
 
         public int
         hashCode() { return 0x174057ED; }
 
         public boolean
         equals(final Object x) {
             return x instanceof Enqueue &&
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
             _.enqueue.run(new Sample());
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
             a = ref(promise.call()).call();
         } catch (final Exception reason) {
             final Class<?> c = (observer instanceof Compose
                 ? ((Compose<?,?>)observer).block : observer).getClass();
             log.got(message, c, reject);
             return observer.reject(reason);
         }
         final Method m;
         final Class<?> c; {
             final Do<?,?> inner = observer instanceof Compose
                 ? ((Compose<?,?>)observer).block : observer;
             if (inner instanceof Invoke) {
                 m = ((Invoke<?>)inner).method;
                 c = Modifier.isStatic(m.getModifiers()) ? null : a.getClass();
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
         Promise<When<T>> next;     // next when block registered on the promise
     }
     
     /**
      * number of deferred when blocks created
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
 
         private final long condition;           // id of corresponding promise
         private final Promise<T> value;        // resolved value of promise
         private final Promise<When<T>> next;   // when block to run
         
         Forward(final long condition, final Promise<T> value,
                 final Promise<When<T>> next) {
             this.condition = condition;
             this.value = value;
             this.next = next;
         }
 
         /**
          * Notifies the next observer of the resolved value.
          */
         public Void
         call() throws Exception {
             final When<T> block;
             try {
                 block = next.call();
             } catch (final Exception e) {
                 /*
                  * There was a problem loading the saved when block. Ignore it
                  * and all subsequent when blocks registered on this promise.
                  */
                 log.problem(e);
                 throw e;
             }
             if (condition != block.condition) { return null; }  // already done
             block.condition = 0;    // ensure block is not run again
             
             if (null != block.next) {
                 enqueue.run(new Forward<T>(condition, value, block.next));
                 try {
                     final String message = here + "#w" + block.message;
                     if (Deferred.trusted(deferred, value)) {
                         log.got(message, null, null);
                         ((Deferred<T>)value).when(block.observer);
                     } else {
                         // AUDIT: call to untrusted application code
                         sample(value, block.observer, log, message);
                     }
                 } catch (final Exception e) {
                     log.problem(e);
                     throw e;
                 }
             }
             freeWhen(next, block);
             return null;
         }
     }
     
     private final class
     State<T> extends Milestone<Promise<T>> {
         static private final long serialVersionUID = 1L;
         
         private final long condition;       // id of this promise
         private Promise<When<T>> back;      // observer list sentinel
         
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
                 enqueue.run(new Forward<T>(condition, get(), back));
                 observe(observer);
             }
         }
     }
     
     static private final class
     Tail<T> extends Deferred<T> {
         static private final long serialVersionUID = 1L;
 
         private final State<T> state;   // mutable store for promise's value
 
         Tail(final Eventual _, final State<T> state) {
             super(_, _.deferred);
             this.state = state;
         }
 
         public int
         hashCode() { return (int)state.condition + 0x3EFF7A11; }
 
         public boolean
         equals(final Object x) {
             return x instanceof Tail && state.equals(((Tail<?>)x).state);
         }
 
         public T
         call() throws Exception { return state.get().call(); }
 
         public void
         when(final Do<T,?> observer) { state.observe(observer); }
     }
     
     private final class
     Head<T> extends Struct implements Resolver<T>, Serializable {
         static private final long serialVersionUID = 1L;
 
         private final long condition;           // id of corresponding promise
         private final Promise<State<T>> state;  // promise's mutable state
         private final Promise<When<T>> front;   // first when block to run
         
         Head(final long condition, final Promise<State<T>> state,
                                    final Promise<When<T>> front) {
             this.condition = condition;
             this.state = state;
             this.front = front;
         }
         
         public void
         run(final T referent) { resolve(ref(referent)); }
 
         public void
         reject(final Exception reason) { chain(new Rejected<T>(reason)); }
         
         public void
         resolve(final Promise<T> p) {
             chain(p instanceof Detachable
                 ? ((Detachable<T>)p).getState()
             : null != p
                 ? p
             : new Rejected<T>(new NullPointerException()));
         }
         
         private void
         chain(final Promise<T> promise) {
             log.resolved(here + "#p" + condition);
             enqueue.run(new Forward<T>(condition, promise, front));
             try { state.call().mark(promise); } catch (final Exception e) {}
         }
     }
     
     /**
      * number of deferred promises {@linkplain #defer created}
      * <p>
      * This variable is only incremented and should never be allowed to wrap.
      * </p>
      */
     private long deferrals;
 
     /**
      * Creates a promise in the deferred state.
      * <p>
      * The return from this method is a ( {@linkplain Promise promise},
      * {@linkplain Resolver resolver} ) pair. The promise is initially in the
      * deferred state and can only be resolved by the resolver once. If the
      * promise is {@linkplain Resolver#run fulfilled}, the promise will forever
      * refer to the provided referent. If the promise, is
      * {@linkplain Resolver#reject rejected}, the promise will forever be in
      * the rejected state, with the provided reason. If the promise is
      * {@linkplain Resolver#resolve resolved}, the promise will forever be in
      * the same state as the provided promise. After this initial state
      * transition, all subsequent invocations of either
      * {@link Resolver#run fulfill}, {@link Resolver#reject reject} or
      * {@link Resolver#resolve resolve} are silently ignored. Any
      * {@linkplain Do observer} {@linkplain #when registered} on the promise
      * will only be notified after the promise is resolved.
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
             new Head<T>(condition, new Detachable<State<T>>(true,state),front));
         /**
          * The resolver only holds a weak reference to the promise's mutable
          * state, allowing it to be garbage collected even if the resolver is
          * still held. This implementation takes advantage of a common pattern
          * in which a when block is registered on a promise as soon as it is
          * created, but no other reference to the promise is retained. Combined
          * with the recycling of when blocks, this common pattern generates no
          * dead objects. Much of the implementation's complexity is in service
          * to this goal.
          */
     }
 
     /**
      * Creates an eventual reference.
      * <p>
      * An eventual reference queues invocations, instead of processing them
      * immediately. Each queued invocation will be processed, in order, in a
      * future event loop turn.
      * </p>
      * <p>
      * For example,
      * </p>
      * <pre>
      *  // Register an observer now, even though we don't know what we plan
      *  // to do with the notifications.
      *  final Channel&lt;Observer&gt; x = _.defer();
      *  account.observe(_.cast(Observer.class, x.promise));
      *  &hellip;
      *  // A log output has been determined, so fulfill the observer promise.
      *  final Observer log = &hellip;
      *  x.resolver.fulfill(log);   // Logs all past, and future, notifications.
      * </pre>
      * <p>
      * If this method returns successfully, the returned eventual reference
      * will not throw an {@link Exception} on invocation of any of the methods
      * defined by its type, provided the invoked method's return type is either
      * <code>void</code>, an {@linkplain Proxies#isImplementable allowed} proxy
      * type or assignable from {@link Promise}. Invocations on the eventual
      * reference will not give the <code>promise</code>, nor any of the
      * invocation arguments, an opportunity to execute in the current event loop
      * turn.
      * </p>
      * <p>
      * Invocations of methods defined by {@link Object} are <strong>not</strong>
      * queued, and so can cause plan interference, or throw an exception.
      * </p>
      * <p>
      * This method will not throw an {@link Exception}. The <code>promise</code>
      * argument will not be given the opportunity to execute in the current
      * event loop turn.
      * </p>
      * @param <T> referent type to implement
      * @param type      referent type to implement
      * @param promise   promise for the referent
      * @return corresponding eventual reference, or <code>null</code> if
      *         <code>type</code> is not eventualizable
      */
 	public final @SuppressWarnings("unchecked") <T> T
     cast(final Class<?> type, final Promise<T> promise) {
         try {
             return (T)(Void.class == type || void.class == type
                 ? null
             : Float.class == type || float.class == type
                 ? Float.NaN
             : Double.class == type || double.class == type
                 ? Double.NaN
             : null == promise
                 ? new Rejected<T>(new NullPointerException())._(type)
             : Rejected.class == promise.getClass()
                 ? ((Rejected<T>)promise)._(type)
             : type.isAssignableFrom(Promise.class)
                 ? promise
             : proxy(trust(promise), type, Selfless.class));
         } catch (final Exception e) {
             try { log.problem(e); } catch (final Exception ee) {}
             return null;
         }
     }
 
     /**
      * Ensures a reference is an {@linkplain #cast eventual reference}.
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
      *     private ConstArray&lt;Observer&gt; observer_s;
      *
      *     public
      *     Account(final Eventual _, final int initial) {
      *         this._ = _;
      *         balance = initial;
      *         observer_s = array();
      *     }
      *
      *     public void
      *     observe(final Observer observer) throws NullPointerException {
      *         // Vet the received arguments.
      *         final Observer observer_ = _.<b>_</b>(observer);
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
      *          for (final Observer observer_ : observer_s) {
      *              // Schedule future execution of notification.
      *              observer_.currentBalance(newBalance);
      *          }
      *     }
      * }
      * </pre>
      * <p>
      * By convention, the return from this method, as well as from
      * {@link #cast cast}, is held in a variable whose name is suffixed with
      * an '<code>_</code>' character. The main part of the variable name
      * should use Java's camelCaseConvention. A list of eventual references is
      * suffixed with "<code>_s</code>". This naming convention creates the
      * appearance of a new operator in the Java language, the eventual
      * operator: "<code><b>_.</b></code>".
      * </p>
      * <p>
      * This method will not throw an {@link Exception}. The
      * <code>reference</code> argument will not be given the opportunity to
      * execute in the current event loop turn.
      * </p>
      * @param <T> referent type, MUST be an
      *            {@linkplain Proxies#isImplementable allowed} proxy type
      * @param reference immediate or eventual reference,
      *                  MUST be non-<code>null</code>
      * @return corresponding eventual reference
      * @throws Error    <code>null</code> <code>reference</code> or
      *                  <code>T</code> not an allowed proxy type
      */
 	public final <T> T
     _(final T reference) {
         if (reference instanceof Proxy) {
             try {
                 final Object handler = Proxies.getHandler((Proxy)reference);
                 if ((null != handler && Rejected.class == handler.getClass()) ||
                     Deferred.trusted(deferred, handler)) { return reference; }
             } catch (final Exception e) {}
         }
         try {
             return new Enqueue<T>(this, ref(reference)).
                 mimic(reference.getClass());
         } catch (final Exception e) { throw new Error(e); }
     }
 
     /**
      * Registers an observer on an {@linkplain #cast eventual reference}.
      * <p>
      * The implementation behavior is the same as that documented for the
      * promise based {@link #when(Promise, Do) when} statement.
      * </p>
      * @param <T> referent type
      * @param <R> <code>observer</code>'s return type
      * @param reference observed reference
      * @param observer  observer, MUST NOT be <code>null</code>
      * @return promise, or {@linkplain #cast eventual reference}, for the
      *         <code>observer</code>'s return, or <code>null</code> if the
      *         <code>observer</code>'s return type is <code>Void</code>
      */
     public final <T,R> R
     when(final T reference, final Do<T,R> observer) {
         return when(null != reference ? reference.getClass() : Object.class,
                     ref(reference), observer);
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
 	 * Gets a corresponding {@linkplain Promise promise}.
 	 * <p>
 	 * This method is the inverse of {@link #cast cast}; it gets the
 	 * corresponding {@linkplain Promise promise} for a given reference.
 	 * </p>
 	 * <p>
 	 * This method will not throw an {@link Exception}. The 
 	 * <code>referent</code> argument will not be given the opportunity to
 	 * execute.
 	 * </p>
 	 * @param <T> referent type
 	 * @param referent immediate or eventual reference
 	 * @return corresponding {@linkplain Promise promise}
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
             return new Detachable<T>(false, referent);
         } catch (final Exception e) {
             return new Rejected<T>(e);
         }
     }
     
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
     public @SuppressWarnings("unchecked") <R> Vat<R>
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
             ConstArray<Object> argv = ConstArray.array();
             if (make.getParameterTypes().length != optional.length) {
                 argv = argv.with(this);
             }
             for (final Object x : optional) {
                 argv = argv.with(x);
             }
             invoke = new Invoke<Class<?>>(make, argv);
         } catch (final Exception e) { throw new Error(e); }
         final Receiver<?> destruct = cast(Receiver.class, null);
         return new Vat((R)when(maker, invoke), destruct);
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
      * <kbd>_._(this).run();</kbd>
      * <p>becomes:</p>
      * <kbd>_._((Runnable)this).run();</kbd>
      * @param x ignored
      * @throws Error    always thrown
      */
     public final <T extends Serializable> void
     _(final T x) throws Exception { throw new Error(); }
 
     /**
      * Causes a compile error for code that attempts to create an
      * {@linkplain #cast eventual reference} of a concrete type.
      * <p>
      * If you encounter a compile error because your code is linking to this
      * method, replace the specified concrete type with an
      * {@linkplain Proxies#isImplementable allowed} proxy type. For example,
      * </p>
      * <kbd>final Logger o_ = _.cast(Logger.class, op);</kbd>
      * <p>becomes:</p>
      * <kbd>final Observer o_ = _.cast(Observer.class, op);</kbd>
      * @param <R> referent type to implement
      * @param type      ignored
      * @param promise   ignored
      * @throws Error    always thrown
      */
     public final <R extends Serializable> void
     cast(final Class<R> type, final Promise<?> promise) throws Exception {
         throw new Error();
     }
 
     /**
      * Causes a compile error for code that attempts to return a concrete type
      * from a when block.
      * <p>
      * If you encounter a compile error because your code is linking to this
      * method, change your when block return type to a promise. For example,
      * </p>
      * <pre>
      * final Promise&lt;Account&gt; pa = &hellip;
      * final Integer balance = _.when(pa, new Do&lt;Account,Integer&gt;() {
      *     public Integer
      *     fulfill(final Account a) { return a.getBalance(); }
      * });
      * </pre>
      * <p>becomes:</p>
      * <pre>
      * final Promise&lt;Account&gt; pa = &hellip;
      * final Promise&lt;Integer&gt; balance =
      *  _.when(pa, new Do&lt;Account,Promise&lt;Integer&gt;&gt;() {
      *     public Promise&lt;Integer&gt;
      *     fulfill(final Account a) { return ref(a.getBalance()); }
      * });
      * </pre>
      * @param promise   ignored
      * @param observer  ignored
      * @throws Error    always thrown
      */
     public final <T,R extends Serializable> void
     when(final Promise<T> promise, final Do<T,R> observer) throws Exception {
         throw new Error();
     }
 
     /**
      * Causes a compile error for code that attempts to return a concrete type
      * from a when block.
      * <p>
      * If you encounter a compile error because your code is linking to this
      * method, change your when block return type to a promise. For example,
      * </p>
      * <pre>
      * final Account a = &hellip;
      * final Observer o_ = &hellip;
      * final Integer initial = _.when(o_, new Do&lt;Observer,Integer&gt;() {
      *     public Integer
      *     fulfill(final Observer o) { return a.getBalance(); }
      * });
      * </pre>
      * <p>becomes:</p>
      * <pre>
      * final Account a = &hellip;
      * final Observer o_ = &hellip;
      * final Promise&lt;Integer&gt; initial =
      *  _.when(o_, new Do&lt;Observer,Promise&lt;Integer&gt;&gt;() {
      *     public Promise&lt;Integer&gt;
      *     fulfill(final Observer o) { return ref(a.getBalance()); }
      * });
      * </pre>
      * @param reference ignored
      * @param observer  ignored
      * @throws Error    always thrown
      */
     public final <T,R extends Serializable> void
     when(final T reference, final Do<T,R> observer) throws Exception {
         throw new Error();
     }
 }
