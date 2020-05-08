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
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import org.javnce.vnc.server.platform.FramebufferHandler;
 import org.javnce.vnc.server.platform.InputEventHandler;
 
 /**
  * The Class VncServerController controls the platform and client threads.
  *
  * The VncServerController is thread safe.
  */
 public class VncServerController implements RemoteClientObserver {
 
     /**
      * The full access mode.
      */
     final private boolean fullAccessMode;
     /**
      * The socket listener.
      */
     private Listener listener;
     /**
      * The input event handler.
      */
     private InputEventHandler inputEventHandler;
     /**
      * The frame buffer handler.
      */
     private FramebufferHandler framebufferHandler;
     /**
      * The observers.
      */
     final private RemoteClientObserverCollection observers;
     /**
      * The clients.
      */
     final private Set<RemoteClient> clients;
     /**
      * The lock.
      */
     final private Object lock;
 
     /**
      * Instantiates a new VNC server controller.
      *
      * @param fullAccessMode the full access mode
      */
     public VncServerController(boolean fullAccessMode) {
         this.fullAccessMode = fullAccessMode;
         observers = new RemoteClientObserverCollection();
         clients = new HashSet<>();
         lock = new Object();
     }
 
     /**
      * Launch the listener.
      */
     public void launch() {
         synchronized (lock) {
             if (null == listener) {
                 listener = new Listener();
                 listener.addObserver(this);
                 listener.start();
             }
         }
     }
 
     @Override
     public void portChanged(int listenerPort) {
         observers.portChanged(listenerPort);
     }
 
     private void addClient(RemoteClient client) {
         if (RemoteClient.State.PendingConnection == client.state()) {
             client.addObserver(this);
             synchronized (lock) {
                 clients.add(client);
             }
             startHandlers();
         }
     }
 
     private void removeClient(RemoteClient client) {
         boolean empty = false;
         if (RemoteClient.State.Disconnected == client.state()) {
             synchronized (lock) {
                 clients.remove(client);
                 empty = clients.isEmpty();
             }
         }
         if (empty) {
             stopHandlers();
         }
     }
 
     @Override
     public void vncClientChanged(RemoteClient client) {
         addClient(client);
         observers.vncClientChanged(client);
         removeClient(client);
     }
 
     /**
      * Adds the observer.
      *
      * @param observer the observer
      */
     public void addObserver(RemoteClientObserver observer) {
         observers.add(observer);
     }
 
     /**
      * Removes the observer.
      *
      * @param observer the observer
      */
     public void removeObserver(RemoteClientObserver observer) {
         observers.remove(observer);
     }
 
     /**
      * Shutdown.
      */
     public void shutdown() {
         ArrayList<RemoteClient> list;
         synchronized (lock) {
             if (null != listener) {
                 listener.shutdown();
                 listener = null;
             }
            if (null != inputEventHandler) {
                inputEventHandler.shutdown();
                inputEventHandler = null;
            }
            if (null != framebufferHandler) {
                framebufferHandler.shutdown();
                framebufferHandler = null;
            }
             list = new ArrayList<>(clients);
         }
         for (Iterator<RemoteClient> i = list.iterator(); i.hasNext();) {
             i.next().disconnect();
         }
 
         stopHandlers();
     }
 
     private void startHandlers() {
         synchronized (lock) {
             if (null == inputEventHandler) {
                 inputEventHandler = new InputEventHandler(fullAccessMode);
                 inputEventHandler.launch();
             }
             if (null == framebufferHandler) {
                 framebufferHandler = new FramebufferHandler();
                 framebufferHandler.launch();
             }
 
         }
     }
 
     private void stopHandlers() {
         synchronized (lock) {
             if (null != inputEventHandler) {
                 inputEventHandler.shutdown();
                 inputEventHandler = null;
             }
             if (null != framebufferHandler) {
                 framebufferHandler.shutdown();
                 framebufferHandler = null;
             }
         }
     }
 }
