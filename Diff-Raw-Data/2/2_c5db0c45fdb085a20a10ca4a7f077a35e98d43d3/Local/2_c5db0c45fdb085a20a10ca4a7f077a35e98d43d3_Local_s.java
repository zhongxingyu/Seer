 // Copyright 2006 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.ref_send.promise;
 
 import java.io.Serializable;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.lang.reflect.Type;
 import java.lang.reflect.TypeVariable;
 
 import org.joe_e.Selfless;
 import org.joe_e.Token;
 import org.joe_e.array.ConstArray;
 import org.joe_e.reflect.Proxies;
 import org.joe_e.reflect.Reflection;
 import org.ref_send.type.Typedef;
 
 /**
  * Implementation hook that users should ignore.
  * <p>
  * An abstract base class for a promise implementation that is scoped to a
  * particular event queue.
  * </p>
  * @param <T> referent type
  */
 public abstract class
 Local<T> implements Promise<T>, InvocationHandler, Selfless, Serializable {
     static private final long serialVersionUID = 1L;
 
     /**
      * corresponding eventual operator
      */
     protected final Eventual _;
 
     /**
      * Constructs an instance.
      * @param _     corresponding eventual operator
      * @param local corresponding {@link Eventual#local}
      */
     protected
     Local(final Eventual _, final Token local) {
         /*
          * *MUST ONLY* allow construction by a caller who directly possesses the
          * corresponding permission. The Eventual implementation relies upon
          * this check being done in this constructor.
          */
         if (_.local != local) { throw new ClassCastException(); }
         this._ = _;
     }
 
     /**
      * Is an untrusted promise actually a trusted implementation?
      * @param local {@link Local} permission
      * @param untrusted currently untrusted promise
      * @return <code>true</code> if trusted, else <code>false</code>
      */
     static protected final boolean
     trusted(final Token local, final Object untrusted) {
         return untrusted instanceof Local<?> &&
                local == ((Local<?>)untrusted)._.local;
     }
 
     // java.lang.Object interface
 
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
     public Object
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
             final Class<?> R = Typedef.raw(Typedef.bound(
                     method.getGenericReturnType(), proxy.getClass()));
             return Eventual.cast(R, _.when(R, this, new Invoke<T>(method,
                     null == args ? null : ConstArray.array(args))));
         } catch (final Exception e) { throw new Error(e); }
     }
 
     // org.ref_send.promise.Promise interface
 
     public abstract T
     call() throws Exception;
 
     // org.ref_send.promise.Deferred interface
 
     /**
      * Notifies an observer in a future event loop turn.
      * @param observer  promise observer
      */
     protected abstract void
     when(Do<T,?> observer);
 
     /**
      * Constructs a pending invocation.
      * @param method    method to invoke
      * @param argv      invocation arguments
      */
     static public <T> Do<T,Object>
     curry(final Method method, final ConstArray<?> argv) {
         return new Invoke<T>(method, argv);
     }
 
     /**
      * Do block parameter type
      */
     static private final TypeVariable<?> P = Typedef.var(Do.class, "P");
 
     /**
      * Determines a block's parameter type.
      * @param x block to introspect on
      * @return <code>x</code>'s parameter type
      */
     static public Type
     parameter(final Do<?,?> x) {
         final @SuppressWarnings("unchecked") Do inner =
             x instanceof Compose ? ((Compose)x).block : x;
         return inner instanceof Invoke<?> ?
             ((Invoke<?>)inner).method.getDeclaringClass() :
         Typedef.value(P, inner.getClass());
     }
 
     /**
      * Do block return type
      */
     static private final TypeVariable<?> R = Typedef.var(Do.class, "R");
 
     /**
      * Determines a block's return type.
      * @param p block's parameter type
      * @param x block to introspect on
      * @return <code>x</code>'s return type
      */
     static protected Type
     output(final Class<?> p, final Do<?,?> x) {
        final @SuppressWarnings("unchecked") Do<?,?> inner =
             x instanceof Compose ? ((Compose)x).block : x;
         return inner instanceof Invoke<?> ?
             Typedef.bound(((Invoke<?>)inner).method.getGenericReturnType(), p) :
         Typedef.value(R, inner.getClass());
     }
 }
