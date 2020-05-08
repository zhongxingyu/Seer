 package me.sd5.mcbetaterraingenerator;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import me.sd5.mcbetaterraingenerator.exceptions.InvalidInputException;
 
 /**
  * @author sd5
  * Main class. Takes user input and tells the generator which areas it has to generate.
  */
 public class MCBetaTerrainGenerator {
 	
 	//The constant names of the files in the jar.
 	public static final String jar_levelDat_b173 = "level_beta_1.7.3.dat";
 	public static final String jar_mcserver_b173 = "minecraft_server_beta_1.7.3.jar";
 	public static final String jar_mcserver_f152 = "minecraft_server_final_1.5.2.jar";
 	public static final String jar_serverproperties_b173 = "server_beta_1.7.3.properties";
 	
 	//The constant names of the files on the hard disk.
 	public static final String levelDat_b173 = "level.dat";
 	public static final String mcserver_b173 = "minecraft_server.jar";
 	public static final String mcserver_f152 = "minecraft_server.jar";
 	public static final String serverproperties_b173 = "server.properties";
 	
 	//The constant paths.
 	public static final String mainDir = "mcBetaTerrainGenerator";
 	public static final String genDir = mainDir + File.separator + "generation";
 	public static final String conDir = mainDir + File.separator + "conversion";
 	public static final String endDir = mainDir + File.separator + "finished";
 	public static final String genDirWorld = genDir + File.separator + "world";
 	public static final String genDirRegion = genDirWorld + File.separator + "region";
 	public static final String conDirWorld = conDir + File.separator + "world";
 	public static final String conDirRegion = conDirWorld + File.separator + "region";
 			
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
 		ArrayList<Area> areas = null;
 		try {
 			 areas = aip.parseInput(areasInput);
 		} catch (InvalidInputException e) {
 			e.printStackTrace();
 		}
 		
 		//Remove old files.
 		Util.deleteFile(mainDir);
 		
 		//Create the needed files.
 		try {
 			Util.copyFileFromJar(jar_mcserver_b173, genDir + File.separator + mcserver_b173);
 			Util.copyFileFromJar(jar_mcserver_f152, conDir + File.separator + mcserver_f152);
 			Util.copyFileFromJar(jar_serverproperties_b173, genDir + File.separator + serverproperties_b173);
 			Util.copyFileFromJar(jar_levelDat_b173, genDirWorld + File.separator + levelDat_b173);
 		} catch(FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		//Set up a new generator with the user's seed.
 		Generator generator = new Generator(seedInput);
 		for(Area area : areas) {
 			generator.generate(area);
 		}
 		
 		//Automatically converts the .mcr files to .mca
 		Converter converter = new Converter();
 		converter.convert(areas);
 
 	}
 
 }
