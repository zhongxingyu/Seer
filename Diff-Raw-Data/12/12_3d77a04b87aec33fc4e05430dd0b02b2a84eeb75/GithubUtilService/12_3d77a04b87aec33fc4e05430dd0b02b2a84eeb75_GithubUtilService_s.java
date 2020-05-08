 package net.codjo.tools.github;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import org.eclipse.egit.github.core.PullRequest;
 import org.eclipse.egit.github.core.Repository;
 import org.eclipse.egit.github.core.RepositoryId;
 import org.eclipse.egit.github.core.client.GitHubClient;
 import org.eclipse.egit.github.core.client.PageIterator;
 import org.eclipse.egit.github.core.event.Event;
 import org.eclipse.egit.github.core.service.EventService;
 import org.eclipse.egit.github.core.service.PullRequestService;
 import org.eclipse.egit.github.core.service.RepositoryService;
 
 import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;
 
 /**
  * Created with IntelliJ IDEA. User: marcona Date: 20/07/12 Time: 09:02 To change this template use File | Settings |
  * File Templates.
  */
 public class GithubUtilService {
     private GitHubClient client = new GitHubClient();
 
 
     public GitHubClient initGithubClient(String githubUser, String githubPassword) {
         client.setCredentials(githubUser, githubPassword);
         return client;
     }
 
 
     public void forkRepo(String githubUser, String githubPassword, String repoName) throws IOException {
         client = initGithubClient(githubUser, githubPassword);
         RepositoryService repositoryService = new RepositoryService(client);
         repositoryService.forkRepository(new RepositoryId("codjo", repoName));
     }
 
 
     public void deleteRepo(String githubUser, String githubPassword, String repoName) throws IOException {
         client = initGithubClient(githubUser, githubPassword);
         client.delete(SEGMENT_REPOS + "/" + githubUser + "/" + repoName);
     }
 
 
     public int getGitHubQuota() throws IOException {
         return client.getRemainingRequests();
     }
 
 
     public List<Repository> list(String githubUser, String githubPassword, String repoName) throws IOException {
         client = initGithubClient(githubUser, githubPassword);
         return getRepositoryList(githubUser, repoName, client);
     }
 
 
     public List<Event> eventsSinceLastRelease(String githubUser,
                                               String githubPassword,
                                               String repoName, String codjoPomRequestPrefix) throws IOException {
         client = initGithubClient(githubUser, githubPassword);
 
         List<Event> pullRequests = new ArrayList<Event>();
 
         RepositoryService repositoryService = new RepositoryService(client);
         Repository superPomRepository = repositoryService.getRepository("codjo", "codjo-pom");
 
         PullRequestService service = new PullRequestService(client);
         List<PullRequest> pullRequestList = service.getPullRequests(superPomRepository, "closed");
         Date closedAt = null;
         //find last  For release pull request --> do we have to sort them ?
         for (PullRequest pullRequest : pullRequestList) {
             String pullRequestTitle = pullRequest.getTitle().toLowerCase();
             if (pullRequestTitle.contains(codjoPomRequestPrefix)) {
                 closedAt = pullRequest.getClosedAt();
                 break;
             }
         }
 
         if (closedAt != null) {
             EventService eventService = new EventService(client);
             PageIterator<Event> codjoEvents = eventService.pageUserReceivedEvents("codjo", true, 0, 1);
             while (codjoEvents.hasNext()) {
                 Collection<Event> next = codjoEvents.next();
                 for (Event event : next) {
                     if (event.getCreatedAt().after(closedAt)) {
                         if ("PullRequestEvent".equals(event.getType())) {
                             pullRequests.add(event);
                         }
                     }
                 }
             }

            PageIterator<Event> codjo = eventService.pageUserEvents("codjo", true, 0, 1);
            while (codjo.hasNext()) {
                Collection<Event> next = codjo.next();
                for (Event event : next) {
                    if (event.getCreatedAt().after(closedAt)) {
                        if ("PullRequestEvent".equals(event.getType())) {
                            pullRequests.add(event);
                        }
                    }
                }
            }
         }
         return pullRequests;
     }
 
 
     private List<Repository> getRepositoryList(String githubUser, String repoName, GitHubClient gitHubClient)
           throws IOException {
         RepositoryService repositoryService = new RepositoryService(gitHubClient);
         if (repoName != null && !repoName.trim().isEmpty()) {
             return repositoryService.getRepositories(repoName);
         }
         else {
             return repositoryService.getRepositories(githubUser);
         }
     }
 }
