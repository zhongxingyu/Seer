 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 
 public class FilterCreator {
 	String[] commandList;
 	DatabaseConnector dbConnector;
 	int filterID;
 	private HashMap<String, Integer> operatorList;
 	
 	public FilterCreator(String filterName, String[] commandList) throws ClassNotFoundException, SQLException {
 		this.commandList = commandList;
 		dbConnector = new DatabaseConnector();
 		fillOperatorList();
 		
 		this.filterID = dbConnector.createFilter(filterName);
 	}
 
 	protected void uploadEntries() throws SQLException {
		System.out.println(this.commandList.length);
 		for (int i = 0; i < this.commandList.length; i++) {
 			parseCommand(i);
 		}
 	}
 	
 	private void parseCommand(int index) throws SQLException {
 		String currentCommand = this.commandList[index];
 		ArrayList<String> infoNames = dbConnector.getInfoTableNames();
 		
		for (String info : infoNames) {
			System.out.println(info);
		}
		
 		for (String key : this.operatorList.keySet()) {
 			if (currentCommand.contains(key)) {
 				String[] arguments = currentCommand.split(key);
				for (String arg : arguments) { arg = arg.trim(); }
 				String[] operands = arguments[1].split(" ");
 				if (infoNames.contains(arguments[0])) {
 					dbConnector.createFilterEntry(this.filterID, this.operatorList.get(key), arguments[0], operands);
 				}
 			}
 		}
 	}
 	
 	private void fillOperatorList() {
 		this.operatorList = new HashMap<String, Integer>();
 		this.operatorList.put("<", 0);
 		this.operatorList.put("less than", 0);
 		this.operatorList.put(">", 1);
 		this.operatorList.put("greater than", 1);
 		this.operatorList.put("<=", 2);
 		this.operatorList.put(">=", 3);
 		this.operatorList.put("=", 4);
 		this.operatorList.put("equal to", 4);
 		this.operatorList.put("between", 5);
 	}
 	
 	
 }
