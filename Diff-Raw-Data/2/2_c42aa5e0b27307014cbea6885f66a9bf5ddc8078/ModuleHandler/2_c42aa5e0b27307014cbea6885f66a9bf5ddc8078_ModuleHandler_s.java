 package com.ijg.darklight.core;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import me.shanked.nicatronTg.darklight.view.VulnerabilityOutput;
 
 import com.ijg.darklight.core.loader.ModuleLoader;
 
 /**
  * Handles the scoring modules
  * @author Isaac Grant
  * @author Lucas Nicodemus
  * @version .1
  * @see com.ijg.darklight.core.ScoreModule
  */
 
 public class ModuleHandler {
 	private Engine engine;
 	
 	private double total;
 	
 	private ArrayList<Issue> issues = new ArrayList<Issue>();
 	private ArrayList<ScoreModule> modules = new ArrayList<ScoreModule>();
 	
 	private VulnerabilityOutput outputManager;
 	
 	/**
 	 * 
 	 * @param engine The instance of engine to which this module handler will belong
 	 */
 	public ModuleHandler(Engine engine) {
 		this.engine = engine;
 		outputManager = new VulnerabilityOutput(this);
 		
 		ScoreModule[] loadedModules;
 		try {
 			loadedModules = ModuleLoader.loadAllModules();
 			for (ScoreModule loadedModule : loadedModules) {
 				modules.add(loadedModule);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
			System.exit(2); //Trigger error code to indicate a file not found
 		}
 		
 		for (ScoreModule module : modules) {
 			total += module.getIssueCount();
 		}
 	}
 	
 	/**
 	 * Generate a hash map of fixed issues
 	 * @return A hash map of fixed issues
 	 */
 	public HashMap<String, String> getFixedIssues() {
 		HashMap<String, String> issuesMap = new HashMap<String, String>();
 		for (Issue issue : issues) {
 			issuesMap.put(issue.getName(), issue.getDescription());
 		}
 		return issuesMap;
 	}
 	
 	/**
 	 * Update the status of all issues
 	 */
 	public void checkAllVulnerabilities() {
 		boolean changed = false;
 		for (ScoreModule module : modules) {
 			ArrayList<Issue> modifiedIssues = module.check();
 			for (Issue issue : modifiedIssues) {
 				if (issue.getFixed() && !issues.contains(issue)) {
 					issues.add(issue);
 					changed = true;
 				} else if (!issue.getFixed() && issues.contains(issue)) {
 					issues.remove(issue);
 					changed = true;
 				}
 			}
 		}
 		
 		if (changed) {
 			outputManager.writeNewOutput();
 			
 			engine.authUser();
 			engine.sendUpdate(issues.size(), getFixedIssues());
 		}
 	}
 	
 	/**
 	 * Get all fixed issues
 	 * @return All fixed issues
 	 */
 	public ArrayList<Issue> getIssues() {
 		return issues;
 	}
 	
 	/**
 	 * Get the total number of issues
 	 * @return The total number of issues
 	 */
 	public int getTotalIssueCount() {
 		return (int) total;
 	}
 	
 	/**
 	 * Get number of issues that have been fixed
 	 * @return Number of issues that have been fixed
 	 */
 	public int getFixedIssueCount() {
 		return issues.size();
 	}
 	
 	/**
 	 * Get the percentage of fixed issues
 	 * @return The percentage of fixed issues
 	 */
 	public String getFixedIssuePercent() {
 		return "" + ((int) (((double) issues.size()) / total) * 100) + "%";
 	}
 }
