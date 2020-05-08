 /*
     MakeEmSpawn - This CraftBukkit plugin allows you to spawn entities from eggs
     Copyright (C) 2013  Hybris95
     hybris_95@hotmail.com
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.hybris.bukkit.makeemspawn;
 
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerEggThrowEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerEvent;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.inventory.ItemStack;
 
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.World;
 import java.util.List;
 
 import org.bukkit.entity.EntityType;
 
 import java.util.HashMap;
 
 import java.util.EnumSet;
 
 class MakeEmSpawnPlayerListener implements Listener {
     
 	private MakeEmSpawn plugin;
 	private HashMap<String,String> makeemspawners;
 	private HashMap<String,Byte> numbers;
 	
 	MakeEmSpawnPlayerListener(MakeEmSpawn plugin){
 		this.plugin = plugin;
 		this.makeemspawners = new HashMap<String,String>();
 		this.numbers = new HashMap<String,Byte>();
 	}
 	
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
 		
 		if(event.isCancelled()){return;}
 		
 		String command = event.getMessage();
 		Player player = event.getPlayer();
 		
 		if(command.toLowerCase().startsWith("/makeemspawn") || command.toLowerCase().startsWith("/mes")){
 			
 			String[] split = command.split(" ", 4);
 			
 			if(split.length < 2){
 				printUsage(player);
 				return;
 			}
 			
 			String option = split[1];
 			int optionInt = giveOptionInt(option);
 			
 			switch(optionInt){
 				case 0:				
 				default:
 					printUsage(player);
 					break;
 				
 				case 1:// makeemspawn
 					spawnCmd(split, player);
 					break;
 				
 				case 2:// unmakeemspawn
 					unspawnCmd(split, player);
 					break;
 				case 3:// list
 					listCmd(split, player);
 					break;
 			}
 			event.setCancelled(true);
 		}
 	}
 	
 	private void spawnCmd(String[] split, Player player)
 	{
 			boolean isMakeEmSpawner = makeemspawners.containsKey(player.getName());
 		
 			if(split.length < 3){
 				printUsage(player);
 				return;
 			}
 			
 			String possibleNumberOfMobs = "1";
 			byte numberOfMobs = (byte)(-1);
 			if(split.length >= 4){
 				possibleNumberOfMobs = split[3];
 				try{
 					numberOfMobs = Byte.parseByte(possibleNumberOfMobs);
 					if(numberOfMobs < (byte)1){
 						player.sendMessage("[MakeEmSpawn] Couldnt parse that number of entities (defaulted to previous or 1)");
 						numberOfMobs = (byte)(-1);
 					}
 				}
 				catch(NumberFormatException e){
 					player.sendMessage("[MakeEmSpawn] Couldnt parse that number of entities (defaulted to previous or 1)");
 					numberOfMobs = (byte)(-1);
 				}
 			}
 			
 			String mobName = split[2];
 			String creatureType = creatureType(mobName, player);
 			if(creatureType.equals("")){
 				printUsage(player);
 				return;
 			}
 			
			switch(creatureType.toLowerCase())
			{
			    case "leashknot":
			    case "painting":
			    case "thrownenderpearl":
			    case "itemframe":
			        printUsage(player);
			        return;
			    default:
			        break;
			}
			
 			makeemspawners.put(player.getName(),creatureType);
 			if(isMakeEmSpawner){
 				player.sendMessage("Next egg launched will spawn this mob: " + mobName);
 			}
 			else{
 				if(giveEgg(player)){
 					player.sendMessage("Next egg launched will spawn this mob: " + mobName);
 				}
 				else{
 					player.sendMessage("Get some place for an egg!");
 					makeemspawners.remove(player.getName());
 				}
 			}
 			
 			if(numberOfMobs != (byte)(-1)){
 				player.sendMessage("Next eggs launched will spawn " + numberOfMobs + " at once");
 				numbers.put(player.getName(),numberOfMobs);
 			}
 	}
 	
 	private void unspawnCmd(String[] split, Player player)
 	{
 			boolean isMakeEmSpawner = makeemspawners.containsKey(player.getName());
 		
 			if(!this.plugin.hasPermissions(player, "spawn")){
 				player.sendMessage("You cannot use this command");
 				return;
 			}
 			
 			if(isMakeEmSpawner){
 				removeEgg(player);
 				makeemspawners.remove(player.getName());
 			}
 			
 			String mobFilter = "";
 			if(split.length >= 3)
 			{
 				mobFilter = split[2];
 			}
 			
 			List<World> worlds = this.plugin.getServer().getWorlds();
 			for(World world : worlds){
 				List<LivingEntity> entities = world.getLivingEntities();
 				for(LivingEntity entity : entities){
 					if(!HumanEntity.class.isAssignableFrom(entity.getClass())){
 						if(!mobFilter.equals("")){
 							if(entity.getType().getName().toLowerCase().equals(mobFilter.toLowerCase()))
 							{
 								entity.setHealth(0);
 							}
 						}
 						else
 						{
 							entity.setHealth(0);
 						}
 					}
 				}
 			}
 			player.sendMessage("[MakeEmSpawn] You are not makeemspawned (anymore) and the worlds got cleaned from creatures");
 	}
 	
 	private void listCmd(String[] split, Player player)
 	{
 		if(!this.plugin.hasPermissions(player, "spawn")){
 			player.sendMessage("[MakeEmSpawn] You cannot use this command");
 			return;
 		}
 		
 		player.sendMessage("[MakeEmSpawn] Available entities :");
 		String listMsg = "";
 		for(EntityType type : EnumSet.allOf(EntityType.class))
 		{
 			if(type.getName() != null && type.isSpawnable())
 			{
    			switch(type.getName().toLowerCase())
    			{
    			    case "leashknot":
    			    case "painting":
    			    case "thrownenderpearl":
    			    case "itemframe":
    			        break;
    			    default:
				        listMsg += type.getName() + " ";
    			        break;
    			}
 			}
 		}
 		player.sendMessage(listMsg);
 	}
 	
 	private String creatureType(String mobName, Player player){
 		String creatureType = "";
 		mobName = mobName.toLowerCase();
 		if(plugin.hasPermissions(player, mobName)){
             for(EntityType type : EnumSet.allOf(EntityType.class))
             {
 				if(type.getName() != null)
 				{
 					if(type.getName().toLowerCase().equals(mobName))
 					{
 						creatureType = type.getName();
 						break;
 					}
 				}
             }
             
             if(creatureType.equals(""))
             {
                 player.sendMessage("Unsupported Creature");
             }
 		}
 		else{
 			player.sendMessage("Unsupported Creature or Unsufficent permissions");
 		}
 		return creatureType;
 	}
 	
 	private boolean giveEgg(Player player){
 		PlayerInventory inv = player.getInventory();
 		HashMap<Integer,ItemStack> couldntFit = inv.addItem(new ItemStack(344, 1)); // 344 = Egg Id
 		if(couldntFit.isEmpty()){
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 	
 	private boolean removeEgg(Player player){
 		PlayerInventory inv = player.getInventory();
 		HashMap<Integer,ItemStack> couldntBeRemoved = inv.removeItem(new ItemStack(344, 1));
 		if(couldntBeRemoved.isEmpty()){
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 	
 	private int giveOptionInt(String option){
 		if(option.startsWith("s") || (option.startsWith("spawn"))){
 			return 1;
 		}
 		else if(option.startsWith("us") || (option.startsWith("unspawn"))){
 			return 2;
 		}
 		else if(option.startsWith("list") || (option.startsWith("l"))){
 			return 3;
 		}
 		return 0;
 	}
 	
 	private void printUsage(Player player){
 		player.sendMessage("/<makeemspawn|mes> <<s|spawn>|<us|unspawn>> [<mobname> [numberOfMobs]]");
 		player.sendMessage("mobnames:");
 		player.sendMessage("chicken|cow|pig|sheep|creeper|skeleton|spider|zombie|squid");
 	}
 	
     @EventHandler
 	public void onPlayerEggThrow(PlayerEggThrowEvent event){		
 		Player player = event.getPlayer();
 		if(makeemspawners.containsKey(player.getName())){
 			try{
 				EntityType type = EntityType.fromName(makeemspawners.get(player.getName()).toUpperCase());
 				event.setHatchingType(type);
 				event.setHatching(true);
 			
 				Byte number = numbers.get(player.getName());
 				if(number != null){
 					event.setNumHatches(number);
 				}
 				else{
 					event.setNumHatches((byte)1);
 				}
 			}
 			catch(IllegalArgumentException e)
 			{
 				player.sendMessage("Could not spawn the given Entity");
 			}
 			finally
 			{
 				makeemspawners.remove(player.getName());
 			}
 		}
 	}
 	
     @EventHandler
 	public void onPlayerDropItem(PlayerDropItemEvent event){
 		if(event.isCancelled()){return;}
 		
 		Player player = event.getPlayer();
 		if(makeemspawners.containsKey(player.getName())){
 			ItemStack items = event.getItemDrop().getItemStack();
 			if(items.getTypeId() == 344){ // 344 = Egg Id
 				makeemspawners.remove(player.getName());
 				player.sendMessage("You are not a makeemspawner anymore");
 			}
 		}
 	}
 	
     @EventHandler
 	public void onPlayerRespawn(PlayerRespawnEvent event){	
 		Player player = event.getPlayer();
 		if(makeemspawners.containsKey(player.getName())){
 			giveEgg(player);
 		}
 	}
 	
     @EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event){
 		Player player = event.getPlayer();
 		
 		if(makeemspawners.containsKey(player.getName())){
 			makeemspawners.remove(event.getPlayer().getName());
 			removeEgg(player);
 		}
 	}
 	
 }
