 import java.net.*;
 import java.io.*;
 import java.util.*;
 import org.apache.commons.io.*;
 
 public class Proxy extends Thread {
     
     // Where to connect to
     public String mysqlHost = null;
     public int mysqlPort;
     
     // MySql server stuff
     public Socket mysqlSocket = null;
     public InputStream mysqlIn = null;
     public OutputStream mysqlOut = null;
     
     // Client stuff
     public Socket clientSocket = null;
     public InputStream clientIn = null;
     public OutputStream clientOut = null;
     
     // Plugins
     public ArrayList<Proxy_Plugin> plugins = new ArrayList<Proxy_Plugin>();
     
     // Packet Buffer. ArrayList so we can grow/shrink dynamically
     public ArrayList<Integer> buffer = new ArrayList<Integer>();
     public int offset = 0;
     
     // Stop the thread?
     public int running = 1;
 
     // Connection info
     public Integer packetType = 0;
     public String schema = "";
     public Integer sequenceId = 0;
     public String query = "";
     public Integer affectedRows = 0;
     public Integer lastInsertId = 0;
     public Integer statusFlags = 0;
     public Integer warnings = 0;
     public Integer errorCode = 0;
     public String sqlState = "";
     public String errorMessage = "";
     public Integer protocolVersion = 0;
     public String serverVersion = "";
     public Integer connectionId = 0;
     public Integer capabilityFlags = 0;
     public Integer characterSet = 0;
     public Integer serverCapabilityFlagsOffset = 0;
     public Integer serverCapabilityFlags = 0;
     public Integer serverCharacterSet = 0;
     public Integer clientCapabilityFlags = 0;
     public Integer clientCharacterSet = 0;
     public String user = "";
     public Integer clientMaxPacketSize = 0;
     
     // Modes
     public int mode = 0;
     
     // Allow plugins to muck with the modes
     public int nextMode = 0;
     
     public static final int MODE_INIT                           = 0; // Connection opened
     public static final int MODE_READ_HANDSHAKE                 = 1; // Read the handshake from the server, process it, and forward it
     public static final int MODE_READ_AUTH                      = 2; // Read the reply from the client, process it, and forward it
     public static final int MODE_READ_AUTH_RESULT               = 3; // Read the reply from the server, process it and forward it
     public static final int MODE_READ_QUERY                     = 4; // Read the query from the client, process it, and forward
     public static final int MODE_READ_QUERY_RESULT              = 5; // Read the result set from the server, and process it
     public static final int MODE_SEND_QUERY_RESULT              = 6; // Send a result set to the client
     public static final int MODE_CLEANUP                        = 7; // Connection closed
     
     // Packet types
     public static final int COM_QUIT                            = 0x01;
     public static final int COM_INIT_DB                         = 0x02;
     public static final int COM_QUERY                           = 0x03;
     public static final int COM_FIELD_LIST                      = 0x04;
     public static final int COM_CREATE_DB                       = 0x05;
     public static final int COM_DROP_DB                         = 0x06;
     public static final int COM_REFRESH                         = 0x07;
     public static final int COM_SHUTDOWN                        = 0x08;
     public static final int COM_STATISTICS                      = 0x09;
     public static final int COM_PROCESS_INFO                    = 0x0a;
     public static final int COM_PROCESS_KILL                    = 0x0c;
     public static final int COM_DEBUG                           = 0x0d;
     public static final int COM_PING                            = 0x0e;
     public static final int COM_CHANGE_USER                     = 0x11;
     public static final int COM_BINLOG_DUMP                     = 0x12;
     public static final int COM_TABLE_DUMP                      = 0x13;
     public static final int COM_CONNECT_OUT                     = 0x14;
     public static final int COM_REGISTER_SLAVE                  = 0x15;
     public static final int COM_STMT_PREPARE                    = 0x16;
     public static final int COM_STMT_EXECUTE                    = 0x17;
     public static final int COM_STMT_SEND_LONG_DATA             = 0x18;
     public static final int COM_STMT_CLOSE                      = 0x19;
     public static final int COM_STMT_RESET                      = 0x1a;
     public static final int COM_SET_OPTION                      = 0x1b;
     public static final int COM_STMT_FETCH                      = 0x1c;
     public static final int COM_UNKNOWN                         = 0xff;
     
     public static final int OK                                  = 0x00;
     public static final int ERR                                 = 0xff;
     public static final int EOF                                 = 0xfe;
     
     public static final int SERVER_STATUS_IN_TRANS              = 0x0001;
     public static final int SERVER_STATUS_AUTOCOMMIT            = 0x0002;
     public static final int SERVER_MORE_RESULTS_EXISTS          = 0x0008;
     public static final int SERVER_STATUS_NO_GOOD_INDEX_USED    = 0x0010;
     public static final int SERVER_STATUS_NO_INDEX_USED         = 0x0020;
     public static final int SERVER_STATUS_CURSOR_EXISTS         = 0x0040;
     public static final int SERVER_STATUS_LAST_ROW_SENT         = 0x0080;
     public static final int SERVER_STATUS_DB_DROPPED            = 0x0100;
     public static final int SERVER_STATUS_NO_BACKSLASH_ESCAPES  = 0x0200;
     public static final int SERVER_STATUS_METADATA_CHANGED      = 0x0400;
     public static final int SERVER_QUERY_WAS_SLOW               = 0x0800;
     public static final int SERVER_PS_OUT_PARAMS                = 0x1000;
     
     public static final int CLIENT_LONG_PASSWORD                = 0x0001;
     public static final int CLIENT_FOUND_ROWS                   = 0x0002;
     public static final int CLIENT_LONG_FLAG                    = 0x0004;
     public static final int CLIENT_CONNECT_WITH_DB              = 0x0008;
     public static final int CLIENT_NO_SCHEMA                    = 0x0010;
     public static final int CLIENT_COMPRESS                     = 0x0020;
     public static final int CLIENT_ODBC                         = 0x0040;
     public static final int CLIENT_LOCAL_FILES                  = 0x0080;
     public static final int CLIENT_IGNORE_SPACE                 = 0x0100;
     public static final int CLIENT_PROTOCOL_41                  = 0x0200;
     public static final int CLIENT_INTERACTIVE                  = 0x0400;
     public static final int CLIENT_SSL                          = 0x0800;
     public static final int CLIENT_IGNORE_SIGPIPE               = 0x1000;
     public static final int CLIENT_TRANSACTIONS                 = 0x2000;
     public static final int CLIENT_RESERVED                     = 0x4000;
     public static final int CLIENT_SECURE_CONNECTION            = 0x8000;
     public static final int CLIENT_MULTI_STATEMENTS             = 0x00010000;
     public static final int CLIENT_MULTI_RESULTS                = 0x00020000;
     public static final int CLIENT_PS_MULTI_RESULTS             = 0x00040000;
     public static final int CLIENT_SSL_VERIFY_SERVER_CERT       = 0x40000000;
     public static final int CLIENT_REMEMBER_OPTIONS             = 0x80000000;
     
     
     public Proxy(Socket clientSocket, String mysqlHost, int mysqlPort, ArrayList<Proxy_Plugin> plugins) {
         this.clientSocket = clientSocket;
         this.mysqlHost = mysqlHost;
         this.mysqlPort = mysqlPort;
         this.plugins = plugins;
         
         try {
             this.clientIn = this.clientSocket.getInputStream();
             this.clientOut = this.clientSocket.getOutputStream();
         
             // Connect to the mysql server on the other side
             this.mysqlSocket = new Socket(this.mysqlHost, this.mysqlPort);
             this.mysqlIn = this.mysqlSocket.getInputStream();
             this.mysqlOut = this.mysqlSocket.getOutputStream();
         }
         catch (IOException e) {
             return;
         }
     }
 
     public void run() {
         this.mode = Proxy.MODE_INIT;
 
         while (this.running == 1) {
             
             switch (this.mode) {
                 case Proxy.MODE_INIT:
                     System.err.print("MODE_INIT\n");
                     this.nextMode = Proxy.MODE_READ_HANDSHAKE;
                     this.call_plugins();
                     break;
                 
                 case Proxy.MODE_READ_HANDSHAKE:
                     System.err.print("MODE_READ_HANDSHAKE\n");
                    this.read_handshake();
                     this.nextMode = Proxy.MODE_READ_AUTH;
                     this.call_plugins();
                     break;
                 
                 case Proxy.MODE_READ_AUTH:
                     System.err.print("MODE_READ_AUTH\n");
                    this.read_auth();
                     this.nextMode = Proxy.MODE_READ_AUTH_RESULT;
                     this.call_plugins();
                     break;
                 
                 case Proxy.MODE_READ_AUTH_RESULT:
                     System.err.print("MODE_READ_AUTH_RESULT\n");
                    this.read_auth_result();
                     this.nextMode = Proxy.MODE_READ_QUERY;
                     this.call_plugins();
                     break;
                 
                 case Proxy.MODE_READ_QUERY:
                     System.err.print("MODE_READ_QUERY\n");
                    this.read_query();
                     this.nextMode = Proxy.MODE_READ_QUERY_RESULT;
                     this.call_plugins();
                     break;
                 
                 case Proxy.MODE_READ_QUERY_RESULT:
                     System.err.print("MODE_READ_QUERY_RESULT\n");
                    this.read_query_result();
                     this.nextMode = Proxy.MODE_SEND_QUERY_RESULT;
                     this.call_plugins();
                     break;
                 
                 case Proxy.MODE_SEND_QUERY_RESULT:
                     System.err.print("MODE_SEND_QUERY_RESULT\n");
                    this.send_query_result();
                     this.nextMode = Proxy.MODE_READ_QUERY;
                     this.call_plugins();
                     break;
                 
                 case Proxy.MODE_CLEANUP:
                     System.err.print("MODE_CLEANUP\n");
                     this.call_plugins();
                     this.running = 0;
                     break;
                 
                 default:
                     System.err.print("UNKNOWN MODE "+this.mode+"\n");
                     this.running = 0;
                     break;
             }
             this.mode = this.nextMode;
             
         }
         System.err.print("Exiting thread.\n");
     }
     
     public void halt() {
         this.mode = Proxy.MODE_CLEANUP;
         this.nextMode = Proxy.MODE_CLEANUP;
     }
     
     public void call_plugins() {
         for (int i = 0; i < this.plugins.size(); i++) {
             Proxy_Plugin plugin = this.plugins.get(i);
             switch (this.mode) {
                 case Proxy.MODE_INIT:
                     plugin.init(this);
                     break;
                 
                 case Proxy.MODE_READ_HANDSHAKE:
                     plugin.read_handshake(this);
                     break;
                 
                 case Proxy.MODE_READ_AUTH:
                     plugin.read_auth(this);
                     break;
                 
                 case Proxy.MODE_READ_AUTH_RESULT:
                     plugin.read_auth_result(this);
                     break;
                 
                 case Proxy.MODE_READ_QUERY:
                     plugin.read_query(this);
                     break;
                 
                 case Proxy.MODE_READ_QUERY_RESULT:
                     plugin.read_query_result(this);
                     break;
                 
                 case Proxy.MODE_SEND_QUERY_RESULT:
                     plugin.send_query_result(this);
                     break;
                 
                 case Proxy.MODE_CLEANUP:
                     plugin.cleanup(this);
                     break;
                 
                 default:
                     System.err.print("UNKNOWN MODE "+this.mode+"\n");
                     this.halt();
                     break;
             }
         }
     }
     
     public void clear_buffer() {
         this.offset = 0;
         this.buffer.clear();
     }
     
     public void read_full_result_set(InputStream in) {
         // Assume we have the start of a result set already
         this.offset = 4;
         Integer colCount = this.get_lenenc_int();
         
         // Assume we have a result set
         ArrayList<Integer> resultset = new ArrayList<Integer>();
         resultset.addAll(this.buffer);
         
         for (int i = 0; i < (colCount+1); i++) {
             this.clear_buffer();
             this.read_sized_packet(this.mysqlIn);
             resultset.addAll(this.buffer);
         }
         
         do {
             this.clear_buffer();
             this.read_sized_packet(this.mysqlIn);
             resultset.addAll(this.buffer);
         } while(this.buffer.get(4) != this.EOF);
         
         // Do we have more results?
         this.offset=7;
         Integer statusFlags = this.get_fixed_int(2);
         if ((statusFlags & Proxy.SERVER_MORE_RESULTS_EXISTS) != 0) {
             this.clear_buffer();
             this.read_sized_packet(this.mysqlIn);
             this.read_full_result_set(this.mysqlIn);
         }
         else
             this.clear_buffer();
         
         // Prepend the result set to the buffer. This should transverse the stack correctly
         this.buffer.addAll(0, resultset);
     }
     
     public void read_sized_packet(InputStream in) {
         int b = 0;
         int size = 0;
         try {
             while (in.available() == 0 && this.running == 1) 
                 Thread.sleep(10);
             
             // Read size (3) and Sequence id (1)
             for (int i = 0; i < (3+1); i++) {
                 b = in.read();
                 if (b == -1) {
                     this.halt();
                     return;
                 }
                 this.buffer.add(b);
             }
 
             size = this.get_packet_size();
             
             for (int i = 0; i < size; i++) {
                 b = in.read();
                 if (b == -1) {
                     this.halt();
                     return;
                 }
                 this.buffer.add(b);
             }
         }
         catch (IOException e) {
             System.err.print("IOException.\n");
             this.halt();
         }
         catch (InterruptedException e) {
             System.err.print("InterruptedException.\n");
             this.halt();
         }
     }
     
     public void read_unsized_packet(InputStream in) {
         int b = 0;
         
         try {
             while (in.available() == 0 && this.running == 1) 
                 Thread.sleep(10);
             
             // Read from the client
             while (in.available() > 0) {
 
                 b = in.read();
                 
                 if (b == -1) {
                     this.halt();
                     return;
                 }
                 this.buffer.add(b);
             }
         }
         catch (IOException e) {
             this.halt();
             System.err.print("IOException.\n");
         }
         catch (InterruptedException e) {
             this.halt();
             System.err.print("InterruptedException.\n");
         }
     }
     
     public void write(OutputStream out) {
         int size = this.buffer.size();
         int i = 0;
         
         if (size == 0)
             return;
         
         try {
             for (i = 0; i < size; i++)
                 out.write(this.buffer.get(i));
             this.clear_buffer();
         }
         catch (IOException e) {
             this.halt();
             System.err.print("IOException.\n");
         }
     }
     
     public void read_handshake() {
         this.read_unsized_packet(this.mysqlIn);
         
         this.offset = 0;
         this.protocolVersion = this.get_fixed_int(1);
         this.offset += 4;
         this.serverVersion   = this.get_nul_string();
         this.connectionId    = this.get_fixed_int(4);
         this.offset += 8; // challenge-part-1
         this.offset += 1; //filler
         this.serverCapabilityFlags = this.get_fixed_int(2);
         
         // Remove Compression and SSL support so we can sniff traffic easily
         this.offset -= 2;
         if ((this.serverCapabilityFlags & Proxy.CLIENT_COMPRESS) != 0)
             this.serverCapabilityFlags ^= Proxy.CLIENT_COMPRESS;
         
         if ((this.serverCapabilityFlags & Proxy.CLIENT_SSL) != 0)
             this.serverCapabilityFlags ^= Proxy.CLIENT_SSL;
         
         this.offset -= 2;
         this.set_fixed_int(2, this.serverCapabilityFlags);
         
         this.serverCharacterSet = this.get_fixed_int(1);
 
         this.write(this.clientOut);
     }
     
     public void read_auth_result() {
         this.read_unsized_packet(this.mysqlIn);
         if (this.packetType != Proxy.OK)
             this.halt();
         this.write(this.clientOut);
     }
     
     public void read_auth() {
         this.read_unsized_packet(this.clientIn);
         
         this.offset = 5;
         this.clientCapabilityFlags = this.get_fixed_int(2);
         
         if ((this.clientCapabilityFlags & Proxy.CLIENT_PROTOCOL_41) != 0) {
             this.offset = 5;
             this.clientCapabilityFlags = this.get_fixed_int(4);
             this.offset -= 4;
             // Remove Compression and SSL support so we can sniff traffic easily
             if ((this.clientCapabilityFlags & Proxy.CLIENT_COMPRESS) != 0)
                 this.clientCapabilityFlags ^= Proxy.CLIENT_COMPRESS;
             
             if ((this.clientCapabilityFlags & Proxy.CLIENT_SSL) != 0)
                 this.clientCapabilityFlags ^= Proxy.CLIENT_SSL;
                 
             if ((this.clientCapabilityFlags & Proxy.CLIENT_MULTI_STATEMENTS) != 0)
                 this.clientCapabilityFlags ^= Proxy.CLIENT_MULTI_STATEMENTS;
                 
             if ((this.clientCapabilityFlags & Proxy.CLIENT_MULTI_RESULTS) != 0)
                 this.clientCapabilityFlags ^= Proxy.CLIENT_MULTI_RESULTS;
                 
             if ((this.clientCapabilityFlags & Proxy.CLIENT_PS_MULTI_RESULTS) != 0)
                 this.clientCapabilityFlags ^= Proxy.CLIENT_PS_MULTI_RESULTS;
             
             this.set_fixed_int(4, this.clientCapabilityFlags);
         
             this.clientMaxPacketSize = this.get_fixed_int(4);
             this.clientCharacterSet = this.get_fixed_int(1);
             this.offset += 22;
             this.user = this.get_nul_string();
             
             // auth-response
             if ((this.clientCapabilityFlags & Proxy.CLIENT_SECURE_CONNECTION) != 0)
                 this.get_lenenc_string();
             else
                 this.get_nul_string();
             
             this.schema = this.get_eop_string();
         }
         else {
             this.offset = 5;
             this.clientCapabilityFlags = this.get_fixed_int(2);
             
             this.offset -= 2;
             // Remove Compression and SSL support so we can sniff traffic easily
             if ((this.clientCapabilityFlags & Proxy.CLIENT_COMPRESS) != 0)
                 this.clientCapabilityFlags ^= Proxy.CLIENT_COMPRESS;
             
             if ((this.clientCapabilityFlags & Proxy.CLIENT_SSL) != 0)
                 this.clientCapabilityFlags ^= Proxy.CLIENT_SSL;
             
             this.set_fixed_int(2, this.clientCapabilityFlags);
             
             this.clientMaxPacketSize = this.get_fixed_int(3);
             this.user = this.get_nul_string();
         }
         
         this.write(this.mysqlOut);
     }
     
     public void read_query() {
         if (this.mode < Proxy.MODE_READ_AUTH_RESULT)
             return;
         
         this.read_unsized_packet(this.clientIn);
         
         this.get_packet_size();
         this.packetType = this.buffer.get(4);
         this.sequenceId = this.buffer.get(3);
         
         switch (this.packetType) {
             case Proxy.COM_QUIT:
                 this.halt();
                 break;
             
             // Extract out the new default schema
             case Proxy.COM_INIT_DB:
                 this.schema = this.get_eop_string();
                 break;
             
             // Query
             case Proxy.COM_QUERY:
                 this.offset ++;
                 this.query = this.get_eop_string();
                 break;
             
             default:
                 break;
         }
         
         this.write(this.mysqlOut);
     }
     
     public void read_query_result() {
         if (this.mode < Proxy.MODE_READ_AUTH_RESULT)
             return;
         
         this.read_sized_packet(this.mysqlIn);
         
         this.get_packet_size();
         this.packetType = this.buffer.get(4);
         this.sequenceId = this.buffer.get(3);
         
         switch (this.packetType) {
             case Proxy.OK:
                 
                 if (this.mode >= Proxy.MODE_READ_AUTH_RESULT) {
                     this.offset = 5;
                     this.affectedRows = this.get_lenenc_int();
                     this.lastInsertId = this.get_lenenc_int();
                     this.statusFlags  = this.get_fixed_int(2);
                     this.warnings     = this.get_fixed_int(2);
                 }
                 break;
             
             case Proxy.ERR:
                 if (this.mode >= Proxy.MODE_READ_AUTH_RESULT) {
                     this.offset = 5;
                     this.errorCode    = this.get_fixed_int(2);
                     this.offset++;
                 }
                 break;
             
             default:
                 this.read_full_result_set(this.mysqlIn);
                 break;
         }
     }
     
     public void send_query_result(){
         this.write(this.clientOut);
     }
     
     public int get_packet_size() {
         int size = 0;
         int offset = this.offset;
         this.offset = 0;
         size = this.get_fixed_int(3);
         this.offset = offset;
         return size;
     }
     
     public void set_packet_size() {
         int size = this.buffer.size();
         
         // Remove packet size size
         size -= 3;
         
         // Remove sequence id
         size -= 1;
         
         int offset = this.offset;
         this.offset = 0;
         this.set_fixed_int(3, size);
         this.offset = offset;
     }
     
     public int get_lenenc_int() {
         int value = -1;
         
         // 1 byte int
         if (this.buffer.get(this.offset) < 251 && this.buffer.size() >= (1 + this.offset) ) {
             value = this.buffer.get(this.offset);
             this.offset += 1;
             return value;
         }
             
         // 2 byte int
         if (this.buffer.get(this.offset) == 252 && this.buffer.size() >= (3 + this.offset) ) {
             value = (this.buffer.get(this.offset+1) << 0)
                   | (this.buffer.get(this.offset+2) << 8);
                   
             this.offset += 3;
             return value;
         }
         
         // 3 byte int
         if (this.buffer.get(this.offset) == 253 && this.buffer.size() >= (4 + this.offset) ) {
             value = (this.buffer.get(this.offset+1) << 0)
                   | (this.buffer.get(this.offset+2) << 8)
                   | (this.buffer.get(this.offset+3) << 16);
                   
             this.offset += 4;
             return value;
         }
         
         // 8 byte int
         if (this.buffer.get(this.offset) == 254  && this.buffer.size() >= (9 + this.offset) ) {
             value = (this.buffer.get(this.offset+5) << 0)
                   | (this.buffer.get(this.offset+6) << 8)
                   | (this.buffer.get(this.offset+7) << 16)
                   | (this.buffer.get(this.offset+8) << 24);
                   
             value = value << 32;
                   
             value |= (this.buffer.get(this.offset+1) << 0)
                   |  (this.buffer.get(this.offset+2) << 8)
                   |  (this.buffer.get(this.offset+3) << 16)
                   |  (this.buffer.get(this.offset+3) << 24);
 
             this.offset += 9;
             return value;
         }
         
         System.err.print("Decoding int at offset "+this.offset+" failed!");
         this.halt();
         return -1;
     }
 
     public void set_fixed_int(Integer size, Integer value) {
         if (size == 8 && this.buffer.size() >= (this.offset + size)) {
             this.buffer.set(this.offset+0, ((value >>  0) & 0xFF) );
             this.buffer.set(this.offset+1, ((value >>  8) & 0xFF) );
             this.buffer.set(this.offset+2, ((value >> 16) & 0xFF) );
             this.buffer.set(this.offset+3, ((value >> 24) & 0xFF) );
             this.buffer.set(this.offset+3, ((value >> 32) & 0xFF) );
             this.buffer.set(this.offset+3, ((value >> 40) & 0xFF) );
             this.buffer.set(this.offset+3, ((value >> 48) & 0xFF) );
             this.buffer.set(this.offset+3, ((value >> 56) & 0xFF) );
             
             this.offset += size;
             return;
         }
         
 
         if (size == 4 && this.buffer.size() >= (this.offset + size)) {
             this.buffer.set(this.offset+0, ((value >>  0) & 0xFF) );
             this.buffer.set(this.offset+1, ((value >>  8) & 0xFF) );
             this.buffer.set(this.offset+2, ((value >> 16) & 0xFF) );
             this.buffer.set(this.offset+3, ((value >> 24) & 0xFF) );
             this.offset += size;
             return;
         }
         
         if (size == 3 && this.buffer.size() >= (this.offset + size)) {
             this.buffer.set(this.offset+0, ((value >>  0) & 0xFF) );
             this.buffer.set(this.offset+1, ((value >>  8) & 0xFF) );
             this.buffer.set(this.offset+2, ((value >> 16) & 0xFF) );
             this.offset += size;
             return;
         }
         
         if (size == 2 && this.buffer.size() >= (this.offset + size)) {
             this.buffer.set(this.offset+0, ((value >>  0) & 0xFF) );
             this.buffer.set(this.offset+1, ((value >>  8) & 0xFF) );
             this.offset += size;
             return;
         }
         
         if (size == 1 && this.buffer.size() >= (this.offset + size)) {
             this.buffer.set(this.offset+0, ((value >>  0) & 0xFF) );
             this.offset += size;
             return;
         }
         
         System.err.print("Setting int "+size+": "+value+" at offset "+this.offset+" failed!\n");
         this.halt();
         return;
     }
     
     public Integer get_fixed_int(int size) {
         Integer value = -1;
         
         // 1 byte int
         if (size == 1 && this.buffer.size() >= (size + this.offset) ) {
             value = this.buffer.get(this.offset);
             this.offset += size;
             return value;
         }
             
         // 2 byte int
         if (size == 2 && this.buffer.size() >= (size + this.offset) ) {
             value = (this.buffer.get(this.offset+0) << 0)
                   | (this.buffer.get(this.offset+1) << 8);
             this.offset += size;
             return value;
         }
         
         // 3 byte int
         if (size == 3 && this.buffer.size() >= (size + this.offset) ) {
             value = (this.buffer.get(this.offset+0) << 0)
                   | (this.buffer.get(this.offset+1) << 8)
                   | (this.buffer.get(this.offset+2) << 16);
             this.offset += size;
             return value;
         }
         
         // 4 byte int
         if (size == 4 && this.buffer.size() >= (size + this.offset) ) {
             value = (this.buffer.get(this.offset+0) << 0)
                   | (this.buffer.get(this.offset+1) << 8)
                   | (this.buffer.get(this.offset+2) << 16)
                   | (this.buffer.get(this.offset+3) << 24);
             this.offset += size;
             return value;
         }
         
         // 8 byte int
         if (size == 8 && this.buffer.size() >= (size + this.offset) ) {
             value = (this.buffer.get(this.offset+4) << 0)
                   | (this.buffer.get(this.offset+5) << 8)
                   | (this.buffer.get(this.offset+6) << 16)
                   | (this.buffer.get(this.offset+7) << 24);
                   
             value = value << 32;
                   
             value |= (this.buffer.get(this.offset+0) << 0)
                   |  (this.buffer.get(this.offset+1) << 8)
                   |  (this.buffer.get(this.offset+2) << 16)
                   |  (this.buffer.get(this.offset+3) << 24);
                   
             this.offset += size;
             return value;
         }
         
         System.err.print("Decoding int "+size+" at offset "+this.offset+" failed!\n");
         return -1;
     }
     
     public String get_fixed_string(int len) {
         String str = "";
         int i = 0;
         
         for (i = this.offset; i < this.offset+len; i++)
             str += Proxy.int2char(this.buffer.get(i));
             
         this.offset += i;
         
         return str;
     }
     
     public String get_eop_string() {
         String str = "";
         int i = 0;
         
         for (i = this.offset; i < this.buffer.size(); i++)
             str += Proxy.int2char(this.buffer.get(i));
         this.offset += i;
         
         return str;
     }
     
     public String get_nul_string() {
         String str = "";
         int i = 0;
         int b = 0;
         
         for (i = this.offset; i < this.buffer.size(); i++) {
             b = this.buffer.get(i).intValue();
             if (b == 0x00) {
                 this.offset += 1;
                 break;
             }
             str += Proxy.int2char(b);
             this.offset += 1;
         }
         
         return str;
     }
 
     public String get_lenenc_string() {
         String str = "";
         int b = 0;
         int i = 0;
         
         for (i = this.offset; i < this.buffer.size(); i++) {
             b = this.buffer.get(i).intValue();
             if (b == 0x00)
                 break;
             str += Proxy.int2char(b);
         }
         this.offset += i;
         
         return str;
     }
     
     public static char int2char(int i) {
         return (char)i;
     }
     
     public static char int2char(Integer i) {
         return (char)i.intValue();
     }
     
 }
