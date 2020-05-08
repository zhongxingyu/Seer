 import java.net.*;
 import java.io.*;
 import org.apache.log4j.Logger;
 
 public class Plugin_Proxy extends Plugin_Base {
     public Logger logger = Logger.getLogger("Plugin.Proxy");
     
     // MySql server stuff
     public String mysqlHost = "";
     public int mysqlPort = 0;
     public Socket mysqlSocket = null;
     public InputStream mysqlIn = null;
     public OutputStream mysqlOut = null;
     
     public void init(Engine context) throws IOException, UnknownHostException {
         this.logger.trace("init");
         
         String[] phs = System.getProperty("proxyHosts").split(",");
         for (String ph: phs) {
             String[] hi = ph.split(":");
             if (context.port == Integer.parseInt(hi[0])) {
                 this.mysqlHost = hi[1];
                 this.mysqlPort = Integer.parseInt(hi[2]);
                 break;
             }
         }
         
         // Connect to the mysql server on the other side
         this.mysqlSocket = new Socket(this.mysqlHost, this.mysqlPort);
        this.mysqlSocket.setPerformancePreferences(0, 2, 1);
        this.mysqlSocket.setTcpNoDelay(true);
        
         this.logger.info("Connected to mysql server at "+this.mysqlHost+":"+this.mysqlPort);
         this.mysqlIn = this.mysqlSocket.getInputStream();
         this.mysqlOut = this.mysqlSocket.getOutputStream();
     }
     
     public void read_handshake(Engine context) throws IOException {
         this.logger.trace("read_handshake");
         byte[] packet = MySQL_Packet.read_packet(this.mysqlIn);
         
         context.authChallenge = MySQL_Auth_Challenge.loadFromPacket(packet);
         
         // Remove some flags from the reply
         context.authChallenge.removeCapabilityFlag(MySQL_Flags.CLIENT_COMPRESS);
         context.authChallenge.removeCapabilityFlag(MySQL_Flags.CLIENT_SSL);
         
         // Set the default result set creation to the server's character set
         MySQL_ResultSet_Text.characterSet = context.authChallenge.characterSet;
         
         // Set Replace the packet in the buffer
         context.buffer.add(context.authChallenge.toPacket());
     }
     
     public void send_handshake(Engine context) throws IOException {
         this.logger.trace("send_handshake");
         MySQL_Packet.write(context.clientOut, context.buffer);
         context.clear_buffer();
     }
     
     public void read_auth(Engine context) throws IOException {
         this.logger.trace("read_auth");
         byte[] packet = MySQL_Packet.read_packet(context.clientIn);
         context.buffer.add(packet);
         
         context.authReply = MySQL_Auth_Response.loadFromPacket(packet);
         
         if (!context.authReply.hasCapabilityFlag(MySQL_Flags.CLIENT_PROTOCOL_41)) {
             this.logger.fatal("We do not support Protocols under 4.1");
             context.halt();
             return;
         }
         
         context.authReply.removeCapabilityFlag(MySQL_Flags.CLIENT_COMPRESS);
         context.authReply.removeCapabilityFlag(MySQL_Flags.CLIENT_SSL);
         
         context.schema = context.authReply.schema;
     }
     
     public void send_auth(Engine context) throws IOException {
         this.logger.trace("send_auth");
         MySQL_Packet.write(this.mysqlOut, context.buffer);
         context.clear_buffer();
     }
     
     public void read_auth_result(Engine context) throws IOException {
         this.logger.trace("read_auth_result");
         byte[] packet = MySQL_Packet.read_packet(this.mysqlIn);
         context.buffer.add(packet);
         if (MySQL_Packet.getType(packet) != MySQL_Flags.OK) {
             this.logger.fatal("Auth is not okay!");
         }
     }
     
     public void send_auth_result(Engine context) throws IOException {
         this.logger.trace("read_auth_result");
         MySQL_Packet.write(context.clientOut, context.buffer);
         context.clear_buffer();
     }
     
     public void read_query(Engine context) throws IOException {
         this.logger.trace("read_query");
         context.bufferResultSet = false;
         
         byte[] packet = MySQL_Packet.read_packet(context.clientIn);
         context.buffer.add(packet);
         
         context.sequenceId = MySQL_Packet.getSequenceId(packet);
         this.logger.trace("Client sequenceId: "+context.sequenceId);
         
         switch (MySQL_Packet.getType(packet)) {
             case MySQL_Flags.COM_QUIT:
                 this.logger.trace("COM_QUIT");
                 context.halt();
                 break;
             
             // Extract out the new default schema
             case MySQL_Flags.COM_INIT_DB:
                 this.logger.trace("COM_INIT_DB");
                 context.schema = MySQL_Com_Initdb.loadFromPacket(packet).schema;
                 break;
             
             // Query
             case MySQL_Flags.COM_QUERY:
                 this.logger.trace("COM_QUERY");
                 context.query = MySQL_Com_Query.loadFromPacket(packet).query;
                 break;
             
             default:
                 break;
         }
     }
     
     public void send_query(Engine context) throws IOException {
         this.logger.trace("send_query");
         MySQL_Packet.write(this.mysqlOut, context.buffer);
         context.clear_buffer();
     }
     
     public void read_query_result(Engine context) throws IOException {
         this.logger.trace("read_query_result");
         
         byte[] packet = MySQL_Packet.read_packet(this.mysqlIn);
         context.buffer.add(packet);
         
         context.sequenceId = MySQL_Packet.getSequenceId(packet);
         
         switch (MySQL_Packet.getType(packet)) {
             case MySQL_Flags.OK:
             case MySQL_Flags.ERR:
                 break;
             
             default:
                 context.buffer = MySQL_Packet.read_full_result_set(this.mysqlIn, context.clientOut, context.buffer, context.bufferResultSet);
                 break;
         }
     }
     
     public void send_query_result(Engine context) throws IOException {
         this.logger.trace("send_query_result");
         MySQL_Packet.write(context.clientOut, context.buffer);
         context.clear_buffer();
     }
     
     public void cleanup(Engine context) throws IOException {
         this.logger.trace("cleanup");
         this.mysqlSocket.close();
     }
 }
