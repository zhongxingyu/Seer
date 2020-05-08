 // Copyright 2002-2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.net.http;
 
 import java.io.InputStream;
 import java.net.Socket;
 
 import org.joe_e.array.PowerlessArray;
 import org.ref_send.promise.Promise;
 import org.ref_send.promise.Receiver;
 import org.waterken.http.Request;
 import org.waterken.http.TokenList;
 import org.waterken.io.limited.Limited;
 import org.waterken.uri.Header;
 
 /**
  * The server side of the HTTP protocol.
  */
 final class
 ServerSide implements Promise<Void> {
 
     private final HTTPD config;
     private final Receiver<?> yield;
     private final String location;
     private final Socket socket;
 
     /**
      * Constructs an instance.
      * <p>
      * The implementation is designed to be defensively consistent with
      * respect to both the remote client and the local
      * {@link HTTPD#server server}. The implementation further attempts to not
      * give the remote client a DOS force multiplier.
      * </p>
      * @param config    configuration
      * @param location  expected value of the Host header
      * @param socket    connection socket, trusted to behave like a socket, but
      *                  not trusted to be connected to a trusted HTTP client
      * @param yield     yield to other threads
      */
     ServerSide(final HTTPD config, final String location, 
                final Socket socket, final Receiver<?> yield){
         this.config = config;
         this.yield = yield;
         this.location = location;
         this.socket = socket;
     }
 
    // org.ref_send.promise.Promise interface
 
     public Void
     call() throws Exception {
         socket.setTcpNoDelay(true);
         socket.setSoTimeout(config.soTimeout);
         final InputStream connection = socket.getInputStream();
         Responder current=new Responder(config.server,socket.getOutputStream());
         while (true) {
 
             // read the Request-Line
             final LineInput hin =
                 new LineInput(Limited.input(32 * 1024, connection));
             final String requestLine = hin.readln();
             final int endRequestLine = requestLine.length();
 
             // empty line is ignored
             if (endRequestLine == 0) { continue; }
 
             // parse the Method
             final int beginMethod = 0;
             final int endMethod = TokenList.skip(
                 TokenList.token, TokenList.nothing,
                 requestLine, beginMethod, endRequestLine);
             if (' ' != requestLine.charAt(endMethod)) { throw new Exception(); }
             final String method = requestLine.substring(beginMethod, endMethod);
 
             // parse the Request-URI
             final int beginRequestURI = endMethod + 1;
             final int endRequestURI = requestLine.indexOf(' ', beginRequestURI);
             final String requestURI = -1 == endRequestURI
                 ? requestLine.substring(beginRequestURI)
             : requestLine.substring(beginRequestURI, endRequestURI);
 
             // parse the HTTP-Version
             final String version = -1 == endRequestURI
                 ? "HTTP/0.9"
             : requestLine.substring(endRequestURI + 1);
 
             // parse the request based on the protocol version
             final InputStream body;
             final PowerlessArray<Header> headers;
             if (version.startsWith("HTTP/1.")) {
                 final PowerlessArray<Header> all = HTTPD.readHeaders(hin); 
                 body = HTTPD.input(all, connection);
                 headers = HTTPD.forward(version, all, current.closing);
             } else if (version.startsWith("HTTP/0.")) {
                 // old HTTP client; no headers, no content
                 current.closing.mark(true);
                 headers = PowerlessArray.array();
                 body = null;
             } else {
                 throw new Exception("HTTP Version Not Supported: " + version);
             }
 
             // do some sanity checking on the request
             if (null != body && "TRACE".equals(method)) {
                 throw new Exception("No entity allowed in TRACE");
             }
             if (null == body &&
                     null != TokenList.find(null, "Content-Type", headers)) {
                 throw new Exception("unknown message length");
             }
 
             // determine the request target
             String host = TokenList.find(null, "Host", headers);
             if (null == host) {
                 if (version.startsWith("HTTP/1.") &&
                         !version.equals("HTTP/1.0")) {
                     throw new Exception("Missing Host header");
                 }
                 host = "localhost";
             }
             if (!Header.equivalent(location, host)) {
                 // client is hosting this server under the wrong origin
                 // this could lead to a browser side scripting attack
                 throw new Exception("wrong origin: " + host);
             }
 
             // process the request
             final Responder next = current.follow(version, method);
             try {
                 config.server.serve(new Request(version, method, requestURI,
                                                 headers), body, current);
             } catch (final Exception e) {
                 current.fail(e);
                 throw e;
             }
             
             // prepare for next request
             if (current.closing.is() || !next.isStillWaiting()) { break; }
             current = next;
 
             // ensure the request body is removed from the input stream
             if (null != body) {
                 while (body.read() != -1) { body.skip(Long.MAX_VALUE); }
                 body.close();
             }
 
             // now is a good time for a context switch since we're not holding
             // any locks, or much memory
             yield.run(null);
         }
         return null;
     }
 }
