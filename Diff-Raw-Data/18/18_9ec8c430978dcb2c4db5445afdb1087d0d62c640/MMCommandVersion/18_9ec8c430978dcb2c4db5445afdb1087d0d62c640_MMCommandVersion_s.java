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
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.forgenz.mobmanager.P;
 import com.forgenz.mobmanager.common.util.Updater;
 
 public class MMCommandVersion extends MMCommand
 {
 
 	MMCommandVersion()
 	{
 		super(Pattern.compile("^version$", Pattern.CASE_INSENSITIVE), Pattern.compile("^.*$"), 0, 0);
 	}
 
 	@Override
 	public void run(final CommandSender sender, String maincmd, String[] args)
 	{
 		if (sender instanceof Player && !sender.hasPermission("mobmanager.version"))
 		{
 			sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to use /mm version");
 			return;
 		}
 		
 		if (!super.validArgs(sender, maincmd, args))
 			return;
 		
 		final String versionString = P.p().getDescription().getVersion();
 		
 		sender.sendMessage(ChatColor.DARK_GREEN + P.p().getDescription().getName() + " version " + versionString);
 		
 		Updater updater = P.p().getUpdater();
 		
 		if (updater == null)
 		{
 			sender.sendMessage(ChatColor.DARK_GREEN + "Turn on EnableVersionCheck to allow checking for a new version");
 			return;
 		}
 		
 		
 		switch (updater.getResult())
 		{
		case FAIL_BADSLUG:
 		case FAIL_DBO:
 		case FAIL_NOVERSION:
 			sender.sendMessage(String.format("%sThe was an error when checking for an update. Please notify me. ERROR: %s", ChatColor.RED, updater.getResult()));
 			break;
 		case FAIL_DOWNLOAD:
			sender.sendMessage(String.format("%sA new version of MobManager 'v%s' is available from BukkitDev", ChatColor.DARK_GREEN, updater.getLatestVersionString()));
 			sender.sendMessage(String.format("%sAn attempt to download this new version failed :(", ChatColor.DARK_GREEN));
 			break;
 		case NO_UPDATE:
 			sender.sendMessage(String.format("%sMobManager is up to date!", ChatColor.DARK_GREEN));
 			break;
 		case SUCCESS:
			sender.sendMessage(String.format("%sA new version of MobManager 'v%s' has been downloaded", ChatColor.DARK_GREEN, updater.getLatestVersionString()));
 			sender.sendMessage(String.format("%sA restart or /reload is required to enable this new version", ChatColor.DARK_GREEN));
 			break;
 		case UPDATE_AVAILABLE:
			sender.sendMessage(String.format("%sA new version of MobManager 'v%s' is available from BukkitDev", ChatColor.DARK_GREEN, updater.getLatestVersionString()));
 			break;
 		}
 	}
 
 	@Override
 	public String getAliases()
 	{
 		return "version";
 	}
 
 	@Override
 	public String getUsage()
 	{
 		return "%s/%s %s%s";
 	}
 
 	@Override
 	public String getDescription()
 	{
 		return "Fetches version information, also checks for updates for MobManager";
 	}
 
 }
