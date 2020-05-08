 package com.ijg.darklightnova.live;
 
 import java.util.ArrayList;
 
 import com.ijg.darklightnova.core.Issue;
 import com.ijg.darklightnova.core.ScoreModule;
 
 public class AssessmentModule {
 	// What is returned with toString()
 	final protected String name = "INSERT IMAGE NAME HERE";
 	
 	// Total issues
 	public double total;
 	
 	// List of all found issues
 	public ArrayList<Issue> issues = new ArrayList<Issue>();
 	
 	// List of the scoring modules used
 	public ArrayList<ScoreModule> modules = new ArrayList<ScoreModule>();
 	
 	public AssessmentModule() {
 		/*
 		 * add scoring modules
 		 * to the vulnerability list
 		 * here.
 		 * Example:
		modules.add(new ExampleScoreingModule(this));*/
 	}
 	
 	public void assess() {
 		// Clear the issues list, add all fixed issues
 		issues.clear();
 		for (ScoreModule module : modules) {
 			issues.addAll(module.check());
 		}
 	}
 	
 	public String toString() {
 		return name;
 	}
 }
