package com.pennapps.apbro;

 import java.util.Scanner;
 import java.io.File;
 import java.io.FileNotFoundException;
 
 
 public class StatParser {
 	
 	// Instance Variables
 	private String user;
 	private File rawData;
 	private String[] userData;
 	
 	public StatParser(String user, File rawData) {
 		this.user = user;
 		//TODO: get data
 		this.rawData = rawData;
 		this.userData = parseUserData(parseFile(rawData));
 	}
 	
 	private String parseFile(File spreadsheetData) {
 		String data = "";
 		try {
 			Scanner fileScan = new Scanner(spreadsheetData);
 			// Read each line of the file.
 			while (fileScan.hasNextLine()) {
 				String currentLine = fileScan.nextLine();
 				Scanner lineScan = new Scanner(currentLine);
 				
 				// Get name from line and check if this is the user we want.
 				String lastName = lineScan.next(),
 					   firstName = lineScan.next();	
 				if (firstName + " " +  lastName == this.user){
 					lineScan.close();
 					data = currentLine;
 					break;
 				}
 				lineScan.close();
 			}
 			fileScan.close();
 		} catch (FileNotFoundException e) {
 			
 		}
 		return data;
 	}
 	
 	
 	private String[] parseUserData(String userData) {
 		if (userData == "") return null;
 		
 		String[] _userData = new String[11];
 		Scanner userDataParser = new Scanner(userData);
 		// Set first element as name.
 		_userData[0] = userDataParser.next() + userDataParser.next();
 		// Populate rest of data array.
 		for (int i = 1; i < _userData.length; ++i) {
 			if (userDataParser.hasNext())
 				_userData[i] = userDataParser.next();
 		}
 		// Close Scanner.
 		userDataParser.close();
 		return _userData;
 	}
 	
 	/* KVC */
 	public String[] getUserData() {
 		return this.userData;
 	}
 }
