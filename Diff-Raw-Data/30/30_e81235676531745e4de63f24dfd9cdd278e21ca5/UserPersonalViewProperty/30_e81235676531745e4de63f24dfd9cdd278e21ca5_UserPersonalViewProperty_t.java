 package de.fspengler.hudson.pview;
 
 import hudson.Extension;
 import hudson.ExtensionPoint;
 import hudson.model.Describable;
 import hudson.model.Hudson;
 import hudson.model.User;
 import hudson.model.UserProperty;
 import hudson.model.UserPropertyDescriptor;
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.export.Exported;
 import org.kohsuke.stapler.export.ExportedBean;
 
 /**
  * 
  * @author Tom Spengler
  */
 
 @ExportedBean(defaultVisibility = 999)
 public class UserPersonalViewProperty extends UserProperty implements Describable<UserProperty>, ExtensionPoint{
 
 	
     private String pViewExpression="";
     private boolean pViewBuildNoQueue=false;
     private boolean pViewBuildNoExecutor=false;
 
 	private boolean pcStatus=true;
     private boolean pcWeather=true;
     private boolean pcJob=true;
     private boolean pcLastSuccess=true;
     private boolean pcLastFailure=true;
     private boolean pcLastStable=true;
 	private boolean pcLastDuration=true;
 	private boolean pcConsoleView=true;
 	private boolean pcBuildButton=true;
 	private String treeSplitChar  ="-";
     private int treePosition=0;
     private int stepInNumberJobs = 30; 
    private static int DEF_STEPIN_NUMBER = 30;
    
 	@DataBoundConstructor
     public UserPersonalViewProperty(String pViewExpression, Boolean pViewBuildNoExecutor, Boolean pViewBuildNoQueue
     		,Boolean pcStatus, Boolean pcWeather, Boolean pcJob, Boolean pcLastSuccess, Boolean pcLastFailure,
     		Boolean pcLastStable,  Boolean pcLastDuration, Boolean pcConsoleView, Boolean pcBuildButton, String treeSplitChar,
     		int treePosition, int stepInNumberJobs) {

		this.pViewExpression = pViewExpression  ;
         this.pViewBuildNoExecutor=Boolean.TRUE.equals(pViewBuildNoExecutor);
         this.pViewBuildNoQueue=Boolean.TRUE.equals(pViewBuildNoQueue);
         this.pcStatus=Boolean.TRUE.equals(pcStatus);
         this.pcWeather=Boolean.TRUE.equals(pcWeather);
         this.pcJob=Boolean.TRUE.equals(pcJob);
         this.pcLastSuccess=Boolean.TRUE.equals(pcLastSuccess);
         this.pcLastFailure=Boolean.TRUE.equals(pcLastFailure);
         this.pcLastStable=Boolean.TRUE.equals(pcLastStable);
         this.pcLastDuration=Boolean.TRUE.equals(pcLastDuration);
         this.pcConsoleView=Boolean.TRUE.equals(pcConsoleView);
         this.pcBuildButton=Boolean.TRUE.equals(pcBuildButton);
         checkEmptyPList();
         this.treeSplitChar   = treeSplitChar;
 		checkEmptyTreeChar();
 		this.treePosition = treePosition;
		if (stepInNumberJobs > 0){
			this.stepInNumberJobs = stepInNumberJobs;
		} else {
			this.stepInNumberJobs = DEF_STEPIN_NUMBER;
		}
		
     }
     
     private void checkEmptyPList(){
         if (!this.pcStatus && !this.pcWeather && !this.pcJob && !this.pcLastSuccess && !this.pcLastFailure
         		&& !this.pcLastStable && !this.pcLastDuration && !this.pcConsoleView && !this.pcBuildButton){
         	this.pcStatus=true;
         	this.pcWeather=true;
         	this.pcJob=true;
         	this.pcLastSuccess=true;
         	this.pcLastFailure=true;
         	this.pcLastStable=true;
         	this.pcLastDuration=true;
         	this.pcConsoleView=true;
         	this.pcBuildButton=true;
         	
         }
     }
 
     public UserPropertyDescriptor getDescriptor() {
     	// descriptor must be of the UserPropertyDescriptor type
             return (UserPropertyDescriptor)Hudson.getInstance().getDescriptorByType(UserPersonalViewPropertyDescriptor.class);
         
     }
 
     @Exported
     public User getUser() {
         return user;
     }
 
     @Exported
     public int getStepInNumberJobs() {
    	if (stepInNumberJobs > 0){
    		return stepInNumberJobs;
    	}  else {
			return DEF_STEPIN_NUMBER;
		}
 	}
 
 	public void setStepInNumberJobs(int stepInNumberJobs) {
		if (stepInNumberJobs > 0){
			this.stepInNumberJobs = stepInNumberJobs;
		} else {
			this.stepInNumberJobs = DEF_STEPIN_NUMBER;
		}
 	}
 	
     @Exported
     public int getTreePosition() {
 		return treePosition;
 	}
 
 	public void setTreePosition(int treePosition) {
 		
 		this.treePosition = treePosition;
 	}
 	
     @Exported
     public String getPViewExpression() {
         return pViewExpression;
     }
 
     public void setPViewExpression(String pViewExpression) {
         this.pViewExpression = pViewExpression;
     }
     
     @Exported
 	public boolean isPViewBuildNoQueue() {
 		return pViewBuildNoQueue;
 	}
 
 	public void setPViewBuildNoQueue(boolean pViewBuildNoQueue) {
 		this.pViewBuildNoQueue = pViewBuildNoQueue;
 	}
 
     @Exported
     public boolean isPViewBuildNoExecutor() {
 		return pViewBuildNoExecutor;
 	}
 
 	public void setPViewBuildNoExecutor(boolean pViewBuildNoExecutor) {
 		this.pViewBuildNoExecutor = pViewBuildNoExecutor;
 	}
 
     
     @Exported
     public boolean isPcStatus() {
     	checkEmptyPList();
 		return pcStatus;
 	}
 
 	public void setPcStatus(boolean pcStatus) {
 		this.pcStatus = pcStatus;
 	}
 
     @Exported
 	public boolean isPcWeather() {
     	checkEmptyPList();
     	return pcWeather;
 	}
 
 	public void setPcWeather(boolean pcWeather) {
 		this.pcWeather = pcWeather;
 	}
 
     @Exported
 	public boolean isPcJob() {
     	checkEmptyPList();
     	return pcJob;
 	}
 
 	public void setPcJob(boolean pcJob) {
 		this.pcJob = pcJob;
 	}
 
     @Exported
 	public boolean isPcLastSuccess() {
     	checkEmptyPList();
 		return pcLastSuccess;
 	}
 
 	public void setPcLastSuccess(boolean pcLastSuccess) {
 		this.pcLastSuccess = pcLastSuccess;
 	}
 
     @Exported
 	public boolean isPcLastFailure() {
     	checkEmptyPList();
 		return pcLastFailure;
 	}
 
 	public void setPcLastFailure(boolean pcLastFailure) {
 		this.pcLastFailure = pcLastFailure;
 	}
 
     @Exported
 	public boolean isPcLastStable() {
     	checkEmptyPList();
 		return pcLastStable;
 	}
 
 	public void setPcLastStable(boolean pcLastStable) {
 		this.pcLastStable = pcLastStable;
 	}
 
     @Exported
 	public boolean isPcLastDuration() {
     	checkEmptyPList();
 		return pcLastDuration;
 	}
 
 	public void setPcLastDuration(boolean pcLastDuration) {
 		this.pcLastDuration = pcLastDuration;
 	}
 
     @Exported
 	public boolean isPcConsoleView() {
     	checkEmptyPList();
 		return pcConsoleView;
 	}
 
 	public void setPcConsoleView(boolean pcConsoleView) {
 		this.pcConsoleView = pcConsoleView;
 	}
 
     @Exported
 	public boolean isPcBuildButton() {
     	checkEmptyPList();
 		return pcBuildButton;
 	}
 
 	public void setPcBuildButton(boolean pcBuildButton) {
 		this.pcBuildButton = pcBuildButton;
 	}
 
 	@Exported
     public String getTreeSplitChar() {
 		checkEmptyTreeChar();
 		return treeSplitChar;
 	}
 
 	public void setTreeSplitChar(String treeSplitChar) {
 		this.treeSplitChar = treeSplitChar;
 	}
 
 	private void checkEmptyTreeChar() {
 		if (this.treeSplitChar == null || this.treeSplitChar.length() == 0){
         	this.treeSplitChar = "-";
         }
 	}
 
 	@Extension
 	public static final class UserPersonalViewPropertyDescriptor extends UserPropertyDescriptor {
 
 	    public UserPersonalViewPropertyDescriptor() {
 	        super(UserPersonalViewProperty.class);
 	    }
 
 	    @Override
 	    public String getDisplayName() {
 	        return "personal view";
 	    }
 	    
 	    @Override
 	    public UserPersonalViewProperty newInstance(StaplerRequest req, JSONObject formData) throws hudson.model.Descriptor.FormException {
 //	        if (formData.has("pViewExpression")) {
 	            return req.bindJSON(UserPersonalViewProperty.class, formData);
 //	        } else {
 //	            return new UserPersonalViewProperty();
 //	        }
 	    }
 
 	    @Override
 	    public UserProperty newInstance(User arg0) {
 	        return null;
 	    }
 	}
 
 }
