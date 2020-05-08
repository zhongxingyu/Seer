 package com.googlecode.prmf;
 
 import com.googlecode.prmf.starter.IOThread;
 
 //TODO: I'm thinking this class needs to be totally redone... the player object should probably track its own votes
 //and it can report back to this when status updates are needed, or something like that
 
 public class VoteTracker {
 	
 	private int[] votes;
 	private int noLynchVotes;
 	private int numberOfPlayers;
 	private Player[] players;
 	
 	public VoteTracker(Player[] players) {
 		this.players = players;
 		numberOfPlayers = players.length;
 		noLynchVotes = 0;
 		votes = new int[players.length];
 		clearAllVotes();
 	}
 
 	private void clearAllVotes() {
 		for (Player p : players)
 		{
 			p.setVote(-1);
 		}
 	}
 	
 	public void status(IOThread inputThread) {
 		StringBuilder toPrint = new StringBuilder();
 	
 		for (int i=0;i<numberOfPlayers;++i)
 		{
 			StringBuilder toAdd = new StringBuilder();
 			
 			//must check if player is alive because player can !quit is set to dead.
 			if (votes[i] == 0 || !players[i].isAlive()) continue;
 			if (toPrint.length() > 0)
 				toAdd.append(", ");
 			toAdd.append(players[i].getName());
 			toAdd.append(" has ");
 			toAdd.append(votes[i]);
 			toAdd.append((votes[i] == 1) ? " vote" : " votes");
 			toPrint.append(toAdd);
 		}
 		if (noLynchVotes > 0)
 		{
 			StringBuilder toAdd = new StringBuilder();
 			if (toPrint.length() > 0)
 				toAdd.append(", ");
 			toAdd.append("No Lynch has ");
 			toAdd.append(noLynchVotes);
 			toAdd.append((noLynchVotes == 1) ? " vote" : " votes");
 			toPrint.append(toAdd);
 		}
 		if(toPrint.length() == 0)
 		{
 			toPrint.append("There are currently no votes");
 			
 		}
 		toPrint.append(".");
 		inputThread.sendMessage("#UFPT", toPrint.toString());
 	}
 	
 	// TODO probably better (i.e. OOP-ish) to pass in a Player object rather than an int voter 
 	public int newVote(int voter, int voted , IOThread inputThread)
 	{
 		Action action = null;
 		/*in Player, votes for -1 is no vote
 		 * -2 is no lynch
 		 *  0-# of players-1 represents the actual players
 		 *  
 		 *  the return types represent who was lynched: -1 is no majority, -2 is nolynch
 		 *   >=0 is the player lynched
 		 */
 
 		//day.processVote(..) takes care of whether voted is valid player
 		if (!players[voter].isAlive())
 			return -1;
 		if ( voted >= 0)
 		{
 			action = new VoteAction(voter, voted);
 		}
 		else if( voted == -1)
 		{
 			action = new UnvoteAction(voter);
 		}
 		else if( voted == -2)
 		{
 			action = new NoLynchAction(voter);
 		}
 		
 		action.handle();
 		status(inputThread);
 		return checkMajority();
 	}
 	
 	public int checkMajority() 
 	{
 		int result = -1;
 		int needed = 0;
 		for (Player p : players)
 			if (p.isAlive())
 				++needed;
 		needed = needed/2 + 1;
 		if (noLynchVotes >= needed)
 			result = -2;
 		for (int i=0;i<numberOfPlayers;++i)
 			if(votes[i] >= needed)
 				result = i;	
 		return result;
 	}
 	
 	class UnvoteAction implements Action
 	{
 		private Player voter;
 		public UnvoteAction(int voter)
 		{
 			this.voter = players[voter];
 		}
 		public void handle()
 		{
 			if(voter.getVote() >= 0)
 			{
 				--votes[voter.getVote()];
 			}
 			else if(voter.getVote() == -2)
 			{
 				--noLynchVotes;
 			}
 			voter.setVote(-1);
 		}
 	}
 	
 	class NoLynchAction implements Action
 	{
 		private Player voter;
 		public NoLynchAction(int voter)
 		{
 			this.voter = players[voter];
 		}
 		public void handle()
 		{
 			if(voter.getVote() >= 0)
 			{
 				--votes[voter.getVote()];
 			}
 			else if(voter.getVote() == -2)
 			{
 				--noLynchVotes;
 			}
 			voter.setVote(-2);
 		}
 	}
 	
 	class VoteAction implements Action
 	{
 		private Player voter;
 		private int target;
 		public VoteAction(int voter, int voted)
 		{
 			this.voter = players[voter];
 			this.target = voted;
 		}
 		public void handle()
 		{
 			if( voter.getVote() >= 0)
 			{
 				--votes[voter.getVote()];
 			}
 			else if(voter.getVote() == -2)
 			{
 				--noLynchVotes;
 			}
 			voter.setVote(target);
 			++votes[target];
 		}
 	}
 	
 }
