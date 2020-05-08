 package main;
 
 import java.sql.Time;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 public class Statistics {
 	ArrayList<LogEntry> results;
 	private int numEntries;
 	private int numUsers;
 	private int numTools;
 	private int numMachines;
 	private long totalTimeLoggedIn;
 	private long avgTimeLoggedIn;
 	private ArrayList<String> users;
 	private int numLockedUsers;
 	private int numNotLoggedOut;
 	private Map<String, Integer> machineFrequencies;
 	private Map<String, Integer> toolsFrequencies;
 	
 	public Statistics() {
 		results = Log.getResults();
 		numEntries = 0;
 		numUsers = 0;
 		numTools = 0;
 		numMachines = 0;
 		totalTimeLoggedIn = 0;
 		avgTimeLoggedIn = 0;
 		users = new ArrayList<String>();
 		numLockedUsers = 0;
 		numNotLoggedOut = 0;
 		machineFrequencies = new HashMap<String, Integer>();
 		toolsFrequencies = new HashMap<String, Integer>();
 	}
 	
 	public void run() {
 		for (LogEntry entry : results) {
 			numEntries++;
 			userCheck(entry.getCwid());
 			if (entry.getTimeOut() != null)
 				determineLogInTime(entry.getTimeIn().getTime(), entry.getTimeOut().getTime());
 			machineCheck(entry.getMachinesUsed());
 			toolsCheck(entry.getToolsCheckedOut());
 			toolsCheck(entry.getToolsReturned());
 		}
 		determineAvgTime();
 	}
 	
 	private void userCheck(String cwid) {
 		if (!users.contains(cwid)) {
 			numUsers++;
 			users.add(cwid);
 		}
 	}
 	
 	private void determineLogInTime(Long start, Long end) {
 		totalTimeLoggedIn += (end - start);
 	}
 	
 	private void determineAvgTime() {
 		avgTimeLoggedIn = totalTimeLoggedIn / numEntries;
 	}
 	
 	private void machineCheck(ArrayList<Machine> machines) {
 		for (Machine m : machines) {
 			String name = m.getName() + " (" + m.getID() + ")";
 			if (!machineFrequencies.containsKey(name)) {
 				numMachines++;
 				machineFrequencies.put(name, 1);
 			} else {
 				machineFrequencies.put(name, machineFrequencies.get(name) + 1);
 			}
 		}
 	}
 	
 	private void toolsCheck(ArrayList<Tool> tools) {
 		for (Tool t : tools) {
 			String name = t.getName() + " (" + t.getUPC() + ")";
 			
 			if (!toolsFrequencies.containsKey(name)) {
 				numTools++;
 				toolsFrequencies.put(name, 1);
 			} else {
 				toolsFrequencies.put(name,  toolsFrequencies.get(name) + 1);
 			}
 		}
 	}
 	
 	//------------------------------GETTERS AND SETTERS------------------------------------
 
 	public ArrayList<LogEntry> getResults() {
 		return results;
 	}
 
 	public int getNumEntries() {
 		return numEntries;
 	}
 
 	public int getNumUsers() {
 		return numUsers;
 	}
 
 	public int getNumTools() {
 		return numTools;
 	}
 
 	public int getNumMachines() {
 		return numMachines;
 	}
 
 	public double getTotalTimeLoggedIn() {
 		return totalTimeLoggedIn;
 	}
 
 	public double getAvgTimeLoggedIn() {
 		return avgTimeLoggedIn;
 	}
 
 	public ArrayList<String> getUsers() {
 		return users;
 	}
 
 	public int getNumLockedUsers() {
 		return numLockedUsers;
 	}
 
 	public int getNumNotLoggedOut() {
 		return numNotLoggedOut;
 	}
 
 	public Map<String, Integer> getMachineFrequencies() {
 		return machineFrequencies;
 	}
 
 	public Map<String, Integer> getToolsFrequencies() {
 		return toolsFrequencies;
 	}
 }
