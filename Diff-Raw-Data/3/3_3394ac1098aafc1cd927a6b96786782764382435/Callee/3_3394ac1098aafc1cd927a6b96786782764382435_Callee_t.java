 // Copyright 2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.remote.http;
 
 import static org.joe_e.array.PowerlessArray.builder;
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.Type;
 
 import org.joe_e.Struct;
 import org.joe_e.array.ArrayBuilder;
 import org.joe_e.array.ByteArray;
 import org.joe_e.array.ConstArray;
 import org.joe_e.array.PowerlessArray;
 import org.joe_e.charset.URLEncoding;
 import org.joe_e.reflect.Reflection;
 import org.ref_send.promise.Fulfilled;
 import org.ref_send.promise.Rejected;
 import org.ref_send.promise.Volatile;
 import org.ref_send.promise.eventual.Do;
 import org.ref_send.promise.eventual.Eventual;
 import org.ref_send.scope.Layout;
 import org.ref_send.scope.Scope;
 import org.ref_send.var.Factory;
 import org.waterken.http.MediaType;
 import org.waterken.http.Request;
 import org.waterken.http.Response;
 import org.waterken.http.Server;
 import org.waterken.http.TokenList;
 import org.waterken.io.snapshot.Snapshot;
 import org.waterken.remote.Exports;
 import org.waterken.syntax.json.JSONDeserializer;
 import org.waterken.syntax.json.JSONSerializer;
 import org.waterken.uri.Header;
 import org.waterken.uri.Path;
 import org.waterken.uri.Query;
 import org.waterken.uri.URI;
 import org.waterken.vat.Root;
 import org.web_send.Entity;
 
 /**
  * Server-side of the HTTP web-amp protocol.
  */
 final class
 Callee extends Struct implements Server, Serializable {
     static private final long serialVersionUID = 1L;
 
     private final Server bootstrap;
     
     private final ClassLoader code;
     private final Exports exports;
 
     Callee(final Server bootstrap, final Root local) {
         this.bootstrap = bootstrap;
 
         code = local.fetch(null, Root.code);
         exports = new Exports(local);
     }
 
     // org.waterken.http.Server interface
 
     public void
     serve(final String resource, final Volatile<Request> requestor,
           final Do<Response,?> respond) throws Exception {
 
         // check for web browser bootstrap request
         final String query = URI.query(null, resource);
         if (null == query) {
             final String project = exports.getProject();
             bootstrap.serve("file:///site/" + URLEncoding.encode(project) + "/"+
                               URLEncoding.encode(Path.name(URI.path(resource))),
                             requestor, respond);
             return;
         }
 
         // determine the request
         final Request request;
         try {
             request = requestor.cast();
         } catch (final Exception e) {
             respond.reject(e);
             return;
         }
 
         // made it to the final processor, so bounce a TRACE
         if ("TRACE".equals(request.method)) {
             respond.fulfill(request.trace());
             return;
         }
         
         // check that there is no path name
         if (!"".equals(Path.name(URI.path(resource)))) {
             respond.fulfill(never(request.method));
             return;
         }
         
         // determine the request subject
         Volatile<?> subject;
         try {
             subject = Eventual.promised(exports.use(Query.arg("", query, "s")));
         } catch (final Exception e) {
             subject = new Rejected<Object>(e);
         }
         
         // determine the request type
         final String p = Query.arg(null, query, "p");
         if (null == p || "*".equals(p)) {   // when block or introspection
             Object value;
             try {
                 // AUDIT: call to untrusted application code
                 value = subject.cast();
             } catch (final NullPointerException e) {
                 respond.fulfill(serialize(request.method, "404", "not yet",
                                           ephemeral, new Rejected<Object>(e)));
                 return;
             } catch (final Exception e) {
                 value = new Rejected<Object>(e);
             }
             if ("GET".equals(request.method) || "HEAD".equals(request.method)) {
                 if ("*".equals(p)) {
                     final Class<?> t = null!=value?value.getClass():Void.class;
                     if (!Exports.isPBC(t)) {
                         value = describe(t);
                     }
                 }
                 respond.fulfill(serialize(request.method, "200", "OK",
                                           forever, value));
             } else {
                 final String[] allow = { "TRACE", "OPTIONS", "GET", "HEAD" };
                 if ("OPTIONS".equals(request.method)) {
                     respond.fulfill(Request.options(allow));
                 } else {
                     respond.fulfill(Request.notAllowed(allow));
                 }
             }
             return;
         }                                   // member access
 
         // to preserve message order, force settling of a promise
         if (!(subject instanceof Fulfilled)) {
             respond.fulfill(never(request.method));
             return;
         }
         // AUDIT: call to untrusted application code
         final Object target = ((Fulfilled<?>)subject).cast();
         
         // prevent access to local implementation details
         final Class<?> type = null != target ? target.getClass() : Void.class;
         if (Exports.isPBC(type)) {
             respond.fulfill(never(request.method));
             return;
         }
         
         // process the request
         final Method lambda = Exports.dispatch(type, p);
         if ("GET".equals(request.method) || "HEAD".equals(request.method)) {
             Object value;
             try {
                 if (null == Exports.property(lambda)) {
                     throw new ClassCastException();
                 }
                 // AUDIT: call to untrusted application code
                 value = Reflection.invoke(Exports.bubble(lambda), target);
             } catch (final Exception e) {
                 value = new Rejected<Object>(e);
             }
            final boolean constant =
                null == lambda || "getClass".equals(lambda.getName());
             final int maxAge = constant ? forever : ephemeral; 
             final String etag = constant ? null : exports.getTransactionTag();
             Response r = request.hasVersion(etag)
                 ? new Response("HTTP/1.1", "304", "Not Modified",
                     PowerlessArray.array(
                         new Header("Cache-Control", "max-age=" + maxAge)
                     ), null)
             : serialize(request.method, "200", "OK", maxAge, value);
             if (null != etag) { r = r.with("ETag", etag); }
             respond.fulfill(r);
         } else if ("POST".equals(request.method)) {
             final String contentType = request.getContentType();
             final MediaType mime = 
                 null != contentType ? MediaType.decode(contentType) : AMP.mime;
             final Entity raw;
             if (AMP.mime.contains(mime) || MediaType.text.contains(mime)) {
                 if (!TokenList.equivalent("UTF-8",
                         mime.get("charset", "UTF-8"))) {
                     throw new Exception("charset MUST be UTF-8");
                 }
                 raw = null;
             } else {
                 raw = new Entity(contentType, ((Snapshot)request.body).content);
             }
             final String m = Query.arg(null, query, "m");
             final Object value = exports.once(m, lambda, new Factory<Object>() {
                 @Override public Object
                 run() {
                     try {
                         if (null != Exports.property(lambda)) {
                             throw new ClassCastException();
                         }
                         final ConstArray<?> argv = null != raw
                             ? ConstArray.array(raw)
                         : deserialize(request, ConstArray.array(
                                             lambda.getGenericParameterTypes()));
 
                         // AUDIT: call to untrusted application code
                         return Reflection.invoke(Exports.bubble(lambda), target,
                                 argv.toArray(new Object[argv.length()]));
                     } catch (final Exception e) {
                         return new Rejected<Object>(e);
                     }
                 }
             });
             respond.fulfill(serialize(request.method, "200", "OK",
                                       ephemeral, value));
         } else {
             final String[] allow = null != lambda
                 ? (null == Exports.property(lambda)
                     ? new String[] { "TRACE", "OPTIONS", "POST" }
                 : new String[] { "TRACE", "OPTIONS", "GET", "HEAD" })
             : new String[] { "TRACE", "OPTIONS" };
             if ("OPTIONS".equals(request.method)) {
                 respond.fulfill(Request.options(allow));
             } else {
                 respond.fulfill(Request.notAllowed(allow));
             }
         }
     }
     
     /**
      * Constructs a 404 response.
      */
     private Response
     never(final String method) throws Exception {
         return serialize(method, "404", "never", forever,
                          new Rejected<Object>(new NullPointerException()));
     }
     
     private Response
     serialize(final String method, final String status, final String phrase,
               final int maxAge, final Object value) throws Exception {
         if (value instanceof Entity) {
             final ByteArray content = ((Entity)value).content;
             return new Response("HTTP/1.1", status, phrase,
                 PowerlessArray.array(
                     new Header("Cache-Control", "max-age=" + maxAge),
                     new Header("Content-Type", ((Entity)value).type),
                     new Header("Content-Length", "" + content.length())
                 ),
                 "HEAD".equals(method) ? null : new Snapshot(content));
         }
         final ByteArray.BuilderOutputStream out =
             ByteArray.builder(1024).asOutputStream();
         new JSONSerializer().run(exports.reply(), ConstArray.array(value), out);
         final Snapshot body = new Snapshot(out.snapshot());           
         return new Response("HTTP/1.1", status, phrase,
             PowerlessArray.array(
                 new Header("Cache-Control", "max-age=" + maxAge),
                 new Header("Content-Type", AMP.mime.toString()),
                 new Header("Content-Length", "" + body.content.length())
             ),
             "HEAD".equals(method) ? null : body);
     }
     
     private ConstArray<?>
     deserialize(final Request request,
                 final ConstArray<Type> parameters) throws Exception {
         final String base = request.base(exports.getHere());
         return new JSONDeserializer().run(base, exports.connect(), parameters,
                 code, ((Snapshot)request.body).content.asInputStream());
     }
     
     static private Scope
     describe(final Class<?> type) {
         final Object ts = types(type);
         return new Scope(new Layout(PowerlessArray.array("$")),
                          ConstArray.array(ts));
     }
     
     /**
      * Enumerate all types implemented by a class.
      */
     static private PowerlessArray<String>
     types(final Class<?> actual) {
         final Class<?> end =
             Struct.class.isAssignableFrom(actual) ? Struct.class : Object.class;
         final PowerlessArray.Builder<String> r = builder(4);
         for (Class<?> i=actual; end!=i; i=i.getSuperclass()) { ifaces(i, r); }
         return r.snapshot();
     }
 
     /**
      * List all the interfaces implemented by a class.
      */
     static private void
     ifaces(final Class<?> type, final ArrayBuilder<String> r) {
         if (type == Serializable.class) { return; }
         if (Modifier.isPublic(type.getModifiers())) {
             try { r.append(Reflection.getName(type).replace('$', '-')); }
             catch (final Exception e) {}
         }
         for (final Class<?> i : type.getInterfaces()) { ifaces(i, r); }
     }
 }
