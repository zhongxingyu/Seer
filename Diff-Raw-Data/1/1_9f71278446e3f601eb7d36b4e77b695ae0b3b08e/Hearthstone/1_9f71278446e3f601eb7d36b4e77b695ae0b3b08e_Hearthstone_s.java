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
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.PersistenceException;
 
 import name.richardson.james.bukkit.utilities.command.CommandManager;
 import name.richardson.james.bukkit.utilities.internals.Logger;
 import name.richardson.james.bukkit.utilities.plugin.SimplePlugin;
 import name.richardson.james.hearthstone.general.SetCommand;
 import name.richardson.james.hearthstone.general.TeleportCommand;
 
 public class Hearthstone extends SimplePlugin {
 
   private CommandManager commandManager;
   private DatabaseHandler database;
   private HearthstoneConfiguration configuration;
   private final Map<String, Long> cooldown = new HashMap<String, Long>();
 
   public Map<String, Long> getCooldownTracker() {
     return this.cooldown;
   }
 
   @Override
   public List<Class<?>> getDatabaseClasses() {
     return DatabaseHandler.getDatabaseClasses();
   }
 
   public DatabaseHandler getDatabaseHandler() {
     return this.database;
   }
 
   public HearthstoneConfiguration getHearthstoneConfiguration() {
     return this.configuration;
   }
 
   @Override
   public void onEnable() {
     try {
      Logger.setDebugging(this, true);
       this.setLoggerPrefix();
       this.loadConfiguration();
       this.setRootPermission();
       this.setResourceBundle();
       this.setupDatabase();
       this.registerCommands();
     } catch (final IOException e) {
       this.logger.severe("Unable to close file stream!");
       this.setEnabled(false);
     } catch (final SQLException e) {
       this.logger.severe(this.getMessage("unable-to-use-database"));
       this.setEnabled(false);
     } finally {
       if (!this.isEnabled()) {
         this.logger.severe(this.getMessage("panic"));
         return;
       }
     }
     this.logger.info(String.format(this.getMessage("plugin-enabled"), this.getDescription().getName()));
   }
 
   private void loadConfiguration() throws IOException {
     this.configuration = new HearthstoneConfiguration(this);
     if (configuration.isDebugging())
       Logger.setDebugging(this, true);
   }
 
   private void registerCommands() {
     this.commandManager = new CommandManager(this);
     this.getCommand("hs").setExecutor(this.commandManager);
     this.commandManager.addCommand(new SetCommand(this));
     TeleportCommand command = new TeleportCommand(this);
     this.commandManager.addCommand(command);
     this.getCommand("home").setExecutor(command);
   }
 
   private void setupDatabase() throws SQLException {
     try {
       this.getDatabase().find(HomeRecord.class).findRowCount();
     } catch (final PersistenceException ex) {
       this.logger.warning(this.getMessage("no-database"));
       this.installDDL();
     }
     this.database = new DatabaseHandler(this.getDatabase());
     this.logger.info(String.format(this.getMessage("homes-loaded"), this.database.count(HomeRecord.class)));
   }
 
 }
