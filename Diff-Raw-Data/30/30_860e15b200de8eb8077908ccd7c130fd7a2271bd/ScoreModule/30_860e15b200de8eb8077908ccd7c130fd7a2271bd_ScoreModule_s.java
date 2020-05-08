 package com.ijg.darklightnova.core;
 
 import java.util.ArrayList;
 
 public abstract class ScoreModule {
 	/*
 	 * All scoring modules are subclassed from
 	 * this
 	 */
 	protected ArrayList<Issue> issues = new ArrayList<Issue>();
 	public abstract ArrayList<Issue> check();
 		/*
 		 * Run private methods to check the status
 		 * of the issues, those whose checks returned
 		 * true but have the fixed boolean as false
 		 * will be added to the returned list, and
 		 * fixed will be switched to true
 		 * 
 		 * Issues whose checks return false but fixed
 		 * booleans are true will be added to the return
 		 * list, with their fixed boolean changed to false
 		 * 
 		 * For example:
 		 * if (issueFound()) {
 		 *     add(issues, issue);
 		 * else {
 		 *     remove(issues, issue);
 		 * 
 		 * (see add and remove functions)
 		 * See com.ijg.darklightnova.modules.ExampleScoringModule
 		 * 
 		 * Then the AssessmentModule will handle, based on
 		 * all of the issues returned and the status of their
 		 * fixed boolean what is going on (lost a point, gained
 		 * points, etc)
 		 * 
 		 * Is there a better way to do this?
 		 */
	
	// TODO: escalation?
	/*
	 * (See ExampleScoringModule)
	 * Nothing is ever removed from the issues ArrayList,
	 * only added. Initially in the constructor, and then 
	 * every subsequent check all issues are added again
	 * (see below). This will cause issues to have loads
	 * of conflicting issue instances(?) Should keep old 
	 * comparison check to validate the addition or removal
	 * of an issue instance.
	 */
 	protected void add(Issue issue) {
 		if (!issue.fixed) issue.fixed = true;
 		issues.add(issue);
 	}
 	
 	protected void remove(Issue issue) {
 		if (issue.fixed) issue.fixed = false;
 		issues.add(issue);
 	}
	// End escalation
 	
 	public int getIssueCount() {
 		return issues.size();
 	}
 }
