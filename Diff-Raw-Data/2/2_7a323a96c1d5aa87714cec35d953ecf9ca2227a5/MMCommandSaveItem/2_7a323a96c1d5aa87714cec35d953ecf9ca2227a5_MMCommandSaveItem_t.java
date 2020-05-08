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
 
 import java.io.File;
 import java.io.IOException;
 import java.util.regex.Pattern;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.forgenz.mobmanager.P;
 
 public class MMCommandSaveItem extends MMCommand
 {
 	MMCommandSaveItem()
 	{
 		super(Pattern.compile("saveitem", Pattern.CASE_INSENSITIVE), Pattern.compile("^.*$"),
 				0, 0);
 	}
 
 	@Override
 	public void run(CommandSender sender, String maincmd, String[] args)
 	{
 		if (sender instanceof Player && !sender.hasPermission("mobmanager.admin"))
 		{
 			sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to use /mm saveitem");
 			return;
 		}
 		
 		if (!(sender instanceof Player))
 		{
 			sender.sendMessage(ChatColor.DARK_RED + "You can not use this command from console");
 			return;
 		}
 		
 		Player player = (Player) sender;
 		
 		ItemStack item = player.getItemInHand();
 		
 		if (item == null || item.getType() == Material.AIR)
 		{
 			sender.sendMessage("You are not holding anything?");
 			return;
 		}
 		
		String key = args.length >= 2 ? args[1] : String.valueOf(System.currentTimeMillis());
 		
 		File cfgFile = new File(P.p().getDataFolder(), "items.yml");
 		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(cfgFile);
 		
 		cfg.set(key, item.serialize());
 		
 		try
 		{
 			cfg.save(cfgFile);
 		}
 		catch (IOException e)
 		{
 			sender.sendMessage(ChatColor.DARK_RED + "Error occured when saving items file: " + e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public String getUsage()
 	{
 		return "%s/%s %s%s [ItemKey]";
 	}
 
 	@Override
 	public String getDescription()
 	{
 		return "Saves the item you are holding into a items.yml for use in other configs.";
 	}
 
 	@Override
 	public String getAliases()
 	{
 		return "saveitem";
 	}
 }
