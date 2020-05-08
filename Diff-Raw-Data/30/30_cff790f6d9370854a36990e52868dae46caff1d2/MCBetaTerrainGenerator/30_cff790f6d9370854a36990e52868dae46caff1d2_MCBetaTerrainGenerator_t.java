 package me.sd5.mcbetaterraingenerator;
 
 import java.util.ArrayList;
 import java.util.Scanner;
 
import me.sd5.mcbetaterraingenerator.exceptions.InvalidInputException;
 
 /**
  * @author sd5
  * Main class. Takes user input and tells the generator which areas it has to generate.
  */
 public class MCBetaTerrainGenerator {
 	
 	public static void main(String[] args) {
 		
 		//A scanner to read the user's input.
 		Scanner in = new Scanner(System.in);
 		
 		String areasInput;
 		String seedInput;
 		
 		//Ask the user for the areas to generate.
 		System.out.println("Areas to generate: ");
 		System.out.println("Example: -4,-4,3,3;0,4,6,4");
 		System.out.print("Input: ");
 		areasInput = in.nextLine();
 		
 		//Ask the user for a seed to generate with.
 		System.out.println("");
 		System.out.println("Seed for terrain generation:");
 		System.out.println("Leave blank for random seed.");
 		System.out.print("Input: ");
 		seedInput = in.nextLine();
 		
 		//Close the scanner, it is no longer needed.
 		in.close();
 		
 		//Try to parse the user input into the regions which will be generated.
 		AreaInputParser aip = new AreaInputParser(";", ",");
 		ArrayList<Area> areas;
 		try {
 			 areas = aip.parseInput(areasInput);
 		} catch (InvalidInputException e) {
 			e.printStackTrace();
 		}
 		
 		//Set up a new generator with the user's seed.
 		Generator generator = new Generator(seedInput);
 
 	}
 
 }
