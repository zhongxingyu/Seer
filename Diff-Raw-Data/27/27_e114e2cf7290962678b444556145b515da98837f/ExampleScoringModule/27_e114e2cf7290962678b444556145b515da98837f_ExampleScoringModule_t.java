 package com.ijg.darklightnova.modules;
 
 import java.util.ArrayList;
 
 import com.ijg.darklightnova.core.Issue;
 import com.ijg.darklightnova.core.ScoreModule;
 
 public class ExampleScoringModule extends ScoreModule {
 	/*
 	 * This is an example scoring module
 	 */
 	
 	// static vulnerability declarations
 	private Issue exampleIssue = new Issue("Example Vulnerability", "This is an example vulnerability.");
 	
 	public ExampleScoringModule() {
 	}
 	
 	private boolean fixedExampleVulnerability() {
 		/*
 		 * Private methods are used to check whether
 		 * or not the vulnerability has been found, usually
 		 * they are booleans, though sometimes other
 		 * return types are warranted.
 		 */
		return true;
 	}
 	
 	public ArrayList<Issue> check() {
 		ArrayList<Issue> issues = new ArrayList<Issue>();
 		if (fixedExampleVulnerability()) {
 			add(issues, exampleIssue);
 		} else {
 			remove(issues, exampleIssue);
 		}
 		return issues;
 	}
 }
