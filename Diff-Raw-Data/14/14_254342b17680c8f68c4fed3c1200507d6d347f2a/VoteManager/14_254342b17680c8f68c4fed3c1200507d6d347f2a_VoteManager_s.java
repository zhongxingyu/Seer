 package com.ftwinston.Killer;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class VoteManager
 {
 	public static VoteManager instance;
 	private Killer plugin;
 	
 	private boolean inVote; 
 	public long voteDuration = 20 * 20; // 20 secs
 
 	private List<String> playersWhoCanVote = new ArrayList<String>();
 	private int numYesVotes, numNoVotes;
 	
 	private int voteResultProcessID;
 	private VoteResult voteResult;
 	
 	public VoteManager(Killer _plugin)
 	{
 		this.plugin = _plugin;
 		instance = this;
 		inVote = false;
 	}
 	
 	public boolean isInVote() { return inVote; }
 	
 	public void startVote(String question, Player initiatedBy, Runnable runOnYes, Runnable runOnNo, Runnable runOnDraw)
 	{
 		playersWhoCanVote.clear();
 		for ( Player player : plugin.getServer().getOnlinePlayers())
 			playersWhoCanVote.add(player.getName());
 		numYesVotes = numNoVotes = 0;
 		inVote = true;
 		
 		plugin.getServer().broadcastMessage(ChatColor.YELLOW + (initiatedBy == null ? "Vote started: " : initiatedBy.getName() + " started a vote: ") + ChatColor.RESET + question + ChatColor.YELLOW + "\nSay " + ChatColor.GREEN + "Y" + ChatColor.YELLOW + " to vote yes, or " + ChatColor.RED + "N" + ChatColor.YELLOW + " to vote no.");
 		
 		voteResult = new VoteResult(runOnYes, runOnNo, runOnDraw);
 		voteResultProcessID = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, voteResult, voteDuration);
 	}
 	
 	class VoteResult implements Runnable
 	{
 		public VoteResult(Runnable onYes, Runnable onNo, Runnable onDraw)
 		{
 			runOnYes = onYes;
 			runOnNo = onNo;
 			runOnDraw = onDraw;
 		}
 		
 		private Runnable runOnYes, runOnNo, runOnDraw;
 		
 		public void run()
 		{
 			if ( numYesVotes > numNoVotes )
 			{
				plugin.getServer().broadcastMessage(ChatColor.YELLOW +"Vote succeeded (" + ChatColor.GREEN + numYesVotes + ChatColor.RESET + " for, " + ChatColor.RED + numNoVotes + ChatColor.RESET + " against)");
				runOnYes.run();
 			}
 			else if ( numNoVotes > numYesVotes )
 			{
				plugin.getServer().broadcastMessage(ChatColor.YELLOW + "Vote failed (" + ChatColor.GREEN + numYesVotes + ChatColor.RESET + " for, " + ChatColor.RED + numNoVotes + ChatColor.RESET + " against)");
				runOnNo.run();
 			}
 			else
 			{
				plugin.getServer().broadcastMessage(ChatColor.YELLOW + "Vote drawn (" + ChatColor.GREEN + numYesVotes + ChatColor.RESET + " for, " + ChatColor.RED + numNoVotes + ChatColor.RESET + " against)");
 				if ( runOnDraw != null )
 					runOnDraw.run();
 			}
 			
 			inVote = false;
 			voteResultProcessID = -1;
 		}
 	}
 	
 	public boolean doVote(Player player, boolean choice)
 	{
 		if ( !isInVote() )
 			return false;
 		
 		if ( !playersWhoCanVote.contains(player.getName()) )
 			return false; // this player already voted
 		
 		if ( choice )
 			numYesVotes ++;
 		else
 			numNoVotes ++;
 		
 		playersWhoCanVote.remove(player.getName());
 		
 		// check for early finish, but delay this by one tick so that the triggering vote message will show up BEFORE the vote result message
 		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 			public void run()
 			{
 				if ( !isInVote() )
 					return;
 				
 				// only finish early if everyone's voted (we could finish once we have a guaranteed result, but that feels weird if you're one of those that are too slow to vote)
 				if ( playersWhoCanVote.size() > 0 )
 					return;
 				
 				// stop the scheduled task
 				if ( voteResultProcessID != -1 )
 					plugin.getServer().getScheduler().cancelTask(voteResultProcessID);
 				
 				// run the result
 				voteResult.run();				
 			}
 		});
 		
 		return true;
 	}
 }
