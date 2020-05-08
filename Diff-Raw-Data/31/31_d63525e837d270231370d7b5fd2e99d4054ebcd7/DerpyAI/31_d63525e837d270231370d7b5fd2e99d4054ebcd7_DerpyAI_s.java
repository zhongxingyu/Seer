 package DerpyAI;
 
 import java.util.ArrayList;
 import sharedfiles.*;
 
 public class DerpyAI {
 	private Boolean myColor; //black is false, white is true. 
 	private ArrayList<Board> boardStore; //The current, and all previous boards
 	private ArrayList<Piece> takenPieces; //The pieces we took
 	private ArrayList<Piece> ourPieces; //Our Array of Pieces
 	private Board currentBoard; //currentBoard is the current chess board
 
 	public DerpyAI(Boolean b, Board c){
 		myColor = b; 
 		boardStore = new ArrayList<Board>();
 		takenPieces = new ArrayList<Piece>();
 		ourPieces = new ArrayList<Piece>();
 		currentBoard = c; 
 	}
 	
 	public ArrayList<Piece> getOurPieces(){
 		return ourPieces; 
 	}
 	
	public void FindOurPieces(){ // Creates an array of our pieces
 	Piece[][] boardState = currentBoard.getBoardArray(); 
 	for(int i=0;i<8;i++){
 			for(int a=0;a<8;a++){ 
 				if (boardState[i][a].getColor() == myColor) ourPieces.add(boardState[i][a]); 
 			}
 		}
 	}
 
 	//Board State Checks
 
 	public boolean inCheck(){
 		boolean b = false;
 		
 		return b;
 	} 
 
 
 	public boolean isPieceOurs(Piece p){
 		if (this.myColor == p.getColor() && !(p instanceof Blank)){
 			return true;}
 		else return false;}
 
 	public ArrayList<Piece> enemyThreats(Board b){
 		ArrayList<Piece> ourThreatenedPieces = new ArrayList<Piece>();
 		for(int i=0; i<8; i++){
 			for(int j=0; j<=8; j++){
				//if(this.isPieceOurs([piece at (i,j)]){
				//if(this.pieceIsThreatened([piece at (i,j)])){
				//ourThreatenedPieces.add([piece at (i,j)])
 			}
 		}
 
 		return ourThreatenedPieces;
 
 	}
 
 
 	public ArrayList<Piece> ourThreats(Board b){
 		ArrayList<Piece> theirThreatenedPieces = new ArrayList<Piece>();
 		for(int i=0; i<8; i++){
 			for(int j=0; j<=8; j++){
				//if(!(this.isPieceOurs(b[i][j]))){
				//if(this.pieceIsThreatened(b[i][j]))){
				//theirThreatenedPieces.add(b[i][j])))
 			}
 		}
 

 		return theirThreatenedPieces;
 	}
 
 
 	public boolean pieceIsThreatened(Piece p) {
 		boolean b = false; 
 		return b; 
 	}
 
 	//Moves
 	public Board getOutOfCheck(Board b){
 		return b;
 	}
 
 	public Board makeMove(Board b){
 		if (this.inCheck() == false){
 			this.getOutOfCheck(b);}
 		else {
 
 
 		}
 		return b;
 	}
 
 }
 
 
