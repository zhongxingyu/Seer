 /*
  * Copyright (C) 2013 eccentric_nz
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package me.eccentric_nz.TARDIS.listeners;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import me.eccentric_nz.TARDIS.TARDIS;
 import me.eccentric_nz.TARDIS.TARDISConstants.COMPASS;
 import me.eccentric_nz.TARDIS.TARDISConstants.SCHEMATIC;
 import me.eccentric_nz.TARDIS.achievement.TARDISAchievementFactory;
 import me.eccentric_nz.TARDIS.database.QueryFactory;
 import me.eccentric_nz.TARDIS.database.ResultSetPlayerPrefs;
 import me.eccentric_nz.TARDIS.rooms.TARDISCondenserData;
 import me.eccentric_nz.TARDIS.rooms.TARDISRoomBuilder;
 import me.eccentric_nz.TARDIS.rooms.TARDISSeedData;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.WorldType;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 /**
  * The Doctor kept some of the clothes from his previous regenerations, as well
  * as clothing for other people in the TARDIS wardrobe. At least some of the
  * clothes had pockets that were bigger on the inside.
  *
  * @author eccentric_nz
  */
 public class TARDISRoomSeeder implements Listener {
 
     private final TARDIS plugin;
     private List<SCHEMATIC> ars = new ArrayList<SCHEMATIC>();
 
     public TARDISRoomSeeder(TARDIS plugin) {
         this.plugin = plugin;
         ars.add(SCHEMATIC.ARS);
         ars.add(SCHEMATIC.BUDGET);
         ars.add(SCHEMATIC.PLANK);
         ars.add(SCHEMATIC.STEAMPUNK);
         ars.add(SCHEMATIC.TOM);
     }
 
     /**
      * Listens for player interaction with one of the blocks required to seed a
      * room. If the block is clicked with the TARDIS key after running the
      * command /tardis room [room type], the seed block will start growing into
      * a passageway or the room type specified.
      *
      * Requires the TARDIS to have sufficient Artron Energy to grow the room.
      *
      * @param event a player clicking a block
      */
     @EventHandler(priority = EventPriority.MONITOR)
     public void onSeedBlockInteract(PlayerInteractEvent event) {
         final Player player = event.getPlayer();
         String playerNameStr = player.getName();
         // check that player is in TARDIS
         if (!plugin.trackRoomSeed.containsKey(playerNameStr)) {
             return;
         }
         Block block = event.getClickedBlock();
         if (block != null) {
             Material blockType = block.getType();
             Material inhand = player.getItemInHand().getType();
             String key;
             HashMap<String, Object> where = new HashMap<String, Object>();
             where.put("player", playerNameStr);
             ResultSetPlayerPrefs rsp = new ResultSetPlayerPrefs(plugin, where);
             if (rsp.resultSet()) {
                 key = (!rsp.getKey().isEmpty()) ? rsp.getKey() : plugin.getConfig().getString("key");
             } else {
                 key = plugin.getConfig().getString("key");
             }
             // only proceed if they are clicking a seed block with the TARDIS key!
             if (plugin.seeds.containsKey(blockType) && inhand.equals(Material.getMaterial(key))) {
                 // check they are still in the TARDIS world
                 World world = block.getLocation().getWorld();
                 String name = world.getName();
                 String gen = world.getGenerator().getClass().getSimpleName();
                 boolean special = name.contains("TARDIS_TimeVortex") && (gen.equals("TARDISChunkGenerator") || world.getWorldType().equals(WorldType.FLAT));
                 if (!name.equals("TARDIS_WORLD_" + playerNameStr) && !special) {
                     player.sendMessage(plugin.pluginName + "You must be in a TARDIS world to grow a room!");
                     return;
                 }
                 // get clicked block location
                 Location b = block.getLocation();
                 // get player's direction
                 COMPASS d = COMPASS.valueOf(plugin.utils.getPlayersDirection(player, false));
                 // get seed data
                 TARDISSeedData sd = plugin.trackRoomSeed.get(playerNameStr);
                 // check they are not in an ARS chunk
                 if (ars.contains(sd.getSchematic())) {
                     Chunk c = b.getWorld().getChunkAt(block.getRelative(BlockFace.valueOf(d.toString()), 4));
                     int cx = c.getX();
                     int cy = block.getY();
                     int cz = c.getZ();
                     if ((cx >= sd.getMinx() && cx <= sd.getMaxx()) && (cy >= 48 && cy <= 96) && (cz >= sd.getMinz() && cz <= sd.getMaxz())) {
                         player.sendMessage(plugin.pluginName + "You cannot manually grow a room here, please use the Architectural Reconfiguration System!");
                         return;
                     }
                 }
                 // get room schematic
                 String r = plugin.seeds.get(blockType);
                 // check that the blockType is the same as the one they ran the /tardis room [type] command for
                 if (!sd.getRoom().equals(r)) {
                    player.sendMessage(plugin.pluginName + "That is not the correct seed block to grow a " + plugin.trackRoomSeed.get(playerNameStr) + "!");
                     return;
                 }
                 TARDISRoomBuilder builder = new TARDISRoomBuilder(plugin, r, b, d, player);
                 if (builder.build()) {
                     plugin.trackRoomSeed.remove(playerNameStr);
                     // ok they clicked it, so take their energy!
                     int amount = plugin.getRoomsConfig().getInt("rooms." + r + ".cost");
                     QueryFactory qf = new QueryFactory(plugin);
                     HashMap<String, Object> set = new HashMap<String, Object>();
                     set.put("owner", playerNameStr);
                     qf.alterEnergyLevel("tardis", -amount, set, player);
                     // remove blocks from condenser table if rooms_require_blocks is true
                     if (plugin.getConfig().getBoolean("rooms_require_blocks")) {
                         TARDISCondenserData c_data = plugin.roomCondenserData.get(playerNameStr);
                         for (Map.Entry<String, Integer> entry : c_data.getBlockIDCount().entrySet()) {
                             HashMap<String, Object> wherec = new HashMap<String, Object>();
                             wherec.put("tardis_id", c_data.getTardis_id());
                             wherec.put("block_data", entry.getKey());
                             qf.alterCondenserBlockCount(entry.getValue(), wherec);
                         }
                         plugin.roomCondenserData.remove(playerNameStr);
                     }
                     // are we doing an achievement?
                     if (plugin.getAchivementConfig().getBoolean("rooms.enabled")) {
                         TARDISAchievementFactory taf = new TARDISAchievementFactory(plugin, player, "rooms", plugin.seeds.size());
                         taf.doAchievement(r);
                     }
                 }
             }
         }
     }
 }
