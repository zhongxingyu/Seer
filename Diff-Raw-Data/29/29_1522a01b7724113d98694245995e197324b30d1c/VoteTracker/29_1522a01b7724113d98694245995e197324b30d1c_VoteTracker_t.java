 package com.googlecode.prmf;
 
 import com.googlecode.prmf.starter.IOThread;
 
 public class VoteTracker {
 	//TODO default visibility is almost as bad as public
 	int[] votes;
 	int noLynchVotes;
 	int notVoting;
 	int numberOfPlayers;
 	Player[] players;
 	
 	public VoteTracker(Player[] players) {
 		this.players = players;
 		notVoting = players.length;
 		numberOfPlayers = players.length;
 		noLynchVotes = 0;
 		votes = new int[players.length];
 		clearAllVotes();
 	}
 
 	private void clearAllVotes() {
		for (Player p : players)
		{
			p.votedFor = -1;
 		}
 	}
 	
 	public void status(IOThread inputThread) {
 		String toPrint = ""; // TODO use a StringBuilder instead of a String
 								// here for better performance so you don't
 								// create a bunch of objects
 	
 		for (int i=0;i<numberOfPlayers;++i)
 		{
 			String toAdd = "";
 			//must check if player is alive because player can !quit is set to dead.
 			if (votes[i] == 0 && players[i].isAlive) continue;
 			if (toPrint.length() > 0)
 				toAdd += ", ";
 			toAdd += players[i].name;
 			toAdd += " has ";
 			toAdd += votes[i];
 			toAdd += " votes";
 			toPrint += toAdd;
 		}
 		if (noLynchVotes > 0)
 		{
 			String toAdd = "";
			if (toPrint.length() > 0)
				toAdd += ", ";
 			toAdd += "No Lynch has ";
 			toAdd += noLynchVotes;
			toAdd += " votes";
			toPrint += toAdd;
 		}
 		toPrint += ".";
 		inputThread.sendMessage("#UFPT", toPrint);
 	}
 	
 	// TODO probably better (i.e. OOP-ish) to pass in a Player object rather than an int voter 
 	public int newVote(int voter, int voted , IOThread inputThread)
 	{
 		/*in Player, votes for -1 is no vote
 		 * -2 is no lynch
 		 *  0-# of players-1 represents the actual players
 		 *  
 		 *  the return types represent who was lynched: -1 is no majority, -2 is nolynch
 		 *   >=0 is the player lynched
 		 */
 
 		//day.processVote(..) takes care of whether voted is valid player
 		
 		System.err.println(voter + " " + voted);
 		if ( voted >= 0)
 		{
 			if (!players[voter].isAlive)
 				return -1;
 			if( players[voter].votedFor >= 0)
 			{
 				--votes[players[voter].votedFor];
 			}
 			else if( players[voter].votedFor == -2)
 			{
 				--noLynchVotes;
 			}
 			players[voter].votedFor = voted;
 			++votes[voted];
 			System.err.println(voted + " has " + votes[voted]+ " votes");
 		}
 		else if( voted == -1)
 		{
 			if(players[voter].votedFor >= 0)
 			{
 				//uncount his previous vote
 				--votes[players[voter].votedFor];
 				players[voter].votedFor = -1;
 			}
 			else if( players[voter].votedFor == -2)
 			{
 				--noLynchVotes;
 				players[voter].votedFor = -1;
 			}
 			else
 			{
 				// if command was to unvote AND players vote was already -1 .. 
 				// he had no vote to begin with...!unvote command should do nothing
 				return -1;
 			}
 		}
 		else if( voted == -2)
 		{
 			if(players[voter].votedFor >= 0 )
 			{
 				--votes[players[voter].votedFor];
 			}
 			if( players[voter].votedFor != -2)
 				++noLynchVotes;
 			
 			players[voter].votedFor = -2;
 		}
 		
 		status(inputThread);
 		return checkMajority();
 	}
 	
 	public int checkMajority() 
 	{
		int result = -1;
 		int needed = numberOfPlayers/2 + 1;
 		if (noLynchVotes >= needed)
			result = 2;
 		for (int i=0;i<numberOfPlayers;++i)
 			if(votes[i] >= needed)
				result = i;	
		return result;
 	}
 	
 }
