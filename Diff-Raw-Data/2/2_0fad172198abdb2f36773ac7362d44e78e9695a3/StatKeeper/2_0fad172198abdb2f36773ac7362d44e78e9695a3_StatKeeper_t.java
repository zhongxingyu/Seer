 package edu.ucsb.cs56.projects.scrapers.baseball_stats;
 
 import java.util.ArrayList;
 
 /** StatKeeper holds ALL of the statistics that have been parsed.
  @author Sam Baldwin
 */
 public class StatKeeper
 {
 	private ArrayList<Player> players;
 	
 	public StatKeeper()
 	{
 		players = new ArrayList<Player>(50);
 	}
 	
 	/** Adds the input Player to the Player ArrayList. If the Player already exists in the ArrayList then all of the Statistics for that player are incremented by the value in the input Player's Statistic.
 	    @param p Player to add to the ArrayList.	 
 	*/
 	public void addPlayer(Player p)
 	{
 		int index = this.getPlayerIndex(p);
 		
 		if(index == -1)
 		{
 			players.add(p);
 		}
 		else
 		{
 			for(int counter = 0; counter < p.getStatsAmount(); counter ++)
 			{
 				players.get(index).addStatistic(p.getStat(counter));
 			}
 		}
 	}
 	
 	/** Adds the input Player ArrayList to the instance Player ArrayList. If a Player in the input list already exists in the instance ArrayList then all of the Statistics for that player are incremented by the value in the input Player's Statistic.
 	    @param p Player ArrayList to add to the ArrayList. 
 	*/
 	public void addPlayer(ArrayList<Player> p)
 	{
 		for(int counter = 0; counter < p.size(); counter ++)
 		{
 			this.addPlayer(p.get(counter));
 		}
 	}
 	
 	/** Returns the index in the ArrayList of the Player with the input ID. If the Player is not found then -1 is returned
 	    @return the index in the ArrayList of the Player with the input ID
 	    @param id ID of player to search for.
 	*/
 	public int getPlayerIndex(int id)
 	{
 		for(int counter = 0; counter < players.size(); counter ++)
 		{
 			if(players.get(counter).getID() == id)
 			{
 				return counter;
 			}
 		}
 		
 		return -1;
 	}
 	
 	/** Returns the index in the ArrayList of the input Player. If the player is not found, then returns -1.
	    @param p Player to search for
 	*/
 	public int getPlayerIndex(Player p)
 	{
 		for(int counter = 0; counter < players.size(); counter ++)
 		{
 			if(players.get(counter).getID() == p.getID())
 			{
 				return counter;
 			}
 		}
 		
 		return -1;
 	}
 	
 	/** 
 	    @return the number of players in the ArrayList.
 	*/
 	public int getPlayerCount()
 	{
 		return players.size();
 	}
 
         /** 
 	    @param index used to search the Player ArrayList 
 	    @return the player at a specific index
 	*/
 	public Player getPlayer(int index)
 	{
 		return players.get(index);
 	}
 
 	/** 
 	    @param index used to search the Player ArrayList 
 	    @return the full name of the player at a specific index
 	*/
 	public String getPlayerName(int index)
 	{
 		return players.get(index).getFullName();
 	}
 	
 	/** Creates a new ArrayList of players, essentially starting over all stats from scratch.
 	*/
 	public void newStats()
 	{
 		players = new ArrayList<Player>(50);
 	}
 }
