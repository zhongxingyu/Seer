 // Copyright 2002-2006 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.ref_send.promise;
 
 import java.io.Serializable;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 
 import org.joe_e.Powerless;
 import org.joe_e.Selfless;
 import org.joe_e.reflect.Proxies;
 import org.joe_e.reflect.Reflection;
 import org.ref_send.Record;
 import org.ref_send.deserializer;
 import org.ref_send.name;
 import org.ref_send.type.Typedef;
 
 /**
  * A rejected promise.
  * @param <T> referent type
  */
 public final class
 Rejected<T> implements Promise<T>, InvocationHandler, Powerless,
                        Selfless, Record, Serializable {
     static private final long serialVersionUID = 1L;
     
     /**
      * reason for rejecting the promise
      */
     public final Exception reason;
 
     /**
      * Construct an instance.
      * @param reason    {@link #reason}
      */
     public @deserializer
     Rejected(@name("reason") final Exception reason) {
         this.reason = reason;
     }
     
     // java.lang.Object interface
     
     /**
      * Is the given object the same?
      * @param x compared to object
      * @return <code>true</code> if the same, else <code>false</code>
      */
     public boolean
     equals(final Object x) {
         return x instanceof Rejected &&
                (null != reason
                    ? reason.equals(((Rejected<?>)x).reason)
                    : null == ((Rejected<?>)x).reason);
     }
     
     /**
      * Calculates the hash code.
      */
     public int
     hashCode() { return 0xDEADBEA7; }
 
     // org.ref_send.promise.Volatile interface
 
     /**
      * Throws the {@link #reason}.
      * @throws  Exception   {@link #reason}
      */
     public T
     cast() throws Exception { throw reason; }
 
     // java.lang.reflect.InvocationHandler interface
 
     /**
      * Forwards a Java language invocation.
      * @param proxy     eventual reference
      * @param method    method to invoke
      * @param args      invocation arguments
      * @return eventual reference for the invocation return
      * @throws Exception    problem invoking an {@link Object} method
      * @throws Error        <code>method</code> return cannot be eventualized
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
             return void.class == R || Void.class == R ? null : _(R);
         } catch (final Exception e) { throw new Error(e); }
     }
     
     // org.ref_send.promise.Rejected interface
     
     /**
      * Creates a rejected reference.
      * @param type  referent type, MUST be an
      *              {@linkplain Proxies#isImplementable allowed} proxy type
      * @throws Error    invalid <code>type</code> argument
      */
    public T
     _(final Class<?> type) {
         try {
            return type.isAssignableFrom(Promise.class)
                ? (T)this
            : (T)Proxies.proxy(this, type, Powerless.class, Selfless.class);
         } catch (final Exception e) { throw new Error(e); }
     }
 }
