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
 package net.daboross.bukkitdev.displaynamemessages;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author daboross
  */
 public class DisplayNameMessagesPlugin extends JavaPlugin {
 
     private MessageConfig mConfig;
 
     @Override
     public void onEnable() {
         mConfig = new MessageConfig(this);
        mConfig.loadConfig();
        getServer().getPluginManager().registerEvents(new MessageListener(this), this);
     }
 
     @Override
     public void onDisable() {
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         sender.sendMessage("DisplayNameMessages doesn't know about the command /" + cmd.getName());
         return true;
     }
 
     public MessageConfig getMConfig() {
         return mConfig;
     }
 }
