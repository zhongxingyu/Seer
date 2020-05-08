 // Copyright 2002-2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.remote;
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 
 import org.joe_e.Struct;
 import org.joe_e.Token;
 import org.joe_e.reflect.Proxies;
 import org.joe_e.reflect.Reflection;
 import org.ref_send.promise.Promise;
 import org.ref_send.promise.Rejected;
 import org.ref_send.promise.eventual.Deferred;
 import org.ref_send.promise.eventual.Do;
 import org.ref_send.promise.eventual.Eventual;
 import org.ref_send.type.Typedef;
 import org.waterken.id.Exporter;
 import org.waterken.id.Importer;
 import org.waterken.model.Root;
 import org.waterken.remote.http.HTTP;
 import org.waterken.uri.URI;
 
 /**
  * A remote reference.
  * @param <T> referent type
  */
 public final class
 Remote<T> extends Deferred<T> implements Promise<T> {
     static private final long serialVersionUID = 1L;
 
     /**
      * local address space
      */
     private final Root local;
 
     /**
      * reference relative URL
      */
     private final String URL;
 
     /**
      * Constructs an instance.
      * @param local local address space
      * @param URL   reference relative URL
      */
     private
     Remote(final Root local, final String URL) {
         super((Eventual)local.fetch(null, Remoting._),
               (Token)local.fetch(null, Remoting.deferred));
         if (null == URL) { throw new NullPointerException(); }
         this.local = local;
         this.URL = URL;
     }
     
     /**
      * Constructs an importer.
      * @param local local address space
      */
     static public Importer
     use(final Root local) {
         class ImporterX extends Struct implements Importer, Serializable {
             static private final long serialVersionUID = 1L;
 
             public Object
             run(final Class<?> type, final String id) {
                 final String here = (String)local.fetch(null, Remoting.here);
                 final String target = null == here ? id : URI.relate(here, id);
                 final Remote<?> rp = new Remote(local, target);
                 return type.isInstance(rp) || !Proxies.isImplementable(type)
                     ? rp
                 : rp._.cast(type, rp);
             }
         }
         return new ImporterX();
     }
     
     /**
      * Constructs an exporter.
      * @param local local address space
      * @param next  next module to try
      */
     static public Exporter
     bind(final Root local, final Exporter next) {
         class ExporterX extends Struct implements Exporter, Serializable {
             static private final long serialVersionUID = 1L;
 
             public String
             run(final Object object) {
                 final Object handler = object instanceof Proxy
                     ? Proxies.getHandler((Proxy)object) : object;
                 if (handler instanceof Remote) {
                     final Remote x = (Remote)handler;
                     if ((Token)local.fetch(null, Remoting.deferred) ==
                         (Token)x.local.fetch(null, Remoting.deferred)) {
                         return x.URL;
                     }
                 }
                 return next.run(object);
             }
         }
         return new ExporterX();
     }
     
     // java.lang.Object interface
 
     /**
      * Is the given object the same?
      * @param x The compared to object.
      * @return true if the same, else false.
      */
     public boolean
     equals(final Object x) {
         return x instanceof Remote &&
                _ == ((Remote)x)._ &&
                URL.equals(((Remote)x).URL) &&
                local.equals(((Remote)x).local);
     }
     
     /**
      * Calculates the hash code.
      */
     public int
     hashCode() { return 0x4E307E4F; }
 
     // org.ref_send.promise.Volatile interface
 
     /**
      * @return <code>this</code>
      */
     @SuppressWarnings("unchecked") public T
     cast() { return (T)this; }
     
     // java.lang.reflect.InvocationHandler interface
 
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
             final String here = (String)local.fetch(null, Remoting.here);
             final String target = null == here ? URL : URI.resolve(here, URL);
             return message(target).invoke(target, proxy, method, arg);
         } catch (final Exception e) {
             final Class<?> R = Typedef.raw(
                 Typedef.bound(method.getGenericReturnType(), proxy.getClass()));
            if (void.class == R || Void.class == R) { return null; }
             final Rejected<?> p = new Rejected<Object>(e);
             return R.isAssignableFrom(Promise.class) ? p : p._(R);
         }
     }
     
     // org.ref_send.promise.eventual.Deferred interface
 
     @SuppressWarnings("unchecked") protected <R> R
     when(final Class<?> R, final Do<T,R> observer) {
         try {
             final String here = (String)local.fetch(null, Remoting.here);
             final String target = null == here ? URL : URI.resolve(here, URL);
             return message(target).when(target, R, observer);
         } catch (final Exception e) {
            if (void.class == R || Void.class == R) { return null; }
             final Rejected<R> p = new Rejected<R>(e);
             return R.isAssignableFrom(Promise.class) ? (R)p : p._(R);
         }
     }
 
     @SuppressWarnings("unchecked") private Messenger
     message(final String target) {
         final String scheme = URI.scheme("", target);
         if ("https".equals(scheme)) { return new HTTP("https", 443, local); }
         if ("http".equals(scheme)) { return new HTTP("http", 80, local); }
         return new Messenger() {
             
             public <P,R> R
             when(String a, Class<?> R, Do<P,R> b) { return null; }
             
             public Object
             invoke(String a, Object b, Method c, Object... d) { return null; }
         };
     }
 }
