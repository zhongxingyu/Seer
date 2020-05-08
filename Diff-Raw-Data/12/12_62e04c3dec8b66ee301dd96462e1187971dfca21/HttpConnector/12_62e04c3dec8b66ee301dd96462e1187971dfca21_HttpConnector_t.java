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
 package com.ymock.client;
 
 // commons
 import com.ymock.commons.Logger;
 import com.ymock.commons.PortDetector;
 import com.ymock.commons.YMockException;
 
 // IO utils from commons-io:commons-io
 import org.apache.commons.io.IOUtils;
 
 // apache httpcomponents:httpclient
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 /**
  * HTTP connector between YMockClient and remote YMockServer.
  *
  * @author Yegor Bugayenko (yegor@ymock.com)
  * @version $Id$
  */
 public final class HttpConnector implements Connector {
 
     /**
      * HTTP client.
      */
     private HttpClient client = new DefaultHttpClient();
 
     /**
      * Public ctor.
      */
     public HttpConnector() {
         // intentionally empty
     }
 
     /**
      * Protected ctor, only for unit testing.
      * @param clt Custom HTTP client
      */
     protected HttpConnector(final HttpClient clt) {
         this.client = clt;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String call(final String request) throws YMockException {
         try {
             final long start = System.currentTimeMillis();
             final HttpPost post = new HttpPost(this.url());
             post.setEntity(new StringEntity(request));
             final HttpResponse response = this.client.execute(post);
            final String body = IOUtils.toString(
                response.getEntity().getContent()
            );
             if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                 throw new YMockException(body);
             }
             Logger.debug(
                 this,
                "#call('%d bytes'): returned %d bytes, in %dms",
                 request.length(),
                 body.length(),
                System.currentTimeMillis() - start
             );
             return body;
         } catch (java.io.IOException ex) {
             throw new YMockException(ex);
         }
     }
 
     /**
      * Build URL to connect to.
      * @return The URL
      */
     private String url() {
         return "http://localhost:"
             + new PortDetector().port()
             + "/ymock/mock";
     }
 
 }
