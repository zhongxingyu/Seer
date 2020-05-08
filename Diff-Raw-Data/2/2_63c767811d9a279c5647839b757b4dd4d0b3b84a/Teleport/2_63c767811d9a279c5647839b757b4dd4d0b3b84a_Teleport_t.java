 /*
  * Copyright 2011 frdfsnlght <frdfsnlght@gmail.com>.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.bennedum.transporter;
 
 import com.iConomy.iConomy;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.StorageMinecart;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.util.Vector;
 
 /**
  *
  * @author frdfsnlght <frdfsnlght@gmail.com>
  */
 public final class Teleport {
 
     private static final int DEFAULT_ARRIVAL_WINDOW = 20000;
 
     private static final Map<String,String> pins = new HashMap<String,String>();
     private static final Set<Integer> gateLocks = new HashSet<Integer>();
     private static final Map<String,PlayerArrival> arrivals = new HashMap<String,PlayerArrival>();
 
     public static void removeGateLock(Entity entity) {
         synchronized (gateLocks) {
             gateLocks.remove(entity.getEntityId());
         }
     }
 
     public static boolean isGateLocked(Entity entity) {
         synchronized (gateLocks) {
             return gateLocks.contains(entity.getEntityId());
         }
     }
 
     public static void addGateLock(Entity entity) {
         synchronized (gateLocks) {
             gateLocks.add(entity.getEntityId());
         }
     }
 
     public static void setPin(Player player, String pin) {
         synchronized (pins) {
             pins.put(player.getName(), pin);
         }
     }
 
     public static String getPin(Player player) {
         if (player == null) return null;
         return getPin(player.getName());
     }
 
     public static String getPin(String playerName) {
         synchronized (pins) {
             return pins.get(playerName);
         }
     }
 
     public static boolean expectingArrival(Player player) {
         synchronized (arrivals) {
             return arrivals.containsKey(player.getName());
         }
     }
 
     // called when an entity moves into a gate on our server
     public static Location send(Entity entity, LocalGate fromGate) throws TeleportException {
         addGateLock(entity);
         Gate toGate;
 
         try {
             toGate = fromGate.getDestinationGate();
         } catch (GateException ge) {
             throw new TeleportException(ge.getMessage());
         }
 
         toGate.attach(fromGate);
 
         if (toGate.isSameServer()) {
             return send(entity, fromGate, (LocalGate)toGate);
         } else {
             send(entity, fromGate, (RemoteGate)toGate);
             return null;
         }
     }
 
     // called when a player goes directly to a gate
     public static Location sendDirect(Player player, Gate toGate) throws TeleportException {
         if (toGate.isSameServer()) {
             return send(player, null, (LocalGate)toGate);
         } else {
             send(player, null, (RemoteGate)toGate);
             return null;
         }
     }
 
     // called when an entity is traveling between gates on our server
     // fromGate can be null if the player is being sent directly
     private static Location send(Entity entity, LocalGate fromGate, LocalGate toGate) throws TeleportException {
         Player player = null;
         String pin = null;
         Context ctx = null;
 
         if (entity instanceof Player)
             player = (Player)entity;
         else if (entity.getPassenger() instanceof Player)
             player = (Player)entity.getPassenger();
         if (player != null) {
             pin = getPin(player);
             ctx = new Context(player);
         }
 
         if (player != null) {
             // check permissions
             if (fromGate != null) {
                 try {
                     ctx.requireAllPermissions("trp.use." + fromGate.getName());
                 } catch (PermissionsException pe) {
                     throw new TeleportException("not permitted to use this gate");
                 }
             }
             try {
                 ctx.requireAllPermissions("trp.use." + toGate.getName());
             } catch (PermissionsException pe) {
                 throw new TeleportException("not permitted to use the remote gate");
             }
 
             // check pin
             if ((fromGate != null) && fromGate.getRequirePin()) {
                 if (pin == null)
                     throw new TeleportException("this gate requires a pin");
                 if (! fromGate.hasPin(pin))
                     throw new TeleportException("this gate rejected your pin");
             }
             if (toGate.getRequirePin()) {
                 if (pin == null)
                     throw new TeleportException("remote gate requires a pin");
                 if ((! toGate.hasPin(pin)) && toGate.getRequireValidPin())
                     throw new TeleportException("remote gate rejected your pin");
             }
 
             // check funds
             double travelCost;
             if (fromGate != null) {
                 if (fromGate.isSameWorld(toGate))
                     travelCost = fromGate.getSendLocalCost() + toGate.getReceiveLocalCost();
                 else
                     travelCost = fromGate.getSendWorldCost() + toGate.getReceiveWorldCost();
             } else {
                 if (toGate.isSameWorld(entity.getWorld()))
                     travelCost = toGate.getReceiveLocalCost();
                 else
                     travelCost = toGate.getReceiveWorldCost();
             }
             try {
                 ctx.requireFunds(travelCost);
             } catch (FundsException fe) {
                 throw new TeleportException("gate use requires %s", iConomy.format(travelCost));
             }
 
         }
 
         // check inventory
         if (! checkInventory(entity, toGate))
            throw new TeleportException("remote gate won't allow some inventory items");
 
 
         if ((fromGate != null) && Global.config.getBoolean("useGatePermissions", false)) {
             try {
                 Permissions.requirePermissions(fromGate.getWorldName(), fromGate.getName(), true, "trp.send." + toGate.getGlobalName());
             } catch (PermissionsException pe) {
                 throw new TeleportException("this gate is not permitted to send to the remote gate");
             }
             try {
                 Permissions.requirePermissions(toGate.getWorldName(), toGate.getName(), true, "trp.receive." + fromGate.getGlobalName());
             } catch (PermissionsException pe) {
                 throw new TeleportException("the remote gate is not permitted to receive from this gate");
             }
         }
 
         // do the teleport
         Location currentLoc = entity.getLocation();
         Location newLocation = prepareSpawnLocation(currentLoc.getPitch(), currentLoc.getYaw(), (fromGate == null) ? null : fromGate.getDirection(), toGate);
 
         // this is necessary so minecarts don't sink into the ground
         // it also gives them a little push out of the gate
         if (! (entity instanceof Player))
             newLocation.setY(newLocation.getY() + 0.5);
         Vector velocity = entity.getVelocity();
         velocity.multiply(2);
         velocity.setY(0);
         entity.setVelocity(velocity);
 
         if (! entity.teleport(newLocation))
             throw new TeleportException("teleport to '%s' failed", toGate.getFullName());
 
         if (filterInventory(entity, toGate)) {
             if (player == null)
                 Utils.info("some inventory items where filtered by the remote gate");
             else
                 ctx.sendLog("some inventory items where filtered by the remote gate");
         }
 
         if (player == null)
             Utils.info("teleported entity '%d' to '%s'", entity.getEntityId(), toGate.getFullName());
         else {
             ctx.sendLog(ChatColor.GOLD + "teleported to '%s'", toGate.getName(ctx));
 
             // do damage for invalid pin
             if (toGate.getRequirePin() &&
                 (! toGate.hasPin(pin)) &&
                 (! toGate.getRequireValidPin()) &&
                 (toGate.getInvalidPinDamage() > 0)) {
                 ctx.sendLog("invalid pin");
                 player.damage(toGate.getInvalidPinDamage());
             }
 
             // deduct funds
             try {
                 if (fromGate != null) {
                     if (fromGate.isSameWorld(toGate)) {
                         ctx.chargeFunds(fromGate.getSendLocalCost(), "debited $$ for on-world transmission");
                         ctx.chargeFunds(toGate.getReceiveLocalCost(), "debited $$ for on-world reception");
                     } else {
                         ctx.chargeFunds(fromGate.getSendWorldCost(), "debited $$ for off-world transmission");
                         ctx.chargeFunds(toGate.getReceiveWorldCost(), "debited $$ for off-world reception");
                     }
                 } else {
                     if (toGate.isSameWorld(entity.getWorld()))
                         ctx.chargeFunds(toGate.getReceiveLocalCost(), "debited $$ for on-world reception");
                     else
                         ctx.chargeFunds(toGate.getReceiveWorldCost(), "debited $$ for off-world reception");
                 }
             } catch (FundsException fe) {
                 ctx.warnLog("unable to deduct travel costs: %s", fe.getMessage());
             }
         }
 
         return newLocation;
     }
 
     // called when an entity on our server is being sent to another server
     // fromGate can be null if the player is being sent directly
     private static void send(final Entity entity, final LocalGate fromGate, final RemoteGate toGate) throws TeleportException {
         final Player player;
         final String pin;
         final Context ctx;
 
         if (entity instanceof Player)
             player = (Player)entity;
         else if (entity.getPassenger() instanceof Player)
             player = (Player)entity.getPassenger();
         else
             player = null;
         if (player == null) {
             pin = null;
             ctx = null;
         } else {
             pin = getPin(player);
             ctx = new Context(player);
         }
 
         if ((player != null) && (fromGate != null)) {
             // check permissions on our side
             try {
                 ctx.requireAllPermissions("trp.use." + fromGate.getName());
             } catch (PermissionsException pe) {
                 throw new TeleportException("not permitted to use this gate");
             }
 
             // check pin
             if (fromGate.getRequirePin()) {
                 if (pin == null)
                     throw new TeleportException("this gate requires a pin");
                 if (! fromGate.hasPin(pin))
                     throw new TeleportException("this gate rejected your pin");
             }
 
             // check funds
             try {
                 ctx.requireFunds(fromGate.getSendServerCost());
             } catch (FundsException fe) {
                 throw new TeleportException("this gate requires %s", iConomy.format(fromGate.getSendWorldCost()));
             }
 
         }
 
         if ((fromGate != null) && Global.config.getBoolean("useGatePermissions", false))
             try {
                 Permissions.requirePermissions(fromGate.getWorldName(), fromGate.getName(), true, "trp.send." + toGate.getGlobalName());
             } catch (PermissionsException pe) {
                 throw new TeleportException("this gate is not permitted to send to the remote gate");
             }
 
         // check connection
         if (! toGate.getServer().isConnected())
             throw new TeleportException("server '%s' is offline", toGate.getServer().getName());
 
         if (player == null)
             Utils.info("teleporting entity '%d' to '%s'...", entity.getEntityId(), toGate.getFullName());
         else
             Utils.info("teleporting player '%s' to '%s'...", player.getName(), toGate.getFullName());
 
         // tell other side to check things
         // if we get a positive response, do our half of the teleport
         Utils.worker(new Runnable() {
             @Override
             public void run() {
                 try {
                     // this call will block until we get a response
                     // if no exception is thrown, we can go ahead and send the entity on our side
                     toGate.getServer().doExpectEntity(entity, fromGate, toGate);
                     // remote side is ready to receive, so clean up here
                     Utils.fire(new Runnable() {
                         @Override
                         public void run() {
                             if (player != null) {
                                 ctx.send(ChatColor.GOLD + "teleporting to '%s'...", toGate.getName(ctx));
 
                                 // charge funds
                                 if (fromGate != null) {
                                     try {
                                         ctx.chargeFunds(fromGate.getSendServerCost(), "debited $$ for off-server transmission");
                                     } catch (FundsException fe) {}
                                 }
 
                                 String mcAddress = toGate.getServer().getMinecraftAddress();
                                 if (mcAddress == null) {
                                     Utils.warning("minecraft address for '%s' is null?", toGate.getServer().getName());
                                     return;
                                 }
                                 String[] addrParts = mcAddress.split("/");
                                 if (addrParts.length == 1) {
                                     // this is a client based reconnect
                                     Utils.info("sending player '%s' to '%s' via client reconnect", player.getName(), addrParts[0]);
                                     player.kickPlayer("[" + Global.pluginName + " Client] please reconnect to: " + addrParts[0]);
                                 } else {
                                     // this is a proxy based reconnect
                                     Utils.info("sending player '%s' to '%s,%s' via proxy reconnect", player.getName(), addrParts[0], addrParts[1]);
                                     player.kickPlayer("[" + Global.pluginName + " Proxy] please reconnect to: " + addrParts[0] + "," + addrParts[1]);
                                 }
                             }
                         }
                     });
                 } catch (final ServerException e) {
                     Utils.fire(new Runnable() {
                         @Override
                         public void run() {
                             if (player != null)
                                 ctx.warnLog("server '%s' complained: %s", toGate.getServer().getName(), e.getMessage());
                             else
                                 Utils.warning("server '%s' complained: %s", toGate.getServer().getName(), e.getMessage());
                         }
                     });
                 }
             }
         });
     }
 
     // called when another server is sending an entity our way, with or without a player
     // throw an exception, which will be returned to the sender, if we won't accept the entity
     // fromGateName and fromGateDirection can be null if the player is being sent directly
     public static void expect(final EntityState entityState, Server fromServer, String fromGateName, String toGateName, final BlockFace fromGateDirection) throws TeleportException {
         Gate gate;
         final RemoteGate fromGate;
 
         if (fromGateName != null) {
             gate = Global.gates.get(fromGateName);
             if (gate == null)
                 throw new TeleportException("unknown gate '%s'", fromGateName);
             if (gate.isSameServer())
                 throw new TeleportException("fromGate must be a remote gate");
             fromGate = (RemoteGate)gate;
         } else
             fromGate = null;
 
         gate = Global.gates.get(toGateName);
         if (gate == null)
             throw new TeleportException("unknown gate '%s'", toGateName);
         if (! gate.isSameServer())
             throw new TeleportException("toGate must be a local gate");
         final LocalGate toGate = (LocalGate)gate;
 
         // playerState will be null if no player is involved
         PlayerState playerState = entityState.getPlayerState();
 
         if (playerState != null) {
             // check permissions on our side
             try {
                 if (! Permissions.isAllowedToConnect(playerState.getName(), playerState.getIPAddress()))
                     throw new TeleportException("player is not allowed to connect");
                 Permissions.requirePermissions(toGate.getWorldName(), playerState.getName(), true, "trp.use." + toGate.getName());
             } catch (PermissionsException pe) {
                 throw new TeleportException("not permitted to use the remote gate");
             }
 
             // check pin
             if (toGate.getRequirePin()) {
                 String pin = playerState.getPin();
                 if (pin == null)
                     throw new TeleportException("remote gate requires a pin");
                 if ((! toGate.hasPin(pin)) && toGate.getRequireValidPin())
                     throw new TeleportException("remote gate rejected your pin");
             }
 
             // check funds
             try {
                 Utils.deductFunds(playerState.getName(), toGate.getReceiveServerCost(), true);
             } catch (FundsException fe) {
                 throw new TeleportException("gate use requires %s", iConomy.format(toGate.getReceiveServerCost()));
             }
 
             // check inventory
             if (! checkInventory(playerState, toGate))
                 throw new TeleportException("remote gate won't allow items in your inventory");
 
         } else {
             // check inventory
             if (! checkInventory(entityState, toGate))
                 throw new TeleportException("remote gate won't allow items in inventory");
         }
 
         if ((fromGate != null) && Global.config.getBoolean("useGatePermissions", false)) {
             try {
                 Permissions.requirePermissions(toGate.getWorldName(), toGate.getName(), true, "trp.receive." + fromGate.getGlobalName());
             } catch (PermissionsException pe) {
                 throw new TeleportException("the remote gate is not permitted to receive from this gate");
             }
         }
 
         Utils.fire(new Runnable() {
             @Override
             public void run() {
                 Utils.info("expecting '%s' from '%s'...", entityState.getName(), fromGate.getFullName());
             }
         });
 
         if (playerState == null) {
             // there's no player coming, so let the other side know and instantiate the entity now
             fromServer.doConfirmArrival(entityState, fromGateName, toGateName);
             Utils.fire(new Runnable() {
                 @Override
                 public void run() {
                     Utils.info("sent confirmation of arrival of '%s' from '%s'...", entityState.getName(), fromGate.getFullName());
                     Location location = prepareSpawnLocation(entityState.getPitch(), entityState.getYaw(), fromGateDirection, toGate);
                     Entity entity = entityState.restore(location, null);
                     if (entity != null) {
                         addGateLock(entity);
                         if (filterInventory(entity, toGate))
                             Utils.info("some inventory items where filtered by the remote gate");
                         Utils.info("teleported '%s' to '%s'", entityState.getName(), toGate.getFullName());
                     } else
                         Utils.warning("unable to teleport '%s' to '%s'", entityState.getName(), toGate.getFullName());
                 }
             });
         } else {
 
             // save the info away and wait for the player to connect
             final PlayerArrival arrival = new PlayerArrival(entityState, fromServer.getName(), fromGateName, toGateName, fromGateDirection);
             synchronized (arrivals) {
                 arrivals.put(playerState.getName(), arrival);
             }
 
             // set up a delayed task to cancel the arrival if they never arrive
             Utils.fireDelayed(new Runnable() {
                 @Override
                 public void run() {
                     cancel(arrival);
                 }
             }, Global.config.getInt("arrivalWindow", DEFAULT_ARRIVAL_WINDOW));
         }
     }
 
     // called if an expected player doesn't join our server within the arrival window
     private static void cancel(PlayerArrival arrival) {
         synchronized (arrivals) {
             if (arrival.hasArrived()) return;
             arrivals.remove(arrival.getPlayerState().getName());
             arrival.setCancelled();
         }
         Utils.info("cancelled expected arrival of '%s' from '%s'", arrival.getPlayerState().getName(), arrival.getFromGateName());
 
         try {
             Server fromServer = Global.servers.get(arrival.getFromServerName());
             if (fromServer == null)
                 throw new TeleportException("from server not found");
             fromServer.doCancelArrival(arrival.getEntityState(), arrival.getFromGateName(), arrival.getToGateName());
         } catch (TeleportException e) {
             Utils.warning("unable to notify other side of cancellation of arrival of '%s' from '%s': %s", arrival.getPlayerState().getName(), arrival.getFromGateName(), e.getMessage());
         }
     }
 
     // called when the remote side cancels a player sent from our server
     public static void cancel(final EntityState entityState, final String fromGateName, final String toGateName) throws TeleportException {
         Utils.fire(new Runnable() {
             @Override
             public void run() {
                 Utils.warning("received a cancellation of arrival of '%s' at '%s'", entityState.getName(), toGateName);
             }
         });
     }
 
     // called when a player joins our server and we're expecting their arrival
     public static Location arrive(Player player) throws TeleportException {
         PlayerArrival arrival;
         synchronized (arrivals) {
             arrival = arrivals.remove(player.getName());
             if (arrival == null) return null;
             arrival.setArrived();
         }
         Utils.info("detected arrival of '%s' from '%s'", player.getName(), arrival.getFromGateName());
 
         Gate gate = Global.gates.get(arrival.getToGateName());
         if (gate == null)
             throw new TeleportException("unknown gate '%s'", arrival.getToGateName());
         if (! gate.isSameServer())
             throw new TeleportException("toGate must be a local gate");
         LocalGate toGate = (LocalGate)gate;
 
         // don't check permissions, pin, or funds on our side since there's nothing we could
         // do about it now
 
         Context ctx = new Context(player);
         Location location = prepareSpawnLocation(arrival.getEntityState().getPitch(), arrival.getEntityState().getYaw(), arrival.getFromGateDirection(), toGate);
         Entity entity = arrival.getEntityState().restore(location, player);
         if (entity == null)
             throw new TeleportException("unable to restore entity");
 
         addGateLock(player);
 
         if (! entity.teleport(location))
             throw new TeleportException("teleport to '%s' failed", toGate.getName(ctx));
         ctx.sendLog(ChatColor.GOLD + "teleported to '%s'", toGate.getName(ctx));
 
         if (filterInventory(entity, toGate))
             ctx.sendLog("some inventory items where filtered by the remote gate");
 
         // do damage for invalid pin
         if (toGate.getRequirePin() &&
             (! toGate.hasPin(arrival.getPlayerState().getPin())) &&
             (! toGate.getRequireValidPin()) &&
             (toGate.getInvalidPinDamage() > 0)) {
             ctx.sendLog("invalid pin");
             player.damage(toGate.getInvalidPinDamage());
         }
 
         // deduct funds
         try {
             ctx.chargeFunds(toGate.getReceiveServerCost(), "debited $$ for off-server reception");
         } catch (FundsException fe) {
             ctx.warnLog("unable to deduct travel costs: %s", fe.getMessage());
         }
 
         Server fromServer = Global.servers.get(arrival.getFromServerName());
         if (fromServer != null)
             fromServer.doConfirmArrival(arrival.getEntityState(), arrival.getFromGateName(), arrival.getToGateName());
 
         return location;
     }
 
     // called after an entity has successfully been received by a remote server
     public static void confirm(final EntityState entityState, final String fromGateName, final String toGateName) throws TeleportException {
         Utils.fire(new Runnable() {
             @Override
             public void run() {
                 Utils.info("received confirmation of arrival of '%s' at '%s'", entityState.getName(), toGateName);
 
                 // if the entity isn't a player, we have to destroy it on this side
                 if ((! entityState.isPlayer()) && (fromGateName != null)) {
                     LocalGate gate = Global.gates.getLocalGate(fromGateName);
                     if (gate == null) return;
                     for (Entity entity : gate.getWorld().getEntities())
                         if (entity.getEntityId() == entityState.getEntityId()) {
                             // this needs to change if we decide the handle nested entities
                             entity.remove();
                             break;
                         }
                 }
             }
         });
     }
 
     public static void sendChat(Player player, String message) {
         Map<Server,Set<RemoteGate>> gates = new HashMap<Server,Set<RemoteGate>>();
         Location loc = player.getLocation();
         Gate destGate;
         Server destServer;
         for (LocalGate gate : Global.gates.getLocalGates()) {
             if (gate.isInChatProximity(loc) && gate.isOpen()) {
                 try {
                     destGate = gate.getDestinationGate();
                     if (! destGate.isSameServer()) {
                         destServer = Global.servers.get(destGate.getServerName());
                         if (gates.get(destServer) == null)
                             gates.put(destServer, new HashSet<RemoteGate>());
                         gates.get(destServer).add((RemoteGate)destGate);
                     }
                 } catch (GateException e) {
                 }
             }
         }
         for (Server server : gates.keySet()) {
             server.doRelayChat(player, message, gates.get(server));
         }
     }
 
     public static void receiveChat(String playerName, String displayName, String serverName, String message, List<String> toGates) {
         Future<Map<String,Location>> future = Utils.call(new Callable<Map<String,Location>>() {
             @Override
             public Map<String,Location> call() {
                 Map<String,Location> players = new HashMap<String,Location>();
                 for (Player player : Global.plugin.getServer().getOnlinePlayers())
                     players.put(player.getName(), player.getLocation());
                 return players;
             }
         });
 
         Map<String,Location> players = null;
         try {
             players = future.get();
         } catch (InterruptedException e) {
         } catch (ExecutionException e) {}
         if (players == null) return;
 
         final Set<String> playersToReceive = new HashSet<String>();
         for (String gateName : toGates) {
             Gate gate = Global.gates.get(gateName);
             if (gate == null) continue;
             if (! gate.isSameServer()) continue;
             for (String player : players.keySet())
                 if (((LocalGate)gate).isInChatProximity(players.get(player)))
                     playersToReceive.add(player);
         }
 
         if (playersToReceive.isEmpty()) return;
 
         final String msg = String.format("<%s@%s> %s", displayName, serverName, message);
         Utils.fire(new Runnable() {
             @Override
             public void run() {
                 for (String playerName : playersToReceive) {
                     Player player = Global.plugin.getServer().getPlayer(playerName);
                     if ((player != null) && (player.isOnline()))
                         player.sendMessage(msg);
                 }
             }
         });
     }
 
     // fromGateDirection can be null if the player is being sent directly
     private static Location prepareSpawnLocation(float fromPitch, float fromYaw, BlockFace fromGateDirection, LocalGate toGate) {
         GateBlock block = toGate.getSpawnBlocks().randomBlock();
         Location location = block.getLocation().clone();
         location.setX(location.getX() + 0.5);
         location.setY(location.getY());
         location.setZ(location.getZ() + 0.5);
         location.setPitch(fromPitch);
         if (fromGateDirection == null)
             fromGateDirection = toGate.getDirection();
         location.setYaw(block.getDetail().getSpawn().calculateYaw(fromYaw, fromGateDirection, toGate.getDirection()));
         Utils.prepareChunk(location);
         return location;
     }
 
     // Beyond here be dragons... and inventory filtering
 
     private static boolean checkInventory(Entity entity, LocalGate toGate) {
         return checkInventory(EntityState.extractState(entity), toGate);
     }
 
     private static boolean checkInventory(EntityState entityState, LocalGate toGate) {
         if (! toGate.getRequireAllowedItems()) return true;
         while (entityState != null) {
             if (entityState instanceof VehicleState) {
                 if (! checkInventory(((VehicleState)entityState).getInventory(), toGate))
                     return false;
                 entityState = ((VehicleState)entityState).getPassengerState();
             } else if (entityState instanceof PlayerState) {
                 if (! checkInventory(((PlayerState)entityState).getInventory(), toGate))
                     return false;
                 if (! checkInventory(((PlayerState)entityState).getArmor(), toGate))
                     return false;
                 entityState = null;
             }
         }
         return true;
     }
 
     private static boolean checkInventory(ItemStack[] stacks, LocalGate toGate) {
         if (stacks == null) return true;
         for (int i = 0; i < stacks.length; i++) {
             ItemStack stack = stacks[i];
             if (stack == null) continue;
             if (filterItemStack(stack, toGate) == null) return false;
         }
         return true;
     }
 
     private static boolean filterInventory(Entity entity, LocalGate toGate) {
         boolean filtered = false;
         while (entity != null) {
             if (entity instanceof StorageMinecart) {
                 Inventory inventory = ((StorageMinecart)entity).getInventory();
                 if (filterInventory(inventory, toGate)) filtered = true;
             } else if (entity instanceof Player) {
                 PlayerInventory inventory = ((Player)entity).getInventory();
                 if (filterInventory(inventory, toGate)) filtered = true;
                 ItemStack[] armor = inventory.getArmorContents();
                 if (filterInventory(armor, toGate)) filtered = true;
                 inventory.setArmorContents(armor);
             }
             entity = entity.getPassenger();
         }
         return filtered;
     }
 
     private static boolean filterInventory(Inventory inventory, LocalGate toGate) {
         ItemStack[] contents = inventory.getContents();
         boolean filtered = filterInventory(contents, toGate);
         inventory.setContents(contents);
         return filtered;
     }
 
     private static boolean filterInventory(ItemStack[] stacks, LocalGate toGate) {
         boolean filtered = false;
         for (int i = 0; i < stacks.length; i++) {
             ItemStack newStack = filterItemStack(stacks[i], toGate);
             if (newStack != stacks[i]) {
                 stacks[i] = newStack;
                 filtered = true;
             }
         }
         return filtered;
     }
 
     private static ItemStack filterItemStack(ItemStack stack, LocalGate toGate) {
         if (stack == null) return null;
         String item = encodeItemStack(stack);
         String newItem = toGate.getReplaceItem(item);
         if ((newItem != null) && (! newItem.equals("*"))) {
             stack = decodeItem(stack, newItem);
             item = newItem;
         }
         if (toGate.hasAllowedItems()) {
             if (toGate.hasAllowedItem(item)) return stack;
             return null;
         }
         if (toGate.hasBannedItem(item)) return null;
         return stack;
     }
 
     private static String encodeItemStack(ItemStack stack) {
         String item = stack.getType().toString();
         if (stack.getDurability() > 0)
             item += ":" + stack.getDurability();
         return item;
     }
 
     private static ItemStack decodeItem(ItemStack oldItem, String item) {
         String[] parts = item.split(":");
         Material material = Material.valueOf(parts[0]);
         int amount = oldItem.getAmount();
         short damage = oldItem.getDurability();
         if (parts.length > 1)
             try {
                 damage = Short.parseShort(parts[1]);
             } catch (NumberFormatException e) {}
         return new ItemStack(material, amount, damage);
     }
 
 }
