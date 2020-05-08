 /*
     This file is part of SudoAlias.
 
     SudoAlias is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     SudoAlias is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with SudoAlias.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.mike724.sudoalias;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
 
 @SuppressWarnings("unused")
 public class AliasListener implements Listener {
 
    @EventHandler(priority = EventPriority.LOWEST)
     public void handleCommandEvent(PlayerCommandPreprocessEvent e) {
         SudoAlias plugin = SudoAlias.getInstance();
         for (Alias alias : plugin.aliases) {
             if (alias.isMatch(e.getMessage())) {
                 plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                         new AliasExecutor(alias, e.getMessage(), e.getPlayer(), e.getPlayer().getName()));
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handleServerCommandEvent(ServerCommandEvent e) {
        SudoAlias plugin = SudoAlias.getInstance();
        String cmd = "/"+e.getCommand();
        for (Alias alias : plugin.aliases) {
            if (alias.isMatch(cmd)) {
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                        new AliasExecutor(alias, cmd, e.getSender(), e.getSender().getName()));
                //No setCancelled, have to be "hacky"
                e.setCommand("sudoalias nothing");
                 return;
             }
         }
     }
 
 }
