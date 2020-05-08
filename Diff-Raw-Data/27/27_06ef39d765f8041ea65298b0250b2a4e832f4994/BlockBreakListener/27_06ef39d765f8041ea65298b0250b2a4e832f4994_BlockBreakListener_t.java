 /*
     Copyright (C) 2012  megagamer410
     Email: megagamer410@gmail.com
 
     This file is part of MechanicsControl.
 
     MechanicsControl is free software: you can redistribute it and/or modify
     it under the terms of the GNU Lesser General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     MechanicsControl is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public License
     along with MechanicsControl.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.gmail.megagamer410.MechanicsControl;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 
 public class BlockBreakListener implements Listener 
 {
 	/**
 	 * Necessary to get the server on static methods.
 	 */
 	
	MechanicsControl plugin;
 	
 	/**
 	 * Passes the ChaosMod instance for static methods.
 	 * @param plugin ChaosMod plugin being passed in.
 	 */
 	
 	public BlockBreakListener(MechanicsControl plugin) 
 	{
		this.plugin = plugin;
 	}
 	
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event)
 	{
		//If the block is not in the config, return
		if (!plugin.getConfig().getConfigurationSection("XP").getKeys(false).contains(event.getBlock().getType().toString())) {
			return;
		}
		//If the amount of XP matches the bukkit default return
		if (plugin.getConfig().getInt("XP."+ event.getBlock().getType().toString()) == event.getExpToDrop())
		{
			return;
		}
		//If XP is supposed to drop OR if the config specifies it should always apply
		if (event.getExpToDrop() != 0 || plugin.getConfig().getBoolean("XP."+event.getBlock().getType().toString()+"FORCE",false))
 		{
			event.setExpToDrop(plugin.getConfig().getInt("XP."+ event.getBlock().getType().toString()));
 		}
 	}
 }
