 package com.adamki11s.npcs.io;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.inventory.ItemStack;
 
 import com.adamki11s.data.ItemStackDrop;
 import com.adamki11s.npcs.NPCHandler;
 import com.adamki11s.npcs.SimpleNPC;
 import com.adamki11s.questx.QuestX;
 
 
 public class NPCTemplate {
 	
 	final String name;
 	final ChatColor nameColour;
 	final boolean moveable, attackable, aggressive;
 	final int minPauseTicks, maxPauseTicks, maxVariation, respawnTicks, maxHealth, damageMod;
 	final double retalliationMultiplier;
 	final ItemStackDrop inventory;
 	final ItemStack[] gear;//boots 1, legs 2, chest 3, head 4, arm 5
 
 	public NPCTemplate(String name, ChatColor nameColour, boolean moveable, boolean attackable, boolean aggressive, int minPauseTicks,
 			int maxPauseTicks, int maxVariation, int health, int respawnTicks, ItemStackDrop inventory, ItemStack[] gear, int damageMod, double retalliationMultiplier) {
 		this.name = name;
 		this.nameColour = nameColour;
 		this.moveable = moveable;
 		this.attackable = attackable;
 		this.aggressive = aggressive;
 		this.minPauseTicks = minPauseTicks;
 		this.maxPauseTicks = maxPauseTicks;
 		this.maxVariation = maxVariation;
 		this.maxHealth = health;
 		this.respawnTicks = respawnTicks;
 		this.inventory = inventory;
 		this.gear = gear;
 		this.damageMod = damageMod;
 		this.retalliationMultiplier = retalliationMultiplier;
 	}
 	
 	
 	//int health, int respawnTicks, ItemStackDrop inventory, ItemStack[] gear, int damageMod, double retalliationMultiplier) {
 	public void registerSimpleNPCFixedSpawn(NPCHandler handle, Location fixedLocation){
 		SimpleNPC snpc = new SimpleNPC(handle, name, nameColour, moveable, attackable, aggressive, minPauseTicks, maxPauseTicks, maxVariation, maxHealth, respawnTicks, inventory, gear, damageMod, retalliationMultiplier);
 		snpc.setFixedLocation(fixedLocation);
		snpc.setNewSpawnLocation(fixedLocation);
 		snpc.spawnNPC();
 	}
 	
 	public void addSimpleNPCToWaitingList(NPCHandler handle){
 		QuestX.logMSG("Adding new simple npc to list");
 		SimpleNPC snpc = new SimpleNPC(handle, name, nameColour, moveable, attackable, aggressive, minPauseTicks, maxPauseTicks, maxVariation, maxHealth, respawnTicks, inventory, gear, damageMod, retalliationMultiplier);
 		handle.addToWaitingList(snpc);
 	}
 
 }
