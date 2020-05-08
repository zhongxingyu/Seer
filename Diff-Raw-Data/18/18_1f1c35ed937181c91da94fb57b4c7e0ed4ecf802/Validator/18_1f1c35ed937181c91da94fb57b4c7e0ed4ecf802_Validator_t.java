 /*
  * Validator.java	
  * Version 1.0 (2013-05-31)
  */
 
 package battleships.game;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 
 
 /**
  * Class Validator validates that the ships in a navy fulfills the requirements in the game. That is that they
  * 	- Are of the specified amount per type
  * 	- Have the length specified for their type
  * 	- Have unique coordinates
  * 	- Are placed liner horizontally or vertically
  * 	- Have space between each other in all directions
  * 
  * The collection Set is used as a help in many places to make sure the same coordinate is not used two times either inside a single ship or in different ships.
  * @author Åsa Waldhe
  *
  */
 public class Validator {
 
 	private final Integer NUM_SUBMARINES, NUM_DESTROYERS, NUM_AIRCRAFT_CARRIERS;	
 
 	private HashSet<Coordinate> allCoords;
 	
 	/**
 	 * Initiates the constants for how many ships there should be of each kind.
 	 * 
 	 * @param ns		Integer, number submarines
 	 * @param nd		Integer, number destroyers
 	 * @param na		Integer, number aircraft carriers
 	 */
 	public Validator(Integer ns, Integer nd, Integer na)
 	{
 		this.NUM_SUBMARINES=ns;
 		this.NUM_DESTROYERS=nd;
 		this.NUM_AIRCRAFT_CARRIERS=na;
 		
 		allCoords = new HashSet<Coordinate>();
 	}
 	
 	/**
 	 * Validates the navy in all aspects mentioned in the description of this class.
 	 * 
 	 * @param n		Navy to validate.
 	 * @return		Ok or not ok. Does not specify the fault.
 	 */	
 	public Boolean validateNavy(Navy n){
 		
 		// Ships in navy 
 		LinkedList<Ship> l = n.getShips();
 		
 		// Boolean variable to return at the end of the function. 
 		Boolean ok=true;
 		
 		// Counters for ship-types. 
 		int subs=0, dests=0, aircs=0;
 		
 		// Iterator to walk through the ships. 
 		Iterator<Ship> it = l.iterator();
 		
 		// The present ship to control. 
 		Ship tmp;
 		
 		// Do the following examinations to all ships.
 		// If one ship by some reason does not fulfill the requirements the while-loop is stopped. 
 		while(it.hasNext() && ok){
 			
 			tmp = it.next();		
 			
 			// Separate functions needed for different ship-types. Identify type by name.			
 			String tmpName = tmp.getName();	
 			int length = tmp.getCoords().size(); // Gets the number of coordinates in the ship. Does not ask for the length-constant of the ship.
 			
 			// Iterator for comparing coordinates in the ship. 
 			Iterator<Coordinate> cordIt = tmp.getCoords().iterator();
 			
 			// Increments counters for types.
 			// Checks the length of the ship.
 			// Validates the placement of the ships coordinates.
 			// Validates space around the ship.
 			switch(tmpName)
 			{
 				case "Submarine": 
 						subs++;
 						ok = (length == Submarine.LENGTH_S);
 					break;
 				case "Destroyer": 
 						dests++;
 						if(length== Destroyer.LENGTH_D)
 							ok = this.validatePlacement(cordIt);
 						if(ok && tmp.getDirection()== Ship.HORIZONTAL)
 							ok = this.validateSpaceHorizontal(length, tmp.getFirstCoord());
 						else if(ok)
 							ok = this.validateSpaceVertical(length, tmp.getFirstCoord());
 					break;
 				case "Aircraft carrier": 
 						aircs++; 
 						if(length == Aircraft_carrier.LENGTH_A)
 							ok = this.validatePlacement(cordIt);
 						if(ok && tmp.getDirection()== Ship.HORIZONTAL)
 							ok = this.validateSpaceHorizontal(length, tmp.getFirstCoord());
 						else if(ok)
 							ok = this.validateSpaceVertical(length, tmp.getFirstCoord());
 					break;
 			}
 			
 			// Collects all coordinates, from all ships, in one set to make sure that the ships are not on the same place.
 			if(ok){
 				while(cordIt.hasNext() && ok){
 					Coordinate tmpc = cordIt.next();
 					ok = allCoords.add(tmpc);	// Ok when the coordinate is new in the set.
 					//System.out.println(tmpc);
 					//System.out.println(ok);
 				}
 				//System.out.println(allCoords);
 			}			
 		} // End while
 		
 		// Checks the sum of the ships.
 		if(ok)
 			ok = ((subs == NUM_SUBMARINES) && (NUM_DESTROYERS == dests && aircs == NUM_AIRCRAFT_CARRIERS));
 		
 		return ok;		
 	}
 	
 	/**
 	 * Checks that all coordinates surrounding a horizontal ship is empty, by assuring that they are not in
 	 *  the set with coordinates from all ships.
 	 * 
 	 * @param length 			Length of the ship.
 	 * @param firstCoord		First Coordinate in the ship. (Upper left.)
 	 * @return Boolean			True if ok, false if not.
 	 */
 	private Boolean validateSpaceHorizontal(Integer length, Coordinate firstCoord){		
 		
 		Coordinate tmpC = new Coordinate((firstCoord.getX()-1), firstCoord.getY());
 		if(allCoords.contains(tmpC))
 			return false;
 		
		Coordinate tmpC1 = new Coordinate((firstCoord.getX()+length), firstCoord.getY());
 		if(allCoords.contains(tmpC1))
 			return false;
 		
 		for(int i=0;i<length+2;i++){
 			
 			Coordinate tmpC2 = new Coordinate((firstCoord.getX()-1+i), firstCoord.getY()-1);
 			if(allCoords.contains(tmpC2))
 				return false;
 			Coordinate tmpC3 = new Coordinate((firstCoord.getX()-1+i), firstCoord.getY()+1);
 			if(allCoords.contains(tmpC3))
 					return false;
 		}
 		
 		return true;		
 	}
 	
 	/**
 	 * Checks that all coordinates surrounding a vertical ship is empty, by assuring that they
 	 *  are not in the set with coordinates from all ships.
 	 * 
 	 * @param length 			Length of the ship.
 	 * @param firstCoord		First Coordinate in the ship. (Upper left.)
 	 * @return Boolean1			True if ok, false if not.
 	 */
 	private Boolean validateSpaceVertical(Integer length, Coordinate firstCoord){		
 		
 		Coordinate tmpC = new Coordinate(firstCoord.getX(), (firstCoord.getY()-1));
 		if(allCoords.contains(tmpC))
 			return false;
 		
		Coordinate tmpC1 = new Coordinate(firstCoord.getX(), (firstCoord.getY()+length));
 		if(allCoords.contains(tmpC1))
 			return false;
 		
 		for(int i=0;i<length+2;i++){
 			
 			Coordinate tmpC2 = new Coordinate((firstCoord.getX()-1), (firstCoord.getY()-1+i));
 			if(allCoords.contains(tmpC2))
 				return false;
			Coordinate tmpC3 = new Coordinate((firstCoord.getX()+1), (firstCoord.getY()-1+i));
 			if(allCoords.contains(tmpC3))
 					return false;
 		}
 		
 		return true;		
 	}
 	
 	/**
 	 * Only horizontal or vertical placement is ok. The coordinates must also be linear.
 	 * 
 	 * @param it, Iterator<Coordinate> All coordinates in one ship.
 	 * @return	ok, Boolean
 	 */
 	private Boolean validatePlacement(Iterator<Coordinate> it)
 	{
 		Boolean ok = true;
 		Coordinate c = it.next();
 		Coordinate c1 = it.next();
 		
 		
 		Integer x = c.getX();
 		Integer y = c.getY();
 		
 		Integer i = 1;		
 		
 		if(x == c1.getX()){
 			while(it.hasNext() && ok){
 				++i;
 				Coordinate tmpC = it.next();
 				ok = (tmpC.getX()== x) && (tmpC.getY()== y+i);				
 			}
 		}
 		else if(y == c1.getY()){
 			while(it.hasNext() && ok){
 				++i;
 				Coordinate tmpC = it.next();
 				ok = (tmpC.getY()== y) && (tmpC.getX()== x+i);				
 			}
 		}
 		else
 			ok = false;
 		
 		return ok;	
 	}
 }
