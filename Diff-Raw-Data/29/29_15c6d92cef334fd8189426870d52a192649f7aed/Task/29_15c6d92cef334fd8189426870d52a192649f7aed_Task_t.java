 // Copyright 2010 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.remote.http;
 
 import org.joe_e.array.PowerlessArray;
 import org.ref_send.promise.Promise;
 import org.waterken.http.Message;
 import org.waterken.http.Request;
 import org.waterken.http.Response;
 import org.waterken.uri.Header;
 
 /**
  * A task to be executed in message order, but with no corresponding message.
  */
 /* package */ abstract class
 Task extends Operation implements Promise<Void> {
     static private final long serialVersionUID = 1L;
     
     protected
     Task() { super(false, false); }
 
     protected final Message<Request>
     render(final String x, final long w, final int m) throws Exception {
         return new Message<Request>(new Request("HTTP/1.1", "OPTIONS", "*",
             PowerlessArray.array(new Header[] {})), null);
     }
 
     protected final void
     fulfill(final String request, final Message<Response> ignored) { call(); }
 
     protected final void
     reject(final String request, final Exception ignored) { call(); }
     
    public abstract Void
     call();
 }
