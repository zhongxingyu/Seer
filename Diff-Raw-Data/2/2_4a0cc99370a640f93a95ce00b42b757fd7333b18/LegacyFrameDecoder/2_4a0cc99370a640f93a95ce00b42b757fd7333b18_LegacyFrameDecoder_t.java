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
 import org.jboss.netty.handler.codec.replay.ReplayingDecoder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Charsets;
 
 import de.cosmocode.palava.bridge.Header;
 import de.cosmocode.palava.bridge.call.CallType;
 
 /**
  * Legacy {@link ReplayingDecoder} to support the legacy php protocol which looks like:<br />
  * {@code <type>://<aliasedName>/<sessionId>/(<contentLength>)?<content>}.
  *
  * @since 1.0
  * @author Willi Schoenborn
  */
 @NotThreadSafe
 final class LegacyFrameDecoder extends ReplayingDecoder<Part> {
 
     private static final Logger LOG = LoggerFactory.getLogger(LegacyFrameDecoder.class);
     
     private CallType type;
     
     private String name;
     
     private String sessionId;
     
     private int length;
     
     private ByteBuffer content;
     
     public LegacyFrameDecoder() {
         super(Part.TYPE);
     }
 
     // Reducing cyclomatic complexity would dramatically reduce readability
     // Fall-throughs are the fastest way here
     /* CHECKSTYLE:OFF */
     @Override
     protected Object decode(ChannelHandlerContext context, Channel channel, 
         ChannelBuffer buffer, Part part) throws Exception {
         
         switch (part) {
             case TYPE: {
                 type = readType(buffer);
                 checkpoint(Part.COLON);
                 // intended fall-through
             }
             case COLON: {
                 final byte c = buffer.readByte();
                 checkState(c == ':', "Expected : but was %s", c);
                 checkpoint(Part.FIRST_SLASH);
                 // intended fall-through
             }
             case FIRST_SLASH: {
                 final byte c = buffer.readByte();
                 checkState(c == '/', "Expected first / but was %s", c);
                 checkpoint(Part.SECOND_SLASH);
                 // intended fall-through
             }
             case SECOND_SLASH: {
                 final byte c = buffer.readByte();
                 checkState(c == '/', "Expected second / but was %s", c);
                 checkpoint(Part.NAME);
                 // intended fall-through
             }
             case NAME: {
                 name = readName(buffer);
                 checkpoint(Part.THIRD_SLASH);
                 // intended fall-through
             }
             case THIRD_SLASH: {
                 final byte c = buffer.readByte();
                 checkState(c == '/', "Expected third / but was %s", c);
                 checkpoint(Part.SESSION_ID);
                 // intended fall-through
             }
             case SESSION_ID: {
                 sessionId = readSessionId(buffer);
                 checkpoint(Part.FOURTH_SLASH);
                 // intended fall-through
             }
             case FOURTH_SLASH: {
                 final byte c = buffer.readByte();
                 checkState(c == '/', "Expected fourth / but was %s", c);
                 checkpoint(Part.LEFT_PARENTHESIS);
                 // intended fall-through
             }
             case LEFT_PARENTHESIS: {
                 final byte c = buffer.readByte();
                 checkState(c == '(', "Expected ( but was %s", c);
                 checkpoint(Part.CONTENT_LENGTH);
                 // intended fall-through
             }
             case CONTENT_LENGTH: {
                 length = readLength(buffer);
                 checkpoint(Part.RIGHT_PARENTHESIS);
                 // intended fall-through
             }
             case RIGHT_PARENTHESIS: {
                 final byte c = buffer.readByte();
                 checkState(c == ')', "Expected ) but was %s", c);
                 checkpoint(Part.QUESTION_MARK);
                 // intended fall-through
             }
             case QUESTION_MARK: {
                 final byte c = buffer.readByte();
                 checkState(c == '?', "Expected ? but was %s", c);
                 checkpoint(Part.CONTENT);
                 // intended fall-through
             }
             case CONTENT: {
                 // FIXME
                 content = readContent(buffer);
                 checkpoint(Part.TYPE);
                 return InternalHeader.copyOf(this);
             }
             default: {
                 throw new AssertionError("Default case matched part " + part);
             }
         }
 
     }
     /* CHECKSTYLE:ON */
     
     private CallType readType(ChannelBuffer buffer) {
         final String value = readUntil(buffer, ':');
         LOG.trace("Read type '{}'", value);
         return CallType.valueOf(value.toUpperCase());
     }
     
     private String readName(ChannelBuffer buffer) {
         final String value = readUntil(buffer, '/');
         LOG.trace("Read name '{}'", value);
         return value;
     }
     
     private String readSessionId(ChannelBuffer buffer) {
         final String value = readUntil(buffer, '/');
         LOG.trace("Read sessionId '{}'", value);
         return value;
     }
     
     private int readLength(ChannelBuffer buffer) {
         final String value = readUntil(buffer, ')');
         LOG.trace("Read length {}", value);
         return Integer.parseInt(value);
     }
     
     private ByteBuffer readContent(ChannelBuffer buffer) {
         final ByteBuffer value = buffer.readSlice(length).toByteBuffer(0, length);
         LOG.trace("Read content {}", value);
         return value;
     }
     
     private String readUntil(ChannelBuffer buffer, char c) {
         int i = buffer.readerIndex();
         while (true) {
             if (c == buffer.getByte(i)) {
                 return readAndIncrement(buffer, i);
             }
             i++;
         }
     }
     
     private String readAndIncrement(ChannelBuffer buffer, int currentIndex) {
         final int size = currentIndex - buffer.readerIndex();
         final String value = buffer.toString(buffer.readerIndex(), size, Charsets.UTF_8);
         buffer.readerIndex(currentIndex);
         return value;
     }
 
     private void checkState(boolean state, String format, byte c) {
         if (state) {
             return;
         } else {
             throw new IllegalArgumentException(String.format(format, (char) c)); 
         }
     }
     
     /**
      * Internal implementation of the {@link Header} interface.
      *
      * @since 1.0 
      * @author Willi Schoenborn
      */
     private static final class InternalHeader implements Header {
         
         private final CallType type;
         
         private final String name;
         
         private final String sessionId;
         
         private final int length;
         
         private final ByteBuffer content;
         
         private InternalHeader(LegacyFrameDecoder decoder) {
             this.type = decoder.type;
             this.name = decoder.name;
             this.sessionId = decoder.sessionId;
             this.length = decoder.length;
             this.content = decoder.content;
         }
         
         @Override
         public CallType getCallType() {
             return type;
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
                 type, getAliasedName(), getSessionId(), getContentLength(), getContent()
             );
         }
         
         public static Header copyOf(LegacyFrameDecoder decoder) {
             return new InternalHeader(decoder);
         }
         
     }
     
 }
