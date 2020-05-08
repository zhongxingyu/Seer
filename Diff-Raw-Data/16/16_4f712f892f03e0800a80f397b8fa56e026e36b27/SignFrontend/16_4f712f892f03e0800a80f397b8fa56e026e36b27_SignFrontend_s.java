 /**
  * This file, SignInteractListener.java, is part of MineQuest:
  * A full featured and customizable quest/mission system.
  * Copyright (C) 2012 The MineQuest Team
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
  **/
 package com.theminequest.MineQuest.Frontend.QuestSign;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.getspout.commons.ChatColor;
 
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.Backend.BackendFailedException;
 import com.theminequest.MineQuest.Backend.QuestBackend;
 
 public class SignFrontend implements Listener {
 	
 	public SignFrontend(){
 		MineQuest.log("[SignFrontend] Starting Sign Frontends...");
 	}
 	
 	private boolean signCheck(Block block){
 		return block.getState() instanceof Sign;
 	}
 	
 	private boolean isQuestSign(Sign sign){
 		String[] line = sign.getLines();
 		if (line[1] != null && line[1].contentEquals("[Quest]")){
 			if(line[2] != null && !line[2].equals("")){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private String questName(Sign sign){
 		return sign.getLines()[2];
 	}
 	
 	
 	//Listeners For Sign interact and place. 
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerInteract(PlayerInteractEvent event){
 		Action action = event.getAction();
 		if (action != Action.RIGHT_CLICK_BLOCK){
 			return;
 		}
 		Block block = event.getClickedBlock();
 		Player player = event.getPlayer();
 		
 		if (signCheck(block)){
 			Sign sign = (Sign) block.getState();
 			if (isQuestSign(sign)){
 				String questName = questName(sign);
 				try {
 					QuestBackend.giveQuestToPlayer(player, questName);
 					QuestBackend.acceptQuest(player, questName);
 				} catch (BackendFailedException e) {
					block.breakNaturally();
					player.sendMessage(ChatColor.RED + "No such quest!");
 				} 
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onBlockPlace(BlockPlaceEvent event) {
 		Block block = event.getBlockAgainst();
 	    if (signCheck(block)){
 	    	Sign s = (Sign)block.getState();
 	    	if (isQuestSign(s)){
 	    		try {
 	    			QuestBackend.isRepeatable(s.getLine(2));
 	    		}catch (IllegalArgumentException e){
 	    			event.setCancelled(true);
 	    			event.getPlayer().sendMessage(ChatColor.RED + "No such quest!");
 	    		}
 	    	}
 	    }
 	    
 	}
 }
