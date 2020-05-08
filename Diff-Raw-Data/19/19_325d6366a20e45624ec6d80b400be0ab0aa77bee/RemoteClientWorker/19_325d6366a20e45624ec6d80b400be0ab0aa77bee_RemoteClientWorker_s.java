 /*
  * Copyright (C) 2012  Pauli Kauppinen
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see <http://www.gnu.org/licenses/>.
  */
 package org.javnce.vnc.server;
 
 import java.io.IOException;
 import java.nio.channels.SocketChannel;
 import org.javnce.eventing.Event;
 import org.javnce.eventing.EventLoop;
 import org.javnce.eventing.EventSubscriber;
 import org.javnce.vnc.common.SocketClosedEvent;
 
 /**
  * The Class RemoteClientWorker is a thread that takes care of communicating
  * with client.
  */
 class RemoteClientWorker extends Thread implements EventSubscriber {
 
     /**
      * The event loop.
      */
     final private EventLoop eventLoop;
     /**
      * The connected client.
      */
     final private RemoteClient client;
     /**
      * The protocol handler.
      */
     final private ProtocolHandler handler;
     /**
      * The frame buffer handler.
      */
     private ClientFramebufferHandler frameHandler;
 
     /**
      * Instantiates a new remote client worker.
      *
      * @param client the client
      */
     RemoteClientWorker(RemoteClient client) {
         this.client = client;
         eventLoop = new EventLoop();
         eventLoop.moveToNewChildGroup();
 
         eventLoop.subscribe(SocketClosedEvent.eventId(), this);
         handler = new ProtocolHandler(eventLoop);
         setName("Javnce-RemoteClientWorker");
     }
 
     /* (non-Javadoc)
      * @see java.lang.Thread#run()
      */
     @Override
     public void run() {
         SocketChannel channel = client.giveChannel();
         if (RemoteClient.State.Connected == client.state() && null != channel) {
             //Create framebuffer handler
             frameHandler = new ClientFramebufferHandler(eventLoop);
             frameHandler.init();
             frameHandler.start();
             handler.init(channel);
             eventLoop.process();
         }
         if (null != channel) {
             try {
                 channel.close();
             } catch (IOException ex) {
                 EventLoop.fatalError(this, ex);
             }
         }
         shutdown();
     }
 
     /* (non-Javadoc)
      * @see org.javnce.eventing.EventSubscriber#event(org.javnce.eventing.Event)
      */
     @Override
     public void event(Event event) {
         if (SocketClosedEvent.eventId().equals(event.Id())) {
            eventLoop.shutdown();
         }
     }
 
     /**
      * Shutdown.
      */
     public void shutdown() {
         // Shutdown ClientFramebufferHandler and RemoteClientWorker 
         eventLoop.shutdownGroup();
         client.disconnect();
     }
 }
