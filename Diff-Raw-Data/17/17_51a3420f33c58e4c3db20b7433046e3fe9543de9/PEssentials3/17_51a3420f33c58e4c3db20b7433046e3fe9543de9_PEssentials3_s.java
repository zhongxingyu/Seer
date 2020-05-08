 /*
  * Copyright 2012-2013 James Geboski <jgeboski@gmail.com>
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
 
 package org.jgeboski.allowplayers.storage.plugin;
 
 import org.bukkit.plugin.Plugin;
 
 import net.ess3.api.IEssentials;
 import net.ess3.api.IUser;
 import net.ess3.bukkit.BukkitPlugin;
 import net.ess3.user.UserData;
 
 import org.jgeboski.allowplayers.storage.StorageException;
 import org.jgeboski.allowplayers.storage.StoragePlugin;
 
 public class PEssentials3 extends StoragePlugin<IEssentials>
 {
     public PEssentials3(Plugin plugin)
     {
         super(null);
         this.plugin = ((BukkitPlugin) plugin).getEssentials();
     }
 
     public String getIP(String player)
         throws StorageException
     {
         IUser u;
 
         u = getIUser(player);
        return u.getData().getIpAddress();
     }
 
     public void setIP(String player, String ip)
         throws StorageException
     {
         IUser u;
 
         u = getIUser(player);
        u.getData().setIpAddress(ip);
        u.queueSave();
     }
 
     private IUser getIUser(String player)
         throws StorageException
     {
         IUser u;
 
         u = plugin.getUserMap().getUser(player);
 
         if (u == null)
             throw new StorageException("Failed to obtain IUser");
 
         return u;
     }
 }
