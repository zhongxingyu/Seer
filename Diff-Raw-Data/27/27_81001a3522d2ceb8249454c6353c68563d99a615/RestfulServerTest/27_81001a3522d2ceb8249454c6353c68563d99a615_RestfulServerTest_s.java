 /**
  * Copyright (c) 2011, yMock.com
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met: 1) Redistributions of source code must retain the above
  * copyright notice, this list of conditions and the following
  * disclaimer. 2) Redistributions in binary form must reproduce the above
  * copyright notice, this list of conditions and the following
  * disclaimer in the documentation and/or other materials provided
  * with the distribution. 3) Neither the name of the yMock.com nor
  * the names of its contributors may be used to endorse or promote
  * products derived from this software without specific prior written
  * permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
  * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
  * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
  * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package com.ymock.server;
 
 import com.ymock.server.responses.ErrorResponse;
 import com.ymock.server.responses.TextResponse;
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.junit.*;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 
 /**
  * @author Yegor Bugayenko (yegor@ymock.com)
  * @version $Id$
  */
 public final class RestfulServerTest {
 
     @Test
     public void testServerInstantiation() throws Exception {
         // start server
         final CallsProvider provider = RestfulServer.INSTANCE;
         final String request = "requested message";
         final String message = "some text";
         provider.register(new PositiveCatcher(request, message));
 
         // connect to it via HTTP and retrieve response
         final HttpClient client = new DefaultHttpClient();
         final HttpPost post = new HttpPost(this.url());
         final ResponseHandler<String> handler = new BasicResponseHandler();
         String body;
         try {
             body = client.execute(post, handler);
         } catch (java.io.IOException ex) {
             throw ex;
         } finally {
             client.getConnectionManager().shutdown();
         }
         assertThat(body, equalTo(message));
     }
 
     private static class PositiveCatcher implements Catcher {
         private final String expected;
         private final String message;
         public PositiveCatcher(final String rqt, final String msg) {
             this.expected = rqt;
             this.message = msg;
         }
         @Override
         public Response call(final String request) {
             assertThat(request, equalTo(this.expected));
             return new TextResponse(this.message);
         }
     }
 
     @Test
     public void testServerWithNegativeResponse() throws Exception {
         final String message = "some error message";
         final CallsProvider provider = RestfulServer.INSTANCE;
         provider.register(new NegativeCatcher(message));
         final HttpClient client = new DefaultHttpClient();
         final HttpPost httppost = new HttpPost(this.url());
         final ResponseHandler<String> handler = new NegativeResponseHandler();
         String body;
         try {
             body = client.execute(httppost, handler);
         } catch (java.io.IOException ex) {
             throw ex;
         } finally {
             client.getConnectionManager().shutdown();
         }
         assertThat(body, equalTo(message));
     }
 
     private static class NegativeCatcher implements Catcher {
         private final String message;
         public NegativeCatcher(final String msg) {
             this.message = msg;
         }
         @Override
         public Response call(final String request) {
             return new ErrorResponse(this.message);
         }
     }
 
     private static class NegativeResponseHandler
         implements ResponseHandler<String> {
         @Override
         public String handleResponse(final HttpResponse response) {
             assertThat(
                 response.getStatusLine().getStatusCode(),
                 equalTo(
                     javax.ws.rs.core.Response.Status.BAD_REQUEST
                     .getStatusCode()
                 )
             );
             try {
                 return IOUtils.toString(response.getEntity().getContent());
             } catch (java.io.IOException ex) {
                 throw new IllegalStateException(ex);
             }
         }
     }
 
     private String url() {
         return "http://localhost:"
             + RestfulServer.INSTANCE.port()
             + "/ymock/mock";
     }
 
 }
