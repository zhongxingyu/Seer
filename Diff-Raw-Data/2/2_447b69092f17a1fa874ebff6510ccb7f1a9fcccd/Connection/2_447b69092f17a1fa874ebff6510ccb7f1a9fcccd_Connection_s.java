 package com.github.veithen.dfpagent.protocol.connection;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.github.veithen.dfpagent.Constants;
 import com.github.veithen.dfpagent.protocol.DFPConstants;
 import com.github.veithen.dfpagent.protocol.message.Message;
 import com.github.veithen.dfpagent.protocol.message.MessageType;
 import com.github.veithen.dfpagent.protocol.tlv.TLV;
 import com.github.veithen.dfpagent.protocol.tlv.Type;
 import com.github.veithen.dfpagent.resources.Messages;
 import com.ibm.ejs.ras.Tr;
 import com.ibm.ejs.ras.TraceComponent;
 
 /**
  * Manages a DFP connection. This class may be used both for outgoing and incoming connections. Its
  * primary responsibility is to deserialize incoming messages and to serialize outgoing messages. To
  * use this class:
  * <ol>
  * <li>Create the underlying {@link Socket}.
  * <li>Create a {@link Handler} instance that processes incoming messages.
  * <li>Create an instance of this class.
  * <li>Execute the {@link Connection#run()} method, potentially in a separate thread.
  * </ol>
  * To close the connection, call {@link Connection#stop()}. This will cause the
  * {@link Connection#run()} method to exit.
  * <p>
  * This class is thread safe.
  */
 public final class Connection implements Runnable {
     private static final TraceComponent TC = Tr.register(Connection.class, Constants.TRACE_GROUP, Messages.class.getName());
     
     private static final Map<Integer,MessageType> messageTypeByCode = new HashMap<Integer,MessageType>();
     private static final Map<Integer,Type> typeByCode = new HashMap<Integer,Type>();
     
     static {
         for (MessageType messageType : MessageType.values()) {
             messageTypeByCode.put(messageType.getCode(), messageType);
         }
         for (Type type : Type.values()) {
             typeByCode.put(type.getCode(), type);
         }
     }
     
     private final Socket socket;
     private final Handler handler;
     private final DataInputStream in;
     private final DataOutputStream out;
 
     public Connection(Socket socket, Handler handler) throws IOException {
         this.socket = socket;
         in = new DataInputStream(socket.getInputStream());
         out = new DataOutputStream(socket.getOutputStream());
         this.handler = handler;
     }
 
     public void run() {
         try {
             ml: while (true) {
                 int version = in.readUnsignedShort();
                 if (version != DFPConstants.VERSION) {
                     Tr.error(TC, Messages._0005E, new Object[] { DFPConstants.VERSION, version });
                     break;
                 }
                 int messageTypeCode = in.readUnsignedShort();
                 int messageLength = in.readInt();
                 int remaining = messageLength - 8;
                 List<TLV> tlvs = new ArrayList<TLV>();
                 while (remaining != 0) {
                     if (remaining < 4) {
                         Tr.error(TC, Messages._0006E);
                         break ml;
                     }
                     int typeCode = in.readUnsignedShort();
                     int length = in.readUnsignedShort();
                     if (remaining < length) {
                         Tr.error(TC, Messages._0006E);
                         break ml;
                     }
                     byte[] value = new byte[length-4];
                     in.readFully(value);
                     Type type = typeByCode.get(typeCode);
                     if (type == null) {
                         Tr.warning(TC, Messages._0010W, typeCode);
                     } else {
                         tlvs.add(new TLV(type, value));
                     }
                     remaining -= length;
                 }
                MessageType messageType = messageTypeByCode.get(messageTypeByCode);
                 if (messageType == null) {
                     Tr.warning(TC, Messages._0009W, messageTypeCode);
                 } else {
                     handler.processMessage(new Message(messageType, tlvs));
                 }
             }
             socket.close();
         } catch (IOException ex) {
             // TODO
         }
     }
     
     /**
      * Send a message to the peer.
      * 
      * @param message the message to send
      */
     public synchronized void sendMessage(Message message) throws IOException {
         int length = 8;
         for (TLV tlv : message) {
             length += tlv.getDataLength() + 4;
         }
         out.writeShort(DFPConstants.VERSION);
         out.writeShort(message.getType().getCode());
         out.writeInt(length);
         for (TLV tlv : message) {
             out.writeShort(tlv.getType().getCode());
             out.writeShort(tlv.getDataLength() + 4);
             tlv.writeValue(out);
         }
         out.flush();
     }
     
     public void stop() {
         // TODO
     }
 }
