 /*******************************************************************************
  * Copyright (c) 2012 James Richardson.
  * 
  * Hearthstone.java is part of Hearthstone.
  * 
  * Hearthstone is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * Hearthstone is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * Hearthstone. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package name.richardson.james.hearthstone;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.PersistenceException;
 
 import com.avaje.ebean.EbeanServer;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.GlobalRegionManager;
 
 import name.richardson.james.bukkit.utilities.command.CommandManager;
 import name.richardson.james.bukkit.utilities.configuration.DatabaseConfiguration;
 import name.richardson.james.bukkit.utilities.formatters.ChoiceFormatter;
 import name.richardson.james.bukkit.utilities.persistence.SQLStorage;
 import name.richardson.james.bukkit.utilities.plugin.AbstractPlugin;
 import name.richardson.james.hearthstone.general.HomeCommand;
 import name.richardson.james.hearthstone.general.SetCommand;
 import name.richardson.james.hearthstone.general.TeleportCommand;
 
 public class Hearthstone extends AbstractPlugin {
 
   /* The backing store for Hearthstone */
   private SQLStorage database;
   
   /* Configuration for the plugin */
   private HearthstoneConfiguration configuration;
   
   /* Cooldown tracker for the plugin */
   private final Map<String, Long> cooldown = new HashMap<String, Long>();
   
   /* Reference to the WorldGuard plugin if loaded */
   private WorldGuardPlugin worldGuard;
 
   public Map<String, Long> getCooldownTracker() {
     return this.cooldown;
   }
 
   @Override
   public List<Class<?>> getDatabaseClasses() {
     final List<Class<?>> classes = new LinkedList<Class<?>>();
     classes.add(HomeRecord.class);
     return classes;
   }
 
   @Override
   protected void establishPersistence() throws SQLException {
     try {
       this.database = new SQLStorage(this, new DatabaseConfiguration(this), this.getDatabaseClasses());
       this.database.initalise();
     } catch (IOException e) {
       e.printStackTrace();
     }
   }
   
   @Override
   public EbeanServer getDatabase() {
     return this.database.getEbeanServer();
   }
   
   public HearthstoneConfiguration getHearthstoneConfiguration() {
     return this.configuration;
   }
   
   public GlobalRegionManager getGlobalRegionManager() {
     if (this.worldGuard != null) {
       return this.worldGuard.getGlobalRegionManager();
     } else {
       return null;
     }
   }
   
 
   private void connectToWorldGuard() {
     this.worldGuard = (WorldGuardPlugin) this.getServer().getPluginManager().getPlugin("WorldGuard");
     if (this.worldGuard != null) {
      this.getCustomLogger().debug(this, this.getLocalisation().getMessage(this, "worldguard-hooked", this.worldGuard.getDescription().getFullName()));
     }
   }
 
   protected void loadConfiguration() throws IOException {
     super.loadConfiguration();
     this.configuration = new HearthstoneConfiguration(this);
     this.connectToWorldGuard();
   }
 
   protected void registerCommands() {
     CommandManager commandManager = new CommandManager(this);
     this.getCommand("hs").setExecutor(commandManager);
     SetCommand setCommand = new SetCommand(this);
     commandManager.addCommand(setCommand);
     TeleportCommand teleportCommand = new TeleportCommand(this);
     commandManager.addCommand(teleportCommand);
     this.getCommand("home").setExecutor(new HomeCommand(this, teleportCommand, setCommand));
   }
 
   public String getArtifactID() {
     return "hearthstone";
   }
 
   public String getGroupID() {
     return "name.richardson.james.bukkit";
   }
 
 }
