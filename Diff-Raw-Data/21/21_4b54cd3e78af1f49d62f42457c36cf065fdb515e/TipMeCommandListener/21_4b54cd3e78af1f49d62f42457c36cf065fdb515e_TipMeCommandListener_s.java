 package net.visualillusionsent.minecraft.server.mod.canary.plugin.tipme;
 
 import net.canarymod.Canary;
 import net.canarymod.api.entity.living.humanoid.Player;
 import net.canarymod.chat.MessageReceiver;
 import net.canarymod.commandsys.Command;
 import net.canarymod.commandsys.CommandDependencyException;
 import net.canarymod.commandsys.CommandListener;
 import net.visualillusionsent.minecraft.server.mod.plugin.tipme.TipMeData;
 import net.visualillusionsent.utils.StringUtils;
 
 /**
  * TipMe
  * <p>
  * Copyright (C) 2013 Visual Illusions Entertainment.
  * <p>
  * This program is free software: you can redistribute it and/or modify it<br/>
  * under the terms of the GNU General Public License as published by the Free Software Foundation,<br/>
  * either version 3 of the License, or (at your option) any later version.
  * <p>
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;<br/>
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.<br/>
  * See the GNU General Public License for more details.
  * <p>
  * You should have received a copy of the GNU General Public License along with this program.<br/>
  * If not, see http://www.gnu.org/licenses/gpl.html
  * 
  * @version 2.0.0
  * @author Jason (darkdiplomat)
  */
 public final class TipMeCommandListener implements CommandListener{
     private TipMeData data;
 
     TipMeCommandListener(TipMe tipme) throws CommandDependencyException{
         this.data = tipme.tmd;
         Canary.commands().registerCommands(this, tipme, false);
     }
 
     @Command(aliases = { "tipme" },
             description = "TipMe command",
             permissions = { "tipme.admin" },
             toolTip = "/tipme <add|remove|getall|reload|tipserver> [index|tipmessage]")
     public void main(MessageReceiver msgrec, String[] args){
        msgrec.message("\u00A7CUsage: /tipme <add|remove|getall|reload|tipserver> [index|tipmessage] (index needed for removal, message need for adding)");
     }
 
     @Command(aliases = { "add" },
             description = "TipMe add tip command",
             permissions = { "tipme.admin" },
             toolTip = "/tipme add <tipmessage>",
             parent = "tipme")
     public void add(MessageReceiver msgrec, String[] args){
        String tip = StringUtils.joinString(args, " ", 1, args.length);
         if (data.createTip(tip)) {
             msgrec.message("\u00A72Tip Added!");
         }
         else {
            msgrec.message("\u00A7CFailed to add tip... Error has been logged.");
         }
     }
 
     @Command(aliases = { "remove" },
             description = "TipMe remove tip command",
             permissions = { "tipme.admin" },
             toolTip = "/tipme remove <index>",
             parent = "tipme")
     public void remove(MessageReceiver msgrec, String[] args){
        if (args.length > 2) {
             try {
                 int index = Integer.parseInt(args[1]);
                 boolean removed = data.removeTip(index);
                 msgrec.message(removed ? "\u00A74Tip Removed!" : "\u00A7cFailed to remove tip... Error has been logged.");
             }
             catch (NumberFormatException nfe) {
                msgrec.message("\u00A7CInvaild Tip #");
             }
         }
     }
 
     @Command(aliases = { "getall" },
             description = "TipMe getall command",
             permissions = { "tipme.admin" },
             toolTip = "/tipme getall",
             parent = "tipme")
     public void getall(MessageReceiver msgrec, String[] args){
         if (msgrec instanceof Player) {
             data.sendAll((Player) msgrec);
         }
         else {
             data.sendAllConsole();
         }
     }
 
     @Command(aliases = { "reload" },
             description = "TipMe reload command",
             permissions = { "tipme.admin" },
             toolTip = "/tipme reload",
             parent = "tipme")
     public void reload(MessageReceiver msgrec, String[] args){
         if (!data.reload()) {
            msgrec.message("\u00A7CFailed to reload tips... Error has been logged.");
         }
         else {
             msgrec.message("\u00A72Tips reloaded!");
         }
     }
 
     @Command(aliases = { "tipserver" },
             description = "TipMe tip server command",
             permissions = { "tipme.admin" },
             toolTip = "/tipme tipserver",
             parent = "tipme")
     public void tipserver(MessageReceiver msgrec, String[] args){
         data.sendTip();
     }
 }
