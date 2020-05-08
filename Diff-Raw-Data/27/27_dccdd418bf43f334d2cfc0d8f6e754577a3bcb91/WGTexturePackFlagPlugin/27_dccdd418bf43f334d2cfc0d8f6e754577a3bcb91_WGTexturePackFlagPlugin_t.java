 /*
  * Copyright (C) 2013 mewin<mewin001@hotmail.de>
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
 
 package de.mewin.wgtpf;
 
 import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import java.util.logging.Level;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author mewin<mewin001@hotmail.de>
  */
 public class WGTexturePackFlagPlugin extends JavaPlugin
 {
     public static final URLFlag TEXTUREPACK_FLAG = new URLFlag("texturepack");
     
     private WGCustomFlagsPlugin wgcf;
     private WorldGuardPlugin wgp;
     private RegionListener listener;
     
     @Override
     public void onEnable()
     {
         if (!getWGCF())
         {
             getLogger().log(Level.SEVERE, "Could not find WGCustomFlags.");
             getPluginLoader().disablePlugin(this);
             return;
         }
         else
         {
             getLogger().log(Level.INFO, "Hooked into WGCustomFlags.");
         }
         
         if (!getWorldGuard())
         {
             getLogger().log(Level.SEVERE, "Could not find WorldGuard.");
             getPluginLoader().disablePlugin(this);
             return;
         }
         else
         {
             getLogger().log(Level.INFO, "Hooked into WorldGuard.");
         }
         
        listener = new RegionListener(this, wgp);
         getServer().getPluginManager().registerEvents(listener, this);
         
         wgcf.addCustomFlag(TEXTUREPACK_FLAG);
     }
     
     private boolean getWGCF()
     {
         Plugin plug = getServer().getPluginManager().getPlugin("WGCustomFlags");
         if (plug == null || !(plug instanceof WGCustomFlagsPlugin))
         {
             return false;
         }
         else
         {
             wgcf = (WGCustomFlagsPlugin) plug;
             return true;
         }
     }
     
     private boolean getWorldGuard()
     {
         Plugin plug = getServer().getPluginManager().getPlugin("WorldGuard");
         if (plug == null || !(plug instanceof WorldGuardPlugin))
         {
             return false;
         }
         else
         {
             wgp = (WorldGuardPlugin) plug;
             return true;
         }
     }
 }
