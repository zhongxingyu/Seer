 package com.github.veithen.dfpagent.protocol.agent;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.github.veithen.dfpagent.Constants;
 import com.github.veithen.dfpagent.protocol.connection.Connection;
 import com.github.veithen.dfpagent.protocol.connection.Handler;
 import com.github.veithen.dfpagent.protocol.connection.LogInterceptor;
 import com.github.veithen.dfpagent.protocol.message.Message;
 import com.github.veithen.dfpagent.protocol.message.MessageType;
 import com.github.veithen.dfpagent.protocol.message.MissingTLVException;
 import com.github.veithen.dfpagent.protocol.tlv.TLV;
 import com.github.veithen.dfpagent.protocol.tlv.Type;
 import com.github.veithen.dfpagent.protocol.tlv.ValueWriter;
 import com.github.veithen.dfpagent.resources.Messages;
 import com.ibm.ejs.ras.Tr;
 import com.ibm.ejs.ras.TraceComponent;
 
 /**
  * Represents a DFP Manager or SLB connecting to an {@link Agent}.
  */
 final class Peer implements Runnable, Handler {
     private static final TraceComponent TC = Tr.register(Peer.class, Constants.TRACE_GROUP, Messages.class.getName());
     
     private final Agent agent;
     private final Connection connection;
     private final KeepAliveManager keepAliveManager;
     private final String identifier;
     
     Peer(Agent agent, Socket socket) throws IOException {
         this.agent = agent;
         identifier = socket.getRemoteSocketAddress().toString();
         connection = new Connection(socket, this);
         connection.addInterceptor(new LogInterceptor(identifier));
         keepAliveManager = new KeepAliveManager(this);
     }
 
     /**
      * Get the identifier of the peer. This identifier is meant to be used in log messages.
      * 
      * @return the peer identifier
      */
     String getIdentifier() {
         return identifier;
     }
     
     public void run() {
         Tr.info(TC, Messages._0014I, identifier);
         new Thread(keepAliveManager, "DFP Keep-Alive Thread: " + identifier).start();
         try {
             connection.run();
         } finally {
             keepAliveManager.stop();
             Tr.info(TC, Messages._0015I, identifier);
         }
     }
     
     void stop() {
         connection.stop();
     }
 
     public void processMessage(Message message) throws IOException {
         try {
             switch (message.getType()) {
                 case SERVER_STATE:
                     TLV loadTLV = message.getRequiredTLV(Type.LOAD);
                     // TODO
                     System.out.println("Got a Server State message");
                     break;
                 case DFP_PARAMETERS:
                     TLV keepAliveTLV = message.getRequiredTLV(Type.KEEP_ALIVE);
                     if (keepAliveTLV.getDataLength() != 4) {
                         Tr.error(TC, Messages._0011E, message.getType().getName());
                         return;
                     }
                     int keepAlive = keepAliveTLV.getValue().readInt();
                     if (TC.isDebugEnabled()) {
                         Tr.debug(TC, "Got new keep-alive value from peer {0}: {1}", new Object[] { identifier, keepAlive });
                     }
                     keepAliveManager.setKeepAlive(keepAlive);
                     break;
                 case BINDID_REQUEST:
                     // Spec: "A separate BindID TLB message should be sent for each DFP
                     // Agent's real server tables."
                     for (WeightInfo weightInfo : agent.getWeightInfoProvider().getWeightInfo()) {
                         TLV tlv = new TLV(Type.BINDID_TABLE);
                         ValueWriter out = tlv.getValueWriter();
                         // ** Server IP Address **
                         out.write(weightInfo.getAddress().getAddress());
                         // ** Server Port Number **
                         out.writeShort(weightInfo.getPort());
                         // ** Protocol **
                         out.writeByte(0);
                         // ** Reserved **
                         out.writeByte(0);
                         // ** Number of Entries **
                         out.writeShort(1);
                         // ** Reserved **
                         out.writeShort(0);
                         // ** BindID **
                         out.writeShort(0);
                         // ** Reserved **
                         out.writeShort(0);
                         // ** Client Network IP Address **
                         out.writeLong(0);
                         // ** Netmask of Client Network **
                         out.writeLong(0);
                         out.commit();
                         connection.sendMessage(new Message(MessageType.BINDID_REPORT, tlv));
                     }
                     // Spec: "When the entire table has been sent (or
                     // there is no table to send), this message should be sent with the
                     // BindID table TLV entries with a zero Server IP address, Server port,
                     // protocol, and number of entries.  This indicates there is no more
                     // data to be sent."
                     connection.sendMessage(new Message(MessageType.BINDID_REPORT, new TLV(Type.BINDID_TABLE, new byte[12])));
                     break;
                 default:
                     Tr.warning(TC, Messages._0007W, message.getType().getName());
             }
         } catch (MissingTLVException ex) {
             Tr.error(TC, Messages._0008E, new Object[] { ex.getMessageType().getName(), ex.getType().getName() });
         }
     }
 
     /**
      * Send a Preference Information message as described in section 6.1 of the DFP specification.
      * This method will call {@link WeightInfoProvider#getWeightInfo()} to get the current weights.
      */
     void sendPreferenceInformation() throws IOException {
         // From the DFP spec: "The real servers are first grouped by
         // their port number and protocol type requiring a separate Load TLV for
         // each grouping."
         Map<Integer,List<WeightInfo>> loadMap = new HashMap<Integer,List<WeightInfo>>();
         for (WeightInfo weightInfo : agent.getWeightInfoProvider().getWeightInfo()) {
             Integer port = weightInfo.getPort();
             List<WeightInfo> list = loadMap.get(port);
             if (list == null) {
                 list = new ArrayList<WeightInfo>();
                 loadMap.put(port, list);
             }
             list.add(weightInfo);
         }
         List<TLV> tlvs = new ArrayList<TLV>(loadMap.size());
         for (Map.Entry<Integer,List<WeightInfo>> entry : loadMap.entrySet()) {
             TLV load = new TLV(Type.LOAD);
             ValueWriter out = load.getValueWriter();
             // ** Port Number **
             out.writeShort(entry.getKey());
             // ** Protocol **
             // Note: the DPF spec is missing the actual values for the protocol field;
             // it only specified the wildcard value (0x00)
             out.writeByte(0);
             // ** Flags **
             out.writeByte(0);
             // ** Number of Hosts **
             out.writeShort(entry.getValue().size());
             // ** Reserved **
             out.writeShort(0);
             for (WeightInfo weightInfo : entry.getValue()) {
                 // ** IP Address **
                 out.write(weightInfo.getAddress().getAddress());
                 // ** BindID **
                 out.writeShort(0);
                 // ** Weight **
                 out.writeShort(weightInfo.getWeight());
             }
             out.commit();
            tlvs.add(load);
         }
         connection.sendMessage(new Message(MessageType.PREFERENCE_INFORMATION, tlvs));
     }
 }
