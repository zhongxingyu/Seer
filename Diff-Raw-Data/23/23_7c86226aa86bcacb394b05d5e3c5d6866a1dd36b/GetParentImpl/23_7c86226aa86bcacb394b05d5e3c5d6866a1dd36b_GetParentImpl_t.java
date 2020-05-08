 package uk.org.sappho.jira.soap.getparent;
 
import java.util.Map;

 import com.atlassian.jira.issue.Issue;
 import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.ModifiedValue;
 import com.atlassian.jira.issue.MutableIssue;
 import com.atlassian.jira.rpc.auth.TokenManager;
 import com.atlassian.jira.rpc.exception.RemoteException;
 
 public class GetParentImpl implements GetParent {
 
     private final TokenManager tokenManager;
     private final IssueManager issueManager;
 
     public GetParentImpl(TokenManager tokenManager, IssueManager issueManager) {
 
         this.tokenManager = tokenManager;
         this.issueManager = issueManager;
     }
 
     public String login(String username, String password) throws RemoteException {
 
         return tokenManager.login(username, password);
     }
 
     public String getParentKey(String token, String issueKey) throws RemoteException {
 
         tokenManager.retrieveUserNoPermissionCheck(token);
         MutableIssue issue = issueManager.getIssueObject(issueKey);
         Issue parentIssue = issue.getParentObject();
         return parentIssue != null ? parentIssue.getKey() : null;
     }

    public Map<String, ModifiedValue> getModifiedFields(String token, String issueKey) throws RemoteException {

        tokenManager.retrieveUserNoPermissionCheck(token);
        MutableIssue issue = issueManager.getIssueObject(issueKey);
        return issue.getModifiedFields();
    }
 }
