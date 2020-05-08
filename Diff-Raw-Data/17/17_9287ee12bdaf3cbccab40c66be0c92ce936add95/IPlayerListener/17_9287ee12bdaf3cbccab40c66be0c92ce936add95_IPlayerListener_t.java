 /**
  * 
  * Copyright 2011 Greatman (https://github.com/greatman)
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  * 
  * Some parts of this class (Line 46-77) is Copyright 2011 MilkBowl (http://github.com/MilkBowl)
  * Modified by Greatman (http://github.com/greatman)
  * 
  */
 
 package me.greatman.plugins.inn;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.material.Door;
 
 import com.nijikokun.register.payment.Method.MethodAccount;
 
 public class IPlayerListener extends PlayerListener {
 	private final Inn plugin;
     public IPlayerListener(Inn instance) {
         plugin = instance;
     }
     
     @Override
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.isCancelled())
             return;
 
         Player player = event.getPlayer();
         String playerName = player.getName();
         if (!plugin.getPlayerData().containsKey(playerName)) {
             plugin.getPlayerData().put(playerName, new PlayerData(plugin, playerName));
         }
 
         // If our user is select & is not holding an item, selection time
         if (plugin.getPlayerData().get(playerName).isSelecting() && player.getItemInHand().getType() == Material.AIR && event.getClickedBlock().getType() == Material.WOODEN_DOOR) {
             int x, y, z;
             Location loc = event.getClickedBlock().getLocation();
             x = loc.getBlockX();
             y = loc.getBlockY();
             z = loc.getBlockZ();
             PlayerData pData = plugin.getPlayerData().get(playerName);
            Door door = (Door)event.getClickedBlock().getState().getData();
             if (door.isTopHalf())
             	y = y - 1;
             if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
             	
             	
             	if (Inn.doorAlreadyExists(x,y,z)){
             		player.sendMessage(ChatColor.RED + "This door is already registered!");
             		return;
             	}
                 int[] xyz = { x, y, z };
                 pData.setPositionA(xyz);
                 player.sendMessage(ChatColor.DARK_AQUA + "Door selected");
                 event.setCancelled(true);
 	            if(pData.getPositionA() != null) {
 	                   player.sendMessage(ChatColor.DARK_AQUA + "Type " + ChatColor.WHITE + "/inn create [Price]" + ChatColor.DARK_AQUA + ", if you're happy with your selection, otherwise keep selecting!");
 	            }
                 
         
             }
         //Are we trying to open a door?
         }else if (event.getClickedBlock().getType() == Material.WOODEN_DOOR){
         	if (IPermissions.permission(player, "inn.bypass", player.isOp()))
         			return;
         	int x, y, z;
             Location loc = event.getClickedBlock().getLocation();
             x = loc.getBlockX();
             y = loc.getBlockY();
             z = loc.getBlockZ();
            Door door = (Door)event.getClickedBlock().getState().getData();
             if (door.isTopHalf())
             	y = y - 1;
             if (Inn.doorAlreadyExists(x,y,z)){
             	String owner = Inn.getOwner(x,y,z);
             	if (owner.equalsIgnoreCase(playerName)){
             		player.sendMessage(ChatColor.GREEN + "This is your inn door!");
             		return;
             	}
             	int price = Inn.getDoorPrice(x,y,z);
             	if (Inn.isTimeoutExpired(x, y, z, playerName)){
             		MethodAccount playerAccount = plugin.Method.getAccount(playerName);
                 	if (playerAccount.hasEnough(price)){
                 		playerAccount.subtract(price);
                 		MethodAccount playerAccount2 = plugin.Method.getAccount(owner);
                 		playerAccount2.add(price);
                 		Inn.addTimeout(x, y, z, playerName);
                 		player.sendMessage(ChatColor.DARK_AQUA + "You are entering " + owner + " inn room");
                 		
                 		
                 	}else
                 		event.setCancelled(true);
             	}else
             		return;
             	
             }
         //Are we trying to delete a door?
         }else if (plugin.getPlayerData().get(playerName).isRemoving() && player.getItemInHand().getType() == Material.AIR && event.getClickedBlock().getType() == Material.WOODEN_DOOR){
         	int x, y, z;
             Location loc = event.getClickedBlock().getLocation();
             x = loc.getBlockX();
             y = loc.getBlockY();
             z = loc.getBlockZ();
            Door door = (Door)event.getClickedBlock().getState().getData();
             if (door.isTopHalf())
             	y = y - 1;
             if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
             	
             	
             	if (Inn.doorAlreadyExists(x,y,z)){
             		String query = "DELETE FROM doors WHERE x=" + x + " AND y=" + y + " AND z=" + z;
             		Inn.manageSQLite.deleteQuery(query);
             		player.sendMessage(ChatColor.RED + "This Inn door has been deleted!");
             		return;
             	}else{
             		player.sendMessage(ChatColor.RED + "This door is not a Inn door!");
             	}
             }
         }
     }
 }
