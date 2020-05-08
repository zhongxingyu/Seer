 // Copyright 2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.remote.http;
 
 import static org.ref_send.promise.Fulfilled.ref;
 import static org.web_send.Entity.maxContentSize;
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 
 import org.joe_e.Powerless;
 import org.joe_e.Struct;
 import org.joe_e.Token;
 import org.joe_e.charset.URLEncoding;
 import org.joe_e.reflect.Reflection;
 import org.ref_send.promise.Rejected;
 import org.ref_send.promise.Volatile;
 import org.ref_send.promise.eventual.Do;
 import org.ref_send.promise.eventual.Eventual;
 import org.ref_send.promise.eventual.Loop;
 import org.ref_send.promise.eventual.Task;
 import org.waterken.http.Request;
 import org.waterken.http.Response;
 import org.waterken.http.Server;
 import org.waterken.io.limited.Limited;
 import org.waterken.io.snapshot.Snapshot;
 import org.waterken.model.Creator;
 import org.waterken.model.Model;
 import org.waterken.model.Root;
 import org.waterken.model.Transaction;
 import org.waterken.remote.Exports;
 import org.waterken.remote.Remote;
 import org.waterken.remote.Remoting;
 import org.waterken.uri.URI;
 import org.web_send.Failure;
 import org.web_send.graph.Collision;
 import org.web_send.graph.Framework;
 import org.web_send.graph.Publisher;
 import org.web_send.graph.Spawn;
 import org.web_send.graph.Unavailable;
 
 /**
  * HTTP web-AMP implementation
  */
 public final class
 AMP extends Struct implements Remoting, Powerless, Serializable {
     static private final long serialVersionUID = 1L;
     
     /**
      * MIME Media-Type for marshalled arguments
      */
     static protected final String contentType = "application/jsonrequest";
     
     // org.waterken.remote.Remoting interface
 
     public Server
     remote(final Server bootstrap, final String scheme, final Model model) {
         return new Server() {
             public void
             serve(final String resource,
                   final Volatile<Request> requestor,
                   final Do<Response,?> respond) throws Exception {
                 final Request buffered; {
                     Request q = requestor.cast();
                     if (null != q.body) {
                         final int length = q.getContentLength();
                         if (length > maxContentSize) { throw Failure.tooBig(); }
                         q = new Request(q.version, q.method, q.URI, q.header,
                             Snapshot.snapshot(length < 0 ? 1024 : length,
                                 Limited.limit(maxContentSize, q.body)));
                     }
                     buffered = q;
                 }
                 respond.fulfill(model.enter("GET".equals(buffered.method) ||
                                             "HEAD".equals(buffered.method) ||
                                             "OPTIONS".equals(buffered.method) ||
                                             "TRACE".equals(buffered.method),
                                             new Transaction<Response>() {
                     public Response
                     run(final Root local) throws Exception {
                         final Response[] response = { null };
                         new Callee(bootstrap, local).serve(resource,
                                 ref(buffered), new Do<Response,Void>() {
                             public Void
                             fulfill(Response r) throws Exception {
                                 if (null != r.body &&
                                     !(r.body instanceof Snapshot)) {
                                     r= new Response(r.version,r.status,r.phrase,
                                         r.header, Snapshot.snapshot(
                                             r.getContentLength(), r.body));
                                 }
                                 response[0] = r;
                                 return null;
                             }
                         });
                         return response[0];
                     }
                 }).cast());
             }
         };
     }
 
     // org.waterken.remote.http.AMP interface
     
     static public Spawn
     spawn(final Publisher publisher) {
         class SpawnX extends Struct implements Spawn, Serializable {
             static private final long serialVersionUID = 1L;
             
             public <T> T
             run(final Class<?> factory) {
                 final Object r = publisher.spawn(null, factory);
                 return (T)r;
             }
         }
         return new SpawnX();
     }
 
     /**
      * Constructs a reference exporter.
      * @param mother    local model root
      */
     static public Publisher
     publish(final Root mother) {
         class PublisherX extends Struct implements Publisher, Serializable {
             static private final long serialVersionUID = 1L;
 
             public void
             bind(final String name, final Object value) throws Collision {
                 vet(name);
                 mother.link(name, value);
             }
 
             public <T> T
             spawn(final String name, final Class<?> factory) throws Collision {
                 if (null != name) { vet(name); }
                 final Method build;
                 try {
                     build= Reflection.method(factory, "build", Framework.class);
                 } catch (final NoSuchMethodException e) {
                     throw new ClassCastException();
                 }
                 final String base = (String)mother.fetch(null, here);
                 final Server client= (Server)mother.fetch(null,Remoting.client);
                 final Creator creator= (Creator)mother.fetch(null,Root.creator);
                final Class<?> T = build.getReturnType();
                 final String URL;
                 try {
                     URL = creator.create(new Transaction<String>() {
                         public String
                         run(final Root local) throws Exception {
                             final String here = base +
                                 URLEncoding.encode(local.getModelName()) + "/";
                             local.link(Remoting.here, here);
                             if (null != client) {
                                 local.link(Remoting.client, client);
                                 local.link(Root.wake, new Wake());
                                 local.link(outbound, new Outbound());
                             }
                             final Token deferred = new Token();
                             local.link(Remoting.deferred, deferred);
                             final Eventual _ = new Eventual(deferred,
                                     (Loop)local.fetch(null, Root.enqueue));
                             local.link(Remoting._, _);
                             final Publisher publisher = publish(local);
                             final Framework framework = new Framework(
                                 _,
                                 new Destruct(
                                     (Runnable)local.fetch(null, Root.destruct)),
                                 AMP.spawn(publisher),
                                 null != name ? publisher : null
                             );
                             return URI.resolve(here, new Exports(local).reply().
                                 run(Reflection.invoke(build, null, framework)));
                         }
                     }, (String)mother.fetch(null, Root.project), name);
                 } catch (final Exception e) {
                     return new Rejected<T>(e)._(T);
                 }
                 return Remote._(T, mother, URL);
             }
             
             private void
             vet(final String name) throws Collision {
                 if (name.startsWith(".")) { throw new Unavailable(); }
                 for (int i = name.length(); i-- != 0;) {
                     if (disallowed.indexOf(name.charAt(i)) != -1) {
                         throw new Unavailable();
                     }
                 }
             }
         }
         return new PublisherX();
     }
 
     static private final class
     Wake extends Struct implements Transaction<Void>, Powerless, Serializable {
         static private final long serialVersionUID = 1L;
 
         public Void
         run(final Root local) throws Exception {
             final Outbound outbound = (Outbound)local.fetch(null, AMP.outbound);
             for (final Outbound.Entry x : outbound.getPending()) {
                 x.msgs.resend();
             }
             return null;
         }
     }
     
     static protected final String outbound = ".outbound";
 }
