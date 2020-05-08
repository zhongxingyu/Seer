 /*******************************************************************************
  * Copyright (c) 2013 Instituto Superior Técnico - João Antunes
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Luis Silva - ACGHSync
  *     João Antunes - initial API and implementation
  ******************************************************************************/
 package pt.ist;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import org.eclipse.egit.github.core.Comment;
 import org.eclipse.egit.github.core.Issue;
 import org.eclipse.egit.github.core.Label;
 import org.eclipse.egit.github.core.Milestone;
 import org.eclipse.egit.github.core.Repository;
 import org.eclipse.egit.github.core.User;
 import org.eclipse.egit.github.core.client.GitHubClient;
 import org.eclipse.egit.github.core.service.IssueService;
 import org.eclipse.egit.github.core.service.LabelService;
 import org.eclipse.egit.github.core.service.MilestoneService;
 import org.eclipse.egit.github.core.service.OrganizationService;
 import org.eclipse.egit.github.core.service.RepositoryService;
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.ist.fenixframework.Atomic;
 import pt.ist.fenixframework.Atomic.TxMode;
 import pt.ist.fenixframework.Config;
 import pt.ist.fenixframework.core.WriteOnReadError;
 import pt.ist.maidSyncher.api.activeCollab.ACCategory;
 import pt.ist.maidSyncher.api.activeCollab.ACComment;
 import pt.ist.maidSyncher.api.activeCollab.ACContext;
 import pt.ist.maidSyncher.api.activeCollab.ACInstance;
 import pt.ist.maidSyncher.api.activeCollab.ACLoggedTime;
 import pt.ist.maidSyncher.api.activeCollab.ACMilestone;
 import pt.ist.maidSyncher.api.activeCollab.ACProject;
 import pt.ist.maidSyncher.api.activeCollab.ACProjectLabel;
 import pt.ist.maidSyncher.api.activeCollab.ACSubTask;
 import pt.ist.maidSyncher.api.activeCollab.ACTask;
 import pt.ist.maidSyncher.api.activeCollab.ACTaskLabel;
 import pt.ist.maidSyncher.api.activeCollab.ACUser;
 import pt.ist.maidSyncher.api.github.RateLimitingService;
 import pt.ist.maidSyncher.api.github.RateLimitingService.RateLimits;
 import pt.ist.maidSyncher.domain.MaidRoot;
 import pt.ist.maidSyncher.domain.activeCollab.ACTaskCategory;
 import pt.ist.maidSyncher.domain.github.GHComment;
 import pt.ist.maidSyncher.domain.github.GHIssue;
 import pt.ist.maidSyncher.domain.github.GHLabel;
 import pt.ist.maidSyncher.domain.github.GHMilestone;
 import pt.ist.maidSyncher.domain.github.GHOrganization;
 import pt.ist.maidSyncher.domain.github.GHRepository;
 import pt.ist.maidSyncher.domain.sync.SyncEvent;
 import pt.ist.maidSyncher.domain.sync.logs.SyncLog;
 
 public class Main {
     private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
 
     private static Config config = null;
 
     static final Properties ffProperties = new Properties();
 
     private static List<URL> urls = null;
 
     private static SyncLog currentSyncLog;
 
     // FenixFramework will try automatic initialization when first accessed
     public static void main(String[] args) {
         try {
             logStartOfSyncProcess();
             processAnyRemainingSyncEvents();
             retrieveAndCreateSyncEvents();
             printChangesBuzz();
             applyChanges();
         } catch (Exception ex) {
             if (ex.getCause() instanceof WriteOnReadError) { //it shouldn't happen
                 throw (WriteOnReadError) ex.getCause();
             } else {
                 registerExceptionAndMarkAsFailed(ex);
                 throw ex;
             }
         }
         //MaidRoot.getInstance().processChangesBuzz(true);
     }
 
     @Atomic(mode = TxMode.WRITE)
     private static void logStartOfSyncProcess() {
        SyncLog previousSyncLog = MaidRoot.getInstance().getCurrentSyncLog();
        if (previousSyncLog != null && previousSyncLog.is(SyncLog.STATUS_ONGOING)) {
            previousSyncLog.markAsInterrupted();
        }
         currentSyncLog = new SyncLog();
         MaidRoot.getInstance().setCurrentSyncLog(currentSyncLog);
 
     }
 
     @Atomic(mode = TxMode.WRITE)
     private static void registerExceptionAndMarkAsFailed(Exception ex) {
 
         currentSyncLog.registerExceptionAndMarkAsFailed(ex);
     }
 
     @Atomic(mode = TxMode.READ)
     private static void processAnyRemainingSyncEvents() {
         MaidRoot maidRoot = MaidRoot.getInstance();
         maidRoot.processRemainingInstances();
 
     }
 
     @Atomic(mode = TxMode.READ)
     private static void applyChanges() {
         try {
             MaidRoot.getInstance().applyChangesBuzz();
         } catch (Exception ex) {
             if (ex.getCause() instanceof WriteOnReadError) {
                 throw (WriteOnReadError) ex.getCause();
             } else {
                 currentSyncLog.registerExceptionAndMarkAsFailed(ex);
                 throw ex;
             }
         }
     }
 
     @Atomic(mode = TxMode.WRITE)
     private static void retrieveAndCreateSyncEvents() {
         MaidRoot.getInstance().setCurrentSyncLog(currentSyncLog);
         try {
             MaidRoot.getChangesBuzz().clear();
             syncGitHub();
             syncActiveCollab();
         } catch (IOException exception) {
             LOGGER.error("caught an IO exception", exception);
             throw new Error(exception);
         }
 
     }
 
     @Atomic(mode = TxMode.WRITE)
     private static void printChangesBuzz() {
         System.out.println("Printing changes buzz: ");
         for (SyncEvent syncEvent : MaidRoot.getChangesBuzz().values()) {
             System.out.println(syncEvent);
 
         }
 
     }
 
     private static void syncActiveCollab() throws IOException {
 
         currentSyncLog.setSyncACStartTime(new DateTime());
 
         // setup ActiveCollab
         Properties acConfigurationProperties = new Properties();
         acConfigurationProperties.load(Main.class.getResourceAsStream("/configuration.properties"));
 
         ACContext acContext = ACContext.getInstance();
         acContext.setServer(acConfigurationProperties.getProperty("ac.server.host"));
         acContext.setToken(acConfigurationProperties.getProperty("ac.server.token"));
 
         ACInstance instanceForDSI = ACInstance.getInstanceForCompanyName();
         pt.ist.maidSyncher.domain.activeCollab.ACInstance.process(instanceForDSI);
         System.out.println("ACInstance: " + instanceForDSI.getName());
 
         List<ACUser> users = instanceForDSI.getUsers();
         for (ACUser user : users) {
             System.out.println("ACUser: " + user.getName());
             pt.ist.maidSyncher.domain.activeCollab.ACUser.process(user);
         }
 
         //let's proccess all of the project labels
         for (ACProjectLabel acProjectLabel : acContext.getACProjectLabels()) {
             System.out.println("ACProjectLabel: " + acProjectLabel.getName());
             pt.ist.maidSyncher.domain.activeCollab.ACProjectLabel.process(acProjectLabel);
         }
 
         //let's proccess all of the task assignment labels
         for (ACTaskLabel acTaskLabel : acContext.getACTaskLabels()) {
             System.out.println("ACTaskLabel: " + acTaskLabel.getName());
             pt.ist.maidSyncher.domain.activeCollab.ACTaskLabel.process(acTaskLabel);
         }
 
         // load ActiveCollab project
         List<ACProject> acProjects = acContext.getProjects();
         Iterator<ACProject> it = acProjects.iterator();
         while (it.hasNext()) {
             ACProject project = it.next();
             System.out
             .println(" Project " + project.getId() + " " + project.getName() + " updated on: " + project.getUpdatedOn());
             pt.ist.maidSyncher.domain.activeCollab.ACProject.process(project);
 
             System.out.println("Listing all of the milestones associated with this project");
             List<ACMilestone> milestones = project.getMilestones();
             for (ACMilestone milestone : milestones) {
                 System.out.println("  Milestone: " + milestone.getName() + " updated on: " + milestone.getUpdatedOn() + " id: "
                         + milestone.getId());
                 pt.ist.maidSyncher.domain.activeCollab.ACMilestone.process(milestone, project);
             }
 
             //let's get all of the task categories for this project
             Set<ACCategory> taskCategories = project.getTaskCategories();
             for (ACCategory taskCategory : taskCategories) {
                 System.out.println("  Task Category: " + taskCategory.getName() + " id: " + taskCategory.getId());
             }
             ACTaskCategory.process(taskCategories, project);
 
             List<ACTask> acTasks = project.getTasks();
             Iterator<ACTask> itt = acTasks.iterator();
             while (itt.hasNext()) {
                 ACTask task = itt.next();
                 System.out.println("\tTask " + task.getId() + " " + task.getName() + " " + task.getDueOn() + " updated on: "
                         + task.getUpdatedOn());
                 pt.ist.maidSyncher.domain.activeCollab.ACTask.process(task, project);
 
                 ACCategory category = task.getCategory();
                 if (category != null)
                     System.out.println("\t Category: " + category.getId() + " name: " + category.getName() + " updated on: "
                             + category.getUpdatedOn());
 
                 //let's take care of the subtasks
                 Set<ACSubTask> subTasks = task.getSubTasks();
                 for (ACSubTask acSubTask : subTasks) {
                     System.out.println("\t\tSubTask: " + acSubTask.getId() + " " + acSubTask.getName() + " updated on: "
                             + acSubTask.getUpdatedOn());
                 }
                 pt.ist.maidSyncher.domain.activeCollab.ACSubTask.process(subTasks, task);
 
                 //getting and printing all of the comments
                 Set<ACComment> comments = task.getComments();
                 System.out.println("\t\t Printing all the comments");
                 for (ACComment comment : comments) {
                     System.out.println("\t\tComment: " + comment.getId() + " content: " + comment.getBody() + " updated on: "
                             + comment.getUpdatedOn());
                 }
                 pt.ist.maidSyncher.domain.activeCollab.ACComment.process(comments, task);
             }
 
             //let's print all of the logged times for this project
             List<ACLoggedTime> loggedTimes = project.getLoggedTimes();
             if (loggedTimes.isEmpty() == false) {
                 System.out.println("\tPrinting logged times: ");
                 for (ACLoggedTime acLoggedTime : loggedTimes) {
                     System.out.println("\t  Logged time: " + acLoggedTime.getId() + " " + acLoggedTime.getUpdatedOn() + " "
                             + acLoggedTime.getName() + " time: " + acLoggedTime.getValue());
                     pt.ist.maidSyncher.domain.activeCollab.ACLoggedTime.process(acLoggedTime);
                 }
             }
         }
 
         currentSyncLog.setSyncACEndTime(new DateTime());
 /*
         List<ACTask> acTasks = acp.getTasks();
         Iterator<ACTask> it = acTasks.iterator();
         while(it.hasNext()) {
             ACTask task = it.next();
             System.out.println(" Task " + task.getId() + " " + task.getName() + " " + task.getDueOn());
             System.out.println(" SER " + task.toString());
         }
  */
 /*
         HashMap<String,String> hm = new HashMap<String,String>();
         hm.put("subtask[name]","This is a subtask of task 10!");
         hm.put("subtask[body]","Body of task!");
 //      hm.put("task[visibility]","1");
 //      hm.put("task[category_id]","1");
         hm.put("subtask[label_id]","1");
 //      hm.put("task[milestone_id]","1");
         hm.put("subtask[priority]","5");
         hm.put("subtask[due_on]","2013-10-01");
         ACContext.processPost("projects/1/tasks/10/subtasks/add", hm);
  */
     }
 
     private static void syncGitHub() throws IOException {
         currentSyncLog.setSyncGHStartTime(new DateTime());
         //this is the first sync task, so let us reset the changesBuzz
         //MaidRoot.getInstance().resetSyncEvents();
 
         //let's try to connect to the GH Account
         Properties configurationProperties = new Properties();
         InputStream configurationInputStream = Main.class.getResourceAsStream("/configuration.properties");
         configurationProperties.load(configurationInputStream);
 
         //let's try to authenticate and get the user and repository list
 
         String oauth2Token = configurationProperties.getProperty("github.oauth2.token");
         String organizationName = configurationProperties.getProperty("github.organization.name");
 
         GitHubClient client = new GitHubClient();
         client.setOAuth2Token(oauth2Token);
 
         RepositoryService repositoryService = new RepositoryService(client);
         OrganizationService organizationService = new OrganizationService(client);
         IssueService issueService = new IssueService(client);
         MilestoneService milestoneServiceService = new MilestoneService(client);
         LabelService labelService = new LabelService(client);
 
         RateLimitingService rateLimitingService = new RateLimitingService(client);
 
         List<User> orgMembers = null;
         List<Repository> repositories = null;
 
         RateLimits remainingHits = rateLimitingService.getRemainingHits();
         currentSyncLog.setNumberGHRequestsAtStartSync(remainingHits.getRemaining());
 
         orgMembers = organizationService.getMembers(organizationName);
 
         orgMembers.addAll(organizationService.getPublicMembers(organizationName));
 
         User organization = organizationService.getOrganization(organizationName);
         GHOrganization.process(organization);
 
         repositories = repositoryService.getRepositories(organization.getLogin());
 
         System.out.println("List of repositories:");
         for (Repository repository : repositories) {
             System.out.println(repository.getName());
             GHRepository.process(repository);
 
             List<Label> labels = labelService.getLabels(repository);
             System.out.println(" Has " + labels.size() + " labels, listing them");
             for (Label label : labels) {
                 System.out.println("  Label: " + label.getName());
             }
             GHLabel.process(labels, repository);
 
             List<Milestone> milestones = new ArrayList<Milestone>(milestoneServiceService.getMilestones(repository, "open"));
 
             milestones.addAll(milestoneServiceService.getMilestones(repository, "closed"));
 
             System.out.println(" Got " + milestones.size() + " milestones, listing them");
             for (Milestone milestone : milestones) {
                 System.out.println("  Milestone " + milestone.getTitle() + " url: " + milestone.getUrl() + " closed issues: "
                         + milestone.getClosedIssues() + " open issues: " + milestone.getOpenIssues());
             }
             GHMilestone.process(milestones, repository);
 
             List<Issue> issues = issueService.getIssues(repository, new HashMap<String, String>());
             HashMap<String, String> stateClosedMap = new HashMap<>();
             stateClosedMap.put("state", "closed");
             issues.addAll(issueService.getIssues(repository, stateClosedMap));
             System.out.println(" Has " + issues.size() + " issues, listing them");
             for (Issue issue : issues) {
                 Milestone milestone = issue.getMilestone();
                 String milestoneString = milestone == null ? "-" : milestone.getTitle();
                 System.out.print("  Issue " + issue.getNumber() + " name: " + issue.getTitle() + " milestone: " + milestoneString
                         + " labels: ");
                 GHIssue.process(issue, repository);
                 for (Label label : issue.getLabels()) {
                     System.out.print(label.getName() + " ");
                 }
                 System.out.println();
 
                 int comments = issue.getComments();
                 if (comments > 0) {
                     System.out.println("    got " + comments + " comments. Showing them:");
                     List<Comment> commentsCollection = issueService.getComments(repository, issue.getNumber());
                     for (Comment comment : commentsCollection) {
                         System.out.println("      Comment " + comment.getId() + " " + comment.getBody());
                     }
                     GHComment.process(commentsCollection, issue);
                 }
             }
 
         }
 
         remainingHits = rateLimitingService.getRemainingHits();
         currentSyncLog.setNumberGHRequestsAtEndSync(remainingHits.getRemaining());
 
         if (orgMembers != null && orgMembers.isEmpty() == false) {
             System.out.println("List of members:");
             for (User user : orgMembers) {
                 System.out.println(user.getName() + user.getCompany() + user.getLogin());
             }
         }
 
         currentSyncLog.setSyncGHEndTime(new DateTime());
 
     }
 
 //    @Atomic
 //    public static void frameworkInit() {
 //        int teste;
 //        if (FenixFramework.getRoot() == null) {
 //            System.out.println("oops");
 //        }
 //
 //    }
 }
