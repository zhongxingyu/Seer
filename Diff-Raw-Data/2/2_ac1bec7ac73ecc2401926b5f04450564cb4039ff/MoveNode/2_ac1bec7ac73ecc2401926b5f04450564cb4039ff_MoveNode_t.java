 package com.chess.genesis;
 
 class MoveNode implements Comparable<MoveNode>
 {
 	public Move move;
 	public int score;
 	public boolean check;
 
 	public MoveNode()
 	{
 		score = 0;
 		check = false;
 		move = new Move();
 	}
  
 	public int compareTo(final MoveNode a)
 	{
		return a.score - score;
 	}
 }
