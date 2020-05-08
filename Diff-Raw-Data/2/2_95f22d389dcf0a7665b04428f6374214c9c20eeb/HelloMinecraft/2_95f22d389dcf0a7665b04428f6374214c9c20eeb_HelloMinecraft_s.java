 package me.smickles.HelloMinecraft;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Scanner;
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * HelloMinecraft plugin for Bukkit
  *
  * @author smickles
  */
 
 public class HelloMinecraft extends JavaPlugin{
 	
 	Logger log = Logger.getLogger("Minecraft");
 	String dataFile;
 	String broadcastMessage = "Hello";
 	long broadcastInterval = 18000;
 
 	public void onEnable(){ 
 		
 		/**
 		 * File creation and handling inspired by Mindless728's RealFluids
 		 * https://github.com/mindless728/RealFluids
 		 */
 		getDataFolder().mkdir(); // ensure that HelloMinecraft has a folder
 		dataFile = getDataFolder().getPath()+File.separatorChar+"HelloMinecraft.conf"; // set file name and path to the variable 'dataFile'
 		loadProperties(); // load properties from file
 		
 		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() { // on this server's scheduler, run the repeating task of...
 
 		    public void run() {
 		    	log.info(broadcastMessage+" hello (automatic)"); // log the instance of Hello
 		    	/** 		    		
 		    	 * Broadcast technique inspiration from robin0van0der0v's AfkKick.
 		    	 * http://forums.bukkit.org/threads/misc-afkkick-v1-0-kicks-idling-players-803.18345/
 		    	 */
 		    	for (Player messageplayer : getServer().getOnlinePlayers()){ //for each player online...
 		    		
 		    		messageplayer.sendMessage(broadcastMessage);                 //say hello
 				} 
 		    }
 		}, 60L, broadcastInterval); // 60 server ticks / 20 = 3 second delay
 		                 // broadcastInterval default is 18000 server ticks = 15 minute repetition
 				
 		log.info("HelloMinecraft successfully enabled.");
 	}
 	
 	public void loadProperties() { // load our conf file
 		
 		String s;
 		Scanner scanner; // construct a new scanner
 		try {
 			
 			scanner = new Scanner(new File(dataFile)); 
 			while(scanner.hasNext()) { // while we still have stuff to read from the conf...
 				s = scanner.next();
 				if(s.equals("BroadcastMessage:")) // read and ...
 					broadcastMessage = scanner.nextLine(); // set the message
 				else if(s.equals("BroadcastInterval:")) // read and ...
 					broadcastInterval = scanner.nextLong(); // set the interval
 			}
 		} catch(FileNotFoundException fnfe) { // if no conf file is there
 			
 			saveProperties(); // make one
 		} catch(Exception e) { // some(thing/one) really f'ed up
 			System.out.println("*** HelloMinecraft: Error in configuration file ***");
 		}
 	}
 
 	public void saveProperties() { // save or crate our conf file
 		
		try { // I don't thoughly understand this stuff yet, thanks again to Mindless728
 			BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile));
 			writer.write("BroadcastMessage: "+broadcastMessage);
 			writer.newLine();
 			writer.write("BroadcastInterval: "+broadcastInterval);
 			writer.newLine();
 			writer.close();
 		} catch(IOException ioe) {}
 	}
 
 	public void onDisable(){ 
 		log.info("HelloMinecraft successfully disabled.");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		
 		if(commandLabel.equalsIgnoreCase(broadcastMessage)){ //If the player typed /hello then say hello
 			
 			log.info(broadcastMessage+"hello (/hello)"); // log the instance of hello
 	    	/** 		    		
 	    	 * Broadcast technique inspiration from robin0van0der0v's AfkKick.
 	    	 * http://forums.bukkit.org/threads/misc-afkkick-v1-0-kicks-idling-players-803.18345/
 	    	 */
 			for (Player messageplayer : getServer().getOnlinePlayers()){ //for each player online...
 				
 				messageplayer.sendMessage(broadcastMessage);                 //say hello
 			}
 			return true;
 		}
 		return false;
 	}
 } 
