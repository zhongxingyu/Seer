 /*
  * This file is part of MineQuest, The ultimate MMORPG plugin!.
  * MineQuest is licensed under GNU General Public License v3.
  * Copyright (C) 2012 The MineQuest Team
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.theminequest.MQCoreEvents;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.API.CompleteStatus;
 import com.theminequest.MineQuest.API.Managers;
 import com.theminequest.MineQuest.API.Events.QuestEvent;
 import com.theminequest.MineQuest.API.Group.Group;
 import com.theminequest.MineQuest.API.Group.QuestGroup;
 
 public class RewardDamaged extends QuestEvent {
 	
 	private int item;
 	private short itemDurability = 100;
 	private boolean isDurableItem;
 	
 	/*
 	 * (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.QEvent#parseDetails(java.lang.String[])
 	 * [n] itemid,qty
 	 */
 	@Override
 	public void parseDetails(String[] details) {
		item = Integer.parseInt(details[0]);
		itemDurability = Short.parseShort(details[1]);	
 	}
 
 	@Override
 	public boolean conditions() {
 		return true;
 	}
 
 	@Override
 	public CompleteStatus action() {
 		QuestGroup g = Managers.getQuestGroupManager().get(getQuest());
 		for (Player p : g.getMembers()){
 			
 			ItemStack itemStack = new ItemStack(item);
 			itemStack.setDurability(itemDurability);
 			
 			p.getInventory().addItem(itemStack);
 		}
 		return CompleteStatus.SUCCESS;
 	}
 
 	public boolean isValidItem(){
 		
 		return false;
 	}
 	
 	@Override
 	public Integer switchTask() {
 		return null;
 	}
 
 }
