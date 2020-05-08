 // Copyright 2006-2008 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.ref_send.promise;
 
 import java.io.Serializable;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.lang.reflect.Type;
 import java.lang.reflect.TypeVariable;
 
 import org.joe_e.Equatable;
 import org.joe_e.Immutable;
 import org.joe_e.JoeE;
 import org.joe_e.Selfless;
 import org.joe_e.Struct;
 import org.joe_e.Token;
 import org.joe_e.array.ConstArray;
 import org.joe_e.reflect.Proxies;
 import org.joe_e.reflect.Reflection;
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
  * {@link RuntimeException} from its {@code apply()} implementation, the
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
  * invokes {@code setBalance()} in its {@code apply()} implementation,
  * the remaining observers will first be notified of the balance after the
  * update, and then be notified of the balance before the update. Again, these
  * other observers may then continue operating using stale data about the
  * account balance.</p>
  * <h4>Interrupted transition</h4>
  * <p>A called method may also initiate an unanticipated state transition in
  * the calling object, while the current transition is still incomplete.  For
  * example, in the default state, an {@code Account} is always ready to accept a
  * new observer; however, this constraint is temporarily not met when the
  * observer list is being iterated over. An observer could catch the
  * {@code Account} in this transitional state by invoking {@code observe()} in
  * its {@code apply()} implementation. As a result, a
  * {@link java.util.ConcurrentModificationException} will be thrown when
  * iteration over the observer list resumes. Again, this exception prevents
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
  * instance of {@link Eventual} is held in a variable named "{@code _}".
  * Additional ways of marking eventual operations with the '{@code _}' character
  * are specified in the documentation for the methods defined by this class. All
  * of these conventions make eventual control flow statements distinguishable by
  * the character sequence "{@code _.}". Example uses are also shown in the
  * method documentation for this class. The '{@code _}' character should only be
  * used to identify eventual operations so that a programmer can readily
  * identify operations that are expected to be eventual by looking for the
  * <b>{@code _.}</b> pseudo-operator.</p>
  * @see <a href="http://www.erights.org/talks/thesis/">Section 13.1
  *      "Sequential Interleaving Hazards" of "Robust Composition: Towards a
  *      Unified Approach to Access Control and Concurrency Control"</a>
  */
 public class
 Eventual implements Selfless, Serializable {
     static private final long serialVersionUID = 1L;
 
     /**
      * trusted promise permission
      */
    private   final Token local;
 
     /**
      * raw event loop
      */
     protected final Receiver<Promise<?>> enqueue;
 
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
      * call like: {@code destruct.apply(null)}
      * </p>
      */
     public    final Receiver<?> destruct;
 
     /**
      * mutable statistics about eventual operations
      */
     protected final Fulfilled<Stats> stats;
 
     /**
      * Constructs an instance.
      * @param enqueue   raw event loop
      * @param here      URI for the event loop
      * @param log       {@link #log}
      * @param destruct  {@link #destruct}
      */
     public
     Eventual(final Receiver<Promise<?>> enqueue, final String here,
              final Log log, final Receiver<?> destruct) {
         this.local = new Token();
         this.enqueue = enqueue;
         this.here = here;
         this.log = log;
         this.destruct = destruct;
         this.stats = new Fulfilled<Stats>(false, new Stats());
     }
 
     /**
      * Constructs an instance.
      * @param enqueue   raw event loop
      */
     public
     Eventual(final Receiver<Promise<?>> enqueue) {
         this(enqueue, "", new Log(),
           cast(Receiver.class, new Rejected<Receiver<?>>(null)));
     }
 
     // org.joe_e.Selfless interface
 
     public int
     hashCode() { return 0x1A7E4D0D; }
 
     public boolean
     equals(final Object x) {
         return x instanceof Eventual && local == ((Eventual)x).local;
     }
 
     // org.ref_send.promise.Eventual interface
 
     /**
      * Does an eventual conditional operation on a promise.
      * <p>
      * The {@code conditional} code block will be notified of the
      * {@code promise}'s state at most once, in a future event loop turn. If
      * there is no referent, the {@code conditional}'s {@link Do#reject reject}
      * method will be called with the reason; otherwise, the
      * {@link Do#fulfill fulfill} method will be called with either an immediate
      * reference for a local referent, or an {@linkplain #_ eventual reference}
      * for a remote referent. For example:
      * </p>
      * <pre>
      * import static org.ref_send.promise.Eventual.ref;
      * &hellip;
      * final Promise&lt;Account&gt; mine = &hellip;
      * final Promise&lt;Integer&gt; balance =
      *     _.when(mine, new Do&lt;Account,Promise&lt;Integer&gt;&gt;() {
      *         public Promise&lt;Integer&gt;
      *         fulfill(final Account x) { return ref(x.getBalance()); }
      *     });
      * </pre>
      * <p>
      * A {@code null} {@code promise} argument is treated like a rejected
      * promise with a reason of {@link NullPointerException}.
      * </p>
      * <p>
      * The {@code conditional} in successive calls to this method with the same
      * {@code promise} will be notified in the same order as the calls were
      * made.
      * </p>
      * <p>
      * This method will not throw an {@link Exception}. Neither the
      * {@code promise}, nor the {@code conditional}, argument will be given the
      * opportunity to execute in the current event loop turn.
      * </p>
      * @param <P> parameter type
      * @param <R> return type
      * @param promise observed promise
      * @param conditional conditional code block, MUST NOT be {@code null}
      * @return promise, or {@linkplain #_ eventual reference}, for the
      *         {@code conditional}'s return, or {@code null} if the
      *         {@code conditional}'s return type is {@code Void}
      */
     public final <P,R> R
     when(final Promise<P> promise, final Do<P,R> conditional) {
         try {
             final Class<?> R= Typedef.raw(returnType(Object.class,conditional));
             return cast(R, trust(promise).when(null, R, conditional));
         } catch (final Exception e) { throw new Error(e); }
     }
 
     /**
      * Does an eventual conditional operation on an
      * {@linkplain #_ eventual reference}.
      * <p>
      * The implementation behavior is the same as that documented for the
      * promise based {@link #when(Promise, Do) when} statement.
      * </p>
      * @param <P> parameter type
      * @param <R> return type
      * @param reference     observed reference
      * @param conditional   conditional code block, MUST NOT be {@code null}
      * @return promise, or {@linkplain #_ eventual reference}, for the
      *         {@code conditional}'s return, or {@code null} if the
      *         {@code conditional}'s return type is {@code Void}
      */
     public final <P,R> R
     when(final P reference, final Do<P,R> conditional) {
         try {
             final Class<?> P = null!=reference?reference.getClass():Void.class;
             final Class<?> R = Typedef.raw(returnType(P, conditional));
             return cast(R, trust(ref(reference)).when(P, R, conditional));
         } catch (final Exception e) { throw new Error(e); }
     }
 
     /**
      * Determines a block's return type.
      * @param P     block's parameter type
      * @param block block to introspect on
      * @return {@code block}'s return type
      */
     static private Type
     returnType(final Type P, final Do<?,?> block) {
         return block instanceof Invoke ?
             Typedef.bound(((Invoke<?>)block).method.getGenericReturnType(), P) :
             Typedef.value(DoR, block.getClass());
     }
 
     /**
      * {@link Do} block return type
      */
     static private final TypeVariable<?> DoR = Typedef.var(Do.class, "R");
 
     /**
      * A reified method invocation.
      * @param <T> target object type
      */
     static protected final class
     Invoke<T> extends Do<T,Object> implements Serializable {
         static private final long serialVersionUID = 1L;
 
         /**
          * method to invoke
          */
         public final Method method;
 
         /**
          * invocation arguments, or {@code null} if none
          */
         public final ConstArray<?> argv;
 
         /**
          * Constructs a pending invocation.
          * @param method    {@link #method}
          * @param argv      {@link #argv}
          */
         public
         Invoke(final Method method, final ConstArray<?> argv) {
             this.method = method;
             this.argv = argv;
         }
 
         public Object
         fulfill(final T object) throws Exception {
             // AUDIT: call to untrusted application code
             return Reflection.invoke(method, object,
                 null == argv ? null : argv.toArray(new Object[argv.length()]));
         }
     }
 
     /**
      * A combined {@link Do} block and return value {@link Resolver}.
      * @param <P> parameter type
      * @param <R> return type
      */
     static protected final class
     Compose<P,R> extends Do<P,Void> implements Serializable {
         static private final long serialVersionUID = 1L;
 
         /**
          * conditional code block to execute
          */
         public final Do<? super P,? extends R> conditional;
 
         /**
          * return value resolver
          */
         public final Resolver<R> resolver;
 
         /**
          * Constructs an instance.
          * @param conditional   {@link #conditional}
          * @param resolver      {@link #resolver}
          */
         public
         Compose(final Do<? super P, ? extends R> conditional,
                 final Resolver<R> resolver) {
             this.conditional = conditional;
             this.resolver = resolver;
         }
 
         // org.ref_send.promise.Do interface
 
         public Void
         fulfill(final P a) {
             final R r;
             try {
                 r = conditional.fulfill(a);
             } catch (final Exception e) {
                 resolver.reject(e);
                 return null;
             }
             resolver.apply(r);
             return null;
         }
 
         public Void
         reject(final Exception reason) {
             final R r;
             try {
                 r = conditional.reject(reason);
             } catch (final Exception e) {
                 resolver.reject(e);
                 return null;
             }
             resolver.apply(r);
             return null;
         }
     }
 
     protected final <T> Local<T>
     trust(final Promise<T> untrusted) {
         return trusted(untrusted) ?
             (Local<T>)untrusted : new Enqueue<T>(this, untrusted);
     }
 
     protected final boolean
     trusted(final Object untrusted) {
         return untrusted instanceof Local &&
                local == ((Local<?>)untrusted)._.local;
     }
 
     /**
      * An abstract base class for a promise implementation that is scoped to a
      * particular event queue.
      * @param <T> referent type
      */
     static protected abstract class
     Local<T> implements Promise<T>, InvocationHandler, Selfless, Serializable {
         static private final long serialVersionUID = 1L;
 
         protected final Eventual _;
         
         protected
        Local(final Eventual _) {
             this._ = _;
         }
 
         // org.joe_e.Selfless interface
 
         public abstract boolean
         equals(Object x);
 
         public abstract int
         hashCode();
 
         // java.lang.reflect.InvocationHandler interface
 
         /**
          * Forwards a Java language invocation.
          * @param proxy     eventual reference
          * @param method    method to invoke
          * @param args      each invocation argument
          * @return eventual reference for the invocation return
          */
         public final Object
         invoke(final Object proxy, final Method method,
                final Object[] args) throws Exception {
             if (Object.class == method.getDeclaringClass()) {
                 if ("equals".equals(method.getName())) {
                     return args[0] instanceof Proxy &&
                         proxy.getClass() == args[0].getClass() &&
                         equals(Proxies.getHandler((Proxy)args[0]));
                 } else {
                     return Reflection.invoke(method, this, args);
                 }
             }
             try {
                 final Class<?> T = proxy.getClass();
                 final Class<?> R =
                     Typedef.raw(Typedef.bound(method.getGenericReturnType(),T));
                 return cast(R, when(T, R, new Invoke<T>(
                     method, null == args ? null : ConstArray.array(args))));
             } catch (final Exception e) { throw new Error(e); }
         }
 
         // org.ref_send.promise.Promise interface
 
         public abstract T
         call() throws Exception;
 
         // org.ref_send.promise.Local interface
 
         /**
          * Shortens the promise chain by one link.
          */
         public abstract Object
         shorten() throws Unresolved;
 
         public <R> Promise<R>
         when(final Class<?> T, final Class<?> R,
              final Do<? super T, ? extends R> conditional) {
             final Promise<R> r;
             final Do<? super T,?> forwarder;
             if (void.class == R || Void.class == R) {
                 r = null;
                 forwarder = conditional;
             } else {
                 final Deferred<R> x = _.defer();
                 r = x.promise;
                 forwarder = new Compose<T,R>(conditional, x.resolver);
             }
             when(T, forwarder);
             return r;
         }
 
         /**
          * Notifies an observer in a future event loop turn.
          * @param T         concrete referent type, {@code null} if not known
          * @param observer  promise observer
          */
         public abstract void
         when(Class<?> T, Do<? super T,?> observer);
     }
 
     static private final class
     Enqueue<T> extends Local<T> {
         static private final long serialVersionUID = 1L;
 
         final Promise<T> untrusted;
 
         Enqueue(final Eventual _, final Promise<T> untrusted) {
            super(_);
             this.untrusted = untrusted;
         }
 
         public int
         hashCode() { return 0x174057ED; }
 
         public boolean
         equals(final Object x) {
             return x instanceof Enqueue &&
                 _.equals(((Enqueue<?>)x)._) &&
                 (null != untrusted ?
                     untrusted.equals(((Enqueue<?>)x).untrusted) :
                     null == ((Enqueue<?>)x).untrusted);
         }
 
         public T
         call() throws Exception { return untrusted.call(); }
 
         public Object
         shorten() { return this; }
 
         public void
         when(final Class<?> T, final Do<? super T, ?> observer) {
             final long id = near(_.stats).newTask();
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
 
     /**
      * A recognizable exception to avoid logging an ignored null resolution.
      */
     static private final NullPointerException VOID = new NullPointerException();
 
     static protected <P,R> R
     sample(final Promise<? extends P> promise,
            final Do<? super P, ? extends R> observer,
            final Log log, final String message) throws Exception {
         final P a;
         try {
             if (null == promise) { throw VOID; }
             a = promise.call();
 
             // ensure the called value is not one that is
             // expected to be handled as a rejection
             if (null == a) { throw VOID; }
             final Promise<?> p = ref(a);
             if (p instanceof Rejected) { p.call(); }
         } catch (final Exception reason) {
             final @SuppressWarnings({ "rawtypes", "unchecked" }) Class<?> c =
                 (observer instanceof Compose ? ((Compose)observer).conditional :
                                                observer).getClass();
             log.got(message, c, reject);
             try {
                 return observer.reject(reason);
             } catch (final NullPointerException shadow) {
                 if (VOID.equals(shadow)) { return null; }
                 throw shadow;
             }
         }
         final Method m;
         final Class<?> c; {
             final @SuppressWarnings({ "rawtypes", "unchecked" }) Do inner =
                 observer instanceof Compose ?
                         ((Compose)observer).conditional : observer;
             if (inner instanceof Invoke) {
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
     static protected final class
     When<T> implements Equatable, Serializable {
         static private final long serialVersionUID = 1L;
 
         long condition;             // id for the corresponding promise
         long message;               // id for this when block
         Do<? super T,?> observer;   // client's when block code
         Promise<When<T>> next;      // next when block registered on the promise
     }
 
     /**
      * Executes the next Do block registered on the promise.
      */
     private final class
     Forward<T> extends Struct implements Promise<Void>, Serializable {
         static private final long serialVersionUID = 1L;
 
         private final boolean ignored;              // value ignored so far?
         private final long condition;               // corresponding promise id
         private final Promise<When<T>> pending;     // when block to run
         private final Class<?> T;                   // referent type of promise
         private final Promise<? extends T> value;   // resolved value of promise
 
         Forward(final boolean ignored,
                 final long condition, final Promise<When<T>> pending,
                 final Class<?> T, final Promise<? extends T> value) {
             this.ignored = ignored;
             this.condition = condition;
             this.pending = pending;
             this.T = T;
             this.value = value;
         }
 
         public Void
         call() throws Exception {
             final long message;
             final Do<? super T,?> observer;
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
                 near(stats).freeWhen(pending, block);
             }
 
             if (null != next) {
                 enqueue.apply(new Forward<T>(false, condition, next, T, value));
                 try {
                     if (trusted(value)) {
                         log.got(here + "#w" + message, null, null);
                         final @SuppressWarnings("unchecked") Local<? extends T>
                             promise = (Local<? extends T>)value;
                         promise.when(T, observer);
                     } else {
                         // AUDIT: call to untrusted application code
                         sample(value, observer, log, here + "#w" + message);
                     }
                 } catch (final Exception e) {
                     log.problem(e);
                     throw e;
                 }
             } else if (ignored && value instanceof Rejected) {
                 /*
                  * No when block has been queued on this promise, so this
                  * rejection will go unnoticed by the application code. To make
                  * the exception show up in the log, create a dummy when block
                  * to log the problem. This dummy when block has the unusual
                  * nature of being a message that is received before it is sent.
                  */
                 final Exception e = ((Rejected<?>)value).reason;
                 log.got(here + "#w" + message, null, null);
                 log.sentIf(false, here+"#w"+message, here+"#p"+condition);
                 log.problem(e);
                 throw e;
             }
             return null;
         }
     }
 
     static private final class
     State<T> implements Serializable {
         static private final long serialVersionUID = 1L;
 
         final long condition;               // id of this promise
               Promise<When<T>> back;        // observer list sentinel
 
               boolean resolved;             // Is resolved?
               Class<?> T;                   // concrete referent type
               Promise<? extends T> value;   // resolved value
 
         State(final long condition, final Promise<When<T>> back) {
             this.condition = condition;
             this.back = back;
         }
 
         protected void
         observe(final Eventual _, final Do<? super T,?> observer) {
             final boolean call = observer instanceof Compose &&
                 ((Compose<?,?>)observer).conditional instanceof Invoke;
             final When<T> block = near(back);
             if (condition == block.condition) {
                 _.log.sentIf(call, _.here + "#w" + block.message,
                                    _.here + "#p" + condition);
                 block.observer = observer;
                 back = block.next = near(_.stats).allocWhen(condition);
             } else {
                 /*
                  * Promise is already resolved and all previously registered
                  * observers run. Register the observer on the resolved value,
                  * but using a separate turn so that we don't lose the causality
                  * chain from this promise to the resolved value.
                  */
                 final long task = near(_.stats).newTask();
                 _.log.sentIf(call, _.here + "#t" + task,
                                    _.here + "#p" + condition);
                 class Fwd extends Struct implements Promise<Void>, Serializable{
                     static private final long serialVersionUID = 1L;
 
                     public Void
                     call() {
                         _.log.got(_.here + "#t" + task, null, null);
                         _.trust(value).when(T, Void.class, observer);
                         return null;
                     }
                 }
                 _.enqueue.apply(new Fwd());
             }
         }
     }
 
     static private final class
     Tail<T> extends Local<T> {
         static private final long serialVersionUID = 1L;
 
         final Promise<State<T>> state;      // promise's mutable state
 
         Tail(final Eventual _, final State<T> state) {
            super(_);
             this.state = new Fulfilled<State<T>>(false, state);
         }
 
         public int
         hashCode() { return 0x3EFF7A11; }
 
         public boolean
         equals(final Object x) {
             return x instanceof Tail && state.equals(((Tail<?>)x).state);
         }
 
         public T
         call() throws Exception {
             final State<T> cell = state.call();
             if (!cell.resolved) { throw new Unresolved(); }
             return cell.value.call();
         }
 
         public Object
         shorten() throws Unresolved {
             final State<T> cell = near(state);
             if (!cell.resolved) { throw new Unresolved(); }
             if (cell.value instanceof Inline) {
                 return ((Inline<?>)cell.value).call();
             }
             return cell.value;
         }
 
         public void
         when(final Class<?> T, final Do<? super T,?> observer) {
             near(state).observe(_, observer);
         }
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
         progress() { log.progressed(here + "#p" + condition); }
 
         public void
         reject(final Exception reason) { set(null, new Rejected<T>(reason)); }
 
         public void
         resolve(final Promise<? extends T> p) { set(null, p); }
 
         public void
         apply(final T r) {
             set(null==r || r instanceof Promise ? null : r.getClass(), ref(r));
         }
 
         private void
         set(final Class<?> T, Promise<? extends T> p) {
             if (p instanceof Fulfilled) {
                 final @SuppressWarnings("unchecked") Fulfilled<? extends T>
                     fulfilled = (Fulfilled<? extends T>)p;
                 p = fulfilled.getState();
                 log.fulfilled(here + "#p" + condition);
             } else if (p instanceof Rejected) {
                 final @SuppressWarnings("unchecked") Rejected<? extends T>
                     rejected = (Rejected<? extends T>)p;
                 log.rejected(here + "#p" + condition, rejected.reason);
             } else {
                 log.resolved(here + "#p" + condition);
             }
             try {
                 final State<T> cell = state.call();
                 if (null != cell) {
                     if (cell.resolved) { return; }
                     cell.resolved = true;
                     cell.T = T;
                     cell.value = p;
                 }
             } catch (final Exception e) { log.problem(e); }
             enqueue.apply(new Forward<T>(true, condition, front, T, p));
         }
     }
 
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
      * the promise is either fulfilled or rejected.
      * </p>
      * @param <T> referent type
      * @return ( {@linkplain Promise promise}, {@linkplain Resolver resolver} )
      */
     public final <T> Deferred<T>
     defer() {
         final long condition = near(stats).newDeferral();
         final Promise<When<T>> front = near(stats).allocWhen(condition);
         final State<T> state = new State<T>(condition, front);
         return new Deferred<T>(new Tail<T>(this, state),
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
      * name is suffixed with an '{@code _}' character. The main part of the
      * variable name should use Java's camelCaseConvention. A list of eventual
      * references is suffixed with "{@code _s}". This naming convention
      * creates the appearance of a new operator in the Java language, the
      * eventual operator: "<b>{@code _.}</b>".
      * </p>
      * <p>
      * If this method returns successfully, the returned eventual reference
      * will not throw an {@link Exception} on invocation of any of the methods
      * defined by its type, provided the invoked method's return type is either
      * {@code void}, an {@linkplain Proxies#isImplementable allowed} proxy
      * type or assignable from {@link Promise}. Invocations on the eventual
      * reference will not give the {@code referent}, nor any of the invocation
      * arguments, an opportunity to execute in the current event loop turn.
      * </p>
      * <p>
      * Invocations of methods defined by {@link Object} are <strong>not</strong>
      * queued, and so can cause plan interference, or throw an exception.
      * </p>
      * @param <T> referent type, MUST be an
      *            {@linkplain Proxies#isImplementable allowed} proxy type
      * @param referent  immediate or eventual reference,
      *                  MUST be non-{@code null}
      * @return corresponding eventual reference
      * @throws NullPointerException {@code null} {@code referent}
      * @throws ClassCastException   {@code T} not an
      *                  {@linkplain Proxies#isImplementable allowed} proxy type
      */
     public final <T> T
     _(final T referent) {
         if (referent instanceof Proxy) {
             final Object handler = Proxies.getHandler((Proxy)referent);
             if ((null != handler && Rejected.class == handler.getClass()) ||
                 trusted(handler)) { return referent; }
         }
         final @SuppressWarnings("unchecked") T proxy =
             (T)Proxies.proxy(new Enqueue<T>(this, ref(referent)),
                              virtualize(referent.getClass(), Selfless.class));
         return proxy;
     }
 
     /**
      * Lists the part of an interface that a proxy can implement.
      * @param r types to mimic
      */
     static protected Class<?>[]
     virtualize(Class<?>... r) {
         for (int i = r.length; i-- != 0;) {
             final Class<?> type = r[i];
             if (type == Serializable.class || !Proxies.isImplementable(type) ||
                     JoeE.isSubtypeOf(type, Immutable.class) ||
                     JoeE.isSubtypeOf(type, Equatable.class)) {
                 // remove the type from the proxy type list
                 {
                     final Class<?>[] c = r;
                     r = new Class<?>[c.length - 1];
                     System.arraycopy(c, 0, r, 0, i);
                     System.arraycopy(c, i + 1, r, i, r.length - i);
                 }
 
                 // search the inheritance tree for types that can be implemented
                 for (Class<?> p = type; null != p; p = p.getSuperclass()) {
                     Class<?>[] x = virtualize(p.getInterfaces());
 
                     // remove any duplicate types from the replacement type list
                     for (int j = x.length; 0 != j--;) {
                         for (int k = r.length; 0 != k--;) {
                             if (x[j] == r[k]) {
                                 final Class<?>[] c = x;
                                 x = new Class<?>[c.length - 1];
                                 System.arraycopy(c, 0, x, 0, j);
                                 System.arraycopy(c, j + 1, x, j, x.length - j);
                                 break;
                             }
                         }
                     }
 
                     // splice in the replacement type list
                     final Class<?>[] c = r;
                     r = new Class<?>[c.length + x.length];
                     System.arraycopy(c, 0, r, 0, i);
                     System.arraycopy(x, 0, r, i, x.length);
                     System.arraycopy(c, i, r, i + x.length, c.length - i);
                 }
             }
         }
         return r;
     }
 
     /**
      * Causes a compile error for code that attempts to create an
      * {@linkplain #_ eventual reference} of a concrete type.
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
      * @throws ClassCastException   no cast to {@code type}
      */
     static public @SuppressWarnings("unchecked") <T> T
     cast(final Class<?> type,final Promise<T> promise)throws ClassCastException{
         return (T)(Void.class == type || void.class == type ?
                 null :
             Promise.class == type ?
                 promise :
             promise instanceof Fulfilled ?
                 near(promise) :
             type.isInstance(promise) ?
                 promise :
             float.class == type ?
                 Float.NaN :
             double.class == type ?
                 Double.NaN :
             null == promise ?
                 null :
             Float.class == type ?
                 Float.NaN :
             Double.class == type ?
                 Double.NaN :
             Selfless.class == type ?
                 Proxies.proxy((InvocationHandler)promise, Selfless.class) :
             Proxies.proxy((InvocationHandler)promise, type, Selfless.class));
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
         if (null == referent) { return null; }
         if (referent instanceof Promise) { return (Promise<T>)referent; }
         if (referent instanceof Proxy) {
             try {
                 final Object handler = Proxies.getHandler((Proxy)referent);
                 if (handler instanceof Promise) {
                     return handler instanceof Enqueue
                         ? ((Enqueue<T>)handler).untrusted : (Promise<T>)handler;
                 }
             } catch (final Exception e) { /* treat as normal reference */ }
         }
         try {
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
      * This method should only be used when the application knows the provided
      * reference refers to a local object. Any other condition is treated as a
      * fatal error. Use the {@link Promise#call call} method to check the status
      * of a promise.
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
      * This method should only be used when the application knows the provided
      * promise refers to a local object. Any other condition is treated as a
      * fatal error. Use the {@link Promise#call call} method to check the status
      * of a promise.
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
      * The {@code maker} MUST be a {@code public}
      * {@linkplain org.joe_e.IsJoeE Joe-E} class with a method of signature:
      * </p>
      * <pre>
      * static public R
      * make({@link Eventual} _, &hellip;)
      * </pre>
      * <p>
      * The ellipsis means the method can have any number of additional
      * arguments. The {@link Eventual} parameter, if present, MUST be the first
      * parameter.
      * </p>
      * <p>
      * This method will not throw an {@link Exception}. None of the arguments
      * will be given the opportunity to execute in the current event loop turn.
      * </p>
      * @param <R> return type, MUST be either an interface, or a {@link Promise}
      * @param label     optional vat label,
      *                  if {@code null} a label will be generated
      * @param maker     constructor class
      * @param optional  more arguments for {@code maker}'s make method
      * @return sub-vat permissions, including a promise for the object returned
      *         by the {@code maker}
      */
     public <R> Vat<R>
     spawn(final String label, final Class<?> maker, final Object... optional) {
         /**
          * The default implementation just calls the make method in a separate
          * event loop turn.
          */
         try {
             final Method make = NotAMaker.dispatch(maker);
             final Class<?>[] paramv = make.getParameterTypes();
             final ConstArray.Builder<Object> argv =
                 ConstArray.builder(paramv.length);
             if (0 != paramv.length && Eventual.class == paramv[0]) {
                 argv.append(this);
             }
             for (final Object x : optional) { argv.append(x); }
             final @SuppressWarnings("unchecked") R top =
                 (R)when(maker, new Invoke<Class<?>>(make, argv.snapshot()));
             final Promise<Receiver<?>> ignore = reject(null);
             return new Vat<R>(top, cast(Receiver.class, ignore));
         } catch (final Exception e) { throw new Error(e); }
     }
 
 //  The following convenience overloads are supported under JDK1.6 and early
 //  versions of JDK1.5, but not on later versions of JDK1.5. They support a
 //  slighter better syntax and are faster than the generic implementations.
 //
 //  /**
 //   * Registers an observer on a promise.
 //   * <p>
 //   * The implementation behavior is the same as that documented for the
 //   * promise based {@link #when(Promise, Do) when} statement.
 //   * </p>
 //   * @param <P> {@code observer}'s parameter type
 //   * @param <R> {@code observer}'s return type
 //   * @param promise   observed promise
 //   * @param observer  observer, MUST NOT be {@code null}
 //   * @return promise for the observer's return value
 //   */
 //  public final <P,R extends Serializable> Promise<R>
 //  when(final Promise<P> promise, final Do<P,R> observer) {
 //      try {
 //          return when(Object.class, promise, observer);
 //      } catch (final Exception e) { throw new Error(e); }
 //  }
 //
 //  /**
 //   * Registers an observer on a promise.
 //   * <p>
 //   * The implementation behavior is the same as that documented for the
 //   * promise based {@link #when(Promise, Do) when} statement.
 //   * </p>
 //   * @param <P> {@code observer}'s parameter type
 //   * @param promise   observed promise
 //   * @param observer  observer, MUST NOT be {@code null}
 //   */
 //  public final <P> void
 //  when(final Promise<P> promise, final Do<P,Void> observer) {
 //      try {
 //          when(Void.class, promise, observer);
 //      } catch (final Exception e) { throw new Error(e); }
 //  }
 //
 //  /**
 //   * Registers an observer on an {@linkplain #_ eventual reference}.
 //   * <p>
 //   * The implementation behavior is the same as that documented for the
 //   * promise based {@link #when(Promise, Do) when} statement.
 //   * </p>
 //   * @param <P> {@code observer}'s parameter type
 //   * @param <R> {@code observer}'s return type
 //   * @param reference observed reference
 //   * @param observer  observer, MUST NOT be {@code null}
 //   * @return promise for the observer's return value
 //   */
 //  public final <P,R extends Serializable> Promise<R>
 //  when(final P reference, final Do<P,R> observer) {
 //      try {
 //          return when(Object.class, ref(reference), observer);
 //      } catch (final Exception e) { throw new Error(e); }
 //  }
 //
 //  /**
 //   * Registers an observer on an {@linkplain #_ eventual reference}.
 //   * <p>
 //   * The implementation behavior is the same as that documented for the
 //   * promise based {@link #when(Promise, Do) when} statement.
 //   * </p>
 //   * @param <P> {@code observer}'s parameter type
 //   * @param reference observed reference
 //   * @param observer  observer, MUST NOT be {@code null}
 //   */
 //  public final <P> void
 //  when(final P reference, final Do<P,Void> observer) {
 //      try {
 //          when(Void.class, ref(reference), observer);
 //      } catch (final Exception e) { throw new Error(e); }
 //  }
 //
 //  /**
 //   * Causes a compile error for code that attempts to cast a promise to a
 //   * concrete type.
 //   * <p>
 //   * If you encounter a compile error because your code is linking to this
 //   * method, replace the specified concrete type with an
 //   * {@linkplain Proxies#isImplementable allowed} proxy type. For example,
 //   * </p>
 //   * <kbd>final Observer o_ = _.cast(Observer.class, op);</kbd>
 //   * <p>becomes:</p>
 //   * <kbd>final Receiver&lt;?&gt; o_ = _.cast(Receiver.class, op);</kbd>
 //   * @param <R> referent type to implement
 //   * @param type      ignored
 //   * @param promise   ignored
 //   * @throws AssertionError   always thrown
 //   */
 //  static public <R extends Serializable> void
 //  cast(final Class<R> type,
 //       final Promise<?> promise) { throw new AssertionError();}
 }
