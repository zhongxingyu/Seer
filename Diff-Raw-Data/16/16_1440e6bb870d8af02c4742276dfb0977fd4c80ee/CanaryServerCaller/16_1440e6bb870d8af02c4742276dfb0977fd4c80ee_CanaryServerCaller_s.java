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
 import java.util.logging.Logger;
 
 import com.greatmancode.tools.commands.canary.CanaryCommandReceiver;
 import com.greatmancode.tools.commands.interfaces.CommandReceiver;
 import com.greatmancode.tools.events.Event;
 import com.greatmancode.tools.events.canary.hooks.EconomyChangeHook;
 import com.greatmancode.tools.events.event.EconomyChangeEvent;
 import com.greatmancode.tools.interfaces.CanaryLoader;
 import com.greatmancode.tools.interfaces.Loader;
 import com.greatmancode.tools.interfaces.caller.ServerCaller;
 
 import net.canarymod.Canary;
 import net.canarymod.chat.Colors;
 import net.canarymod.chat.TextFormat;
 import net.canarymod.commandsys.CommandDependencyException;
 import net.larry1123.util.CanaryUtil;
 import net.larry1123.util.api.plugin.commands.CommandData;
 
 public class CanaryServerCaller extends ServerCaller {
 	public CanaryServerCaller(Loader loader) {
 		super(loader);
 		addPlayerCaller(new CanaryPlayerCaller(this));
 		addSchedulerCaller(new CanarySchedulerCaller(this));
 	}
 
 	@Override
 	public void disablePlugin() {
 		Canary.loader().disablePlugin(((CanaryLoader) loader).getName());
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
 		coloredString = coloredString.replace("{{OBFUSCATED}}", TextFormat.RANDOM.toString());
 		coloredString = coloredString.replace("{{BOLD}}", TextFormat.BOLD.toString());
 		coloredString = coloredString.replace("{{STRIKETHROUGH}}", TextFormat.STRIKE.toString());
 		coloredString = coloredString.replace("{{UNDERLINE}}", TextFormat.UNDERLINED.toString());
 		coloredString = coloredString.replace("{{ITALIC}}", TextFormat.ITALICS.toString());
 		coloredString = coloredString.replace("{{RESET}}", TextFormat.RESET.toString());
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
 		File folder = new File(new File(CanaryServerCaller.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile(), "Craftconomy3");
 		folder.mkdirs();
 		return folder;
 	}
 
 	@Override
 	public void addCommand(String name, String help, CommandReceiver manager) {
 
 		try {
 			CanaryUtil.commands().registerCommand(new CommandData(new String[]{name}, new String[0], help, help), ((CanaryLoader) loader), null, ((CanaryCommandReceiver) manager), false);
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
 		return ((CanaryLoader) loader).getVersion();
 	}
 
     @Override
     public String getPluginName() {
         return ((CanaryLoader) loader).getName();
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
		return ((CanaryLoader) loader).getLogman().getParent();
 	}
 
 	@Override
 	public void throwEvent(Event event) {
 		if (event instanceof EconomyChangeEvent) {
 			Canary.hooks().callHook(new EconomyChangeHook(((EconomyChangeEvent) event).getAccount(), ((EconomyChangeEvent) event).getAmount()));
 		}
 	}
 }
