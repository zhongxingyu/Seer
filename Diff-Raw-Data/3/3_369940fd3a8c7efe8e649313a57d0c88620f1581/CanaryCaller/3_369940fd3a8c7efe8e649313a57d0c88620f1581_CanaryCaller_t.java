 /*
  * This file is part of GreatmancodeTools.
  *
  * Copyright (c) 2013-2013, Greatman <http://github.com/greatman/>
  *
  * GreatmancodeTools is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * GreatmancodeTools is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with GreatmancodeTools.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.greatmancode.tools.caller.canary;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Logger;
 
 import com.greatmancode.tools.commands.canary.CanaryCommandReceiver;
 import com.greatmancode.tools.commands.interfaces.CommandReceiver;
 import com.greatmancode.tools.interfaces.Caller;
 import com.greatmancode.tools.interfaces.CanaryLoader;
 import com.greatmancode.tools.interfaces.Loader;
 
 import net.canarymod.Canary;
 import net.canarymod.chat.Colors;
 import net.canarymod.commandsys.CommandDependencyException;
 import net.canarymod.tasks.ServerTask;
 import net.canarymod.tasks.ServerTaskManager;
 import net.larry1123.lib.CanaryUtil;
 import net.larry1123.lib.plugin.commands.CommandData;
 
 public class CanaryCaller extends Caller {
 
 	public CanaryCaller(Loader loader) {
 		super(loader);
 	}
 
 	@Override
 	public void disablePlugin() {
 
 	}
 
 	@Override
 	public boolean checkPermission(String playerName, String perm) {
 		if (playerName.equals("Console")) {
 			return true;
 		}
 		return Canary.getServer().getPlayer(playerName).hasPermission(perm) || isOp(playerName);
 	}
 
 	@Override
 	public void sendMessage(String playerName, String message) {
 		if (playerName.equals("Console")) {
 			Canary.getServer().message(addColor(getCommandPrefix() + message));
 		} else {
 			Canary.getServer().getPlayer(playerName).sendMessage(addColor(getCommandPrefix() + message));
 		}
 
 	}
 
 	@Override
 	public String getPlayerWorld(String playerName) {
 		return Canary.getServer().getPlayer(playerName).getWorld().getName();
 	}
 
 	@Override
 	public boolean isOnline(String playerName) {
 		return Canary.getServer().getPlayer(playerName) != null;
 	}
 
 	@Override
 	public String addColor(String message) {
 		String coloredString = message;
 		coloredString = coloredString.replace("{{BLACK}}", Colors.BLACK.toString());
 		coloredString = coloredString.replace("{{DARK_BLUE}}", Colors.DARK_BLUE.toString());
 		coloredString = coloredString.replace("{{DARK_GREEN}}", Colors.GREEN.toString());
 		coloredString = coloredString.replace("{{DARK_CYAN}}", Colors.TURQUIOSE.toString());
 		coloredString = coloredString.replace("{{DARK_RED}}", Colors.RED.toString());
 		coloredString = coloredString.replace("{{PURPLE}}", Colors.PURPLE.toString());
 		coloredString = coloredString.replace("{{GOLD}}", Colors.ORANGE.toString());
 		coloredString = coloredString.replace("{{GRAY}}", Colors.LIGHT_GRAY.toString());
 		coloredString = coloredString.replace("{{DARK_GRAY}}", Colors.GRAY.toString());
 		coloredString = coloredString.replace("{{BLUE}}", Colors.BLUE.toString());
 		coloredString = coloredString.replace("{{BRIGHT_GREEN}}", Colors.LIGHT_GREEN.toString());
 		coloredString = coloredString.replace("{{CYAN}}", Colors.CYAN.toString());
 		coloredString = coloredString.replace("{{RED}}", Colors.LIGHT_RED.toString());
 		coloredString = coloredString.replace("{{PINK}}", Colors.PINK.toString());
 		coloredString = coloredString.replace("{{YELLOW}}", Colors.YELLOW.toString());
 		coloredString = coloredString.replace("{{WHITE}}", Colors.WHITE.toString());
 		return coloredString;
 	}
 
 	@Override
 	public boolean worldExist(String worldName) {
 		return Canary.getServer().getWorld(worldName) != null;
 	}
 
 	@Override
 	public String getDefaultWorld() {
 		return Canary.getServer().getDefaultWorld().getName();
 	}
 
 	@Override
 	public File getDataFolder() {
 		File folder = new File(new File(CanaryCaller.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile(), "Craftconomy3");
 		folder.mkdirs();
 		return folder;
 	}
 
 	@Override
 	public int schedule(final Runnable entry, long firstStart, long repeating) {
 
 		if (repeating != 0) {
 			ServerTaskManager.addTask(new ServerTask(((CanaryLoader)loader), repeating, true) {
 				@Override
 				public void run() {
 					entry.run();
 				}
 			});
 		} else {
 			ServerTaskManager.addTask(new ServerTask(((CanaryLoader)loader), firstStart, false) {
 				@Override
 				public void run() {
 					entry.run();
 				}
 			});
 		}
 		return 0;
 	}
 
 	@Override
 	public int schedule(final Runnable entry, long firstStart, long repeating, boolean async) {
 		if (repeating != 0) {
 			ServerTaskManager.addTask(new ServerTask(((CanaryLoader)loader), repeating, true) {
 				@Override
 				public void run() {
 					entry.run();
 				}
 			});
 		} else {
 			ServerTaskManager.addTask(new ServerTask(((CanaryLoader)loader), firstStart, false) {
 				@Override
 				public void run() {
 					entry.run();
 				}
 			});
 		}
 		return 0;
 	}
 
 	@Override
 	public void cancelSchedule(int id) {
 
 	}
 
 	@Override
 	public int delay(final Runnable entry, long start) {
 
 		ServerTaskManager.addTask(new ServerTask(((CanaryLoader)loader), start, false) {
 		@Override
 			public void run() {
 				entry.run();
 			}
 		});
 		return 0;
 	}
 
 	@Override
 	public int delay(final Runnable entry, long start, boolean async) {
 		ServerTaskManager.addTask(new ServerTask(((CanaryLoader)loader), start, false) {
 			@Override
 			public void run() {
 				entry.run();
 			}
 		});
 		return 0;
 	}
 
 	@Override
 	public List<String> getOnlinePlayers() {
 		return Arrays.asList(Canary.getServer().getPlayerNameList());
 	}
 
 	@Override
 	public void addCommand(String name, String help, CommandReceiver manager) {
 
 		try {
 			CanaryUtil.commands().registerCommand(new CommandData(new String[] {name}, new String[0], help, help), ((CanaryLoader)loader), null, ((CanaryCommandReceiver)manager), false);
 		} catch (CommandDependencyException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public String getServerVersion() {
		return Canary.getServer().getServerVersion() + " - " + Canary.getServer().getCanaryModVersion();
 	}
 
 	@Override
 	public String getPluginVersion() {
 		return ((CanaryLoader)loader).getVersion();
 	}
 
 	@Override
 	public boolean isOp(String playerName) {
 		return Canary.ops().isOpped(playerName);
 	}
 
 	@Override
 	public void loadLibrary(String path) {
 		//Put stuff in lib folder mofo
 	}
 
 	@Override
 	public void registerPermission(String permissionNode) {
 		//TODO: Hmm, idk yet.
 		//Canary.permissionManager().addPermission()
 	}
 
 	@Override
 	public boolean isOnlineMode() {
 		return true;
 		//TODO: Hmm, idk yet.
 		//return Canary.getServer().
 	}
 
 	@Override
 	public Logger getLogger() {
 		return ((CanaryLoader)loader).getLogman().getParent();
 	}
 }
