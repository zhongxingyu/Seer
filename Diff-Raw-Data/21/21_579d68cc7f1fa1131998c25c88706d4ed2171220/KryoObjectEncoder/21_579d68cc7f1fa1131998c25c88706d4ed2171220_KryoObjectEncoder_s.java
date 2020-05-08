 package com.reversemind.glia.serialization;
 
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBufferOutputStream;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelHandler;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.ObjectOutputStream;
 
 import static org.jboss.netty.buffer.ChannelBuffers.dynamicBuffer;
 
 /**
  * Copyright (c) 2013 Eugene Kalinin
  * <p/>
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * <p/>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p/>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 @ChannelHandler.Sharable
 public class KryoObjectEncoder extends OneToOneEncoder {
 
     private final static Logger LOG = LoggerFactory.getLogger(KryoObjectEncoder.class);
 
     private static final byte[] LENGTH_PLACEHOLDER = new byte[4];
 
     private final int estimatedLength;
 
     /**
      * Creates a new encoder with the estimated length of 512 bytes.
      */
     public KryoObjectEncoder() {
         this(512);
     }
 
     /**
      * Creates a new encoder.
      *
      * @param estimatedLength
      *        the estimated byte length of the serialized form of an object.
      *        If the length of the serialized form exceeds this value, the
      *        internal buffer will be expanded automatically at the cost of
      *        memory bandwidth.  If this value is too big, it will also waste
      *        memory bandwidth.  To avoid unnecessary memory copy or allocation
      *        cost, please specify the properly estimated value.
      */
     public KryoObjectEncoder(int estimatedLength) {
         if (estimatedLength < 0) {
             throw new IllegalArgumentException("estimatedLength: " + estimatedLength);
         }
         this.estimatedLength = estimatedLength;
     }
 
     @Override
     protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
         ChannelBufferOutputStream bout =
                 new ChannelBufferOutputStream(dynamicBuffer(
                         estimatedLength, ctx.getChannel().getConfig().getBufferFactory()));
         bout.write(LENGTH_PLACEHOLDER);
         ObjectOutputStream oout = new KryoCompactObjectOutputStream(bout);
         oout.writeObject(msg);
         oout.flush();
         oout.close();
 
        LOG.info("Encode message:" + msg);
 
         ChannelBuffer encoded = bout.buffer();
         encoded.setInt(0, encoded.writerIndex() - 4);
 
        LOG.info("Encode buffer index:" + encoded.writerIndex() + " capacity:" + encoded.capacity() + " readableBytes:" + encoded.readableBytes());
         return encoded;
     }
 
 }
