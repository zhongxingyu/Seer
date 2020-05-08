 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package cubetech.server;
 
 import cubetech.Game.Game;
 import cubetech.Game.GameClient;
 import cubetech.client.CLSnapshot;
 import cubetech.collision.BlockModel;
 import cubetech.collision.ClipmapException;
 import cubetech.collision.CollisionResult;
 import cubetech.common.CS;
 import cubetech.common.CVar;
 import cubetech.common.CVarFlags;
 import cubetech.common.Commands;
 import cubetech.common.Common;
 import cubetech.common.Content;
 import cubetech.common.Helper;
 import cubetech.common.ICommand;
 import cubetech.common.ITrace;
 import cubetech.common.Info;
 import cubetech.common.PlayerState;
 import cubetech.entities.EntityShared;
 import cubetech.entities.EntityState;
 import cubetech.entities.SharedEntity;
 import cubetech.entities.SvEntity;
 import cubetech.gfx.ResourceManager;
 import cubetech.input.PlayerInput;
 import cubetech.misc.MasterServer;
 import cubetech.misc.Ref;
 import cubetech.net.Net;
 import cubetech.net.NetChan;
 import cubetech.net.NetChan.NetSource;
 import cubetech.net.Packet;
 import cubetech.server.ServerRun.ServerState;
 import cubetech.server.SvClient.ClientState;
 import cubetech.spatial.SectorQuery;
 import cubetech.spatial.WorldSector;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.HashMap;
 import org.lwjgl.util.vector.Quaternion;
 
 
 import org.lwjgl.util.vector.Vector3f;
 
 /**
  *
  * @author mads
  */
 public class Server implements ITrace {
 
 
     private boolean initialized = false;
     int timeResidual;
     int time;
 
     public ServerRun sv = new ServerRun();
 
     int numSnapshotEntities;
     int nextSnapshotEntities;
     SvClient[] clients;
     EntityState[] snapshotEntities;
     int snapFlagServerBit = 0;
     WorldSector sv_worldSector = null;
     Challenge[] challenges = new Challenge[128];
     
     CVar sv_fps;
     CVar sv_timeout;
     CVar sv_hostname;
     CVar sv_mapname;
     CVar sv_maxclients;
     CVar sv_zombietime;
     CVar sv_killserver;
     CVar sv_gametype;
     CVar sv_serverid;
 //    CVar sv_mapChecksum;
     CVar sv_lan;
 
     public HashMap<String, ICommand> ucmds = new HashMap<String, ICommand>();
 
     public Server() {
         Init();
     }
     
     public PlayerState GameClientNum(int id) {
         return sv.gameClients[id].ps;
     }
 
     public PlayerInput GetUserCommand(int index) {
         return clients[index].lastUserCmd;
     }
 
     SvEntity SvEntityForGentity(SharedEntity ent) {
         if(ent == null || ent.s.ClientNum < 0 || ent.s.ClientNum >= Common.MAX_GENTITIES)
             Ref.common.Error(Common.ErrorCode.DROP, "SvEntityForGentity invalid");
         return sv.svEntities[ent.s.ClientNum];
     }
 
     public String GetUserInfo(int id) {
         return clients[id].userinfo;
     }
 
     
     public void Frame(int msec) {
         if(sv_killserver.iValue > 0) {
             Shutdown("Server was killed");
             Ref.cvars.Set2("sv_killserver", "0", true);
             return;
         }
 
         if(Ref.common.sv_running.iValue == 0)
             return; // no map loaded
 
         // allow pause if only the local client is connected
         if(CheckPaused())
             return;
 
         int frameMsec = (int)(1000f / sv_fps.fValue * Ref.common.com_timescale.fValue);
         if(frameMsec < 1) {
             Ref.cvars.Set2("timescale", ""+ sv_fps.fValue / 1000f, true);
             frameMsec = 1;
         }
 
         timeResidual += msec;
 
         if(sv.restartTime > 0 && sv.time >= sv.restartTime) {
             sv.restartTime = 0;
             Ref.commands.AddText("map_restart 0\n");
             return;
         }
 
         // update infostrings if anything has been changed
         if(Ref.cvars.modifiedFlags.contains(CVarFlags.SERVER_INFO)) {
             SetConfigString(CS.CS_SERVERINFO, Ref.cvars.InfoString(CVarFlags.SERVER_INFO));
             Ref.cvars.modifiedFlags.remove(CVarFlags.SERVER_INFO);
         }
         if(Ref.cvars.modifiedFlags.contains(CVarFlags.SYSTEM_INFO)) {
             SetConfigString(CS.CS_SYSTEMINFO, Ref.cvars.InfoString(CVarFlags.SYSTEM_INFO));
             Ref.cvars.modifiedFlags.remove(CVarFlags.SYSTEM_INFO);
         }
 
         // update ping based on the all received frames
         for (int i= 0; i < clients.length; i++) {
             if(clients[i].state == ClientState.FREE || clients[i].state == ClientState.ZOMBIE)
                 continue;
             clients[i].UpdatePing();
         }
 
         while(timeResidual >= frameMsec) {
             timeResidual -= frameMsec;
             time += frameMsec;
             sv.time += frameMsec;
 
             // Game run frame
             Ref.game.RunFrame(sv.time);
         }
 
         CheckTimeouts();
 
         // Let the clients send their messages
         for (int i= 0; i < clients.length; i++) {
             clients[i].SendClientMessage();
         }
 
         if(sv_lan.iValue == 0)
             MasterServer.autohandleHeartbeat(Ref.common.Milliseconds());
     }
 
     public boolean CheckPaused() {
         if(Ref.common.cl_paused.iValue == 0)
             return false;
 
         // only pause if there is just a single client connected
         int count = 0;
         SvClient cl;
         for (int i= 0; i < clients.length; i++) {
             cl = clients[i];
             if(cl.state != ClientState.FREE && cl.state != ClientState.ZOMBIE)
                 count++;
         }
         
         if(count > 1) {
             // dont pause
             if(Ref.common.sv_paused.iValue == 1)
                 Ref.cvars.Set2("sv_paused", "0", true);
             return false;
         }
 
         if(Ref.common.sv_paused.iValue == 0)
             Ref.cvars.Set2("sv_paused", "1", true);
         return true;
     }
     
     public void Init() {
         AddOperatorCommands();
         //Ref.cvars.Get("fraglimit", "20", CVarFlags.SERVER_INFO);
         //Ref.cvars.Get("timelimit", "0", CVarFlags.SERVER_INFO);
         sv_fps = Ref.cvars.Get("sv_fps", "60", EnumSet.of(CVarFlags.TEMP));
         sv_fps.Min = 10;
         sv_timeout = Ref.cvars.Get("sv_timeout", "200", EnumSet.of(CVarFlags.TEMP));
         sv_hostname = Ref.cvars.Get("sv_hostname", "CubeTech Server", EnumSet.of(CVarFlags.SERVER_INFO, CVarFlags.ARCHIVE));
         sv_mapname = Ref.cvars.Get("mapname", "nomap", EnumSet.of(CVarFlags.SERVER_INFO));
         sv_lan = Ref.cvars.Get("sv_lan", "1", EnumSet.of(CVarFlags.SERVER_INFO, CVarFlags.ARCHIVE));
         Ref.cvars.Get("nextmap", "", EnumSet.of(CVarFlags.TEMP));
         sv_maxclients = Ref.cvars.Get("sv_maxclients", "32", EnumSet.of(CVarFlags.SERVER_INFO, CVarFlags.LATCH));
         sv_zombietime = Ref.cvars.Get("sv_zombietime", "2", EnumSet.of(CVarFlags.TEMP));
         sv_killserver = Ref.cvars.Get("sv_killserver", "0", EnumSet.of(CVarFlags.TEMP));
         sv_gametype = Ref.cvars.Get("g_gametype", "0", EnumSet.of(CVarFlags.SERVER_INFO, CVarFlags.LATCH));
         sv_serverid = Ref.cvars.Get("sv_serverid", "0", EnumSet.of(CVarFlags.SYSTEM_INFO,CVarFlags.ROM));
 //        sv_mapChecksum = Ref.cvars.Get("sv_mapChecksum", "", EnumSet.of(CVarFlags.ROM));
         for (int i= 0; i < challenges.length; i++) {
             challenges[i] = new Challenge();
         }
     }
 
     
 
     private void SpawnServer(String mapname) {
         // shut down the existing game if it is running
         ShutdownGameProgs();
 
         Common.Log("Server initializating...");
         Common.Log("Map: " + mapname);
         
         
         // Notice the client system
         Ref.client.MapLoading();
 
         // make sure all the client stuff is unloaded
         Ref.client.ShutdownAll();
 
         Ref.common.HunkClear();
         
 
         Ref.cm.ClearCubeMap();
 
         // init client structures and numSnapshotEntities
         if(Ref.cvars.Find("sv_running").iValue == 0) {
             Ref.cvars.Get("sv_maxclients", "8", EnumSet.of(CVarFlags.NONE));
             Startup();
             sv_maxclients.modified = false;
         } else if(sv_maxclients.modified)
             ChangeMaxClients();
 
         // allocate the snapshot entities
         snapshotEntities = new EntityState[numSnapshotEntities];
         for (int i= 0; i < numSnapshotEntities; i++) {
             snapshotEntities[i] = new EntityState();
         }
         nextSnapshotEntities = 0;
 
         // toggle the server bit so clients can detect that a
         // server has changed
         snapFlagServerBit ^= CLSnapshot.SF_SERVERCOUNT; // Diff them
 
         Ref.cvars.Set2("nextmap", "map_restart 0", true);
 
         for(SvClient client : clients) {
             // save when the server started for each client already connected
             if(client.state == SvClient.ClientState.CONNECTED ||
                 client.state == SvClient.ClientState.PRIMED ||
                 client.state == SvClient.ClientState.ACTIVE) {
                 client.oldServerTime = sv.time;
             }
         }
 
         // wipe the entire per-level structure
         ClearServer();
         sv.configstrings.clear();
         Ref.cvars.Set2("cl_paused", "0", true);
         Ref.cm.GenerateCubeMap(System.currentTimeMillis());
 //        try {
 //            Ref.cm.LoadBlockMap(mapname, false);
 //        } catch (ClipmapException ex) {
 //            Ref.common.Error(Common.ErrorCode.DROP, Common.getExceptionString(ex));
 //        }
         Ref.cvars.Set2("mapname", mapname, true);
 //        Ref.cvars.Set2("sv_mapChecksum", ""+Ref.cm.cm.checksum, true);
 
         // serverid should be different each time
         sv.serverid = Ref.common.frametime;
         sv.restartedServerId = sv.serverid;
         Ref.cvars.Set2("sv_serverid", ""+sv.serverid, true);
 
         ClearWorld();
 
         sv.state = ServerRun.ServerState.LOADING;
         Ref.game = new Game();
         InitGame(false);
         sv_gametype.modified = false;
 
         // run a few frames to allow everything to settle
         for (int i= 0; i < 3; i++) {
             Ref.game.RunFrame(sv.time);
             sv.time += 100;
             time += 100;
         }
 
         // create a baseline for more efficient communications
         CreateBaseline();
 
         for (int i= 0; i < clients.length; i++) {
             SvClient client = clients[i];
             // send the new gamestate to all connected clients
             if(client.state == SvClient.ClientState.CONNECTED ||
                 client.state == SvClient.ClientState.PRIMED ||
                 client.state == SvClient.ClientState.ACTIVE) {
                 // connect the client again
                 String denied = Ref.game.Client_Connect(i, false);
                 if(denied != null) {
                     // this generally shouldn't happen, because the client
                     // was connected before the level change
                     client.DropClient(denied);
                 } else {
                     // when we get the next packet from a connected client,
                     // the new gamestate will be sent
                     client.state = ClientState.CONNECTED;
                 }
             }
         }
 
         // run another frame to allow things to look at all the players
         Ref.game.RunFrame(sv.time);
         sv.time += 100;
         time += 100;
 
         // save systeminfo and serverinfo strings
         String systemInfo = Ref.cvars.InfoString(CVarFlags.SYSTEM_INFO);
         Ref.cvars.modifiedFlags.remove(CVarFlags.SYSTEM_INFO);
         SetConfigString(CS.CS_SYSTEMINFO, systemInfo);
         SetConfigString(CS.CS_SERVERINFO, Ref.cvars.InfoString(CVarFlags.SERVER_INFO));
         Ref.cvars.modifiedFlags.remove(CVarFlags.SERVER_INFO);
 
         sv.state = ServerRun.ServerState.GAME;
     }
 
     public void SetConfigString(int index, String p) {
         if(index < 0 || index >= 1025) {
             Common.Log("SetConfigString: Bad Index " + index);
             return;
         }
 
         if(p == null)
             p = "";
 
         // don't bother broadcasting an update if no change
         if(sv.configstrings.containsKey(index) && sv.configstrings.get(index).equals(p))
             return;
 
         // change the string in sv
         sv.configstrings.put(index, p);
 
         // send it to all the clients if we aren't
         // spawning a new server
         if(sv.state == ServerState.GAME || sv.restarting) {
             // send the data to all relevent clients
             for (int i= 0; i < clients.length; i++) {
                 if(clients[i].state == ClientState.FREE || clients[i].state == ClientState.ZOMBIE)
                     continue;
 
                 SvClient client = clients[i];
                 if(client.state != SvClient.ClientState.ACTIVE) {
                     if(client.state == SvClient.ClientState.PRIMED)
                         client.csUpdated[index] = true;
                     continue;
                 }
 
                 // do not always send server info to all clients
                 if(index == CS.CS_SERVERINFO && client.gentity != null &&
                         client.gentity.r.svFlags.contains(SvFlags.NOSERVERINFO))
                     continue;
 
                 client.SendConfigString(index);
             }
         }
     }
 
     void SendServerCommand(SvClient cl, String str) {
         if(str.length() > 1022) {
             Common.Log("SendServerCommand: Str too long: " + str);
             return;
         }
         
         if(cl != null) {
             cl.AddServerCommand(str);
             return;
         }
 
         // send the data to all relevent clients
         for(SvClient client : clients) {
             client.AddServerCommand(str);
         }
     }
 
     public void DropClient(GameClient gc, String reason) {
         clients[gc.clientIndex].DropClient(reason);
     }
 
     /**
      * Handle incomming packet
      * @param data
      */
     public void PacketEvent(Packet data) {
         // Out Of Band packets is mostly for connecting
         if(data.OutOfBand) {
             ConnectionLessPacket(data);
             return;
         }
         
         // read the qport out of the message so we can fix up
         // stupid address translating routers
         data.buf.ReadInt();
         int qport = data.buf.ReadInt() & 0xffff;
 
         // find which client the message is from
         for (int i= 0; i < clients.length; i++) {
             SvClient cl = clients[i];
             if(cl.state == ClientState.FREE)
                 continue;
 
             if(!data.endpoitn.getAddress().equals(cl.netchan.addr.getAddress()))
                 continue;
 
             // it is possible to have multiple clients from a single IP
             // address, so they are differentiated by the qport variable
             if(cl.netchan.qport != qport)
                 continue;
 
             // the IP port can't be used to differentiate them, because
             // some address translating routers periodically change UDP
             // port assignments
             if(cl.netchan.addr.getPort() != data.endpoitn.getPort()) {
                 Common.Log("Fixing up translation port");
                 cl.netchan.addr = new InetSocketAddress(cl.netchan.addr.getAddress(), data.endpoitn.getPort());
             }
 
             // make sure it is a valid, in sequence packet
             //??
             // zombie clients still need to do the Netchan_Process
             // to make sure they don't need to retransmit the final
             // reliable message, but they don't do any other processing
             if(cl.state != ClientState.ZOMBIE) {
                 cl.lastPacketTime = time;
                 cl.ExecuteClientMessage(data.buf);
             }
             return;
         }
     }
 
     private void ConnectionLessPacket(Packet data) {
         String str = data.buf.ReadString();
         if(str == null)
             return;
 
         String[] tokens = Commands.TokenizeString(str, false);
 
         str = tokens[0];
         if(str.equalsIgnoreCase("getchallenge"))
             GetChallenge(data.endpoitn);
         else if(str.equalsIgnoreCase("connect"))
             DirectConnect(data.endpoitn, tokens);
         else if(str.equalsIgnoreCase("getstatus"))
             Status(data.endpoitn, tokens);
         else if(str.equalsIgnoreCase("getinfo"))
             Info(data.endpoitn, tokens);
         else if(str.equalsIgnoreCase("disconnect")) {
             // if a client starts up a local server, we may see some spurious
             // server disconnect messages when their new server sees our final
             // sequenced messages to the old client
         }
         else
             Common.LogDebug("bad connectionless packet from " + data.endpoitn + ": " + str);
     }
 
     private void CheckTimeouts() {
         int droppoint = time - 1000 * sv_timeout.iValue;
         int zombiepoint = time - 1000 * sv_zombietime.iValue;
         for (int i= 0; i < clients.length; i++) {
             SvClient cl = clients[i];
             if(cl.lastPacketTime > time) cl.lastPacketTime = time;
             cl.CheckTimeout(droppoint, zombiepoint);
         }
     }
 
 
     // Hook in some structures from Game
     public void LocateGameData(SharedEntity[] sentities, int num_entities, GameClient[] clients) {
         sv.gentities = sentities;
         sv.num_entities = num_entities;
         sv.gameClients = clients;
     }
 
     public void Shutdown(String string) {
         if(Ref.common.sv_running.iValue != 1)
             return;
 
         Common.Log("--- Server Shutdown (" + string + ") ---");
         
         if(clients.length > 0) {
             FinalMessage(string);
         }
 
         ClearServer();
         clients = new SvClient[64];
         for (int i= 0; i < clients.length; i++) {
             clients[i] = new SvClient();
             clients[i].id = i;
         }
 
         Ref.cvars.Set2("sv_running", "0", true);
 
         initialized = false;
 
         // disconnect local client
         if(sv_killserver.iValue != 2)
             Ref.client.Disconnect(false);
 
     }
 
     private void FinalMessage(String string) {
         if(sv.gameClients == null)
             return;
         for (int i= 0; i < clients.length; i++) {
             SvClient cl = clients[i];
             if(cl.state != ClientState.ZOMBIE && cl.state != ClientState.FREE)
             {
                 SendServerCommand(cl, String.format("print \"%s\n\"\n", string));
                 SendServerCommand(cl, String.format("disconnect \"%s\"", string));
                 cl.nextSnapshotTime = -1;
                 cl.SendClientSnapshot();
             }
         }
     }
 
     public void GameSendServerCommand(int i, String format) {
         if(i == -1)
             SendServerCommand(null, format);
         else if(i < 0 || i >= clients.length)
             return;
         else
             SendServerCommand(clients[i], format);
     }
     
     private void GetChallenge(InetSocketAddress from) {
         int oldestTime = Integer.MAX_VALUE;
         int oldest = 0;
         int existingChallenge = -1;
         // see if we already have a challenge for this ip
         for (int i= 0; i < challenges.length; i++) {
             Challenge chal = challenges[i];
             if(!chal.connected && from.equals(chal.addr)) {
                 existingChallenge = i;
                 break;
             }
 
             if(chal.time < oldestTime)
             {
                 oldestTime = chal.time;
                 oldest = i;
             }
         }
 
         // First time connection
         Challenge chal;
         if(existingChallenge == -1) {
             chal = challenges[oldest];
             chal.addr = from;
             chal.firstTime = time;
             chal.time = time;
             chal.connected = false;
         } else
             chal = challenges[existingChallenge];
 
         // always generate a new challenge number, so the client cannot circumvent sv_maxping
         chal.challenge = (Ref.rnd.nextInt() << 16) ^ Ref.rnd.nextInt() ^ time;
         chal.wasRefused = false;
         chal.pingTime = time;
         Ref.net.SendOutOfBandPacket(NetSource.SERVER, from, String.format("challengeResponse %d %d", chal.challenge, chal.clientChallenge));
     }
 
     private void DirectConnect(InetSocketAddress from, String[] tokens) {
         String userinfo = tokens[1];
         String chal = Info.ValueForKey(userinfo, "challenge");
         int challenge;
         int qport;
         try {
             challenge = Integer.parseInt(chal);
             chal = Info.ValueForKey(userinfo, "qport");
             qport = Integer.parseInt(chal);
         } catch (NumberFormatException e) {
             Common.Log("Malformed challenge in connection attempt");
             return;
         }
         String ip = from.getAddress().toString();
 
         userinfo = Info.SetValueForKey(userinfo, "ip", ip);
 
 
 
         int i;
         if(!ip.endsWith("127.0.0.1")) {
             for (i= 0; i < challenges.length; i++) {
                 if(from.equals(challenges[i].addr)) {
                     if(challenges[i].challenge == challenge)
                         break;
                     else
                         Common.LogDebug("Hey man");
                 }
             }
 
             // No challenge found
             if(i == challenges.length) {
                 Ref.net.SendOutOfBandPacket(NetSource.SERVER, from, String.format("print \"No or bad challenge for you address.\""));
                 return;
             }
 
             // Challenge found
             Challenge ch = challenges[i];
             if(ch.wasRefused) {
                 // Return silently, so that error messages written by the server keep being displayed.
                 Common.LogDebug("Silent refuse");
                 return;
             }
 
             int ping = time - ch.pingTime;
             Common.Log("Client connected with challenge ping of " + ping);
             ch.connected = true;
         }
 
         boolean gotcl = false;
         SvClient newcl = null;
         // if there is already a slot for this ip, reuse it
         for (i= 0; i < clients.length; i++) {
             SvClient cl = clients[i];
             if(cl.state == ClientState.FREE)
                 continue;
 
             if(from.getAddress().equals(cl.netchan.addr.getAddress()) &&
                     (from.getPort() == cl.netchan.addr.getPort() || qport == cl.netchan.qport)) {
                 Common.LogDebug("Reconnect...");
                 clients[i] = new SvClient();
                 clients[i].id = i;
                 newcl = clients[i];
                 gotcl = true;
                 break;
             }
         }
         if(!gotcl) {
             for (i= 0; i < clients.length; i++) {
                 
                 if(clients[i].state == ClientState.FREE) {
                     clients[i] = new SvClient(); // Create new to clear it out
                     clients[i].id = i;
                     newcl = clients[i];
                     break;
                 }
             }
 
 //            if(newcl == null && i < sv_maxclients.iValue) {
 //                // Create new
 //                newcl = new SvClient();
 //                clients.add(newcl);
 //                i = clients.size()-1;
 //                newcl.id = i;
 //            }
 
             if(newcl == null) {
                 Ref.net.SendOutOfBandPacket(NetSource.SERVER, from, String.format("print \"Server is full.\""));
                 Common.Log("Rejected connection: Server is full");
                 return;
             }
 
             // we got a newcl, so reset the reliableSequence and reliableAcknowledge
             
         }
             newcl.reliableAcknowledge = 0;
             newcl.reliableSequence = 0;
         // build a new connection
         // accept the new client
         // this is the only place a client_t is ever initialized
         SharedEntity ent = sv.gentities[i];
         newcl.gentity = ent;
 
         // save the address
         newcl.netchan = new NetChan(NetSource.SERVER, from, qport);
 
         // save the userinfo
         newcl.userinfo = userinfo;
 
         // get the game a chance to reject this connection or modify the userinfo
         String denied = Ref.game.Client_Connect(i, true);
         if(denied != null) {
             Ref.net.SendOutOfBandPacket(NetSource.SERVER, from, String.format("print \"%s\"", denied));
             Common.Log("Rejected a connection: " + denied);
             return;
         }
 
        // newcl.
         newcl.UserInfoChanged(true); // autofix playername
         
 
         // send the connect packet to the client
         Ref.net.SendOutOfBandPacket(NetSource.SERVER, from, "connectResponse");
         newcl.state = ClientState.CONNECTED;
         newcl.nextSnapshotTime = time;
         newcl.lastPacketTime = time;
         newcl.lastConnectTime = time;
 
         // when we receive the first packet from the client, we will
         // notice that it is from a different serverid and that the
         // gamestate message was not just sent, forcing a retransmit
         newcl.gamestateMessageNum = -1;
     }
 
     public void ShutdownGameProgs() {
         if(Ref.game != null)
         {
             Ref.game.ShutdownGame(false);
             Ref.game = null;
         }
     }
 
     // Clear and recreate worldsector
     private void ClearWorld() {
         Vector3f mins = new Vector3f(), maxs = new Vector3f();
         GetWorldBounds(mins, maxs);
         sv_worldSector = WorldSector.CreateWorldSector(mins, maxs, 2);
     }
 
     private void GetWorldBounds(Vector3f mins, Vector3f maxs) {
         // FIX - There is no world bounds! (well..maybe?)
         float max = 10000;
         maxs.set(max, max, max);
         mins.set(maxs);
         mins.scale(-1f);
     }
 
     public void LinkEntity(SharedEntity gent) {
         SvEntity ent = gent.GetSvEntity();
 
         if(ent.worldSector != null)
             UnlinkEntity(gent); // unlink from old position
 
         if(gent.r.bmodel)
             gent.s.solid = EntityState.SOLID_BMODEL;
         else if((gent.r.contents & (Content.BODY | Content.SOLID)) > 0) {
             int x = Helper.Clamp((int) gent.r.maxs.x, 1, 255);
             int y = Helper.Clamp((int) gent.r.maxs.y, 1, 255);
             int height = Helper.Clamp((int)(gent.r.maxs.z - gent.r.mins.z), 1, 255);
             int minzLenght = Helper.Clamp((int)(- gent.r.mins.z), 0, 127);
 
             gent.s.solid = (minzLenght<<24) | (height<<16) | (y<<8) | x;
         } else
             gent.s.solid = 0;
 
         Vector3f origin = gent.r.currentOrigin;
         Vector3f.add(origin, gent.r.mins, gent.r.absmin);
         Vector3f.add(origin, gent.r.maxs, gent.r.absmax);
 
         // because movement is clipped an epsilon away from an actual edge,
 	// we must fully check even when bounding boxes don't quite touch
         gent.r.absmin.x -= 1; gent.r.absmin.y -= 1;
         gent.r.absmax.x += 1; gent.r.absmax.y += 1;
         gent.r.absmax.z += 1; gent.r.absmin.z -= 1;
 
         gent.r.linkcount++;
 
         // find the first world sector node that the ent's box crosses
         // FIX FIX FIX
         WorldSector node = sv_worldSector.FindCrossingNode(gent.r.absmin, gent.r.absmax);
 
         // link it in
         node.LinkEntity(ent);
         gent.r.linked = true;
     }
 
     public void UnlinkEntity(SharedEntity gent) {
         gent.r.linked = false;
 
         // Get worldsector
         SvEntity ent = gent.GetSvEntity();
         if(ent.worldSector == null)
             return; // not linked
 
         // Unlink from worldsector
         if(!ent.worldSector.UnlinkEntity(ent)) {
             Common.Log("Warning: UnlinkEntity: Not found in worldsector");
         }
     }
 
 
     Vector3f boxmins = new Vector3f(), boxmaxs = new Vector3f();
     Vector3f delta = new Vector3f();
     public CollisionResult Trace(Vector3f start, Vector3f end, Vector3f mins, Vector3f maxs, int tracemask, int passEntityNum) {
         if(mins == null)
             mins = new Vector3f();
         if(maxs == null)
             maxs = new Vector3f();
 
         Vector3f.sub(end, start, delta);
         // clip to world
         CollisionResult worldResult;
         if((tracemask & Content.SOLID) == Content.SOLID) {
             worldResult = Ref.collision.traceCubeMap(start, delta, mins, maxs, true);
             if(worldResult.frac == 0.0f) return worldResult; // Blocked instantl by world
         } else {
             worldResult = Ref.collision.GetNext();
             worldResult.reset(start, delta, maxs);
         }
 
         // create the bounding box of the entire move
        float padding = -1f;
         for (int i= 0; i < 3; i++) {
             if(Helper.VectorGet(end, i) > Helper.VectorGet(start, i)) {
                 Helper.VectorSet(boxmins, i, Helper.VectorGet(start, i) + Helper.VectorGet(mins, i) - padding);
                 Helper.VectorSet(boxmaxs, i, Helper.VectorGet(end, i) + Helper.VectorGet(maxs, i) + padding);
             } else {
                 Helper.VectorSet(boxmins, i, Helper.VectorGet(end, i) + Helper.VectorGet(mins, i) - padding);
                 Helper.VectorSet(boxmaxs, i, Helper.VectorGet(start, i) + Helper.VectorGet(maxs, i) + padding);
             }
         }
 
         ClipMoveToEntities(worldResult, start, end, mins, maxs, boxmins, boxmaxs, tracemask, passEntityNum);
         return worldResult;
     }
 
     public SectorQuery EntitiesInBox(Vector3f mins, Vector3f maxs) {
         return sv_worldSector.AreaEntities(mins, maxs);
     }
 
     public boolean EntityContact(Vector3f mins, Vector3f maxs, SharedEntity gEnt) {
         // check for exact collision
         Vector3f origin = new Vector3f(gEnt.r.currentOrigin);
         // TODO: Take account for angles
 
         Vector3f halfSize = new Vector3f();
         Vector3f.sub(gEnt.r.maxs, gEnt.r.mins, halfSize);
         halfSize.scale(0.5f);
 //        Vector2f.sub(gEnt.r.maxs, delta, origin);
 
         Ref.collision.SetBoxModel(halfSize, origin); // FIX
         CollisionResult res = Ref.collision.TransformedBoxTrace(new Vector3f(), null, mins, maxs, -1);
 
         return res.startsolid;
     }
 
     private void ClipMoveToEntities(CollisionResult clip, Vector3f start, Vector3f end, Vector3f mins, Vector3f maxs,
         Vector3f boxmins, Vector3f boxmaxs, int tracemask, int passEntityNum) {
 
         SectorQuery entityList = sv_worldSector.AreaEntities(boxmins, boxmaxs); // FIX
 
         int passOwnerNum = -1;
         if(passEntityNum != Common.ENTITYNUM_NONE) {
             passOwnerNum = sv.gentities[passEntityNum].r.ownernum;
             if(passOwnerNum == Common.ENTITYNUM_NONE)
                 passOwnerNum = -1;
         }
 
         for (Integer entIndex : entityList.List) {
             // Starting solid, no need to check any further
             if(clip.startsolid) return;
 
             SharedEntity touch = sv.gentities[entIndex];
             // see if we should ignore this entity
             if(passEntityNum != Common.ENTITYNUM_NONE) {
                 if(entIndex == passEntityNum)
                     continue; // don't clip against pass entity
                 if(touch.r.ownernum == passEntityNum)
                     continue; // don't clip against own missiles
                 if(touch.r.ownernum == passOwnerNum)
                     continue; // don't clip against other missiles from our owner
             }
 
             // if it doesn't have any brushes of a type we
             // are looking for, ignore it
             if((touch.r.contents & tracemask) == 0)
                 continue;
 
             // might intersect, so do an exact clip
             ClipHandleForEntity(touch);
 
             CollisionResult res = Ref.collision.TransformedBoxTrace(start, end, mins, maxs, tracemask);
             if(res.frac  < clip.frac) {
                 clip.frac = res.frac;
                 clip.hit = res.hit;
                 clip.entitynum = touch.s.ClientNum;
                 clip.hitAxis = res.hitAxis;
                 clip.hitmask = res.hitmask;
 
                 if(res.frac < 0) {
                     res.frac = 0;
                 }
 
                 if(res.frac == 0f)
                     break;
             }
         }
     }
 
     public void SetBrushModel(SharedEntity shEnt, int index) {
         if(index <= 0)
             Ref.common.Error(Common.ErrorCode.DROP, "SetBrushModel: Invalid index " + index);
 
         shEnt.s.modelindex = index;
         int h = Ref.cm.cm.InlineModel(shEnt.s.modelindex);
 
         BlockModel model = Ref.cm.cm.getModel(h);
         model.attachEntity(shEnt.s.ClientNum);
         shEnt.r.mins.set(model.size.x, model.size.y);
         shEnt.r.mins.scale(-0.5f);
         shEnt.r.maxs.set(model.size.x, model.size.y);
         shEnt.r.maxs.scale(0.5f);
         shEnt.r.bmodel = true;
         shEnt.r.contents = 1;
         LinkEntity(shEnt);
     }
 
     /*
     ================
     SVC_Status
 
     Responds with all the info that qplug or qspy can see about the server
     and all connected players.  Used for getting detailed information after
     the simple info query.
     ================
     */
     private void Status(InetSocketAddress from, String[] tokens) {
         String info = Ref.cvars.InfoString(CVarFlags.SERVER_INFO);
 
         // echo back the parameter to status. so master servers can use it as a challenge
 	// to prevent timed spoofed reply packets that add ghost servers
         info = Info.SetValueForKey(info, "challenge", Commands.ArgsFrom(tokens, 1));
         StringBuilder status = new StringBuilder();
         for (int i= 0; i < clients.length; i++) {
             SvClient cl = clients[i];
             if(cl.state != ClientState.FREE && cl.state != ClientState.ZOMBIE)
             {
                 PlayerState ps = GameClientNum(i);
                 // TODO: Get score from ps
                 String player = String.format("%d %d \"%s\"\n", 0, cl.ping, cl.name);
                 status.append(player);
             }
         }
 
         Ref.net.SendOutOfBandPacket(NetSource.SERVER, from, String.format("statusResponse\n%s\n%s", info, status));
     }
 
     /*
     ================
     SVC_Info
 
     Responds with a short info message that should be enough to determine
     if a user is interested in a server to do a full status
     ================
     */
     private void Info(InetSocketAddress from, String[] tokens) {
         int playercount = 0;
         for(SvClient cl : clients) {
             if(cl.state == ClientState.FREE || cl.state == ClientState.ZOMBIE)
                 continue;
             playercount++;
         }
         
         String arg = Commands.ArgsFrom(tokens, 1);
         if(arg.length() > 128) {
             Common.Log("SV: Ignored CL info packet, huge arg: " + arg);
             return;
         }
 
         String info = "";
         // echo back the parameter to status. so servers can use it as a challenge
 	// to prevent timed spoofed reply packets that add ghost servers
         info = Info.SetValueForKey(info, "challenge", arg);
         info = Info.SetValueForKey(info, "protocol", ""+Net.MAGIC_NUMBER);
         info = Info.SetValueForKey(info, "hostname", sv_hostname.sValue);
         info = Info.SetValueForKey(info, "mapname", sv_mapname.sValue);
         info = Info.SetValueForKey(info, "clients", ""+playercount);
         info = Info.SetValueForKey(info, "sv_maxclients", ""+sv_maxclients.iValue);
         info = Info.SetValueForKey(info, "gametype", ""+sv_gametype.iValue);
 
         Ref.net.SendOutOfBandPacket(NetSource.SERVER, from, String.format("infoResponse\n%s", info));
     }
 
     // Inits some structures on the first mapload
     private void Startup() {
         if(initialized) {
             Ref.common.Error(Common.ErrorCode.FATAL, "Server.Startup(): Already initialized.");
         }
 
         clients = new SvClient[64];
         for (int i= 0; i < clients.length; i++) {
             clients[i] = new SvClient();
             clients[i].id = i;
         }
         numSnapshotEntities = 2048;
         initialized = true;
         Ref.cvars.Set2("sv_running", "1", true);
     }
 
     // Updates structures during mapchange
     private void ChangeMaxClients() {
         // get the highest client number in use
         int count = 0;
         for (int i = 0; i < clients.length; i++)
         {
             if (clients[i].state == ClientState.CONNECTED ||
                     clients[i].state == ClientState.PRIMED ||
                     clients[i].state == ClientState.ACTIVE)
             {
                 if (i > count)
                     count = i;
             }
         }
         count++;
 
         int oldmaxClients = sv_maxclients.iValue;
         BoundMaxClients(count);
         if (sv_maxclients.iValue == oldmaxClients)
             return;
 
 //        ArrayList<SvClient> toremove = new ArrayList<SvClient>();
 //        for (SvClient cl : clients)
 //        {
 //            if (cl.state == ClientState.ZOMBIE || cl.state == ClientState.FREE)
 //                toremove.add(cl);
 //        }
 //        for (SvClient cl : toremove)
 //            clients.remove(cl);
 
         numSnapshotEntities = sv_maxclients.iValue * 32 * 64;
     }
 
     // Set max players
     private void BoundMaxClients(int count) {
         Ref.cvars.Get("sv_maxclients", "8", EnumSet.of(CVarFlags.NONE));
 
         sv_maxclients.modified = false;
 
         if(sv_maxclients.iValue < count)
             Ref.cvars.Set2("sv_maxclients", ""+count, true);
         else
             Ref.cvars.Set2("sv_maxclients", "32", true);
     }
 
     // Clear server for new map
     private void ClearServer() {
         sv = new ServerRun();
         sv.configstrings = new HashMap<Integer, String>();
     }
 
     // Initialize the game system
     private void InitGame(boolean restart) {
         for (int i= 0; i < clients.length; i++) {
             clients[i].gentity = null;
         }
         Ref.game.Init(sv.time, Ref.common.Milliseconds(), restart);
     }
 
     // Create baseline entities. They will be used as the inital delta
     // source, when initally comming into view
     private void CreateBaseline() {
         for (int i= 0; i < sv.num_entities; i++) {
             SharedEntity ent = sv.gentities[i];
             if(!ent.r.linked)
                 continue;
 
             ent.s.ClientNum = i;
             ent.s.Clone(sv.svEntities[i].baseline);
         }
     }
 
     // Init server commands
     private void AddOperatorCommands() {
         Ref.commands.AddCommand("map", new ICommand() {
             public void RunCommand(String[] args) {
                 cmd_Map(args);
             }
         });
     }
 
     private void cmd_Map(String[] tokens) {
         if(tokens.length < 2) {
             Common.Log("usage: map <mapname>");
             return;
         }
 
         // Check if map exists before changing
         if(!tokens[1].equals("newmap") && !ResourceManager.FileExists(tokens[1])) {
             Common.Log("Cannot find map: " + tokens[1]);
             return;
         }
 
 
         Ref.cvars.Get("g_gametype", "0", EnumSet.of(CVarFlags.LATCH, CVarFlags.SERVER_INFO, CVarFlags.USER_INFO));
         SpawnServer(tokens[1]);
     }
 
     private void ClipHandleForEntity(SharedEntity touch) {
         if(touch.r.bmodel) {
             Ref.collision.SetSubModel(touch.s.modelindex, touch.r.currentOrigin);
         } else {
             
             Ref.collision.SetBoxModel(touch.r.mins, touch.r.maxs, touch.r.currentOrigin);
         }
     }
 
 
     
 }
