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
 import java.util.regex.Pattern;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 
 import com.forgenz.mobmanager.Config;
 import com.forgenz.mobmanager.MobType;
 import com.forgenz.mobmanager.P;
 import com.forgenz.mobmanager.world.MMChunk;
 import com.forgenz.mobmanager.world.MMWorld;
 
 public class MMCommandButcher extends MMCommand
 {
 
 	MMCommandButcher()
 	{
 		super(Pattern.compile("butcher|butcherall", Pattern.CASE_INSENSITIVE), Pattern.compile("^[a-z ]*$", Pattern.CASE_INSENSITIVE),
 				0, 5);
 	}
 
 	@Override
 	public void run(CommandSender sender, String maincmd, String[] args)
 	{
 		if (sender instanceof Player && !sender.hasPermission("mobmanager.butcher"))
 		{
 			sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to use /mm butcher");
 			return;
 		}
 		
 		if (!super.validArgs(sender, maincmd, args))
 			return;
 		
 		ArrayList<MobType> toRemove = new ArrayList<MobType>(1);
 		int numMobs = 0;
 		
 		if (args.length == 1)
 		{
 			toRemove.add(MobType.MONSTER);
 			toRemove.add(MobType.ANIMAL);
 			toRemove.add(MobType.AMBIENT);
 			toRemove.add(MobType.WATER_ANIMAL);
 		}
 		else if (args.length > 1)
 		{
 			for (int i = 1; i < args.length; ++i)
 			{
 				if (!addMobType(toRemove, args[i]))
 				{
 					sender.sendMessage(ChatColor.RED + "Invalid mob type '" + ChatColor.YELLOW + args[i] + ChatColor.RED + "'");
 					return;
 				}
 			}
 		}
 		
 		numMobs = removeMobs(toRemove, args[0].equalsIgnoreCase("butcherall"));
 		
 		sender.sendMessage(ChatColor.GRAY + "~Removed " + numMobs + " mobs");
 	}
 	
 	public int removeMobs(ArrayList<MobType> mobTypes, boolean removeAll)
 	{
 		int numMobs = 0;
 		
 		for (MMWorld world : P.worlds.values())
 		{
 			for (LivingEntity entity : world.getWorld().getLivingEntities())
 			{
 				MobType mob = MobType.valueOf(entity);
				if (mobTypes.contains(mob) && (removeAll || !Config.ignoredMobs.containsValue(entity.getType().toString())))
 				{
 					world.decrementMobCount(mob);
 					
 					if (mob == MobType.ANIMAL)
 					{
 						MMChunk chunk = world.getChunk(entity.getLocation().getChunk());
 						if (chunk != null)
 						{
 							chunk.changeNumAnimals(false);
 						}
 					}
 					entity.remove();
 					++numMobs;
 				}
 			}
 		}
 		
 		return numMobs;
 	}
 	
 	public boolean addMobType(ArrayList<MobType> toRemove, String type)
 	{
 		if (type.equalsIgnoreCase("monster"))
 		{
 			toRemove.add(MobType.MONSTER);
 		}
 		else if (type.equalsIgnoreCase("animal"))
 		{
 			toRemove.add(MobType.ANIMAL);
 		}
 		else if (type.equalsIgnoreCase("wateranimal"))
 		{
 			toRemove.add(MobType.WATER_ANIMAL);
 		}
 		else if (type.equalsIgnoreCase("ambient"))
 		{
 			toRemove.add(MobType.AMBIENT);
 		}
 		else if (type.equalsIgnoreCase("villager"))
 		{
 			toRemove.add(MobType.VILLAGER);
 		}
 		else
 		{
 			return false;
 		}
 		return true;
 	}
 	
 	@Override
 	public String getUsage()
 	{
 		return "%s/%s %s %s[MobTypes]";
 	}
 
 	@Override
 	public String getDescription()
 	{
 		return "Despawns entities from each world managed by MobManager";
 	}
 
 	@Override
 	public String getAliases()
 	{
 		return "butcher,butcherall";
 	}
 
 }
