 /**
  * Copyright 2010 CosmoCode GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.cosmocode.palava.ipc.json;
 
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.handler.codec.frame.FrameDecoder;
 
 import javax.annotation.concurrent.NotThreadSafe;
 
 /**
  * A {@link FrameDecoder} which frames json arrays/objects.
  * 
  * @since 1.0
  * @author Willi Schoenborn
  * @author Tobias Sarnowski
  */
 @NotThreadSafe
 final class JsonFrameDecoder extends FrameDecoder {
 
     private char open;
     
     private char close;
     
     private int counter;
     
     private int index;
     
     private boolean string;
     
     private boolean escaped;
     
     @Override
     protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
         if (buffer.readable()) {
             readFirst(buffer);
             count(buffer);
             if (counter == 0) {
                 final ChannelBuffer result = buffer.readBytes(index);
                 // reset for next iteration
                 index = 0;
                 open = 0;
                 close = 0;
                 return result;
             }
         }
         return null;
     }
     
     private void readFirst(ChannelBuffer buffer) {
         if (open == 0 || close == 0) {
             final byte b = buffer.getByte(0);
             if (b == '[') {
                 open = '[';
                 close = ']';
             } else if (b == '{') {
                 open = '{';
                 close = '}';
             } else {
                throw new IllegalArgumentException(String.format("Unknown starting character %s", (char) b));
             }
         }
     }
     
     private void count(ChannelBuffer buffer) {
         int i = buffer.readerIndex() + index;
 
         while (i < buffer.writerIndex()) {
             if (string) {
                 inString(buffer.getByte(i));
             } else {
                 outsideOfString(buffer.getByte(i));
 
             }
             
             i++;
 
             if (counter == 0) break;
         }
         index = i - buffer.readerIndex();
     }
     
     private void inString(byte current) {
         if (current == '"' && !escaped) {
             string = false;
         } else if (current == '\\' && !escaped) {
             escaped = true;
         } else if (escaped) {
             escaped = false;
         }
     }
     
     private void outsideOfString(byte current) {
         if (current == open) {
             counter += 1;
         } else if (current == close) {
             counter -= 1;
         }
     }
 
     int getCounter() {
         return counter;
     }
     
 }
