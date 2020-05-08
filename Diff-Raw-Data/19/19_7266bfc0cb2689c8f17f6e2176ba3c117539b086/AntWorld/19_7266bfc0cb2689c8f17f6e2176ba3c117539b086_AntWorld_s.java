 package models;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import util.RandomGen;
 import ai.AntBrain;
 import antgame.Config;
 import cells.AntHill;
 import cells.BlackAntHill;
 import cells.Cell;
 import cells.ClearCell;
 import cells.RedAntHill;
 import cells.RockyCell;
 import exceptions.AntNotFoundException;
 import exceptions.CellOccupiedException;
 
 /**
  * World.java
  * 
  * @author Chris B
  * @date 17 Mar 2013
  * @version 1.0
  */
 public class AntWorld implements Model {
 
 	private Cell[][] world;
 	private List<Ant> ants;
 	private int roundNum;
 	private int redScore;
 	private int blackScore;
 	private AntBrain redBrain;
 	private AntBrain blackBrain;
 	private RandomGen random;
 
 	/**
 	 * 
 	 * @param world
 	 */
 	public AntWorld(Cell[][] world, AntBrain redBrain, AntBrain blackBrain) {
 		this.world = world;
 		this.redBrain = redBrain;
 		this.blackBrain = blackBrain;
 		this.ants = new ArrayList<Ant>();
 		this.roundNum = 0;
 		this.redScore = 0;
 		this.blackScore = 0;
 		// TODO some way of making the seeds different for games but somehow saveable?
 		this.random = new RandomGen(12345);
 		this.initialise();
 	}
 
 	/**
 	 * 
 	 */
 	private void initialise() {
 		// iterate over world cells finding ant hill cells adding them to the
 		// ant hill lists
 		// Iterate through world cells and populate the ant hill coordinate arrays
 
 		// create ants. Each ant hill cell should spawn an ant at the start of the game
 		for (int x = 0; x < this.world.length; x += 1) {
 			for (int y = 0; y < this.world[0].length; y += 1) {
 				Cell c = this.world[x][y];
 				if (c instanceof AntHill) {
 					// spawn an ant on each anthill
 					AntHill b = (AntHill) c;
 					try {
 						Ant a = b.spawnAnt(this);
 						//this.ants.add(a);
 					}
 					catch (CellOccupiedException e) {
 						// this shouldn't happen but if the ant hill 
 						// already has an ant we don't need to spawn
 						// one anyway.
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Iterate over world elements updating them
 	 */
 	@Override
 	public void update() {
 
 		// update all ants
 		for(int y = 0; y < this.world[0].length; y += 1) {
 			for(int x = 0; x < this.world.length; x += 1) {
 				Cell c = this.world[x][y];
 				if(c instanceof ClearCell) {
 					if(c.isOccupied()) {
 						ClearCell clearCell = (ClearCell) c;
 						Ant a = clearCell.getAnt();
 						if(a.getLastupdated() < this.roundNum) {
 							a.update();
 						}
 					}
 				}
 			}
 		}
 
 		// increment the round counter
 		roundNum += 1;
 
 	}
 
 	/**
 	 * Try to move an ant from the cell indicated by fromX and fromY to
 	 * the cell indicated by toX and toY. If the cell is occupied already
 	 * then the function returns false. If the move is successful then true
 	 * is returned.
 	 * 
 	 * @param fromX the x coordinate of the cell to move from
 	 * @param fromY the y coordinate of the cell to move from
 	 * @param toX the x coordinate of the destination cell
 	 * @param toY the y coordinate of the destination cell
 	 * @return true is the move is successful, false otherwise
 	 */
 	public boolean move(int fromX, int fromY, int toX, int toY) {
 		try {
 			Cell from = this.world[fromX][fromY];
 			Cell to = this.world[toX][toY];
 
 			// precondition source cell must be occupied
 			assert (from.isOccupied());
 
 			// precondition destination must be clear
 			assert (!to.isOccupied());
 
 			// return failure if destination is occupied or source is not.
 			// failing here stops a bug where an ant is removed from a cell and can't
 			// be placed in an occupied cell.
 			if(to instanceof RockyCell) {
 				return false;
 			}
 			if (to.isOccupied()) {
 				return false;
 			}
 			if (!from.isOccupied()) {
 				return false;
 			}
 			
 			try {
 				Ant a = from.leave();
 				to.arrive(a);
 
 				// postcondition source must be empty
 				assert (!from.isOccupied());
 
 				// postcondition destination must be populated
 				assert (to.isOccupied());
 
 				// TODO check for surrounded ants				
 				return true;
 			}
 			catch (AntNotFoundException e) {				
 				e.printStackTrace();
 				return false;
 			}
 			catch (CellOccupiedException e) {				
 				e.printStackTrace();
 				return false;
 			}
 		}
 		catch (ArrayIndexOutOfBoundsException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	/**
 	 * returns a random number from this worlds random generator.
 	 * The number generated is between 0 and num-1.
 	 */
 	public int getInt(int num) {
 		return this.random.randomint(num);
 	}
 
 	/**
 	 * @param position
 	 * @return
 	 */
 	public boolean pickUpFood(int x, int y) {
 		try {
 			Cell cell = this.world[x][y];
 			if (cell instanceof ClearCell) {
 				ClearCell clearCell = (ClearCell) cell;
 				return clearCell.pickUpFood();
 				
 				// TODO keep track of score possible to pick up food from clear cell, home, foe home
 			}
 		}
 		catch (ArrayIndexOutOfBoundsException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	/**
 	 * @param x
 	 * @param y
 	 */
 	public void dropFood(int x, int y) {
 		try {
 			Cell cell = this.world[x][y];
 			if (cell instanceof ClearCell) {
 				ClearCell clearCell = (ClearCell) cell;
 				clearCell.dropFood();
 				
 				// TODO keep track of score possible to drop food on clear cell, home, foe home
 			}
 		}
 		catch (ArrayIndexOutOfBoundsException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public boolean senseFriend(int x, int y, int color) {
 		try {
 			Cell cell = this.world[x][y];
 			if(cell instanceof ClearCell) {
 				if(cell.isOccupied()) {
 					ClearCell clearCell = (ClearCell) cell;
 					Ant a = clearCell.getAnt();
 					if(a.getColor() == color) {
 						return true;
 					}
 				}
 			}
 		}
 		catch (ArrayIndexOutOfBoundsException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public boolean senseFoe(int x, int y, int color) {
 		try {
 			Cell cell = this.world[x][y];
 			if(cell instanceof ClearCell) {
 				if(cell.isOccupied()) {
 					ClearCell clearCell = (ClearCell) cell;
 					Ant a = clearCell.getAnt();
 					if(a.getColor() != color) {
 						return true;
 					}
 				}
 			}
 		}
 		catch (ArrayIndexOutOfBoundsException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public boolean senseFriendWithFood(int x, int y, int color) {
 		try {
 			Cell cell = this.world[x][y];
 			if(cell instanceof ClearCell) {
 				if(cell.isOccupied()) {
 					ClearCell clearCell = (ClearCell) cell;
 					Ant a = clearCell.getAnt();
 					if(a.getColor() == color) {
 						if(a.hasFood()) {
 							return true;
 						}
 					}
 				}
 			}
 		}
 		catch (ArrayIndexOutOfBoundsException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public boolean senseFoeWithFood(int x, int y, int color) {
 		try {
 			Cell cell = this.world[x][y];
 			if(cell instanceof ClearCell) {
 				if(cell.isOccupied()) {
 					ClearCell clearCell = (ClearCell) cell;
 					Ant a = clearCell.getAnt();
 					if(a.getColor() != color) {
 						if(a.hasFood()) {
 							return true;
 						}
 					}
 				}
 			}
 		}
 		catch (ArrayIndexOutOfBoundsException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public boolean senseFood(int x, int y) {
 		try {
 			Cell cell = this.world[x][y];
 			if(cell instanceof ClearCell) {
				if(cell.isOccupied()) {
					ClearCell clearCell = (ClearCell) cell;
					return clearCell.numFood() > 0;
				}
 			}
 		}
 		catch (ArrayIndexOutOfBoundsException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public boolean senseRock(int x, int y) {
 		try {
 			Cell cell = this.world[x][y];
 			if(cell instanceof RockyCell) {
 				return true;
 			}
 		}
 		catch (ArrayIndexOutOfBoundsException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	/**
 	 * @param x
 	 * @param y
 	 * @param type
 	 * @param color
 	 * @return
 	 */
 	public boolean senseMarker(int x, int y, int type, int color) {
 		try {
 			Cell cell = this.world[x][y];
 			if (cell instanceof ClearCell) {
 				ClearCell clearCell = (ClearCell) cell;
 				if (color == Config.BLACK_ANT) {
 					return clearCell.senseBlackTrail(type);
 				}
 				else if (color == Config.RED_ANT) {
 					return clearCell.senseRedTrail(type);
 				}
 			}
 		}
 		catch (ArrayIndexOutOfBoundsException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public boolean senseFoeMarker(int x, int y, int color) {
 		try {
 			Cell cell = this.world[x][y];
 			if(cell instanceof ClearCell) {
 				if(cell.isOccupied()) {
 					ClearCell clearCell = (ClearCell) cell;
 					return clearCell.senseFoeMarker(color);
 				}
 			}
 		}
 		catch (ArrayIndexOutOfBoundsException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public boolean senseHome(int x, int y, int color) {
 		try {
 			Cell cell = this.world[x][y];
 			if(color == Config.RED_ANT) {
 				if(cell instanceof RedAntHill) {
 					return true;
 				}
 			}
 			else if(color == Config.BLACK_ANT) {
 				if(cell instanceof BlackAntHill) {
 					return true;
 				}
 			}
 		}
 		catch (ArrayIndexOutOfBoundsException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public boolean senseFoeHome(int x, int y, int color) {
 		try {
 			Cell cell = this.world[x][y];
 			if(color == Config.RED_ANT) {
 				if(cell instanceof BlackAntHill) {
 					return true;
 				}
 			}
 			else if(color == Config.BLACK_ANT) {
 				if(cell instanceof RedAntHill) {
 					return true;
 				}
 			}
 		}
 		catch (ArrayIndexOutOfBoundsException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	/**
 	 * @param x
 	 * @param y
 	 * @param type
 	 * @param color
 	 */
 	public void mark(int x, int y, int type, int color) {
 		try {
 			Cell cell = this.world[x][y];
 			if (cell instanceof ClearCell) {
 				ClearCell clearCell = (ClearCell) cell;
 				if (color == Config.BLACK_ANT) {
 					clearCell.setBlackTrail(type);
 				}
 				else if (color == Config.RED_ANT) {
 					clearCell.setRedTrail(type);
 				}
 			}
 		}
 		catch (ArrayIndexOutOfBoundsException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * @param x
 	 * @param y
 	 * @param type
 	 * @param color
 	 */
 	public void unmark(int x, int y, int type, int color) {
 		try {
 			Cell cell = this.world[x][y];
 			if (cell instanceof ClearCell) {
 				ClearCell clearCell = (ClearCell) cell;
 				if (color == Config.BLACK_ANT) {
 					clearCell.clearBlackTrail(type);
 				}
 				else if (color == Config.RED_ANT) {
 					clearCell.clearRedTrail(type);
 				}
 			}
 		}
 		catch (ArrayIndexOutOfBoundsException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * @return the world cells
 	 */
 	public Cell[][] getCells() {
 		return this.world;
 	}
 
 	
 	/**
 	 * @return the redPlayer
 	 */
 	public AntBrain getRedBrain() {
 		return this.redBrain;
 	}
 
 	/**
 	 * @return the blackPlayer
 	 */
 	public AntBrain getBlackBrain() {
 		return this.blackBrain;
 	}
 
 	/**
 	 * @return
 	 */
 	public int getRoundNum() {
 		return this.roundNum;
 	}
 
 	public int getRedScore() {
 		return this.redScore;
 	}
 	
 	public int getBlackScore() {
 		return this.blackScore;
 	}
 	
 	public Cell[][] getWorld() {
 		return this.world;
 	}
 	
 	// TODO ants that die
 	
 	@Override
 	public String toString(){
 		int antID = 0;
 		StringBuffer buffer = new StringBuffer();
 		for(int i = 0; i < world.length; i++){
 			for (int j = 0; j < world[0].length; j++){
 				buffer.append("cell ("+j+", "+i+"): ");
 				Cell cell = world[j][i];
 				String type = cell.getClass().getName();
 				if (type.equals("cells.RockyCell")){
 					buffer.append("rock");
 				}
 				else{
 					ClearCell myCell =(ClearCell) cell;
 					if (type.equals("cells.BlackAntHill")){
 						buffer.append("black hill; ");
 					}
 					else if (type.equals("cells.RedAntHill")){
 						buffer.append("red hill; ");
 					}
 					else if (type.equals("cells.ClearCell")){
 						
 						if (myCell.numFood() == 0){
 							}
 							else{
 								buffer.append(myCell.numFood() + " food; ");
 							}
 					}
 					if (myCell.senseFoeMarker(Config.RED_ANT)){
 						buffer.append("black marks: ");
 						for (int x = 0; x <= 5; x++){
 							if (myCell.senseBlackTrail(x)){
 								buffer.append(x);
 							}
 						}
 						buffer.append("; ");
 					}
 					if (myCell.senseFoeMarker(Config.BLACK_ANT)){
 						buffer.append("red marks: ");
 						for (int x = 0; x <= 5; x++){
 							if (myCell.senseRedTrail(x)){
 								buffer.append(x);
 							}
 						}
 						buffer.append("; ");
 					}
 					if (cell.isOccupied()&&type!="cells.RockyCell"){
 						Ant a = ((ClearCell)cell).getAnt();
 						if(a.getColor()== Config.RED_ANT){
 						buffer.append("red ant of id "+ antID++ + ", dir "+ a.getDirection()+ ", food "+ ((a.hasFood())? 1 : 0) + ", state "+ a.getState() + ", resting " + a.getResting());
 						}
 						else{
 							buffer.append("black ant of id "+antID++ + ", dir "+ a.getDirection()+ ", food "+ ((a.hasFood())? 1 : 0) + ", state "+ a.getState() + ", resting " + a.getResting());
 						}
 					}
 				}
 				buffer.append("\r\n");
 			}
 		}
 		return buffer.toString();
 	}
 }
