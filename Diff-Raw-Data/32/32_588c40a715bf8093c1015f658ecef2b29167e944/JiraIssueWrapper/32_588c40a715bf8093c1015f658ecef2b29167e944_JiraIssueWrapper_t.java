 package uk.org.sappho.code.heatmap.issues.jira;
 
import java.util.Set;

 import com.atlassian.jira.rpc.soap.client.RemoteIssue;
 
 import uk.org.sappho.code.heatmap.issues.IssueWrapper;
 
 public class JiraIssueWrapper implements IssueWrapper {
 
     private final String issueKey;
     private final String summary;
     private final String subTaskKey;
    private final Set<String> releases;
     private final int weight;
 
    public JiraIssueWrapper(RemoteIssue issue, String subTaskKey, Set<String> releases, int weight) {
 
         issueKey = issue.getKey();
         summary = issue.getSummary();
         this.subTaskKey = subTaskKey;
        this.releases = releases;
         this.weight = weight;
     }
 
     public String getKey() {
 
         return issueKey;
     }
 
     public String getSubTaskKey() {
 
         return subTaskKey;
     }
 
     public String getSummary() {
 
         return summary;
     }
 
    public Set<String> getReleases() {

        return releases;
    }

     public int getWeight() {
 
         return weight;
     }
 }
