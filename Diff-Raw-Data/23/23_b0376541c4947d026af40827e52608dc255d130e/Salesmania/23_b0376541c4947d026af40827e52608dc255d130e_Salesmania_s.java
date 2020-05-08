 /*
 This file is part of Salesmania.
 
     Salesmania is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Salesmania is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Salesmania.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package net.invisioncraft.plugins.salesmania;
 
 import net.invisioncraft.plugins.salesmania.channels.ChannelManager;
 import net.invisioncraft.plugins.salesmania.commands.auction.AuctionCommandExecutor;
 import net.invisioncraft.plugins.salesmania.commands.salesmania.SalesmaniaCommandExecutor;
 import net.invisioncraft.plugins.salesmania.commands.stash.StashCommandExecutor;
 import net.invisioncraft.plugins.salesmania.configuration.*;
 import net.invisioncraft.plugins.salesmania.listeners.AuctionEventListener;
 import net.invisioncraft.plugins.salesmania.listeners.PlayerEventListener;
 import net.invisioncraft.plugins.salesmania.worldgroups.WorldGroupManager;
 import net.milkbowl.vault.economy.Economy;
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.HashSet;
 import java.util.logging.Logger;
 
 public class Salesmania extends JavaPlugin {
     public static Logger consoleLogger;
     private Economy economy;
     private Settings settings;
     private AuctionIgnoreList auctionIgnoreList;
     private LocaleHandler localeHandler;
     private HashSet<Configuration> configSet;
     private ItemStash itemStash;
     private WorldGroupManager worldGroupManager;
     private ChannelManager channelManager;
 
     @Override
     public void onEnable() {
         configSet = new HashSet<Configuration>();
         settings = new Settings(this);
         consoleLogger = this.getLogger();
         localeHandler = new LocaleHandler(this);
         auctionIgnoreList = new AuctionIgnoreList(this);
         itemStash = new ItemStash(this);
         worldGroupManager = new WorldGroupManager(this);
         channelManager = new ChannelManager(this);
 
         getCommand("auction").setExecutor(new AuctionCommandExecutor(this));
         getCommand("bid").setExecutor(getCommand("auction").getExecutor());
         getCommand("collect").setExecutor(new StashCommandExecutor(this));
         getCommand("salesmania").setExecutor(new SalesmaniaCommandExecutor(this));
 
        getServer().getPluginManager().registerEvents(new AuctionEventListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);

         // Vault
         if(getServer().getPluginManager().getPlugin("Vault") != null && getServer().getPluginManager().getPlugin("Vault").isEnabled()) {
             consoleLogger.info("Found Vault.");
             RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
             if(economyProvider != null) economy = economyProvider.getProvider();
             else {
                 consoleLogger.severe("No vault-supported economy plugin found.");
                 Bukkit.getServer().getPluginManager().disablePlugin(this);
             }
         }
         else {
             consoleLogger.severe("Vault not found.");
             Bukkit.getServer().getPluginManager().disablePlugin(this);
         }
 
         if(isEnabled()) consoleLogger.info("Salesmania Activated");
     }
 
     @Override
     public void onDisable() {
         consoleLogger.info("Disabled.");
     }
 
     public Settings getSettings() {
         return settings;
     }
 
     public LocaleHandler getLocaleHandler() {
         return localeHandler;
     }
 
     public void registerConfig(Configuration config) {
         configSet.add(config);
     }
 
     public void reloadConfig(CommandSender sender) {
         Locale locale = localeHandler.getLocale(sender);
         for(Configuration config : configSet) {
             consoleLogger.info(String.format(
                     locale.getMessage("Misc.reloadConfig"), config.getFilename()));
             sender.sendMessage(String.format(
                     locale.getMessage("Misc.reloadConfig"),
                     config.getFilename()));
             config.reload();
         }
     }
 
     @Override
     public void saveConfig() {
         Locale locale = localeHandler.getLocale(getServer().getConsoleSender());
         for(Configuration config : configSet) {
             config.save();
             consoleLogger.info(String.format(
                     locale.getMessage("Misc.saveConfig"), config.getFilename()));
         }
     }
 
     public AuctionIgnoreList getAuctionIgnoreList() {
         return auctionIgnoreList;
     }
 
     public Economy getEconomy() {
         return economy;
     }
 
     public ItemStash getItemStash() {
         return itemStash;
     }
 
     public WorldGroupManager getWorldGroupManager() {
         return worldGroupManager;
     }
 
     public ChannelManager getChannelManager() {
         return channelManager;
     }
 }
