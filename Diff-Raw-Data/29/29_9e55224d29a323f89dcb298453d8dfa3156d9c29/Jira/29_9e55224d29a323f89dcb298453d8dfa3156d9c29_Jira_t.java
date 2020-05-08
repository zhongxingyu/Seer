 package uk.org.sappho.code.change.management.issues;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 
 import com.atlassian.jira.rpc.soap.client.RemoteComponent;
 import com.atlassian.jira.rpc.soap.client.RemoteIssue;
 import com.atlassian.jira.rpc.soap.client.RemoteVersion;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 import uk.org.sappho.code.change.management.data.IssueData;
 import uk.org.sappho.configuration.Configuration;
 import uk.org.sappho.configuration.ConfigurationException;
 import uk.org.sappho.jira4j.soap.GetParentService;
 import uk.org.sappho.jira4j.soap.JiraSoapService;
import uk.org.sappho.string.mapping.Mapper;
 import uk.org.sappho.warnings.WarningsList;
 
 @Singleton
 public class Jira implements IssueManagement {
 
     private String jiraURL = null;
     private JiraSoapService jiraSoapService = null;
     private GetParentService getParentService = null;
     private final Map<String, RemoteIssue> mappedRemoteIssues = new HashMap<String, RemoteIssue>();
     private final Map<String, IssueData> parentIssues = new HashMap<String, IssueData>();
     private final Map<String, String> subTaskParents = new HashMap<String, String>();
     private final Map<String, String> warnedSubTasks = new HashMap<String, String>();
     private final Map<String, String> releases = new HashMap<String, String>();
     private final Map<String, String> issueTypes = new HashMap<String, String>();
     private final WarningsList warnings;
     private final Configuration config;
    private final Mapper fixVersionMapper;
     private static final Logger LOG = Logger.getLogger(Jira.class);
 
     @Inject
    public Jira(WarningsList warnings, Configuration config) throws IssueManagementException, ConfigurationException {
 
         LOG.info("Using Jira issue management plugin");
         this.warnings = warnings;
         this.config = config;
        fixVersionMapper = (Mapper) config.getGroovyScriptObject("mapper.fix.version.to.release");
         connect();
     }
 
     private void connect() throws IssueManagementException {
 
         jiraURL = config.getProperty("jira.url", "http://example.com");
         String username = config.getProperty("jira.username", "nobody");
         String password = config.getProperty("jira.password", "nopassword");
         LOG.info("Connecting to " + jiraURL + " as " + username);
         try {
             jiraSoapService = new JiraSoapService(jiraURL, username, password);
         } catch (Throwable t) {
             throw new IssueManagementException("Unable to log in to Jira at " + jiraURL + " as user " + username, t);
         }
         try {
             getParentService = new GetParentService(jiraURL, username, password);
             LOG.info("Using optional GetParent SOAP web service");
         } catch (Throwable t) {
             LOG.info("GetParent SOAP web service is not installed or authentication failed");
             getParentService = null;
             preFetchIssues();
         }
     }
 
     private void preFetchIssues() throws IssueManagementException {
 
         try {
             String jql = config.getProperty("jira.jql.issues.allowed");
             int jqlMax = Integer.parseInt(config.getProperty("jira.jql.issues.allowed.max", "1000"));
             LOG.info("Running JQL query (max. " + jqlMax + " issues): " + jql);
             RemoteIssue[] remoteIssues = jiraSoapService.getService().getIssuesFromJqlSearch(
                     jiraSoapService.getToken(), jql, jqlMax);
             LOG.info("Processing " + remoteIssues.length + " issues returned by JQL query");
             for (RemoteIssue remoteIssue : remoteIssues) {
                 String issueKey = remoteIssue.getKey();
                 mappedRemoteIssues.put(issueKey, remoteIssue);
                 RemoteIssue[] remoteSubTasks = jiraSoapService.getService().getIssuesFromJqlSearch(
                         jiraSoapService.getToken(), "parent = " + issueKey, 500);
                 for (RemoteIssue remoteSubTask : remoteSubTasks) {
                     String subTaskKey = remoteSubTask.getKey();
                     if (mappedRemoteIssues.get(subTaskKey) == null) {
                         mappedRemoteIssues.put(subTaskKey, remoteSubTask);
                     }
                     subTaskParents.put(subTaskKey, issueKey);
                 }
             }
             LOG.info("Processed " + mappedRemoteIssues.size()
                     + " issues - added subtasks might have inflated this figure");
         } catch (Throwable t) {
             throw new IssueManagementException("Unable to pre-fetch issues", t);
         }
     }
 
     public IssueData getIssueData(String issueKey) {
 
         String subTaskKey = null;
         if (parentIssues.get(issueKey) == null) {
             String parentKey = subTaskParents.get(issueKey);
             if (parentKey == null) {
                 if (getParentService != null) {
                     try {
                         parentKey = getParentService.getParent(issueKey);
                         if (parentKey != null) {
                             subTaskParents.put(issueKey, parentKey);
                         }
                     } catch (Exception e) {
                         warnings.add(new JiraParentIssueFetchWarning(jiraURL, issueKey));
                     }
                 }
             }
             if (parentKey != null) {
                 if (warnedSubTasks.get(issueKey) == null) {
                     warnings.add(new JiraSubTaskMappingWarning(jiraURL, issueKey, parentKey));
                     warnedSubTasks.put(issueKey, issueKey);
                 }
                 subTaskKey = issueKey;
                 issueKey = parentKey;
             }
         }
         IssueData issueData = parentIssues.get(issueKey);
         if (issueData == null) {
             RemoteIssue remoteIssue = mappedRemoteIssues.get(issueKey);
             if (remoteIssue == null) {
                 try {
                     remoteIssue = jiraSoapService.getService().getIssue(jiraSoapService.getToken(), issueKey);
                     mappedRemoteIssues.put(issueKey, remoteIssue);
                 } catch (Exception e) {
                     warnings.add(new JiraIssueNotFoundWarning(jiraURL, issueKey));
                 }
             }
             if (remoteIssue != null) {
                 List<String> issueReleases = new Vector<String>();
                 Map<String, String> issueReleaseMap = new HashMap<String, String>();
                 RemoteVersion[] fixVersions = remoteIssue.getFixVersions();
                if (fixVersions.length > 0) {
                     for (RemoteVersion remoteVersion : fixVersions) {
                         String remoteVersionName = remoteVersion.getName();
                         String release = releases.get(remoteVersionName);
                         if (release == null) {
                            release = fixVersionMapper.map(remoteVersionName);
                            if (release != null) {
                                warnings.add(new JiraVersionMappingWarning(jiraURL, issueKey, remoteVersionName,
                                        release));
                                releases.put(remoteVersionName, release);
                             }
                         }
                         issueReleaseMap.put(release, release);
                     }
                 }
                 for (String release : issueReleaseMap.keySet()) {
                     issueReleases.add(release);
                 }
                 if (issueReleases.size() > 1) {
                     warnings.add(new JiraIssueWithMultipleReleasesWarning(jiraURL, issueKey, issueReleases));
                 }
                 String typeId = remoteIssue.getType();
                 String typeName = issueTypes.get(typeId);
                 if (typeName == null) {
                     typeName = config.getProperty("jira.type.map.id." + typeId, "housekeeping");
                     warnings.add(new JiraIssueTypeMappingWarning(jiraURL, issueKey, typeId, typeName));
                     issueTypes.put(typeId, typeName);
                 }
                 RemoteComponent[] remoteComponents = remoteIssue.getComponents();
                 List<String> components = new Vector<String>();
                 for (RemoteComponent remoteComponent : remoteComponents) {
                     components.add(remoteComponent.getName());
                 }
                 issueData = new IssueData(issueKey, typeName, remoteIssue.getSummary(), remoteIssue.getCreated()
                         .getTime(), remoteIssue.getUpdated().getTime(), components, issueReleases);
                 parentIssues.put(issueKey, issueData);
             }
         }
         if (issueData != null && subTaskKey != null) {
             issueData.putSubTaskKey(subTaskKey);
         }
         return issueData;
     }
 }
