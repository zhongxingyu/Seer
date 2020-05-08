 /* 
  * Copyright (C) 2013 Dr Daniel R. Naylor
  * 
  * This file is part of mcMMO Party Admin.
  *
  * mcMMO Party Admin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * mcMMO Party Admin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with mcMMO Party Admin.  If not, see <http://www.gnu.org/licenses/>.
  * 
  **/
 package uk.co.drnaylor.mcmmopartyadmin;
 
 import uk.co.drnaylor.mcmmopartyadmin.commands.PartyAdminCommand;
 import uk.co.drnaylor.mcmmopartyadmin.commands.PartySpyCommand;
 import uk.co.drnaylor.mcmmopartyadmin.listeners.PartyChangeListener;
 import uk.co.drnaylor.mcmmopartyadmin.listeners.PartyChatListener;
 import com.gmail.nossr50.datatypes.party.Party;
 import com.gmail.nossr50.locale.LocaleLoader;
 import org.bukkit.plugin.java.JavaPlugin;
 import com.gmail.nossr50.mcMMO;
 import com.gmail.nossr50.party.PartyManager;
 import com.gmail.nossr50.util.player.UserManager;
 import java.lang.reflect.Method;
 import java.util.logging.Level;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.plugin.Plugin;
 import uk.co.drnaylor.mcmmopartyadmin.commands.FixPartiesCommand;
 import uk.co.drnaylor.mcmmopartyadmin.locales.L10n;
 
 public class PartyAdmin extends JavaPlugin {
 
     public static PartyAdmin plugin;
     public static mcMMO mcmmo;
     private PartyChangeListener pa;
     private PartyChatListener pc;
     private PartySpy ps;
 
 
     @Override
     public void onEnable() {
         plugin = this;
         if (!isMcmmoAvailable()) {
             this.getServer().getLogger().severe(L10n.getString("Enable.NotFound"));
             this.getPluginLoader().disablePlugin(this);
             return;
         }
         this.getServer().getLogger().info(L10n.getString("Enable.Hooked"));
 
         this.getServer().getLogger().info(L10n.getString("Enable.Checking"));
         
         if (!checkForRequiredMethod()) {
             this.getServer().getLogger().severe(L10n.getString("Enable.CheckFailed"));
             this.getPluginLoader().disablePlugin(this);
             return;
         }
         this.getServer().getLogger().info(L10n.getString("Enable.CheckSucceeded"));
         pa = new PartyChangeListener();
         pc = new PartyChatListener();
 
         getServer().getPluginManager().registerEvents(pa, this);
         getServer().getPluginManager().registerEvents(pc, this);
 
         getCommand("pa").setExecutor(new PartyAdminCommand());
         getCommand("partyspy").setExecutor(new PartySpyCommand());
         getCommand("fixparties").setExecutor(new FixPartiesCommand());
         
         this.reloadConfig();
         ps = new PartySpy(plugin.getConfig().getStringList("partyspy"));
         
        this.getServer().getLogger().log(Level.INFO, L10n.getString("Enable.CheckSucceeded", this.getDescription().getVersion()));
     }
 
     @Override
     public void onDisable() {
         this.getServer().getLogger().info(L10n.getString("Disable.Complete", this.getDescription().getVersion()));
     }
 
     /**
      * Checks to see if the required non-API methods are available in mcMMO.
      * 
      * @return true if so, false otherwise
      */
     private boolean checkForRequiredMethod() {
    
         // Reflection!
         try {            
             Method m = PartyManager.class.getMethod("disbandParty", new Class[]{Party.class});
             Method n = UserManager.class.getMethod("getPlayer", new Class[]{OfflinePlayer.class});
             Method o = LocaleLoader.class.getMethod("getCurrentLocale");
             return ((m != null) && (n != null) && (o != null));
         } catch (Exception e) {
             // doesn't matter
         }
         return false;
     }
 
     /**
      * Checks to see if mcMMO is loaded on the server. This is a somewhat redundant check, as Bukkit will
      * do that for us, but it also provides the plugin object.
      * 
      * @return true if it is.
      */
     private boolean isMcmmoAvailable() {
         // Checking for mcMMO, just in case
         Plugin plugin = this.getServer().getPluginManager().getPlugin("mcMMO");
 
         //If we have found a plugin by the name of "mcMMO", check if it is actually
         //mcMMO. If not, or if we didn't find it, then it's not loaded in.
         if (plugin == null || !(plugin instanceof mcMMO)) {
             return false; //Nope, it's not loaded.
         }
         mcmmo = (mcMMO) plugin;
         return true;
     }
     
     /**
      * Returns the Party Spy base class
      * 
      * @return PartySpy class
      */
     public PartySpy getPartySpyHandler() {
         return ps;
     }
 
 }
