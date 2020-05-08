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
 
 import com.google.common.base.Charsets;
 import com.google.common.base.Preconditions;
 
 import de.cosmocode.palava.bridge.Header;
 import de.cosmocode.palava.bridge.call.CallType;
 
 /**
  * Decodes framed chunks of the legacy palava protocol which looks like:<br />
  * {@code <type>://<aliasedName>/<sessionId>/(<contentLength>)?<content>}.
  *
  * @since 
  * @author Willi Schoenborn
  */
 @NotThreadSafe
 final class LegacyFrameDecoder extends FrameDecoder {
 
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
      * be set to the first char of the corresponding type.
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
     protected Header decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
     /* CHECKSTYLE:ON */
         if (buffer.readable()) {
            
             for (int i = buffer.readerIndex(); i < buffer.writerIndex(); i++) {
                 final byte c = buffer.getByte(i);
                 if (part == null) part = Part.TYPE;
                 switch (part) {
                     case TYPE: {
                         if (c == ':') {
                             type = readAndIncrement(buffer, i);
                             part = Part.COLON;
                         }
                         break;
                     }
                     case COLON: {
                         Preconditions.checkState(c == '/', ": must be followed by /");
                         buffer.skipBytes(1);
                         part = Part.FIRST_SLASH;
                         break;
                     }
                     case FIRST_SLASH: {
                         Preconditions.checkState(c == '/', "/ must be followed by /");
                         buffer.skipBytes(1);
                         part = Part.SECOND_SLASH;
                         break;
                     }
                     case SECOND_SLASH: {
                         buffer.skipBytes(1);
                         break;
                     }
                     case NAME: {
                         if (c == '/') {
                             name = readAndIncrement(buffer, i);
                             part = Part.THIRD_SLASH;
                         }
                         break;
                     }
                     case THIRD_SLASH: {
                         buffer.skipBytes(1);
                         break;
                     }
                     case SESSION_ID: {
                         if (c == '/') {
                             sessionId = readAndIncrement(buffer, i);
                             part = Part.FOURTH_SLASH;
                         }
                         break;
                     }
                     case FOURTH_SLASH: {
                         Preconditions.checkState(c == '(', "/ must be followed by (");
                         buffer.skipBytes(1);
                         break;
                     }
                     case LEFT_PARENTHESIS: {
                         buffer.skipBytes(1);
                         break;
                     }
                     case CONTENT_LENGTH: {
                         if (c == ')') {
                             length = Integer.parseInt(readAndIncrement(buffer, i));
                             part = Part.RIGHT_PARENTHESIS;
                         }
                         break;
                     }
                     case RIGHT_PARENTHESIS: {
                         Preconditions.checkState(c == ')', ") must be followed by ?");
                         buffer.skipBytes(i);
                         break;
                     }
                     case QUESTION_MARK: {
                         buffer.skipBytes(1);
                         break;
                     }
                     case CONTENT: {
                         content = buffer.toByteBuffer(i, length);
                         return new InternalHeader();
                     }
                     default: {
                         throw new AssertionError("Default case matched part " + part);
                     }
                 }
             }
            
            return null;
        } else {
            return null;
         }
     }
     
     private String readAndIncrement(ChannelBuffer buffer, int currentIndex) {
         final String s = buffer.toString(buffer.readerIndex(), currentIndex - buffer.readerIndex(), Charsets.UTF_8);
         buffer.readerIndex(currentIndex);
         return s;
     }
 
     /**
      * Internal implementation of the {@link Header} interface.
      *
      * @since 
      * @author Willi Schoenborn
      */
     private final class InternalHeader implements Header {
         
         private final CallType callType = CallType.valueOf(type);
         
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
         
     }
     
 }
