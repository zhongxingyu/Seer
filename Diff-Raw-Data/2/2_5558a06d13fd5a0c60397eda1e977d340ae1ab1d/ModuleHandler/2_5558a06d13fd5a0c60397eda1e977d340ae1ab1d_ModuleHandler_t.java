 package com.ijg.darklight.sdk.core;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import me.shanked.nicatronTg.darklight.view.VulnerabilityOutput;
 
 public class ModuleHandler {
 	private CoreEngine engine;
 	
 	private double total;
 	
 	private ArrayList<Issue> issues = new ArrayList<Issue>();
 	private ArrayList<ScoreModule> modules;
 	
 	private VulnerabilityOutput outputManager;
 	
 	public ModuleHandler(CoreEngine engine, ArrayList<ScoreModule> loadedModules) {
 		this.engine = engine;
 		outputManager = new VulnerabilityOutput(this);
 		
 		modules = loadedModules;
 		for (ScoreModule module : modules) {
 			total += module.getIssueCount();
 		}
 	}
 	
 	/**
 	 * Transfer all the fixed issues from the array list to a hash map
 	 * @return A HashMap of all fixed issues
 	 */
 	public HashMap<String, String> getFixedIssues() {
 		HashMap<String, String> issuesMap = new HashMap<String, String>();
 		for (Issue issue : issues) {
 			issuesMap.put(issue.getName(), issue.getDescription());
 		}
 		return issuesMap;
 	}
 	
 	/**
 	 * Check for what issues have been fixed and write a new output file if
 	 * any issues' fixed status has changed
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
 			engine.sendUpdate();
 		}
 	}
 	
 	/**
 	 * Get array list of fixed issues
 	 * @return ArrayList of fixed issues
 	 */
 	public ArrayList<Issue> getIssues() {
 		return issues;
 	}
 	
 	/**
 	 * Get the number of total issues
 	 * @return The number of total issues
 	 */
 	public int getTotalIssueCount() {
 		return (int) total;
 	}
 	
 	/**
 	 * Get the number of fixed issues
 	 * @return The number of fixed issues
 	 */
 	public int getFixedIssueCount() {
 		return issues.size();
 	}
 	
 	/**
 	 * Get the percentage of issues fixed
 	 * @return The percentage of issues fixed
 	 */
 	public String getFixedIssuePercent() {
		return "" + Math.round((issues.size() / total) * 100) + "%";
 	}
 }
