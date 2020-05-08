 /*
  * Base proxy code. This should really just move data back and forth
  * Calling plugins as needed
  */
 
 import java.net.*;
 import java.io.*;
 import java.util.*;
 import org.apache.log4j.Logger;
 
 public class Engine extends Thread {
     public Logger logger = Logger.getLogger("Engine");
     
     int port = 0;
     
     public Socket clientSocket = null;
     public InputStream clientIn = null;
     public OutputStream clientOut = null;
     
     // Plugins
     public ArrayList<Plugin_Base> plugins = new ArrayList<Plugin_Base>();
     
     // Packet Buffer. ArrayList so we can grow/shrink dynamically
     public ArrayList<byte[]> buffer = new ArrayList<byte[]>();
     public int offset = 0;
     
     // Stop the thread?
     public boolean running = true;
 
     // What sorta of result set should we expect?
     public int expectedResultSet = MySQL_Flags.RS_OK;
     
     // Connection info
     MySQL_Auth_Challenge authChallenge = null;
     MySQL_Auth_Response authReply = null;
     
     public String schema = "";
     public String query = "";
     public long statusFlags = 0;
     public long sequenceId = 0;
     
     // Buffer or directly pass though the data
     public boolean bufferResultSet = true;
     
     // Modes
     public int mode = MySQL_Flags.MODE_INIT;
     
     // Allow plugins to muck with the modes
     public int nextMode = MySQL_Flags.MODE_INIT;
     
     public Engine(int port, Socket clientSocket, ArrayList<Plugin_Base> plugins) throws IOException {
         this.port = port;
         this.plugins = plugins;
         
        this.clientSocket = clientSocket;
        this.clientSocket.setPerformancePreferences(0, 2, 1);
        this.clientSocket.setTcpNoDelay(true);
        
         this.clientIn = this.clientSocket.getInputStream();
         this.clientOut = this.clientSocket.getOutputStream();
     }
 
     public void run() {
         try {
             while (this.running) {
                 switch (this.mode) {
                     case MySQL_Flags.MODE_INIT:
                         this.logger.trace("MODE_INIT");
                         this.nextMode = MySQL_Flags.MODE_READ_HANDSHAKE;
                         for (Plugin_Base plugin : this.plugins)
                             plugin.init(this);
                     
                     case MySQL_Flags.MODE_READ_HANDSHAKE:
                         this.logger.trace("MODE_READ_HANDSHAKE");
                         this.nextMode = MySQL_Flags.MODE_SEND_HANDSHAKE;
                         for (Plugin_Base plugin : this.plugins)
                             plugin.read_handshake(this);
                         break;
                     
                     case MySQL_Flags.MODE_SEND_HANDSHAKE:
                         this.logger.trace("MODE_SEND_HANDSHAKE");
                         this.nextMode = MySQL_Flags.MODE_READ_AUTH;
                         for (Plugin_Base plugin : this.plugins)
                             plugin.send_handshake(this);
                         break;
                     
                     case MySQL_Flags.MODE_READ_AUTH:
                         this.logger.trace("MODE_READ_AUTH");
                         this.nextMode = MySQL_Flags.MODE_SEND_AUTH;
                         for (Plugin_Base plugin : this.plugins)
                             plugin.read_auth(this);
                         break;
                     
                     case MySQL_Flags.MODE_SEND_AUTH:
                         this.logger.trace("MODE_SEND_AUTH");
                         this.nextMode = MySQL_Flags.MODE_READ_AUTH_RESULT;
                         for (Plugin_Base plugin : this.plugins)
                             plugin.send_auth(this);
                         break;
                     
                     case MySQL_Flags.MODE_READ_AUTH_RESULT:
                         this.logger.trace("MODE_READ_AUTH_RESULT");
                         this.nextMode = MySQL_Flags.MODE_SEND_AUTH_RESULT;
                         for (Plugin_Base plugin : this.plugins)
                             plugin.read_auth_result(this);
                         break;
                     
                     case MySQL_Flags.MODE_SEND_AUTH_RESULT:
                         this.logger.trace("MODE_SEND_AUTH_RESULT");
                         this.nextMode = MySQL_Flags.MODE_READ_QUERY;
                         for (Plugin_Base plugin : this.plugins)
                             plugin.send_auth_result(this);
                         break;
                     
                     case MySQL_Flags.MODE_READ_QUERY:
                         this.logger.trace("MODE_READ_QUERY");
                         this.nextMode = MySQL_Flags.MODE_SEND_QUERY;
                         for (Plugin_Base plugin : this.plugins)
                             plugin.read_query(this);
                         break;
                     
                     case MySQL_Flags.MODE_SEND_QUERY:
                         this.logger.trace("MODE_SEND_QUERY");
                         this.nextMode = MySQL_Flags.MODE_READ_QUERY_RESULT;
                         for (Plugin_Base plugin : this.plugins)
                             plugin.send_query(this);
                         break;
                     
                     case MySQL_Flags.MODE_READ_QUERY_RESULT:
                         this.logger.trace("MODE_READ_QUERY_RESULT");
                         this.nextMode = MySQL_Flags.MODE_SEND_QUERY_RESULT;
                         for (Plugin_Base plugin : this.plugins)
                             plugin.read_query_result(this);
                         break;
                     
                     case MySQL_Flags.MODE_SEND_QUERY_RESULT:
                         this.logger.trace("MODE_SEND_QUERY_RESULT");
                         this.nextMode = MySQL_Flags.MODE_READ_QUERY;
                         for (Plugin_Base plugin : this.plugins)
                             plugin.send_query_result(this);
                         break;
                     
                     case MySQL_Flags.MODE_CLEANUP:
                         this.logger.trace("MODE_CLEANUP");
                         this.nextMode = MySQL_Flags.MODE_CLEANUP;
                         for (Plugin_Base plugin : this.plugins)
                             plugin.cleanup(this);
                         this.halt();
                         break;
                     
                     default:
                         this.logger.fatal("UNKNOWN MODE "+this.mode);
                         this.halt();
                         break;
                 }
                 this.mode = this.nextMode;
             }
             
             this.logger.info("Exiting thread.");            
             this.clientSocket.close();
         }
         catch (IOException e) {}
         finally {
             try {
                 this.clientSocket.close();
             }
             catch (IOException e) {}
             
             try {
                 for (Plugin_Base plugin : this.plugins)
                             plugin.cleanup(this);
             }
             catch (IOException e) {}
         }
     }
     
     public void buffer_result_set() {
         if (!this.bufferResultSet)
             this.bufferResultSet = true;
     }
     
     public void halt() {
         this.logger.trace("Halting!");
         this.running = false;
     }
     
     public void clear_buffer() {
         this.logger.trace("Clearing Buffer.");
         this.offset = 0;
         
         // With how ehcache works, if we clear the buffer via .clear(), it also
         // clears the cached value. Create a new ArrayList and count on java
         // cleaning up after ourselves.
         this.buffer = new ArrayList<byte[]>();
     }
 }
