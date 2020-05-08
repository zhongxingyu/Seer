 package controllers;
 
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import models.Enumeration;
 import models.Issue;
 import models.Project;
 import models.State;
 import models.Tracker;
 import models.User;
 import play.Logger;
 import play.data.validation.Valid;
 import play.data.validation.Validation;
 import play.mvc.Before;
 
 public class Application extends Main {
 	
 //	private static class TrackerSummary {
 //		public String trackerType;
 //		public long sum;
 //		public long opened;
 //	}
 
     @Before
     static void setConnectedUser() {
         if(Security.isConnected()){
             User user = User.find("byLogin",Security.connected()).first();
             renderArgs.put("username", user);
             // TODO : examine the path of k33g.org
 //            renderArgs.put("security",Security.connected());
 //        } else {
 //            renderArgs.put("username", new User("","","John Doe"));
         }
     }
 	
     public static void index() {
         List<Project> projects = Project.all().fetch();
         render(projects);	
 
 //        render();
     }
 
 	public static void project (String identifier) {
 		Project project = Project.find("identifier", identifier).first();
 		List<Tracker> trackers = Tracker.findAll();
 		Tracker tracker;
 		Map summary = new HashMap();
 		Map opened = new HashMap();
 		
 		Iterator trackerCursor = trackers.iterator();		
 		while (trackerCursor.hasNext()) {
 			tracker = (Tracker) trackerCursor.next();
 			long i = Issue.count("tracker = ? and project = ?", tracker, project);
 			long j = Issue.count("tracker = ? and project = ? and state.closed = ? ", tracker, project, false);
 			summary.put(tracker.id, i);
 			opened.put(tracker.id, j);
 		}
 		
 		render(project, trackers, summary, opened);
 
 	}
 	
     public static void issues(String identifier) {
 //    	List<Tracker> trackers = Tracker.find("order by position asc").fetch();
 		Project project = Project.find("identifier", identifier).first();
 		List<Issue> issues = Issue.find("byProject", project).fetch();
     	render(issues, project);
     }
 
     public static void myIssues() {
 		List<Issue> issues = Issue.find("assignee.login = ? ", Security.connected()).fetch();
     	render("@issues", issues);
     }
 
     
     public static void newIssue(String identifier) {
     	List<Tracker> trackers = Tracker.find("order by position asc").fetch();
 		Project project = Project.find("identifier", identifier).first();
		List<User> users = User.find("order by login").fetch();
 		List<State> states = State.find("order by position").fetch();
     	List<Enumeration> priorities = Enumeration.find("byType", "IssuePriority").fetch();
 
     	render("@issue", trackers, project, users, states, priorities);
     }
     
    public static void saveIssue(Long projectId, Long trackerId, Long assigneeId, Long stateId, Long priorityId, @Valid Issue issue) {
         Project project = Project.findById(projectId);
     	List<Tracker> trackers = Tracker.find("order by position asc").fetch();
         if (Validation.hasErrors()) {
             render("@issue", issue, project, trackers);            
         }
         Logger.info("assignee = %s", issue.assignee);
         Tracker tracker = Tracker.findById(trackerId);
         issue.project = project;
         issue.tracker = tracker;
         issue.state = State.findById(stateId);
    	issue.priority = Enumeration.findById(priorityId);
         if (assigneeId == null) {
         	issue.assignee = null;
         } else {
 	        User assignee = User.findById(assigneeId);
 	        issue.assignee = assignee;
         }
         issue.updated = Calendar.getInstance().getTime();
         
         issue.save();
         
         issues(project.identifier);
         
     }
     
     public static void issue(Long id) {
     	Issue issue = Issue.findById(id);
     	Project project = issue.project;
     	List<Tracker> trackers = Tracker.find("order by position asc").fetch();
		List<User> users = User.find("order by login").fetch();
 		List<State> states = State.find("order by position").fetch();
     	List<Enumeration> priorities = Enumeration.find("byType", "IssuePriority").fetch();
 
     	render(issue, project, trackers, users, states, priorities);
     	}
     
     
 }
