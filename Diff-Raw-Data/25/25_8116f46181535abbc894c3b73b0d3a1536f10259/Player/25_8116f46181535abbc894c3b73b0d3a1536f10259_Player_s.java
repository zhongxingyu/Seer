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
 	
	// TODO the equals() method should be public take an argument of type Object
	boolean equals(Player p)
	{
		if (this.name.equals(p.name))
			return true;
		return false;
 	
 	}
 
	// TODO if you override equals(), Java practice states that you should also override hashCode()
 	
 	public String toString()
 	{
 		return name;
 	}
 
 }
