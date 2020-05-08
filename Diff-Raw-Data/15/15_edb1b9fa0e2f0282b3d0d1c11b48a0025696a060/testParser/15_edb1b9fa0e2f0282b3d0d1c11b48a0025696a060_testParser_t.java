 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Scanner;
 import java.util.ArrayList;
 
 /**
  * Priority Queue Test Parser
 	@author Tom Ravenscroft
 	@version version
  */
 
public class testParser{
 	/**
 	 * Constructor - Do nothing.
 	 */
 	public testParser(){}
 	
 	/**
 	 * Parse File
 	@param file_path Path the a test file that contains process records in the format: [name, creation, priority, age, burst time].
 	@return 6xN dimension array containing records representing the input file.
 	 */
 	private <proc>ArrayList parseTests(String file_path){
 		
 		ArrayList record = new ArrayList();
 		
 		File file = new File(file_path);
 
 		try {
 
 			// Create a new Scanner object which will read the data 
 			// from the file passed in. To check if there are more 
 			// line to read from it we check by calling the 
 			// scanner.hasNextLine() method. We then read line one 
 			// by one till all line is read.
 
 			Scanner scanner = new Scanner(file);
 			
 			while (scanner.hasNextLine()) {
 
 				String line = scanner.nextLine();
 				System.out.println(line);
 			}
 		} 
 			catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 }
