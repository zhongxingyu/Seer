 package ch.zhaw.labyrinth.utils;
 
 import java.util.HashMap;
 import java.util.Random;
 
 public class Labyrinth {
 	
 	// instance variables
 	private HashMap<Coordinate, Cell> maze;
 	private int dimension;
     private Coordinate entry;
     private Coordinate exit;
 	
 	// constructor
 	public Labyrinth(int dim){
 		this.maze = new HashMap<Coordinate, Cell>();
 		this.dimension = dim;
 		createEmptyMaze();
 	}
 
    public Labyrinth() {};
 	
 	// create empty maze with cell's and coordinate's
 	private void createEmptyMaze() {
 		
 		for (int x = 0; x < getDimension(); x++) {
 			for (int y = 0; y < getDimension(); y++) {
 				this.maze.put(new Coordinate(x, y), new Cell());
 			}
 		}
 	}
 
     // getter for dimension
 	public int getDimension() {
 		return dimension;
 	}
 	
 	// getter for Cell with coordinate attribute
 	public Cell getCellAt(Coordinate c){
 		return this.maze.get(c);
 	}
 	
 	// getter for Cell with x and y attributes
 	public Cell getCellAt(int x, int y){
 		return this.maze.get(new Coordinate(x,y));
 	}
 
     public HashMap<Coordinate, Cell> getMaze() {
         return maze;
     }
 
     /**
 	  * @return a random int within the dimension 
 	 */
 	public int getRandomIntOdd(int dim){
 		Random rand = new Random();
 		int r = rand.nextInt(dim);
 		while (r % 2 == 0) {
 			r = rand.nextInt(dim);
 		}
 		return r;
 	}
 	
 	// setter method to set the cell value from a coordinate
 	public void setCellValue(Coordinate c,boolean val){
 		maze.get(c).setPath(val);
 	}
 	
 	// setter method to set the cell value from an x and y parameter
 	public void setCellValue(int x,int y,boolean val){
         Coordinate coordinate = new Coordinate(x,y);
 		Cell cell = maze.get(coordinate);
         cell.setPath(val);
 	}
 	
 	// getter method for cell value at specific coordinate
 	public boolean getCellValueAt(Coordinate c){
 		return maze.get(c).isPath();
 	}
 	
 	// getter method for cell value at specific x,y point
 	public boolean getCellValueAt(int x, int y){
		return maze.get(new Coordinate(x,y)).isPath();
 	}
 
     public Coordinate getEntry() {
         return entry;
     }
 
     public void setEntry(Coordinate entry) {
         this.entry = entry;
     }
 
     public Coordinate getExit() {
         return exit;
     }
 
     public void setExit(Coordinate exit) {
         this.exit = exit;
     }
 }
