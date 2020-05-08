 /*
  * MCTrade
  * Copyright (C) 2012 Fogest <http://fogest.net16.net> and contributors
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
 
 package me.fogest.mctrade;
 
 import java.util.*;
 import java.util.Map.Entry;
 
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class AcceptTrade {
 	private Material tradeItem;
 	private int tradeId;
 	private int itemAmount = 0;
 	private int itemCost = 0;
 	private double itemDur = 0;
 	private String enchantments = "";
 	private Player player;
 	private ItemStack[] istack;
 	World world;
 	private MessageHandler m;
 
 	public AcceptTrade(int tradeId, Player player, MessageHandler m) {
 		this.m = m;
 		this.setTradeId(tradeId);
 		this.setPlayer(player);
 		itemAmount = DatabaseManager.getTradeAmount(getTradeId());
 		itemCost = DatabaseManager.getItemCost(getTradeId());
 		itemDur = DatabaseManager.getItemDur(getTradeId());
 		enchantments = DatabaseManager.getEnchantments(getTradeId());
 		DatabaseManager.acceptTrade(getTradeId());
 		getItemName();
 		world = player.getWorld();
 		ItemStack item = new ItemStack(getTradeItem(), itemAmount);
 		addItemsWithDrops(player, item);
 		
 	}
 
 	public Material getItemName() {
 		String itemString = DatabaseManager.getTradeItem(getTradeId());
 		setTradeItem(Material.getMaterial(itemString));
 		return getTradeItem();
 	}
 
 	public void addItemsWithDrops(Player p, ItemStack is) {
 		//Get ItemDurability
 			m.tellAll("Item Dur: " + itemDur); 
 			itemDur = (itemDur / 100);
			double itemDurability = (itemDur * is.getType().getMaxDurability());
			int finalItemDur = (int) Math.round(itemDurability);
 			is.setDurability((short) finalItemDur);
 			
 		//Add Enchants if any
 			if(!enchantments.isEmpty() && enchantments!=null) {
 				DatabaseManager.decodeEnchantments(p, is, enchantments);
 			}
 
 		//Give items	
 			HashMap<Integer, ItemStack> extra = p.getInventory().addItem(is);
 			Iterator<Entry<Integer, ItemStack>> it = extra.entrySet().iterator();
 			while (it.hasNext()) {
 				Map.Entry pairs = (Map.Entry) it.next();
 				world.dropItemNaturally(p.getLocation(), (ItemStack) pairs.getValue());
 				it.remove();
 			}
 	}
 
 	public String getUsername(int id) {
 		return DatabaseManager.getTradeUsername(id);
 	}
 
 	public Material getTradeItem() {
 		return tradeItem;
 	}
 
 	public void setTradeItem(Material tradeItem) {
 		this.tradeItem = tradeItem;
 	}
 
 	public int getTradeId() {
 		return tradeId;
 	}
 
 	public void setTradeId(int tradeId) {
 		this.tradeId = tradeId;
 	}
 
 	public Player getPlayer() {
 		return player;
 	}
 
 	public void setPlayer(Player player) {
 		this.player = player;
 	}
 
 	public int getAmount() {
 		return itemAmount;
 	}
 
 	public void setAmount(int itemAmount) {
 		this.itemAmount = itemAmount;
 	}
 }
