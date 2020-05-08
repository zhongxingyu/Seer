 /*
  * Copyright 2013 Michael McKnight. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice, this list of
  *       conditions and the following disclaimer.
  *
  *    2. Redistributions in binary form must reproduce the above copyright notice, this list
  *       of conditions and the following disclaimer in the documentation and/or other materials
  *       provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * The views and conclusions contained in the software and documentation are those of the
  * authors and contributors and should not be interpreted as representing official policies,
  * either expressed or implied, of anybody else.
  */
 
 package com.forgenz.mobmanager.listeners.commands;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map.Entry;
 import java.util.regex.Pattern;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.forgenz.mobmanager.MobType;
 import com.forgenz.mobmanager.P;
 import com.forgenz.mobmanager.world.MMChunk;
 import com.forgenz.mobmanager.world.MMCoord;
 import com.forgenz.mobmanager.world.MMWorld;
 
 class MMCommandCount extends MMCommand
 {
 
 	MMCommandCount()
 	{
 		super(Pattern.compile("count", Pattern.CASE_INSENSITIVE),
 				Pattern.compile("^.*$", Pattern.CASE_INSENSITIVE),
 				0, 1);
 	}
 
 	@Override
 	public void run(CommandSender sender, String maincmd, String[] args)
 	{
 		if (sender instanceof Player && !sender.hasPermission("mobmanager.count"))
 		{
 			sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to use /mm count");
 			return;
 		}
 		
 		if (!super.validArgs(sender, maincmd, args))
 			return;
 		
 		Collection<MMWorld> worldList;
 		
 		if (args.length > 1)
 		{
 			MMWorld world = P.worlds.get(args[1]);
 			
 			if (world == null)
 			{
 				sender.sendMessage("The world '" + args[1] + "' does not exist or is inactive");
 				return;
 			}
 			
 			worldList = new ArrayList<MMWorld>();
 			
 			worldList.add(world);
 		}
 		else
 		{
 			worldList = P.worlds.values();
 		}
 		
 		if (worldList.size() == 0)
 		{
 			sender.sendMessage("[MobManger] No worlds were found");
 		}
 		
 		int totalMonsters = 0;
 		int totalAnimals = 0;
 		int totalWaterAnimals = 0;
 		int totalAmbient = 0;
 		int totalVillagers = 0;
 		
 		int totalMaxMonsters = 0;
 		int totalMaxAnimals = 0;
 		int totalMaxWaterAnimals = 0;
 		int totalMaxAmbient = 0;
 		int totalMaxVillagers = 0;
 		
 		int totalWorlds = 0;
 		int totalChunks = 0;
 
 		for (final MMWorld world : worldList)
 		{					
 			world.updateMobCounts();
 			
 			int numPlayers = 0;
 			for (final Entry<MMCoord, MMChunk> chunk : world.getChunks())
 				numPlayers += chunk.getValue().getNumPlayers();
 								
 			sender.sendMessage(String.format("%1$sWorld:%2$s%3$s, %1$sChunks:%2$s%4$d, %1$sPlayers:%2$s%5$d",
 					ChatColor.DARK_GREEN, ChatColor.AQUA, world.getWorld().getName(), world.getChunks().size(), numPlayers));
 			
 			sender.sendMessage(String.format("%1$sM:%2$s%4$d%3$s/%2$s%5$d, %1$sA:%2$s%6$d%3$s/%2$s%7$d, %1$sW:%2$s%8$d%3$s/%2$s%9$d, %1$sAm:%2$s%10$d%3$s/%2$s%11$d, %1$sV:%2$s%12$d%3$s/%2$s%13$d",
 					ChatColor.GREEN, ChatColor.AQUA, ChatColor.YELLOW,
 					world.getMobCount(MobType.MONSTER), world.maxMobs(MobType.MONSTER), 
 					world.getMobCount(MobType.ANIMAL), world.maxMobs(MobType.ANIMAL), 
 					world.getMobCount(MobType.WATER_ANIMAL), world.maxMobs(MobType.WATER_ANIMAL), 
 					world.getMobCount(MobType.AMBIENT), world.maxMobs(MobType.AMBIENT),
 					world.getMobCount(MobType.VILLAGER), world.maxMobs(MobType.VILLAGER)));
 			
 			if (args.length == 1)
 			{
 				totalMonsters += world.getMobCount(MobType.MONSTER);
 				totalAnimals += world.getMobCount(MobType.ANIMAL);
 				totalWaterAnimals += world.getMobCount(MobType.WATER_ANIMAL);
 				totalAmbient += world.getMobCount(MobType.AMBIENT);
 				totalVillagers += world.getMobCount(MobType.VILLAGER);
 				
 				totalMaxMonsters += world.maxMobs(MobType.MONSTER);
 				totalMaxAnimals += world.maxMobs(MobType.ANIMAL);
 				totalMaxWaterAnimals += world.maxMobs(MobType.WATER_ANIMAL);
 				totalMaxAmbient += world.maxMobs(MobType.AMBIENT);
				totalMaxVillagers += world.getMobCount(MobType.VILLAGER);
 				
 				++totalWorlds;
 				totalChunks += world.getChunks().size();
 			}
 		}
 		
 		if (args.length == 1)
 		{
 			int totalMobs = totalMonsters + totalAnimals + totalWaterAnimals + totalAmbient + totalVillagers;
 			int totalMaxMobs = totalMaxMonsters + totalMaxAnimals + totalMaxWaterAnimals + totalMaxAmbient + totalMaxVillagers;
 			
 			sender.sendMessage(String.format("%1$sTotals - Worlds:%2$s%3$d, %1$sChunks:%2$s%4$d, %1$sPlayers:%2$s%5$d",
 					ChatColor.GREEN, ChatColor.AQUA,
 					totalWorlds,
 					totalChunks,
 					P.p.getServer().getOnlinePlayers().length));
 			
 			sender.sendMessage(String.format("%1$sM:%2$s%4$d%3$s/%2$s%5$d, %1$sA:%2$s%6$d%3$s/%2$s%7$d, %1$sW:%2$s%8$d%3$s/%2$s%9$d, %1$sAm:%2$s%10$d%3$s/%2$s%11$d, %1$sV:%2$s%12$d%3$s/%2$s%13$d %1$sT:%2$s%14$d%3$s/%2$s%15$d",
 					ChatColor.GREEN, ChatColor.AQUA, ChatColor.YELLOW,
 					totalMonsters, totalMaxMonsters, 
 					totalAnimals, totalMaxAnimals, 
 					totalWaterAnimals, totalMaxWaterAnimals,
 					totalAmbient, totalMaxAmbient,
 					totalVillagers, totalMaxVillagers,
 					totalMobs, totalMaxMobs));
 		}
 	}
 
 	@Override
 	public String getUsage()
 	{
 		return "%s/%s %s %s[World]";
 	}
 
 	@Override
 	public String getDescription()
 	{
 		return "Displays a list of mob counts for each type of mob along with limits";
 	}
 
 	@Override
 	public String getAliases()
 	{
 		return "count";
 	}
 
 }
