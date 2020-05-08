 // Copyright 2006-2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.ref_send.promise.eventual;
 
 import static org.joe_e.reflect.Proxies.proxy;
 import static org.ref_send.promise.Fulfilled.detach;
 
 import java.io.Serializable;
 import java.lang.reflect.Proxy;
 
 import org.joe_e.Equatable;
 import org.joe_e.Immutable;
 import org.joe_e.JoeE;
 import org.joe_e.Selfless;
 import org.joe_e.Struct;
 import org.joe_e.Token;
 import org.joe_e.reflect.Proxies;
 import org.ref_send.promise.Fulfilled;
 import org.ref_send.promise.Promise;
 import org.ref_send.promise.Rejected;
 import org.ref_send.promise.Volatile;
 import org.ref_send.type.Typedef;
 
 /**
  * The eventual operator.
  * <p>This class decorates an {@linkplain #enqueue event loop} with methods
  * implementing the core eventual control flow statements needed for defensive
  * programming. The primary aim of these new control flow statements is
  * preventing plan interference.</p>
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
 public final class
 Eventual implements Equatable, Serializable {
     static private final long serialVersionUID = 1L;
 
     /**
      * {@link Deferred} permission
      */
     /* package */ final Token deferred;
 
     /**
      * Schedules a task for execution in a future turn.
      * <p>
      * The implementation preserves the <i>F</i>irst <i>I</i>n <i>F</i>irst
      * <i>O</i>ut ordering of tasks, meaning the tasks will be
      * {@linkplain Task#run executed} in the same order as they were enqueued.
      * </p>
      */
     public final Loop<Task> enqueue;
 
     /**
      * Constructs an instance.
      * @param deferred  {@link Deferred} permission
      * @param enqueue   {@link #enqueue}
      */
     public
     Eventual(final Token deferred, final Loop<Task> enqueue) {
         this.deferred = deferred;
         this.enqueue = enqueue;
     }
 
     // org.ref_send.promise.eventual.Eventual interface
 
     /**
      * Registers an observer on a promise.
      * <p>
      * The <code>observer</code> will be notified of the <code>promise</code>'s
      * state at most once, in a future {@linkplain #enqueue event loop} turn. If
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
     public <T,R> R
     when(final Volatile<T> promise, final Do<T,R> observer) {
         try {
             final Class R= Typedef.raw(Typedef.value(Do.R,observer.getClass()));
             return trust(promise).when(R, observer);
         } catch (final Exception reason) {
             throw new Error(reason);
         }
     }
 
     private <T> Deferred<T>
     trust(final Volatile<T> untrusted) {
         return null == untrusted
             ? new Enqueue<T>(this, new Rejected<T>(new NullPointerException()))
         : untrusted instanceof Deferred && this == ((Deferred<T>)untrusted)._
             ? (Deferred<T>)untrusted
         : new Enqueue<T>(this, untrusted);
     }
 
     static private final class
     Enqueue<T> extends Deferred<T> {
         static private final long serialVersionUID = 1L;
 
         final Volatile<T> untrusted;
 
         Enqueue(final Eventual _, final Volatile<T> untrusted) {
             super(_, _.deferred);
             this.untrusted = untrusted;
         }
 
         public int
         hashCode() { return 0x174057ED; }
 
         public boolean
         equals(final Object x) {
             return x instanceof Enqueue &&
                 _ == ((Enqueue)x)._ &&
                 (null != untrusted
                     ? untrusted.equals(((Enqueue)x).untrusted)
                     : null == ((Enqueue)x).untrusted);
         }
 
         public T
         cast() throws Exception { return untrusted.cast(); }
 
         @SuppressWarnings("unchecked") public <R> R
         when(final Class<?> R, final Do<T,R> observer) {
             final R r;
             final Do<T,?> forwarder;
             if (void.class == R || Void.class == R) {
                 r = null;
                 forwarder = observer;
             } else {
                 final Channel<R> x = _.defer();
                 r = R.isAssignableFrom(Promise.class)
                         ? (R)x.promise : _.cast(R, x.promise);
                 forwarder = compose(observer, x.resolver);
             }
             class Sample extends Struct implements Task, Serializable {
                 static private final long serialVersionUID = 1L;
 
                 public void
                 run() throws Exception {
                     // AUDIT: call to untrusted application code
                     sample(untrusted, forwarder);
                 }
             }
             _.enqueue.run(new Sample());
             return r;
         }
     }
     
     static private <P,R> R
     sample(final Volatile<P> p, final Do<P,R> observer) throws Exception {
         final P a;
         try {
             a = Fulfilled.ref(p.cast()).cast();
         } catch (final Exception reason) {
             return observer.reject(reason);
         }
         return observer.fulfill(a);
     }
 
     /**
      * Constructs a call return block.
      * @param first     code block to execute
      * @param second    code block's return resolver
      */
     static public <A,B> Do<A,Void>
     compose(final Do<A,B> first, final Resolver<B> second) {
         class Compose extends Do<A,Void> implements Serializable {
             static private final long serialVersionUID = 1L;
 
             public Void
             fulfill(final A a) {
                 final B b;
                 try {
                     b = first.fulfill(a);
                 } catch (final Exception e) {
                     return second.reject(e);
                 }
                 return second.resolve(promised(b));
             }
 
             public Void
             reject(final Exception reason) {
                 final B b;
                 try {
                     b = first.reject(reason);
                 } catch (final Exception e) {
                     return second.reject(e);
                 }
                 return second.resolve(promised(b));
             }
         }
         return new Compose();
     }
 
     /**
      * A registered promise observer.
      * @param <T> referent type
      */
     static private final class
     When<T> implements Serializable {
         static private final long serialVersionUID = 1L;
 
         Fulfilled<When<T>> next;
         Do<T,?> observer;
     }
 
     /**
      * allocated when blocks
      * <p>When objects are reused so as to reduce waste in
      * implementations that provide orthogonal persistence.</p>
      */
     private Fulfilled allocatedWhens;
 
     @SuppressWarnings("unchecked") private <T> Fulfilled<When<T>>
     allocateWhen(final Do<T,?> observer) {
         final Fulfilled<When<T>> r;
         if (null != allocatedWhens) {
             r = allocatedWhens;
             final When<T> x = r.cast();
             allocatedWhens = x.next;
             x.next = null;
             x.observer = observer;
         } else {
             final When<T> x = new When<T>();
             x.observer = observer;
             r = detach(x);
         }
         return r;
     }
 
     @SuppressWarnings("unchecked") private <T> void
     freeWhen(final Fulfilled<When<T>> p) {
         final When<T> x = p.cast();
         x.next = allocatedWhens;
         x.observer = null;
         allocatedWhens = p;
     }
 
     /**
      * Creates a promise in the deferred state.
      * <p>
      * The return from this method is a ( {@linkplain Promise promise},
      * {@linkplain Resolver resolver} ) pair. The promise is initially in the
      * deferred state and can only be resolved by the resolver once. If the
      * promise is {@linkplain Resolver#fulfill fulfilled}, the promise will
      * forever refer to the provided referent. If the promise, is
      * {@linkplain Resolver#reject rejected}, the promise will forever be in the
      * rejected state, with the provided reason. If the promise is
      * {@linkplain Resolver#resolve resolved}, the promise will forever be in
      * the same state as the provided promise. After this initial state
      * transition, all subsequent invocations of either
      * {@link Resolver#fulfill fulfill}, {@link Resolver#reject reject} or
      * {@link Resolver#resolve resolve} are silently ignored. Any
      * {@linkplain Do observer} {@linkplain #when registered} on the promise
      * will only be notified after the promise is resolved.
      * </p>
      * @param <T> referent type
      * @return ( {@linkplain Promise promise}, {@linkplain Resolver resolver} )
      */
     public <T> Channel<T>
     defer() {
         class State implements Serializable {
             static private final long serialVersionUID = 1L;
 
             /**
              * current state of this promise
              * <ul>
              *  <li>deferred: <code>null</code></li>
              *  <li>fulfilled: {@link Fulfilled}</li>
              *  <li>rejected: {@link Rejected}</li>
              *  <li>more resolved: {@link Volatile}</li>
              * </ul>
              */
             Volatile<T> value;
 
             // observer queue
             Fulfilled<When<T>> front;
             Fulfilled<When<T>> back;
         }
         final Fulfilled<State> state = detach(new State());
         class Pop extends Struct implements Task, Serializable {
             static private final long serialVersionUID = 1L;
 
             /**
              * Notifies the next observer of the resolved value.
              */
             public void
             run() throws Exception {
                 final State m = state.cast();
                 final Fulfilled<When<T>> p = m.front;
                 final When<T> x = p.cast();
                 m.front = x.next;
                 if (null == m.front) {
                     m.back = null;
                 } else {
                     enqueue.run(this);
                 }
                 final Do<T,?> observer = x.observer;
                 freeWhen(p);
                 if (m.value instanceof Deferred &&
                     Eventual.this == ((Deferred)m.value)._) {
                     ((Deferred<T>)m.value).when(Void.class, observer);
                 } else {
                     // AUDIT: call to untrusted application code
                     sample(m.value, observer);
                 }
             }
         }
         class Head extends Do<T,Void> implements Resolver<T>, Serializable {
             static private final long serialVersionUID = 1L;
 
             public Void
             fulfill(final T value) { return resolve(promised(value)); }
 
             public Void
             reject(final Exception reason) {
                 return resolve(new Rejected<T>(reason));
             }
             
             public Void
             resolve(final Volatile<T> value) {
                 final State m = state.cast();
                 if (null == m.value) {
                     m.value = null != value
                         ? value
                     : new Rejected<T>(new NullPointerException());
                     if (null != m.front) { enqueue.run(new Pop()); }
                 }
                 return null;
             }
         }
         class Tail extends Deferred<T> implements Promise<T> {
             static private final long serialVersionUID = 1L;
 
             Tail() { super(Eventual.this, deferred); }
 
             public int
             hashCode() { return state.hashCode() + 0x3EFF7A11; }
 
             public boolean
             equals(final Object x) {
                 return x instanceof Tail && state.equals(((Tail)x).state());
             }
 
             private Fulfilled<State>
             state() { return state; }
 
             public T
             cast() throws Exception { return state.cast().value.cast(); }
 
             @SuppressWarnings("unchecked") public <R> R
             when(final Class<?> R, final Do<T,R> observer) {
                 final R r;
                 final Do<T,?> forwarder;
                 if (void.class == R || Void.class == R) {
                     r = null;
                     forwarder = observer;
                 } else {
                     final Channel<R> x = _.defer();
                     r = R.isAssignableFrom(Promise.class)
                             ? (R)x.promise : _.cast(R, x.promise);
                     forwarder = compose(observer, x.resolver);
                 }
 
                 final State m = state.cast();
                 if (null == m.front) {
                     m.front = allocateWhen(forwarder);
                     m.back = m.front;
                     if (null != m.value) { enqueue.run(new Pop()); }
                 } else {
                     final When<T> previous = m.back.cast();
                     m.back = previous.next = allocateWhen(forwarder);
                 }
                 
                 return r;
             }
         }
         return new Channel<T>(new Tail(), new Head());
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
      * @param type      referent type to implement, MUST be an
      *                  {@linkplain Proxies#isImplementable allowed} proxy type
      * @param promise   promise for the referent
      * @return corresponding eventual reference
      */
     @SuppressWarnings("unchecked") public <T> T
     cast(final Class type, final Volatile<T> promise) {
         try {
             return null == promise
                 ? new Rejected<T>(new NullPointerException())._(type)
             : Rejected.class == promise.getClass()
                 ? ((Rejected<T>)promise)._(type)
             : (T)proxy(trust(promise), type, Selfless.class);
         } catch (final Exception e) {
             throw new Error(e);
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
     @SuppressWarnings("unchecked") public <T> T
    _(final T reference) throws NullPointerException, ClassCastException {
         if (reference instanceof Proxy) {
             try {
                 final Object handler = Proxies.getHandler((Proxy)reference);
                 if ((null != handler && Rejected.class == handler.getClass()) ||
                     (handler instanceof Deferred&&this==((Deferred)handler)._)){
                     return reference;   // already a trusted eventual reference
                 }
             } catch (final Exception e) {}
         }
         try {
             // Build the list of types to implement.
             Class[] types = virtualize(reference.getClass());
             boolean selfless = false;
             for (final Class i : types) {
                 selfless = Selfless.class.isAssignableFrom(i);
                 if (selfless) { break; }
             }
             if (!selfless) {
                 final int n = types.length;
                 System.arraycopy(types, 0, types = new Class[n + 1], 0, n);
                 types[n] = Selfless.class;
             }
             return (T)proxy(new Enqueue<T>(this, detach(reference)), types);
         } catch (final Exception e) {
             throw new Error(e);
         }
     }
 
     /**
      * Lists the allowed interfaces implemented by a type.
      * @param base  base type
      * @return allowed interfaces implemented by <code>base</code>
      */
     static private Class[]
     virtualize(final Class base) {
         Class[] r = base.getInterfaces();
         int i = r.length;
         final Class parent = base.getSuperclass();
         if (null != parent && Object.class != parent) {
             final Class[] p = virtualize(parent);
             if (0 != p.length) {
                 System.arraycopy(r, 0, r = new Class[i + p.length], 0, i);
                 System.arraycopy(p, 0, r, i, p.length);
             }
         }
         while (i-- != 0) {
             final Class type = r[i];
             if (!Proxies.isImplementable(type) ||
                     JoeE.isSubtypeOf(type, Immutable.class) ||
                     JoeE.isSubtypeOf(type, Equatable.class)) {
                 final Class[] x = virtualize(r[i]);
                 final Class[] c = r;
                 r = new Class[c.length - 1 + x.length];
                 System.arraycopy(c, 0, r, 0, i);
                 System.arraycopy(x, 0, r, i, x.length);
                 System.arraycopy(c, i + 1, r, i + x.length, c.length - (i+1));
             }
         }
         return r;
     }
 
     /**
      * Registers an observer on an {@linkplain #cast eventual reference}.
      * <p>
      * The implementation behavior is the same as that documented for the
      * promise based {@link #when(Volatile, Do) when} statement.
      * </p>
      * @param <T> referent type
      * @param <R> <code>observer</code>'s return type
      * @param reference observed reference
      * @param observer  observer, MUST NOT be <code>null</code>
      * @return promise, or {@linkplain #cast eventual reference}, for the
      *         <code>observer</code>'s return, or <code>null</code> if the
      *         <code>observer</code>'s return type is <code>Void</code>
      */
     public <T,R> R
     when(final T reference, final Do<T,R> observer) {
         return when(promised(reference), observer);
     }
     
     /**
      * Gets the corresponding immediate reference.
      * <p>
      * This method is the inverse of {@link #_(Object) _}; it gets the
      * corresponding immediate reference for a given eventual reference.
      * </p>
      * @param <T> referent type
      * @param reference possibly eventual reference for a local referent
      * @return corresponding immediate reference
      * @throws ClassCastException   no corresponding immediate reference
      */
     static public <T> T
     near(final T reference) throws ClassCastException {
         return near(promised(reference));
     }
     
     /**
      * Gets the corresponding immediate reference.
      * <p>
      * The implementation behavior is the same as that documented for the
      * reference based {@link #near(Object) near} guard.
      * </p>
      * @param <T> referent type
      * @param promise   promise for a local referent
      * @return corresponding immediate reference
      * @throws ClassCastException   no corresponding immediate reference
      */
     static public <T> T
     near(final Volatile<T> promise) throws ClassCastException {
         return ((Fulfilled<T>)promise).cast();
     }
 
     /**
      * Gets the corresponding {@linkplain Volatile promise}.
      * <p>
      * This method is the inverse of {@link #cast cast}; it gets the
      * corresponding {@linkplain Volatile promise} for a given reference.
      * </p>
      * <p>
      * This method will not throw an {@link Exception}. The
      * <code>reference</code> argument will not be given the opportunity to
      * execute.
      * </p>
      * @param <T> referent type
      * @param reference immediate or eventual reference
      * @return corresponding {@linkplain Volatile promise}
      */
     @SuppressWarnings("unchecked") static public <T> Volatile<T>
     promised(final T reference) {
         if (reference instanceof Volatile) { return (Volatile)reference; }
         if (reference instanceof Proxy) {
             try {
                 final Object handler = Proxies.getHandler((Proxy)reference);
                 if (handler instanceof Volatile){
                     return handler instanceof Enqueue
                         ? ((Enqueue<T>)handler).untrusted
                     : (Volatile<T>)handler;
                 }
             } catch (final Exception e) {}
         }
         return Fulfilled.ref(reference);
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
     public <T extends Serializable> void
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
     public <R extends Serializable> void
     cast(final Class<R> type, final Volatile<?> promise) throws Exception {
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
     public <T,R extends Serializable> void
     when(final Volatile<T> promise, final Do<T,R> observer) throws Exception {
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
     public <T,R extends Serializable> void
     when(final T reference, final Do<T,R> observer) throws Exception {
         throw new Error();
     }
 }
