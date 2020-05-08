 package com.gmail.zariust.LightVote;
 
 import java.util.List;
 import java.util.HashSet;
 //import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerBedEnterEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 
 /**
  * Handle events for all Player related events
  * @author XUPWUP
  */
 public class LVTPlayerListener extends PlayerListener {
     private final LightVote plugin;
     //private Logger log;
 
 	//private double reqYesVotes, minAgree;
 	//private int permaOffset; 
 	//private int voteTime, voteFailDelay, votePassDelay, voteRemindCount;
 	//private boolean perma, bedVote;
 	private static final int nightstart = 14000;
 	//private Set<String> canStartVotes = null;
     
     public LVTPlayerListener(LightVote instance, Logger log) {
         plugin = instance;
         //this.log = log;
     }
 	
     
     /*public void config(double reqYesVotes, double minAgree, int permaOffset, int voteTime, int voteFailDelay, int votePassDelay, int voteRemindCount, boolean perma, Set<String> set, boolean bedVote){
     	this.reqYesVotes = reqYesVotes;
     	this.minAgree = minAgree;
     	this.permaOffset = permaOffset;
     	this.voteTime = voteTime;
     	this.voteFailDelay = voteFailDelay;
     	this.votePassDelay = votePassDelay;
     	this.voteRemindCount = voteRemindCount;
     	this.perma = perma;
     	canStartVotes = set;
     	this.bedVote = bedVote;
     }*/
     
     private int agrees = 0;
 	private boolean dayVote = true;
 	private int remindCounter = 0;
 	private boolean disabled = false;
 	private boolean voting = false;
 	Timer tReset = null;
 	private World currentWorld = null;
 	
 	private HashSet<Player> voters = new HashSet<Player>();
 	
 	Timer t = new Timer();
 	Timer reminder;
 	
 	
 	private boolean isDay(long currenttime){
 		return ((currenttime % 24000) < 12000 && currenttime > 0 )|| (currenttime < 0 && (currenttime % 24000) < -12000);
 	}
 	
 	
 	public void setReset(){
 		tReset = new Timer();
     	tReset.schedule(new timeReset(), 15000, 15000);
 	}
 	
 	private class timeReset extends TimerTask{
 		public void run(){
 			long currenttime = plugin.getServer().getWorlds().get(0).getTime();
 			boolean isNight = !isDay(currenttime);
 			currenttime = currenttime - (currenttime % 24000); // one day lasts 24000
 			currenttime += plugin.config.permaOffset;
 			if (isNight) currenttime += nightstart;
 			plugin.getServer().getWorlds().get(0).setTime(currenttime);
 		}
 	}
 	
 	
 	private class voteEnd extends TimerTask{
 		public void run(){
 			endVote();
 		}
 	}
 	
 	private class reEnable extends TimerTask{
 		public void run(){
 			disabled = false;
 		}
 	}
 	
 	private class remind extends TimerTask{
 		public void run(){
 			int timeBetween = plugin.config.voteTime / (plugin.config.voteRemindCount+1);
 			remindCounter++;
 			if (remindCounter > plugin.config.voteRemindCount) {
 				reminder.cancel();
 				return;
 			}
 			
 			for (Player player : currentWorld.getPlayers()) {
 				player.sendMessage(ChatColor.GOLD + "Vote for " + (dayVote ? "day" : "night") + ", " + (plugin.config.voteTime - remindCounter*timeBetween)/1000 + " seconds remaining.");
 			}
 			//plugin.getServer().broadcastMessage(ChatColor.GOLD + "Vote for " + (dayVote ? "day" : "night") + ", " + (plugin.config.voteTime - remindCounter*timeBetween)/1000 + " seconds remaining.");
 		}
 	}	
 	
 	private void endVote(){
 		plugin.sMdebug("Starting endvote...");
 		List<Player> playerlist = currentWorld.getPlayers();
 		plugin.sMdebug("Endvote: got players...");
 		String msg = "";
 		boolean passed = false;
 
 		double reqYesVotes;
 		double minAgree;
 		
 		int numplayers = playerlist.size();
 		if (dayVote) {
 			reqYesVotes = plugin.config.reqYesVotesDay;
 			minAgree = plugin.config.minAgreeDay;
 		} else {
 			reqYesVotes = plugin.config.reqYesVotesNight;
 			minAgree = plugin.config.minAgreeNight;
 		}
 		if (voters.size() > numplayers * reqYesVotes){
 			if (agrees > minAgree * voters.size()) {
 				msg = "Vote passed. (" + agrees + " yes, " + (voters.size() - agrees) + " no)";
 				long currenttime = currentWorld.getTime();
 				currenttime = currenttime - (currenttime % 24000); // one day lasts 24000
 				
 				if (currenttime < 0){
 					currenttime *= -1;
 					plugin.sM("LVT: Current time was negative!");
 				}
 				
 				if (!dayVote) currenttime += nightstart;
 				if(plugin.config.perma) currenttime += plugin.config.permaOffset;
 				
 				currentWorld.setTime(currenttime);
 				passed = true;
 				plugin.sM("LVT: changed time to "+ (dayVote ? "day" : "night"));
 			}
 			else {
 				msg = "Vote failed. (" + agrees + " yes, " + (voters.size() - agrees) + " no)";
 				plugin.sM("LVT: vote failed (" + voters.size() + " votes, "+ agrees + " agree)");
 			}
 		}else{
 			msg = "Vote failed, insufficient \"yes\" votes. (" + agrees + "/" + (numplayers * reqYesVotes) + ")";
 			plugin.sM("LVT: vote failed, insufficient votes (" + agrees + " yes votes, "+ numplayers + " players, req " + (numplayers * reqYesVotes)+ ")");
 		}
 		
 		plugin.sMdebug("Endvote: checked status, broadcasting message...");
 
 		for (Player player : playerlist) {
 			player.sendMessage(ChatColor.GOLD + msg);
 		}
 		//plugin.getServer().broadcastMessage(ChatColor.GOLD + msg);
 		agrees = 0;
 		voters = new HashSet<Player>();
 		voting = false;
 		disabled = true;
 		Timer reenable = new Timer();
 		reenable.schedule(new reEnable(), (passed ? plugin.config.votePassDelay : plugin.config.voteFailDelay));
 	}
 	
 	public boolean canSVote(CommandSender sender){
 		if(sender instanceof Player) {
 			return plugin.config.canStartVotes == null || plugin.config.canStartVotes.contains(((Player) sender).getName().toLowerCase());
 		}else return true;
 	}
 	
 	public boolean onPlayerCommand(CommandSender sender, Command command,
     		String label, String[] args){
 
 		Player player = (Player) sender;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 			currentWorld = player.getWorld();
 		} else {
 			plugin.sM("onPlayerCommand - sender is not a player, skipping commands.");
 			return false;
 		}
 		String[] split = args;
 		if (!label.equalsIgnoreCase("lvt")) return false;
 
 		if (split.length == 0 || (split.length == 1 && split[0].equalsIgnoreCase("help"))){
 			sender.sendMessage(ChatColor.GOLD + "Lightvote commands");
 			if (!(plugin.config.lightVoteNoCommands)) {
 				if(canSVote(sender)) {
 					sender.sendMessage(ChatColor.GOLD + "/lvt start -- start a vote(for day)");
 					sender.sendMessage(ChatColor.GOLD + "/lvt start night -- start a vote for night");
 				}
 				sender.sendMessage(ChatColor.GOLD + "/lvt yes/no -- vote");
 			}
 
 			if (plugin.config.bedVote) {
 				sender.sendMessage(ChatColor.GOLD + "Bedvote: sleep in a bed to start a vote for day or agree to one in progress.");
 			}
 			
 			sender.sendMessage(ChatColor.GOLD + "Itemvote: vote for day - hit "+plugin.config.bedVoteItemInHandDay+" onto "+plugin.config.bedVoteItemHitsDay+ " for yes. " 
 					+plugin.config.bedVoteNoVoteItemInHandDay+" onto " +plugin.config.bedVoteNoVoteItemHitsDay+" for no.");
 			sender.sendMessage(ChatColor.GOLD + "Itemvote: vote for night - hit "+plugin.config.bedVoteItemInHandNight+" onto "+plugin.config.bedVoteItemHitsNight+ " for yes. " 
 					+plugin.config.bedVoteNoVoteItemInHandNight+" onto " +plugin.config.bedVoteNoVoteItemHitsNight+" for no.");
 
 			sender.sendMessage(ChatColor.GOLD + "/lvt help -- this message");
 			sender.sendMessage(ChatColor.GOLD + "/lvt info -- some information");
 			return true;
 		}
 		
 		if(split[0].equalsIgnoreCase("info")){
 			sender.sendMessage(ChatColor.GOLD + "Lightvote created by XUPWUP, further developer by Xarqn");
 			sender.sendMessage(ChatColor.GOLD + "Lightvote version " + plugin.getDescription().getVersion());
 			sender.sendMessage(ChatColor.GOLD + "Static time is " + (plugin.config.perma ? "enabled" : "disabled"));
 			sender.sendMessage(ChatColor.GOLD + "Current time:" + player.getWorld().getTime()%24000 + " ("+player.getWorld().getName()+")");
 			sender.sendMessage(ChatColor.GOLD + "Bedvote is: " + (plugin.config.bedVote ? "on - sleep in a bed to vote for day." : "off."));
 			sender.sendMessage(ChatColor.GOLD + "Itemvote: vote for day - hit "+plugin.config.bedVoteItemInHandDay+" onto "+plugin.config.bedVoteItemHitsDay+ " for yes. " 
 					+plugin.config.bedVoteNoVoteItemInHandDay+" onto " +plugin.config.bedVoteNoVoteItemHitsDay+" for no.");
 			sender.sendMessage(ChatColor.GOLD + "Itemvote: vote for night - hit "+plugin.config.bedVoteItemInHandNight+" onto "+plugin.config.bedVoteItemHitsNight+ " for yes. " 
 					+plugin.config.bedVoteNoVoteItemInHandNight+" onto " +plugin.config.bedVoteNoVoteItemHitsNight+" for no.");
 			return true;
 		}
 
 		if (!(plugin.config.lightVoteNoCommands)) {
 		if (split[0].equalsIgnoreCase("start")){
 
 			if (split.length > 1){
 				if (split[1].equalsIgnoreCase("night")) {
 					startVote(false, sender);
 				} else {
 					startVote(true, sender);
 				}
 			} else {
 				startVote(true, sender);
 			}
 			
 			//long currenttime = currentWorld.getTime();				
 						
 			//startVote(this.dayVote, sender);
 		}else{
 			if (split[0].equalsIgnoreCase("yes") || split[0].equalsIgnoreCase("y")) {
 				addToVote(this.dayVote, sender, true);
 			} else if (split[0].equalsIgnoreCase("no") || split[0].equalsIgnoreCase("n")) {
 				plugin.sMdebug("Starting no vote...");
 				addToVote(this.dayVote, sender, false);
 			}
 		}
 		}
 		return true;
 	}
 	
 	public boolean addToVote(boolean day, CommandSender sender, boolean agreed) {
 		if(sender instanceof Player) if (voters.contains((Player) sender)){
 			sender.sendMessage(ChatColor.GOLD + "You have already voted");
 			return true;
 		}
 		if (!voting){
 			sender.sendMessage(ChatColor.GOLD + (agreed ? "'Yes'" : "'No'") + " vote attempted but no votes in progress. "+(plugin.config.lightVoteNoCommands ? "Use /lvt help to find out how to start a vote." : "Use /lvt start to start a vote for day or /lvt help for more info."));
 			return true;
 		}
 
 		//boolean agreed = false;
 		//if (split[0].equalsIgnoreCase("yes") || split[0].equalsIgnoreCase("y")) {
 		if (agreed) {
 			agrees++;
 		} else {
 			//agrees--;
 		}
 
 		if(sender instanceof Player) voters.add((Player)sender);
 		if (voters.size() == currentWorld.getPlayers().size()){// plugin.getServer().getOnlinePlayers().length){
 			t.cancel();
 			t = new Timer();
 			reminder.cancel();
 			endVote();
 		}
 		sender.sendMessage(ChatColor.GOLD + "Thanks for voting! (" + (agreed ? "yes" : "no") + ")");
 
 		return true;
 	}
 		
 	public boolean startVote(boolean day, CommandSender sender) {
 
 		String daymsg = "";
 		if (day){
 			daymsg = "for daylight";
 		}else daymsg = "for darkness";
 		
 		String pname;
 		
 		this.dayVote = day;
 		
 		voters.clear();
 		agrees = 1;
 
 		if(sender instanceof Player) {
 			pname =((Player) sender).getName();
 			voters.add((Player) sender);
 		}else {
 			pname = "<CONSOLE>";
 		}
 
 		if(!canSVote(sender)){
 			sender.sendMessage(ChatColor.GOLD + "You are not allowed to start votes.");
 			return true;
 		}
 		if (voting) {
 			sender.sendMessage(ChatColor.GOLD + "A vote is still in progress.");
 			return true;
 		}
 		
 		if (disabled){
 			sender.sendMessage(ChatColor.GOLD + "You cannot vote again this quickly.");
 			return true;
 		}
 		
 		if (isDay(currentWorld.getTime())){ // it is day now
 			if (day){
 				sender.sendMessage(ChatColor.GOLD + "It is already day!");
 				return true;
 			}
 		}else{ // it is night now
 			if (!day){
 				sender.sendMessage(ChatColor.GOLD + "It is already night!");
 				return true;
 			}
 		}
 
 		// After all checks (vote in progress, permission, etc - set vote type to day or night
 		this.dayVote = day;
 		
 		voting = true;
 		plugin.sMdebug("Startvote detected... just before broadcast message.");
 
 		for (Player player : currentWorld.getPlayers()) {
 			player.sendMessage(ChatColor.GOLD + "Lightvote " + daymsg + " in world '"+currentWorld.getName()+"' started by "+ pname + ",");
 			if (plugin.config.lightVoteNoCommands) {
 				player.sendMessage(ChatColor.GOLD + "type /lvt help to find out how to vote.");
 			} else {
 				player.sendMessage(ChatColor.GOLD + "type /lvt yes, or /lvt no to vote.");
 			}
 		}
 
 		//plugin.getServer().broadcastMessage(ChatColor.GOLD + "Lightvote " + daymsg + " in world '"+currentWorld.getName()+"' started by "+ pname + ",");
 		//plugin.getServer().broadcastMessage(ChatColor.GOLD + "type /lvt yes, or /lvt no to vote.");
 		
 		t.schedule(new voteEnd(), plugin.config.voteTime);
 		if (voters.size() == currentWorld.getPlayers().size()){
 			t.cancel();
 			t = new Timer();
 			endVote();
 			return true;
 		}
 		
 		reminder = new Timer();
 		
 		if (plugin.config.voteRemindCount > 0){
 			remindCounter = 0;
 			int timeBetween = plugin.config.voteTime / (plugin.config.voteRemindCount+1);
 			reminder.schedule(new remind(), timeBetween, timeBetween);
 		}
 		return true;
 	}
 
 	public void onPlayerBedEnter (PlayerBedEnterEvent e)
 	{
 		if (plugin.config.bedVote) {
 			Player player = e.getPlayer();
			currentWorld = player.getWorld();
 			long currenttime = player.getWorld().getTime();
 			//String[] commandArgs = {""};
 			if (!voting) {
 				startVote(!(isDay(currenttime)), player);
 			} else {
 				addToVote(!(isDay(currenttime)), player, true);
 			}
 			//player.sendMessage(ChatColor.GOLD + "Sleeping, attempting to vote for day time...");
 			//onPlayerCommand(player, null, String.valueOf("lvt"), commandArgs);
 		}
 	}
 	
 	public void onPlayerInteract (PlayerInteractEvent e)
 	{
 		try {
 		  if (plugin.config.itemVote) {	
 			if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
 				Player player = e.getPlayer();
 				this.currentWorld = player.getWorld();
 				long currenttime = player.getWorld().getTime();
 				Material itemHits;
 				Material itemInHand;
 				Material noVoteItemHits;
 				Material noVoteItemInHand;
 				if (isDay(currenttime)) {
 					itemHits = plugin.config.bedVoteItemHitsNight;
 					itemInHand = plugin.config.bedVoteItemInHandNight;				
 					noVoteItemHits = plugin.config.bedVoteNoVoteItemHitsNight;
 					noVoteItemInHand = plugin.config.bedVoteNoVoteItemInHandNight;				
 				} else {
 					itemHits = plugin.config.bedVoteItemHitsDay;
 					itemInHand = plugin.config.bedVoteItemInHandDay;
 					noVoteItemHits = plugin.config.bedVoteNoVoteItemHitsNight;
 					noVoteItemInHand = plugin.config.bedVoteNoVoteItemInHandNight;				
 				}
 
 				plugin.sMdebug("Bedvote interaction detected... items loaded. itemHits: "+itemHits+" NoVoteItemHits: "+noVoteItemHits.name()+" itemhit: "+e.getClickedBlock().getType().name());
 
 				if (e.getClickedBlock().getType() == itemHits) {
 					if (e.getItem() != null) {
 						if (e.getItem().getType() == itemInHand) {
 							plugin.sMdebug("Bedvote interaction detected... items matched.");
 							if (!voting) {
 								startVote(!(isDay(currenttime)), player);
 							} else {
 								addToVote(!(isDay(currenttime)), player, true);
 							}
 						}
 					}
 				} else if (e.getClickedBlock().getType() == noVoteItemHits) {
 					if (e.getItem() != null) {
 						plugin.sMdebug("Bedvote interaction detected 'novote'... item held: "+e.getItem().getType().name()+" item needed: "+noVoteItemInHand.name());
 						if (e.getItem().getType() == noVoteItemInHand) {
 							if (voting) {
 								//startVote(!(isDay(currenttime)), player);
 							//} else {
 								addToVote(!(isDay(currenttime)), player, false);
 							}
 						}
 					}
 				}
 			}
 		  }
 		} catch (Exception exception) {
 			System.err.println("LightVote - 'onPlayerInteract' Error: " + exception.getMessage());			  
 		}
 	}
 }
