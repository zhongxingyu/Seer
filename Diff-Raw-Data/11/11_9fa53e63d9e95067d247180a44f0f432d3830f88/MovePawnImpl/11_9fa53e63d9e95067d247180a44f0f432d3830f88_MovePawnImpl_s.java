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
 		//Pair<PawnImpl> test = new Pair<PawnImpl>(setting.getPawn(subject, setting))
 		
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
 			if(setting.wallBetween(start, destination) || destination.hasPawn(setting.getPawn(owner.getOpponent(),setting))) {
 				valid = false;
 			}
 		}
 		//check if the square is 2 away in 1 direction
 		else if(between != null) {
 			if(!between.hasPawn(setting.getPawn(owner.getOpponent(),setting))) {
 				valid = false;
 			} else if (setting.wallBetween(start, between) 
 					|| setting.wallBetween(between, destination)) {
 				valid = false;
 			}
 		}
 		//check if the square is a diagonal
 		else if(start_col - destination_col == 1 && start_row - destination_row == 1) {
 			//System.out.println("diag 1");
 			temp  = new SquareImpl(destination_col, start_row);
 			temp2 = new SquareImpl(start_col, destination_row);
 			if(temp.hasPawn(setting.getPawn(owner.getOpponent(),setting))) {
 				temp2 = new SquareImpl(destination_col - 1, start_row);
 				if(!setting.wallBetween(temp, temp2)) {
 					valid = false;
 				} else if(setting.wallBetween(start, temp)) {
 					valid = false;
 				} else if(setting.wallBetween(temp, destination)) {
 					valid = false;
 				}
 			} else if(temp2.hasPawn(setting.getPawn(owner.getOpponent(),setting))) {
 				temp = new SquareImpl(start_col, destination_row - 1);
 				if(!setting.wallBetween(temp, temp2)) {
 					valid = false;
 				} else if(setting.wallBetween(start, temp2)) {
 					valid = false;
 				} else if(setting.wallBetween(temp2, destination)) {
 					valid = false;
 				}
 			}
 		} else if(destination_col - start_col == 1 && start_row - destination_row == 1) {
 			//System.out.println("diag 2");
 			temp  = new SquareImpl(destination_col, start_row);
 			temp2 = new SquareImpl(start_col, destination_row);
 			if(temp.hasPawn(setting.getPawn(owner.getOpponent(),setting))) {
 				temp2 = new SquareImpl(destination_col + 1, start_row);
 				//System.out.println("temp");
 				if(!setting.wallBetween(temp, temp2)) {
 					valid = false;
 				} else if(setting.wallBetween(start, temp)) {
 					valid = false;
 				} else if(setting.wallBetween(temp, destination)) {
 					valid = false;
 				}
 			} else if(temp2.hasPawn(setting.getPawn(owner.getOpponent(),setting))) {
 				temp = new SquareImpl(start_col, destination_row - 1);
 				//System.out.println("temp2");
 				if(!setting.wallBetween(temp, temp2)) {
 				//	System.out.println("in here");
 					valid = false;
 				} else if(setting.wallBetween(start, temp2)) {
 					//System.out.println("number2 ");
 					valid = false;
 				} else if(setting.wallBetween(temp2, destination)) {
 					//System.out.println("number3");
 					valid = false;
 				}
 			}
 		} else if(destination_col - start_col == 1 && destination_row - start_row == 1) {
 			//System.out.println("diag 3");
 			temp  = new SquareImpl(destination_col, start_row);
 			temp2 = new SquareImpl(start_col, destination_row);
 			
 			if(temp.hasPawn(setting.getPawn(owner.getOpponent(),setting))) {
 				temp2 = new SquareImpl(destination_col + 1, start_row);
 				//System.out.println("temp1 has pawn?");
 				if(!setting.wallBetween(temp, temp2)) {
 					//System.out.println("valid is false!");
 					valid = false;
 				} else if(setting.wallBetween(start, temp)) {
 					valid = false;
 				} else if(setting.wallBetween(temp, destination)) {
 					valid = false;
 				}
 			} else if(temp2.hasPawn(setting.getPawn(owner.getOpponent(),setting))) {
 				//System.out.println("temp2 has pawn");
 				temp = new SquareImpl(start_col, destination_row + 1);
 				if(!setting.wallBetween(temp, temp2)) {
 					valid = false;
 				} else if(setting.wallBetween(start, temp2)) {
 					valid = false;
 				} else if(setting.wallBetween(temp2, destination)) {
 					valid = false;
 				}
 			}
 			//System.out.println("not even in pawn");
 		} else if(start_col - destination_col == 1 && destination_row - start_row == 1) {
 			//System.out.println("diag 4");
 			temp  = new SquareImpl(destination_col, start_row);
 			temp2 = new SquareImpl(start_col, destination_row);
 			
 			if(temp.hasPawn(setting.getPawn(owner.getOpponent(),setting))) {
 				temp2 = new SquareImpl(destination_col - 1, start_row);
 				//System.out.println("temp");
 				if(!setting.wallBetween(temp, temp2)) {
 					valid = false;
 				} else if(setting.wallBetween(start, temp)) {
 					valid = false;
 				} else if(setting.wallBetween(temp, destination)) {
 					valid = false;
 				}
 			} else if(temp2.hasPawn(setting.getPawn(owner.getOpponent(),setting))) {
 				temp = new SquareImpl(start_col, destination_row + 1);
 				//System.out.println("temp2");
 				if(!setting.wallBetween(temp, temp2)) {
 					//System.out.println("first");
 					valid = false;
 				} else if(setting.wallBetween(start, temp2)) {
 					//System.out.println("second");
 					valid = false;
 				} else if(setting.wallBetween(temp2, destination)) {
 					//System.out.println("third");
 					valid = false;
 				}
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
         setting.getPawn(owner, setting).setSquare(destination);
     }
 
 	@Override
 	public String getMessage() {
 		return "Invalid move";
 	}
 
 	public String toString() {
 		String retVal = "";
 		retVal+= (char)(destination.getCol() + (int) 'a');
 		retVal+= destination.getRow()+1;
 		return retVal;
 	}
 
 	@Override
 	public Player getPlayer() {
 		return owner;
 	}
 }
