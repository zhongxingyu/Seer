 package drop7.game;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import drop7.Drop7Constants;
 import drop7.disks.Disk;
 import drop7.disks.GrayDisk;
 import drop7.disks.NumberDisk;
 import drop7.grid.Drop7Grid;
 import drop7.world.Drop7World;
 import info.gridworld.actor.Actor;
 import info.gridworld.grid.Grid;
 import info.gridworld.grid.Location;
 import info.gridworld.world.World;
 
 public class Drop7Game implements Drop7Constants {
 	/** The world - probably want to subclass this */
	private Drop7World<?> world;
 	private Drop7Grid<Disk> grid;
 	private Random generator;
 	private int emptyLocations;
 
 	public Drop7Game() {
 		generator = new Random();
 		grid = new Drop7Grid<Disk>(NUM_ROWS, NUM_COLS);
 		world = new Drop7World(grid);
 		world.show();
 
 	}
 
 	/**
 	 * Plays the game until it is over (no player can play).
 	 */
 	public void playGame() {
 		setUpGrid();
 		System.out.println(this.toString());
 		while (emptyLocations != 0) {
 			// nextMove();
 			// calculateScore();
 		}
 		// displayMessage();
 	}
 
 	private void setUpGrid() {
 		for (int i = 0; i < 1; i++) {
 			Disk d = new NumberDisk();
 			d.putSelfInGrid(grid, new Location(6, 0));
 		}
 		
 	}
 
 	/**
 	 * Creates a string with the current game state. (used for the GUI message
 	 * and debugging).
 	 */
 	public String toString() {
 		return this.grid.toString();
 	}
 }
