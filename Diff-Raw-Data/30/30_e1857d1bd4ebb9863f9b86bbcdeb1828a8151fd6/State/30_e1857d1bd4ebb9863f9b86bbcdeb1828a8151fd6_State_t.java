 /*
  * state node class
  * this class defines the states
  * for the 8-puzzle problem
  */
 
 import java.util.* ;
 
 public class State
 {
 
	private int[ ][ ] content ;	// the configuration of the state
 	private int heuristic ;		// the heuristic value of the state
 
 	/*
 	 * the constructor defines a configuration of the problem
 	 * in the following way:
 	 *      a b c
 	 *      d e f
 	 *      g h i
 	 * assume that each letter stands for a digit from 0 to 8
 	 * with 0 denotes the blank
 	 * the heuristic value is set to -1
 	 */
 	public State ( int a, int b, int c, int d, int e, int f, int g, int h, int i )
 	{
		content = new int[ 3 ][ 3 ] ;
		content[ 0 ][ 0 ] = a ;
		content[ 0 ][ 1 ] = b ;
		content[ 0 ][ 2 ] = c ;
		content[ 1 ][ 0 ] = d ;
		content[ 1 ][ 1 ] = e ;
		content[ 1 ][ 2 ] = f ;
		content[ 2 ][ 0 ] = g ;
		content[ 2 ][ 1 ] = h ;
		content[ 2 ][ 2 ] = i ;
 		heuristic = -1 ;
 	}
 
 	// an empty state
	public State ()
 	{
		content   = new int[ 3 ][ 3 ] ;
		heuristic = -1 ;
 	}
 
 	// copy a state
 	public State copy ()
 	{
 		State n = new State();
 		for ( int i = 0 ; i < 3 ; i++ )
 			for ( int j = 0 ; j < 3 ; j++ )
 				n.content[ i ][ j ] = content[ i ][ j ] ;
 
 		return n ;
 	}
 
 	// checking for equality between two states
 	public boolean equal ( State n )
 	{
 		for ( int i = 0 ; i < 3 ; i++ )
 			for ( int j = 0 ; j < 3 ; j++ )
 				if ( n.content[ i ][ j ] != content[ i ][ j ] )
 					return false ;
 
 		return true ;
 	}
 
 	// execution of action "move blank to left"
 	public State left ()
 	{
 		State n = this.copy() ;
 		for ( int i = 0 ; i < 3 ; i++ )
 		{
 			for ( int j = 0 ; j < 3 ; j++ )
 			{
 				if ( n.content[ i ][ j ] == 0 )
 				{
 					if ( j == 0 )
 						return n ;
 					n.content[ i ][ j ]     = n.content[ i ][ j - 1 ] ;
 					n.content[ i ][ j - 1 ] = 0 ;
 					return n ;
 				}
 			}
 		}
 		return n ;
 	}
 
 	// execution of action "move blank to right"
 	public State right ()
 	{
 		State n = this.copy() ;
 		for ( int i = 0 ; i < 3 ; i++ )
 		{
 			for ( int j = 0 ; j < 3 ; j++ )
 			{
 				if ( n.content[ i ][ j ] == 0 )
 				{
 					if ( j == 2 )
 						return n ;
 					n.content[ i ][ j ]     = n.content[ i ][ j + 1 ] ;
 					n.content[ i ][ j + 1 ] = 0 ;
 					return n ;
 				}
 			}
 		}
 		return n ;
 	}
 
 	// execution of action "move blank up"
 	public State up ()
 	{
 		State n = this.copy() ;
 		for ( int i = 0 ; i < 3 ; i++ )
 		{
 			for ( int j = 0 ; j < 3 ; j++ )
 			{
 				if ( n.content[ i ][ j ] == 0 )
 				{
 					if ( i == 0 )
 						return n ;
 					n.content[ i ][ j ]     = n.content[ i - 1 ][ j ] ;
 					n.content[ i - 1 ][ j ] = 0 ;
 					return n ;
 				}
 			}
 		}
 		return n ;
 	}
 
 	// execution of action "move blank down"
 	public State down ()
 	{
 		State n = this.copy() ;
 		for ( int i = 0 ; i < 3 ; i++ )
 		{
 			for ( int j = 0 ; j < 3 ; j++ )
 			{
 				if ( n.content[ i ][ j ] == 0 )
 				{
 					if ( i == 2 )
 						return n ;
 					n.content[ i ][ j ]     = n.content[ i + 1 ][ j ] ;
 					n.content[ i + 1 ][ j ] = 0 ;
 					return n ;
 				}
 			}
 		}
 		return n ;
 	}
 
 	// a controller for the execution of actions
 	public State execution ( int action )
 	{
 		switch ( action )
 		{
 			case 1:
 				return this.left() ;
 			case 2:
 				return this.right() ;
 			case 3:
 				return this.down() ;
 			case 4:
 				return this.up() ;
 		}
 		return null ;
 	}
 
 	// evaluate the state and compute the heuristic value of the state
 	public void evaluate(State goal)
 	{
 		int d = 0 ;
 		for ( int i = 0 ; i < 3 ; i++ )
 			for (int j=0; j < 3; j++ )
 				d += distance ( goal.content[ i ][ j ] , i , j ) ;
 		heuristic = d ;
 	}
 
 	// return the heuristic value of the state
 	public int getHeuristic ()
 	{
 		return heuristic ;
 	}
 
 	// compute the manhattan distance for one position
 	public int distance ( int value , int row , int column )
 	{
 		int v = 0 ;
 		for ( int i = 0 ; i < 3 ; i++ )
 			for (int j=0; j < 3; j++ )
 				if ( content[ i ][ j ] == value )
 				 return Math.abs( i-row ) + Math.abs( j-column ) ;
 
 		return v;
 	}
 
 	// display the content of a state
 	public String toString ()
 	{
 		return " " + content[ 0 ][ 0 ] + " " + content[ 0 ][ 1 ] + " " + content[ 0 ][ 2 ] + "\n" +
 					 " " + content[ 1 ][ 0 ] + " " + content[ 1 ][ 1 ] + " " + content[ 1 ][ 2 ] + "\n" +
 					 " " + content[ 2 ][ 0 ] + " " + content[ 2 ][ 1 ] + " " + content[ 2 ][ 2 ] + "\n" ;
 	}
 }
