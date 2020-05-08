 package cubetech.client;
 
 import cubetech.CGame.CGame;
 import cubetech.CGame.Snapshot;
 import cubetech.common.*;
 import cubetech.common.Common.ErrorCode;
 import cubetech.entities.EntityState;
 import cubetech.gfx.ResourceManager;
 import cubetech.gfx.SpriteManager.Type;
 import cubetech.gfx.TextManager.Align;
 import cubetech.input.Input;
 import cubetech.input.PlayerInput;
 import cubetech.misc.FinishedUpdatingListener;
 import cubetech.misc.MasterServer;
 import cubetech.misc.Ref;
 import cubetech.net.*;
 import cubetech.net.NetChan.*;
 import cubetech.ui.DebugUI;
 import cubetech.ui.ServerListUI;
 import cubetech.ui.ServerListUI.ServerSource;
 import cubetech.ui.UI;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.SocketException;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.util.AbstractMap;
 import java.util.EnumSet;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.util.vector.Vector2f;
 
 /**
  *
  * @author mads
  */
 public class Client {
     CVar name;
     CVar rate;
     CVar model;
     CVar cl_updaterate;
     CVar cl_timeout;
     public CVar cl_cmdrate;
     CVar cl_timenudge;
     CVar cl_debugui;
     CVar cl_showfps;
     public CVar cl_netquality; // 0, less lag more jitter - ~50-100, more lag, less time jitter
     public CVar cl_nodelta;
     public CVar cl_cmdbackup;
 
     public String servername;
 
     public int frametime;
     public int realtime;
     public int state = ConnectState.DISCONNECTED;
 
     public ClientConnect clc = new ClientConnect();
     public ClientActive cl = null;
 
     DebugUI debugUI = null;
 
     // Used by usercmd assembly
     public int frame_msec;
     int oldFrameTime;
 
     protected FinishedUpdatingListener updateListener = null;
     private boolean cgameStarted;
 
     int lastFpsUpdateTime = 0;
     int nFrames = 0;
 
     int currentFPS;
 
     Ping[] cl_pinglist = new Ping[128];
     public ServerInfo[] cl_localServers = new ServerInfo[128];
     public int cl_nLocalServers = 0;
     public ServerInfo[] cl_globalServers = new ServerInfo[128];
     public int cl_nGlobalServers = 0;
     public ServerStatus[] cl_serverStatusList = new ServerStatus[16];
     public int serverStatusCount = 0;
     private ServerInfo[] serversourceList = cl_localServers;
     private int max;
     public ServerListUI.ServerSource serversource = ServerListUI.ServerSource.LAN;
 
     public Message message = new Message();
 
     public void Init() {
         Common.Log("--- Client Initialization ---");
         cl = new ClientActive(); // Clear State
         state = ConnectState.DISCONNECTED;
         realtime = 0;
 
         for (int i= 0; i < cl_pinglist.length; i++) {
             cl_pinglist[i] = new Ping();
         }
         for (int i= 0; i < cl_localServers.length; i++) {
             cl_localServers[i] = new ServerInfo();
         }
         for (int i= 0; i < cl_globalServers.length; i++) {
             cl_globalServers[i] = new ServerInfo();
         }
 //        for (int i= 0; i < cl_serverStatusList.length; i++) {
 //            cl_serverStatusList[i] = new ServerStatus();
 //        }
 
 
         name = Ref.cvars.Get("name", "^3Running^4Man^0", EnumSet.of(CVarFlags.USER_INFO, CVarFlags.ARCHIVE));
         rate = Ref.cvars.Get("rate", "25000", EnumSet.of(CVarFlags.USER_INFO, CVarFlags.ARCHIVE));
         model = Ref.cvars.Get("model", "unknown", EnumSet.of(CVarFlags.USER_INFO, CVarFlags.ARCHIVE));
         cl_updaterate = Ref.cvars.Get("cl_updaterate", "30", EnumSet.of(CVarFlags.USER_INFO, CVarFlags.ARCHIVE));
         cl_updaterate.Min = 15;
         cl_updaterate.Max = 115;
         cl_timeout = Ref.cvars.Get("cl_timeout", "120", EnumSet.of(CVarFlags.NONE));
         cl_cmdrate = Ref.cvars.Get("cl_cmdrate", "101", EnumSet.of(CVarFlags.ARCHIVE));
         cl_timenudge = Ref.cvars.Get("cl_timenudge", "0", EnumSet.of(CVarFlags.TEMP));
         cl_timenudge.Min = -30;
         cl_timenudge.Max = 30;
         cl_nodelta = Ref.cvars.Get("cl_nodelta", "0", EnumSet.of(CVarFlags.NONE));
         cl_cmdbackup = Ref.cvars.Get("cl_cmdbackup", "1", EnumSet.of(CVarFlags.NONE));
         cl_cmdbackup.Min = 0;
         cl_cmdbackup.Max = 5;
         cl_debugui = Ref.cvars.Get("cl_debugui", "0", EnumSet.of(CVarFlags.TEMP));
         cl_debugui.modified = false;
         cl_showfps  = Ref.cvars.Get("cl_showfps", "1", EnumSet.of(CVarFlags.TEMP));
         cl_netquality = Ref.cvars.Get("cl_netquality", "50", EnumSet.of(CVarFlags.TEMP)); // allow 50ms cgame delta
 
         Ref.commands.AddCommand("connect", new cmd_Connect());
         Ref.commands.AddCommand("disconnect", new cmd_Disconnect());
         Ref.commands.AddCommand("cmd", new cmd_Cmd());
         Ref.commands.AddCommand("localservers", new cmd_LocalServers());
         Ref.commands.AddCommand("internetservers", new cmd_InternetServers());
 
         Ref.cvars.Set2("cl_running", "1", true);
 
 //        debugUI = new DebugUI();
 //        debugUI.setVisible(true);
     }
 
     public void updateServerPinging(ServerListUI.ServerSource source) {
         serversource = source;
 
         if(serversource == ServerSource.LAN) {
             serversourceList = cl_localServers;
             max = cl_nLocalServers;
         }
         else {
             serversourceList = cl_globalServers;
             max = cl_nGlobalServers;
         }
 
         for (int i= 0; i < max; i++) {
             ServerInfo info = serversourceList[i];
             if(info.ping > 0)
                 continue;
 
             int j = 0;
             int firstEmpty = -1;
             for (j= 0; j < cl_pinglist.length; j++) {
                 if(cl_pinglist[j].adr == null && firstEmpty == -1)
                     firstEmpty = j;
                 if(cl_pinglist[j].adr != null
                         && cl_pinglist[j].adr.equals(info.adr)) {
                     firstEmpty = j;
                     break;
                 }
             }
             if(firstEmpty != -1) {
                 cl_pinglist[firstEmpty].adr = info.adr;
                 cl_pinglist[firstEmpty].time = 0;
                 cl_pinglist[firstEmpty].start = Ref.common.Milliseconds();
                 Ref.net.SendOutOfBandPacket(NetSource.CLIENT, info.adr, "getinfo");
             }
         }
 
 
     }
 
     private int getPingQueueCount() {
         int count = 0;
         for (Ping ping : cl_pinglist) {
             if(ping.adr != null)
                 count++;
         }
         return count;
     }
 
     public void SetUpdateListener(FinishedUpdatingListener listener) {
         updateListener = listener;
     }
 
     public void RemoveUpdateListener()
     {
         updateListener = null;
     }
 
     // ServerInfoAccessors
     
 
     
 
     public void Frame(int msec) {
         if(Ref.common.cl_running.iValue == 0)
             return;
 
         if(state == ConnectState.DISCONNECTED && (Ref.Input.GetKeyCatcher() & Input.KEYCATCH_UI) == 0) {
             Ref.ui.SetActiveMenu(UI.MENU.MAINMENU);
         }
         
         // decide the simulation time
         frametime = msec;
         realtime += msec;
 
         Ref.glRef.Update();
 
         // see if we need to update any userinfo
         CheckUserInfo();
 
         // if we haven't gotten a packet in a long time,
         // drop the connection
         CheckTimeout();
 
         // send intentions now
         Ref.Input.Update();
         SendCommand();
 
         // resend a connection request if necessary
         CheckForResend();
 
         // decide on the serverTime to render
         cl.SetCGameTime();
 
         Ref.ResMan.Update(); // Do a bit of texture loading
         
         
         // update the screen
         UpdateScreen();
 
         Ref.soundMan.Update((int)frametime);
 
         
 
         if(cl_debugui.modified) {
             if(debugUI == null && cl_debugui.iValue == 1)
                 debugUI = new DebugUI();
             debugUI.setVisible(cl_debugui.iValue == 1);
             cl_debugui.modified = false;
         }
 
         if(updateListener != null)
             updateListener.FinishedUpdating();
     }
 
     public void PacketEvent(Packet data) {
         clc.LastPacketTime = realtime;
 
         if(data.OutOfBand)
         {
             ConnectionlessPacket(data);
             return;
         }
 
         if(state < ConnectState.CONNECTED)
             return;
 
         //
         // packet from server
         //
         if(!data.endpoitn.equals(clc.ServerAddr)) {
             Common.LogDebug("Sequence packet without connection");
             return;
         }
 
         if(!clc.netchan.Process(data))
             return; // out of order, duplicate, etc..
 
         ByteBuffer buf = data.buf.GetBuffer();
         int currPos = buf.position();
         buf.rewind();
 
         buf.getInt(); // Remove magic int
         clc.serverMessageSequence = buf.getInt();
         buf.position(currPos);
 
         ParseServerMessage(data);
     }
 
     public void MapLoading() {
         if(Ref.common.cl_running.iValue == 0)
             return;
 
         Ref.Console.Close();
         Ref.Input.SetKeyCatcher(Input.KEYCATCH_NONE);
 
         // if we are already connected to the local host, stay connected
         if(state >= ConnectState.CONNECTED && servername.equalsIgnoreCase("localhost"))
         {
             state = ConnectState.CONNECTED;
             clc.servermessage = "";
             cl.GameState.clear();
             clc.LastPacketSentTime = -9999;
             UpdateScreen();
         } else {
             Ref.cvars.Set2("nextmap", "", true);
             Disconnect(true);
             servername = "localhost";
             state = ConnectState.CHALLENGING;
             Ref.Input.SetKeyCatcher(Input.KEYCATCH_NONE);
             UpdateScreen();
             clc.ConnectTime = -3000;
             clc.ServerAddr = new InetSocketAddress("localhost", Ref.net.net_svport.iValue);
             CheckForResend();
         }
     }
 
     void ParseServerMessage(Packet packet) {
         // get the reliable sequence acknowledge number
         clc.reliableAcknowlege = packet.buf.ReadInt();
         if(clc.reliableAcknowlege < clc.reliableSequence - 64) {
             clc.reliableSequence = clc.reliableAcknowlege;
         }
 
         // parse the message
         while(true) {
             int cmd = packet.buf.ReadInt();
             
             if(cmd == SVC.OPS_EOF)
                 break;
 
             switch(cmd) {
                 case SVC.OPS_NOP:
                     break;
                 case SVC.OPS_SERVERCOMMAND:
                     clc.ParseCommandString(packet.buf);
                     break;
                 case SVC.OPS_GAMESTATE:
                     ParseGameState(packet.buf);
                     break;
                 case SVC.OPS_SNAPSHOT:
                     ParseSnapshot(packet.buf);
                     break;
                 case SVC.OPS_DOWNLOAD:
                     ParseDownload(packet.buf);
                     break;
                 default:
                     Common.Log("Illegable server message.");
                     break;
             }
         }
     }
 
     void ConnectionlessPacket(Packet packet) {
         String str = packet.buf.ReadString();
         String[] tokens = Commands.TokenizeString(str, false);
         if(tokens.length <= 0)
             return;
 
         String c = tokens[0];
         if(c.equalsIgnoreCase("challengeResponse")) {
             if(state != ConnectState.CONNECTING) {
                 Common.Log("Unwanted challenge response recived. Ignored");
                 return;
             }
 
             if(!packet.endpoitn.equals(clc.ServerAddr))
             {
                 // This challenge response is not coming from the expected address.
                 // Check whether we have a matching client challenge to prevent
                 // connection hi-jacking.
                 c = tokens[2];
                 try {
                     if(clc.challenge != Integer.parseInt(c)){
                         Common.Log("Challenge response was recived from an unexpected source");
                         return;
                     }
                 } catch(NumberFormatException e) {
                     Common.Log("Bogus challenge response was recived from an unexpected source");
                     return;
                 }
             }
 
             // start sending challenge response instead of challenge request packets
             clc.challenge = Integer.parseInt(tokens[1]);
             state = ConnectState.CHALLENGING;
             clc.ConnectPacketCount = 0;
             clc.ConnectTime = -9999;
 
             // take this address as the new server address.  This allows
             // a server proxy to hand off connections to multiple servers
             clc.ServerAddr = packet.endpoitn;
             //System.out.println("Got challenge response.");
             return;
         }
         if(c.equalsIgnoreCase("connectResponse")) {
             if(state >= ConnectState.CONNECTED)
             {
                 Common.Log("Duplicate connection recieved. Rejected.");
                 return;
             }
 
             if(state != ConnectState.CHALLENGING) {
                 Common.Log("ConnectResonse while not connecting. Ignoring.");
                 return;
             }
 
             if(!packet.endpoitn.equals(clc.ServerAddr))
             {
                 Common.Log("ConnectResponse from wrong address. ignored.");
                 return;
             }
 
             clc.netchan = new NetChan(NetSource.CLIENT, packet.endpoitn, Ref.cvars.Find("net_qport").iValue);
             try {
                 Ref.net.ConnectClient(packet.endpoitn);
             } catch (SocketException ex) {
                 Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
             }
             state = ConnectState.CONNECTED;
             clc.LastPacketSentTime = -999;
             return;
         }
 
         // server responding to an info broadcast
         if(c.equalsIgnoreCase("infoResponse")) {
             ServerInfoPacket(packet.endpoitn, tokens);
             return;
         }
 
         // server responding to a get playerlist
         if(c.equalsIgnoreCase("statusResponse")) {
             ServerStatusResponse(packet.endpoitn, tokens);
             return;
         }
 
         // a disconnect message from the server, which will happen if the server
 	// dropped the connection but it is still getting packets from us
         if(c.equalsIgnoreCase("disconnect")) {
             DisconnectPacket(packet.endpoitn);
             return;
         }
 
         // echo request from server
         if(c.equalsIgnoreCase("echo")) {
             Ref.net.SendOutOfBandPacket(NetSource.CLIENT, packet.endpoitn, Commands.ArgsFrom(tokens, 1));
             return;
         }
 
         // echo request from server
         if(c.equalsIgnoreCase("print")) {
             clc.servermessage = tokens[1];
             Common.Log(clc.servermessage);
             return;
         }
 
         // list of servers sent back by a master server
         if(c.equalsIgnoreCase("getserversResponse")) {
             // TODO: Add master server
             return;
         }
 
         Common.LogDebug("Unknown OOB packet from " + packet.endpoitn.toString());
     }
     
     
 
     public void ForwardCommandToServer(String str, String[] tokens) {
         String cmd = tokens[0];
 
         // ignore key up
         if(cmd.charAt(0) == '-')
             return;
 
         if(state < ConnectState.CONNECTED || cmd.charAt(0) == '+')
         {
             Common.Log("Unknown command " + cmd);
             return;
         }
 
         if(tokens.length > 1)
             AddReliableCommand(str, false); // concatted
         else
             AddReliableCommand(cmd, false); // single command
     }
 
     public void AddReliableCommand(String cmd, boolean isDisconnect) {
         int unacknowledged = clc.reliableSequence - clc.reliableAcknowlege;
         // if we would be losing an old command that hasn't been acknowledged,
         // we must drop the connection
         // also leave one slot open for the disconnect command in this case.
         if((isDisconnect && unacknowledged > 64) ||
                 (!isDisconnect  && unacknowledged >= 64)) {
             Ref.common.Error(ErrorCode.DROP, "Client command overflow.");
         }
 
         clc.reliableCommands[++clc.reliableSequence & 63] = cmd;
     }
 
 
 
     public void Disconnect(boolean showMainMenu) {
         if(Ref.common.cl_running.iValue == 0)
             return;
 
         Ref.cvars.Set2("ui_fullscreen", "1", true);
 
         if(showMainMenu)
             Ref.ui.SetActiveMenu(UI.MENU.MAINMENU);
 
         // send a disconnect message to the server
         // send it a few times in case one is dropped
         if(state >= ConnectState.CONNECTED) {
             AddReliableCommand("disconnect", true);
             WritePacket();
             WritePacket();
             WritePacket();
         }
 
         cl = new ClientActive();
 
         // wipe the client connection
         clc = new ClientConnect();
         state = ConnectState.DISCONNECTED;
     }
 
     public void ShutdownAll() {
         cgameStarted = false;
         if(Ref.cgame != null) {
             Ref.cgame.Shutdown();
             Ref.cgame = null;
         }
         Ref.Input.SetKeyCatcher(Ref.Input.GetKeyCatcher() & ~Input.KEYCATCH_UI);
         //Ref.cgame.CG_Shutdown();
     }
 
     private void CheckUserInfo() {
         // don't add reliable commands when not yet connected
         if(state < ConnectState.CHALLENGING)
             return;
 
         // don't overflow the reliable command buffer when paused
         if(CheckPaused())
             return;
 
         // send a reliable userinfo update if needed
         if(Ref.cvars.modifiedFlags.contains(CVarFlags.USER_INFO)) {
             Ref.cvars.modifiedFlags.remove(CVarFlags.USER_INFO);
             AddReliableCommand(String.format("userinfo \"%s\"", Ref.cvars.InfoString(CVarFlags.USER_INFO)), false);
         }
     }
 
     private void CheckTimeout() {
         if( (!CheckPaused() || Ref.common.sv_paused.iValue == 0)
             &&    state >= ConnectState.CONNECTED && realtime - clc.LastPacketTime > cl_timeout.fValue * 1000f) {
             if(++cl.timeoutCount > 5) {
                 Ref.common.Error(ErrorCode.DROP, "Server connection timed out.");
                 //Disconnect(true);
                 return;
             }
         } else
             cl.timeoutCount = 0;
     }
 
     private void CheckForResend() {
         // resend if we haven't gotten a reply yet
         if(state != ConnectState.CONNECTING && state != ConnectState.CHALLENGING)
             return;
 
         if(realtime - clc.ConnectTime < 3000) // wait 3 secs before resending
             return;
 
         clc.ConnectTime = realtime;
         clc.ConnectPacketCount++;
 
         if(clc.ConnectPacketCount == 5) {
             state = ConnectState.DISCONNECTED;
             clc.ConnectPacketCount = 0;
 
             Ref.common.Error(Common.ErrorCode.DROP, "Could not connect. No response from server");
             //System.out.println("Could not connect. No response from server.");
             //Disconnect(true);
             return;
         }
 
         switch(state) {
             case ConnectState.CONNECTING:
                 String data = "getchallenge " + clc.challenge;
                 Ref.net.SendOutOfBandPacket(NetSource.CLIENT, clc.ServerAddr, data);
                 Common.Log("Connecting...");
                 break;
             case ConnectState.CHALLENGING:
                 // sending back the challenge
                 int port = Ref.cvars.Find("net_qport").iValue;
                 
                 String cs = Ref.cvars.InfoString(CVarFlags.USER_INFO);
                 cs = Info.SetValueForKey(cs, "qport", ""+port);
                 cs = Info.SetValueForKey(cs, "challenge", ""+clc.challenge);
 
                 data = String.format("connect \"%s\"", cs);
                 Ref.net.SendOutOfBandPacket(NetSource.CLIENT, clc.ServerAddr, data);
                 Ref.cvars.modifiedFlags.remove(CVarFlags.USER_INFO);
 //                System.out.println("CL: Got challenge.");
                 break;
         }
     }
 
     public void UpdateScreen() {
         BeginFrame();
 
 
         if(!Ref.ui.IsFullscreen()) {
             switch(state) {
                 case ConnectState.DISCONNECTED:
                     // Force ui up
                     Ref.ui.SetActiveMenu(UI.MENU.MAINMENU);
                     break;
                 case ConnectState.CONNECTING:
                 case ConnectState.CHALLENGING:
                 case ConnectState.CONNECTED:
                     // connecting clients will only show the connection dialog
                     // refresh to update the time
                     Ref.ui.DrawConnectScreen(false);
                     break;
                 case ConnectState.LOADING:
                 case ConnectState.PRIMED:
                     // draw the game information screen and loading progress
                     Ref.cgame.DrawActiveFrame(cl.serverTime);
 
                     // also draw the connection information, so it doesn't
                     // flash away too briefly on local or lan games
                     // refresh to update the time
                     Ref.ui.DrawConnectScreen(true);
                     break;
                 case ConnectState.ACTIVE:
                     Ref.cgame.DrawActiveFrame(cl.serverTime);
                     break;
             }
         }
 
 //        Ref.StateMan.RunFrame((int)frametime);
 
             if((Ref.Input.GetKeyCatcher() & Input.KEYCATCH_UI) > 0)
                 Ref.ui.Update(realtime);
 
         EndFrame();
     }
 
     private void ParseGameState(NetBuffer buf) {
         Ref.Console.Close();
 
 
         clc.ConnectPacketCount = 0;
         // wipe local client state
         cl = new ClientActive();
         // a gamestate always marks a server command sequence
         clc.serverCommandSequence = buf.ReadInt();
         // parse all the configstrings and baselines
         EntityState nullstate2 = new EntityState();
         while(true) {
             int cmd = buf.ReadInt();
 
             if(cmd == SVC.OPS_EOF)
                 break;
 
             if(cmd == SVC.OPS_CONFIGSTRING) {
                 // Read index and string
                 int index = buf.ReadInt();
                 String s = buf.ReadString();
                 cl.GameState.put(index, s);
             }
             else if(cmd == SVC.OPS_BASELINE) {
                 // Read baseline
                 int newnum = buf.ReadInt();
                 if(newnum < 0 || newnum >= Common.MAX_GENTITIES) {
                     Ref.common.Error(ErrorCode.DROP, "ParseGameState: Baseline number out of range");
                 }
                 cl.entityBaselines[newnum].ReadDeltaEntity(buf, nullstate2, newnum);
             } else {
                 Ref.common.Error(ErrorCode.DROP, "ParseGameState(): Bad command byte");
             }
         }
 
         clc.ClientNum = buf.ReadInt();
 
         // parse serverId and other cvars
         cl.SystemInfoChanged();
 
         InitDownloads();
     }
 
     // Called every frame to builds and sends a command packet to the server.
     public void SendCommand() {
         // don't send any message if not connected
         if(state < ConnectState.CONNECTED)
             return;
 
         // don't send commands if paused
         if(Ref.common.sv_running.iValue == 1 && Ref.common.sv_paused.iValue == 1 && Ref.common.cl_paused.iValue == 1)
             return;
 
         // we create commands even if a demo is playing,
         CreateNewCommands();
 
         // don't send a packet if the last packet was sent too recently
         if(!ReadyToSendPacket())
             return;
 
         // Write packet
         WritePacket();
     }
 
     private boolean ReadyToSendPacket() {
 //        if(Ref.client.clc.downloadName != null && clc.reliableAcknowlege < clc.reliableSequence) {
 //            return true;
 ////            System.out.println("Sending: " + clc.reliableCommands[i & 63]);
 //
 //        }
 
         // if we don't have a valid gamestate yet, only send
         // one packet a second
         if(Ref.client.state != ConnectState.ACTIVE &&
                 Ref.client.state != ConnectState.PRIMED &&
                 (Ref.client.realtime - Ref.client.clc.LastPacketSentTime < 1000 && (Ref.client.clc.downloadName == null)))
             return false;
 
         int oldpacketnum = (Ref.client.clc.netchan.outgoingSequence - 1) & 31;
         int delta = Ref.client.realtime - Ref.client.cl.outPackets[oldpacketnum].realtime;
 
         // the accumulated commands will go out in the next packet
         if(delta < 1000 / Ref.client.cl_cmdrate.iValue)
             return false;
 
         return true;
     }
 
     /*
     ===================
     CL_WritePacket
 
     Create and send the command packet to the server
     Including both the reliable commands and the usercmds
 
     During normal gameplay, a client packet will contain something like:
 
     4	sequence number
     2	qport
     4	serverid
     4	acknowledged sequence number
     4	clc.serverCommandSequence
     <optional reliable commands>
     1	clc_move or clc_moveNoDelta
     1	command count
     <count * usercmds>
 
     ===================
     */
     PlayerInput nullstate = new PlayerInput();
     public void WritePacket() {
         NetBuffer msg = NetBuffer.GetNetBuffer(false); // Netchan will add the magic
 
         // write the current serverId so the server
         // can tell if this is from the current gameState
         msg.Write(cl.serverid);
 
         // write the last message we received, which can
         // be used for delta compression, and is also used
         // to tell if we dropped a gamestate
         msg.Write(clc.serverMessageSequence);
 
         // write the last reliable message we received
         msg.Write(clc.serverCommandSequence);
 
         // write any unacknowledged clientCommands
         for (int i = clc.reliableAcknowlege+1; i <= clc.reliableSequence; i++) {
             msg.Write(CLC.OPS_CLIENTCOMMAND);
             msg.Write(i);
             msg.Write(clc.reliableCommands[i & 63]);
 //            System.out.println("Sending: " + clc.reliableCommands[i & 63]);
         }
 
         // we want to send all the usercmds that were generated in the last
         // few packet, so even if a couple packets are dropped in a row,
         // all the cmds will make it to the server
         int oldpacketnum = (clc.netchan.outgoingSequence-1-cl_cmdbackup.iValue)&31;
         int count = cl.cmdNumber - cl.outPackets[oldpacketnum].cmdNumber;
         if(count > 32)
         {
             count = 32;
             Common.Log("WritePacket: Can't send more than 32 usercommands pr frame");
         }
 
         PlayerInput old = nullstate;
         if(count >= 1) {
             // begin a client move command
             if(!cl.snap.valid
                     || clc.serverMessageSequence != cl.snap.messagenum
                     || cl_nodelta.iValue > 0)
                 msg.Write(CLC.OPS_MOVENODELTA);
             else
                 msg.Write(CLC.OPS_MOVE);
 
             // write the command count
             msg.Write(count);
 
             // write all the commands, including the predicted command
             for (int i= 0; i < count; i++) {
                 int index = (cl.cmdNumber - count + 1 + i) & 63;
                 cl.cmds[index].WriteDeltaUserCmd(msg, old);
                 old = cl.cmds[index];
             }
         }
 
         // deliver the message
         int packetnum = clc.netchan.outgoingSequence & 31;
         cl.outPackets[packetnum].realtime = realtime;
         cl.outPackets[packetnum].servertime = old.serverTime;
         cl.outPackets[packetnum].cmdNumber = cl.cmdNumber;
         clc.LastPacketSentTime = realtime;
 
         // Send!
         msg.Write(CLC.OPS_EOF);
         clc.netchan.Transmit(msg);
     }
 
     // Called every frame to builds and sends a command packet to the server.
     private void CreateNewCommands() {
         // don't send any message if not connected
         if(Ref.client.state < ConnectState.PRIMED)
             return;
 
 
         // Get delta msecs since last frame from the common subsystem
         frame_msec = Ref.common.frametime - oldFrameTime;
 
         // if running less than 5fps, truncate the extra time to prevent
         // unexpected moves after a hitch
         if(frame_msec > 200)
             frame_msec = 200;
         oldFrameTime = Ref.common.frametime;
 
         cl.cmdNumber++;
         int cmdNum = cl.cmdNumber & 63;
        if(cl.cmdNumber > 1 &&
                ((Ref.Input.GetKeyCatcher() & (Input.KEYCATCH_CONSOLE | Input.KEYCATCH_MESSAGE | Input.KEYCATCH_UI)) > 0)) {
            cl.cmds[cmdNum] = cl.cmds[(cl.cmdNumber-1) & 63].Clone();
        } else {
            cl.cmds[cmdNum] = Ref.Input.CreateCmd();
        }
         cl.cmds[cmdNum].serverTime = cl.serverTime;
     }
 
     private void InitCGame() {
         String mapname = Info.ValueForKey(cl.GameState.get(0), "mapname");
         cl.mapname = mapname;
         state = ConnectState.LOADING;
 
         // init for this gamestate
         // use the lastExecutedServerCommand instead of the serverCommandSequence
         // otherwise server commands sent just before a gamestate are dropped
         Ref.cgame = new CGame();
         Ref.cgame.Init(clc.serverMessageSequence, clc.lastExecutedServerCommand, clc.ClientNum);
 
         // we will send a usercmd this frame, which
         // will cause the server to send us the first snapshot
         state = ConnectState.PRIMED;
     }
 
     public boolean CheckPaused() {
         // if cl_paused->modified is set, the cvar has only been changed in
 	// this frame. Keep paused in this frame to ensure the server doesn't
 	// lag behind.
         if(Ref.common.cl_paused.iValue == 1 || Ref.common.cl_paused.modified)
             return true;
 
         return false;
     }
 
     /*
     ================
     CL_ParseSnapshot
 
     If the snapshot is parsed properly, it will be copied to
     cl.snap and saved in cl.snapshots[].  If the snapshot is invalid
     for any reason, no changes to the state will be made at all.
     ================
     */
     private void ParseSnapshot(NetBuffer buf) {
         // read in the new snapshot to a temporary buffer
         // we will only copy to cl.snap if it is valid
         CLSnapshot newsnap = new CLSnapshot();
         CLSnapshot oldsnap = null;
 
         // we will have read any new server commands in this
         // message before we got to svc_snapshot
         newsnap.serverCommandSequence = clc.serverCommandSequence;
         newsnap.serverTime = buf.ReadInt();
 
         Ref.common.cl_paused.modified = false;
         newsnap.messagenum = clc.serverMessageSequence;
         
         int deltaNum = buf.ReadInt();
         if(deltaNum <= 0)
             newsnap.deltanum = -1;
         else
             newsnap.deltanum = newsnap.messagenum - deltaNum;
         newsnap.snapFlag = buf.ReadInt();
 
         // If the frame is delta compressed from data that we
         // no longer have available, we must suck up the rest of
         // the frame, but not use it, then ask for a non-compressed
         // message
         if(newsnap.deltanum <= 0) {
             newsnap.valid = true; // uncompressed frame
         } else {
             oldsnap = cl.snapshots[newsnap.deltanum & 31];
             if(!oldsnap.valid) {
                 // Should never happen
                 Common.Log("ParseSnapshot: Delta from invalid frame");
             }
             else if(oldsnap.messagenum != newsnap.deltanum) {
                 // The frame that the server did the delta from
                 // is too old, so we can't reconstruct it properly.
                 Common.LogDebug("Delta frame too old");
             } else if(cl.parseEntitiesNum - oldsnap.parseEntitiesNum > Common.MAX_GENTITIES - 128)
                 Common.LogDebug("ParseSnapshot: Delta parseEntitiesnum too old");
             else
                 newsnap.valid = true; // valid delta parse
         }
 
         // read playerinfo
         if(oldsnap == null)
             newsnap.ps.ReadDelta(buf, null);
         else
             newsnap.ps.ReadDelta(buf, oldsnap.ps);
 
 
         // read packet entities
         ParsePacketEntities(buf, oldsnap, newsnap);
 
          // if not valid, dump the entire thing now that it has
          // been properly read
         if(!newsnap.valid)
             return;
 
         // clear the valid flags of any snapshots between the last
         // received and this one, so if there was a dropped packet
         // it won't look like something valid to delta from next
         // time we wrap around in the buffer
         int oldMessageNum = cl.snap.messagenum + 1;
         if(newsnap.messagenum - oldMessageNum >= 32) {
             oldMessageNum = newsnap.messagenum-31;
         }
 
         for (; oldMessageNum < newsnap.messagenum; oldMessageNum++) {
             cl.snapshots[oldMessageNum&31].valid = false;
         }
 
         // copy to the current good spot
         cl.snap = newsnap;
         cl.snap.ping = 999;
 
         // save the frame off in the backup array for later delta comparisons
         cl.snapshots[cl.snap.messagenum & 31] = cl.snap;
         cl.newsnapshots = true;
 
     }
 
     private void ParsePacketEntities(NetBuffer buf, CLSnapshot oldsnap, CLSnapshot newsnap) {
         newsnap.parseEntitiesNum = cl.parseEntitiesNum;
         newsnap.numEntities = 0;
 
         // delta from the entities present in oldframe
         int oldindex = 0, oldnum = 0;
         EntityState oldstate = null;
         if(oldsnap == null)
             oldnum = 99999;
         else {
             if(oldindex >= oldsnap.numEntities)
                 oldnum = 99999;
             else
             {
                 oldstate = cl.parseEntities[(oldsnap.parseEntitiesNum + oldindex) & Common.MAX_GENTITIES-1];
                 oldnum = oldstate.ClientNum;
             }
         }
 
         while(true) {
             // read the entity index number
             int newnum = buf.ReadInt();
 
             if(newnum >= Common.MAX_GENTITIES-1)
                 break;
 
             while(oldnum < newnum) {
                 // one or more entities from the old packet are unchanged
                 DeltaEntity(buf, newsnap, oldnum, oldstate, true);
                 oldindex++;
 
                 if(oldindex >= oldsnap.numEntities)
                     oldnum = 99999;
                 else {
                     oldstate = cl.parseEntities[(oldsnap.parseEntitiesNum + oldindex) & Common.MAX_GENTITIES-1];
                     oldnum = oldstate.ClientNum;
                 }
             }
 
             if(oldnum == newnum) {
                 // delta from previous state
                 DeltaEntity(buf, newsnap, newnum, oldstate, false);
 
                 oldindex++;
 
                 if(oldindex >= oldsnap.numEntities)
                     oldnum = 99999;
                 else {
                     oldstate = cl.parseEntities[(oldsnap.parseEntitiesNum + oldindex) & Common.MAX_GENTITIES-1];
                     oldnum = oldstate.ClientNum;
                 }
                 continue;
             }
 
             if(oldnum > newnum) {
                 // delta from baseline
                 DeltaEntity(buf, newsnap, newnum, cl.entityBaselines[newnum], false);
                 continue;
             }
         }
 
         // any remaining entities in the old frame are copied over
         while(oldnum != 99999) {
             // one or more entities from the old packet are unchanged
             DeltaEntity(buf, newsnap, oldnum, oldstate, true);
 
             oldindex++;
 
             if(oldindex >= oldsnap.numEntities)
                 oldnum = 99999;
             else
             {
                 oldstate = cl.parseEntities[(oldsnap.parseEntitiesNum + oldindex) & Common.MAX_GENTITIES-1];
                 oldnum = oldstate.ClientNum;
             }
         }
     }
 
     private void DeltaEntity(NetBuffer msg, CLSnapshot frame, int newnum, EntityState old, boolean unchanged) {
         // save the parsed entity state into the big circular buffer so
         // it can be used as the source for a later delta
         EntityState ent = cl.parseEntities[cl.parseEntitiesNum & Common.MAX_GENTITIES-1];
         if(unchanged)
             old.Clone(ent);
         else
             ent.ReadDeltaEntity(msg, old, newnum);
 
         cl.parseEntities[cl.parseEntitiesNum & Common.MAX_GENTITIES-1] = ent;
         if(ent.ClientNum == Common.MAX_GENTITIES-1)
             return; // entity was removed
 
         cl.parseEntitiesNum++;
         frame.numEntities++;
     }
 
     private void BeginFrame() {
 //       GL11.glClearDepth(1000);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 //       GL11.glClearDepth(1000);
        Ref.SpriteMan.Reset();
     }
 
     private void EndFrame() {
         nFrames++;
         if(realtime >= lastFpsUpdateTime + 1000) {
             currentFPS = nFrames;
             nFrames = 0;
             lastFpsUpdateTime = realtime;
         }
 //        if(true) {
 //        Ref.SpriteMan.Reset();
 //        return;
 //        }
         // Render normal sprites
         Ref.SpriteMan.DrawNormal();
 
         // Set HUD render projection
         GL11.glViewport(0, 0, (int)Ref.glRef.GetResolution().x, (int)Ref.glRef.GetResolution().y);
         GL11.glMatrixMode(GL11.GL_PROJECTION);
         GL11.glLoadIdentity();
         //GL11.glOrtho(0, 1,1, 0, 1,-1000);
         GL11.glOrtho(0, (int)Ref.glRef.GetResolution().x, 0, (int)Ref.glRef.GetResolution().y, 1,-1000);
         GL11.glMatrixMode(GL11.GL_MODELVIEW);
         GL11.glLoadIdentity();
 //        Ref.textMan.Render();
         Ref.SpriteMan.DrawHUD();
         Ref.SpriteMan.Reset();
 
         // Queue HUD renders
         Ref.Console.Render();
         if(cl_showfps.iValue == 1)
             Ref.textMan.AddText(new Vector2f(Ref.glRef.GetResolution().x, 0), ""+currentFPS, Align.RIGHT, Type.HUD);
 //        Ref.textMan.Render();
         Ref.SpriteMan.DrawHUD();
         Ref.textMan.Render(); // Draw remaining text - shouldn't be any
         // Display frame
 //        Display.sync(60);
         Display.update();
 //        GL11.glGetError();
     }
 
     public void FlushMemory() {
         ShutdownAll();
 
         // if not running a server clear the whole hunk
         if(Ref.common.sv_running.iValue == 0) {
             Ref.common.HunkClear();
             // clear collision map data
             Ref.cm.ClearMap();
         }
         
     }
 
     public void ShutdownCGame() {
         Ref.Input.SetKeyCatcher(Ref.Input.GetKeyCatcher() & ~Input.KEYCATCH_CGAME);
         cgameStarted = false;
         if(Ref.cgame != null)
         {
             Ref.cgame.Shutdown();
             Ref.cgame = null;
         }
         
     }
 
     public String GetServerCommand(int commandNumber) {
         // if we have irretrievably lost a reliable command, drop the connection
         if(commandNumber <= clc.serverCommandSequence - 64) {
             Ref.common.Error(Common.ErrorCode.DROP, "GetServerCommand: A reliable command was cycled");
             return null;
         }
 
         if(commandNumber > clc.serverCommandSequence)
         {
             Ref.common.Error(Common.ErrorCode.DROP, "GetServerCommand: Requested command doesn't exist");
             return null;
         }
 
         String cmd = clc.serverCommands[commandNumber & 63];
         clc.lastExecutedServerCommand = commandNumber;
 
         Common.LogDebug("ServerCommand: " + cmd);
 
         String[] tokens = Commands.TokenizeString(cmd, false);
         if(tokens[0].equalsIgnoreCase("disconnect")) {
             // allow server to indicate why they were disconnected
             if(tokens.length > 1)
                 Ref.common.Error(Common.ErrorCode.SERVERDISCONNECT, "Server disconnected - " + Commands.ArgsFrom(tokens, 1));
             else
                 Ref.common.Error(Common.ErrorCode.SERVERDISCONNECT, "Server disconnected.");
         }
 
         if(tokens[0].equalsIgnoreCase("cs")) {
             cl.ConfigStringModified(cmd);
             return cmd;
         }
 
         if(tokens[0].equalsIgnoreCase("map_restart")) {
             cl.cmds = new PlayerInput[64];
             return cmd;
         }
 
         return cmd;
     }
 
     public PlayerInput GetUserCommand(int cmdNum) {
         if(cmdNum > cl.cmdNumber)
             Ref.common.Error(Common.ErrorCode.DROP, "GetUserCommand(): cmdnum > cl.cmdNumber");
 
         // the usercmd has been overwritten in the wrapping
         // buffer because it is too far out of date
         if(cmdNum <= cl.cmdNumber - 64)
             return null;
 
         return cl.cmds[cmdNum & 63];
     }
 
     /*
     ===================
     CL_DisconnectPacket
 
     Sometimes the server can drop the client and the netchan based
     disconnect can be lost.  If the client continues to send packets
     to the server, the server will send out of band disconnect packets
     to the client so it doesn't have to wait for the full timeout period.
     ===================
     */
     private void DisconnectPacket(InetSocketAddress endpoitn) {
         if(state < ConnectState.CONNECTING)
             return;
 
         // if not from our server, ignore it
         if(!endpoitn.equals(clc.ServerAddr))
             return;
 
         // if we have received packets within three seconds, ignore it
 	// (it might be a malicious spoof)
         // if ( cls.realtime - clc.lastPacketTime < 3000 ) {
         // TODO: If necessary
 
         // drop the connection
         Common.Log("Server disconnected for unknown reasons.");
         Ref.cvars.Set2("errorMessage", "Server disconnected for unknown reasons.", true);
         Disconnect(true);
     }
 
     private void ServerInfoPacket(InetSocketAddress from, String[] tokens) {
         String info = Commands.ArgsFrom(tokens, 1);
 
         try {
             // if this isn't the correct protocol version, ignore it
             int protocol = Integer.parseInt(Info.ValueForKey(info, "protocol"));
             if(protocol != Net.MAGIC_NUMBER) {
                 Common.Log("Different protocol info packet: " + protocol);
                 return;
             }
 
             for (int i= 0; i < cl_pinglist.length; i++) {
                 Ping ping = cl_pinglist[i];
                 if(ping.adr != null && ping.time <= 0 && from.equals(ping.adr)) {
                     // calc ping time
                     ping.time = Ref.common.Milliseconds() - ping.start;
                     Common.Log("Ping time " + ping.time + "ms from " + ping.adr);
 
                     // save of info
                     ping.info = info;
                     SetServerInfoByAddress(ping.adr, info, ping.time);
                     return;
                 }
             }
 
             // TODO: Ignore if not requsting LAN servers
             int i;
             for (i= 0; i < serversourceList.length; i++) {
                 if(serversourceList[i].adr == null)
                     break;
 
                 // avoid duplicate
                 if(serversourceList[i].adr.equals(from)) {
                     serversourceList[i].SetInfo(info, -1);
                     return;
                 }
             }
 
             if(i == serversourceList.length) {
                 Common.LogDebug("No room for more servers in the list");
                 return;
             }
 
             // add this to the list
 
             ServerInfo dest = serversourceList[max];
             
             dest.adr = from;
             dest.nClients = 0;
             dest.hostname = "";
             dest.mapname = "";
             dest.maxClients = 0;
             dest.ping = -1;
             dest.gametype = 0;
             dest.SetInfo(info, -1);
             if(serversource == ServerSource.LAN)
                 cl_nLocalServers++;
             else
                 cl_nGlobalServers++;
             
         } catch(NumberFormatException e) {
             Common.LogDebug("Failed parsing ServerInfo packet: " + e.getMessage());
         }
     }
 
     private void SetServerInfoByAddress(InetSocketAddress adr, String info, int ping) {
         for(ServerInfo si : cl_localServers) {
             if(si.adr != null && si.adr.equals(adr))
                 si.SetInfo(info, ping);
         }
 
         for(ServerInfo si : cl_globalServers) {
             if(si.adr != null && si.adr.equals(adr))
                 si.SetInfo(info, ping);
         }
     }
 
     private void ServerStatusResponse(InetSocketAddress from, String[] tokens) {
         ServerStatus serverStatus = null;
         for (int i = 0; i < cl_serverStatusList.length; i++) {
             if(cl_serverStatusList[i].adr != null && cl_serverStatusList[i].adr.equals(from)) {
                 serverStatus = cl_serverStatusList[i];
                 break;
             }
         }
 
         // if we didn't request this server status
         if(serverStatus == null)
             return;
 
         String s = Commands.ArgsFrom(tokens, 1);
         serverStatus.str = s;
         if(serverStatus.print) {
             System.out.println("Server settings:");
             // print cvars
             Map<String, String> vars = Info.GetPairs(s);
             for(String key : vars.keySet()) {
                 System.out.println(String.format(" %s \t: %s", key, vars.get(key)));
             }
         }
 
         serverStatus.time = Ref.common.Milliseconds();
         serverStatus.adr = from;
         serverStatus.pending = false;
         if(serverStatus.print)
             serverStatus.retrieved = true;
     }
 
     private void InitDownloads() {
         // ListenServer client doesn't need the map
         String map = Info.ValueForKey(cl.GameState.get(CS.CS_SERVERINFO), "mapname");
         if(Ref.common.sv_running.iValue == 0 && (map.equalsIgnoreCase("custom") || map.equalsIgnoreCase("data/custom") || !ResourceManager.FileExists(map)))
             clc.downloadList.add("map");
         if(clc.downloadList != null) {
             state = ConnectState.CONNECTED;
             clc.downloadName = null;
             NextDownload();
             return;
         }
 
         DownloadsComplete();
     }
 
     private void DownloadsComplete() {
         // notify server that we are done downloading
         if(clc.downloadRestart) {
             clc.downloadRestart = false;
 
             AddReliableCommand("donedl", false);
 
 //            return; //
         }
 
         // let the client game init and load data
         state = ConnectState.LOADING;
 
         // Pump the loop, this may change gamestate!
         Ref.common.EventLoop();
 
         // if the gamestate was changed by calling Com_EventLoop
         // then we loaded everything already and we don't want to do it again.
         if(state != ConnectState.LOADING)
             return;
 
         Ref.cvars.Set2("ui_fullscreen", "0", true);
         FlushMemory();
 
         // initialize the CGame
         cgameStarted = true;
         InitCGame();
 
         WritePacket();
         WritePacket();
         WritePacket();
 
         Ref.cvars.Set2("cl_paused", "0", true);
     }
 
     private void NextDownload() {
         if(clc.downloadName != null) {
             // finished downloading a file.. check checksum?
 
             clc.downloadName = null;
         }
 
         String dlStr = clc.downloadList.poll();
         if(dlStr != null) {
             BeginDownload(dlStr);
             clc.downloadRestart = true;
             return;
         }
 
         DownloadsComplete();
     }
 
     private void BeginDownload(String file) {
         Common.LogDebug("Begin download: " + file);
 
         clc.downloadName = file;
         clc.downloadBlock = 0;
         clc.downloadCount = 0;
         clc.downloadTime = realtime;
 
         AddReliableCommand("download " + file, false);
     }
 
     private void ParseDownload(NetBuffer buf) {
         if(clc.downloadName == null) {
             Common.Log("Recieving unrequested download");
             AddReliableCommand("stopdl", false);
             return;
         }
 
         int block = buf.ReadInt();
         if(block == 0) {
             // block zero is special, contains file size
             clc.downloadSize = buf.ReadInt();
 
             // size -1 is an errormessage
             if(clc.downloadSize < 0)
             {
                 Ref.common.Error(ErrorCode.DROP, buf.ReadString());
                 return;
             }
         }
 
         int size = buf.ReadInt();
         if(size < 0) {
             Ref.common.Error(ErrorCode.DROP, "ParseDownload: Invalid block " + block + ", got " + size);
             return;
         }
 
         byte[] data = new byte[size];
         buf.GetBuffer().get(data);
 
         if(clc.downloadBlock != block) {
             Common.Log("ParseDownload: Expected block " + clc.downloadBlock + ", got " + block);
             return;
         }
 
         // open the file if not opened yet
         if(clc.download == null) {
             clc.download = ByteBuffer.allocate(clc.downloadSize);
         }
 
         if(size > 0) {
             clc.download.put(data);
         }
 
         AddReliableCommand("nextdl " + clc.downloadBlock, false);
         clc.downloadBlock++;
 
         clc.downloadCount += size;
 
         if(size == 0) {
             // got EOF
             if(clc.download != null) {
                 try {
                     if(clc.downloadName.equalsIgnoreCase("map")) {
                         // load map
                         clc.download.limit(clc.download.position());
                         clc.download.flip();
                         clc.mapdata = NetBuffer.CreateCustom(clc.download);
                     } else
                     {
                         FileChannel chan = new FileOutputStream(clc.downloadName, false).getChannel();
                         clc.download.limit(clc.download.position());
                         clc.download.flip();
                         chan.write(clc.download);
                         chan.close();
                     }
                 } catch (IOException ex) {
                     Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                 }
 
 
                 clc.download = null;
             }
 
             // send intentions now
             // We need this because without it, we would hold the last nextdl and then start
             // loading right away.  If we take a while to load, the server is happily trying
             // to send us that last block over and over.
             // Write it twice to help make sure we acknowledge the download
             WritePacket();
             WritePacket();
 
             NextDownload();
         }
     }
 
     // Console commands
     private class cmd_Connect implements ICommand {
         public void RunCommand(String[] args) {
             if(args.length != 2)
             {
                 Common.Log("usage: connect <hostname[:ip]>");
                 return;
             }
 
             String server = args[1];
             clc.servermessage = "";
 
             if(Ref.common.sv_running.iValue == 1 && !server.equalsIgnoreCase("localhost")) {
                 // If running a server, shut it down
                 Ref.server.Shutdown("Server quit");
             }
 
             // Make sure the local server is killed
             Ref.cvars.Set2("sv_killserver", "1", true);
             Ref.server.Frame(0);
 
             Disconnect(true);
             Ref.Console.Close();
 
             
 
             InetSocketAddress endp = Ref.net.LookupHost(server);
             if(endp == null || endp.getAddress() == null) {
                 Common.Log("Connect failed: Could not lookup hostname");
                 state = ConnectState.DISCONNECTED;
                 return;
             }
 
             if(endp.getHostName() != null)
             Common.Log(String.format("%s resolved to %s", endp.getHostName(), endp.getAddress().getHostAddress()));
 
 
 
             servername = server;
             clc.ServerAddr = endp;
             state = ConnectState.CONNECTING;
             clc.challenge = Ref.rnd.nextInt(9999999);
             clc.ConnectTime = -99999;
             clc.ConnectPacketCount = 0;
             Ref.Input.SetKeyCatcher(Input.KEYCATCH_NONE);
         }
     }
 
     // Console commands
     private class cmd_Disconnect implements ICommand {
         public void RunCommand(String[] args) {
             if(state != ConnectState.DISCONNECTED)
                 Ref.common.Error(Common.ErrorCode.DISCONNECT, "Disconnected from server");
         }
     }
 
     // Console commands
     private class cmd_LocalServers implements ICommand {
         public void RunCommand(String[] args) {
             Common.LogDebug("Scanning for local servers on the network...");
 
             // reset the list, waiting for response+
             serversourceList = cl_localServers;
             max = 0;
             serversource = ServerSource.LAN;
             cl_nLocalServers = 0;
             for (int i= 0; i < cl_localServers.length; i++) {
                 boolean b = cl_localServers[i].visible;
                 cl_localServers[i] = new ServerInfo();
                 cl_localServers[i].visible = b;
             }
 
             String msg = "getinfo";
 
             // send each message twice in case one is dropped
             for (int i= 0; i < 2; i++) {
                 // send a broadcast packet on each server port
 		// we support multiple server ports so a single machine
 		// can nicely run multiple servers
                 for (int j= 0; j < 5; j++) {
 
                     InetSocketAddress to = new InetSocketAddress(Ref.net.GetBroadcastAddress(), Net.DEFAULT_PORT + j);
                     Ref.net.SendOutOfBandPacket(NetSource.CLIENT, to, msg);
                 }
             }
         }
     }
 
     private class cmd_InternetServers implements ICommand {
         public void RunCommand(String[] args) {
             Common.LogDebug("Scanning for global servers...");
 
             // reset the list, waiting for response
             serversourceList = cl_globalServers;
             max = 0;
             serversource = ServerSource.INTERNET;
             cl_nGlobalServers = 0;
             for (int i= 0; i < cl_globalServers.length; i++) {
 //                boolean b = cl_globalServers[i].visible;
                 cl_globalServers[i] = new ServerInfo();
 //                cl_globalServers[i].visible = b;
             }
 
             InetSocketAddress[] adr = MasterServer.getServerList();
             for (InetSocketAddress inetSocketAddress : adr) {
                 cl_globalServers[cl_nGlobalServers].adr = inetSocketAddress;
                 cl_nGlobalServers++;
             }
 
             updateServerPinging(ServerSource.INTERNET);
 //            String msg = "getinfo";
 //
 //            // send each message twice in case one is dropped
 //            for (int i= 0; i < 2; i++) {
 //                // send a broadcast packet on each server port
 //		// we support multiple server ports so a single machine
 //		// can nicely run multiple servers
 //                for (int j= 0; j < 5; j++) {
 //
 //                    InetSocketAddress to = new InetSocketAddress(Ref.net.GetBroadcastAddress(), Net.DEFAULT_PORT + j);
 //                    Ref.net.SendOutOfBandPacket(NetSource.CLIENT, to, msg);
 //                }
 //            }
         }
     }
 
     // Console commands
     private class cmd_Cmd implements ICommand {
         public void RunCommand(String[] args) {
             if(state != ConnectState.ACTIVE) {
                 Common.Log("Not connected to a server.");
                 return;
             }
 
             // don't forward the first argument
             if(args.length > 1) {
                 AddReliableCommand(Commands.Args(args), false);
             }
         }
     }
 
     // Returns the messagenum and servertime from the current snapshot
     public AbstractMap.SimpleEntry<Integer, Integer> GetCurrentSnapshotNumber() {
         return new AbstractMap.SimpleEntry<Integer, Integer>(cl.snap.messagenum, cl.snap.serverTime);
     }
 
     public boolean GetSnapshot(int snapNumber, Snapshot dest) {
         if(snapNumber > cl.snap.messagenum)
             Ref.common.Error(Common.ErrorCode.DROP, "GetSnapshot: snapNumber > cl.snap.messagenum");
 
         // if the frame has fallen out of the circular buffer, we can't return it
         if(cl.snap.messagenum - snapNumber >= 32)
             return false;
 
         // if the frame is not valid, we can't return it
         CLSnapshot clSnap = cl.snapshots[snapNumber & 31];
         if(!clSnap.valid)
             return false;
 
         // if the entities in the frame have fallen out of their
 	// circular buffer, we can't return it
         // TODO: Checkup on limit
         if(cl.parseEntitiesNum - clSnap.parseEntitiesNum >= Common.MAX_GENTITIES)
             return false;
 
         // write the snapshot
 //        if(clSnap.snapFlag != 0) {
 //            System.out.println("SnapFlags: " + clSnap.snapFlag);
 //        }
         dest.snapFlags = clSnap.snapFlag;
         dest.serverCommandSequence = clSnap.serverCommandSequence;
         dest.ping = clSnap.ping;
         dest.serverTime = clSnap.serverTime;
         dest.ps = clSnap.ps;
         dest.numEntities = clSnap.numEntities;
         if(dest.numEntities > Snapshot.MAX_ENTITIES_IN_SNAPSHOT) {
             Common.LogDebug(String.format("GetSnapshot(): truncated %d entities to %d", dest.numEntities, Snapshot.MAX_ENTITIES_IN_SNAPSHOT));
             dest.numEntities = Snapshot.MAX_ENTITIES_IN_SNAPSHOT;
         }
         for (int i= 0; i < dest.numEntities; i++) {
             dest.entities[i] = cl.parseEntities[(clSnap.parseEntitiesNum + i) & Common.ENTITYNUM_NONE];
         }
         
         // FIXME: configstring changes and server commands!!!
 
         return true;
     }
 }
