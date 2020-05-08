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
 		businessProcess.setVariable("approved", true);
 		
 		Course registeredCourse = businessProcess.getVariable("courseToApprove");
 		
 		 ACT_ID_USER trainerToBeNotified = entityManager.find(ACT_ID_USER.class, registeredCourse.getTrainer());
 		 businessProcess.setVariable("trainer", trainerToBeNotified.getEmail_());
 		 log.info("Mail: "+trainerToBeNotified.getEmail_());
 		
 		businessProcess.completeTask();
 	}
 	
 	public void rejectCourse(String taskId) {
 		businessProcess.startTask(taskId);
 		businessProcess.setVariable("approvalAction", "back");
		businessProcess.setVariable("deletedCourseName", ((Course) businessProcess.getVariable("courseToApprove")).getName());
 		businessProcess.setVariable("approved", false);
 		businessProcess.completeTask();
 	}
 	
 	
 	public String logout() {
 		Course registeredCourse = businessProcess.getVariable("courseToApprove");
 		businessProcess.getTask().setAssignee(registeredCourse.getTrainer());
 		log.info("setting task assigne for: "+ businessProcess.getTask().getName() + " to " +  registeredCourse.getTrainer());
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
