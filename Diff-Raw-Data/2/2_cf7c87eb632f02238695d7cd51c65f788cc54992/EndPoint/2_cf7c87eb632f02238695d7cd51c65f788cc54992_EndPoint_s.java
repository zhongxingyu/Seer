 /**
  * Copyright (c) 2012, 2013 SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.web.push;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.servlet.AsyncContext;
 
 import nl.surfnet.bod.web.security.RichUserDetails;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public interface EndPoint {
   void sendMessage(String message);
 
   void send(String type, String data);
 
   RichUserDetails getUser();
 
   void setAsyncContext(AsyncContext asyncContext);
 
   class LongPollEndPoint implements EndPoint {
     private final Logger logger = LoggerFactory.getLogger(LongPollEndPoint.class);
 
     private AsyncContext asyncContext;
     private final String id;
     private final RichUserDetails user;
     private final AtomicInteger eventId = new AtomicInteger(0);
 
     public LongPollEndPoint(String id, RichUserDetails user) {
       this.id = id;
       this.user = user;
     }
 
     @Override
     public void setAsyncContext(AsyncContext asyncContext) {
       this.asyncContext = asyncContext;
     }
 
     @Override
     public RichUserDetails getUser() {
       return user;
     }
 
     @Override
     public void sendMessage(String message) {
       send("message", message);
     }
 
     @Override
     public void send(String type, String data) {
       if (asyncContext.getRequest().isAsyncStarted()) {
         eventId.incrementAndGet();
         PrintWriter writer;
         try {
           writer = asyncContext.getResponse().getWriter();
           String template = "{\"type\": \"%s\", \"data\": %s, \"id\": %d}";
           writer.write(String.format(template, type, data, eventId.get()));
           writer.flush();
           asyncContext.complete();
         }
         catch (IOException e) {
           logger.warn("Could not sent async message", e);
         }
       }
       else {
        logger.debug("Could not send message to {}, {}", id, data);
       }
     }
 
   }
 }
