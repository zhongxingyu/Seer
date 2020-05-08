 package wfm.task;
 
 import javax.ejb.Stateful;
 import javax.enterprise.context.ConversationScoped;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.activiti.cdi.BusinessProcess;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 @Stateful
 @Named
 @ConversationScoped
 public class CourseApprovalTask {
 	
 	private static final Logger log = LoggerFactory.getLogger(LoginTask.class);
 	
 	@Inject
 	private BusinessProcess businessProcess;
 	
 	public void goToAddCourse(String taskId) {
 		log.info("going to add course");
 		businessProcess.startTask(taskId);
 		businessProcess.setVariable("approvalAction", "add");
 		businessProcess.completeTask();
 		
 	}
 	public void goToDeleteCourse(String taskId) {
 		log.info("going to delete course ");
 		businessProcess.startTask(taskId);
		log.info(">>>> routing to delete course");
		log.info("(process id=) " + businessProcess.getProcessInstanceId());
 		businessProcess.setVariable("approvalAction", "delete");
 		businessProcess.completeTask();
 		
 	}
 	public String logout(String taskId) {
 		
 		log.info("going to logout");
 		businessProcess.startTask(taskId);
 		businessProcess.setVariable("approvalAction", "logout");
 		businessProcess.completeTask();
 		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
         return "login.xhtml?faces-redirect=true";
     }
 
 }
