 /*
  * Copyright	Mex (ellism88@gmail.com)	2010
  * Copyright	A.Cassidy (a.cassidy@bytz.co.uk)        2007
  *
  *   This program is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.PluggableBot;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.net.URL;
 import java.security.InvalidParameterException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 import org.jibble.pircbot.PircBot;
 import org.jibble.pircbot.User;
 
 import com.PluggableBot.plugin.Plugin;
 import com.PluggableBot.plugin.PluginInternal;
 
 /**
  * An implementation of a PircBot with loadable module support.
  * 
  * @author A.Cassidy (a.cassidy@bytz.co.uk) 09 October 2007
  * @author M.Ellis (ellism88@gmail.com)
  */
 public class PluggableBot extends PircBot {
 
 	private ConcurrentHashMap<String, PluginInternal> loadedPlugins = new ConcurrentHashMap<String, PluginInternal>();
 	private ConcurrentHashMap<String, PluginCommand> commands = new ConcurrentHashMap<String, PluginCommand>();
 	private static Settings settings;
 	private static PluggableBot b = new PluggableBot();
 	private static ThreadPoolExecutor pool = new ThreadPoolExecutor(5, 10, 100, TimeUnit.SECONDS,
 			new ArrayBlockingQueue<Runnable>(100));
 	private static String admin = "";
 	private static Logger log = Logger.getLogger(PluggableBot.class.getName());
 
 	private static final String PLUGIN_DIR = "plugins";
 
 	private static final String COMMAND_IDENTIFY = "!identify";
 	private static final String COMMAND_LOGOUT = "!logout";
 
 	/**
 	 * Main method. Used to star up the plugin
 	 * 
 	 * @param args
 	 *            takes no options.
 	 */
 	public static void main(String[] args) {
 		try {
 			System.out.println("loading logger config....");
 			LogManager.getLogManager().readConfiguration(
 															new FileInputStream(new File(
 																	"logging.properties")));
 			System.out.println(LogManager.getLogManager().toString());
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(-1);
 		}
 
 		settings = new Settings();
 
 		// add the shutdown hook for cleaning up
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 			public void run() {
 				b.cleanup();
 			}
 		});
 		b.setVerbose(true);
 		b.loadPlugins(settings.getPlugins());
 
 		b.connect();
 
 		b.identify(settings.getNickservPassword());
 
 	}
 
 	/**
 	 * Load a list of plugins. Currently used to start up the bot.
 	 * 
 	 * @param plugins
 	 *            A list of plugins to load.
 	 */
 	public void loadPlugins(String[] plugins) {
 		for (String plugin : plugins)
 			loadPlugin(plugin);
 	}
 
 	/**
 	 * Load a singular plugin. Currently used internaly to laod plugins. But
 	 * could be used if a plugin wanted to load a plugin.
 	 * 
 	 * @param name
 	 *            The Name of the plugin.
 	 */
 	public void loadPlugin(String name) {
 		try {
 			log.log(Level.INFO, "MainBot: attempting to load " + name);
 
 			ArrayList<URL> paths = new ArrayList<URL>();
 			File f = new File(PLUGIN_DIR + "/" + name + ".jar");
 			paths.add(f.toURI().toURL());
 
 			File f2 = new File("lib");
 			for (File ff : f2.listFiles())
 				paths.add(ff.toURI().toURL());
 
 			URL[] urls = new URL[paths.size()];
 			paths.toArray(urls);
 
 			pool.execute(new PluggableBotLoader(name, urls, b));
 
 		} catch (Exception ex) {
 			log.log(Level.WARNING, "Failed to load plugin: " + ex.getMessage());
 			ex.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Add a plugin to the list of loaded plugins.
 	 * 
 	 * @param name
 	 *            Name of the plugin being added. This is the name you will need
 	 *            to use to remove the plugin.
 	 * @param p
 	 *            The plugin that is being added
 	 */
 	protected void addPlugin(String name, final Plugin p) {
 		loadedPlugins.put(name, new PluginInternal(p));
 		p.setBot(b);
 		pool.execute(
 		new Runnable() {
 			
 			@Override
 			public void run() {
 				p.load();
 				
 			}
 		});
 		
 	}
 
 	public void unloadPlugin(String name) {
 		Plugin plugin = loadedPlugins.get(name);
 		cleanUpCommands(plugin);
 		plugin.unload();
 		loadedPlugins.remove(name);
 	}
 
 	@Override
 	protected void onAction(String sender, String login, String hostname, String target,
 			String action) {
 		for (Plugin p : loadedPlugins.values())
 			p.onAction(sender, login, hostname, target, action);
 	}
 
 	@Override
 	protected void onJoin(String channel, String sender, String login, String hostname) {
 		for (Plugin p : loadedPlugins.values())
 			p.onJoin(channel, sender, login, hostname);
 	}
 
 	private void connect() {
 		try {
 			b.setName(settings.getNick());
 			b.connect(settings.getServer());
 			for (String s : settings.getChannels())
 				b.joinChannel(s);
 		} catch (Exception e) {
 			System.err.println("Could not connect to server: " + e.getMessage());
 			System.exit(0);
 		}
 	}
 
 	@Override
 	protected void onDisconnect() {
 		while (!b.isConnected()) {
 			try {
 				java.lang.Thread.sleep(60000);
 			} catch (InterruptedException ex) {
 
 			}
 			connect();
 		}
 	}
 
 	@Override
 	protected void onKick(String channel, String kickerNick, String kickerLogin,
 			String kickerHostname, String recipientNick, String reason) {
 		for (Plugin p : loadedPlugins.values())
 			p.onKick(channel, kickerNick, kickerLogin, kickerHostname, recipientNick, reason);
 	}
 
 	@Override
 	protected void onMessage(String channel, String sender, String login, String hostname,
 			String message) {
 		passCommand(channel, sender, login, hostname, message);
 		if (message.startsWith("!help")) {
 			if (message.trim().split(" ").length == 1) {
 				// loaded plugins
 				String m = "Plugins loaded: ";
 
 				if (loadedPlugins.size() > 0) {
 					for (String s : loadedPlugins.keySet())
 						m += s + ", ";
 				} else {
 					m += " no plugins laoded., ";
 				}
 				m = m.substring(0, m.length() - 2);
 				sendMessage(channel, m);
 			} else {
 				// try to find loaded plugin help
 				String[] s = message.trim().split(" ");
 
 				boolean flag = false;
 				for (String string : loadedPlugins.keySet()) {
 					if (string.toLowerCase().equals(s[1].toLowerCase())) {
 						sendMessage(channel, loadedPlugins.get(string).getHelp());
 						flag = true;
 					}
 				}
 				if (!flag) {
 					sendMessage(channel, "Could not find help for the specified plugin");
 				}
 
 			}
 		} else {
 			for (Plugin p : loadedPlugins.values())
 				p.onMessage(channel, sender, login, hostname, message);
 		}
 	}
 
 	@Override
 	protected void onPart(String channel, String sender, String login, String hostname) {
 		for (Plugin p : loadedPlugins.values())
 			p.onPart(channel, sender, login, hostname);
 	}
 
 	@Override
 	protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname,
 			String reason) {
 		for (Plugin p : loadedPlugins.values())
 			p.onQuit(sourceNick, sourceLogin, sourceHostname, reason);
 
 		if (sourceNick.equals(admin))
 			admin = "";
 	}
 
 	@Override
 	protected void onPrivateMessage(String sender, String login, String hostname, String message) {
 		passCommand(null, sender, login, hostname, message);
 		if (message.startsWith(COMMAND_IDENTIFY)) {
 			if (settings.getPassword() != null) {
 				String password = message.substring(COMMAND_IDENTIFY.length()).trim();
 				if (password.equals(settings.getPassword())) {
 					admin = sender;
 					b.sendMessage(sender, "identified");
 					log.info("User " + sender + " is now admin");
 				} else {
 					log.info("User " + sender + " tried to become admin ussing password '"
 							+ password + "'");
 				}
 			} else {
 				log.info("Password was not Set, Admin is disabled.");
 			}
 		} else if (message.startsWith(COMMAND_LOGOUT) && sender == admin) {
 			admin = null;
 			b.Message(sender, "You are no longer admin.");
 		} else if (admin.equals(sender)) {
 			for (Plugin p : loadedPlugins.values())
 				p.onAdminMessage(sender, login, hostname, message);
 		}
 
 		for (Plugin p : loadedPlugins.values())
 			p.onPrivateMessage(sender, login, hostname, message);
 
 	}
 
 	@Override
 	protected void onNickChange(String oldNick, String login, String hostname, String newNick) {
 		if (oldNick.equals(admin))
 			admin = newNick;
 	}
 
 	public String Nick() {
 		return b.getNick();
 	}
 
 	public void Action(String target, String action) {
 		b.sendAction(target, action);
 	}
 
 	public void Message(String target, String message) {
 		b.sendMessage(target, message);
 	}
 
 	public void Message(String channel, String target, String message) {
 		b.sendMessage(channel, new StringBuilder().append(target).append(": ").append(message)
 				.toString());
 	}
 
 	private void cleanup() {
 		log.log(Level.INFO, "Shutting down...");
 		for (Plugin p : loadedPlugins.values())
 			p.unload();
 	}
 
 	@Override
 	protected void onUserList(String channel, User[] users) {
 
 		for (Plugin p : loadedPlugins.values())
 			p.onUserList(channel, users);
 
 	}
 
 	public void sendFileDcc(File file, String nick, int timeout) {
 		b.dccSendFile(file, nick, timeout);
 	}
 
 	public String[] getChans() {
 		return b.getChannels();
 	}
 
 	public void kill(String nick, String channel) {
 		b.kick(channel, nick);
 	}
 
 	public User[] users(String channel) {
 		return b.getUsers(channel);
 	}
 
 	public void addCommand(String command, Plugin p) {
 		log.info("Trying to add command: '" + command + "'");
 		command = command.toLowerCase();
 		if (commands.containsKey(command)) {
 			throw new InvalidParameterException("Command already Exists");
 		} else {
 			commands.put(command, new PluginCommand(command, p, false));
 		}
 		String commandsMsg = "";
 		for (String cm : commands.keySet()) {
 			commandsMsg += cm + " ";
 		}
 		log.info("Commands are now:" + commandsMsg);
 	}
 
 	public void addAdminCommand(String command, Plugin p) {
 		log.info("Trying to add admin command: '" + command + "'");
 		command = command.toLowerCase();
 		if (commands.containsKey(command)) {
 			throw new InvalidParameterException("Command already Exists");
 		} else {
 			commands.put(command, new PluginCommand(command, p, true));
 		}
 	}
 
 	public void passCommand(String channel, String sender, String login, String hostname,
 			String message) {
 		String[] messageParts = message.split(" ");
 		log.info("trying to parse command " + messageParts);
 		for (String command : commands.keySet()) {
 			if (messageParts[0].toLowerCase().equals(command)) {
 				PluginCommand c = commands.get(command);
 				log.info("Command " + command + "matched" + (c.isAdmin() ? "(is admin)" : ""));
 				if (c.isAdmin() && admin.equals(sender)) {
 					Plugin p = c.getPlugin();
 					if (channel == null) {
 						p.onPrivateAdminCommand(command, sender, login, hostname, message
 								.substring(command.length()));
 					} else {
 						p.onAdminCommand(command, channel, sender, login, hostname, message
 								.substring(command.length()));
 					}
 				} else {
 					Plugin p = c.getPlugin();
 					if (channel == null) {
 						p.onPrivateCommand(command, sender, login, hostname, message
 								.substring(command.length()));
 					} else {
 						p.onCommand(command, channel, sender, login, hostname, message
 								.substring(command.length()));
 					}
 				}
 				return;
 			}
 			
 			
 		}
 		String failinfo = "command " + messageParts[0] + "did not match any of " ;
 		for (String cm : commands.keySet()) {
 			failinfo += cm + " ";
 		}
 		log.info(failinfo);
 	}
 
 	public void cleanUpCommands(Plugin plugin) {
 		for (String command : commands.keySet()) {
 			Plugin p = commands.get(command).getPlugin();
			if (p.equals(plugin)) {
				
 				commands.remove(command);
 			}
 		}
 
 	}
 
 	@Override
 	protected void onInvite(String targetNick, String sourceNick, String sourceLogin,
 			String sourceHostname, String channel) {
 		super.onInvite(targetNick, sourceNick, sourceLogin, sourceHostname, channel);
 		log.info("Joing " + channel + " because " + sourceNick + " invited me!");
 		joinChannel(channel);
 	}
 
 	public List<String> listPlugins() {
 		return Collections.list(loadedPlugins.keys());
 	}
 	
 	public Set<String> listCommands() {
 		return commands.keySet();
 	}
 	
 }
