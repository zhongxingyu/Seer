 package com.gmail.zariust.LightVote;
 
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Scanner;
 import java.util.logging.Logger;
 
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 import fr.crafter.tickleman.RealPlugin.RealTranslationFile;
 
 
 /**
  * LightVote for Bukkit
  *
  * @author XUPWUP
  */
 public class LightVote extends JavaPlugin {
 	public static RealTranslationFile translate;
 	private LVTPlayerListener playerListener;
     private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
 	private Logger log;
 	public LVTConfig config;
 	//private String name;
     //private String version;
     File voteStartersFile;
     //HashSet<String> voters;
     
     // default configuration
     //private double reqYesVotes = 0.05, minAgree = 0.5;
 	//private int permaOffset = 4000; 
 	//private int voteTime = 30000, voteFailDelay = 30000, votePassDelay = 50000, voteRemindCount = 2;
 	//private boolean bedVote = false;
 	//private boolean perma = false;
 	//private boolean debugMessages;
 	private static final String defaultConfig = 
 		"language fr\n" +
 		"# At least 'required-yes-percentage'*peopleOnServer people must vote yes, " +
 		"and there must be more people that voted yes than no" + '\n' + 
 		"# day" + '\n' +
 		"required-yes-percentage-day 5" + '\n' +
 	 	"minimum-agree-percentage-day 50" + '\n' +
 		"# night" + '\n' +
 	 	"required-yes-percentage-night 5" + '\n' +
 	 	"minimum-agree-percentage-night 50" + '\n' +
 		"# sun" + '\n' +
 	 	"required-yes-percentage-sun 5" + '\n' +
 	 	"minimum-agree-percentage-sun 50" + '\n' +
 		"vote-fail-delay 30" + '\n' +
 		"vote-pass-delay 50" + '\n' +
 		"vote-time 30" + '\n' +
 		"reminders 2" + '\n' +
 		"# enable bedvote (sleeping in a bed starts or agrees with a vote)" + '\n' +
 		"bedvote yes" + '\n' +
 		"# no commands - if this is enabled then all lightvote voting commands are disabled (bedvote or itemvote must be used)" + '\n' +
 		"lightvote-nocommands no" + '\n' +
 		"##### Configure items for commandless voting (hitting 'itemhits' with 'iteminhand' starts/agrees/disagrees with a vote)" + '\n' +
 		"# These are for starting a vote or agreeing to a vote in progress (day and night can be different if you want)" + '\n' +
 		"itemvote yes" + '\n' +
 		"bedvote-iteminhand-day TORCH" + '\n' +
 		"bedvote-itemhits-day BED_BLOCK" + '\n' +
 		"bedvote-iteminhand-night TORCH" + '\n' +
 		"bedvote-itemhits-night BED_BLOCK" + '\n' +
 		"# These are for disagreeing (voting no) with a vote in progress)" + '\n' +
 		"bedvote-novote-iteminhand-day TORCH" + '\n' +
 		"bedvote-novote-itemhits-day DIRT" + '\n' +
 		"bedvote-novote-iteminhand-night TORCH" + '\n' +
 		"bedvote-novote-itemhits-night DIRT" + '\n' +
 		"debug-messages no" + '\n' +
 		"use-permissions no" + '\n' +
		"permanent no" + '\n' +
	    "perma-offset 4000";
 
 	public PermissionHandler permissionHandler = null;
     public static Plugin permissionsPlugin;
 
 	void logWarning(String msg) {
 		log.warning("["+getDescription().getName()+"] "+msg);		
 	}
 	void logInfo(String msg) {
 		log.info("["+getDescription().getName()+"] "+msg);
 	}
 
     void setupPermissions() {
         permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
 
         if (config.usePermissions) {
       	  if (this.permissionHandler == null) {
       		  if (permissionsPlugin != null) {
       			  this.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
       			  if (this.permissionHandler != null) {
       				  this.logInfo("Hooked into Permissions.");
       			  } else {
       				  this.logWarning("Cannot hook into Permissions - failed.");
       			  }
       		  } else {
       			  // TODO: read ops.txt file if Permissions isn't found.
       			  System.out.println("[OtherBlocks] Permissions not found.  Permissions disabled.");
       		  }
       	  }
         } else {
       	  this.logInfo("Permissions not enabled in config.");
       	  permissionsPlugin = null;
       	  permissionHandler = null;
         }
 
       }
 	
     private void parseSettings(Scanner sc){ 
 		while(sc.hasNext()){
 			String thisline = sc.nextLine();
 			String[] contents = thisline.split(" ");
 			if (contents.length > 1){
 				if (contents[0].equals("language")){
 					config.language = contents[1];
 				}else if (contents[0].equals("minimum-agree-percentage-day")){
 						config.minAgreeDay = Double.parseDouble(contents[1]);
 						config.minAgreeDay /= 100.0;
 				}else if (contents[0].equals("required-yes-percentage-day")){
 					config.reqYesVotesDay = Double.parseDouble(contents[1]);
 					config.reqYesVotesDay /= 100.0;
 				}else if (contents[0].equals("minimum-agree-percentage-night")){
 						config.minAgreeNight = Double.parseDouble(contents[1]);
 						config.minAgreeNight /= 100.0;
//System.out.println("minAgreeNight = " + config.minAgreeNight);
 				}else if (contents[0].equals("required-yes-percentage-night")){
 					config.reqYesVotesNight = Double.parseDouble(contents[1]);
 					config.reqYesVotesNight /= 100.0;
 				}else if (contents[0].equals("minimum-agree-percentage-sun")){
 					config.minAgreeSun = Double.parseDouble(contents[1]);
 					config.minAgreeSun /= 100.0;
 				}else if (contents[0].equals("required-yes-percentage-sun")){
 					config.reqYesVotesSun = Double.parseDouble(contents[1]);
 					config.reqYesVotesSun /= 100.0;
 				}else if (contents[0].equals("vote-fail-delay")){
 					config.voteFailDelay = Integer.parseInt(contents[1]) * 1000;
 				}else if (contents[0].equals("vote-pass-delay")){
 					config.votePassDelay = Integer.parseInt(contents[1]) * 1000;
 				}else if (contents[0].equals("vote-time")){
 					config.voteTime = Integer.parseInt(contents[1]) * 1000;
 				}else if (contents[0].equals("reminders")){
 					config.voteRemindCount = Integer.parseInt(contents[1]);
 				}else if (contents[0].equals("permanent")){
 					config.perma = contents[1].equals("yes");
                }else if (contents[0].equals("perma-offset")){
                    config.permaOffset = Integer.parseInt(contents[1]);
 				}else if (contents[0].equals("bedvote")){
 					config.bedVote = contents[1].equals("yes");
 				}else if (contents[0].equals("itemvote")){
 					config.itemVote = contents[1].equals("yes");
 				}else if (contents[0].equals("lightvote-nocommands")){
 					config.lightVoteNoCommands = contents[1].equals("yes");
 				}else if (contents[0].equals("bedvote-iteminhand-day")){
 					config.bedVoteItemInHandDay = Material.getMaterial(contents[1]);
 					if (config.bedVoteItemInHandDay == null) System.out.println("Bad material 1 : " + contents[1]);
 				}else if (contents[0].equals("bedvote-itemhits-day")){
 					config.bedVoteItemHitsDay = Material.getMaterial(contents[1]);
 					if (config.bedVoteItemHitsDay == null) System.out.println("Bad material 1 : " + contents[1]);
 				}else if (contents[0].equals("bedvote-iteminhand-night")){
 					config.bedVoteItemInHandNight = Material.getMaterial(contents[1]);
 					if (config.bedVoteItemInHandNight == null) System.out.println("Bad material 1 : " + contents[1]);
 				}else if (contents[0].equals("bedvote-itemhits-night")){
 					config.bedVoteItemHitsNight = Material.getMaterial(contents[1]);
 					if (config.bedVoteItemHitsNight == null) System.out.println("Bad material 1 : " + contents[1]);
 
 				}else if (contents[0].equals("bedvote-novote-iteminhand-day")){
 					config.bedVoteNoVoteItemInHandDay = Material.getMaterial(contents[1]);
 					if (config.bedVoteNoVoteItemInHandDay == null) System.out.println("Bad material 1 : " + contents[1]);
 				}else if (contents[0].equals("bedvote-novote-itemhits-day")){
 					config.bedVoteNoVoteItemHitsDay = Material.getMaterial(contents[1]);
 					if (config.bedVoteNoVoteItemHitsDay == null) System.out.println("Bad material 1 : " + contents[1]);
 				}else if (contents[0].equals("bedvote-novote-iteminhand-night")){
 					config.bedVoteNoVoteItemInHandNight = Material.getMaterial(contents[1]);
 					if (config.bedVoteNoVoteItemInHandNight == null) System.out.println("Bad material 1 : " + contents[1]);
 				}else if (contents[0].equals("bedvote-novote-itemhits-night")){
 					config.bedVoteNoVoteItemHitsNight = Material.getMaterial(contents[1]);
 					if (config.bedVoteNoVoteItemHitsNight == null) System.out.println("Bad material 1 : " + contents[1]);
 				}else if (contents[0].equals("debug-messages")){
 					config.debugMessages = contents[1].equals("yes");
 				}else if (contents[0].equals("use-permissions")){
 					config.usePermissions = contents[1].equals("yes");
 				}
 			}
 		}
 	}
 
    
 
    @Override
     public void onEnable() { 
     	log = Logger.getLogger("Minecraft");
     	config = new LVTConfig();
     	playerListener = new LVTPlayerListener(this, log);
         sM("Initialised");
     	
     	// Register event for beds
 		PluginManager pm = this.getServer().getPluginManager();
 		pm.registerEvents(playerListener, this);		
         
         File folder = new File("plugins" + File.separator + "LightVote");
         if (!folder.exists()) {
             folder.mkdir();
         }
         
         File configFile = new File(folder.getAbsolutePath() + File.separator +"LightVote.conf");
         if (configFile.exists()){
         	sM("Scanning properties file.");
         	Scanner sc = null;
         	try {
 				sc = new Scanner(configFile);
 				parseSettings(sc);
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
         }else{
         	sM("Creating properties file.");
         	BufferedWriter out = null;
         	try {
 				configFile.createNewFile();
 				out = new BufferedWriter(new FileWriter(configFile));
 				out.write(defaultConfig);
 				out.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
         }
         sM("Properties loaded.  Debug messages: " +config.debugMessages);
         
         voteStartersFile = new File(folder.getAbsolutePath() + File.separator +"voteStarters.conf");
         if(voteStartersFile.exists()){
         	updateVoters(voteStartersFile);
         }else{
         	BufferedWriter out = null;
         	try {
 				voteStartersFile.createNewFile();
 				out = new BufferedWriter(new FileWriter(voteStartersFile));
 				out.write("*");
 				out.close();
 				config.canStartVotes = null;
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
         }
 
 	    	LightVote.translate = new RealTranslationFile(this, config.language).load();
 	    	sM(LightVote.translate.tr("Language is " + config.language));
 
 	    	setupPermissions();
 
         //playerListener.config(config, voters);
         //reqYesVotes, minAgree, permaOffset, voteTime, voteFailDelay, votePassDelay, voteRemindCount, perma, voters, bedVote);
 
         if(config.perma){
         	playerListener.setReset();
         }
     }
     
     private void updateVoters(File f){
     	try {
 			Scanner sc = new Scanner(f);
 			config.canStartVotes = new HashSet<String>();
 			while(sc.hasNext()){
 				String name = sc.next();
 				if (name.equals("*")){
 					config.canStartVotes = null;
 					return;
 				}
 				config.canStartVotes.add(name.toLowerCase());
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command command,
     		String label, String[] args) {
 
     	return playerListener.onPlayerCommand(sender, command, label, args);
     }
    @Override
     public void onDisable() {
     	if (playerListener.tReset != null) playerListener.tReset.cancel();
 
         // NOTE: All registered events are automatically unregistered when a plugin is disabled
 
         sM("Disabled");
     }
     public boolean isDebugging(final Player player) {
         if (debugees.containsKey(player)) {
             return debugees.get(player);
         } else {
             return false;
         }
     }
 
     public void setDebugging(final Player player, final boolean value) {
         debugees.put(player, value);
     }
 
     public void sM(String message) {
     	PluginDescriptionFile pdfFile = getDescription();
     	log.info("[" + pdfFile.getName() + ":" + pdfFile.getVersion() + "] " + message);
     }
 
     public void sMdebug(String message) {
     	if (config.debugMessages) {
     		sM(message);
     	}
     }
 }
