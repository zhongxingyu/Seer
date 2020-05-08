 package solutionEvaluator;
 
 import sokoban.*;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.StringReader;
 
 /**
  * 
  * Class to simulate Kattis and evaluate our sokoban solver.
  * 
  * @author Ariel Ekgren
  *
  */
 
 public class SolutionEvaluator {
 
 	public SolutionEvaluator() throws IOException{
 		System.out.println("Initializing evaluation.");
 		long startTime = System.currentTimeMillis();
 		
		//String filePath = "./sokoban/res/00_sample.in";
		String filePath = "./sokoban/res/TestTrack.in";
 
 		
 		// Initializing sokoban solver object with map from file.
 		Sokoban path = new Sokoban(new FileReader(filePath));
 		
 		System.out.println((startTime - System.currentTimeMillis()));
 	}
 	
 	public static void main(String[] args) throws IOException{
 		SolutionEvaluator MapEval = new SolutionEvaluator();
 	}
 }
