 package com.googlecode.prmf;
 
 class Player
 {
 	String name;
 	Role role;
 	// TODO the variables below should have consistent naming
 	boolean isAlive;
 	boolean mafiaTarget;
 	boolean saved;
 	boolean checked;
 	boolean vigged;
 	boolean wasLynched;
 	int votedFor;
 	//ID is the player's position in the list of players
 	int ID;
 	// TODO there is no reason for a player object to know its position
 	// in the list of players
 	
 	public Player(String name)
 	{
 		this.name = name;
 		isAlive = true;
 		role = null;
 		votedFor = -1; // TODO what does this mean?
 	}
 	
 	
	public boolean equals(Object o) 
	{
		boolean ret = false;
		if(o instanceof Player)
		{
			Player temp = (Player)o;
			
			if (this.name.equals(temp.name))
				ret = true;
		}
		return ret;	
 	}
 
	
	public int hashCode()
	{
		return name.hashCode();
	}
 	
 	public String toString()
 	{
 		return name;
 	}
 
 }
