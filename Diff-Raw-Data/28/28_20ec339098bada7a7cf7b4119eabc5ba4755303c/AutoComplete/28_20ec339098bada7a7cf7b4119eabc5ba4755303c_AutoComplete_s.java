 package controllers;
 
 import java.util.List;
 
 import models.CandidateContributions;
 import play.mvc.Controller;
 
 public class AutoComplete extends Controller {
 	public static void candidates() {
		List<String> candidatesNames = CandidateContributions.getCandidatesNames();
		candidatesNames.add("All");
		candidatesNames.add("All Current");
		renderJSON(candidatesNames);
 	}
 	
 	public static void companies() {
 		renderJSON(CandidateContributions.getCompaniesNames());
 	}
 	
 	public static void currentCandidates() {
 		renderJSON(CandidateContributions.getCurrentCandidates());	
 	}
 }
