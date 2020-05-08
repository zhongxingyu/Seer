 package com.reversemind.glia.serialization;
 
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBufferInputStream;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
 import org.jboss.netty.handler.codec.serialization.ClassResolver;
 import org.jboss.netty.handler.codec.serialization.ClassResolvers;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Copyright (c) 2013 Eugene Kalinin
  *
  * Netty ObjectDecoder experiments
  *
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
 public class KryoObjectDecoder extends LengthFieldBasedFrameDecoder {
 
     private final static Logger LOG = LoggerFactory.getLogger(KryoObjectDecoder.class);
 
     private final ClassResolver classResolver;
     /**
      * Creates a new decoder whose maximum object size is {@code 1048576}
      * bytes.  If the size of the received object is greater than
      * {@code 1048576} bytes, a {@link java.io.StreamCorruptedException} will be
      * raised.
      *
      * @deprecated use {@link #KryoObjectDecoder(ClassResolver)}
      */
     @Deprecated
     public KryoObjectDecoder() {
         this(KryoSettings.EXPERIMENT_MAX_SIZE);
     }
 
     /**
      * Creates a new decoder whose maximum object size is {@code 1048576}
      * bytes.  If the size of the received object is greater than
      * {@code 1048576} bytes, a {@link java.io.StreamCorruptedException} will be
      * raised.
      *
      * @param classResolver  the {@link ClassResolver} to use for this decoder
      */
     public KryoObjectDecoder(ClassResolver classResolver) {
         this(KryoSettings.EXPERIMENT_MAX_SIZE, classResolver);
     }
 
     /**
      * Creates a new decoder with the specified maximum object size.
      *
      * @param maxObjectSize  the maximum byte length of the serialized object.
      *                       if the length of the received object is greater
      *                       than this value, {@link java.io.StreamCorruptedException}
      *                       will be raised.
      * @deprecated           use {@link #KryoObjectDecoder(int, ClassResolver)}
      */
     @Deprecated
     public KryoObjectDecoder(int maxObjectSize) {
         this(maxObjectSize, ClassResolvers.weakCachingResolver(null));
     }
 
     /**
      * Creates a new decoder with the specified maximum object size.
      *
      * @param maxObjectSize  the maximum byte length of the serialized object.
      *                       if the length of the received object is greater
      *                       than this value, {@link java.io.StreamCorruptedException}
      *                       will be raised.
      * @param classResolver    the {@link ClassResolver} which will load the class
      *                       of the serialized object
      */
     public KryoObjectDecoder(int maxObjectSize, ClassResolver classResolver) {
         super(maxObjectSize, 0, 4, 0, 4);
         if (classResolver == null) {
             throw new NullPointerException("classResolver");
         }
         this.classResolver = classResolver;
     }
 
     /**
      * Create a new decoder with the specified maximum object size and the {@link ClassLoader}
      * wrapped in {@link ClassResolvers#weakCachingResolver(ClassLoader)}.
      *
      * @param maxObjectSize  the maximum byte length of the serialized object.
      *                       if the length of the received object is greater
      *                       than this value, {@link java.io.StreamCorruptedException}
      *                       will be raised.
      * @param classLoader    the the classloader to use
      * @deprecated           use {@link #KryoObjectDecoder(int, ClassResolver)}
      */
     @Deprecated
     public KryoObjectDecoder(int maxObjectSize, ClassLoader classLoader) {
         this(maxObjectSize, ClassResolvers.weakCachingResolver(classLoader));
     }
 
     @Override
     protected Object decode(
             ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
 
         if(buffer != null){
            LOG.debug("Buffer readable bytes:" + buffer.readableBytes());
         }
 
         ChannelBuffer frame = (ChannelBuffer) super.decode(ctx, channel, buffer);
         if (frame == null) {
             return null;
         }
 
         return new KryoCompactObjectInputStream(
                 new ChannelBufferInputStream(frame), classResolver).readObject();
     }
 
     @Override
     protected ChannelBuffer extractFrame(ChannelBuffer buffer, int index, int length) {
         return buffer.slice(index, length);
     }
 }
