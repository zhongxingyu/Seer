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
 	
	static MechanicsControl Plugin;
 	
 	/**
 	 * Passes the ChaosMod instance for static methods.
 	 * @param plugin ChaosMod plugin being passed in.
 	 */
 	
 	public BlockBreakListener(MechanicsControl plugin) 
 	{
		Plugin = plugin;
 	}
 	
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event)
 	{
		if(Plugin.getConfig().getConfigurationSection("XP").getKeys(false).contains(event.getBlock().getType().toString()))
 		{
			if(Plugin.getConfig().getInt("XP."+ event.getBlock().getType().toString()) != event.getExpToDrop())
			{
				if(event.getExpToDrop() != 0 || Plugin.getConfig().getBoolean("XP."+event.getBlock().getType().toString()+"FORCE",false))
				{
					event.setExpToDrop(Plugin.getConfig().getInt("XP."+ event.getBlock().getType().toString()));
				}
			}
 		}
 	}
 }
