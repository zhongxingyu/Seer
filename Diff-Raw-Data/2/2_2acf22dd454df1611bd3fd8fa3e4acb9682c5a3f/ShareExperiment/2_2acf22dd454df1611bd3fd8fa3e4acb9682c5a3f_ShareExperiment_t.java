 package pals.actions.experiment;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.apache.struts2.interceptor.validation.SkipValidation;
 
 import pals.actions.UserAwareAction;
 import pals.entity.Experiment;
 import pals.entity.User;
 import pals.service.ExperimentService;
 
 public class ShareExperiment extends UserAwareAction
 {
 	static final Logger log = Logger.getLogger(ShareExperiment.class);
 	
     String LIST = "list";
     String DENIED = "denied";
     String INPUT = "input";
     String SAVE = "save";
     
     Integer experimentId;
     Experiment experiment;
     List<User> allUsers;
     List<String> selected = new ArrayList<String>();
     
     ExperimentService experimentService;
     
     String shareWithAll;
     
     public String load()
     {
         if( getExperimentId() != null )
         {
         	experiment = experimentService.getExperiment(getExperimentId());
             log.debug("Loaded experiment: "+experiment);
         	if( experiment.getOwner().equals(getUser()) )
         	{
         		allUsers = getUserService().getAllUsers();
         		for( User user : allUsers )
         		{
         			if( experiment.getSharedList().contains(user) )
         			{
         				user.setShared(true);
         			}
         			else
         			{
         				user.setShared(false);
         			}
         		}
         	}
         	else
         	{
         		return DENIED;
         	}
         }
     	return LIST;
     }
     
     @SkipValidation
     public String list()
     {
         return load();
     }
     
     @SkipValidation
     public String denied()
     {
     	return DENIED;
     }
     
     @SkipValidation
     public String input()
     {
     	load();
     	experimentService.setTheseUsersAsShared(selected, experiment);
    	if( shareWithAll != null && shareWithAll.equals("true") ) experiment.setShareWithAll(true);
     	else experiment.setShareWithAll(false);
     	experimentService.update(experiment);
     	//load();
     	return INPUT;
     }
 
 	public Integer getExperimentId() {
 		return experimentId;
 	}
 
 	public void setExperimentId(Integer experimentId) {
 		this.experimentId = experimentId;
 	}
 
 	public ExperimentService getExperimentService() {
 		return experimentService;
 	}
 
 	public void setExperimentService(ExperimentService experimentService) {
 		this.experimentService = experimentService;
 	}
 
 	public Experiment getExperiment() {
 		return experiment;
 	}
 
 	public void setExperiment(Experiment experiment) {
 		this.experiment = experiment;
 	}
 
 	public List<User> getAllUsers() {
 		return allUsers;
 	}
 
 	public void setAllUsers(List<User> allUsers) {
 		this.allUsers = allUsers;
 	}
 
 	public List<String> getSelected() {
 		return selected;
 	}
 
 	public void setSelected(List<String> selected) {
 		this.selected = selected;
 	}
 
 	public String getShareWithAll() {
 		return shareWithAll;
 	}
 
 	public void setShareWithAll(String shareWithAll) {
 		this.shareWithAll = shareWithAll;
 	}
 }
