 /*
  * Copyright (c) 2011, Sho SHIMIZU
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package org.galibier.core;
 
 import com.google.common.base.Preconditions;
 import org.jboss.netty.channel.ChannelFuture;
 import org.openflow.protocol.OFMessage;
 
 import java.util.Date;
 import java.util.concurrent.atomic.AtomicReference;
 
 public class OFMessageFuture {
     private final OFMessage message;
     private final ChannelFuture requestFuture;
     private final AtomicReference<OFMessage> reply = new AtomicReference<OFMessage>();
 
     public OFMessageFuture(OFMessage message, ChannelFuture requestFuture) {
         this.message = message;
         this.requestFuture = requestFuture;
     }
 
     public boolean isRequest() {
         return Constants.REQUEST_TYPE.contains(message.getType());
     }
 
     public boolean isDone() {
         if (requestFuture != null) {
             return requestFuture.isDone();
         } else {
             return false;
         }
     }
 
     public int transactionId() {
         return message.getXid();
     }
 
     public OFMessage getReply() {
         while (reply.get() == null) {
         }
 
         return reply.get();
     }
 
     public OFMessage getReply(long timeoutMillis) {
         long start = System.currentTimeMillis();
         while (true) {
             long current = System.currentTimeMillis();
             long diff = current - start;
             if (diff > timeoutMillis || reply.get() != null) {
                 return reply.get();
             }
         }
     }
 
     public void setReply(OFMessage msg) {
         Preconditions.checkState(
                 isRequest(), "Message (%s) is not a request type message", message.getType());
         Preconditions.checkArgument(
                 Constants.REPLY_TYPE.contains(msg.getType()), "Reply (%s) is not a reply type message", msg.getType());
 
         reply.set(msg);
     }
 }
