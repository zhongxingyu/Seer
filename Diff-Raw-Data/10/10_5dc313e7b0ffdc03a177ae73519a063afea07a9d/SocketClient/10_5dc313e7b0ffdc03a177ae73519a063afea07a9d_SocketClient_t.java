 package com.github.noxan.aves.client;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import com.github.noxan.aves.net.Connection;
 import com.github.noxan.aves.protocol.InputProtocol;
 import com.github.noxan.aves.protocol.OutputProtocol;
 import com.github.noxan.aves.protocol.ProtocolFactory;
 import com.github.noxan.aves.protocol.string.StringProtocolFactory;
 import com.github.noxan.aves.util.Tuple;
 
 public class SocketClient implements Client, Connection {
     private String host;
     private int port;
     private Socket socket;
     private ClientHandler handler;
 
     private ProtocolFactory factory;
     private InputProtocol in;
     private OutputProtocol out;
 
     private InputManager inputManager;
     private Thread inputThread;
 
     private Thread managerThread;
 
     private BlockingQueue<Tuple<ClientEvent, Object>> clientEvents;
 
     public SocketClient(ClientHandler handler) {
         this("localhost", 1666, handler, new StringProtocolFactory());
     }
 
     public SocketClient(String host, int port, ClientHandler handler, ProtocolFactory factory) {
         this.host = host;
         this.port = port;
         this.handler = handler;
         this.factory = factory;
         this.clientEvents = new LinkedBlockingQueue<Tuple<ClientEvent, Object>>();
     }
 
     @Override
     public void write(Object data) throws IOException {
         out.write(data);
     }
 
     @Override
     public String getHost() {
         return host;
     }
 
     @Override
     public int getPort() {
         return port;
     }
 
     @Override
     public void connect() throws IOException {
         handler.clientConnect();
         socket = new Socket();
         socket.connect(new InetSocketAddress(host, port));
         in = factory.createInputProtocol(socket.getInputStream());
         out = factory.createOutputProtocol(socket.getOutputStream());
         inputManager = new InputManager();
         inputThread = new Thread(inputManager);
         inputThread.start();
         managerThread = new Thread(new EventManager());
         managerThread.start();
     }
 
     @Override
     public void disconnect() {
         handler.clientDisconnect();
     }
 
     private void offerEvent(ClientEvent event, Object data) {
         clientEvents.offer(new Tuple<ClientEvent, Object>(event, data));
     }
 
     private class EventManager implements Runnable {
         @Override
         public void run() {
             while(true) {
                 Tuple<ClientEvent, Object> event = clientEvents.poll();
                if(event != null) {
                    switch(event.getFirst()) {
                        case DATA_READ:
                            handler.readData(event.getSecond());
                            break;
                    }
                 }
             }
         }
     }
 
     private class InputManager implements Runnable {
         @Override
         public void run() {
             while(true) {
                 try {
                     Object data = in.read();
                     offerEvent(ClientEvent.DATA_READ, data);
                 } catch(IOException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 }
