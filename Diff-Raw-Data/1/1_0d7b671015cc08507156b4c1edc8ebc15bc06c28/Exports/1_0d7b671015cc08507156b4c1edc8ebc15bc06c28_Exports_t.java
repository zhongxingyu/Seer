 // Copyright 2006-2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.remote;
 
 import java.io.Serializable;
 import java.security.SecureRandom;
 
 import org.joe_e.Struct;
 import org.joe_e.Token;
 import org.joe_e.charset.URLEncoding;
 import org.ref_send.Brand;
 import org.ref_send.promise.Fulfilled;
 import org.ref_send.promise.Promise;
 import org.ref_send.promise.Volatile;
 import org.ref_send.promise.eventual.Do;
 import org.ref_send.promise.eventual.Eventual;
 import org.waterken.id.Exporter;
 import org.waterken.id.Importer;
 import org.waterken.id.base.Base;
 import org.waterken.model.Base32;
 import org.waterken.model.Root;
 import org.waterken.syntax.json.Java;
 import org.waterken.uri.Path;
 import org.waterken.uri.Query;
 import org.waterken.uri.URI;
 
 /**
  * A web-key interface to a {@link Root}.
  */
 public final class
 Exports extends Struct implements Serializable {
     static private final long serialVersionUID = 1L;
 
     /**
      * model root
      */
     private final Root local;
 
     /**
      * Constructs an instance.
      * @param local model root
      */
     public
     Exports(final Root local) {
         this.local = local;
     }
     
     /**
      * Gets the base URL for this namespace.
      */
     public String
     getHere() { return (String)local.fetch("x-browser:", Remoting.here); }
     
     /**
      * Gets the project name for this model.
      */
     public String
     getProject() { return (String)local.fetch(null, Root.project); }
     
     /**
      * Fetches an exported reference.
      * @param name  name to lookup
      * @return bound value
      * @throws NullPointerException <code>name</code> is not bound
      */
     public Object
     use(final String name) throws NullPointerException {
         if (name.startsWith(".")) { throw new NullPointerException(); }
         final Token pumpkin = new Token();
         final Object r = local.fetch(pumpkin, name);
         if (pumpkin == r) { throw new NullPointerException(); }
         return r;
     }
     
     /**
      * Constructs a reference importer.
      * @param base  base URL
      */
     public Importer
     connect(final String base) {
         final ClassLoader code = (ClassLoader)local.fetch(null, Root.code);
         final Importer next =
             Java.connect(base, code, ID.connect(base, Remote.use(local)));
         class ImporterX extends Struct implements Importer, Serializable {
             static private final long serialVersionUID = 1L;
 
             public Object
             run(final Class<?> type, final String URL) {
                 try {
                     if (URI.resolve(URL, ".").equalsIgnoreCase(getHere())) {
                         final String name = Path.name(URI.path(URL));
                         return use("".equals(name) ? key(URL) : name);
                     }
                 } catch (final Exception e) {}
                 return next.run(type, URL);
             }
         }
         return new ImporterX();
     }
 
     /**
      * Constructs a reference exporter.
      */
     private Exporter
     export() {
         class ExporterX extends Struct implements Exporter, Serializable {
             static private final long serialVersionUID = 1L;
 
             public String
             run(final Object object) {
                 final String key = local.export(object);
                 return object instanceof Brand.Local
                     ? key
                 : (object instanceof Volatile ||
                         null == object || Java.isPBC(object.getClass()) ||
                         !(Eventual.promised(object) instanceof Fulfilled)
                     ? "./?src=." : "./") + "#" + key; 
             }
         }
         return Remote.bind(local, new ExporterX());
     }
     
     /**
      * Constructs an invocation argument exporter.
      * @param base  base URL of the recipient
      */
     public Exporter
     send(final String base) {
         return Java.export(ID.export(
                 Base.relative(base, Base.absolute(getHere(), export()))));
     }
     
     /**
      * Constructs a return argument exporter.
      */
     public Exporter
     reply() { return Java.export(ID.export(export())); }
     
     /**
      * Does an operation at most once.
      * @param <P> operand type
      * @param <R> return type
      * @param mid   {@link #mid}, or <code>null</code> for idempotent operator
      * @param p     operand
      * @param op    operator
      * @return <code>op</code> return
      * @throws Exception    any exception produced by the <code>op</code>
      */
     @SuppressWarnings("unchecked") public <P,R> R
     once(final String mid, final P p, final Do<P,R> op) throws Exception {
         final String pipe = null == mid ? null : local.pipeline(mid);
         final Token pumpkin = new Token();
         R r = (R)(null == pipe ? pumpkin : local.fetch(pumpkin, pipe));
         if (pumpkin == r) {
             r = op.fulfill(p);
             if (null != pipe) { local.store(pipe, r); }
         }
         return r;
     }
     
     /**
      * Produces a promise for the server-side copy of a return value.
      * @param <R> return type
      * @param base      base URL for the server
      * @param mid       {@link #mid}
      * @param R         return type
      * @param response  client-side copy of a return promise
      * @return remote reference to the server-side copy of the return value
      */
     @SuppressWarnings("unchecked") public <R> R
     far(final String base, final String mid,
         final Class R, final Promise<R> response) {
         final String here = (String)local.fetch(null, Remoting.here);
         if (null == here) {
             final Eventual _ = (Eventual)local.fetch(null, Remoting._);
             return R.isInstance(response) ? (R)response : _.cast(R, response);
         }
         final String pipe = local.pipeline(mid);
         local.store(pipe, response);
         return (R)Remote.use(local).run(R, href(base, pipe, here));
     }
     
     /**
      * Constructs a pipeline web-key.
      * @param dst   target model URL
      * @param key   pipeline key
      * @param src   local model URL
      */
     static private String
     href(final String dst, final String key, final String src) {
         return URI.resolve(dst,
             "./?src=" + URLEncoding.encode(URI.relate(dst, src)) + "#" + key);
     }
 
     /**
      * Generates a message identifier.
      */
     public String
     mid() {
         final byte[] secret = new byte[128 / Byte.SIZE];
         final SecureRandom prng = (SecureRandom)local.fetch(null, Root.prng);
         prng.nextBytes(secret);
         return Base32.encode(secret);
     }
     
     /**
      * Extracts the key from a web-key.
      * @param URL   web-key
      * @return corresponding key
      */
     static public String
     key(final String URL) { return URI.fragment("", URL); }
     
     /**
      * Is the given web-key a pipeline web-key?
      * @param URL   web-key
      * @return <code>true</code> if a promise, else <code>false</code>
      */
     static public boolean
     isPromise(final String URL) { return null != arg(URL, "src"); }
     
     /**
      * Extracts the soure model URL from a pipeline web-key.
      * @param URL   web-key
      * @return source model URL, or <code>null</code> if <code>URL</code> is not
      *         a pipeline web-key
      */
     static public String
     src(final String URL) {
         final String src = arg(URL, "src");
         return null == src ? null : URI.resolve(URL, src);
     }
 
     /**
      * Extracts a web-key argument.
      * @param URL   web-key
      * @param name  parameter name
      * @return argument value, or <code>null</code> if not specified
      */
     static private String
     arg(final String URL, final String name) {
         return Query.arg(null, URI.query("", URL), name);
     }
     
     /**
      * Constructs a web-key.
      * @param dst   target model URL
      * @param key   key
      */
     static public String
     href(final String dst, final String key) {
         return "".equals(key) ? dst : URI.resolve(dst, "#" + key);
     }
 }
