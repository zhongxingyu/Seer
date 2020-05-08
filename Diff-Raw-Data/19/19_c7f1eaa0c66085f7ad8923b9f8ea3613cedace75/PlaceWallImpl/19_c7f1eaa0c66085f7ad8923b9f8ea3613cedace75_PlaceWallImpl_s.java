 package quoridor;
 
 public class PlaceWallImpl extends AbstractMove implements PlaceWall{
 
 	Wall tentative;
 	
 	
 	
 	public PlaceWallImpl(int col, int row, boolean direction, Player owner, Board setting) {
 		super(owner, setting);
 		tentative = new WallImpl(new SquareImpl(col, row), direction);
 	}
 	
 	@Override
 	public boolean isValid() {
 		boolean valid = true;
 		Wall test = new WallImpl(new SquareImpl(tentative.topLeft().getCol(), tentative.topLeft().getRow()),
 									tentative.getDirection());
 		boolean direction = tentative.getDirection();
 		if (direction == Wall.HORIZONTAL) {
 			direction = Wall.VERTICAL;
 		} else {
 			direction = Wall.HORIZONTAL;
 		}
 		Wall opposite = new WallImpl(new SquareImpl(tentative.topLeft().getCol(), tentative.topLeft().getRow()), direction);		
 		
 		if (setting.getPawn(owner, setting).getOwner().wallCount() <= 0) {
 			valid = false;
 		}
 		if(tentative.topLeft().getCol() < 0) {
 			valid = false;
 		} else if(tentative.topLeft().getRow() < 0) {
 			valid = false;
 		} else if(tentative.topLeft().getCol() > 7) {
 			valid = false;
 		} else if(tentative.topLeft().getRow() > 7) {
 			valid = false;
 		} else if (setting.containsWall(test) == true) {
 				valid = false;
 		} else if (setting.containsWall(opposite) == true) {
 				valid = false;
 		} else {
 			setting.addWall(tentative);
			System.out.println("++++++++++++++++" + setting);
 			if (setting.pathToGoal(setting.getPawn(owner, setting)) == false) {
 				valid = false;
				System.out.println("here");
 			} else if (setting.pathToGoal(setting.getPawn(owner.getOpponent(), setting)) == false) {
 				valid = false;
				System.out.println("here");
 			}
 			setting.removeWall(tentative);
 		}
 
 		if (owner.hasWon(setting) || owner.getOpponent().hasWon(setting)) {
 			valid = false;
 		}
 		
 		return valid;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see quoridor.PlaceWall#getTentative()
 	 */
 	@Override
 	public Wall getTentative() {
 		return tentative;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see quoridor.AbstractMove#type()
 	 */
 	@Override
 	public boolean type() {
 		return WALL;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see quoridor.AbstractMove#makeMove()
 	 */
     @Override
     public void makeMove() {
     	setting.addWall(tentative);
     	setting.getPawn(owner, setting).getOwner().decreaseWallCount();
     }
 
     /*
      * (non-Javadoc)
      * @see quoridor.GenericMove#getMessage()
      */
 	@Override
 	public String getMessage() {
 		String retVal;
 		if (setting.getPawn(owner, setting).getOwner().wallCount() <= 0) {
 			retVal = "Invalid move: Player has no walls left";
 		} else {
 			retVal = "Invalid wall placement";
 		}
 		
 		return retVal;
 	}
 	
 	public String toString() {
 		String retVal = "";
 		
 		retVal+= (char)(tentative.topLeft().getCol() + (int) 'a');
 		retVal+= tentative.topLeft().getRow()+1;
 		if (tentative.getDirection() == Wall.HORIZONTAL) {
 			retVal += "h";
 		} else {
 			retVal+= "v";
 		}
 		
 		return retVal;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see quoridor.GenericMove#getPlayer()
 	 */
 	@Override
 	public Player getPlayer() {
 		return owner;
 	}
 
 }
