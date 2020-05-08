 package org.vxp7755_nxz3937.freeforall;
 
 public class Board {
 	
 	private int boardWidth;
 	private int boardHeight;
 	private PieceThread boardCells[][];
 	private int scores[] = {0,0,0,0};
 	
	/**
	 * Constructor method
	 * 
	 * @param width		width of the game board in cells
	 * @param height	height of the game board in cells
	 */
 	Board( int width, int height)
 	{
 		// init boardCells
 		this.boardWidth = width;
 		this.boardHeight = height;
 		boardCells = new PieceThread[this.boardWidth][this.boardHeight];
 	}
 	
 	public PieceThread getCell( int x, int y )
 	{
 		return null;
 	}
 	
 	/**
 	 * Place the provided Piece Thread in the specified position
 	 * 
 	 * @param x		horizontal position to set piece, left to right
 	 * @param y		vertical position to set piece, top to bottom
 	 * @param piece PieceThread to set in specified position, provide null to
 	 * 				clear cell
 	 * @return true if piece was place in valid position, otherwise false
 	 */
 	public boolean setCell( int x, int y, PieceThread piece )
 	{
		if ( (x < 0 || x >= this.boardWidth )
				|| (y < 0 || y >= this.boardHeight) ) {
			return false;
		} else {
			this.boardCells[x][y] = piece;
			return true;
		}
 	}
 	
 	/**
 	 * increments the score of the specified team
 	 * 
 	 * @param team		 the team number who's score should be incremented,
 	 * 					 accepts 1 - 4.
 	 * @return  the newly incremented score for the specified team, 
 	 * 			returns -1 if an invalid team # was provided
 	 */
 	public int givePoint( int team )
 	{
 		// check team number
 		if ( team < 1 || team > 4 ) {
 			// return -1 if invalid team
 			return -1;
 		} else {
 			// otherwise, increment and return new score
 			int pos = team--;
 			this.scores[pos]++;
 			return this.scores[pos];
 		}
 	}
 	
 	/**
 	 * Retrieve the array of scores for each team
 	 * 
 	 *  @return array of team scores ints, i.e. {team1, team2, team3, team4}
 	 */
 	public int[] getScores() { return this.scores; }
 }
