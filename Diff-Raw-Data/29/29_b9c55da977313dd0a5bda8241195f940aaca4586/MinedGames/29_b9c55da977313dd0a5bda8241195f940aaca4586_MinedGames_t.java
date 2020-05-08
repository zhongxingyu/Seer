 package edu.unca.jderbysh.MinedGames;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.Scanner;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /*
  * This is the main class of the sample plug-in
  */
 public final class MinedGames extends JavaPlugin {
     /*
      * This is called when your plug-in is enabled
      */
 	
 	public HashMap<Player, Integer> scores = new HashMap<Player, Integer>();
 	public boolean gameRunning = false;
 	public boolean gameInit = false;
 
     @Override
     public void onEnable() {
         // save the configuration file
         saveDefaultConfig();
         
         // Create the SampleListener
         new MinedGamesListener(this);
         
         // set the command executor for sample
         this.getCommand("message").setExecutor(new MinedGamesCommandExecutor(this));
         this.getCommand("score").setExecutor(new MinedGamesCommandExecutor(this));
         this.getCommand("join").setExecutor(new MinedGamesCommandExecutor(this));
        this.getCommand("lobby").setExecutor(new MinedGamesCommandExecutor(this));
        this.getCommand("start").setExecutor(new MinedGamesCommandExecutor(this));
     }
     
     /*
      * This is called when your plug-in shuts down
      */
     @Override
     public void onDisable() {
         
     }
     
     public File getRecords() {
    		File records = new File("plugins/MinedGames/scores.hi");
    		if(!records.exists()) {
 			try {
 				records.createNewFile();
 				BufferedWriter out = new BufferedWriter(new FileWriter(records));
 				out.write("Testing");
 				out.newLine();
 				out.write("Twice!");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
    		}
    		return records;
     }
 
 	public void addPlayer(Player sender) {
 		this.writeToRecords(sender.getDisplayName() + ": " + 0);
 	}
 	
 	public void writeToRecords(String str) {
 		File records = getRecords();
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(records));
 			out.write(str);
 			out.newLine();
 			out.close();
 		} catch (IOException e) {
 			this.getLogger().warning("File io messed up in writeToRecords function");
 		}
 	}
 
 }
