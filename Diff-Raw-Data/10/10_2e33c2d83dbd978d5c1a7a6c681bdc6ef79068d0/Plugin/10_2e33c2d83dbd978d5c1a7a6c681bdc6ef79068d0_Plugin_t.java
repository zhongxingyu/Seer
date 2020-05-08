 /*
  * This file is part of Plugin.
  *
  * Plugin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Plugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with SaveStopper.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /*
  * Author: Robert 'Bobby' Zenz
  * Website: http://www.bonsaimind.org
  * GitHub: https://github.com/RobertZenz/org.bonsaimind.bukkitplugins/tree/master/Plugin
  * E-Mail: bobby@bonsaimind.org
  */
 package org.bonsaimind.bukkitplugins.SaveStopper;
 
 import java.util.Timer;
 import java.util.TimerTask;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * 
  * @author Robert 'Bobby' Zenz
  */
 public class Plugin extends JavaPlugin {
 
 	private static final String CONFIG_FILE = "./plugins/SaveStopper/config.yml";
 	private Server server = null;
 	private boolean isSaving = true;
 	private Timer timer = new Timer(true);
 	private PlayerListener listener = new PlayerListener(this);
 	private Settings settings;
 
 	public void onDisable() {
 		settings.save();
 
 		timer.cancel();
 		timer = null;
 
 		listener = null;
 
 		server = null;
 	}
 
 	public void onEnable() {
 		server = getServer();
 
 		PluginManager pm = server.getPluginManager();
 		pm.registerEvent(Type.PLAYER_LOGIN, listener, Priority.Monitor, this);
 		pm.registerEvent(Type.PLAYER_QUIT, listener, Priority.Monitor, this);
 
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println(pdfFile.getName() + " " + pdfFile.getVersion() + " is enabled.");
 
 		setCommand();
 
 		settings = new Settings("./plugins/SaveStopper/config.yml");
 		settings.load();
 
 		if (settings.getDisableOnStart()) {
 			internalDisableSaving();
 		}
 	}
 
 	protected void check() {
 		// Clear out the timer to make sure that the event
 		// does not interrupt us.
 		timer.purge();
 
 		if (server.getOnlinePlayers().length == 0 && isSaving) {
 			saveOffScheduled();
		} else if (server.getOnlinePlayers().length > 0 && !isSaving) {
 			saveOn();
		} else {
			// Ohoh...something's wrong...*very* wrong...
			if(server.getOnlinePlayers().length == 0) {
				saveOffScheduled();
			} else {
				saveOn();
			}
 		}
 	}
 
 	/**
 	 * Disable saving.
 	 */
 	private void internalDisableSaving() {
 		if (isSaving && server.getOnlinePlayers().length == 0) {
 			println("Disabling saving.");
 
 			if (settings.getSaveAll()) {
 				CommandHelper.queueConsoleCommand(server, "save-all");
 			}
 
 			CommandHelper.queueConsoleCommand(server, "save-off");
 
 			isSaving = false;
 		}
 	}
 
 	private void println(String text) {
 		System.out.println("SaveStopper: " + text);
 	}
 
 	private void saveOff() {
 		println("Disabling saving.");
 
 		if (settings.getSaveAll()) {
 			CommandHelper.queueConsoleCommand(server, "save-all");
 		}
 
 		CommandHelper.queueConsoleCommand(server, "save-off");
 
 		isSaving = false;
 	}
 
 	private void saveOffScheduled() {
 		if (settings.getWait() > 0) {
 			println("Disabled scheduled, " + settings.getWait() + " seconds.");
 
 			timer.schedule(new TimerTask() {
 
 				@Override
 				public void run() {
 					saveOff();
 				}
 			}, settings.getWait() * 1000);
 		} else {
 			saveOff();
 		}
 	}
 
 	private void saveOn() {
 		println("Enabling saving.");
 
 		CommandHelper.queueConsoleCommand(server, "save-on");
 	}
 
 	private void setCommand() {
 		getCommand("savestopper").setExecutor(new CommandExecutor() {
 
 			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 				if (args.length == 0) {
 					return false;
 				}
 
 				for (String arg : args) {
 					if (arg.equalsIgnoreCase("check")) {
 						check();
 					} else if (arg.equalsIgnoreCase("save-off")) {
 						saveOff();
 					} else if (arg.equalsIgnoreCase("save-on")) {
 						saveOn();
 					} else if (arg.equalsIgnoreCase("start")) {
 						listener.setEnabled(true);
 					} else if (arg.equalsIgnoreCase("status")) {
 						println("Started: " + listener.isEnabled() + ", Saving: " + isSaving);
 					} else if (arg.equalsIgnoreCase("stop")) {
 						listener.setEnabled(false);
 					}
 				}
 
 				return true;
 			}
 		});
 	}
 }
