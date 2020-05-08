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
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.Map;
 
 import org.codehaus.jackson.map.ObjectMapper;
 
 import com.google.common.base.Charsets;
 import com.google.common.base.Preconditions;
 
 import de.cosmocode.palava.bridge.call.CallType;
 import de.cosmocode.palava.ipc.netty.Client;
 import de.cosmocode.palava.ipc.netty.ClientConnection;
 import de.cosmocode.palava.ipc.netty.NettyClient;
 
 /**
  * Netty based implementation of the {@link LegacyClient} interface.
  *
  * @since 1.0
  * @author Willi Schoenborn
  */
 public final class LegacyNettyClient implements LegacyClient {
 
     private final Client client = new NettyClient();
     
     @Override
     public LegacyClientConnection connect(String host, int port) {
         return connect(new InetSocketAddress(host, port));
     }
     
     @Override
     public LegacyClientConnection connect(InetSocketAddress address) {
         return new InternalConnection(client.connect(address));
     }
     
     /**
      * Internal implementation of the {@link LegacyClientConnection} interface.
      *
      * @since 1.0
      * @author Willi Schoenborn
      */
     private final class InternalConnection implements LegacyClientConnection {
 
         private final ObjectMapper mapper = new ObjectMapper();
         
         private final ClientConnection connection;
         
         public InternalConnection(ClientConnection connection) {
             this.connection = Preconditions.checkNotNull(connection, "Connection");
         }
         
         @Override
         public <T> T send(CallType type, String name, Map<String, Object> content) {
            return this.<T>send(type, name, "", content);
         }
         
         @Override
         public <T> T send(CallType type, String name, String sessionId, Map<String, Object> content) {
             final String encodedContent;
             
             switch (type) {
                 case DATA: {
                     try {
                         encodedContent = mapper.writeValueAsString(content);
                     } catch (IOException e) {
                         throw new IllegalArgumentException(e);
                     }
                     break;
                 }
                 case JSON: {
                     try {
                         encodedContent = mapper.writeValueAsString(content);
                     } catch (IOException e) {
                         throw new IllegalArgumentException(e);
                     }
                     break;
                 }
                 default: {
                     throw new AssertionError("Default case matched " + type);
                 }
             }
             
             final String request = String.format("%s://%s/%s/(%s)?%s",
                 type.name().toLowerCase(),
                 name,
                 sessionId,
                 encodedContent.getBytes(Charsets.UTF_8).length,
                 encodedContent
             );
             final String response = send(request);
             @SuppressWarnings("unchecked")
             final T decoded = (T) decode(response);
             return decoded;
         }
         
         private Object decode(String response) {
             return response;
         }
         
         @Override
         public String send(String request) {
             return connection.send(request);
         }
         
         @Override
         public void disconnect() {
             connection.disconnect();
         }
         
     }
     
     @Override
     public void shutdown() {
         client.shutdown();
     }
     
 }
