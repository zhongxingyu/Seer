 /*
  * PluggableBot.java
  * An implementation of a PircBot with loadable module support.
  *
  * Created on 09 October 2007, 14:04
  *
  */
 
 package AndrewCassidy.PluggableBot;
 
 import java.io.File;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.jibble.pircbot.PircBot;
 import org.jibble.pircbot.User;
 
 /**
  * 
  * @author AndyC
  * @author M.Ellis (ellism88@gmail.com)
  */
 public class PluggableBot extends PircBot {
 
 	private static ConcurrentHashMap<String, Plugin> loadedPlugins = new ConcurrentHashMap<String, Plugin>();
 	private static Settings settings;
 	private static PluggableBot b = new PluggableBot();
 	private static ArrayList<String> channels = new ArrayList<String>();
 	private static ThreadPoolExecutor pool = new ThreadPoolExecutor(5, 10, 100,
 			TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100));
 	private static String admin = "";
 	private static Logger log = Logger.getLogger(PluggableBot.class.getName());
 	
 
	private static final String PLUGIN_DIR = "plugins";
 	
 	public static String[] getChans() {
 		return b.getChannels();
 	}
 
 	public static void kill(String nick, String channel) {
 		b.kick(channel, nick);
 	}
 
 	public static User[] users(String channel) {
 		return b.getUsers(channel);
 	}
 
 	public static void getNicks() {
 
 	}
 
 	public static void main(String[] args) {
 
 		settings = new Settings();
 
 		// add the shutdown hook for cleaning up
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 			public void run() {
 				cleanup();
 			}
 		});
 		b.setVerbose(true);
 		loadPlugins(settings.getPlugins());
 		channels.addAll(Arrays.asList(settings.getChannels()));
 		
 		b.identify(settings.getNickservPassword()); //I think this needs to come after the connect
 		b.connect();
 	}
 
 
 	public static void loadPlugins(String[] plugins) {
 		for (String plugin: plugins) loadPlugin(plugin);
 	}
 	
 	public static void loadPlugin(String name) {
 		try {
 			log.log(Level.INFO, "MainBot: attempting to load " + name);
 
 			ArrayList<URL> paths = new ArrayList<URL>();
 			File f = new File( PLUGIN_DIR + "/" + name + ".jar");
 			paths.add(f.toURI().toURL());
 
 			File f2 = new File("lib");
 			for (File ff : f2.listFiles())
 				paths.add(ff.toURI().toURL());
 
 			URL[] urls = new URL[paths.size()];
 			paths.toArray(urls);
 
 			pool.execute(new PluggableBotLoader(name, urls));
 
 		} catch (Exception ex) {
 			log.log(Level.WARNING, "Failed to load plugin: " + ex.getMessage());
 			ex.printStackTrace();
 		}
 
 	}
 
 	public static void addPlugin(String name, Plugin p) {
 		loadedPlugins.put(name, p);
 
 	}
 
	private static void unloadPlugin(String name) {
 		loadedPlugins.get(name).unload();
 		loadedPlugins.remove(name);
 	}
 
 	@Override
 	protected void onAction(String sender, String login, String hostname,
 			String target, String action) {
 		for (Plugin p : loadedPlugins.values())
 			p.onAction(sender, login, hostname, target, action);
 	}
 
 	@Override
 	protected void onJoin(String channel, String sender, String login,
 			String hostname) {
 		for (Plugin p : loadedPlugins.values())
 			p.onJoin(channel, sender, login, hostname);
 	}
 
 	private void connect() {
 		try {
 			b.setName(settings.getNick());
 			b.connect(settings.getServer());
 			for (String s : channels)
 				b.joinChannel(s);
 		} catch (Exception e) {
 			System.err
 					.println("Could not connect to server: " + e.getMessage());
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
 	protected void onKick(String channel, String kickerNick,
 			String kickerLogin, String kickerHostname, String recipientNick,
 			String reason) {
 		for (Plugin p : loadedPlugins.values())
 			p.onKick(channel, kickerNick, kickerLogin, kickerHostname,
 					recipientNick, reason);
 	}
 
 	@Override
 	protected void onMessage(String channel, String sender, String login,
 			String hostname, String message) {
 		if (message.startsWith("!help")) {
 			if (message.trim().split(" ").length == 1) {
 				// loaded plugins
 				String m = "Plugins loaded: ";
 			
 				if (loadedPlugins.size() > 0){
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
 						sendMessage(channel, loadedPlugins.get(string)
 								.getHelp());
 						flag = true;
 					}
 				}
 				if (!flag) {
 					sendMessage(channel,
 							"Could not find help for the specified plugin");
 				}
 
 			}
 		} else {
 			for (Plugin p : loadedPlugins.values())
 				p.onMessage(channel, sender, login, hostname, message);
 		}
 	}
 
 	@Override
 	protected void onPart(String channel, String sender, String login,
 			String hostname) {
 		for (Plugin p : loadedPlugins.values())
 			p.onPart(channel, sender, login, hostname);
 	}
 
 	@Override
 	protected void onQuit(String sourceNick, String sourceLogin,
 			String sourceHostname, String reason) {
 		for (Plugin p : loadedPlugins.values())
 			p.onQuit(sourceNick, sourceLogin, sourceHostname, reason);
 
 		if (sourceNick.equals(admin))
 			admin = "";
 	}
 
 	@Override
 	protected void onPrivateMessage(String sender, String login,
 			String hostname, String message) {
 
 		if (message.startsWith("identify")) {
 			if (message.substring(9).equals(settings.getPassword())) {
 				admin = sender;
 				b.sendMessage(sender, "identified");
 			}
 		} else if (message.startsWith("load") && admin.equals(sender)) {
 			loadPlugin(message.substring(5));
 			b.sendMessage(sender, "loaded");
 		} else if (admin.equals(sender)) {
 			for (Plugin p : loadedPlugins.values())
 				p.onAdminMessage(sender, login, hostname, message);
 		}
 
 		for (Plugin p : loadedPlugins.values())
 			p.onPrivateMessage(sender, login, hostname, message);
 
 	}
 
 	@Override
 	protected void onNickChange(String oldNick, String login, String hostname,
 			String newNick) {
 		if (oldNick.equals(admin))
 			admin = newNick;
 	}
 
 	public static String Nick() {
 		return b.getNick();
 	}
 
 	public static void Action(String target, String action) {
 		b.sendAction(target, action);
 	}
 
 	public static void Message(String target, String action) {
 		b.sendMessage(target, action);
 	}
 
 	private static void cleanup() {
 		log.log(Level.INFO, "Shutting down...");
 		for (Plugin p : loadedPlugins.values())
 			p.unload();
 	}
 
 	public void onAdminMessage(String sender, String login, String hostname,
 			String message) {
 		
 
 	}
 
 	@Override
 	protected void onUserList(String channel, User[] users) {
 
 		for (Plugin p : loadedPlugins.values())
 			p.onUserList(channel, users);
 
 	}
 
 	public static void sendFileDcc(File file, String nick, int timeout) {
 		b.dccSendFile(file, nick, timeout);
 	}
 
 }
