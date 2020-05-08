 /*
  * This file is part of MineQuest-Citizens, MineQuest connector to CitizensAPI.
  * MineQuest-Citizens is licensed under GNU General Public License v3.
  * Copyright (C) 2012 teamIO + Contributors
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
 package net.teamio.minequest.citizens.command;
 
 import java.io.IOException;
 
 import net.teamio.minequest.citizens.MQAddonCitizens;
 import net.teamio.minequest.citizens.statistic.NPCStatisticUtils;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.theminequest.MineQuest.I18NMessage;
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.API.Utils.ChatUtils;
 import com.theminequest.MineQuest.Frontend.Command.CommandFrontend;
 
 public class NPCCommandFrontend extends CommandFrontend {
 
 	public NPCCommandFrontend() {
 		super("mqnpc");
 	}
 	
	public Boolean assign(CommandSender p, String[] args) {
 		CommandSender sender = p;
 		if (sender == null)
 			sender = Bukkit.getConsoleSender();
 		
 		if (args.length!=2){
 			sender.sendMessage(I18NMessage.getLocale().getString(CommandFrontend.IINVALID));
 			return false;
 		}
 		
 		Integer citizensid;
 		
 		try {
 			citizensid = Integer.parseInt(args[0]);
 		} catch (Exception e) {
 			sender.sendMessage(I18NMessage.getLocale().getString(CommandFrontend.IINVALID));
 			return false;
 		}
 
 		Player target = Bukkit.getPlayer(args[1]);
 		
 		if (target==null) {
 			sender.sendMessage(I18NMessage.getLocale().getString(CommandFrontend.IINVALID));
 			return false;
 		}
 		
 		NPCStatisticUtils.assignNPCToPlayer(target.getName(), citizensid);
 		sender.sendMessage("Assigned NPC to target.");
 		return true;
 	}
 	
	public Boolean deassign(CommandSender p, String[] args) {
 		CommandSender sender = p;
 		if (sender == null)
 			sender = Bukkit.getConsoleSender();
 		
 		if (args.length!=2){
 			sender.sendMessage(I18NMessage.getLocale().getString(CommandFrontend.IINVALID));
 			return false;
 		}
 		
 		Integer citizensid;
 		
 		try {
 			citizensid = Integer.parseInt(args[0]);
 		} catch (Exception e) {
 			sender.sendMessage(I18NMessage.getLocale().getString(CommandFrontend.IINVALID));
 			return false;
 		}
 
 		Player target = Bukkit.getPlayer(args[1]);
 		
 		if (target==null) {
 			sender.sendMessage(I18NMessage.getLocale().getString(CommandFrontend.IINVALID));
 			return false;
 		}
 		
 		NPCStatisticUtils.deassignNPCFromPlayer(target.getName(), citizensid);
 		sender.sendMessage("Deassigned NPC to target.");
 		return true;
 	}
 	
	public Boolean check(CommandSender p, String[] args) {
 		CommandSender sender = p;
 		if (sender == null)
 			sender = Bukkit.getConsoleSender();
 		
 		if (args.length!=1){
 			sender.sendMessage(I18NMessage.getLocale().getString(CommandFrontend.IINVALID));
 			return false;
 		}
 		
 		Integer citizensid;
 		
 		try {
 			citizensid = Integer.parseInt(args[0]);
 		} catch (Exception e) {
 			sender.sendMessage(I18NMessage.getLocale().getString(CommandFrontend.IINVALID));
 			return false;
 		}
 		
 		if (MQAddonCitizens.descriptionManager.activeNPCs.contains(citizensid)){
 			sender.sendMessage("This NPC gives quests.");
 		} else {
 			sender.sendMessage("This NPC does not give quests.");
 		}
 		
 		return true;
 	}
 	
	public Boolean toggle(CommandSender p, String[] args) {
 		CommandSender sender = p;
 		if (sender == null)
 			sender = Bukkit.getConsoleSender();
 		
 		if (args.length!=1){
 			sender.sendMessage(I18NMessage.getLocale().getString(CommandFrontend.IINVALID));
 			return false;
 		}
 		
 		Integer citizensid;
 		
 		try {
 			citizensid = Integer.parseInt(args[0]);
 		} catch (Exception e) {
 			sender.sendMessage(I18NMessage.getLocale().getString(CommandFrontend.IINVALID));
 			return false;
 		}
 		
 		if (MQAddonCitizens.descriptionManager.activeNPCs.contains(citizensid)){
 			MQAddonCitizens.descriptionManager.activeNPCs.remove(citizensid);
 			sender.sendMessage("This NPC will no longer give quests.");
 		} else {
 			MQAddonCitizens.descriptionManager.activeNPCs.add(citizensid);
 			try {
 				MQAddonCitizens.descriptionManager.getNPCDescription(citizensid);
 			} catch (IOException e) {
 				sender.sendMessage("Hmm... Is plugins/MineQuest/npc readable?");
 			}
 			sender.sendMessage("This NPC will now give quests.");
 			sender.sendMessage("You should configure what quests it gives and says in its .yaml file.");
 		}
 		
 		return true;
 	}
 
 	@Override
 	public void help(CommandSender sender, String[] args) {
 		
 		String[] send = {
 				ChatUtils.formatHeader("NPC Management"),
 				ChatUtils.formatHelp("mqnpc assign [npcid] [target]", "Assign a contact to a player."),
 				ChatUtils.formatHelp("mqnpc deassign [npcid] [target]", "Deassign a contact from a player."),
 				ChatUtils.formatHelp("mqnpc toggle [npcid]", "Toggle an NPC to give quests."),
 				ChatUtils.formatHelp("mqnpc check [npcid]", "Check if an NPC gives quests.")
 		};
 		
 		sender.sendMessage(send);
 
 	}
 
 	@Override
 	public boolean allowConsole() {
 		return true;
 	}
 
 	@Override
 	public void noOptionSpecified(CommandSender sender, String[] args) {
 		help(sender,args);
 	}
 
 }
