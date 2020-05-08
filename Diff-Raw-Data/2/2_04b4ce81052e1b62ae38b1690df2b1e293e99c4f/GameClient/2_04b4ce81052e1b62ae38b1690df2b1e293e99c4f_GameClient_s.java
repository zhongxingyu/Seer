 package cubetech.Game;
 
 import cubetech.Block;
 import cubetech.common.CS;
 import cubetech.common.Commands;
 import cubetech.common.Common;
 import cubetech.common.Common.ErrorCode;
 import cubetech.common.Content;
 import cubetech.common.Info;
 import cubetech.common.Move;
 import cubetech.common.Move.MoveType;
 import cubetech.common.MoveQuery;
 import cubetech.common.PlayerState;
 import cubetech.entities.EntityFlags;
 import cubetech.entities.EntityType;
 import cubetech.entities.Event;
 import cubetech.input.PlayerInput;
 import cubetech.misc.Ref;
 import cubetech.spatial.SectorQuery;
 import java.util.ArrayList;
 import org.lwjgl.util.vector.Vector2f;
 
 
 /**
  * this structure is cleared on each ClientSpawn(),
  * except for 'client->pers' and 'client->sess'
  * @author mads
  */
 public class GameClient extends Gentity {
     public PlayerState ps = new PlayerState();  // communicated by server to clients
 
     // the rest of the structure is private to game
     public ClientPersistant pers = new ClientPersistant();
     public int clientIndex = -1;
     public int lastCmdTime;
 
     // timers
     public int inactivityTime; // for kicking afk players
     public int respawnTime;
     private int timeResidual;
 
     /*
     ================
     ClientEvents
 
     Events will be passed on to the clients for presentation,
     but any server game effects are handled here
     ================
     */
     void HandleEvents(int oldEventSequence) {
         if(oldEventSequence < ps.eventSequence - Common.MAX_PS_EVENTS)
             oldEventSequence = ps.eventSequence - Common.MAX_PS_EVENTS;
 
         for(int i = oldEventSequence; i < ps.eventSequence; i++) {
             int event = ps.events[i & (Common.MAX_PS_EVENTS-1)];
 
             switch(event) {
                 case Event.FOOTSTEP:
                 case Event.STEP:
                 case Event.DIED:
                 case Event.HIT_WALL:
                 case Event.JUMP:
                     break;
 
                 default:
                     //System.out.println("Unhandled GClient event.");
                     break;
             }
             
         }
     }
 
     /**
      * Updates the scoreboard for this client
      */
     public void ScoreboardMessage() {
         StringBuilder str = new StringBuilder();
         int nClients = 0;
         for (int i= 0; i < Ref.game.level.clients.length; i++) {
             GameClient cl = Ref.game.level.clients[i];
             if(cl == null)
                 continue;
             if(cl.pers.connected == ClientPersistant.ClientConnected.DISCONNECTED)
                 continue;
 
             int ping = -1;
             if(cl.pers.connected == ClientPersistant.ClientConnected.CONNECTED)
                 ping = cl.ps.ping;
 
             nClients++;
             str.append(String.format(" %d %d %d", i, ping, (Ref.game.level.time - cl.pers.JoinTime)/60000));
         }
 
         Ref.server.GameSendServerCommand(s.ClientNum, String.format("scores %d %s", nClients, str));
     }
 
     // Handle incomming commands from the client
     public void Client_Command(String[] tokens) {
         String cmd = tokens[0].toLowerCase();
         if(cmd.equalsIgnoreCase("say"))
             cmd_Say(tokens, false);
         else if(cmd.equalsIgnoreCase("score"))
             cmd_Score();
         else if(cmd.equalsIgnoreCase("block")) {
             cmd_Block(tokens);
         }
     }
 
     private void cmd_Score() {
         ScoreboardMessage();
     }
 
     private void cmd_Say(String[] tokens, boolean b) {
         String text;
         if(b)
             text = Commands.ArgsFrom(tokens, 0);
         else
             text = Commands.ArgsFrom(tokens, 1);
 
         Ref.game.Say(this, text);
     }
 
     // Client want's to modify the map
     private void cmd_Block(String[] tokens) {
         if(!Ref.game.level.editmode) {
             // not in edit mode
             return;
         }
 
         if(ps.moveType != MoveType.EDITMODE) {
             Common.LogDebug("Player not in EDITMODE :S");
             return;
         }
 
         if(tokens.length < 2)
             return;
 
         String bCmd = tokens[1];
         if(bCmd.equalsIgnoreCase("add")) {
             Vector2f position = ps.origin;
             Block b = Ref.cm.cm.AddBlock();
             b.SetCentered(position, new Vector2f(8, 8));
             Ref.game.SendBlock(b);
         }
     }
 
     private boolean ClientInactivityTimer() {
 //        if(cl.lastCmdTime != 0 && level.time - cl.lastCmdTime > 10000) {
 //            if(!cl.pers.LocalClient)
 //                Ref.server.DropClient(cl, "Lost connection to player");
 //            return false;
 //        }
         return true;
     }
 
     public void ClientUserInfoChanged() {
         String info = Ref.server.GetUserInfo(clientIndex);
 
         // check for local client
         if(Info.ValueForKey(info, "ip").equals("localhost"))
             pers.LocalClient = true;
 
         // set name
         String oldname = pers.Name;
         pers.Name = Info.ValueForKey(info, "name");
 
         if(pers.connected == ClientPersistant.ClientConnected.CONNECTED && !oldname.equals(pers.Name)) {
             Ref.server.GameSendServerCommand(-1, String.format("print \"%s changed name to %s\n\"", oldname, pers.Name));
         }
 
         String str = String.format("n\\%s",pers.Name);
         Ref.server.SetConfigString(CS.CS_PLAYERS+clientIndex, str);
     }
 
     public void Begin() {
         if(r.linked)
             Unlink();
 
         Init(s.ClientNum);
 
         // locate ent at a spawn point
         ClientSpawn();
 
         pers.connected = ClientPersistant.ClientConnected.CONNECTED;
         pers.JoinTime = Ref.game.level.time;
         
         // save eflags around this, because changing teams will
         // cause this to happen with a valid entity, and we
         // want to make sure the teleport bit is set right
         // so the viewpoint doesn't interpolate through the
         // world to the new position
         int entFlags = ps.eFlags;
         ps = new PlayerState();
         ps.eFlags = entFlags;
         ps.clientNum = s.ClientNum;
 
         Ref.server.LocateGameData(Ref.game.level.sentities, Ref.game.level.num_entities, Ref.game.level.clients);
         Ref.server.GameSendServerCommand(-1, String.format("print \"%s entered the game.\"\n", pers.Name));
         Ref.game.CalculateRanks();
     }
 
     /**
      * Send chat message to a specific client
      * @param target
      * @param sourceName
      * @param text
      */
     public void SayTo(String sourceName, String text) {
         if(!inuse ||  pers.connected != ClientPersistant.ClientConnected.CONNECTED)
             return;
 
 //        System.out.println("Sending chat to: " + clientIndex);
 
         Ref.server.GameSendServerCommand(clientIndex, String.format("chat \"%s^0: %s\"\n", sourceName, text));
     }
 
     private void ClientSpawn() {
         int index = s.ClientNum;
         Common.LogDebug("Spawning : " + pers.Name);
         // toggle the teleport bit so the client knows to not lerp
         // and never clear the voted flag
         int flags = ps.eFlags & EntityFlags.TELEPORT_BIT;
         flags ^= EntityFlags.TELEPORT_BIT;
 
         // clear everything but the persistant data
         ClientPersistant perss = pers;
         int ping = ps.ping;
         Clear();
         pers = perss;
         ps.ping = ping;
         ps.eFlags = flags;
         inuse = true;
         classname = "player";
         r.mins = new Vector2f(Game.PlayerMins);
         r.maxs = new Vector2f(Game.PlayerMaxs);
         r.contents = Content.BODY;
         ClipMask = Content.PLAYERCLIP | Content.BODY;
         ps.clientNum = index;
 
         ps.stats.Health = ps.stats.MaxHealth;
 
 
         // Set spawn
         Gentity spawnPoint = selectSpawnPoint(ps.origin);
         if(spawnPoint != null) {
             SetOrigin(new Vector2f(spawnPoint.s.origin));
             ps.origin = new Vector2f(spawnPoint.s.origin);
         }
         
         pers.cmd = Ref.server.GetUserCommand(index);
 
         Link();
         respawnTime = Ref.game.level.time;
 
         // fire the targets of the spawn point
         if(spawnPoint != null)
             spawnPoint.UseTargets(this);
 
         // run a client frame to drop exactly to the floor,
         // initialize animations and other things
         ps.commandTime = Ref.game.level.time - 100;
         pers.cmd.serverTime = Ref.game.level.time;
         Client_Think();
 
         // positively link the client, even if the command times are weird
         ps.ToEntityState(s, false);
         r.currentOrigin.x = ps.origin.x;
         r.currentOrigin.y = ps.origin.y;
         Link();
 
         ClientEndFrame();
         ps.ToEntityState(s, false);
         //System.out.println(""+s.ClientNum);
     }
 
     // Prepares the EntityState structure and sends predictable events to
     // other clients
     public void ClientEndFrame() {
         ps.ToEntityState(s, false);
         ps.SendPendingPredictableEvents();
         if(!ClientInactivityTimer())
             return;
     }
 
     public void Client_Think() {
         pers.cmd = Ref.server.GetUserCommand(s.ClientNum);
         lastCmdTime = Ref.game.level.time;
 
         // don't think if the client is not yet connected (and thus not yet spawned in)
         if(pers.connected != ClientPersistant.ClientConnected.CONNECTED)
             return;
 
         PlayerInput cmd = pers.cmd;
         // sanity check the command time to prevent speedup cheating
         if(cmd.serverTime > Ref.game.level.time + 200)
             cmd.serverTime = Ref.game.level.time + 200;
         if(cmd.serverTime < Ref.game.level.time - 1000)
             cmd.serverTime = Ref.game.level.time -1000;
 
         int msec = cmd.serverTime - ps.commandTime;
         if(msec < 1)
             return;
         if(msec > 200)
             msec = 200;
 
         ps.moveType = MoveType.NORMAL;
         if(Ref.game.level.editmode)
             ps.moveType = MoveType.EDITMODE;
         else if(ps.stats.Health <= 0)
             ps.moveType = MoveType.DEAD;
 
 //        if(ps.moveType == MoveType.NORMAL)
 //            ps.applyPull = true;
 
         int oldEventSequence = ps.eventSequence;
 
         // Prepare for move
         MoveQuery move = new MoveQuery(Ref.server);
         move.ps = ps;
         move.cmd = cmd;
         move.tracemask = Content.MASK_PLAYERSOLID;
 
         // Do the move
         Move.Move(move);
 
         // save results of pmove
         if(ps.entityEventSequence != oldEventSequence)
             eventTime = Ref.game.level.time;
 
         ps.ToEntityState(s, false);
 
         ps.SendPendingPredictableEvents();
 
         // use the snapped origin for linking so it matches client predicted versions
         r.currentOrigin.x = s.pos.base.x;
         r.currentOrigin.y = s.pos.base.y;
         r.mins = new Vector2f(Game.PlayerMins);
         r.maxs = new Vector2f(Game.PlayerMaxs);
 
         // execute client events
         HandleEvents(oldEventSequence);
 
         // link entity now, after any personal teleporters have been used
         Link();
         TouchTriggers();
 
         // NOTE: now copy the exact origin over otherwise clients can be snapped into solid
         r.currentOrigin.x = ps.origin.x;
         r.currentOrigin.y = ps.origin.y;
 
         // save results of triggers and client events
         if(ps.eventSequence != oldEventSequence)
             eventTime = Ref.game.level.time;
 
         // Check for respawning
         if(ps.stats.Health <= 0) {
             if(cmd.Mouse1)
                 respawn();
         }
 
         ClientTimerActions(msec);
 
         // Check for death
         if(ps.origin.y < Ref.game.g_killheight.iValue) {
             Die();
         }
     }
 
     public void startPull() {
         Ref.cvars.Set2("g_editmode", "0", true);
         if(ps != null)
             ps.applyPull = true;
     }
 
     public void Die() {
         stopPull();
         ps.AddPredictableEvent(Event.DIED, 0);
         ps.velocity.set(0,0);
         ps.stats.Health = 0;
     }
 
     public void stopPull() {
         if(ps != null)
             ps.applyPull = false;
     }
 
     
     private void ClientTimerActions(int msec) {
         timeResidual += msec;
         while(timeResidual >= 1000) {
             timeResidual -= 1000;
 
             if(ps.stats.Health > ps.stats.MaxHealth)
                 ps.stats.Health--;
 
             if(ps.stats.Health > 0)
                 ps.maptime += 1;
 
             for (int i= 0; i < ps.powerups.length; i++) {
                 if(ps.powerups[i] > 0)
                     ps.powerups[i] -= 1000;
                 if(ps.powerups[i] < 0)
                     ps.powerups[i] = 0;
             }
         }
     }
 
     private void TouchTriggers() {
         // dead clients don't activate triggers!
         if(ps.stats.Health <= 0)
             return;
 
         Vector2f range = new Vector2f(40,40);
         Vector2f mins = new Vector2f(ps.origin);
         mins.x -= range.x;
         mins.y -= range.y;
         Vector2f maxs = new Vector2f(ps.origin);
         maxs.x += range.x;
         maxs.y += range.y;
 
         SectorQuery query = Ref.server.EntitiesInBox(mins, maxs);
 
         // can't use ent->absmin, because that has a one unit pad
         Vector2f.add(ps.origin, r.mins, mins);
         Vector2f.add(ps.origin, r.maxs, maxs);
 
         for (int index : query.List) {
             Gentity hit = Ref.game.g_entities[index];
 
             if(hit.touch == null && touch == null)
                 continue;
 
             if((hit.r.contents & Content.TRIGGER) == 0)
                 continue;
 
             // use seperate code for determining if an item is picked up
             // so you don't have to actually contact its bounding box
             if(hit.s.eType == EntityType.ITEM) {
                 if(!Ref.common.items.playerTouchesItem(ps, hit.s, Ref.game.level.time))
                     continue;
             } else {
                 if(!Ref.server.EntityContact(mins, maxs, hit.shEnt))
                     continue;
             }
 
             if(hit.touch != null)
                 hit.touch.touch(hit, this);
 
             
         }
     }
 
     public void respawn() {
         
         // TODO: Spawn effect and maybe bodyque
         ClientSpawn();
         Ref.game.respawnAllItems();
         startPull();
         ps.maptime = 0;
     }
 
     public String Client_Connect(int id, boolean firsttime) {
         Clear();
 
         if(id != s.ClientNum)
             Ref.common.Error(ErrorCode.DROP, "Client_Connect: YOU DERPED UP");
         
         clientIndex = id;
         pers.connected = ClientPersistant.ClientConnected.CONNECTING;
         if(firsttime) {
             // InitSessionData
         }
         ClientUserInfoChanged();
         // don't do the "xxx connected" messages if they were caried over from previous level
         if(firsttime) {
             Ref.server.GameSendServerCommand(-1, String.format("print \"%s connected.\"\n", pers.Name));
         }
 
         Ref.game.CalculateRanks();
 
         return null;
     }
 
     @Override
     public void Clear() {
         // The super class is only cleared when Free is called,
         // and players should never be free'd, so don't call it here.
         ////// super.Clear();
         ps.Clear();
         pers = new ClientPersistant();
         lastCmdTime = 0;
         inactivityTime = 0;
         respawnTime = 0;
     }
 
     void Client_Disconnect() {
         Unlink();
         inuse = false;
         classname = "disconnected";
         pers.connected = ClientPersistant.ClientConnected.DISCONNECTED;
 
         Ref.server.SetConfigString(CS.CS_PLAYERS+s.ClientNum, null);
 
         Ref.game.CalculateRanks();
     }
 
     void PlaceInEditMode() {
         ps.moveType = MoveType.NORMAL;
     }
 
     void RemoveFromEditMode() {
         ps.moveType = MoveType.EDITMODE;
     }
 
     private Gentity selectSpawnPoint(Vector2f origin) {
         Gentity spot = null;
         ArrayList<Gentity> spots = new ArrayList<Gentity>();
 
         while((spot = Ref.game.Find(spot, null, "info_player_spawn")) != null) {
             spots.add(spot);
         }
 
         Vector2f spawn = null;
 
         if(spots.isEmpty()) {
             Common.LogDebug("Warning: No spawn points found.");
         } else {
             spot = spots.get(Ref.rnd.nextInt(spots.size()));
         }
 
         return spot;
     }
 }
