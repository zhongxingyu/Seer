 /**
  * IRCNetwork.java
  */
 package de.ekdev.ekirc.core;
 
import java.net.UnknownHostException;
 import java.util.List;
 
 import de.ekdev.ekirc.core.commands.connection.IRCPassCommand;
 import de.ekdev.ekirc.core.commands.connection.IRCQuitCommand;
 import de.ekdev.ekirc.core.commands.misc.IRCPongCommand;
 import de.ekdev.ekirc.core.event.IRCConnectEvent;
 
 /**
  * @author ekDev
  */
 public class IRCNetwork
 {
     public final static int MAX_SERVER_NAME_LENGTH = 63;
 
     private String name;
    // private IRCNetworkInfo ircNetworkInfo;
 
     private final IRCManager ircManager;
 
     protected final IRCChannelManager ircChannelManager;
     protected final IRCUserManager ircUserManager;
 
     protected IRCConnection ircConnection;
     protected IRCReader ircReader;
     protected IRCWriter ircWriter;
     protected IRCConnectionLog ircLog;
 
     public IRCNetwork(IRCManager ircManager)
     {
         if (ircManager == null)
         {
             throw new IllegalArgumentException("Argument ircManager is null!");
         }
 
         this.ircManager = ircManager;
 
         this.ircChannelManager = this.createDefaultIRCChannelManager();
         this.ircUserManager = this.createDefaultIRCUserManager();
     }
 
     // ------------------------------------------------------------------------
 
     public void connect(String host, int port, String password)
     {
         if (this.ircConnection != null && this.ircConnection.isConnected()) return;
 
         try
         {
             this.ircLog = new IRCConnectionLog("IRCLog_" + host.replaceAll("\\.", "(dot)") + ".log", true);
             this.ircLog.header(host, null);
         }
         catch (Exception e)
         {
             // FileNotFoundException
             // IllegalArgumentException
             e.printStackTrace();
             this.ircLog = new IRCConnectionLog(System.out);
             this.ircLog.header(host, "IRCConnectionLog file creation failed. Switch to std::out.");
             this.ircLog.exception(e);
         }
 
         this.ircConnection = new IRCConnection(host, port);
         this.name = host;
 
         // create reader, writer threads
        this.ircReader = new IRCReader(this.ircConnection, this, this.ircLog, this.createDefaultIRCMessageProcessor());
         this.ircWriter = new IRCWriter(this.ircConnection, this.ircLog);
 
         // start connection
         this.ircLog.message("Connecting to network ...");
        try
        {
            this.ircConnection.connect();
        }
        catch (UnknownHostException e)
        {
            this.ircLog.exception(e);
        }
         if (this.ircConnection.isConnected())
         {
             this.ircReader.start();
             this.ircWriter.start();
 
             if (password != null && password.length() > 0)
             {
                 this.ircWriter.sendImmediate(new IRCPassCommand(password));
             }
 
             // raise an identification / socket opened / connection established event
             this.ircManager.getEventManager().dispatch(new IRCConnectEvent(this));
         }
        else
        {
            this.ircLog.message("Couldn't connect to network!");

            this.disconnect();
        }
     }
 
     public void quit(String reason)
     {
         if (this.ircWriter != null && this.ircWriter.isRunning())
             this.ircWriter.sendImmediate(new IRCQuitCommand(reason));
     }
 
     public void disconnect()
     {
         if (this.ircConnection == null) return;
 
         this.ircLog.message("Disconnecting from network ...");
 
         this.ircWriter.stop();
         this.ircWriter = null;
         this.ircReader.stop();
         this.ircReader = null;
 
         this.ircConnection.disconnect();
         this.ircConnection = null;
 
        // close log if connection is closed
         this.ircLog.close();
         this.ircLog = null;
     }
 
     // ------------------------------------------------------------------------
 
     public String getName()
     {
         return this.name;
     }
 
    // public IRCNetworkInfo getIRCNetworkInfo()
    // {
    // return this.ircNetworkInfo;
    // }

     // ------------------------------------------------------------------------
 
     public void send(AsIRCMessage ircMessage)
     {
         if (this.ircConnection != null && this.ircConnection.isConnected())
         {
             this.ircWriter.send(ircMessage);
         }
     }
 
     public void sendImmediate(AsIRCMessage ircMessage)
     {
         // TODO: only IRCPongCommands?
         if (this.ircConnection != null && this.ircConnection.isConnected())
         {
             this.ircWriter.sendImmediate(ircMessage);
         }
     }
 
     public void send(List<AsIRCMessage> ircMessages)
     {
         if (ircMessages == null) return;
 
         for (AsIRCMessage ircMessage : ircMessages)
         {
             this.send(ircMessage);
         }
     }
 
     public void send(AsIRCMessage... ircMessages)
     {
         if (ircMessages == null) return;
 
         for (AsIRCMessage ircMessage : ircMessages)
         {
             this.send(ircMessage);
         }
     }
 
     public void sendPong(String pingValue)
     {
         try
         {
             this.sendImmediate(new IRCPongCommand(pingValue));
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
     }
 
     // ------------------------------------------------------------------------
 
     protected IRCChannelManager createDefaultIRCChannelManager()
     {
         return new IRCChannelManager(this);
     }
 
     protected IRCUserManager createDefaultIRCUserManager()
     {
         return new IRCUserManager(this);
     }
 
     protected IRCMessageProcessor createDefaultIRCMessageProcessor()
     {
         // TODO: create new or use from manager?
         return new IRCMessageProcessor(this.ircManager, this);
     }
 
     // ------------------------------------------------------------------------
 
     protected void registerEvents()
     {
 
     }
 
     // ------------------------------------------------------------------------
 
     public final IRCManager getIRCManager()
     {
         return this.ircManager;
     }
 
     public final IRCChannelManager getIRCChannelManager()
     {
         return this.ircChannelManager;
     }
 
     public final IRCUserManager getIRCUserManager()
     {
         return this.ircUserManager;
     }
 
     public final IRCConnectionLog getIRCConnectionLog()
     {
         return this.ircLog;
     }
 }
