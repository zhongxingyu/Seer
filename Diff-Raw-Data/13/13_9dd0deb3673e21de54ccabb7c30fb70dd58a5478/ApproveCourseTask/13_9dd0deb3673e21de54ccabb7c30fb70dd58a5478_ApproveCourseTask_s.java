 package wfm.task;
 
 import javax.ejb.Stateful;
 import javax.enterprise.context.ConversationScoped;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.activiti.cdi.BusinessProcess;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import wfm.bean.ApproveCourseBean;
 import wfm.db.Course;
 import wfm.db.ACT_ID_USER;
 
 
 @Stateful
 @Named
 @ConversationScoped
 public class ApproveCourseTask {
 	
 	private static final Logger log = LoggerFactory.getLogger(LoginTask.class);
 
 	@Inject
 	private BusinessProcess businessProcess;
 
 	@PersistenceContext
 	private EntityManager entityManager;
 
 	@Inject
 	private ApproveCourseBean approveBean;
 	
 	
 	private Course courseToApprove;
 	private ACT_ID_USER userToApprove;
 
 	public void approveCourse(String taskId) {
 
 		businessProcess.startTask(taskId);
 		
 		try{
 			
 			userToApprove.getCourses().add(courseToApprove);
 
 			courseToApprove.getUsers().add(userToApprove);
 
 			entityManager.merge(courseToApprove);
 			entityManager.merge(userToApprove);
 			entityManager.getTransaction().commit();
 			entityManager.flush();		  
 
 
 		}catch(Exception e){
 			log.error("Error ApproveCourseTask: "+e.getMessage());
 		}
 
 		businessProcess.setVariable("approvalAction", "back");
 		businessProcess.completeTask();
 	}
 	
 	public String logout() {
         FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
         return "login.xhtml?faces-redirect=true";
     }
 
 	public Course getCourseToApprove() {
 		courseToApprove = businessProcess.getVariable("courseToApprove");			
 		return courseToApprove;
 	}
 
 	public void setCourseToApprove(Course courseToApprove) {
 		this.courseToApprove = courseToApprove;
 	}
 
 	public ACT_ID_USER getUserToApprove() {
 		userToApprove = businessProcess.getVariable("userToApprove");
 		
 		return userToApprove;
 	}
 
 	public void setUserToApprove(ACT_ID_USER userToApprove) {
 		this.userToApprove = userToApprove;
 	}
 
 }
