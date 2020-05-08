 package cpsc310.server;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.TreeMap;
 
 import au.com.bytecode.opencsv.CSVParser;
 import java.util.HashMap;
 import java.util.regex.*;
 
 public class FileParser {
 
 	public FileParser() {
 	}
 
 	/**
 	 * Method to parse the lines of the house data csv file and store the
 	 * information into an ArrayList<HouseDataPoint> object.
 	 * 
 	 * @pre rawFile != null;
 	 * @post true;
 	 * @param rawFile
 	 *            - a List<String> containing the lines of the .csv file.
 	 * @return an ArrayList<HouseDataPoint> containing the information parsed
 	 *         from the .csv file.
 	 */
 	public ArrayList<HouseDataPoint> parseData(List<String> rawFile) {
 
 		ArrayList<HouseDataPoint> houseOutput = null;
 		CSVParser parser = new CSVParser();
 
 		// create iterator to iterate through all lines contained in the .csv
 		// file
 		Iterator<String> itr = rawFile.iterator();
 		String currentLine = itr.next();
 		String[] header;
 		try {
 			// grab the values of the title for each column
 			header = parser.parseLine(currentLine);
 			// stores PIDs to check for duplicates
 			HashMap<String, HouseDataPoint> houseIDs = new HashMap<String, HouseDataPoint>();
 			while (itr.hasNext()) {
 				// store the values of each house into a HashMap to create
 				// HouseDataPoint objects
 				HashMap<String, String> currentHouse = new HashMap<String, String>();
 				String[] currentParsedLine = parser.parseLine(itr.next());
 				for (int j = 0; j < currentParsedLine.length; j++) {
 					currentHouse.put(header[j], currentParsedLine[j]);
 				}
 				// check for duplicates
 				String currentHouseID = currentHouse.get("TO_CIVIC_NUMBER")
 						+ " " + currentHouse.get("STREET_NAME");
 				if (validateLine(currentHouse)) {
 					int currentLandValue = Integer.parseInt(currentHouse
							.get("CURRENT_LAND_VALUE").replaceAll("\\..+$", ""));
 					if (houseIDs.containsKey(currentHouseID)) {
 						if (houseIDs.get(currentHouseID).getCurrentLandValue() < currentLandValue) {
 							houseIDs.put(currentHouseID, new HouseDataPoint(
 									currentHouse));
 						}
 						houseIDs.put(currentHouseID, new HouseDataPoint(
 								currentHouse));
 					} else {
 						houseIDs.put(currentHouseID, new HouseDataPoint(
 								currentHouse));
 					}
 				}
 			}
 			houseOutput = convertToArrayList(houseIDs);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return houseOutput;
 	}
 
 	/**
 	 * @pre a hashmap parsed from file representing a single line
 	 * @post a boolean to whether current line in file is valid
 	 * @param currentHouse
 	 * @return boolean
 	 */
 	private boolean validateLine(HashMap<String, String> currentHouse) {
 		if ((!currentHouse.get("STREET_NAME").equals(""))
 				&& (!currentHouse.get("TO_CIVIC_NUMBER").equals(""))
 				&& (!currentHouse.get("CURRENT_LAND_VALUE").equals(""))) {
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * helper  method
 	 * converts generic list object to arraylist
 	 * @TODO replace/remove? should have function parseData return HashMap?
 	 */
 	private ArrayList<HouseDataPoint> convertToArrayList(HashMap<String, HouseDataPoint> currentHouses) {
 		Iterator<HouseDataPoint> itr = currentHouses.values().iterator();
 		ArrayList<HouseDataPoint> houseList = new ArrayList<HouseDataPoint>();
 		while(itr.hasNext())
 		{
 			houseList.add(itr.next());
 		}
 		return houseList;
 	}
 }
