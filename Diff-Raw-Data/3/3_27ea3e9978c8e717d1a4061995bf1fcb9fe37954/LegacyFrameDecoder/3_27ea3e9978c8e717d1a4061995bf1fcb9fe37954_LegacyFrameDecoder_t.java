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
 
 package de.cosmocode.palava.ipc.legacy;
 
 import java.nio.ByteBuffer;
 
 import javax.annotation.concurrent.NotThreadSafe;
 
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.handler.codec.frame.FrameDecoder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Charsets;
 
 import de.cosmocode.palava.bridge.Header;
 import de.cosmocode.palava.bridge.call.CallType;
 
 /**
  * Decodes framed chunks of the legacy palava protocol which looks like:<br />
  * {@code <type>://<aliasedName>/<sessionId>/(<contentLength>)?<content>}.
  *
  * @since 1.0 
  * @author Willi Schoenborn
  */
 @NotThreadSafe
 final class LegacyFrameDecoder extends FrameDecoder {
 
     private static final Logger LOG = LoggerFactory.getLogger(LegacyFrameDecoder.class);
     
     /**
      * Identifies the different parts of the protocol structure.
      *
      * @author Willi Schoenborn
      */
     private static enum Part {
      
         TYPE, 
         
         COLON, 
         
         FIRST_SLASH, 
         
         SECOND_SLASH, 
         
         NAME, 
         
         THIRD_SLASH,
         
         SESSION_ID, 
         
         FOURTH_SLASH, 
         
         LEFT_PARENTHESIS, 
         
         CONTENT_LENGTH,
         
         RIGHT_PARENTHESIS, 
         
         QUESTION_MARK, 
         
         CONTENT;
         
     }
     
     /**
      * Defines the current part. The readerIndex of the buffer will
      * be set to the first byte of the corresponding part.
      */
     private Part part;
 
     private String type;
     
     private String name;
     
     private String sessionId;
     
     private int length;
     
     private ByteBuffer content;
     
     // Reducing cyclomatic complexity would dramatically reduce readability
     /* CHECKSTYLE:OFF */
     @Override
     /* CHECKSTYLE:ON */
     protected Header decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
         if (buffer.readable()) {
             if (part == null) part = Part.TYPE;
             
             if (part == Part.CONTENT) {
                 return contentOf(buffer);
             }
             
             for (int i = buffer.readerIndex(); i < buffer.writerIndex(); i++) {
                 final byte c = buffer.getByte(i);
                 switch (part) {
                     case TYPE: {
                         if (c == ':') {
                             type = readAndIncrement(buffer, i);
                             LOG.trace("Setting type to {}", type);
                             part = Part.COLON;
                         }
                         break;
                     }
                     case COLON: {
                         buffer.skipBytes(1);
                         checkState(c == '/', ": must be followed by first / but was %s", c);
                         part = Part.FIRST_SLASH;
                         break;
                     }
                     case FIRST_SLASH: {
                         buffer.skipBytes(1);
                         checkState(c == '/', "first / must be followed by second / but was %s", c);
                         part = Part.SECOND_SLASH;
                         break;
                     }
                     case SECOND_SLASH: {
                         buffer.skipBytes(1);
                         part = Part.NAME;
                         i--;
                         break;
                     }
                     case NAME: {
                         // c == *second* char of name
                         if (c == '/') {
                             name = readAndIncrement(buffer, i);
                             LOG.trace("Setting name to {}", name);
                             part = Part.THIRD_SLASH;
                         }
                         break;
                     }
                     case THIRD_SLASH: {
                         buffer.skipBytes(1);
                         part = Part.SESSION_ID;
                        i--;
                         break;
                     }
                     case SESSION_ID: {
                        // c == *second* char of sessionId
                         if (c == '/') {
                             sessionId = readAndIncrement(buffer, i);
                             LOG.trace("Setting sessionId to {}", sessionId);
                             part = Part.FOURTH_SLASH;
                         }
                         break;
                     }
                     case FOURTH_SLASH: {
                         buffer.skipBytes(1);
                         checkState(c == '(', "fourth / must be followed by ( but was %s", c);
                         part = Part.LEFT_PARENTHESIS;
                         break;
                     }
                     case LEFT_PARENTHESIS: {
                         buffer.skipBytes(1);
                         part = Part.CONTENT_LENGTH;
                         break;
                     }
                     case CONTENT_LENGTH: {
                         if (c == ')') {
                             final String value = readAndIncrement(buffer, i);
                             length = Integer.parseInt(value);
                             LOG.trace("Setting content length to {}", value);
                             part = Part.RIGHT_PARENTHESIS;
                         }
                         break;
                     }
                     case RIGHT_PARENTHESIS: {
                         buffer.skipBytes(1);
                         checkState(c == '?', ") must be followed by ? but was %s", c);
                         part = Part.QUESTION_MARK;
                         break;
                     }
                     case QUESTION_MARK: {
                         buffer.skipBytes(1);
                         part = Part.CONTENT;
                         return null;
                     }
                     default: {
                         throw new AssertionError("Default case matched part " + part);
                     }
                 }
             }
             
         }
         
         return null;
     }
     
     private Header contentOf(ChannelBuffer buffer) {
         if (buffer.readableBytes() >= length) {
             content = buffer.toByteBuffer(buffer.readerIndex(), length);
             LOG.trace("Setting content to {}", content);
             buffer.skipBytes(length);
             final Header header = new InternalHeader();
             LOG.trace("Incoming call {}", header);
             reset();
             return header;
         } else {
             return null;
         }
     }
     
     private String readAndIncrement(ChannelBuffer buffer, int currentIndex) {
         final int size = currentIndex - buffer.readerIndex();
         final String string = buffer.toString(buffer.readerIndex(), size, Charsets.UTF_8);
         buffer.readerIndex(currentIndex);
         return string;
     }
 
     private void checkState(boolean state, String format, byte c) {
         if (state) {
             return;
         } else {
             throw new IllegalArgumentException(String.format(format, (char) c)); 
         }
     }
     
     private void reset() {
         part = null;
         type = null;
         name = null;
         sessionId = null;
         length = 0;
         content = null;
     }
     
     /**
      * Internal implementation of the {@link Header} interface.
      *
      * @since 
      * @author Willi Schoenborn
      */
     private final class InternalHeader implements Header {
         
         private final CallType callType = CallType.valueOf(type.toUpperCase());
         
         private final String name = LegacyFrameDecoder.this.name;
         
         private final String sessionId = LegacyFrameDecoder.this.sessionId;
         
         private final int length = LegacyFrameDecoder.this.length;
         
         private final ByteBuffer content = LegacyFrameDecoder.this.content;
         
         @Override
         public CallType getCallType() {
             return callType;
         }
         
         @Override
         public String getAliasedName() {
             return name;
         }
         
         @Override
         public String getSessionId() {
             return sessionId;
         }
         
         @Override
         public int getContentLength() {
             return length;
         }
         
         @Override
         public ByteBuffer getContent() {
             return content;
         }
 
         @Override
         public String toString() {
             return String.format("Header [callType=%s, name=%s, sessionId=%s, contentLength=%s, content=%s]",
                 callType, getAliasedName(), getSessionId(), getContentLength(), getContent()
             );
         }
         
     }
     
 }
