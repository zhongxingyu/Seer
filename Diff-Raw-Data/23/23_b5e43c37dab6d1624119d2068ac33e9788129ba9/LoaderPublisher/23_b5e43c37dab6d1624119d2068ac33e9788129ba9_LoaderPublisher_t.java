 package io.loader.jenkins;
 
 import com.cloudbees.plugins.credentials.CredentialsProvider;
 
 import io.loader.jenkins.api.LoaderAPI;
 import io.loader.jenkins.api.SummaryData;
 import io.loader.jenkins.api.TestData;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import jenkins.model.Jenkins;
 
 import org.apache.commons.lang.StringUtils;
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.Job;
 import hudson.model.BuildListener;
 import hudson.model.Item;
 import hudson.model.Result;
 import hudson.security.ACL;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Notifier;
 import hudson.tasks.Publisher;
 import hudson.util.FormValidation;
 import hudson.util.Secret;
 import hudson.util.ListBoxModel;
 import net.sf.json.JSONObject;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.Stapler;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  * Sample {@link Builder}.
  *
  * <p>
  * When the user configures the project and enables this builder,
  * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
  * and a new {@link LoaderPublisher} is created. The created
  * instance is persisted to the project configuration XML by using
  * XStream, so this allows you to use instance fields (like {@link #name})
  * to remember the configuration.
  *
  * <p>
  * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
  * method will be invoked. 
  *
  * @author
  */
 public class LoaderPublisher extends Notifier {
 	
 	private String apiKey;
 	
 	private String testId = "";
 
     private int errorFailedThreshold = 0;
 
     private int errorUnstableThreshold = 0;
 
     private int responseTimeFailedThreshold = 0;
 
     private int responseTimeUnstableThreshold = 0;
     
     private PrintStream logger;
 	
 	@DataBoundConstructor
     public LoaderPublisher(String apiKey,
             String testId,
             int errorFailedThreshold,
             int errorUnstableThreshold,
             int responseTimeFailedThreshold,
             int responseTimeUnstableThreshold) {
         this.apiKey = apiKey;
         this.errorFailedThreshold = errorFailedThreshold;
         this.errorUnstableThreshold = errorUnstableThreshold;
         this.responseTimeFailedThreshold = responseTimeFailedThreshold;
         this.responseTimeUnstableThreshold = responseTimeUnstableThreshold;
         this.testId = testId;
     }
 	
 	@Override
     public boolean perform(AbstractBuild build, Launcher launcher,
             BuildListener listener) throws InterruptedException, IOException {
 		logger = listener.getLogger();
         Result result; // Result.SUCCESS;
         String session;
         if ((result = validateParameters(logger)) != Result.SUCCESS) {
             return true;
         }
         String apiKeyId = StringUtils.defaultIfEmpty(getApiKey(), getDescriptor().getApiKey());
         String apiKey = null;
         for (LoaderCredential c : CredentialsProvider
                 .lookupCredentials(LoaderCredential.class, build.getProject(), ACL.SYSTEM)) {
             if (StringUtils.equals(apiKeyId, c.getId())) {
                 apiKey = c.getApiKey().getPlainText();
                 break;
             }
         }
         
         LoaderAPI loaderApi = new LoaderAPI(apiKey);
         String resTestResultId = loaderApi.runTest(getTestId());
         if (resTestResultId == null) {
         	logInfo("Invalid test information");
         	result = Result.NOT_BUILT;
             return false;
         }
         
         int lastPrint = 0;
         int interval = 5;
         
         while (true) {
         	TestData testInfo = loaderApi.getTest(getTestId());
         	
         	if (testInfo == null) {
         		logInfo("API return invalid test information");
             	result = Result.NOT_BUILT;
                 return false;
         	}
         	
         	if (testInfo.status.equalsIgnoreCase("running") || testInfo.status.equalsIgnoreCase("pending")) {
         		logInfo("Waiting for test results " + lastPrint + " sec");
         		if (testInfo.duration > 0 && (testInfo.duration + 60) < lastPrint) {
         			logInfo("API doesn't return test results");
                 	result = Result.NOT_BUILT;
                     return false;
         		} else {
         			lastPrint = lastPrint + interval; 
         			Thread.sleep(interval * 1000);
         		}
         	} else {
         		break;
         	}
         	
         	
         }
         
         Thread.sleep(10 * 1000);
         SummaryData testSummaryInfo = loaderApi.getTestSummaryData(getTestId(), resTestResultId);
         
         double thresholdTolerance = 0.00005;
         
         if (errorFailedThreshold >= 0 && testSummaryInfo.avgErrorRate - errorFailedThreshold > thresholdTolerance) {
             result = Result.FAILURE;
             logInfo("Test ended with " + Result.FAILURE + " on error percentage threshold");
         } else if (errorUnstableThreshold >= 0
                 && testSummaryInfo.avgErrorRate - errorUnstableThreshold > thresholdTolerance) {
         	logInfo("Test ended with " + Result.UNSTABLE + " on error percentage threshold");
             result = Result.UNSTABLE;
         }
 
         if (responseTimeFailedThreshold >= 0 && testSummaryInfo.avgResponseTime - responseTimeFailedThreshold > thresholdTolerance) {
             result = Result.FAILURE;
             logInfo("Test ended with " + Result.FAILURE + " on response time threshold");
 
         } else if (responseTimeUnstableThreshold >= 0
                 && testSummaryInfo.avgResponseTime - responseTimeUnstableThreshold > thresholdTolerance) {
             result = Result.UNSTABLE;
             logInfo("Test ended with " + Result.UNSTABLE + " on response time threshold");
         }
         	
     	LoaderBuildAction action = new LoaderBuildAction(build, getTestId(), resTestResultId);
         build.getActions().add(action);
         build.setResult(result);
         
 		return true;
 	}
 	
 	private void logInfo(String str) {
 		if (logger != null) {
 			logger.println("loader.io: " + str);
 		}
 	}
 	
 	private Result validateParameters(PrintStream logger) {
         Result result = Result.SUCCESS;
         if (errorUnstableThreshold >= 0 && errorUnstableThreshold <= 100) {
        	logInfo("Errors percentage greater than or equal to "
                     + errorUnstableThreshold + "% will be considered as "
                     + Result.UNSTABLE.toString().toLowerCase());
         } else {
        	logInfo("ERROR! percentage should be between 0 to 100");
             result = Result.NOT_BUILT;
         }
 
         if (errorFailedThreshold >= 0 && errorFailedThreshold <= 100) {
        	logInfo("Errors percentage greater than or equal to "
                     + errorFailedThreshold + "% will be considered as "
                     + Result.FAILURE.toString().toLowerCase());
         } else {
        	logInfo("ERROR! percentage should be between 0 to 100");
             result = Result.NOT_BUILT;
         }
 
         if (responseTimeUnstableThreshold >= 0) {
        	logInfo("Response time greater than or equal to "
                     + responseTimeUnstableThreshold + "millis will be considered as "
                     + Result.UNSTABLE.toString().toLowerCase());
         } else {
            logger.println("ERROR! percentage should be greater than or equal to 0");
             result = Result.NOT_BUILT;
         }
 
         if (responseTimeFailedThreshold >= 0) {
        	logInfo("Response time greater than or equal to "
                     + responseTimeFailedThreshold + "millis will be considered as "
                     + Result.FAILURE.toString().toLowerCase());
         } else {
        	logInfo("ERROR! percentage should be greater than or equal to 0");
             result = Result.NOT_BUILT;
         }
         return result;
 	}
 
 	public BuildStepMonitor getRequiredMonitorService() {
 		return BuildStepMonitor.BUILD;
 	}
 	
 	public String getApiKey() {
         return apiKey;
     }
 	
 	public int getResponseTimeFailedThreshold() {
         return responseTimeFailedThreshold;
     }
 
     public void setResponseTimeFailedThreshold(int responseTimeFailedThreshold) {
         this.responseTimeFailedThreshold = responseTimeFailedThreshold;
     }
 
     public int getResponseTimeUnstableThreshold() {
         return responseTimeUnstableThreshold;
     }
 
     public void setResponseTimeUnstableThreshold(int responseTimeUnstableThreshold) {
         this.responseTimeUnstableThreshold = responseTimeUnstableThreshold;
     }
     
     public int getErrorFailedThreshold() {
         return errorFailedThreshold;
     }
 
     public void setErrorFailedThreshold(int errorFailedThreshold) {
         this.errorFailedThreshold = Math.max(0, Math.min(errorFailedThreshold, 100));
     }
 
     public int getErrorUnstableThreshold() {
         return errorUnstableThreshold;
     }
 
     public void setErrorUnstableThreshold(int errorUnstableThreshold) {
         this.errorUnstableThreshold = Math.max(0, Math.min(errorUnstableThreshold,
                 100));
     }
 
     public String getTestId() {
         return testId;
     }
 
     public void setTestId(String testId) {
         this.testId = testId;
     }
 	
 	@Override
     public LoaderioPerformancePublisherDescriptor getDescriptor() {
         return DESCRIPTOR;
     }
 
     @Extension
     public static final LoaderioPerformancePublisherDescriptor DESCRIPTOR = new LoaderioPerformancePublisherDescriptor();
 	
 	public static final class DescriptorImpl
     	extends LoaderioPerformancePublisherDescriptor {
 	}
 	
 	public static class LoaderioPerformancePublisherDescriptor extends BuildStepDescriptor<Publisher> {
 		private String apiKey;
 
         public LoaderioPerformancePublisherDescriptor() {
             super(LoaderPublisher.class);
             load();
         }
         
      // Used by config.jelly to display the test list.
         public ListBoxModel doFillTestIdItems(@QueryParameter String apiKey) throws FormValidation {
             if (StringUtils.isBlank(apiKey)) {
                 apiKey = getApiKey();
             }
 
             Secret apiKeyValue = null;
             Item item = Stapler.getCurrentRequest().findAncestorObject(Item.class);
             for (LoaderCredential c : CredentialsProvider
                     .lookupCredentials(LoaderCredential.class, item, ACL.SYSTEM)) {
                 if (StringUtils.equals(apiKey, c.getId())) {
                 	apiKeyValue = c.getApiKey();
                     break;
                 }
             }
             ListBoxModel items = new ListBoxModel();
             if (apiKeyValue == null) {
                 items.add("No API Key", "-1");
             } else {
 	            LoaderAPI lda = new LoaderAPI(apiKeyValue.getPlainText());
 	
 	            try {
 	                Map<String, String> testList = lda.getTestList();
 	                if (testList == null){
 	                    items.add("Invalid API key ", "-1");
 	                } else if (testList.isEmpty()){
 	                    items.add("No tests", "-1");
 	                } else {
 	                    for (Map.Entry<String, String> test : testList.entrySet()) {
 	                        items.add(test.getValue(), test.getKey());
 	                    }
 	                }
 	            } catch (Exception e) {
 	                throw FormValidation.error(e.getMessage(), e);
 	            }
             }
             return items;
         }
         
         public ListBoxModel doFillApiKeyItems() {
             ListBoxModel items = new ListBoxModel();
             Set<String> apiKeys = new HashSet<String>();
 
             Item item = Stapler.getCurrentRequest().findAncestorObject(Item.class);
             if (item instanceof Job) {
                 List<LoaderCredential> global = CredentialsProvider
                         .lookupCredentials(LoaderCredential.class, Jenkins.getInstance(), ACL.SYSTEM);
                 if (!global.isEmpty() && !StringUtils.isEmpty(getApiKey())) {
                     items.add("Default API Key", "");
                 }
             }
             for (LoaderCredential c : CredentialsProvider
                     .lookupCredentials(LoaderCredential.class, item, ACL.SYSTEM)) {
                 String id = c.getId();
                 if (!apiKeys.contains(id)) {
                     items.add(StringUtils.defaultIfEmpty(c.getDescription(), id), id);
                     apiKeys.add(id);
                 }
             }
             return items;
         }
         
         public List<LoaderCredential> getCredentials(Object scope) {
             List<LoaderCredential> result = new ArrayList<LoaderCredential>();
             Set<String> apiKeys = new HashSet<String>();
 
             Item item = scope instanceof Item ? (Item) scope : null;
             for (LoaderCredential c : CredentialsProvider
                     .lookupCredentials(LoaderCredential.class, item, ACL.SYSTEM)) {
                 String id = c.getId();
                 if (!apiKeys.contains(id)) {
                     result.add(c);
                     apiKeys.add(id);
                 }
             }
             return result;
         }
 		
 		@Override
 		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
 			return true;
 		}
 
 		@Override
 		public String getDisplayName() {
 			return "Loader.io";
 		}
 		
 		@Override
         public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
             apiKey = formData.optString("apiKey");
             save();
             return true;
         }
 		
 		public String getApiKey() {
             List<LoaderCredential> credentials = CredentialsProvider
                     .lookupCredentials(LoaderCredential.class, Jenkins.getInstance(), ACL.SYSTEM);
             if (StringUtils.isBlank(apiKey) && !credentials.isEmpty()) {
                 return credentials.get(0).getId();
             }
             if (credentials.size() == 1) {
                 return credentials.get(0).getId();
             }
             for (LoaderCredential c: credentials) {
                 if (StringUtils.equals(c.getId(), apiKey)) {
                     return apiKey;
                 }
             }
             // API key is not valid any more
             return "";
         }
 		
 		public void setApiKey(String apiKey) {
 			this.apiKey = apiKey;
 	    }
 		
 	}
 
    
 
 }
 
