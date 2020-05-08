 /**
  * 
  */
 package Server;
 
 /**
  * The game board for the Can't Stop board game. Each player's game pieces
  * are stored and moved by the BoardManager.
  * 
  * @author Alex MacKenzie
  * @author Matthew Rozon
  *
  */
 public class BoardManager {
 	private int tempCounter;
 	private GamePiece[][][] board;
 	/*
 	private GamePiece[][] boardCol2;
 	private GamePiece[][] boardCol3;
 	private GamePiece[][] boardCol4;
 	private GamePiece[][] boardCol5;
 	private GamePiece[][] boardCol6;
 	private GamePiece[][] boardCol7;
 	private GamePiece[][] boardCol8;
 	private GamePiece[][] boardCol9;
 	private GamePiece[][] boardCol10;
 	private GamePiece[][] boardCol11;
 	private GamePiece[][] boardCol12;
 	*/
 	
 	/**
 	 * Class constructor.
 	 */
 	public BoardManager(){
 		tempCounter = 0;
 		board = new GamePiece[11][][];
 		
 		board[0] = new GamePiece[3][2];
 		board[1] = new GamePiece[5][2];
 		board[2] = new GamePiece[7][2];
 		board[3] = new GamePiece[9][2];
 		board[4] = new GamePiece[11][2];
 		board[5] = new GamePiece[13][2];
 		board[6] = new GamePiece[11][2];
 		board[7] = new GamePiece[9][2];
 		board[8] = new GamePiece[7][2];
 		board[9] = new GamePiece[5][2];
 		board[10] = new GamePiece[3][2];
 		
 		/*
 		boardCol2 = new GamePiece[3][2];
 		boardCol3 = new GamePiece[5][2];
 		boardCol4 = new GamePiece[7][2];
 		boardCol5 = new GamePiece[9][2];
 		boardCol6 = new GamePiece[11][2];
 		boardCol7 = new GamePiece[13][2];
 		boardCol8 = new GamePiece[11][2];
 		boardCol9 = new GamePiece[9][2];
 		boardCol10 = new GamePiece[7][2];
 		boardCol11 = new GamePiece[5][2];
 		boardCol12 = new GamePiece[3][2];
 		*/
 	}
 	
 	public boolean addPiece(int rowNum, int columnNum, boolean perm, int player){
 		GamePiece newIn = new GamePiece(perm, player);
 		int highestRow = 0;
 		if(columnNum >= 2 && columnNum <= 12){
 			for(int i = 0; i < board[columnNum - 2].length; i++)
 				if(board[columnNum - 2][i][player-1] != null && board[columnNum - 2][i][player-1].getPerm())
 					highestRow = i;
 			if(highestRow > rowNum)
 				board[columnNum - 2][highestRow][player-1] = newIn;
 			else if(rowNum >= board[columnNum - 2].length)
 				return false;
 			else
 				board[columnNum - 2][rowNum][player-1] = newIn;
 			tempCounter++;
 			return true;
 		}
 			/*
 			switch (columnNum){
 				case 2:
 					for(int i = 0; i < boardCol2.length; i++)
 						if(boardCol2[i][player-1] != null && boardCol2[i][player-1].getPerm())
 							highestRow = i;
 					if(highestRow > rowNum)
 						boardCol2[highestRow][player-1] = newIn;
 					else if(rowNum >= boardCol2.length)
 						return false;
 					else
 						boardCol2[rowNum][player-1] = newIn;
 					tempCounter++;
 					break; 
 				case 3:
 					for(int i = 0; i < boardCol3.length; i++)
 						if(boardCol3[i][player-1] != null && boardCol3[i][player-1].getPerm())
 							highestRow = i;
 					if(highestRow > rowNum)
 						boardCol3[highestRow][player-1] = newIn;
 					else if(rowNum >= boardCol3.length)
 						return false;
 					else
 						boardCol3[rowNum][player-1] = newIn;
 					tempCounter++;
 					break;
 				case 4:
 					for(int i = 0; i < boardCol4.length; i++)
 						if(boardCol4[i][player-1] != null && boardCol4[i][player-1].getPerm())
 							highestRow = i;
 					if(highestRow > rowNum)
 						boardCol4[highestRow][player-1] = newIn;
 					else if(rowNum >= boardCol4.length)
 						return false;
 					else
 						boardCol4[rowNum][player-1] = newIn;
 					tempCounter++;
 					break;
 				case 5:
 					for(int i = 0; i < boardCol5.length; i++)
 						if(boardCol5[i][player-1] != null && boardCol5[i][player-1].getPerm())
 							highestRow = i;
 					if(highestRow > rowNum)
 						boardCol5[highestRow][player-1] = newIn;
 					else if(rowNum >= boardCol5.length)
 						return false;
 					else
 						boardCol5[rowNum][player-1] = newIn;
 					tempCounter++;
 					break;
 				case 6:
 					for(int i = 0; i < boardCol6.length; i++)
 						if(boardCol6[i][player-1] != null && boardCol6[i][player-1].getPerm())
 							highestRow = i;
 					if(highestRow > rowNum)
 						boardCol6[highestRow][player-1] = newIn;
 					else if(rowNum >= boardCol6.length)
 						return false;
 					else
 						boardCol6[rowNum][player-1] = newIn;
 					tempCounter++;
 					break;
 				case 7:
 					for(int i = 0; i < boardCol7.length; i++)
 						if(boardCol7[i][player-1] != null && boardCol7[i][player-1].getPerm())
 							highestRow = i;
 					if(highestRow > rowNum)
 						boardCol7[highestRow][player-1] = newIn;
 					else if(rowNum >= boardCol7.length)
 						return false;
 					else
 						boardCol7[rowNum][player-1] = newIn;
 					tempCounter++;
 					break;
 				case 8:
 					for(int i = 0; i < boardCol8.length; i++)
 						if(boardCol8[i][player-1] != null && boardCol8[i][player-1].getPerm())
 							highestRow = i;
 					if(highestRow > rowNum)
 						boardCol8[highestRow][player-1] = newIn;
 					else if(rowNum >= boardCol8.length)
 						return false;
 					else
 						boardCol8[rowNum][player-1] = newIn;
 					tempCounter++;
 					break;
 				case 9:
 					for(int i = 0; i < boardCol9.length; i++)
 						if(boardCol9[i][player-1] != null && boardCol9[i][player-1].getPerm())
 							highestRow = i;
 					if(highestRow > rowNum)
 						boardCol9[highestRow][player-1] = newIn;
 					else if(rowNum >= boardCol9.length)
 						return false;
 					else
 						boardCol9[rowNum][player-1] = newIn;
 					tempCounter++;
 					break;
 				case 10:
 					for(int i = 0; i < boardCol10.length; i++)
 						if(boardCol10[i][player-1] != null && boardCol10[i][player-1].getPerm())
 							highestRow = i;
 					if(highestRow > rowNum)
 						boardCol10[highestRow][player-1] = newIn;
 					else if(rowNum >= boardCol10.length)
 						return false;
 					else
 						boardCol10[rowNum][player-1] = newIn;
 					tempCounter++;
 					break;
 				case 11:
 					for(int i = 0; i < boardCol11.length; i++)
 						if(boardCol11[i][player-1] != null && boardCol11[i][player-1].getPerm())
 							highestRow = i;
 					if(highestRow > rowNum)
 						boardCol11[highestRow][player-1] = newIn;
 					else if(rowNum >= boardCol11.length)
 						return false;
 					else
 						boardCol11[rowNum][player-1] = newIn;
 					tempCounter++;
 					break;
 				case 12:
 					for(int i = 0; i < boardCol12.length; i++)
 						if(boardCol12[i][player-1] != null && boardCol12[i][player-1].getPerm())
 							highestRow = i;
 					if(highestRow > rowNum)
 						boardCol12[highestRow][player-1] = newIn;
 					else if(rowNum >= boardCol12.length)
 						return false;
 					else
 						boardCol12[rowNum][player-1] = newIn;
 					tempCounter++;
 					break;
 				}
 			return true;
 			*/
 			
 			else
 				return false;		
 	}
 	
 	/**
 	 * Removes a temporary game piece from the board.
 	 * @param columnNum the board column to be removed from
 	 * @param player the player's piece to be removed
 	 * @return the row the piece was removed from
 	 */
 	public int removePiece(int columnNum, int player){
 		int row = -1;
 		if(columnNum >= 2 && columnNum <= 12){
 			for(int i = 0; i < board[columnNum - 2].length; i++)
 				if(board[columnNum - 2][i][player-1] != null && !board[columnNum - 2][i][player-1].getPerm())
 					row = i;
 			if(row > -1){
 				board[columnNum - 2][row][player] = null;
 				tempCounter--;
 			}
 		}
 		return row;
 
 			/*
 			switch (columnNum){
 				case 2:
 					for(int i = 0; i < boardCol2.length; i++)
 						if(boardCol2[i][player-1] != null && !boardCol2[i][player-1].getPerm())
 							row = i;
 					if(row > -1){
 						boardCol2[row][player] = null;
 						tempCounter--;
 					}
 					break; 
 				case 3:
 					for(int i = 0; i < boardCol3.length; i++)
 						if(boardCol3[i][player-1] != null && !boardCol3[i][player-1].getPerm())
 							row = i;
 					if(row > -1){
 						boardCol3[row][player] = null;
 						tempCounter--;
 					}
 					break;
 				case 4:
 					for(int i = 0; i < boardCol4.length; i++)
 						if(boardCol4[i][player-1] != null && !boardCol4[i][player-1].getPerm())
 							row = i;
 					if(row > -1){
 						boardCol4[row][player] = null;
 						tempCounter--;
 					}
 					break;
 				case 5:
 					for(int i = 0; i < boardCol5.length; i++)
 						if(boardCol5[i][player-1] != null && !boardCol5[i][player-1].getPerm())
 							row = i;
 					if(row > -1){
 						boardCol5[row][player] = null;
 						tempCounter--;
 					}
 					break;
 				case 6:
 					for(int i = 0; i < boardCol6.length; i++)
 						if(boardCol6[i][player-1] != null && !boardCol6[i][player-1].getPerm())
 							row = i;
 					if(row > -1){
 						boardCol6[row][player] = null;
 						tempCounter--;
 					}
 					break;
 				case 7:
 					for(int i = 0; i < boardCol7.length; i++)
 						if(boardCol7[i][player-1] != null && !boardCol7[i][player-1].getPerm())
 							row = i;
 					if(row > -1){
 						boardCol7[row][player] = null;
 						tempCounter--;
 					}
 					break;
 				case 8: 
 					for(int i = 0; i < boardCol8.length; i++)
 						if(boardCol8[i][player-1] != null && !boardCol8[i][player-1].getPerm())
 							row = i;
 					if(row > -1){
 						boardCol8[row][player] = null;
 						tempCounter--;
 					}
 					break;
 				case 9: 
 					for(int i = 0; i < boardCol9.length; i++)
 						if(boardCol9[i][player-1] != null && !boardCol9[i][player-1].getPerm())
 							row = i;
 					if(row > -1){
 						boardCol9[row][player] = null;
 						tempCounter--;
 					}
 					break;
 				case 10: 
 					for(int i = 0; i < boardCol10.length; i++)
 						if(boardCol10[i][player-1] != null && !boardCol10[i][player-1].getPerm())
 							row = i;
 					if(row > -1){
 						boardCol10[row][player] = null;
 						tempCounter--;
 					}
 					break;
 				case 11: 
 					for(int i = 0; i < boardCol11.length; i++)
 						if(boardCol11[i][player-1] != null && !boardCol11[i][player-1].getPerm())
 							row = i;
 					if(row > -1){
 						boardCol11[row][player] = null;
 						tempCounter--;
 					}
 					break;
 				case 12: 
 					for(int i = 0; i < boardCol12.length; i++)
 						if(boardCol12[i][player-1] != null && !boardCol12[i][player-1].getPerm())
 							row = i;
 					if(row > -1){
 						boardCol12[row][player] = null;
 						tempCounter--;
 					}
 					break;
 			}
 			*/
 	}
 	
 	/**
 	 * Makes the player's temporary pieces permanent.
 	 * @param player the player's pieces to be changed
 	 */
 	public void tempToPerm(int player){
 		int row = -1;
 		for(int i = 0; i < board.length; i++){
 			for(int j = 0; j < board[i].length; j++)
 				if(board[i][j][player-1] != null)
					row = j;
 			if(row > -1){
 				board[i][row][player-1].setPerm(true);
 				row = -1;
 			}
 		}
 		
 		/*
 		for(int i = 0; i < boardCol2.length; i++)
 			if(boardCol2[i][player-1] != null)
 				row = i;
 		if(row > -1){
 			boardCol2[row][player-1].setPerm(true);
 			row = -1;
 		}
 		
 		for(int i = 0; i < boardCol3.length; i++)
 			if(boardCol3[i][player-1] != null)
 				row = i;
 		if(row > -1){
 			boardCol3[row][player-1].setPerm(true);
 			row = -1;
 		}
 		
 		for(int i = 0; i < boardCol4.length; i++)
 			if(boardCol4[i][player-1] != null)
 				row = i;
 		if(row > -1){
 			boardCol4[row][player-1].setPerm(true);
 			row = -1;
 		}
 		
 		for(int i = 0; i < boardCol5.length; i++)
 			if(boardCol5[i][player-1] != null)
 				row = i;
 		if(row > -1){
 			boardCol5[row][player-1].setPerm(true);
 			row = -1;
 		}
 		
 		for(int i = 0; i < boardCol6.length; i++)
 			if(boardCol6[i][player-1] != null)
 				row = i;
 		if(row > -1){
 			boardCol6[row][player-1].setPerm(true);
 			row = -1;
 		}
 		
 		for(int i = 0; i < boardCol7.length; i++)
 			if(boardCol7[i][player-1] != null)
 				row = i;
 		if(row > -1){
 			boardCol7[row][player-1].setPerm(true);
 			row = -1;
 		}
 		
 		for(int i = 0; i < boardCol8.length; i++)
 			if(boardCol8[i][player-1] != null)
 				row = i;
 		if(row > -1){
 			boardCol8[row][player-1].setPerm(true);
 			row = -1;
 		}
 		
 		for(int i = 0; i < boardCol9.length; i++)
 			if(boardCol9[i][player-1] != null)
 				row = i;
 		if(row > -1){
 			boardCol9[row][player-1].setPerm(true);
 			row = -1;
 		}
 		
 		for(int i = 0; i < boardCol10.length; i++)
 			if(boardCol10[i][player-1] != null)
 				row = i;
 		if(row > -1){
 			boardCol10[row][player-1].setPerm(true);
 			row = -1;
 		}
 		
 		for(int i = 0; i < boardCol11.length; i++)
 			if(boardCol11[i][player-1] != null)
 				row = i;
 		if(row > -1){
 			boardCol11[row][player-1].setPerm(true);
 			row = -1;
 		}
 		
 		for(int i = 0; i < boardCol12.length; i++)
 			if(boardCol12[i][player-1] != null)
 				row = i;
 		if(row > -1){
 			boardCol12[row][player-1].setPerm(true);
 			row = -1;
 		}
 		*/
 		
 	}
 	
 	/**
 	 * Checks to see if a player has won the game.
 	 * @param player the player's pieces to be checked
 	 * @return if the player has won
 	 */
 	public boolean winCheck(int player){
 		int conquered = 0;
 		for(int i = 0; i < board.length; i++){
 			if(board[i][board[i].length-1][player-1] != null)
 				if(board[i][board[i].length-1][player-1].getPerm())
 					conquered++;
 		}
 		if(conquered >= 3)
 			return true;
 		else
 			return false;
 		/*
 		if(boardCol2[boardCol2.length-1][player-1] != null)
 			if(boardCol2[boardCol2.length-1][player-1].getPerm())
 				conquered++;
 		
 		if(boardCol3[boardCol3.length-1][player-1] != null)
 			if(boardCol3[boardCol3.length-1][player-1].getPerm())
 				conquered++;
 		
 		if(boardCol4[boardCol4.length-1][player-1] != null)
 			if(boardCol4[boardCol4.length-1][player-1].getPerm())
 				conquered++;
 		
 		if(boardCol5[boardCol5.length-1][player-1] != null)
 			if(boardCol5[boardCol5.length-1][player-1].getPerm())
 				conquered++;
 		
 		if(boardCol6[boardCol6.length-1][player-1] != null)
 			if(boardCol6[boardCol6.length-1][player-1].getPerm())
 				conquered++;
 		
 		if(boardCol7[boardCol7.length-1][player-1] != null)
 			if(boardCol7[boardCol7.length-1][player-1].getPerm())
 				conquered++;
 		
 		if(boardCol8[boardCol8.length-1][player-1] != null)
 			if(boardCol8[boardCol8.length-1][player-1].getPerm())
 				conquered++;
 		
 		if(boardCol9[boardCol9.length-1][player-1] != null)
 			if(boardCol9[boardCol9.length-1][player-1].getPerm())
 				conquered++;
 		
 		if(boardCol10[boardCol10.length-1][player-1] != null)
 			if(boardCol10[boardCol10.length-1][player-1].getPerm())
 				conquered++;
 		
 		if(boardCol11[boardCol11.length-1][player-1] != null)
 			if(boardCol11[boardCol11.length-1][player-1].getPerm())
 				conquered++;
 		
 		if(boardCol12[boardCol12.length-1][player-1] != null)
 			if(boardCol12[boardCol12.length-1][player-1].getPerm())
 				conquered++;
 		*/
 
 	}
 	
 	/**
 	 * Checks to see if a given row has been conquered by either player.
 	 * @param columnNum the board column to be checked
 	 * @return if the column has been conquered
 	 */
 	public boolean checkConquered(int columnNum){
 		if(columnNum >= 2 && columnNum <= 12){
 			for(int i = 0; i < board.length; i++){
 				if(board[i][board[i].length-1][0] != null)
 					if(board[i][board[i].length-1][0].getPerm())
 						return true;
 				if(board[i][board[i].length-1][1] != null)
 					if(board[i][board[i].length-1][1].getPerm())
 						return true;
 			}
 		}
 		return false;
 		/*
 			switch (columnNum){
 			case 2:
 				if(boardCol2[boardCol2.length-1][0] != null)
 					if(boardCol2[boardCol2.length-1][0].getPerm())
 						return true;
 				if(boardCol2[boardCol2.length-1][1] != null)
 					if(boardCol2[boardCol2.length-1][1].getPerm())
 						return true;
 				break;
 				
 			case 3:
 				if(boardCol3[boardCol3.length-1][0] != null)
 					if(boardCol3[boardCol3.length-1][0].getPerm())
 						return true;
 				if(boardCol3[boardCol3.length-1][0] != null)
 					if(boardCol3[boardCol3.length-1][1].getPerm())
 						return true;
 				break;
 				
 			case 4:
 				if(boardCol4[boardCol4.length-1][0] != null)
 					if(boardCol4[boardCol4.length-1][0].getPerm())
 						return true;
 				if(boardCol4[boardCol4.length-1][0] != null)
 					if(boardCol4[boardCol4.length-1][1].getPerm())
 						return true;
 				break;
 				
 			case 5:
 				if(boardCol5[boardCol5.length-1][0] != null)
 					if(boardCol5[boardCol5.length-1][0].getPerm())
 						return true;
 				if(boardCol5[boardCol5.length-1][1] != null)
 					if(boardCol5[boardCol5.length-1][1].getPerm())
 						return true;
 				break;
 				
 			case 6:
 				if(boardCol6[boardCol6.length-1][0] != null)
 					if(boardCol6[boardCol6.length-1][0].getPerm())
 						return true;
 				if(boardCol6[boardCol6.length-1][1] != null)
 					if(boardCol6[boardCol6.length-1][1].getPerm())
 						return true;
 				break;
 				
 			case 7:
 				if(boardCol7[boardCol7.length-1][0] != null)
 					if(boardCol7[boardCol7.length-1][0].getPerm())
 						return true;
 				if(boardCol7[boardCol7.length-1][1] != null)
 					if(boardCol7[boardCol7.length-1][1].getPerm())
 						return true;
 				break;
 				
 			case 8:
 				if(boardCol8[boardCol8.length-1][0] != null)
 					if(boardCol8[boardCol8.length-1][0].getPerm())
 						return true;
 				if(boardCol8[boardCol8.length-1][1] != null)
 					if(boardCol8[boardCol8.length-1][1].getPerm())
 						return true;
 				break;
 				
 			case 9:
 				if(boardCol9[boardCol9.length-1][0] != null)
 					if(boardCol9[boardCol9.length-1][0].getPerm())
 						return true;
 				if(boardCol9[boardCol9.length-1][1] != null)
 					if(boardCol9[boardCol9.length-1][1].getPerm())
 						return true;
 				break;
 				
 			case 10:
 				if(boardCol10[boardCol10.length-1][0] != null)
 					if(boardCol10[boardCol10.length-1][0].getPerm())
 						return true;
 				if(boardCol10[boardCol10.length-1][1] != null)
 					if(boardCol10[boardCol10.length-1][1].getPerm())
 						return true;
 				break;
 				
 			case 11:
 				if(boardCol11[boardCol11.length-1][0] != null)
 					if(boardCol11[boardCol11.length-1][0].getPerm())
 						return true;
 				if(boardCol11[boardCol11.length-1][1] != null)
 					if(boardCol11[boardCol11.length-1][1].getPerm())
 						return true;
 				break;
 				
 			case 12:
 				if(boardCol12[boardCol12.length-1][0] != null)
 					if(boardCol12[boardCol12.length-1][0].getPerm())
 						return true;
 				if(boardCol12[boardCol12.length-1][1] != null)
 					if(boardCol12[boardCol12.length-1][1].getPerm())
 						return true;
 			}
 			*/
 		
 	}
 	
 	/**
 	 * Gets the number of temporary game pieces a player has on the board.
 	 * @return the number of temporary pieces in play by the player
 	 */
 	public int getCounter(){
 		return tempCounter;
 	}
 }
