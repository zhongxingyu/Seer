 package com.maestrodev;
 
 import com.maestrodev.jira.create.Project;
 import com.maestrodev.jira.create.Fields;
 import com.maestrodev.jira.create.JiraCreateResponse;
 import com.atlassian.jira.rest.client.JiraRestClient;
 import com.atlassian.jira.rest.client.NullProgressMonitor;
 import com.atlassian.jira.rest.client.domain.Issue;
 import com.atlassian.jira.rest.client.domain.Transition;
 import com.atlassian.jira.rest.client.domain.input.TransitionInput;
 
 import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
 
 import com.maestrodev.jira.create.Issuetype;
 import com.maestrodev.jira.create.JiraCreate;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.org.apache.xml.internal.security.utils.Base64;
 import java.net.URI;
 import java.util.List;
 import java.util.Map;
 import javax.naming.AuthenticationException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 public class JiraWorker
     extends MaestroWorker
 {
 
     private final Logger logger = LoggerFactory.getLogger( this.getClass() );
     private final String CREATE_ISSUE_PATH = "rest/api/2/issue/";
     
     
     private String getWebPath(){
       if(getField("web_path") == null)
         return "/";
       return "/" + getField("web_path").replaceAll("\\/$", "").replaceAll("^\\/", "");
     }
     
     private JiraCreate getJiraIssue(){
       JiraCreate jiraCreate = new JiraCreate();
       Fields fields = new Fields();
       Project project = new Project();
       project.setKey(getField("project_key"));
       fields.setSummary(getField("summary"));
       fields.setDescription(getField("description"));
       Issuetype issuetype = new Issuetype();
       issuetype.setName(getField("issue_type_name"));
       fields.setIssuetype(issuetype);
       fields.setProject(project);
       jiraCreate.setFields(fields);
       
       return jiraCreate;
     }
     
     /**
      * update an issue in Jira
      */
     public void createIssue()
     {
       try{
         logger.info("Creating Issue In JIRA");
         writeOutput("Creating An Issue In JIRA\n");
         
         boolean useSsl = Boolean.parseBoolean(this.getField("use_ssl"));
         
         URI jiraServerUri = new URI(
            ("http" + (useSsl ? "s" : "")),
            null,
            this.getField("host"),
            Integer.parseInt(this.getField("port")),
            this.getWebPath() + CREATE_ISSUE_PATH,
            null,
            null);
         
        
         String auth = String.valueOf(Base64.encode((this.getField("username") + 
                 ":" + this.getField("password")).getBytes()));
         ObjectMapper mapper = new ObjectMapper();
         
         
         
         String data = mapper.writeValueAsString(getJiraIssue());
         Client client = Client.create();
         WebResource webResource = client.resource(jiraServerUri.toString());
         ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json")
                     .accept("application/json").post(ClientResponse.class, data);
         int statusCode = response.getStatus();
         
         String body = response.getEntity(String.class);
         JiraCreateResponse createResponse = mapper.readValue(body, JiraCreateResponse.class);
         if (statusCode == 401) {
           throw new AuthenticationException("Invalid Username or Password");
         } else if (statusCode == 404) {
           throw new Exception("Rest Endpoint Not Found, Make Sure Jira Is "
                   + "Atleast Version 5 And Accept Remote API Calls Is Enabled");
         }else if(statusCode == 201){
           writeOutput("Successfully Created An Issue In Jira\n");
           writeOutput("Issue Key :: " + createResponse.getKey() + "\n");
          
          String link = (new URI(
           ("http" + (useSsl ? "s" : "")),
           null,
           this.getField("host"),
           Integer.parseInt(this.getField("port")),
           "",
           null,
           null)).toASCIIString() + "/browse/" +
                  createResponse.getKey();
          writeOutput("Link :: " + link + "\n");
          addLink("Issue " + createResponse.getKey(), link);
           
           setField("jira", mapper.convertValue(createResponse, Map.class));
         } else {
          String errorMessage = "";
           if(createResponse.getErrorMessages() != null) {
             for(String message : createResponse.getErrorMessages()) {
               errorMessage += message + "\n";
             }

          }
          
          if(createResponse.getErrors() != null){
            for(Object key : createResponse.getErrors().keySet()) {
              errorMessage += key + " :: " + createResponse.getErrors().get(key) + "\n";
            }
           }
            
          throw new Exception(errorMessage);          
         }
       }catch(Exception e) {
         this.setError("Failed To Create Issue In JIRA " + e.getMessage());
       }
     }
     
     
     
     
     /**
      * update an issue in Jira
      */
     public void transitionIssues()
     {
       try{
         logger.info("Transitioning Issue In JIRA");
         writeOutput("Transitioning An Issue In JIRA\n");
         
     
         JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
         URI jiraServerUri = new URI(
            ("http" + (Boolean.parseBoolean(this.getField("use_ssl")) ? "s" : "")),
            null,
            this.getField("host"),
            Integer.parseInt(this.getField("port")),
            this.getWebPath(),
            null,
            null);
         
         JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri,
                 this.getField("username"),
                 this.getField("password"));
         NullProgressMonitor pm = new NullProgressMonitor();
         
         if(getFields().get("issue_keys") == null || ((List)getFields().get("issue_keys")).isEmpty()) {
           throw new Exception("Null Or Empty Issue Key List");
         }
         List<String> issueKeys = (List<String>) getFields().get("issue_keys");
         
         for(String issueKey : issueKeys){        
           transitionIssueToTransitionName(restClient, issueKey, pm);
         }
       }catch(Exception e) {
           this.setError("Failed To Transition Issue In JIRA " + e.getMessage());
       }
     }
 
   private void transitionIssueToTransitionName(JiraRestClient restClient, String issueKey, NullProgressMonitor pm) throws Exception {
     Issue issue = restClient.getIssueClient().getIssue(issueKey, pm);
     if(issue == null)
       throw new Exception("Issue Not Found - " + issueKey);
 
 
     final Iterable<Transition> transitions = restClient.getIssueClient().getTransitions(issue, pm);
     Transition transition = getTransitionByName(transitions, getField("transition_name"));
     if(transition == null)
       throw new Exception("Transition Not Found - " + getField("transition_name"));
     TransitionInput transitionInput = new TransitionInput(transition.getId());
     restClient.getIssueClient().transition(issue, transitionInput, pm);
 
     writeOutput("Successfully Transitioned Issue " + issueKey + " To " + getField("transition_name") + "\n");
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
