 package com.maestrodev;
 
 import com.atlassian.jira.rest.client.JiraRestClient;
 import com.atlassian.jira.rest.client.NullProgressMonitor;
 import com.atlassian.jira.rest.client.ProgressMonitor;
 import com.atlassian.jira.rest.client.domain.BasicIssue;
 import com.atlassian.jira.rest.client.domain.Issue;
 import com.atlassian.jira.rest.client.domain.Transition;
 import com.atlassian.jira.rest.client.domain.input.ComplexIssueInputFieldValue;
 import com.atlassian.jira.rest.client.domain.input.FieldInput;
 import com.atlassian.jira.rest.client.domain.input.IssueInput;
 import com.atlassian.jira.rest.client.domain.input.TransitionInput;
 
 import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
 
 import com.maestrodev.maestro.plugins.MaestroWorker;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class JiraWorker
         extends MaestroWorker {
   private final Log logger = LogFactory.getLog(this.getClass());
 
   private String getWebPath() {
     if (getField("web_path") == null) {
       return "/";
     }
     return "/" + getField("web_path").replaceAll("\\/$", "").replaceAll("^\\/", "");
   }
 
   private IssueInput getJiraIssue() {
     List<FieldInput> fields = new ArrayList<FieldInput>();
     Map<String, Object> project = new HashMap<String, Object>();
     Map<String, Object> issueType = new HashMap<String, Object>();
 
     project.put("key", getField("project_key"));
     issueType.put("name", getField("issue_type_name"));
 
     fields.add(new FieldInput("project", new ComplexIssueInputFieldValue(project)));
     fields.add(new FieldInput("summary", getField("summary")));
     fields.add(new FieldInput("description", getField("description")));
     fields.add(new FieldInput("issuetype", new ComplexIssueInputFieldValue(issueType)));
 
     return IssueInput.createWithFields(fields.toArray(new FieldInput[0]));
   }
 
   /**
    * update an issue in Jira
    * 
    * @throws IOException
    * @throws JsonMappingException
    * @throws JsonGenerationException
    * @throws URISyntaxException
    */
   public void createIssue() {
     logger.info("Creating Issue In JIRA");
     writeOutput("Creating An Issue In JIRA\n");
 
     JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
     URI jiraServerUri;
     try {
       jiraServerUri = buildUri("");
     } catch (URISyntaxException e) {
       setError("URI is not valid: " + buildUrl("") );
       return;
     }
 
     JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri,
             this.getField("username"),
             this.getField("password"));
     ProgressMonitor pm = new NullProgressMonitor();
 
     try {
         BasicIssue issue = restClient.getIssueClient().createIssue(getJiraIssue(), pm);
         writeOutput("Successfully Created An Issue In Jira\n");
         writeOutput("Issue Key :: " + issue.getKey() + "\n");
 
         String link;
         try {
           link = (buildUri("/browse/" + issue.getKey())).toASCIIString();
         } catch (URISyntaxException e) {
           setError("URI is not valid: " + buildUrl("/browse/" + issue.getKey()));
           return;
         }
         writeOutput("Link :: " + link + "\n");
         addLink("Issue " + issue.getKey(), link);
     }
     catch (Exception e) {
         logger.error("Error in Create Issue", e);
        setError("Problem creating issue in JIRA: " + e.getLocalizedMessage() + " (" + e.getClass().getName() + ")");
     }
   }
 
   private URI buildUri(String path) throws URISyntaxException {
     boolean useSsl = Boolean.parseBoolean(this.getField("use_ssl"));
 
     return (new URI(
             ("http" + (useSsl ? "s" : "")),
             null,
             this.getField("host"),
             Integer.parseInt(this.getField("port")),
             this.getWebPath() + path,
             null,
             null));
   }
 
   private String buildUrl(String path) {
     boolean useSsl = Boolean.parseBoolean(this.getField("use_ssl"));
     return "http" + (useSsl ? "s" : "") + "://" + this.getField("host") + ":" + this.getField("port") + "/" + this.getWebPath() + path;
   }
 
 
   /**
    * update an issue in Jira
    */
   @SuppressWarnings("unchecked")
   public void transitionIssues() {
     logger.info("Transitioning Issue In JIRA");
     writeOutput("Transitioning An Issue In JIRA\n");
 
 
     JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
     URI jiraServerUri;
     try {
       jiraServerUri = buildUri("");
     } catch (URISyntaxException e) {
       setError("URI is not valid: " + buildUrl("") );
       return;
     }
 
     JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri,
             this.getField("username"),
             this.getField("password"));
     ProgressMonitor pm = new NullProgressMonitor();
 
     List<String> issueKeys = (List<String>) getFields().get("issue_keys");
 
     if (issueKeys == null || issueKeys.isEmpty()) {
       throw new RuntimeException("Null Or Empty Issue Key List");
     }
 
     for (String issueKey : issueKeys) {
       Issue issue = restClient.getIssueClient().getIssue(issueKey, pm);
       if (issue == null) {
         setError("Issue Not Found: " + issueKey);
         return;
       }
 
       final Iterable<Transition> transitions = restClient.getIssueClient().getTransitions(issue, pm);
       String transitionName = getField("transition_name");
       Transition transition = getTransitionByName(transitions, transitionName);
       if (transition == null) {
         setError("Transition Not Found: " + transitionName);
         return;
       }
 
       transitionIssueToTransitionName(restClient, issue, transition, pm);
     }
   }
 
   private void transitionIssueToTransitionName(JiraRestClient restClient, Issue issue, Transition transition, ProgressMonitor pm) {
     TransitionInput transitionInput = new TransitionInput(transition.getId());
     restClient.getIssueClient().transition(issue, transitionInput, pm);
 
     writeOutput("Successfully Transitioned Issue " + issue.getKey() + " To " + transition.getName() + "\n");
   }
 
   private static Transition getTransitionByName(Iterable<Transition> transitions, String transitionName) {
     for (Transition transition : transitions) {
       if (transition.getName().equals(transitionName)) {
         return transition;
       }
     }
     return null;
   }
 }
