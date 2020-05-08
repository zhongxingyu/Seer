 package controllers;
 
 import java.util.Calendar;
 import java.util.List;
 
 import models.Enumeration;
 import models.Issue;
 import models.Project;
 import models.State;
 import models.Tracker;
 import models.User;
 import play.Logger;
 import play.data.validation.Valid;
 import play.data.validation.Validation;
 import play.mvc.*;
 
 public class Issues extends Main {
 
     
     @SuppressWarnings("unused")
 	@Before(only={"newIssue", "editIssue"})
     private static void loadCombos() {
     	renderArgs.put("trackers", Tracker.find("order by position asc").fetch());
     	renderArgs.put("users", User.find("login <> 'root' order by login").fetch());
     	renderArgs.put("states", State.find("order by position").fetch());
     	renderArgs.put("priorities", Enumeration.find("byType", Enumeration.ISSUE_PRIORITY_TYPE).fetch());    	
     }
 
     @SuppressWarnings("unused")
     @Before(only={"myIssues"})
     private static void checkAccess() {
 		if (Security.connected() == null)
 			try {
 				Secure.login();
 			} catch (Throwable e) {
 				Logger.error(e, "Error loading Login page");
 			}
     }
     
 	public static void newIssue(String identifier) {
 		Project project = Project.find("identifier", identifier).first();
 		Issue issue = new Issue(project);
 		render("@issue", issue);
 	}
 
     public static void editIssue(Long id) {
     	Issue issue = Issue.findById(id);
    	
    	if (! issue.project.isVisible()) {
    		render("@deny", issue);
    	} else {
    		render("@issue", issue);
    	}
 
     }
     
 
 	public static void issues(String identifier) {
 			Project project = Project.find("identifier", identifier).first();
 			List<Issue> issues = Issue.find("byProject", project).fetch();
 	    	render(issues, project);
 	    }
 
 	public static void myIssues() {
 		List<Issue> issues = Issue.find("assignee.login = ? ", Security.connected()).fetch();
 
 		render("@issues", issues);
 	}
 
 
 	public static void saveIssue(@Valid Issue issue) {
         if (Validation.hasErrors()) {
         	List<Tracker> trackers = Tracker.find("order by position asc").fetch();
     		List<User> users = User.find("login <> 'root' order by login").fetch();
     		List<State> states = State.find("order by position").fetch();
         	List<Enumeration> priorities = Enumeration.find("byType", "IssuePriority").fetch();
             params.flash(); // add http parameters to the flash scope
             render("@issue", issue, trackers, users, states, priorities);            
         }
         issue.updated = Calendar.getInstance().getTime();        
         issue.save();
 
         issues(issue.project.identifier);
         
     }
 
 }
