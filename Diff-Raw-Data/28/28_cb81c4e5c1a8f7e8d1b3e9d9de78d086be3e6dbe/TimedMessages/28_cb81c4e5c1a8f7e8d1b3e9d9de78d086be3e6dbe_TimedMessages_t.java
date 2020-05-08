 /*******************************************************************************
  * Copyright (c) 2011 James Richardson.
  * 
  * TimedMessages.java is part of TimedMessages.
  * 
  * TimedMessages is free software: you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * TimedMessages is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * TimedMessages. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 
 package name.richardson.james.bukkit.timedmessages;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.configuration.ConfigurationSection;
 
 import name.richardson.james.bukkit.timedmessages.management.ReloadCommand;
 import name.richardson.james.bukkit.timedmessages.management.StartCommand;
 import name.richardson.james.bukkit.timedmessages.management.StatusCommand;
 import name.richardson.james.bukkit.timedmessages.management.StopCommand;
 import name.richardson.james.bukkit.timedmessages.random.RandomMessage;
 import name.richardson.james.bukkit.timedmessages.rotation.RotatingMessage;
 import name.richardson.james.bukkit.util.Logger;
 import name.richardson.james.bukkit.util.Plugin;
 import name.richardson.james.bukkit.util.Time;
 import name.richardson.james.bukkit.util.command.CommandManager;
 
 public class TimedMessages extends Plugin {
 
   public static final long START_DELAY = 30;
 
   private final Set<Message> timers = new HashSet<Message>();
   
   private List<ConfigurationSection> messages;
   private TimedMessagesConfiguration configuration;
   private CommandManager commandManager;
   private boolean timersStarted = false;
 
   public boolean isTimersStarted() {
     return timersStarted;
   }
 
   public void loadMessagesConfiguration() throws IOException {
     final MessagesConfiguration configuration = new MessagesConfiguration(this);
     this.messages = configuration.getConfigurationSections();
   }
 
   public void onDisable() {
     this.stopTimers();
     logger.info(String.format("%s is disabled.", this.getDescription().getName()));
   }
 
   public void onEnable() {
     logger.setPrefix("[TimedMessages] ");
 
     try {
       this.loadConfiguration();
       this.loadMessagesConfiguration();
       this.setPermission();
       this.startTimers(START_DELAY);
       this.registerCommands();
       logger.info(String.format("%d timers started.", messages.size()));
     } catch (IOException exception) {
       logger.severe("Unable to load configuration!");
     }
 
     logger.info(String.format("%s is enabled.", this.getDescription().getFullName()));
   }
 
   public void startTimers(long startDelay) {
     if (this.timersStarted) this.stopTimers();
     this.timersStarted = true;
     startDelay = startDelay * 20;
     for (ConfigurationSection section : messages) {
       Long milliseconds = Time.parseTime(section.getString("delay", "5m"));
       List<String> messages = section.getStringList("messages");
       String permission = section.getString("permission");
       String mode = section.getString("mode", "rotation");
      String worldName = section.getString("worldName");
       Message task;
       if (mode.equalsIgnoreCase("rotation")) {
        task = new RotatingMessage(this.getServer(), milliseconds, messages, permission, worldName);
       } else {
        task = new RandomMessage(this.getServer(), milliseconds, messages, permission, worldName);
       }
       this.getServer().getScheduler().scheduleSyncRepeatingTask(this, task, startDelay, task.getTicks());
       timers.add(task);
     }
   }
   
   public int getTimerCount() {
     return timers.size();
   }
 
   public void stopTimers() {
     this.timersStarted = false;
     this.timers.clear();
     this.getServer().getScheduler().cancelTasks(this);
   }
 
   private void loadConfiguration() throws IOException {
     this.configuration = new TimedMessagesConfiguration(this);
     if (this.configuration.getDebugging()) Logger.enableDebugging("timedmessages");
   }
 
   private void registerCommands() {
     commandManager = new CommandManager(this.getDescription());
     this.getCommand("tm").setExecutor(commandManager);
     commandManager.registerCommand("reload", new ReloadCommand(this));
     commandManager.registerCommand("start", new StartCommand(this));
     commandManager.registerCommand("status", new StatusCommand(this));
     commandManager.registerCommand("stop", new StopCommand(this));
   }
 
 }
