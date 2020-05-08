 package edu.berkeley.gamesman.game;
 
 public interface LoopyGame<S> {
 	public int maxParents();
	public int possibleParents(S pos, S[] parents);
 }
