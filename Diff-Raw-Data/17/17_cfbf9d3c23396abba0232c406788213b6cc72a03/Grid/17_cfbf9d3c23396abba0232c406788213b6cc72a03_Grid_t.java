 package com.sixtyfourbitsperminute.crushhour;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 /**
  * @author Jonathan Thompson
  * @author Kelly Croswell
  * 
  * This class represents the grid of the Rush Hour game, on which vehicles are 
  * located and moved around.
  */
 public class Grid {
 	
 	/**
 	 * A Map containing all of the vehicles on the grid and the character that 
 	 * represents them visually.
 	 */
 	HashMap<Character, Vehicle> vehicles;
 	
 	/**
 	 * A list of any previous grids that directly led to the formation of the 
 	 * current grid.
 	 */
 	ArrayList<Grid> previousGrids;
 	
 	/**
 	 * The length (or width, as grids are square) of the grid.
 	 */
 	int gridSize;
 
 	/**
 	 * This is the constructor for this class. It takes in an initial list of 
 	 * vehicles that are present on the grid, and the size of the grid.
 	 * 
 	 * @param vehicles A HashMap containing the list of vehicles and their characters.
 	 * @param gridSize An integer containing the size of the grid.
 	 */
 	public Grid(HashMap<Character, Vehicle> vehicles, int gridSize, ArrayList<Grid> previousGrids) {
 		this.vehicles = vehicles;
 		this.gridSize = gridSize;
 		this.previousGrids = previousGrids;
 	}
 
 	/**
 	 * This method moves a vehicle on a grid. It does this by taking in a move 
 	 * object, getting the vehicle associated with the grid, and updating all of 
 	 * the grid information that pertains to that vehicle by modifying the position 
 	 * of the vehicle and updating the vehicle map on the grid. It then returns 
 	 * the new grid that has resulted with the updated vehicle map and updated list 
 	 * of previous grids.
 	 * 
 	 * @param move The move about to be executed.
 	 * @return The resultant grid.
 	 */
 	public Grid executeMove(Move move) {
 		HashMap<Character, Vehicle> newVehicles = new HashMap<Character, Vehicle>(vehicles);
 		ArrayList<Grid> newPreviousGrids = new ArrayList<Grid>(previousGrids);
 		newPreviousGrids.add(this);
 		Vehicle v = newVehicles.get(move.getVehicle().identifier);
 		Coordinate modifiedPosition = new Coordinate(v.getPosition().x,v.getPosition().y);
 		if(v.horizontal){
 			modifiedPosition.x+=move.steps;
 		}else{
 			modifiedPosition.y+=move.steps;
 		}
 		if(v.length==2){
 			Car c = new Car(v.horizontal, modifiedPosition, v.identifier);
 			newVehicles.put(c.identifier, c);
 		} else {
 			Truck t = new Truck(v.horizontal, modifiedPosition, v.identifier);
 			newVehicles.put(t.identifier, t);
 		}
 		
 	
 		return new Grid(newVehicles,gridSize,newPreviousGrids);
 	}
 
 	/**
 	 * This method returns the size of the grid.
 	 * @return An int holding the size of the grid.
 	 */
 	public int getGridSize() {
 		return gridSize;
 	}
 
 	/**
 	 * This method returns the map of vehicles on the grid.
 	 * @return A HashMap containing the vehicles.
 	 */
 	public HashMap<Character, Vehicle> getVehicles() {
 		return vehicles;
 	}
 
 	/**
 	 * This method returns the list of previous grids associated with the current 
 	 * grid.
 	 * @return An ArrayList of previous grids.
 	 */
 	public ArrayList<Grid> getPreviousGrids() {
 		return previousGrids;
 	}
 
 	/**
 	 * This method sets the previous grids on a current grid.
 	 * @param previousGrids An ArrayList containing the previous grids.
 	 */
 	public void setPreviousGrids(ArrayList<Grid> previousGrids) {
 		this.previousGrids = previousGrids;
 	}
 
 	/**
 	 * This method gets a list of all of the Coordinates on the grid that are 
 	 * covered by one vehicle or another.
 	 * @return An ArrayList containing the covered coordinates.
 	 */
 	public ArrayList<Coordinate> getCoveredCoordinates() {
 		ArrayList<Coordinate> coveredCoordinates = new ArrayList<Coordinate>();
 		for (Character key : getVehicles().keySet()) {
 			coveredCoordinates.addAll(getVehicles().get(key).getCoveredCoordinates());
 		}
 		return coveredCoordinates;
 	}
 	
 	/**
 	 * This method gets a list of all of the Coordinates on the grid that are 
 	 * covered with the exception of one particular vehicle on the grid.
 	 * @param v The vehicle to be excluded from the list of covered coordinates.
 	 * @return An ArrayList containing all of the covered coordinates save those 
 	 * covered by vehicle v.
 	 */
 	public ArrayList<Coordinate> getCoveredCoordinatesExcludingVehicle(Vehicle v) {
 		ArrayList<Coordinate> coveredCoordinates = new ArrayList<Coordinate>();
 		for (Character key : getVehicles().keySet()){
 			if(key!=v.identifier){
 			coveredCoordinates.addAll(getVehicles().get(key).getCoveredCoordinates());
 		}
 		}
 		coveredCoordinates.removeAll(v.getCoveredCoordinates());
 		return coveredCoordinates;
 	}
 
 	/**
 	 * This is the overridden hash code method for the coordinate class. Implemented 
 	 * so that .equals() works properly. 
 	 * @return An integer containing the hash code.
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((previousGrids == null) ? 0 : previousGrids.hashCode());
 		result = prime * result
 				+ ((vehicles == null) ? 0 : vehicles.hashCode());
 		return result;
 	}
 
 	/**
 	 * This is the overridden equals method for the coordinate class. Implemented 
 	 * so that two grids can be easily compared for the purpose of eliminating loops.
 	 * @return Whether or not two grids are equal.
 	 */
 	@Override
 	public boolean equals(Object obj) {
		//if (this == obj)
			//return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Grid other = (Grid) obj;
 		if (vehicles == null) {
			if (other.vehicles != null){
 				return false;
			} else {
				return true;
			}
 		} else {
 			if(vehicles.size() != other.vehicles.size()){
 				return false;
 			}
 			for(char c : vehicles.keySet()){
 				if(!vehicles.get(c).equals(other.vehicles.get(c))){
 					return false;
 				}
 			}
			return true;
 		}
		//return true;
 	}
 
 	/**
 	 * This method returns whether or not there is a clear path from the user car 
 	 * on the grid to the exit spot on the grid, thereby finishing the game.
 	 * @return A boolean containing whether or not the path is clear.
 	 */
 	public boolean playerCanExit() {
 		Car player = (Car) vehicles.get('A');
 		ArrayList<Coordinate> coords = this.getCoveredCoordinatesExcludingVehicle(player);
 		for (int i = player.position.x; i < gridSize; i++) {
 			if(coords.contains(new Coordinate(i, player.position.y))){
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * This method returns null if the current grid is in the list of previous grids.
 	 * @return The grid, if it isn't in the previous grids list.
 	 */
 	public Grid nullIfPreviousState(){
 		String thisState = this.gridToString();
 		for(Grid g: previousGrids){
 			if(thisState.equals(g.gridToString())){
 				return null;
 			}
 		}
 		return this;
 	}
 	
 	/**
 	 * This method turns the list of vehicles associated with a grid into a String 
 	 * complete with x's for empty spots so that the grid can be handed over to 
 	 * the GUI for parsing.
 	 * @return A String containing the grid.
 	 */
 	public String gridToString () {
 		char[][] result = {{'x', 'x', 'x', 'x', 'x', 'x'},  
 						   {'x', 'x', 'x', 'x', 'x', 'x'},  
 						   {'x', 'x', 'x', 'x', 'x', 'x'},  
 						   {'x', 'x', 'x', 'x', 'x', 'x'},  
 						   {'x', 'x', 'x', 'x', 'x', 'x'},  
 						   {'x', 'x', 'x', 'x', 'x', 'x'}};
 		for(char c : this.vehicles.keySet()){
 			Vehicle current = vehicles.get(c);
 			//System.out.println(c);
 			ArrayList<Coordinate> coveredPositions = current.getCoveredCoordinates();
 			//System.out.println("Position x: " + current.getPosition().x + ", Position y: " + current.getPosition().y);
 			//System.out.println("Orientation: " + current.horizontal);
 			for(int i = 0; i < coveredPositions.size(); i++){
 				Coordinate currentCoordinate = coveredPositions.get(i);
 				//System.out.println("x: " + currentCoordinate.x + ", y: " + currentCoordinate.y);
 				result[currentCoordinate.y][currentCoordinate.x] = c;
 			}
 		}
 		String resultString = "";
 		for(int i = 0; i < 6; i++){
 			for(int j = 0; j < 6; j++){
 				resultString = resultString + result[i][j];
 				//System.out.println(resultString);
 			}
 		}
 		
 		return resultString;
 	}
 
 }
