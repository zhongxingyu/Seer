 /*******************************************************************************
  * Copyright (c) 2012 GamezGalaxy.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  ******************************************************************************/
 package net.mcforge.server;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Random;
 
 import net.mcforge.API.EventSystem;
 import net.mcforge.API.io.ServerLogEvent;
 import net.mcforge.API.plugin.CommandHandler;
 import net.mcforge.API.plugin.PluginHandler;
 import net.mcforge.chat.Messages;
 import net.mcforge.groups.Group;
 import net.mcforge.iomodel.Player;
 import net.mcforge.networking.packets.PacketManager;
 import net.mcforge.util.logger.LogInterface;
 import net.mcforge.util.logger.Logger;
 import net.mcforge.util.properties.Properties;
 import net.mcforge.sql.ISQL;
 import net.mcforge.sql.MySQL;
 import net.mcforge.system.Console;
 import net.mcforge.system.heartbeat.Beat;
 import net.mcforge.system.heartbeat.Heart;
 import net.mcforge.system.heartbeat.MBeat;
 import net.mcforge.system.heartbeat.WBeat;
 import net.mcforge.system.updater.UpdateService;
 import net.mcforge.world.Level;
 import net.mcforge.world.LevelHandler;
 
 public final class Server implements LogInterface {
 	private PacketManager pm;
 	private LevelHandler lm;
 	private Logger logger;
 	private CommandHandler ch;
 	private UpdateService us;
 	private Properties p;
 	private PluginHandler ph;
 	private ArrayList<Tick> ticks = new ArrayList<Tick>();
 	private Thread tick;
 	private Beat heartbeater;
 	private EventSystem es;
 	private String Salt;
 	private ISQL sql;
 	private Console console;
 	private Messages m; //Pls lets make everything in messages static this is just stupid
 	public static final List<String> devs = Arrays.asList( "Dmitchell", "501st_commander", "Lavoaster", "Alem_Zupa", "bemacized", "Shade2010", "edh649", "hypereddie10", "Gamemakergm", "Serado", "Wouto1997", "cazzar", "givo");
 	/**
 	 * The players currently on the server
 	 */
 	public ArrayList<Player> players = new ArrayList<Player>();
 	/**
 	 * Weather the server is running or not
 	 */
 	public boolean Running;
 	/**
 	 * The port of the server
 	 */
 	public int Port;
 	/**
 	 * How many players are allowed on the server
 	 */
 	public int MaxPlayers;
 	/**
 	 * The name of the server
 	 */
 	public String Name;
 	/**
 	 * The name of the server that will appear on the WoM list
 	 */
 	public String altName;
 	/**
 	 * The description of the server
 	 */
 	public String description;
 	/**
 	 * WoM Flags
 	 */
 	public String flags;
 	/**
 	 * The MoTD of the server (The message the player sees when first joining the server)
 	 */
 	public String MOTD;
 	/**
 	 * The main level (The level the user first joins when the player joins the server)
 	 */
 	public Level MainLevel;
 	/**
 	 * Weather or not the server is public
 	 */
 	public boolean Public;
 	/**
 	 * The default filename for the system properties
 	 */
 	public final String configpath = "system.config";
 	/**
 	 * The version of GGS this server runs
 	 */
 	public final String VERSION = "1.0.0";
 	/**
 	 * The handler that handles level loading,
 	 * level unloading and finding loaded
 	 * levels
 	 * @return
 	 *        The {@link LevelHandler}
 	 */
 	public final LevelHandler getLevelHandler() {
 		return lm;
 	}
 	/**
 	 * The handler that handles events.
 	 * Use the EventSystem to register event or
 	 * call events
 	 * @return
 	 *        The {@link EventSystem}
 	 */
 	public final EventSystem getEventSystem() {
 		return es;
 	}
 	/**
 	 * Get the handler that handles the packets
 	 * @return
 	 *        The {@link PacketManager}
 	 */
 	public final PacketManager getPacketManager() {
 		return pm;
 	}
 	/**
 	 * Get the handler that handles the player
 	 * commands. Use this to add/remove commands
 	 * or excute commands
 	 * @return
 	 *        The {@link CommandHandler}
 	 */
 	public final CommandHandler getCommandHandler() {
 		return ch;
 	}
 	/**
 	 * Get the handler that handles the plugins
 	 * @return
 	 *        The {@link PluginHandler}
 	 */
 	public final PluginHandler getPluginHandler() {
 		return ph;
 	}
 	
 	/**
 	 * Get the console object that is controlling the server
 	 * @return
 	 *        The {@link Console} object
 	 */
 	public final Console getConsole() {
 		return console;
 	}
 	/**
 	 * The SQL object where you can execute
 	 * Queries
 	 * @return
 	 *        The {@link ISQL} object
 	 */
 	public final ISQL getSQL() {
 		return sql;
 	}
 	/**
 	 * Get the properties for {@link Server#configpath} file
 	 * @return
 	 *        The {@link Properties} object
 	 */
 	public final Properties getSystemProperties() {
 		return p;
 	}
 	
 	/**
 	 * Get the object that controls the updating of plugins
 	 * and other {@link Updatable} objects.
 	 * @return
 	 *       The {@link UpdateService} object
 	 */
 	public final UpdateService getUpdateService() {
 		return us;
 	}
 	
 	/**
 	 * Gets the class that handles messages
 	 * @return The Message class
 	 */
 	public final Messages getMessages() {
 		return m;
 	}
 	/**
 	 * The contructor to make a new {@link Server} object
 	 * @param Name
 	 *            The default name of the server.
 	 *            This will be changed if the properties
 	 *            file has something different.
 	 * @param Port
 	 *            The default port of the server.
 	 *            This will be changed if the properties
 	 *            file has something different.
 	 * @param MOTD
 	 *            The MoTD message for the server.
 	 *            This will be changed if the properties
 	 *            file has something different.
 	 */
 	public Server(String Name, int Port, String MOTD) {
 		this.Port = Port;
 		this.Name = Name;
 		this.MOTD = MOTD;
 		tick = new Ticker();
 	}
 	
 	/**
 	 * Get the salt.
 	 * <b>This method can only be called by heartbeaters and the Connect Packet.
 	 * If this method is called anywhere else, then a {@link IllegalAccessException} is thrown</b>
 	 * @return
 	 *        The server Salt
 	 * @throws IllegalAccessException
 	 *                               This is thrown when an attempt to call this method
 	 *                               is invalid.
 	 */
 	public final String getSalt() throws IllegalAccessException {
 		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
 		try {
 			StackTraceElement e = stacks[2]; //The heartbeat class will always be the 3rd in the stacktrace if the heartbeat is being sent correctly
 			Class<?> class_ = Class.forName(e.getClassName());
 			class_.asSubclass(Heart.class);
 			return Salt;
 		} catch (ClassNotFoundException e1) { }
 		catch (ClassCastException e2) { }
 		catch (ArrayIndexOutOfBoundsException e3) { }
 		try {
 			if (stacks[4].getClassName().equals("net.mcforge.networking.packets.minecraft.Connect"))
 				return Salt;
 		}
 		catch (ArrayIndexOutOfBoundsException e3) { }
 		throw new IllegalAccessException("The salt can only be accessed by the heartbeaters and the Connect packet!");
 	}
 	
 	/**
 	 * Load the server properties such as the server {@link Server#Name}.
 	 * These properties will always load from the {@link Server#configpath}
 	 */
 	public void loadSystemProperties() {
 		Name = getSystemProperties().getValue("Server-Name");
 		altName = getSystemProperties().getValue("WOM-Alternate-Name");
 		MOTD = getSystemProperties().getValue("MOTD");
 		Port = getSystemProperties().getInt("Port");
 		MaxPlayers = getSystemProperties().getInt("Max-Players");
 		Public = getSystemProperties().getBool("Public");
 		description = getSystemProperties().getValue("WOM-Server-description");
 		flags = getSystemProperties().getValue("WOM-Server-Flags");
 		try {
 			sql = (ISQL)Class.forName(getSystemProperties().getValue("SQL-Driver")).newInstance();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		sql.setPrefix(getSystemProperties().getValue("SQL-table-prefix"));
 		if (sql instanceof MySQL) {
 			final MySQL mysql = (MySQL)sql;
 			mysql.setUsername(getSystemProperties().getValue("MySQL-username"));
 			mysql.setPassword(getSystemProperties().getValue("MySQL-password"));
 			mysql.setDatabase(getSystemProperties().getValue("MySQL-database-name"));
 		}
 	}
 	/**
 	 * Start the logger.
 	 * The logger can be started before the server is started, but
 	 * this method is called in {@link Server#Start()}
 	 */
 	public void startLogger() {
 		Calendar cal = Calendar.getInstance();
 		cal.clear(Calendar.HOUR);
 		cal.clear(Calendar.MINUTE);
 		cal.clear(Calendar.SECOND);
 		cal.clear(Calendar.MILLISECOND);
 		logger = new Logger("logs/" + cal.getTime() + ".txt", this);
 		String filename = cal.getTime().toString().replace(" ", "-");
 		String finalname = filename.split("-")[0] + "-" + filename.split("-")[1] + "-" + filename.split("-")[2];
 		try {
 			logger.ChangeFilePath("logs/" , finalname + ".txt");
 		} catch (IOException e2) {
 			System.out.println("logs/" + finalname + ".txt");
 			e2.printStackTrace();
 		}
 		try {
 			logger.Start(false);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	/**
 	 * Start the server
 	 */
 	public void Start(Console console) {
 		if (Running)
 			return;
 		Running = true;
 		this.console = console;
 		console.setServer(this);
 		es = new EventSystem(this);
 		startLogger();
 		Log("Starting MCForge v" + VERSION);
 		ch = new CommandHandler(this);
 		Group.Load(this);
 		p = Properties.init(this);
 		loadSystemProperties();
 		us = new UpdateService(this);
 		m = new Messages(this);
 		pm = new PacketManager(this);
 		pm.StartReading();
 		ph = new PluginHandler();
 		ph.loadplugins(this);
 		Log("Loaded plugins");
 		lm = new LevelHandler(this);
 		if (!new File(getSystemProperties().getValue("MainLevel")).exists()) {
 			Level l = new Level((short)64, (short)64, (short)64);
 			l.name = "Main";
 			l.FlatGrass(this);
 			try {
 				l.save();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		MainLevel = lm.loadLevel(getSystemProperties().getValue("MainLevel"));
 		lm.loadLevels();
 		tick.start();
 		Log("Loaded levels");
 		SecureRandom sr = null;
 		try {
 			sr = SecureRandom.getInstance("SHA1PRNG");
 		} catch (NoSuchAlgorithmException e1) {
 			e1.printStackTrace();
 		}
 		for (int i = 0; i < 100; i++) {
 			byte[] seedb = new byte[16];
 			sr.nextBytes(seedb);
 			Salt = new sun.misc.BASE64Encoder().encode(seedb).replace("=", "" + ((Salt != null) ? Salt.toCharArray()[0] : "A"));
 			if (new Random().nextDouble() < .3)
 				break;
 		}
 		Salt = LetterOrNumber(Salt);
 		Salt = Salt.substring(0, 16);
 		Log("SALT: " + Salt);
 		sql.Connect(this);
 		final String[] commands = new String[] {
				"CREATE TABLE if not exists " + sql.getPrefix() + "_extra (name VARCHAR(20), setting TEXT, value VARBINARY);",
 		};
 		sql.ExecuteQuery(commands);
 		Log("Set up SQL");
 		heartbeater = new Beat(this);
 		heartbeater.addHeart(new MBeat());
 		heartbeater.addHeart(new WBeat());
 		heartbeater.start();
 		Log("Created heartbeat");
 		Log("Server url can be found in 'url.txt'");
 	}
 	
 	/**
 	 * Search for a player based on the name given.
 	 * A part of the name will be given and will find
 	 * the full name and player. If part of the name is given, and
 	 * more than 1 player is found, then it will return null.
 	 * @param name
 	 *            The full/part name of the player
 	 * @return
 	 *        The player found. If more than 1 player is found,
 	 *        then it will return null.
 	 */
 	public Player findPlayer(String name) {
 		Player toreturn = null;
 		for (int i = 0; i < players.size(); i++) {
 			if (name.equalsIgnoreCase(players.get(i).username))
 				return players.get(i);
 			else if (players.get(i).username.toLowerCase().indexOf(name.toLowerCase()) != -1 && toreturn == null)
 				toreturn = players.get(i);
 			else if (players.get(i).username.toLowerCase().indexOf(name.toLowerCase()) != -1 && toreturn != null)
 				return null;
 		}
 		return toreturn;
 	}
 	private static String LetterOrNumber(String string) {
 		final String works = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
 		String finals = "";
 		boolean change = true;
 		for (char c : string.toCharArray()) {
 			for (char x : works.toCharArray()) {
 				if (x == c) {
 					change = false;
 					break;
 				}
 			}
 			if (change)
 				finals += works.toCharArray()[new Random().nextInt(works.toCharArray().length)];
 			else
 				finals += c;
 			change = true;
 		}
 		return finals;
 	}
 
 	/**
 	 * Stop the server, this will kick all the players on the server
 	 * and stop all server services.
 	 * @throws InterruptedException
 	 *                             if any thread has interrupted the current thread. The interrupted status of the current thread is cleared when this exception is thrown.
 	 * @throws IOException
 	 *                    If there is a problem saving the levels that are loaded
 	 */
 	public void Stop() throws InterruptedException, IOException {
 		if (!Running)
 			return;
 		Running = false;
 		Log("Stopping server...");
 		for(Player p : players)
 		{
 			p.sendMessage("Stopping server...");
 		}
 		for (Level l : this.getLevelHandler().getLevelList()) {
 			if (l != MainLevel)
 				l.unload(this);
 		}
 		MainLevel.save();
 		tick.join();
 		logger.Stop();
 		heartbeater.stop();
 		ArrayList<Player> players = new ArrayList<Player>();
 		for (Player p : this.players) {
 			players.add(p);
 		}
 		for(Player p : players)
 		{						
 			p.kick("Server shut down. Thanks for playing!");
 		}
 		players.clear();
 		pm.StopReading();
 	}
 	
 	/**
 	 * Log something to the logs
 	 * @param log
 	 */
 	public void Log(String log) {
 		logger.Log(log);
 	}
 	
 	/**
 	 * Get the logger object
 	 * @return
 	 *        The {@link Logger} object
 	 */
 	public Logger getLogger() {
 		return logger;
 	}
 
 	/**
 	 * Add a task to be called every 10 milliseconds
 	 * @param t
 	 *         The {@link Tick} object to call
 	 */
 	public  void Add(Tick t) {
 		synchronized(ticks) {
 			if (!ticks.contains(t))
 				ticks.add(t);
 		}
 	}
 	
 	/**
 	 * Remove a task from the Tick list
 	 * @param t
 	 *         The {@link Tick} object to remove
 	 */
 	public void Remove(Tick t) {
 		synchronized(ticks) {
 			if (ticks.contains(t))
 				ticks.remove(t);
 		}
 	}
 
 	private class Ticker extends Thread {
 
 		@Override
 		public void run() {
 			while (Running) {
 				for (int i = 0; i < ticks.size(); i++) {
 					Tick t = ticks.get(i);
 					try {
 						t.tick();
 					} catch (Exception e) {
 						Log("ERROR TICKING!");
 						e.printStackTrace();
 					}
 				}
 				try {
 					Thread.sleep(10);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void onLog(String message) {
 		//TODO ..colors?
 		ServerLogEvent sle = new ServerLogEvent(this, message, message.split("\\]")[1].trim());
 		this.es.callEvent(sle);
 		System.out.println(message);
 	}
 	@Override
 	public void onError(String message) {
 		//TODO ..colors?
 		ServerLogEvent sle = new ServerLogEvent(this, message, message.split("\\]")[1].trim());
 		this.es.callEvent(sle);
 		System.out.println("==!ERROR!==");
 		System.out.println(message);
 		System.out.println("==!ERROR!==");
 	}
 	
 	/**
 	 * Calls {@link Server#findPlayer(String)}
 	*/
 	public Player getPlayer(String name) {
 		return findPlayer(name);
 	}
 }
