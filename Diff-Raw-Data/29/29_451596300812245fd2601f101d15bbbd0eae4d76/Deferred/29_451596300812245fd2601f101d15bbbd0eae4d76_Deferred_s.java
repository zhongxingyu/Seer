 // Copyright 2006 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.ref_send.promise.eventual;
 
 import java.io.Serializable;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.lang.reflect.Type;
 
 import org.joe_e.Selfless;
 import org.joe_e.Token;
 import org.joe_e.array.ConstArray;
 import org.joe_e.reflect.Proxies;
 import org.joe_e.reflect.Reflection;
 import org.ref_send.promise.Volatile;
 import org.ref_send.type.Typedef;
 
 /**
  * Implementation hook that users should ignore.
  * @param <T> referent type
  */
 public abstract class
 Deferred<T> implements Volatile<T>, InvocationHandler, Selfless, Serializable {
     static private final long serialVersionUID = 1L;
 
     /**
      * corresponding eventual operator
      */
     protected final Eventual _;
 
     /**
      * Constructs an instance.
      * @param _         corresponding eventual operator
      * @param deferred  {@link Deferred} permission
      */
     protected
     Deferred(final Eventual _, final Token deferred) {
         // MUST ONLY allow construction by a caller who directly possesses the
         // corresponding deferred permission. The Eventual implementation relies
         // upon this check being done in this constructor.
         if (_.deferred != deferred) { throw new ClassCastException(); }
         this._ = _;
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
      * @param arg       each invocation argument
      * @return eventual reference for the invocation return
      */
     public Object
     invoke(final Object proxy, final Method method,
            final Object[] arg) throws Exception {
         if (Object.class == method.getDeclaringClass()) {
             if ("equals".equals(method.getName())) {
                 return arg[0] instanceof Proxy &&
                     proxy.getClass() == arg[0].getClass() &&
                     equals(Proxies.getHandler((Proxy)arg[0]));
             } else {
                 return Reflection.invoke(method, this, arg);
             }
         }
         try {
             final ConstArray<?> argv = null==arg ? null : ConstArray.array(arg);
             class Invoke extends Do<T,Object> implements Serializable {
                 static private final long serialVersionUID = 1L;
     
                public Object
                 fulfill(final T object) throws Exception {
                     // AUDIT: call to untrusted application code
                     return Reflection.invoke(method,
                         object instanceof Deferred
                             ? _.cast(method.getDeclaringClass(),
                                      (Deferred<T>)object)
                         : object, null == argv
                             ? null
                         : argv.toArray(new Object[argv.length()]));
                 }
             }
             final Type R = Typedef.bound(method.getGenericReturnType(),
                                          proxy.getClass());
             return when(Typedef.raw(R), new Invoke());
         } catch (final Exception e) { throw new Error(e); }
     }
 
     // org.ref_send.promise.Volatile interface
 
     public abstract T
     cast() throws Exception;
 
     // org.ref_send.promise.eventual.Deferred interface
     
     /**
      * Notifies an observer in a future event loop turn.
      * @param <R> observer's return type
      * @param R         observer's return type
      * @param observer  promise observer
      * @return promise, or {@linkplain Eventual#cast eventual reference}, for
      *         the <code>observer</code>'s return, or <code>null</code> if the
      *         <code>observer</code>'s return type is <code>Void</code>
      */
     protected abstract <R> R
     when(Class<?> R, Do<T,R> observer);
 }
