 package org.snack.irc.handler.message;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import org.pircbotx.Channel;
 import org.pircbotx.hooks.events.MessageEvent;
 import org.snack.irc.enums.EventType;
 import org.snack.irc.main.FunctionTester;
 import org.snack.irc.main.Monitor;
 import org.snack.irc.main.Startup;
 import org.snack.irc.main.TriggerHandler;
 import org.snack.irc.model.Bot;
 import org.snack.irc.model.Chan;
 import org.snack.irc.settings.Config;
 import org.snack.irc.settings.ConfigStorer;
 
 public class Admin extends TriggerHandler {
 
 	private MessageEvent<?> event;
 
 	public Admin() {}
 
 	public Admin(MessageEvent<?> event) {
 		this.event = event;
 	}
 
 	@Override
 	public void run() {
 		handleCommand();
 	}
 
 	@Override
 	public boolean trigger(MessageEvent<?> event) {
 		String command;
 		try {
 			command = event.getMessage().split(" ")[0];
 		} catch(Exception e) {
 			command = event.getMessage();
 		}
 		boolean exists = false;
 		for (String s : commands){
 			if (command.equals(s)) {
 				exists = true;
 			}
 		}
 		return (event.getMessage().substring(0, 1).equals(".") && exists && Config.admins.containsKey(event.getUser().getHostmask()));
 	}
 
 	@Override
 	public boolean permission(Chan chan) {
 		return true;
 	}
 
 	@Override
 	public void attachEvent(MessageEvent<?> event) {
 		this.event = event;
 	}
 
 	private final String[] commands = {".admins",".admin",".enable",".disable",".enabled",".disabled",".bot",".bots",".mute",".muted",".join",".leave",".restart",".reload"};
 
 	private void handleCommand() {
 		String message = event.getMessage();
 		Channel channel = event.getChannel();
 		String response = "";
 
 		// .admins
 		if (message.equals(".admins")) {
 			response = "Admins: ";
 			for (Entry<String, String> entry : Config.admins.entrySet()) {
 				response += entry.getKey() + "(" + entry.getValue() + "), ";
 			}
 			response = response.substring(0, response.length() - 2);
 		}
 
 		// .admin add/remove global/main vhost
 		if (message.startsWith(".admin ") && message.length() > 7 && message.split(" ").length == 4 && Config.admins.get(event.getUser().getHostmask()).equals("global")) {
 			String what = message.split(" ")[1];
 			String type = message.split(" ")[2];
 			if ((!what.equals("add") && !what.equals("remove")) || (!type.equals("global") && !type.equals("main"))) {
 				response = "[Error] Syntax: .admin [add/remove] [global/main] [vhost]";
 			} else {
 				if (message.split(" ")[1].equals("add")) {
 					Config.admins.put(message.split(" ")[3], message.split(" ")[2]);
 					response = "Added admin (" + message.split(" ")[2] + "): " + message.split(" ")[3];
 				} else {
 					Config.admins.remove(message.split(" ")[3]);
 					response = "Removed admin (" + message.split(" ")[2] + "): " + message.split(" ")[3];
 				}
 				try {
 					ConfigStorer.storeAdmins();
 					Config.initialize();
 				} catch (Exception e) {
 					response = "[Error] Couldn't update config";
 				}
 			}
 		}
 
 		// .enable/.disable module(s)
 		if (((message.startsWith(".enable ") && message.length() > 8) || (message.startsWith(".disable ") && message.length() > 9)) && message.split(" ").length >= 2) {
 			String[] modules = message.split(" ");
 			HashMap<String, Boolean> functions = Config.channels.get(channel.getName()).def_func;
 			response = (message.startsWith(".enable ")) ? "Enabled: " : "Disabled: ";
 			for (String module : modules) {
 				if (functions.containsKey(module)) {
 					functions.put(module, message.startsWith(".enable"));
 					response += module + ", ";
 				}
 			}
 			response = response.substring(0, response.length() - 2);
 			response = storeChannel(response, channel.getName());
 		}
 
 		// .enabled/.disabled
 		if (message.equals(".enabled") || message.equals(".disabled")) {
 			HashMap<String, Boolean> functions = Config.channels.get(channel.getName()).def_func;
 			if (message.equals(".enabled")) {
 				response = "Enabled on " + channel.getName() + ": ";
 				for (String key : functions.keySet()) {
 					if (functions.get(key)) {
 						response += key + ", ";
 					}
 				}
 				response = response.substring(0, response.length() - 2);
 			} else {
 				response = "Disabled on " + channel.getName() + ": ";
 				for (String key : functions.keySet()) {
 					if (!functions.get(key)) {
 						response += key + ", ";
 					}
 				}
 				response = response.substring(0, response.length() - 2);
 			}
 		}
 
 		// .mute
 		if (message.equals(".mute")) {
 			Chan chan = Config.channels.get(channel.getName());
 			chan.mute = !chan.mute;
 			response = "Muted: " + chan.mute;
 			response = storeChannel(response, channel.getName());
 		}
 
 		// .muted
 		if (message.equals(".muted")) {
 			Chan chan = Config.channels.get(channel.getName());
 			response = "Muted on " + channel.getName() + ": " + chan.mute;
 		}
 
		// .channels
		if (message.equals(".channels")) {
			response = "Channels I'm in: ";
			for (Chan chan : Config.channels.values()) {
				response += chan.name + ", ";
			}
			response = response.substring(0, response.length() - 2);
		}

 		// .join channel
 		if (message.startsWith(".join ") && message.length() > 6 && message.split(" ").length == 2 && message.split(" ")[1].startsWith("#")
 				&& !channel.getBot().channelExists(message.split(" ")[1])) {
 			Chan chan;
 			if (Config.channels.containsKey(message.split(" ")[1])) {
 				chan = Config.channels.get(message.split(" ")[1]);
 				chan.join = true;
 			} else {
 				chan = new Chan(message.split(" ")[1], true, false, new ArrayList<Bot>());
 				chan.initDefault();
 			}
 			Config.channels.put(chan.name, chan);
 			channel.getBot().sendRawLine("JOIN " + chan.name);
 			response = "Joined: " + chan.name;
 			response = storeChannel(response, chan.name);
 		}
 
 		// .leave channel
 		if (message.startsWith(".leave ") && message.length() > 7 && message.split(" ").length == 2 && message.split(" ")[1].startsWith("#")
 				&& channel.getBot().channelExists(message.split(" ")[1])) {
 			Chan chan;
 			if (Config.channels.containsKey(message.split(" ")[1])) {
 				chan = Config.channels.get(message.split(" ")[1]);
 				chan.join = false;
 				channel.getBot().sendRawLine("PART " + chan.name);
 				response = "Left: " + chan.name;
 				response = storeChannel(response, chan.name);
 			} else {
 				response = "[Error] Wasn't in there.";
 			}
 		}
 
 		// .restart
 		if (message.equals(".restart")) {
 			Startup.restart();
 		}
 
 		// .bots
 		if (message.equals(".bots")) {
 			response = "Bots on " + channel.getName() + ": ";
 			Chan chan = Config.channels.get(channel.getName());
 			for (Bot b : chan.bots) {
 				response += b.name + ", ";
 			}
 			response = response.substring(0, response.length() - 2);
 		}
 
 		// .bot add/remove nick
 		if (message.startsWith(".bot ") && message.length() > 5 && message.split(" ").length == 3
 				&& (message.split(" ")[1].equals("add") || message.split(" ")[1].equals("remove"))) {
 			Chan chan = Config.channels.get(channel.getName());
 			int index = -1;
 			for (int i = 0; i < chan.bots.size(); i++) {
 				if (chan.bots.get(i).name.equals(message.split(" ")[2])) {
 					index = i;
 				}
 			}
 			if (index != -1) {
 				if (message.split(" ")[1].equals("add")) {
 					response = "[Error] Bot already known: " + message.split(" ")[2];
 				} else {
 
 					chan.bots.remove(chan.bots.remove(index));
 					response = "Removed bot: " + message.split(" ")[2];
 					response = storeChannel(response, channel.getName());
 				}
 			} else {
 				if (message.split(" ")[1].equals("add")) {
 					Bot b = new Bot(message.split(" ")[2]);
 					b.initDefault();
 					chan.bots.add(b);
 
 					response = "Added bot: " + message.split(" ")[2];
 					response = storeChannel(response, channel.getName());
 				} else {
 					response = "[Error] Unknown bot: " + message.split(" ")[2];
 				}
 			}
 		}
 
 		// .bot nick enable/disable module(s)
 		if (message.startsWith(".bot ") && message.length() > 5 && message.split(" ").length >= 3
 				&& (message.split(" ")[2].equals("enable") || message.split(" ")[2].equals("disable"))) {
 			String[] modules = message.split(" ");
 			Chan chan = Config.channels.get(channel.getName());
 			int index = 0;
 			for (int i = 0; i < chan.bots.size(); i++) {
 				if (chan.bots.get(i).name.equals(message.split(" ")[1])) {
 					index = i;
 				}
 			}
 			HashMap<String, Boolean> functions = Config.channels.get(channel.getName()).bots.get(index).functions;
 			response = (message.split(" ")[2].equals("enable")) ? "Enabled: " : "Disabled: ";
 			for (String module : modules) {
 				if (functions.containsKey(module)) {
 					functions.put(module, message.split(" ")[2].equals("enable"));
 					response += module + ", ";
 				}
 			}
 			response = response.substring(0, response.length() - 2);
 			response = storeChannel(response, channel.getName());
 			new FunctionTester(event, channel, event.getUser(), EventType.JOIN).run();
 		}
 
 		// .bot nick enabled disabled
 		if (message.startsWith(".bot ") && message.length() > 5 && message.split(" ").length == 3
 				&& (message.split(" ")[2].equals("enabled") || message.split(" ")[2].equals("disabled"))) {
 			Chan chan = Config.channels.get(channel.getName());
 			int index = 0;
 			for (int i = 0; i < chan.bots.size(); i++) {
 				if (chan.bots.get(i).name.equals(message.split(" ")[1])) {
 					index = i;
 				}
 			}
 			if (index == -1) {
 				response = "[Error] Unknown bot";
 			} else {
 				HashMap<String, Boolean> functions = Config.channels.get(channel.getName()).bots.get(index).functions;
 
 				if (message.split(" ")[2].equals("enabled")) {
 					response = "Enabled for " + message.split(" ")[3] + ": ";
 					for (String key : functions.keySet()) {
 						if (functions.get(key)) {
 							response += key + ", ";
 						}
 					}
 					response = response.substring(0, response.length() - 2);
 				} else {
 					response = "Disabled for " + message.split(" ")[3] + ": ";
 					for (String key : functions.keySet()) {
 						if (!functions.get(key)) {
 							response += key + ", ";
 						}
 					}
 					response = response.substring(0, response.length() - 2);
 				}
 			}
 		}
 
 		// .reload
 		if (message.equals(".reload")) {
 			try {
 				Config.initialize();
 				response = "Reloaded config";
 			} catch (Exception e) {
 				response =  "[Error] Couldn't update config";
 			}
 		}
 
 		if (!response.equals("")) {
 			Monitor.print("~INFO Response: " + response);
 			event.getBot().sendNotice(event.getUser(), response);
 		}
 	}
 
 	private String storeChannel(String orig, String name) {
 		try {
 			ConfigStorer.storeChannel(name);
 			Config.initialize();
 			return orig;
 		} catch (Exception e) {
 			return "[Error] Couldn't update config";
 		}
 	}
 }
