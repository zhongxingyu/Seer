 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 
 // ########### What does this guy do?
 /**
  * Talk about it here!
 **/
 public class FilterCreator {
 	String[] commandList;
 	DatabaseConnector dbConnector;
 	int filterID;
 	private HashMap<String, Integer> operatorList;
 	
 	public FilterCreator(String filterName, String[] commandList) throws ClassNotFoundException, SQLException {
 		this.commandList = commandList;
 		dbConnector = new DatabaseConnector();
 		FilterComparison operations = new FilterComparison();
 		this.operatorList = operations.getOperatorList();
 		
 		this.filterID = dbConnector.createFilter(filterName);
 	}
 
 	protected void uploadEntries() throws SQLException {
 		for (int i = 0; i < this.commandList.length; i++) {
 			parseCommand(i);
 		}
 	}
 	
 	private void parseCommand(int index) throws SQLException {
 		String currentCommand = this.commandList[index];
 		ArrayList<String> infoNames = dbConnector.getAllEntryTableNames();
 		ArrayList<String> genoNames = dbConnector.getGenotypeTableNames();
 		ArrayList<String> indNames = new ArrayList<String>(Arrays.asList("ind", "IND", "Ind", "individual", "in"));
 		ArrayList<String> entryNames = new ArrayList<String>(Arrays.asList("entry", "ENT", "ent"));
 		ArrayList<String> optionNames = new ArrayList<String>(Arrays.asList("option", "opt", "options"));
 		
 		for (String key : this.operatorList.keySet()) {
 			if (currentCommand.contains(key) && !containedByOthers(key, currentCommand)) {
 				String[] arguments = currentCommand.split(key);
 				trimAllArguments(arguments);
 				String indicator = arguments[0].split(" ")[0];
 				String infoName = arguments[0].split(" ")[1];
 				
 				if (indNames.contains(indicator) && genoNames.contains(infoName)) {
 					if (key.contains("exists")) {
 						this.dbConnector.createFilterIndividual(this.filterID, this.operatorList.get(key), infoName, null); 
 					} else {
 						String[] operands = arguments[1].split(" ");
 						this.dbConnector.createFilterIndividual(this.filterID, this.operatorList.get(key), infoName, operands);
 					}
 				} else if (entryNames.contains(indicator) && infoNames.contains(infoName)) {
 					if (key.contains("exists")) {
 						this.dbConnector.createFilterEntry(this.filterID, this.operatorList.get(key), infoName, null);
 					} else {
 						String[] operands = arguments[1].split(" ");
 						this.dbConnector.createFilterEntry(this.filterID, this.operatorList.get(key), infoName, operands);
 					}
 				} else if (optionNames.contains(indicator)) {
 					if (!this.operatorList.get(key).equals(4)) {
 						System.out.println("The comparator'" + key + "' is not valid for an option.");
 						return;
 					} else {
						this.dbConnector.createOption(this.filterID, infoName);
 					}
 				}
 			}
 		}
 	}
 
 	private void trimAllArguments(String[] arguments) {
 		for (int i = 0; i < arguments.length; i++) { 
 			arguments[i] = arguments[i].trim(); 
 		}
 	}
 	
 	private boolean containedByOthers(String key, String currentCommand) {
 		for (String checkKey : this.operatorList.keySet()) {
 			if (!checkKey.equals(key) && currentCommand.contains(checkKey) && checkKey.contains(key)) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 }
