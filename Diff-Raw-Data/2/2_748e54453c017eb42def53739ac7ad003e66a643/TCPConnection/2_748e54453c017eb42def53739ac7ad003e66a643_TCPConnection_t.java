 package org.gethydrated.hydra.core.transport;
 
 import com.fasterxml.jackson.core.JsonGenerator.Feature;
 import com.fasterxml.jackson.core.JsonParser;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.SerializationFeature;
 import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
 import org.gethydrated.hydra.actors.ActorRef;
 import org.gethydrated.hydra.core.sid.IdMatcher;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.Map;
 import java.util.UUID;
 import java.util.concurrent.ExecutorService;
 
 /**
  *
  */
 public class TCPConnection implements Connection {
 
     private UUID nodeid;
 
     private final Socket socket;
 
     private final ObjectMapper objectMapper;
 
     private final IdMatcher idMatcher;
 
     private boolean connected = false;
 
     private boolean closed = false;
 
     private boolean hidden = false;
 
     private NodeAddress nodeAddress;
 
     private SocketRunner socketRunner;
 
     public TCPConnection(Socket socket, IdMatcher idMatcher) {
         this.socket = socket;
         this.idMatcher = idMatcher;
         objectMapper = new ObjectMapper();
         objectMapper.configure(Feature.AUTO_CLOSE_TARGET, false);
         objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
         objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
         objectMapper.registerModule(new EnvelopeModule());
         objectMapper.registerModule(new JaxbAnnotationModule());
     }
 
     @Override
     public synchronized Map<UUID, NodeAddress> connect(NodeAddress connectorAddress) throws IOException {
         if(connected) {
             throw new IllegalStateException("Connection handshake already done.");
         }
         Envelope connect = new Envelope(MessageType.CONNECT);
         connect.setCookie("nocookie");
         connect.setHiddenNode(hidden);
         connect.setSender(idMatcher.getLocal());
         connect.setConnector(connectorAddress);
         objectMapper.writeValue(socket.getOutputStream(), connect);
         Envelope handShake = objectMapper.readValue(socket.getInputStream(), Envelope.class);
         if(handShake.getType() == MessageType.DECLINE) {
             throw new IllegalArgumentException(handShake.getReason());
         }
         nodeid = handShake.getSender();
         connected = true;
         return handShake.getNodes();
     }
 
     @Override
     public synchronized boolean handshake(Map<UUID, NodeAddress> nodes) throws IOException {
         if(connected) {
             throw new IllegalStateException("Connection handshake already done.");
         }
         Envelope connect = objectMapper.readValue(socket.getInputStream(), Envelope.class);
         nodeid = connect.getSender();
        nodeAddress = new NodeAddress(socket.getInetAddress().toString(), connect.getConnector().getPort());
         hidden = connect.isHiddenNode();
         Envelope response;
         if (idMatcher.contains(connect.getSender())) {
             response = new Envelope(MessageType.DECLINE);
             response.setReason("Node UUID already in use. Please check your address input or restart hydra.");
             response.setSender(idMatcher.getLocal());
             response.setTarget(connect.getSender());
             objectMapper.writeValue(socket.getOutputStream(), response);
             socket.close();
             return false;
         }
         response = new Envelope(MessageType.ACCEPT);
         response.setSender(idMatcher.getLocal());
         response.setTarget(connect.getSender());
         response.setNodes(nodes);
         objectMapper.writeValue(socket.getOutputStream(),response);
         connected = true;
         return true;
     }
 
     @Override
     public void disconnect() throws IOException {
         if(!connected) {
             throw new IllegalStateException("Connection not connected.");
         }
         if(!isClosed()) {
             Envelope disconnect = new Envelope(MessageType.DISCONNECT);
             disconnect.setSender(idMatcher.getLocal());
             disconnect.setTarget(nodeid);
             objectMapper.writeValue(socket.getOutputStream(), disconnect);
             idMatcher.remove(nodeid);
             socket.shutdownOutput();
             socket.shutdownInput();
             socket.close();
         }
     }
 
     @Override
     public void sendEnvelope(Envelope envelope) throws IOException {
         objectMapper.writeValue(socket.getOutputStream(), envelope);
     }
 
     @Override
     public void setReceiveCallback(final ActorRef target, ExecutorService executorService) {
         if(socketRunner != null) {
             socketRunner.stop();
         }
         socketRunner = new SocketRunner(target);
         executorService.execute(socketRunner);
     }
 
     @Override
     public UUID getUUID() {
         return nodeid;
     }
 
     @Override
     public InetAddress getIp() {
         return socket.getInetAddress();
     }
 
     @Override
     public int getPort() {
         return socket.getPort();
     }
 
     @Override
     public ObjectMapper getMapper() {
         return objectMapper;
     }
 
     @Override
     public void setConnector(NodeAddress addr) {
         this.nodeAddress = addr;
     }
 
     @Override
     public boolean isConnected() {
         return connected;
     }
 
     @Override
     public boolean isClosed() throws IOException {
         return closed ||socket.isClosed() ;
     }
 
     @Override
     public boolean isHidden() {
         return hidden;
     }
 
     @Override
     public NodeAddress getConnector() {
         return nodeAddress;
     }
 
     public class SocketRunner implements Runnable {
 
         private volatile boolean stopped = false;
 
         private Thread thread;
 
         private ActorRef callback;
 
         public SocketRunner(ActorRef callback) {
             this.callback = callback;
         }
 
         @Override
         public void run() {
             thread = Thread.currentThread();
             try {
                 JsonParser parser = objectMapper.getFactory().createJsonParser(socket.getInputStream());
                 while (!stopped) {
                     if(isClosed()) {
                         if(!stopped) {
                             stopped = true;
                             callback.tell("disconnected", null);
                         }
                     } else {
                         Envelope env = parser.readValueAs(Envelope.class);
                         //Envelope env = objectMapper.readValue(socket.getInputStream(), Envelope.class);
                         if(env.getType() == MessageType.DISCONNECT) {
                             callback.tell("disconnected", null);
                             closed = true;
                         } else {
                             callback.tell(env, null);
                         }
                     }
                 }
             } catch (IOException  e) {
                 stopped = true;
                 callback.tell(e, null);
             }
         }
 
         public void stop() {
             stopped = true;
             if(thread != null) {
                 thread.interrupt();
             }
         }
     }
 }
