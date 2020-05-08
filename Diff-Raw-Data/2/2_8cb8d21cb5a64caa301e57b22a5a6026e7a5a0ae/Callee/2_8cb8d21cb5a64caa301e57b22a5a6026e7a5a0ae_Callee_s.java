 // Copyright 2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.remote.http;
 
 import java.io.Serializable;
 import java.lang.reflect.Type;
 
 import org.joe_e.Struct;
 import org.joe_e.array.ByteArray;
 import org.joe_e.array.ConstArray;
 import org.joe_e.array.PowerlessArray;
 import org.joe_e.reflect.Reflection;
 import org.ref_send.promise.Eventual;
 import org.ref_send.promise.Promise;
 import org.ref_send.promise.Unresolved;
 import org.waterken.http.Message;
 import org.waterken.http.Request;
 import org.waterken.http.Response;
 import org.waterken.http.Server;
 import org.waterken.io.FileType;
 import org.waterken.syntax.BadSyntax;
 import org.waterken.syntax.Deserializer;
 import org.waterken.syntax.json.JSONDeserializer;
 import org.waterken.syntax.json.JSONSerializer;
 import org.waterken.uri.Header;
 
 /**
  * Server-side of the web-key protocol.
  */
 /* package */ final class
 Callee extends Struct implements Serializable {
     static private final long serialVersionUID = 1L;
     
     private final HTTP.Exports exports;
 
     protected
     Callee(final HTTP.Exports exports) {
         this.exports = exports;
     }
     
     static private final Class<?> Fulfilled = Eventual.ref(0).getClass();
 
     protected Message<Response>
     apply(final String query, final Message<Request> m) throws Exception {
         
         // further dispatch the request based on the accessed member
         final String p = HTTP.predicate(m.head.method, query);
         if (null == p) {                        // when block
             if ("OPTIONS".equals(m.head.method)) {
                 return new Message<Response>(
                     Response.options("TRACE", "OPTIONS", "GET", "HEAD"), null);
             }
             if (!("GET".equals(m.head.method) || "HEAD".equals(m.head.method))){
                 return new Message<Response>(
                     Response.notAllowed("TRACE","OPTIONS","GET","HEAD"), null);
             }
             Object value;
             try {
                 // AUDIT: call to untrusted application code
                 value = Eventual.ref(exports.reference(query)).call();
             } catch (final Unresolved e) {
                 return serialize(m.head.method, "404", "not yet",
                                  Server.ephemeral, Exception.class, e);
             } catch (final Exception e) {
                 value = Eventual.reject(e);
             }
             final Response failed = m.head.allow("\"\"");
             if (null != failed) { return new Message<Response>(failed, null); }
             return serialize(m.head.method, "200", "OK", Server.forever,
                              Object.class, value);
         }                                       // member access
 
         // determine the target object
         final Object target;
         try {
             final Promise<?> subject = Eventual.ref(exports.reference(query));
             // to preserve message order, only access members on a fulfilled ref
             if (!Fulfilled.isInstance(subject)) { throw new Unresolved(); }
             target = subject.call();
         } catch (final Exception e) {
             return serialize(m.head.method, "404", "never", Server.forever,
                              Exception.class, e);
         }
 
         if ("GET".equals(m.head.method) || "HEAD".equals(m.head.method)) {
             final Dispatch property = Dispatch.get(target, p);
             if (null == property) {             // no such property
                 final boolean post = null != Dispatch.post(target, p);
                 return new Message<Response>(Response.notAllowed(
                     post ? new String[] { "TRACE", "OPTIONS", "POST" } : 
                            new String[] { "TRACE", "OPTIONS"         }), null);
             }
             Object value;                       // property access
             try {
                 // AUDIT: call to untrusted application code
                 final Object r = Reflection.invoke(property.declaration,target);
                 value = Fulfilled.isInstance(r) ? ((Promise<?>)r).call() : r;
             } catch (final Exception e) {
                 value = Eventual.reject(e);
             }
             final String etag = exports.getTransactionTag();
             final Response failed = m.head.allow(etag);
             if (null != failed) { return new Message<Response>(failed, null); }
             Message<Response> r = serialize(
                     m.head.method, "200", "OK", Server.ephemeral,
                    property.implementation.getGenericReturnType(), value);
             if (null != etag) {
                 r = new Message<Response>(r.head.with("ETag", etag), r.body);
             }
             return r;
         }
         
         if ("POST".equals(m.head.method)) {
             final Response failed = m.head.allow(null);
             if (null != failed) { return new Message<Response>(failed, null); }
             return exports.execute(query, new NonIdempotent() {
                 public Message<Response>
                 apply(final String message) throws Exception {
                     /*
                      * SECURITY CLAIM: do request processing inside once block
                      * to ensure application layer cannot detect request replay
                      */
                     final Dispatch lambda = Dispatch.post(target, p);
                     if (null == lambda) {       // no such method
                         exports._.log.got(message, null, null);
                         exports._.log.returned(message + "-return");
                         final boolean get = null != Dispatch.get(target, p);
                         return new Message<Response>(Response.notAllowed(
                             get ? new String[]{"TRACE","OPTIONS","GET"} : 
                                   new String[]{"TRACE","OPTIONS"      }), null);
                     }                           // method invocation
                     if (null != message) {
                         exports._.log.got(message, null, lambda.implementation);
                     }
                     Object value;
                     try {
                         String contentType = m.head.getContentType();
                         if (null == contentType) {
                             contentType = FileType.json.name;
                         } else {
                             final int end = contentType.indexOf(';');
                             if (-1 != end) {
                                 contentType = contentType.substring(0, end);
                             }
                         }
                         final Deserializer syntax =
                             Header.equivalent(FileType.json.name, contentType)||
                             Header.equivalent("text/plain",       contentType) ?
                                 new JSONDeserializer() : null;
                         final ConstArray<?> argv;
                         try {
                             argv = null == syntax ? ConstArray.array(m.body) : 
                                 syntax.deserializeTuple(m.body.asInputStream(),
                                   exports.connect(), exports.getHere(),
                                   exports.code, lambda.implementation.
                                       getGenericParameterTypes());
                         } catch (final BadSyntax e) {
                             /*
                              * strip out the parsing information to avoid
                              * leaking information to the application layer
                              */ 
                             throw (Exception)e.getCause();
                         }
     
                         // AUDIT: call to untrusted application code
                         value = Reflection.invoke(lambda.declaration, target,
                                 argv.toArray(new Object[argv.length()]));
                         if (Fulfilled.isInstance(value)) {
                             value = ((Promise<?>)value).call();
                         }
                     } catch (final Exception e) {
                         value = Eventual.reject(e);
                     }
                     final Type R = lambda.declaration.getGenericReturnType();
                     if (null != message) {
                         if (null!=value || (void.class!=R && Void.class!=R)) {
                             exports._.log.returned(message + "-return");
                         }
                     }
                     return serialize(m.head.method, "200", "OK",
                                      Server.ephemeral, R, value);
                 }
             });
         }
         
         final boolean get = null != Dispatch.get(target, p);
         final boolean post = null != Dispatch.post(target, p);
         final String[] allow =
             get && post ? new String[] { "TRACE", "OPTIONS", "GET", "POST" } : 
             get         ? new String[] { "TRACE", "OPTIONS", "GET"         } :
                    post ? new String[] { "TRACE", "OPTIONS",        "POST" } :
                           new String[] { "TRACE", "OPTIONS"                };
         return "OPTIONS".equals(m.head.method) ?
                 new Message<Response>(Response.options(allow), null) :
             new Message<Response>(Response.notAllowed(allow), null);
     }
     
     private Message<Response>
     serialize(final String method, final String status,
               final String phrase, final int maxAge,
               final Type type, final Object value) throws Exception {
         final String contentType;
         final ByteArray content;
         if (value instanceof ByteArray) {
             contentType = FileType.unknown.name;
             content = (ByteArray)value;
         } else {
             contentType = FileType.json.name;
             content=new JSONSerializer().serialize(exports.export(),type,value);
         }
         if ("GET".equals(method) || "HEAD".equals(method)) {
             return new Message<Response>(new Response(
                 "HTTP/1.1", status, phrase,
                 PowerlessArray.array(
                     new Header("Cache-Control", 
                                "must-revalidate, max-age=" + maxAge),
                     new Header("Content-Type", contentType),
                     new Header("Content-Length", "" + content.length())
                 )),
                 "HEAD".equals(method) ? null : content);
         }
         return new Message<Response>(new Response(
             "HTTP/1.1", status, phrase,
             PowerlessArray.array(
                 new Header("Content-Type", contentType),
                 new Header("Content-Length", "" + content.length())
             )), content);
     }
 }
