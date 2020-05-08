 package quoridor;
 
 public class MovePawnImpl extends AbstractMove implements MovePawn {
 
 	Square destination;
 	Square start;
 
 	
 	public MovePawnImpl(int col, int row, Player owner, Board setting) {
 		super(owner, setting);
 		destination = new SquareImpl(col, row);
 		start = setting.getPawn(owner, setting).getSquare();
     }
 	
 	@Override
 	public Square getDestination() {
 		return destination;
 	}
 
 	@Override
 	public boolean isValid() {
 		
 		Square temp;
 		Square temp2;
 		boolean valid = true;
 		int destination_col = destination.getCol();
 		int destination_row = destination.getRow();
 		int start_col = start.getCol();
 		int start_row = start.getRow();
 		
 		Square between = isTwoAway(start, destination);
 		
 		//check whether square is on the board
 		if(destination_col > 8 || destination_col < 0 ||
 				   destination_row > 8 || destination_row < 0) {
 			
             valid = false;
		}
 		//check the destination is not the start
        else if(start == destination) {
 			valid = false;
 		}
 		//check if square is adjacent
 		else if(isAdjacent(start, destination)) {
 			if(setting.wallBetween(start, destination) || destination.hasPawn()) {
 				valid = false;
 			}
 		}
 		//check if the square is 2 away in 1 direction
 		else if(between != null) {
 			if(!between.hasPawn()) {
 				valid = false;
 			} else if (setting.wallBetween(start, between) 
 					|| setting.wallBetween(between, destination)) {
 				valid = false;
 			}
 		}
 		//check if the square is a diagonal
 		else if(destination_col - start_col == 1 && destination_row - start_row == 1) {
 			temp  = new SquareImpl(destination_col, destination_row - start_row);
 			temp2 = new SquareImpl(destination_col - start_col, destination_col);
 			if(!temp.hasPawn() && !temp2.hasPawn()) {
 				valid = false;
 			}
 		} else if(destination_col + start_col == 1 && destination_row - start_row == 1) {
 			temp  = new SquareImpl(destination_col, destination_row - start_row);
 			temp2 = new SquareImpl(destination_col + start_col, destination_col);
 			if(!temp.hasPawn() && !temp2.hasPawn()) {
 				valid = false;
 			}
 		} else if(destination_col - start_col == 1 && destination_row + start_row == 1) {
 			temp  = new SquareImpl(destination_col, destination_row + start_row);
 			temp2 = new SquareImpl(destination_col - start_col, destination_col);
 			if(!temp.hasPawn() && !temp2.hasPawn()) {
 				valid = false;
 			}
 		} else if(destination_col + start_col == 1 && destination_row + start_row == 1) {
 			temp  = new SquareImpl(destination_col, destination_row + start_row);
 			temp2 = new SquareImpl(destination_col + start_col, destination_col);
 			if(!temp.hasPawn() && !temp2.hasPawn()) {
 				valid = false;
 			}
		} else {
			valid = false;
 		}
 		return valid;
 	}
 
 	private boolean isAdjacent(Square a, Square b) {
 		boolean adjacent = false;
 		int aCol = a.getCol();
 		int aRow = a.getRow();
 		int bCol = b.getCol();
 		int bRow = b.getRow();
 		
 		if(aRow == bRow && (aCol - bCol == 1 || bCol - aCol == 1)) {
 			adjacent = true;
 		} else if(aCol == bCol && (aRow - bRow == 1 || bRow - aRow == 1)) {
 			adjacent = true;
 		}
 		return adjacent;
 	}
 	
 	private Square isTwoAway(Square a, Square b) {
 		Square between = null;
 		int aCol = a.getCol();
 		int aRow = a.getRow();
 		int bCol = b.getCol();
 		int bRow = b.getRow();
 		
 		if(aRow == bRow) {
 			if(aCol - bCol == 2) {
 				between = new SquareImpl(aCol - 1, aRow);
 			} else if(bCol - aCol == 2) {
 				between = new SquareImpl(bCol - 1, bRow);
 			}
 		} else if(aCol == bCol) {
 			if (aRow - bRow == 2) {
 				between = new SquareImpl(aCol, aRow - 1);
 			} else if(bRow - aRow == 2) {
 				between = new SquareImpl(bCol, bRow - 1);
 			}
 		}
 		return between;
 	}
 	
 	@Override
 	public boolean type() {
 		return PAWN;
 	}
 
 
     @Override
     public void makeMove() {
        //Pair <Player> players = new PairImpl();
        /*if (players._1().getName().equalsIgnoreCase(owner.getName())) {
        	System.out.println("name of this player is " +players._1().getName());
        } else if (players._2().getName().equalsIgnoreCase(owner.getName())) {
        	System.out.println("name of the other player is: " +players._2().getName());
        }*/
    	
    	System.out.println("in pawn move" +setting);
    	System.out.println("the name of the player is: " +owner.getName());
       
     }
 
 }
