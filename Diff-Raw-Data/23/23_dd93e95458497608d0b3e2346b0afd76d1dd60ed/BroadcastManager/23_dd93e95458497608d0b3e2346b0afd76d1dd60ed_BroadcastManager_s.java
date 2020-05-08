 /*
  * Copyright (c) 2008 Kasper Nielsen.
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
 package dk.dma.navnet.server.broadcast;
 
 import static java.util.Objects.requireNonNull;
 import jsr166e.CompletableFuture;
 import jsr166e.CompletableFuture.Action;

import org.picocontainer.Startable;

 import dk.dma.enav.maritimecloud.broadcast.BroadcastMessage;
 import dk.dma.enav.model.geometry.PositionTime;
 import dk.dma.enav.util.function.Consumer;
 import dk.dma.navnet.messages.c2c.broadcast.BroadcastAck;
 import dk.dma.navnet.messages.c2c.broadcast.BroadcastDeliver;
 import dk.dma.navnet.messages.c2c.broadcast.BroadcastSend;
 import dk.dma.navnet.messages.c2c.broadcast.BroadcastSendAck;
 import dk.dma.navnet.server.connection.ServerConnection;
 import dk.dma.navnet.server.requests.RequestException;
 import dk.dma.navnet.server.requests.RequestProcessor;
 import dk.dma.navnet.server.requests.ServerMessageBus;
 import dk.dma.navnet.server.targets.Target;
 import dk.dma.navnet.server.targets.TargetManager;
 
 /**
  * 
  * @author Kasper Nielsen
  */
 public class BroadcastManager implements Startable {
     private final TargetManager tm;
 
     private final ServerMessageBus bus;
 
     public BroadcastManager(TargetManager tm, ServerMessageBus bus) {
         this.tm = requireNonNull(tm);
         this.bus = requireNonNull(bus);
     }
 
     BroadcastSendAck broadcast(final ServerConnection source, final BroadcastSend send) throws RequestException {
         final BroadcastMessage bm;
        try {
            bm = send.tryRead();
        } catch (Exception e) {
            throw new RequestException(e);
        }
 
         final Target target = source.getTarget();
         final PositionTime sourcePositionTime = send.getPositionTime();
 
         tm.forEachTarget(new Consumer<Target>() {
             @Override
             public void accept(Target t) {
                 if (t != target && t.isConnected()) { // do not broadcast to self
                     PositionTime latest = t.getLatestPosition();
                     if (latest != null) {
                         double distance = sourcePositionTime.geodesicDistanceTo(latest);
                         if (distance < send.getDistance()) {
                            BroadcastDeliver bd = BroadcastDeliver.create(send.getId(), send.getPositionTime(), bm);
                             final ServerConnection connection = t.getConnection();
                             CompletableFuture<Void> f = connection.messageSend(bd).protocolAcked();
                             if (send.isReceiverAck()) {
                                 f.thenAccept(new Action<Void>() {
                                     public void accept(Void paramA) {
                                         Target t = connection.getTarget();
                                         BroadcastAck ba = new BroadcastAck(send.getReplyTo(), t.getId(), t
                                                 .getLatestPosition());
                                         source.messageSend(ba);
                                     }
                                 });
                             }
                         }
                     }
                 }
             }
         });
 
         return send.createReply();
     }
 
     void broadCastTo(BroadcastSend bs, Target target, BroadcastDeliver bd) {
 
     }
 

     /** {@inheritDoc} */
     @Override
     public void start() {
         bus.subscribe(BroadcastSend.class, new RequestProcessor<BroadcastSend, BroadcastSendAck>() {
             public BroadcastSendAck process(ServerConnection connection, BroadcastSend message) throws RequestException {
                 return broadcast(connection, message);
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void stop() {}
 }
