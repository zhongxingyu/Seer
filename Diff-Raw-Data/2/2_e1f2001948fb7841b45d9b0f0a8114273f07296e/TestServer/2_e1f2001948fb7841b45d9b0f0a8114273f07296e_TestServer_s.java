 /*
  * Copyright 2013 the original author or authors.
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
 package wine;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.SocketChannel;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import silvertip.Connection;
 import silvertip.Events;
 import silvertip.Server;
 
 public class TestServer {
 
     public static void main(String[] args) throws IOException {
         if (args.length != 1)
             usage();
 
         int port = port(args);
         if (port == 0)
             usage();
 
         Map<ByteString, ByteString> config = new HashMap<ByteString, ByteString>();
 
         final Callback callback = new Callback(config);
 
         final Parser parser = new Parser();
         final Server server = Server.accept(port, new Server.ConnectionFactory<Message>() {
             @Override
             public Connection<Message> newConnection(SocketChannel channel) {
                 return new Connection(channel, parser, callback);
             }
         });
 
         info(String.format("Listening on port %s", port));
 
         Events io = Events.open();
 
         io.register(server);
 
         while (true)
             io.process(1000);
     }
 
     private static class Callback implements Connection.Callback<Message> {
         private Map<ByteString, ByteString> config;
 
         public Callback(Map<ByteString, ByteString> config) {
             this.config = config;
         }
 
         @Override
         public void connected(Connection<Message> connection) {
         }
 
         @Override
         public void messages(Connection<Message> connection, Iterator<Message> messages) {
             while (messages.hasNext())
                 handle(connection, messages.next());
         }
 
         @Override
         public void closed(Connection<Message> connection) {
         }
 
         @Override
         public void garbledMessage(Connection<Message> connection, String message, byte[] data) {
         }
 
         @Override
         public void sent(ByteBuffer buffer) {
         }
 
         private void handle(final Connection<Message> connection, Message message) {
             message.accept(new MessageVisitor() {
                 @Override
                 public void visit(Login message) {
                     connection.send(new LoginAccepted().format());
                 }
 
                 @Override
                 public void visit(LoginAccepted message) {
                 }
 
                 @Override
                 public void visit(LoginRejected message) {
                 }
 
                 @Override
                 public void visit(Get message) {
                     ByteString key   = new ByteString(message.key());
                     ByteString value = config.get(key);
 
                     if (value == null)
                         value = new ByteString();
 
                     connection.send(new Value(key.toArray(), value.toArray()).format());
                 }
 
                 @Override
                 public void visit(Value message) {
                 }
 
                 @Override
                 public void visit(Set message) {
                     ByteString key   = new ByteString(message.key());
                     ByteString value = new ByteString(message.value());
 
                     config.put(key, value);
                 }
             });
         }
     }
 
     private static class ByteString {
         private final byte[] value;
 
         public ByteString() {
             this(new byte[] {});
         }
 
         public ByteString(byte[] value) {
             this.value = value;
         }
 
         @Override
         public boolean equals(Object that) {
             if (that == null)
                 return false;
 
             if (that == this)
                 return true;
 
             if (that.getClass() != this.getClass())
                 return false;
 
             byte[] thatValue = ((ByteString) that).value;
 
             return Arrays.equals(thatValue, this.value);
         }
 
         @Override
         public int hashCode() {
             return Arrays.hashCode(value);
         }
 
         public byte[] toArray() {
             return value;
         }
     }
 
     private static int port(String[] args) {
         try {
             return Integer.parseInt(args[0]);
         } catch (NumberFormatException e) {
             return 0;
         }
     }
 
     private static void info(String message) {
        System.out.println(String.format("wine-server: info: %s", message));
     }
 
     private static void usage() {
         System.err.println("Usage: wine-test-server <port>");
         System.exit(2);
     }
 
 }
