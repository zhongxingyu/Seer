 /*
 
 	Fanorona game
 
 	Constructor takes two arguments, the number of rows and columns
 
 
 
 	Possible input format:
 		[row][column] [row2][column2] [w/a/f]
 
 		Where [row][column] is the piece to move, [row2][column2] is the place to move it, and
 		[w/a/f] indicates whether this move is to be a withdrawal capture, approach capture, or capture-free move
 
 	If a capturing move is possible, a capturing move must be taken
 
 */
 
 
 
 class Fanorona implements Evaluatable{
 
 	public Board board;
 
 	long player1Time;
 	long player2Time;
 	
 	int numberOfPossibleMoves;
 	int lastEvaluated;
 	
 	int lastStateReturned;
 	
 	String lastStringReturned;
 	
 	static int cc;
 	
 	static String tempMove;
 
 	Fanorona(int row, int col){
 		board = new Board(row, col,Piece.Type.WHITE);
 		player1Time = 0;
 		player2Time = 0;
 		lastStateReturned = 0;
 	}
 	
 	public void setCC(int val){
 		cc = val;
 	}
 	
 	Fanorona(int row, int col, Board b, Piece.Type active){
 		board = new Board(row,col,b.array,active);
 		player1Time = 0;
 		player2Time = 0;
 	}
 	
 	private Fanorona getState(int n){
 		String move = numberOfMoves3(n);
		System.out.print("to next state: "+move+"  evaluates to: ");
 		int temp = 0;
 		Fanorona newState = copyGame();
 		for(int i = 0; i < move.length()/7; i++){
 			String move2 = move.substring(temp,temp+7);
 			temp+=8;
 			newState.move(getFirstRow(move2),getFirstColumn(move2),getSecondRow(move2),getSecondColumn(move2),getMoveType(move2));
 		}
 		return newState;
 	}	
 	
 	private int getFirstRow(String move){
 		//defined assuming standard 9x5 board
 		return move.charAt(0)-48;
 	}
 	
 	private int getFirstColumn(String move){
 		//defined assuming standard 9x5 board
 		
 		return move.charAt(1)-48;
 	}
 	
 	private int getSecondRow(String move){
 		//defined assuming standard 9x5 board
 		
 		return move.charAt(3)-48;
 	}
 	
 	private int getSecondColumn(String move){
 		//defined assuming standard 9x5 board
 		
 		return move.charAt(4)-48;
 	}
 	
 	private char getMoveType(String move){
 		//defined assuming standard 9x5 board
 		
 		return move.charAt(6);
 	}
 	
 	private boolean moveHasSuccessiveCaptures(String move){
 		Fanorona moveTest = copyGame();
 		Piece.Type original = this.activePlayer();
 		moveTest.move(getFirstRow(move),getFirstColumn(move),getSecondRow(move),getSecondColumn(move),getMoveType(move));
 		moveTest.board.activePlayer = original;
 		if(original == Piece.Type.WHITE && moveTest.board.whiteMoves.endsWith("\n")){
 			moveTest.board.whiteMoves = moveTest.board.whiteMoves.substring(0,moveTest.board.whiteMoves.length()-1);
 		}
 		else if(original == Piece.Type.BLACK && moveTest.board.blackMoves.endsWith("\n")){ 
 			moveTest.board.blackMoves = moveTest.board.blackMoves.substring(0,moveTest.board.blackMoves.length()-1);
 		}
 		if(moveTest.board.capturingMoveAvailable(moveTest.board.array[getSecondRow(move)][getSecondColumn(move)])){
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 	
 	
 	public String numberOfMoves3(int row, int column, boolean recursion, int goal, String tempString){
 		int count = 0;
 		if(this.board.capturingMoveAvailable(this.board.array[row][column])){
 			String possibleMoves = this.board.PossibleCapturingMovesWithDirection(this.board.array[row][column]);
 			//System.out.print("possible moves: "+possibleMoves);
 			//System.out.println("    length: "+possibleMoves.length());
 			int temp = 0;
 			for(int k = 0; k < possibleMoves.length()/8; k++){
 				String move = possibleMoves.substring(temp+1,temp+8);
 				if(moveHasSuccessiveCaptures(move)){
 					if(tempString.length() == 0){
 						tempString += move;
 					}
 					else{
 						tempString += ">"+move;
 					}
 					Fanorona newGame = copyGame();
 					int tempRow = getSecondRow(move);	//hold the destination position so i can call recursively
 					int tempCol = getSecondColumn(move);
 					newGame.move(getFirstRow(move), getFirstColumn(move), getSecondRow(move), getSecondColumn(move), getMoveType(move));
 					String tempCount = newGame.numberOfMoves3(tempRow, tempCol, true,goal,tempString);
 					tempString = tempCount;
 					if(cc == goal) return tempString;
 				}
 				else{
 					count++;
 					cc++;
 					if(cc == goal){
 						if(tempString.length() == 0){
 							return move;
 						}
 						else{
 							return tempString + ">"+move;
 						}
 					}
 				}
 				temp+=8;
 				/*TODO make this code more general. now it just does the same thing as count += length/8 */
 			}
 		}
 		else if(!recursion){
 			String possibleMoves = this.board.possibleMovesWithDirection(this.board.array[row][column]);
 			for(int k = 0; k < possibleMoves.length()/8; k++){
 				//also generalize this code
 				count++;
 				cc++;
 			}
 		}
 		return "";
 	}
 	
 	public String numberOfMoves3(int goal){
 		cc = 0;
 		int count = 0;
 		String eMove = "";
 		for(int i = 0; i < board.rows; i++){
 			for(int j = 0; j < board.columns; j++){
 				eMove = "";
 				tempMove = "";
 				if(board.capturingMoveAvailable(board.array[i][j])){
 					//count += numberOfMoves3(i,j,false,goal);
 					eMove = numberOfMoves3(i,j,false,goal,eMove);
 					if(cc == goal) return eMove;
 				}
 				else if(!capturingMoveAvailable()){
 					String possibleMoves = board.possibleMovesWithDirection(board.array[i][j]);
 					//System.out.println("Possible moves: "+possibleMoves);
 					int temp = 0;
 					for(int k = 0; k < possibleMoves.length()/8; k++){
 						//also generalize this code
 						String move = possibleMoves.substring(temp+1,temp+8);
 						temp+=8;
 						count++;
 						cc++;
 						if(cc == goal) return move;
 					}
 				}
 			}
 		}
 		return tempMove;
 	}
 	
 	public int numberOfMoves(int row, int column, boolean recursion){
 		int count = 0;
 		if(this.board.capturingMoveAvailable(this.board.array[row][column])){
 			String possibleMoves = this.board.PossibleCapturingMovesWithDirection(this.board.array[row][column]);
 			//System.out.print("possible moves: "+possibleMoves);
 			//System.out.println("    length: "+possibleMoves.length());
 			int temp = 0;
 			for(int k = 0; k < possibleMoves.length()/8; k++){
 				String move = possibleMoves.substring(temp+1,temp+8);
 				if(moveHasSuccessiveCaptures(move)){
 					Fanorona newGame = copyGame();
 					int tempRow = getSecondRow(move);	//hold the destination position so i can call recursively
 					int tempCol = getSecondColumn(move);
 					newGame.move(getFirstRow(move), getFirstColumn(move), getSecondRow(move), getSecondColumn(move), getMoveType(move));
 					count += newGame.numberOfMoves(tempRow, tempCol, true);
 				}
 				else{
 					count++;
 				}
 				temp+=8;
 				/*TODO make this code more general. now it just does the same thing as count += length/8 */
 			}
 		}
 		else if(!recursion){
 			String possibleMoves = this.board.possibleMovesWithDirection(this.board.array[row][column]);
 			//System.out.println("Possible moves: "+possibleMoves);
 			for(int k = 0; k < possibleMoves.length()/8; k++){
 				//also generalize this code
 				count++;
 			}
 		}
 		return count;
 	}
 	
 	public int numberOfMoves(){
 		int count = 0;
 		for(int i = 0; i < board.rows; i++){
 			for(int j = 0; j < board.columns; j++){
 				if(board.capturingMoveAvailable(board.array[i][j])){
 					count += numberOfMoves(i,j,false);
 				}
 				else if(!capturingMoveAvailable()){
 					String possibleMoves = board.possibleMovesWithDirection(board.array[i][j]);
 					//System.out.println("Possible moves: "+possibleMoves);
 					for(int k = 0; k < possibleMoves.length()/8; k++){
 						//also generalize this code
 						count++;
 					}
 				}
 			}
 		}
 		return count;
 	}
 	
 	//Calculates the total number of possible moves (state changes)
 	public void recalculateNumberOfMoves(){
 		numberOfPossibleMoves = numberOfMoves();
 		lastEvaluated = 0;
 	}
 
 	public void printTime(){
 		System.out.println("White time: "+player1Time/1000.0);
 		System.out.println("Black time: "+player2Time/1000.0+"\n");
 	}
 
 	void prettyprint(){
 		board.prettyprint();
 	}
 	
 	public Fanorona copyGame(){
 		Fanorona f = new Fanorona(board.rows, board.columns, board.copyBoard(), board.activePlayer);
 		f.board.whiteMoves = this.board.whiteMoves;
 		f.board.blackMoves = this.board.blackMoves;
 		return f;
 	}
 	
 	public Board copyBoard(){
 		return board.copyBoard();
 	}
 
 	void printScore(){
 		board.printScore(Piece.Type.WHITE);
 		board.printScore(Piece.Type.BLACK);
 		System.out.println();
 	}
 
 	boolean isPossibleCapturingMove(int row1, int col1, int row2, int col2, char type){
 		return board.isPossibleCapturingMove(row1, col1,row2,col2, type);
 	}
 
 	boolean capturingMoveAvailable(){
 
 		return board.capturingMoveAvailable();
 	}
 
 	//returns true if a successive capture is possible, false otherwise
 	public boolean move(int row1, int col1, int row2, int col2, char type){
 
 		//boolean valid = board.isPossibleMove(row1,col1,row2, col2, type);
 		boolean valid = false;
 		//boolean captAvail = false;
 		if(capturingMoveAvailable() && board.isPossibleCapturingMove(row1,col1,row2,col2,type)){
 			valid = true;
 		}
 		else if(!capturingMoveAvailable() && board.isPossibleMove(row1,col1,row2,col2,type)){
 			valid = true;
 		}
 
 		//check to see if move is valid
 		if(valid){
 			//System.out.println("valid");
 			Piece.Type temp = board.activePlayer;
 			board.movePiece(row1,col1,row2,col2,type);
 			lastStateReturned = 0;
 			board.activePlayer = temp;
 
 			if(board.activePlayer == Piece.Type.WHITE){
 				board.whiteMoves += ""+row1+col1+">"+row2+col2;
 			}
 			else{
 				board.blackMoves += ""+row1+col1+">"+row2+col2;
 			}
 
 			//board.prettyprint();
 			
 			if(board.capturingMoveAvailable(board.array[row2][col2]) && type != 'f'){
 				//System.out.println("Successive capture available");
 				return true;
 			}
 
 			if(board.activePlayer == Piece.Type.WHITE){
 				board.whiteMoves += "\n";
 			}
 			else{
 				board.blackMoves += "\n";
 			}
 			
 			board.latestDirectionMoved = "";
 
 			if(temp == Piece.Type.WHITE){
 				board.activePlayer = Piece.Type.BLACK;
 			}
 			else{
 				board.activePlayer = Piece.Type.WHITE;
 			}
 
 			return false;
 		}
 		else{
 			System.out.println("Invalid move entered");
 		}
 		return false;
 	}
 
 	public String getRandomMove(){
 
 		String ret = "";
 		if(capturingMoveAvailable()){
 			for(int i = 0; i < board.rows; i++){
 				for(int j = 0; j < board.columns; j++){
 					String move = board.PossibleCapturingMoves(board.array[i][j]);
 					Piece p = board.array[i][j];
 					if(move.length() > 0){
 						if(isPossibleCapturingMove(p.row, p.column, move.charAt(1)-48, move.charAt(2)-48, 'a')){
 							ret = ""+p.row+p.column+" "+(move.charAt(1))+""+(move.charAt(2))+" a";
 							break;
 						}
 						else if(isPossibleCapturingMove(p.row, p.column, move.charAt(1)-48, move.charAt(2)-48, 'w')){
 							ret = ""+p.row+p.column+" "+(move.charAt(1))+""+(move.charAt(2)) + " w";
 							break;
 						}
 					}
 				}
 			}
 			return ret;
 		}
 		else{
 			for(int i = 0; i < board.rows; i++){
 				for(int j = 0; j < board.columns; j++){
 					String move = board.possibleMoves(board.array[i][j]);
 					Piece p = board.array[i][j];
 					if(move.length() > 0){
 						if(board.isPossibleMove(p.row, p.column, move.charAt(1)-48, move.charAt(2)-48, 'f')){
 							ret = ""+p.row+p.column+" "+(move.charAt(1))+""+(move.charAt(2))+" f";
 							break;
 						}
 					}
 				}
 			}
 			return ret;
 		}
 	}
 
 	Piece.Type activePlayer(){
 		return board.activePlayer;
 	}
 	
 	@Override
 	public int evaluate() {
 		return board.numberRemaining(Piece.Type.WHITE) -
 				board.numberRemaining(Piece.Type.BLACK);
 	}
 
 	@Override
 	public Evaluatable getNextState() {
 		// TODO Auto-generated method stub
 		return getState(++lastStateReturned);
 		//return null;
 	}
 }
