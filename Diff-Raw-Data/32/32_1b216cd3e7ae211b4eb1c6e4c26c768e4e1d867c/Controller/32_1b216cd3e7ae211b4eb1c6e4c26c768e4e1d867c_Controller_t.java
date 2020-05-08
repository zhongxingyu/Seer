 package Trains.Controllers;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Scanner;
 import java.util.regex.Pattern;
 
 import Trains.Database.*;
 import Trains.GUI.FileChoose;
 import Trains.GUI.GUI;
 import Trains.GUI.GetNodes;
import Trains.GUI.SelectSolutionType;
 
 /**
  * Controller for the Trains.Database class to interface with the Trains.GUI.
  * @author Andrew Donley
  */
 public class Controller {
 	
 	// Database Instance
 	private Database database = Database.getInstance();
 	
 	private final static Controller controllerInstance = new Controller();
 	
 	private GetNodes get;
 	private FileChoose file;
	private SelectSolutionType select;
 	
 	private Controller() { }
 	
 	public static Controller getInstance() {
 		return controllerInstance;
 	}
 	
 	public void start() {
 		GUI.createAndShowGUI();
 		file = new FileChoose();
 		get = new GetNodes();
		select = new SelectSolutionType();
 		get.show();
 	}
 	
 	public void ChangeToFileChooser() {
 		//file = new FileChoose();
 		file.show();
 	}
 	
 	public void ChangeToGetNodes() {
 		get.show();
 	}
 	
	public void ChangeToSelectSolutionType() {
		select.show();
	}
	
 	/**
 	 * Scans a string, puts it into the Model while making sure 
 	 * the string is in the correct format
 	 * @param input the string to be scanned
 	 * @return <code>null</code> when the input is scanned and put into the model. <code>String</code>
 	 * with the values that were incorrect input containing a 'train stop' not in the correct format.
 	 */
	// TODO make this return String, for display working correctly, and pass back what went wrong if it doesnt
 	public boolean parseString(String input) {
 		// Get rid of all the spaces in the string
 		input = input.replaceAll(" ","");
 		Scanner scan = new Scanner(input);
 		
 		// Delineate by commas and new lines
 		Pattern pattern = Pattern.compile(",|\n");
 		scan.useDelimiter(pattern);
 		boolean badInput = false;
 		String temp;
 		
 		// Scanner automatically commas and newline chars
 		while(scan.hasNext()){
 			temp = scan.next();
 			// Check to see if there are two letters followed by an int
 			if(Character.isLetter(temp.charAt(0)) && Character.isLetter(temp.charAt(1)) && Character.isDigit(temp.charAt(2))
 					&& temp.length() == 3) {
 				// Make sure everything is in uppercase here
 				temp = temp.toUpperCase();
 				System.out.print("Good ");
 				database.add(temp.charAt(0), temp.charAt(1), Character.getNumericValue(temp.charAt(2)));
 			}
 			else {
 				badInput = true;
 				System.out.print("Bad ");
 				// TODO Reset database, and tell user that input was bad
 			}
 		}
 		
		if(!badInput) {
			database.print();
			
		}
 		
 		return (!badInput);
 	}
 	
 	public boolean readFile(String file) throws IOException {
 		String contents = "", temp = "";
 		BufferedReader reader = new BufferedReader(new FileReader(file));
 		while((temp = reader.readLine()) != null)
 			contents = contents + temp + ", ";
 		
 		reader.close();
 		System.out.print(contents);
 		return parseString(contents);
 	}
 
 }
