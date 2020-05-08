 /**
  * Copyright 2009 Google Inc.
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
  *
  */
 
 package org.waveprotocol.wave.examples.fedone;
 
 import com.google.inject.Guice;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 import com.google.inject.name.Named;
 
 import org.apache.commons.cli.ParseException;
 import org.waveprotocol.wave.examples.fedone.federation.xmpp.WaveXmppComponent;
 import org.waveprotocol.wave.examples.fedone.rpc.ServerRpcProvider;
 import org.waveprotocol.wave.examples.fedone.util.Log;
 import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.ProtocolWaveClientRpc;
 import org.xmpp.component.ComponentException;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 
 
 /**
  * Prototype server entrypoint.
  *
  *
  */
 public class ServerMain {
 
   private static final Log LOG = Log.get(ServerMain.class);
 
   public static void main(String...args) throws ParseException {
     Module flags = FlagBinder.parseFlags(args, FlagSettings.class);
 
     try {
       run(flags);
       return;
     } catch (IOException e) {
      LOG.severe("IOException when running server: " + e.getMessage());
     }
   }
 
   public static void run(Module flags)
       throws IOException {
 
     Injector injector = Guice.createInjector(new ServerModule(), flags);
     WaveXmppComponent wxComponent = injector.getInstance(WaveXmppComponent.class);
     ServerRpcProvider server = new ServerRpcProvider(injector.getInstance(
         RpcInetSocketAddressFactory.class).create());
     ProtocolWaveClientRpc.Interface rpcImpl = injector.getInstance(
         ProtocolWaveClientRpc.Interface.class);
     server.registerService(ProtocolWaveClientRpc.newReflectiveService(rpcImpl));
     try {
       wxComponent.run();
     } catch (ComponentException e) {
       System.err.println("couldn't connect to XMPP server:" + e);
     }
     LOG.info("Starting server");
     server.startServer();
   }
 
   /** Creates InetSocketAddresses from injected parameters */
   static class RpcInetSocketAddressFactory {
 
     private final String host;
     private final Integer port;
 
     @Inject
     RpcInetSocketAddressFactory(@Named("client_frontend_hostname") String host,
         @Named("client_frontend_port") Integer port) {
       this.host = host;
       this.port = port;
       LOG.info("Starting client frontend on host: " + host + " port: " + port);
     }
 
     InetSocketAddress create() {
       return new InetSocketAddress(host, port);
     }
 
   }
 }
