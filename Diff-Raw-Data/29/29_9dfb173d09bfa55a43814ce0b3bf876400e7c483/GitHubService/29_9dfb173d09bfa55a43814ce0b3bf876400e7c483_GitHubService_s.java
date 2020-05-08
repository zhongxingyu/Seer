 package org.peaceful.banana.git;
 
 import org.eclipse.egit.github.core.*;
 import org.eclipse.egit.github.core.client.GitHubClient;
 import org.eclipse.egit.github.core.client.NoSuchPageException;
 import org.eclipse.egit.github.core.client.PageIterator;
 import org.eclipse.egit.github.core.event.Event;
 import org.eclipse.egit.github.core.service.*;
 import org.peaceful.banana.git.util.CommitStatistics;
 import org.scribe.model.Token;
 
 import java.io.IOException;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ekun
  * Date: 02.11.12
  * Time: 09:14
  */
 public class GitHubService {
 
     private GitHubClient gitHubClient;
     private RepositoryService repositoryService;
     private UserCommitService userCommitService;
     private MilestoneService milestoneService;
     private IssueService issueService;
     private UserService userService;
     private EventService eventService;
 
     public GitHubService(Token gitHubToken) {
         gitHubClient = new GitHubClient();
         this.gitHubClient.setOAuth2Token(gitHubToken.getToken());
         this.repositoryService = new RepositoryService(gitHubClient);
         this.userCommitService = new UserCommitService(gitHubClient);
         this.milestoneService = new MilestoneService(gitHubClient);
         this.issueService = new IssueService(gitHubClient);
         this.userService = new UserService(gitHubClient);
         this.eventService = new EventService(gitHubClient);
     }
 
     public void setToken(Token gitHubToken) {
         this.gitHubClient.setOAuth2Token(gitHubToken.getToken());
     }
 
     /**
      * Retrivies all the repositories the currently authenticated user is connected with
      * @return
      */
     public List<Repository> getRepositories() {
         try {
             return repositoryService.getRepositories();
         } catch (IOException e) {
             return new ArrayList<Repository>();
         }
     }
 
     /**
      * Will return a list withe the number of commits each user have done to the repo
      * @param repository
      */
     public ArrayList<Object[]> getRepositoryCommitStats(Repository repository) {
         List<Contributor> collaborators = null;
         ArrayList<Object[]> commitsPerUser = new ArrayList();
         try {
             collaborators = repositoryService.getContributors(repository, true);
             for(Contributor contributor : collaborators) {
                 Object[] array = new Object[2];
                 if (contributor.getLogin() != null) {
                     array[0] = contributor.getLogin();
                     array[1] = userCommitService.getCommits(repository, contributor.getLogin()).size();
                 } else {
                     array[0] = contributor.getName();
                     array[1] = userCommitService.getCommits(repository, contributor.getName()).size();
                 }
                 commitsPerUser.add(array);
             }
         } catch (IOException e) {
             return new ArrayList<Object[]>();
         }
         return commitsPerUser;
     }
 
     /**
      *
      * @param repositoryId
      * @return
      */
     public Repository getRepository(long repositoryId) {
         List<Repository> repositoryList = getRepositories();
         for(Repository repository : repositoryList) {
             if(repository.getId() == repositoryId)
                 return repository;
         }
         return null;
     }
 
     /**
      *
      * @param repository
      * @return
      */
     public ArrayList<String> getRepositoryCollaborators(Repository repository) {
         ArrayList<String> collaborators = new ArrayList<String>();
         try {
             List<Contributor> collaboratorsList = repositoryService.getContributors(repository, true);
             for(Contributor contributor : collaboratorsList) {
                 if (contributor.getLogin() != null) {
                     collaborators.add(contributor.getLogin());
                 } else {
                     collaborators.add(contributor.getName());
                 }
             }
         } catch (IOException e) {
             //
         }
         return collaborators;
     }
 
     /**
      *
      * @param repository
      * @param githubUser
      * @return
      */
     public CommitStatistics getUserRepositoryImpact(Repository repository, String githubUser) {
         CommitStatistics impactStatistics = new CommitStatistics();
         impactStatistics.setUser(githubUser);
         try {
             List<RepositoryCommit> commits = userCommitService.getCommits(repository, githubUser);
             for(RepositoryCommit repositoryCommit : commits) {
                 RepositoryCommit commit = userCommitService.getCommit(repository, repositoryCommit.getSha());
                 impactStatistics.addImpact(commit.getStats().getAdditions(), commit.getStats().getDeletions());
             }
         } catch (IOException e) {
             return new CommitStatistics();
         }
         return impactStatistics;
     }
 
     /**
      *
      * @param repository
      * @return
      */
     public ArrayList<CommitStatistics> getRepositoryImpact(Repository repository) {
         ArrayList<CommitStatistics> impact = new ArrayList<CommitStatistics>();
 
         ArrayList<String> contributors = getRepositoryCollaborators(repository);
 
         for (String contributor : contributors) {
             impact.add(getUserRepositoryImpact(repository, contributor));
         }
 
         return impact;
     }
 
     /**
      *
      * @param repository
      * @param open
      * @return
      */
     public List<Milestone> getMilestones(Repository repository, boolean open) {
         String state = "open";
         if(!open)
             state = "closed";
         try {
             return milestoneService.getMilestones(repository, state);
         } catch (IOException e) {
             System.out.println("ERROR IO exception");
             return new ArrayList<Milestone>();
         }
     }
 
     /**
      *
      * @param repository
      * @return
      */
     public ArrayList<Issue> getIssues(Repository repository) {
         return getIssues(repository, "open");
     }
 
     public ArrayList<Issue> getIssues(Repository repository, String state) {
         ArrayList<Issue> output = new ArrayList<Issue>();
         HashMap<String, String> filterData = new HashMap<String, String>();
         filterData.put(IssueService.FILTER_STATE, state);
         try {
             List<Issue> issues = issueService.getIssues(repository, filterData);
             for(Issue issue : issues){
                 output.add(issueService.getIssue(repository, issue.getNumber()));
             }
         } catch (IOException e) {
             return null;
         }
         return output;
     }
 
     /**
      *
      * @param repository
      * @param lastUpdate
      * @return
      */
     public boolean hasUpdated(Repository repository, Date lastUpdate) {
         try {
             return userCommitService.getCommits(repository, null, lastUpdate, null).size() > 0;
         } catch (IOException e) {
             return false;
         }
     }
 
     /**
      *
      * @param repository
      * @param lastUpdate
      * @return
      */
     public ArrayList<CommitStatistics> getCommitsSince(Repository repository, Date lastUpdate) {
         ArrayList<CommitStatistics> repositoryCommits = new ArrayList<CommitStatistics>();
         try{
             ArrayList<String> contributors = getRepositoryCollaborators(repository);
 
             for (String contributor : contributors) {
                 List<RepositoryCommit> commits = userCommitService.getCommits(repository, contributor, lastUpdate, null);
                 for(RepositoryCommit repositoryCommit : commits) {
                     RepositoryCommit commit = userCommitService.getCommit(repository, repositoryCommit.getSha());
                     repositoryCommits.add(new CommitStatistics(commit.getStats().getAdditions(),
                            commit.getStats().getDeletions(), contributor, commit.getCommit().getMessage()));
                 }
             }
         } catch (IOException e) {
             return new ArrayList<CommitStatistics>();
         }
         return repositoryCommits;
     }
 
     /**
      * Checks if the user is authenticated with github
      *
      * @return true/false based on authentication.
      */
     public boolean isAuthenticated(){
         return this.gitHubClient.getUser() != null;
     }
 
     /**
      * Get login of the currenctly authenticated user
      * @return
      */
     public String getAuthenticatedUser() {
         try {
             return this.userService.getUser().getLogin();
         } catch (IOException e) {
             return "";
         }
     }
 
     /**
      * Get all issues events related to this repository.
      * @param repository
      * @return
      */
     public ArrayList<Event> getIssueEvents(Repository repository) {
         PageIterator<Event> iterator = eventService.pageEvents(repository);
         ArrayList<Event> issueEvents = new ArrayList<Event>();
 
         try {
             while(iterator.hasNext()){
                 issueEvents.addAll(iterator.next());
             }
         } catch (NoSuchPageException pageException) {
             return new ArrayList<Event>();
         }
         return issueEvents;
     }
 }
