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
 import com.google.inject.Injector;
 import com.google.inject.Module;
 
 import org.apache.commons.cli.ParseException;
 import org.waveprotocol.wave.examples.fedone.authentication.AccountStoreHolder;
 import org.waveprotocol.wave.examples.fedone.persistence.AccountStore;
 import org.waveprotocol.wave.examples.fedone.persistence.PersistenceModule;
 import org.waveprotocol.wave.examples.fedone.robots.RobotApiModule;
 import org.waveprotocol.wave.examples.fedone.robots.RobotRegistrationServlet;
 import org.waveprotocol.wave.examples.fedone.robots.active.ActiveApiServlet;
 import org.waveprotocol.wave.examples.fedone.robots.dataapi.DataApiServlet;
 import org.waveprotocol.wave.examples.fedone.robots.passive.RobotsGateway;
 import org.waveprotocol.wave.examples.fedone.rpc.AttachmentServlet;
 import org.waveprotocol.wave.examples.fedone.rpc.AuthenticationServlet;
 import org.waveprotocol.wave.examples.fedone.rpc.FetchServlet;
 import org.waveprotocol.wave.examples.fedone.rpc.ServerRpcProvider;
 import org.waveprotocol.wave.examples.fedone.rpc.WaveClientServlet;
 import org.waveprotocol.wave.examples.fedone.util.Log;
 import org.waveprotocol.wave.examples.fedone.waveserver.WaveBus;
 import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.ProtocolWaveClientRpc;
 import org.waveprotocol.wave.federation.xmpp.ComponentPacketTransport;
 import org.xmpp.component.ComponentException;
 
 import java.io.IOException;
 
 
 /**
 * Prototype server entrypoint.
 *
 *
  */
 public class ServerMain {
 
   private static final Log LOG = Log.get(ServerMain.class);
 
   public static void main(String... args) throws ParseException {
     Module flags = FlagBinder.parseFlags(args, FlagSettings.class);
 
     try {
       run(flags);
       return;
     } catch (IOException e) {
       LOG.severe("IOException when running server: " + e.getMessage());
     }
   }
 
   public static void run(Module flags) throws IOException {
     Injector flagInjector = Guice.createInjector(flags);
     PersistenceModule persistenceModule = flagInjector.getInstance(PersistenceModule.class);
    Injector injector = flagInjector.createChildInjector(new ServerModule(), new RobotApiModule(),
        persistenceModule);
     ComponentPacketTransport xmppComponent = injector.getInstance(ComponentPacketTransport.class);
     ServerRpcProvider server = injector.getInstance(ServerRpcProvider.class);
 
     AccountStoreHolder.init(injector.getInstance(AccountStore.class));
 
     server.addServlet("/attachment/*", injector.getInstance(AttachmentServlet.class));
     server.addServlet("/fetch/*", injector.getInstance(FetchServlet.class));
 
     server.addServlet("/auth", injector.getInstance(AuthenticationServlet.class));
 
     server.addServlet("/robot/rpc", injector.getInstance(ActiveApiServlet.class));
     server.addServlet("/robot/dataapi", injector.getInstance(DataApiServlet.class));
    server.addServlet("/robot/*", injector.getInstance(RobotRegistrationServlet.class));
 
     server.addServlet("/", injector.getInstance(WaveClientServlet.class));
 
     RobotsGateway robotsGateway = injector.getInstance(RobotsGateway.class);
     WaveBus waveBus = injector.getInstance(WaveBus.class);
     waveBus.subscribe(robotsGateway);
 
     ProtocolWaveClientRpc.Interface rpcImpl =
         injector.getInstance(ProtocolWaveClientRpc.Interface.class);
     server.registerService(ProtocolWaveClientRpc.newReflectiveService(rpcImpl));
     try {
       xmppComponent.run();
     } catch (ComponentException e) {
       System.err.println("couldn't connect to XMPP server:" + e);
     }
     LOG.info("Starting server");
     server.startRpcServer();
     server.startWebSocketServer();
   }
 }
