 /*
  * Copyright (C) 2013 Dabo Ross <http://www.daboross.net/>
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
 package net.daboross.bungeedev.ncommon.commands;
 
 import java.util.Arrays;
 import net.daboross.bungeedev.ncommon.ColorList;
 import net.daboross.bungeedev.ncommon.config.SharedConfig;
 import net.md_5.bungee.api.CommandSender;
 import net.md_5.bungee.api.plugin.Command;
 
 public class ConfigCommand extends Command {
 
     private final SharedConfig config;
 
     public ConfigCommand(SharedConfig config) {
         super("config");
         this.config = config;
     }
 
     @Override
     public void execute(CommandSender sender, String[] args) {
         if (!sender.hasPermission("ncommon.config")) {
             sender.sendMessage(ColorList.ERR + "No permission.");
             return;
         }
         if (args.length == 1 && args[0].equals("list")) {
             sender.sendMessages("ping.players", "ping.version", "ping.maintenance", "ping.ping1", "ping.ping2");
         }
         if (args.length < 2) {
             sender.sendMessage(ColorList.ERR + "Not enough arguments.");
             sender.sendMessage(ColorList.CMD + "/config " + ColorList.ARGS + "<Key> <Value>");
         }
         if (args.length > 2) {
             config.getConfig().put(args[0], Arrays.asList(Arrays.copyOfRange(args, 1, args.length)));
         } else {
             config.getConfig().put(args[0], args[1]);
         }
     }
 }
