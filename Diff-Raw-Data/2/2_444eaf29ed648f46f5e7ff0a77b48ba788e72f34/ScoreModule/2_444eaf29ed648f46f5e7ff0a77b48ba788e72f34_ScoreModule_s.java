 package com.ijg.darklight.core;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 /**
  * Superclass for scoring modules
  * @author Isaac Grant
  * @author Lucas Nicodemus
  * @version .1
  *
  */
 
 public abstract class ScoreModule {
 
 	protected ArrayList<Issue> issues = new ArrayList<Issue>();
 	public abstract ArrayList<Issue> check();
		 
 	/**
 	 * Add a fixed issue to the list of fixed issues
 	 * @param issue An issue that has been fixed
 	 */
 	protected void add(Issue issue) {
 		purgeIssue(issue);
 		if (!issue.getFixed()) issue.setFixed(true);
 		issues.add(issue);
 	}
 	
 	/**
 	 * Remove an unfixed issue from the list of fixed issues
 	 * @param issue An issue that has not been fixed
 	 */
 	protected void remove(Issue issue) {
 		purgeIssue(issue);
 		if (issue.getFixed()) issue.setFixed(false);
 		issues.add(issue);
 	}
 	
 	/**
 	 * Purge the issue from the list of fixed issues
 	 * @param issue The issue to purge
 	 */
 	protected void purgeIssue(Issue issue) {
 		Iterator<Issue> i = issues.iterator();
 		
 		while (i.hasNext()) {
 			Issue oldIssue = (Issue) i.next();
 			if (oldIssue.getName().equals(issue.getName())) {
 				i.remove();
 			}
 		}
 	}
 	
 	/**
 	 * Get the number of issues the module manages
 	 * @return The number of issues the module manages 
 	 */
 	public int getIssueCount() {
 		return issues.size();
 	}
 }
