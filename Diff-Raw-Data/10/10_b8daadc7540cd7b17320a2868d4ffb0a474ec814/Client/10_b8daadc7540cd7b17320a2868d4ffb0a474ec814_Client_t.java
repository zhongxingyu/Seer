 // Copyright 2005-2006 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.net.http;
 
 import static org.joe_e.array.PowerlessArray.array;
 import static org.waterken.io.Content.chunkSize;
 
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Writer;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.util.ArrayList;
 
 import org.joe_e.Powerless;
 import org.joe_e.charset.ASCII;
 import org.ref_send.list.List;
 import org.ref_send.promise.Fulfilled;
 import org.ref_send.promise.Promise;
 import org.ref_send.promise.Rejected;
 import org.ref_send.promise.Volatile;
 import org.ref_send.promise.eventual.Do;
 import org.ref_send.promise.eventual.Loop;
 import org.ref_send.promise.eventual.Task;
 import org.waterken.http.Request;
 import org.waterken.http.Response;
 import org.waterken.http.Server;
 import org.waterken.io.bounded.Bounded;
 import org.waterken.io.limited.Limited;
 import org.waterken.io.open.Open;
 import org.waterken.io.stream.Stream;
 import org.waterken.net.Locator;
 import org.waterken.net.Execution;
 import org.waterken.uri.Header;
 
 /**
  * An HTTP protocol client session.
  * <p>
  * The connection to the remote server will be automatically retried until a non
  * 5xx HTTP response is received for each pending HTTP request. The responses
  * are settled in the same order as the requests were registered.
  * </p>
  */
 public final class
 Client implements Server {
     
     /**
      * The minimum milliseconds to wait before retrying a connection.
      * <p>
      * This value reflects the amount of time for a network hiccup to sort
      * itself out. 
      * </p>
      */
     static private final long minSleep = 60 * 1000;
     
     /**
      * The maximum milliseconds to wait before retrying a connection.
      * <p>
      * This value reflects the amount of time expected for an administrator to
      * intervene.
      * </p>
      */
     static private final long maxSleep = 60 * 60 * 1000;
     
     /**
      * Indicates a configuration error on the remote server.
      */
     static private final class
     Nap extends IOException implements Powerless {
         static private final long serialVersionUID = 1L;
         
        Nap(final String message) {
            super(message);
        }
     }
     
     static public interface
     Outbound extends Task {}
     
     static public interface
     Inbound extends Task {}
     
     private final String host;
     private final Locator locator;
     private final Execution thread;
     private final Loop<Outbound> sender;
     private final Loop<Inbound> receiver;
 
     private
     Client(final String host, final Locator locator, final Execution thread,
            final Loop<Outbound> sender, final Loop<Inbound> receiver) {
         this.host = host;
         this.locator = locator;
         this.thread = thread;
         this.sender = sender;
         this.receiver = receiver;
     }
     
     /*
      * This implementation uses a pair of communicating event loops: the
      * sender event loop and the receiver event loop. Outgoing HTTP requests
      * are put on the wire in the sender event loop. Incomming HTTP
      * responses are taken off the wire in the receiver event loop.
      * Connection initiation is done from the sender event loop. The queue
      * of pending request/response exchanges is owned by the sender event
      * loop. For each pending exchange, a send task is posted to the sender
      * event loop. Whether the send task successfully sends its request or
      * not, it will put a corresponding receive task on the receiver event
      * loop. When the receive task is run, it will pull the HTTP response
      * off the wire, and deliver it to the provided response resolver. A
      * task to pop the exchange from the pending exchange queue is then
      * queued on the sender event loop. Any I/O error with the connection is
      * handled by a receive task. The receive task will close down the
      * socket, mark the current connection as dead, and queue a connection
      * initiation task on the sender event loop, whereupon we start the
      * process from scratch.
      */
     
     class Connection implements Outbound {
         private         SocketAddress mostRecent;
         private         Outbound retry;
         
         private         Socket socket;
         protected       InputStream in;
         protected       OutputStream out;
         
         Connection(final SocketAddress mostRecent, final Outbound retry) {
             this.mostRecent = mostRecent;
             this.retry = retry;
         }
         
         public void
         run() {
             for (long a = 0, b = minSleep; true;) {
                 try {
                     socket = locator.locate(host, mostRecent);
                     in = socket.getInputStream();
                     out = socket.getOutputStream();
                     break;
                 } catch (final IOException e) {
                     mostRecent = null;
                     if (null != socket) {
                         try { socket.close(); } catch (final IOException e2) {}
                         socket = null;
                     }
                     in = null;
                     out = null;
 
                     // Wait and try again.
                     if (b < maxSleep) {
                         final long c = a + b;
                         a = b;
                         b = c;
                         b = Math.min(b, maxSleep);
                     }
                     try {
                         thread.sleep(b);
                     } catch (final Exception e2) {
                         // Just go around again.
                     }
                 }
             }
             mostRecent = null;
         }
         
         void
         retry() {
             in = null;
             out = null;
             sender.run(retry);
             retry = null;
             try { socket.close(); } catch (final Exception e) {}
             socket = null;
         }
     }
     class Exchange extends Do<Response,Void> {
         protected final Promise<Request> request;
         private   final Do<Response,?> respond;
         private   final Outbound pop;
         
         Exchange(final Promise<Request> request,
                  final Do<Response,?> respond,
                  final Outbound pop) {
             this.request = request;
             this.respond = respond;
             this.pop = pop;
         }
         
         public Void
         fulfill(final Response value) throws Exception {
             respond.fulfill(value); // don't pop if an I/O error is encountered
             sender.run(pop);
             return null;
         }
         
         public Void
         reject(final Exception reason) throws Exception {
             respond.reject(reason);
             sender.run(pop);
             return null;
         }
     }
     class Receive implements Inbound {
         private final Connection on;
         private final Exchange x;
         
         Receive(final Connection on, final Exchange x) {
             this.on = on;
             this.x = x; 
         }
         
         public void
         run() throws Exception {
             // Only proceed if the connection is still active. Once a
             // response has failed, no more can be received. The first
             // failure is responsible for scheduling the connection retry.
             if (null == on.in) { return; }
             try {
                 final Request q;
                 try {
                     q = (Request)x.request.cast();
                 } catch (final Exception reason) {
                     x.reject(reason);
                     return;
                 }
 
                 if (!receive(q.method, on.in, x)) { on.retry(); }
             } catch (final Exception e) {
                 if (e instanceof Nap) {
                     sender.run(new Outbound() {
                         public void
                         run() throws Exception { thread.sleep(maxSleep); }
                     });
                 }
                 on.retry();
                 throw e;
             }
         }
     }
     class Send implements Outbound {
         private final Connection on;
         private final Exchange x;
         
         Send(final Connection on, final Exchange x) {
             this.on = on;
             this.x = x;
         }
         
         public void
         run() {
             final Request q;
             try {
                 q = (Request)x.request.cast();
             } catch (final Exception reason) {
                 // Nothing to send since request failed to render.
                 receiver.run(new Receive(on, x));
                 return;
             }
             try {
                 send(q, on.out);
             } catch (final Exception e) {
                 final OutputStream tmp = on.out;
                 on.out = null;
                 try { tmp.close(); } catch (final Exception e2) {}
             }
             receiver.run(new Receive(on, x));
         }
     }
     class Retry implements Outbound {
         private       SocketAddress mostRecent = null;
         private       Connection current = null;
         private final List<Exchange> pending = List.list();
         
         public void
         run() {
             current = new Connection(mostRecent, this);
             sender.run(current);
             sender.run(new Outbound() {
                 public void
                 run() { mostRecent = current.socket.getRemoteSocketAddress(); }
             });
             for (final Exchange x : pending) {
                 sender.run(new Send(current, x));
             }
         }
         
         void
         enqueue(final Volatile<Request> request, final Do<Response,?> respond) {
             Promise<Request> requestX;
             try {
                 requestX = Fulfilled.ref(request.cast());
             } catch (final Exception reason) {
                 requestX = new Rejected<Request>(reason);
             }
             final Exchange x = new Exchange(requestX, respond, new Outbound() {
                 public void
                 run() { pending.pop(); }
             });
             sender.run(new Outbound() {
                 public void
                 run() {
                     pending.append(x);
                     sender.run(new Send(current, x));
                 }
             });
         }
     }
     
     private Retry entry;
     
     void
     start() {
         entry = new Retry();
         sender.run(entry);
     }
     
     /**
      * Constructs an instance.
      * <p>
      * Each response block will be invoked from the receiver event loop.
      * </p>
      * @param host      URL identifying the remote host  
      * @param locator   socket factory
      * @param thread    authority to sleep
      * @param sender    HTTP request event loop
      * @param receiver  HTTP response event loop
      */
     static public Server
     make(final String host, final Locator locator, final Execution thread,
          final Loop<Outbound> sender, final Loop<Inbound> receiver) {
         final Client r = new Client(host, locator, thread, sender, receiver);
         r.start();
         return r;
     }
 
     public void
     serve(final String resource,
           final Volatile<Request> request,
           final Do<Response,?> respond) { entry.enqueue(request, respond); }
     
     /**
      * Sends an HTTP request.
      * @param request   request to send
      * @param output    output stream
      * @throws Exception    any problem sending the request
      */
     static public void
     send(final Request request, final OutputStream output) throws Exception {
         final OutputStream out =
             new BufferedOutputStream(output, chunkSize - "0\r\n\r\n".length());
 
         // Output the Request-Line.
         HTTP.vetToken(request.method);
         HTTP.vet(" ", request.URL);
         
         final Writer hout = ASCII.output(Open.output(out));
         hout.write(request.method);
         hout.write(" ");
         hout.write(request.URL);
         hout.write(" HTTP/1.1\r\n");
 
         // Output the header.
         boolean contentLengthSpecified = false;
         long length = 0;
         boolean selfDelimiting = null == request.body;
         for (final Header h : request.header) {
             if (!contentLengthSpecified &&
                 "Content-Length".equalsIgnoreCase(h.name)) {
                 contentLengthSpecified = true;
                 length = Long.parseLong(h.value);
                 if (0 > length) { throw new Exception("Bad Length"); }
                 selfDelimiting = true;
             } else {
                 for (final String name : new String[] { "Content-Length",
                                                         "Connection",
                                                         "Transfer-Encoding",
                                                         "TE",
                                                         "Trailer",
                                                         "Upgrade" }) {
                     if (name.equalsIgnoreCase(h.name)) {
                         throw new Exception("Illegal request header");
                     }
                 }
             }
             HTTP.vetHeader(h);
 
             hout.write(h.name);
             hout.write(": ");
             hout.write(h.value);
             hout.write("\r\n");
         }
 
         // Output the entity body.
         if (selfDelimiting) {
             hout.write("\r\n");
             hout.flush();
             hout.close();
 
             final OutputStream bout = Bounded.output(length, out);
             if (null != request.body) { request.body.writeTo(bout); }
             bout.close();
         } else {
             hout.write("Transfer-Encoding: chunked\r\n");
             hout.write("\r\n");
             hout.flush();
             hout.close();
 
             final OutputStream bout = new ChunkedOutputStream(chunkSize, out);
             request.body.writeTo(bout);
             bout.close();
         }
         out.flush();
     }
     
     /**
      * Receives an HTTP response.
      * @param method    HTTP request method
      * @param cin       input stream
      * @param respond   response block
      * @return should the connection be kept alive?
      * @throws Exception    any problem reading the response
      */
     static public boolean
     receive(final String method, final InputStream cin,
             final Do<Response,Void> respond) throws Exception {
         final LineInput hin = new LineInput(Limited.input(32 * 1024, cin));
 
         // read the Status-Line
         final String statusLine = hin.readln();
        if (!statusLine.startsWith("HTTP/1.")) {throw new Nap("incompatible");}
         final int endStatusLine = statusLine.length();
 
         // parse the Status-Line
         final int beginVersion = 0;
         final int endVersion =
             HTTP.findSP(statusLine, "HTTP/1.".length(), endStatusLine);
         final String version = statusLine.substring(beginVersion, endVersion);
         final int beginStatus =
             HTTP.skipSP(statusLine, endVersion + 1, endStatusLine);
         final int endStatus =
             HTTP.findSP(statusLine, beginStatus + 1, endStatusLine);
         final String status = statusLine.substring(beginStatus, endStatus);
         final String phrase = statusLine.substring(endStatus + 1);
 
         // sleep on a 5xx response
        if (status.startsWith("5")) { throw new Nap(phrase); }
 
         // parse the response headers
         final ArrayList<Header> header = new ArrayList<Header>(16);
         HTTP.readHeaders(header, hin);
 
         // check for informational response
         if (status.startsWith("1")) {
             // RFC 2616, section 10.1:
             // Unexpected 1xx status responses MAY be ignored by a user agent.
             return receive(method, cin, respond);
         }
 
         // build the response
         final boolean persist = HTTP.persist(version, header);
         final InputStream entity;
         if ("204".equals(status)) {
             entity = null;
         } else if ("304".equals(status)) {
             entity = null;
         } else if ("HEAD".equals(method)) {
             entity = null;
         } else if ("CONNECT".equals(method)) {
             entity = null;
         } else {
             // with the exception of the cases handled above, all responses have
             // a message body, which is either explicitly delimited, or
             // terminated by connection close
             final InputStream explicit = HTTP.body(header, cin);
             entity = null != explicit ? explicit : cin;
         }
         respond.fulfill(new Response(version, status, phrase,
             array(header.toArray(new Header[header.size()])),
             null != entity ? new Stream(entity) : null));
         
         // ensure this response has been fully read out of the
         // response stream before reading in the next response
         if (persist && null != entity) {
             while (entity.read() != -1) { entity.skip(Long.MAX_VALUE); }
             entity.close();
         }
         return persist;
     }
 }
