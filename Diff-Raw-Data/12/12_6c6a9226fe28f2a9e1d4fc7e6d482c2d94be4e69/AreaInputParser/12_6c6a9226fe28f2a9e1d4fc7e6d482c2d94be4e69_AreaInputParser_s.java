 package me.sd5.mcbetaterraingenerator;
 
 import java.util.ArrayList;
import java.util.List;
 
 import me.sd5.mcbetaterraingenerator.exceptions.InvalidInputException;
 
 /**
  * @author sd5
  * Parses the user input and lists all regions which will be generated.
  */
 public class AreaInputParser {
 
 	private String areaSeparator;
 	private String coordinateSeparator;
 	
 	/**
 	 * Creates a new input parser.
 	 * @param areaSeparator The char which separates the areas.
 	 * @param coordinateSeparator The char which separates the coordinates of the two regions which define the area.
 	 */
 	public AreaInputParser(String areaSeparator, String coordinateSeparator) {
 		
 		this.areaSeparator = areaSeparator;
 		this.coordinateSeparator = coordinateSeparator;
 		
 	}
 	
 	/**
 	 * Parses the input using the separators defined in the constructor.
 	 * @param input The input of the user.
 	 * @return A list of all the areas defined by the user.
 	 * @throws InvalidInputException
 	 */
 	public ArrayList<Area> parseInput(String input) throws InvalidInputException {
 		
 		ArrayList<Area> areas = new ArrayList<Area>();
 		
 		//Split the string into the different areas.
 		String[] areaRectangles = input.split(areaSeparator);
 		
 		for(String rectangle : areaRectangles) {
 			//Split every area into the defining coordinates.
 			String[] areaCoordinates = rectangle.split(coordinateSeparator);
 			
 			//Check if the current area is defined by four coordinates.
 			if(areaCoordinates.length != 4) throw new InvalidInputException("Tried to define an area with " + areaCoordinates.length + " instead of 4 vertices!");
 			
 			//Try to convert the input into coordinates and throw an exception if this fails.
 			int x1, z1, x2, z2;
 			try {
 				x1 = Integer.parseInt(areaCoordinates[0].trim());
 				z1 = Integer.parseInt(areaCoordinates[1].trim());
 				x2 = Integer.parseInt(areaCoordinates[2].trim());
 				z2 = Integer.parseInt(areaCoordinates[3].trim());
 			} catch(NumberFormatException e) {
 				throw new InvalidInputException("Could not read a coordinate of an area!");
 			}
 			areas.add(new Area(x1, z1, x2, z2));
 			
 		}
 		
 		return areas;
 		
 	}
 	
 }
