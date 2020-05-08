package com.game.weichi;
 
 public class GoMove implements Comparable<GoMove> {
 	
 	final int  x;
 	final int y;
 	
 	
 	public static final GoMove pass=new GoMove(-1,0);
 	GoMove(int a, int b)
 	{
 		x=a;
 		y=b;
 	}
 
     public boolean equals( Object oth)
     {
         if( oth == null) return false;
         GoMove other = (GoMove) oth;
         return (other.x == x) &&
                 (other.y == y);
         
     }
 	
 	public static GoMove parseMove(String input) {
         String[] inputParts = input.split(" ");
         if( Integer.decode(inputParts[0]) < 0) return pass;
         GoMove newMove = GoGame.allMoves[ Integer.decode(inputParts[0])][Integer.decode(inputParts[1])];
         return newMove;
 	}
 
 	public String toString()
 	{
 		return x + " " + y;
 	}
 	
 	public void printMove()
 	{
 		System.out.println("the move is " + toString());
 	}
 
     public int compareTo( GoMove other)
     {
         if( other.x< this.x || 
                 (other.x == this.x && other.y< this.y ))
         return 1;
         if( other.x == this.x && other.y == this.y)
             return 0;
         return -1;
     }
 }
