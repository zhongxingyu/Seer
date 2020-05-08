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
 
 package com.forgenz.mobmanager.commands;
 
 import java.util.regex.Pattern;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 
 import com.forgenz.mobmanager.P;
 import com.forgenz.mobmanager.abilities.abilities.AbilitySet;
 import com.forgenz.mobmanager.common.util.ExtendedEntityType;
 
 public class MMCommandSpawn extends MMCommand
 {
 	
 	MMCommandSpawn()
 	{
		super(Pattern.compile("spawn|spawnset", Pattern.CASE_INSENSITIVE), Pattern.compile("^([a-zA-Z_]+ \\d{1,2}|[a-zA-Z_]+ \\d{1,2} [a-zA-Z_]+ (-?\\d+ ){2}-?\\d)$"),
 				2, 6);
 	}
 
 	@Override
 	public void run(CommandSender sender, String maincmd, String[] args)
 	{
 		if (sender instanceof Player && !sender.hasPermission("mobmanager.spawn"))
 		{
 			sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to use /mm spawn");
 			return;
 		}
 		
 		if (!super.validArgs(sender, maincmd, args))
 			return;
 		
 		Location loc = getLocation(sender, args);
 		
 		if (loc == null)
 			return;
 		
 		if (loc.getChunk() == null || !loc.getChunk().isLoaded())
 		{
 			sender.sendMessage(ChatColor.RED + "~Can't spawn mob in unloaded chunk");
 			return;
 		}
 		
 		int count = Integer.valueOf(args[2]);
 		
 		if (args[0].equalsIgnoreCase("spawn"))
 			spawn(sender, args[1], loc, count);
 		else
 			spawnset(sender, args[1], loc, count);
 	}
 	
 	private Location getLocation(CommandSender sender, String[] args)
 	{
 		if (args.length == 7)
 		{
 			World world = P.p().getServer().getWorld(args[3]);
 			
 			if (world == null)
 			{
 				sender.sendMessage(ChatColor.RED + "~No world named " + args[3]);
 				return null;
 			}
 			
 			return new Location(world, Integer.valueOf(args[4]), Integer.valueOf(args[5]), Integer.valueOf(args[6]));
 		}
 		else if (args.length == 3)
 		{
 			if (sender instanceof Player == false)
 			{
 				sender.sendMessage(ChatColor.RED + "~Use /mm " + args[0] + " <MobType|SetName> <count> [world] [x] [y] [z] from console");
 				return null;
 			}
 			
 			return ((Player) sender).getLocation();
 		}
 		else
 		{
 			sender.sendMessage(ChatColor.RED + "~Check /mm help for information on how to use this command");
 			return null;
 		}
 	}
 
 	protected static void spawn(CommandSender sender, String mob, Location loc, int count)
 	{		
 		ExtendedEntityType entityType = ExtendedEntityType.get(mob);
 		
 		if (entityType == null)
 		{
 			sender.sendMessage(ChatColor.RED + "~No mob type named " + mob + " see /mm mobtypes");
 			return;
 		}
 		
 		for (int i = 0; i < count; ++i)
 		{
 			P.p().limiterIgnoreNextSpawn(true);
 			
 			Entity entity = entityType.spawnMob(loc);
 			
 			if (entity == null)
 			{
 				sender.sendMessage(ChatColor.RED + "~Failed to spawn entity");
 				return;
 			}
 		}
 		
 		sender.sendMessage(ChatColor.GRAY + "~" + mob + " was spawned");
 	}
 	
 	protected static void spawnset(CommandSender sender, String mob, Location loc, int count)
 	{
 		if (!P.p().isAbiltiesEnabled())
 		{
 			sender.sendMessage(ChatColor.RED + "~Abilities must be enabled to spawn AbilitySet Mobs");
 			return;
 		}
 		
 		AbilitySet set = AbilitySet.getAbilitySet(mob);
 		
 		if (set == null)
 		{
 			sender.sendMessage(ChatColor.RED + "~No AbilitySet named " + mob);
 			return;
 		}
 		
 		ExtendedEntityType entityType = set.getAbilitySetsEntityType();
 		
 		if (entityType == null)
 		{
 			sender.sendMessage(ChatColor.RED + "~Failed to find AbilitySets entity type");
 			return;
 		}
 		
 		
 		for (int i = 0; i < count; ++i)
 		{
 			// Make sure the mob spawns without any abilities
 			P.p().ignoreNextSpawn(true);
 			P.p().abilitiesIgnoreNextSpawn(true);
 			P.p().limiterIgnoreNextSpawn(true);
 			
 			Entity entity = entityType.spawnMob(loc);
 			
 			if (entity == null)
 			{
 				sender.sendMessage(ChatColor.RED + "~Failed to spawn entity");
 				return;
 			}
 			
 			if (entity instanceof LivingEntity)
 				set.addAbility((LivingEntity) entity);
 		}
 		
 		sender.sendMessage(ChatColor.GRAY + "~" + mob + " was spawned");
 	}
 
 	@Override
 	public String getUsage()
 	{
 		return "%s/%s %s %s<MobType|SetName> <count> [world] [x] [y] [z]";
 	}
 
 	@Override
 	public String getDescription()
 	{
 		return "Spawns a given mob or mob with an abilityset";
 	}
 
 	@Override
 	public String getAliases()
 	{
 		return "spawn,spawnset";
 	}
 
 }
