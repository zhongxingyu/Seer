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
 import com.gamezgalaxy.GGS.system.heartbeat.MBeat;
 import com.gamezgalaxy.GGS.system.heartbeat.WBeat;
 import com.gamezgalaxy.GGS.world.Level;
 import com.gamezgalaxy.GGS.world.LevelHandler;
 
 public class Server implements LogInterface {
 	private boolean startPlugins;
 	private PacketManager pm;
 	private LevelHandler lm;
 	private Logger logger;
 	private CommandHandler ch;
 	private PluginHandler ph;
 	private ArrayList<Tick> ticks = new ArrayList<Tick>();
 	private Thread tick;
 	private Beat heartbeater;
 	private EventSystem es;
 	private ISQL sql;
 	public ArrayList<Player> players = new ArrayList<Player>();
 	public boolean Running;
 	public int Port;
 	public int MaxPlayers;
 	public String Name;
 	public String altName;
 	public String description;
 	public String flags;
 	public String MOTD;
 	public String Salt;
 	public Level MainLevel;
 	public boolean Public;
 	public final String configpath = "system.config";
 	public final LevelHandler getLevelHandler() {
 		return lm;
 	}
 	public final EventSystem getEventSystem() {
 		return es;
 	}
 	public final PacketManager getPacketManager() {
 		return pm;
 	}
 	public final CommandHandler getCommandHandler() {
 		return ch;
 	}
 	public final PluginHandler getPluginHandler() {
 		return ph;
 	}
 	public final ISQL getSQL() {
 		return sql;
 	}
 	public Server(String Name, int Port, String MOTD) {
 		this.Port = Port;
 		this.Name = Name;
 		this.MOTD = MOTD;
 		tick = new Ticker();
 	}
 	
 	public void Load() {
 		Name = Properties.getValue("Server-Name");
 		altName = Properties.getValue("WOM-Alternate-Name");
 		MOTD = Properties.getValue("MOTD");
 		Port = Properties.getInt("Port");
 		MaxPlayers = Properties.getInt("Max-Players");
 		Public = Properties.getBool("Public");
 		description = Properties.getValue("WOM-Server-description");
 		flags = Properties.getValue("WOM-Server-Flags");
 		try {
 			sql = (ISQL)Class.forName(Properties.getValue("SQL-Driver")).newInstance();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		sql.setPrefix(Properties.getValue("SQL-table-prefix"));
 		if (sql instanceof MySQL) {
 			final MySQL mysql = (MySQL)sql;
 			mysql.setUsername(Properties.getValue("MySQL-username"));
 			mysql.setPassword(Properties.getValue("MySQL-password"));
 			mysql.setDatabase(Properties.getValue("MySQL-database-name"));
 		}
 	}
 
 	public void Start() {
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
 			logger.ChangeFilePath("logs/" + finalname + ".txt");
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
 		Properties.init(this);
 		Load();
		pm = new PacketManager(this);
 		pm.StartReading();
 		Log("Loading main level..");
 		lm = new LevelHandler(this);
 		if (!new File(Properties.getValue("MainLevel")).exists()) {
 			Level l = new Level((short)64, (short)64, (short)64);
 			l.name = "Main";
 			l.FlatGrass();
 			try {
 				l.Save();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		MainLevel = lm.loadLevel(Properties.getValue("MainLevel"));
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
 		//ConsoleCommands consoleCommands = new ConsoleCommands(this);
 		//consoleCommands.start();
 
 		try {
 			addCommands();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void addCommands() throws IOException
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
 	
 	public static String LetterOrNumber(String string) {
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
 
 	public void Stop() throws InterruptedException, IOException {
 		Running = false;
 		Log("Stopping server...");
 		for(Player p : players)
 		{
 			p.sendMessage("Stopping server...");
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
 
 		System.exit(0);
 	}
 	
 	public void Log(String log) {
 		logger.Log(log);
 	}
 
 	public boolean isStartPlugins() {
 		return startPlugins;
 	}
 
 	public void setStartPlugins(boolean startPlugins) {
 		this.startPlugins = startPlugins;
 	}
 
 	public Logger getLogger() {
 		return logger;
 	}
 
 	public  void Add(Tick t) {
 		synchronized(ticks) {
 			if (!ticks.contains(t))
 				ticks.add(t);
 		}
 	}
 
 	public void Remove(Tick t) {
 		synchronized(ticks) {
 			if (ticks.contains(t))
 				ticks.remove(t);
 		}
 	}
 
 	public class Ticker extends Thread {
 
 		@Override
 		public void run() {
 			while (Running) {
 				for (int i = 0; i < ticks.size(); i++) {
 					Tick t = ticks.get(i);
					try {
						t.Tick();
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
 
 	public Player getPlayer(String name)
 	{
 		for(Player p : players)
 		{
 			if(p.username.equals(name))
 			{
 				return p;
 			}
 		}
 
 		return null;
 	}
 
 	public void removeLineFromFile(String file, String lineToRemove)
 	{
 		try {
 
 			File inFile = new File(file);
 
 			if (!inFile.isFile()) {
 				System.out.println("Parameter is not an existing file");
 				return;
 			}
 
 			//Construct the new file that will later be renamed to the original filename.
 			File tempFile = new File(inFile.getAbsolutePath() + ".tmp");
 
 			BufferedReader br = new BufferedReader(new FileReader(file));
 			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
 
 			String line = null;
 
 			//Read from the original file and write to the new
 			//unless content matches data to be removed.
 			while ((line = br.readLine()) != null) {
 
 				if (!line.trim().equals(lineToRemove)) {
 
 					pw.println(line);
 					pw.flush();
 				}
 			}
 			pw.close();
 			br.close();
 
 			//Delete the original file
 			if (!inFile.delete()) {
 				System.out.println("Could not delete file");
 				return;
 			}
 
 			//Rename the new file to the filename the original file had.
 			if (!tempFile.renameTo(inFile))
 				System.out.println("Could not rename file");
 
 		}
 		catch (FileNotFoundException ex) {
 			ex.printStackTrace();
 		}
 		catch (IOException ex) {
 			ex.printStackTrace();
 		}
 	}
 }
