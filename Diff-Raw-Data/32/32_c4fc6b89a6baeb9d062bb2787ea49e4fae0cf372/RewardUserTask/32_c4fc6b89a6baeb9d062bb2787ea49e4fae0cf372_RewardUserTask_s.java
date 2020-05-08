 package wfm.task;
 
 import javax.ejb.Stateful;
 import javax.enterprise.context.ConversationScoped;
 import javax.inject.Named;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.activiti.engine.delegate.DelegateExecution;
 import org.activiti.engine.delegate.JavaDelegate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import wfm.db.ACT_ID_USER;
 import wfm.db.Course;
 
 @Stateful
 @Named
 @ConversationScoped
 public class RewardUserTask implements JavaDelegate {
 	
 	public static Logger log = LoggerFactory.getLogger(RewardUserTask.class);
 	
 	@PersistenceContext
 	private EntityManager entityManager = LoginTask.entityManager; // this is just - bad :( any idea how i can get hold of an instance of the entity manager -> spring injection does not seem to work
 	
 	@Override
 	public void execute(DelegateExecution execution) throws Exception {
 		//getting instances of the current course and member via process variables
 		Course course = (Course) execution.getVariable("courseToApprove");
 		ACT_ID_USER user = (ACT_ID_USER) execution.getVariable("userToApprove");	
 		//setting userRewardPoints to zero in case user has not gathered poins so far
 		Integer userRewardPoints = user.getRewardPoints();
 		// db contains null values in the beginning... so we need to convert them
 		if (userRewardPoints==null) userRewardPoints=0;
 		
 		// adding reward points according to course type
		user.setRewardPoints(userRewardPoints += course.getRewardPoints());
		log.info("entity belongs to current persistence context " + entityManager.contains(user));
 		try {
	    entityManager.merge(user);
	    entityManager.flush();	
 		}
 		catch (Exception ex) {
 			log.error("Persisting user failed! " + ex.getMessage());
 		}
 		log.info(user.getId_() + " already has collected " + userRewardPoints + " points!!! - WOW " );		
 	}
 
 }
