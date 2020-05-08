 package ChessAPI;
 
 public class Pawn extends Piece {
 
 	public Pawn() {
 		super();
 	}
 	
 	public Pawn(Color c, Square s, Type t) {
 		super(c,s,t);
 	}
 
 	public String err="";
 
 	@Override
 	public boolean moveTo(Square destination) {
 		//System.out.println("Pawn");
 
 		if(this.validateMove(destination) == true){
 			System.out.println("Piece moved to "+destination.get_x()+","+destination.get_y());
 			//move piece to destination 
 			destination.setPiece(this);
 			//set piece.square to destination square
 			this.setSquare(destination);
 			return true;			
 		}else{
			System.out.println("the move is not completed due to existance of another piece in the destination square");
 			return false;
 		}
 		//logic for checking if the Pawn can move from current Location to this Destination
 		//i.e validate()
 		//return true and move else return false
 	}
 
 	private boolean validateMove(Square d){
 		Square s = null;
 		boolean decision = true;
 		s = this.getSquare();
 		if(d.get_x()>8 || d.get_y()>8 || d.get_x()<1 || d.get_y()<1){
 			decision = false;
 			err="Destination square is not a part of the game board, the requested moved is rejected.";
 			return decision;
 		}
 
 		if(s.get_x()==d.get_x() && s.get_y()==d.get_y()){
 			decision = false;
 			err="Source square and destination square can not be the same, the requested move is rejected.";
 			return decision;
 		}
 
 		if(!this.validateAgainstRule(s, d)){
 			decision=false;
			err="Not a valid move for a Biship, the requested move is rejected.";
 			return decision;
 		}
 		
 		if(this.isObstructed(s, d) == true){
 			decision=false;
 			err="Another piece exists in the path to the destination square, the requested move is rejected.";
 		}
 		
 		return decision;
 	}
 	
 	private boolean validateAgainstRule(Square s, Square d) {
 		boolean result = false; 
 		int diff_x = Math.abs(s.get_x() - d.get_x());
 		int diff_y = Math.abs(s.get_y() - d.get_y());
 
 		// Pawn can move by one squares in X direction
 		// Or it can move by two squares when it is starting from its initial position 
 		if(diff_x == 1 && diff_y == 0)
 			result = true;
 		else if (diff_x == 2 && (s.get_x() == 7 || s.get_x() == 2))
 			result = true;
 
 		return result;
 	}
 
 	private boolean isObstructed(Square s, Square d) {
 		Board board = new Board();
 		boolean result = false;
 		int s_x = s.get_x();
 		int s_y = s.get_y();
 		int d_x = d.get_x();
 		int d_y = d.get_y();
 		int diff_x = d_x - s_x;
 		int diff_y = d_y - s_y;
 	
 		while(s_x != d_x || s_y != d_y){
 
 			if (diff_x != 0)
 				s_x=s_x+diff_x/Math.abs(diff_x);
 			if (diff_y != 0)
 				s_y=s_y+diff_y/Math.abs(diff_y);
 			
 			if(board.getSquare(s_x, s_y).getPiece() != null){
 				result = true;
 				break;
 			}
 		}
 
 		return result;
 	}
 	
 
 	
 	//the class will also include other functions specific to Pawn Object
 }
