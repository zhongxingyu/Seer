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
 package nl.surfnet.bod.nsi.v2;
 
 import java.io.IOException;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import javax.xml.bind.JAXBException;
 import javax.xml.soap.SOAPException;
 import javax.xml.soap.SOAPMessage;
 import javax.xml.ws.Provider;
 import javax.xml.ws.Service.Mode;
 import javax.xml.ws.ServiceMode;
 import javax.xml.ws.WebServiceProvider;
 
 import com.google.common.base.Objects;
 import com.sun.xml.ws.developer.SchemaValidation;
 
 import nl.surfnet.bod.util.JaxbUserType;
 
 import org.ogf.schemas.nsi._2013._07.framework.headers.CommonHeaderType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Simple server that always replies "200 OK" and exposes a queue of response objects,
  * which you obviously should only use poll() on.
  *
  */
 @WebServiceProvider(serviceName = "ConnectionServiceRequester", portName = "ConnectionServiceRequesterPort", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/request")
 @ServiceMode(Mode.MESSAGE)
 @SchemaValidation
 public class SoapReplyListener implements Provider<SOAPMessage> {
 
   private static final Logger LOG = LoggerFactory.getLogger(SoapReplyListener.class);
 
   private BlockingQueue<SOAPMessage> responses = new ArrayBlockingQueue<>(1); // we can only deal with one response at a time
 
   public static class Message<T> {
     public CommonHeaderType header;
     public T body;
 
     public Message(CommonHeaderType header, T body) {
       this.header = header;
       this.body = body;
     }
 
     @Override
     public int hashCode() {
       return Objects.hashCode(header, body);
     }
 
     @Override
     public boolean equals(Object obj) {
       if (this == obj)
         return true;
       if (obj == null || getClass() != obj.getClass())
         return false;
       Message<?> that = (Message<?>) obj;
       return Objects.equal(this.body, that.body) && Objects.equal(this.header, that.header);
     }
 
     @Override
     public String toString() {
       return "Message [header=" + header + ", body=" + body + "]";
     }
   }
 
   public <T> Message<T> getLastReply(JaxbUserType<T> converter) {
     try {
       SOAPMessage message = responses.poll(5000, TimeUnit.MILLISECONDS);
       CommonHeaderType header = Converters.parseNsiHeader(message);
       T body = Converters.parseBody(converter, message);
       return new Message<>(header, body);
     } catch (InterruptedException | SOAPException | JAXBException e) {
       throw new RuntimeException("response did not arrive in time", e);
     }
   }
 
   @Override
   public SOAPMessage invoke(SOAPMessage asyncReply) {
     try {
       LOG.warn("received SOAP message: {}", Converters.serializeMessage(asyncReply));
       responses.add(asyncReply);
     } catch (IOException | SOAPException e) {
       e.printStackTrace();
     }
    return null;
   }
 }
