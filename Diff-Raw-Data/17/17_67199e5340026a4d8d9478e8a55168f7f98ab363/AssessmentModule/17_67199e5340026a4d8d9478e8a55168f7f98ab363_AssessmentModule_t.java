 package com.ijg.darklightnova.core;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class AssessmentModule {
 	private Engine engine;
 	
 	// What is returned with toString()
 	final protected String name = "INSERT IMAGE NAME HERE";
 	
 	// Total issues
 	public double total;
 	
 	// List of all fixed issues
 	public ArrayList<Issue> issues = new ArrayList<Issue>();
 	
 	// List of the scoring modules
 	public ArrayList<ScoreModule> modules = new ArrayList<ScoreModule>();
 	
 	public AssessmentModule(Engine engine) {
 		this.engine = engine;
 		/*
 		 * add scoring modules
 		 * to the vulnerability list
 		 * here.
 		 * Example:
 		modules.add(new ExampleScoringModule());*/
 		
 		for (ScoreModule m : modules) {
 			total += m.getIssueCount();
 		}
 	}
 	
 	public void assess() {
 		// Make any modifications necessary to the issues list
 		boolean changed = false;
 		for (ScoreModule module : modules) {
 			ArrayList<Issue> modIssues = module.check();
 			for (Issue issue : modIssues) {
 				if (issue.fixed && !issues.contains(issue)) {
 					issues.add(issue);
 					changed = true;
 				} else if (!issue.fixed && issues.contains(issue)) {
 					issues.remove(issue);
 					changed = true;
 				}
 			}
 		}
 		// If the issues list is changed, write a new progress file, and send progress to server
 		if (changed == true) {
 			engine.writeFoundList();
 			
 			// Build hash to be converted to JSON for the API
			HashMap<String, String> issuesMap = generateIssuesHashMap();
 			
 			// If there is not a valid session key try to get one
 			if (engine.SESSION_KEY == null || engine.SESSION_KEY == "") {
 				engine.authUser();
 			}
 			// Send update to server
			engine.sendUpdate(issues.size(), issuesMap);
 		}
 		
 	}
 	
	public HashMap<String, String> generateIssuesHashMap() {
		HashMap<String, String> issuesMap = new HashMap<String, String>();
		for (Issue issue : issues) {
			issuesMap.put(issue.name, issue.description);
		}
		return issuesMap;
	}
	
 	public String toString() {
 		return name;
 	}
 }
