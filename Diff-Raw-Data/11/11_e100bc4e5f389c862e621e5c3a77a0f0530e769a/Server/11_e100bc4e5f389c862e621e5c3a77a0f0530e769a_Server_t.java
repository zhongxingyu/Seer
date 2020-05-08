 /*******************************************************************************
  * Copyright (c) 2012 GamezGalaxy.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  ******************************************************************************/
 package com.gamezgalaxy.GGS.server;
 
 import java.io.*;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.util.*;
 
 import com.gamezgalaxy.GGS.API.EventSystem;
 import com.gamezgalaxy.GGS.API.plugin.CommandHandler;
 import com.gamezgalaxy.GGS.API.plugin.PluginHandler;
 import com.gamezgalaxy.GGS.defaults.commands.*;
 import com.gamezgalaxy.GGS.groups.Group;
 import com.gamezgalaxy.GGS.iomodel.Player;
 import com.gamezgalaxy.GGS.networking.packets.PacketManager;
 import com.gamezgalaxy.GGS.util.logger.LogInterface;
 import com.gamezgalaxy.GGS.util.logger.Logger;
 import com.gamezgalaxy.GGS.util.properties.Properties;
 import com.gamezgalaxy.GGS.sql.ISQL;
 import com.gamezgalaxy.GGS.sql.MySQL;
 import com.gamezgalaxy.GGS.system.BanHandler;
 import com.gamezgalaxy.GGS.system.heartbeat.Beat;
 import com.gamezgalaxy.GGS.system.heartbeat.Heart;
 import com.gamezgalaxy.GGS.system.heartbeat.MBeat;
 import com.gamezgalaxy.GGS.system.heartbeat.WBeat;
 import com.gamezgalaxy.GGS.world.Level;
 import com.gamezgalaxy.GGS.world.LevelHandler;
 
 public final class Server implements LogInterface {
 	private PacketManager pm;
 	private LevelHandler lm;
 	private Logger logger;
 	private CommandHandler ch;
 	private Properties p;
 	private PluginHandler ph;
 	private ArrayList<Tick> ticks = new ArrayList<Tick>();
 	private Thread tick;
 	private Beat heartbeater;
 	private EventSystem es;
 	private String Salt;
 	private ISQL sql;
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
 			if (stacks[4].getClassName().equals("com.gamezgalaxy.GGS.networking.packets.minecraft.Connect"))
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
 	 * Start the server
 	 */
 	public void Start() {
 		if (Running)
 			return;
 		Running = true;
 		BanHandler.init();
 		es = new EventSystem(this);
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
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Log("Starting..");
 		ch = new CommandHandler(this);
 		Group.Load(this);
 		p = Properties.init(this);
 		loadSystemProperties();
 		pm = new PacketManager(this);
 		pm.StartReading();
 		Log("Loading main level..");
 		lm = new LevelHandler(this);
 		if (!new File(getSystemProperties().getValue("MainLevel")).exists()) {
 			Level l = new Level((short)64, (short)64, (short)64);
 			l.name = "Main";
 			l.FlatGrass(this);
 			try {
 				l.Save();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		MainLevel = lm.loadLevel(getSystemProperties().getValue("MainLevel"));
 		lm.loadLevels();
 		tick.start();
 		Log("Done!");
 		Log("Generating salt");
 		SecureRandom sr = null;
 		try {
 			sr = SecureRandom.getInstance("SHA1PRNG");
 		} catch (NoSuchAlgorithmException e1) {
 			// TODO Auto-generated catch block
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
 		Log("SALT: " + Salt);
 		Log("Setting up SQL");
 		sql.Connect(this);
 		final String[] commands = new String[] {
 				"CREATE TABLE if not exists " + sql.getPrefix() + "_extra (name VARCHAR(20), setting TEXT, value TEXT);",
 		};
 		sql.ExecuteQuery(commands);
 		Log("Done!");
 		Log("Create heartbeat..");
 		heartbeater = new Beat(this);
 		heartbeater.addHeart(new MBeat());
 		heartbeater.addHeart(new WBeat());
 		heartbeater.start();
 		Log("Done!");
 		ph = new PluginHandler();
 		ph.loadplugins(this);
 		try {
 			addCommands();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
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
 			if (name.equals(players.get(i).username))
 				return players.get(i);
 			else if (players.get(i).username.indexOf(name) != -1 && toreturn == null)
 				toreturn = players.get(i);
 			else if (players.get(i).username.indexOf(name) != -1 && toreturn != null)
 				return null;
 		}
 		return toreturn;
 	}
 
 	private void addCommands() throws IOException
 	{
 		ch.addCommand(new Afk());
 		ch.addCommand(new Ban());
 		ch.addCommand(new Goto());
 		ch.addCommand(new Loaded());
 		ch.addCommand(new Newlvl());
 		ch.addCommand(new Spawn());
 		ch.addCommand(new Stop());
 		ch.addCommand(new Unban());
 		ch.addCommand(new TP());
 		ch.addCommand(new Send());
 		ch.addCommand(new Maps());
 		ch.addCommand(new Load());
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
 	 *                             If something intrerruptes the process of stopping the server
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
 				l.Unload(this);
 		}
 		MainLevel.Save();
 		tick.join();
 		logger.Stop();
 		heartbeater.stop();
 		for(Player p : players) // Implementing this again because I want to stop everything first.
 		{						// The idea is that at the begining of the "stopping" session, the player receives that the server is stopping.
 								// Then after everything has stopped but the server itself, the player is kicked from the server before actually shutdown.
 			p.Kick("Server shut down. Thanks for playing!"); // Kicking so we can place a message that the server was stopped.
 
 		}
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
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void onLog(String message) {
 		//TODO ..colors?
 		System.out.println(message);
 	}
 	@Override
 	public void onError(String message) {
 		//TODO ..colors?
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
