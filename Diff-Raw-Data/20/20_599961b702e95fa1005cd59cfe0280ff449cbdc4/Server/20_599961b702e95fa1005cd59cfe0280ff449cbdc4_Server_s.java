 /*******************************************************************************
  * Copyright (c) 2012 GamezGalaxy.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  ******************************************************************************/
 package com.gamezgalaxy.GGS.server;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.util.*;
 
 import com.gamezgalaxy.GGS.API.EventSystem;
 import com.gamezgalaxy.GGS.API.GGSPlugin;
 import com.gamezgalaxy.GGS.networking.PacketManager;
 import com.gamezgalaxy.GGS.util.logger.LogInterface;
 import com.gamezgalaxy.GGS.util.logger.Logger;
 import com.gamezgalaxy.GGS.util.properties.Properties;
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
 	private ArrayList<Tick> ticks = new ArrayList<Tick>();
 	private Thread tick;
 	private Beat heartbeater;
 	private EventSystem es;
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
 	public PacketManager getPacketManager() {
 		return pm;
 	}
 	public Server(String Name, int Port, String MOTD) {
 		this.Port = Port;
 		this.Name = Name;
 		this.MOTD = MOTD;
 		pm = new PacketManager(this);
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
 	}
 
 	public void Start() {
 		Running = true;
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
 		Properties.init(this);
 		Load();
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
 		Log("SALT: " + Salt);
 		Log("Create heartbeat..");
 		heartbeater = new Beat(this);
 		heartbeater.addHeart(new MBeat());
 		heartbeater.addHeart(new WBeat());
 		heartbeater.start();
 		Log("Done!");
 
 		PluginsThread pluginsThread = new PluginsThread(this);
 		pluginsThread.start();
 	}
 
 	private class PluginsThread extends Thread
 	{
 		public PluginsThread(Server server)
 		{
 			this.server = server;
 		}
 
 		private Server server;
 
 		@Override
 		public void run()
 		{
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 
 			// TODO: Automatically find all classes that extend to GGPlugin.
 			// TODO: Create a system where the plugin is in a separate JAR.
 
 			try {
 				Class c = Class.forName("com.gamezgalaxy.test.console.TestPlugin");
 				Constructor<? extends GGSPlugin> constructor = c.getConstructor();
 				GGSPlugin result = constructor.newInstance();
 
 				result.initialize(server);
 				result.onEnable();
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			} catch (NoSuchMethodException e) {
 				e.printStackTrace();
 			} catch (InvocationTargetException e) {
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			}
 
 			Log("Plugins started!");
 		}
 	}
 
 	public void Stop() throws InterruptedException {
 		Running = false;
 		Log("Stopping server...");
 		for(Player p : players)
 		{
 			p.sendMessage("Stopping server...");
 		}
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
 
 	@SuppressWarnings("unchecked")
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
 				synchronized(ticks) {
 					for (Tick t : ticks) {
 						t.Tick();
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
 }
