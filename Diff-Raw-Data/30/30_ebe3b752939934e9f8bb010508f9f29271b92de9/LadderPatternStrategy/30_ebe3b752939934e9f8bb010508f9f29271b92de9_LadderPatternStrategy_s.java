 package polarizedladder;
 
 import java.awt.Point;
 
 public class LadderPatternStrategy extends GameHeuristics
 {	
 	private Point ladderPatterns[][] = winPatterns;
 
 	////////////////////////////////////////////////////////////////////////////////////////////////////
 
	private int[] ladderDiscWeights  = {0, 1, 2, 3, 100, 1000};		// weights for 0, 1, 2, 3, 4, or 5 ladder discs
 
 	/* TODO: validate - there is no polarity block on sides
 		private Point leftSidePolarity[] = { new Point(1,1), new Point(2,2), new Point(3,3), 
 				new Point(4,4), new Point(5,5), new Point(6,6), new Point(7,7)};
 
 		private Point rightSidePolarity[] = { new Point(13,1), new Point(12,2), new Point(11,3), 
 				new Point(10,4), new Point(9,5), new Point(8,6), new Point(7,7)};
 	 */
 
 	public int calculate(Player p, Player o, Board b)
 	{		
 		int numPossLadders	 = 0;	// heuristic: player possible ladders - opponent possible ladders
 		int ladderDiscs		 = 0;	// heuristic: weighted sum of ladder discs
 
 		String[][] board 	 = b.getState();			
 
 		String playerToken   = p.getPlayerToken();	
 		String opponentToken = o.getPlayerToken();
 
 		// analyze every populated board point for potential ladders
 		for (int i = b.BOARD_ROWS - 1; i >= 1 ; i--) 		 
 		{	
 			int jFrom   = i;
 			int jTo 	= b.BOARD_COLS - i;
 
 			for ( ; jFrom <= jTo; jFrom++) 					
 			{
 				if ( (board[i][jFrom]).equalsIgnoreCase(playerToken) )
 				{
 					// add player disc potential ladders
 					numPossLadders += detectPossibleLadders( playerToken, opponentToken, board, new Point(jFrom, i) );
 					ladderDiscs	   += numberOfLadderDiscs( playerToken, opponentToken, board, new Point(jFrom, i) );
 
 				//	System.out.printf("P1 Ladders = %d\n", numPossLadders);
 				//	System.out.printf("P1 Ladder Discs = %d\n", ladderDiscs);
 				}
 				else if ( (board[i][jFrom]).equalsIgnoreCase(opponentToken) )
 				{
 					// subtract opponent's disc potential ladders
 					numPossLadders -= detectPossibleLadders( opponentToken, playerToken, board, new Point(jFrom, i) );
 					ladderDiscs    -= numberOfLadderDiscs( opponentToken, playerToken, board, new Point(jFrom, i) );
 
 				//	System.out.printf("P2 Ladders = %d\n", numPossLadders);
 				//	System.out.printf("P2 Ladder Discs = %d\n", ladderDiscs);
 				}
 			}
 		}
 
 		return (numPossLadders + ladderDiscs);
 	}
 
 	private int detectPossibleLadders(String playerToken, String opponentToken, String[][] board, Point boardPoint)
 	{
 		int possibleLadders	= 0;
 
 		// calculate potential ladders...
 		for (int row = 0; row < ladderPatterns.length; row++)
 		{		
 			// ...for each possible ladder pattern
 			Boolean currLadderBlocked = false;
 
 			for (int col = 0; col < ladderPatterns[0].length; col++)	
 			{
 				int xPos = boardPoint.x + ladderPatterns[row][col].x;
 				int yPos = boardPoint.y + ladderPatterns[row][col].y;
 
 				try
 				{
 					if ( col == 2 ) 
 					{
 						// use ladder pattern midpoint to determine ladder blocked
 						currLadderBlocked = isLadderPolarityBlocked( playerToken, opponentToken, board, new Point(xPos, yPos) );
 
 						if (currLadderBlocked)
 						{
 						//	System.out.printf("Current Ladder Blocked: row = %d, col=%d\n", row, col);
 							break;
 						}
 					}
 
 					if ( (board[yPos][xPos].equalsIgnoreCase(playerToken)) || 
 							(board[yPos][xPos].equalsIgnoreCase("_")) )
 					{
 						// ladder not blocked
 					}
 					else
 					{
 						// ladder blocked
 						currLadderBlocked = true;
 						break;
 					}
 				}
 				catch (Exception e)
 				{
 					// array out of bounds position, ladder blocked
 					currLadderBlocked = true;
 					break;
 				}
 			}
 
 			possibleLadders = (currLadderBlocked == true) ? possibleLadders : possibleLadders + 1;
 		}
 
 		return possibleLadders;
 	}
 
 	private int numberOfLadderDiscs(String playerToken, String opponentToken, String[][] board, Point boardPoint)
 	{
 		int numOfLadderDiscs = 0;
 
 		for (int row = 0; row < ladderPatterns.length; row++)			// calculate non-blocked ladders discs...
 		{		
 			Boolean currLadderBlocked = false;
 			int numOfLadDiscsDetected = 0;
 
 			for (int col = 0; col < ladderPatterns[0].length; col++)	// ...for each possible ladder pattern	
 			{
 				int xPos = boardPoint.x + ladderPatterns[row][col].x;
 				int yPos = boardPoint.y + ladderPatterns[row][col].y;
 
 				try
 				{
 					if ( col == 2 ) 
 					{
 						// use ladder pattern midpoint to determine ladder blocked
 						currLadderBlocked = isLadderPolarityBlocked( playerToken, opponentToken, board, new Point(xPos, yPos) );
 
 						if (currLadderBlocked)
 						{
 							numOfLadDiscsDetected 	= 0;	// do not count blocked ladders
 						//	System.out.printf("Current Ladder Blocked: row = %d, col=%d\n", row, col);
 							break;
 						}
 					}
 
 					if ( (board[yPos][xPos].equalsIgnoreCase(playerToken)) || 
 							(board[yPos][xPos].equalsIgnoreCase("_")) )
 					{
 						// ladder not blocked
 						if ( (board[yPos][xPos].equalsIgnoreCase(playerToken)) )
 						{
 							// detected disc, record detection.
 							numOfLadDiscsDetected++;
 						}
 					}
 					else
 					{
 						// ladder blocked
 						currLadderBlocked 		= true;
 						numOfLadDiscsDetected 	= 0;	// do not count blocked ladders 
 						break;
 					}
 				}
 				catch (Exception e)
 				{
 					// array out of bounds position, ladder blocked
 					currLadderBlocked 		= true;
 					numOfLadDiscsDetected 	= 0;		// do not count blocked ladders
 					break;
 				}
 			}
 
 			numOfLadderDiscs = (currLadderBlocked == false) ? 
 					numOfLadderDiscs + ladderDiscWeights[numOfLadDiscsDetected] : 
 						numOfLadderDiscs;
 		}
 
 		return numOfLadderDiscs;
 	}
 
 	public boolean isLadderPolarityBlocked(String playerToken, String opponentToken, String[][] board, Point boardPoint)
 	{
 		// check left ladder blocks
 		Point p1 = new Point(boardPoint.x - 1, boardPoint.y - 1);	// lower left block
 		Point p2 = new Point(boardPoint.x + 1, boardPoint.y + 1);	// upper right block
 
 		// if board points p1 and p2 are blocked...
 		if ( (board[p1.y][p1.x].equalsIgnoreCase(opponentToken)) &&
 				(board[p2.y][p2.x].equalsIgnoreCase(opponentToken)) )
 		{			
 			// ladder polarized?
 			boolean isPolarizable = isPolarizable(playerToken, opponentToken, board, boardPoint);
 
 			if (isPolarizable)
 			{
 				return false;
 			}
 
 			// ladder blocked
 		//	System.out.println("Left Ladder Blocked!");
 
 			return true;
 		}
 
 		// check right ladder blocks
 		Point p3 = new Point(boardPoint.x - 1, boardPoint.y + 1);	// upper left block
 		Point p4 = new Point(boardPoint.x + 1, boardPoint.y - 1);	// lower right block
 
 		// if board points p3 and p4 are blocked...
 		if ( (board[p3.y][p3.x].equalsIgnoreCase(opponentToken)) &&
 				(board[p4.y][p4.x].equalsIgnoreCase(opponentToken)) )
 		{			
 			// ladder polarizable
 			boolean isPolarizable = isPolarizable(playerToken, opponentToken, board, boardPoint);
 
 			if (isPolarizable)
 			{
 				return false;
 			}
 
 			// ladder blocked
 		//	System.out.println("Right Ladder Blocked!");
 
 			return true;
 		}
 
 		return false;
 	}
 
 	public boolean isPolarizable(String playerToken, String opponentToken, String[][] board, Point boardPoint)
 	{
 		// check polarity using pattern mid point
 		if ( !(board[boardPoint.y - 1][boardPoint.x].equalsIgnoreCase(opponentToken)) &&				// right ladder base polarity 
 				!(board[boardPoint.y - 1][boardPoint.x - 1].equalsIgnoreCase(opponentToken)) &&
 				(boardPoint.y - 1 == 1) )  
 		{
 		//	System.out.println("Base of Right Ladder Polarized!");
 			return true;
 		}
 
 		if ( !(board[boardPoint.y - 1][boardPoint.x].equalsIgnoreCase(opponentToken)) &&				// left ladder base polarity 
 				!(board[boardPoint.y - 1][boardPoint.x + 1].equalsIgnoreCase(opponentToken)) &&
 				(boardPoint.y - 1 == 1) ) 
 
 		{
 		//	System.out.println("Base of Left Ladder Polarized!");
 			return true;
 		}
 
 		/* TODO: validate: there is no possible polarity block on the sides
 			for (int i = 1; i < leftSidePolarity.length - 1; i++)											 // left side polarity
 			{
 				if ( boardPoint.equals(leftSidePolarity[i]) )	
 				{
 					if ( !(board[boardPoint.y - 1][boardPoint.x - 1].equalsIgnoreCase(opponentToken)) && 
 							!(board[boardPoint.y + 1][boardPoint.x + 1].equalsIgnoreCase(opponentToken)) )
 					{
 						System.out.println("Right Ladder Polarized!");	
 						return true;
 					}
 				}
 			}
 
 			for (int i = 1; i < rightSidePolarity.length - 1; i++)											 // right side polarity
 			{	
 				if ( boardPoint.equals(rightSidePolarity[i]) )	
 				{
 					if ( !(board[boardPoint.y + 1][boardPoint.x - 1].equalsIgnoreCase(opponentToken)) && 
 							!(board[boardPoint.y - 1][boardPoint.x + 1].equalsIgnoreCase(opponentToken)) )
 					{
 						System.out.println("Left Ladder Polarized!");					
 						return true;
 					}
 				}
 			}
 		 */
 
 		return false;
 	}
 
 	public static void main(String[] args) 
 	{
 		
 			// Heuristics Test Case 1 
 			Board boardA = new Board();			// state a
 
 			boardA.setPosition(2, 2, "*");		// player positions
 			boardA.setPosition(3, 2, "*");
 			//boardA.setPosition(9, 3, "*");
 			//boardA.setPosition(9, 4, "*");
 
 			boardA.setPosition(7, 3, "o");		// opponent positions
 			boardA.setPosition(8, 6, "o");
 			//boardA.setPosition(7, 4, "o");
 			//boardA.setPosition(8, 4, "o");
 
 			boardA.printBoard();
 
 			LadderPatternStrategy hA = new LadderPatternStrategy();
 			Player pA 		 = new Player("Player 1", '*', 22, boardA);
 			Player oA 		 = new Player("Player 1", 'o', 22, boardA);
 			int scoreA 		 = hA.calculate(pA, oA, boardA);
 			System.out.printf("Board A Heuristics Score = %d\n", scoreA);
 
 			Board boardB = new Board();			// state b
 
 			boardB.setPosition(8, 2, "*");		// player positions
 			boardB.setPosition(8, 3, "*");
 			boardB.setPosition(9, 3, "*");
 			boardB.setPosition(8, 5, "*");
 
 			boardB.setPosition(6, 3, "o");		// opponent positions
 			boardB.setPosition(7, 3, "o");
 			boardB.setPosition(7, 4, "o");
 			boardB.setPosition(8, 4, "o");
 
 			boardB.printBoard();
 
 			LadderPatternStrategy hB = new LadderPatternStrategy();
 			Player pB 		  = new Player("Player 1", '*', 22, boardB);
 			Player oB 		  = new Player("Player 2", 'o', 22, boardB);
 			int scoreB		  = hB.calculate(pB, oB, boardB);
 
 			System.out.printf("Board B Heuristics Score = %d\n", scoreB);
 		 
 
 		/*
 			// Heuristics Test Case 2 
 			Board boardA = new Board();			// state a
 
 			boardA.setPosition(3, 8, "*");		// player positions
 
 			boardA.setPosition(4, 7, "o");		// opponent positions
 			boardA.setPosition(2, 9, "o");
 			boardA.setPosition(1, 10, "o");
 
 			boardA.printBoard();
 
 			GameHeuristics hA = new GameHeuristics();
 			Player pA 		  = new Player("Player 1", '*', 22, boardA);
 			Player oA 		  = new Player("Player 2", 'o', 22, boardA);
 			int scoreA 		  = hA.calculate(pA, oA, boardA);
 			System.out.printf("Board A Heuristics Score = %d\n", scoreA);
 		 */
 	}
 }
 
 
 
 ////////////////////////////////////////////////////////////////////////////////////////////////////      
