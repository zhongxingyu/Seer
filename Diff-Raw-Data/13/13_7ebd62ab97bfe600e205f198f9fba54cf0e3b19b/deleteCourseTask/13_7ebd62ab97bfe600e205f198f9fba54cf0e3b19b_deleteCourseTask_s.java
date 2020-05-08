 package wfm.task;
 
import java.util.ArrayList;
import java.util.List;


 import javax.ejb.Stateful;
 import javax.enterprise.context.ConversationScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.activiti.cdi.BusinessProcess;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import wfm.bean.User;
 import wfm.db.ACT_ID_USER;
 import wfm.db.Course;
import javax.persistence.Query;
 
 @Stateful
 @Named
 @ConversationScoped
 public class deleteCourseTask {
 	
 	private static final Logger log = LoggerFactory.getLogger(LoginTask.class);
 
 	@Inject
 	private BusinessProcess businessProcess;
 
 	@Inject
 	private Course course;
 	
 	@Inject
 	private User user;
 
 	@PersistenceContext
 	private EntityManager entityManager;
 	
 	private String courseName;
 	
 	//private List<String> userMails = new ArrayList<String>();
 	//private List<String> courseNames = new ArrayList<String>();
 	//private List<String> userNames = new ArrayList<String>();
 
 	public void deleteCourse(String taskId, int id) {
 		businessProcess.startTask(taskId);
 		/*
 		//get all mails from subscribed users
 		Query q = entityManager
 				.createNativeQuery("SELECT EMAIL_ FROM USER_COURSE uc, ACT_ID_USER u WHERE uc.USER_ID = u.ID_ AND uc.COURSE_NR ='"+id+"'");
 		
 		@SuppressWarnings("unchecked")
 		List<String> users = q.getResultList();		
 		for(String mail: users){
 			System.out.println("Email of subscribed person: "+mail);
 			userMails.add(mail);
 		}
 		//get all Names from subscribed users
 		Query qq = entityManager
 				.createNativeQuery("SELECT ID_ FROM USER_COURSE uc, ACT_ID_USER u WHERE uc.USER_ID = u.ID_ AND uc.COURSE_NR ='"+id+"'");
 		
 		@SuppressWarnings("unchecked")
 		List<String> uNames = qq.getResultList();		
 		for(String u: uNames){
 			System.out.println("User name of subscribed person: "+u);
 			userNames.add(u);
 		}
 		//get all affected course Names		
 		Query qqq = entityManager
 				.createNativeQuery("SELECT c.NAME  FROM USER_COURSE uc, ACT_ID_USER u, COURSE c WHERE uc.USER_ID = u.ID_ AND uc.COURSE_NR = '"+id+"' AND uc.COURSE_NR = c.COURSE_NR");
 		
 		@SuppressWarnings("unchecked")
 		List<String> cNames = qqq.getResultList();		
 		for(String c: cNames){
 			System.out.println("Course name to be deleted: "+c);
 			courseNames.add(c);
 		}
 		
 		//nice would be one query:
 		//ex: SELECT EMAIL_, ID_, c.NAME  FROM USER_COURSE uc, ACT_ID_USER u, COURSE c WHERE uc.USER_ID = u.ID_ AND uc.COURSE_NR = 75 AND uc.COURSE_NR = c.COURSE_NR
 		//but how to access each own column?
 		*/
 		log.info("User: " + user.getUsername());
 		
 		// delete from database:
 				
 		try{
 				log.info("Searching course with Id: "+id);
 				course = entityManager.find(Course.class, id);
 				
 				log.info("Course found: " + course.toString());
 				
 				courseName = course.getName();
 								
 				
 				//removing the course from users
 				for (ACT_ID_USER u : course.getUsers()) {
 					u.getCourses().remove(course);
 				}
 				//removing all users from the collection of users
 				course.getUsers().clear();
 				
 			    entityManager.remove(course);
 			   
 			    entityManager.flush();		  
 			    
 			    log.info("Course deleted successfully...");
 			
 		}catch(Exception e){
 			log.error(e.getMessage());
 		}
 		businessProcess.setVariable("deletedCourseName", courseName);
 		//businessProcess.setVariable("deleteProcess", userMails);
 		//businessProcess.setVariable("courseName", courseNames);
 		//businessProcess.setVariable("userName", userNames);		
 		businessProcess.completeTask();
 	}
 
 }
