 package com.globalsight.tools;
 
 import java.net.URL;
 import java.rmi.RemoteException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 
 import net.sundell.snax.SNAXUserException;
 
 import com.globalsight.www.webservices.Ambassador;
 import com.globalsight.www.webservices.AmbassadorServiceLocator;
 
 public class WebService {
 
     private String url;
     private Ambassador service;
     private String authToken;
     private XMLInputFactory factory;
     
     public WebService(String baseUrl, XMLInputFactory factory) {
         StringBuilder sb = new StringBuilder();
         sb.append(baseUrl);
         if (!baseUrl.endsWith("/")) {
             sb.append("/");
         }
         sb.append("/services/AmbassadorWebService");
         url = sb.toString();
         this.factory = factory;
     }
     
     public String login(String username, String password) throws RemoteException {
         return getService().login(username, password);
     }
     
     public List<FileProfile> getFileProfiles() throws RemoteException {
         try {
             String fileProfileInfoEx = getService().getFileProfileInfoEx(getToken());
             return new FileProfilesParser(factory).parse(fileProfileInfoEx);
         }
         catch (XMLStreamException e) {
             throw new RuntimeException(e);
         }
         catch (SNAXUserException e) {
             throw new RuntimeException(e);
         }
     }
 
     public List<Job> getJobs() throws RemoteException {
         try {
             String jobsXml = getService().fetchJobsPerCompany(getToken());
             return new JobsParser(factory).parse(jobsXml);
         }
         catch (XMLStreamException e) {
  
             throw new RuntimeException(e);
         }
         catch (SNAXUserException e) {
             throw new RuntimeException(e);
         }
     }
     
     // Argument to this needs to be a WFId from a job
     public String getWorkflow(Long id) throws RemoteException {
     	String taskXml = 
     		getService().getCurrentTasksInWorkflow(getToken(), id);
     	System.out.println(taskXml);
     	/* Task XML
  <?xml version="1.0" encoding="UTF-8" ?>
   <tasksInWorkflow>
     <workflowId>9</workflowId>
     <task>
             <id>258</id>
             <name>Translation1_1000</name>
             <state>ACTIVE</state>
     </task>     
   </tasksInWorkflow>
 */  
     	// note: nullable name field
     	//String taskXml2 = getService().getTasksInJob(getToken(), 1008, "Translation1_1000");
     	//System.out.println(taskXml2);
     	/* getTasksInJob output -- note this requires a user to be both admin + pm
 <?xml version="1.0" encoding="UTF-8" ?>
  <tasksInJob>
      <jobId>1008</jobId>
      <task>     
              <id>258</id>
              <workflowId>9</workflowId>
              <name>Translation1_1000</name>
              <state>ACTIVE</state>
              <estimatedAcceptanceDate>2/21/11 5:00:00 PM UTC</estimatedAcceptanceDate>
              <assignees>All qualified users</assignees>
              <acceptedDate></acceptedDate>
              <estimatedCompletionDate>2/22/11 5:00:00 PM UTC</estimatedCompletionDate>
              <completedDate></completedDate>
      </task>    
  </tasksInJob>
     	 */
         return getService().fetchWorkflowRelevantInfo(getToken(), id.toString());
         /* WF XML:
 <?xml version="1.0" encoding="UTF-8" ?>
 <WorkflowInfo>  
     <workflowId>9</workflowId>
     <targetLocale>fr_FR</targetLocale>
     <state>DISPATCHED</state>
     <percentageCompletion>0</percentageCompletion>
     <currentActivity>Translation1</currentActivity>
     <estimatedTranslateCompletionDate>2/24/11 5:00:00 PM UTC</estimatedTranslate
 CompletionDate>
     <estimatedCompletionDate>2/24/11 5:00:00 PM UTC</estimatedCompletionDate>
     <workflowPriority>3</workflowPriority>
     <wordCountSummary>  
             <leverageOption>Leverage in context matches</leverageOption>
             <100%>0</100%>
             <95%-99%>0</95%-99%>
             <85%-94%>0</85%-94%>
             <75%-84%>0</75%-84%>
             <noMatch>196</noMatch>
             <repetitions>3</repetitions>
             <InContextMatches>0</InContextMatches>
             <total>199</total>
     </wordCountSummary>
 </WorkflowInfo>
 
          */
     }
     
     public String getUniqueJobName(String jobName) throws RemoteException {
         return getService().getUniqueJobName(getToken(), jobName);
     }
     
     // webService.uploadFile(filePath, jobName, fileProfileId, bytes);
     public void uploadFile(String path, String jobName, String fpId, 
                 byte[] data) throws RemoteException {
         HashMap<String, Object> args = new HashMap<String, Object>();
         args.put("accessToken", getToken());
         args.put("filePath", path);
         args.put("jobName", jobName);
         args.put("fileProfileId", fpId);
         args.put("bytes", data);
         getService().uploadFile(args);
     }
     
     /**
      * Create a job, using the default target locales for the 
      * file profile.
      */
     public void createJob(String jobName, List<String> filePaths,
                 FileProfile fileProfile) throws RemoteException {
          getService().createJob(getToken(), jobName, "", 
                 serializeStrings(filePaths), fileProfile.getId(),
                 serializeStrings(fileProfile.getTargetLocales()));
          /* For reference, this is the minimal attr xml
          <?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n<attributes/>
          */
     }
 
    private String serializeStrings(Collection<String> strings) {
         StringBuilder sb = new StringBuilder();
         boolean first = true;
         for (String s : strings) {
             if (!first) {
                sb.append('|');
             }
             else {
                 first = false;
             }
             sb.append(s);
         }
         return sb.toString();
     }
     
     protected Ambassador getService() {
         if (service != null) {
             return service;
         }
         try {
             AmbassadorServiceLocator locator = new AmbassadorServiceLocator();
             this.service = locator.getAmbassadorWebService(new URL(url));
             return service;
         }
         catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
     
     private String getToken() {
         if (authToken == null) {
             throw new IllegalStateException("No auth token");
         }
         return authToken;
     }
     
     public void setToken(String token) {
         this.authToken = token;
     }
 }
