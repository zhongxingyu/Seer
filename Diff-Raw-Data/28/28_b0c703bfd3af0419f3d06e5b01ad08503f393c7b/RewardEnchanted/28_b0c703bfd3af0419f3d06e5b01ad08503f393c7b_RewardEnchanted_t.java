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
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.API.CompleteStatus;
 import com.theminequest.MineQuest.API.Managers;
 import com.theminequest.MineQuest.API.Events.QuestEvent;
 import com.theminequest.MineQuest.API.Group.Group;
 import com.theminequest.MineQuest.API.Group.QuestGroup;
 
 public class RewardEnchanted extends QuestEvent {
 	
 	private int item;
 	private int itemEnchant;
 	private short itemDurability = 100;
 	private boolean isDurableItem;
 	private boolean isEnchantable; 
 	private int enchantNumber;
 	private int enchantLevel;
 	
 	/*
 	 * (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.QEvent#parseDetails(java.lang.String[])
 	 * [n] itemid,qty
 	 */
 	@Override
 	public void parseDetails(String[] details) {
		item = Integer.parseInt(details[0]);
		
		enchantNumber = Integer.parseInt(details[1]);
		enchantLevel = Integer.parseInt(details[2]);
		itemDurability = Short.parseShort(details[3]);
 
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
 			itemStack.addEnchantment(enchant(enchantNumber), enchantLevel);
 			
 			p.getInventory().addItem(itemStack);
 		}
 		return CompleteStatus.SUCCESS;
 	}
 	
 	public Enchantment enchant(int enchantnumber){
 		if(enchantnumber == 1){
 			return Enchantment.ARROW_DAMAGE;
 		}
 		if(enchantnumber == 2){
 			return Enchantment.ARROW_FIRE;
 		}
 		if(enchantnumber == 3){
 			return Enchantment.ARROW_KNOCKBACK;
 		}
 		if(enchantnumber == 4){
 			return Enchantment.DAMAGE_ALL;
 		}
 		if(enchantnumber == 5){
 			return Enchantment.DAMAGE_ARTHROPODS;
 		}
 		if(enchantnumber == 6){
 			return Enchantment.DAMAGE_UNDEAD;
 		}
 		if(enchantnumber == 7){
 			return Enchantment.DIG_SPEED;
 		}
 		if(enchantnumber == 8){
 			return Enchantment.DURABILITY;
 		}
 		if(enchantnumber == 9){
 			return Enchantment.FIRE_ASPECT;
 		}
 		if(enchantnumber == 10){
 			return Enchantment.KNOCKBACK;
 		}
 		if(enchantnumber == 11){
 			return Enchantment.LOOT_BONUS_BLOCKS;
 		}
 		if(enchantnumber == 12){
 			return Enchantment.LOOT_BONUS_MOBS;
 		}
 		if(enchantnumber == 13){
 			return Enchantment.OXYGEN;
 		}
 		if(enchantnumber == 14){
 			return Enchantment.PROTECTION_ENVIRONMENTAL;
 		}
 		if(enchantnumber == 15){
 			return Enchantment.PROTECTION_EXPLOSIONS;
 		}
 		if(enchantnumber == 16){
 			return Enchantment.PROTECTION_FALL;
 		}
 		if(enchantnumber == 17){
 			return Enchantment.PROTECTION_PROJECTILE;
 		}
 		if(enchantnumber == 18){
 			return Enchantment.SILK_TOUCH;
 		}
 		if(enchantnumber == 19){
 			return Enchantment.WATER_WORKER;
 		}
 		return null; 
 		
 	}
 		
 	
 	@Override
 	public Integer switchTask() {
 		return null;
 	}
 
 }
