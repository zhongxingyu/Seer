 package com.ijg.darklight.sdk.core;
 
 import java.util.HashMap;
 
 /*
  * Copyright (C) 2013  Isaac Grant
  * 
  * This file is part of the Darklight Nova Core.
  *  
  * Darklight Nova Core is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Darklight Nova Core is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with the Darklight Nova Core.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * Handles issues
  * 
  * @author Isaac Grant
  *
  */
 
 public class IssueHandler {
 	private HashMap<String, String> fixedIssues = new HashMap<String, String>();
 	private Issue[] issues;
 	
 	/**
 	 * @param loadedIssues Issues to monitor
 	 */
 	public IssueHandler(Issue[] loadedIssues) {
 		issues = loadedIssues;
 	}
 	
 	/**
 	 * Check for the status of all the monitored issues
 	 */
 	public void checkAllIssues() {
 		for (Issue issue : issues) {
 			if (issue.isFixed()) {
 				if (!fixedIssues.containsKey(issue.getName())) {
 					fixedIssues.put(issue.getName(), issue.getDescription());
 				}
 			} else {
 				if (fixedIssues.containsKey(issue.getName())) {
 					fixedIssues.remove(issue.getName());
 				}
 			}
 		}
 	}
 	
 	/**
 	 * @return HashMap of fixed issues, key is issue name, value is issue description
 	 */
 	public HashMap<String, String> getFixedIssues() {
 		return fixedIssues;
 	}
 	
 	/**
 	 * @return Total number of issues
 	 */
 	public int getTotalIssueCount() {
		return issues.length | 0;
 	}
 	
 	/**
 	 * @return Number of fixed issues
 	 */
 	public int getFixedIssueCount() {
 		return fixedIssues.size();
 	}
 	
 	/**
 	 * @return The percentage of issues fixed as a string
 	 */
 	public String getFixedIssuePercent() {
 		return "" + Math.round((fixedIssues.size() / ((double) issues.length) * 100)) + "%";
 	}
 }
