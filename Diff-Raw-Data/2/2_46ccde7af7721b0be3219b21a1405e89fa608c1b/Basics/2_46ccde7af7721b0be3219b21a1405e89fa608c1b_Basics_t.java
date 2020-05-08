 /*
  * Hello source code modifyer, READ THIS
  * 
  * This plugin has an automatic update and bug report function.
  * Please disable it, before modifying the code.
  * 
  * TO DISABLE:
  *  Change the variable MODIFIED to true in BasicsVariables.java
  */
 
 package cc.co.vijfhoek.basics;
 
 import java.io.*;
 import java.net.*;
 import java.util.logging.*;
 
 import org.bukkit.command.*;
 import org.bukkit.event.*;
 import org.bukkit.event.Event.*;
 import org.bukkit.plugin.*;
 import org.bukkit.plugin.java.*;
 import org.bukkit.util.config.*;
 
 import cc.co.vijfhoek.basics.commands.*;
 import cc.co.vijfhoek.basics.listeners.*;
 
 public class Basics extends JavaPlugin {
 	private Logger log;
 	private PluginManager pm;
 	private PluginDescriptionFile pdf;
 	private Socket bsSocket;
 	
 	public Configuration cConfig;
 	public boolean enabled;
 	
 	public void onEnable() {
 		pm = getServer().getPluginManager();
 		registerEvents();
 		
 		log = Logger.getLogger("Minecraft");
 		pdf = getDescription();
 		
 		BasicsVariables.basicsVersion = pdf.getVersion();
 		
 		BasicsConfiguration bcfConfig = new BasicsConfiguration("config");	
 		bcfConfig.createIfNotExists();
 		cConfig = bcfConfig.getConfiguration();
 		
 		try {
 			cConfig.load();
 		} catch (Exception e) {
 			log.severe("[Basics] Couldn't load configuration file!");
 			return;
 		}
 		
 		enabled = true;
 	}
 	
 
 	
 	public void onDisable() {
 		enabled = false;
 	}
 	
 	public void registerEvents() {
 		BasicsEntityListener entityListener = new BasicsEntityListener();
 		pm.registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Priority.High, this);
 		
 		//BasicsBlockListener blockListener = new BasicsBlockListener();
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] cmdArgs) {
 		
 		
 		if (cmdLabel.equalsIgnoreCase("spawn")) {
 			return (new CommandSpawn(sender)).returnValue;
 		}
 		
 		if (cmdLabel.equalsIgnoreCase("spawnmob")) {
 			return (new CommandSpawnmob(sender, cmdArgs)).returnValue;
 		}
 		
 		if (cmdLabel.equalsIgnoreCase("stopfire")) {
 			return (new CommandStopfire(sender)).returnValue;
 		}
 		
 		if (cmdLabel.equalsIgnoreCase("startfire")) {
 			return (new CommandStartfire(sender)).returnValue;
 		}
 		
 		if (cmdLabel.equalsIgnoreCase("extplayer")) {
 			
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public void reportBug(Exception e) { // TODO Clean this up
 		if (BasicsVariables.MODIFIED) return;
 		log.info("[Basics] Sending bug report");
 		
 		try {
			bsSocket = new Socket(InetAddress.getByName("bugsploder.vijfhoek.co.cc"), 7878);
 			PrintWriter writer = new PrintWriter(bsSocket.getOutputStream());
 			BufferedReader reader = new BufferedReader(new InputStreamReader(bsSocket.getInputStream()));
 			
 			writer.println("VERSION 1");
 			
 			String line = "";
 			line = reader.readLine();
 			
 			if (line.equals("INCOMPATIBLE")) {
 				log.severe("[Basics] Incompatible with the latest BugSploder.");
 				log.severe("[Basics] Please update to the latest Basics.");
 				return;
 			}
 			if (line.equals("COMPATIBILITY")) {
 				log.warning("[Basics] BugSploder turned to compatibility mode.");
 				log.warning("[Basics] Please update to the latest Basics, if there");
 				return;
 			}
 			
 			writer.println("PROJECT basics");
 			
 			line = reader.readLine();
 			if (line.equals("UNKNOWN")) {
 				log.severe("[Basics] Help! BugSploder doesn't know me!");
 				log.severe("[Basics] I might be abandoned...");
 				return;
 			}
 			if (line.equals("ABANDONED")) {
 				log.severe("[Basics] Ohnoes! Vijfhoek has abandoned me...");
 				log.severe("[Basics] No future updates for me :(");
 				return;
 			}
 			if (line.equals("UPDATE")) {
 				log.info("[Basics] Please update to the latest Basics.");
 				return;
 			}
 			if (line.equals("OK")) {
 				StackTraceElement ste = e.getStackTrace()[0];
 				String file = ste.getFileName();
 				String linenumber =  String.valueOf(ste.getLineNumber());
 				String message = e.getMessage();
 				
 				String shittosend = "EXCEPTION " + file + " | " + linenumber + " | " + message;
 				writer.println(shittosend);
 				
 				log.info("[Basics] Bug report sent successfully");
 			}
 			bsSocket.close();
 		} catch (Exception e1) {
 			log.severe("[Basics] Please report this to the Basics topic:");
 			e.printStackTrace();
 			try { Thread.sleep(1000); } catch (Exception e2) {}
 			try { bsSocket.close();   } catch (Exception e2) {}
 		}
 	}
 }
