 /*
 * Copyright (c) 2013, LankyLord
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
 package net.lankylord.xraymachinenotifier.listeners;
 
 import net.lankylord.xraymachinenotifier.XrayMachineNotifier;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockPistonExtendEvent;
 
 public class XrayMachineListener implements Listener {
     private XrayMachineNotifier plugin;
 
     public XrayMachineListener(XrayMachineNotifier plugin) {
         this.plugin = plugin;
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onPistonExtend(final BlockPistonExtendEvent e) {
         Block block = e.getBlock().getRelative(e.getDirection()).getRelative(e.getDirection()).getRelative(BlockFace.DOWN);
         for (Player p : plugin.getServer().getOnlinePlayers()) {
             if (!p.hasPermission("xraymachinenotifier.exempt")) {
                 Location loc = p.getLocation();
                 if (loc.getBlockX() == block.getX() && loc.getBlockY() == block.getY() && loc.getBlockZ() == block.getZ()) {
                     alertAdminsOfXray(p.getName());
                 }
             }
         }
     }
 
     private void alertAdminsOfXray(String name) {
         for (Player p : plugin.getServer().getOnlinePlayers()) {
             if (p.hasPermission("xraymachinenotifier.notify")) {
                p.sendMessage(ChatColor.RED + "[XML]" + ChatColor.WHITE + name + " possibily used an xray machine.");
             }
         }
         plugin.getLogger().info(name + " possibily used an xray machine.");
     }
 }
