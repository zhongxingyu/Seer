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
  */
 
 package me.greatman.plugins.inn;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.material.Door;
 
 public class IBlockListener extends BlockListener{
 	
     public IBlockListener() {
     }
     @Override
 	public void onBlockBreak(BlockBreakEvent event){
     	if (event.getBlock().getType() == Material.WOODEN_DOOR){
     		int x,y,z;
         	Location loc = event.getBlock().getLocation();
         	x = loc.getBlockX();
             y = loc.getBlockY();
             z = loc.getBlockZ();
            Door door = (Door)event.getBlock().getState().getData();
             if (door.isTopHalf())
             	y = y - 1;
         	if (Inn.doorAlreadyExists(x,y,z) && !Inn.getOwner(x,y,z).equalsIgnoreCase(event.getPlayer().getName()) || IPermissions.permission(event.getPlayer(), "inn.bypass", event.getPlayer().isOp())){
         		event.getPlayer().sendMessage(ChatColor.RED + "[Inn] You doesn't own this door!");
         		event.setCancelled(true);
         	}else{
         		String query = "DELETE FROM doors WHERE x=" + x + " AND y=" + y + " AND z=" + z +"";
         		Inn.manageSQLite.deleteQuery(query);
         		event.getPlayer().sendMessage(ChatColor.RED + "[Inn] Door unregistered!");
         	}
     	}
     	
     }
 }
