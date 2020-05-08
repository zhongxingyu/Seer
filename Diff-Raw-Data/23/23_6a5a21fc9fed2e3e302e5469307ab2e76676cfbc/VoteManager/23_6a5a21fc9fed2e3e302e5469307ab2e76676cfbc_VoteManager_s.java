 package com.ftwinston.Killer;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.conversations.Conversation;
 import org.bukkit.conversations.ConversationContext;
 import org.bukkit.conversations.ConversationFactory;
 import org.bukkit.conversations.InactivityConversationCanceller;
 import org.bukkit.conversations.MessagePrompt;
 import org.bukkit.conversations.NumericPrompt;
 import org.bukkit.conversations.Prompt;
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
 
 	private ConversationFactory voteConvFactory;
 	
 	public VoteManager(Killer _plugin)
 	{
 		this.plugin = _plugin;
 		instance = this;
 		inVote = false;
 
         setupConversation();
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
 				plugin.getServer().broadcastMessage(ChatColor.YELLOW +"Vote succeeded (" + ChatColor.GREEN + numYesVotes + ChatColor.YELLOW + " for, " + ChatColor.RED + numNoVotes + ChatColor.YELLOW + " against)");
 				if ( runOnYes != null )
 					runOnYes.run();
 			}
 			else if ( numNoVotes > numYesVotes )
 			{
 				plugin.getServer().broadcastMessage(ChatColor.YELLOW + "Vote failed (" + ChatColor.GREEN + numYesVotes + ChatColor.YELLOW + " for, " + ChatColor.RED + numNoVotes + ChatColor.YELLOW + " against)");
 				if ( runOnNo != null )
 					runOnNo.run();
 			}
 			else
 			{
 				plugin.getServer().broadcastMessage(ChatColor.YELLOW + "Vote drawn (" + ChatColor.GREEN + numYesVotes + ChatColor.YELLOW + " for, " + ChatColor.RED + numNoVotes + ChatColor.YELLOW + " against)");
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
 
 	private void setupConversation()
 	{
 		final MessagePrompt cantVotePrompt = new MessagePrompt() {
 			@Override
 			public String getPromptText(ConversationContext context) {
 				return "You cannot start a vote right now, as another vote is in progress.";
 			}
 			
 			@Override
 			protected Prompt getNextPrompt(ConversationContext context)
 			{
 				return Prompt.END_OF_CONVERSATION;
 			}
 		};
 		
 		final NumericPrompt gameModePrompt = new NumericPrompt() {
         	public String getPromptText(ConversationContext context)
 			{
 				String message = "The current game mode is " + ChatColor.YELLOW + plugin.getGameMode().getName() + ChatColor.RESET + ", and the next game mode will be " + ChatColor.YELLOW + plugin.getNextGameMode().getName() + ChatColor.RESET + ".\nWhat do you want to set the next game mode to?";
 				
 				int i = 1;
 				for (GameMode mode : GameMode.gameModes.values())
 				{
 					if ( plugin.getNextGameMode() == mode )
 						continue;
 					
 					message += "\n" + ChatColor.GOLD + i + "." + ChatColor.RESET + mode.getName();
 					i++;
 				}
 				
 				message += "\n" + ChatColor.GOLD + "0." + ChatColor.RESET + " Cancel";
 				return message;
 			}
 			
 			protected Prompt acceptValidatedInput(ConversationContext context, Number val)
 			{
 				Player player = context.getForWhom() instanceof Player ? (Player)context.getForWhom() : null;
 			
 				if ( isInVote() )
 					return cantVotePrompt;
 				
 				int choice = val.intValue();
 				
 				int i = 1;
 				for (final GameMode mode : GameMode.gameModes.values())
 				{
 					if ( plugin.getNextGameMode() == mode )
 						continue;
 						
 					if ( i != choice )
 					{
 						i++;
 						continue;
 					}
 				
 					startVote("Set the next game mode to " + mode.getName() + "?", player, new Runnable() {
 						public void run()
 						{
 							plugin.setNextGameMode(mode, null);
 						}
 					}, null, null);
 					break;
 				}
 
 				return Prompt.END_OF_CONVERSATION;
 			}
         };
 		
         final NumericPrompt restartPrompt = new NumericPrompt() {
         	public String getPromptText(ConversationContext context)
 			{
 				return "How do you want to restart?\n" + ChatColor.GOLD + "1." + ChatColor.RESET + " In the same world, keeping items\n" + ChatColor.GOLD + "2." + ChatColor.RESET + " In the same world, removing items\n" + ChatColor.GOLD + "3." + ChatColor.RESET + " In a new world\n" + ChatColor.GOLD + "0." + ChatColor.RESET + " Cancel";
 			}
 			
 			protected Prompt acceptValidatedInput(ConversationContext context, Number val)
 			{
 				Player player = context.getForWhom() instanceof Player ? (Player)context.getForWhom() : null;
 			
 				if ( isInVote() )
 					return cantVotePrompt;
 				
 				else if ( val.intValue() == 1 )
 					startVote("End this round, and start a new round with the same world & items?", player, new Runnable() {
 						public void run()
 						{
 							plugin.restartGame(true, false);
 						}
 					}, null, null);
 				
 				else if ( val.intValue() == 2 )
 					startVote("End this round, and start a new round with the same world, removing all players' items?", player, new Runnable() {
 						public void run()
 						{
 							plugin.restartGame(true, true);
 						}
 					}, null, null);
 				
 				else if ( val.intValue() == 3 )
 					startVote("End this round, and start a new round in a new world?", player, new Runnable() {
 						public void run()
 						{
 							plugin.restartGame(false, true);
 						}
 					}, null, null);
 				
 				return Prompt.END_OF_CONVERSATION;
 			}
         };
         
         final NumericPrompt initialPrompt = new NumericPrompt() {
         	
 			public String getPromptText(ConversationContext context)
 			{
 				if ( plugin.canChangeGameMode )
 					return "What do you want to start a vote on? Say the number to choose:\n" + ChatColor.GOLD + "1." + ChatColor.RESET + " Change next game mode\n" + ChatColor.GOLD + "2." + ChatColor.RESET + " Restart game\n" + ChatColor.GOLD + "3." + ChatColor.RESET + " Clear killer\n" + ChatColor.GOLD + "4." + ChatColor.RESET + " Reallocate killer\n" + ChatColor.GOLD + "0." + ChatColor.RESET + " Cancel";
 				
 				return "What do you want to start a vote on? Say the number to choose:\n" + ChatColor.GOLD + "1." + ChatColor.RESET + " Restart game\n" + ChatColor.GOLD + "2." + ChatColor.RESET + " Clear killer\n" + ChatColor.GOLD + "3." + ChatColor.RESET + " Reallocate killer\n" + ChatColor.GOLD + "0." + ChatColor.RESET + " Cancel";
 			}
 			
 			protected Prompt acceptValidatedInput(ConversationContext context, Number val)
 			{
 				Player player = context.getForWhom() instanceof Player ? (Player)context.getForWhom() : null;
 				
 				int choice = val.intValue();
 				if ( !plugin.canChangeGameMode )
 					choice++; // "first choice" isn't available
 				
 				if ( choice == 1 )
 					return gameModePrompt;
 					
 				if ( choice == 2 )
 					return restartPrompt;
 				
 				else if ( choice == 3 )
 					startVote("Clear the killer, so that there isn't one assigned?", player, new Runnable() {
 						public void run()
 						{
 							plugin.playerManager.clearKillers(null);
 						}
 					}, null, null);
 					
 				else if ( choice == 4 )
 					startVote("Clear the killer, and assign a new one?", player, new Runnable() {
 						public void run()
 						{
 							plugin.playerManager.clearKillers(null);
 							plugin.playerManager.assignKillers(null);
 						}
 					}, null, null);
 				
 				return Prompt.END_OF_CONVERSATION;
 			}
 		};
         
         voteConvFactory = new ConversationFactory(plugin);
         voteConvFactory.withFirstPrompt(initialPrompt);
         voteConvFactory.withLocalEcho(false);
         voteConvFactory.withModality(false);
         voteConvFactory.withConversationCanceller(new InactivityCanceller(plugin, 30));
 	}
 
	protected class InactivityCanceller : InactivityConversationCanceller
 	{
 		public InactivityCanceller(Plugin plugin, int timeoutSeconds)
 		{
 			super(plugin, timeoutSeconds);
 		}
 	
 		@Override
 		protected void cancelling(Conversation conversation)
 		{
 			Player player = conversation.getForWhom() instanceof Player ? (Player)conversation.getForWhom() : null;
 			if ( player != null )
 				player.sendMessage("Vote setup cancelled");
 		}
 	}
 	
 	public void showVoteMenu(Player sender)
 	{
 		Player player = (Player)sender;
 		Conversation convo = voteConvFactory.buildConversation(player);
 		convo.begin();
 	}
 }
