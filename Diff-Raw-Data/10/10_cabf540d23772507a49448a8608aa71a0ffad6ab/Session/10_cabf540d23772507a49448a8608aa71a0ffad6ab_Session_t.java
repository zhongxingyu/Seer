 /*
  * Copyright 2010 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fast.bats.europe.session;
 
 import java.nio.ByteBuffer;
 
 import silvertip.Connection;
 import fast.Encoder;
 import fast.Message;
 import fast.soup.Elements;
 import fast.soup.templates.LoginRequest;
 import fast.templates.MessageTemplate;
 
 public class Session {
  private static final String SESSION = "0006";
  private static final String SEQUENCE_NUMBER = "";

   private final Encoder encoder;

  public Session(Encoder encoder) {
     this.encoder = encoder;
   }
 
   public void login(Connection<?> connection, String username, String password) {
     Message message = new Message();
     message.set(Elements.USERNAME, username);
     message.set(Elements.PASSWORD, password);
     message.set(Elements.SESSION, SESSION);
     message.set(Elements.SEQUENCE_NUMBER, SEQUENCE_NUMBER);
     send(connection, LoginRequest.TEMPLATE, message);
   }
 
   private void send(Connection<?> connection, MessageTemplate template, Message message) {
     ByteBuffer buffer = encoder.encode(message, template);
     connection.send(new silvertip.Message(bytes(buffer)));
   }
 
   private byte[] bytes(ByteBuffer buffer) {
     byte[] bytes = new byte[buffer.position()];
     for (int i = 0; i < buffer.position(); i++) {
       bytes[i] = buffer.get(i);
     }
     buffer.position(0);
     return bytes;
   }
 }
