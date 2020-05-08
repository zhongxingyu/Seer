 package org.jenkinsci.plugins.rallyBuild;
 import hudson.EnvVars;
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.Descriptor;
 import hudson.model.AbstractDescribableImpl;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.Builder;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import net.sf.json.JSONObject;
 
 import org.jenkinsci.plugins.rallyBuild.rallyActions.Action;
 import org.jenkinsci.plugins.rallyBuild.rallyActions.CommentAction;
 import org.jenkinsci.plugins.rallyBuild.rallyActions.DefectStateAction;
 import org.jenkinsci.plugins.rallyBuild.rallyActions.ReadyAction;
 import org.jenkinsci.plugins.rallyBuild.rallyActions.StateAction;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 import com.rallydev.rest.RallyRestApi;
 
 public class RallyBuild extends Builder {
 
     public final String issueString;
 
     public final Boolean updateOnce;
     
     public String  issueRallyState; 
     public String  defectRallyState;
     public String  commentText;
     public Boolean issueReady;
     
     public String  preCommentText;
     public String  preIssueRallyState;
     public Boolean preReadyState;
     
     public Boolean changeRallyState =false;
     public Boolean changeDefectRallyState= false;
     public Boolean createComment    =false;
     public Boolean changeReady      =false;
     
 
     public Boolean preComment       =false;
     public Boolean preReady         =false;
     
     public Set<String> updatedIssues = new HashSet<String>();
     private static final Logger logger = Logger.getLogger(RallyBuild.class.getName());
     
     public List<PreRallyState> preRallyState;
 
     //Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
     @DataBoundConstructor
     public RallyBuild(List<PreRallyState> preRallyState, String issueString, Boolean updateOnce, PreCommentBlock preComment, 
     		 PreReadyBlock preReady, EnableReadyBlock changeReady,CreateCommentBlock createComment, ChangeStateBlock changeRallyState, ChangeDefectStateBlock changeDefectRallyState) {
         this.issueString = issueString;
         this.updateOnce=updateOnce;
         this.preRallyState=preRallyState;
         if(changeDefectRallyState!=null){
         	this.changeDefectRallyState=true;
         	this.defectRallyState=changeDefectRallyState.getIssueState();
         }
         if(changeRallyState!=null){
         	this.changeRallyState=true;
         	this.issueRallyState=changeRallyState.getIssueState();
         }
         if(createComment!=null){
         	this.createComment=true;
         	this.commentText=createComment.getComment();
         }
         
         if(changeReady!=null){
         	this.changeReady=true;
         	this.issueReady=changeReady.getIssueReady();
         }
         
 
         	if(preComment!=null){
         		this.preComment=true;
         		this.preCommentText = preComment.getComment();
         	}
         	if(preReady!=null){
         		this.preReady=true;
         		this.preReadyState=preReady.getIssueReady();
         	}
                 
         
        
     }
 
     
 
     @Override
     public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
     	
     	EnvVars env = build.getEnvironment(listener); 
         String expandedCommentText = env.expand(commentText);
         String expandedPreConditionText = env.expand(preCommentText);
         String expandedIssueString = env.expand(issueString);
     	List<Action> rallyActions = new ArrayList<Action>();
     	List<Action> preConditions = new ArrayList<Action>();
     	List<StateAction> preStates = new ArrayList<StateAction>();
     	
     	Rally rally = null;
     	
     	try {
     		logger.info("Server "+getDescriptor().getRallyServer());
     		RallyRestApi api = new RallyRestApi(new URI(getDescriptor().getRallyServer()), getDescriptor().getRallyUser(), getDescriptor().getRallyPassword());
 			rally = new Rally(api,listener);
 		} catch (URISyntaxException e) {
 			e.printStackTrace();
 		}
     	
     	if(rally!=null){
     		
     		logger.info("Pre Condition Comment "+createComment);
 	    	if(preComment){
 	    		CommentAction comment = new CommentAction(expandedPreConditionText);
 	    		preConditions.add(comment);
 	    	}
 	    	
 	    	logger.info("Pre Condition Ready "+changeReady);
 	    	if(preReady){
 	    		ReadyAction ready = new ReadyAction(preReadyState);
 	    		preConditions.add(ready);
 	    	}
 	    	
	    	logger.info("Pre RallyStates "+preRallyState.size());
 	    	if(preRallyState!=null && preRallyState.size()>0){
 	    		for(PreRallyState rallyState :preRallyState){
 	    			StateAction action = new StateAction(rallyState.getStateName());
 	    			preStates.add(action);
 	    		}
 	    	}
 	    	
     		logger.info("Create Comment "+createComment);
 	    	if(createComment){
 	    		CommentAction comment = new CommentAction(expandedCommentText);
 	    		rallyActions.add(comment);
 	    	}
 	    	
 	    	logger.info("Mark ready "+changeReady);
 	    	if(changeReady){
 	    		ReadyAction ready = new ReadyAction(issueReady);
 	    		rallyActions.add(ready);
 	    	}
 	    	
 	    	logger.info("Change State "+changeRallyState);
 	    	if(changeRallyState){
 	    		StateAction state = new StateAction(issueRallyState);
 	    		rallyActions.add(state);
 	    	}
 	    	
 	    	logger.info("Change State "+changeDefectRallyState);
 	    	if(changeDefectRallyState){
 	    		DefectStateAction defectState = new DefectStateAction(defectRallyState);
 	    		rallyActions.add(defectState);
 	    	}
 	    	
 	    	
 	    	
 	    	HashSet<String> issues = rally.getIssues(expandedIssueString);
 	    	rally.updateIssues(issues,preConditions,preStates,rallyActions,updateOnce);
     	}
     	return true;
     }
     
     public static class EnableReadyBlock
     {
         private Boolean issueReady;
 
         @DataBoundConstructor
         public EnableReadyBlock(Boolean issueReady)
         {
             this.issueReady = issueReady;
         }
 
 		public Boolean getIssueReady() {
 			return issueReady;
 		}
 
     }
     
     public static class ChangeStateBlock
     {
         private String issueRallyState;
 
         @DataBoundConstructor
         public ChangeStateBlock(String issueRallyState)
         {
             this.issueRallyState = issueRallyState;
         }
 
 		public String getIssueState() {
 			return issueRallyState;
 		}
 
     }
     
     public static class ChangeDefectStateBlock
     {
         private String defectRallyState;
 
         @DataBoundConstructor
         public ChangeDefectStateBlock(String defectRallyState)
         {
             this.defectRallyState = defectRallyState;
         }
 
 		public String getIssueState() {
 			return defectRallyState;
 		}
 
     }
     
     public static final class PreRallyState extends AbstractDescribableImpl<PreRallyState> implements Serializable{
     	/**
 		 * 
 		 */
 		private static final long serialVersionUID = 147571780733339453L;
 		private String stateName;
     	
     	@DataBoundConstructor
     	public PreRallyState(String stateName){
     		this.stateName=stateName;
     	}
 
 		public String getStateName() {
 			return stateName;
 		}
 		
 
 	    @Extension
 	    public static class DescriptorImpl extends Descriptor<PreRallyState> {
 	        public String getDisplayName() { return ""; }
 	    }
 	
     	
     }
     
     public static class CreateCommentBlock
     {
         private String commentText;
 
         @DataBoundConstructor
         public CreateCommentBlock(String commentText)
         {
             this.commentText = commentText;
         }
 
 		public String getComment() {
 			return commentText;
 		}
 
     }
     
     public static class PreCommentBlock
     {
         private String preCommentText;
 
         @DataBoundConstructor
         public PreCommentBlock(String preCommentText)
         {
             this.preCommentText = preCommentText;
         }
 
 		public String getComment() {
 			return preCommentText;
 		}
 
     }
     
     public static class PreReadyBlock
     {
         private Boolean preReadyState;
 
         @DataBoundConstructor
         public PreReadyBlock(Boolean preReadyState)
         {
             this.preReadyState = preReadyState;
         }
 
 		public Boolean getIssueReady() {
 			return preReadyState;
 		}
 
     }
     
     public static class PreRallyStateBlock
     {
         private String preIssueRallyState;
 
         @DataBoundConstructor
         public PreRallyStateBlock(String preIssueRallyState)
         {
             this.preIssueRallyState = preIssueRallyState;
         }
 
 		public String getIssueState() {
 			return preIssueRallyState;
 		}
 
     }
     
 
     // Overridden for better type safety.
     // If your plugin doesn't really define any property on Descriptor,
     // you don't have to do this.
     @Override
     public DescriptorImpl getDescriptor() {
         return (DescriptorImpl)super.getDescriptor();
     }
 
 
     @Extension // This indicates to Jenkins that this is an implementation of an extension point.
     public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
         /**
          * To persist global configuration information,
          * simply store it in a field and call save().
          *
          * <p>
          * If you don't want fields to be persisted, use <tt>transient</tt>.
          */
         private String rallyServer; 
         private String rallyPassword;
         private String rallyUser;
        
         public DescriptorImpl(){
 			load();
 		}
         
         public boolean isApplicable(Class<? extends AbstractProject> aClass) {
             // Indicates that this builder can be used with all kinds of project types 
             return true;
         }
 
         /**
          * This human readable name is used in the configuration screen.
          */
         public String getDisplayName() {
             return "Rally Builder";
         }
 
         @Override
         public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
             // To persist global configuration information,
             // set that to properties and call save().
             
         	//name          = formData.getString("name");
             rallyServer   = formData.getString("rallyServer");
             rallyUser     = formData.getString("rallyUser");
             rallyPassword = formData.getString("rallyPassword");
             
             // ^Can also use req.bindJSON(this, formData);
             //  (easier when there are many fields; need set* methods for this, like setUseFrench)
             save();
             return super.configure(req,formData);
         }
 
         /**
          * This method returns true if the global configuration says we should speak French.
          *
          * The method name is bit awkward because global.jelly calls this method to determine
          * the initial state of the checkbox by the naming convention.
          */
         
         public String getRallyServer(){
         	return rallyServer;
         }
         
         public String getRallyUser(){
         	return rallyUser;
         }
         
         public String getRallyPassword(){
         	return rallyPassword;
         }
         
 
     }
 }
 
