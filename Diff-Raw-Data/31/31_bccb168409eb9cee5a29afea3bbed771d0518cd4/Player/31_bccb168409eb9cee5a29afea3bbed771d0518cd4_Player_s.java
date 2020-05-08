 package ChessAPI;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import ChessAPI.Piece.Color;
 import ChessAPI.Piece.Type;
 
 public class Player {
 	public enum PlayerStatus{
 		won,lost,draw,progress
 	}
 
 	Pawn pawn[];
 	Knight knight[];
 	Bishop bishop[];
 	Rook rook[];
 	King king;
 	Queen queen;
 	private Color color;
 	int noOfAlive, noOfDead, noOfMoves;
 	boolean myTurn;
 	public Square originalSquare = null;
 	private PlayerStatus status;
 
 	boolean isCastling;
 
 	public Player(Color c)
 	{
 		status = PlayerStatus.progress;
 		color = c;
 		noOfAlive = 16;
 		noOfDead = 0;
 		pawn = new Pawn[9];
 		knight = new Knight[3];
 		bishop = new Bishop[3];
 		rook = new Rook[3];
 		king = new King(c,null,Type.King);
 		queen = new Queen(c,null,Type.Queen);
 
 		for (int i = 1; i <= 8; i++){
 			pawn[i] = new Pawn(c,null,Type.Pawn);
 		}
 
 		for (int i = 1; i <= 2; i++)
 		{
 			knight[i] = new Knight(c,null,Type.Knight);
 			bishop[i] = new Bishop(c,null,Type.Bishop);
 			rook[i] = new Rook(c,null,Type.Rook);
 		}
 
 		noOfMoves = 0;
 		myTurn = false;
 		isCastling = false;
 	}
 
 	//returns the player status
 	//when we see that a player is won or lost then we set this variable to corresponding value
 	//status of one player won indicates other has lost and viceversa
 	//status of one player draw indicates game is drawn, because it wnt be posible to update the status of ither player.
 	public PlayerStatus getStatus() {
 		return this.status;
 	}
 
 	public void setStatus(PlayerStatus status) {
 		this.status = status;
 	}
 
 	public void setColor(Color color) {
 		this.color = color;
 	}
 
 	public Color getColor() {
 		return color;
 	}
 
 	public int getNoOfAlive() {
 		return noOfAlive;
 	}
 
 	public int getNoOfDead() {
 		return noOfDead;
 	}
 
 	public boolean moveTo(Square s, Square d) {
		System.out.println(" Move dest = " + d.get_x() + ", " + d.get_y());
 		if (s.getPiece() == null) {
			System.out.println("piece object not found at the source location specified " + s.get_x() + ", " + s.get_y());
			Throwable t = new Throwable();
			t.printStackTrace();
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			try {
				String input = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 			return false;
 		} else if (s.getPiece().getColor() != this.getColor())
 		{
 			// This piece does not belong to you
 			System.out.println(" This piece does not belong to you ");
 			return false;
 		}
 		else {
 			boolean result = true;
 
 			if (d.getPiece() != null && d.getPiece().getColor() == s.getPiece().getColor())
 			{
/*
				System.out.println(" Another piece of your color found at the destination location specified " + s.get_x() + ", " + s.get_y() + " d = " + d.get_x() + ", " + d.get_y());
				String input = null;
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				try {
					input = br.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	*/
 				return false;
 			} else {
 				result = true;
 				Piece temp_dead = null;
 				//System.out.println(" result = " + result);
 				if (result == true && s.getPiece().getType() != Type.Pawn)
 				{
 					temp_dead = null;
 					temp_dead = d.getPiece();
 					if (d.getPiece() != null)
 					{
 						d.getPiece().setIsDead(true);
 						//	System.out.println(d.getPiece().getColor() + " Player's " + d.getPiece().getType() + " at (" + d.get_x() + ", " + d.get_y() + ") is DEAD !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
 						d.setPiece(null);
 					}
 					updateDeadAlive();
 				}
 
 				result = s.getPiece().moveTo(d);
 				if (result == false)
 				{
 					if (temp_dead != null)
 					{
						System.out.println(" temp_dead is not NULL ");
 						d.setPiece(temp_dead);
 						d.getPiece().setIsDead(false);
 						temp_dead.setIsDead(false);
 					}
 				}
 			}
 
 			//if a valid move is completed remove piece from souce
 			if(result) {
 				s.setPiece(null);
 				//System.out.println(" move completed from : " + s.get_x() + ", " + s.get_y() + ") to (" + d.get_x() + ", " + d.get_y() + ") ");
 			}
 			myTurn = false;
 
 			return result;
 		}
 	}
 
 	public boolean killPiece(Square s) {
 		boolean result = true;
 		if (s.getPiece() == null) {
 			System.out.println("piece object not found at the source location specified");
 			return false;
 		} else {
 				result = true;
 				//System.out.println(" result = " + result);
 					s.getPiece().setIsDead(true);
 					//System.out.println(d.getPiece().getColor() + " Player's " + d.getPiece().getType() + " at (" + d.get_x() + ", " + d.get_y() + ") is DEAD !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
 					s.setPiece(null);
 			}
 		return result;
 	}
 
 	public boolean moveTonoCheck(Square s, Square d) {
 			boolean result = true;
 			//d.getPiece().setIsDead(true);
 			//System.out.println(d.getPiece().getColor() + " Player's " + d.getPiece().getType() + " at (" + d.get_x() + ", " + d.get_y() + ") is DEAD !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
 			//d.setPiece(null);
 
 			result = s.getPiece().moveTonoCheck(d);
 			s.setPiece(null);
 			updateDeadAlive();
 			return result;
 	}
 
 	//returns true if the player's King is in Check
 	//Logic: if a valid move of any of the opponents piece can replace the King
 	//than it is in check
 	public boolean isInCheck() {
 		boolean result = false;
 		Square currentBoard[][] = new Square[10][10];
 
 		for (int i = 1; i <= 8; i++)
 			for (int j = 1; j <=8; j++)
 			{
 				currentBoard[i][j] = Board.getInstance().getSquare(i, j);
 			}
 
 		Square kingPosition = this.king.getSquare(); //get Kings current position
 
 		if (kingPosition != null) {
 
 			for (int i=1; i<Board.ROWS ; i++){
 				for (int j=1; j<Board.COLS ; j++) {
 					Square currentSquare = currentBoard[i][j];
 					if (currentSquare.getPiece() != null) {
 						if (currentSquare.getPiece().getColor() != this.color) {
 
 							//if I am here, this means that the Board's square[i][j] has opponents piece
 							//now I need to check if this Piece's valid move can capture my King or not
 							//If yes, I am in check else I am not
 							if (currentSquare.getPiece().validateMove(currentSquare, kingPosition)) {
 								//the piece on square[i][j] can Kill my King
 								result = true;
 								return result;
 							}
 						}
 					}					
 				}//end inner for
 			}//end outer for
 
 		}
 		return result;
 	}//end isInCheck()
 
 	// 	For checking if king will be land up in check if moved to this new (x, y) Square
 	public boolean isInCheck(int x, int y) {
 		boolean result = false;
 		Square currentBoard[][] = new Square[10][10];
 
 		for (int i = 1; i <= 8; i++)
 			for (int j = 1; j <=8; j++)
 			{
 				currentBoard[i][j] = Board.getInstance().getSquare(i, j);
 			}
 
 		Square kingPosition = new Square(this.color, x, y, this.king);
 
 		if (kingPosition != null) {
 
 			for (int i=1; i<Board.ROWS ; i++){
 				for (int j=1; j<Board.COLS ; j++) {
 					Square currentSquare = currentBoard[i][j];
 					if (currentSquare.getPiece() != null) {
 						if (currentSquare.getPiece().getColor() != this.color) {
 
 							//if I am here, this means that the Board's square[i][j] has opponents piece
 							//now I need to check if this Piece's valid move can capture my King or not
 							//If yes, I am in check else I am not
 							if (currentSquare.getPiece().validateMove(currentSquare, kingPosition)) {
 								//the piece on square[i][j] can Kill my King
 								result = true;
 								return result;
 							}
 						}
 					}					
 				}//end inner for
 			}//end outer for
 
 		}
 		return result;
 	}//end isInCheck()
 
 	//listOfMoves contains a set of moves and corresponding heuristics
 	//we first find the best value in this list (most +ve for WHITE and most -ve for BLACK)
 	//if there are more than one Moves with heurisic value = best heuristic than we choose one randomly out of them
 	private Move refineList(List<Move> listOfMoves) {
 		float bestHeuristic = listOfMoves.get(0).getHeuristicValue();
 
 		//Lets first find the best heuristic value in this list of Moves
 		for (Move temp : listOfMoves) {
 			if(this.color == Color.white) {
 				//find more positive heuristic because its a white player
 				if(temp.getHeuristicValue() > bestHeuristic){
 					bestHeuristic = temp.getHeuristicValue();
 				}
 
 			}else {
 				//find more negative heuristic because its a black player
 				if(temp.getHeuristicValue() < bestHeuristic){
 					bestHeuristic = temp.getHeuristicValue();
 				}
 			}
 		}
 
 		//now lets find all the moves in listOfMoves with heuristic = bestHeuristic
 		//there can be more than one such moves. If yes then return randomly else return the one.
 		List<Move> refinedList = new ArrayList<Move>();
 		for (Move temp : listOfMoves) {
 			if(temp.getHeuristicValue() == bestHeuristic) {
 				refinedList.add(temp);
 			}
 		}
 
 		if(refinedList.size() == 1) {
 			//there is only one move with best heuristic
 			return refinedList.get(0);
 
 		} else {
 			//there are more than one moves with best heuristic, return randomly from the best
 			int listSize = refinedList.size();
 			Random r = new Random();
 			int result = r.nextInt(listSize);
 
 			return refinedList.get(result);
 		}
 
 	}
 
 
 	//Randomly select a Piece and return a random Square where it can move legally, out of all the possible legal moves
 	//If this method returns null, than there is no valid move possible for the randomly selected Piece
 	//Suggestion: Re-invoke this method till it returns a non-null value
 	public Move selectBestMove() {
 		int i;
 		// If King is InCheck condition Move the King(priority) else we can move anything
 		if (this.isInCheck() == true)
 		{
 			if( !this.king.validMoves().isEmpty() ) {
 				//A valid Move exists for the King, return the best of it
 				//here we also need to check if King is in check even after applying this bestmove
 
 				//this is a list of all valid moves for King with its heuristic
 				List<Move> validKingMoves = this.king.validMoves();
 				Move bestMove = null;
 
 				if(validKingMoves.size() == 1) {
 					bestMove = validKingMoves.get(0);
 				} else {
 					bestMove = this.refineList(validKingMoves);
 				}
 
 				return bestMove;
 			} else {
 				//King is in check and there is no valid move that exists for King
 				//GAME OVER
 				this.setStatus(PlayerStatus.lost);
 				return null;
 			}	
 		}
 
 		// Check if we can do Castling
 		/*
 		1. The king has not previously moved.
 		2. The chosen rook has not previously moved.
 		3. There are no pieces between the king and the chosen rook.
 		4. The king is not currently in check.
 		5. The king does not pass through a square that is under attack by enemy pieces.[2]
 		6. The king does not end up in check (true of any legal move).
 		7. The king and the chosen rook are on the first rank of the player (rank 1 for White, rank 8 for Black, in algebraic notation).[3]
 		*/
 
 		// System.out.println(" King " + this.king.isKingMoved);
 		// 1.1, 2.1 The king has not previously moved.
 		if (this.king.isKingMoved == false)
 		{
 			boolean notObstructed = true, success = true;
 			i = 0; 
 			int diff_y = 0, x_current = 0;
 
 			// 1.2. The chosen rook has not previously moved.
 			if (this.rook[1].isRookMoved == false)
 			{
 				diff_y = this.king.getSquare().get_y() - this.rook[1].getSquare().get_y();
 				x_current = this.rook[1].getSquare().get_x();
 
 				if (diff_y > 0)
 				{
 					for (i = this.rook[1].getSquare().get_y() + 1; i < this.king.getSquare().get_y(); i++)
 					{
 						if (Board.getInstance().getSquare(x_current, i).getPiece() != null)
 						{
 							notObstructed = false;
 							success = false;
 							break;
 						}
 					}
 				}
 
 				// 1.3. There are no pieces between the king and the chosen rook.
 				if (notObstructed == true)
 				{
 					// 1.4. The king is not currently in check.
 					if (isInCheck() == false)
 					{
 						x_current = this.rook[1].getSquare().get_x();
 						// 1.5. The king does not pass through a square that is under attack by enemy pieces.
 						for (i = this.rook[1].getSquare().get_y() + 1; i < this.king.getSquare().get_y(); i++)
 						{
 							// 1.6. The king does not end up in check (true of any legal move).
 							if (isInCheck(x_current, i) == true)
 							{								
 								success = false;
 								break;
 							}
 						}
 					}
 				}
 
 				// 1.7. The king and the chosen rook are on the first rank of the player
 				// (That means on original rows same row as that of their initial rows)
 				// isNotMoved flag should serve the purpose
 				if (success == true)
 				{
 					Rook currentRook = this.rook[1];
 					Square new_king = new Square(this.color, currentRook.getSquare().get_x(), currentRook.getSquare().get_y() + 2, null);
 					Square new_rook = new Square(this.color, currentRook.getSquare().get_x(), currentRook.getSquare().get_y() + 3, null);
 
 					this.moveTonoCheck(this.king.getSquare(), new_king);
 					Move bestMove = new Move(new_rook, 100, currentRook.getSquare());
 					bestMove.setSource(currentRook.getSquare());
 
 					isCastling = true;
 					return bestMove;
 				}
 
 				if (success == false)
 				{
 					notObstructed = true; success = true;
 					i = 0; 
 					diff_y = 0; x_current = 0;
 
 					// 2.2. The chosen rook has not previously moved.
 					if (this.rook[2].isRookMoved == false)
 					{
 						diff_y = this.rook[2].getSquare().get_y() - this.king.getSquare().get_y();
 						x_current = this.rook[2].getSquare().get_x();
 
 						if (diff_y > 0)
 						{
 							for (i = this.king.getSquare().get_y() + 1; i < this.rook[2].getSquare().get_y(); i++)
 							{
 								if (Board.getInstance().getSquare(x_current, i).getPiece() != null)
 								{
 									notObstructed = false;
 									success = false;
 									break;
 								}
 							}
 						}
 						// 2.3. There are no pieces between the king and the chosen rook.
 						if (notObstructed == true)
 						{
 							// 2.4. The king is not currently in check.
 							if (isInCheck() == false)
 							{
 								x_current = this.rook[2].getSquare().get_x();
 								// 2.5. The king does not pass through a square that is under attack by enemy pieces.
 								for (i = this.king.getSquare().get_y(); i < this.rook[2].getSquare().get_y(); i++)
 								{
 									// 2.6. The king does not end up in check (true of any legal move).
 									if (isInCheck(x_current, i) == true)
 									{
 										success = false;
 										break;
 									}
 								}
 							}
 						}
 
 						// 2.7. The king and the chosen rook are on the first rank of the player
 						// (That means on original rows same row as that of their initial rows)
 						// isNotMoved flag should serve the purpose
 						if (success == true)
 						{
 							Rook currentRook = this.rook[2];
 							Square new_king = new Square(this.color, currentRook.getSquare().get_x(), currentRook.getSquare().get_y() - 1, null);
 							Square new_rook = new Square(this.color, currentRook.getSquare().get_x(), currentRook.getSquare().get_y() - 2, null);
 
 							this.moveTonoCheck(this.king.getSquare(), new_king);
 							Move bestMove = new Move(new_rook, 100, currentRook.getSquare());
 							bestMove.setSource(currentRook.getSquare());
 
 							isCastling = true;
 							return bestMove;
 						}
 					}
 				}
 			}
 		} else {
 			// If King is moved no Castling can be done
 		}
 
 		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 
 		//If King is not in check, we can move other pieces
 		//This list will store best moves obtained for each possible alive piece from calling selectBestMove() for that piece
 		List<Move> setOfMoves = new ArrayList<Move>();
 
 		//find the best possible move for each Pawn and add it to setOfMoves
 		for (i=1; i<=8; i++) {
 			if ( this.pawn[i].isPieceDead() == false) {
 				//Piece is alive
				System.out.println(" i = " + i + " (" + this.pawn[i].getSquare().get_x() + ", " + this.pawn[i].getSquare().get_y() + ") ");
 				if(!this.pawn[i].validMoves().isEmpty()) {
 					//A valid Move exists for this piece, so add it to the list of setOfMoves
 					for( Move temp : this.pawn[i].validMoves()) {
 						setOfMoves.add(temp);
 					}
 				}	
 			}
 		}
 
 		//find the best possible move for each Rook and add it to setOfMoves
 		for (i=1; i<=2; i++) {
 			if ( this.rook[i].isPieceDead() == false) {
 				//Rook is alive
 				if(!this.rook[i].validMoves().isEmpty()) {
 					//A valid Move exists for this Rook, so add it to the list of setOfMoves
 					for( Move temp : this.rook[i].validMoves()) {
 						setOfMoves.add(temp);
 					}
 				}
 			}
 		}
 
 		//find the best possible move for each Bishop and add it to setOfMoves
 		for (i=1; i<=2; i++) {
 			if ( this.bishop[i].isPieceDead() == false) {
 				//Bishop is alive
 				if(!this.bishop[i].validMoves().isEmpty()) {
 					//A valid Move exists for this Bishop, so add it to the list of setOfMoves
 					for( Move temp : this.bishop[i].validMoves()) {
 						setOfMoves.add(temp);
 					}
 				}
 			}
 		}
 
 		//find the best possible move for each Knight and add it to setOfMoves
 		for (i=1; i<=2; i++) {
 			if ( this.knight[i].isPieceDead() == false) {
 				//Knight is alive
 				if(!this.knight[i].validMoves().isEmpty()) {
 					//A valid Move exists for this Knight, so add it to the list of setOfMoves
 					for( Move temp : this.knight[i].validMoves()) {
 						setOfMoves.add(temp);
 					}
 				}
 			}
 		}
 
 		//find the best possible move for a Queen and add it to setOfMoves
 		if ( this.queen.isPieceDead() == false) {
 			//Queen is alive
 			if(!this.queen.validMoves().isEmpty()) {
 				//A valid Move exists for this Queen, so add it to the list of setOfMoves
 				for( Move temp : this.queen.validMoves()) {
 					setOfMoves.add(temp);
 				}
 			}
 		}
 
 		//find the best possible move for a King and add it to setOfMoves
 		if ( this.king.isPieceDead() == false) {
 			//Queen is alive
 			if(!this.king.validMoves().isEmpty()) {
 				//A valid Move exists for this Queen, so add it to the list of setOfMoves
 				for( Move temp : this.king.validMoves()) {
 					setOfMoves.add(temp);
 				}
 			}
 		}
 
 		//At this stage we have a list of all valid moves with their heuristic
 		//Now we need to find the best of this list, so that we can find the best Heuristic move out of all
 
 		if(!setOfMoves.isEmpty()) {
 			if(setOfMoves.size() == 1) {
 				return setOfMoves.get(0);
 			} else {
 				return this.refineList(setOfMoves);
 			}
 
 		}else {
 			//There is no possible move for this player
 			//STALEMATE
 			this.setStatus(PlayerStatus.draw);
 			return null;
 		}
 
 	}//end randomMove()
 
 	public int getNoMoves()
 	{
 		return noOfMoves;
 	}
 
 	public void incMoves()
 	{
 		noOfMoves++;
 	}
 
 	public void updateDeadAlive()
 	{
 		boolean debug = false;
 
 		noOfDead = 0;
 		if (king.isPieceDead() == true)
 		{
 			if (debug == true)
 				System.out.println(" King is DEAD ");
 			noOfDead++;
 			//System.exit(0);
 		}
 
 		if (queen.isPieceDead() == true)
 		{
 			if (debug == true)
 				System.out.println(" Queen is DEAD ");
 			noOfDead++;
 		}
 
 		for (int i = 1; i <= 8; i++){
 			if (pawn[i].isPieceDead() == true)
 			{
 				if (debug == true)
 					System.out.println(" Pawn " + i + " is DEAD ");
 				noOfDead++;
 			}
 		}
 
 		for (int i = 1; i <= 2; i++)
 		{
 			if (knight[i].isPieceDead() == true)
 			{
 				if (debug == true)
 					System.out.println(" Knight " + i + " is DEAD ");
 				noOfDead++;
 			}
 			if (bishop[i].isPieceDead() == true)
 			{
 				if (debug == true)
 					System.out.println(" Bishop " + i + " is DEAD ");
 				noOfDead++;
 			}
 			if (rook[i].isPieceDead() == true)
 			{
 				if (debug == true)
 					System.out.println(" Rook " + i + " is DEAD ");
 				noOfDead++;
 			}
 		}
 
 		noOfAlive = 16 - noOfDead;
 		//System.out.println(" noOfDead  " + noOfDead);
 	}
 
 	public boolean getTurn()
 	{
 		return myTurn;
 	}
 
 	public boolean isGameOver()
 	{
 		boolean result = false;
 
 		if (this.king.isPieceDead() == true)
 		{
 			System.out.println(" The Game is Over.. \n Player (" + this.getColor() + ") is lost.. ");
 			result = true;
 		}
 		return result;
 	}
 	//other functions needed by the Player class....
 }
