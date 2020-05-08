 package uk.org.sappho.code.heatmap.issues.jira;
 
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 
 import com.atlassian.jira.rpc.soap.client.JiraSoapService;
 import com.atlassian.jira.rpc.soap.client.JiraSoapServiceServiceLocator;
 import com.atlassian.jira.rpc.soap.client.RemoteIssue;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 import uk.org.sappho.code.heatmap.config.Configuration;
 import uk.org.sappho.code.heatmap.issues.IssueManagement;
 import uk.org.sappho.code.heatmap.issues.IssueManagementException;
 import uk.org.sappho.code.heatmap.issues.IssueWrapper;
 
 @Singleton
 public class JiraService implements IssueManagement {
 
     protected JiraSoapService jiraSoapService = null;
     protected String JiraSoapServiceToken = null;
     protected Map<String, IssueWrapper> allowedIssues = new HashMap<String, IssueWrapper>();
     protected Map<String, String> issueTypes = new HashMap<String, String>();
     protected Map<String, Integer> issueTypeWeightMultipliers = new HashMap<String, Integer>();
     protected Configuration config;
     protected static final Pattern SIMPLE_JIRA_REGEX = Pattern.compile("^([a-zA-Z]{2,}-\\d+):.*$");
     private static final Logger LOG = Logger.getLogger(JiraService.class);
 
     @Inject
     public JiraService(Configuration config) throws IssueManagementException {
 
         LOG.info("Using Jira issue management plugin");
         this.config = config;
         connect();
         getAllowedIssues();
     }
 
     protected void connect() throws IssueManagementException {
 
         String url = config.getProperty("jira.url", "http://example.com");
         String username = config.getProperty("jira.username", "nobody");
         String password = config.getProperty("jira.password", "nopassword");
         LOG.info("Connecting to " + url + " as " + username);
         try {
             jiraSoapService = new JiraSoapServiceServiceLocator().getJirasoapserviceV2(new URL(url
                     + "/rpc/soap/jirasoapservice-v2"));
             JiraSoapServiceToken = jiraSoapService.login(username, password);
         } catch (Throwable t) {
             throw new IssueManagementException("Unable to log in to Jira at " + url + " as user " + username, t);
         }
     }
 
     protected void getAllowedIssues() throws IssueManagementException {
 
         /**
          * note: this is a bit rubbish but because jira's soap interface doesn't have a getParent function it's the only way to fake it
          * making this better will require an installed plugin
          * **/
         LOG.info("Getting list of allowed issues");
         try {
             // get all tasks we're prepared to deal with
             RemoteIssue[] remoteIssues = jiraSoapService.getIssuesFromJqlSearch(JiraSoapServiceToken, config
                     .getProperty("jira.filter.issues.allowed"), 1000);
             // map all subtasks back to their parents
             Map<String, RemoteIssue> mappedRemoteIssues = new HashMap<String, RemoteIssue>();
             Map<String, String> subTaskParents = new HashMap<String, String>();
             for (RemoteIssue remoteIssue : remoteIssues) {
                 String issueKey = remoteIssue.getKey();
                 mappedRemoteIssues.put(issueKey, remoteIssue);
                 RemoteIssue[] subTasks = jiraSoapService.getIssuesFromJqlSearch(JiraSoapServiceToken, "parent = "
                         + issueKey, 200);
                 for (RemoteIssue subTask : subTasks) {
                     String subTaskKey = subTask.getKey();
                     LOG.info("Mapping " + subTaskKey + " to parent issue " + issueKey);
                     if (mappedRemoteIssues.get(subTaskKey) == null) {
                         mappedRemoteIssues.put(subTaskKey, subTask);
                     }
                     subTaskParents.put(subTaskKey, issueKey);
                 }
             }
             // create issue wrappers for all allowed root (non-subtask) issues
             for (String issueKey : mappedRemoteIssues.keySet()) {
                 String parentKey = subTaskParents.get(issueKey);
                 IssueWrapper issueWrapper = parentKey != null ?
                         createIssueWrapper(mappedRemoteIssues.get(parentKey), issueKey) :
                        createIssueWrapper(mappedRemoteIssues.get(issueKey), "");
                 allowedIssues.put(issueKey, issueWrapper);
             }
         } catch (Throwable t) {
             throw new IssueManagementException("Unable to get list of allowed issues", t);
         }
     }
 
     protected IssueWrapper createIssueWrapper(RemoteIssue issue, String subTaskKey) throws IssueManagementException {
 
         String typeId = issue.getType();
         String typeName = issueTypes.get(typeId);
         if (typeName == null) {
             typeName = config.getProperty("jira.type.map.id." + typeId, "housekeeping");
             LOG.info("Mapping raw issue type " + typeId + " to " + typeName);
             issueTypes.put(typeId, typeName);
         }
         Integer weight = issueTypeWeightMultipliers.get(typeName);
         if (weight == null) {
             String typeNameKey = "jira.type.multiplier." + typeName;
             try {
                 weight = Integer.parseInt(config.getProperty(typeNameKey, "0"));
             } catch (Throwable t) {
                 throw new IssueManagementException(
                             "Issue type weight configuration \"" + typeNameKey + "\" is invalid", t);
             }
             LOG.info("Weight of issue type " + typeName + " is " + weight);
             issueTypeWeightMultipliers.put(typeName, weight);
         }
         return new JiraIssueWrapper(issue, subTaskKey, weight);
     }
 
     protected String getIssueKeyFromCommitComment(String commitComment) {
 
         String key = null;
         Matcher matcher = SIMPLE_JIRA_REGEX.matcher(commitComment.split("\n")[0]);
         if (matcher.matches()) {
             key = matcher.group(1);
         } else {
             LOG.info("No issue ID found in commit comment: " + commitComment);
         }
         return key;
     }
 
     public IssueWrapper getIssue(String commitComment) {
 
         IssueWrapper issue = null;
         String key = getIssueKeyFromCommitComment(commitComment);
         if (key != null) {
             issue = allowedIssues.get(key);
         }
         return issue;
     }
 }
