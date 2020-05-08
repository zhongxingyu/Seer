 package cubetech.Game;
 
 import cubetech.Block;
 import cubetech.common.CS;
 import cubetech.common.CVar;
 import cubetech.common.CVarFlags;
 import cubetech.common.Commands.ExecType;
 import cubetech.common.Common;
 import cubetech.common.GItem;
 import cubetech.common.ICommand;
 import cubetech.entities.EntityType;
 import cubetech.entities.Func_Door;
 import cubetech.entities.IEntity;
 import cubetech.entities.SharedEntity;
 import cubetech.entities.Info_Player_Spawn;
 import cubetech.misc.Ref;
 import java.util.EnumSet;
 import java.util.HashMap;
 import org.lwjgl.util.vector.Vector2f;
 
 /**
  *
  * @author mads
  */
 public class Game {
     public static final Vector2f PlayerMins = new Vector2f(-8,-12);
     public static final Vector2f PlayerMaxs = new Vector2f(8,12);
     CVar sv_speed;
     CVar sv_gravity;
     CVar sv_jumpmsec;
     CVar sv_jumpvel;
     CVar g_cheats;
     CVar g_gametype;
     CVar g_restarted;
     CVar g_editmode;
     CVar g_maxclients;
     CVar sv_pullacceleration;
     CVar sv_acceleration;
     CVar sv_friction;
     CVar sv_stopspeed;
     CVar sv_stepheight;
     CVar sv_movemode;
     CVar g_killheight;
 
     CVar sv_pull1;
     CVar sv_pull2;
     CVar sv_pull3;
     CVar sv_pull4;
     CVar sv_pull5;
     CVar sv_pull6;
 
     CVar sv_pullstep;
 //    CVar sv_doublejump;
 
     public SpawnEntities spawnEntities;
     public Gentity[] g_entities;
     GameClient[] g_clients;
     public LevelLocal level;
     HashMap<String, IEntity> spawns = new HashMap<String, IEntity>();
 
     public Game() {
         sv_speed = Ref.cvars.Get("sv_speed", "100", EnumSet.of(CVarFlags.SERVER_INFO, CVarFlags.USER_INFO, CVarFlags.ARCHIVE));
         g_cheats = Ref.cvars.Get("g_cheats", "0", EnumSet.of(CVarFlags.NONE));
         g_gametype = Ref.cvars.Get("g_gametype", "0", EnumSet.of(CVarFlags.SERVER_INFO, CVarFlags.LATCH, CVarFlags.USER_INFO));
         g_restarted = Ref.cvars.Get("g_restarted", "0", EnumSet.of(CVarFlags.ROM));
         g_maxclients = Ref.cvars.Get("g_maxclients", "32", EnumSet.of(CVarFlags.SERVER_INFO, CVarFlags.LATCH, CVarFlags.USER_INFO));
         g_editmode = Ref.cvars.Get("g_editmode", "0", EnumSet.of(CVarFlags.SERVER_INFO, CVarFlags.USER_INFO));
         g_killheight = Ref.cvars.Get("g_killheight", "-1000", EnumSet.of(CVarFlags.NONE, CVarFlags.ARCHIVE));
 
         sv_pull1 = Ref.cvars.Get("sv_pull1", "60", EnumSet.of(CVarFlags.SERVER_INFO, CVarFlags.USER_INFO, CVarFlags.ARCHIVE));
         sv_pull2 = Ref.cvars.Get("sv_pull2", "120", EnumSet.of(CVarFlags.SERVER_INFO, CVarFlags.USER_INFO, CVarFlags.ARCHIVE));
         sv_pull3 = Ref.cvars.Get("sv_pull3", "180", EnumSet.of(CVarFlags.SERVER_INFO, CVarFlags.USER_INFO, CVarFlags.ARCHIVE));
         sv_pull4 = Ref.cvars.Get("sv_pull4", "260", EnumSet.of(CVarFlags.SERVER_INFO, CVarFlags.USER_INFO, CVarFlags.ARCHIVE));
         sv_pull5 = Ref.cvars.Get("sv_pull5", "350", EnumSet.of(CVarFlags.SERVER_INFO, CVarFlags.USER_INFO, CVarFlags.ARCHIVE));
         sv_pull6 = Ref.cvars.Get("sv_pull6", "500", EnumSet.of(CVarFlags.SERVER_INFO, CVarFlags.USER_INFO, CVarFlags.ARCHIVE));
         sv_pullstep = Ref.cvars.Get("sv_pullstep", "0.65", EnumSet.of(CVarFlags.SERVER_INFO, CVarFlags.USER_INFO, CVarFlags.ARCHIVE));
 
         sv_movemode = Ref.cvars.Get("sv_movemode", "1", EnumSet.of(CVarFlags.SERVER_INFO, CVarFlags.USER_INFO));
         sv_gravity = Ref.cvars.Get("sv_gravity", "300", EnumSet.of(CVarFlags.SERVER_INFO, CVarFlags.USER_INFO, CVarFlags.ARCHIVE));
         sv_jumpmsec = Ref.cvars.Get("sv_jumpmsec", "250", EnumSet.of(CVarFlags.SERVER_INFO, CVarFlags.USER_INFO, CVarFlags.ARCHIVE));
         sv_jumpvel = Ref.cvars.Get("sv_jumpvel", "125", EnumSet.of(CVarFlags.SERVER_INFO, CVarFlags.USER_INFO, CVarFlags.ARCHIVE));
         sv_pullacceleration = Ref.cvars.Get("sv_pullacceleration", "80", EnumSet.of(CVarFlags.SERVER_INFO, CVarFlags.USER_INFO, CVarFlags.ARCHIVE));
         sv_acceleration = Ref.cvars.Get("sv_acceleration", "8", EnumSet.of(CVarFlags.SERVER_INFO, CVarFlags.USER_INFO, CVarFlags.ARCHIVE));
         sv_friction = Ref.cvars.Get("sv_friction", "4", EnumSet.of(CVarFlags.SERVER_INFO, CVarFlags.USER_INFO, CVarFlags.ARCHIVE));
         sv_stopspeed = Ref.cvars.Get("sv_stopspeed", "15", EnumSet.of(CVarFlags.SERVER_INFO, CVarFlags.USER_INFO, CVarFlags.ARCHIVE));
         sv_stepheight = Ref.cvars.Get("sv_stepheight", "4", EnumSet.of(CVarFlags.SERVER_INFO, CVarFlags.USER_INFO, CVarFlags.ARCHIVE));
 //        sv_doublejump = Ref.cvars.Get("sv_doublejump", "0", EnumSet.of(CVarFlags.SERVER_INFO, CVarFlags.USER_INFO));
 
         IEntity ent = new Info_Player_Spawn();
         addEntityToSpawn(ent);
         ent = new Func_Door();
         addEntityToSpawn(ent);
         spawnEntities = Ref.cm.cm.spawnEntities;
     }
 
     private void addEntityToSpawn(IEntity ent) {
         spawns.put(ent.getClassName(), ent);
     }
 
     public void Init(int leveltime, int randSeed, boolean restart) {
         Common.Log("--- Game init ---");
         g_cheats = Ref.cvars.Get("g_cheats", "0", EnumSet.of(CVarFlags.NONE));
         Ref.commands.AddCommand("start", new ICommand() {
             public void RunCommand(String[] args) {
                 for (int i= 0; i < g_clients.length; i++) {
                     if(g_clients[i].inuse && g_clients[i].ps != null) {
                         g_clients[i].respawn();
                         g_clients[i].startPull();
                     }
                 }
             }
         });
 
         Ref.commands.AddCommand("stop", new ICommand() {
             public void RunCommand(String[] args) {
                 for (int i= 0; i < g_clients.length; i++) {
                     if(g_clients[i].inuse && g_clients[i].ps != null) {
                         g_clients[i].stopPull();
                     }
                 }
             }
         });
 
         level = new LevelLocal();
         level.time = leveltime;
         level.startTime = leveltime;
 
         //InitWorldSession();
 
         // initialize all clients for this game
         level.maxclients = g_maxclients.iValue;
         
         // initialize all entities for this game
         g_entities = new Gentity[Common.MAX_GENTITIES];
         level.sentities = new SharedEntity[Common.MAX_GENTITIES];
         for (int i= 0; i < Common.MAX_GENTITIES; i++) {
             if(i < level.maxclients) {
                 g_entities[i] = new GameClient();
                 g_entities[i].s.ClientNum = i;
             }
             else
                 g_entities[i] = new Gentity();
             level.sentities[i] = g_entities[i].shEnt;
         }
         level.gentities = g_entities;
 
         
         g_clients = new GameClient[level.maxclients];
         for (int i= 0; i < level.maxclients; i++) {
             g_clients[i] = (GameClient)g_entities[i];
         }
         level.clients = g_clients;
 
         // always leave room for the max number of clients,
         // even if they aren't all used, so numbers inside that
         // range are NEVER anything but clients
         level.num_entities = 64;
 
         WorldSpawn();
 
         SpawnEntity spEnt = new SpawnEntity("item_boots", new Vector2f(150,50));
         spawnEntities.AddEntity(spEnt);
 
 //        Gentity hp = Spawn();
 //        hp.s.origin.set(150,50);
 //        hp.classname = "item_boots";
 //        hp.s.pos.base.set(hp.s.origin);
 //        hp.r.currentOrigin.set(hp.s.origin);
 //
 //        if(!callSpawn(hp))
 //            hp.Free();
 
         Gentity hp = Spawn();
         hp.classname = "info_player_spawn";
         hp.s.origin.set(100, 100);
         hp.s.pos.base.set(hp.s.origin);
         hp.r.currentOrigin.set(hp.s.origin);
 
         if(!callSpawn(hp))
             hp.Free();
 
         spawnEntities.SpawnAll();
 
 //        Ref.cm.cm.ToSubModel(Ref.cm.cm.GetBlock(20));
 //        Ref.cm.cm.GetBlock(20).SetSize(new Vector2f(100,10));
 //
 //        hp = Spawn();
 //        hp.classname = "func_door";
 //        hp.s.origin.set(-100, 100);
 //
 //        hp.s.pos.base.set(hp.s.origin);
 //        hp.r.currentOrigin.set(hp.s.origin);
 //
 //        if(!callSpawn(hp))
 //            hp.Free();
 
         Ref.server.LocateGameData(level.sentities, level.num_entities, level.clients);
     }
 
     private void WorldSpawn() {
         Ref.server.SetConfigString(CS.CS_LEVEL_START_TIME, ""+level.startTime);
         g_entities[Common.ENTITYNUM_WORLD].s.ClientNum = Common.ENTITYNUM_WORLD;
         g_entities[Common.ENTITYNUM_WORLD].classname = "worldspawn";
 
         Ref.server.SetConfigString(CS.CS_WARMUP, ""+0);
         if(g_restarted.iValue == 1) {
             Ref.cvars.Set2("g_restarted", "0", true);
         }
     }
 
     public void RunFrame(int time) {
         // if we are waiting for the level to restart, do nothing
         if(level.restarted)
             return;
 
         level.framenum++;
         level.previousTime = level.time;
         level.time = time;
         int msec = level.time - level.previousTime;
 
         //int start = Ref.common.Milliseconds();
         for (int i= 0; i < level.num_entities; i++) {
             Gentity ent = g_entities[i];
             if(!ent.inuse)
                 continue;
 
             // clear events that are too old
             if(level.time - ent.eventTime > 300) {
                 if(ent.s.evt > 0) {
                     ent.s.evt = 0;
                     if(i < level.maxclients) // If GameClient, clear of its externalevent
                         ((GameClient)ent).ps.externalEvent = 0;
                 }
                 if(ent.freeAfterEvent) {
                     // tempEntities or dropped items completely go away after their event
                     ent.Free();
                     continue;
                 }
                 else if(ent.unlinkAfterEvent) {
                     // items that will respawn will hide themselves after their pickup event
                     ent.unlinkAfterEvent = false;
                     ent.Unlink();
                 }
             }
 
             if(ent.freeAfterEvent)
                 continue;
 
             if(!ent.r.linked && ent.neverfree)
                 continue;
 
             if(ent.s.eType == EntityType.ITEM || ent.physicsObject) {
                 ent.runItem();
                 continue;
             }
 
             if(ent.s.eType == EntityType.MOVER) {
                 ent.mover.runMover();
                 continue;
             }
 
             if(i < 64)
             {
                 continue;
             }
 
             ent.runThink();
         }
         
         // perform final fixups on the players
         for (int i= 0; i < level.maxclients; i++) {
             GameClient ent = (GameClient)g_entities[i];
             if(ent.inuse)
                 ent.ClientEndFrame();
         }
 
         // Check if editmode has changed
         if(g_editmode.modified) {
             CheckEditMode();
         }
 
 
     }
 
     /**
      * Creates a one-off entity that holds an event
      * @param origin position to place the entity
      * @param evt the event type
      * @return a linked temporary Gentity
      */
     public Gentity TempEntity(Vector2f origin, int evt) {
         Gentity e = Spawn();
         e.s.eType = EntityType.EVENTS + evt;
         e.classname = "tempentity";
         e.eventTime = level.time;
         e.freeAfterEvent = true;
         e.SetOrigin(origin);
 
         e.Link();
 
         return e;
     }
 
     /**
      * Finds and returns a free entity
      * @return
      */
     public Gentity Spawn() {
         int i = 0;
         Gentity e = null;
         for(int force = 0; force < 2; force++) {
             // if we go through all entities and can't find one to free,
             // override the normal minimum times before use
             for (i= 64; i < level.num_entities; i++) {
                 e = g_entities[i];
                 if(e.inuse)
                     continue;
 
                 // the first couple seconds of server time can involve a lot of
                 // freeing and allocating, so relax the replacement policy
                 if(force == 0 && e.freetime > level.startTime + 2000 && level.time - e.freetime < 1000)
                     continue;
 
                 // A free entity is always clean, so just init a few things
                 e.Init(i);
                 return e;
             }
             if(i != Common.MAX_GENTITIES)
                 break; // Don't go for aggresive force if we aren't using all the entities
         }
 
         // Sorry, we're full.
         if(i == Common.ENTITYNUM_MAX_NORMAL) {
             for (i = 0;i < Common.MAX_GENTITIES; i++) {
                 Common.Log(String.format("%s: %s", i, g_entities[i].classname));
             }
             Common.Log("No free entities.");
         }
 
         // open up a new slot
         level.num_entities++;
 
         // let the server system know that there are more entities
         Ref.server.LocateGameData(level.sentities, level.num_entities, level.clients);
         e = g_entities[level.num_entities-1];
         e.Init(level.num_entities-1);
         return e;
     }
 
     // TODO: Cache scoreboard here, then let clients use the cached scoreboard string
     public void CalculateRanks() {
         for (int i= 0; i < level.clients.length; i++) {
             if(level.clients[i] != null && level.clients[i].pers.connected == ClientPersistant.ClientConnected.CONNECTED)
                 level.clients[i].ScoreboardMessage();
         }
     }
 
 
     /**
      * Send chat message to all clients
      * @param ent
      * @param text
      */
     public void Say(GameClient ent, String text) {
         for (int i= 0; i < level.maxclients; i++) {
             ((GameClient)g_entities[i]).SayTo(ent.pers.Name, text);
         }
     }
 
     public void ShutdownGame(boolean b) {
         Common.Log("--- GAME SHUTDOWN ---");
     }
 
     public void Client_Begin(int i) {
         GameClient ent = (GameClient) g_entities[i];
         ent.Begin();
     }
 
     // Handle console commands from the client
     public void Client_Command(int id, String[] tokens) {
         g_clients[id].Client_Command(tokens);
     }
 
     public void ClientUserInfoChanged(int id) {
         GameClient cl = (GameClient)g_entities[id];
         cl.ClientUserInfoChanged();
     }
 
     // Unlinks and clears a player that has disconnected
     public void Client_Disconnect(int ClientNum) {
         GameClient ent = (GameClient)g_entities[ClientNum];
         ent.Client_Disconnect();
     }
 
     public String Client_Connect(int id, boolean firsttime) {
         GameClient ent = (GameClient)g_entities[id];
         return ent.Client_Connect(id, firsttime);
     }
 
     public void Client_Think(int id) {
         ((GameClient)g_entities[id]).Client_Think();
     }
 
     // Reacts on a change to g_editmode
     private void CheckEditMode() {
         g_editmode.modified = false;
         if((g_editmode.iValue == 1) == level.editmode)
             return; // no change
 
         if(level.editmode) {
             ExitEditMode();
         } else
             EnterEditMode();
     }
 
     
 
     private void ExitEditMode() {
         if(!level.editmode)
             return;
 
         level.editmode = false;
         // Notify all clients
         for (int i= 0; i < g_clients.length; i++) {
             if(g_clients[i] != null || !g_clients[i].inuse)
                 continue;
 
             g_clients[i].RemoveFromEditMode();
         }
 
         // Cache the changed map
         Ref.cm.SaveMap("custom");
         spawnEntities.SpawnAll();
     }
 
     private void EnterEditMode() {
         if(level.editmode)
             return;
 
         Ref.cvars.Set2("mapname", "custom", true);
         Ref.commands.ExecuteText(ExecType.NOW, "stop");
 
         level.editmode = true;
         // Notify all clients
         for (int i= 0; i < g_clients.length; i++) {
            if(g_clients[i] == null || !g_clients[i].inuse)
                 continue;
 
             g_clients[i].PlaceInEditMode();
         }
         spawnEntities.UnspawnAll();
     }
 
     
 
     // Find the spawn function for the entity and calls it,
     // returns false if not found
     boolean callSpawn(Gentity ent) {
         if(ent.classname == null || ent.classname.isEmpty())
         {
             Common.LogDebug("callSpawn: Null classname");
             return false;
         }
 
         // check item spawn functions
         GItem item = Ref.common.items.findItemByClassname(ent.classname);
         if(item != null) {
             spawnItem(ent, item);
             return true;
         }
 
         // check normal spawn functions
         IEntity spawn = spawns.get(ent.classname);
         if(spawn != null) {
             spawn.init(ent);
             return true;
         }
 
         Common.LogDebug(ent.classname + " doesn't have a spawn function");
         return false;
     }
 
     /**
      * Sets the clipping size and plants the object on the floor.
      *
      * Items can't be immediately dropped to floor, because they might
      * be on an entity that hasn't spawned yet.
      * @param ent
      * @param item
      */
     void spawnItem(Gentity ent, GItem item) {
         ent.item = item;
         // some movers spawn on the second frame, so delay item
 	// spawns until the third frame so they can ride trains
         ent.nextthink = level.time + 100 * 2; // fix 100 = frametime
         ent.think = Ref.common.items.FinishSpawningItem;
 
         ent.physicsBounce = 0.5f;
     }
 
     // Adds an event+parm and twiddles the event counter
     public void AddEvent(Gentity ent, int evt, int evtParms) {
         if(evt == 0) {
             Common.LogDebug("Zero event added for entity " + ent.s.ClientNum);
             return;
         }
 
         // clients need to add the event in playerState_t instead of entityState_t
         int bits;
         if(ent.isClient()) {
             GameClient cl = ent.getClient();
             bits = cl.ps.externalEvent & Common.EV_EVENT_BITS;
             bits = (bits + Common.EV_EVENT_BIT1) & Common.EV_EVENT_BITS;
             cl.ps.externalEvent = evt | bits;
             cl.ps.externalEventParam = evtParms;
             cl.ps.externalEventTime = level.time;
         } else {
             bits = ent.s.evt & Common.EV_EVENT_BITS;
             bits = (bits + Common.EV_EVENT_BIT1) & Common.EV_EVENT_BITS;
             ent.s.evt = evt | bits;
             ent.s.evtParams = evtParms;
         }
         ent.eventTime = level.time;
     }
 
     public Gentity Find(Gentity from, IGentityFilter filter, String match) {
         if(filter == null)
             filter = GentityFilter.CLASSNAME;
 
         int start = 0;
         if(from != null)
             start = from.s.ClientNum+1;
 
         for (; start < level.num_entities; start++) {
             from = g_entities[start];
             if(!from.inuse)
                 continue;
 
             if(filter.filter(from, match))
                 return from;
         }
 
         return null;
     }
 
     // Sends an update to all clients, so they can know about this block
     void SendBlock(Block b) {
         // TODO: Don't send to localclient?
         Ref.server.GameSendServerCommand(-1, String.format("setblock %d \"%s\"", b.Handle, b.GetSendString()));
 //        for (int i= 0; i < g_clients.length; i++) {
 //            GameClient cl = g_clients[i];
 //            if(cl == null || !cl.inuse || cl.pers.connected == ClientConnected.DISCONNECTED)
 //                continue;
 //
 //
 //        }
     }
 
     void respawnAllItems() {
         for (int i= 0; i < level.num_entities; i++) {
             Gentity ent = g_entities[i];
             if(!ent.inuse || ent.item == null)
                 continue;
 
             //if(ent.think == Ref.common.items.FinishSpawningItem) {
                 // Finalize spawn
                 ent.think.think(ent);
             //}
         }
     }
 
 }
